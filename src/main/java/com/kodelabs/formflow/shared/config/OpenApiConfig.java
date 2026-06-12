package com.kodelabs.formflow.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI formFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FormFlow API")
                        .description("""
                                API REST para la plataforma de formularios dinámicos FormFlow — Kode Labs.

                                **Cómo autenticarse:**
                                1. Registra una empresa con `POST /api/v1/auth/register` (o usa `POST /api/v1/auth/login` si ya tienes cuenta)
                                2. Copia el `accessToken` de la respuesta
                                3. Haz clic en el botón **Authorize** (candado) y pega el token
                                4. Todos los endpoints protegidos usarán ese Bearer token automáticamente

                                El access token expira en 24h — usa `POST /api/v1/auth/refresh` para rotarlo.""")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Kode Labs")
                                .email("dev@kodelabs.co")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Auth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa el token JWT obtenido en /api/v1/auth/login")));
    }
}
