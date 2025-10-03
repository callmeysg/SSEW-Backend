package com.singhtwenty2.commerce_service.service.catalogue.helper;

import com.singhtwenty2.commerce_service.data.entity.Manufacturer;
import com.singhtwenty2.commerce_service.data.entity.Product;
import com.singhtwenty2.commerce_service.data.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SkuGeneratorService {

    private final ProductRepository productRepository;

    public String generateUniqueSku(Manufacturer manufacturer, Product parentProduct, String productName) {
        String baseSku;

        if (parentProduct != null) {
            String parentBaseSku = extractBaseSku(parentProduct.getSku());
            baseSku = parentBaseSku + "-V";
        } else {
            String categoryCode = manufacturer.getCategories() != null && !manufacturer.getCategories().isEmpty() ?
                    cleanAndTruncate(manufacturer.getCategories().get(0).getName()).toUpperCase() : "GEN";
            String manufacturerCode = cleanAndTruncate(manufacturer.getName()).toUpperCase();
            String productCode = cleanAndTruncate(productName).toUpperCase();
            baseSku = String.format("SSEW-%s-%s-%s", categoryCode, manufacturerCode, productCode);
        }

        String uniqueSku;
        int attempts = 0;
        do {
            String suffix = generateRandomSuffix();
            uniqueSku = baseSku + "-" + suffix;
            attempts++;
            if (attempts > 10) {
                uniqueSku = baseSku + "-" + System.currentTimeMillis();
                break;
            }
        } while (productRepository.existsBySku(uniqueSku));

        return uniqueSku;
    }

    private String extractBaseSku(String sku) {
        int lastDashIndex = sku.lastIndexOf("-");
        return lastDashIndex > 0 ? sku.substring(0, lastDashIndex) : sku;
    }

    private String generateRandomSuffix() {
        return String.format("%04X", ThreadLocalRandom.current().nextInt(0x10000));
    }

    private String cleanAndTruncate(String input) {
        if (input == null) return "UNK";
        String cleaned = input.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        return cleaned.length() > 3 ? cleaned.substring(0, 3) : cleaned;
    }
}