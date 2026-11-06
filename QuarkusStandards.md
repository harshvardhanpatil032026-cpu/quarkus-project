# Quarkus Coding Standards and Best Practices

## 1. Project Structure

Use a layered package layout, grouped by feature or by layer:

```
com.company.app
├── resource/      (REST endpoints)
├── service/       (business logic)
├── repository/    (data access)
├── entity/        (JPA/Panache entities)
├── dto/           (request/response objects)
├── mapper/        (entity <-> DTO)
├── exception/     (custom exceptions + mappers)
└── config/        (config mappings, producers)
```

Feature-based packaging (`order/`, `customer/`) scales better in larger services.

## 2. Dependency Injection (CDI)

- **Use constructor injection** (a single constructor needs no `@Inject`). It is test-friendly and works with final fields.
- Avoid `@Inject` on private fields. Quarkus can't access private members without reflection overhead, so use package-private fields if you must use field injection.
- Prefer `@ApplicationScoped` for stateless services; `@Singleton` skips the client proxy but can't be intercepted or mocked as easily; use `@RequestScoped` only when truly needed.
- Don't use `new` for beans. Let CDI manage them.

```java
@ApplicationScoped
public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
```

## 3. REST Layer

- Use **Quarkus REST** (formerly RESTEasy Reactive) for new projects.
- Keep resources thin: validate, delegate to services, map responses. No business logic.
- Never expose entities directly; use DTOs (records work well).
- Use proper HTTP verbs and status codes (`201 Created` with `Location`, `204`, `404`, `409`).
- Version your APIs (`/api/v1/...`).
- Annotate with `@Produces`/`@Consumes` and document with OpenAPI (`quarkus-smallrye-openapi`).

```java
@Path("/api/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final OrderService service;

    public OrderResource(OrderService service) { this.service = service; }

    @POST
    public Response create(@Valid CreateOrderRequest request, @Context UriInfo uri) {
        OrderResponse created = service.create(request);
        return Response.created(uri.getAbsolutePathBuilder().path(created.id().toString()).build())
                       .entity(created).build();
    }
}
```

## 4. Validation

- Use Bean Validation (`quarkus-hibernate-validator`): `@Valid`, `@NotNull`, `@NotBlank`, `@Size`, etc.
- Validate at the boundary (DTOs), not deep inside services.
- Return consistent error payloads via `ExceptionMapper`s.

## 5. Exception Handling

- Define domain-specific exceptions.
- Centralize handling with `@ServerExceptionMapper` (Quarkus REST) or `ExceptionMapper<T>`.
- Return a uniform error structure (code, message, timestamp, trace/correlation id).
- Never leak stack traces or internal details to clients.

```java
@ServerExceptionMapper
public RestResponse<ErrorResponse> map(OrderNotFoundException e) {
    return RestResponse.status(Response.Status.NOT_FOUND,
            new ErrorResponse("ORDER_NOT_FOUND", e.getMessage()));
}
```

## 6. Configuration

- Use `application.properties` (or YAML with `quarkus-config-yaml`) with **profiles**: `%dev`, `%test`, `%prod`.
- Prefer type-safe **`@ConfigMapping`** interfaces over scattered `@ConfigProperty`.
- Never commit secrets. Use env vars, Kubernetes secrets, Vault, or `quarkus-credentials`.
- Provide sensible defaults for dev, and require explicit values in prod.

```java
@ConfigMapping(prefix = "app.payment")
public interface PaymentConfig {
    String baseUrl();

    @WithDefault("5s")
    Duration timeout();
}
```

## 7. Persistence (Hibernate ORM / Panache)

- Pick one Panache style (Active Record or Repository) and stay consistent. The Repository pattern is generally easier to test and keeps entities clean.
- Put `@Transactional` on **service** methods, not resources.
- Avoid N+1 queries: use `JOIN FETCH`, projections, or entity graphs.
- Use **Flyway** or **Liquibase** for schema migrations. Don't use `drop-and-create` outside dev/test.
- Use projections/DTO queries for read-heavy endpoints.
- Paginate list endpoints.

