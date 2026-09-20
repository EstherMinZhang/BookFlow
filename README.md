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

## Next Steps

- Configure PostgreSQL for local development or RDS for production.
- Deploy backend to AWS Elastic Beanstalk.
- Deploy frontend to S3 and CloudFront.
- Add GitHub Actions CI/CD.
