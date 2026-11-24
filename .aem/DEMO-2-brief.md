# DEMO-2: connection check endpoint

Add api given in pdf

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- Add an API endpoint to check database connection status.
- The endpoint should return the name of the connected database.

## Non-functional requirements

- The API response time should be less than 500ms under normal load conditions.
- The API should handle concurrent requests efficiently without performance degradation.

## Acceptance criteria

- When a GET request is made to the new endpoint, it returns a status code of 200 and a message indicating successful connection along with the database name.
- If the database connection fails, the API should return an appropriate error response with a status code of 500.

## Business rules

- The system must be able to verify the health of its database connection at any time via this endpoint.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `src/test/java/com/my/proj/resource/Demo2AcceptanceTest1.java`
- `src/test/java/com/my/proj/resource/Demo2AcceptanceTest2.java`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Endpoints the tests call: `GET /db`

## Planned subtasks

- [other] Implement database connection status endpoint (DEMO-4)
- [other] Update ApiExceptionMapper for better error handling (DEMO-5)
- [other] Add logging for connection status (DEMO-6)
- [other] Write tests for database connection status endpoint (DEMO-7)


Open questions for the product owner (not answered yet). Do not invent an answer. Before writing code that depends on one, stop and ask the developer.
- What should be the exact response format if the connection fails? The current implementation returns a generic 'Unexpected error'.
- Should there be any logging for successful connections, or only failed ones?