#
# Copyright 2025 Aryan Singh
# Developer: Aryan Singh (@singhtwenty2)
# Portfolio: https://singhtwenty2.pages.dev/
# This file is part of SSEW E-commerce Backend System
# Licensed under MIT License
# For commercial use and inquiries: aryansingh.corp@gmail.com
# @author Aryan Singh (@singhtwenty2)
# @project SSEW E-commerce Backend System
# @since 2025
#

FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache postgresql-client

COPY --from=build /app/target/*.jar app.jar

EXPOSE 9000

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9000/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]