// Copyright 2025 Aryan Singh
// Developer: Aryan Singh (@singhtwenty2)
// Portfolio: https://singhtwenty2.pages.dev/
// This file is part of SSEW E-commerce Backend System
// Licensed under MIT License
// For commercial use and inquiries: aryansingh.corp@gmail.com
// @author Aryan Singh (@singhtwenty2)
// @project SSEW E-commerce Backend System
// @since 2025

package controllers

import (
	"context"
	"net/http"
	"time"

	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/config"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/dto"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/enums"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/middleware"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/models"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/service"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/utils"
	"github.com/gin-gonic/gin"
)

type PollingController struct {
	eventService service.EventService
}

func NewPollingController(eventService service.EventService) *PollingController {
	return &PollingController{
		eventService: eventService,
	}
}

func (pc *PollingController) HealthCheck(c *gin.Context) {
	ctx, cancel := context.WithTimeout(c.Request.Context(), 2*time.Second)
	defer cancel()

	if err := pc.eventService.CheckHealth(ctx); err != nil {
		utils.ErrorResponse(c, http.StatusServiceUnavailable, "Service is unhealthy", map[string]interface{}{
			"service": "telemetry",
			"status":  "unhealthy",
			"dependencies": map[string]string{
				"redis": "disconnected",
			},
			"error": err.Error(),
		})
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Telemetry service is healthy", map[string]interface{}{
		"service": "telemetry",
		"status":  "healthy",
		"dependencies": map[string]string{
			"redis": "connected",
		},
	})
}

func (pc *PollingController) PollEvents(c *gin.Context) {
	var req dto.PollRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid request parameters")
		return
	}

	if req.EventType != "" && !enums.IsValidEventType(req.EventType) {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid event type")
		return
	}

	userId := middleware.GetUserID(c)
	if userId == "" {
		utils.ErrorResponse(c, http.StatusUnauthorized, "Unauthorized access")
		return
	}

	isAdmin := middleware.IsAdmin(c)

	ctx := c.Request.Context()
	var events []models.PollEvent
	var err error

	fetchFunc := func() ([]models.PollEvent, error) {
		if req.EventType != "" {
			return pc.eventService.GetEventsByType(ctx, enums.PollEventType(req.EventType), userId, req.LastEventID)
		}

		if isAdmin && (req.EventType == string(enums.AdminNewOrder) || req.EventType == string(enums.AdminOrderUpdate)) {
			return pc.eventService.GetAdminEvents(ctx, req.LastEventID)
		}

		return pc.eventService.GetUserEvents(ctx, userId, req.LastEventID)
	}

	if req.LongPoll {
		events, err = pc.eventService.PollEventsWithTimeout(ctx, fetchFunc, config.AppConfig.LongPollTimeout)
	} else {
		events, err = fetchFunc()
	}

	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to retrieve events")
		return
	}

	response := pc.eventService.BuildPollResponse(events, req.LastEventID, req.LongPoll)
	utils.SuccessResponse(c, http.StatusOK, "Events retrieved", response)
}

func (pc *PollingController) PollAdminEvents(c *gin.Context) {
	var req dto.AdminPollRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid request parameters")
		return
	}

	ctx := c.Request.Context()
	var events []models.PollEvent
	var err error

	fetchFunc := func() ([]models.PollEvent, error) {
		return pc.eventService.GetAdminEvents(ctx, req.LastEventID)
	}

	if req.LongPoll {
		events, err = pc.eventService.PollEventsWithTimeout(ctx, fetchFunc, config.AppConfig.LongPollTimeout)
	} else {
		events, err = fetchFunc()
	}

	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to retrieve events")
		return
	}

	response := pc.eventService.BuildPollResponse(events, req.LastEventID, req.LongPoll)
	utils.SuccessResponse(c, http.StatusOK, "Admin events retrieved", response)
}

func (pc *PollingController) PollUserEvents(c *gin.Context) {
	var req dto.UserPollRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid request parameters")
		return
	}

	userId := middleware.GetUserID(c)
	if userId == "" {
		utils.ErrorResponse(c, http.StatusUnauthorized, "Unauthorized access")
		return
	}

	ctx := c.Request.Context()
	var events []models.PollEvent
	var err error

	fetchFunc := func() ([]models.PollEvent, error) {
		return pc.eventService.GetUserEvents(ctx, userId, req.LastEventID)
	}

	if req.LongPoll {
		events, err = pc.eventService.PollEventsWithTimeout(ctx, fetchFunc, config.AppConfig.LongPollTimeout)
	} else {
		events, err = fetchFunc()
	}

	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to retrieve events")
		return
	}

	response := pc.eventService.BuildPollResponse(events, req.LastEventID, req.LongPoll)
	utils.SuccessResponse(c, http.StatusOK, "User events retrieved", response)
}

func (pc *PollingController) PublishEvent(c *gin.Context) {
	var req models.EventPublishRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	if !enums.IsValidEventType(string(req.EventType)) || !enums.IsValidActionType(string(req.Action)) {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid event type or action")
		return
	}

	ctx := context.Background()
	if err := pc.eventService.PublishGenericEvent(ctx, &req); err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to publish event")
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Event published successfully", nil)
}

func (pc *PollingController) PublishOrderStatusChange(c *gin.Context) {
	var req models.OrderStatusChangeRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	ctx := context.Background()
	if err := pc.eventService.PublishOrderStatusChangeEvent(ctx, req.OrderID, req.UserID, req.NewStatus); err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to publish order status change event")
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Order status change event published", nil)
}

func (pc *PollingController) PublishNewOrderEvent(c *gin.Context) {
	var req models.NewOrderEventRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	ctx := context.Background()
	if err := pc.eventService.PublishNewOrderEventForAdmin(ctx, req.OrderID, req.CustomerName, req.TotalAmount); err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to publish new order event")
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "New order event published", nil)
}

func (pc *PollingController) PublishOrderUpdateEvent(c *gin.Context) {
	var req models.OrderUpdateEventRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid request body")
		return
	}

	ctx := context.Background()
	if err := pc.eventService.PublishOrderUpdateEventForAdmin(ctx, req.OrderID, req.UpdateType, req.Details); err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to publish order update event")
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Order update event published", nil)
}
