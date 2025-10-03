package models

import (
	"time"

	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/enums"
)

type PollEvent struct {
	EventID    string                 `json:"eventId"`
	EventType  enums.PollEventType    `json:"eventType"`
	Action     enums.PollActionType   `json:"action"`
	EntityID   string                 `json:"entityId"`
	EntityType string                 `json:"entityType"`
	Metadata   map[string]interface{} `json:"metadata"`
	Timestamp  time.Time              `json:"timestamp"`
	TTL        int64                  `json:"ttl"`
}

type EventPublishRequest struct {
	EventType  enums.PollEventType    `json:"eventType" binding:"required"`
	Action     enums.PollActionType   `json:"action" binding:"required"`
	EntityID   string                 `json:"entityId" binding:"required"`
	EntityType string                 `json:"entityType" binding:"required"`
	UserID     string                 `json:"userId,omitempty"`
	Metadata   map[string]interface{} `json:"metadata"`
}

type OrderStatusChangeRequest struct {
	OrderID   string `json:"orderId" binding:"required"`
	UserID    string `json:"userId" binding:"required"`
	NewStatus string `json:"newStatus" binding:"required"`
}

type NewOrderEventRequest struct {
	OrderID      string `json:"orderId" binding:"required"`
	CustomerName string `json:"customerName" binding:"required"`
	TotalAmount  string `json:"totalAmount" binding:"required"`
}

type OrderUpdateEventRequest struct {
	OrderID    string                 `json:"orderId" binding:"required"`
	UpdateType string                 `json:"updateType" binding:"required"`
	Details    map[string]interface{} `json:"details"`
}
