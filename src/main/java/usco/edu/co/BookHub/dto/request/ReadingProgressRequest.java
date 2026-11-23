package usco.edu.co.BookHub.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReadingProgressRequest {

    @NotNull
    @Positive
    private Long chapterId;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double scrollPercentage;
}
