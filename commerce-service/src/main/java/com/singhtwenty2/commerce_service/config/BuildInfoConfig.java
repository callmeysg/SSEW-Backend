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
package com.singhtwenty2.commerce_service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Properties;

@Configuration
public class BuildInfoConfig {

    @Bean
    @ConditionalOnMissingBean
    public BuildProperties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty("version", "1.0.0");
        properties.setProperty("name", "SSEW Auth Service");
        properties.setProperty("time", Instant.now().toString());
        return new BuildProperties(properties);
    }
}
