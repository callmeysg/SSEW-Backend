package com.singhtwenty2.ssew_core.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "brands",
        indexes = {
                @Index(name = "idx_brand_name", columnList = "name"),
                @Index(name = "idx_brand_slug", columnList = "slug", unique = true),
                @Index(name = "idx_brand_category", columnList = "category_id"),
                @Index(name = "idx_brand_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Brand extends BaseEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        products.add(product);
        product.setBrand(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setBrand(null);
    }
}