# 💻 CBS Backend

A **Coach Booking System (CBS) Backend** built with **Spring Boot 4**, **Java 21**, **Spring Security + JWT**, and **PostgreSQL**. It exposes a RESTful API consumed by the [CBS Frontend](https://github.com/rugvedpatil0803/cbs-frontend).

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Programming language |
| Spring Boot | 4.0.4 | Application framework |
| Spring Web | — | REST API |
| Spring Security | — | Authentication & Authorization |
| Spring Data JPA | — | ORM / Database layer |
| JJWT | 0.12.5 | JWT token generation & validation |
| PostgreSQL | — | Relational database |
| Lombok | — | Boilerplate reduction |
| spring-dotenv | 3.0.0 | `.env` file support |
| Jakarta Validation | — | Request validation |
| Maven | — | Build tool |

---

## 📁 Project Structure

```
cbs-backend/
├── src/
│   └── main/
│       ├── java/com/project/cbs/        # Main application source
│       └── resources/
│           └── application.properties  # App configuration
├── .mvn/wrapper/                        # Maven wrapper
├── pom.xml                              # Project dependencies
├── mvnw / mvnw.cmd                      # Maven wrapper scripts
└── .gitignore
```

---

## ⚙️ Prerequisites

Make sure the following are installed:

- **Java 21** — [Download here](https://adoptium.net/)
- **Maven** (or use the included `mvnw` wrapper)
- **PostgreSQL** — running locally or remotely

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/rugvedpatil0803/cbs-backend.git
cd cbs-backend
```

### 2. Configure Environment Variables

Create a `.env` file in the project root (the app uses `spring-dotenv` to load it):

```env
DB_URL=jdbc:postgresql://localhost:5432/cbs_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret_key
```

Or configure directly in `src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

### 3. Set Up PostgreSQL Database

```sql
CREATE DATABASE cbs_db;
```

### 4. Build and Run

Using the Maven wrapper (no global Maven install required):

```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The server will start at:

```
http://localhost:8080
```

---


📊 Initial Data Setup (IMPORTANT)

To run the project correctly, role data and api link data are mandatory.

✅ Required: tbl_role.csv


INSERT INTO tbl_role 
(id, created_at, is_active, is_deleted, role_description, role_name, updated_at)
VALUES
(1, '2026-03-23 05:20:41.142', true, false, 'Full system access', 'ADMIN', '2026-03-23 05:20:41.142'),
(2, '2026-03-23 05:20:41.142', true, false, 'Can manage sessions and participants', 'COACH', '2026-03-23 05:20:41.142'),
(3, '2026-03-23 05:20:41.142', true, false, 'Can book and attend sessions', 'PARTICIPANT', '2026-03-23 05:20:41.142');


✅  Optional: tbl_api_role_link.csv

INSERT INTO tbl_api_role_link
(id, allowed_actions, api_url, created_at, is_active, is_deleted, updated_at, role_id)
VALUES
(4, 'POST', '/api/session/create', '2026-03-23 10:46:13.744', true, false, '2026-03-23 10:46:13.744', 1),
(5, 'POST', '/api/session/create', '2026-03-23 10:46:23.943', true, false, '2026-03-23 10:46:23.943', 2),
(6, 'PUT', '/api/session/update', '2026-03-23 11:11:41.974', true, false, '2026-03-23 11:11:41.974', 1),
(7, 'PUT', '/api/session/update', '2026-03-23 11:11:50.725', true, false, '2026-03-23 11:11:50.725', 2),
(8, 'POST', '/api/booking/create', '2026-03-23 11:55:31.605', true, false, '2026-03-23 11:55:31.605', 1),
(9, 'POST', '/api/booking/create', '2026-03-23 11:55:41.596', true, false, '2026-03-23 11:55:41.596', 3),
(10, 'DELETE', '/api/booking/unenroll', '2026-03-23 13:03:06.979', true, false, '2026-03-23 13:03:06.979', 1),
(11, 'DELETE', '/api/booking/unenroll', '2026-03-23 13:03:16.985', true, false, '2026-03-23 13:03:16.985', 3),
(1, 'GET,PUT', '/api/user/profile', '2026-03-23 09:57:47.895', true, false, '2026-03-24 01:23:13.600', 1),
(2, 'GET,PUT', '/api/user/profile', '2026-03-23 09:57:57.051', true, false, '2026-03-24 01:23:13.600', 2),
(3, 'GET,PUT', '/api/user/profile', '2026-03-23 09:58:07.135', true, false, '2026-03-24 01:23:13.600', 3),
(12, 'GET', '/api/session/upcoming', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 1),
(13, 'GET', '/api/session/ongoing', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 1),
(14, 'GET', '/api/session/completed', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 1),
(15, 'GET', '/api/session/upcoming', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 3),
(16, 'GET', '/api/session/ongoing', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 3),
(17, 'GET', '/api/session/completed', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 3),
(18, 'GET', '/api/booking/my-bookings', '2026-03-24 06:51:32.789', true, false, '2026-03-24 06:51:32.789', 1),
(19, 'GET', '/api/booking/my-bookings', '2026-03-24 06:51:32.789', true, false, '2026-03-24 06:51:32.789', 3),
(20, 'POST', '/api/feedback/create', '2026-03-24 08:12:23.028', true, false, '2026-03-24 08:12:23.028', 1),
(21, 'POST', '/api/feedback/create', '2026-03-24 08:12:23.028', true, false, '2026-03-24 08:12:23.028', 3),
(22, 'GET', '/api/feedback/session/', '2026-03-24 08:12:23.028', true, false, '2026-03-24 08:12:23.028', 1),
(23, 'GET', '/api/feedback/session/', '2026-03-24 08:12:23.028', true, false, '2026-03-24 08:12:23.028', 2),
(24, 'GET', '/api/feedback/user', '2026-03-24 08:12:23.028', true, false, '2026-03-24 08:12:23.028', 1),
(25, 'GET', '/api/feedback/user', '2026-03-24 08:12:23.028', true, false, '2026-03-24 08:12:23.028', 3),
(26, 'GET', '/api/session/my-sessions', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 2),
(27, 'GET', '/api/session/details', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 2),
(28, 'GET', '/api/session/details', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 3),
(29, 'DELETE', '/api/session/delete', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 2),
(30, 'DELETE', '/api/session/delete', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 1),
(31, 'GET', '/api/user/list-by-roles', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 1),
(32, 'PUT', '/api/user/deactivate', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 1),
(33, 'GET', '/api/user/bookingslist', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 1),
(34, 'GET', '/api/session/analytics', '2026-03-24 05:18:15.462', true, false, '2026-03-24 05:18:15.462', 1);







👉 Without this, authentication/authorization will fail.



---

## 🔐 Security

- Authentication is handled via **JWT (JSON Web Tokens)** using the JJWT 0.12.5 library.
- **Spring Security** protects endpoints — public routes (login/register) are accessible without a token; all others require a valid `Authorization: Bearer <token>` header.

---

## 📡 API Overview

Base URL: `http://localhost:8080`

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | ❌ |
| POST | `/api/auth/login` | Login and receive JWT | ❌ |
| GET | `/api/accounts` | Get account details | ✅ |
| POST | `/api/transactions` | Perform a transaction | ✅ |
| GET | `/api/transactions` | View transaction history | ✅ |

> Refer to the source code in `src/main/java/com/project/cbs/` for the complete list of endpoints.

---

## 🧪 Running Tests

```bash
./mvnw test
```

---

## 🏗️ Build for Production

```bash
./mvnw clean package
java -jar target/cbs-backend-0.0.1-SNAPSHOT.jar
```

---

## 🔗 Related Repository

- **Frontend:** [cbs-frontend](https://github.com/rugvedpatil0803/cbs-frontend) — React + TypeScript + Vite

---

## 👨‍💻 Author

**Rugved Patil**
- GitHub: [@rugvedpatil0803](https://github.com/rugvedpatil0803)
