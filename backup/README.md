# Database Backup Service - Setup Guide

## Overview

This automated database backup service runs as a scheduled task within Spring Boot application. It performs daily PostgreSQL backups, compresses them, uploads to S3, and maintains only the last 3 backups.

## Features

- ✅ **Failsafe Design**: Will never crash commerce service
- ✅ **Automatic Scheduling**: Runs daily at 3:00 AM (configurable)
- ✅ **S3 Integration**: Uses existing AWS configuration
- ✅ **Compression**: GZIP compression to save storage space
- ✅ **Rotation**: Automatically keeps only the last N backups
- ✅ **Comprehensive Logging**: Detailed logs for monitoring
- ✅ **Manual Trigger**: REST API endpoint for manual backups

## File Structure

Create the following files in project:

```
commerce_service/
├── config/
│   └── SchedulingConfig.java          (NEW)
├── controller/
│   └── backup/
│       └── BackupController.java      (NEW)
└── service/
    └── backup/
        └── DatabaseBackupService.java (NEW)
```

## Installation Steps

### 1. Create the Required Directories

```bash
mkdir -p src/main/java/com/singhtwenty2/commerce_service/service/backup
mkdir -p src/main/java/com/singhtwenty2/commerce_service/controller/backup
```

### 2. Add the Files

Copy the three Java files provided:

- `DatabaseBackupService.java` → `service/backup/`
- `SchedulingConfig.java` → `config/`
- `BackupController.java` → `controller/backup/`

### 3. Update Dependencies (if needed)

Add to `pom.xml` if not already present:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
</dependency>
```

### 4. Update Configuration

Add to `application-prod.yml`:

```yaml
# Database Backup Configuration
backup:
    enabled: true
    schedule: "0 0 3 * * *" # 3:00 AM daily
    postgres:
        container-name: ssew-postgres
    s3:
        folder: database-backups
    retention:
        count: 3
```

### 5. Set Environment Variables

Add these to `.env` file (optional, only if you want to override defaults):

```bash
BACKUP_ENABLED=true
BACKUP_SCHEDULE="0 0 3 * * *"
BACKUP_POSTGRES_CONTAINER_NAME=ssew-postgres
BACKUP_S3_FOLDER=database-backups
BACKUP_RETENTION_COUNT=3
```

### 6. Verify Docker Access

Ensure Spring Boot container can execute Docker commands on the host:

**Option A: Docker Socket Mounting (Recommended)**

Update `docker-compose.yml`:

```yaml
services:
    commerce-service:
        # ... other config
        volumes:
            - /var/run/docker.sock:/var/run/docker.sock # Add this line
```

**Option B: Docker-in-Docker**

Alternatively, use Docker-in-Docker (more complex).

### 7. Build and Deploy

```bash
# Rebuild application
./mvnw clean package -DskipTests

# Restart the service
docker-compose down
docker-compose up -d
```

## Configuration Options

### Cron Schedule Format

```
 ┌───────────── second (0-59)
 │ ┌───────────── minute (0-59)
 │ │ ┌───────────── hour (0-23)
 │ │ │ ┌───────────── day of month (1-31)
 │ │ │ │ ┌───────────── month (1-12)
 │ │ │ │ │ ┌───────────── day of week (0-7)
 │ │ │ │ │ │
 * * * * * *
```

**Examples:**

- `0 0 3 * * *` - Every day at 3:00 AM
- `0 0 2 * * 0` - Every Sunday at 2:00 AM
- `0 0 */6 * * *` - Every 6 hours
- `0 30 1 * * *` - Every day at 1:30 AM

### Environment Variables

| Variable                         | Default            | Description               |
| -------------------------------- | ------------------ | ------------------------- |
| `BACKUP_ENABLED`                 | `true`             | Enable/disable backups    |
| `BACKUP_SCHEDULE`                | `0 0 3 * * *`      | Cron expression           |
| `BACKUP_POSTGRES_CONTAINER_NAME` | `ssew-postgres`    | Docker container name     |
| `BACKUP_S3_FOLDER`               | `database-backups` | S3 folder path            |
| `BACKUP_RETENTION_COUNT`         | `3`                | Number of backups to keep |

## Testing

### 1. Test Manual Backup (Recommended First)

```bash
# Trigger a manual backup via API
curl -X POST http://your-server:9000/api/v1/admin/backup/trigger \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "X-Developer-Secret: YOUR_SECRET"
```

### 2. Check Logs

```bash
# View real-time logs
docker logs -f ssew-commerce-service

