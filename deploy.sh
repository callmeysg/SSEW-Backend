#!/bin/bash

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