package com.PokeScam.PokeScam.Controllers;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;

import reactor.core.publisher.Mono;


@Controller
public class CatchController {

    private final PokemonDataService pkmnDataService;
    private final PokeAPIService pokeAPIService;

    public CatchController(PokemonDataService pkmnData, PokeAPIService pokeAPIService) {
        this.pkmnDataService = pkmnData;
        this.pokeAPIService = pokeAPIService;
    }


    @GetMapping("/catch")
    public String catchRndPkmn(Model m) {
        m.addAttribute("encounterPkmn", pokeAPIService.getRandomPokemon());
        return "catchRndPkmn";
    }

    @GetMapping("/tryCatchPkmn")
    @ResponseBody
    public String isCatchSuccessful(@RequestParam("name") String pkmnToCatchName) {
        final long catchDelay = ThreadLocalRandom.current().nextLong(100, 1500);
        boolean isSuccessful = ThreadLocalRandom.current().nextFloat() < 0.5 ? true : false; 
        if(isSuccessful) {
            Pokemon pkmnToAdd = new Pokemon();
            pkmnToAdd.setName(pkmnToCatchName);
            pkmnDataService.addPokemon(pkmnToAdd);
        }
        return Mono.delay(Duration.ofMillis(catchDelay)).thenReturn(Boolean.toString(isSuccessful)).block();
    }
}