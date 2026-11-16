
package usco.edu.co.BookHub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import usco.edu.co.BookHub.service.BookService;
import usco.edu.co.BookHub.service.ReviewService;

@Controller
public class PageController {

    private final BookService bookService;
    private final ReviewService reviewService;

    public PageController(
            BookService bookService,
            ReviewService reviewService) {

        this.bookService = bookService;
        this.reviewService = reviewService;
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

        model.addAttribute(
            "libros",
            bookService.obtenerTodos()
        );

        model.addAttribute("busqueda", q);

        return "explorar";
    }

    @GetMapping("/libro/{id}")
    public String detalleLibro(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
            "libro",
            bookService.obtenerPorId(id)
        );

        // Calculate community rating
        model.addAttribute(
            "ratingPromedio",
            reviewService.getAverageRating(id)
        );

        // Count total reviews
        model.addAttribute(
            "totalResenas",
            reviewService.getReviewCount(id)
        );

        return "libro";
    }

    @GetMapping("/crear-libro")
    public String crearLibro() {
        return "crear-libro";
    }

    @GetMapping("/mibiblioteca")
    public String miBiblioteca() {
        return "mibiblioteca";
    }
}
