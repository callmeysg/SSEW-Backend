package com.singhtwenty2.commerce_service.service.impls;

import com.singhtwenty2.commerce_service.data.entity.CompatibilityBrand;
import com.singhtwenty2.commerce_service.data.repository.CompatibilityBrandRepository;
import com.singhtwenty2.commerce_service.service.catalogue.CompatibilityBrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.singhtwenty2.commerce_service.data.dto.catalogue.CompatibilityBrandDTO.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CompatibilityBrandServiceImpl implements CompatibilityBrandService {

    private final CompatibilityBrandRepository compatibilityBrandRepository;

    @Override
    public CompatibilityBrandResponse createCompatibilityBrand(CreateCompatibilityBrandRequest createRequest) {
        log.debug("Creating new compatibility brand with name: {}", createRequest.getName());

        validateCreateRequest(createRequest);

        String slug = generateSlug(createRequest.getName());
        ensureUniqueSlug(slug, null);

        CompatibilityBrand savedCompatibilityBrand = saveCompatibilityBrandData(createRequest, slug);

        log.info("Compatibility brand created successfully with ID: {}", savedCompatibilityBrand.getId());
        return buildCompatibilityBrandResponse(savedCompatibilityBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public CompatibilityBrandResponse getCompatibilityBrandById(String compatibilityBrandId) {
        log.debug("Fetching compatibility brand by ID: {}", compatibilityBrandId);

        CompatibilityBrand compatibilityBrand = findCompatibilityBrandById(compatibilityBrandId);
        return buildCompatibilityBrandResponse(compatibilityBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public CompatibilityBrandResponse getCompatibilityBrandBySlug(String slug) {
        log.debug("Fetching compatibility brand by slug: {}", slug);

        if (!StringUtils.hasText(slug)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug is required");
        }

        Optional<CompatibilityBrand> compatibilityBrandOptional = compatibilityBrandRepository.findBySlug(slug);
        if (compatibilityBrandOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compatibility brand not found with slug: " + slug);
        }

        return buildCompatibilityBrandResponse(compatibilityBrandOptional.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompatibilityBrandResponse> getAllCompatibilityBrands(Pageable pageable) {
        log.debug("Fetching all compatibility brands with pagination");

        Page<CompatibilityBrand> compatibilityBrandPage = compatibilityBrandRepository.findAll(pageable);
        return compatibilityBrandPage.map(this::buildCompatibilityBrandResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompatibilityBrandResponse> searchCompatibilityBrands(String searchTerm, Pageable pageable) {
        log.debug("Searching compatibility brands with term: {}", searchTerm);

        Page<CompatibilityBrand> compatibilityBrandPage = compatibilityBrandRepository.findCompatibilityBrandsWithFilters(
                StringUtils.hasText(searchTerm) ? searchTerm.trim() : null,
                pageable
        );
        return compatibilityBrandPage.map(this::buildCompatibilityBrandResponse);
    }

    @Override
    public CompatibilityBrandResponse updateCompatibilityBrand(String compatibilityBrandId, UpdateCompatibilityBrandRequest updateRequest) {
        log.debug("Updating compatibility brand with ID: {}", compatibilityBrandId);

        CompatibilityBrand compatibilityBrand = findCompatibilityBrandById(compatibilityBrandId);
        validateUpdateRequest(updateRequest, compatibilityBrand);

        boolean needsNewSlug = false;
        if (StringUtils.hasText(updateRequest.getName()) &&
            !updateRequest.getName().trim().equals(compatibilityBrand.getName())) {
            compatibilityBrand.setName(updateRequest.getName().trim());
            needsNewSlug = true;
        }

        if (needsNewSlug) {
            String newSlug = generateSlug(compatibilityBrand.getName());
            ensureUniqueSlug(newSlug, compatibilityBrand.getId());
            compatibilityBrand.setSlug(newSlug);
        }

        compatibilityBrand.setUpdatedAt(LocalDateTime.now());

        CompatibilityBrand updatedCompatibilityBrand = compatibilityBrandRepository.save(compatibilityBrand);

        log.info("Compatibility brand updated successfully with ID: {}", updatedCompatibilityBrand.getId());
        return buildCompatibilityBrandResponse(updatedCompatibilityBrand);
    }

    @Override
    public void deleteCompatibilityBrand(String compatibilityBrandId) {
        log.debug("Deleting compatibility brand with ID: {}", compatibilityBrandId);

        CompatibilityBrand compatibilityBrand = findCompatibilityBrandById(compatibilityBrandId);

        Long productCount = compatibilityBrandRepository.countProductsByCompatibilityBrandId(compatibilityBrand.getId());
        if (productCount > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete compatibility brand. It has " + productCount + " associated products"
            );
        }

        compatibilityBrandRepository.delete(compatibilityBrand);

        log.info("Compatibility brand deleted successfully with ID: {}", compatibilityBrandId);
    }

    private void validateCreateRequest(CreateCompatibilityBrandRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Compatibility brand name is required");
        }

        String trimmedName = request.getName().trim();
        if (trimmedName.length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Compatibility brand name must be at least 2 characters long");
        }

        if (compatibilityBrandRepository.existsByName(trimmedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Compatibility brand with this name already exists");
        }
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private void ensureUniqueSlug(String baseSlug, UUID excludeCompatibilityBrandId) {
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            boolean exists = excludeCompatibilityBrandId != null ?
                    compatibilityBrandRepository.existsBySlugAndIdNot(slug, excludeCompatibilityBrandId) :
                    compatibilityBrandRepository.existsBySlug(slug);

            if (!exists) {
                break;
            }

            slug = baseSlug + "-" + counter;
            counter++;
        }
    }

    private CompatibilityBrandResponse buildCompatibilityBrandResponse(CompatibilityBrand compatibilityBrand) {
        Long productCount = compatibilityBrandRepository.countProductsByCompatibilityBrandId(compatibilityBrand.getId());

        return CompatibilityBrandResponse.builder()
                .compatibilityBrandId(compatibilityBrand.getId().toString())
                .name(compatibilityBrand.getName())
                .slug(compatibilityBrand.getSlug())
                .createdAt(compatibilityBrand.getCreatedAt().toString())
                .updatedAt(compatibilityBrand.getUpdatedAt().toString())
                .productCount(productCount)
                .build();
    }

    private CompatibilityBrand saveCompatibilityBrandData(CreateCompatibilityBrandRequest createRequest, String slug) {
        CompatibilityBrand compatibilityBrand = new CompatibilityBrand();
        compatibilityBrand.setName(createRequest.getName().trim());
        compatibilityBrand.setSlug(slug);
        compatibilityBrand.setCreatedAt(LocalDateTime.now());
        compatibilityBrand.setUpdatedAt(LocalDateTime.now());

        return compatibilityBrandRepository.save(compatibilityBrand);
    }

    private CompatibilityBrand findCompatibilityBrandById(String compatibilityBrandId) {
        try {
            UUID uuid = UUID.fromString(compatibilityBrandId);
            Optional<CompatibilityBrand> compatibilityBrandOptional = compatibilityBrandRepository.findById(uuid);

            if (compatibilityBrandOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compatibility brand not found with ID: " + compatibilityBrandId);
            }

            return compatibilityBrandOptional.get();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid compatibility brand ID format");
        }
    }

    private void validateUpdateRequest(UpdateCompatibilityBrandRequest request, CompatibilityBrand existingCompatibilityBrand) {
        if (StringUtils.hasText(request.getName())) {
            String trimmedName = request.getName().trim();
            if (trimmedName.length() < 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Compatibility brand name must be at least 2 characters long");
            }

            if (compatibilityBrandRepository.existsByNameAndIdNot(trimmedName, existingCompatibilityBrand.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Compatibility brand with this name already exists");
            }
        }
    }
}