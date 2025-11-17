# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is Yudao Boot Mini (芋道快速开发平台精简版) - a Java-based rapid development platform built on Spring Boot. It's a simplified version of the full Yudao platform, focusing on core system functionality and infrastructure.

**Current State:** This is the "mini" version with only essential modules enabled. Additional business modules (member, bpm, pay, mall, etc.) are available but commented out in the configuration for faster development and compilation.

## Quick Start

1. **Prerequisites**: Java 8, MySQL 5.7+, Redis, Maven Daemon (mvnd)
2. **Database Setup**: Create database `ruoyi-vue-pro` and run SQL scripts from `sql/` directory
3. **Configuration**: Update database credentials in `application-local.yaml`
4. **Build**: `mvnd clean compile`
5. **Run**: `mvnd spring-boot:run`

## Build System

**IMPORTANT: Use mvnd instead of mvn**

This project uses Maven Daemon (mvnd) for faster builds. Always use `mvnd` commands instead of `mvn`.

### Common Commands

```bash
# Build the entire project
mvnd clean compile

# Run tests
mvnd test

# Package the application
mvnd clean package -DskipTests

# Run a single test
mvnd test -Dtest=ClassName#methodName

# Install dependencies
mvnd clean install

# Run the application
mvnd spring-boot:run

# Build specific module
mvnd clean compile -pl yudao-module-system

# Run tests for specific module
mvnd test -pl yudao-module-system
```

### Development Profiles

The project uses Spring profiles for different environments:
- `local` - Local development (default)
- `dev` - Development environment
- `test` - Testing environment
- `prod` - Production environment

## Project Architecture

### Module Structure

The project follows a multi-module Maven architecture:

- **yudao-dependencies**: Maven BOM for dependency version management
- **yudao-framework**: Core framework components and Spring Boot starters
- **yudao-server**: Main application container (thin wrapper that aggregates modules)
- **yudao-module-system**: Core business functionality (users, roles, permissions, dictionaries)
- **yudao-module-infra**: Infrastructure services (code generation, file management, monitoring)

**Note**: This is the "mini" version with only core modules enabled. Additional modules (member, bpm, pay, mall, etc.) are commented out in the root pom.xml and can be enabled as needed.

### Package Organization

Each module follows a standard package structure:

```
cn.iocoder.yudao.module.{module-name}
├── api/                 # API interfaces for other modules
├── controller/          # REST controllers (admin/app subpackages)
├── service/            # Business logic layer
├── convert/            # Object mapping with MapStruct
├── dal/                # Data access layer
│   ├── dataobject/     # Entity classes (DO)
│   └── mysql/          # MyBatis mapper interfaces
├── enums/              # Enum definitions
└── framework/          # Module-specific framework code
```

### Framework Components

Key framework starters in `yudao-framework`:

**Core Framework Starters:**
- `yudao-spring-boot-starter-web`: Web MVC, API logging, exception handling
- `yudao-spring-boot-starter-security`: JWT authentication and authorization
- `yudao-spring-boot-starter-mybatis`: Database access with MyBatis Plus
- `yudao-spring-boot-starter-redis`: Redis integration and caching
- `yudao-spring-boot-starter-job`: Quartz scheduled tasks
- `yudao-spring-boot-starter-mq`: Message queue integration
- `yudao-spring-boot-starter-websocket`: WebSocket communication

**Business Framework Starters:**
- `yudao-spring-boot-starter-biz-tenant`: Multi-tenant support
- `yudao-spring-boot-starter-biz-data-permission`: Data-level permissions
- `yudao-spring-boot-starter-biz-ip`: IP-based access control

**Infrastructure Framework Starters:**
- `yudao-spring-boot-starter-monitor`: Application monitoring (Spring Boot Admin, SkyWalking)
- `yudao-spring-boot-starter-protection`: Service protection (distributed lock, rate limiting, idempotency)
- `yudao-spring-boot-starter-excel`: Excel import/export functionality

**Common Utilities:**
- `yudao-common`: Common utilities and shared components

## Development Guidelines

### Code Generation