## 8. Reactive vs. Imperative

- Don't run blocking calls on the event loop. Use `@Blocking` or virtual threads (`@RunOnVirtualThread`, Java 21+) when calling blocking code from reactive endpoints.
- Choose one model per service where possible. With Mutiny, don't call `.await().indefinitely()` on the IO thread.
- Use Hibernate Reactive only if you're committed to the fully reactive stack.

## 9. Native Image / Performance

- Prefer build-time over runtime features: avoid reflection, dynamic proxies, and classpath scanning in your own code.
- Register unavoidable reflection with `@RegisterForReflection`.
- Use Quarkus extensions rather than raw libraries when one exists (they are build-time optimized).
- Keep startup logic light; use `@Startup` sparingly.
- Test native builds in CI, not just JVM mode.

## 10. Security

- Use `quarkus-oidc` / `quarkus-smallrye-jwt` for auth; avoid hand-rolled security.
- Apply `@RolesAllowed`, `@Authenticated`, or `quarkus.http.auth.permission.*` policies. Deny by default.
- Enable HTTPS and configure CORS deliberately (never `*` in prod).
- Validate and sanitize all input; never build SQL by string concatenation.

## 11. Resilience & Integration

- Use **SmallRye Fault Tolerance**: `@Timeout`, `@Retry`, `@CircuitBreaker`, `@Fallback`.
- Use the **Quarkus REST Client** (`@RegisterRestClient`) with configured timeouts instead of manual HTTP clients.
- Make retried operations idempotent-safe.

## 12. Observability

- **Health:** `quarkus-smallrye-health` (`/q/health/live`, `/q/health/ready`).
- **Metrics:** Micrometer + Prometheus.
- **Tracing:** OpenTelemetry.
- **Logging:** use `org.jboss.logging.Logger` or `@Log`; structured JSON logging (`quarkus-logging-json`) in prod. Use parameterized messages and never log secrets or PII.

```java
private static final Logger LOG = Logger.getLogger(OrderService.class);

LOG.infof("Order %s created for customer %s", orderId, customerId);
```

## 13. Testing

- Use `@QuarkusTest` for integration tests and plain JUnit 5 + Mockito for unit tests (faster).
- Use **Dev Services** (Testcontainers) for DBs, Kafka, etc. instead of hand-managed infrastructure.
- Use REST Assured for endpoint tests; `@InjectMock` for CDI beans.
- Use `@QuarkusIntegrationTest` for packaged-artifact/native verification.
- Aim for fast, deterministic tests; separate unit and integration suites.

## 14. General Java Style

- Follow standard Java conventions (or Google Java Style) and enforce with **Checkstyle/Spotless**.
- Use **records** for DTOs and immutable value objects.
- Prefer `Optional` for return types, not parameters or fields.
- Keep methods small, classes cohesive, and avoid static mutable state.
- Use SonarQube/SpotBugs in CI for static analysis.

## 15. Build, Deployment & Hygiene

- Keep Quarkus and extension versions aligned through the **BOM**.
- Use `quarkus-container-image-*` extensions for container builds; run as a non-root user.
- Use `mvn quarkus:dev` for live coding; don't rely on dev-mode behaviors in prod.
- Update Quarkus regularly (LTS releases are good for stability) and run `quarkus update` for migrations.
- Add a `.dockerignore`/`.gitignore` and keep `target/` out of version control.

## Quick Checklist

- [ ] Constructor injection, no private-field `@Inject`
- [ ] Thin resources, DTOs instead of entities
- [ ] `@Valid` input and centralized exception mapping
- [ ] `@ConfigMapping` and profile-based config, no secrets in code
- [ ] `@Transactional` at the service layer, migrations via Flyway/Liquibase
- [ ] No blocking on the event loop
- [ ] Health, metrics, and tracing enabled
- [ ] Dev Services-based tests, native build verified in CI
- [ ] Fault tolerance on remote calls