package usco.edu.co.BookHub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "libros_guardados",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"user_id", "book_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class SavedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
}