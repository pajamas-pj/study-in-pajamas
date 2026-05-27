# studyinpj — 被窝学堂

Spring Cloud Alibaba microservices (online education platform). Java 11, Spring Boot 2.7.2, Spring Cloud 2021.0.3.

## Modules & entrypoints

| Module | Type | Port | Service Name | Main class |
|--------|------|------|-------------|------------|
| `pj-gateway` | Spring Cloud Gateway | 10010 | `gateway-service` | `GateWayApplication` |
| `pj-auth` | Auth service | 10011 | `auth-service` | `AuthApplication` |
| `pj-user` | User service | 8081 | `user-service` | `UserApplication` |
| `pj-course` | Course service | 8082 | `course-service` | `CourseApplication` |
| `pj-learning` | Learning service | 8082* | `learning-service` | `LearnApplication` |
| `pj-remark` | Remark service | 8089 | `remark-service` | `RemarkApplication` |
| `pj-promotion` | Promotion service | 8085 | `promotion-service` | `PromotionApplication` |
| `pj-common` | Shared library (jar) | — | — | — |
| `pj-api` | Feign clients + DTOs (jar) | — | — | — |

\* `course-service` and `learning-service` both default to port 8082 — will conflict locally without override.

## Build

```powershell
mvn clean install -DskipTests        # full build
mvn clean install -pl pj-course -am  # single module + deps
```

## Key infrastructure (all `192.168.150.120`)

- **Nacos**: `192.168.150.120:8848` — registry + config center (profiles: `dev`/`local` both use same address)
- **MySQL**: `3306`, root/1234 — databases: `pj_user`, `pj_course`, `pj_learn`, `pj_remark`, `pj_promotion`
- **Redis**: `6379`, password `1234`
- **RabbitMQ**: `5672`, vhost `/hmall`, user `hmall`/`123`
- **Seata**: disabled by default on all services (`enabled: false`)
- **Xxl-Job**: admin `http://192.168.150.120:8880/xxl-job-admin`, token `tianji`

## Architecture quirks

- **Gateway routes** only `/users/**`→`user-service` and `/auth/**`→`auth-service`; other services accessed directly.
- **User context**: gateway sets `user-info` header → `UseInfoInterceptor` extracts into `UserContext` (ThreadLocal) → `DefaultFeignConfig` forwards header on Feign calls.
- **Feign clients** live in `pj-api` (`com.pajamas.api.client.*`). Enable with `@EnableFeignClients(basePackages = "com.pajamas.api.client")`.
- **MyBatis-Plus**: `assign_id` ID strategy, logic delete field `deleted`, enum type handler `MybatisEnumTypeHandler`. Pagination max 1000.
- **Global JSON**: all `Long`/`BigInteger` serialized as String (Jackson config in `pj-common`).
- **SearchClient** references `search-service` but no `pj-search` module exists in this repo.
- **Lombok** is a project-wide dependency — used everywhere.
- **Codebase comments are in Chinese**; error messages in Chinese.

## Testing

Only 2 tests exist, both in `pj-course`:
- `CategoryServiceImplTest` — Mockito `@ExtendWith(MockitoExtension.class)` service-layer test
- `CategoryControllerTest` — `@WebMvcTest(CategoryController.class)` with MockMvc

No integration tests, no testcontainers, no other module has tests. Run tests for a single module:

```powershell
mvn test -pl pj-course
```
