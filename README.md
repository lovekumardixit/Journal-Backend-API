# Journal Backend API

This is a professional, production-ready backend for a personal journaling application implemented with Spring Boot (3.x), Java 17, MongoDB and JWT-based authentication. It provides user & admin roles, CRUD for journal entries, sentiment tracking, a weather integration for personalized greetings, email/kafka hooks, and a documented OpenAPI/Swagger surface for easy exploration.

Key goals:
- Secure authentication and token management (access + refresh tokens)
- Clear separation of concerns (controller/service/repository)
- Container-friendly: ready to run in Docker
- Auto-generated API documentation (OpenAPI / Swagger)

Table of contents
- Features
- Quickstart (run locally / Docker)
- Configuration & environment variables
- API reference & Swagger
- Project structure & architecture
- Testing
- Development notes: keeping Swagger docs up-to-date
- Contributing & license

---

Features
- JWT-based authentication with refresh tokens and role-based access (USER / ADMIN)
- **User registration with strict validation rules**: username (lowercase, no spaces, 3-50 chars), password (min 8 chars with uppercase/lowercase/number/special char), unique email. See `VALIDATION_RULES.md` for details.
- User registration, login, logout and profile management
- Profile photo support: users can upload/update/delete profile photos. The user profile includes `profilePhotoUrl` (served from `/uploads/profile-photos/`).
- CRUD endpoints for journal entries (create/read/update/delete, partial updates supported)
- Sentiment tracking (HAPPY, SAD, ANGRY, NEUTRAL) and filters
- Weather API integration for greetings
- Email (SMTP) and Kafka integration points (configured in dev profile)
- OpenAPI (springdoc) auto-generated documentation with Bearer Authentication support
- Docker image + environment-variable driven configuration

Quickstart — run locally
Prerequisites: Java 17, Maven 3.x, and (optionally) Docker.

1) Clone
```powershell
git clone <your-repo-url>
cd Backend
```

2) Configure environment
The project uses profile-based yml files. By default the `dev` profile is active and server runs on port 8081.

Important environment variables (defaults are defined in `src/main/resources/application-dev.yml`):
- MONGO_URI — MongoDB connection string (default points to a demo Atlas URI)
- JWT_SECRET — secret used to sign JWT tokens (default: a dev key in `application-dev.yml`)
- SERVER_PORT — server port (default 8081)
- WEATHER_API_KEY — external weather provider key
- MAIL_USERNAME / MAIL_PASSWORD — SMTP credentials for sending email
- KAFKA_BOOTSTRAP_SERVERS — Kafka bootstrap address

You can export variables or run with JVM properties, for example (PowerShell):
```powershell
$env:MONGO_URI='mongodb://localhost:27017/userdb';
$env:JWT_SECRET='a_strong_secret_here';
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

3) Build and run
```powershell
mvn clean package
# run with java -jar
java -jar target/Backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

Default server URL: http://localhost:8081

