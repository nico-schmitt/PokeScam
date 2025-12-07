package com.PokeScam.PokeScam.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PokeScam.PokeScam.Pokemon;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Integer>{
}
