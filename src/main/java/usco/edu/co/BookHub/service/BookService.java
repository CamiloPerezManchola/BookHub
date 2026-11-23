package usco.edu.co.BookHub.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usco.edu.co.BookHub.dto.request.BookRequest;
import usco.edu.co.BookHub.dto.response.BookResponse;
import usco.edu.co.BookHub.exception.ResourceNotFoundException;
import usco.edu.co.BookHub.model.Book;
import usco.edu.co.BookHub.model.Role;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.BookRepository;
import usco.edu.co.BookHub.repository.ChapterBookmarkRepository;
import usco.edu.co.BookHub.repository.ChapterRepository;
import usco.edu.co.BookHub.repository.FavoriteRepository;
import usco.edu.co.BookHub.repository.ReadingProgressRepository;
import usco.edu.co.BookHub.repository.ReviewRepository;
import usco.edu.co.BookHub.repository.SavedBookRepository;
import usco.edu.co.BookHub.repository.UserRepository;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final ChapterService chapterService;
    private final ChapterRepository chapterRepository;
    private final ChapterBookmarkRepository chapterBookmarkRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final FavoriteRepository favoriteRepository;
    private final SavedBookRepository savedBookRepository;
    private final ReviewRepository reviewRepository;

    public BookService(
            BookRepository bookRepository,
            ChapterService chapterService,
            ChapterRepository chapterRepository,
            ChapterBookmarkRepository chapterBookmarkRepository,
            ReadingProgressRepository readingProgressRepository,
            FavoriteRepository favoriteRepository,
            SavedBookRepository savedBookRepository,
            ReviewRepository reviewRepository) {

        this.bookRepository = bookRepository;
        this.chapterService = chapterService;
        this.chapterRepository = chapterRepository;
        this.chapterBookmarkRepository = chapterBookmarkRepository;
        this.readingProgressRepository = readingProgressRepository;
        this.favoriteRepository = favoriteRepository;
        this.savedBookRepository = savedBookRepository;
        this.reviewRepository = reviewRepository;
    }

    private BookResponse convertirAResponse(Book book) {

        BookResponse response = new BookResponse();

        response.setId(book.getId());
        response.setTitulo(book.getTitulo());
        response.setAutor(book.getAutor());
        response.setPortadaUrl(book.getPortadaUrl());
        response.setAnio(book.getAnio());
        response.setGenero(book.getGenero());
        response.setSinopsis(book.getSinopsis());

        response.setPublisherId(book.getPublisher().getId());
        response.setPublisherName(book.getPublisher().getNombre());

        return response;
    }

    private void verificarPermiso(Book book, User user) {

        boolean esAdmin = user.getRol() == Role.ADMIN;

        boolean esPropietario = book.getPublisher() != null
            && book.getPublisher().getId().equals(user.getId());

        if (!esAdmin && !esPropietario) {
            throw new AccessDeniedException(
                "No tienes permiso para modificar este libro"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<BookResponse> obtenerTodos() {

        return bookRepository.findAll()
            .stream()
            .map(this::convertirAResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse obtenerPorId(Long id) {

        Book book = bookRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Book", id)
            );

        return convertirAResponse(book);
    }

    // Create book and its chapters in one transaction
    public BookResponse crear(BookRequest request, User publisher) {

        if (request.getCapitulos() == null
                || request.getCapitulos().isEmpty()) {

            throw new IllegalArgumentException(
                "You must provide at least one chapter."
            );
        }

        Book book = new Book();

        book.setTitulo(request.getTitulo());
        book.setAutor(request.getAutor());
        book.setPortadaUrl(request.getPortadaUrl());
        book.setAnio(request.getAnio());
        book.setGenero(request.getGenero());
        book.setSinopsis(request.getSinopsis());
        book.setPublisher(publisher);

        Book guardado = bookRepository.save(book);

        chapterService.guardarCapitulos(
            guardado,
            request.getCapitulos()
        );

        return convertirAResponse(guardado);
    }

    // Update book and optionally replace its chapters
    public BookResponse actualizar(
            Long id,
            BookRequest request,
            User currentUser) {

        Book book = bookRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Book", id)
            );

        verificarPermiso(book, currentUser);

        book.setTitulo(request.getTitulo());
        book.setAutor(request.getAutor());
        book.setPortadaUrl(request.getPortadaUrl());
        book.setAnio(request.getAnio());
        book.setGenero(request.getGenero());
        book.setSinopsis(request.getSinopsis());

        // Only replace chapters when the request includes them
        if (request.getCapitulos() != null) {

            chapterService.actualizarCapitulos(
                book,
                request.getCapitulos()
            );
        }

        Book actualizado = bookRepository.save(book);

        return convertirAResponse(actualizado);
    }

    public void eliminar(Long id, User currentUser) {

        Book book = bookRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Book", id)
            );

        // Verify ownership or administrator permissions
        verificarPermiso(book, currentUser);

        // 1. Delete chapter bookmarks
        chapterBookmarkRepository.deleteByChapter_Book_Id(id);

        // 2. Delete reading progress
        readingProgressRepository.deleteByBook_Id(id);

        // 3. Delete reviews
        reviewRepository.deleteByBook_Id(id);

        // 4. Delete favorites
        favoriteRepository.deleteByBook_Id(id);

        // 5. Delete saved books
        savedBookRepository.deleteByBook_Id(id);

        // 6. Delete chapters
        chapterRepository.deleteAll(
            chapterRepository.findByBook_IdOrderByNumeroAsc(id)
        );

        // 7. Finally delete the book
        bookRepository.delete(book);
    }
}
