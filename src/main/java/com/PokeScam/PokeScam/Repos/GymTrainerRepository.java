package com.PokeScam.PokeScam.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Model.GymTrainer;

public interface GymTrainerRepository extends JpaRepository<GymTrainer, Long> {

    // Fetch all trainers for a gym, ordered by sequenceNumber
    List<GymTrainer> findByGymOrderBySequenceNumberAsc(Gym gym);
}
