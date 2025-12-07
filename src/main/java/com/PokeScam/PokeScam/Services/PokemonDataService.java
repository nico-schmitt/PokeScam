package com.PokeScam.PokeScam.Services;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.Pokemon;
import com.PokeScam.PokeScam.Repos.PokemonRepository;

@Service
public class PokemonDataService {
    private PokemonRepository r;

    public PokemonDataService(PokemonRepository r) {
        this.r = r;
    }
    public void savePkmn() {
        Pokemon p = new Pokemon();
        p.setName("aoeu");
        r.save(p);
    }
}
