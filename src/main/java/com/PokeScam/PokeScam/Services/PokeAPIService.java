package com.PokeScam.PokeScam.Services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        this.normalWebClient = webClientBuilder.build();
        this.pokemonCalcService = pokemonCalcService;
    }

    // ====================== PUBLIC METHODS ======================

    // DB Pokémon
    public PokemonDTO fetchPokemonDTO(Pokemon pkmn) {
        return fetchPokemonDTO(pkmn.getName(), pkmn.getLevel(), pkmn);
    }

    // Random wild Pokémon (catch / encounter)
    public PokemonDTO fetchPokemonDTO(String speciesName) {
        return fetchPokemonDTO(speciesName, null, null);
    }

    // Explicit level (scripted encounters, gyms, etc.)
    public PokemonDTO fetchPokemonDTO(String speciesName, int level) {
        return fetchPokemonDTO(speciesName, level, null);
    }

    public PokemonDTO fetchPokemonDTO(
            String speciesName,
            Integer forcedLevel,
            Pokemon existingPokemon) {
        boolean fromDb = existingPokemon != null;

        int level = fromDb
                ? existingPokemon.getLevel()
                : forcedLevel != null
                        ? forcedLevel
                        : pokemonCalcService.calcRndPkmnLevel();

        // 1️⃣ Fetch base API data (stats, sprite, flavor)
        PokeAPIDTO_PokemonData pokemonData = pokeAPIWebClient.get()
                .uri("/pokemon/" + speciesName)
                .retrieve()
                .bodyToMono(PokeAPIDTO_PokemonData.class)
                .block();

        PokeAPIDTO_PokemonSpeciesData speciesData = pokeAPIWebClient.get()
                .uri("/pokemon-species/" + pokemonData.species.name)
                .retrieve()
                .bodyToMono(PokeAPIDTO_PokemonSpeciesData.class)
                .block();

        String language = LocaleContextHolder.getLocale().getLanguage();

        String displayName = speciesData.names.stream()
                .filter(n -> language.equals(n.language.name))
                .map(n -> n.name)
                .findFirst()
                .orElse(speciesName);

        String flavorText = speciesData.flavor_text_entries.stream()
                .filter(f -> language.equals(f.language.name))
                .map(f -> f.flavor_text)
                .findFirst()
                .orElse("");

        // 2️⃣ Resolve moves
        List<String> moveNames = fromDb
                ? extractMovesFromEntity(existingPokemon)
                : generateMovesFromApi(pokemonData, level);

        List<AllMoveInfo> moveInfos = fetchMoveInfos(moveNames, language);

        // 3️⃣ Stats
        PokemonDTO_AllStats stats = fromDb
                ? buildStatsFromEntity(existingPokemon)
                : buildStatsFromApi(pokemonData, level);

        int maxHp = stats.hp().statValue();
        int curHp = fromDb ? existingPokemon.getCurHp() : maxHp;

        // 4️⃣ Build DTO
        return new PokemonDTO(
                fromDb ? existingPokemon.getId() : -1,
                fromDb && existingPokemon.isInBox(),
                speciesName,
                displayName,
                pokemonData.sprites.front_default,
                flavorText,
                level,
                fromDb ? existingPokemon.getExp() : 0,
                maxHp,
                curHp,
                stats,
                new PokemonDTO_AllMoves(
                        moveInfos.stream()
                                .map(this::populatePokemonDTOAllMovesInfo)
                                .toList()),
                fromDb && existingPokemon.isActivePkmn(),
                false,
                false,
                fromDb ? pokemonCalcService.getPokemonValue(existingPokemon) : 0,
                PokemonCalcService.expToNextLevel(level));
    }

    public PokemonDTO getRandomPokemon() {
        return fetchPokemonDTO(getRandomPokemonName());
    }

    public boolean pokemonExists(String name) {
        int statusCode = pokeAPIWebClient.get()
                .uri("/pokemon/" + name)
                .exchangeToMono(res -> Mono.just(res.statusCode().value()))
                .block();
        return statusCode != 404;
    }

    public int getNationalDexNumber(String speciesName) {
        try {
            PokeAPIDTO_PokemonData data = pokeAPIWebClient.get()
                    .uri("/pokemon/" + speciesName)
                    .retrieve()
                    .bodyToMono(PokeAPIDTO_PokemonData.class)
                    .block();
            return data != null ? data.id : Integer.MAX_VALUE;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    // ====================== PRIVATE HELPERS ======================

    private List<String> extractMovesFromEntity(Pokemon p) {
        return Stream.of(
                p.getMove1(),
                p.getMove2(),
                p.getMove3(),
                p.getMove4())
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> generateMovesFromApi(
            PokeAPIDTO_PokemonData pokemonData,
            int level) {
        return pokemonData.moves.stream()
                .map(m -> m.move.name)
                .distinct()
                .limit(4)
                .toList();
    }

    private List<AllMoveInfo> fetchMoveInfos(
            List<String> moveNames,
            String language) {
        return moveNames.stream()
                .map(name -> normalWebClient.get()
                        .uri("/move/" + name)
                        .retrieve()
                        .bodyToMono(PokeAPIDTO_PokemonMoveData.class)
                        .map(md -> getMoveData(md, language))
                        .block())
                .filter(Objects::nonNull)
                .toList();
    }

    private PokemonDTO_AllStats buildStatsFromEntity(Pokemon p) {
        return new PokemonDTO_AllStats(
                new PokemonDTO_StatInfo(
                        p.getHpBaseStat(),
                        p.getMaxHp(),
                        "HP"),
                new PokemonDTO_StatInfo(
                        p.getAtkBaseStat(),
                        p.getAtk(),
                        "ATK"),
                new PokemonDTO_StatInfo(
                        p.getDefBaseStat(),
                        p.getDef(),
                        "DEF"),
                new PokemonDTO_StatInfo(
                        p.getSpaBaseStat(),
                        p.getSpa(),
                        "SPA"),
                new PokemonDTO_StatInfo(
                        p.getSpdBaseStat(),
                        p.getSpd(),
                        "SPD"),
                new PokemonDTO_StatInfo(
                        p.getSpeBaseStat(),
                        p.getSpe(),
                        "SPE"));
    }

    private PokemonDTO_AllStats buildStatsFromApi(
            PokeAPIDTO_PokemonData apiData,
            int level) {
        int hpBase = getStat(apiData, "hp");
        int atkBase = getStat(apiData, "attack");
        int defBase = getStat(apiData, "defense");
        int spaBase = getStat(apiData, "special-attack");
        int spdBase = getStat(apiData, "special-defense");
        int speBase = getStat(apiData, "speed");

        return new PokemonDTO_AllStats(
                new PokemonDTO_StatInfo(
                        hpBase,
                        pokemonCalcService.calcPkmnMaxHp(hpBase, level),
                        "HP"),
                new PokemonDTO_StatInfo(
                        atkBase,
                        pokemonCalcService.calcPkmnAtk(atkBase, level),
                        "ATK"),
                new PokemonDTO_StatInfo(
                        defBase,
                        pokemonCalcService.calcPkmnDef(defBase, level),
                        "DEF"),
                new PokemonDTO_StatInfo(
                        spaBase,
                        pokemonCalcService.calcPkmnSpa(spaBase, level),
                        "SPA"),
                new PokemonDTO_StatInfo(
                        spdBase,
                        pokemonCalcService.calcPkmnSpd(spdBase, level),
                        "SPD"),
                new PokemonDTO_StatInfo(
                        speBase,
                        pokemonCalcService.calcPkmnSpe(speBase, level),
                        "SPE"));
    }

    private PokemonDTO_MoveInfo populatePokemonDTOAllMovesInfo(AllMoveInfo allMoveInfo) {
        return new PokemonDTO_MoveInfo(
                allMoveInfo.apiName,
                allMoveInfo.displayName,
                allMoveInfo.damageClass,
                allMoveInfo.power,
                allMoveInfo.accuracy,
                allMoveInfo.pp);
    }

    private PokemonAPIDTOHelper getPokeAPIData(String speciesName, APIMovesLookup apiMovesLookup) {
        String languageToUse = LocaleContextHolder.getLocale().getLanguage();

        PokeAPIDTO_PokemonData pokemonData = pokeAPIWebClient.get()
                .uri("/pokemon/" + speciesName)
                .retrieve()
                .bodyToMono(PokeAPIDTO_PokemonData.class)
                .block();

        PokeAPIDTO_PokemonSpeciesData speciesData = pokeAPIWebClient.get()
                .uri("/pokemon-species/" + pokemonData.species.name)
                .retrieve()
                .bodyToMono(PokeAPIDTO_PokemonSpeciesData.class)
                .block();

        String displayName = speciesData.names.stream()
                .filter(n -> languageToUse.equals(n.language.name))
                .map(n -> n.name)
                .findFirst()
                .orElse("No name");

        String description = speciesData.flavor_text_entries.stream()
                .filter(f -> languageToUse.equals(f.language.name))
                .map(f -> f.flavor_text)
                .findFirst()
                .orElse("No description");

        String sprite = pokemonData.sprites.front_default;
        AllStats allStats = getAllStats(pokemonData);

        AllMoves allMoves = getAllMoves(pokemonData, languageToUse, apiMovesLookup);

        return new PokemonAPIDTOHelper(displayName, sprite, description, allStats, allMoves);
    }

    private AllMoves getAllMoves(
            PokeAPIDTO_PokemonData pokemonData,
            String languageToUse,
            APIMovesLookup apiMovesLookup) {

        if (apiMovesLookup == null) {
            return new AllMoves(Collections.emptyList());
        }

        // 1️⃣ Collect DB move names
        List<String> moveNames = Stream.of(
                apiMovesLookup.move1(),
                apiMovesLookup.move2(),
                apiMovesLookup.move3(),
                apiMovesLookup.move4())
                .filter(Objects::nonNull)
                .toList();

        if (moveNames.isEmpty()) {
            throw new IllegalStateException("No moves defined for Pokémon " + pokemonData.species.name);
        }

        // 2️⃣ Call the PokeAPI for each move
        List<AllMoveInfo> allMoveInfo = moveNames.stream()
                .map(moveName -> normalWebClient.get()
                        .uri("/move/" + moveName.toLowerCase()) // lowercase API requirement
                        .retrieve()
                        .bodyToMono(PokeAPIDTO_PokemonMoveData.class)
                        .map(md -> getMoveData(md, languageToUse))
                        .block() // block because we want the list immediately
                )
                .filter(Objects::nonNull)
                .toList();

        return new AllMoves(allMoveInfo);
    }

    private AllMoveInfo getMoveData(PokeAPIDTO_PokemonMoveData moveData, String languageToUse) {
        String apiName = moveData.name;
        String displayName = moveData.names.stream()
                .filter(n -> languageToUse.equals(n.language.name))
                .map(n -> n.name)
                .findFirst()
                .orElse("No name");

        String damageClass = moveData.damage_class.name;
        int power = moveData.power.orElse(0);
        int accuracy = moveData.accuracy.orElse(0);
        int pp = moveData.pp.orElse(0);

        return new AllMoveInfo(apiName, displayName, damageClass, power, accuracy, pp);
    }

    private AllStats getAllStats(PokeAPIDTO_PokemonData pokemonData) {
        return new AllStats(
                new AllStatInfo(getStat(pokemonData, "hp")),
                new AllStatInfo(getStat(pokemonData, "attack")),
                new AllStatInfo(getStat(pokemonData, "defense")),
                new AllStatInfo(getStat(pokemonData, "special-attack")),
                new AllStatInfo(getStat(pokemonData, "special-defense")),
                new AllStatInfo(getStat(pokemonData, "speed")));
    }

    private int getStat(PokeAPIDTO_PokemonData data, String statName) {
        return data.stats.stream()
                .filter(s -> statName.equals(s.stat.name))
                .mapToInt(s -> s.base_stat)
                .findFirst()
                .orElse(0);
    }

    private List<MoveInfo> getMovesInfo(PokeAPIDTO_PokemonData pokemonData, List<String> moveNames) {
        final int moveAmount = 4;
        List<MoveInfo> movesInfo;
        if (moveNames == null) {
            movesInfo = new ArrayList<>(pokemonData.moves.stream().map(m -> m.move).toList());
            Collections.shuffle(movesInfo);
        } else {
            movesInfo = new ArrayList<>(pokemonData.moves.stream()
                    .filter(m -> moveNames.contains(m.move.name))
                    .map(m -> m.move)
                    .toList());
        }
        return movesInfo.stream().limit(moveAmount).toList();
    }

    private String getRandomPokemonName() {
        final int existingPkmnCount = 1025;
        final int randPkmnID = ThreadLocalRandom.current().nextInt(1, existingPkmnCount + 1);
        return pokeAPIWebClient.get()
                .uri("/pokemon/" + randPkmnID)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.at("/name").asString())
                .block();
    }

    // ====================== RECORDS ======================

    public record PokeAPIDTO_PokemonData(int id, Sprites sprites, Species species, List<Stat> stats, List<Move> moves) {
    }

    public record PokeAPIDTO_PokemonSpeciesData(List<Name> names, List<FlavorText> flavor_text_entries) {
    }

    public record PokeAPIDTO_PokemonMoveData(String name, List<Name> names, DamageClass damage_class, OptionalInt power,
            OptionalInt accuracy, OptionalInt pp) {
    }

    public record Sprites(String front_default) {
    }

    public record Stat(int base_stat, StatInfo stat) {
    }

    public record StatInfo(String name) {
    }

    public record Move(MoveInfo move) {
    }

    public record MoveInfo(String name, String url) {
    }

    public record DamageClass(String name) {
    }

    public record Name(Language language, String name) {
    }

    public record Species(String name) {
    }

    public record FlavorText(String flavor_text, Language language) {
    }

    public record Language(String name) {
    }

    public record PokemonAPIDTOHelper(String displayName, String spriteURL, String flavorText, AllStats allStats,
            AllMoves allMoves) {
    }

    public record APIMovesLookup(String move1, String move2, String move3, String move4) {
    }

    public record AllStats(AllStatInfo hp, AllStatInfo atk, AllStatInfo def, AllStatInfo spa, AllStatInfo spd,
            AllStatInfo spe) {
    }

    public record AllStatInfo(int baseStat) {
    }

    public record AllMoves(List<AllMoveInfo> moves) {
    }

    public record AllMoveInfo(String apiName, String displayName, String damageClass, int power, int accuracy, int pp) {
    }

}
