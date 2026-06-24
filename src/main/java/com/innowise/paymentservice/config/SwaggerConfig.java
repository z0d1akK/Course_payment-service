package com.innowise.paymentservice.config;

import com.innowise.paymentservice.common.constants.Headers;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    private static final String API_GATEWAY_URL = "http://localhost:8084";

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .version("1.0")
                        .description("REST API for Payment Service")
                )
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .schemaRequirement(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                )
                .addServersItem(createGatewayServer());
    }

    private Server createGatewayServer() {
        Server server = new Server();
        server.setDescription("API Gateway");
        String baseUrl = getGatewayBaseUrl();
        server.setUrl(baseUrl);
        return server;
    }

    private String getGatewayBaseUrl() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return API_GATEWAY_URL;
        }

        HttpServletRequest request = attributes.getRequest();

        String forwardedHost = request.getHeader(Headers.FORWARDED_HOST);
        String forwardedProto = request.getHeader(Headers.FORWARDED_PROTO);
        String forwardedPort = request.getHeader(Headers.FORWARDED_PORT);

        if (forwardedHost != null) {
            String protocol = forwardedProto != null ? forwardedProto : "http";
            String port = "";

            if (forwardedPort != null &&
                    !"80".equals(forwardedPort) &&
                    !"443".equals(forwardedPort)) {
                port = ":" + forwardedPort;
            }

            return protocol + "://" + forwardedHost + port;
        }

        return API_GATEWAY_URL;
    }
}

