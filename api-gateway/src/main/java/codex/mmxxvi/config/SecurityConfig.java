package codex.mmxxvi.config;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login").permitAll()
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/payments/vnpay/callback",
                                "/api/payments/vnpay-payment",
                                "/api/payment/vnpay/callback",
                                "/api/payment/vnpay-payment",
                                "/api/search/tickets",
                                "/api/search/tickets/**"
                        ).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                }))
                .build();
    }
}
