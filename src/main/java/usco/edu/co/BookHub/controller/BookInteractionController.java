package usco.edu.co.BookHub.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.UserRepository;
import usco.edu.co.BookHub.service.BookInteractionService;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookInteractionController {

    private final BookInteractionService interactionService;
    private final UserRepository userRepository;

    private Long getCurrentUserId(Principal principal) {

        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Authentication required"
            );
        }

        User user = userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() ->
                new RuntimeException("User not found"));

        return user.getId();
    }

    @PostMapping("/{bookId}/favorite")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable Long bookId,
            Principal principal) {

        Long userId = getCurrentUserId(principal);

        boolean active = interactionService.toggleFavorite(
            userId, bookId
        );

        return ResponseEntity.ok(Map.of(
            "active", active,
            "count", interactionService.countFavorites(bookId)
        ));
    }

    @PostMapping("/{bookId}/save")
    public ResponseEntity<?> toggleSaved(
            @PathVariable Long bookId,
            Principal principal) {

        Long userId = getCurrentUserId(principal);

        boolean active = interactionService.toggleSaved(
            userId, bookId
        );

        return ResponseEntity.ok(Map.of(
            "active", active,
            "count", interactionService.countSaved(bookId)
        ));
    }
}