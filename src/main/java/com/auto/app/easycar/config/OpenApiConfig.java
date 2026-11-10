package com.auto.app.easycar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI easycarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EasyCar API")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestion de usuarios, vehiculos, tipos de mantenimiento
                                e historial de mantenimientos.

                                Autenticacion: obtenga un token en POST /auth/login y enviarlo en el header
                                `Authorization: Bearer <token>`.
                                """)
                        .license(new License().name("Uso interno")))
                // Requerimiento global: todos los endpoints lo heredan salvo los marcados con @SecurityRequirements
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido en /auth/login")));
    }
}
