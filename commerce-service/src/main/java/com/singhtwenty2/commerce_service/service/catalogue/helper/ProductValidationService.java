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
package com.singhtwenty2.commerce_service.service.catalogue.helper;

import com.singhtwenty2.commerce_service.data.entity.Manufacturer;
import com.singhtwenty2.commerce_service.data.entity.Product;
import com.singhtwenty2.commerce_service.data.repository.ProductRepository;
import com.singhtwenty2.commerce_service.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.singhtwenty2.commerce_service.data.dto.catalogue.ProductDTO.CreateProductRequest;
import static com.singhtwenty2.commerce_service.data.dto.catalogue.ProductDTO.UpdateProductRequest;

@Service
@RequiredArgsConstructor
public class ProductValidationService {

    private final ProductRepository productRepository;

    public void validateProductData(CreateProductRequest request, Manufacturer manufacturer) {
        if (request.getName() != null) {
            if (productRepository.existsByNameAndManufacturerId(request.getName(), manufacturer.getId())) {
                throw new BusinessException("Product with name '" + request.getName() + "' already exists for this manufacturer");
            }
        }

        if (request.getModelNumber() != null) {
            if (productRepository.existsByModelNumber(request.getModelNumber())) {
                throw new BusinessException("Product with this model number already exists");
            }
        }
    }

    public void validateProductUpdateData(UpdateProductRequest request, Product existingProduct) {
        if (request.getName() != null && !request.getName().equals(existingProduct.getName())) {
            if (productRepository.existsByNameAndManufacturerId(request.getName(), existingProduct.getManufacturer().getId())) {
                throw new BusinessException("Product with name '" + request.getName() + "' already exists for this manufacturer");
            }
        }

        if (request.getModelNumber() != null && !request.getModelNumber().equals(existingProduct.getModelNumber())) {
            if (productRepository.existsByModelNumber(request.getModelNumber())) {
                throw new BusinessException("Product with this model number already exists");
            }
        }
    }

    public void validateProductUpdateDataWithManufacturerChange(UpdateProductRequest request, Product existingProduct, Manufacturer newManufacturer) {
        String nameToCheck = request.getName() != null ? request.getName() : existingProduct.getName();

        if (productRepository.existsByNameAndManufacturerIdAndIdNot(nameToCheck, newManufacturer.getId(), existingProduct.getId())) {
            throw new BusinessException("Product with name '" + nameToCheck + "' already exists for the new manufacturer");
        }

        if (request.getModelNumber() != null && !request.getModelNumber().equals(existingProduct.getModelNumber())) {
            if (productRepository.existsByModelNumber(request.getModelNumber())) {
                throw new BusinessException("Product with this model number already exists");
            }
        }
    }
}