package com.det.ragchat.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RAG Chat Storage Service",
                version = "0.1.0",
                description = "Microservice for storing RAG chat sessions and messages"
        )
)
@SecurityScheme(
        name = "ApiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key"
)
public class OpenApiConfig {
    // Note: User scoping header is also required: X-User-Id (configurable via APP_SECURITY_USER_ID_HEADER).
}
