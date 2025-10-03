// Copyright 2025 Aryan Singh
// Developer: Aryan Singh (@singhtwenty2)
// Portfolio: https://singhtwenty2.pages.dev/
// This file is part of SSEW E-commerce Backend System
// Licensed under MIT License
// For commercial use and inquiries: aryansingh.corp@gmail.com
// @author Aryan Singh (@singhtwenty2)
// @project SSEW E-commerce Backend System
// @since 2025

package service

import (
	"context"

	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/dto"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/enums"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/models"
)

type EventService interface {
	CheckHealth(ctx context.Context) error
	PublishOrderStatusChangeEvent(ctx context.Context, orderId, userId, newStatus string) error
	PublishNewOrderEventForAdmin(ctx context.Context, orderId, customerName, totalAmount string) error
	PublishOrderUpdateEventForAdmin(ctx context.Context, orderId, updateType string, details map[string]interface{}) error
	PublishGenericEvent(ctx context.Context, req *models.EventPublishRequest) error
	GetUserEvents(ctx context.Context, userId, lastEventId string) ([]models.PollEvent, error)
	GetAdminEvents(ctx context.Context, lastEventId string) ([]models.PollEvent, error)
	GetEventsByType(ctx context.Context, eventType enums.PollEventType, userId, lastEventId string) ([]models.PollEvent, error)
	PollEventsWithTimeout(ctx context.Context, fetchFunc func() ([]models.PollEvent, error), timeout int64) ([]models.PollEvent, error)
	BuildPollResponse(events []models.PollEvent, lastEventId string, longPoll bool) *dto.PollResponse
}
