# Project Structure

## Overview

This project follows **Clean Architecture** principles with clear separation of concerns across different layers.

## Directory Structure

```
src/main/kotlin/com/aivanouski/auth_kotlin_demo/
├── AuthKotlinDemoApplication.kt          # Main application entry point
│
├── domain/                                # Domain Layer (Entities & Business Models)
│   └── entity/
│       ├── Role.kt                        # User role enum (USER, ADMIN, MODERATOR)
│       ├── User.kt                        # User entity with profile and credentials
│       ├── RefreshToken.kt                # Refresh token entity
│       ├── EmailVerificationToken.kt      # Email verification token entity
│       └── PasswordResetToken.kt          # Password reset token entity
│
├── application/                           # Application Layer (Use Cases & DTOs)
│   ├── dto/
│   │   ├── request/                       # Request DTOs
│   │   │   ├── RegisterRequest.kt         # User registration data
│   │   │   ├── LoginRequest.kt            # Login credentials
│   │   │   ├── RefreshTokenRequest.kt     # Refresh token request
│   │   │   ├── ForgotPasswordRequest.kt   # Forgot password request
│   │   │   └── ResetPasswordRequest.kt    # Reset password with token
│   │   └── response/                      # Response DTOs
│   │       ├── AuthResponse.kt            # Authentication response with tokens
│   │       ├── UserResponse.kt            # User profile response
│   │       └── MessageResponse.kt         # Generic message response
│   │
│   ├── service/                           # Business Logic Services
│   │   ├── AuthService.kt                 # Main authentication service
│   │   ├── TokenService.kt                # Refresh token management
│   │   ├── EmailVerificationService.kt    # Email verification logic
│   │   ├── PasswordResetService.kt        # Password reset logic
│   │   └── EmailService.kt                # Email sending service
│   │
│   └── exception/                         # Custom Exceptions
│       └── CustomExceptions.kt            # Application-specific exceptions
│
├── infrastructure/                        # Infrastructure Layer (Technical Details)
│   ├── config/                            # Configuration Classes
│   │   ├── SecurityConfig.kt              # Spring Security configuration
│   │   └── SecurityProperties.kt          # Security properties binding
│   │
│   ├── security/                          # Security Components
│   │   ├── JwtService.kt                  # JWT token generation & validation
│   │   ├── JwtAuthenticationFilter.kt     # JWT authentication filter
│   │   └── CustomUserDetailsService.kt    # UserDetailsService implementation
│   │
│   └── repository/                        # Data Access Layer
│       ├── UserRepository.kt              # User repository
│       ├── RefreshTokenRepository.kt      # Refresh token repository
│       ├── EmailVerificationTokenRepository.kt
│       └── PasswordResetTokenRepository.kt
│
└── presentation/                          # Presentation Layer (API Controllers)
    ├── controller/                        # REST Controllers
    │   ├── AuthController.kt              # Authentication endpoints
    │   └── UserController.kt              # User management endpoints
    │
    └── exception/                         # Exception Handling
        └── GlobalExceptionHandler.kt      # Global exception handler

src/main/resources/
├── application.yml                        # Application configuration
└── db/migration/                          # Flyway database migrations
    ├── V1_1.0__init.sql                   # Initial schema
    ├── V1_1.1__users.sql                  # Users table and seed data
    └── V1_1.2__tokens.sql                 # Token tables

src/test/kotlin/com/aivanouski/auth_kotlin_demo/
├── AuthKotlinDemoApplicationTests.kt      # Application context test
├── BaseIntegrationTest.kt                 # Base class for integration tests
└── presentation/controller/
    ├── AuthControllerTest.kt              # Auth endpoint tests
    └── UserControllerTest.kt              # User endpoint tests

src/test/resources/
└── sql/
    ├── run_before_tests.sql               # Test data setup
    └── run_after_tests.sql                # Test data cleanup

Configuration Files:
├── build.gradle.kts                       # Gradle build configuration
├── settings.gradle.kts                    # Gradle settings
├── Dockerfile                             # Application Docker image
├── docker-compose.yml                     # Docker services (App, PostgreSQL, MailHog)
├── .env                                   # Environment variables
├── .env.example                           # Environment variables template
├── README.md                              # Main documentation
└── PROJECT_STRUCTURE.md                   # This file
```

## Layer Responsibilities

### 1. Domain Layer
- **Purpose**: Core business entities and domain models
- **Dependencies**: None (pure domain logic)
- **Contents**: JPA entities representing the database schema
- **Files**: 5 entities (User, Role, RefreshToken, EmailVerificationToken, PasswordResetToken)

### 2. Application Layer
- **Purpose**: Business logic and use cases
- **Dependencies**: Domain layer only
- **Contents**:
  - DTOs for request/response transformation
  - Service classes implementing business logic
  - Custom exceptions
- **Files**: 8 DTOs, 5 services, 6 custom exceptions

### 3. Infrastructure Layer
- **Purpose**: Technical implementation details
- **Dependencies**: Domain and Application layers
- **Contents**:
  - Spring Security configuration
  - JWT implementation
  - Repository interfaces for data access
  - Configuration classes
- **Files**: 2 configs, 3 security components, 4 repositories

### 4. Presentation Layer
- **Purpose**: HTTP API endpoints
- **Dependencies**: Application layer
- **Contents**:
  - REST controllers
  - Global exception handler
  - API request/response mapping
- **Files**: 2 controllers, 1 global exception handler

## Key Design Patterns

