package usco.edu.co.BookHub.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usco.edu.co.BookHub.dto.request.ReadingProgressRequest;
import usco.edu.co.BookHub.dto.response.ReadingProgressResponse;
import usco.edu.co.BookHub.exception.ResourceNotFoundException;
import usco.edu.co.BookHub.model.*;
import usco.edu.co.BookHub.repository.*;

@Service
@Transactional
public class ReadingProgressService {

    private final ReadingProgressRepository progressRepository;
    private final BookRepository bookRepository;
    private final ChapterRepository chapterRepository;

    public ReadingProgressService(
            ReadingProgressRepository progressRepository,
            BookRepository bookRepository,
            ChapterRepository chapterRepository) {

        this.progressRepository = progressRepository;
        this.bookRepository = bookRepository;
        this.chapterRepository = chapterRepository;
    }

    public ReadingProgressResponse obtenerProgreso(
            Long bookId,
            User user) {

        ReadingProgress progress = progressRepository
            .findByUser_IdAndBook_Id(user.getId(), bookId)
            .orElse(null);

        if (progress == null) {
            return null;
        }

        return convertirAResponse(progress);
    }

    public ReadingProgressResponse guardarProgreso(
            Long bookId,
            ReadingProgressRequest request,
            User user) {

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Book", bookId)
            );

        Chapter chapter = chapterRepository
            .findById(request.getChapterId())
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Chapter",
                    request.getChapterId()
                )
            );

        if (!chapter.getBook().getId().equals(bookId)) {
            throw new IllegalArgumentException(
                "The chapter does not belong to this book."
            );
        }

        ReadingProgress progress = progressRepository
            .findByUser_IdAndBook_Id(user.getId(), bookId)
            .orElseGet(ReadingProgress::new);

        progress.setUser(user);
        progress.setBook(book);
        progress.setChapter(chapter);
        progress.setScrollPercentage(
            request.getScrollPercentage()
        );

        return convertirAResponse(
            progressRepository.save(progress)
        );
    }

    private ReadingProgressResponse convertirAResponse(
            ReadingProgress progress) {

        ReadingProgressResponse response =
            new ReadingProgressResponse();

        response.setBookId(progress.getBook().getId());
        response.setChapterId(progress.getChapter().getId());
        response.setChapterNumber(
            progress.getChapter().getNumero()
        );
        response.setScrollPercentage(
            progress.getScrollPercentage()
        );

        return response;
    }
}
