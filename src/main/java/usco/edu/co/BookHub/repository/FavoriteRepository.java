package usco.edu.co.BookHub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import usco.edu.co.BookHub.model.Book;
import usco.edu.co.BookHub.model.Favorite;

public interface FavoriteRepository
        extends JpaRepository<Favorite, Long> {

    long countByBook_Id(Long bookId);

    boolean existsByUser_IdAndBook_Id(
        Long userId, Long bookId
    );

    void deleteByUser_IdAndBook_Id(
        Long userId, Long bookId
    );

    @Query("""
        SELECT f.book
        FROM Favorite f
        WHERE f.user.id = :userId
    """)
    List<Book> findBooksByUserId(
        @Param("userId") Long userId
    );

    void deleteByBook_Id(Long bookId);
}