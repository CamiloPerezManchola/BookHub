package usco.edu.co.BookHub.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String password = "Carlos123!";

        String hash = encoder.encode(password);

        System.out.println(hash);
    }
}