
package usco.edu.co.BookHub.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with ID: " + id);
    }

}