The project includes a code generator in the infra module. Use it to generate CRUD code for new entities.

### Database Operations

- Uses MyBatis Plus for database operations
- Follows soft delete pattern with `deleted` field
- Multi-tenant data isolation is built-in
- Entity classes are in `dal/dataobject/` package
- Mapper interfaces are in `dal/mysql/` package
- Tenants are enabled by default use mybatis-plus TenantLineHandler @src/main/java/cn/iocoder/yudao/framework/tenant/core/db/TenantDatabaseInterceptor.java

### API Development

- RESTful API design with standardized response format (`CommonResult<T>`)
- Controllers are separated into `admin/` and `app/` subpackages
- Use MapStruct for object mapping between layers
- Input validation with Bean Validation annotations

### Security

- JWT-based stateless authentication
- Role-based access control
- Data-level permissions
- Multi-tenant security isolation

## Configuration

### Main Configuration Files

- `yudao-server/src/main/resources/application.yaml` - Main configuration (shared across environments)
- `yudao-server/src/main/resources/application-local.yaml` - Local development (port 48080, mock auth enabled)
- `yudao-server/src/main/resources/application-dev.yaml` - Development environment

### Key Configuration Properties

- `spring.profiles.active` - Active profile (default: local)
- `yudao.info.base-package` - Base package for component scanning (`cn.iocoder.yudao`)
- `yudao.tenant.enable` - Multi-tenant feature toggle (enabled by default)
- `mybatis-plus.configuration.map-underscore-to-camel-case` - Database field mapping
- `spring.datasource.dynamic.datasource.master.url` - Primary database connection
- `spring.redis.host` - Redis connection host
- `yudao.security.mock-enable` - Mock authentication for local development

## Testing

### Test Structure

- Unit tests use JUnit 5 and Mockito
- Test classes follow the same package structure as main code
- Use `@SpringBootTest` for integration tests
- Embedded Redis is available for testing

### Running Tests

```bash
# Run all tests
mvnd test

# Run tests for specific module
mvnd test -pl yudao-module-system

# Run single test class
mvnd test -Dtest=ClassName

# Run single test method
mvnd test -Dtest=ClassName#methodName
```

## Database Setup

### Initial Setup

1. Create MySQL database
2. Run SQL scripts from `sql/` directory
3. Configure database connection in application-local.yaml

### Database Migrations

- The project uses manual SQL scripts for database changes
- Check `sql/` directory for initialization and migration scripts
- Follow naming convention: `V{version}__{description}.sql`

## Common Development Tasks

### Adding New Module

1. Create new module directory and pom.xml
2. Add module to root pom.xml
3. Create standard package structure
4. Add dependency in yudao-server/pom.xml
5. Configure component scanning if needed

### Adding New Entity

1. Create entity class in `dal/dataobject/`
2. Create mapper interface in `dal/mysql/`
3. Create service interface and implementation
4. Create controller with REST endpoints
5. Use code generator for CRUD operations

### API Development

1. Create DTO classes for request/response
2. Create MapStruct converter
3. Implement service layer
4. Create controller with validation
5. Add API documentation annotations

## Troubleshooting

### Common Issues

- **Build failures**: Ensure you're using `mvnd` not `mvn`
- **Component not found**: Check package scanning configuration
- **Database connection**: Verify datasource configuration
- **Permission issues**: Check security configuration and role assignments

### Getting Help

- Check project README.md for detailed documentation
- Visit official documentation: https://doc.iocoder.cn/
- Review existing code patterns in similar modules
- Check test cases for usage examples

## Technology Stack

- **Java 8**
- **Spring Boot 2.7.18**
- **MyBatis Plus 3.5.7**
- **Redis**
- **MySQL 5.7/8.0+**
- **Maven 3.9+**
- **JUnit 5**
- **Mockito**
- **Lombok 1.18.38**
- **MapStruct 1.6.3**
- **Knife4j** (API documentation)
- **Druid** (Database connection pool)
- **Redisson** (Redis client)
- **Flowable** (Workflow engine)
- **Quartz** (Scheduled tasks)