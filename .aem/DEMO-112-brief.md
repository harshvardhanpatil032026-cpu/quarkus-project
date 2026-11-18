# DEMO-112: Add a get user by email API, GET /users/email/{email}

Add an endpoint that fetches a user's details using their email address.

API: GET /users/email/{email}

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- Add an API endpoint GET /users/email/{email} to fetch user details by email.
- The endpoint should return user details in JSON format.

## Non-functional requirements

- The response time for the API should be within acceptable limits as defined by the system performance standards.
- The API should handle a high volume of requests without degradation in performance.

## Acceptance criteria

- When a valid email is provided, the API returns the user details associated with that email in JSON format.
- If no user is found for the given email, the API returns a 404 Not Found response with an appropriate error message.
- The API should be able to handle requests with special characters or case variations in the email address.

## Business rules

- User details can only be retrieved using their unique email address.
- Email addresses are case-insensitive for lookup purposes.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `tests/api/DEMO-112/test-1.txt`
- `tests/api/DEMO-112/test-2.txt`
- `tests/api/DEMO-112/test-3.txt`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Endpoints the tests call: `POST /users`, `GET /users/email/john.doe@example.com`, `GET /users/email/nonexistent@example.com`, `GET /users/email/JANE.DOE+TEST@example.com`

## Planned subtasks

- [other] Clarify behavior for multiple users with the same email (DEMO-117)
- [other] Add GET /users/email/{email} endpoint to UserResource (DEMO-113)
- [other] Implement rate limiting on GET /users/email/{email} endpoint (DEMO-118)
- [other] Update UserService to support fetching users by email (DEMO-114)
- [other] Handle special characters in email addresses (DEMO-115)
- [other] Add tests for GET /users/email/{email} endpoint (DEMO-116)


Clarifications from the product owner (authoritative; follow these):
- What is the expected behavior if multiple users have the same email address? (This seems unlikely given typical user management practices, but worth verifying.) → Unique email is present there

Open questions for the product owner (not answered yet). Do not invent an answer. Before writing code that depends on one, stop and ask the developer.
- Should there be any rate limiting or throttling on this endpoint to prevent abuse?