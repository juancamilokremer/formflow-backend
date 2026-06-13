# FormFlow — Backend

[![CI - Build & Test](https://github.com/juancamilokremer/formflow-backend/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/juancamilokremer/formflow-backend/actions/workflows/ci.yml)

API REST de la plataforma SaaS de formularios dinámicos FormFlow.

**Kode Labs** | Java 17 · Spring Boot 3 · PostgreSQL · Arquitectura Hexagonal

---

## Requisitos

- Java 17+
- Maven 3.9+
- Docker y Docker Compose (PostgreSQL + MailHog locales)

## Inicio rápido

```bash
# 1. Levantar base de datos y servidor SMTP de desarrollo
docker-compose up -d

# 2. Ejecutar la aplicación (Flyway aplica las migraciones al arrancar)
mvn spring-boot:run

# 3. Ejecutar los tests
mvn test
```

| Servicio local | URL |
|----------------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| MailHog (bandeja de correos de dev) | http://localhost:8025 |
| PostgreSQL | localhost:5434 (`formflow` / `formflow123` / db `formflow_dev`) |

### Probar la API desde Swagger

1. `POST /api/v1/auth/register` crea una empresa + usuario admin (o usa `POST /api/v1/auth/login`)
2. Copia el `accessToken` de la respuesta
3. Botón **Authorize** (candado) → pega el token
4. Los endpoints protegidos quedan autenticados; los correos enviados se ven en MailHog

## Funcionalidad actual (M1)

- **Autenticación JWT multi-tenant**: registro de empresa, login por tenant, refresh tokens
  con rotación de un solo uso y detección de reuso (revoca todas las sesiones)
- **Password reset y verificación de email**: tokens de un solo uso enviados por correo
  (reset expira en 1h, verificación en 24h); resetear contraseña revoca todos los refresh tokens
- **Emails transaccionales**: módulo notifications con un composer por tipo de correo
  (patrón Strategy), plantillas Thymeleaf internacionalizadas y envío asíncrono
- **i18n**: todos los mensajes visibles al usuario en `messages_es.properties` — agregar
  un idioma no requiere tocar código
- **Observabilidad**: `requestId` por request (header `X-Request-Id`), `tenantId`/`userId`
  en cada línea de log, código de soporte en errores 500 y access log con duración

## Estructura del proyecto

```
src/main/java/com/kodelabs/formflow/
├── FormFlowApplication.java
├── shared/                    # Transversal
│   ├── config/                # OpenAPI, Async (executor de emails)
│   ├── exception/             # GlobalExceptionHandler, BusinessException (claves i18n)
│   ├── i18n/                  # MessageSource + helper Messages
│   ├── logging/               # RequestLoggingFilter (requestId + access log)
│   ├── security/              # JWT: servicio, filtro, SecurityConfig
│   ├── tenant/                # TenantContext (ThreadLocal), TenantFilter
│   └── web/                   # ApiResponse wrapper
└── modules/
    ├── auth/                  # Tenants, usuarios, JWT, password reset, verificación email
    └── notifications/         # Emails: composers (Strategy), plantillas, SMTP
```

Cada módulo sigue **arquitectura hexagonal** con separación estricta dominio/persistencia:

```
[modulo]/
├── domain/
│   ├── model/       # POJOs puros (sin JPA/Hibernate)
│   └── port/
│       ├── in/      # Interfaces XxxUseCase (+ command/ y result/)
│       └── out/     # Puertos de persistencia, tokens, email...
├── application/
│   ├── usecase/     # XxxService — implementaciones de los puertos de entrada
│   └── service/     # Colaboradores compartidos (TokenIssuer, AuthEmailSender...)
└── infrastructure/
    ├── web/         # Controllers REST + DTOs (dependen solo de puertos de entrada)
    ├── security/    # Adaptadores de hashing y tokens
    └── persistence/ # entity/ repository/ mapper/ adapter/
```

## Endpoints de autenticación

| Método | Path | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/api/v1/auth/register` | Pública | Registrar empresa + usuario admin |
| POST | `/api/v1/auth/login` | Pública | Login con email + contraseña + slug del tenant |
| POST | `/api/v1/auth/refresh` | Pública | Rotar tokens (refresh de un solo uso) |
| POST | `/api/v1/auth/forgot-password` | Pública | Solicitar reset (200 siempre) |
| POST | `/api/v1/auth/reset-password` | Pública | Cambiar contraseña con token del correo |
| POST | `/api/v1/auth/verify-email` | Pública | Confirmar correo con token |
| POST | `/api/v1/auth/resend-verification` | JWT | Reenviar correo de verificación |

## Variables de entorno (producción)

| Variable | Descripción |
|----------|-------------|
| `DATABASE_URL` | URL de conexión PostgreSQL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Credenciales de BD |
| `JWT_SECRET` | Secreto para firmar JWT (mín. 32 chars) |
| `JWT_EXPIRATION_MS` | Expiración del access token (default: 24h) |
| `JWT_REFRESH_MS` | Expiración del refresh token (default: 7 días) |
| `CORS_ORIGINS` | Orígenes permitidos para CORS |
| `SMTP_HOST` / `SMTP_PORT` | Servidor SMTP (SendGrid en prod) |
| `SMTP_USERNAME` / `SMTP_PASSWORD` | Credenciales SMTP |
| `MAIL_FROM` / `MAIL_FROM_NAME` | Remitente de los correos |
| `FRONTEND_BASE_URL` | Base de los links en correos (reset/verificación) |
| `EMAIL_POOL_CORE` / `EMAIL_POOL_MAX` / `EMAIL_QUEUE_CAPACITY` | Executor de envío asíncrono (defaults: 2 / 4 / 100) |

## CI/CD

- **CI** (GitHub Actions): build + tests con PostgreSQL en contenedor en cada PR y push a `main`.
  El check "Build y Tests" es requerido para mergear.
- **CD**: deploy automático a Railway en cada merge a `main` (se activa al configurar el
  secret `RAILWAY_TOKEN`; ver issue #14).
