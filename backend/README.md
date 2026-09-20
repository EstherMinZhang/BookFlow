# BookFlow Backend

Spring Boot REST API for the BookFlow bookstore inventory management system.

## Local Run

This module is configured for Java 8 and Spring Boot 2.7 so it can run on the current machine.

```powershell
mvn spring-boot:run
```

The default profile uses an in-memory H2 database:

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

Example request:

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "price": 42.50,
  "stockQuantity": 8
}
```

Checkout request:

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

## PostgreSQL

Set these environment variables when running against PostgreSQL:

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/bookflow"
$env:DATABASE_USERNAME = "postgres"
$env:DATABASE_PASSWORD = "postgres"
$env:HIBERNATE_DIALECT = "org.hibernate.dialect.PostgreSQLDialect"
```
