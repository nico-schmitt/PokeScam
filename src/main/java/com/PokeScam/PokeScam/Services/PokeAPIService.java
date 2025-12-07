package com.PokeScam.PokeScam.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

@Service
public class PokeAPIService {
    private final String pokeAPIBaseURL = "https://pokeapi.co/api/v2";
    private final WebClient webClientBuilder;


    public PokeAPIService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder.baseUrl(pokeAPIBaseURL).build();
    }

    public Mono<String> getPokemon(String name) {
        return webClientBuilder
            .get().uri("/pokemon/"+name).retrieve().bodyToMono(JsonNode.class)
            .map(json->json.at("/sprites/front_default")
            .asString());
    }
}
