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
            List.of(
                populatePokemonDTOAllMovesInfo(apiData.allMoves.moves.get(0)),
                populatePokemonDTOAllMovesInfo(apiData.allMoves.moves.get(1)),
                populatePokemonDTOAllMovesInfo(apiData.allMoves.moves.get(2)),
                populatePokemonDTOAllMovesInfo(apiData.allMoves.moves.get(3))
            )
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
        int rndLevel = pokemonCalcService.calcRndPkmnLevel();
        PokemonDTO_AllStats allStats = new PokemonDTO_AllStats(
            new PokemonDTO_StatInfo(apiData.allStats.hp.baseStat, pokemonCalcService.calcPkmnMaxHp(apiData.allStats.hp.baseStat, rndLevel), "HP"),
            new PokemonDTO_StatInfo(apiData.allStats.atk.baseStat, pokemonCalcService.calcPkmnAtk(apiData.allStats.atk.baseStat, rndLevel), "ATK"),
            new PokemonDTO_StatInfo(apiData.allStats.def.baseStat, pokemonCalcService.calcPkmnDef(apiData.allStats.def.baseStat, rndLevel), "DEF"),
            new PokemonDTO_StatInfo(apiData.allStats.spa.baseStat, pokemonCalcService.calcPkmnSpa(apiData.allStats.spa.baseStat, rndLevel), "SPA"),
            new PokemonDTO_StatInfo(apiData.allStats.spd.baseStat, pokemonCalcService.calcPkmnSpd(apiData.allStats.spd.baseStat, rndLevel), "SPD"),
            new PokemonDTO_StatInfo(apiData.allStats.spe.baseStat, pokemonCalcService.calcPkmnSpe(apiData.allStats.spe.baseStat, rndLevel), "SPE")
        );
        PokemonDTO_AllMoves allMoves = new PokemonDTO_AllMoves(
            List.of(
                populatePokemonDTOAllMovesInfo(apiData.allMoves.moves.get(0)),
                populatePokemonDTOAllMovesInfo(apiData.allMoves.moves.get(1)),
                populatePokemonDTOAllMovesInfo(apiData.allMoves.moves.get(2)),
                populatePokemonDTOAllMovesInfo(apiData.allMoves.moves.get(3))
            )
        );

        return new PokemonDTO(
            -1,
            false,
            pkmnToUse,
            apiData.displayName,
            apiData.spriteURL,
            apiData.flavorText,
            rndLevel,
            0,
            pokemonCalcService.calcPkmnMaxHp(apiData.allStats.hp.baseStat, rndLevel),
            pokemonCalcService.calcPkmnMaxHp(apiData.allStats.hp.baseStat, rndLevel),
            allStats,
            allMoves
        );
    }

    private PokemonDTO_MoveInfo populatePokemonDTOAllMovesInfo(AllMoveInfo allMoveInfo) {
        return new PokemonDTO_MoveInfo(
            allMoveInfo.apiName,
            allMoveInfo.displayName,
            allMoveInfo.damageClass,
            allMoveInfo.power,
            allMoveInfo.accuracy,
            allMoveInfo.pp
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
        Mono<PokeAPIDTO_PokemonMoveData> pokemonMoveData1_mono = normalWebClient.get().uri(moves.get(0).url).retrieve().bodyToMono(PokeAPIDTO_PokemonMoveData.class);
        Mono<PokeAPIDTO_PokemonMoveData> pokemonMoveData2_mono = normalWebClient.get().uri(moves.get(1).url).retrieve().bodyToMono(PokeAPIDTO_PokemonMoveData.class);
        Mono<PokeAPIDTO_PokemonMoveData> pokemonMoveData3_mono = normalWebClient.get().uri(moves.get(2).url).retrieve().bodyToMono(PokeAPIDTO_PokemonMoveData.class);
        Mono<PokeAPIDTO_PokemonMoveData> pokemonMoveData4_mono = normalWebClient.get().uri(moves.get(3).url).retrieve().bodyToMono(PokeAPIDTO_PokemonMoveData.class);
        List<AllMoveInfo> allMoveInfo = Mono.zip(pokemonMoveData1_mono, pokemonMoveData2_mono, pokemonMoveData3_mono, pokemonMoveData4_mono).map(tuple->{
            return List.of(
                getMoveData(tuple.getT1(), languageToUse),
                getMoveData(tuple.getT2(), languageToUse),
                getMoveData(tuple.getT3(), languageToUse),
                getMoveData(tuple.getT4(), languageToUse)
            );
        }).block();
        AllMoves allMoves = new AllMoves(
            List.of(
                allMoveInfo.get(0),
                allMoveInfo.get(1),
                allMoveInfo.get(2),
                allMoveInfo.get(3)
            )
        );
        return allMoves;
    }

    private AllMoveInfo getMoveData(PokeAPIDTO_PokemonMoveData moveData, String languageToUse) {
        String apiName = moveData.name;
        String displayName = moveData.names.stream().filter(names->languageToUse.equals(names.language.name)).map(names->names.name).findFirst().orElse("No name");
        String damageClass = moveData.damage_class.name;
        int power = moveData.power.orElse(0);
        int accuracy = moveData.accuracy.orElse(0);
        int pp = moveData.pp.orElse(0);
        return new AllMoveInfo(apiName, displayName, damageClass, power, accuracy, pp);
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
        // get random moves if no names given
        if(moveNames == null) {
            movesInfo = new ArrayList<>(pokemonData.moves.stream().map(move->move.move).toList());
            Collections.shuffle(movesInfo);
        }
        else {
            movesInfo = new ArrayList<>(pokemonData.moves.stream().filter(move->moveNames.contains(move.move.name)).map(move->move.move).toList());
        }
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
   public record PokeAPIDTO_PokemonMoveData(String name, List<Name> names, DamageClass damage_class, OptionalInt power, OptionalInt accuracy, OptionalInt pp) {}

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
    public record AllMoves(List<AllMoveInfo> moves) {}
    public record AllMoveInfo(String apiName, String displayName, String damageClass, int power, int accuracy, int pp) {}
}