### 1. **Clean Architecture**
- Clear separation of concerns
- Dependency inversion (inner layers don't depend on outer layers)
- Business logic independent of frameworks

### 2. **Repository Pattern**
- Data access abstraction using Spring Data JPA
- Query methods following Spring Data conventions

### 3. **Service Layer Pattern**
- Business logic encapsulated in service classes
- Transactional boundaries defined at service level

### 4. **DTO Pattern**
- Separation of API contracts from domain entities
- Input validation at DTO level
- Response transformation

### 5. **Filter Chain Pattern**
- JWT authentication filter in Spring Security filter chain
- Request interception for authentication

### 6. **Strategy Pattern**
- Different token types (access, refresh, verification, reset)
- Each with its own expiration and validation strategy

## Data Flow

### Registration Flow
```
AuthController → AuthService → UserRepository
              ↓
         EmailVerificationService → EmailService
```

### Login Flow
```
AuthController → AuthService → AuthenticationManager
              ↓
         JwtService + TokenService
```

### Protected Endpoint Access
```
Request → JwtAuthenticationFilter → JwtService
       ↓
   UserDetailsService → UserRepository
       ↓
   SecurityContext → Controller
```

## Database Management

### Flyway Migrations
- **V1_1.0__init.sql**: Initial schema setup
- **V1_1.1__users.sql**: Users table with seed data (admin, user, moderator)
- **V1_1.2__tokens.sql**: Refresh tokens, email verification tokens, password reset tokens

### Migration Strategy
- Version-controlled database schema
- Automatic migration on application startup
- Rollback support for production deployments
- Seed data for development and testing

## Testing Strategy

### Integration Tests
- **BaseIntegrationTest**: Base class with Testcontainers setup
  - PostgreSQL 16-alpine container
  - MailHog container for email testing
  - Helper methods for authentication (authToken, refreshToken)
  - SQL scripts for test data setup/cleanup

- **AuthControllerTest**: Authentication endpoint tests
  - Registration, login, refresh token flows
  - Email verification and password reset
  - Error handling and validation
  - Real database operations (no mocks)

- **UserControllerTest**: User management endpoint tests
  - Get current user
  - Get all users (admin only)
  - Role-based access control
  - Real JWT token authentication

### Test Infrastructure
- **Testcontainers**: Docker-based test dependencies
- **MockMvc**: Spring MVC test framework
- **Kotlin DSL**: Expressive test syntax
- **Hamcrest Matchers**: Readable assertions
- **Real Database**: PostgreSQL with actual data operations
- **Real Email**: MailHog for email verification testing

### Test Coverage
- 39 integration tests covering all API endpoints
- Authentication and authorization scenarios
- Error handling and validation
- Token expiration and revocation

## Configuration Files

### application.yml
- Database connection settings
- JWT configuration (secret, expiration)
- Email server settings
- Token expiration settings
- Flyway migration settings

### build.gradle.kts
- Dependencies management
- Kotlin configuration
- Spring Boot plugins
- Testcontainers dependencies

### docker-compose.yml
- Application container (port 18080)
- PostgreSQL database (port 15432)
- MailHog (SMTP: 1025, Web UI: 8025)
- Health checks and service dependencies

### Dockerfile
- Multi-stage build
- Java 21 runtime
- Application JAR packaging

## Security Features

### JWT Implementation
- **JwtService**: Token generation and validation
- **JwtAuthenticationFilter**: Request interception
- **SecurityConfig**: Security rules and authentication provider

### Token Management
- Access tokens: 15 minutes expiration
- Refresh tokens: 7 days expiration, stored in database
- Verification tokens: 24 hours expiration
- Reset tokens: 1 hour expiration

### Password Security
- BCrypt hashing with default strength (10 rounds)
- Minimum 8 characters validation

### Role-Based Access Control
- USER: Standard access
- ADMIN: Administrative access (get all users)
- MODERATOR: Moderation privileges

### Error Handling
- **401 Unauthorized**: No token or invalid/expired token
- **403 Forbidden**: Valid token but insufficient permissions
- Custom error responses with status, error, message, and path
- Field-level validation errors

## Scalability Considerations

1. **Stateless Authentication**: JWT enables horizontal scaling
2. **Database Indexing**: Email column indexed for fast lookups
3. **Connection Pooling**: HikariCP for database connections
4. **Token Cleanup**: Expired tokens cleaned via CASCADE deletes
5. **Docker Deployment**: Container-based deployment with docker-compose

## Completed Features

✅ Integration tests with real database (Testcontainers)
✅ Database migration with Flyway
✅ Email testing with MailHog
✅ Docker Compose setup
✅ Comprehensive test coverage (39 tests)
✅ Role-based access control tests
✅ Token expiration and revocation tests

## Future Enhancements

1. Implement OAuth2 social login
2. Add rate limiting for authentication endpoints
3. Implement account lockout after failed attempts
4. Add audit logging for security events
5. Implement two-factor authentication (2FA)
6. Add API documentation with Swagger/OpenAPI
7. Add health checks and metrics with Actuator
8. Implement CORS configuration for frontend
9. Add test coverage reporting (JaCoCo)
10. Implement refresh token rotation

## Total Files Count

- **Entities**: 5 files
- **DTOs**: 8 files
- **Services**: 5 files
- **Repositories**: 4 files
- **Controllers**: 2 files
- **Security Components**: 3 files
- **Configuration**: 2 files
- **Exception Handling**: 2 files
- **Flyway Migrations**: 3 files
- **Test Files**: 4 files
- **Test SQL Scripts**: 2 files

**Total**: 40 Kotlin files + 5 SQL files + configuration files
