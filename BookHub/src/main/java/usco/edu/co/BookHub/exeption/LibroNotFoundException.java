package usco.edu.co.BookHub.exeption;

public class LibroNotFoundException extends RuntimeException {

    public LibroNotFoundException(Long id) {
        super("No se encontró un libro con el ID: " + id);
    }

}