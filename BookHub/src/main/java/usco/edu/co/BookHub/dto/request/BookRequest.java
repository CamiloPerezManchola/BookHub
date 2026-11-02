package usco.edu.co.BookHub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BookRequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    private String autor;

    @Pattern(
        regexp = "^(https?://.*)?$",
        message = "La portada debe ser una URL válida"
    )
    private String portadaUrl;

    @NotNull(message = "El año es obligatorio")
    @Min(value = 0, message = "El año no puede ser negativo")
    @Max(value = 2026, message = "El año no puede ser futuro")
    private Integer anio;

    @NotBlank(message = "El género es obligatorio")
    private String genero;

    @NotBlank(message = "La sinopsis es obligatoria")
    private String sinopsis;

}