# Look for these log entries:
# - "Starting scheduled database backup"
# - "Step 1/5: Creating PostgreSQL backup"
# - "Step 2/5: Compressing backup file"
# - "Step 3/5: Uploading backup to S3"
# - "Step 4/5: Rotating old backups"
# - "Step 5/5: Cleaning up temporary files"
# - "Database backup completed successfully"
```

### 3. Verify S3 Upload

```bash
# List backups in S3
aws s3 ls s3://YOUR_BUCKET/database-backups/

# You should see files like:
# backup_20241015_030000.sql.gz
# backup_20241016_030000.sql.gz
# backup_20241017_030000.sql.gz
```

### 4. Test Scheduled Execution

Wait for the scheduled time (default 3:00 AM) and check logs.

## Monitoring

### Log Levels

The backup service logs at different levels:

- **INFO**: Normal operation, progress updates
- **WARN**: Non-critical issues (e.g., cleanup failures)
- **ERROR**: Critical failures (backup will fail but app continues)

### Key Log Messages

✅ **Success Indicators:**

```
=== Starting scheduled database backup at 2024-10-15T03:00:00 ===
Step 1/5: Creating PostgreSQL backup from container 'ssew-postgres'
Backup file created: backup_20241015_030000.sql (size: 12345678 bytes)
...
=== Database backup completed successfully at 2024-10-15T03:05:23 ===
```

❌ **Failure Indicators:**

```
!!! CRITICAL: Database backup failed at 2024-10-15T03:00:00 !!!
```

## Troubleshooting

### Issue: "Docker command not found"

**Solution:** Mount Docker socket or install Docker inside container

```yaml
# docker-compose.yml
volumes:
    - /var/run/docker.sock:/var/run/docker.sock
```

### Issue: "Permission denied for Docker socket"

**Solution:** Add user to docker group or run container with appropriate permissions

```yaml
# docker-compose.yml
user: "1000:999" # user_id:docker_group_id
```

### Issue: "pg_dumpall: command not found"

**Solution:** Ensure PostgreSQL client is installed in the container

```dockerfile
# Add to Dockerfile
RUN apt-get update && apt-get install -y postgresql-client
```

### Issue: "Backup times out"

**Solution:** Increase timeout in DatabaseBackupService.java

```java
boolean finished = process.waitFor(30, TimeUnit.MINUTES); // Increase from 10
```

### Issue: "S3 upload fails"

**Solution:** Verify AWS credentials and bucket permissions

```bash
# Test AWS credentials
aws s3 ls s3://YOUR_BUCKET/

# Check IAM permissions (need: s3:PutObject, s3:ListBucket, s3:DeleteObject)
```

## Disaster Recovery

### Restoring from Backup

1. **Download backup from S3:**

```bash
aws s3 cp s3://YOUR_BUCKET/database-backups/backup_TIMESTAMP.sql.gz .
```

2. **Decompress:**

```bash
gunzip backup_TIMESTAMP.sql.gz
```

3. **Restore to database:**

```bash
docker exec -i ssew-postgres psql -U YOUR_USER < backup_TIMESTAMP.sql
```

## Security Considerations

1. **S3 Bucket Access**: Use IAM roles with minimal required permissions
2. **Encryption**: Consider enabling S3 server-side encryption
3. **Network**: Ensure backup traffic is over secure connections
4. **Credentials**: Never log database credentials
5. **API Endpoint**: The backup trigger endpoint requires ADMIN role

## Performance Impact

- **CPU**: Minimal (compression is I/O bound)
- **Memory**: ~50-100MB during backup (depends on DB size)
- **Disk**: Temporary files cleaned up immediately
- **Network**: S3 upload bandwidth (runs at 3 AM when traffic is low)
- **Database**: `pg_dumpall` locks are minimal (read-only)

## Maintenance

### Changing Backup Schedule

Update environment variable and restart:

```bash
# .env
BACKUP_SCHEDULE="0 0 2 * * *"  # Change to 2:00 AM

# Restart
docker-compose restart commerce-service
```

### Changing Retention Policy

```bash
# Keep 7 backups instead of 3
BACKUP_RETENTION_COUNT=7
```

### Disabling Backups Temporarily

```bash
BACKUP_ENABLED=false
```

## Support

If you encounter issues:

1. Check application logs: `docker logs ssew-commerce-service`
2. Verify environment variables are set correctly
3. Test manual backup via API first
4. Ensure Docker socket is accessible
5. Verify AWS credentials and S3 permissions
