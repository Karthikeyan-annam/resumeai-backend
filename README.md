# 🤖 ResumeIQ AI — Backend API Service

ResumeIQ AI is an enterprise-grade backend service built with **Java 21**, **Spring Boot 3.x**, and **Google Gemini 1.5 Flash**. It provides real-time, asynchronous resume analysis, ATS scoring, skill matching, interview preparation, and dashboard analytics.

---

## 🛠️ Technology Stack
*   **Java 21** & **Spring Boot 3.3.x**
*   **PostgreSQL** (Database)
*   **Flyway** (Schema Migrations)
*   **Spring Security** & **JWT (JJWT 0.12.5)** (Authentication & Refresh Tokens)
*   **Google Gemini 1.5 Flash** (AI Engine)
*   **Apache PDFBox 3.x** (Resume parsing)
*   **Spring Cache** (ConcurrentMapCacheManager)
*   **Spring Boot Actuator** (Monitoring)
*   **Docker** & **Docker Compose** (Containerization)

---

## ⚙️ Environment Variables

Configure these variables in your environment or inside an `.env` file:

| Variable | Description | Default / Example |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection JDBC URL | `jdbc:postgresql://localhost:5432/resumeiq` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` |
| `GEMINI_API_KEY` | Google Gemini API key | `AIzaSy...` |
| `JWT_SECRET` | Base64-encoded secret key for signing JWTs | `404E635266556A586E3272357538782F413F4428...` |
| `JWT_EXPIRATION_MS` | Access Token lifetime in MS | `3600000` (1 Hour) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh Token lifetime in MS | `86400000` (24 Hours) |

---

## 🚀 Quick Start Guide

### Option 1: Run Locally (Maven)
1.  **Start a PostgreSQL database** (e.g. at `localhost:5432` with user/pass `postgres`).
2.  **Export the Gemini API key**:
    ```bash
    $env:GEMINI_API_KEY="your-gemini-key" # PowerShell
    export GEMINI_API_KEY="your-gemini-key" # Bash
    ```
3.  **Run the application**:
    ```bash
    mvn spring-boot:run
    ```
4.  Access **Swagger UI** at `http://localhost:8080/swagger-ui/index.html`.

### Option 2: Run with Docker Compose
1.  **Set the API Key**:
    Add your `GEMINI_API_KEY` to your system environment.
2.  **Run Compose**:
    ```bash
    docker-compose up --build
    ```
    This spins up a PostgreSQL database and the Spring Boot application container, mapping uploads and logs to persistent volumes.

---

## 📡 API Endpoints Matrix

### 1. Authentication (`/api/v1/auth`)
*   `POST /api/v1/auth/register`: Create user. Expects `username`, `email`, `password`.
*   `POST /api/v1/auth/login`: Authenticate credentials. Returns Access JWT & Refresh Token.
*   `POST /api/v1/auth/refresh`: Re-issue access tokens using refresh token.
*   `POST /api/v1/auth/logout`: Revoke active refresh token session.

### 2. Users (`/api/v1/users`)
*   `GET /api/v1/users/profile`: Retrieve user profile info & active plan status.
*   `PUT /api/v1/users/change-password`: Update account password.

### 3. Resumes & Analysis
*   `POST /api/v1/resume/upload`: Upload PDF resume, parse text, and store metadata.
*   `POST /api/v1/resume/analyze`: Generate ATS analysis using an uploaded resume ID.
*   `POST /api/analyze` (or `/api/v1/resume/analyze-with-jd`): Direct multipart upload & analysis in a single request. (Used by React Frontend).
*   `POST /api/v1/ai/interview-questions`: Generate behavioral/technical interview questions.
*   `POST /api/v1/ai/suggestions`: Generate career roadmap.

### 4. Dashboards & Analytics
*   `GET /api/v1/dashboard/history`: Retrieve history of analyzed resumes.
*   `GET /api/v1/dashboard/stats`: Retrieve cached stats (Average ATS score, skills counts, etc.).
*   `GET /api/v1/admin/analytics`: Exposes user counts, total tokens consumed (Admin only).

---

## 📊 Database Schema (Flyway)
Flyway migrations are executed automatically on startup:
*   `V1__initial_schema.sql`: Standard tables for users, roles, and subscriptions.
*   `V2__resume_analysis.sql`: Core tables for uploaded resumes, ATS scores, interview questions, and roadmap plans.
*   `V3__analytics.sql`: Tables for system logging, user audits, and AI usage logging.
