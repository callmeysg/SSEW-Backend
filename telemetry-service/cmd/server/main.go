package main

import (
	"context"
	"log"
	"net"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/config"
	grpcServer "github.com/callmeysg/SSEW-Backend/telemetry-service/internal/grpc"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/repository"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/routes"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/service"
	pb "github.com/callmeysg/SSEW-Backend/telemetry-service/proto"
	"github.com/gin-gonic/gin"
	"google.golang.org/grpc"
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

	go func() {
		grpcPort := "9002"
		lis, err := net.Listen("tcp", ":"+grpcPort)
		if err != nil {
			log.Fatalf("failed to listen for gRPC: %v", err)
		}
		s := grpc.NewServer()
		pb.RegisterTelemetryServiceServer(s, grpcServer.NewTelemetryServer(eventService))
		log.Printf("gRPC server listening at %v", lis.Addr())
		if err := s.Serve(lis); err != nil {
			log.Fatalf("failed to serve gRPC: %v", err)
		}
	}()

	router := gin.New()
	router.Use(gin.Recovery(), gin.Logger())
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
		log.Printf("HTTP server (Gin) starting on port %s", config.AppConfig.Port)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Failed to start HTTP server: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down servers...")

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		log.Fatalf("HTTP server forced to shutdown: %v", err)
	}

	log.Println("Server shutdown complete.")
}
