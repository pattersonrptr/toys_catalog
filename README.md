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
| Object Storage    | MinIO (S3-compatible) via AWS SDK v2    |
| MIME Detection    | Apache Tika 3.1                         |
| API Docs          | SpringDoc OpenAPI 3 / Swagger UI        |
| Containerization  | Docker + Docker Compose                 |
| Testing           | JUnit 5 + Mockito + Testcontainers      |
| Build             | Maven 3.9 (Docker — no local install)   |

---

## 🚀 Running the project

### Requirements
- **Docker** and **Docker Compose** — that's it. No Java, no Maven needed locally.

### Option A — Full stack (app + database + MinIO)
```bash
docker compose up --build
```
- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html  
  *(click **Authorize 🔒**, paste the token from `POST /auth/login` to unlock protected endpoints)*
- Health: http://localhost:8080/actuator/health
- **MinIO Console**: http://localhost:9001 *(user: `minioadmin`, password: `minioadmin`)*

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
| POST   | `/api/v1/products/{id}/image`    | ADMIN | Upload / replace image   |
| DELETE | `/api/v1/products/{id}/image`    | ADMIN | Remove image             |

### 🖼️ Image Upload

Upload a product image with `multipart/form-data`, field name `file`:

```bash
curl -X POST http://localhost:8080/api/v1/products/1/image \
  -H "Authorization: Bearer <token>" \
  -F "file=@photo.jpg"
```

**Rules:**
- Accepted MIME types: `image/jpeg`, `image/png`, `image/webp` (detected via Apache Tika — content-type header is ignored)
- Maximum size: **5 MB** per file
- Existing image is automatically replaced (old file deleted from MinIO)

**Response (200):**
```json
{ "productId": 1, "imageUrl": "http://localhost:9000/shopfy-images/products/1/abc123.jpg" }
```

The `imageUrl` is also returned in every `GET /products/{id}` response. The URL is publicly accessible (no auth required).

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

## ⚙️ Environment Variables

| Variable             | Default                      | Description                                  |
|----------------------|------------------------------|----------------------------------------------|
| `DB_HOST`            | `localhost`                  | PostgreSQL host                              |
| `DB_PORT`            | `5432`                       | PostgreSQL port                              |
| `DB_NAME`            | `shopfy`                     | Database name                                |
| `DB_USER`            | `shopfy`                     | Database user                                |
| `DB_PASSWORD`        | `shopfy`                     | Database password                            |
| `JWT_SECRET`         | (insecure dev default)       | HMAC-SHA256 key — **must change in prod**    |
| `JWT_EXPIRATION_MS`  | `86400000` (24 h)            | Token lifetime in milliseconds               |
| `STORAGE_ENDPOINT`   | `http://localhost:9000`      | MinIO / S3 API endpoint                      |
| `STORAGE_BUCKET`     | `shopfy-images`              | Target bucket name                           |
| `STORAGE_ACCESS_KEY` | `minioadmin`                 | MinIO / S3 access key                        |
| `STORAGE_SECRET_KEY` | `minioadmin`                 | MinIO / S3 secret key                        |
| `STORAGE_PUBLIC_URL` | same as `STORAGE_ENDPOINT`   | Public base URL for object downloads (CDN)   |
| `SERVER_PORT`        | `8080`                       | HTTP port                                    |

> In Docker Compose, `STORAGE_ENDPOINT` is automatically set to `http://minio:9000` for container-to-container communication. `STORAGE_PUBLIC_URL` should point to the externally reachable MinIO address.

---

## 🧪 Running tests

### Unit / slice tests (no Docker required)

```bash
docker run --rm -v "$(pwd)":/workspace -w /workspace \
  maven:3.9-eclipse-temurin-21-alpine mvn test
```

Tests use **H2 in-memory** (PostgreSQL compatibility mode) + **Mockito**. No running containers needed.

**Current status: 15/15 passing ✅**

| Suite                    | Tests | Status |
|--------------------------|-------|--------|
| `ProductControllerTest`  | 1     | ✅     |
| `JwtServiceTest`         | 6     | ✅     |
| `AuthServiceTest`        | 4     | ✅     |
| `ProductServiceTest`     | 4     | ✅     |

### Smoke tests (requires running stack)

```bash
docker compose up --build -d
./scripts/smoke/run_all_tests.sh
```

**Current status: 32/32 passing ✅** (image tests require running MinIO stack)

| Suite            | Tests | Status |
|------------------|-------|--------|
| Authentication   | 7     | ✅     |
| User (/me)       | 3     | ✅     |
| Categories       | 10    | ✅     |
| Products         | 12    | ✅     |
| Product Images   | 11    | ✅     |

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
- [x] Swagger UI **Authorize** button (`bearerAuth` SecurityScheme)
- [x] Smoke test suite (32 tests across 4 suites, `scripts/smoke/`)
- [x] H2 in-memory for unit/slice tests — no Docker required
- [x] Unit tests: `JwtServiceTest`, `AuthServiceTest`

### ✅ Phase 3 — Product Images (done)
- [x] MinIO object storage (S3-compatible) via Docker Compose
- [x] AWS SDK v2 `S3Client` wired with path-style access for MinIO
- [x] Apache Tika MIME detection (prevents content-type spoofing)
- [x] `POST /api/v1/products/{id}/image` — upload / replace (JPEG, PNG, WebP, max 5 MB)
- [x] `DELETE /api/v1/products/{id}/image` — remove image + delete from storage
- [x] Automatic old-image cleanup on replace
- [x] Publicly accessible image URLs (anonymous download bucket policy)
- [x] `S3Exception → 502`, `MaxUploadSizeExceededException → 400` in GlobalExceptionHandler
- [x] Smoke test suite extended (`test_images.sh`, 11 tests)

### 🛒 Phase 4 — Shopping Cart & Orders
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
