package usco.edu.co.BookHub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import usco.edu.co.BookHub.service.BookService;
import usco.edu.co.BookHub.service.ChapterService;

@Controller
public class ReaderController {

    private final BookService bookService;
    private final ChapterService chapterService;

    public ReaderController(
            BookService bookService,
            ChapterService chapterService) {

        this.bookService = bookService;
        this.chapterService = chapterService;
    }

    @GetMapping("/libro/{id}/leer")
    public String leerLibro(
            @PathVariable Long id,
            Model model) {

        var libro = bookService.obtenerPorId(id);

        var capitulos = chapterService.obtenerCapitulos(id);

        model.addAttribute("libro", libro);
        model.addAttribute("capitulos", capitulos);

        return "lector";
    }
}
