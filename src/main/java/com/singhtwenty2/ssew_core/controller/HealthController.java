package com.singhtwenty2.ssew_core.controller;

import com.singhtwenty2.ssew_core.data.dto.common.Health.PingDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class HealthController {

    @GetMapping("/ping")
    public PingDTO ping() {
        return PingDTO
                .builder()
                .message("pong 🤡")
                .build();
    }
}
