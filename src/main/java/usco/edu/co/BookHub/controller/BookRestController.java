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
import usco.edu.co.BookHub.dto.request.BookRequest;
import usco.edu.co.BookHub.dto.response.BookResponse;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.UserRepository;
import usco.edu.co.BookHub.service.BookService;
import usco.edu.co.BookHub.service.ReviewService;


@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService bookService;
    private final UserRepository userRepository;
    private final ReviewService reviewService;

    public BookRestController(BookService bookService, UserRepository userRepository, ReviewService reviewService) {
        this.bookService = bookService;
        this.userRepository = userRepository;
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<BookResponse> obtenerTodos() {
        return bookService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public BookResponse obtenerPorId(@PathVariable Long id) {
        return bookService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse crear(
            @Valid @RequestBody BookRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        User publisher = userRepository
                .findByEmail(principal.getUsername())
                .orElseThrow(() ->
                    new IllegalArgumentException("Usuario no encontrado")
                );

        return bookService.crear(request, publisher);
    }

    @PutMapping("/{id}")
    public BookResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        User currentUser = userRepository
                .findByEmail(principal.getUsername())
                .orElseThrow(() ->
                    new IllegalArgumentException("Usuario no encontrado")
                );

        return bookService.actualizar(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {

        User currentUser = userRepository
                .findByEmail(principal.getUsername())
                .orElseThrow(() ->
                    new IllegalArgumentException("Usuario no encontrado")
                );

        bookService.eliminar(id, currentUser);
    }
}