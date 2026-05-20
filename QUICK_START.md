# Quick Start Guide — Journal Backend Deployment

Step-by-step guide jo 5 minutes mein production par deploy kar dega.

---

## Video Overview

```
Local Development → GitHub Push → CI/CD Pipeline → Docker Build → AWS ECR → EC2 Deploy
```

---

## Prerequisites

- ✅ Git installed
- ✅ Java 17+ installed
- ✅ Maven 3.x+ installed
- ✅ Docker Desktop installed
- ✅ GitHub account (with repository access)
- ✅ AWS account (free tier)

---

## Local Development (5 min)

### Windows (PowerShell)
```powershell
# Clone repo (agar already nahi hai)
git clone https://github.com/your-username/Backend.git
cd Backend

# Setup script chalao (sab kuch install + start)
powershell -ExecutionPolicy Bypass -File setup.ps1

# Application start hoyega automatically
# Swagger UI: http://localhost:8081/swagger-ui.html
```

### Linux/Mac (Bash)
```bash
git clone https://github.com/your-username/Backend.git
cd Backend

chmod +x setup.sh
./setup.sh

# Application ready
# Swagger UI: http://localhost:8081/swagger-ui.html
```

---

## Production Deployment (15 min)

### Step 1: AWS Setup

```bash
# 1. AWS Account banana (https://aws.amazon.com/free/)
# 2. IAM user banana: github-deployer (S3, EC2, ECR access)
# 3. Access keys generate karo
# 4. S3 bucket banana: journal-backend-photos-<your-name>
# 5. EC2 instance launch karo (t2.micro, Ubuntu 22.04)
```

### Step 2: GitHub Secrets Add Karo

```
GitHub Repo → Settings → Secrets and variables → Actions → New repository secret
```

Add ye secrets:
```
AWS_ACCOUNT_ID = 123456789012
AWS_ACCESS_KEY_ID = AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY = wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
EC2_PUBLIC_IP = 54.123.45.67
EC2_PRIVATE_KEY = (content of .pem file)
MONGO_URI = mongodb+srv://user:pass@cluster.mongodb.net/dbname
JWT_SECRET = your-super-secret-key
KAFKA_BOOTSTRAP_SERVERS = localhost:9092
AWS_S3_BUCKET = journal-backend-photos-<your-name>
```

### Step 3: EC2 Setup

```bash
# SSH into EC2
ssh -i journal-backend-key.pem ubuntu@<EC2_PUBLIC_IP>

# Docker install
sudo apt-get update
sudo apt-get install -y docker.io docker-compose
sudo usermod -aG docker ubuntu

# App folder
mkdir -p /home/ubuntu/journal-app
cd /home/ubuntu/journal-app

# Copy docker-compose-prod.yml (ye file repository mein hai)
# ya create karo:
nano docker-compose-prod.yml
# (paste docker-compose-prod.yml content from repo)

# .env file
nano .env
# (add all env variables)
```

### Step 4: Deploy

```bash
# Option 1: Manual push aur auto-deploy
git add .
git commit -m "Deploy to production"
git push origin main

# GitHub Actions automatically deploy karega! ✅

# Option 2: Manual deploy (agar CI/CD nahi chahiye)
docker pull <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/journal-backend:latest
docker-compose -f docker-compose-prod.yml up -d
```

### Step 5: Verify

```bash
# Check container logs
docker logs -f journal-api

# Test API
curl http://localhost:8081/swagger-ui.html

# Browser mein
http://<EC2_PUBLIC_IP>:8081/swagger-ui.html
```

---

## Live URLs (Resume ke liye)

```
Main API: http://<EC2_PUBLIC_IP>:8081
Swagger UI: http://<EC2_PUBLIC_IP>:8081/swagger-ui.html
OpenAPI Docs: http://<EC2_PUBLIC_IP>:8081/v3/api-docs
Health Check: http://<EC2_PUBLIC_IP>:8081/actuator/health
```

---

## Important: Environment Variables (Zyada nhi chhedo!)

❌ **Mat chhedo**:
- `application-dev.yml` — dev config
- `src/main/resources/*` — source files
- Kafka topic names

✅ **Production mein set karo**:
```
MONGO_URI = your-atlas-cluster
JWT_SECRET = strong-random-secret
KAFKA_BOOTSTRAP_SERVERS = your-kafka-broker
AWS_S3_BUCKET = your-bucket-name
AWS_ACCESS_KEY_ID = your-key
AWS_SECRET_ACCESS_KEY = your-secret
```

---

## Kafka Setup (Optional - Resume ke liye impress)

### Development (already setup)
```bash
# Docker compose mein Kafka already chalti hai
docker-compose logs -f kafka

# Register karo test user
# Kafka consumers welcome message denge
```

### Production

**Option 1: AWS MSK** (Recommended)
```
AWS Console → MSK → Create Cluster
→ Select brokers, storage
→ Get bootstrap servers
→ Set KAFKA_BOOTSTRAP_SERVERS env var
→ Done! Fully managed ✅
```

