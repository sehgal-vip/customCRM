# customCRM

A sales pipeline CRM for managing leads, deals, tasks, and documents through a multi-stage deal lifecycle. Built as a full-stack project with a Spring Boot backend and a React frontend.

## What it does

Think of it as a focused alternative to a generic CRM where every feature is tuned to one workflow: lead lands, gets qualified, moves through defined stages with gating checks, and closes into a paid deal. Not a Salesforce clone. Closer to a purpose-built ops tool.

Core capabilities:

- **Lead ingestion** via webhook or manual entry, with de-duplication
- **Deal lifecycle** across configurable phases and stages, with exit-criteria validation before each transition
- **Task management** with list, board, week, and month views, plus overdue flagging
- **Pipeline board** with drag-and-drop between stages
- **Activity reports** with date-range filtering and CSV export
- **Role-based access** (admin, manager, agent) with scoped data visibility
- **Document and attachment handling** for contracts, KYC, and invoices
- **Pricing engine** with configurable components and approval thresholds
- **Audit log** on every admin write operation
- **Notifications** for assignments, stage changes, and deadlines
- **Search** across deals, operators, and tasks

## Tech stack

**Backend**
- Spring Boot 3.2, Java 17
- PostgreSQL with Flyway migrations
- Spring Security with JWT auth
- JPA / Hibernate

**Frontend**
- React 19, TypeScript, Vite
- Tailwind CSS 4
- TanStack Query for server state
- React Hook Form + Zod for forms
- dnd-kit for drag-and-drop

**Testing**
- JUnit + Mockito on the backend
- Vitest + Testing Library on the frontend
- Playwright for end-to-end flows

## Repository layout

```
customCRM/
├── src/                    # Spring Boot backend
│   ├── main/java/...       # controllers, services, repositories, models
│   ├── main/resources/db/  # Flyway migrations
│   └── test/               # backend tests
├── frontend/               # React + Vite app
│   ├── src/                # components, pages, hooks
│   └── e2e/                # Playwright specs
├── scripts/                # dev / seed utilities
├── pom.xml
└── DEPLOYMENT.md
```

## Running locally

### 1. Database

Start PostgreSQL locally (Docker is easiest). The app expects a reachable Postgres instance. Point `spring.datasource.url`, `username`, and `password` at it via `application.yml` under `src/main/resources/` or via env vars.

### 2. Backend

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw spring-boot:run
```

Backend starts on `http://localhost:8080`. Flyway runs migrations on startup.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend serves on `http://localhost:5173`. CORS is configured for `localhost:5173` and `localhost:3000` by default.

## Environment configuration

Environment-specific Spring configs (`src/main/resources/{dev,uat,prod,test}/`) and all `.env*` files are gitignored. You supply your own. At minimum:

- Database URL, user, password
- JWT signing secret
- Webhook signing keys (if using webhook ingestion)
- Upload directory path

## Running tests

```bash
# Backend unit tests
./mvnw test

# Frontend unit tests
cd frontend && npm test

# End-to-end (requires backend + frontend running)
cd frontend && npx playwright test
```

## Deployment

See `DEPLOYMENT.md` for the production deployment notes.

## License

No license specified. Treat as "all rights reserved" unless a LICENSE file is added.
