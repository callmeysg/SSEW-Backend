# Automated Database Backup System

This directory contains scripts for automatically backing up the PostgreSQL database to AWS S3 and rotating old backups.

## Features

- **Automated:** Runs daily via a cron job on the host machine.
- **Decoupled:** Does not depend on the application containers being online.
- **Efficient:** Compresses backups before uploading to S3.
- **Rotation:** Automatically keeps the last 3 backups and deletes older ones.
- **Environment-Aware:** Pulls configuration from the `.env` file in the project root.

## One-Time Setup on EC2

To enable automated backups, SSH into your EC2 instance and run the following commands. This only needs to be done once per machine.

```bash
# Navigate to your deployment directory
cd /home/ubuntu/ssew-deployment

# Run the setup script
./backup/setup_backup.sh
```

The script will install the AWS CLI and add a cron job that runs the `backup.sh` script every day at 3:00 AM.

## How to Restore from a Backup

In case of a disaster, follow these steps to restore your database from the latest backup in S3.

1.  **Identify the latest backup file** in your S3 bucket.

2.  **SSH into your EC2 machine** (or a new one if the old one is gone). Ensure Docker and `docker-compose` are installed and the `ssew-postgres` container is running.

3.  **Download and decompress the backup:**

    ```bash
    # Replace with your actual S3 bucket and backup filename
    S3_BUCKET="ssew-db-backups-staging"
    BACKUP_FILE="backup_YYYYMMDD_HHMMSS.sql.gz"

    aws s3 cp "s3://${S3_BUCKET}/postgresql/${BACKUP_FILE}" .
    gunzip "${BACKUP_FILE}"
    ```

4.  **Restore the database:**
    This command pipes the SQL dump directly into the `psql` client running inside your PostgreSQL Docker container.

    ```bash
    # Ensure you are in the ssew-deployment directory and the .env file is present
    source ./.env

    # The command will ask for the POSTGRES_PASSWORD
    docker exec -i ssew-postgres psql -U "${POSTGRES_USER}" < ${BACKUP_FILE%.gz}
    ```

## Configuration

The `backup.sh` script is configured by variables from the root `.env` file and variables at the top of the script itself. For production, you will need to ensure a new secret is available:

- `S3_BACKUP_BUCKET`: The name of the S3 bucket to store backups. It's highly recommended to use a separate bucket for staging and production backups.
