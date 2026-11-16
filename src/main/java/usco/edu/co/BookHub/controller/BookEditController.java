package usco.edu.co.BookHub.controller;

import java.security.Principal;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import usco.edu.co.BookHub.dto.request.BookRequest;
import usco.edu.co.BookHub.dto.response.BookResponse;
import usco.edu.co.BookHub.model.Role;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.UserRepository;
import usco.edu.co.BookHub.service.BookService;

@Controller
public class BookEditController {

    private final BookService bookService;
    private final UserRepository userRepository;

    public BookEditController(
            BookService bookService,
            UserRepository userRepository) {

        this.bookService = bookService;
        this.userRepository = userRepository;
    }

    private User obtenerUsuarioActual(Principal principal) {

        return userRepository.findByEmail(principal.getName())
            .orElseThrow(() ->
                new RuntimeException("User not found"));
    }

    private void verificarPermiso(
            BookResponse libro,
            User usuario) {

        boolean esPropietario =
            java.util.Objects.equals(
                libro.getPublisherId(),
                usuario.getId()
            );

        boolean esAdmin =
            usuario.getRol() == Role.ADMIN;

        if (!esPropietario && !esAdmin) {
            throw new AccessDeniedException(
                "You cannot edit this book."
            );
        }
    }

    // GET: Display editing form
    @GetMapping("/libro/{id}/editar")
    public String mostrarFormulario(
            @PathVariable Long id,
            Principal principal,
            Model model) {

        User usuario = obtenerUsuarioActual(principal);

        BookResponse libro = bookService.obtenerPorId(id);

        verificarPermiso(libro, usuario);

        BookRequest request = new BookRequest();

        request.setTitulo(libro.getTitulo());
        request.setAutor(libro.getAutor());
        request.setPortadaUrl(libro.getPortadaUrl());
        request.setAnio(libro.getAnio());
        request.setGenero(libro.getGenero());
        request.setSinopsis(libro.getSinopsis());

        model.addAttribute("libro", libro);
        model.addAttribute("bookRequest", request);
        model.addAttribute("currentUser", usuario);

        return "editar-libro";
    }

    // POST: Update book
    @PostMapping("/libro/{id}/editar")
    public String actualizarLibro(
            @PathVariable Long id,

            @Valid @ModelAttribute("bookRequest")
            BookRequest request,

            BindingResult result,

            Principal principal,

            Model model,

            RedirectAttributes redirectAttributes) {

        User usuario = obtenerUsuarioActual(principal);

        // Check permissions before processing
        BookResponse libro = bookService.obtenerPorId(id);

        verificarPermiso(libro, usuario);

        if (result.hasErrors()) {

            model.addAttribute("libro", libro);
            model.addAttribute("currentUser", usuario);

            return "editar-libro";
        }

        bookService.actualizar(id, request, usuario);

        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Book successfully updated!"
        );

        return "redirect:/libro/" + id;
    }
}