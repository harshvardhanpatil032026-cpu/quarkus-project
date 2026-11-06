# DEMO-92: Register a new user with a hashed password

As a new user, I want to register with my name, email and password so that I can later log in. Passwords must be stored securely and never returned by the API.

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- A new user can register by submitting a name, an email and a password.
- On successful registration the system persists the user so that the user can later log in.
- The password is stored only in a secure (hashed) form, never in plain text.
- No API response ever contains the password or its hash.

## Non-functional requirements

- Password storage must use a secure one-way hashing approach (security).
- Passwords must not appear in API responses; passwords should also not be written to logs (security).

## Acceptance criteria

- Given a valid name, email and password, when the user registers, then the API responds with a success status and the user is created.
- Given a successful registration, then the stored password value differs from the submitted plain-text password and is a hash.
- Given a successful registration, then the response body does not contain the password or the password hash.
- Given any API response that returns user data, then it does not contain the password or the password hash.

## Business rules

- Passwords must be stored securely and never returned by the API.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `tests/api/DEMO-92/test-1.txt`
- `tests/api/DEMO-92/test-2.txt`
- `tests/api/DEMO-92/test-3.txt`
- `tests/api/DEMO-92/test-4.txt`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Endpoints the tests call: `POST /users`, `GET /users`

## Planned subtasks

- [other] Update User entity to store password hash (DEMO-93)
  Modify the User entity to include a password_hash field instead of storing plain text passwords.
- [other] Implement password hashing in UserService (DEMO-94)
  Add functionality to hash passwords using a secure one-way algorithm (e.g., bcrypt) before persisting the user in UserService.
- [other] Update UserResource to exclude password from responses (DEMO-95)
  Modify the UserResource to ensure that API responses do not include passwords or their hashes.
- [other] Write registration tests (DEMO-96)
  Create REST Assured tests for user registration, including success scenarios and edge cases (e.g., duplicate email, missing fields). Ensure that the stored password is hashed and not included in responses.
- [other] Add validation to UserResource (DEMO-97)
  Implement request validation in UserResource to ensure that name, email, and password fields are present and correctly formatted.
- [other] Configure datasource and schema generation (DEMO-98)
  Set up the application.properties file with necessary configurations for datasource connection and schema generation or migration settings.
