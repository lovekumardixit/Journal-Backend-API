# Journal Backend API

Personal journaling REST API built with Spring Boot, MongoDB, and JWT authentication.

## Features

- User authentication with JWT & refresh tokens
- CRUD operations for journal entries
- Sentiment tracking (SAD, HAPPY, ANGRY, NEUTRAL)
- Weather API integration for personalized greetings
- Role-based access control (User/Admin)
- Swagger API documentation
- Docker support

## Tech Stack

- **Java 17** | **Spring Boot 3.x** | **MongoDB** | **JWT**
- **Spring Security** | **Swagger/OpenAPI** | **Maven** | **Docker**

## Prerequisites

- Java 17+
- Maven 3.6+
- MongoDB (Atlas or local)

## Quick Start

### 1. Clone & Setup
```bash
git clone https://github.com/lovekumardixit/journal-backend-api.git
cd journal-backend-api
```

### 2. Configure MongoDB & JWT
Create `src/main/resources/application.yml`:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://user:pass@cluster.mongodb.net/journal_db
  security:
    jwt:
      secret: your_secret_key_here
      expiration: 3600000

server:
  port: 8080
```

### 3. Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

API available at: `http://localhost:8080/api/v1`  
Swagger docs: `http://localhost:8080/api/v1/swagger-ui.html`

## API Endpoints

### Authentication Endpoints
```
POST   /auth/register        - Register a new user
POST   /auth/login           - Authenticate user and get JWT token
POST   /auth/refresh         - Refresh expired JWT token
POST   /auth/logout          - Logout user and invalidate token
GET    /auth/profile         - Get authenticated user profile
```

### Journal Entry Endpoints
```
POST   /entries              - Create a new journal entry
GET    /entries              - Get all entries (paginated)
GET    /entries/{id}         - Get specific entry by ID
PUT    /entries/{id}         - Update an entire entry
PATCH  /entries/{id}         - Partially update an entry
DELETE /entries/{id}         - Delete an entry
```

### Sentiment Endpoints
```
GET    /sentiment?sentiment=HAPPY  - Get entries by sentiment filter
```

### User Management Endpoints
```
GET    /user                 - Get all users
GET    /user/me              - Get current authenticated user
GET    /user/age?age=25      - Get users above specified age
PUT    /user/update          - Fully update user profile
PATCH  /user/update          - Partially update user profile
```

### Weather Integration
```
GET    /user/get/{city}      - Get weather greeting for a city
```

## 📖 Usage Examples

### 1. User Registration
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "userName": "johndoe",
    "password": "SecurePass123@"
  }'
```

### 2. Login & Get JWT Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "johndoe",
    "password": "SecurePass123@"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600000
}
```

### 3. Create a Journal Entry (Requires Authentication)
```bash
curl -X POST http://localhost:8080/api/v1/entries \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "My First Day",
    "content": "Today was amazing! Had a great time with friends.",
    "sentiment": "HAPPY"
  }'
```

### 4. Get All Entries with Pagination
```bash
curl -X GET "http://localhost:8080/api/v1/entries?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Get Entries by Sentiment
```bash
curl -X GET "http://localhost:8080/api/v1/sentiment?sentiment=SAD" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 6. Get Weather Greeting
```bash
curl -X GET "http://localhost:8080/api/v1/user/get/New%20York" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "message": "Hi johndoe! Today feels like sunny :22.5°C"
}
```

## 🏗️ Architecture

### Project Structure
```
src/main/java/com/love/Backend/
├── controller/           # REST API endpoints
│   ├── AuthController
│   ├── UserController
│   ├── BackendController (Entries)
│   ├── PublicController
│   └── sentimentController
├── service/              # Business logic layer
│   ├── JwtService
│   ├── UserEntryService
│   ├── UserDetailsServiceImpl
│   ├── WeatherService
│   └── RedisService (planned)
├── repository/           # Database access layer
│   ├── UserRepository
│   ├── EntryRepository
│   └── BackendConfigRepository
├── entity/               # Domain models
│   ├── User
│   ├── entry
│   ├── RefreshToken
│   └── WeatherResponse
├── dto/                  # Data Transfer Objects
│   ├── request/
│   └── response/
├── config/               # Spring configurations
│   ├── SecurityConfig
│   ├── CacheConfig
│   └── SwaggerConfig
├── enums/                # Enumerations
│   └── Sentiment
├── exception/            # Custom exceptions
│   ├── ResourceNotFoundException
│   └── BadRequestException
├── filter/               # HTTP filters
│   └── JwtRequestFilter
└── BackendApplication.java  # Main Spring Boot app
```

## 🔐 Security Features

1. **JWT Token Management**: Secure token generation, validation, and refresh mechanisms
2. **CORS Configuration**: Controlled cross-origin requests
3. **SQL Injection Prevention**: Parameterized queries via Spring Data
4. **Password Security**: BCrypt hashing with configurable strength
5. **HTTPS Support**: Ready for SSL/TLS in production
6. **Rate Limiting** (Planned): Prevent API abuse with request throttling

## 🧪 Testing

Run the test suite to validate functionality:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserDetailsServiceImplTests

# Run tests with coverage
mvn test jacoco:report
```

**Test Coverage:**
- Unit tests for services and utilities
- Integration tests for database operations
- Controller endpoint tests with MockMvc

## 🐳 Docker Deployment

### Build Docker Image
```bash
docker build -t journal-backend-api:1.0 .
```

### Run Container
```bash
docker run -p 8080:8080 \
  -e MONGO_URI=mongodb+srv://... \
  -e JWT_SECRET=your_secret_key \
  journal-backend-api:1.0
```

## 📈 Future Enhancements

- [ ] **Rate Limiting**: Implement request rate limiting using Spring's RateLimiter
- [ ] **Pagination**: Add advanced pagination with sorting and filtering
- [ ] **Redis Caching**: Cache frequently accessed entries and user data
- [ ] **Email Notifications**: Send email alerts for important events
- [ ] **Advanced Analytics**: Dashboard with mood trends and statistics
- [ ] **File Upload**: Support for uploading images/attachments in entries
- [ ] **Scheduled Tasks**: Daily mood summary and reminders using Quartz Scheduler
- [ ] **GraphQL API**: Alternative query language support
- [ ] **Multi-language Support**: i18n for global users
- [ ] **Mobile App Backend**: Push notifications integration

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Author

**Love Dixit**
- GitHub: [@yourusername](https://github.com/yourusername)
- Email: your.email@example.com
- LinkedIn: [Your Profile](https://linkedin.com/in/yourprofile)

## 📞 Support & Contact

For questions, issues, or suggestions:
- Open an issue on GitHub
- Email: support@journalbackendapi.com
- Discord: [Join our community](https://discord.gg/yourlink)

## 🎯 Learning Outcomes

This project demonstrates proficiency in:
- ✅ Spring Boot & Spring Security framework
- ✅ RESTful API design and implementation
- ✅ JWT authentication and authorization
- ✅ MongoDB NoSQL database design
- ✅ Exception handling and validation
- ✅ API documentation with Swagger/OpenAPI
- ✅ Unit and integration testing
- ✅ Docker containerization
- ✅ Git version control
- ✅ Professional coding standards and best practices

---

**Last Updated:** April 21, 2026  
**Version:** 1.0.0  
**Status:** Active Development


