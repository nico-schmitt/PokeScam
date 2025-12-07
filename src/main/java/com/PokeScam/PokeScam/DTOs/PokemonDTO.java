package com.PokeScam.PokeScam.DTOs;

import lombok.Data;

@Data
public class PokemonDTO {
    private String name;
    private String imageURL;

    public static PokemonDTO getEmpty() {
        PokemonDTO empty = new PokemonDTO();
        empty.setName("Not set");
        empty.setImageURL("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/201-question.png");
        return empty;
    }
}
