package usco.edu.co.BookHub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import usco.edu.co.BookHub.model.ReadingProgress;

@Repository
public interface StatisticsRepository
        extends JpaRepository<ReadingProgress, Long> {

    // Total completed books
    @Query("""
        SELECT COUNT(rp)
        FROM ReadingProgress rp
        WHERE rp.user.id = :userId
          AND rp.scrollPercentage >= 100
          AND rp.chapter.numero = (
              SELECT MAX(c.numero)
              FROM Chapter c
              WHERE c.book.id = rp.book.id
          )
    """)
    Long countCompletedBooks(@Param("userId") Long userId);


    // Average rating given by the user
    @Query("""
        SELECT AVG(r.rating)
        FROM Review r
        WHERE r.user.id = :userId
    """)
    Double calculateUserAverageRating(
        @Param("userId") Long userId
    );


    // Most-read author (only completed books)
    @Query("""
        SELECT rp.book.autor, COUNT(rp)
        FROM ReadingProgress rp
        WHERE rp.user.id = :userId
          AND rp.scrollPercentage >= 100
          AND rp.chapter.numero = (
              SELECT MAX(c.numero)
              FROM Chapter c
              WHERE c.book.id = rp.book.id
          )
        GROUP BY rp.book.autor
        ORDER BY COUNT(rp) DESC
    """)
    List<Object[]> findMostReadAuthor(
        @Param("userId") Long userId
    );


    // Most-read genres (only completed books)
    @Query("""
        SELECT rp.book.genero, COUNT(rp)
        FROM ReadingProgress rp
        WHERE rp.user.id = :userId
          AND rp.scrollPercentage >= 100
          AND rp.chapter.numero = (
              SELECT MAX(c.numero)
              FROM Chapter c
              WHERE c.book.id = rp.book.id
          )
          AND rp.book.genero IS NOT NULL
        GROUP BY rp.book.genero
        ORDER BY COUNT(rp) DESC
    """)
    List<Object[]> findMostReadGenres(
        @Param("userId") Long userId
    );
}
