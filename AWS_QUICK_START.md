AWS Quick Start — Simple, practical, beginner-friendly guide

Goal
- Quickly learn AWS basics and deploy this Spring Boot backend with minimal cost (prefer free-tier).
- Short theory, lots of step-by-step commands and exact clicks.

1) Very short AWS concepts (in simple language)
- AWS = cloud provider (like a remote computer shop).
- EC2 = virtual machine (you get a server you can SSH into).
- S3 = object storage (upload files, backups, static assets).
- IAM = identities & permissions (who can do what). Use least privilege.
- VPC & Security Groups = network and firewall for your servers.
- Elastic Beanstalk / ECS = platforms to run apps more easily (optional).

2) Create AWS Free Tier account (step-by-step)
- Go to https://aws.amazon.com/ and click "Create an AWS Account".
- Use your real email and create password; provide phone and address when asked.
- You must add a credit card; free-tier still needs card for verification but you won't be charged if you stay inside free limits.
- Verify phone (SMS/call) and choose Basic Support (free).
- After confirmation, sign in to AWS Management Console.

3) Set up IAM best-practice user (do not use root)
- In console, open "IAM" → "Users" → "Add users".
- Create a user named e.g. "dev-yourname". Check "AWS Management Console access" and create password OR check "Programmatic access" if you want AWS CLI use.
- Attach existing policies: start with "AdministratorAccess" to learn, but for production create least-privilege policies. For practice it's OK if account limited.
- Click create and save the user credentials (Access Key ID / Secret) if programmatic access used.
- Sign out from root and sign in with this new IAM user for day-to-day tasks.

4) Install AWS CLI locally (so you can deploy from your laptop)
- Windows PowerShell steps:
  - Download and install AWS CLI MSI from https://aws.amazon.com/cli/ or run:
    choco install awscli   (if you use Chocolatey)
  - Configure:
    aws configure
    - Enter Access Key ID, Secret Access Key, region (e.g. ap-south-1), default output json

5) Basic: Launch a free-tier EC2 instance (fastest way to get public IP)
- Console steps:
  - Open EC2 → Instances → Launch instances.
  - Name: backend-dev
  - Image: Amazon Linux 2 AMI (or Ubuntu 22.04 LTS)
  - Instance type: t2.micro or t3.micro (free tier eligible depends on account)
  - Key pair: Create new key pair (download .pem) — save it safely.
  - Network settings: Create or use default VPC. Security group: allow inbound 22 (SSH), 80 (HTTP), 443 (HTTPS) from My IP or 0.0.0.0/0 (for public testing). For security, prefer restricting SSH to your IP.
  - Launch instance.
- Connect to EC2:
  - From PowerShell (on Windows use WSL or putty) — example with OpenSSH in PowerShell/WSL:
    ssh -i C:\path\to\key.pem ec2-user@<EC2_PUBLIC_IP>
  - For Ubuntu use ubuntu@<ip>

6) Deploy Spring Boot jar to EC2 quickly
- Build a fat jar locally:
  - mvn clean package -DskipTests
  - target/Backend-0.0.1-SNAPSHOT.jar created
- Copy jar to server:
  - scp -i C:\path\to\key.pem target/Backend-0.0.1-SNAPSHOT.jar ec2-user@<EC2_PUBLIC_IP>:~/
- On EC2:
  - sudo yum install -y java-17-amazon-corretto (or apt install openjdk-17-jdk)
  - java -jar Backend-0.0.1-SNAPSHOT.jar --server.port=8080 &
  - Or create a systemd service to run as daemon.
- Access from browser: http://<EC2_PUBLIC_IP>:8080

7) Simpler: Use Elastic Beanstalk (free-friendly)
- Elastic Beanstalk automatically handles EC2, load balancer, logs.
- Steps:
  - Install EB CLI (pip install awsebcli)
  - From project root:
    eb init --platform java --region ap-south-1
    eb create backend-env --instance_type t2.micro
    eb deploy
  - After deploy, eb open will open the public URL.
- EB will run your jar by detecting the build artifact or you can add Procfile.

8) Use S3 for file storage (practical)
- Console steps:
  - Open S3 → Create bucket → choose unique name, region, keep default options for private bucket.
  - From app, use AWS SDK and an IAM user with S3 permission to upload/download.
- Quick AWS CLI example:
  - aws s3 cp myfile.png s3://your-bucket-name/
  - aws s3 ls s3://your-bucket-name/

9) IAM role for EC2 (recommended for production)
- Instead of embedding AWS keys in app, create IAM Role with required policies (S3 access) and attach to EC2 instance.
- App on EC2 (or EB) will automatically get temporary credentials from instance metadata.

10) Domain or public IP to access project
- If you have a domain, create an A-record pointing to your EC2 public IPv4 address.
- For Elastic Beanstalk, use the provided CNAME (e.g., backend-env.us-east-1.elasticbeanstalk.com) or create a CNAME record to point to it.
- If you want a static IP for EC2: allocate an Elastic IP (EC2 -> Network & Security -> Elastic IPs) and associate to your instance (apps with changing IPs will break DNS).

11) Security basics (practical)
- Do not use root account; use IAM users/roles.
- Keep security groups tight: only open ports you need, restrict SSH to your IP.
- Use HTTPS: get a TLS cert using AWS Certificate Manager + CloudFront/ALB (for production) or Let's Encrypt on the server.
- Do not store secrets in code. Use environment variables, Parameter Store, or Secrets Manager.
- Use S3 bucket policies to restrict public read unless purposely hosting static site.

12) Common mistakes and how to avoid them
- Forgetting to open port in Security Group: server not reachable. Solution: add inbound rule.
- Leaving SSH open (0.0.0.0/0) — risk. Restrict to your IP.
- Storing AWS keys in code or git — use IAM role or secrets manager.
- Not using Elastic IP when using domain for EC2 — IP changes on stop/start. Use Elastic IP.
- Using production DB on tests — isolate test DB or use embedded/LocalStack for tests.

13) Tips to deploy our Spring Boot project cheaply
- Use a single t2.micro EC2 (free tier) for basic testing.
- Use environment variables for config: set spring.profiles.active=prod and provide DB/Redis endpoints.
- For persistence use managed services if budget allows (RDS, DocumentDB, or Atlas MongoDB free tier).
- For small static backend, use Elastic Beanstalk (easy) or EC2 + cron/service.

14) Quick checklist for your first deployment (copy-paste)
- [ ] Create IAM user, configure AWS CLI.
- [ ] Launch t2.micro EC2, create key-pair.
- [ ] Build jar: mvn clean package -DskipTests
- [ ] scp -i key.pem target/Backend-0.0.1-SNAPSHOT.jar ec2-user@<IP>:/home/ec2-user/
- [ ] SSH and run: java -jar Backend-0.0.1-SNAPSHOT.jar --server.port=8080 &
- [ ] Open http://<IP>:8080 and test endpoints.

15) Extra resources
- AWS Free Tier: https://aws.amazon.com/free/
- Elastic Beanstalk: https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/Welcome.html
- S3: https://docs.aws.amazon.com/s3/index.html
- IAM best practices: https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html

Notes and final advice
- For practice, start with EC2 + manual deploys. When comfortable, use Elastic Beanstalk or CI/CD (GitHub Actions) to automate.
- Keep costs low: stop instance when not in use, delete unused resources (EIPs, buckets, snapshots).

---
If you want, I can now:
- add a simple `eb` deployment config to this repo (Procfile or Dockerrun) so you can run `eb deploy` quickly; or
- provide a short, one-command script to build & scp & restart the app on EC2.


