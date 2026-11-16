
package usco.edu.co.BookHub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String nombre;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String descripcion;

    @Pattern(
        regexp = "^(https?://\\S+)?$",
        message = "Enter a valid HTTP or HTTPS image URL"
    )
    private String fotoPerfilUrl;
}
