package lmc.config;

import lmc.user.model.User;
import lmc.user.model.UserRole;
import lmc.user.model.UserStatus;
import lmc.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
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

//            .logout(logout -> logout
//            .logoutRequestMatcher(request ->
//                    request.getMethod().equals("GET") && request.getRequestURI().equals("/logout"))
//            .logoutSuccessUrl("/")
//    )  - Логаут с GET заявка !

        return http.build();
    }
 @Bean
public CommandLineRunner initializeAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder){

    return args -> {
        if (userRepository.count() == 0) {

            User admin = User.builder()
                    .email("admin@local.me")
                    .password(passwordEncoder.encode("admin321"))
                    .firstName("Admin")
                    .lastName("Deactivate after Permanent User is created")
                    .userRole(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(admin);
        }
    };
}



}
