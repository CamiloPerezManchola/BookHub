package usco.edu.co.BookHub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @Size(max = 500)
    private String descripcion;

    private String fotoPerfilUrl;

    @NotNull
    private String rol;

    private String password;
}