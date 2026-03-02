# 🛍️ Shopfy

A modern, generic e-commerce platform built with **Java 21** and **Spring Boot 3.4**.  
Sell anything — toys, electronics, clothing, books. Just register your products and go.

> Originally a 2017 university project (Java EE Servlets + JSP + MySQL) called *Toys Catalog*. Fully rebuilt from scratch with modern architecture, ready to evolve into a feature-complete online store.

---

## 🏗️ Architecture

```
api/v1/           ← REST Controllers, DTOs, Exception Handlers
application/      ← Services (use cases / business logic)
domain/           ← Entities, Repository interfaces (pure domain)
infrastructure/   ← Security config, JWT filter, external integrations
```

Layered Architecture inspired by Domain-Driven Design, easy to evolve toward Hexagonal/Clean Architecture.

---

## 🧰 Tech Stack

| Layer             | Technology                              |
|-------------------|-----------------------------------------|
| Language          | Java 21 (LTS)                           |
| Framework         | Spring Boot 3.4                         |
| Persistence       | Spring Data JPA + Hibernate 6           |
| Database          | PostgreSQL 16                           |
| Migrations        | Flyway (V1 schema, V2 seed, V3 users)   |
| Security          | Spring Security + JWT (JJWT 0.12)       |
| API Docs          | SpringDoc OpenAPI 3 / Swagger UI        |
| Containerization  | Docker + Docker Compose                 |
| Testing           | JUnit 5 + Mockito + Testcontainers      |
| Build             | Maven 3.9 (Docker — no local install)   |

---

## 🚀 Running the project

### Requirements
- **Docker** and **Docker Compose** — that's it. No Java, no Maven needed locally.

### Option A — Full stack (app + database)
```bash
docker compose up --build
```
- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### Option B — Development mode (only database, run app from IDE)
```bash
docker compose -f docker-compose.dev.yml up -d
```
Then run `ShopfyApplication` from your IDE.

### Stopping
```bash
docker compose down          # keeps data
docker compose down -v       # removes volumes (wipes database)
```

---

## 📡 API Endpoints

### 🔐 Authentication (public)
| Method | Endpoint                    | Description                        |
|--------|-----------------------------|------------------------------------|
| POST   | `/api/v1/auth/register`     | Register new account (CUSTOMER)    |
| POST   | `/api/v1/auth/login`        | Login and receive JWT token        |

**Register request:**
```json
{ "name": "Alice", "email": "alice@example.com", "password": "Senha@123" }
```

**Login request:**
```json
{ "email": "alice@example.com", "password": "Senha@123" }
```

**Response (both):**
```json
{
  "accessToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": { "id": 1, "name": "Alice", "email": "alice@example.com", "role": "CUSTOMER" }
}
```

Use the token in subsequent requests:
```
Authorization: Bearer eyJhbGci...
```

### 👤 User (authenticated)
| Method | Endpoint          | Auth     | Description        |
|--------|-------------------|----------|--------------------|
| GET    | `/api/v1/me`      | Required | Get own profile    |

### 📦 Products (GET = public, mutations = ADMIN only)
| Method | Endpoint                         | Auth  | Description              |
|--------|----------------------------------|-------|--------------------------|
| GET    | `/api/v1/products`               | —     | List all (paginated)     |
| GET    | `/api/v1/products/{id}`          | —     | Get by ID                |
| GET    | `/api/v1/products/search`        | —     | Search with filters      |
| GET    | `/api/v1/products/featured`      | —     | List featured products   |
| GET    | `/api/v1/products/category/{id}` | —     | List by category         |
| POST   | `/api/v1/products`               | ADMIN | Create product           |
| PUT    | `/api/v1/products/{id}`          | ADMIN | Update product           |
| DELETE | `/api/v1/products/{id}`          | ADMIN | Soft-delete product      |

