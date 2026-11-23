package usco.edu.co.BookHub.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usco.edu.co.BookHub.dto.request.RegisterRequest;
import usco.edu.co.BookHub.dto.request.UpdateProfileRequest;
import usco.edu.co.BookHub.dto.request.AdminUpdateUserRequest;
import usco.edu.co.BookHub.dto.response.UserResponse;
import usco.edu.co.BookHub.exception.DuplicateResourceException;
import usco.edu.co.BookHub.exception.ResourceNotFoundException;
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
        response.setUsername(user.getUsername());
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

        String username = request.getUsername()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

        String email = request.getEmail()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException(
                "The username is already registered."
            );
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException(
                "The email is already registered."
            );
        }

        User user = new User();

        user.setNombre(request.getNombre().trim());
        user.setUsername(username);
        user.setEmail(email);

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
                    new ResourceNotFoundException("User", email)
                );

        return convertirAResponse(user);
    }

    // Update authenticated user's profile
    public UserResponse actualizarMiPerfil(
            String email,
            UpdateProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User", email)
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

    // Update user (ADMIN only)
    public UserResponse actualizarUsuario(
            Long id,
            AdminUpdateUserRequest request,
            User currentUser) {

        // Verify administrator permissions
        verificarAdmin(currentUser);

        // Find user
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User", id)
                );

        // Normalize username and email
        String username = request.getUsername()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

        String email = request.getEmail()
                .trim()
                .toLowerCase(java.util.Locale.ROOT);

        // Check duplicate username excluding current user
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(
                username, id)) {

            throw new DuplicateResourceException(
                "The username is already registered."
            );
        }

        // Check duplicate email excluding current user
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(
                email, id)) {

            throw new DuplicateResourceException(
                "The email is already registered."
            );
        }

        // Validate role
        Role newRole = user.getRol();

        if (request.getRol() != null
                && !request.getRol().trim().isEmpty()) {

            try {
                newRole = Role.valueOf(
                    request.getRol().trim().toUpperCase(
                        java.util.Locale.ROOT
                    )
                );
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Invalid user role."
                );
            }
        }

        // Protect current administrator from demotion
        if (currentUser.getId().equals(id)
                && newRole != Role.ADMIN) {

            throw new IllegalArgumentException(
                "You cannot remove your own administrator role."
            );
        }

        // Protect the last administrator
        if (user.getRol() == Role.ADMIN
                && newRole != Role.ADMIN
                && userRepository.countByRol(Role.ADMIN) <= 1) {

            throw new IllegalArgumentException(
                "Cannot demote the last administrator."
            );
        }

        // Update user information
        user.setNombre(request.getNombre().trim());
        user.setEmail(email);
        user.setUsername(username);
        user.setDescripcion(request.getDescripcion());
        user.setFotoPerfilUrl(request.getFotoPerfilUrl());

        user.setRol(newRole);

        // Update password only when provided
        if (request.getPassword() != null
                && !request.getPassword().trim().isEmpty()) {

            user.setPassword(
                passwordEncoder.encode(request.getPassword())
            );
        }

        // Save and return response
        User updated = userRepository.save(user);

        return convertirAResponse(updated);
    }

    // Delete user (ADMIN only)
    public void eliminar(Long id, User currentUser) {

        verificarAdmin(currentUser);

        if (currentUser.getId().equals(id)) {
            throw new IllegalArgumentException(
                "You cannot delete your own account."
            );
        }

        User user = userRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("User", id)
            );

        if (user.getRol() == Role.ADMIN
                && userRepository.countByRol(Role.ADMIN) <= 1) {

            throw new IllegalArgumentException(
                "Cannot delete the last administrator."
            );
        }

        userRepository.delete(user);
    }
}
