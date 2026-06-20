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
| SonarQube (calidad de código) | http://localhost:9000 (ver sección abajo) |

### Probar la API desde Swagger

1. `POST /api/v1/auth/register` crea una empresa + usuario admin (o usa `POST /api/v1/auth/login`)
2. Copia el `accessToken` de la respuesta
3. Botón **Authorize** (candado) → pega el token
4. Los endpoints protegidos quedan autenticados; los correos enviados se ven en MailHog

## Análisis de calidad (SonarQube)

SonarQube corre localmente vía Docker. Detecta código duplicado, code smells, bugs,
vulnerabilidades y mide la cobertura de tests. **No corre en CI** — se ejecuta bajo demanda
durante el desarrollo.

### Primera vez

```bash
# 1. Levantar SonarQube (puede tardar ~60s en arrancar)
docker-compose up -d sonarqube

# 2. Abrir http://localhost:9000
#    Usuario: admin | Contraseña: admin
#    (pedirá cambiar la contraseña al primer login)

# 3. Crear el proyecto en la UI:
#    "Create project" → "Manually" → Project key: formflow-backend
```

### Ejecutar análisis

```bash
# Asegúrate de que SonarQube esté corriendo
docker-compose up -d sonarqube

# Análisis completo: tests + cobertura JaCoCo + reporte Sonar
mvn verify sonar:sonar

# Ver resultados en:
# http://localhost:9000/dashboard?id=formflow-backend
```

### Solo tests y cobertura (sin Sonar)

```bash
# Lo mismo que corre el CI — más rápido
mvn verify

# Reporte HTML de cobertura generado en:
# target/site/jacoco/index.html
```

### Qué detecta SonarQube

| Categoría | Ejemplos |
|-----------|---------|
| Code smells | Métodos muy largos, complejidad cognitiva alta |
| Duplicaciones | Bloques de código repetidos entre clases |
| Bugs | Null pointer potenciales, recursos no cerrados |
| Vulnerabilidades | Secrets hardcodeados, inyecciones |
| Cobertura | % de líneas/ramas cubiertas por tests |

Las clases sin lógica de negocio (POJOs de dominio, entidades JPA, DTOs) están excluidas
del análisis para no sesgar las métricas.

### Migrar a SonarCloud (cuando estés listo para producción)

En `pom.xml`, cambiar solo dos líneas:

```xml
<!-- Reemplazar: -->
<sonar.host.url>http://localhost:9000</sonar.host.url>

<!-- Por: -->
<sonar.host.url>https://sonarcloud.io</sonar.host.url>
<sonar.organization>juancamilokremer</sonar.organization>
```

Luego agregar el secret `SONAR_TOKEN` en GitHub Actions y añadir `sonar:sonar`
al paso de CI. Con eso el Quality Gate queda visible en cada PR.

---

## Funcionalidad actual (M2)

### Formularios, secciones y preguntas

- **CRUD de formularios**: crear, listar, obtener, actualizar y eliminar formularios con
  soft delete y versionamiento automático (`version` se incrementa en cada cambio estructural)
