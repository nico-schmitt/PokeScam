package com.PokeScam.PokeScam.Services;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Pokemon;

import tools.jackson.databind.JsonNode;

@Service
public class PokeAPIService {
    private final String pokeAPIBaseURL = "https://pokeapi.co/api/v2";
    private final WebClient webClient;

    public PokeAPIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(pokeAPIBaseURL).build();
    }

    public String getImageURL(String name) {
        return webClient
            .get().uri("/pokemon/"+name).retrieve().bodyToMono(JsonNode.class)
            .map(json->json.at("/sprites/front_default").asString())
            .block();
    }

    public PokemonDTO populatePokemonDTO(Pokemon pkmnToUse) {
        PokemonDTO pDTO = new PokemonDTO();
        pDTO.setId(pkmnToUse.getId());
        pDTO.setName(pkmnToUse.getName());
        pDTO.setImageURL(getImageURL(pkmnToUse.getName()));
        pDTO.setInBox(pkmnToUse.isInBox());
        return pDTO;
    }

    public PokemonDTO getRandomPokemon() {
        return populatePokemonDTO(getRandomPokemonName());
    }

    private PokemonDTO populatePokemonDTO(String pkmnToUse) {
        PokemonDTO pDTO = new PokemonDTO();
        pDTO.setName(pkmnToUse);
        pDTO.setImageURL(getImageURL(pkmnToUse));
        return pDTO;
    }

    private String getRandomPokemonName() {
        final int existingPkmnCount = 1025;
        final int randPkmnID = ThreadLocalRandom.current().nextInt(1, existingPkmnCount+1);
        return webClient
            .get().uri("/pokemon/"+randPkmnID).retrieve().bodyToMono(JsonNode.class)
            .map(json->json.at("/name").asString())
            .block();
    }
}
