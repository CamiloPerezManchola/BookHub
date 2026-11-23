package usco.edu.co.BookHub.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewResponse {

    private Long id;

    private Integer rating;

    private String comentario;

    private LocalDateTime fechaCreacion;

    private Long userId;

    private String username;

    private String nombre;

    private String fotoPerfilUrl;

    private Long bookId;

    private String bookTitle;
}