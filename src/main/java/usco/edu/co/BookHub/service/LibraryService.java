package usco.edu.co.BookHub.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usco.edu.co.BookHub.dto.response.LibraryReadingResponse;
import usco.edu.co.BookHub.model.ReadingProgress;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.ChapterRepository;
import usco.edu.co.BookHub.repository.ReadingProgressRepository;

@Service
@Transactional(readOnly = true)
public class LibraryService {

    private final ReadingProgressRepository progressRepository;
    private final ChapterRepository chapterRepository;

    public LibraryService(
            ReadingProgressRepository progressRepository,
            ChapterRepository chapterRepository) {

        this.progressRepository = progressRepository;
        this.chapterRepository = chapterRepository;
    }

    public List<LibraryReadingResponse> obtenerLecturas(
            User user) {

        List<LibraryReadingResponse> readings =
            new ArrayList<>();

        List<ReadingProgress> progresses =
            progressRepository.findByUser_Id(user.getId());

        for (ReadingProgress progress : progresses) {

            var book = progress.getBook();
            var chapter = progress.getChapter();

            long totalChapters =
                chapterRepository.countByBook_Id(book.getId());

            if (totalChapters == 0) {
                continue;
            }

            double percentage =
                ((chapter.getNumero() - 1)
                + (progress.getScrollPercentage() / 100.0))
                / totalChapters * 100.0;

            percentage = Math.max(
                0,
                Math.min(100, percentage)
            );

            LibraryReadingResponse response =
                new LibraryReadingResponse();

            response.setBookId(book.getId());
            response.setTitulo(book.getTitulo());
            response.setAutor(book.getAutor());
            response.setPortadaUrl(book.getPortadaUrl());
            response.setGenero(book.getGenero());

            response.setChapterId(chapter.getId());
            response.setChapterNumber(chapter.getNumero());
            response.setChapterTitle(chapter.getTitulo());

            response.setProgressPercentage(
                Math.round(percentage * 100.0) / 100.0
            );

            response.setCompleted(percentage >= 100.0);

            readings.add(response);
        }

        return readings;
    }
}
