# EC2 + S3 Practical Deployment Guide — Journal Backend (Free Tier)

Yeh file step-by-step batata hai ki AWS free tier pe app kaise deploy karte hain. Sab kaam completely **free** hai first 12 months ke liye!

---

## Kya Milega Deploy Karne Ke Baad?

✅ Live URL (jisko resume mein lga sakte ho)
✅ Swagger UI live (API testing ke liye)
✅ S3 mein profile photos store ho hongi
✅ Automated CI/CD (GitHub se push → auto deploy)
✅ Docker-based deployment
✅ Free tier eligibility

---

## Architecture Diagram

```
GitHub (Code)
    ↓
GitHub Actions (CI/CD Pipeline)
    ↓
Docker Build & Push to ECR (AWS Container Registry)
    ↓
EC2 Instance (Running Docker Container)
    ↓
S3 (Profile Photos)
MongoDB (Atlas — External)
Kafka (MSK ya EC2)
```

---

## Step-by-Step Deployment Guide

### STEP 1: AWS Account Setup (5 minutes)

1) **AWS Account Create Karo** (free tier eligible):
   - Go to: https://aws.amazon.com/free/
   - Click "Create a free account"
   - Credit card add karo (charges nahi honge free tier mein)
   - Verify email aur phone

2) **IAM User Create Karo** (for CI/CD):
   - AWS Console → IAM → Users → Create user
   - Username: `github-deployer`
   - Next → Attach policies:
     - `AmazonEC2FullAccess`
     - `AmazonECS_FullAccess`
     - `AmazonECRFullAccess`
     - `IAMFullAccess`
     - `AmazonS3FullAccess`
   - Next → Create user
   - Access key generate karo (CI/CD ke liye zaroori):
     - Click user → Security credentials → Create access key
     - Download CSV ya note karo: `ACCESS_KEY_ID` aur `SECRET_ACCESS_KEY`

---

### STEP 2: S3 Bucket Create Karo (2 minutes)

1) **Bucket Create**:
   ```
   AWS Console → S3 → Create bucket
   - Bucket name: journal-backend-photos-<your-name>
     (unique hona chahiye, e.g., journal-backend-photos-love123)
   - Region: us-east-1 (Free tier mein best)
   - Next → Block Public Access settings:
     - Uncheck "Block public access" (taki files publicly accessible rahen)
   - Create bucket
   ```

2) **CORS Policy Add Karo** (taki frontend se access kar sake):
   ```
   Bucket → Permissions → CORS
   ```
   Paste yeh:
   ```json
   [
     {
       "AllowedHeaders": ["*"],
       "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
       "AllowedOrigins": ["*"],
       "ExposeHeaders": ["ETag", "x-amz-version-id"],
       "MaxAgeSeconds": 3000
     }
   ]
   ```

