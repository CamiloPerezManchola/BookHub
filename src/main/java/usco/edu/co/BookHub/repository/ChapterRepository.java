package usco.edu.co.BookHub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.edu.co.BookHub.model.Chapter;

@Repository
public interface ChapterRepository
        extends JpaRepository<Chapter, Long> {

    List<Chapter> findByBook_IdOrderByNumeroAsc(Long bookId);

    boolean existsByBook_Id(Long bookId);

    long countByBook_Id(Long bookId);
}
