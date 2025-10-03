// Copyright 2025 Aryan Singh
// Developer: Aryan Singh (@singhtwenty2)
// Portfolio: https://singhtwenty2.pages.dev/
// This file is part of SSEW E-commerce Backend System
// Licensed under MIT License
// For commercial use and inquiries: aryansingh.corp@gmail.com
// @author Aryan Singh (@singhtwenty2)
// @project SSEW E-commerce Backend System
// @since 2025

package middleware

import (
	"encoding/base64"
	"errors"
	"net/http"
	"strings"

	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/config"
	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/utils"
	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

type Claims struct {
	Subject string `json:"sub"`
	Type    string `json:"type"`
	Role    string `json:"role"`
	jwt.RegisteredClaims
}

func AuthRequired() gin.HandlerFunc {
	return func(c *gin.Context) {
		tokenString := extractToken(c)
		if tokenString == "" {
			utils.ErrorResponse(c, http.StatusUnauthorized, "Missing authentication token")
			return
		}

		claims, err := validateToken(tokenString)
		if err != nil {
			utils.ErrorResponse(c, http.StatusUnauthorized, "Invalid or expired token")
			return
		}

		if claims.Type != "access_token" {
			utils.ErrorResponse(c, http.StatusUnauthorized, "Invalid token type, expected access token")
			return
		}

		c.Set("userId", claims.Subject)
		c.Set("role", claims.Role)
		c.Set("claims", claims)

		c.Next()
	}
}

func AdminRequired() gin.HandlerFunc {
	return func(c *gin.Context) {
		role, exists := c.Get("role")
		if !exists {
			utils.ErrorResponse(c, http.StatusForbidden, "Access denied. Role not found.")
			return
		}

		userRole, ok := role.(string)
		if !ok || (userRole != "ADMIN") {
			utils.ErrorResponse(c, http.StatusForbidden, "Admin access required")
			return
		}

		c.Next()
	}
}

func OptionalAuth() gin.HandlerFunc {
	return func(c *gin.Context) {
		tokenString := extractToken(c)
		if tokenString != "" {
			if claims, err := validateToken(tokenString); err == nil && claims.Type == "access_token" {
				c.Set("userId", claims.Subject)
				c.Set("role", claims.Role)
				c.Set("claims", claims)
				c.Set("authenticated", true)
			}
		}
		c.Next()
	}
}

func extractToken(c *gin.Context) string {
	bearerToken := c.GetHeader("Authorization")
	if bearerToken != "" {
		parts := strings.Split(bearerToken, " ")
		if len(parts) == 2 && strings.ToLower(parts[0]) == "bearer" {
			return parts[1]
		}
	}

	if token := c.Query("token"); token != "" {
		return token
	}

	if token, err := c.Cookie("token"); err == nil {
		return token
	}

	return ""
}

func validateToken(tokenString string) (*Claims, error) {
	decodedSecret, err := base64.StdEncoding.DecodeString(config.AppConfig.JWTSecret)
	if err != nil {
		return nil, errors.New("failed to decode jwt secret")
	}

	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return decodedSecret, nil
	})

	if err != nil {
		return nil, err
	}

	if claims, ok := token.Claims.(*Claims); ok && token.Valid {
		return claims, nil
	}

	return nil, jwt.ErrSignatureInvalid
}

func GetUserID(c *gin.Context) string {
	if userId, exists := c.Get("userId"); exists {
		if id, ok := userId.(string); ok {
			return id
		}
	}
	return ""
}

func GetUserRole(c *gin.Context) string {
	if role, exists := c.Get("role"); exists {
		if r, ok := role.(string); ok {
			return r
		}
	}
	return ""
}

func IsAdmin(c *gin.Context) bool {
	return GetUserRole(c) == "ADMIN"
}
