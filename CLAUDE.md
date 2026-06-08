# FormFlow — Backend

> Para contexto completo del proyecto ver: `E:\emprendimiento\KodeLabs\formflow\CLAUDE.md`

## Stack
- Java 17 (migrar a 21 LTS cuando sea posible)
- Spring Boot 3 + Maven
- PostgreSQL + Flyway
- Spring Security + JWT
- Redis (caché y rate limiting)
- springdoc-openapi (Swagger)
- Apache POI (exportación Excel)

## Arquitectura
Monolito Modular con Arquitectura Hexagonal por módulo.

```
formflow-backend/
└── src/main/java/com/kodelabs/formflow/
    ├── shared/              — utilidades, excepciones, configuración global
    └── modules/
        ├── auth/            — autenticación, JWT, usuarios
        │   ├── domain/      — entidades, value objects, puertos
        │   ├── application/ — casos de uso
        │   └── infrastructure/ — repositorios, controladores, adaptadores
        ├── tenants/         — gestión de empresas clientes
        ├── forms/           — formularios, secciones, preguntas
        ├── responses/       — recolección de respuestas
        ├── reports/         — estadísticas y exportación
        └── notifications/   — emails y alertas
```

## Reglas de arquitectura hexagonal
- El dominio NO depende de ningún framework
- Los casos de uso solo conocen interfaces (puertos), nunca implementaciones
- Los controladores REST van en `infrastructure/web/`
- Los repositorios JPA van en `infrastructure/persistence/`
- NUNCA acceder al repositorio de otro módulo directamente — usar la interfaz de servicio

## Multi-tenancy
- Cada request lleva header `X-Tenant-ID` o subdominio
- `TenantContext` (ThreadLocal) disponible en toda la request
- Todas las entidades tienen campo `tenantId`
- Validar siempre que el recurso pertenece al tenant activo

## Convenciones
- Idioma del código: inglés
- Commits en español referenciando el issue: `feat: agregar endpoint de login (#13)`
- Branches: `feature/nombre`, `fix/nombre`
- Tests: JUnit 5 + Mockito, mínimo en casos de uso

## Issues activos (M1)
- [ ] #11 Inicializar proyecto Spring Boot 3
- [ ] #12 Configurar PostgreSQL + Flyway
- [ ] #13 Implementar autenticación JWT multi-tenant
- [ ] #14 Configurar CI/CD con GitHub Actions
- [ ] #15 Configurar Swagger / OpenAPI

## Links
- Issues: https://github.com/juancamilokremer/formflow-backend/issues
- Proyecto: https://github.com/users/juancamilokremer/projects/2
