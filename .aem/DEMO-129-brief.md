# DEMO-129: Platform admin role and firm management

Add the first piece of access control: database tables for modules, permissions and roles, a PLATFORM_ADMIN role, and APIs that let a platform admin manage firms. Every protected endpoint requires a valid token and the right permission. Until Story 2, the existing user-management endpoints are restricted to platform admins, which closes the current gap where anyone can list, change or delete users.

Data model
- modules (code, name, description, active). Seeded with: FIRM, USER, ROLE.
- permissions (module, action, code module:action, description). Seeded with: firm:create/read/update/delete, user:create/read/update/delete, role:read, role:assign.
- roles (code, name, scope PLATFORM or FIRM, system flag). A system role can't be edited or deleted by the future role-management endpoints.
- role_permissions (role, permission).
- user_roles (user, role, assigned_by, assigned_at).
- firms (name, unique code, contact email, phone, address, status ACTIVE/INACTIVE, created_at, updated_at).
- Because Flyway is skipped, seeding is done by an idempotent startup step: it creates missing records and never duplicates them.

API

┌────────────────────────────────────────────────────────────────┬────────────────────┐
│                            Endpoint                            │     Permission     │
├────────────────────────────────────────────────────────────────┼────────────────────┤
│ POST /firms                                                    │ firm:create        │
├────────────────────────────────────────────────────────────────┼────────────────────┤
│ GET /firms, GET /firms/{id}                                    │ firm:read          │
├────────────────────────────────────────────────────────────────┼────────────────────┤
│ PUT /firms/{id}                                                │ firm:update        │
├────────────────────────────────────────────────────────────────┼────────────────────┤
│ DELETE /firms/{id}: sets the firm inactive, never hard-deletes │ firm:delete        │
├────────────────────────────────────────────────────────────────┼────────────────────┤
│ GET /me: current user, roles, permissions                      │ Any logged-in user │
├────────────────────────────────────────────────────────────────┼────────────────────┤
│ GET /roles: roles and their permissions                        │ role:read          │
└────────────────────────────────────────────────────────────────┴────────────────────┘

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- Create database tables for modules, permissions, roles, role_permissions, user_roles, and firms.
- Seed the modules table with FIRM, USER, ROLE.
- Seed the permissions table with firm:create/read/update/delete, user:create/read/update/delete, role:read, role:assign.
- Seed a PLATFORM_ADMIN role with scope PLATFORM and the system flag set, granted all ten seeded permissions through role_permissions.
- Implement APIs for platform admins to manage firms including POST /firms, GET /firms, GET /firms/{id}, PUT /firms/{id}, DELETE /firms/{id}.
- Ensure all protected endpoints require a valid token and the appropriate permission.
- Restrict existing user-management endpoints to platform admins only.
- Seeding runs as an idempotent startup step (Flyway is not used): it creates missing modules, permissions, roles and role-permission links and never duplicates them.
- Firms record created_at on creation and updated_at on every change.
- GET /me returns the current user, their roles and their permissions to any logged-in user.
- GET /roles returns every role with its permissions and requires role:read.
- Each existing /users endpoint requires a permission: GET /users, GET /users/{id} and GET /users/email/{email} require user:read; POST /users requires user:create; PATCH /users/email/{email} requires user:update; DELETE /users/email/{email} requires user:delete. POST /users/login stays public.

## Non-functional requirements

- Running the seeding step any number of times, including from two instances starting at once, leaves exactly one row per seeded module, permission, role and role-permission link, backed by database unique constraints.
- Access is denied by default and enforced server-side on every request using the framework's security mechanisms (quarkus-smallrye-jwt); the permissions returned by GET /me are informational only.

## Acceptance criteria

- After application start the modules table holds exactly FIRM, USER and ROLE, and restarting the application adds no duplicate rows.
- After application start the permissions table holds exactly firm:create, firm:read, firm:update, firm:delete, user:create, user:read, user:update, user:delete, role:read and role:assign, and restarting adds no duplicate rows.
- The PLATFORM_ADMIN role exists with scope PLATFORM and the system flag true, and is linked to all ten seeded permissions.
- Calling any protected endpoint with no token, an invalid token or an expired token returns 401.
- Calling a protected endpoint with a valid token that lacks the endpoint's permission returns 403.
- POST /firms by a user with firm:create returns 201 with a Location header and the firm with status ACTIVE and created_at set.
- POST /firms with a code that an existing firm already has returns 409.
- GET /firms/{id} for an id that does not exist returns 404.
- PUT /firms/{id} by a user with firm:update stores the new values and updates updated_at.
- DELETE /firms/{id} by a user with firm:delete returns 204, and the firm is still returned by GET /firms/{id} with status INACTIVE.
- GET /me with a valid token returns the user's id, name, email, roles and permissions; without a token it returns 401.
- GET /roles by a user with role:read returns each role with its permission codes.
- The /users endpoints (list, get by id, get by email, create, update, delete) return 403 for a valid token without the matching user:* permission and behave as before for a platform admin.
- POST /users/login still works without a token.

