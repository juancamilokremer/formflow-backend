# FormFlow — Backend

API REST de la plataforma SaaS de formularios dinámicos FormFlow.

**Kode Labs** | Java 17 · Spring Boot 3 · PostgreSQL · Arquitectura Hexagonal

---

## Requisitos

- Java 17+
- Maven 3.9+
- Docker y Docker Compose (para PostgreSQL local)

## Inicio rápido

```bash
# 1. Levantar base de datos local
docker-compose up -d

# 2. Ejecutar la aplicación
mvn spring-boot:run

# 3. Swagger UI disponible en:
# http://localhost:8080/swagger-ui.html
```

## Estructura del proyecto

```
src/main/java/com/kodelabs/formflow/
├── FormFlowApplication.java
├── shared/                    # Utilidades transversales
│   ├── config/                # Configuraciones Spring
│   ├── exception/             # Manejo global de errores
│   ├── tenant/                # Contexto multi-tenant
│   └── web/                   # ApiResponse wrapper
└── modules/
    ├── auth/                  # Autenticación y usuarios
    ├── tenants/               # Gestión de empresas
    ├── forms/                 # Formularios y preguntas
    ├── responses/             # Recolección de respuestas
    ├── reports/               # Estadísticas y exportación
    └── notifications/         # Emails y alertas
```

Cada módulo sigue **Arquitectura Hexagonal**:
```
[modulo]/
├── domain/
│   ├── model/       # Entidades de dominio
│   └── port/        # Interfaces (puertos de entrada y salida)
├── application/
│   └── usecase/     # Casos de uso (lógica de negocio)
└── infrastructure/
    ├── web/         # Controllers REST
    └── persistence/ # Repositorios JPA
```

## Variables de entorno (producción)

| Variable | Descripción |
|----------|-------------|
| `DATABASE_URL` | URL de conexión PostgreSQL |
| `DATABASE_USERNAME` | Usuario de BD |
| `DATABASE_PASSWORD` | Contraseña de BD |
| `JWT_SECRET` | Secreto para firmar JWT (mín. 32 chars) |
| `JWT_EXPIRATION_MS` | Expiración del access token (default: 86400000) |
| `CORS_ORIGINS` | Orígenes permitidos para CORS |

## Documentación API

Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON: `http://localhost:8080/api-docs`
