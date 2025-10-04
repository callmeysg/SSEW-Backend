package com.singhtwenty2.commerce_service.service.catalogue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.singhtwenty2.commerce_service.data.dto.catalogue.ManufacturerDTO.*;

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