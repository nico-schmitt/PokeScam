package com.PokeScam.PokeScam.Controllers;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import com.PokeScam.PokeScam.Services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.PokedexService;

import reactor.core.publisher.Mono;

@Controller
public class CatchController {

    private final PokemonDataService pkmnDataService;
    private final PokeAPIService pokeAPIService;
    private final PokedexService pokedexService;
    private final CustomUserDetails customUserDetails;
    private final UserService userService;

    public CatchController(
            PokemonDataService pkmnData,
            PokeAPIService pokeAPIService,
            PokedexService pokedexService,
            CustomUserDetails customUserDetails,
            UserService userService) {
        this.pkmnDataService = pkmnData;
        this.pokeAPIService = pokeAPIService;
        this.pokedexService = pokedexService;
        this.customUserDetails = customUserDetails;
        this.userService = userService;
    }

    @GetMapping("/catch")
    public String catchRndPkmn(Model m) {
        PokemonDTO rndPokemon = pokeAPIService.getRandomPokemon();

        User user = customUserDetails.getThisUser();

        // MARK AS SEEN
        pokedexService.markSeen(user, rndPokemon.apiName());

        userService.updateRecentActivity(user, "Catching Pokemon");

        m.addAttribute("encounterPkmn", rndPokemon);
        return "catchRndPkmn";
    }

    @PostMapping("/tryCatchPkmn")
    @ResponseBody
    public Mono<CatchResponse> isCatchSuccessful(
            @RequestBody PokemonDTO pkmnToCatchData) throws InterruptedException {

        final long catchDelay = ThreadLocalRandom.current().nextLong(100, 1500);
        final float catchRate = 0.5f;

        boolean catchSuccessful = ThreadLocalRandom.current().nextFloat() < catchRate;

        String addMsg = "";

        if (catchSuccessful) {
            addMsg = pkmnDataService.addPokemon(pkmnToCatchData);

            User user = customUserDetails.getThisUser();

            // MARK AS CAUGHT
            pokedexService.markCaught(user, pkmnToCatchData.apiName());
        }

        return Mono.delay(Duration.ofMillis(catchDelay))
                .thenReturn(new CatchResponse(catchSuccessful, addMsg));
    }

    record CatchResponse(boolean catchSuccessful, String addMsg) {
    }
}
