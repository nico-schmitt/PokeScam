package com.PokeScam.PokeScam.DTOs;


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

    public record PokemonDTO_AllStats(PokemonDTO_StatInfo hp, PokemonDTO_StatInfo atk, PokemonDTO_StatInfo def, PokemonDTO_StatInfo spa, PokemonDTO_StatInfo spd, PokemonDTO_StatInfo spe) {}
    public record PokemonDTO_StatInfo(int baseStat, int statValue, String name) {}
    public record PokemonDTO_AllMoves(PokemonDTO_MoveInfo move1, PokemonDTO_MoveInfo move2, PokemonDTO_MoveInfo move3, PokemonDTO_MoveInfo move4) {}
    public record PokemonDTO_MoveInfo(String apiName, String displayName, int power) {}
}
