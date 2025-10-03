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
        name = "compatibility_brands",
        indexes = {
                @Index(name = "idx_compatibility_brand_name", columnList = "name"),
                @Index(name = "idx_compatibility_brand_slug", columnList = "slug", unique = true),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompatibilityBrand extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @ManyToMany(mappedBy = "compatibilityBrands", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
}