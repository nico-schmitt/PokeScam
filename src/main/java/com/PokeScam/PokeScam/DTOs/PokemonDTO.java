package com.PokeScam.PokeScam.DTOs;


public record PokemonDTO(
    int id,
    boolean isInBox,
    String apiName,
    String displayName,
    String imageURL,
    String flavorText
) {
    public static PokemonDTO getEmpty() {
        return new PokemonDTO(
            -1,
            false,
            "",
            "Not set",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/201-question.png",
            ""
        );
    }
}