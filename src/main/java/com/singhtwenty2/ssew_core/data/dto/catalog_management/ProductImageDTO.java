package com.singhtwenty2.ssew_core.data.dto.catalog_management;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public class ProductImageDTO {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateProductImageRequest {

        @NotBlank(message = "Image URL is required")
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        private String image_url;

        @Size(max = 200, message = "Alt text must not exceed 200 characters")
        private String alt_text;

        private Boolean is_thumbnail = false;

        private Integer display_order = 0;

        private Long file_size;

        @Size(max = 10, message = "File format must not exceed 10 characters")
        private String file_format;

        @Min(value = 1, message = "Width must be greater than 0")
        private Integer width;

        @Min(value = 1, message = "Height must be greater than 0")
        private Integer height;

        @NotNull(message = "Product ID is required")
        private String product_id;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateProductImageRequest {

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        private String image_url;

        @Size(max = 200, message = "Alt text must not exceed 200 characters")
        private String alt_text;

        private Boolean is_thumbnail;

        private Integer display_order;

        private Long file_size;

        @Size(max = 10, message = "File format must not exceed 10 characters")
        private String file_format;

        @Min(value = 1, message = "Width must be greater than 0")
        private Integer width;

        @Min(value = 1, message = "Height must be greater than 0")
        private Integer height;
    }

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductImageResponse {
        private String image_id;
        private String image_url;
        private String alt_text;
        private Boolean is_thumbnail;
        private Integer display_order;
        private Long file_size;
        private String file_format;
        private Integer width;
        private Integer height;
        private String created_at;
        private String updated_at;
        private String product_id;
    }
}
