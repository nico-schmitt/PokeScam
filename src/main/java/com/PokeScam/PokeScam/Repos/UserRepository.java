package com.PokeScam.PokeScam.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PokeScam.PokeScam.Model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
    User findByUsername(String username);
}
