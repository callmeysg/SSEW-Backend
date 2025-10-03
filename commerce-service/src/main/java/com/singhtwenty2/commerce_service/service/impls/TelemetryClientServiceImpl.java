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
package com.singhtwenty2.commerce_service.service.impls;

import com.google.protobuf.Struct;
import com.singhtwenty2.commerce_service.grpc.PublishNewOrderRequest;
import com.singhtwenty2.commerce_service.grpc.PublishOrderStatusChangeRequest;
import com.singhtwenty2.commerce_service.grpc.PublishOrderUpdateRequest;
import com.singhtwenty2.commerce_service.grpc.TelemetryServiceGrpc;
import com.singhtwenty2.commerce_service.service.grpc.TelemetryClientService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TelemetryClientServiceImpl implements TelemetryClientService {

    private ManagedChannel channel;
    private TelemetryServiceGrpc.TelemetryServiceBlockingStub blockingStub;

    @Value("${grpc.telemetry-service.host}")
    private String host;

    @Value("${grpc.telemetry-service.port}")
    private int port;

    @PostConstruct
    private void init() {
        log.info("Initializing gRPC client for Telemetry Service at {}:{}", host, port);
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = TelemetryServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void publishOrderStatusChangeEvent(String orderId, String userId, String newStatus) {
        try {
            log.debug("gRPC -> Publishing OrderStatusChange event for order {}", orderId);
            PublishOrderStatusChangeRequest request = PublishOrderStatusChangeRequest.newBuilder()
                    .setOrderId(orderId)
                    .setUserId(userId)
                    .setNewStatus(newStatus)
                    .build();
            var ignored = blockingStub.publishOrderStatusChange(request);
        } catch (Exception e) {
            log.error("Failed to send gRPC OrderStatusChange event for order {}: {}", orderId, e.getMessage());
        }
    }

    @Override
    public void publishNewOrderEventForAdmin(String orderId, String customerName, String totalAmount) {
        try {
            log.debug("gRPC -> Publishing NewOrder event for order {}", orderId);
            PublishNewOrderRequest request = PublishNewOrderRequest.newBuilder()
                    .setOrderId(orderId)
                    .setCustomerName(customerName)
                    .setTotalAmount(totalAmount)
                    .build();
            var ignored = blockingStub.publishNewOrder(request);
        } catch (Exception e) {
            log.error("Failed to send gRPC NewOrder event for order {}: {}", orderId, e.getMessage());
        }
    }

    @Override
    public void publishOrderUpdateEventForAdmin(String orderId, String updateType, Map<String, Object> details) {
        try {
            log.debug("gRPC -> Publishing OrderUpdate event for order {}", orderId);

            Struct.Builder detailsStructBuilder = Struct.newBuilder();
            for (Map.Entry<String, Object> entry : details.entrySet()) {
                if (entry.getValue() instanceof String) {
                    detailsStructBuilder.putFields(entry.getKey(), com.google.protobuf.Value.newBuilder().setStringValue((String) entry.getValue()).build());
                }
            }

            PublishOrderUpdateRequest request = PublishOrderUpdateRequest.newBuilder()
                    .setOrderId(orderId)
                    .setUpdateType(updateType)
                    .setDetails(detailsStructBuilder.build())
                    .build();
            var ignored = blockingStub.publishOrderUpdate(request);
        } catch (Exception e) {
            log.error("Failed to send gRPC OrderUpdate event for order {}: {}", orderId, e.getMessage());
        }
    }

    @PreDestroy
    private void shutdown() throws InterruptedException {
        log.info("Shutting down gRPC client for Telemetry Service");
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}