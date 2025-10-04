package com.singhtwenty2.commerce_service.data.dto.common;

import lombok.*;

@Setter
@Getter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalApiResponse<T> {
    private Boolean success;
    private String message;
    private T data;
}
