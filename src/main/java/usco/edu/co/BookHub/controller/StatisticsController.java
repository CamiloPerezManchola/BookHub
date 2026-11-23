package usco.edu.co.BookHub.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import usco.edu.co.BookHub.repository.UserRepository;
import usco.edu.co.BookHub.service.StatisticsService;

@Controller
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserRepository userRepository;

    public StatisticsController(
            StatisticsService statisticsService,
            UserRepository userRepository) {

        this.statisticsService = statisticsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(
            Authentication authentication,
            Model model) {

        String identifier = authentication.getName();

        var user = userRepository
            .findByUsernameIgnoreCaseOrEmailIgnoreCase(
                identifier,
                identifier
            )
            .orElseThrow(() ->
                new IllegalStateException("User not found")
            );

        model.addAttribute(
            "estadisticas",
            statisticsService.obtenerEstadisticas(user)
        );

        return "estadisticas";
    }
}
