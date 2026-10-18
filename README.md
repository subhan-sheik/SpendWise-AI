# SpendWise AI

A full-stack personal finance tracker for recording income and expenses, monitoring monthly spending, and reviewing transactions through a dashboard. The current version includes transaction management, validation, API documentation, and automated tests. AI-powered insights are a planned enhancement, not part of the current implemented feature set.

## Features

### Transaction management
- Create, view, update, and delete income and expense transactions.
- Record transaction type, amount, category, date, and optional description.
- Validate transaction input: type must be `INCOME` or `EXPENSE`, amount must be positive and no more than ₹1 crore, category is required, and the date cannot be in the future.

### Dashboard and usability
- Monthly income, expense, balance, and budget summaries.
- Category-wise visualisation using Recharts.
- Search, transaction-type filtering, sorting, and pagination.
- CSV export.

### Backend quality
- REST APIs built with Spring Boot.
- Persistence using Spring Data JPA and MySQL.
- Centralised validation and exception responses.
- OpenAPI/Swagger documentation.
- Automated service, controller, and repository tests.
- JaCoCo coverage report; the last reviewed report showed 97% overall instruction coverage. Coverage can change as the code and tests change.

## Technology stack

| Layer | Technologies |
|---|---|
| Frontend | React, Vite, JavaScript, CSS, Recharts |
| Backend | Java 25, Spring Boot 4.1.1, Spring MVC, Spring Data JPA |
| Database | MySQL for application data; H2 for repository tests |
| Validation and API documentation | Jakarta Bean Validation, Spring REST, Springdoc OpenAPI / Swagger UI |
| Testing | JUnit 5, Mockito, MockMvc, Spring Boot test support |
| Coverage | JaCoCo |

## REST API

Base URL: `http://localhost:9999`

| Method | Endpoint | Purpose | Successful response |
|---|---|---|---|
| `GET` | `/api/transactions` | Retrieve all transactions | `200 OK` |
| `POST` | `/api/transactions` | Create a transaction | `201 Created` |
| `PUT` | `/api/transactions/{id}` | Update an existing transaction | `200 OK` |
| `DELETE` | `/api/transactions/{id}` | Delete a transaction | `204 No Content` |

Swagger UI (when the backend is running): [`http://localhost:9999/swagger-ui/index.html`](http://localhost:9999/swagger-ui/index.html)

### Example request

```json
{
  "type": "EXPENSE",
  "amount": 500,
  "category": "Food",
  "date": "2026-10-12",
  "description": "Lunch"
}
```

### Error handling

Invalid input returns `400 Bad Request`, for example:

```json
{
  "message": "Validation failed",
  "errors": {
    "amount": "Amount must be greater than zero"
  }
}
```

When a transaction ID does not exist for an update or delete, the API returns `404 Not Found`, for example:

```json
{
  "message": "Resource not found",
  "errors": {
    "transaction": "Transaction not found with id: 999"
  }
}
```

## Prerequisites

- JDK 25 (or the JDK configured for this project)
- Maven
- MySQL for the application database
- Node.js and npm for the React frontend

## Running locally

### 1. Configure the database

Create a MySQL database and update the backend's `src/main/resources/application.properties` with your local database connection details. Use your own database name, username, password, and configured MySQL port. Do not commit real credentials to GitHub.

Example template (replace the placeholders and keep this aligned with your existing configuration):

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/YOUR_DATABASE_NAME
spring.datasource.username=YOUR_DATABASE_USERNAME
spring.datasource.password=YOUR_DATABASE_PASSWORD
```

### 2. Run the backend

Open a terminal in the backend directory (the directory containing `pom.xml`) and run:

```bash
mvn spring-boot:run
```

The backend is configured to run at `http://localhost:9999` in the current local development setup. Confirm `server.port` in `application.properties` if needed.

### 3. Run the frontend

Open a terminal in the React frontend directory (the directory containing `package.json`) and run:

```bash
npm install
npm run dev
```

The Vite development server is currently used at `http://localhost:5173`. If the port differs on your machine, use the URL printed by Vite.

## Running tests

From the backend directory containing `pom.xml`:

Run the tests:

```bash
mvn test
```

Run the full Maven verification lifecycle, including tests and the JaCoCo report:

```bash
mvn verify
```

Open the coverage report at:

```text
target/site/jacoco/index.html
```

Repository tests use H2 so they can exercise persistence without using the regular MySQL database. The full Maven suite was run successfully during project development.

## Suggested repository layout

Adjust this example to match your actual folders before committing:

```text
SpendWise-AI/
├── backend/
│   ├── pom.xml
│   └── src/
├── frontend/
│   ├── package.json
│   └── src/
└── README.md
```

## Future enhancements

- AI-generated spending summaries and expense insights.
- Authentication and user-specific transactions.
- Additional reporting and budgeting features.

## Project status

Core transaction CRUD, validation, structured exception handling, Swagger documentation, React dashboard functionality, service/controller tests, repository integration tests, and JaCoCo coverage reporting have been implemented. AI-based insights remain a future enhancement.
