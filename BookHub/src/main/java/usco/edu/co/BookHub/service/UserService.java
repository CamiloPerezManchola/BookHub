package usco.edu.co.BookHub.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usco.edu.co.BookHub.dto.request.RegisterRequest;
import usco.edu.co.BookHub.dto.request.UpdateProfileRequest;
import usco.edu.co.BookHub.dto.response.UserResponse;
import usco.edu.co.BookHub.model.Role;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Entity -> Response DTO
    private UserResponse convertirAResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setNombre(user.getNombre());
        response.setEmail(user.getEmail());
        response.setDescripcion(user.getDescripcion());
        response.setFotoPerfilUrl(user.getFotoPerfilUrl());
        response.setRol(user.getRol().name());
        response.setFechaRegistro(user.getFechaRegistro());

        return response;
    }

    // Verify admin permission
    private void verificarAdmin(User user) {

        if (user.getRol() != Role.ADMIN) {
            throw new AccessDeniedException(
                "No tienes permisos de administrador"
            );
        }
    }

    // Register
    public UserResponse registrar(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                "El nombre de usuario ya está registrado"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                "El correo electrónico ya está registrado"
            );
        }

        User user = new User();

        user.setNombre(request.getNombre());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        user.setPassword(
            passwordEncoder.encode(request.getPassword())
        );

        user.setRol(Role.USER);

        User saved = userRepository.save(user);

        return convertirAResponse(saved);
    }

    // Get authenticated user's profile
    @Transactional(readOnly = true)
    public UserResponse obtenerMiPerfil(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new IllegalArgumentException("Usuario no encontrado")
                );

        return convertirAResponse(user);
    }

    // Update authenticated user's profile
    public UserResponse actualizarMiPerfil(
            String email,
            UpdateProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new IllegalArgumentException("Usuario no encontrado")
                );

        user.setNombre(request.getNombre());
        user.setDescripcion(request.getDescripcion());
        user.setFotoPerfilUrl(request.getFotoPerfilUrl());

        return convertirAResponse(userRepository.save(user));
    }

    // Get all users (ADMIN only)
    @Transactional(readOnly = true)
    public List<UserResponse> obtenerTodos(User currentUser) {

        verificarAdmin(currentUser);

        return userRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Delete user (ADMIN only)
    public void eliminar(Long id, User currentUser) {

        verificarAdmin(currentUser);

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException("Usuario no encontrado")
                );

        userRepository.delete(user);
    }
}
