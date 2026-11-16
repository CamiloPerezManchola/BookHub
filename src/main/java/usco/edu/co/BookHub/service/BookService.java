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

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Convert entity to DTO
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

    // Verify ownership or ADMIN role
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

    // Get all books
    @Transactional(readOnly = true)
    public List<BookResponse> obtenerTodos() {  

        return bookRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    // Get book by ID
    @Transactional(readOnly = true)
    public BookResponse obtenerPorId(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Book", id)
                );

        return convertirAResponse(book);
    }

    // Create book
    public BookResponse crear(BookRequest request, User publisher) {

        Book book = new Book();

        book.setTitulo(request.getTitulo());
        book.setAutor(request.getAutor());
        book.setPortadaUrl(request.getPortadaUrl());
        book.setAnio(request.getAnio());
        book.setGenero(request.getGenero());
        book.setSinopsis(request.getSinopsis());

        // Assign authenticated user as publisher
        book.setPublisher(publisher);

        Book guardado = bookRepository.save(book);

        return convertirAResponse(guardado);
    }

    // Update book
    public BookResponse actualizar(
            Long id, BookRequest request, User currentUser) {

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

        // Do not allow changing the publisher through this request

        Book actualizado = bookRepository.save(book);

        return convertirAResponse(actualizado);
    }

    // Delete book
    public void eliminar(Long id, User currentUser) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Book", id)
                );

        verificarPermiso(book, currentUser);

        bookRepository.delete(book);
    }
}
