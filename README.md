# User Management Dashboard – Delivery Plan (v1)

## 1) Goals & Non‑Goals
**Goals**
- Manage users, roles, and permissions from a secure web dashboard.
- Provide auditable trails of every sensitive action.
- Offer clean APIs through an API Gateway consumable by the UI and external clients.
- Ship a production‑ready MVP with CI/CD, observability, and security hardening.

**Non‑Goals (MVP)**
- SSO federation with multiple IdPs (can be Phase 2).
- Granular ABAC policies beyond RBAC (future).
- Multi‑tenant partitioning (optional add‑on later).

---

## 2) Reference Architecture
- **UI Web Application** → talks to **API Gateway**.
- **API Gateway** → routes to **User Service** and **Audit Service**.
- **User Service** → owns user, role, permission data in **User RDB**.
- **Audit Service** → consumes audit events from **Kafka Audit Topic**; persists in **NoSQL Audit DB** and exposes search endpoints.

---

## 3) Tech Stack (proposed)
**Frontend**
- React + Vite + TypeScript; shadcn/ui, Tailwind; React Query; Zod for validation; Playwright for e2e.

**Gateway & Services**
- Java 21 + Spring Boot; OpenAPI first; Swagger UI in dev; Feign clients/RestTemplate internally.
- AuthN/AuthZ: JWT with refresh tokens; RBAC middleware and per‑route guards.

**Data**
- User RDB: PostgreSQL 15+.
- Audit DB: MongoDB or OpenSearch (choose MongoDB MVP; switchable adapter).
- Cache: Redis (session limits, rate limiting, list views).

**Messaging**
- Kafka (1 topic: `audit.events.v1`, 3 partitions, acks=all, compaction disabled; 7–30d retention).

**Infra & DevX**
- Docker Compose for local; Helm + Kubernetes for prod; GitHub Actions CI; Renovate for deps.
- Observability: OpenTelemetry SDK → OTLP → Tempo/Jaeger; Prometheus metrics; Loki logs; Grafana dashboards.
- Secrets: Doppler or Vault (MVP: .env + Docker secrets).

---

## 4) Data Models (MVP)
**User**
- id (uuid), email (unique), username (unique), password_hash, status, created_at, updated_at, last_login_at.

**Role**
- id (uuid), name (unique), description.

**Permission**
- id (uuid), key (unique, e.g., `user.read`), description.

**UserRole**
- user_id (fk), role_id (fk), created_at.

**RolePermission**
- role_id (fk), permission_id (fk).

**AuditEvent** (NoSQL)
- _id, ts (ISO), actor: {id, email}, action (string, e.g., `user.create`), target: {type, id}, context: {ip, ua}, payload (object), outcome (success|deny|error), request_id, trace_id.

### PostgreSQL DDL (sketch)
```sql
create table users (
  id uuid primary key default gen_random_uuid(),
  email citext not null unique,
  username citext not null unique,
  password_hash text not null,
  status text not null default 'active',
  last_login_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create table roles (
  id uuid primary key default gen_random_uuid(),
  name citext not null unique,
  description text
);
create table permissions (
  id uuid primary key default gen_random_uuid(),
  key text not null unique,
  description text
);
create table user_roles (
  user_id uuid references users(id) on delete cascade,
  role_id uuid references roles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (user_id, role_id)
);
create table role_permissions (
  role_id uuid references roles(id) on delete cascade,
  permission_id uuid references permissions(id) on delete cascade,
  primary key (role_id, permission_id)
);
```

---

## 5) API Contracts (OpenAPI sketch)
- `POST /auth/login` → issue access & refresh tokens.
- `POST /auth/refresh`
- `POST /users` (perm: `user.create`)
- `GET /users` with filter/sort/pagination (perm: `user.read`)
- `GET /users/{id}` (perm: `user.read`)
- `PATCH /users/{id}` (perm: `user.update`)
- `DELETE /users/{id}` (perm: `user.delete`)
- `GET /roles`, `POST /roles`, `PATCH /roles/{id}`, `DELETE /roles/{id}`
- `GET /permissions`
- `POST /users/{id}/roles` (add/remove)
- `GET /audit/events` (filters: actor, action, target, time range, outcome; pagination)

