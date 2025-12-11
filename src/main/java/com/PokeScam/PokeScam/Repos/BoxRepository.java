package com.PokeScam.PokeScam.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.User;

import java.util.List;


@Repository
public interface BoxRepository extends JpaRepository<Box, Integer>{
    List<Box> findByOwnerID(User ownerID);
}
