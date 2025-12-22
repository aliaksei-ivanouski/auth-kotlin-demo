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
└── application.yml                        # Application configuration

Configuration Files:
├── build.gradle.kts                       # Gradle build configuration
├── settings.gradle.kts                    # Gradle settings
├── docker-compose.yml                     # Docker services (PostgreSQL, MailHog)
├── .env.example                           # Environment variables template
├── README.md                              # Main documentation
├── TESTING.md                             # Testing guide
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

## Configuration Files

### application.yml
- Database connection settings
- JWT configuration (secret, expiration)
- Email server settings
- Token expiration settings

### build.gradle.kts
- Dependencies management
- Kotlin configuration
- Spring Boot plugins

### docker-compose.yml
- PostgreSQL database
- MailHog (email testing)

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
- ADMIN: Administrative access
- MODERATOR: Moderation privileges

## Testing Strategy

- Unit tests for service layer
- Integration tests for repositories
- Controller tests for API endpoints
- Security tests for authentication

## Scalability Considerations

1. **Stateless Authentication**: JWT enables horizontal scaling
2. **Database Indexing**: Email column indexed for fast lookups
3. **Connection Pooling**: HikariCP for database connections
4. **Token Cleanup**: Expired tokens should be cleaned periodically
5. **Caching**: Consider caching for frequently accessed data

## Future Enhancements

1. Add unit and integration tests
2. Implement OAuth2 social login
3. Add rate limiting for authentication endpoints
4. Implement account lockout after failed attempts
5. Add audit logging for security events
6. Implement two-factor authentication (2FA)
7. Add API documentation with Swagger/OpenAPI
8. Implement database migration with Flyway
9. Add health checks and metrics with Actuator
10. Implement CORS configuration for frontend

## Total Files Count

- **Entities**: 5 files
- **DTOs**: 8 files
- **Services**: 5 files
- **Repositories**: 4 files
- **Controllers**: 2 files
- **Security Components**: 3 files
- **Configuration**: 2 files
- **Exception Handling**: 2 files

**Total**: 31 Kotlin files + configuration files