## Business rules

- A system role cannot be edited or deleted by future role management endpoints.
- Firm deletion sets the firm inactive instead of hard-deleting it.
- Access is granted by permission, not by role name; until Story 2 only the PLATFORM_ADMIN role holds the user:* permissions, so only platform admins can use the user-management endpoints.
- A firm's code is unique; creating or updating a firm with a code another firm already has is refused with 409.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `src/test/java/com/my/proj/resource/Criterion1_ModulesSeededOnceTest.java`
- `src/test/java/com/my/proj/resource/Criterion2_PermissionsSeededOnceTest.java`
- `src/test/java/com/my/proj/resource/Criterion3_PlatformAdminRoleSeededTest.java`
- `src/test/java/com/my/proj/resource/Criterion4_MissingOrBadTokenGives401Test.java`
- `src/test/java/com/my/proj/resource/Criterion5_MissingPermissionGives403Test.java`
- `src/test/java/com/my/proj/resource/Criterion6_CreateFirmReturns201Test.java`
- `src/test/java/com/my/proj/resource/Criterion7_DuplicateFirmCodeGives409Test.java`
- `src/test/java/com/my/proj/resource/Criterion8_UnknownFirmGives404Test.java`
- `src/test/java/com/my/proj/resource/Criterion9_UpdateFirmStoresValuesTest.java`
- `src/test/java/com/my/proj/resource/Criterion10_DeleteFirmSetsInactiveTest.java`
- `src/test/java/com/my/proj/resource/Criterion11_MeReturnsUserRolesAndPermissionsTest.java`
- `src/test/java/com/my/proj/resource/Criterion12_RolesListedWithPermissionsTest.java`
- `src/test/java/com/my/proj/resource/Criterion13_UserEndpointsNeedUserPermissionsTest.java`
- `src/test/java/com/my/proj/resource/Criterion14_LoginStaysPublicTest.java`

## Test contract

What the tests above expect the code to provide. Each item must exist when the story is done; the pull request review checks them.

- Endpoints the tests call: `POST /firms`, `GET /firms`, `GET /roles`, `GET /users`, `GET /firms/{id}`, `PUT /firms/{id}`, `GET /firms/not-a-number`, `DELETE /firms/not-a-number`, `DELETE /firms/{id}`, `GET /me`, `GET /users/{id}`, `GET /users/email/{email}`, `POST /users`, `PATCH /users/email/{email}`, `DELETE /users/email/{email}`, `POST /users/login`

## Planned subtasks

- [backend] 1. Access-control entities and repositories (DEMO-131)
- [backend] 7. Restrict the existing /users endpoints (DEMO-137)
- [backend] 2. Idempotent startup seeding (DEMO-132)
- [backend] 8. Update the existing test suite for authentication (DEMO-138)
- [backend] 3. Bootstrap the first platform admin (DEMO-133)
- [backend] 9. Production schema for the new tables (DEMO-139)
- [backend] 4. Permission-based authorization (DEMO-134)
- [backend] 10. Acceptance tests for the new behaviour (DEMO-140)
- [backend] 5. Firm entity and firm management API (DEMO-135)
- [backend] 6. GET /me and GET /roles (DEMO-136)


Open questions for the product owner (not answered yet). Do not invent an answer. Before writing code that depends on one, stop and ask the developer.
- How is the first PLATFORM_ADMIN assigned (for example, a configured email that receives the role at startup)?
- Should POST /users (sign-up), POST /users/reset-password and PUT /users/email/{email}/password also be restricted to platform admins, given the last two already require the user's current password and restricting them would stop users changing their own password?
- Which firm fields are required on create: only name and code, or also contact email, phone and address?
- Should GET /firms include inactive firms, and may PUT /firms/{id} set an inactive firm back to ACTIVE?