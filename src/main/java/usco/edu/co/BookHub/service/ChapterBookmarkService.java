package usco.edu.co.BookHub.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usco.edu.co.BookHub.dto.response.ChapterBookmarkResponse;
import usco.edu.co.BookHub.exception.ResourceNotFoundException;
import usco.edu.co.BookHub.model.*;
import usco.edu.co.BookHub.repository.*;

@Service
@Transactional
public class ChapterBookmarkService {

    private final ChapterBookmarkRepository bookmarkRepository;
    private final ChapterRepository chapterRepository;
    private final BookRepository bookRepository;

    public ChapterBookmarkService(
            ChapterBookmarkRepository bookmarkRepository,
            ChapterRepository chapterRepository,
            BookRepository bookRepository) {

        this.bookmarkRepository = bookmarkRepository;
        this.chapterRepository = chapterRepository;
        this.bookRepository = bookRepository;
    }

    public List<ChapterBookmarkResponse> obtenerBookmarks(
            Long bookId,
            User user) {

        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book", bookId);
        }

        return bookmarkRepository
            .findByUser_IdAndChapter_Book_Id(
                user.getId(), bookId
            )
            .stream()
            .map(bookmark -> {

                Chapter chapter = bookmark.getChapter();

                ChapterBookmarkResponse response =
                    new ChapterBookmarkResponse();

                response.setChapterId(chapter.getId());
                response.setChapterNumber(chapter.getNumero());
                response.setChapterTitle(chapter.getTitulo());

                return response;
            })
            .toList();
    }

    public void agregarBookmark(
            Long bookId,
            Long chapterId,
            User user) {

        Chapter chapter = obtenerCapituloDelLibro(
            bookId, chapterId
        );

        if (bookmarkRepository.existsByUser_IdAndChapter_Id(
                user.getId(), chapterId)) {
            return;
        }

        ChapterBookmark bookmark = new ChapterBookmark();

        bookmark.setUser(user);
        bookmark.setChapter(chapter);

        bookmarkRepository.save(bookmark);
    }

    public void eliminarBookmark(
            Long bookId,
            Long chapterId,
            User user) {

        obtenerCapituloDelLibro(bookId, chapterId);

        bookmarkRepository
            .findByUser_IdAndChapter_Id(
                user.getId(), chapterId
            )
            .ifPresent(bookmarkRepository::delete);
    }

    private Chapter obtenerCapituloDelLibro(
            Long bookId,
            Long chapterId) {

        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Chapter", chapterId)
            );

        if (!chapter.getBook().getId().equals(bookId)) {
            throw new IllegalArgumentException(
                "The chapter does not belong to this book."
            );
        }

        return chapter;
    }
}
