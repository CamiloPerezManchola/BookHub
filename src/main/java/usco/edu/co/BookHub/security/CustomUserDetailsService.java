package usco.edu.co.BookHub.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(
                    identifier,
                    identifier
                )
                .orElseThrow(() ->
                    new UsernameNotFoundException(
                        "Usuario o correo no encontrado"
                    )
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRol().name())
                .build();
    }
}