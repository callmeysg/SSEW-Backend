package utils

import (
	"github.com/gin-gonic/gin"
)

type GlobalAPIResponse struct {
	Success bool        `json:"success"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
	Error   interface{} `json:"error,omitempty"`
}

func SuccessResponse(c *gin.Context, statusCode int, message string, data interface{}) {
	c.JSON(statusCode, GlobalAPIResponse{
		Success: true,
		Message: message,
		Data:    data,
	})
}

func ErrorResponse(c *gin.Context, statusCode int, message string, details ...interface{}) {
	res := GlobalAPIResponse{
		Success: false,
		Message: message,
	}
	if len(details) > 0 {
		res.Error = details[0]
	}
	c.AbortWithStatusJSON(statusCode, res)
}
