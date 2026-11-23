package usco.edu.co.BookHub.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import usco.edu.co.BookHub.dto.request.AdminUpdateUserRequest;
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

    @PutMapping("/{id}")
    public UserResponse actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        User currentUser = obtenerUsuarioActual(principal);

        return userService.actualizarUsuario(
            id,
            request,
            currentUser
        );
    }
}
