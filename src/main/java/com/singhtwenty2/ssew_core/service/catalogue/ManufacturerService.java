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
package com.singhtwenty2.ssew_core.service.catalogue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.singhtwenty2.ssew_core.data.dto.catalogue.ManufacturerDTO.*;

public interface ManufacturerService {

    ManufacturerResponse createManufacturer(CreateManufacturerRequest createManufacturerRequest);

    ManufacturerResponse getManufacturerById(String manufacturerId);

    ManufacturerResponse getManufacturerBySlug(String slug);

    Page<ManufacturerResponse> getAllManufacturers(Pageable pageable);

    List<ManufacturerResponse> getActiveManufacturers();

    Page<ManufacturerResponse> getActiveManufacturers(Pageable pageable);

    Page<ManufacturerResponse> getManufacturersByCategories(List<String> categoryIds, Pageable pageable);

    Page<ManufacturerResponse> getActiveManufacturersByCategories(List<String> categoryIds, Pageable pageable);

    Page<ManufacturerResponse> searchManufacturers(String name, String categoryId, Boolean isActive, Pageable pageable);

    ManufacturerResponse updateManufacturer(String manufacturerId, UpdateManufacturerRequest updateManufacturerRequest);

    void deleteManufacturer(String manufacturerId);

    void toggleManufacturerStatus(String manufacturerId);

    List<ManufacturerResponse> getManufacturersByCategoriesOrderByDisplayOrder(List<String> categoryIds);
}