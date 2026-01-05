package com.PokeScam.PokeScam.Repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;

import java.util.List;


@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Integer>{
    List<Pokemon> findByOwnerId(User ownerID);
    List<Pokemon> findByBoxId(Box boxID);
    Pokemon findByIdAndOwnerId(int id, User ownerID);
    long countByBoxId(Box boxID);
    Page<Pokemon> findByOwnerIdAndInBoxAndBoxId(User ownerId, boolean inBox, Box boxId, Pageable pageable);
    void deleteByIdAndOwnerId(int id, User thisUser);
    List<Pokemon> findByOwnerIdAndInBox(User thisUser, boolean inBox);
    List<Pokemon> findByOwnerIdAndInBoxFalse(User ownerId);
    Pokemon findByOwnerIdAndIsActivePkmnTrue(User thisUser);
}
