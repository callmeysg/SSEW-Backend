/**
 * Copyright 2025 Aryan Singh
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
package com.singhtwenty2.commerce_service.data.entity;

import com.singhtwenty2.commerce_service.data.enums.VariantType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_sku", columnList = "sku", unique = true),
                @Index(name = "idx_product_model_number", columnList = "model_number", unique = true),
                @Index(name = "idx_product_slug", columnList = "slug", unique = true),
                @Index(name = "idx_product_manufacturer", columnList = "manufacturer_id"),
                @Index(name = "idx_product_active", columnList = "is_active"),
                @Index(name = "idx_product_parent", columnList = "parent_product_id"),
                @Index(name = "idx_product_variant_type", columnList = "variant_type"),
                @Index(name = "idx_product_price", columnList = "price"),
                @Index(name = "idx_product_created", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 250)
    private String slug;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "model_number", unique = true, length = 100)
    private String modelNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specifications", columnDefinition = "jsonb")
    private Map<String, String> specifications;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "meta_title", length = 150)
    private String metaTitle;

    @Column(name = "meta_description", length = 300)
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    @Column(name = "search_tags", length = 1000)
    private String searchTags;

    @Column(name = "thumbnail_object_key", length = 500)
    private String thumbnailObjectKey;

    @Column(name = "thumbnail_file_size")
    private Long thumbnailFileSize;

    @Column(name = "thumbnail_content_type", length = 50)
    private String thumbnailContentType;

    @Column(name = "thumbnail_width")
    private Integer thumbnailWidth;

    @Column(name = "thumbnail_height")
    private Integer thumbnailHeight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private Manufacturer manufacturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_product_id")
    private Product parentProduct;

    @OneToMany(mappedBy = "parentProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> variants = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "variant_type", length = 20)
    private VariantType variantType = VariantType.STANDALONE;

    @Column(name = "variant_position")
    private Integer variantPosition;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_compatibility_brands",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "compatibility_brand_id"),
            indexes = {
                    @Index(name = "idx_product_compatibility_product", columnList = "product_id"),
                    @Index(name = "idx_product_compatibility_brand", columnList = "compatibility_brand_id")
            }
    )
    private List<CompatibilityBrand> compatibilityBrands = new ArrayList<>();

    public void addVariant(Product variant) {
        variants.add(variant);
        variant.setParentProduct(this);
        variant.setVariantType(VariantType.VARIANT);
        variant.setVariantPosition(variants.size());

        if (this.variantType == VariantType.STANDALONE) {
            this.variantType = VariantType.PARENT;
        }
    }

    public void removeVariant(Product variant) {
        variants.remove(variant);
        variant.setParentProduct(null);
        variant.setVariantType(VariantType.STANDALONE);
        variant.setVariantPosition(null);

        if (variants.isEmpty() && this.variantType == VariantType.PARENT) {
            this.variantType = VariantType.STANDALONE;
        }

        reorderVariantPositions();
    }

    public void addProductImage(ProductImage productImage) {
        productImages.add(productImage);
        productImage.setProduct(this);
        productImage.setDisplayOrder(productImages.size());
    }

    public void removeProductImage(ProductImage productImage) {
        productImages.remove(productImage);
        productImage.setProduct(null);
        reorderImagePositions();
    }

    public void addCompatibilityBrand(CompatibilityBrand compatibilityBrand) {
        if (!compatibilityBrands.contains(compatibilityBrand)) {
            compatibilityBrands.add(compatibilityBrand);
            compatibilityBrand.getProducts().add(this);
        }
    }

    public void removeCompatibilityBrand(CompatibilityBrand compatibilityBrand) {
        compatibilityBrands.remove(compatibilityBrand);
        compatibilityBrand.getProducts().remove(this);
    }

    public void clearCompatibilityBrands() {
        for (CompatibilityBrand compatibilityBrand : new ArrayList<>(compatibilityBrands)) {
            removeCompatibilityBrand(compatibilityBrand);
        }
    }

    private void reorderVariantPositions() {
        for (int i = 0; i < variants.size(); i++) {
            variants.get(i).setVariantPosition(i + 1);
        }
    }

    private void reorderImagePositions() {
        for (int i = 0; i < productImages.size(); i++) {
            productImages.get(i).setDisplayOrder(i + 1);
        }
    }

    public boolean isVariant() {
        return variantType == VariantType.VARIANT;
    }

    public boolean hasVariants() {
        return variantType == VariantType.PARENT && !variants.isEmpty();
    }

    public boolean isStandalone() {
        return variantType == VariantType.STANDALONE;
    }

    public Product getRootProduct() {
        return parentProduct != null ? parentProduct : this;
    }

    public List<Product> getAllVariants() {
        return parentProduct != null ? parentProduct.getVariants() : this.getVariants();
    }

    public List<String> getCategoryIds() {
        return manufacturer != null ?
                manufacturer.getCategories().stream()
                        .map(category -> category.getId().toString())
                        .toList() :
                new ArrayList<>();
    }

    public List<String> getCategoryNames() {
        return manufacturer != null ?
                manufacturer.getCategories().stream()
                        .map(Category::getName)
                        .toList() :
                new ArrayList<>();
    }

    public String getManufacturerName() {
        return manufacturer != null ? manufacturer.getName() : null;
    }
}