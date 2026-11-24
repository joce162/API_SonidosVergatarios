package com.ms_SonidosVergatarios.api.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Puerta de entrada única. El Gateway solo enruta: la autenticación, los permisos y las reglas
 * de negocio las ejecuta cada microservicio. Las rutas /internal/** no se publican.
 */
@Configuration
public class RutasConfig {

    @Bean
    public RouteLocator rutas(RouteLocatorBuilder builder,
                              @Value("${app.login-url}") String login,
                              @Value("${app.productos-url}") String productos,
                              @Value("${app.carrito-url}") String carrito) {
        return builder.routes()
            .route("login", r -> r.path("/api/auth/**", "/api/usuarios/**").uri(login))
            .route("productos", r -> r.path("/api/productos/**").uri(productos))
            .route("carrito", r -> r.path("/api/carrito/**").uri(carrito))
            // Documentación OpenAPI de cada microservicio, para el Swagger UI unificado del Gateway.
            .route("docs-login", r -> r.path("/openapi/login")
                .filters(f -> f.setPath("/v3/api-docs")).uri(login))
            .route("docs-productos", r -> r.path("/openapi/productos")
                .filters(f -> f.setPath("/v3/api-docs")).uri(productos))
            .route("docs-carrito", r -> r.path("/openapi/carrito")
                .filters(f -> f.setPath("/v3/api-docs")).uri(carrito))
            .build();
    }

    /**
     * CORS: el navegador pregunta (OPTIONS) si el origen del frontend puede llamar al Gateway.
     * Con credenciales (cookie de renovación) el origen debe ser concreto, nunca "*".
     */
    @Bean
    public CorsWebFilter cors(@Value("${app.allowed-origins}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(fuente);
    }
}
