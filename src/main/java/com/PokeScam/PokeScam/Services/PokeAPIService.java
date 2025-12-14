package com.PokeScam.PokeScam.Services;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.LocaleResolver;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Pokemon;

import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

@Service
public class PokeAPIService {
    private final String pokeAPIBaseURL = "https://pokeapi.co/api/v2";
    private final WebClient webClient;

    public PokeAPIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(pokeAPIBaseURL).build();
    }

    public PokemonDTO populatePokemonDTO(Pokemon pkmnToUse) {
        PokemonAPIDTOHelper apiData = getPokeAPIData(pkmnToUse.getName());
        return new PokemonDTO(
            pkmnToUse.getId(),
            pkmnToUse.isInBox(),
            apiData.name,
            apiData.spriteURL,
            apiData.flavorText
        );
    }

    private PokemonDTO populatePokemonDTO(String pkmnToUse) {
        PokemonAPIDTOHelper apiData = getPokeAPIData(pkmnToUse);
        return new PokemonDTO(
            -1,
            false,
            apiData.name,
            apiData.spriteURL,
            apiData.flavorText
        );
    }

    private PokemonAPIDTOHelper getPokeAPIData(String pkmnName) {
        String languageToUse = LocaleContextHolder.getLocale().getLanguage();

        PokeAPIDTO_PokemonData pokemonData = webClient.get().uri("/pokemon/"+pkmnName).retrieve().bodyToMono(PokeAPIDTO_PokemonData.class).block();
        PokeAPIDTO_PokemonSpeciesData pokemonSpeciesData = webClient.get().uri("/pokemon-species/"+pokemonData.species.name).retrieve().bodyToMono(PokeAPIDTO_PokemonSpeciesData.class).block();

        String name = pokemonSpeciesData.names.stream().filter(names->languageToUse.equals(names.language.name)).map(names->names.name).findFirst().orElse("kek");
        String description = pokemonSpeciesData.flavor_text_entries.stream().filter(f->languageToUse.equals(f.language.name)).map(f->f.flavor_text).findFirst().orElse("ha");
        String sprite = pokemonData.sprites.front_default;

        return new PokemonAPIDTOHelper(name, sprite, description);
    }

    public PokemonDTO getRandomPokemon() {
        return populatePokemonDTO(getRandomPokemonName());
    }

    public boolean pokemonExists(String name) {
        int statusCode = webClient.get().uri("/pokemon/"+name).exchangeToMono(res->Mono.just(res.statusCode().value())).block();
        return statusCode == 404 ? false : true;
    };

    private String getRandomPokemonName() {
        final int existingPkmnCount = 1025;
        final int randPkmnID = ThreadLocalRandom.current().nextInt(1, existingPkmnCount+1);
        return webClient
            .get().uri("/pokemon/"+randPkmnID).retrieve().bodyToMono(JsonNode.class)
            .map(json->json.at("/name").asString())
            .block();
    }

   public record PokeAPIDTO_PokemonData(Sprites sprites, Species species) {}
   public record PokeAPIDTO_PokemonSpeciesData(List<Name> names, List<FlavorText> flavor_text_entries) {}

   public record Sprites(String front_default) {}
   public record Name(Language language, String name) {}
   public record Species(String name) {}
   public record FlavorText(String flavor_text, Language language) {}
   public record Language(String name) {}

   public record PokemonAPIDTOHelper(String name, String spriteURL, String flavorText) {}
}