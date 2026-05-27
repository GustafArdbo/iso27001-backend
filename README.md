# ISO 27001 Backend

Spring Boot backend for an ISO 27001 readiness and audit platform.

The current MVP covers a vertical slice:

- Create organizations
- Create organization users
- Create assessments
- List ISO 27001-style control questions
- Submit assessment answers
- Calculate assessment progress and gap score
- Persist data in Supabase Postgres
- Manage schema changes with Flyway

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Supabase Postgres
- Supabase Auth JWT
- Flyway
- Maven

## Local Setup

Set the database environment variables in the same terminal where you start the app:

```powershell
$env:DB_URL="jdbc:postgresql://aws-0-eu-west-1.pooler.supabase.com:5432/postgres?sslmode=require"
$env:DB_USERNAME="postgres.<project-ref>"
$env:DB_PASSWORD="<database-password>"
$env:SUPABASE_JWT_ISSUER="https://<project-ref>.supabase.co/auth/v1"
$env:SUPABASE_JWT_JWK_SET_URI="https://<project-ref>.supabase.co/auth/v1/.well-known/jwks.json"
```

Then start the backend:

```powershell
mvn spring-boot:run
```

When the app starts, Flyway applies migrations from:

```text
src/main/resources/db/migration
```

The first migration creates:

- `organizations`
- `app_users`
- `assessments`
- `assessment_answers`
- `flyway_schema_history`

Later migrations add:

- app user role constraints
- `app_users.supabase_user_id`
- `auth_revocations`

## Authentication

All business endpoints require a Supabase Auth access token:

```text
Authorization: Bearer <supabase-access-token>
```

Only these endpoints are public:

```text
GET /health
GET /actuator/health
```

Recommended Supabase setup:

- Use asymmetric JWT signing keys.
- Verify tokens through the Supabase JWKS endpoint.
- Keep access tokens short-lived.
- Use Supabase sign-out/session controls for refresh-token/session termination.
- Use this backend's revocation table to block already-issued access tokens or sessions from this API.

Auth endpoints:

```text
GET  /auth/me
POST /auth/revocations/current-token
POST /auth/revocations/current-session
```

`/auth/me` returns the Supabase JWT identity plus any `app_users` memberships linked through `supabase_user_id`.

`/auth/revocations/current-token` revokes only the presented access token until its `exp`.

`/auth/revocations/current-session` revokes the presented token's `session_id` for this backend. Pair this with Supabase sign-out so refresh tokens are also terminated.

## Tests

Run:

```powershell
mvn test
```

Tests use an in-memory H2 database configured in:

```text
src/test/resources/application.yaml
```

## API Smoke Test

Health check:

```powershell
Invoke-RestMethod http://localhost:8080/health
```

For the remaining requests, include a Supabase access token:

```powershell
$headers = @{ Authorization = "Bearer <supabase-access-token>" }
```

Create organization:

```powershell
$org = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/organizations" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body '{"name":"Demo AB"}'
```

Create assessment:

```powershell
$assessment = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/assessments" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body (@{ organizationId = $org.id; name = "Initial ISO 27001 readiness assessment" } | ConvertTo-Json)
```

Create organization user:

```powershell
$user = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/organizations/$($org.id)/users" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body '{"email":"auditor@example.com","role":"AUDITOR","supabaseUserId":"<supabase-auth-user-id>"}'
```

List organization users:

```powershell
Invoke-RestMethod "http://localhost:8080/organizations/$($org.id)/users" -Headers $headers
```

List controls:

```powershell
Invoke-RestMethod "http://localhost:8080/controls" -Headers $headers
```

List assessment questions:

```powershell
Invoke-RestMethod "http://localhost:8080/assessments/$($assessment.id)/questions" -Headers $headers
```

Submit an answer:

```powershell
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/assessments/$($assessment.id)/answers" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body '{"controlId":"A.5.1","answer":"YES","comment":"Policy exists"}'
```

Get summary:

```powershell
Invoke-RestMethod "http://localhost:8080/assessments/$($assessment.id)/summary" -Headers $headers
```

## Current Package Structure

```text
se.iso27001platform.iso27001backend
  auth
    controller
    dto
    enums
    model
    repository
    security
    service
  assessment
    controller
    dto
    enums
    model
    repository
    service
  common
    controller
    exception
    model
  config
  control
    controller
    dto
    enums
    model
    service
  organization
    controller
    dto
    model
    repository
    service
  user
    controller
    dto
    enums
    model
    repository
    service
```
