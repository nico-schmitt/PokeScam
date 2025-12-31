package com.PokeScam.PokeScam.Controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.BoxService;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;

import jakarta.validation.constraints.Min;


@Controller
public class AdminController {
    private final BoxService boxService ;
    private final PokemonDataService pkmnDataService;
    private final PokeAPIService pokeAPIService;

    public AdminController(BoxService boxService, PokemonDataService pkmnDataService, PokeAPIService pokeAPIService) {
        this.boxService = boxService;
        this.pkmnDataService = pkmnDataService;
        this.pokeAPIService = pokeAPIService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String admin(Model m) {
        return "admin";
    }
}