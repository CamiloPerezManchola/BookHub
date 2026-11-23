package usco.edu.co.BookHub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.edu.co.BookHub.model.ReadingProgress;

@Repository
public interface ReadingProgressRepository
        extends JpaRepository<ReadingProgress, Long> {

    Optional<ReadingProgress> findByUser_IdAndBook_Id(
        Long userId,
        Long bookId
    );

    List<ReadingProgress> findByUser_Id(Long userId);

    void deleteByBook_Id(Long bookId);
}
