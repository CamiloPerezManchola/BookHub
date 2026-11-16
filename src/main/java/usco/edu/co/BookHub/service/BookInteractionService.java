package usco.edu.co.BookHub.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import usco.edu.co.BookHub.model.*;
import usco.edu.co.BookHub.repository.*;

@Service
@RequiredArgsConstructor
public class BookInteractionService {

    private final FavoriteRepository favoriteRepository;
    private final SavedBookRepository savedBookRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public boolean toggleFavorite(Long userId, Long bookId) {

        if (favoriteRepository.existsByUser_IdAndBook_Id(
                userId, bookId)) {

            favoriteRepository.deleteByUser_IdAndBook_Id(
                userId, bookId
            );

            return false;
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() ->
                new RuntimeException("User not found"));

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() ->
                new RuntimeException("Book not found"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setBook(book);

        favoriteRepository.save(favorite);

        return true;
    }

    @Transactional
    public boolean toggleSaved(Long userId, Long bookId) {

        if (savedBookRepository.existsByUser_IdAndBook_Id(
                userId, bookId)) {

            savedBookRepository.deleteByUser_IdAndBook_Id(
                userId, bookId
            );

            return false;
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() ->
                new RuntimeException("User not found"));

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() ->
                new RuntimeException("Book not found"));

        SavedBook saved = new SavedBook();
        saved.setUser(user);
        saved.setBook(book);

        savedBookRepository.save(saved);

        return true;
    }

    @Transactional(readOnly = true)
    public long countFavorites(Long bookId) {
        return favoriteRepository.countByBook_Id(bookId);
    }

    @Transactional(readOnly = true)
    public long countSaved(Long bookId) {
        return savedBookRepository.countByBook_Id(bookId);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long bookId) {
        return favoriteRepository.existsByUser_IdAndBook_Id(
            userId, bookId
        );
    }

    @Transactional(readOnly = true)
    public java.util.List<Book> getFavoriteBooks(Long userId) {
        return favoriteRepository.findBooksByUserId(userId);
    }

    @Transactional(readOnly = true)
    public java.util.List<Book> getSavedBooks(Long userId) {
        return savedBookRepository.findBooksByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isSaved(Long userId, Long bookId) {
        return savedBookRepository.existsByUser_IdAndBook_Id(
            userId, bookId
        );
    }
}