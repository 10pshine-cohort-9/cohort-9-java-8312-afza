# Contact Manager

A full-stack contact-management application with account registration, session-based authentication, and owner-scoped contact records. The repository contains a Spring Boot API and a React single-page application.

## Features

- Register with either an email address or a phone number.
- Sign in, maintain a secure server session, sign out, and change a password.
- Create, view, search, filter, sort, update, and delete contacts.
- Store multiple labeled email addresses and phone numbers per contact.
- Keep every contact private to its authenticated owner.
- Paginate results and sort contacts by first name, title, or email address.
- Return consistent API validation and authentication errors.
- Protect state-changing requests with CSRF tokens and restrict browser access with CORS.
- Run backend tests with H2 and configure static analysis with SonarQube.

## Technology

| Area | Tools |
| --- | --- |
| Backend | Java 25, Spring Boot, Spring Security, Spring Data JPA, Liquibase |
| Database | Microsoft SQL Server in development/production; H2 for tests |
| Frontend | React 19, Vite, Axios, Tailwind CSS, Lucide icons |
| Quality | JUnit, Mockito, Spring MockMvc, SonarQube |

## Repository layout

```text
backend/                 Spring Boot application and tests
  src/main/resources/db/ Liquibase changelog and schema migration
  db/migrations/         Manual SQL Server rollout scripts for existing databases
frontend/                React/Vite single-page application
sonar-project.properties SonarQube project configuration
```

## Prerequisites

- JDK 25
- Node.js 20 or newer and npm
- SQL Server for the application runtime
- Maven is available through the backend Maven wrapper

## Run locally

### 1. Configure the database

Create `backend/.env` (this file is intentionally not committed) with your SQL Server connection values:

```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=contact_manager;encrypt=true;trustServerCertificate=true
DB_USERNAME=sa
DB_PASSWORD=replace-with-a-secret
CORS_ALLOWED_ORIGIN=http://localhost:5173
```

The backend imports this file automatically. Liquibase creates the user and contact ownership schema at startup. For an existing database with contacts that have no owner, read and run the documented rollout script in [`backend/db/migrations/README.md`](backend/db/migrations/README.md) before deployment.

### 2. Start the backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The API starts at `http://localhost:8080` and is exposed under `/api`.

### 3. Start the frontend

In a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open the Vite URL shown in the terminal, normally `http://localhost:5173`.

To point the frontend to another API host, set `VITE_API_BASE_URL`, for example:

```properties
VITE_API_BASE_URL=https://api.example.com/api
```

## API overview

Public endpoints:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/users/register` | Create an account; a newly created account is signed in automatically. |
| `POST` | `/api/auth/login` | Start an authenticated session. |

Authenticated endpoints:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/auth/session` | Get the current user and initialize a CSRF token. |
| `POST` | `/api/auth/logout` | End the current session. |
| `GET` | `/api/users/me` | Get the current user profile. |
| `PUT` | `/api/users/me/password` | Change the current user’s password. |
| `GET` | `/api/contacts` | List owner-scoped contacts. Supports `page`, `size`, `search`, `title`, and `sort`. |
| `GET` | `/api/contacts/titles` | List the current owner’s available contact titles. |
| `GET` | `/api/contacts/{id}` | Get one owned contact. |
| `POST` | `/api/contacts` | Create a contact for the current user. |
| `PUT` | `/api/contacts/{id}` | Update an owned contact. |
| `DELETE` | `/api/contacts/{id}` | Delete an owned contact. |

`sort` accepts `firstName`, `title`, or `email`. The API uses an HTTP session cookie and sends the CSRF value in the `XSRF-TOKEN` cookie; Axios is configured to return it as the `X-XSRF-TOKEN` header for protected requests.

## Quality commands

```powershell
# Backend tests
cd backend
.\mvnw.cmd test

# Frontend checks
cd frontend
npm run lint
npm run build
```

For SonarQube, compile and test the backend first so `backend/target/classes` and `backend/target/test-classes` exist, then run your organization’s configured SonarQube scanner from the repository root. The scanner configuration is in [`sonar-project.properties`](sonar-project.properties).

## Security notes

- Passwords are stored with BCrypt.
- Login changes the session ID to mitigate session fixation.
- Contacts are always queried and mutated through the authenticated owner ID.
- Existing-account registration responses avoid revealing whether an identifier is already registered.
- Do not commit `.env` files, database passwords, or deployment credentials.

## Troubleshooting

- **CORS errors:** confirm `CORS_ALLOWED_ORIGIN` exactly matches the frontend origin, including protocol and port.
- **401 responses:** sign in again; protected endpoints require an active session.
- **403 responses on writes:** first obtain the CSRF cookie through the app/session endpoint and send the `X-XSRF-TOKEN` header. The frontend does this automatically.
- **Database startup errors:** verify the SQL Server URL and credentials, and consult the manual migration instructions before enabling owner-scoped contacts on a legacy database.
