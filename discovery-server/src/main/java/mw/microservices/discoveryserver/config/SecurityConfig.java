package mw.microservices.discoveryserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

@Configuration
@EnableWebSecurity //we use spring security with spring security configure adapter
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //from application.properties
//    @Value("${app.eureka.username}")
//    private String username;
//    @Value("${app.eureka.password}")
//    private String password;

    @Override
    protected void configure(AuthenticationManagerBuilder authMangerBuilder) throws Exception {
        authMangerBuilder.inMemoryAuthentication()
                //password encoder (in production user a password encoder not NoOpPasswordEncoder)
                .passwordEncoder(NoOpPasswordEncoder.getInstance())
                .withUser("eureka").password("password")
                //.withUser(username).password(password)
                .authorities("USER");
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .authorizeRequests().anyRequest()
                .authenticated()
                .and()
                .httpBasic();
    }
}
