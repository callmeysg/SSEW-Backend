/**
 * Copyright 2025 SSEW Core Service
 * Developer: Aryan Singh (@singhtwenty2)
 * Portfolio: https://singhtwenty2.pages.dev/
 * This file is part of SSEW E-commerce Backend System
 * Licensed under MIT License
 * For commercial use and inquiries: aryansingh.corp@gmail.com
 * @author Aryan Singh (@singhtwenty2)
 * @project SSEW E-commerce Backend System
 * @since 2025
 */
package com.singhtwenty2.ssew_core.service.catalogue.helper;

import com.singhtwenty2.ssew_core.data.entity.Product;
import com.singhtwenty2.ssew_core.data.repository.ProductRepository;
import com.singhtwenty2.ssew_core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.singhtwenty2.ssew_core.data.dto.catalogue.ProductDTO.CreateProductRequest;
import static com.singhtwenty2.ssew_core.data.dto.catalogue.ProductDTO.UpdateProductRequest;

@Service
@RequiredArgsConstructor
public class ProductValidationService {

    private final ProductRepository productRepository;

    public void validateProductData(CreateProductRequest request, Product existingProduct) {
        if (request.getName() != null && (existingProduct == null || !request.getName().equals(existingProduct.getName()))) {
            if (productRepository.existsByName(request.getName())) {
                throw new BusinessException("Product with this name already exists");
            }
        }

        if (request.getModelNumber() != null && (existingProduct == null || !request.getModelNumber().equals(existingProduct.getModelNumber()))) {
            if (productRepository.existsByModelNumber(request.getModelNumber())) {
                throw new BusinessException("Product with this model number already exists");
            }
        }
    }

    public void validateProductUpdateData(UpdateProductRequest request, Product existingProduct) {
        if (request.getName() != null && !request.getName().equals(existingProduct.getName())) {
            if (productRepository.existsByName(request.getName())) {
                throw new BusinessException("Product with this name already exists");
            }
        }

        if (request.getModelNumber() != null && !request.getModelNumber().equals(existingProduct.getModelNumber())) {
            if (productRepository.existsByModelNumber(request.getModelNumber())) {
                throw new BusinessException("Product with this model number already exists");
            }
        }
    }
}