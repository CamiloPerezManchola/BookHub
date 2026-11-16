package usco.edu.co.BookHub.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import usco.edu.co.BookHub.dto.request.RegisterRequest;
import usco.edu.co.BookHub.dto.request.UpdateProfileRequest;
import usco.edu.co.BookHub.dto.response.UserResponse;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.UserRepository;
import usco.edu.co.BookHub.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(
            UserService userService,
            UserRepository userRepository) {

        this.userService = userService;
        this.userRepository = userRepository;
    }

    // Resolve authenticated user
    private User obtenerUsuarioActual(UserDetails principal) {

        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() ->
                    new IllegalArgumentException("Usuario no encontrado")
                );
    }

    // Register
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registrar(
            @Valid @RequestBody RegisterRequest request) {

        System.out.println("================================");
        System.out.println("REGISTRATION CONTROLLER REACHED");
        System.out.println("Username: " + request.getUsername());
        System.out.println("Email: " + request.getEmail());
        System.out.println("================================");

        return userService.registrar(request);
    }

    // Get own profile
    @GetMapping("/me")
    public UserResponse obtenerMiPerfil(
            @AuthenticationPrincipal UserDetails principal) {

        return userService.obtenerMiPerfil(principal.getUsername());
    }

    // Update own profile
    @PutMapping("/me")
    public UserResponse actualizarMiPerfil(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {

        return userService.actualizarMiPerfil(
            principal.getUsername(),
            request
        );
    }

    // Get all users (ADMIN)
    @GetMapping
    public List<UserResponse> obtenerTodos(
            @AuthenticationPrincipal UserDetails principal) {

        User currentUser = obtenerUsuarioActual(principal);

        return userService.obtenerTodos(currentUser);
    }

    // Delete user (ADMIN)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        User currentUser = obtenerUsuarioActual(principal);

        userService.eliminar(id, currentUser);
    }
}
