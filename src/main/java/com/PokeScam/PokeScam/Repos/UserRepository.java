package com.PokeScam.PokeScam.Repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PokeScam.PokeScam.Model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
    Optional<User> findByUsername(String username);
    // id >= 19 because lower ids are not compatible test users
    @Query(value = "SELECT * FROM users WHERE id >= 19 AND id != :thisUserId ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    User findRandomUser(@Param("thisUserId") int id);
}
