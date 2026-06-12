# FormFlow — Backend

> Para contexto completo del proyecto ver: `E:\emprendimiento\KodeLabs\formflow\CLAUDE.md`

## Stack
- Java 17 (migrar a Java 21 LTS cuando sea posible)
- Spring Boot 3.3.4 + Maven
- PostgreSQL + Flyway (migraciones)
- Spring Security + JWT (jjwt 0.12.5)
- Thymeleaf — plantillas HTML para emails
- springdoc-openapi 2.5.0 — Swagger UI en /swagger-ui.html
- Apache POI — exportación Excel
- Spring Actuator + Micrometer — observabilidad
- Bucket4j — rate limiting en memoria (sin Redis)
- stripe-java 25.x — billing y pagos

## Arquitectura
Monolito Modular con Arquitectura Hexagonal por módulo.

```
formflow-backend/
└── src/main/java/com/kodelabs/formflow/
    ├── shared/              — utilidades, excepciones, configuración global
    │   ├── tenant/          — TenantContext (ThreadLocal), TenantFilter
    │   ├── web/             — ApiResponse wrapper
    │   └── exception/       — GlobalExceptionHandler, BusinessException
    └── modules/
        ├── auth/            — JWT, usuarios, password reset, verificación email
        ├── billing/         — Stripe Customer, subscriptions, invoices, webhooks
        ├── forms/           — formularios, secciones, preguntas, convocatorias
        ├── responses/       — recolección de respuestas + snapshot
        ├── reports/         — estadísticas y exportación Excel
        └── notifications/   — emails con Thymeleaf + JavaMailSender
```

## Reglas de arquitectura hexagonal
- El dominio NO depende de ningún framework
- Los casos de uso solo conocen interfaces (puertos), nunca implementaciones
- Los controladores REST van en `infrastructure/web/`
- Los repositorios JPA van en `infrastructure/persistence/`
- NUNCA acceder al repositorio de otro módulo directamente — usar la interfaz de servicio

### Separación dominio / persistencia (OBLIGATORIA)
El modelo de dominio y el modelo de persistencia son clases DISTINTAS. Esto prepara la
extracción futura de módulos a microservicios: el dominio y los casos de uso se mueven
tal cual, solo se reescriben adaptadores.

- `domain/model/` — POJOs puros. PROHIBIDO importar `jakarta.persistence.*` u
  `org.hibernate.*`. Lombok está permitido (solo compile-time).
- `infrastructure/persistence/entity/` — entidades JPA (`XxxJpaEntity`) con las anotaciones
  `@Entity`, `@Column`, etc. Sin relaciones `@ManyToOne` entre agregados: usar columnas
  UUID planas (`tenant_id`, `user_id`) — facilita separar BDs después.
- `infrastructure/persistence/mapper/` — `XxxPersistenceMapper` convierte JPA ↔ dominio.
- `infrastructure/persistence/adapter/` — `XxxRepositoryAdapter` implementa el puerto del
  dominio usando el repositorio Spring Data + mapper.
- La capa web tampoco expone dominio: siempre DTOs de request/response.

Estructura completa por módulo:
```
[modulo]/
├── domain/
│   ├── model/       # POJOs puros de dominio
│   └── port/        # Interfaces (puertos)
├── application/
│   └── usecase/     # Casos de uso — solo conocen puertos
└── infrastructure/
    ├── web/         # Controllers REST + DTOs
    └── persistence/
        ├── entity/      # Entidades JPA
        ├── repository/  # Interfaces Spring Data JPA
        ├── mapper/      # JPA ↔ dominio
        └── adapter/     # Implementación de los puertos
```

## Multi-tenancy
- Cada request lleva header `X-Tenant-ID`
- `TenantContext` (ThreadLocal) disponible en toda la request — se limpia en `finally`
- Todas las entidades tienen campo `tenantId`
- Validar siempre que el recurso pertenece al tenant activo

## Decisiones técnicas clave

### Soft delete
`deleted_at TIMESTAMPTZ NULL` en: `forms`, `form_sections`, `form_questions`, `answer_options`, `convocatorias`.
Los repositorios filtran automáticamente `WHERE deleted_at IS NULL`.

### Snapshot de formulario
Al guardar una respuesta, `FormResponse.form_snapshot JSONB` almacena la estructura completa
del formulario en ese momento. Esto hace inmutables los datos históricos aunque el formulario se edite.

### Mensajes i18n
Todos los mensajes visibles al usuario en `messages_es.properties`. Usar `MessageSource` en
`GlobalExceptionHandler`. Nunca strings hardcodeados en código Java.

### Seguridad — password reset
Tokens de 64 chars generados con `SecureRandom`, expiran en 1 hora, uso único.
Al resetear contraseña se invalidan todos los `refresh_tokens` activos del usuario.
`POST /auth/forgot-password` retorna 200 siempre (no revelar si el email existe).

### Billing — Stripe
- Crear Stripe Customer de forma **asíncrona** al registrar tenant
- `POST /api/v1/webhooks/stripe` excluido de JWT y TenantFilter
- Verificar `Stripe-Signature` header en CADA webhook — rechazar si inválido
- Idempotencia por `stripe_event_id UNIQUE` en `invoices`
- Plan se actualiza SOLO desde webhook, nunca desde redirect del frontend

## Convenciones
- Idioma del código: inglés
- Commits en español referenciando el issue: `feat: agregar endpoint de login (#13)`
- Branches: `feature/nombre`, `fix/nombre`
- Tests: JUnit 5 + Mockito, mínimo en casos de uso

## Issues por milestone
| Milestone | Issues |
|-----------|--------|
| M1 | #11 ✅ #12 ✅ #13 🔄 #14 #15 #25 #26 |
| M2 | #1 #2 #3 #4 #20 #21 #27 |
| M3 | #16 #17 #18 #19 |
| M4 | #5 #6 #7 #8 |
| M5 | #9 #10 #28 |
| M6 | #22 #23 #24 |

## Links
- Issues: https://github.com/juancamilokremer/formflow-backend/issues
- Proyecto: https://github.com/users/juancamilokremer/projects/2
- Swagger (local): http://localhost:8080/swagger-ui.html
