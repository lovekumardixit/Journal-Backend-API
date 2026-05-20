# AWS Commands — Copy-Paste Ready

Yeh file mein exact AWS CLI commands hain jo copy-paste kar ke run kar sakte ho. Sab free tier eligible hain!

---

## 📋 Checklist

- [ ] AWS Account create kiya?
- [ ] AWS CLI install kiya? (`aws --version`)
- [ ] AWS credentials configure kiye? (`aws configure`)
- [ ] GitHub repository ready hai?
- [ ] Dockerfile prepare hai?

---

## Step 1: IAM User + Access Keys (Copy-paste commands)

```bash
# Create IAM user (github-deployer)
aws iam create-user --user-name github-deployer

# Attach S3 policy
aws iam attach-user-policy --user-name github-deployer --policy-arn arn:aws:iam::aws:policy/AmazonS3FullAccess

# Attach EC2 policy
aws iam attach-user-policy --user-name github-deployer --policy-arn arn:aws:iam::aws:policy/AmazonEC2FullAccess

# Attach ECR policy
aws iam attach-user-policy --user-name github-deployer --policy-arn arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryFullAccess

# Create access key
aws iam create-access-key --user-name github-deployer

# Output will show:
# AccessKeyId: AKIAIOSFODNN7EXAMPLE
# SecretAccessKey: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
# ⚠️ Save these! GitHub Secrets mein paste karega
```

---

## Step 2: S3 Bucket (Create karo)

```bash
# Set variables
BUCKET_NAME="journal-backend-photos-$(date +%s)"

# Create bucket
aws s3 mb s3://$BUCKET_NAME --region us-east-1

# Add public read access (CORS)
cat > /tmp/cors.json << 'EOF'
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": ["ETag", "x-amz-version-id"],
    "MaxAgeSeconds": 3000
  }
]
EOF

aws s3api put-bucket-cors --bucket $BUCKET_NAME --cors-configuration file:///tmp/cors.json

# Add bucket policy (public read)
cat > /tmp/policy.json << 'EOF'
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::BUCKET_NAME/*"
    }
  ]
}
EOF

# Replace BUCKET_NAME in policy
sed -i "s/BUCKET_NAME/$BUCKET_NAME/g" /tmp/policy.json

aws s3api put-bucket-policy --bucket $BUCKET_NAME --policy file:///tmp/policy.json

# List buckets to verify
aws s3 ls

# Note: Save bucket name: $BUCKET_NAME
```

---

## Step 3: ECR Repository

```bash
# Create ECR repository
aws ecr create-repository \
  --repository-name journal-backend \
  --region us-east-1

# Output will show repository URI:
# registryId: 123456789012
# ⚠️ Save AWS Account ID (first part of URI)

# Get your account ID
aws sts get-caller-identity --query Account --output text
# Output: 123456789012
```

---

## Step 4: EC2 Instance (Launch)

```bash
# Create key pair
aws ec2 create-key-pair \
  --key-name journal-backend-key \
  --region us-east-1 \
  --query 'KeyMaterial' \
  --output text > journal-backend-key.pem

# Set permissions (Linux/Mac)
chmod 600 journal-backend-key.pem

# Windows: run as admin
# icacls.exe journal-backend-key.pem /inheritance:r /grant:r "$($env:USERNAME):(F)" /c

# Create security group
aws ec2 create-security-group \
  --group-name journal-sg \
  --description "Security group for Journal Backend" \
  --region us-east-1

# Get security group ID (from output)
SG_ID="sg-0123456789abcdef0"  # Update this

# Add inbound rules
# SSH (port 22)
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 22 \
  --cidr 0.0.0.0/0 \
  --region us-east-1

# HTTP (port 80)
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 80 \
  --cidr 0.0.0.0/0 \
  --region us-east-1

# HTTPS (port 443)
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 443 \
  --cidr 0.0.0.0/0 \
  --region us-east-1

# Custom TCP (port 8081)
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 8081 \
  --cidr 0.0.0.0/0 \
  --region us-east-1

# Launch EC2 instance (t2.micro - free tier)
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \  # Ubuntu 22.04 LTS
  --instance-type t2.micro \
  --key-name journal-backend-key \
  --security-group-ids $SG_ID \
  --region us-east-1 \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=journal-backend-api}]'

# Get instance ID from output
# Note: InstanceId will appear in output

# Wait for instance to start
sleep 30

# Describe instances to get public IP
aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=journal-backend-api" \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text \
  --region us-east-1

# Output: 54.123.45.67 (your public IP)
```

