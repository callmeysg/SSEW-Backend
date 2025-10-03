#!/bin/bash
#
# This script performs the one-time setup for the automated backup cron job.

set -e

DEPLOYMENT_DIR="/home/ubuntu/ssew-deployment"
BACKUP_SCRIPT_PATH="${DEPLOYMENT_DIR}/backup.sh"
LOG_FILE_PATH="${DEPLOYMENT_DIR}/backup.log"
CRON_JOB="0 3 * * * cd ${DEPLOYMENT_DIR} && ${BACKUP_SCRIPT_PATH} >> ${LOG_FILE_PATH} 2>&1"

echo "--- Starting backup setup ---"

# --- 1. Install AWS CLI v2 (Official Method) ---
if ! command -v aws &> /dev/null; then
    echo "AWS CLI not found. Installing via official AWS method..."

    # Update package list and install unzip, a required dependency
    sudo apt-get update
    sudo apt-get install -y unzip

    # Download the official installer
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"

    # Unzip the installer
    unzip awscliv2.zip

    # Run the installer
    sudo ./aws/install

    # Clean up the installation files
    rm -rf aws awscliv2.zip

    echo "AWS CLI installed successfully."
else
    echo "AWS CLI is already installed."
fi

# --- 2. Make the backup script executable ---
echo "Making backup script executable..."
chmod +x "${BACKUP_SCRIPT_PATH}"

# --- 3. Set up the Cron Job ---
echo "Setting up cron job to run daily at 3:00 AM..."
(crontab -l 2>/dev/null | grep -Fv "${BACKUP_SCRIPT_PATH}"; echo "${CRON_JOB}") | crontab -
echo "Cron job created. Current crontab:"
crontab -l

echo "--- Backup setup complete! ---"
echo "The first backup will run automatically at 3:00 AM."
echo "You can monitor its execution by checking the log file: ${LOG_FILE_PATH}"
