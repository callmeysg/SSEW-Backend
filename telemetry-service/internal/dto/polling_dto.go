// Copyright 2025 Aryan Singh
// Developer: Aryan Singh (@singhtwenty2)
// Portfolio: https://singhtwenty2.pages.dev/
// This file is part of SSEW E-commerce Backend System
// Licensed under MIT License
// For commercial use and inquiries: aryansingh.corp@gmail.com
// @author Aryan Singh (@singhtwenty2)
// @project SSEW E-commerce Backend System
// @since 2025

package dto

import (
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/models"
)

type PollResponse struct {
	Events       []models.PollEvent `json:"events"`
	LastEventID  string             `json:"lastEventId"`
	PollInterval int64              `json:"pollInterval"`
	HasMore      bool               `json:"hasMore"`
}

type GlobalAPIResponse struct {
	Success bool        `json:"success"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
	Error   string      `json:"error,omitempty"`
}

type PollRequest struct {
	EventType   string `form:"eventType"`
	LastEventID string `form:"lastEventId"`
	LongPoll    bool   `form:"longPoll"`
}

type AdminPollRequest struct {
	LastEventID string `form:"lastEventId"`
	LongPoll    bool   `form:"longPoll"`
}

type UserPollRequest struct {
	LastEventID string `form:"lastEventId"`
	LongPoll    bool   `form:"longPoll"`
}
