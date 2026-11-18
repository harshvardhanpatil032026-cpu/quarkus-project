# DEMO-99: Reset Password API – Allow a registered user to reset their password

As a registered user
I want to reset my password by confirming my current password
So that I can keep my account secure and change a password that may have been exposed

Background
Users are registered through POST /users, and their passwords are stored as salted bcrypt hashes . There is no way to change a password after registration yet. This story adds a reset endpoint that follows the same validation, error-handling and security approach as registration.

API Contract
POST /users/reset-password

Request:
{
  "email": "user@example.com",
  "currentPassword": "OldP@ss1",
  "newPassword": "NewP@ss2"
}
Response:
- 204 No Content when the reset succeeds.

Acceptance Criteria
1. A valid request with the correct current password replaces the stored password_hash with a new bcrypt hash of newPassword and returns 204.
2. Emails are matched after trimming and lowercasing, the same way as in registration.
3. A missing or blank email, currentPassword or newPassword, or a badly formatted email, returns 400 with an ErrorResponse listing the validation messages.
4. If newPassword is the same as currentPassword, the API returns 400.
5. A wrong current password, an unknown email, or a user with no stored password hash all return 401 with the same generic message, "Invalid email or password". This stops anyone from finding out which emails are registered.
6. Passwords are never stored in plain text, logged, or The request DTO's toString() masks both password fields.
7. After a reset, the old password stops working and the new one works.
8. The update runs in a single transaction.

Technical Notes
- Add a ResetPasswordRequest record in dto/ with @NotBlank and @Email checks and a masked toString().
- Add a resetPassword(email, currentPassword, newPassworIt should check the current password against the storedbcrypt hash, for example with Elytron's BcryptUtil.matches or by parsing the hash with ModularCrypt.
- Add a new InvalidCredentialsException and map it to 40
- Add the endpoint to UserResource. It will be documented automatically through the existing SmallRye OpenAPI setup.

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- Allow registered users to reset their password by confirming their current password.
- Replace the stored password_hash with a new bcrypt hash of newPassword upon successful validation.
- Return a 204 No Content response when the password reset is successful.

## Non-functional requirements

- Emails should be matched after trimming and lowercasing, similar to registration.
- Passwords must never be stored in plain text or logged; request DTO's toString() method should mask both password fields.
- The update operation should run within a single transaction.

## Acceptance criteria

- A valid request with the correct current password replaces the stored password_hash with a new bcrypt hash of newPassword and returns 204.
- Emails are matched after trimming and lowercasing, the same way as in registration.
- A missing or blank email, currentPassword, or newPassword, or a badly formatted email, returns 400 with an ErrorResponse listing the validation messages.
- If newPassword is the same as currentPassword, the API returns 400.
- A wrong current password, an unknown email, or a user with no stored password hash all return 401 with the message 'Invalid email or password'.
- After a reset, the old password stops working and the new one works.

## Business rules

- Users must provide their current password to reset it.
- The new password cannot be the same as the current password.
- Emails are case-insensitive and whitespace is trimmed for matching purposes.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `tests/api/DEMO-99/test-1.txt`
- `tests/api/DEMO-99/test-2.txt`
- `tests/api/DEMO-99/test-3.txt`
- `tests/api/DEMO-99/test-4.txt`
- `tests/api/DEMO-99/test-5.txt`
- `tests/api/DEMO-99/test-6.txt`
- `tests/api/DEMO-99/test-7.txt`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Endpoints the tests call: `POST /users`, `POST /users/reset-password`

## Planned subtasks

- [backend] Add ResetPasswordRequest DTO with validation and masked toString() (DEMO-100)
- [backend] Add InvalidCredentialsException and map it to 401 response (DEMO-102)
- [backend] Implement resetPassword method in UserService (DEMO-101)
- [backend] Write tests for password reset functionality (DEMO-104)
- [backend] Add reset-password endpoint in UserResource (DEMO-103)


Clarifications from the product owner (authoritative; follow these):
- Are there specific error messages required beyond 'Invalid email or password' for security reasons? → no
- Should there be any additional complexity requirements for the new password (e.g., minimum length, special characters)? → Minimum length should be 10