---

## Step 5: Allocate Elastic IP (Fixed IP)

```bash
# Allocate Elastic IP
aws ec2 allocate-address \
  --domain vpc \
  --region us-east-1

# Get allocation ID from output

# Associate with instance
aws ec2 associate-address \
  --instance-id i-0123456789abcdef0 \  # Your instance ID
  --allocation-id eipalloc-0123456789abcdef0 \  # Your allocation ID
  --region us-east-1

# Verify
aws ec2 describe-addresses \
  --region us-east-1 \
  --query 'Addresses[0].PublicIp'
```

---

## Step 6: SSH into EC2 (Connect karo)

```bash
# Get your public IP
PUBLIC_IP="54.123.45.67"  # Update with actual IP

# SSH connect (Linux/Mac)
ssh -i journal-backend-key.pem ubuntu@$PUBLIC_IP

# Or use Session Manager (no SSH needed)
aws ssm start-session \
  --target i-0123456789abcdef0 \  # Your instance ID
  --region us-east-1
```

---

## Step 7: EC2 Setup (On remote machine — SSH ke baad)

```bash
# Update system
sudo apt-get update
sudo apt-get upgrade -y

# Install Docker
sudo apt-get install -y docker.io docker-compose

# Add user to docker group
sudo usermod -aG docker ubuntu

# Apply group changes
newgrp docker

# Verify Docker
docker --version
docker-compose --version

# Create app directory
mkdir -p /home/ubuntu/journal-app
cd /home/ubuntu/journal-app

# Create .env file
cat > .env << 'EOF'
MONGO_URI=mongodb+srv://your-user:your-pass@cluster.mongodb.net/userdb
JWT_SECRET=your-super-strong-secret-key-here
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
AWS_S3_BUCKET=journal-backend-photos-xxxx
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
REGISTRY=123456789012.dkr.ecr.us-east-1.amazonaws.com
ECR_REPOSITORY=journal-backend
EOF

# Create docker-compose-prod.yml (copy from repo)
# ... (paste docker-compose-prod.yml content here)
```

---

## Step 8: Docker Image Build & Push (Local Machine)

```bash
# Set variables
AWS_ACCOUNT_ID="123456789012"
REGISTRY="$AWS_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com"
ECR_REPOSITORY="journal-backend"
REGION="us-east-1"

# Login to ECR
aws ecr get-login-password --region $REGION | \
  docker login --username AWS --password-stdin $REGISTRY

# Build Docker image
docker build -t $REGISTRY/$ECR_REPOSITORY:latest \
            -t $REGISTRY/$ECR_REPOSITORY:$(date +%s) .

# Push to ECR
docker push $REGISTRY/$ECR_REPOSITORY:latest
docker push $REGISTRY/$ECR_REPOSITORY:$(date +%s)

# Verify in ECR
aws ecr describe-images --repository-name $ECR_REPOSITORY --region $REGION
```

---

## Step 9: Deploy to EC2 (Manual way)

```bash
# SSH into EC2
ssh -i journal-backend-key.pem ubuntu@$PUBLIC_IP

# On EC2:
cd /home/ubuntu/journal-app

# Login to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin $REGISTRY

# Pull latest image
docker pull $REGISTRY/$ECR_REPOSITORY:latest

# Stop old container
docker stop journal-api || true
docker rm journal-api || true

# Start new container
docker-compose -f docker-compose-prod.yml up -d

# Check logs
docker logs -f journal-api

# Verify (in another terminal or after few seconds)
curl http://localhost:8081/swagger-ui.html
```

---

## Step 10: GitHub Actions Setup (Local machine)