**Error model**
```json
{
  "error": "string",
  "code": "string",
  "requestId": "uuid",
  "details": {}
}
```

---

## 6) Eventing
**Topic**: `audit.events.v1`
**Key**: `request_id` or `actor.id`
**Value schema (JSON)**
```json
{
  "ts": "2025-08-25T12:00:00Z",
  "actor": {"id": "uuid", "email": ""},
  "action": "user.update",
  "target": {"type": "user", "id": "uuid"},
  "context": {"ip": "", "ua": ""},
  "payload": {},
  "outcome": "success",
  "trace_id": "",
  "request_id": ""
}
```

---

## 7) Security Controls
- **Password storage**: Argon2id with memory cost ≥ 64MB, time cost ≥ 3.
- **JWT**: RS256, 15‑min access, 7‑day refresh, rotation with reuse detection.
- **RBAC**: route‑level guards; policy cache (Redis) with 60s TTL; admin‑only endpoints.
- **Input**: Bean Validation (Jakarta Validation) in DTOs + size limits on payloads; allowlist CORS.
- **Transport**: HTTPS‑only; HSTS; secure cookies optional.
- **Secrets**: least privilege DB users; app‑level service accounts; rotate quarterly.
- **Audit integrity**: append‑only collection; optional daily digest hash anchored to object storage.

---

## 8) Observability & SRE
- **Tracing**: OTel instrumentation, request_id propagation via `X-Request-Id`.
- **Metrics**: p95 latency, RPS, error rate per endpoint; DB connection pool; Kafka consumer lag.
- **Logs**: structured JSON, redaction of PII.
- **Dashboards**: API latency, auth failures, audit ingestion throughput.
- **Alerts**: 5xx rate > 1% for 5m; consumer lag > threshold; DB CPU > 80% for 10m.

---

## 9) Testing Strategy
- Unit tests (JUnit + Mockito) targeting services and guards.
- Contract tests from OpenAPI using Spring Cloud Contract or Schemathesis.
- e2e tests (Playwright) for critical flows.
- Load tests (k6) target list‑users and audit search.
- Chaos: pod restarts, Kafka broker down, DB failover drills.

---

## 10) Delivery Plan (12–14 weeks, MVP)
**Phase 0 — Project setup (Week 1)**
- Repo scaffolding (multi‑module Maven/Gradle for services, separate frontend).
- CI (lint, test, docker build) + pre‑commit hooks.

**Phase 1 — Auth & Users (Weeks 2–4)**
- User schema & migrations; login/refresh/logout; RBAC MVP; users CRUD + pagination.

**Phase 2 — Audit (Weeks 4–6)**
- Publish audit events from User Service; Kafka infra; Audit Service consumer + search API.

**Phase 3 — UI Dashboard (Weeks 6–9)**
- Layout, table views, filters; user detail view; role management; audit explorer.

**Phase 4 — Hardening & Ops (Weeks 9–11)**
- Observability, rate limiting, permissions editor, data export.

**Phase 5 — Beta & Rollout (Weeks 12–14)**
- Staging soak, performance tuning, docs.

---

## 11) Backlog (initial)
**Epics**
- E1 AuthN/AuthZ
- E2 User CRUD & RBAC
- E3 Audit pipeline
- E4 Admin UI
- E5 Observability & Ops

**Sample Stories**
- As an admin, I can invite a user via email.
- As an admin, I can assign/remove roles.
- As an auditor, I can search events by actor and action within a time window.
- As a developer, I can view request traces across Gateway → services.

---

## 12) Risks & Mitigations
- Kafka operational overhead → Start with a managed cluster or fall back to Redis Streams for MVP if needed.
- Audit data growth → TTL + archiving to S3; partition by month.
- Permission creep → permission registry and PR checks.

---

## 13) Next Steps (what I’ll implement first)
1) Multi‑module Maven/Gradle setup: API Gateway, User Service, Audit Service.
2) PostgreSQL migrations and basic Users API + tests.
3) Audit event publishing stub + consumer skeleton.



