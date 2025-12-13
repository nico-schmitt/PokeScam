package com.PokeScam.PokeScam.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class PokemonController {

    private final PokemonDataService pkmnDataService;
    private final PokeAPIService pokeAPIService;

    public PokemonController(PokemonDataService pkmnData, PokeAPIService pokeAPIService) {
        this.pkmnDataService = pkmnData;
        this.pokeAPIService = pokeAPIService;
    }

    @DeleteMapping("/pokemon/{id}")
    public String releasePokemon(@PathVariable int id, @RequestHeader(name="Referer", defaultValue = "/") String referer) {
        pkmnDataService.deletePkmn(id);
        return "redirect:" + referer;
    }

    @GetMapping("/pokemon/{id}")
    @ResponseBody
    public String getMethodName(@PathVariable int id) {
        return Integer.toString(id);
    }
    
}