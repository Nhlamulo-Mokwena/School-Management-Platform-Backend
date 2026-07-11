# School Management Platform — Backend

The backend API for **SchoolApply**, a full-stack school admissions and management
platform built for South African primary and high schools. This service handles
authentication, application processing, and data for the platform's admin, teacher,
and parent-facing dashboards.

> 🔗 Frontend repo: [School-Management-Platform-Frontend](https://github.com/Nhlamulo-Mokwena/School-Management-Platform-Frontend)

## ✨ Features

- 🔐 JWT-based authentication with role-based access control (`ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_PARENT`)
- 📝 Endpoints for a multi-step school application process
- 📁 Document upload handling for admissions (ID copies, report cards, proof of residence, etc.)
- 🧑‍💼 Role-scoped access enforced via `@PreAuthorize` annotations, with roles carried in the JWT payload
- 🗄️ PostgreSQL persistence via JPA/Hibernate
- 📰 Endpoints for a public school news and announcements feed

## 🛠️ Tech Stack

- **Language / Framework:** Java, Spring Boot
- **Persistence:** Spring Data JPA / Hibernate, PostgreSQL
- **Auth:** JWT (JJWT), Spring Security
- **Build tool:** Maven / Gradle *(update to match your project)*
- **Utilities:** Lombok

## 🚀 Getting Started

```bash
# Clone the repo
git clone https://github.com/Nhlamulo-Mokwena/School-Management-Platform-Backend.git
cd School-Management-Platform-Backend/backend

# Configure your database connection in application.properties / application.yml
# spring.datasource.url=jdbc:postgresql://localhost:5432/schoolapply
# spring.datasource.username=...
# spring.datasource.password=...

# Run the app
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080` by default and is consumed by the
[SchoolApply frontend](https://github.com/Nhlamulo-Mokwena/School-Management-Platform-Frontend).

## 📂 Project Structure

```
backend/
├── src/main/java/...     # Controllers, services, repositories, entities
├── src/main/resources/   # application.properties, migrations
└── pom.xml / build.gradle
```

*(Update this section once your package structure is finalised — it's one of the
first things reviewers look at to understand how the codebase is organised.)*

## 🧭 Project Status

Actively in development. Current focus: application workflow endpoints and
role-based dashboard data.

## 👤 Author

**Phillemon Nhlamulo Mokwena**
[GitHub](https://github.com/Nhlamulo-Mokwena) ·
[LinkedIn](https://www.linkedin.com/in/nhlamulo-mokwena-947a0b321/)
