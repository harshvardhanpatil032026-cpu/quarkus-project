# DEMO-90: User Login with Password Verification

As a registered user, I want to log in using my email address and password so that I can authenticate my identity, receive an authentication token, and access protected API endpoints.

The login endpoint must validate the submitted credentials against the user's registered email address and stored bcrypt password hash.

Upon successful authentication, the API must generate and return a JWT (JSON Web Token) containing the authenticated user's identity and appropriate claims.

If authentication fails, the API must return a generic error response without revealing whether the email address or password was incorrect.

For security reasons:
- Passwords and password hashes must never be included in API responses or application logs.
- Users created before password hashing was introduced, who do not have a stored password hash, must be rejected gracefully.
- Invalid credentials must not expose sensitive user information.
- JWT tokens must have an appropriate expiration time.

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- The system must provide an endpoint to authenticate users using their email address and password.
- Upon successful authentication, the system must generate and return a JWT containing the user's identity and appropriate claims.
- If authentication fails, the system must return a generic error response without revealing whether the email or password was incorrect.

## Non-functional requirements

- Passwords and password hashes must never be included in API responses or application logs.
- JWT tokens must expire 3600 seconds (1 hour) after issue.

## Acceptance criteria

- The login endpoint should accept a POST request with JSON body containing 'email' and 'password'.
- Upon successful authentication, the response should include a JWT token in the response body.
- If authentication fails, the response should be a generic error message (e.g., 'Invalid credentials') without revealing whether the email or password was incorrect.
- The generated JWT token must have an expiration time of 3600 seconds (1 hour).

## Business rules

- Users created before password hashing was introduced must be rejected gracefully if they do not have a stored password hash.
- Invalid credentials must not expose sensitive user information.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `tests/api/DEMO-90/test-1.txt`
- `tests/api/DEMO-90/test-2.txt`
- `tests/api/DEMO-90/test-3.txt`
- `tests/api/DEMO-90/test-4.txt`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Endpoints the tests call: `POST /users`

## Planned subtasks

- [other] Implement User Authentication Endpoint (DEMO-105)
- [other] Write Unit Tests for Authentication Logic (DEMO-111)
- [other] Handle Users Without Password Hashes (DEMO-106)
- [other] Configure JWT Token Expiration (DEMO-107)
- [other] Map Unknown-Email Lookups to the Generic Login Failure (DEMO-108)
- [other] Implement Validation for Login Credentials (DEMO-109)
- [other] Keep Password Hash Out of Login Responses and Logs (DEMO-110)


Clarifications from the product owner (authoritative; follow these):
- What is the appropriate expiration time for JWT tokens? → 3600 seconds