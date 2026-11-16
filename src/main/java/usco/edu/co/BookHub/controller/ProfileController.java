package usco.edu.co.BookHub.controller;

import java.security.Principal;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import usco.edu.co.BookHub.dto.request.UpdateProfileRequest;
import usco.edu.co.BookHub.service.UserService;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/perfil/editar")
    public String editarPerfil(
            Principal principal,
            Model model) {

        var perfil = userService.obtenerMiPerfil(
            principal.getName()
        );

        UpdateProfileRequest request = new UpdateProfileRequest();

        request.setNombre(perfil.getNombre());
        request.setDescripcion(perfil.getDescripcion());
        request.setFotoPerfilUrl(perfil.getFotoPerfilUrl());

        model.addAttribute("profileRequest", request);
        model.addAttribute("perfil", perfil);

        return "editar-perfil";
    }

    @PostMapping("/perfil/editar")
    public String actualizarPerfil(
            @Valid @ModelAttribute("profileRequest")
            UpdateProfileRequest request,

            BindingResult result,

            Principal principal,

            Model model,

            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {

            model.addAttribute(
                "perfil",
                userService.obtenerMiPerfil(principal.getName())
            );

            return "editar-perfil";
        }

        userService.actualizarMiPerfil(
            principal.getName(),
            request
        );

        redirectAttributes.addFlashAttribute(
            "successMessage",
            "Profile updated successfully!"
        );

        return "redirect:/perfil/editar";
    }
}
