package usco.edu.co.BookHub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.edu.co.BookHub.model.ChapterBookmark;

@Repository
public interface ChapterBookmarkRepository
        extends JpaRepository<ChapterBookmark, Long> {

    List<ChapterBookmark> findByUser_IdAndChapter_Book_Id(
        Long userId,
        Long bookId
    );

    Optional<ChapterBookmark> findByUser_IdAndChapter_Id(
        Long userId,
        Long chapterId
    );

    boolean existsByUser_IdAndChapter_Id(
        Long userId,
        Long chapterId
    );

    void deleteByChapter_Book_Id(Long bookId);
}