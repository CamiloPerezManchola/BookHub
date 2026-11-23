package usco.edu.co.BookHub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import jakarta.servlet.http.HttpServletResponse;
import usco.edu.co.BookHub.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(Customizer.withDefaults())

            .authorizeHttpRequests(auth -> auth

            // Public pages
            .requestMatchers(
                "/",
                "/login",
                "/register",
                "/explorar",
                "/libro/*",
                "/libro/*/leer"
            ).permitAll()

            // Static resources
            .requestMatchers(
                "/css/**",
                "/js/**",
                "/images/**",
                "/webjars/**"
            ).permitAll()

            .requestMatchers(
                org.springframework.http.HttpMethod.GET,
                "/api/books/*/chapters"
            ).permitAll()

            // Public registration
            .requestMatchers(
                "/api/users/register"
            ).permitAll()

            // Authenticated own profile
            .requestMatchers(
                "/api/users/me",
                "/perfil/**"
            ).authenticated()

            // ADMIN user management
            .requestMatchers(
                org.springframework.http.HttpMethod.GET,
                "/api/users"
            ).hasRole("ADMIN")

            .requestMatchers(
                org.springframework.http.HttpMethod.PUT,
                "/api/users/*"
            ).hasRole("ADMIN")

            .requestMatchers(
                org.springframework.http.HttpMethod.DELETE,
                "/api/users/*"
            ).hasRole("ADMIN")

            .requestMatchers(
                org.springframework.http.HttpMethod.GET,
                "/api/admin/reviews"
            ).hasRole("ADMIN")

            .requestMatchers(
                org.springframework.http.HttpMethod.DELETE,
                "/api/admin/reviews/*"
            ).hasRole("ADMIN")

            // Public book reading
            .requestMatchers(
                org.springframework.http.HttpMethod.GET,
                "/api/books",
                "/api/books/*"
            ).permitAll()

            // Authenticated book modifications
            .requestMatchers(
                org.springframework.http.HttpMethod.POST,
                "/api/books"
            ).authenticated()

            .requestMatchers(
                org.springframework.http.HttpMethod.PUT,
                "/api/books/*"
            ).authenticated()

            .requestMatchers(
                org.springframework.http.HttpMethod.DELETE,
                "/api/books/*"
            ).authenticated()

            // Public community reviews
            .requestMatchers(
                org.springframework.http.HttpMethod.GET,
                "/api/books/*/reviews"
            ).permitAll()

            // Authenticated review creation
            .requestMatchers(
                org.springframework.http.HttpMethod.POST,
                "/api/books/*/reviews"
            ).authenticated()

            // Authenticated review modifications
            .requestMatchers(
                "/api/reviews/**",
                "/api/books/*/reviews/mine"
            ).authenticated()

            // Authenticated interactions
            .requestMatchers(
                "/api/books/*/favorite",
                "/api/books/*/save"
            ).authenticated()

            // Book publishing and editing pages
            .requestMatchers(
                "/crear-libro",
                "/libro/*/editar"
            ).authenticated()

            // Reading progress - authenticated users
            .requestMatchers(
                "/api/books/*/progress"
            ).authenticated()

            // Chapter bookmarks - authenticated users
            .requestMatchers(
                "/api/books/*/bookmarks",
                "/api/books/*/bookmarks/*"
            ).authenticated()

            .requestMatchers("/admin/**").hasRole("ADMIN")

            // ADMIN review management
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            .anyRequest().authenticated()
        )

            // Exception handling
            .exceptionHandling(ex -> ex

                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) ->
                        response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Authentication required"
                        ),
                    PathPatternRequestMatcher.withDefaults()
                        .matcher("/api/**")
                )

                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Access denied"
                    )
                )
            )

            // Form login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("identifier")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // Logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
