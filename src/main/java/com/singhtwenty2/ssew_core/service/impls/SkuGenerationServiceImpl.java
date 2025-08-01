package com.singhtwenty2.ssew_core.service.impls;

import com.singhtwenty2.ssew_core.data.entity.Brand;
import com.singhtwenty2.ssew_core.data.repository.ProductRepository;
import com.singhtwenty2.ssew_core.service.SkuGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SkuGenerationServiceImpl implements SkuGenerationService {

    private final ProductRepository productRepository;
    private static final String SKU_PREFIX = "SSEW";
    private static final int MAX_RETRY_ATTEMPTS = 10;

    @Override
    @Transactional(readOnly = true)
    public String generateUniqueSku(Brand brand) {
        String brandCode = generateBrandCode(brand.getName());
        String categoryCode = generateCategoryCode(brand.getCategory().getName());

        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            String sku = generateSku(categoryCode, brandCode);

            if (!productRepository.existsBySku(sku)) {
                log.debug("Generated unique SKU: {} for brand: {}", sku, brand.getName());
                return sku;
            }

            log.debug("SKU collision detected: {}, retrying...", sku);
        }

        throw new RuntimeException("Failed to generate unique SKU after " + MAX_RETRY_ATTEMPTS + " attempts");
    }

    private String generateSku(String categoryCode, String brandCode) {
        int sequence = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.format("%s-%s-%s-%04d", SKU_PREFIX, categoryCode, brandCode, sequence);
    }

    private String generateBrandCode(String brandName) {
        if (brandName == null || brandName.trim().isEmpty()) {
            return "UNK";
        }

        String cleaned = brandName.toUpperCase()
                .replaceAll("[^A-Z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();

        String[] words = cleaned.split(" ");

        if (words.length == 1) {
            return words[0].substring(0, Math.min(3, words[0].length())).toUpperCase();
        } else if (words.length == 2) {
            return (words[0].substring(0, Math.min(2, words[0].length())) +
                    words[1].substring(0, Math.min(2, words[1].length()))).toUpperCase();
        } else {
            return (words[0].substring(0, 1) +
                    words[1].substring(0, 1) +
                    words[2].substring(0, 1)).toUpperCase();
        }
    }

    private String generateCategoryCode(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "GEN";
        }

        String cleaned = categoryName.toUpperCase()
                .replaceAll("[^A-Z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();

        String[] words = cleaned.split(" ");

        if (words.length == 1) {
            return words[0].substring(0, Math.min(3, words[0].length())).toUpperCase();
        } else {
            return (words[0].substring(0, Math.min(2, words[0].length())) +
                    words[words.length - 1].substring(0, 1)).toUpperCase();
        }
    }
}
