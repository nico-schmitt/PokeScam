package com.PokeScam.PokeScam.Controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Services.AdminService;
import com.PokeScam.PokeScam.Services.BoxService;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;

import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.RequestBody;


@PreAuthorize("hasRole('ADMIN')")
@Controller
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String admin(Model m) {
        m.addAttribute("user", new User());
        return "admin";
    }

    @PostMapping("/admin/ban")
    public String banUser(Model m, @ModelAttribute User userToBan, RedirectAttributes redirectAttributes) {
        NotificationMsg notifMsg = adminService.banUser(userToBan.getUsername());
        redirectAttributes.addFlashAttribute("notifMsg", notifMsg);
        return "redirect:/admin";
    }
}