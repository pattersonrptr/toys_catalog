# 🧸 Toys Catalog

A modern, production-ready e-commerce platform built with **Java 21** and **Spring Boot 3.4**.

> Originally a 2017 university project (Java Servlets + JSP + MySQL). Fully rebuilt from scratch with modern architecture, ready to evolve into a feature-complete online store.

---

## 🏗️ Architecture

```
api/v1/           ← REST Controllers, DTOs, Exception Handlers
application/      ← Services (use cases / business logic)
domain/           ← Entities, Repository interfaces (pure domain)
infrastructure/   ← Security config, external integrations
```

This follows a **Layered Architecture** inspired by Domain-Driven Design, making it easy to evolve toward a Hexagonal or Clean Architecture as complexity grows.

---

## 🧰 Tech Stack

| Layer             | Technology                              |
|-------------------|-----------------------------------------|
| Language          | Java 21 (LTS)                           |
| Framework         | Spring Boot 3.4                         |
| Persistence       | Spring Data JPA + Hibernate             |
| Database          | PostgreSQL 16                           |
| Migrations        | Flyway                                  |
| Security          | Spring Security (JWT — Phase 2)         |
| API Docs          | SpringDoc OpenAPI 3 / Swagger UI        |
| Containerization  | Docker + Docker Compose                 |
| Testing           | JUnit 5 + Mockito + Testcontainers      |
| Build             | Maven (via wrapper — no install needed) |

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

### Option B — Development mode (only database, run app from IDE)
```bash
docker compose -f docker-compose.dev.yml up -d
```
Then run `ToysCatalogApplication` from your IDE, or:
```bash
./mvnw spring-boot:run
```

### Stopping
```bash
docker compose down          # keeps data
docker compose down -v       # removes volumes (wipes database)
```

---

## 📡 API Endpoints

### Products
| Method | Endpoint                         | Description              |
|--------|----------------------------------|--------------------------|
| GET    | `/api/v1/products`               | List all (paginated)     |
| GET    | `/api/v1/products/{id}`          | Get by ID                |
| GET    | `/api/v1/products/search`        | Search with filters      |
| GET    | `/api/v1/products/featured`      | List featured products   |
| GET    | `/api/v1/products/category/{id}` | List by category         |
| POST   | `/api/v1/products`               | Create product           |
| PUT    | `/api/v1/products/{id}`          | Update product           |
| DELETE | `/api/v1/products/{id}`          | Soft-delete product      |

### Categories
| Method | Endpoint                   | Description      |
|--------|----------------------------|------------------|
| GET    | `/api/v1/categories`       | List all active  |
| GET    | `/api/v1/categories/{id}`  | Get by ID        |
| POST   | `/api/v1/categories`       | Create category  |
| PUT    | `/api/v1/categories/{id}`  | Update category  |
| DELETE | `/api/v1/categories/{id}`  | Soft-delete      |

### Search example
```
GET /api/v1/products/search?q=saint+seiya&categoryId=2&minPrice=100&maxPrice=200&page=0&size=10&sort=price,asc
```

---

## 🧪 Running tests

```bash
./mvnw test
```
Tests use **Testcontainers** — a real PostgreSQL container is spun up automatically. Docker must be running.

---

## 🗺️ Roadmap

### ✅ Phase 1 — Foundation (done)
- [x] Modern Spring Boot 3 + Java 21 project structure
- [x] Product & Category CRUD with pagination
- [x] Search with filters (text, category, price range)
- [x] Soft delete (deactivation, not physical deletion)
- [x] Flyway migrations with seed data
- [x] Docker + Docker Compose setup
- [x] OpenAPI / Swagger UI documentation
- [x] Unit tests + Testcontainers integration tests

### 🔐 Phase 2 — Users & Security
- [ ] User registration and login
- [ ] JWT authentication
- [ ] Roles: `ADMIN`, `CUSTOMER`
- [ ] Admin panel (product/category management)

### 🛒 Phase 3 — Shopping
- [ ] Shopping cart (Redis-backed)
- [ ] Orders and order status tracking
- [ ] Payment integration (Mercado Pago / Stripe)
- [ ] Transactional email (order confirmation)

### 🔍 Phase 4 — Discovery
- [ ] Full-text search (PostgreSQL FTS or Elasticsearch)
- [ ] Product reviews and ratings
- [ ] Purchase history

### 🤖 Phase 5 — AI & Recommendations
- [ ] Collaborative filtering ("customers also bought")
- [ ] Content-based recommendations (similar products)
- [ ] Spring AI integration (semantic search, chatbot)
- [ ] Personalized storefront per user profile

---

## 📁 Legacy

The original 2017 Java EE project (Servlets + JSP + MySQL) is preserved in the `legacy/` folder for historical reference.

---

## �� License

MIT
