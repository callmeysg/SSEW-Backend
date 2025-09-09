#!/bin/bash
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


sudo apt update
sudo apt install -y docker.io docker-compose-plugin

sudo usermod -aG docker ubuntu
newgrp docker

sudo systemctl start docker
sudo systemctl enable docker

mkdir -p /home/ubuntu/ssew-deployment
cd /home/ubuntu/ssew-deployment

curl -o docker-compose.yml https://raw.githubusercontent.com/callmeysg/SSEW-Backend-Core-Service/main/docker-compose.yml

docker-compose up -d postgres redis

echo "Initial deployment setup completed!"
echo "Configure GitHub Actions secrets and push to main branch to deploy the application."