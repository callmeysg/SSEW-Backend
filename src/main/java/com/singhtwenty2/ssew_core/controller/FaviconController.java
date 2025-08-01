package com.singhtwenty2.ssew_core.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FaviconController {

    @GetMapping("/favicon.ico")
    @ResponseBody
    public ResponseEntity<Resource> favicon() {
        ClassPathResource favicon = new ClassPathResource("static/favicon.png");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/x-icon"))
                .body(favicon);
    }
}
