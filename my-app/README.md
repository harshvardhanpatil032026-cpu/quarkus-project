# my-app

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/my-app-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Database migrations (Flyway)

Flyway owns the database schema. Hibernate only validates it (`quarkus.hibernate-orm.database.generation=validate`
in every profile except test, where the schema is also built by the migrations first), so it never alters tables.

Migrations live in `src/main/resources/db/migration` and run automatically when the application starts:

- `V1__baseline.sql`: the schema as it was before access control (the `users` table and its `users_seq` sequence).
- `V2__access_control.sql`: modules, permissions, roles, role_permissions, user_roles and firms.

### Adding a migration

1. Create the next numbered file in `src/main/resources/db/migration`, named `V<next number>__<short_description>.sql`
   (for example `V3__add_firm_logo.sql`). Version numbers only go up.
2. Change the entity and the SQL in the same commit, so Hibernate validation still passes.
3. Run `mvn test`: the migration tests apply every migration to an empty database and fail if an entity and the
   migrated schema disagree.

Never edit, rename or delete a migration that has been applied anywhere. Flyway stores a checksum of each applied file
and refuses to start if it changed. Fix a mistake with a new, higher-numbered migration instead.

### Existing databases (baselining)

A database that already holds the schema, because Hibernate `update` created it before Flyway or because
`db/DEMO-129-access-control.sql` was applied by hand, has no `flyway_schema_history` table. With
`quarkus.flyway.baseline-on-migrate=true` and `quarkus.flyway.baseline-version=1`, Flyway baselines such an existing
database at version 1 on first start: `V1` is skipped, the rows are left untouched, and `V2` onwards runs (`V2` uses
`create table if not exists`, so it is safe over tables that already exist). An empty database simply runs `V1` then `V2`.

Production must keep supplying `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`. Take a backup before the first start that
applies a new migration.

## Related Guides

- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
