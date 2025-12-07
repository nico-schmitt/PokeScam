package com.PokeScam.PokeScam.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;

import java.util.List;


@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Integer>{
    List<Pokemon> findByOwnerID(User ownerID);
}
