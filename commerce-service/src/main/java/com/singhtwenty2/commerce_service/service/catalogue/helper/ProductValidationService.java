package com.singhtwenty2.commerce_service.service.catalogue.helper;

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