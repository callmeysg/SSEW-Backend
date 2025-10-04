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

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "manufacturers",
        indexes = {
                @Index(name = "idx_manufacturer_name", columnList = "name"),
                @Index(name = "idx_manufacturer_slug", columnList = "slug", unique = true),
                @Index(name = "idx_manufacturer_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Manufacturer extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "logo_object_key", length = 500)
    private String logoObjectKey;

    @Column(name = "logo_file_size")
    private Long logoFileSize;

    @Column(name = "logo_content_type", length = 50)
    private String logoContentType;

    @Column(name = "logo_width")
    private Integer logoWidth;

    @Column(name = "logo_height")
    private Integer logoHeight;

    @Column(name = "website_url", length = 300)
    private String websiteUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "manufacturer_categories",
            joinColumns = @JoinColumn(name = "manufacturer_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"),
            indexes = {
                    @Index(name = "idx_manufacturer_category_manufacturer", columnList = "manufacturer_id"),
                    @Index(name = "idx_manufacturer_category_category", columnList = "category_id")
            }
    )
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public void addCategory(Category category) {
        if (!categories.contains(category)) {
            categories.add(category);
            category.getManufacturers().add(this);
        }
    }

    public void removeCategory(Category category) {
        categories.remove(category);
        category.getManufacturers().remove(this);
    }

    public void addProduct(Product product) {
        products.add(product);
        product.setManufacturer(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setManufacturer(null);
    }
}