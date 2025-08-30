package com.singhtwenty2.ssew_core.data.dto.common;

import lombok.*;

public class Health {

    @Setter
    @Getter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PingDTO {
        String message;
    }
}
