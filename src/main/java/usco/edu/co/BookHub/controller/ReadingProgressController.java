package usco.edu.co.BookHub.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import usco.edu.co.BookHub.dto.request.ReadingProgressRequest;
import usco.edu.co.BookHub.dto.response.ReadingProgressResponse;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.UserRepository;
import usco.edu.co.BookHub.service.ReadingProgressService;

@RestController
@RequestMapping("/api/books/{bookId}/progress")
public class ReadingProgressController {

    private final ReadingProgressService progressService;
    private final UserRepository userRepository;

    public ReadingProgressController(
            ReadingProgressService progressService,
            UserRepository userRepository) {

        this.progressService = progressService;
        this.userRepository = userRepository;
    }

    private User obtenerUsuario(UserDetails principal) {

        return userRepository.findByEmail(principal.getUsername())
            .orElseThrow(() ->
                new IllegalArgumentException("User not found")
            );
    }

    @GetMapping
    public ReadingProgressResponse obtenerProgreso(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserDetails principal) {

        return progressService.obtenerProgreso(
            bookId, obtenerUsuario(principal)
        );
    }

    @PutMapping
    public ReadingProgressResponse guardarProgreso(
            @PathVariable Long bookId,
            @Valid @RequestBody ReadingProgressRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        return progressService.guardarProgreso(
            bookId,
            request,
            obtenerUsuario(principal)
        );
    }
}
