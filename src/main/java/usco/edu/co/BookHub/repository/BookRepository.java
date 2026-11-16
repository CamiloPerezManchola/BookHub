package usco.edu.co.BookHub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import usco.edu.co.BookHub.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByPublisher_Id(Long publisherId);

    @Query("""
        SELECT DISTINCT b.genero
        FROM Book b
        WHERE b.genero IS NOT NULL
        AND TRIM(b.genero) <> ''
        ORDER BY b.genero
    """)
    List<String> findDistinctGenres();

    @Query("SELECT MIN(b.anio) FROM Book b")
    Integer findMinimumYear();

    @Query("SELECT MAX(b.anio) FROM Book b")
    Integer findMaximumYear();
}