package routes

import (
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/controllers"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/middleware"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/service"
	"github.com/gin-gonic/gin"
)

func SetupRoutes(router *gin.Engine, eventService service.EventService) {
	router.Use(middleware.CORS())

	pollingController := controllers.NewPollingController(eventService)

	router.GET("/health", pollingController.HealthCheck)

	v1 := router.Group("/v1")
	{
		v1.GET("/ping", pollingController.HealthCheck)
		polling := v1.Group("/polling")
		{
			polling.GET("/events", middleware.AuthRequired(), pollingController.PollEvents)
			polling.GET("/user/events", middleware.AuthRequired(), pollingController.PollUserEvents)
			admin := polling.Group("/admin")
			admin.Use(middleware.AuthRequired(), middleware.AdminRequired())
			{
				admin.GET("/events", pollingController.PollAdminEvents)
			}
		}

		internal := v1.Group("/internal")
		{
			internal.GET("/ping", pollingController.HealthCheck)
			events := internal.Group("/events")
			{
				events.POST("/publish", pollingController.PublishEvent)
				events.POST("/order-status-change", pollingController.PublishOrderStatusChange)
				events.POST("/new-order", pollingController.PublishNewOrderEvent)
				events.POST("/order-update", pollingController.PublishOrderUpdateEvent)
			}
		}
	}
}
