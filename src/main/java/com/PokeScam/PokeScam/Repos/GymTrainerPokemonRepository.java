package com.PokeScam.PokeScam.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.PokeScam.PokeScam.Model.GymTrainer;
import com.PokeScam.PokeScam.Model.GymTrainerPokemon;

public interface GymTrainerPokemonRepository extends JpaRepository<GymTrainerPokemon, Long> {

    // Fetch a trainer's Pokémon, ordered by ID (or you can add a sequence if
    // needed)
    List<GymTrainerPokemon> findByTrainerOrderByIdAsc(GymTrainer trainer);
}
