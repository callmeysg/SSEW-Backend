package com.singhtwenty2.commerce_service.service.catalogue.helper;

import com.singhtwenty2.commerce_service.data.entity.Product;
import com.singhtwenty2.commerce_service.data.enums.VariantType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.singhtwenty2.commerce_service.data.dto.catalogue.ProductDTO.ProductSearchFilters;

@Service
public class ProductSearchService {

    public Specification<Product> buildProductSpecification(ProductSearchFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (filters.getKeyword() != null && !filters.getKeyword().trim().isEmpty()) {
                String keyword = "%" + filters.getKeyword().toLowerCase() + "%";
                jakarta.persistence.criteria.Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), keyword);
                jakarta.persistence.criteria.Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword);
                jakarta.persistence.criteria.Predicate skuPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sku")), keyword);

                predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate, skuPredicate));
            }

            if (filters.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("manufacturer").join("categories").get("id"), UUID.fromString(filters.getCategoryId())));
            }

            if (filters.getManufacturerId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("manufacturer").get("id"), UUID.fromString(filters.getManufacturerId())));
            }

            if (filters.getCompatibilityBrandIds() != null && !filters.getCompatibilityBrandIds().isEmpty()) {
                List<UUID> compatibilityBrandUuids = filters.getCompatibilityBrandIds().stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList());
                predicates.add(root.join("compatibilityBrands").get("id").in(compatibilityBrandUuids));
            }

            if (filters.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filters.getMinPrice()));
            }

            if (filters.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filters.getMaxPrice()));
            }

            if (filters.getIsFeatured() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFeatured"), filters.getIsFeatured()));
            }

            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public Specification<Product> buildProductSpecificationExcludingVariants(ProductSearchFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.notEqual(root.get("variantType"), VariantType.VARIANT));

            if (filters.getKeyword() != null && !filters.getKeyword().trim().isEmpty()) {
                String keyword = "%" + filters.getKeyword().toLowerCase() + "%";
                jakarta.persistence.criteria.Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), keyword);
                jakarta.persistence.criteria.Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword);
                jakarta.persistence.criteria.Predicate skuPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sku")), keyword);

                predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate, skuPredicate));
            }

            if (filters.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("manufacturer").join("categories").get("id"), UUID.fromString(filters.getCategoryId())));
            }

            if (filters.getManufacturerId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("manufacturer").get("id"), UUID.fromString(filters.getManufacturerId())));
            }

            if (filters.getCompatibilityBrandIds() != null && !filters.getCompatibilityBrandIds().isEmpty()) {
                List<UUID> compatibilityBrandUuids = filters.getCompatibilityBrandIds().stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList());
                predicates.add(root.join("compatibilityBrands").get("id").in(compatibilityBrandUuids));
            }

            if (filters.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), filters.getMinPrice()));
            }

            if (filters.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), filters.getMaxPrice()));
            }

            if (filters.getIsFeatured() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFeatured"), filters.getIsFeatured()));
            }

            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}