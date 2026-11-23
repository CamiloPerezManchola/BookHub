package usco.edu.co.BookHub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "capitulos",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"libro_id", "numero"}
        )
    }
)
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Book book;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false)
    private Integer numero;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String contenido;

    @Column(name = "contenido_demo", nullable = false)
    private boolean demoContent = false;
}
