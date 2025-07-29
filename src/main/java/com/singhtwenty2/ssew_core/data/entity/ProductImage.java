package com.singhtwenty2.ssew_core.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_product_images_product", columnList = "product_id"),
                @Index(name = "idx_product_images_thumbnail", columnList = "is_thumbnail"),
                @Index(name = "idx_product_images_order", columnList = "display_order")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage extends BaseEntity {

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "alt_text", length = 200)
    private String altText;

    @Column(name = "is_thumbnail", nullable = false)
    private Boolean isThumbnail = false;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_format", length = 10)
    private String fileFormat;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}