# Auth Kotlin Demo

Spring Boot 4.0.1 authentication application with JWT and email verification.

## Environment Setup

### Prerequisites

- Java 21 or higher
- Docker and Docker Compose

### Configure Environment Variables

Create a `.env` file in the project root with the following configuration:

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/auth_demo
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT Configuration
# Generate a secure secret key using: openssl rand -base64 64
JWT_SECRET=your-base64-encoded-secret-key-at-least-256-bits

# Email Configuration (for email verification and password reset)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@authdemo.com

# Application Configuration
APP_BASE_URL=http://localhost:8080
```

**Note:** For Gmail, you need to create an [App Password](https://support.google.com/accounts/answer/185833).

The `.env` file is already configured with default values. Update `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_FROM` if you need email functionality.

## How to Run

### Using Docker Compose

Docker Compose will start the application along with PostgreSQL and MailHog.

```bash
# Build the application JAR
./gradlew clean build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down
```

The application will start on **http://localhost:18080**

Services:
- **Application**: http://localhost:18080
- **PostgreSQL**: localhost:15432
- **MailHog Web UI**: http://localhost:8025 (view sent emails)
- **MailHog SMTP**: localhost:1025

### Alternative: Running Without Docker Compose

If you prefer to run the application without Docker Compose, you need to set up PostgreSQL separately.

#### Setup PostgreSQL

**Option A: Using Docker container**
```bash
docker run --name auth-postgres \
  -e POSTGRES_DB=auth_demo \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16-alpine
```

**Option B: Using local PostgreSQL service**
```bash
createdb auth_demo
```

#### Update Environment Variables

Modify `.env` to use localhost and exposed port instead of the Docker service name:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:15432/auth_demo
```

#### Run the Application

**Using Gradle:**
```bash
./gradlew clean build
./gradlew bootRun
```

**Using JAR:**
```bash
./gradlew clean build
java -jar build/libs/auth-kotlin-demo-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8080**

### Verify Application is Running

**For Docker Compose:**
```bash
curl http://localhost:18080/api/v1/auth/health
```

**For Gradle or JAR:**
```bash
curl http://localhost:8080/api/v1/auth/health
```

Expected response:
```json
{
  "message": "Auth service is running",
  "success": true
}
```

## How to Test

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests "com.aivanouski.auth_kotlin_demo.presentation.controller.AuthControllerTest"
./gradlew test --tests "com.aivanouski.auth_kotlin_demo.presentation.controller.UserControllerTest"
```

### Run Tests with Reports

```bash
./gradlew test
# Open test report
open build/reports/tests/test/index.html
```

### Test Configuration

Tests use Testcontainers for integration testing:
- **PostgreSQL**: Automatically started via Testcontainers
- **MailHog**: Automatically started for email testing
- **Docker**: Required for running tests

**Note:** First test run will download Docker images and may take longer.

### Test Coverage

```bash
# Run tests with coverage
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```
