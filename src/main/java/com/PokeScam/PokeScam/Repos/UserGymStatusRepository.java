package com.PokeScam.PokeScam.Repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Model.UserGymStatus;

@Repository
public interface UserGymStatusRepository extends JpaRepository<UserGymStatus, Long> {
    Optional<UserGymStatus> findByUserAndGym(User user, Gym gym);

    List<UserGymStatus> findByUser(User user);
}
