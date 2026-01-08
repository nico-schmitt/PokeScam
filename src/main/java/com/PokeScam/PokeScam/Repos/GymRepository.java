package com.PokeScam.PokeScam.Repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.PokeScam.PokeScam.Model.Gym;
import java.util.List;

public interface GymRepository extends JpaRepository<Gym, Long> {

    // Existing non-paginated methods
    List<Gym> findByNpcGymTrue();

    List<Gym> findByNpcGymFalse();

    // New paginated versions
    Page<Gym> findByNpcGymTrue(Pageable pageable);

    Page<Gym> findByNpcGymFalse(Pageable pageable);
}
