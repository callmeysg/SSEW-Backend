package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.data.entity.Category;
import com.singhtwenty2.ssew_core.data.entity.Manufacturer;
import com.singhtwenty2.ssew_core.data.repository.CategoryRepository;
import com.singhtwenty2.ssew_core.data.repository.ManufacturerRepository;
import com.singhtwenty2.ssew_core.service.catalogue.ManufacturerImageService;
import com.singhtwenty2.ssew_core.service.catalogue.ManufacturerService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.singhtwenty2.ssew_core.data.dto.catalogue.ManufacturerDTO.*;
import static com.singhtwenty2.ssew_core.data.dto.catalogue.ManufacturerImage.ManufacturerImageResult;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final CategoryRepository categoryRepository;
    private final ManufacturerImageService manufacturerImageService;

    @Override
    public ManufacturerResponse createManufacturer(CreateManufacturerRequest createManufacturerRequest) {
        log.debug("Creating manufacturer with name: {}", createManufacturerRequest.getName());

        validateCreateManufacturerRequest(createManufacturerRequest);

        List<Category> categories = findCategoriesByIds(createManufacturerRequest.getCategoryIds());

        String slug = generateSlug(createManufacturerRequest.getName());
        validateUniqueSlug(slug);
        validateUniqueName(createManufacturerRequest.getName());

        Integer displayOrder = createManufacturerRequest.getDisplayOrder() != null
                ? createManufacturerRequest.getDisplayOrder()
                : getNextDisplayOrder();

        Manufacturer manufacturer = createManufacturerFromRequest(createManufacturerRequest, categories, slug, displayOrder);

        ManufacturerImageResult logoResult = null;
        if (createManufacturerRequest.getLogoFile() != null && !createManufacturerRequest.getLogoFile().isEmpty()) {
            logoResult = manufacturerImageService.processManufacturerLogo(createManufacturerRequest.getLogoFile(), manufacturer.getSlug());

            if (logoResult.isTaskExecuted()) {
                manufacturer.setLogoObjectKey(logoResult.getObjectKey());
                manufacturer.setLogoFileSize(logoResult.getFileSize());
                manufacturer.setLogoContentType(logoResult.getContentType());
                manufacturer.setLogoWidth(logoResult.getWidth());
                manufacturer.setLogoHeight(logoResult.getHeight());
            } else {
                log.warn("Failed to process manufacturer logo: {}", logoResult.getErrorMessage());
            }
        }

        Manufacturer savedManufacturer = manufacturerRepository.save(manufacturer);

        log.info("Manufacturer created successfully with ID: {}", savedManufacturer.getId());

        return buildManufacturerResponse(savedManufacturer);
    }

    @Override
    @Transactional(readOnly = true)
    public ManufacturerResponse getManufacturerById(String manufacturerId) {
        log.debug("Fetching manufacturer by ID: {}", manufacturerId);

        Manufacturer manufacturer = findManufacturerById(manufacturerId);
        return buildManufacturerResponse(manufacturer);
    }

    @Override
    @Transactional(readOnly = true)
    public ManufacturerResponse getManufacturerBySlug(String slug) {
        log.debug("Fetching manufacturer by slug: {}", slug);

        Optional<Manufacturer> manufacturerOptional = manufacturerRepository.findBySlug(slug);
        if (manufacturerOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Manufacturer not found with slug: " + slug);
        }

        return buildManufacturerResponse(manufacturerOptional.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManufacturerResponse> getAllManufacturers(Pageable pageable) {
        log.debug("Fetching all manufacturers with pagination");

        Page<Manufacturer> manufacturers = manufacturerRepository.findAll(pageable);
        return manufacturers.map(this::buildManufacturerResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturerResponse> getActiveManufacturers() {
        log.debug("Fetching all active manufacturers");

        List<Manufacturer> manufacturers = manufacturerRepository.findByIsActiveTrue();
        return manufacturers.stream()
                .map(this::buildManufacturerResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManufacturerResponse> getActiveManufacturers(Pageable pageable) {
        log.debug("Fetching active manufacturers with pagination");

        Page<Manufacturer> manufacturers = manufacturerRepository.findByIsActiveTrue(pageable);
        return manufacturers.map(this::buildManufacturerResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManufacturerResponse> getManufacturersByCategories(List<String> categoryIds, Pageable pageable) {
        log.debug("Fetching manufacturers by category IDs: {}", categoryIds);

        List<Category> categories = findCategoriesByIds(categoryIds);
        Page<Manufacturer> manufacturers = manufacturerRepository.findByCategoriesIn(categories, pageable);
        return manufacturers.map(this::buildManufacturerResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManufacturerResponse> getActiveManufacturersByCategories(List<String> categoryIds, Pageable pageable) {
        log.debug("Fetching active manufacturers by category IDs: {}", categoryIds);

        List<Category> categories = findCategoriesByIds(categoryIds);
        Page<Manufacturer> manufacturers = manufacturerRepository.findByCategoriesInAndIsActiveTrue(categories, pageable);
        return manufacturers.map(this::buildManufacturerResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ManufacturerResponse> searchManufacturers(String name, String categoryId, Boolean isActive, Pageable pageable) {
        log.debug("Searching manufacturers with filters - name: {}, categoryId: {}, isActive: {}", name, categoryId, isActive);

        UUID categoryUuid = null;
        if (StringUtils.hasText(categoryId)) {
            try {
                categoryUuid = UUID.fromString(categoryId);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID format");
            }
        }

        Page<Manufacturer> manufacturers = manufacturerRepository.findManufacturersWithFilters(name, categoryUuid, isActive, pageable);
        return manufacturers.map(this::buildManufacturerResponse);
    }

    @Override
    public ManufacturerResponse updateManufacturer(String manufacturerId, UpdateManufacturerRequest updateManufacturerRequest) {
        log.debug("Updating manufacturer with ID: {}", manufacturerId);

        Manufacturer existingManufacturer = findManufacturerById(manufacturerId);

        updateManufacturerFields(existingManufacturer, updateManufacturerRequest);

        handleLogoUpdate(existingManufacturer, updateManufacturerRequest);

        Manufacturer updatedManufacturer = manufacturerRepository.save(existingManufacturer);

        log.info("Manufacturer updated successfully with ID: {}", updatedManufacturer.getId());

        return buildManufacturerResponse(updatedManufacturer);
    }

    @Override
    public void deleteManufacturer(String manufacturerId) {
        log.debug("Deleting manufacturer with ID: {}", manufacturerId);

        Manufacturer manufacturer = findManufacturerById(manufacturerId);

        Long productCount = manufacturerRepository.countProductsByManufacturerId(manufacturer.getId());
        if (productCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete manufacturer. It has " + productCount + " associated products");
        }

        if (StringUtils.hasText(manufacturer.getLogoObjectKey())) {
            manufacturerImageService.deleteManufacturerLogo(manufacturer.getSlug(), manufacturer.getLogoObjectKey());
        }

        manufacturerRepository.delete(manufacturer);

        log.info("Manufacturer deleted successfully with ID: {}", manufacturerId);
    }

    @Override
    public void toggleManufacturerStatus(String manufacturerId) {
        log.debug("Toggling manufacturer status for ID: {}", manufacturerId);

        Manufacturer manufacturer = findManufacturerById(manufacturerId);
        manufacturer.setIsActive(!manufacturer.getIsActive());
        manufacturer.setUpdatedAt(LocalDateTime.now());

        manufacturerRepository.save(manufacturer);

        log.info("Manufacturer status toggled successfully for ID: {}, new status: {}", manufacturerId, manufacturer.getIsActive());
    }

    @Override
    public List<ManufacturerResponse> getManufacturersByCategoriesOrderByDisplayOrder(List<String> categoryIds) {
        log.debug("Fetching manufacturers by categories ordered by display order: {}", categoryIds);

        List<Category> categories = findCategoriesByIds(categoryIds);
        List<Manufacturer> manufacturers = manufacturerRepository.findByCategoriesInOrderByDisplayOrderAsc(categories);

        return manufacturers.stream()
                .map(this::buildManufacturerResponse)
                .collect(Collectors.toList());
    }

    private void handleLogoUpdate(Manufacturer manufacturer, UpdateManufacturerRequest request) {
        boolean logoUpdated = false;

        if (request.getRemoveLogo() != null && request.getRemoveLogo()) {
            if (StringUtils.hasText(manufacturer.getLogoObjectKey())) {
                manufacturerImageService.deleteManufacturerLogo(manufacturer.getSlug(), manufacturer.getLogoObjectKey());
                clearManufacturerLogoFields(manufacturer);
                logoUpdated = true;
            }
        }

        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            ManufacturerImageResult logoResult = manufacturerImageService.updateManufacturerLogo(
                    request.getLogoFile(),
                    manufacturer.getSlug(),
                    manufacturer.getLogoObjectKey()
            );

            if (logoResult.isTaskExecuted()) {
                manufacturer.setLogoObjectKey(logoResult.getObjectKey());
                manufacturer.setLogoFileSize(logoResult.getFileSize());
                manufacturer.setLogoContentType(logoResult.getContentType());
                manufacturer.setLogoWidth(logoResult.getWidth());
                manufacturer.setLogoHeight(logoResult.getHeight());
                logoUpdated = true;
            } else {
                log.warn("Failed to update manufacturer logo: {}", logoResult.getErrorMessage());
            }
        }

        if (logoUpdated) {
            manufacturer.setUpdatedAt(LocalDateTime.now());
        }
    }

    private void clearManufacturerLogoFields(Manufacturer manufacturer) {
        manufacturer.setLogoObjectKey(null);
        manufacturer.setLogoFileSize(null);
        manufacturer.setLogoContentType(null);
        manufacturer.setLogoWidth(null);
        manufacturer.setLogoHeight(null);
    }

    private void validateCreateManufacturerRequest(CreateManufacturerRequest request) {
        if (!StringUtils.hasText(request.getName()) || request.getName().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manufacturer name must be at least 2 characters long");
        }

        if (request.getCategoryIds() == null || request.getCategoryIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category IDs are required");
        }

        if (request.getLogoFile() != null && !request.getLogoFile().isEmpty()) {
            if (!manufacturerImageService.validateManufacturerLogoFile(request.getLogoFile())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid logo file");
            }
        }
    }

    private List<Category> findCategoriesByIds(List<String> categoryIds) {
        List<Category> categories = new ArrayList<>();
        for (String categoryId : categoryIds) {
            try {
                UUID categoryUuid = UUID.fromString(categoryId);
                Optional<Category> categoryOptional = categoryRepository.findById(categoryUuid);

                if (categoryOptional.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + categoryId);
                }

                categories.add(categoryOptional.get());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID format: " + categoryId);
            }
        }
        return categories;
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }

    private void validateUniqueSlug(String slug) {
        if (manufacturerRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Manufacturer with this slug already exists");
        }
    }

    private void validateUniqueName(String name) {
        if (manufacturerRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Manufacturer with this name already exists");
        }
    }

    private Integer getNextDisplayOrder() {
        Integer maxOrder = manufacturerRepository.findMaxDisplayOrder();
        return maxOrder != null ? maxOrder + 1 : 1;
    }

    private Manufacturer createManufacturerFromRequest(
            CreateManufacturerRequest request,
            List<Category> categories,
            String slug,
            Integer displayOrder) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName(request.getName().trim());
        manufacturer.setSlug(slug);
        manufacturer.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        manufacturer.setWebsiteUrl(StringUtils.hasText(request.getWebsiteUrl()) ? request.getWebsiteUrl().trim() : null);
        manufacturer.setDisplayOrder(displayOrder);
        manufacturer.setIsActive(true);
        manufacturer.setCategories(categories);
        manufacturer.setCreatedAt(LocalDateTime.now());
        manufacturer.setUpdatedAt(LocalDateTime.now());

        return manufacturer;
    }

    private ManufacturerResponse buildManufacturerResponse(Manufacturer manufacturer) {
        Long productCount = manufacturerRepository.countProductsByManufacturerId(manufacturer.getId());

        LogoInfo logoInfo = null;
        String logoAccessUrl;

        if (StringUtils.hasText(manufacturer.getLogoObjectKey())) {
            logoAccessUrl = manufacturerImageService.generateLogoAccessUrl(manufacturer.getLogoObjectKey(), 60);

            logoInfo = LogoInfo.builder()
                    .objectKey(manufacturer.getLogoObjectKey())
                    .accessUrl(logoAccessUrl)
                    .fileSize(manufacturer.getLogoFileSize() != null ? manufacturer.getLogoFileSize() : 0)
                    .contentType(manufacturer.getLogoContentType())
                    .width(manufacturer.getLogoWidth() != null ? manufacturer.getLogoWidth() : 0)
                    .height(manufacturer.getLogoHeight() != null ? manufacturer.getLogoHeight() : 0)
                    .build();
        }

        List<CategoryInfo> categoryInfos = manufacturer.getCategories().stream()
                .map(category -> CategoryInfo.builder()
                        .categoryId(category.getId().toString())
                        .categoryName(category.getName())
                        .categorySlug(category.getSlug())
                        .build())
                .collect(Collectors.toList());

        return ManufacturerResponse.builder()
                .manufacturerId(manufacturer.getId().toString())
                .name(manufacturer.getName())
                .slug(manufacturer.getSlug())
                .description(manufacturer.getDescription())
                .websiteUrl(manufacturer.getWebsiteUrl())
                .displayOrder(manufacturer.getDisplayOrder())
                .isActive(manufacturer.getIsActive())
                .createdAt(manufacturer.getCreatedAt().toString())
                .updatedAt(manufacturer.getUpdatedAt().toString())
                .categories(categoryInfos)
                .productCount(productCount)
                .logoInfo(logoInfo)
                .build();
    }

    private Manufacturer findManufacturerById(String manufacturerId) {
        try {
            UUID manufacturerUuid = UUID.fromString(manufacturerId);
            Optional<Manufacturer> manufacturerOptional = manufacturerRepository.findById(manufacturerUuid);

            if (manufacturerOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Manufacturer not found with ID: " + manufacturerId);
            }

            return manufacturerOptional.get();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid manufacturer ID format");
        }
    }

    private void updateManufacturerFields(Manufacturer manufacturer, UpdateManufacturerRequest request) {
        boolean updated = false;

        if (StringUtils.hasText(request.getName()) && !request.getName().equals(manufacturer.getName())) {
            validateUniqueNameForUpdate(request.getName(), manufacturer.getId());
            manufacturer.setName(request.getName().trim());

            String newSlug = generateSlug(request.getName());
            if (!newSlug.equals(manufacturer.getSlug())) {
                validateUniqueSlugForUpdate(newSlug, manufacturer.getId());
                manufacturer.setSlug(newSlug);
            }
            updated = true;
        }

        if (request.getDescription() != null) {
            manufacturer.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
            updated = true;
        }

        if (request.getWebsiteUrl() != null) {
            manufacturer.setWebsiteUrl(StringUtils.hasText(request.getWebsiteUrl()) ? request.getWebsiteUrl().trim() : null);
            updated = true;
        }

        if (request.getDisplayOrder() != null && !request.getDisplayOrder().equals(manufacturer.getDisplayOrder())) {
            manufacturer.setDisplayOrder(request.getDisplayOrder());
            updated = true;
        }

        if (request.getIsActive() != null && !request.getIsActive().equals(manufacturer.getIsActive())) {
            manufacturer.setIsActive(request.getIsActive());
            updated = true;
        }

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> newCategories = findCategoriesByIds(request.getCategoryIds());
            if (!manufacturer.getCategories().equals(newCategories)) {
                manufacturer.getCategories().clear();
                manufacturer.getCategories().addAll(newCategories);
                updated = true;
            }
        }

        if (updated) {
            manufacturer.setUpdatedAt(LocalDateTime.now());
        }
    }

    private void validateUniqueNameForUpdate(String name, UUID manufacturerId) {
        if (manufacturerRepository.existsByNameAndIdNot(name, manufacturerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Manufacturer with this name already exists");
        }
    }

    private void validateUniqueSlugForUpdate(String slug, UUID manufacturerId) {
        if (manufacturerRepository.existsBySlugAndIdNot(slug, manufacturerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Manufacturer with this slug already exists");
        }
    }
}