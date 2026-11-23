package usco.edu.co.BookHub.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
import usco.edu.co.BookHub.dto.request.ReviewRequest;
import usco.edu.co.BookHub.dto.response.ReviewResponse;
import usco.edu.co.BookHub.model.Role;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.UserRepository;
import usco.edu.co.BookHub.service.ReviewService;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    public ReviewController(
            ReviewService reviewService,
            UserRepository userRepository) {

        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    private User obtenerUsuarioActual(UserDetails principal) {

        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                "You must be logged in."
            );
        }

        return userRepository.findByEmail(principal.getUsername())
            .orElseThrow(() ->
                new IllegalArgumentException("User not found")
            );
    }

    // PUBLIC: Community reviews
    @GetMapping("/books/{bookId}/reviews")
    public List<ReviewResponse> obtenerResenas(
            @PathVariable Long bookId) {

        return reviewService.obtenerPorLibro(bookId);
    }

    // AUTHENTICATED: My reviews
    @GetMapping("/books/{bookId}/reviews/mine")
    public List<ReviewResponse> obtenerMisResenas(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserDetails principal) {

        User user = obtenerUsuarioActual(principal);

        return reviewService.obtenerMisResenas(
            bookId,
            user.getId()
        );
    }

    // AUTHENTICATED: Create review
    @PostMapping("/books/{bookId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse crearResena(
            @PathVariable Long bookId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        User user = obtenerUsuarioActual(principal);

        return reviewService.crear(bookId, request, user);
    }

    // AUTHENTICATED: Edit review
    @PutMapping("/reviews/{id}")
    public ReviewResponse actualizarResena(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        User user = obtenerUsuarioActual(principal);

        return reviewService.actualizar(id, request, user);
    }

    // AUTHENTICATED: Delete review
    @DeleteMapping("/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarResena(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        User user = obtenerUsuarioActual(principal);

        reviewService.eliminar(id, user);
    }

    @GetMapping("/admin/reviews")
    public List<ReviewResponse> obtenerTodasLasResenas(
            @AuthenticationPrincipal UserDetails principal) {

        User user = obtenerUsuarioActual(principal);

        if (user.getRol() != Role.ADMIN) {
            throw new AccessDeniedException("Admin only");
        }

        return reviewService.obtenerTodas();
    }

    @DeleteMapping("/admin/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarComoAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        User user = obtenerUsuarioActual(principal);

        reviewService.eliminarComoAdmin(id, user);
    }
}
