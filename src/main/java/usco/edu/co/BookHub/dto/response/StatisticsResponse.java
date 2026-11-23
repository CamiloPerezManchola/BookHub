
package usco.edu.co.BookHub.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class StatisticsResponse {

    private Long totalCompletados;

    private Double promedioCalificaciones;

    private String autorMasLeido;

    private Long cantidadLibrosAutor;

    private List<String> generos;

    private List<Long> cantidadesPorGenero;
}
