package com.PokeScam.PokeScam.Controllers;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.PokemonDataService;

import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping("/api")
public class APIController {

    private final PokemonDataService pkmnDataService;

    public APIController(PokemonDataService pkmnData) {
        this.pkmnDataService = pkmnData;
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