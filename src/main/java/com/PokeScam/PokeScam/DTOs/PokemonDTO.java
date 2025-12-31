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
    int atk, int def, int spa, int spd, int spe,
    int atkBase, int defBase, int spaBase, int spdBase, int speBase, int hpBase
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
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0
        );
    }
}