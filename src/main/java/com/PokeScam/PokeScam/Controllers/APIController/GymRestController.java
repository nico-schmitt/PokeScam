package com.PokeScam.PokeScam.Controllers.APIController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.GymRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;

@RestController
@RequestMapping("/api")
public class GymRestController {

    private final GymRepository gymRepo;
    private final UserRepository userRepo;

    record GymCreateDTO(String name, String ownerUsername) {
    }

    record GymUpdateDTO(String name) {
    }

    public GymRestController(GymRepository gymRepo, UserRepository userRepo) {
        this.gymRepo = gymRepo;
        this.userRepo = userRepo;
    }

    /*
     * =======================
     * READ
     * =======================
     */

    /** Get all player gyms */
    @GetMapping("/gyms")
    public ResponseEntity<List<Gym>> getAllPlayerGyms() {
        return ResponseEntity.ok(gymRepo.findByNpcGymFalse());
    }

    /** Get a single player gym by id */
    @GetMapping("/gyms/{id}")
    public ResponseEntity<Gym> getGymById(@PathVariable Long id) {
        Optional<Gym> gym = gymRepo.findById(id);
        if (gym.isEmpty() || gym.get().isNpcGym()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gym.get());
    }

    /*
     * =======================
     * CREATE
     * =======================
     */

    @PostMapping("/gyms")
    public ResponseEntity<Gym> createGym(@RequestBody GymCreateDTO gymToCreate)
            throws URISyntaxException {

        if (gymToCreate.name() == null || gymToCreate.ownerUsername() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> ownerOpt = userRepo.findByUsername(gymToCreate.ownerUsername());
        if (ownerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User owner = ownerOpt.get();

        // One player gym per user
        if (gymRepo.findByOwnerAndNpcGymFalse(owner).isPresent()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_CONTENT);
        }

        Gym gym = new Gym();
        gym.setName(gymToCreate.name());
        gym.setNpcGym(false);
        gym.setOwner(owner);

        gym = gymRepo.save(gym);

        URI location = new URI("/api/gyms/" + gym.getId());
        return ResponseEntity.created(location).body(gym);
    }

    /*
     * =======================
     * UPDATE
     * =======================
     */

    @PatchMapping("/gyms/{id}")
    public ResponseEntity<Gym> updateGym(
            @PathVariable Long id,
            @RequestBody GymUpdateDTO updateInfo) {

        Optional<Gym> gymOpt = gymRepo.findById(id);
        if (gymOpt.isEmpty() || gymOpt.get().isNpcGym()) {
            return ResponseEntity.notFound().build();
        }

        Gym gym = gymOpt.get();

        if (updateInfo.name() != null) {
            gym.setName(updateInfo.name());
        }

        gym = gymRepo.save(gym);
        return ResponseEntity.ok(gym);
    }

    /*
     * =======================
     * DELETE
     * =======================
     */

    @DeleteMapping("/gyms/{id}")
    public ResponseEntity<Gym> deleteGym(@PathVariable Long id) {
        Optional<Gym> gymOpt = gymRepo.findById(id);
        if (gymOpt.isEmpty() || gymOpt.get().isNpcGym()) {
            return ResponseEntity.notFound().build();
        }

        Gym gym = gymOpt.get();
        gymRepo.delete(gym);

        return ResponseEntity.ok(gym);
    }
}
