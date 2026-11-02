package usco.edu.co.BookHub.dto.response;

import lombok.Data;

@Data
public class BookResponse {

    private Long id;

    private String titulo;

    private String autor;

    private String portadaUrl;

    private Integer anio;

    private String genero;

    private String sinopsis;
    
    private Long publisherId;

    private String publisherName;
}