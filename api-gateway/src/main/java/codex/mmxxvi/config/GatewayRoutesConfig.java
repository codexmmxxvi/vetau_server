package codex.mmxxvi.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-auth", route -> route
                        .path("/api/users/login", "/api/users/register")
                        .filters(filter -> filter.rewritePath("/api/users/(?<segment>.*)", "/v1/${segment}"))
                        .uri("lb://USER-SERVICE"))
                .route("user-resource", route -> route
                        .path("/api/users", "/api/users/**")
                        .filters(filter -> filter.rewritePath("/api/users(?<remaining>/?.*)", "/v1/users${remaining}"))
                        .uri("lb://USER-SERVICE"))
                .route("order-plural", route -> route
                        .path("/api/orders", "/api/orders/**")
                        .filters(filter -> filter.rewritePath("/api/orders(?<remaining>/?.*)", "/v1/orders${remaining}"))
                        .uri("lb://ORDER-SERVICE"))
                .route("order-singular", route -> route
                        .path("/api/order", "/api/order/**")
                        .filters(filter -> filter.rewritePath("/api/order(?<remaining>/?.*)", "/v1/orders${remaining}"))
                        .uri("lb://ORDER-SERVICE"))
                .route("payment-plural", route -> route
                        .path("/api/payments", "/api/payments/**")
                        .filters(filter -> filter.rewritePath("/api/payments(?<remaining>/?.*)", "/v1/payments${remaining}"))
                        .uri("lb://PAYMENT-SERVICE"))
                .route("payment-singular", route -> route
                        .path("/api/payment", "/api/payment/**")
                        .filters(filter -> filter.rewritePath("/api/payment(?<remaining>/?.*)", "/v1/payments${remaining}"))
                        .uri("lb://PAYMENT-SERVICE"))
                .route("ticket-plural", route -> route
                        .path("/api/tickets", "/api/tickets/**")
                        .filters(filter -> filter.rewritePath("/api/tickets(?<remaining>/?.*)", "/v1/tickets${remaining}"))
                        .uri("lb://TICKET-SERVICE"))
                .route("ticket-singular", route -> route
                        .path("/api/ticket", "/api/ticket/**")
                        .filters(filter -> filter.rewritePath("/api/ticket(?<remaining>/?.*)", "/v1/tickets${remaining}"))
                        .uri("lb://TICKET-SERVICE"))
                .route("search-resource", route -> route
                        .path("/api/search/tickets", "/api/search/tickets/**")
                        .uri("lb://SEARCH-SERVICE"))
                .build();
    }
}
