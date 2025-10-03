package repository

import (
	"context"

	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/models"
)

type EventRepository interface {
	Ping(ctx context.Context) error
	SaveEvent(ctx context.Context, key string, event *models.PollEvent) error
	GetEvents(ctx context.Context, key string, lastEventID string, limit int) ([]models.PollEvent, error)
	GetEventsByType(ctx context.Context, key string, eventType string, lastEventID string, limit int) ([]models.PollEvent, error)
	FindEventByID(ctx context.Context, key string, eventID string) (*models.PollEvent, error)
	CleanupExpiredEvents(ctx context.Context, key string, ttlSeconds int64) error
	SetKeyTTL(ctx context.Context, key string, ttlSeconds int64) error
}
