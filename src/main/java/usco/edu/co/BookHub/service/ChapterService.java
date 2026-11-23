package usco.edu.co.BookHub.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usco.edu.co.BookHub.dto.request.ChapterRequest;
import usco.edu.co.BookHub.dto.response.ChapterResponse;
import usco.edu.co.BookHub.exception.ResourceNotFoundException;
import usco.edu.co.BookHub.model.Book;
import usco.edu.co.BookHub.model.Chapter;
import usco.edu.co.BookHub.repository.BookRepository;
import usco.edu.co.BookHub.repository.ChapterRepository;

@Service
@Transactional
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final BookRepository bookRepository;

    public ChapterService(
            ChapterRepository chapterRepository,
            BookRepository bookRepository) {

        this.chapterRepository = chapterRepository;
        this.bookRepository = bookRepository;
    }

    private ChapterResponse convertirAResponse(Chapter chapter) {

        ChapterResponse response = new ChapterResponse();

        response.setId(chapter.getId());
        response.setBookId(chapter.getBook().getId());
        response.setTitulo(chapter.getTitulo());
        response.setNumero(chapter.getNumero());
        response.setContenido(chapter.getContenido());
        response.setDemoContent(chapter.isDemoContent());

        return response;
    }

    // Save chapters supplied by the author
    public void guardarCapitulos(
            Book book,
            List<ChapterRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException(
                "A book must contain at least one chapter."
            );
        }

        List<Chapter> chapters = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {

            ChapterRequest request = requests.get(i);

            Chapter chapter = new Chapter();

            chapter.setBook(book);
            chapter.setNumero(i + 1);
            chapter.setTitulo(request.getTitulo().trim());
            chapter.setContenido(request.getContenido().trim());
            chapter.setDemoContent(false);

            chapters.add(chapter);
        }

        chapterRepository.saveAll(chapters);
    }

    // Replace existing chapters with the author's new chapters
    public void actualizarCapitulos(
            Book book,
            List<ChapterRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException(
                "A book must contain at least one chapter."
            );
        }

        chapterRepository.deleteAll(
            chapterRepository.findByBook_IdOrderByNumeroAsc(
                book.getId()
            )
        );

        guardarCapitulos(book, requests);
    }

    // Automatically create sample chapters if none exist
    public void crearContenidoDemoSiEsNecesario(Long bookId) {

        if (chapterRepository.existsByBook_Id(bookId)) {
            return;
        }

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Book", bookId)
            );

        String[] titles = {
            "The Beginning",
            "A New Perspective",
            "The Journey Continues"
        };

        String[] contents = {

            "Every story begins somewhere. " +
            "In this introductory chapter, we explore the world " +
            "surrounding " + book.getTitulo() + ".\n\n" +
            "This is demonstration content created by BookHub.",

            "Every journey brings new questions and discoveries. " +
            "Readers can experience the reading interface and " +
            "explore its features.\n\n" +
            "This sample text is illustrative and does not represent " +
            "the original work by " + book.getAutor() + ".",

            "As the story continues, new possibilities emerge. " +
            "This demonstration chapter allows readers to test " +
            "chapter navigation and reading progress."
        };

        List<Chapter> chapters = new ArrayList<>();

        for (int i = 0; i < titles.length; i++) {

            Chapter chapter = new Chapter();

            chapter.setBook(book);
            chapter.setNumero(i + 1);
            chapter.setTitulo(titles[i]);
            chapter.setContenido(contents[i]);
            chapter.setDemoContent(true);

            chapters.add(chapter);
        }

        chapterRepository.saveAll(chapters);
    }

    @Transactional
    public List<ChapterResponse> obtenerCapitulos(Long bookId) {

        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book", bookId);
        }

        crearContenidoDemoSiEsNecesario(bookId);

        return chapterRepository
            .findByBook_IdOrderByNumeroAsc(bookId)
            .stream()
            .map(this::convertirAResponse)
            .toList();
    }
}
