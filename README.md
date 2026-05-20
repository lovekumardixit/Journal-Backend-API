# Journal Backend API

A production-ready, enterprise-grade backend for a personal journaling application with JWT authentication, MongoDB, Redis caching, and AWS cloud deployment.

**Live API:** [https://journalapilav.tech](https://journalapilav.tech)  
**Swagger UI:** [https://journalapilav/swagger-ui.html](https://journalapilav.tech/swagger-ui.html)  
**Author:** [Lav Kumar Dixit](https://www.linkedin.com/in/lavkumardixit/) | GitHub: [@lovekumardixit](https://github.com/lovekumardixit)

---

## 🚀 Key Features

- **JWT-based Authentication** with refresh tokens and role-based access (USER / ADMIN)
- **Secure User Registration** with strict validation (username, password, email)
- **User Profile Management** with profile photo upload/update/delete (AWS S3 storage)
- **Complete Journal CRUD Operations** with partial update support
- **Sentiment Tracking** (HAPPY, SAD, ANGRY, NEUTRAL) with filtering
- **Weather API Integration** for personalized greetings
- **Email (SMTP) & Kafka Integration** for event-driven architecture
- **OpenAPI/Swagger** auto-generated documentation with Bearer Authentication
- **Redis Caching** for improved performance
- **HTTPS/SSL Secured** with Let's Encrypt
- **Production-Ready Deployment** on AWS EC2 with Docker & Nginx

---

## 🛠️ Tech Stack

### Backend
- **Java 17** with Spring Boot 3.x
- **Spring Security** with JWT Authentication
- **Spring Data MongoDB** for persistence
- **Spring Data Redis** for caching
- **Spring Kafka** for event streaming
- **OAuth2** (Google Login support)
- **REST APIs** with comprehensive documentation

### Database & Caching
- **MongoDB Atlas** for scalable NoSQL database
- **Redis Cache** for session & query caching

### Messaging & Event Streaming
- **Apache Kafka** with Zookeeper
- **Event-Driven Architecture** for asynchronous processing

### Cloud & Deployment
- **AWS EC2** (Ubuntu Server) for hosting
- **AWS S3** for profile image storage
- **Docker & Docker Compose** for containerization
- **Nginx Reverse Proxy** for load balancing
- **HTTPS/SSL** with Let's Encrypt certificate
- **GitHub Actions** for CI/CD automation

### Security
- JWT-based authentication with secure token management
- Role-based access control (RBAC)
- OAuth2 authentication (Google Login)
- Environment-based configuration (dev, staging, prod)
- Secure HTTPS/TLS encryption
- Protected AWS credentials and sensitive data

---

## 📋 Quick Start

### Prerequisites
- Java 17+
- Maven 3.x
- Docker & Docker Compose (optional)
- Linux/Unix environment for production

### Run Locally

1. **Clone the repository**
```bash
git clone https://github.com/lovekumardixit/Journal-Backend-API.git
cd Journal-Backend-API
```

2. **Configure environment variables**
```bash
export MONGO_URI='mongodb+srv://<user>:<pass>@cluster.mongodb.net/userdb'
export JWT_SECRET='your-strong-secret-key-here'
export SERVER_PORT=8081
export WEATHER_API_KEY='your-api-key'
export MAIL_USERNAME='your-email@gmail.com'
export MAIL_PASSWORD='your-app-password'
export KAFKA_BOOTSTRAP_SERVERS='localhost:9092'
export AWS_ACCESS_KEY_ID='your-aws-key'
export AWS_SECRET_ACCESS_KEY='your-aws-secret'
export AWS_S3_BUCKET='your-bucket-name'
```

3. **Build & Run**
```bash
# With Maven (dev profile)
mvn clean package
mvn -Dspring-boot.run.profiles=dev spring-boot:run

# With Java
java -jar target/Backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

**Local server:** http://localhost:8081

### Docker Deployment

```bash
# Build Docker image
docker build -t journal-backend-api:latest .

# Run container
docker run -p 8081:8081 \
  -e MONGO_URI='<your-mongodb-uri>' \
  -e JWT_SECRET='<your-secret>' \
  -e AWS_ACCESS_KEY_ID='<your-key>' \
  -e AWS_SECRET_ACCESS_KEY='<your-secret>' \
  journal-backend-api:latest

# With Docker Compose
docker-compose up -d
```

---

## 🌐 API Documentation

### Access Points
- **Base URL:** https://your-api-url.com
- **OpenAPI JSON:** https://your-api-url.com/v3/api-docs
- **Swagger UI:** https://your-api-url.com/swagger-ui.html

### Core Endpoints

**Authentication**
- `POST /auth/register` — Register new user
- `POST /auth/login` — Login and receive JWT tokens
- `POST /auth/refresh` — Refresh access token
- `POST /auth/logout` — Logout user

**User Profile**
- `GET /user/me` — Get current user profile
- `GET /user/profile` — Get profile with photo URL
- `GET /user/get/{city}` — Get weather greeting
- `POST /user/profile-photo/upload` — Upload profile photo (multipart)
- `DELETE /user/profile-photo` — Delete profile photo

**Journal Entries**
- `POST /entry` — Create new entry
- `GET /entry/{id}` — Get entry by ID
- `PUT /entry/{id}` — Update entry
- `PATCH /entry/{id}` — Partial update
- `DELETE /entry/{id}` — Delete entry
- `GET /entry/user/{userId}` — Get user's entries

**Filters & Analytics**
- `GET /sentiment` — Filter entries by sentiment
- `GET /sentiment/stats` — Get sentiment statistics

**For complete API details, visit:** [Swagger UI](https://journalapilav/swagger-ui.html)

---

## 🔒 Authentication in Swagger UI

1. Click the **"Authorize"** button in Swagger UI
2. Enter your token in the format: `Bearer YOUR_JWT_TOKEN`
3. Execute requests directly from the UI

---

## 📁 Project Structure

```
src/main/java/com/love/Backend/
├── controller/       # REST endpoints (Auth, User, Entry, Sentiment)
├── service/          # Business logic & core services
├── repository/       # Spring Data repositories
├── entity/           # JPA domain models
├── dto/              # Request/Response DTOs
├── config/           # Spring config (Security, Swagger, Redis, AWS S3)
├── kafka/            # Event producers & consumers
├── exception/        # Custom exceptions & error handlers
├── util/             # Utility classes & helpers
└── BackendApplication.java
```

---

## 🧪 Testing

Run all tests with Maven:
```bash
mvn test
```

**Validation Tests:** Comprehensive test cases for user registration, authentication, and input validation.

See related documentation:
- **VALIDATION_RULES.md** — User validation rules & best practices
- **TEST_VALIDATION.md** — Step-by-step test scenarios

---

## 🚀 Production Deployment (AWS EC2)

### Infrastructure Setup
- **OS:** Ubuntu 20.04+ LTS
- **Java 17** installed
- **MongoDB Atlas** for managed database
- **Redis** for caching layer
- **Nginx** reverse proxy with SSL
- **Let's Encrypt** for HTTPS/TLS

### Deployment Steps

1. **Push Docker image to registry**
```bash
docker tag journal-backend-api:latest your-registry/journal-backend-api:latest
docker push your-registry/journal-backend-api:latest
```

2. **Deploy on EC2**
```bash
ssh ec2-user@your-ec2-ip
cd /opt/journal-api
docker pull your-registry/journal-backend-api:latest
docker-compose up -d
```

3. **Configure Nginx**
```nginx
server {
    listen 443 ssl http2;
    server_name your-api-url.com;
    
    ssl_certificate /etc/letsencrypt/live/your-api-url.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-api-url.com/privkey.pem;
    
    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Environment Configuration
Production uses profile-based configuration. Set environment variables:
```bash
export SPRING_PROFILES_ACTIVE=prod
export MONGO_URI='mongodb+srv://user:pass@cluster.mongodb.net/journal'
export JWT_SECRET='<strong-production-secret>'
export AWS_S3_BUCKET='journal-profile-photos'
```

---

## 🔐 Security Best Practices

✅ **JWT Secret:** Strong, random, environment-specific  
✅ **HTTPS/TLS:** Let's Encrypt SSL certificates  
✅ **Database:** MongoDB Atlas with authentication & IP whitelist  
✅ **AWS Credentials:** Stored in EC2 IAM roles (not hardcoded)  
✅ **Environment Variables:** Profile-based, never committed to VCS  
✅ **CORS:** Configured for trusted domains only  
✅ **Rate Limiting:** Implemented on authentication endpoints  

---

## 📚 Documentation

- **VALIDATION_RULES.md** — Comprehensive validation standards
- **TEST_VALIDATION.md** — Test case scenarios
- **KAFKA_GUIDE.md** — Event streaming setup
- **EC2_S3_DEPLOYMENT.md** — AWS deployment guide
- **QUICK_START.md** — 5-minute quick setup

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License** — see [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Love Dixit**
- GitHub: [@lovekumardixit](https://github.com/lovekumardixit)
- LinkedIn: [Love Dixit](https://www.linkedin.com/in/lavkumardixit/)
- Email: cyber.lavdixit@gmail.com

---

**Last Updated:** May 20, 2026 | Status: Production Ready ✅
