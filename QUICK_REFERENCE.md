# 🎯 QUICK REFERENCE CARD

**Sabse zaroori file names aur commands — one page mein!**

---

## 📖 Start Kahan Se?

```
START_HERE.md ← FIRST OPEN THIS
    ↓
QUICK_START.md (5 min local setup)
    ↓
EC2_S3_DEPLOYMENT.md (production)
    ↓
AWS_COMMANDS.md (copy-paste)
```

---

## 🚀 3 Quick Commands

### **Windows Local Setup (5 min)**
```powershell
powershell -ExecutionPolicy Bypass -File setup.ps1
# Wait...
# Open: http://localhost:8081/swagger-ui.html
```

### **Linux/Mac Local Setup (5 min)**
```bash
bash setup.sh
# Wait...
# Open: http://localhost:8081/swagger-ui.html
```

### **Production Deploy (auto)**
```bash
git push origin main
# GitHub Actions auto deploy karega! ✅
# Check: GitHub → Actions → Latest workflow
```

---

## 📁 Key Files

### **Read These**
| File | Content | Time |
|------|---------|------|
| `START_HERE.md` | Overview | 5 min |
| `QUICK_START.md` | Local setup | 5 min |
| `EC2_S3_DEPLOYMENT.md` | Production | 20 min |
| `KAFKA_GUIDE.md` | Events | 15 min |
| `AWS_COMMANDS.md` | AWS CLI | Copy-paste |

### **Use These**
| File | Purpose |
|------|---------|
| `setup.ps1` / `setup.sh` | Auto setup |
| `docker-compose.yml` | Local services |
| `docker-compose-prod.yml` | Production services |
| `.github/workflows/deploy.yml` | CI/CD |
| `Dockerfile` | Container image |
| `application-prod.yml` | Production config |

---

## 🔑 Environment Variables

```bash
# These are set automatically in production via:
# - GitHub Secrets
# - .env file on EC2
# - docker-compose environment

MONGO_URI                  # MongoDB connection
JWT_SECRET                 # JWT signing key
KAFKA_BOOTSTRAP_SERVERS    # Kafka broker
AWS_S3_BUCKET              # S3 bucket name
AWS_ACCESS_KEY_ID          # AWS credentials
AWS_SECRET_ACCESS_KEY      # AWS credentials
AWS_REGION                 # AWS region (us-east-1)
```

❌ **Don't put in code** — use environment variables!

---

## 🎯 Workflow

```
Local Development:
  setup.ps1 → Code → Test → git push

↓ (GitHub Actions triggered)

Production Deployment:
  Maven build → Docker build → ECR push 
  → SSH EC2 → docker-compose up
  → Live! ✅

Live URL: http://<EC2_IP>:8081
```

---

## ✅ Key Files Created

```
✅ KAFKA_GUIDE.md              Event streaming guide
✅ EC2_S3_DEPLOYMENT.md        Production deployment
✅ QUICK_START.md              5-min setup
✅ AWS_COMMANDS.md             Copy-paste AWS CLI
✅ SETUP_SUMMARY.md            Complete overview
✅ START_HERE.md               Navigation
✅ DOCUMENTATION_INDEX.md      File reference
✅ .github/workflows/deploy.yml CI/CD pipeline
✅ setup.ps1, setup.sh         Auto setup
✅ docker-compose-prod.yml     Production services
✅ application-prod.yml        Production config
✅ Dockerfile                  Optimized image
```

---

## 🚦 Quick Status Check

**Local**:
- [ ] Docker running? `docker ps`
- [ ] App accessible? http://localhost:8081
- [ ] Swagger UI? http://localhost:8081/swagger-ui.html

**Production**:
- [ ] EC2 running? `aws ec2 describe-instances`
- [ ] Deployment successful? GitHub Actions
- [ ] App accessible? http://<EC2_IP>:8081
- [ ] Swagger UI? http://<EC2_IP>:8081/swagger-ui.html

---

## 📞 Helpful Commands

### **Docker**
```bash
docker-compose up -d         # Start services
docker-compose logs -f       # View logs
docker-compose down          # Stop services
docker ps                    # List containers
docker logs -f <container>   # Container logs
```

### **Git**
```bash
git push origin main         # Trigger CI/CD
git log --oneline            # View commits
git status                   # Status
```

### **AWS CLI**
```bash
aws sts get-caller-identity  # Check account
aws ec2 describe-instances   # List EC2
aws s3 ls                    # List buckets
aws ecr list-images --repository-name journal-backend
```

### **SSH**
```bash
ssh -i journal-backend-key.pem ubuntu@<EC2_IP>
docker logs -f journal-api
```

---

## 🎓 Interview Talking Points

```
"Completely dockerized application with automated CI/CD.
GitHub push triggers Maven build, Docker image creation,
ECR push, and automatic EC2 deployment. 
Uses Kafka for event streaming, S3 for file storage,
MongoDB for persistence, all on AWS free tier."
```

---

## 🆘 Troubleshooting

| Problem | Solution |
|---------|----------|
| Local setup fails | Check `QUICK_START.md` Troubleshooting |
| AWS commands error | Verify AWS CLI installed + credentials |
| Deployment fails | Check GitHub Actions logs |
| App not accessible | Check EC2 security group + port 8081 |
| Kafka not working | Check `KAFKA_GUIDE.md` Testing |

---

## 📊 Timeline

| Task | Time | Status |
|------|------|--------|
| Local setup | 5 min | ✅ Done |
| Code modifications | - | ✅ All done |
| AWS account creation | 10 min | 🟡 Manual |
| EC2 + S3 setup | 30 min | 🟡 Manual |
| First deployment | 20 min | 🟡 Manual |
| **Total** | **~1 hour** | 🟡 First time |
| **Next time** | **1 click** | ✅ Git push |

---

## 🎁 Resume Content Ready

```markdown
Journal Backend API
- Live: http://<EC2_IP>:8081
- Swagger: http://<EC2_IP>:8081/swagger-ui.html
- Stack: Spring Boot 3.x, Docker, AWS, Kafka
- CI/CD: GitHub Actions (auto-deploy)
- Features: Kafka events, S3 storage, JWT auth
```

---

## 📌 Important Remember

✅ **Good**:
- Use setup scripts
- Copy-paste AWS commands
- Follow guides step-by-step
- Keep .pem file safe

❌ **Avoid**:
- Hardcoding secrets
- Modifying dev config
- Losing EC2 key file
- Committing .env file

---

## 🎯 Next Action

```
1. Open: START_HERE.md
2. Follow: QUICK_START.md
3. Run: ./setup.ps1 (Windows) or bash setup.sh (Mac/Linux)
4. Test: http://localhost:8081/swagger-ui.html
5. Done! ✅
```

---

## 📞 Files Quick Links

For help on:
- **Local setup**: `QUICK_START.md`
- **Production**: `EC2_S3_DEPLOYMENT.md`
- **Kafka**: `KAFKA_GUIDE.md`
- **AWS commands**: `AWS_COMMANDS.md`
- **Overview**: `START_HERE.md` or `SETUP_SUMMARY.md`
- **Navigation**: `DOCUMENTATION_INDEX.md`

---

**🎉 You're ready! Start with START_HERE.md 🎉**

Last updated: May 16, 2026