```bash
# Navigate to repository
cd /path/to/Backend

# Add secrets to GitHub (use GitHub CLI or web UI)
# Web UI: GitHub → Settings → Secrets and variables → Actions

# Via GitHub CLI (install from https://cli.github.com/)
gh secret set AWS_ACCOUNT_ID -b "123456789012"
gh secret set AWS_ACCESS_KEY_ID -b "AKIAIOSFODNN7EXAMPLE"
gh secret set AWS_SECRET_ACCESS_KEY -b "wJalrXUtnFEMI/..."
gh secret set EC2_PUBLIC_IP -b "54.123.45.67"
gh secret set EC2_PRIVATE_KEY < journal-backend-key.pem
gh secret set MONGO_URI -b "mongodb+srv://..."
gh secret set JWT_SECRET -b "your-secret"
gh secret set KAFKA_BOOTSTRAP_SERVERS -b "localhost:9092"
gh secret set AWS_S3_BUCKET -b "journal-backend-photos-xxxx"

# Commit and push CI/CD workflow
git add .github/workflows/deploy.yml
git commit -m "Add CI/CD pipeline"
git push origin main

# GitHub Actions trigger hoyega automatically!
# Check: GitHub → Actions → Latest workflow run
```

---

## Step 11: Monitoring & Verification

```bash
# Check EC2 status
aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=journal-backend-api" \
  --query 'Reservations[0].Instances[0].State' \
  --region us-east-1

# Check ECR images
aws ecr describe-images \
  --repository-name journal-backend \
  --region us-east-1

# Check S3 bucket
aws s3 ls s3://journal-backend-photos-xxxx/

# Check instance CPU/memory (from local machine)
aws ec2 get-console-output \
  --instance-id i-0123456789abcdef0 \
  --region us-east-1

# SSH aur docker check
ssh -i journal-backend-key.pem ubuntu@$PUBLIC_IP "docker ps"
```

---

## Step 12: Cleanup (Jab project poora ho jaye)

```bash
# Stop EC2 instance (don't delete if resume mein use karna hai!)
aws ec2 stop-instances \
  --instance-ids i-0123456789abcdef0 \
  --region us-east-1

# Terminate EC2 instance (agar chahiye to)
aws ec2 terminate-instances \
  --instance-ids i-0123456789abcdef0 \
  --region us-east-1

# Release Elastic IP
aws ec2 release-address \
  --allocation-id eipalloc-0123456789abcdef0 \
  --region us-east-1

# Delete ECR repository
aws ecr delete-repository \
  --repository-name journal-backend \
  --force \
  --region us-east-1

# Delete S3 bucket (सभी objects को पहले delete करें)
aws s3 rm s3://journal-backend-photos-xxxx --recursive
aws s3 rb s3://journal-backend-photos-xxxx

# Delete security group
aws ec2 delete-security-group \
  --group-id sg-0123456789abcdef0 \
  --region us-east-1

# Delete key pair
aws ec2 delete-key-pair \
  --key-name journal-backend-key \
  --region us-east-1
```

---

## 🎯 Quick Command Reference

```bash
# Most commonly used

# Get account ID
aws sts get-caller-identity --query Account --output text

# Get EC2 public IP
aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=journal-backend-api" \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text --region us-east-1

# SSH connect
ssh -i journal-backend-key.pem ubuntu@<EC2_IP>

# Check EC2 status
aws ec2 describe-instances --filters "Name=tag:Name,Values=journal-backend-api" --query 'Reservations[0].Instances[0].State.Name' --region us-east-1

# Docker logs on EC2
ssh -i journal-backend-key.pem ubuntu@<EC2_IP> "docker logs -f journal-api"

# Check if app is running
curl http://<EC2_IP>:8081/swagger-ui.html
```

---

## ⚠️ Important Notes

1. **Bucket Name**: हर bucket का नाम unique होना चाहिए। Timestamp add करो
2. **Account ID**: AWS Account ID सभी जगह same होना चाहिए
3. **Region**: सभी commands में `us-east-1` है (free tier के लिए best)
4. **Key File**: `journal-backend-key.pem` को safe रखो — lost हो गई तो कोई नहीं बना सकता
5. **Secrets**: GitHub secrets में कभी commit मत करो
6. **Cost**: First 12 months free है। After that ~$11-400/month depending on usage

---

## 🔗 Useful Links

- AWS CLI Install: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html
- GitHub CLI Install: https://cli.github.com/
- AWS Free Tier: https://aws.amazon.com/free/

---

## ✅ Verification Checklist

After setup complete, verify:
- [ ] `aws s3 ls` — bucket दिख रहा है?
- [ ] `aws ec2 describe-instances` — instance running है?
- [ ] `curl http://<EC2_IP>:8081` — app responding है?
- [ ] GitHub Actions workflow successful है?
- [ ] Swagger UI accessible है?
- [ ] Profile photo upload S3 में save हो रही है?

---

Last updated: May 16, 2026

