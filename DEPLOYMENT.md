# TurnoCRM Deployment Guide

## Architecture

- **Backend**: Spring Boot JAR on AWS EC2
- **Database**: AWS RDS PostgreSQL 16
- **Frontend**: S3 + CloudFront (static site)
- **File uploads**: Local filesystem on EC2 (`/var/turnocrm/uploads`)

---

## 1. AWS RDS Setup

1. Create a PostgreSQL 16 instance in RDS
2. Note the endpoint (e.g. `turnocrm.xxxxx.us-east-1.rds.amazonaws.com`)
3. Connect and create the database and user:
   ```sql
   CREATE DATABASE turnocrm;
   CREATE USER turnocrm_app WITH PASSWORD 'your-strong-password';
   GRANT ALL PRIVILEGES ON DATABASE turnocrm TO turnocrm_app;
   ```
4. Flyway will run all migrations automatically on first backend startup

---

## 2. EC2 Setup

### Install Java 17
```bash
# Amazon Linux 2023
sudo yum install java-17-amazon-corretto -y

# Ubuntu
sudo apt install openjdk-17-jre -y
```

### Create app directory and user
```bash
sudo useradd -r -s /bin/false turnocrm
sudo mkdir -p /opt/turnocrm
sudo mkdir -p /var/turnocrm/uploads
sudo chown turnocrm:turnocrm /opt/turnocrm /var/turnocrm/uploads
```

### Create environment file
```bash
sudo cp .env.production.example /opt/turnocrm/.env
sudo nano /opt/turnocrm/.env
```

Fill in your RDS endpoint, credentials, JWT secret, and CloudFront domain:
```
DB_URL=jdbc:postgresql://your-rds-endpoint:5432/turnocrm
DB_USERNAME=turnocrm_app
DB_PASSWORD=your-strong-password
JWT_SECRET=$(openssl rand -base64 64)
JWT_EXPIRY=86400000
SERVER_PORT=8080
UPLOAD_PATH=/var/turnocrm/uploads
CORS_ORIGINS=https://your-cloudfront-domain.cloudfront.net
LAUNCH_DATE=2026-04-01
BACKFILL_DEADLINE=2026-05-01
```

### Install systemd service
```bash
sudo cp scripts/turnocrm.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable turnocrm
```

### Security group
Open port **8080** (or 443 if using a load balancer/reverse proxy) for inbound traffic.

---

## 3. S3 + CloudFront Setup

1. **Create S3 bucket** (e.g. `turnocrm-frontend`)
   - Block all public access (CloudFront will use OAC)

2. **Create CloudFront distribution**
   - Origin: your S3 bucket (use Origin Access Control)
   - Default root object: `index.html`
   - Custom error response: **403 → /index.html (200)** and **404 → /index.html (200)** (required for SPA routing)

3. **Note the CloudFront domain** and add it to the backend's `CORS_ORIGINS` in `/opt/turnocrm/.env`

---

## 4. Deploying

### Backend
```bash
EC2_HOST=your-ec2-ip EC2_USER=ec2-user ./scripts/deploy-ec2.sh
```

### Frontend
```bash
S3_BUCKET=turnocrm-frontend \
CLOUDFRONT_DIST_ID=EXXXXXXXXX \
API_BASE_URL=https://your-ec2-ip:8080/api/v1 \
./scripts/deploy-frontend.sh
```

---

## 5. Managing the Backend

```bash
# Check status
sudo systemctl status turnocrm

# View logs
sudo journalctl -u turnocrm -f

# Restart
sudo systemctl restart turnocrm

# Stop
sudo systemctl stop turnocrm
```

---

## Local Development

Install PostgreSQL locally (Homebrew: `brew install postgresql@16`).

```bash
# Create local database
createdb turnocrm

# Start backend
./scripts/build-backend.sh
./scripts/run-backend.sh

# Start frontend (in another terminal)
cd frontend && npm run dev
```

The backend reads from `.env` in the project root. Default config connects to `localhost:5432`.
