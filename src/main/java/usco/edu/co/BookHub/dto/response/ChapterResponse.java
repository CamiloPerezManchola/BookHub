package usco.edu.co.BookHub.dto.response;

import lombok.Data;

@Data
public class ChapterResponse {

    private Long id;

    private Long bookId;

    private String titulo;

    private Integer numero;

    private String contenido;

    private boolean demoContent;
}
