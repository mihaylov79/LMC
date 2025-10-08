package lmc.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
            .authorizeHttpRequests(authorizeRequest ->
                    authorizeRequest.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                            .requestMatchers("/**").permitAll()
                            .anyRequest().authenticated()
            ).formLogin(formLogin -> formLogin
                    .loginPage("/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .defaultSuccessUrl("/home")
                    .failureUrl("/login?error")
                    .permitAll()
            ).logout(logout -> logout.logoutUrl("/logout")
                    .logoutSuccessUrl("/")
            );

        return http.build();
    }
}
