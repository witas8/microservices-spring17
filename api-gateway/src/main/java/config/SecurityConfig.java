package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity //because the API Gateway Spring Cloud is based on the Spring Web Flux
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity){
        serverHttpSecurity
                //because we just use postman to communication:
                .csrf().disable()
                //exclude each endpoint instead of eureka
                .authorizeExchange(exchange ->
                        exchange.pathMatchers("/eureka/**")
                                .permitAll()
                                .anyExchange()
                                .authenticated())
                //enable token authentication
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);

        return serverHttpSecurity.build();
    }
}
