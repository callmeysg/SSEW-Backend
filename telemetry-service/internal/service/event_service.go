package service

import (
	"context"
	"log"
	"sync"
	"time"

	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/config"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/dto"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/enums"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/models"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/repository"
	"github.com/google/uuid"
)

const (
	EventKeyPrefix     = "poll:events:"
	UserEventKeyPrefix = "poll:user:"
	AdminEventKey      = "poll:admin:events"
)

type eventService struct {
	repo       repository.EventRepository
	workerPool chan struct{}
	wg         sync.WaitGroup
}

func NewEventService(repo repository.EventRepository) EventService {
	svc := &eventService{
		repo:       repo,
		workerPool: make(chan struct{}, config.AppConfig.WorkerPoolSize),
	}
	return svc
}

func (s *eventService) CheckHealth(ctx context.Context) error {
	return s.repo.Ping(ctx)
}

func (s *eventService) PublishOrderStatusChangeEvent(ctx context.Context, orderId, userId, newStatus string) error {
	event := &models.PollEvent{
		EventID:    uuid.New().String(),
		EventType:  enums.CustomerOrderStatus,
		Action:     enums.Refresh,
		EntityID:   orderId,
		EntityType: "ORDER",
		Metadata: map[string]interface{}{
			"status":  newStatus,
			"orderId": orderId,
		},
		Timestamp: time.Now(),
		TTL:       config.AppConfig.DefaultTTLSeconds,
	}

	userKey := UserEventKeyPrefix + userId
	return s.saveEventAsync(ctx, userKey, event)
}

func (s *eventService) PublishNewOrderEventForAdmin(ctx context.Context, orderId, customerName, totalAmount string) error {
	event := &models.PollEvent{
		EventID:    uuid.New().String(),
		EventType:  enums.AdminNewOrder,
		Action:     enums.FetchNew,
		EntityID:   orderId,
		EntityType: "ORDER",
		Metadata: map[string]interface{}{
			"orderId":      orderId,
			"customerName": customerName,
			"totalAmount":  totalAmount,
			"timestamp":    time.Now().Format(time.RFC3339),
		},
		Timestamp: time.Now(),
		TTL:       config.AppConfig.DefaultTTLSeconds,
	}

	return s.saveEventAsync(ctx, AdminEventKey, event)
}

func (s *eventService) PublishOrderUpdateEventForAdmin(ctx context.Context, orderId, updateType string, details map[string]interface{}) error {
	if details == nil {
		details = make(map[string]interface{})
	}
	details["updateType"] = updateType
	details["orderId"] = orderId

	event := &models.PollEvent{
		EventID:    uuid.New().String(),
		EventType:  enums.AdminOrderUpdate,
		Action:     enums.UpdatePartial,
		EntityID:   orderId,
		EntityType: "ORDER",
		Metadata:   details,
		Timestamp:  time.Now(),
		TTL:        config.AppConfig.DefaultTTLSeconds,
	}

	return s.saveEventAsync(ctx, AdminEventKey, event)
}

func (s *eventService) PublishGenericEvent(ctx context.Context, req *models.EventPublishRequest) error {
	event := &models.PollEvent{
		EventID:    uuid.New().String(),
		EventType:  req.EventType,
		Action:     req.Action,
		EntityID:   req.EntityID,
		EntityType: req.EntityType,
		Metadata:   req.Metadata,
		Timestamp:  time.Now(),
		TTL:        config.AppConfig.DefaultTTLSeconds,
	}

	key := AdminEventKey
	if req.UserID != "" {
		key = UserEventKeyPrefix + req.UserID
	}

	return s.saveEventAsync(ctx, key, event)
}

func (s *eventService) GetUserEvents(ctx context.Context, userId, lastEventId string) ([]models.PollEvent, error) {
	userKey := UserEventKeyPrefix + userId
	return s.repo.GetEvents(ctx, userKey, lastEventId, config.AppConfig.MaxEventsPerPoll)
}

func (s *eventService) GetAdminEvents(ctx context.Context, lastEventId string) ([]models.PollEvent, error) {
	return s.repo.GetEvents(ctx, AdminEventKey, lastEventId, config.AppConfig.MaxEventsPerPoll)
}

func (s *eventService) GetEventsByType(ctx context.Context, eventType enums.PollEventType, userId, lastEventId string) ([]models.PollEvent, error) {
	key := AdminEventKey
	if eventType == enums.CustomerOrderStatus && userId != "" {
		key = UserEventKeyPrefix + userId
	}
	return s.repo.GetEventsByType(ctx, key, string(eventType), lastEventId, config.AppConfig.MaxEventsPerPoll)
}

func (s *eventService) PollEventsWithTimeout(ctx context.Context, fetchFunc func() ([]models.PollEvent, error), timeout int64) ([]models.PollEvent, error) {
	timeoutDuration := time.Duration(timeout) * time.Millisecond
	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	deadline := time.Now().Add(timeoutDuration)

	resultChan := make(chan []models.PollEvent, 1)
	errorChan := make(chan error, 1)

	go func() {
		for {
			select {
			case <-ctx.Done():
				errorChan <- ctx.Err()
				return
			case <-ticker.C:
				if time.Now().After(deadline) {
					resultChan <- []models.PollEvent{}
					return
				}

				events, err := fetchFunc()
				if err != nil {
					errorChan <- err
					return
				}

				if len(events) > 0 {
					resultChan <- events
					return
				}
			}
		}
	}()

	select {
	case events := <-resultChan:
		return events, nil
	case err := <-errorChan:
		return nil, err
	case <-ctx.Done():
		return nil, ctx.Err()
	}
}

func (s *eventService) BuildPollResponse(events []models.PollEvent, lastEventId string, longPoll bool) *dto.PollResponse {
	newLastEventId := lastEventId
	if len(events) > 0 {
		newLastEventId = events[len(events)-1].EventID
	}

	pollInterval := config.AppConfig.ShortPollInterval
	if longPoll {
		pollInterval = config.AppConfig.LongPollInterval
	}

	return &dto.PollResponse{
		Events:       events,
		LastEventID:  newLastEventId,
		PollInterval: pollInterval,
		HasMore:      len(events) >= config.AppConfig.MaxEventsPerPoll,
	}
}

func (s *eventService) saveEventAsync(ctx context.Context, key string, event *models.PollEvent) error {
	select {
	case s.workerPool <- struct{}{}:
		s.wg.Add(1)
		go func() {
			defer func() {
				<-s.workerPool
				s.wg.Done()
			}()
			if err := s.repo.SaveEvent(ctx, key, event); err != nil {
				log.Printf("Failed to save event: %v", err)
			}
		}()
		return nil
	default:
		return s.repo.SaveEvent(ctx, key, event)
	}
}

func (s *eventService) Close() {
	close(s.workerPool)
	s.wg.Wait()
}
