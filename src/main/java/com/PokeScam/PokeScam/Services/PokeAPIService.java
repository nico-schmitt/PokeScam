package com.PokeScam.PokeScam.Services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_AllMoves;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_AllStats;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_MoveInfo;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_StatInfo;
import com.PokeScam.PokeScam.Model.Pokemon;

import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

@Service
public class PokeAPIService {
    private final String pokeAPIBaseURL = "https://pokeapi.co/api/v2";
    private final WebClient pokeAPIWebClient;
    private final WebClient normalWebClient;
    private final PokemonCalcService pokemonCalcService;

    public PokeAPIService(WebClient.Builder webClientBuilder, PokemonCalcService pokemonCalcService) {
        this.pokeAPIWebClient = webClientBuilder.baseUrl(pokeAPIBaseURL).build();
        normalWebClient = webClientBuilder.build();
        this.pokemonCalcService = pokemonCalcService;
    }

    public PokemonDTO populatePokemonDTO(Pokemon pkmnToUse) {
        APIMovesLookup apiMovesLookup = new APIMovesLookup(pkmnToUse.getMove1(), pkmnToUse.getMove2(), pkmnToUse.getMove3(), pkmnToUse.getMove4());
        PokemonAPIDTOHelper apiData = getPokeAPIData(pkmnToUse.getName(), apiMovesLookup);
        PokemonDTO_AllStats allStats = new PokemonDTO_AllStats(
            new PokemonDTO_StatInfo(pkmnToUse.getHpBaseStat(), pkmnToUse.getMaxHp(), "HP"),
            new PokemonDTO_StatInfo(pkmnToUse.getAtkBaseStat(), pkmnToUse.getAtk(), "ATK"),
            new PokemonDTO_StatInfo(pkmnToUse.getDefBaseStat(), pkmnToUse.getDef(), "DEF"),
            new PokemonDTO_StatInfo(pkmnToUse.getSpaBaseStat(), pkmnToUse.getSpa(), "SPA"),
            new PokemonDTO_StatInfo(pkmnToUse.getSpdBaseStat(), pkmnToUse.getSpd(), "SPD"),
            new PokemonDTO_StatInfo(pkmnToUse.getSpeBaseStat(), pkmnToUse.getSpe(), "SPE")
        );
        PokemonDTO_AllMoves allMoves = new PokemonDTO_AllMoves(
            new PokemonDTO_MoveInfo(apiData.allMoves.move1.apiName, apiData.allMoves.move1.displayName, apiData.allMoves.move1.power),
            new PokemonDTO_MoveInfo(apiData.allMoves.move2.apiName, apiData.allMoves.move2.displayName, apiData.allMoves.move2.power),
            new PokemonDTO_MoveInfo(apiData.allMoves.move3.apiName, apiData.allMoves.move3.displayName, apiData.allMoves.move3.power),
            new PokemonDTO_MoveInfo(apiData.allMoves.move4.apiName, apiData.allMoves.move4.displayName, apiData.allMoves.move4.power)
        );
        return new PokemonDTO(
            pkmnToUse.getId(),
            pkmnToUse.isInBox(),
            pkmnToUse.getName(),
            apiData.displayName,
            apiData.spriteURL,
            apiData.flavorText,
            pkmnToUse.getLevel(), pkmnToUse.getExp(),
            pkmnToUse.getMaxHp(), pkmnToUse.getCurHp(),
            allStats,
            allMoves
        );
    }

    public PokemonDTO populateRandomPokemonDTO(String pkmnToUse) {
        PokemonAPIDTOHelper apiData = getPokeAPIData(pkmnToUse, null);
        PokemonDTO_AllStats allStats = new PokemonDTO_AllStats(
            new PokemonDTO_StatInfo(apiData.allStats.hp.baseStat, pokemonCalcService.calcPkmnMaxHp(apiData.allStats.hp.baseStat), "HP"),
            new PokemonDTO_StatInfo(apiData.allStats.atk.baseStat, pokemonCalcService.calcPkmnAtk(apiData.allStats.atk.baseStat), "ATK"),
            new PokemonDTO_StatInfo(apiData.allStats.def.baseStat, pokemonCalcService.calcPkmnDef(apiData.allStats.def.baseStat), "DEF"),
            new PokemonDTO_StatInfo(apiData.allStats.spa.baseStat, pokemonCalcService.calcPkmnSpa(apiData.allStats.spa.baseStat), "SPA"),
            new PokemonDTO_StatInfo(apiData.allStats.spd.baseStat, pokemonCalcService.calcPkmnSpd(apiData.allStats.spd.baseStat), "SPD"),
            new PokemonDTO_StatInfo(apiData.allStats.spe.baseStat, pokemonCalcService.calcPkmnSpe(apiData.allStats.spe.baseStat), "SPE")
        );
        PokemonDTO_AllMoves allMoves = new PokemonDTO_AllMoves(
            new PokemonDTO_MoveInfo(apiData.allMoves.move1.apiName, apiData.allMoves.move1.displayName, apiData.allMoves.move1.power),
            new PokemonDTO_MoveInfo(apiData.allMoves.move2.apiName, apiData.allMoves.move2.displayName, apiData.allMoves.move2.power),
            new PokemonDTO_MoveInfo(apiData.allMoves.move3.apiName, apiData.allMoves.move3.displayName, apiData.allMoves.move3.power),
            new PokemonDTO_MoveInfo(apiData.allMoves.move4.apiName, apiData.allMoves.move4.displayName, apiData.allMoves.move4.power)
        );

        return new PokemonDTO(
            -1,
            false,
            pkmnToUse,
            apiData.displayName,
            apiData.spriteURL,
            apiData.flavorText,
            pokemonCalcService.calcRndPkmnLevel(),
            0,
            pokemonCalcService.calcPkmnMaxHp(apiData.allStats.hp.baseStat),
            pokemonCalcService.calcPkmnMaxHp(apiData.allStats.hp.baseStat),
            allStats,
            allMoves
        );
    }

