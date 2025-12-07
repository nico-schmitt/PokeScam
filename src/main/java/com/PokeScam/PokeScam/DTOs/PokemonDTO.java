package com.PokeScam.PokeScam.DTOs;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.PokeAPIService;

import lombok.Data;

@Data
public class PokemonDTO {
    private String name;
    private String imageURL;
}