- **CRUD de secciones**: agregar, actualizar, eliminar y reordenar secciones dentro de un formulario
- **CRUD de preguntas**: agregar, actualizar, eliminar y reordenar preguntas dentro de una sección
- **Motor de tipos de campo extensible**: 8 tipos soportados con validación, post-procesamiento
  y schema por defecto encapsulados en un handler por tipo (ver [Patrón TypeHandler](#patrón-questiontypehandler))
- **Auto-scoring para tipo SCALE**: distribución lineal de puntajes `score = round(value × 10 / max)`
- **Catálogo de tipos**: `GET /api/v1/forms/question-types` retorna todos los tipos con su schema
  de configuración esperado (consumido por el constructor drag & drop del frontend)

---

## Patrón QuestionTypeHandler

### El problema

Cada tipo de pregunta (`TEXT`, `SCALE`, `MATRIX`…) tiene su propia estructura de configuración,
reglas de validación y lógica de post-procesamiento. El enfoque naive usa `switch` en cada punto
del sistema que necesita saber el tipo:

```java
// ❌ Cada nuevo tipo obliga a modificar 3 archivos existentes
switch (type) {
    case TEXT   -> objectMapper.convertValue(raw, TextConfig.class);
    case SCALE  -> buildScale(raw);  // lógica especial aquí
    case MATRIX -> ...
}
```

Esto viola el **principio Open/Closed**: agregar un tipo requiere abrir y modificar clases ya
probadas en producción.

### La solución

Cada tipo encapsula **todo** su comportamiento en un `QuestionTypeHandler` propio, descubierto
automáticamente por Spring. El sistema central nunca necesita conocer los tipos concretos.

```
QuestionTypeHandler<T> (interfaz)
  ├── type()            → identidad del tipo (QuestionType record)
  ├── build(Map)        → deserializar + post-procesar + validar (desde HTTP request)
  ├── deserialize(json) → reconstruir desde BD
  └── defaultSchema()   → schema de ejemplo para el catálogo del frontend
```

`QuestionType` es un `record(String code)` en lugar de un enum, lo que permite agregar tipos
externos sin modificar el core.

### Flujo de una request

```
POST /questions  { type: "SCALE", config: { min:1, max:5, scoringType:"AUTO" } }
        │
        ▼
QuestionConfigFactory
  → QuestionTypeRegistry.get(QuestionType("SCALE"))
        │
        ▼
ScaleTypeHandler.build(rawConfig)
  1. objectMapper.convertValue(raw, ScaleConfig.class)
  2. config.calculateAutoScores()   ← lógica exclusiva de SCALE
  3. config.validate()              ← ScaleConfig implements Validatable
  4. retorna ScaleConfig listo
        │
        ▼
FormQuestionPersistenceMapper.toEntity()
  → type = "SCALE" (String en BD)
  → config = JSON serializado

// En lectura, el mapper hace lo contrario:
  → registry.get(new QuestionType("SCALE")).deserialize(json)
```

### Cómo agregar un nuevo tipo

1. Agregar el tipo al enum (una línea):
```java
// QuestionType no es enum, es record — solo crear los archivos de abajo
```

2. Crear `RankingConfig.java`:
```java
public class RankingConfig extends QuestionConfig implements Validatable {
    private List<String> items;
    @Override public void validate() { require(items, "items"); }
}
```

3. Crear `RankingTypeHandler.java`:
```java
@Component
public class RankingTypeHandler implements QuestionTypeHandler<RankingConfig> {
    public static final QuestionType TYPE = new QuestionType("RANKING");
    // build(), deserialize(), defaultSchema()
}
```

**Spring descubre el handler automáticamente. `QuestionConfigFactory`,
`FormQuestionPersistenceMapper` y `QuestionTypesController` no se tocan.**

### Estructura de clases

```
application/service/
├── QuestionTypeRegistry.java          # Mapa tipo → handler (singleton, construido al arrancar)
├── QuestionConfigFactory.java         # Fachada delgada usada por los casos de uso
└── handler/
    ├── QuestionTypeHandler.java        # Interfaz + helper validateIfNeeded()
    ├── TextTypeHandler.java
    ├── SingleTypeHandler.java
    ├── MultipleTypeHandler.java
    ├── ScaleTypeHandler.java           # Incluye lógica calculateAutoScores
    ├── DateTypeHandler.java
    ├── FileTypeHandler.java
    ├── MatrixTypeHandler.java
    └── NpsTypeHandler.java

domain/model/
├── QuestionType.java                   # record(String code) — no enum
└── config/
    ├── QuestionConfig.java             # Clase base abstracta con helper require()
    ├── Validatable.java                # Interface para configs con reglas de validación
    └── [TextConfig, ScaleConfig, ...]  # 8 POJOs de configuración
```

---

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
    ├── forms/                 # Formularios, secciones, preguntas (ver Patrón TypeHandler)
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

## Endpoints de formularios y preguntas

| Método | Path | Descripción |
|--------|------|-------------|
| GET | `/api/v1/forms/question-types` | Catálogo de tipos con schema de configuración |
| POST | `/api/v1/forms` | Crear formulario |
| GET | `/api/v1/forms` | Listar formularios del tenant |
| GET | `/api/v1/forms/{id}` | Obtener formulario con secciones y preguntas |
| PUT | `/api/v1/forms/{id}` | Actualizar formulario |
| DELETE | `/api/v1/forms/{id}` | Eliminar formulario (soft delete) |
| POST | `/api/v1/forms/{formId}/sections` | Agregar sección |
| PUT | `/api/v1/forms/{formId}/sections/{id}` | Actualizar sección |
| DELETE | `/api/v1/forms/{formId}/sections/{id}` | Eliminar sección |
| PUT | `/api/v1/forms/{formId}/sections/reorder` | Reordenar secciones |
| POST | `/api/v1/forms/{formId}/sections/{sectionId}/questions` | Agregar pregunta |
| PUT | `/api/v1/forms/{formId}/sections/{sectionId}/questions/{id}` | Actualizar pregunta |
| DELETE | `/api/v1/forms/{formId}/sections/{sectionId}/questions/{id}` | Eliminar pregunta |
| PUT | `/api/v1/forms/{formId}/sections/{sectionId}/questions/reorder` | Reordenar preguntas |

Todos los endpoints de formularios requieren JWT. Documentación interactiva en `/swagger-ui.html`.

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

- **CI** (GitHub Actions): `mvn verify` en cada PR y push a `main` — compila, corre los 114+
  tests y genera el reporte de cobertura JaCoCo. El reporte queda disponible como artifact
  descargable en cada run. El check es requerido para mergear.
- **CD**: deploy automático a Railway en cada merge a `main` (se activa al configurar el
  secret `RAILWAY_TOKEN`; ver issue #14).
- **Análisis estático**: SonarQube corre localmente bajo demanda (ver sección de arriba),
  no en CI. Para activarlo en CI en el futuro, ver instrucciones de migración a SonarCloud.