    private PokemonAPIDTOHelper getPokeAPIData(String pkmnName, APIMovesLookup apiMovesLookup) {
        String languageToUse = LocaleContextHolder.getLocale().getLanguage();

        PokeAPIDTO_PokemonData pokemonData = pokeAPIWebClient.get().uri("/pokemon/"+pkmnName).retrieve().bodyToMono(PokeAPIDTO_PokemonData.class).block();
        PokeAPIDTO_PokemonSpeciesData pokemonSpeciesData = pokeAPIWebClient.get().uri("/pokemon-species/"+pokemonData.species.name).retrieve().bodyToMono(PokeAPIDTO_PokemonSpeciesData.class).block();

        String displayName = pokemonSpeciesData.names.stream().filter(names->languageToUse.equals(names.language.name)).map(names->names.name).findFirst().orElse("No name");
        String description = pokemonSpeciesData.flavor_text_entries.stream().filter(f->languageToUse.equals(f.language.name)).map(f->f.flavor_text).findFirst().orElse("No description");
        String sprite = pokemonData.sprites.front_default;
        AllStats allStats = getAllStats(pokemonData);
        AllMoves allMoves = getAllMoves(pokemonData, languageToUse, apiMovesLookup);

        return new PokemonAPIDTOHelper(
            displayName,
            sprite,
            description,
            allStats,
            allMoves
        );
    }

    private AllMoves getAllMoves(PokeAPIDTO_PokemonData pokemonData, String languageToUse, APIMovesLookup apiMovesLookup) {
        List<MoveInfo> moves =
            apiMovesLookup == null 
                ? getMovesInfo(pokemonData, null)
                : getMovesInfo(pokemonData, List.of(apiMovesLookup.move1, apiMovesLookup.move2, apiMovesLookup.move3, apiMovesLookup.move4));
        String move1_apiName = moves.get(0).name;
        String move2_apiName = moves.get(1).name;
        String move3_apiName = moves.get(2).name;
        String move4_apiName = moves.get(3).name;
        PokeAPIDTO_PokemonMoveData pokemonMoveData1 = normalWebClient.get().uri(moves.get(0).url).retrieve().bodyToMono(PokeAPIDTO_PokemonMoveData.class).block();
        PokeAPIDTO_PokemonMoveData pokemonMoveData2 = normalWebClient.get().uri(moves.get(1).url).retrieve().bodyToMono(PokeAPIDTO_PokemonMoveData.class).block();
        PokeAPIDTO_PokemonMoveData pokemonMoveData3 = normalWebClient.get().uri(moves.get(2).url).retrieve().bodyToMono(PokeAPIDTO_PokemonMoveData.class).block();
        PokeAPIDTO_PokemonMoveData pokemonMoveData4 = normalWebClient.get().uri(moves.get(3).url).retrieve().bodyToMono(PokeAPIDTO_PokemonMoveData.class).block();
        String move1_displayName = pokemonMoveData1.names.stream().filter(names->languageToUse.equals(names.language.name)).map(names->names.name).findFirst().orElse("No name");
        String move2_displayName = pokemonMoveData2.names.stream().filter(names->languageToUse.equals(names.language.name)).map(names->names.name).findFirst().orElse("No name");
        String move3_displayName = pokemonMoveData3.names.stream().filter(names->languageToUse.equals(names.language.name)).map(names->names.name).findFirst().orElse("No name");
        String move4_displayName = pokemonMoveData4.names.stream().filter(names->languageToUse.equals(names.language.name)).map(names->names.name).findFirst().orElse("No name");
        int move1_power = pokemonMoveData1.power.orElse(0);
        int move2_power = pokemonMoveData2.power.orElse(0);
        int move3_power = pokemonMoveData3.power.orElse(0);
        int move4_power = pokemonMoveData4.power.orElse(0);
        AllMoves allMoves = new AllMoves(
            new AllMoveInfo(move1_apiName, move1_displayName, move1_power),
            new AllMoveInfo(move2_apiName, move2_displayName, move2_power),
            new AllMoveInfo(move3_apiName, move3_displayName, move3_power),
            new AllMoveInfo(move4_apiName, move4_displayName, move4_power)
        );
        return allMoves;
    }

