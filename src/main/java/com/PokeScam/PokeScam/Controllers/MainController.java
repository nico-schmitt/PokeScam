package com.PokeScam.PokeScam.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class MainController {

    private final PokemonDataService pkmnDataService;
    private final PokeAPIService pokeAPIService;

    public MainController(PokemonDataService pkmnData, PokeAPIService pokeAPIService) {
        this.pkmnDataService = pkmnData;
        this.pokeAPIService = pokeAPIService;
    }

    @GetMapping("/")
    public String home(Model m) {
        m.addAttribute("pokemon", new Pokemon());
        m.addAttribute("pkmnTeam", pkmnDataService.getPkmnTeamInfo());
        return "home";
    }

    @GetMapping("/box")
    public String box(Model m) {
        m.addAttribute("pkmnInBox", pkmnDataService.getPkmnInBox());
        return "box";
    }
    
    @GetMapping("/catch")
    public String catchRndPkmn(Model m) {
        m.addAttribute("encounterPkmn", pokeAPIService.getRandomPokemon());
        return "catchRndPkmn";
    }
    
}