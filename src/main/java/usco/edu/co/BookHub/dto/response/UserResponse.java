package usco.edu.co.BookHub.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponse {

    private Long id;

    private String nombre;

    private String username;

    private String email;

    private String descripcion;

    private String fotoPerfilUrl;

    private String rol;

    private LocalDateTime fechaRegistro;
}
