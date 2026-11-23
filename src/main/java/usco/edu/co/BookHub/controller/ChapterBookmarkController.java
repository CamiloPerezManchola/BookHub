package usco.edu.co.BookHub.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import usco.edu.co.BookHub.dto.response.ChapterBookmarkResponse;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.UserRepository;
import usco.edu.co.BookHub.service.ChapterBookmarkService;

@RestController
@RequestMapping("/api/books/{bookId}/bookmarks")
public class ChapterBookmarkController {

    private final ChapterBookmarkService bookmarkService;
    private final UserRepository userRepository;

    public ChapterBookmarkController(
            ChapterBookmarkService bookmarkService,
            UserRepository userRepository) {

        this.bookmarkService = bookmarkService;
        this.userRepository = userRepository;
    }

    private User obtenerUsuario(UserDetails principal) {

        return userRepository.findByEmail(principal.getUsername())
            .orElseThrow(() ->
                new IllegalArgumentException("User not found")
            );
    }

    @GetMapping
    public List<ChapterBookmarkResponse> obtenerBookmarks(
            @PathVariable Long bookId,
            @AuthenticationPrincipal UserDetails principal) {

        return bookmarkService.obtenerBookmarks(
            bookId, obtenerUsuario(principal)
        );
    }

    @PostMapping("/{chapterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void agregarBookmark(
            @PathVariable Long bookId,
            @PathVariable Long chapterId,
            @AuthenticationPrincipal UserDetails principal) {

        bookmarkService.agregarBookmark(
            bookId,
            chapterId,
            obtenerUsuario(principal)
        );
    }

    @DeleteMapping("/{chapterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarBookmark(
            @PathVariable Long bookId,
            @PathVariable Long chapterId,
            @AuthenticationPrincipal UserDetails principal) {

        bookmarkService.eliminarBookmark(
            bookId,
            chapterId,
            obtenerUsuario(principal)
        );
    }
}
