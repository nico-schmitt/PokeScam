package com.PokeScam.PokeScam.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.PokemonDataService;


@Controller
public class MainController {

    private final PokemonDataService pkmnDataService;

    public MainController(PokemonDataService pkmnData) {
        this.pkmnDataService = pkmnData;
    }

    @GetMapping("/")
    public String home(Model m) {
        m.addAttribute("pokemon", new Pokemon());
        m.addAttribute("pkmnTeam", pkmnDataService.getPkmnTeamInfoDTO());
        return "home";
    }
}