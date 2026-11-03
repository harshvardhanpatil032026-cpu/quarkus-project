# DEMO-86: Register a new user with a hashed password

As a new user, I want to register with my name, email and password so that I can later log in. Passwords must be stored securely and never returned by the API.

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- A new user can register by providing a name, an email and a password.
- On successful registration the system creates a user account that can later be used to log in.
- The password is stored only in hashed form; the plain-text password is never persisted.
- No API response ever contains the password or its hash.

## Non-functional requirements

- Passwords are hashed with a recognised, salted, adaptive password-hashing algorithm.
- Registration must not log plain-text passwords.

## Acceptance criteria

- Given valid name, email and password, when the user submits registration, then the API responds with a success status and the created user's identifier, name and email.
- Given a successful registration, when the stored user record is inspected, then the stored password value differs from the submitted password and is a hash.
- Given a successful registration, then the response body contains no password or password-hash field.
- Given any API endpoint that returns user data, then the response contains no password or password-hash field.
- Given a registration request with a missing name, email or password, then the API rejects it with a client-error status and no user is created.
- Given a registration request with an email that is not a valid email format, then the API rejects it with a client-error status and no user is created.

## Business rules

- Registration requires name, email and password.
- Passwords must be stored securely and never returned by the API.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `tests/api/DEMO-86/test-1.txt`
- `tests/api/DEMO-86/test-2.txt`
- `tests/api/DEMO-86/test-3.txt`
- `tests/api/DEMO-86/test-4.txt`
- `tests/api/DEMO-86/test-5.txt`
- `tests/api/DEMO-86/test-6.txt`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Endpoints the tests call: `POST /users`, `GET /users`

## Planned subtasks

- [service] Hash passwords and persist them on registration (DEMO-87)
- [api] Registration endpoint with request/response DTOs and validation (DEMO-88)
- [tests] Tests for registration and password hashing (DEMO-89)
