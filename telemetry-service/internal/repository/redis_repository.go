// Copyright 2025 Aryan Singh
// Developer: Aryan Singh (@singhtwenty2)
// Portfolio: https://singhtwenty2.pages.dev/
// This file is part of SSEW E-commerce Backend System
// Licensed under MIT License
// For commercial use and inquiries: aryansingh.corp@gmail.com
// @author Aryan Singh (@singhtwenty2)
// @project SSEW E-commerce Backend System
// @since 2025

package repository

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"time"

	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/config"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/models"
	"github.com/go-redis/redis/v8"
)

type RedisEventRepository struct {
	client *redis.Client
}

func NewRedisEventRepository() (*RedisEventRepository, error) {
	addr := fmt.Sprintf("%s:%s", config.AppConfig.RedisHost, config.AppConfig.RedisPort)

	opt := &redis.Options{
		Addr:         addr,
		Password:     config.AppConfig.RedisPassword,
		DB:           config.AppConfig.RedisDB,
		PoolSize:     config.AppConfig.RedisPoolSize,
		MinIdleConns: config.AppConfig.RedisMinIdleConns,
		MaxRetries:   3,
		ReadTimeout:  3 * time.Second,
		WriteTimeout: 3 * time.Second,
	}

	client := redis.NewClient(opt)

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := client.Ping(ctx).Err(); err != nil {
		return nil, fmt.Errorf("failed to connect to Redis at %s: %w", addr, err)
	}

	log.Printf("Successfully connected to Redis at %s", addr)

	return &RedisEventRepository{
		client: client,
	}, nil
}

func (r *RedisEventRepository) Ping(ctx context.Context) error {
	return r.client.Ping(ctx).Err()
}

func (r *RedisEventRepository) SaveEvent(ctx context.Context, key string, event *models.PollEvent) error {
	eventJSON, err := json.Marshal(event)
	if err != nil {
		return fmt.Errorf("failed to marshal event: %w", err)
	}

	score := float64(event.Timestamp.Unix())

	pipe := r.client.Pipeline()
	pipe.ZAdd(ctx, key, &redis.Z{
		Score:  score,
		Member: string(eventJSON),
	})
	pipe.Expire(ctx, key, time.Duration(event.TTL)*time.Second)

	if _, err := pipe.Exec(ctx); err != nil {
		return fmt.Errorf("failed to save event: %w", err)
	}

	size, err := r.client.ZCard(ctx, key).Result()
	if err == nil && size > int64(config.AppConfig.MaxEventsPerPoll*2) {
		r.client.ZRemRangeByRank(ctx, key, 0, size-int64(config.AppConfig.MaxEventsPerPoll)-1)
	}

	return nil
}

func (r *RedisEventRepository) GetEvents(ctx context.Context, key string, lastEventID string, limit int) ([]models.PollEvent, error) {
	minScore := "-inf"

	if lastEventID != "" {
		lastEvent, err := r.FindEventByID(ctx, key, lastEventID)
		if err == nil && lastEvent != nil {
			minScore = fmt.Sprintf("%d", lastEvent.Timestamp.Unix()+1)
		}
	}

	maxScore := fmt.Sprintf("%d", time.Now().Unix())

	results, err := r.client.ZRangeByScore(ctx, key, &redis.ZRangeBy{
		Min:   minScore,
		Max:   maxScore,
		Count: int64(limit),
	}).Result()

	if err != nil {
		if err == redis.Nil {
			return []models.PollEvent{}, nil
		}
		return nil, fmt.Errorf("failed to get events: %w", err)
	}

	events := make([]models.PollEvent, 0, len(results))
	for _, result := range results {
		var event models.PollEvent
		if err := json.Unmarshal([]byte(result), &event); err != nil {
			log.Printf("Failed to unmarshal event: %v", err)
			continue
		}
		events = append(events, event)
	}

	go r.CleanupExpiredEvents(context.Background(), key, config.AppConfig.DefaultTTLSeconds)

	return events, nil
}

func (r *RedisEventRepository) GetEventsByType(ctx context.Context, key string, eventType string, lastEventID string, limit int) ([]models.PollEvent, error) {
	allEvents, err := r.GetEvents(ctx, key, lastEventID, limit*2)
	if err != nil {
		return nil, err
	}

	filteredEvents := make([]models.PollEvent, 0)
	for _, event := range allEvents {
		if string(event.EventType) == eventType {
			filteredEvents = append(filteredEvents, event)
			if len(filteredEvents) >= limit {
				break
			}
		}
	}

	return filteredEvents, nil
}

func (r *RedisEventRepository) FindEventByID(ctx context.Context, key string, eventID string) (*models.PollEvent, error) {
	results, err := r.client.ZRange(ctx, key, 0, -1).Result()
	if err != nil {
		return nil, fmt.Errorf("failed to find event: %w", err)
	}

	for _, result := range results {
		var event models.PollEvent
		if err := json.Unmarshal([]byte(result), &event); err != nil {
			continue
		}
		if event.EventID == eventID {
			return &event, nil
		}
	}

	return nil, nil
}

func (r *RedisEventRepository) CleanupExpiredEvents(ctx context.Context, key string, ttlSeconds int64) error {
	maxScore := time.Now().Add(-time.Duration(ttlSeconds) * time.Second).Unix()

	_, err := r.client.ZRemRangeByScore(ctx, key, "-inf", fmt.Sprintf("%d", maxScore)).Result()
	if err != nil && err != redis.Nil {
		return fmt.Errorf("failed to cleanup expired events: %w", err)
	}

	return nil
}

func (r *RedisEventRepository) SetKeyTTL(ctx context.Context, key string, ttlSeconds int64) error {
	return r.client.Expire(ctx, key, time.Duration(ttlSeconds)*time.Second).Err()
}

func (r *RedisEventRepository) Close() error {
	return r.client.Close()
}
