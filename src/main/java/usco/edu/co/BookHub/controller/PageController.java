package usco.edu.co.BookHub.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.model.Role;
import usco.edu.co.BookHub.repository.UserRepository;
import usco.edu.co.BookHub.repository.BookRepository;
import usco.edu.co.BookHub.service.BookService;
import usco.edu.co.BookHub.service.ReviewService;
import usco.edu.co.BookHub.service.BookInteractionService;
import usco.edu.co.BookHub.repository.ReviewRepository;

@Controller
public class PageController {

    private final BookService bookService;
    private final ReviewService reviewService;
    private final BookInteractionService interactionService;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public PageController(
            BookService bookService,
            ReviewService reviewService,
            BookInteractionService interactionService,
            UserRepository userRepository,
            BookRepository bookRepository,
            ReviewRepository reviewRepository) {

        this.bookService = bookService;
        this.reviewService = reviewService;
        this.interactionService = interactionService;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/")
    public String inicio(Model model) {

        model.addAttribute(
            "libros",
            bookService.obtenerTodos()
        );

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/explorar")
    public String explorar(
            @RequestParam(required = false) String q,
            Model model) {

        // All books
        model.addAttribute(
            "libros",
            bookService.obtenerTodos()
        );

        // Search parameter
        model.addAttribute("busqueda", q);

        // Dynamic genres
        model.addAttribute(
            "generos",
            bookRepository.findDistinctGenres()
        );

        // Available publication years
        Integer minYear = bookRepository.findMinimumYear();
        Integer maxYear = bookRepository.findMaximumYear();

        model.addAttribute(
            "minYear",
            minYear != null ? minYear : 1800
        );

        model.addAttribute(
            "maxYear",
            maxYear != null ? maxYear : 2026
        );

        // Average rating per book
        Map<Long, Double> ratingByBook = new HashMap<>();

        for (Object[] result : reviewRepository.findAverageRatings()) {

            Long bookId = ((Number) result[0]).longValue();

            Double averageRating =
                ((Number) result[1]).doubleValue();

            ratingByBook.put(bookId, averageRating);
        }

        model.addAttribute("ratingByBook", ratingByBook);

        return "explorar";
    }

    @GetMapping("/libro/{id}")
    public String detalleLibro(
            @PathVariable Long id,
            Model model,
            Principal principal) {

        // Book information
        var libro = bookService.obtenerPorId(id);

        model.addAttribute("libro", libro);

        // Community rating
        model.addAttribute(
            "ratingPromedio",
            reviewService.getAverageRating(id)
        );

        // Total reviews
        model.addAttribute(
            "totalResenas",
            reviewService.getReviewCount(id)
        );

        // Favorite and saved counters
        model.addAttribute(
            "totalFavoritos",
            interactionService.countFavorites(id)
        );

        model.addAttribute(
            "totalGuardados",
            interactionService.countSaved(id)
        );

        // Current authenticated user
        User currentUser = null;

        if (principal != null) {

            currentUser = userRepository
                .findByEmail(principal.getName())
                .orElse(null);
        }

        model.addAttribute("currentUser", currentUser);

        // Individual interaction states
        if (currentUser != null) {

            Long userId = currentUser.getId();

            model.addAttribute(
                "esFavorito",
                interactionService.isFavorite(userId, id)
            );

            model.addAttribute(
                "estaGuardado",
                interactionService.isSaved(userId, id)
            );

        } else {

            model.addAttribute("esFavorito", false);
            model.addAttribute("estaGuardado", false);
        }

        boolean puedeEditarLibro = false;

        if (currentUser != null) {

            boolean esPropietario =
                libro.getPublisherId().equals(currentUser.getId());

            boolean esAdmin =
                currentUser.getRol() == Role.ADMIN;

            puedeEditarLibro = esPropietario || esAdmin;
        }

        model.addAttribute("puedeEditarLibro", puedeEditarLibro);

        return "libro";
    }

    @GetMapping("/crear-libro")
    public String crearLibro() {
        return "crear-libro";
    }

    @GetMapping("/mibiblioteca")
    public String miBiblioteca(
            Model model,
            Principal principal) {

        User currentUser = userRepository
            .findByEmail(principal.getName())
            .orElseThrow(() ->
                new RuntimeException("User not found"));

        Long userId = currentUser.getId();

        // Books published by this user
        var publicados = bookRepository.findByPublisher_Id(userId);

        // Favorite books
        var favoritos = interactionService.getFavoriteBooks(userId);

        // Saved books
        var guardados = interactionService.getSavedBooks(userId);

        model.addAttribute("publicados", publicados);
        model.addAttribute("favoritos", favoritos);
        model.addAttribute("librosGuardados", guardados);

        model.addAttribute("totalPublicados", publicados.size());
        model.addAttribute("totalFavoritos", favoritos.size());
        model.addAttribute("totalGuardados", guardados.size());

        return "mibiblioteca";
    }
}
