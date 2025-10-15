// Copyright 2025 Aryan Singh
// Developer: Aryan Singh (@singhtwenty2)
// Portfolio: https://singhtwenty2.pages.dev/
// This file is part of SSEW E-commerce Backend System
// Licensed under MIT License
// For commercial use and inquiries: aryansingh.corp@gmail.com
// @author Aryan Singh (@singhtwenty2)
// @project SSEW E-commerce Backend System
// @since 2025

package config

import (
	"log"
	"os"
	"strconv"
	"strings"

	"github.com/joho/godotenv"
)

type Config struct {
	Port               string
	Environment        string
	RedisHost          string
	RedisPort          string
	RedisPassword      string
	RedisDB            int
	JWTSecret          string
	LogLevel           string
	CORSAllowedOrigins []string
	CORSAllowedMethods []string
	CORSAllowedHeaders []string
	MaxEventsPerPoll   int
	DefaultTTLSeconds  int64
	ShortPollInterval  int64
	LongPollInterval   int64
	LongPollTimeout    int64
	WorkerPoolSize     int
	RedisPoolSize      int
	RedisMinIdleConns  int
}

var AppConfig *Config

func LoadConfig() {
	_ = godotenv.Load()

	AppConfig = &Config{
		Port:               getEnv("PORT", "9001"),
		Environment:        getEnv("ENVIRONMENT", "development"),
		RedisHost:          getEnv("REDIS_HOST", "localhost"),
		RedisPort:          getEnv("REDIS_PORT", "6379"),
		RedisPassword:      getEnv("REDIS_PASSWORD", ""),
		RedisDB:            getEnvAsInt("REDIS_DB", 0),
		JWTSecret:          getEnv("JWT_SECRET", "default_secret"),
		LogLevel:           getEnv("LOG_LEVEL", "info"),
		CORSAllowedOrigins: strings.Split(getEnv("CORS_ALLOWED_ORIGINS", "*"), ","),
		CORSAllowedMethods: strings.Split(getEnv("CORS_ALLOWED_METHODS", "GET,POST,PUT,DELETE,OPTIONS"), ","),
		CORSAllowedHeaders: strings.Split(getEnv("CORS_ALLOWED_HEADERS", "Origin,Content-Type,Authorization"), ","),
		MaxEventsPerPoll:   getEnvAsInt("MAX_EVENTS_PER_POLL", 50),
		DefaultTTLSeconds:  getEnvAsInt64("DEFAULT_TTL_SECONDS", 300),
		ShortPollInterval:  getEnvAsInt64("SHORT_POLL_INTERVAL_MS", 5000),
		LongPollInterval:   getEnvAsInt64("LONG_POLL_INTERVAL_MS", 30000),
		LongPollTimeout:    getEnvAsInt64("LONG_POLL_TIMEOUT_MS", 25000),
		WorkerPoolSize:     getEnvAsInt("WORKER_POOL_SIZE", 100),
		RedisPoolSize:      getEnvAsInt("REDIS_POOL_SIZE", 50),
		RedisMinIdleConns:  getEnvAsInt("REDIS_MIN_IDLE_CONNS", 10),
	}

	log.Printf("Configuration loaded: Environment=%s, RedisHost=%s, RedisPort=%s, RedisDB=%d",
		AppConfig.Environment,
		AppConfig.RedisHost,
		AppConfig.RedisPort,
		AppConfig.RedisDB)
}

func getEnv(key, defaultValue string) string {
	if value, exists := os.LookupEnv(key); exists {
		return value
	}
	return defaultValue
}

func getEnvAsInt(key string, defaultValue int) int {
	valueStr := getEnv(key, "")
	if value, err := strconv.Atoi(valueStr); err == nil {
		return value
	}
	return defaultValue
}

func getEnvAsInt64(key string, defaultValue int64) int64 {
	valueStr := getEnv(key, "")
	if value, err := strconv.ParseInt(valueStr, 10, 64); err == nil {
		return value
	}
	return defaultValue
}
