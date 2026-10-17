# BookFlow


BookFlow is a full-stack bookstore inventory management system built with a Spring Boot REST API backend, a React TypeScript frontend, and database support for PostgreSQL.

## Current Features

- Book inventory CRUD API and UI.
- Customer CRUD API and UI.
- Order checkout API and UI.
- Checkout validates stock and decrements inventory inside one transaction.
- Centralized API exception handling.
- Request validation for books, customers, and checkout requests.
- Local development defaults to an H2 in-memory database.
- PostgreSQL can be enabled through environment variables.

## Local Setup

Install frontend dependencies once after cloning the project, after deleting `node_modules`, or after dependency changes:

```powershell
cd frontend
npm install
```

You do not need to run `npm install` every time.

## Run Locally

Open two terminals from the project root.

Terminal 1, backend:

```powershell
cd backend
mvn spring-boot:run
```

Terminal 2, frontend:

```powershell
cd frontend
npm run dev
```

Then open the frontend URL shown in the frontend terminal.

## API Endpoints

```text
GET    /api/books
GET    /api/books/{id}
POST   /api/books
PUT    /api/books/{id}
DELETE /api/books/{id}

GET    /api/customers
GET    /api/customers/{id}
POST   /api/customers
PUT    /api/customers/{id}
DELETE /api/customers/{id}

GET    /api/orders
GET    /api/orders/{id}
POST   /api/orders/checkout
```

Example book request:

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "price": 42.50,
  "stockQuantity": 8
}
```

Example checkout request:

```json
{
  "customerId": 1,
  "items": [
    {
      "bookId": 1,
      "quantity": 2
    }
  ]
}
```

## Database

By default, local development uses H2 in-memory database, so data resets when the backend restarts.

To run against PostgreSQL, set:

```powershell
$env:DATABASE_URL = "<your-postgresql-jdbc-url>"
$env:DATABASE_USERNAME = "<your-database-username>"
$env:DATABASE_PASSWORD = "<your-database-password>"
$env:HIBERNATE_DIALECT = "org.hibernate.dialect.PostgreSQLDialect"
```

## Verification

Backend tests:

```powershell
cd backend
mvn test
```

Frontend build:

```powershell
cd frontend
npm run build
```

## CI/CD

The workflow is defined in `.github/workflows/ci-cd.yml`.

- Every push and every pull request targeting `main` runs backend tests and the frontend build in parallel.
- Backend: Java 8, Maven `clean verify`, and a temporary PostgreSQL 16 service. Missing tests fail the build.
- Frontend: Node.js 22, locked dependency installation with `npm ci`, TypeScript checks, and the Vite production build. There is no separate frontend unit test suite yet.
- Test reports and successful build artifacts are retained for seven days.
- Azure deployment jobs run only on `main` outside pull requests, after checks pass, and when the repository variable `AZURE_DEPLOY_ENABLED` is explicitly set to `true`. Leave it unset while setting up CI.

Local verification uses the same build commands. Backend tests default to H2 locally; CI switches them to PostgreSQL with `TEST_DATABASE_*` and `TEST_HIBERNATE_DIALECT`. A local pass does not verify the GitHub runner or Azure deployment.

After the first successful GitHub run, configure branch protection for `main` to require the `Backend tests and build` and `Frontend typecheck and build` checks before merging. Adding the workflow alone does not enable branch protection.

## Next Steps

- Push and verify the workflow in GitHub Actions after reviewing the local changes.
- Create Azure Database for PostgreSQL, Azure App Service for the backend, and Azure Static Web Apps for the frontend.
- Configure production database settings, CORS, deployment credentials, and the frontend API URL before enabling Azure deployments.
