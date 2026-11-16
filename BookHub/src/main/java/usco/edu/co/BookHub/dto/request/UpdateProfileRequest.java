package usco.edu.co.BookHub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    @Pattern(
        regexp = "^(https?://.*)?$",
        message = "La foto debe ser una URL válida"
    )
    private String fotoPerfilUrl;
}