    private AllStats getAllStats(PokeAPIDTO_PokemonData pokemonData) {
        int hp_baseStat = pokemonData.stats.stream().filter(stats->"hp".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        int atk_baseStat = pokemonData.stats.stream().filter(stats->"attack".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        int def_baseStat = pokemonData.stats.stream().filter(stats->"defense".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        int spa_baseStat = pokemonData.stats.stream().filter(stats->"special-attack".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        int spd_baseStat = pokemonData.stats.stream().filter(stats->"special-defense".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        int spe_baseStat = pokemonData.stats.stream().filter(stats->"speed".equals(stats.stat.name)).mapToInt(stats->stats.base_stat).findFirst().orElse(0);
        AllStats allStats = new AllStats(
            new AllStatInfo(hp_baseStat),
            new AllStatInfo(atk_baseStat),
            new AllStatInfo(def_baseStat),
            new AllStatInfo(spa_baseStat),
            new AllStatInfo(spd_baseStat),
            new AllStatInfo(spe_baseStat)
        );
        return allStats;
    }

    private List<MoveInfo> getMovesInfo(PokeAPIDTO_PokemonData pokemonData, List<String> moveNames) {
        final int moveAmount = 4;
        List<MoveInfo> movesInfo;
        System.out.println(moveNames + "aaa\n\n\naaa");
        System.out.println(pokemonData.moves + "bbb\n\n\nbbb");
        // get random moves if no names given
        if(moveNames == null) {
            movesInfo = new ArrayList<>(pokemonData.moves.stream().map(move->move.move).toList());
            Collections.shuffle(movesInfo);
        }
        else {
            movesInfo = new ArrayList<>(pokemonData.moves.stream().filter(move->moveNames.contains(move.move.name)).map(move->move.move).toList());
        }
        System.out.println(movesInfo+"\n\n\n\n\n\n");
        return movesInfo.stream().limit(moveAmount).toList();
    }

    public PokemonDTO getRandomPokemon() {
        return populateRandomPokemonDTO(getRandomPokemonName());
    }

    public boolean pokemonExists(String name) {
        int statusCode = pokeAPIWebClient.get().uri("/pokemon/"+name).exchangeToMono(res->Mono.just(res.statusCode().value())).block();
        return statusCode == 404 ? false : true;
    };

    private String getRandomPokemonName() {
        final int existingPkmnCount = 1025;
        final int randPkmnID = ThreadLocalRandom.current().nextInt(1, existingPkmnCount+1);
        return pokeAPIWebClient
            .get().uri("/pokemon/"+randPkmnID).retrieve().bodyToMono(JsonNode.class)
            .map(json->json.at("/name").asString())
            .block();
    }

   public record PokeAPIDTO_PokemonData(Sprites sprites, Species species, List<Stat> stats, List<Move> moves) {}
   public record PokeAPIDTO_PokemonSpeciesData(List<Name> names, List<FlavorText> flavor_text_entries) {}
   public record PokeAPIDTO_PokemonMoveData(OptionalInt power, List<Name> names, DamageClass damage_class) {}

   public record Sprites(String front_default) {}
   public record Stat(int base_stat, StatInfo stat) {}
   public record StatInfo(String name) {}
   public record Move(MoveInfo move) {}
   public record MoveInfo(String name, String url) {}
   public record DamageClass(String name) {}
   public record Name(Language language, String name) {}
   public record Species(String name) {}
   public record FlavorText(String flavor_text, Language language) {}
   public record Language(String name) {}

   public record PokemonAPIDTOHelper(
        String displayName, String spriteURL, String flavorText,
        AllStats allStats,
        AllMoves allMoves
    ) {}

    public record APIMovesLookup(String move1, String move2, String move3, String move4) {}

    public record AllStats(AllStatInfo hp, AllStatInfo atk, AllStatInfo def, AllStatInfo spa, AllStatInfo spd, AllStatInfo spe) {}
    public record AllStatInfo(int baseStat) {}
    public record AllMoves(AllMoveInfo move1, AllMoveInfo move2, AllMoveInfo move3, AllMoveInfo move4) {}
    public record AllMoveInfo(String apiName, String displayName, int power) {}
}