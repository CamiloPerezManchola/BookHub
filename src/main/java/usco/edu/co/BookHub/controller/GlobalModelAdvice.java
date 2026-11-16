package usco.edu.co.BookHub.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.UserRepository;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UserRepository userRepository;

    public GlobalModelAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record HeaderUser(
        String nombre,
        String fotoPerfilUrl
    ) {}

    @ModelAttribute("currentUser")
    public HeaderUser currentUser(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElse(null);

        if (user == null) {
            return null;
        }

        return new HeaderUser(
            user.getNombre(),
            user.getFotoPerfilUrl()
        );
    }
}