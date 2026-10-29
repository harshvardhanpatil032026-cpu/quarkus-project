# DEMO-74: Search and paging for the users list

As an API consumer I want to search the users by name and page through the results, so that I can find a user without downloading the whole list.

GET /users accepts optional query parameters: name (a case-insensitive partial match), page (starting at 0) and size (default 20, at most 100). With no parameters it behaves as it does today.

Acceptance criteria:
1. GET /users?name=ash returns only users whose name contains "ash", ignoring case.
2. GET /users?page=0&size=2 returns at most 2 users, and page=1 returns the next 2.
3. GET /users without parameters still returns the users, now limited to the default page size of 20.
4. A size above 100, or a negative page, returns 400.
5. The response includes the total number of users that match, so a client knows how many pages there are.

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- The API should support searching users by name with case-insensitive partial matching.
- The API should allow paging through the list of users using 'page' and 'size' parameters.
- When no query parameters are provided, the API should return a paginated list of all users with a default size of 20.

## Non-functional requirements

- The search operation should be efficient even for large datasets.
- The API response time should not exceed acceptable thresholds under normal load conditions.

## Acceptance criteria

- GET /users?name=ash returns only users whose name contains 'ash', ignoring case.
- GET /users?page=0&size=2 returns at most 2 users, and page=1 returns the next 2.
- GET /users without parameters still returns the users, now limited to the default page size of 20.
- A request with a 'size' above 100 or a negative 'page' should return a 400 Bad Request status code.
- The response includes the total number of users that match the search criteria.

## Business rules

- Search functionality is case-insensitive and supports partial matches on user names.
- Pagination parameters are used to limit the number of results returned in each request.
- Default page size is set to 20, with a maximum allowed size of 100 per request.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `tests/api/DEMO-74/test-1.txt`
- `tests/api/DEMO-74/test-2.txt`
- `tests/api/DEMO-74/test-3.txt`
- `tests/api/DEMO-74/test-4.txt`
- `tests/api/DEMO-74/test-5.txt`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Endpoints the tests call: `GET /users`

## Planned subtasks

- [backend] Update UserService to support case-insensitive name search and pagination (DEMO-75)
- [backend] Update UserResource to handle search and pagination parameters (DEMO-76)
- [backend] Add tests for new search and pagination functionality (DEMO-79)
- [backend] Update UserRepository to support case-insensitive name search and pagination (DEMO-78)
- [backend] Implement error handling for invalid pagination parameters (DEMO-77)
