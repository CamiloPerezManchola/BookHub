package usco.edu.co.BookHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import usco.edu.co.BookHub.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
