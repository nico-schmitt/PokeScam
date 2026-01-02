package com.PokeScam.PokeScam.Services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Pokemon;

import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

@Service
public class PokeAPIService {
    private final String pokeAPIBaseURL = "https://pokeapi.co/api/v2";
    private final WebClient webClient;
    private final PokemonCalcService pokemonCalcService;

    public PokeAPIService(WebClient.Builder webClientBuilder, PokemonCalcService pokemonCalcService) {
        this.webClient = webClientBuilder.baseUrl(pokeAPIBaseURL).build();
        this.pokemonCalcService = pokemonCalcService;

    }

    public PokemonDTO populatePokemonDTO(Pokemon pkmnToUse) {
        PokemonAPIDTOHelper apiData = getPokeAPIData(pkmnToUse.getName());
        return new PokemonDTO(
            pkmnToUse.getId(),
            pkmnToUse.isInBox(),
            pkmnToUse.getName(),
            apiData.displayName,
            apiData.spriteURL,
            apiData.flavorText,
            pkmnToUse.getLevel(), pkmnToUse.getExp(),
            pkmnToUse.getMaxHp(), pkmnToUse.getCurHp(),
            pkmnToUse.getAtk(), pkmnToUse.getDef(), pkmnToUse.getSpa(), pkmnToUse.getSpd(), pkmnToUse.getSpe(),
            pkmnToUse.getAtkBaseStat(), pkmnToUse.getDefBaseStat(), pkmnToUse.getSpaBaseStat(), pkmnToUse.getSpdBaseStat(), pkmnToUse.getSpeBaseStat(), pkmnToUse.getHpBaseStat(),
            pkmnToUse.getMove1(), pkmnToUse.getMove2(), pkmnToUse.getMove3(), pkmnToUse.getMove4()
        );
    }

    public PokemonDTO populateRandomPokemonDTO(String pkmnToUse) {
        PokemonAPIDTOHelper apiData = getPokeAPIData(pkmnToUse);

        return new PokemonDTO(
            -1,
            false,
            pkmnToUse,
            apiData.displayName,
            apiData.spriteURL,
            apiData.flavorText,
            pokemonCalcService.calcRndPkmnLevel(),
            0,
            pokemonCalcService.calcPkmnMaxHp(apiData.hp_baseStat), pokemonCalcService.calcPkmnMaxHp(apiData.hp_baseStat),
            pokemonCalcService.calcPkmnAtk(apiData.atk_baseStat),
            pokemonCalcService.calcPkmnDef(apiData.def_baseStat),
            pokemonCalcService.calcPkmnSpa(apiData.spa_baseStat),
            pokemonCalcService.calcPkmnSpd(apiData.spd_baseStat),
            pokemonCalcService.calcPkmnSpe(apiData.spe_baseStat),
            apiData.atk_baseStat, apiData.def_baseStat, apiData.spa_baseStat, apiData.spd_baseStat, apiData.spe_baseStat, apiData.hp_baseStat,
            apiData.move1, apiData.move2, apiData.move3, apiData.move4
        );
    }

    private PokemonAPIDTOHelper getPokeAPIData(String pkmnName) {
        String languageToUse = LocaleContextHolder.getLocale().getLanguage();

        PokeAPIDTO_PokemonData pokemonData = webClient.get().uri("/pokemon/"+pkmnName).retrieve().bodyToMono(PokeAPIDTO_PokemonData.class).block();
        PokeAPIDTO_PokemonSpeciesData pokemonSpeciesData = webClient.get().uri("/pokemon-species/"+pokemonData.species.name).retrieve().bodyToMono(PokeAPIDTO_PokemonSpeciesData.class).block();

        String name = pokemonSpeciesData.names.stream().filter(names->languageToUse.equals(names.language.name)).map(names->names.name).findFirst().orElse("No name");
        String description = pokemonSpeciesData.flavor_text_entries.stream().filter(f->languageToUse.equals(f.language.name)).map(f->f.flavor_text).findFirst().orElse("No description");
        String sprite = pokemonData.sprites.front_default;
        int hp_baseStat = pokemonData.stats.stream().filter(stats->"hp".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        int atk_baseStat = pokemonData.stats.stream().filter(stats->"attack".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        int def_baseStat = pokemonData.stats.stream().filter(stats->"defense".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        int spa_baseStat = pokemonData.stats.stream().filter(stats->"special-attack".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        int spd_baseStat = pokemonData.stats.stream().filter(stats->"special-defense".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        int spe_baseStat = pokemonData.stats.stream().filter(stats->"speed".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);

        List<String> randomMoves = getRandomMoves(pokemonData);
        String move1 = randomMoves.get(0);
        String move2 = randomMoves.get(1);
        String move3 = randomMoves.get(2);
        String move4 = randomMoves.get(3);

        return new PokemonAPIDTOHelper(
            name,
            sprite,
            description,
            atk_baseStat, def_baseStat, spa_baseStat, spd_baseStat, spe_baseStat, hp_baseStat,
            move1, move2, move3, move4
        );
    }

    private List<String> getRandomMoves(PokeAPIDTO_PokemonData pokemonData) {
        final int moveAmount = 4;
        List<String> randomMoves = new ArrayList<>(pokemonData.moves.stream().map(move->move.move.name).toList());
        System.out.println(randomMoves);
        System.out.println("\n\n\n");
        Collections.shuffle(randomMoves);
        return randomMoves.stream().limit(moveAmount).toList();
    }

    public PokemonDTO getRandomPokemon() {
        return populateRandomPokemonDTO(getRandomPokemonName());
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

   public record PokeAPIDTO_PokemonData(Sprites sprites, Species species, List<Stat> stats, List<Move> moves) {}
   public record PokeAPIDTO_PokemonSpeciesData(List<Name> names, List<FlavorText> flavor_text_entries) {}

   public record Sprites(String front_default) {}
   public record Stat(int base_stat, StatInfo stat) {}
   public record StatInfo(String name) {}
   public record Move(MoveInfo move) {}
   public record MoveInfo(String name) {}
   public record Name(Language language, String name) {}
   public record Species(String name) {}
   public record FlavorText(String flavor_text, Language language) {}
   public record Language(String name) {}

   public record PokemonAPIDTOHelper(
        String displayName, String spriteURL, String flavorText,
        int atk_baseStat, int def_baseStat, int spa_baseStat, int spd_baseStat, int spe_baseStat, int hp_baseStat,
        String move1, String move2, String move3, String move4
    ) {}
}