3) **Bucket Policy Set Karo** (public read):
   ```
   Bucket → Permissions → Bucket Policy
   ```
   Paste yeh:
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Principal": "*",
         "Action": "s3:GetObject",
         "Resource": "arn:aws:s3:::journal-backend-photos-<your-name>/*"
       }
     ]
   }
   ```

---

### STEP 3: EC2 Instance Launch Karo (5 minutes)

1) **EC2 Dashboard → Instances → Launch Instances**
   - Name: `journal-backend-api`
   - AMI: Ubuntu 22.04 LTS (Free tier eligible)
   - Instance type: t2.micro (Free tier)
   - Key pair: Create new
     - Name: `journal-backend-key`
     - Download: `.pem` file save karo locally
   - Security group:
     - Create new: `journal-sg`
     - Inbound rules:
       ```
       SSH: 22, Source: 0.0.0.0/0 (only for dev — prod mein restrict karo)
       HTTP: 80, Source: 0.0.0.0/0
       HTTPS: 443, Source: 0.0.0.0/0
       Custom TCP: 8081 (app port), Source: 0.0.0.0/0
       ```
   - Storage: 20 GB (default — free tier)
   - Launch instances

2) **Elastic IP Assign Karo** (taki restart ke baad IP na badle):
   ```
   EC2 → Elastic IPs → Allocate address
   Select instance aur associate
   ```

3) **Note Karo**:
   - Public IP (e.g., `54.123.45.67`)
   - Private IP (e.g., `10.0.1.100`)

---

### STEP 4: EC2 Setup — Docker + App Deployment (10 minutes)

1) **SSH Connect Karo** (from your terminal):
   ```bash
   # Windows (PowerShell) ya Git Bash
   # .pem file ke liye permissions set karo (Windows)
   icacls.exe journal-backend-key.pem /inheritance:r /grant:r "$($env:USERNAME):(F)" /c
   
   # SSH karo
   ssh -i journal-backend-key.pem ubuntu@<EC2_PUBLIC_IP>
   
   # Linux/Mac
   chmod 600 journal-backend-key.pem
   ssh -i journal-backend-key.pem ubuntu@<EC2_PUBLIC_IP>
   ```

2) **Docker Install Karo** (on EC2):
   ```bash
   # Update
   sudo apt-get update
   
   # Docker install
   sudo apt-get install -y docker.io docker-compose
   
   # Docker permission
   sudo usermod -aG docker ubuntu
   newgrp docker
   
   # Verify
   docker --version
   ```

3) **App Folder Create Karo**:
   ```bash
   cd /home/ubuntu
   mkdir journal-app
   cd journal-app
   ```

4) **Docker Compose File Create Karo** (EC2 par):
   ```bash
   nano docker-compose-prod.yml
   ```
   Yeh paste karo:
   ```yaml
   version: '3.8'
   services:
     app:
       image: <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/journal-backend:latest
       container_name: journal-api
       ports:
         - "8081:8081"
         - "80:8081"
       environment:
         - SPRING_PROFILES_ACTIVE=prod
         - MONGO_URI=${MONGO_URI}
         - JWT_SECRET=${JWT_SECRET}
         - SERVER_PORT=8081
         - KAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS}
         - AWS_REGION=us-east-1
         - AWS_S3_BUCKET=journal-backend-photos-<your-name>
         - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
         - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
       restart: always
       logging:
         driver: "awslogs"
         options:
           awslogs-group: "/ecs/journal-backend"
           awslogs-region: "us-east-1"
           awslogs-stream-prefix: "ecs"
   ```

5) **Environment File Create Karo** (EC2 par):
   ```bash
   nano .env
   ```
   Paste karo:
   ```
   MONGO_URI=mongodb+srv://user:password@cluster.mongodb.net/dbname
   JWT_SECRET=your_jwt_secret_key_here
   KAFKA_BOOTSTRAP_SERVERS=localhost:9092
   AWS_ACCESS_KEY_ID=your_aws_key
   AWS_SECRET_ACCESS_KEY=your_aws_secret
   ```

---

### STEP 5: GitHub Actions CI/CD Setup (5 minutes)

1) **GitHub Repo mein `.github/workflows/deploy.yml` Create Karo**:

   ```bash
   # Local machine mein (repository folder)
   mkdir -p .github/workflows
   nano .github/workflows/deploy.yml
   ```

2) **Yeh paste karo**:

   ```yaml
   name: Deploy to AWS EC2
   
   on:
     push:
       branches:
         - main
         - master
   
   env:
     AWS_REGION: us-east-1
     ECR_REPOSITORY: journal-backend
     REGISTRY: ${{ secrets.AWS_ACCOUNT_ID }}.dkr.ecr.us-east-1.amazonaws.com
   
   jobs:
     build-and-push:
       runs-on: ubuntu-latest
       
       steps:
         - name: Checkout Code
           uses: actions/checkout@v3
         
         - name: Set up JDK 17
           uses: actions/setup-java@v3
           with:
             java-version: '17'
             distribution: 'temurin'
         
         - name: Build with Maven
           run: mvn clean package -DskipTests
         
         - name: Configure AWS Credentials
           uses: aws-actions/configure-aws-credentials@v2
           with:
             aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
             aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
             aws-region: ${{ env.AWS_REGION }}
         
         - name: Login to ECR
           run: aws ecr get-login-password --region ${{ env.AWS_REGION }} | docker login --username AWS --password-stdin ${{ env.REGISTRY }}
         
         - name: Build Docker Image
           run: docker build -t ${{ env.REGISTRY }}/${{ env.ECR_REPOSITORY }}:latest .
         
         - name: Push to ECR
           run: docker push ${{ env.REGISTRY }}/${{ env.ECR_REPOSITORY }}:latest
         
         - name: Deploy to EC2
           env:
             EC2_USER: ubuntu
             EC2_HOST: ${{ secrets.EC2_PUBLIC_IP }}
             EC2_KEY: ${{ secrets.EC2_PRIVATE_KEY }}
           run: |
             mkdir -p ~/.ssh
             echo "${{ env.EC2_KEY }}" > ~/.ssh/id_rsa
             chmod 600 ~/.ssh/id_rsa
             ssh -o StrictHostKeyChecking=no -i ~/.ssh/id_rsa ${{ env.EC2_USER }}@${{ env.EC2_HOST }} << 'EOF'
               cd /home/ubuntu/journal-app
               docker pull ${{ env.REGISTRY }}/${{ env.ECR_REPOSITORY }}:latest
               docker stop journal-api || true
               docker rm journal-api || true
               docker-compose -f docker-compose-prod.yml up -d
               echo "Deployment successful!"
             EOF
   ```

3) **GitHub Secrets Add Karo** (Settings → Secrets and variables → Actions):
   ```
   AWS_ACCOUNT_ID: your-aws-account-id
   AWS_ACCESS_KEY_ID: xxxxxxxx
   AWS_SECRET_ACCESS_KEY: xxxxxxxx
   EC2_PUBLIC_IP: 54.123.45.67
   EC2_PRIVATE_KEY: (paste .pem file content)
   ```

4) **Git Push Karo**:
   ```bash
   git add .github/workflows/deploy.yml
   git commit -m "Add CI/CD pipeline"
   git push origin main
   ```

   **Auto deployment trigger hoyega!** 🎉

---

### STEP 6: Production Configuration Files (Application Setup)

1) **Create `application-prod.yml`** (repository mein):

   ```yaml
   server:
     port: ${SERVER_PORT:8081}
   
   spring:
     profiles: prod
     data:
       mongodb:
         uri: ${MONGO_URI}
       redis:
         host: localhost
         port: 6379
     kafka:
       bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
       producer:
         key-serializer: org.apache.kafka.common.serialization.StringSerializer
         value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
       consumer:
         group-id: ${KAFKA_CONSUMER_GROUP:my-group}
         key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
         value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
         properties:
           spring.json.trusted.packages: "*"
   
   jwt:
     secret: ${JWT_SECRET}
   
   aws:
     s3:
       enabled: true
       bucket: ${AWS_S3_BUCKET}
       base-url: https://${AWS_S3_BUCKET}.s3.${AWS_REGION}.amazonaws.com
     region: ${AWS_REGION:us-east-1}
   
   logging:
     level:
       root: INFO
       com.love.Backend: DEBUG
   ```

2) **Update `pom.xml`** (add AWS S3 dependency):

   ```xml
   <dependency>
     <groupId>software.amazon.awssdk</groupId>
     <artifactId>s3</artifactId>
     <version>2.20.0</version>
   </dependency>
   ```

---

### STEP 7: Test Deployment

1) **Check Application Health** (browser mein):
   ```
   http://<EC2_PUBLIC_IP>:8081/user/me
   (Authorize first Swagger se — GET /user/me)
   ```

2) **Swagger Access** (full API docs):
   ```
   http://<EC2_PUBLIC_IP>:8081/swagger-ui.html
   ```

3) **Check Docker Logs** (EC2 par):
   ```bash
   docker logs -f journal-api
   ```

4) **S3 Upload Test** (profile photo):
   ```bash
   curl -X POST "http://<EC2_PUBLIC_IP>:8081/user/profile-photo/upload" \
     -H "Authorization: Bearer <JWT_TOKEN>" \
     -F "file=@/path/to/photo.jpg"
   ```

---

### STEP 8: Production Checklist

- [ ] EC2 instance running aur healthy
- [ ] Security groups properly configured
- [ ] S3 bucket working aur CORS enabled
- [ ] GitHub Actions workflow successfully deployed
- [ ] Application logs mein koi error nahi
- [ ] Swagger UI accessible
- [ ] Profile photo upload S3 mein store ho raha hai
- [ ] MongoDB connection working
- [ ] Kafka connection setup (MSK ya self-hosted)
- [ ] Elastic IP assigned (public IP fixed)

---

## Live URL aur Resume Entry

**Deploy karne ke baad, resume mein likha sakta ho:**

```
🚀 Journal Backend API (Live)
- Live URL: http://<EC2_PUBLIC_IP>:8081
- Swagger UI: http://<EC2_PUBLIC_IP>:8081/swagger-ui.html
- Architecture: Docker + AWS EC2 + S3 + MongoDB + Kafka + GitHub Actions CI/CD
- Technologies: Spring Boot 3.x, Java 17, MongoDB, Kafka, AWS (EC2, S3, ECR)
- Features: JWT Auth, Profile Photos (S3), Sentiment Analysis, Weather Integration
- Deployment: Automated CI/CD via GitHub Actions (push → Docker build → ECR → EC2)
```

---

## Troubleshooting

### Issue 1: Docker Push Fail
```bash
# Solution: ECR repository create karo
aws ecr create-repository --repository-name journal-backend --region us-east-1
```

### Issue 2: EC2 Connection Timeout
```bash
# Security group mein SSH (port 22) allow karo
# Or use Session Manager (AWS Console → Systems Manager → Session Manager)
```

### Issue 3: S3 Upload Failing
```bash
# Check IAM user permissions aur S3 bucket policy
# Verify AWS credentials in .env file
```

### Issue 4: Kafka Connection Error
```bash
# Kafka broker address check karo (MSK ya self-hosted)
# Security groups mein port 9092 allow karo
```

---

## Cost Estimate (12 months free tier)

| Service | Free Tier Benefit | Actual Cost (After) |
|---------|-----------------|-------------------|
| EC2 | 750 hours/month | ~$10-20/month |
| S3 | 5 GB storage | ~$1/month |
| Bandwidth | 100 GB/month | ~$0-5/month |
| Kafka (MSK) | None (not eligible) | $300-400/month |
| Kafka (EC2 Self) | Included in EC2 | Included |
| MongoDB (Atlas) | Free tier | ~$0-9/month |
| **Total** | **Free** | **~$11-434/month** |

**Recommendation**: Use self-hosted Kafka on EC2 to keep costs minimal (~$11-20/month after free tier).

---

## Next Steps

1. ✅ AWS account setup
2. ✅ S3 bucket create
3. ✅ EC2 instance launch
4. ✅ Docker setup
5. ✅ GitHub Actions pipeline
6. ✅ Deploy code
7. ✅ Test live URL
8. ✅ Add to resume

---

**Notes:**
- Free tier valid: 12 months from account creation
- After 12 months, costs apply (see cost estimate above)
- For production, use CloudFront + Route 53 for better performance
- Setup CloudWatch alarms for monitoring

Last updated: May 16, 2026

