package usco.edu.co.BookHub.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import usco.edu.co.BookHub.dto.response.LibraryReadingResponse;
import usco.edu.co.BookHub.dto.response.StatisticsResponse;
import usco.edu.co.BookHub.model.User;
import usco.edu.co.BookHub.repository.ReviewRepository;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private final LibraryService libraryService;
    private final ReviewRepository reviewRepository;

    public StatisticsService(
            LibraryService libraryService,
            ReviewRepository reviewRepository) {

        this.libraryService = libraryService;
        this.reviewRepository = reviewRepository;
    }

    public StatisticsResponse obtenerEstadisticas(User user) {

        StatisticsResponse response = new StatisticsResponse();

        // Get the same reading information used by My Library
        List<LibraryReadingResponse> lecturas =
            libraryService.obtenerLecturas(user);

        // Only completed books
        List<LibraryReadingResponse> completados =
            lecturas.stream()
                .filter(LibraryReadingResponse::isCompleted)
                .toList();

        // Total completed books
        response.setTotalCompletados(
            (long) completados.size()
        );

        // Average rating given by the user
        Double average =
            reviewRepository.calculateUserAverageRating(
                user.getId()
            );

        response.setPromedioCalificaciones(
            average != null
                ? Math.round(average * 100.0) / 100.0
                : 0.0
        );

        // Most-read author
        Map<String, Long> authors = completados.stream()
            .map(LibraryReadingResponse::getAutor)
            .filter(author ->
                author != null && !author.isBlank()
            )
            .collect(Collectors.groupingBy(
                author -> author,
                Collectors.counting()
            ));

        authors.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .ifPresentOrElse(entry -> {
                response.setAutorMasLeido(entry.getKey());
                response.setCantidadLibrosAutor(entry.getValue());
            }, () -> {
                response.setAutorMasLeido("Sin datos");
                response.setCantidadLibrosAutor(0L);
            });

        // Most-read genres
        Map<String, Long> genres = completados.stream()
            .map(LibraryReadingResponse::getGenero)
            .filter(genre ->
                genre != null && !genre.isBlank()
            )
            .collect(Collectors.groupingBy(
                String::trim,
                Collectors.counting()
            ));

        List<Map.Entry<String, Long>> sortedGenres =
            new ArrayList<>(genres.entrySet());

        sortedGenres.sort(
            Map.Entry.<String, Long>comparingByValue()
                .reversed()
                .thenComparing(Map.Entry::getKey)
        );

        List<String> genreNames = new ArrayList<>();
        List<Long> genreCounts = new ArrayList<>();

        for (Map.Entry<String, Long> entry : sortedGenres) {
            genreNames.add(entry.getKey());
            genreCounts.add(entry.getValue());
        }

        response.setGeneros(genreNames);
        response.setCantidadesPorGenero(genreCounts);

        return response;
    }
}
