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
package com.singhtwenty2.commerce_service.service.grpc;

import java.util.Map;

public interface TelemetryClientService {

    void publishOrderStatusChangeEvent(String orderId, String userId, String newStatus);

    void publishNewOrderEventForAdmin(String orderId, String customerName, String totalAmount);

    void publishOrderUpdateEventForAdmin(String orderId, String updateType, Map<String, Object> details);
}