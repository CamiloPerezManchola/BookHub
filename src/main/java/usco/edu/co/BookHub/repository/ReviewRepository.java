package usco.edu.co.BookHub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import usco.edu.co.BookHub.model.Review;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    List<Review> findByBookIdOrderByFechaCreacionDesc(
        Long bookId
    );

    List<Review> findByBookIdAndUserIdOrderByFechaCreacionDesc(
        Long bookId,
        Long userId
    );

    @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        WHERE r.book.id = :bookId
    """)
    Double calculateAverageRating(@Param("bookId") Long bookId);

    @Query("""
        SELECT r.book.id, AVG(r.rating)
        FROM Review r
        GROUP BY r.book.id
    """)
    List<Object[]> findAverageRatings();

    @Query("""
        SELECT COUNT(r)
        FROM Review r
        WHERE r.book.id = :bookId
    """)
    Long countReviewsByBookId(@Param("bookId") Long bookId);
    
}