**Option 2: EC2 Self-Hosted** (Cheap)
```bash
# EC2 par
cd /opt
sudo wget https://archive.apache.org/dist/kafka/3.5.0/kafka_2.13-3.5.0.tgz
sudo tar -xzf kafka_2.13-3.5.0.tgz
sudo mv kafka_2.13-3.5.0 /opt/kafka

# Start
nohup /opt/kafka/bin/zookeeper-server-start.sh /opt/kafka/config/zookeeper.properties > /tmp/zk.log 2>&1 &
nohup /opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties > /tmp/kafka.log 2>&1 &

# Set env var: KAFKA_BOOTSTRAP_SERVERS=<EC2_IP>:9092
```

---

## GitHub Actions CI/CD (Auto-Deploy)

```
flowchart:
1. git push origin main
2. GitHub Actions trigger (`.github/workflows/deploy.yml`)
3. Maven build
4. Docker image build
5. Push to ECR
6. SSH into EC2
7. Pull image
8. docker-compose up -d
9. App deployed! ✅
```

**Check deployment status**: GitHub Repo → Actions → Latest workflow

---

## Resume Entry Template

```
🚀 Journal Backend API

Live URL: http://<EC2_PUBLIC_IP>:8081
Swagger UI: http://<EC2_PUBLIC_IP>:8081/swagger-ui.html

Technologies:
- Backend: Spring Boot 3.x, Java 17
- Database: MongoDB (Atlas)
- Cache: Redis
- Message Queue: Kafka (AWS MSK or self-hosted)
- File Storage: AWS S3
- Cloud: AWS (EC2, ECR, S3, IAM)
- CI/CD: GitHub Actions + Docker

Features:
✅ JWT Authentication with refresh tokens
✅ Role-based access (USER, ADMIN)
✅ Profile photos on AWS S3
✅ Sentiment analysis on journal entries
✅ Weather integration for greetings
✅ Kafka event streaming (user registration)
✅ Email notifications
✅ OpenAPI (Swagger) documentation
✅ Automated CI/CD pipeline with GitHub Actions

Deployment:
- Dockerized application
- Automated CI/CD via GitHub Actions (push → deploy)
- Running on AWS EC2 (free tier)
- S3 for profile photo storage
- MongoDB Atlas for database
- Production-ready with health checks and logging
```

---

## Troubleshooting

### Problem 1: Docker build fails
```bash
# Solution: Clean aur rebuild
docker system prune -a
mvn clean package -DskipTests
docker build -t journal-backend:latest .
```

### Problem 2: EC2 connection timeout
```bash
# Check security group
AWS Console → EC2 → Security Groups → journal-sg
→ Inbound rules: SSH (22), HTTP (80), Custom TCP (8081) are open

# Or use Session Manager (no SSH needed)
AWS Console → Systems Manager → Session Manager
```

### Problem 3: S3 upload fails
```bash
# Check permissions
AWS IAM → Users → github-deployer
→ Policies: AmazonS3FullAccess attached?

# Check bucket policy
AWS S3 → Bucket → Permissions → Bucket policy → Public read enabled?
```

### Problem 4: Kafka not connecting
```bash
# Check Kafka broker
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Production: Check security group mein port 9092 allow hai
# Set KAFKA_BOOTSTRAP_SERVERS correctly
```

---

## Quick Commands

```bash
# Local
./setup.ps1                          # Windows full setup
./setup.sh                           # Linux/Mac full setup
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Docker (local)
docker-compose up -d                 # Start services
docker-compose down                  # Stop services
docker logs -f journal-mongo         # View logs
docker exec kafka <command>          # Run Kafka command

# Git
git add .github/workflows/deploy.yml # Add CI/CD config
git push origin main                 # Trigger auto-deploy

# AWS
aws s3 ls s3://bucket-name/          # List S3 files
aws ec2 describe-instances           # List EC2 instances
aws ecr describe-repositories         # List ECR repos
```

---

## Final Checklist Before Submitting

- [ ] Local setup works (Swagger UI accessible)
- [ ] GitHub repo updated with CI/CD pipeline (`.github/workflows/deploy.yml`)
- [ ] AWS account setup (IAM, S3, EC2, ECR)
- [ ] EC2 instance running aur accessible
- [ ] GitHub Actions successfully deployed code
- [ ] Live URL accessible in browser
- [ ] Swagger UI working
- [ ] S3 profile photo upload working
- [ ] Kafka setup (dev mein local, prod mein MSK/EC2)
- [ ] Environment variables properly set (no hardcoded secrets)
- [ ] Resume updated with live URLs aur tech stack

---

## Support URLs

- 📚 Official Documentation: https://github.com/your-username/Backend
- 🐛 Report Issues: GitHub Issues
- 📞 Contact: cyber.lavdixit@gmail.com

---

Happy Deploying! 🚀

Last updated: May 16, 2026

