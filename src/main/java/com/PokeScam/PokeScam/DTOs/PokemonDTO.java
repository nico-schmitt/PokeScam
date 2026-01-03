package com.PokeScam.PokeScam.DTOs;

import java.util.List;

public record PokemonDTO(
    int id,
    boolean isInBox,
    String apiName,
    String displayName,
    String imageURL,
    String flavorText,
    int level, int exp,
    int maxHp, int curHp,
    PokemonDTO_AllStats allStats,
    PokemonDTO_AllMoves allMoves
) {
    public static PokemonDTO getEmpty() {
        return new PokemonDTO(
            -1,
            false,
            "",
            "Not set",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/201-question.png",
            "",
            0, 0,
            0, 0,
            null,
            null
        );
    }

    public PokemonDTO withNewHealth(int newHealth) {
        return new PokemonDTO(
            this.id,
            this.isInBox,
            this.apiName,
            this.displayName,
            this.imageURL,
            this.flavorText,
            this.level,
            this.exp,
            this.maxHp,
            newHealth,
            this.allStats,
            this.allMoves
        );
    }

    public record PokemonDTO_AllStats(PokemonDTO_StatInfo hp, PokemonDTO_StatInfo atk, PokemonDTO_StatInfo def, PokemonDTO_StatInfo spa, PokemonDTO_StatInfo spd, PokemonDTO_StatInfo spe) {}
    public record PokemonDTO_StatInfo(int baseStat, int statValue, String name) {}
    public record PokemonDTO_AllMoves(List<PokemonDTO_MoveInfo> moves) {}
    public record PokemonDTO_MoveInfo(String apiName, String displayName, String damageClass, int power, int accuracy, int pp) {}
}
