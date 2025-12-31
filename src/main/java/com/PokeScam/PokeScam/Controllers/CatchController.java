package com.PokeScam.PokeScam.Controllers;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.catalina.startup.Catalina;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
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

    @PostMapping("/tryCatchPkmn")
    @ResponseBody
    public Mono<CatchResponse> isCatchSuccessful(@RequestBody PokemonDTO pkmnToCatchData) throws InterruptedException {
        final long catchDelay = ThreadLocalRandom.current().nextLong(100, 1500);
        final float catchRate = 0.5f;
        boolean catchSuccessful = ThreadLocalRandom.current().nextFloat() < catchRate ? true : false; 
        String addMsg = "";
        if(catchSuccessful) {
            addMsg = pkmnDataService.addPokemon(pkmnToCatchData);
        }
        return Mono.delay(Duration.ofMillis(catchDelay))
            .thenReturn(new CatchResponse(catchSuccessful, addMsg));
    }

    record CatchResponse(boolean catchSuccessful, String addMsg) {}
}