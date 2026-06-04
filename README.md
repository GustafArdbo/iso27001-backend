# ISO 27001 Backend

Spring Boot backend for an ISO 27001 readiness and audit platform.

The current MVP covers a vertical slice:

- Create organizations
- Bootstrap the organization creator as `OWNER`
- Create organization memberships
- Invite users to an organization
- Receive public organization applications
- Manually approve or reject applications
- Create the approved organization and invite its owner through Supabase Auth
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
$env:SUPABASE_URL="https://<project-ref>.supabase.co"
$env:SUPABASE_SECRET_KEY="<supabase-secret-or-service-role-key>"
$env:FRONTEND_INVITE_REDIRECT_URL="http://localhost:3000/auth/callback"
$env:PLATFORM_ADMIN_USER_IDS="<supabase-auth-user-uuid>"
$env:CORS_ALLOWED_ORIGIN_PATTERNS="http://localhost:3000,https://complypilot.se,https://www.complypilot.se"
```

Find the platform administrator UUID in Supabase under **Authentication > Users**. Multiple UUIDs can be configured as a comma-separated list.

Then start the backend:

```powershell
mvn spring-boot:run
```

When the app starts, Flyway applies migrations from:

```text
src/main/resources/db/migration
```

`SUPABASE_SECRET_KEY` is a server-only credential. Never expose it to the frontend or commit it to Git.

The migrations create:

- `organizations`
- `user_profiles`
- `organization_memberships`
- `assessments`
- `assessment_answers`
- `auth_revocations`
- `organization_invitations`
- `organization_applications`
- `organization_application_materials`
- `flyway_schema_history`

`V3__organization_application_onboarding.sql` replaces the old demo-request tables. It intentionally deletes rows from those legacy tables because the project currently only contains test data.

For Railway, configure the same environment variables and use the production callback URL:

```text
FRONTEND_INVITE_REDIRECT_URL=https://complypilot.se/auth/callback
```

Add that exact callback URL to the allowed redirect URLs in Supabase Auth as well.

## Authentication

All business endpoints require a Supabase Auth access token:

```text
Authorization: Bearer <supabase-access-token>
```

Only these endpoints are public:

```text
GET /health
GET /actuator/health
POST /organization-applications
```

`POST /organization-applications` stores the public form submission. It never creates an organization automatically.

Recommended Supabase setup:

- Use asymmetric JWT signing keys.
- Verify tokens through the Supabase JWKS endpoint.
- Keep access tokens short-lived.
- Disable open public sign-up if all customer access should start from an approved application or organization invitation.
- Use Supabase sign-out/session controls for refresh-token/session termination.
- Use this backend's revocation table to block already-issued access tokens or sessions from this API.

Auth endpoints:

```text
GET  /auth/me
POST /auth/revocations/current-token
POST /auth/revocations/current-session
```

`/auth/me` creates or links the global `user_profiles` record, completes an approved owner invitation, and returns the JWT identity, `platformAdmin` flag, profile, and organization memberships.

`/auth/revocations/current-token` revokes only the presented access token until its `exp`.

`/auth/revocations/current-session` revokes the presented token's `session_id` for this backend. Pair this with Supabase sign-out so refresh tokens are also terminated.

## Organization Onboarding

The onboarding flow is:

1. A visitor submits `POST /organization-applications`.
2. A platform administrator lists and reviews submitted applications.
3. The administrator approves or rejects the application.
4. Approval creates the organization, owner profile, and `OWNER` membership exactly once.
5. The backend asks Supabase Auth to invite the owner or send an existing user a magic link.
6. The frontend handles `/auth/callback`, establishes the Supabase session, and calls `GET /auth/me`.
7. `/auth/me` links the Supabase user to the prepared owner profile and returns the organization membership used for dashboard routing.

Application statuses:

```text
SUBMITTED -> APPROVED
SUBMITTED -> REJECTED
```

Owner invitation statuses:

```text
NOT_SENT -> SENT -> ACCEPTED
NOT_SENT -> FAILED -> SENT
```

Platform administrator endpoints require a verified Supabase JWT whose immutable subject UUID is listed in `PLATFORM_ADMIN_USER_IDS`:

```text
GET  /admin/organization-applications
GET  /admin/organization-applications/{id}
POST /admin/organization-applications/{id}/approve
POST /admin/organization-applications/{id}/reject
POST /admin/organization-applications/{id}/resend-owner-invitation
```

Approval is idempotent. Repeating it does not create another organization or membership. If Supabase delivery fails, the application remains approved with invitation status `FAILED`, and an administrator can resend it.

## Authorization

Authorization is organization-scoped through `organization_memberships`.

The backend resolves:

```text
Supabase JWT subject
-> user_profiles.supabase_user_id
-> organization_memberships.user_profile_id
-> role
```

Role rules:

- `OWNER`: full organization access.
- `ADMIN`: manage memberships and assessments.
- `AUDITOR`: create assessments and submit answers.
- `MEMBER`: submit answers and read organization assessment data.
- `VIEWER`: read-only organization access.

Current endpoint rules:

- `POST /organizations` is a platform-admin-only operational endpoint that creates the organization and bootstraps the administrator as `OWNER`. Normal customer organizations are created by approving an application.
- `GET /organizations/{id}` requires membership in that organization.
- `POST /organizations/{id}/memberships` requires `OWNER` or `ADMIN`.
- `GET /organizations/{id}/memberships` requires `OWNER` or `ADMIN`.
- `GET /memberships/{id}` allows the member themself, or `OWNER`/`ADMIN` in that organization.
- `POST /organizations/{id}/invitations` requires `OWNER` or `ADMIN`.
- `GET /organizations/{id}/invitations` requires `OWNER` or `ADMIN`.
- `DELETE /organizations/{id}/invitations/{invitationId}` requires `OWNER` or `ADMIN`.
- `POST /invitations/accept` requires a JWT whose email matches the invitation email.
- `POST /assessments` requires `OWNER`, `ADMIN`, or `AUDITOR` in the target organization.
- `GET /organizations/{id}/assessments` requires membership in that organization.
- `GET /assessments/{id}`, `/questions`, and `/summary` require membership in the assessment organization.
- `POST /assessments/{id}/answers` requires `OWNER`, `ADMIN`, `AUDITOR`, or `MEMBER`.
- `VIEWER` can read but cannot create assessments or submit answers.

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

Submit a public organization application:

```powershell
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/organization-applications" `
  -ContentType "application/json" `
  -Body '{"company":"Demo AB","name":"Jane Doe","email":"jane@example.com","country":"Sweden (+46)","phone":"555 123 4567","size":"11-50","message":"We need help defining scope.","materials":["standard-forms","gap-analysis"]}'
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

The authenticated platform administrator is automatically created as an `OWNER` membership for this operationally created organization.

Create assessment:

```powershell
$assessment = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/assessments" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body (@{ organizationId = $org.id; name = "Initial ISO 27001 readiness assessment" } | ConvertTo-Json)
```

List organization assessments:

```powershell
Invoke-RestMethod "http://localhost:8080/organizations/$($org.id)/assessments" -Headers $headers
```

Create organization membership:

```powershell
$membership = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/organizations/$($org.id)/memberships" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body '{"email":"auditor@example.com","role":"AUDITOR","supabaseUserId":"<supabase-auth-user-id>"}'
```

List organization memberships:

```powershell
Invoke-RestMethod "http://localhost:8080/organizations/$($org.id)/memberships" -Headers $headers
```

Invite organization member:

```powershell
$invite = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/organizations/$($org.id)/invitations" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body '{"email":"auditor@example.com","role":"AUDITOR"}'
```

The response includes `acceptanceToken` once. Store or send it through your invitation channel; the backend only stores a hash.

Accept invitation while authenticated as the invited Supabase user:

```powershell
$invitedHeaders = @{ Authorization = "Bearer <invited-user-supabase-access-token>" }

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/invitations/accept" `
  -Headers $invitedHeaders `
  -ContentType "application/json" `
  -Body (@{ token = $invite.acceptanceToken } | ConvertTo-Json)
```

Revoke pending invitation:

```powershell
Invoke-RestMethod -Method Delete `
  -Uri "http://localhost:8080/organizations/$($org.id)/invitations/$($invite.invitation.id)" `
  -Headers $headers
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
  onboarding
    client
    controller
    dto
    enums
    model
    repository
    service
  invitation
    controller
    dto
    enums
    model
    repository
    service
  membership
    controller
    dto
    enums
    model
    repository
    service
  organization
    controller
    dto
    model
    repository
    service
  user
    dto
    model
    repository
    service
```
