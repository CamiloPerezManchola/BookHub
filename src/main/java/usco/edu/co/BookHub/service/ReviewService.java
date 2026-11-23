package usco.edu.co.BookHub.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usco.edu.co.BookHub.dto.request.ReviewRequest;
import usco.edu.co.BookHub.dto.response.ReviewResponse;
import usco.edu.co.BookHub.exception.ResourceNotFoundException;
import usco.edu.co.BookHub.model.Book;
import usco.edu.co.BookHub.model.Review;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.model.Role;
import usco.edu.co.BookHub.repository.BookRepository;
import usco.edu.co.BookHub.repository.ReviewRepository;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookRepository bookRepository) {

        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
    }

    private ReviewResponse convertirAResponse(Review review) {

        ReviewResponse response = new ReviewResponse();

        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComentario(review.getComentario());
        response.setFechaCreacion(review.getFechaCreacion());

        response.setBookId(review.getBook().getId());
        response.setBookTitle(review.getBook().getTitulo());

        response.setUserId(review.getUser().getId());
        response.setUsername(review.getUser().getUsername());
        response.setNombre(review.getUser().getNombre());
        response.setFotoPerfilUrl(
            review.getUser().getFotoPerfilUrl()
        );

        return response;
    }

    // All community reviews
    @Transactional(readOnly = true)
    public List<ReviewResponse> obtenerPorLibro(Long bookId) {

        return reviewRepository
            .findByBookIdOrderByFechaCreacionDesc(bookId)
            .stream()
            .map(this::convertirAResponse)
            .toList();
    }

    // Current user's reviews
    @Transactional(readOnly = true)
    public List<ReviewResponse> obtenerMisResenas(
            Long bookId, Long userId) {

        return reviewRepository
            .findByBookIdAndUserIdOrderByFechaCreacionDesc(
                bookId, userId
            )
            .stream()
            .map(this::convertirAResponse)
            .toList();
    }

    // Create review
    public ReviewResponse crear(
            Long bookId,
            ReviewRequest request,
            User user) {

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Book", bookId)
            );

        Review review = new Review();

        review.setBook(book);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComentario(request.getComentario());

        return convertirAResponse(
            reviewRepository.save(review)
        );
    }

    // Edit review
    public ReviewResponse actualizar(
            Long reviewId,
            ReviewRequest request,
            User user) {

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Review", reviewId)
            );

        verificarPermiso(review, user);

        review.setRating(request.getRating());
        review.setComentario(request.getComentario());

        return convertirAResponse(
            reviewRepository.save(review)
        );
    }

    // Delete review
    public void eliminar(Long reviewId, User user) {

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Review", reviewId)
            );

        verificarPermiso(review, user);

        reviewRepository.delete(review);
    }

    private void verificarPermiso(
            Review review,
            User user) {

        boolean esAdmin = user.getRol() == Role.ADMIN;

        boolean esPropietario =
            review.getUser().getId().equals(user.getId());

        if (!esAdmin && !esPropietario) {
            throw new AccessDeniedException(
                "You cannot modify this review."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> obtenerTodas() {

        return reviewRepository.findAll()
            .stream()
            .map(this::convertirAResponse)
            .toList();
    }

    public void eliminarComoAdmin(
            Long reviewId,
            User currentUser) {

        if (currentUser.getRol() != Role.ADMIN) {
            throw new AccessDeniedException(
                "Administrator permissions required."
            );
        }

        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Review", reviewId)
            );

        reviewRepository.delete(review);
    }

    public Double getAverageRating(Long bookId) {
        Double average = reviewRepository.calculateAverageRating(bookId);
        return average != null ? average : 0.0;
    }

    public Long getReviewCount(Long bookId) {
        return reviewRepository.countReviewsByBookId(bookId);
    }
}