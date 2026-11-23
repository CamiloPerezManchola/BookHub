package usco.edu.co.BookHub.dto.response;

import lombok.Data;

@Data
public class LibraryReadingResponse {

    private Long bookId;

    private String titulo;

    private String autor;

    private String genero;

    private String portadaUrl;

    private Long chapterId;

    private Integer chapterNumber;

    private String chapterTitle;

    private Double progressPercentage;

    private boolean completed;
}