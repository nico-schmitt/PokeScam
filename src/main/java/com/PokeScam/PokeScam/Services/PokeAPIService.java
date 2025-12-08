package com.PokeScam.PokeScam.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Pokemon;

import tools.jackson.databind.JsonNode;

@Service
public class PokeAPIService {
    private final String pokeAPIBaseURL = "https://pokeapi.co/api/v2";
    private final WebClient webClientBuilder;

    public PokeAPIService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder.baseUrl(pokeAPIBaseURL).build();
    }

    public String getImageURL(String name) {
        return webClientBuilder
            .get().uri("/pokemon/"+name).retrieve().bodyToMono(JsonNode.class)
            .map(json->json.at("/sprites/front_default")
            .asString())
            .block();
    }

    public PokemonDTO populatePokemonDTO(Pokemon pkmnToUse) {
        PokemonDTO pDTO = new PokemonDTO();
        pDTO.setName(pkmnToUse.getName());
        pDTO.setImageURL(getImageURL(pkmnToUse.getName()));
        return pDTO;
    }
}
