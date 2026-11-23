package usco.edu.co.BookHub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChapterRequest {

    @NotBlank(message = "Chapter title is required")
    @Size(max = 200)
    private String titulo;

    @NotBlank(message = "Chapter content is required")
    private String contenido;
}