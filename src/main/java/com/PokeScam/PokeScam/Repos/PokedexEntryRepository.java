package com.PokeScam.PokeScam.Repos;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.PokeScam.PokeScam.Model.PokedexEntry;
import com.PokeScam.PokeScam.Model.User;

public interface PokedexEntryRepository extends JpaRepository<PokedexEntry, Integer> {

    Optional<PokedexEntry> findByUserAndSpeciesName(User user, String speciesName);

    List<PokedexEntry> findByUser(User user);

    Page<PokedexEntry> findAllByUser(User user, Pageable pageable);

    List<PokedexEntry> findAllByUser(User user);

}
