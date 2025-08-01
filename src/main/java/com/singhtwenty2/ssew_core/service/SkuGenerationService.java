package com.singhtwenty2.ssew_core.service;

import com.singhtwenty2.ssew_core.data.entity.Brand;
import org.springframework.stereotype.Service;

@Service
public interface SkuGenerationService {

    String generateUniqueSku(Brand brand);
}
