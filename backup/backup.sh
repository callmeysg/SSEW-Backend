#!/bin/bash
#
# This script performs a backup of the PostgreSQL database running in a Docker container,
# compresses it, uploads it to AWS S3, and rotates old backups.

set -e

# --- Configuration ---
# Load environment variables from the .env file in the current directory.
source ./.env

S3_BUCKET=${AWS_S3_TEMP_BUCKET}
BACKUP_FOLDER="database-backups" # A dedicated folder within your staging bucket
NUM_BACKUPS_TO_KEEP=3
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE_SQL="backup_${TIMESTAMP}.sql"
BACKUP_FILE_GZ="${BACKUP_FILE_SQL}.gz"
CONTAINER_NAME="ssew-postgres"

echo "--- Starting backup process at $(date) ---"

# --- 1. Configure AWS CLI ---
echo "Configuring AWS CLI..."
aws configure set aws_access_key_id "$AWS_ACCESS_KEY_ID"
aws configure set aws_secret_access_key "$AWS_SECRET_ACCESS_KEY"
aws configure set default.region "$AWS_REGION"
echo "AWS CLI configured for region: $AWS_REGION"

# --- 2. Create PostgreSQL Backup ---
echo "Creating PostgreSQL backup from container '${CONTAINER_NAME}'..."
docker exec "${CONTAINER_NAME}" pg_dumpall -U "${POSTGRES_USER}" > "${BACKUP_FILE_SQL}"
echo "Backup created: ${BACKUP_FILE_SQL}"

# --- 3. Compress the Backup ---
echo "Compressing backup file..."
gzip "${BACKUP_FILE_SQL}"
echo "Backup compressed: ${BACKUP_FILE_GZ}"

# --- 4. Upload to S3 ---
S3_PATH="s3://${S3_BUCKET}/${BACKUP_FOLDER}/${BACKUP_FILE_GZ}"
echo "Uploading backup to ${S3_PATH}..."
aws s3 cp "${BACKUP_FILE_GZ}" "${S3_PATH}"
echo "Upload complete."

# --- 5. Rotate Backups in S3 ---
echo "Rotating backups in S3, keeping the latest ${NUM_BACKUPS_TO_KEEP}..."

FILES_TO_DELETE=$(aws s3 ls "s3://${S3_BUCKET}/${BACKUP_FOLDER}/" | awk '{print $4}' | grep "^backup_" | sort | head -n -${NUM_BACKUPS_TO_KEEP})

if [ -z "$FILES_TO_DELETE" ]; then
    echo "No old backups to delete."
else
    for FILE_TO_DELETE in $FILES_TO_DELETE; do
        echo "Deleting old backup: ${FILE_TO_DELETE}"
        aws s3 rm "s3://${S3_BUCKET}/${BACKUP_FOLDER}/${FILE_TO_DELETE}"
    done
fi
echo "Rotation complete."

# --- 6. Clean Up Local File ---
echo "Cleaning up local backup file: ${BACKUP_FILE_GZ}"
rm "${BACKUP_FILE_GZ}"

echo "--- Backup process finished successfully at $(date) ---"
