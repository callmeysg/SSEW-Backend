package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/config"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/repository"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/routes"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/service"
	"github.com/gin-gonic/gin"
)

func main() {
	config.LoadConfig()

	if config.AppConfig.Environment == "production" {
		gin.SetMode(gin.ReleaseMode)
	}

	redisRepo, err := repository.NewRedisEventRepository(config.AppConfig.RedisURL)
	if err != nil {
		log.Fatalf("Failed to connect to Redis: %v", err)
	}
	defer redisRepo.Close()

	eventService := service.NewEventService(redisRepo)

	router := gin.New()
	router.Use(gin.Recovery())
	router.Use(gin.Logger())

	routes.SetupRoutes(router, eventService)

	srv := &http.Server{
		Addr:           ":" + config.AppConfig.Port,
		Handler:        router,
		ReadTimeout:    30 * time.Second,
		WriteTimeout:   30 * time.Second,
		IdleTimeout:    60 * time.Second,
		MaxHeaderBytes: 1 << 20,
	}

	go func() {
		log.Printf("Starting telemetry service on port %s", config.AppConfig.Port)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Failed to start server: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	log.Println("Shutting down server...")

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("Server forced to shutdown: %v", err)
	}

	log.Println("Server shutdown complete")
}
