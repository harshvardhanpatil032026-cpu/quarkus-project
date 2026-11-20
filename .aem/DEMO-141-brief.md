# DEMO-141: Flyway database migrations (P1)

Add `quarkus-flyway` and a migration workflow. Create `V1__baseline.sql` from the current schema and `V2__access_control.sql` from `db/DEMO-129-access-control.sql`. Keep `validate` in prod, stop using `update` outside tests, and add a migration test. Do this before any further schema-changing story.

> The approved brief for this story, committed when the branch was created. The pull request is reviewed against the acceptance criteria below, so change them in the tool rather than here — edits to this file are not read back.

## Functional requirements

- Add the quarkus-flyway extension to my-app/pom.xml and enable Flyway migrations from src/main/resources/db/migration, run automatically at application startup.
- Create V1__baseline.sql that reproduces the schema Hibernate currently generates from the entities: users (id, name, email, password_hash, uk_users_email) and its Hibernate id sequence, plus every other table the entities map.
- Create V2__access_control.sql from my-app/db/DEMO-129-access-control.sql, creating modules, permissions, roles, role_permissions, user_roles and firms with their existing constraints (uk_modules_code, uk_permissions_code, uk_roles_code, uk_user_roles_user_role, uk_firms_code, scope and status check constraints, foreign keys).
- Existing databases that already hold the schema (created by hibernate update or by manually applying db/DEMO-129-access-control.sql) are adopted without data loss: configure quarkus.flyway.baseline-on-migrate=true with baseline-version=1 so V1 is skipped on a non-empty database without a flyway_schema_history table, and V2 is written with if-not-exists so it is safe to run on them. Empty databases run V1 and V2 normally.
- Set quarkus.hibernate-orm.database.generation to none or validate in every profile except test, so Hibernate never alters the schema in dev or prod; the current default of update in application.properties is removed.
- In the test profile, the schema is built by running the Flyway migrations (replacing drop-and-create) on a clean test database, with Hibernate validate on, so every test exercises the migrated schema.
- Document the migration workflow (adding the next Vn__description.sql, never editing an applied migration, how to run migrations locally and in prod, baselining existing databases) in my-app/README.md.
- Add a migration test that runs all migrations against an empty PostgreSQL database and asserts the resulting schema is accepted by Hibernate validation of the current entities and that the expected tables, columns and unique constraints exist.
- Once migrations are in place, the manual db/DEMO-129-access-control.sql script and the comment in it saying prod runs without Flyway are removed or marked superseded by V2.

## Non-functional requirements

- Migrations are idempotent in outcome and ordered: V1 then V2, with versions strictly increasing and checksums stable once released.
- Application startup fails fast with a clear error if a migration fails or if the schema does not match the entities (validate).
- Prod continues to supply DB credentials through DB_URL, DB_USERNAME and DB_PASSWORD; no credentials are added to migration files or committed.
- Migration SQL stays PostgreSQL-specific and uses the same table and constraint names as the entity annotations so Hibernate validation passes.
- Running the full existing test suite must still pass against the test database (my-db-test).

## Acceptance criteria

- quarkus-flyway is declared in pom.xml and src/main/resources/db/migration contains V1__baseline.sql and V2__access_control.sql.
- Starting the application against an empty PostgreSQL database applies V1 and V2 in order and records both in flyway_schema_history.
- After migration, Hibernate validation of all entities succeeds with no schema errors.
- V2__access_control.sql creates the same tables, columns, foreign keys and unique and check constraints as db/DEMO-129-access-control.sql.
- Starting against an existing non-empty database with no flyway_schema_history leaves existing rows untouched, baselines it at version 1, applies V2 without failing on existing tables, and the application starts.
- quarkus.hibernate-orm.database.generation is not update in any profile; prod uses validate (or none) and dev does not auto-alter the schema.
- In the test profile the schema is created by Flyway migrations, not drop-and-create, and Hibernate validation is enabled.
- A migration test in src/test/java runs the migrations on an empty database and fails if an entity and the migrated schema disagree.
- Re-running migrations on an already migrated database is a no-op and the application still starts.
- The full existing test suite passes after the change.
- my-app/README.md describes how to add a new migration, how existing databases are baselined, and warns against editing applied ones.

## Business rules

- Flyway owns the schema; Hibernate only validates it (prod) or runs against it, and must never use update outside tests.
- Applied migrations are immutable: schema changes after this story are made only through new versioned migration files.
- Seed rows (modules, permissions, PLATFORM_ADMIN role and links) stay created by AccessControlSeeder at startup, not by V2, so the seeding behaviour of DEMO-129 is unchanged.
- This story must be merged before any further schema-changing story.

## Test-driven workflow

The acceptance criteria above were used to generate the tests below, committed alongside this brief. Build to make them pass — run them with the project's own test tools as you work, not only once at the end. Do not consider this story implemented until every one passes, or you have said here why one cannot be.

- `src/test/java/com/my/proj/resource/Criterion1_FlywayDependencyAndMigrationFilesExistTest.java`
- `src/test/java/com/my/proj/resource/Criterion2_EmptyDbAppliesV1AndV2InOrderTest.java`
- `src/test/java/com/my/proj/resource/Criterion3_HibernateValidatesMigratedSchemaTest.java`
- `src/test/java/com/my/proj/resource/Criterion4_V2MatchesAccessControlScriptTest.java`
- `src/test/java/com/my/proj/resource/Criterion5_ExistingDbIsBaselinedAndKeepsDataTest.java`
- `src/test/java/com/my/proj/resource/Criterion6_NoProfileUsesUpdateGenerationTest.java`
- `src/test/java/com/my/proj/resource/Criterion7_TestProfileUsesFlywayAndValidateTest.java`
- `src/test/java/com/my/proj/resource/Criterion8_MigrationTestFailsOnSchemaDriftTest.java`
- `src/test/java/com/my/proj/resource/Criterion9_RerunningMigrationsIsANoOpTest.java`
- `src/test/java/com/my/proj/resource/Criterion10_ExistingSuiteRunsOnMigratedSchemaTest.java`
- `src/test/java/com/my/proj/resource/Criterion11_ReadmeDocumentsMigrationsTest.java`

## Planned subtasks

- [backend] Write V1__baseline.sql (DEMO-212)
- [backend] Write V2__access_control.sql (DEMO-213)
- [backend] Make tests run on Flyway (DEMO-214)
- [backend] Add the migration tests (DEMO-215)
- [backend] Docs and cleanup (DEMO-216)
- [backend] Add Flyway and configure it (DEMO-211)
