package usco.edu.co.BookHub.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import usco.edu.co.BookHub.dto.response.ChapterResponse;
import usco.edu.co.BookHub.service.ChapterService;

@RestController
@RequestMapping("/api/books/{bookId}/chapters")
public class ChapterRestController {

    private final ChapterService chapterService;

    public ChapterRestController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }

    @GetMapping
    public List<ChapterResponse> obtenerCapitulos(
            @PathVariable Long bookId) {

        return chapterService.obtenerCapitulos(bookId);
    }
}
