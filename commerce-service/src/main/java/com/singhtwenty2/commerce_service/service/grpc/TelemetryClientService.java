package com.singhtwenty2.commerce_service.service.grpc;

import java.util.Map;

public interface TelemetryClientService {

    void publishOrderStatusChangeEvent(String orderId, String userId, String newStatus);

    void publishNewOrderEventForAdmin(String orderId, String customerName, String totalAmount);

    void publishOrderUpdateEventForAdmin(String orderId, String updateType, Map<String, Object> details);
}