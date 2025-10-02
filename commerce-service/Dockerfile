#
# Copyright 2025 SSEW Core Service
# Developer: Aryan Singh (@singhtwenty2)
# Portfolio: https://singhtwenty2.pages.dev/
# This file is part of SSEW E-commerce Backend System
# Licensed under MIT License
# For commercial use and inquiries: aryansingh.corp@gmail.com
# @author Aryan Singh (@singhtwenty2)
# @project SSEW E-commerce Backend System
# @since 2025
#

FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "app.jar"]