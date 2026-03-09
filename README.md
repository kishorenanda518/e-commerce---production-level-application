# 🛒 E-Commerce Microservices Platform

A production-grade, enterprise-level **E-Commerce Backend** built with **Spring Boot 3.2.x** and **Java 21** following microservices architecture. Each service is independently deployable, scalable, and communicates via REST and Apache Kafka.

---

## 🏗️ Architecture Overview

```
                        ┌─────────────────┐
                        │   API Gateway   │  :8080
                        │ Spring Cloud GW │
                        └────────┬────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
   ┌──────▼──────┐      ┌───────▼──────┐      ┌───────▼──────┐
   │ User Service│      │Product Service│      │ Order Service│
   │   :8081     │      │    :8082      │      │    :8083     │
   │  MySQL DB   │      │   MySQL DB    │      │ PostgreSQL DB│
   └─────────────┘      └──────────────┘      └──────────────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │      Apache Kafka        │
                    │   Event-Driven Backbone  │
                    └─────────────────────────┘
```

---

## 🚀 Services

| Service | Port | Database | Description |
|---|---|---|---|
| **API Gateway** | 8080 | — | Single entry point, routing, rate limiting |
| **Eureka Server** | 8761 | — | Service discovery and registry |
| **Config Server** | 8888 | Git | Centralized configuration management |
| **User Service** | 8081 | MySQL | Authentication, profiles, addresses |
| **Product Service** | 8082 | MySQL | Catalog, inventory, categories |
| **Order Service** | 8083 | PostgreSQL | Order lifecycle management |
| **Payment Service** | 8084 | MongoDB | Payments, refunds, wallet |

---

## 🔧 Tech Stack

### Backend
![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.3-green?style=flat-square&logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-green?style=flat-square&logo=springsecurity)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0.x-green?style=flat-square)

### Databases
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green?style=flat-square&logo=mongodb)
![Redis](https://img.shields.io/badge/Redis-7.x-red?style=flat-square&logo=redis)

### Messaging & Infrastructure
![Kafka](https://img.shields.io/badge/Apache_Kafka-3.x-black?style=flat-square&logo=apachekafka)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=flat-square&logo=docker)
![Flyway](https://img.shields.io/badge/Flyway-DB_Migration-red?style=flat-square)

### Developer Tools
![Lombok](https://img.shields.io/badge/Lombok-1.18-pink?style=flat-square)
![MapStruct](https://img.shields.io/badge/MapStruct-1.5.5-yellow?style=flat-square)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3.0-green?style=flat-square&logo=swagger)

---

## ✅ Features Implemented (User Service)

### Authentication
- [x] User Registration with email validation
- [x] Email Verification via OTP (Redis — 10 min TTL)
- [x] Resend OTP with rate limiting (max 3/hour via Redis counter)
- [x] JWT Authentication with **HttpOnly Cookies** (XSS safe)
- [x] BCrypt Password Hashing (strength 12)
- [x] Login with username or email
- [ ] Logout with cookie clearing
- [ ] Forgot Password / Reset Password
- [ ] Refresh Token rotation

### User Management
- [ ] Get / Update profile
- [ ] Change password
- [ ] Address CRUD
- [ ] Profile picture upload

### Admin
- [ ] List users (paginated)
- [ ] Change user status
- [ ] Assign roles
- [ ] Audit logs

---

## 🔐 Security Architecture

```
Request → JwtAuthenticationFilter
              ↓
        Extract JWT from HttpOnly Cookie
              ↓
        Validate Signature + Expiry (HS256)
              ↓
        Extract userId + roles → SecurityContext
              ↓
        SecurityConfig → check route permissions
              ↓
        Controller
```

**JWT Strategy:**
- Access Token → HttpOnly Cookie → 15 min TTL
- Refresh Token → Redis → 7 day TTL
- Algorithm → HS256
- Cookie flags → HttpOnly + SameSite=Strict

---

## 📁 Project Structure

```
user-service/
├── src/main/java/com/ecommerce/user_service/
│   ├── config/          # SecurityConfig, RedisConfig, JwtProperties
│   ├── controller/      # AuthApi (interface) + impl/AuthController
│   ├── entity/          # JPA entities + enums
│   ├── exception/       # GlobalExceptionHandler + custom exceptions
│   ├── mapper/          # MapStruct mappers
│   ├── model/           # Request/Response DTOs
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # JwtUtil, JwtFilter, CookieUtil, UserDetailsService
│   └── service/         # Service interfaces + implementations
├── src/main/resources/
│   ├── application.yml
│   ├── logback-spring.xml
│   └── db/migration/    # Flyway SQL migrations
└── pom.xml
```

---

## 🗃️ Database Schema

```
users ──────────── user_profiles
  │
  ├──────────────── user_roles ──── roles
  │
  ├──────────────── addresses
  │
  ├──────────────── refresh_tokens
  │
  └──────────────── audit_logs
```

---

## 📡 API Endpoints

### Auth APIs
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register new user | Public |
| GET | `/api/v1/auth/verify-email` | Verify email with OTP | Public |
| POST | `/api/v1/auth/resend-verification` | Resend OTP | Public |
| POST | `/api/v1/auth/login` | Login → set JWT cookie | Public |
| POST | `/api/v1/auth/logout` | Logout → clear cookie | Auth |
| POST | `/api/v1/auth/forgot-password` | Send reset OTP | Public |
| POST | `/api/v1/auth/reset-password` | Reset with OTP | Public |
| POST | `/api/v1/auth/refresh-token` | Rotate refresh token | Auth |

---

## ⚙️ Getting Started

### Prerequisites
```
Java 21
MySQL 8.0
Redis 7.x
Maven 3.9+
```

### 1 — Clone the repository
```bash
git clone https://github.com/your-username/ecommerce-microservices.git
cd ecommerce-microservices/user-service
```

### 2 — Create database
```sql
CREATE DATABASE user_db;
```

### 3 — Configure application.yml
```yaml
spring:
  datasource:
    username: root
    password: your-password
  mail:
    username: your-email@gmail.com
    password: your-app-password
```

### 4 — Start Redis
```bash
redis-server
```

### 5 — Run the service
```bash
mvn spring-boot:run
```

### 6 — Access Swagger UI
```
http://localhost:8081/swagger-ui.html
```

---

## 📊 Kafka Event Topics

| Topic | Producer | Consumer | Description |
|---|---|---|---|
| `user.registered` | User Service | Email Service | Send welcome email |
| `user.login-success` | User Service | Audit Service | Log successful login |
| `user.login-failed` | User Service | Fraud Service | Track failed attempts |
| `user.updated` | User Service | Order Service | Sync user cache |
| `user.deleted` | User Service | Order/Cart Service | Cleanup user data |

---

## 📈 Monitoring

| Tool | URL | Purpose |
|---|---|---|
| Swagger UI | `http://localhost:8081/swagger-ui.html` | API documentation |
| Actuator Health | `http://localhost:8081/actuator/health` | Health check |
| Prometheus | `http://localhost:8081/actuator/prometheus` | Metrics |
| Grafana | `http://localhost:3000` | Dashboard |
| Eureka | `http://localhost:8761` | Service registry |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License.

---

## 👨‍💻 Author

**Nanda** — Full Stack Developer
- Building enterprise microservices with Spring Boot & Java 21
