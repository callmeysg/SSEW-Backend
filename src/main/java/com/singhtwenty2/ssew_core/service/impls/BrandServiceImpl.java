package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.data.entity.Brand;
import com.singhtwenty2.ssew_core.data.entity.Category;
import com.singhtwenty2.ssew_core.data.repository.BrandRepository;
import com.singhtwenty2.ssew_core.data.repository.CategoryRepository;
import com.singhtwenty2.ssew_core.service.BrandService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.singhtwenty2.ssew_core.data.dto.catalog_management.BrandDTO.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public BrandResponse createBrand(CreateBrandRequest createBrandRequest) {
        log.debug("Creating brand with name: {}", createBrandRequest.getName());

        validateCreateBrandRequest(createBrandRequest);

        Category category = findCategoryById(createBrandRequest.getCategory_id());

        String slug = generateSlug(createBrandRequest.getName());
        validateUniqueSlug(slug);
        validateUniqueName(createBrandRequest.getName());

        Integer displayOrder = createBrandRequest.getDisplay_order() != null
                ? createBrandRequest.getDisplay_order()
                : getNextDisplayOrder(category);

        Brand brand = createBrandFromRequest(createBrandRequest, category, slug, displayOrder);
        Brand savedBrand = brandRepository.save(brand);

        log.info("Brand created successfully with ID: {}", savedBrand.getId());

        return buildBrandResponse(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(String brandId) {
        log.debug("Fetching brand by ID: {}", brandId);

        Brand brand = findBrandById(brandId);
        return buildBrandResponse(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandBySlug(String slug) {
        log.debug("Fetching brand by slug: {}", slug);

        Optional<Brand> brandOptional = brandRepository.findBySlug(slug);
        if (brandOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with slug: " + slug);
        }

        return buildBrandResponse(brandOptional.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> getAllBrands(Pageable pageable) {
        log.debug("Fetching all brands with pagination");

        Page<Brand> brands = brandRepository.findAll(pageable);
        return brands.map(this::buildBrandResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getActiveBrands() {
        log.debug("Fetching all active brands");

        List<Brand> brands = brandRepository.findByIsActiveTrue();
        return brands.stream()
                .map(this::buildBrandResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> getActiveBrands(Pageable pageable) {
        log.debug("Fetching active brands with pagination");

        Page<Brand> brands = brandRepository.findByIsActiveTrue(pageable);
        return brands.map(this::buildBrandResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> getBrandsByCategory(String categoryId, Pageable pageable) {
        log.debug("Fetching brands by category ID: {}", categoryId);

        Category category = findCategoryById(categoryId);
        Page<Brand> brands = brandRepository.findByCategory(category, pageable);
        return brands.map(this::buildBrandResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> getActiveBrandsByCategory(String categoryId, Pageable pageable) {
        log.debug("Fetching active brands by category ID: {}", categoryId);

        Category category = findCategoryById(categoryId);
        Page<Brand> brands = brandRepository.findByCategoryAndIsActiveTrue(category, pageable);
        return brands.map(this::buildBrandResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> searchBrands(String name, String categoryId, Boolean isActive, Pageable pageable) {
        log.debug("Searching brands with filters - name: {}, categoryId: {}, isActive: {}", name, categoryId, isActive);

        UUID categoryUuid = null;
        if (StringUtils.hasText(categoryId)) {
            try {
                categoryUuid = UUID.fromString(categoryId);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID format");
            }
        }

        Page<Brand> brands = brandRepository.findBrandsWithFilters(name, categoryUuid, isActive, pageable);
        return brands.map(this::buildBrandResponse);
    }

    @Override
    public BrandResponse updateBrand(String brandId, UpdateBrandRequest updateBrandRequest) {
        log.debug("Updating brand with ID: {}", brandId);

        Brand existingBrand = findBrandById(brandId);

        updateBrandFields(existingBrand, updateBrandRequest);

        Brand updatedBrand = brandRepository.save(existingBrand);

        log.info("Brand updated successfully with ID: {}", updatedBrand.getId());

        return buildBrandResponse(updatedBrand);
    }

    @Override
    public void deleteBrand(String brandId) {
        log.debug("Deleting brand with ID: {}", brandId);

        Brand brand = findBrandById(brandId);

        Long productCount = brandRepository.countProductsByBrandId(brand.getId());
        if (productCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete brand. It has " + productCount + " associated products");
        }

        brandRepository.delete(brand);

        log.info("Brand deleted successfully with ID: {}", brandId);
    }

    @Override
    public void toggleBrandStatus(String brandId) {
        log.debug("Toggling brand status for ID: {}", brandId);

        Brand brand = findBrandById(brandId);
        brand.setIsActive(!brand.getIsActive());
        brand.setUpdatedAt(LocalDateTime.now());

        brandRepository.save(brand);

        log.info("Brand status toggled successfully for ID: {}, new status: {}", brandId, brand.getIsActive());
    }

    @Override
    public List<BrandResponse> getBrandsByCategoryOrderByDisplayOrder(String categoryId) {
        log.debug("Fetching brands by category ordered by display order: {}", categoryId);

        Category category = findCategoryById(categoryId);
        List<Brand> brands = brandRepository.findByCategoryOrderByDisplayOrderAsc(category);

        return brands.stream()
                .map(this::buildBrandResponse)
                .collect(Collectors.toList());
    }

    private void validateCreateBrandRequest(CreateBrandRequest request) {
        if (!StringUtils.hasText(request.getName()) || request.getName().trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand name must be at least 2 characters long");
        }

        if (!StringUtils.hasText(request.getCategory_id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID is required");
        }
    }

    private Category findCategoryById(String categoryId) {
        try {
            UUID categoryUuid = UUID.fromString(categoryId);
            Optional<Category> categoryOptional = categoryRepository.findById(categoryUuid);

            if (categoryOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + categoryId);
            }

            return categoryOptional.get();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID format");
        }
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }

    private void validateUniqueSlug(String slug) {
        if (brandRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand with this slug already exists");
        }
    }

    private void validateUniqueName(String name) {
        if (brandRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand with this name already exists");
        }
    }

    private Integer getNextDisplayOrder(Category category) {
        Integer maxOrder = brandRepository.findMaxDisplayOrderByCategory(category);
        return maxOrder != null ? maxOrder + 1 : 1;
    }

    private Brand createBrandFromRequest(
            CreateBrandRequest request,
            Category category,
            String slug,
            Integer displayOrder) {
        Brand brand = new Brand();
        brand.setName(request.getName().trim());
        brand.setSlug(slug);
        brand.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        brand.setLogoUrl(StringUtils.hasText(request.getLogo_url()) ? request.getLogo_url().trim() : null);
        brand.setWebsiteUrl(StringUtils.hasText(request.getWebsite_url()) ? request.getWebsite_url().trim() : null);
        brand.setDisplayOrder(displayOrder);
        brand.setIsActive(true);
        brand.setCategory(category);
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());

        return brand;
    }

    private BrandResponse buildBrandResponse(Brand brand) {
        Long productCount = brandRepository.countProductsByBrandId(brand.getId());

        return BrandResponse.builder()
                .brand_id(brand.getId().toString())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logo_url(brand.getLogoUrl())
                .website_url(brand.getWebsiteUrl())
                .display_order(brand.getDisplayOrder())
                .is_active(brand.getIsActive())
                .created_at(brand.getCreatedAt().toString())
                .updated_at(brand.getUpdatedAt().toString())
                .category_id(brand.getCategory().getId().toString())
                .category_name(brand.getCategory().getName())
                .product_count(productCount)
                .build();
    }

    private Brand findBrandById(String brandId) {
        try {
            UUID brandUuid = UUID.fromString(brandId);
            Optional<Brand> brandOptional = brandRepository.findById(brandUuid);

            if (brandOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found with ID: " + brandId);
            }

            return brandOptional.get();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid brand ID format");
        }
    }

    private void updateBrandFields(Brand brand, UpdateBrandRequest request) {
        boolean updated = false;

        if (StringUtils.hasText(request.getName()) && !request.getName().equals(brand.getName())) {
            validateUniqueNameForUpdate(request.getName(), brand.getId());
            brand.setName(request.getName().trim());

            String newSlug = generateSlug(request.getName());
            if (!newSlug.equals(brand.getSlug())) {
                validateUniqueSlugForUpdate(newSlug, brand.getId());
                brand.setSlug(newSlug);
            }
            updated = true;
        }

        if (request.getDescription() != null) {
            brand.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
            updated = true;
        }

        if (request.getLogo_url() != null) {
            brand.setLogoUrl(StringUtils.hasText(request.getLogo_url()) ? request.getLogo_url().trim() : null);
            updated = true;
        }

        if (request.getWebsite_url() != null) {
            brand.setWebsiteUrl(StringUtils.hasText(request.getWebsite_url()) ? request.getWebsite_url().trim() : null);
            updated = true;
        }

        if (request.getDisplay_order() != null && !request.getDisplay_order().equals(brand.getDisplayOrder())) {
            brand.setDisplayOrder(request.getDisplay_order());
            updated = true;
        }

        if (request.getIs_active() != null && !request.getIs_active().equals(brand.getIsActive())) {
            brand.setIsActive(request.getIs_active());
            updated = true;
        }

        if (StringUtils.hasText(request.getCategory_id()) &&
            !request.getCategory_id().equals(brand.getCategory().getId().toString())) {
            Category newCategory = findCategoryById(request.getCategory_id());
            brand.setCategory(newCategory);
            updated = true;
        }

        if (updated) {
            brand.setUpdatedAt(LocalDateTime.now());
        }
    }

    private void validateUniqueNameForUpdate(String name, UUID brandId) {
        if (brandRepository.existsByNameAndIdNot(name, brandId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand with this name already exists");
        }
    }

    private void validateUniqueSlugForUpdate(String slug, UUID brandId) {
        if (brandRepository.existsBySlugAndIdNot(slug, brandId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand with this slug already exists");
        }
    }
}