Run with Maven (dev profile):
```powershell
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

Docker
```powershell
# build
docker build -t journal-backend-api:latest .
# run (provide required env vars)
docker run -p 8081:8081 -e MONGO_URI='<your-mongo>' -e JWT_SECRET='<secret>' journal-backend-api:latest
```

API reference & Swagger
- OpenAPI JSON: GET /v3/api-docs
- Swagger UI: http://localhost:8081/swagger-ui.html (redirects to /swagger-ui/index.html)

Note: This project uses `springdoc-openapi` (see `pom.xml`) and the `SwaggerConfig` bean to configure API title, description and a Bearer JWT security scheme. The OpenAPI document is generated at runtime from controller APIs and model classes.

Authentication in Swagger UI
1. Click "Authorize" in the Swagger UI
2. Provide your token in the format: `Bearer YOUR_JWT_TOKEN`

Project structure (high level)
```
src/main/java/com/love/Backend/
├── controller/    # REST controllers (AuthController, UserController, BackendController (entry), sentimentController ...)
├── service/       # business logic (JwtService, UserEntryService, WeatherService, etc.)
├── repository/    # Spring Data repositories (UserRepository, EntryRepository ...)
├── entity/        # domain models and persistence objects
├── dto/           # request/response DTOs
├── config/        # Spring configuration (Security, Swagger/OpenAPI, Redis, etc.)
├── kafka/         # kafka producers/consumers & events
├── exception/     # custom exceptions and handlers
└── BackendApplication.java
```

Endpoints (base path is root — check controllers for exact paths)
- POST  /auth/register        — register a new user
- POST  /auth/login           — authenticate & receive access + refresh token
- POST  /auth/refresh         — refresh access token
- POST  /auth/logout          — logout
- GET   /user/me              — get current user profile
- GET   /user/get/{city}      — weather greeting for city
- POST  /user/profile-photo/upload — upload or update authenticated user's profile photo (multipart/form-data: file param name `file`)
- GET   /user/profile        — get authenticated user's profile including `profilePhotoUrl`
- DELETE /user/profile-photo — delete authenticated user's profile photo
- CRUD  /entry/**             — journal entry endpoints (see controllers for full paths)
- GET   /sentiment            — filter entries by sentiment

For the canonical and complete API surface, use the Swagger UI — it always reflects the compiled controllers and DTOs.

Profile photo notes (quick)
- Upload: POST `/user/profile-photo/upload` — multipart file param named `file`. On success the response contains `photoUrl` (path under `/uploads/profile-photos/`).
- Get profile: GET `/user/profile` — returns `UserResponseDTO` including `profilePhotoUrl`.
- Delete: DELETE `/user/profile-photo` — removes stored photo and clears `profilePhotoUrl` on the user.

Swagger / OpenAPI
- The OpenAPI doc is generated from controller annotations. The `SwaggerConfig` OpenAPI bean has been updated to v1.1 and includes contact/license metadata. Use the Swagger UI to exercise file upload endpoints (authorize with Bearer token first).

Testing
Run unit and integration tests with Maven:
```powershell
mvn test
```

**User Validation Testing**
See `VALIDATION_RULES.md` for comprehensive validation rules and `TEST_VALIDATION.md` for step-by-step test cases covering:
- Username validation (lowercase, no spaces, 3-50 chars)
- Password complexity (uppercase + lowercase + number + special char)
- Email validation (valid format, unique)
- Duplicate username/email checks
- Login with various scenarios

Development notes — keeping Swagger docs up-to-date
- Controllers and DTOs drive the OpenAPI output. To keep Swagger documentation accurate:
  - Annotate controllers and methods with descriptive JavaDoc or `@Operation` / `@Parameter` (from `io.swagger.v3.oas.annotations`) where needed.
  - Keep request/response DTO classes well-typed and documented so the model schemas in OpenAPI are generated correctly.
  - Update the `SwaggerConfig` `OpenAPI` bean (src/main/java/com/love/Backend/config/SwaggerConfig.java) if you need to change title/description/version or to add contact/license metadata.
  - Because a security scheme (Bearer) is already registered in `SwaggerConfig`, Swagger UI will display the lock/Authorize button — provide a valid JWT to exercise secured endpoints from the UI.

If you want automated OpenAPI generation to a file as part of CI, add a step that requests `/v3/api-docs` and saves the output (or use a plugin to generate static docs).

Security & production readiness
- Ensure `JWT_SECRET` is strong and not committed into VCS.
- Use a managed MongoDB instance (Atlas) or a secure production cluster, enable authentication and network rules.
- Configure TLS/HTTPS (reverse proxy / load balancer) for public deployments.
- Rotate secrets and configure logging/monitoring.

Contributing
- Fork, create a feature branch, open a PR. See project tags and tests for expected style and coverage.

License
- MIT (see LICENSE file)

Author & contact
- Love Dixit — lovekumardixit on GitHub
- Email: cyber.lavdixit@gmail.com

Last updated: May 17, 2026

---

## 📋 Related Documentation
- **VALIDATION_RULES.md** — Comprehensive user validation rules (username, password, email, best practices)
- **TEST_VALIDATION.md** — Step-by-step test cases for all validation scenarios
- **KAFKA_GUIDE.md** — Kafka setup and event streaming
- **EC2_S3_DEPLOYMENT.md** — AWS production deployment
- **QUICK_START.md** — 5-minute local setup
