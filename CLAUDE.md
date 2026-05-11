# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**PhanMemTuyenSinh-MVC** is a Vietnamese university admissions system (Hệ thống tuyển sinh). It allows students to calculate estimated admission scores and look up their official admission results. The system supports three admission methods: ĐGNL (Direct Graduate Entrance Exam), THPT (High School Graduation Exam), and VSAT (Vietnam Standardized Assessment Test).

## Build & Run Commands

```bash
# Run application (requires MySQL running on localhost:3306)
mvn spring-boot:run

# Or start MySQL via Docker first, then run
docker-compose up -d
mvn spring-boot:run

# Build JAR
mvn package

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Compile only
mvn clean compile
```

The app starts on **port 8080**. MySQL database name is `xettuyen2026`.

## Architecture

Standard Spring Boot 3.x layered MVC with Thymeleaf server-side rendering. All packages are under `com.tuyensinh.web`.

**Package responsibilities:**
- `controller/` — Maps HTTP routes to services, returns Thymeleaf view names or redirects
- `service/` — All business logic lives here (score calculation, priority points, conversions)
- `repository/` — Spring Data JPA repositories; no custom queries beyond method names
- `entity/` — JPA entities mapped to MySQL tables
- `dto/` — Form objects (input) and result VOs (output); not persisted
- `filter/` — `AuthFilter` protects `/tra-cuu-ket-qua` by checking `HttpSession`
- `config/` — `WebConfig` registers the auth filter as a Spring bean

**Key domain concepts:**
- `ThiSinh` — Student/candidate (authenticated by CCCD + date-of-birth as password)
- `NguyenVongXetTuyen` — A student's ranked admission preference (wish list entry)
- `ToHopMonThi` — Subject group (e.g., A00 = Math+Physics+Chemistry)
- `Nganh` — University major/program with its admission score threshold
- `DiemThiXetTuyen` / `DiemCongXetTuyen` — Exam scores and bonus certificate scores

**Score calculation flow (services):**
1. `TinhDiemXetTuyenService` — Entry point; orchestrates the full score pipeline
2. `QuyDoiService` — Converts raw VSAT/ĐGNL scores to the 10-pt or 30-pt scale
3. `UuTienService` — Adds priority points (ethnicity/region-based, capped rules apply)
4. `DoLechToHopService` — Adjusts scores when converting between subject groups

**Authentication:** Session-based. Login stores student info in `HttpSession`; `AuthFilter` rejects requests to protected routes if session is missing.

## Database

- **DDL:** `ddl-auto=none` — schema is managed manually via SQL scripts in `docs/`
- **Dialect:** `MySQLDialect` (MySQL 8)
- **Credentials (local dev):** `root` / `password` on `localhost:3306`
- Schema SQL files and sample data are in the `docs/` directory

## Key Technology Notes

- **Java 21**, Spring Boot 3.3.5, Jakarta EE namespaces (not `javax.*`)
- **Lombok** is used extensively — use `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` on entities and DTOs
- **Thymeleaf** templates are in `src/main/resources/templates/`; shared layout fragments are in `templates/fragments/`
- **DevTools** is on the classpath — application auto-restarts on class changes during `spring-boot:run`
- Vietnamese text is throughout the codebase (variable names, comments, view labels); UTF-8 is configured at every layer