### 🗂️ Categories (GET = public, mutations = ADMIN only)
| Method | Endpoint                   | Auth  | Description      |
|--------|----------------------------|-------|------------------|
| GET    | `/api/v1/categories`       | —     | List all active  |
| GET    | `/api/v1/categories/{id}`  | —     | Get by ID        |
| POST   | `/api/v1/categories`       | ADMIN | Create category  |
| PUT    | `/api/v1/categories/{id}`  | ADMIN | Update category  |
| DELETE | `/api/v1/categories/{id}`  | ADMIN | Soft-delete      |

### Search example
```
GET /api/v1/products/search?q=action+figure&categoryId=2&minPrice=100&maxPrice=200&page=0&size=10&sort=price,asc
```

---

## 🔑 Default admin account

Seeded automatically by Flyway on first run:

| Field    | Value               |
|----------|---------------------|
| Email    | `admin@shopfy.com`  |
| Password | `Admin@123`         |
| Role     | `ADMIN`             |

> ⚠️ Change this in production via the `JWT_SECRET` and database environment variables.

---

## 🧪 Running tests

```bash
docker run --rm -v "$(pwd)":/workspace -w /workspace \
  maven:3.9-eclipse-temurin-21-alpine mvn test
```

Tests use **Mockito** (unit) and **Testcontainers** (integration — spins up a real PostgreSQL). Docker must be running.

**Current status: 15/15 passing ✅**

| Suite                    | Tests | Status |
|--------------------------|-------|--------|
| `ProductControllerTest`  | 1     | ✅     |
| `JwtServiceTest`         | 6     | ✅     |
| `AuthServiceTest`        | 4     | ✅     |
| `ProductServiceTest`     | 4     | ✅     |

---

## 🗺️ Roadmap

### ✅ Phase 1 — Foundation (done)
- [x] Modern Spring Boot 3.4 + Java 21 project structure
- [x] Product & Category CRUD with pagination
- [x] Search with filters (text, category, price range)
- [x] Soft delete (deactivation, not physical deletion)
- [x] Flyway migrations with seed data (8 categories, 9 products)
- [x] Docker + Docker Compose setup (multi-stage build, ~71s)
- [x] OpenAPI / Swagger UI documentation
- [x] Unit tests + Testcontainers integration tests

### ✅ Phase 2 — Users & Security (done)
- [x] User registration (`POST /auth/register`)
- [x] JWT login (`POST /auth/login`, HMAC-SHA256, JJWT 0.12)
- [x] Roles: `ADMIN`, `CUSTOMER`
- [x] Protected endpoints (write operations require `ROLE_ADMIN`)
- [x] `/api/v1/me` — authenticated profile endpoint
- [x] BCrypt password hashing
- [x] Flyway V3 migration (users table + default admin seed)
- [x] `JwtAuthenticationFilter` (stateless, `OncePerRequestFilter`)
- [x] Unit tests: `JwtServiceTest`, `AuthServiceTest`

### 🛒 Phase 3 — Product Images
- [ ] Image upload (multipart, stored locally or S3)
- [ ] Image URL served via static resource or CDN

### �️ Phase 4 — Shopping Cart & Orders
- [ ] Shopping cart (session or DB-backed)
- [ ] Orders and order status tracking
- [ ] Payment integration (Mercado Pago / Stripe)
- [ ] Transactional email (order confirmation)

### 🔍 Phase 5 — Discovery
- [ ] Full-text search (PostgreSQL FTS or Elasticsearch)
- [ ] Product reviews and ratings
- [ ] Purchase history

### ⚙️ Phase 6 — CI/CD
- [ ] GitHub Actions (build + test + Docker push)
- [ ] Automated test pipeline on pull requests

### 🤖 Phase 7 — AI & Recommendations
- [ ] Collaborative filtering ("customers also bought")
- [ ] Content-based recommendations (similar products)
- [ ] Spring AI integration (semantic search, chatbot)
- [ ] Personalized storefront per user profile

---

## 📁 Legacy

The original 2017 Java EE project (Servlets + JSP + MySQL), then called *Toys Catalog*, is preserved in the `legacy/` folder for historical reference.

---

## 📄 License

MIT
