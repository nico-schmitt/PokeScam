package com.PokeScam.PokeScam.Controllers;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.TrainerNameGenerator;
import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Model.GymTrainer;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.GymRepository;
import com.PokeScam.PokeScam.Repos.GymTrainerRepository;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Random;

@Controller
@RequestMapping("/player/gym")
public class PlayerGymController {

    private final CustomUserDetails userDetails;
    private final GymRepository gymRepo;
    private final PokemonDataService pkmnService;
    private final PokemonRepository pokemonRepo;
    private final GymTrainerRepository gymTrainerRepo;

    public PlayerGymController(
            CustomUserDetails userDetails,
            GymRepository gymRepo,
            PokemonDataService pkmnService,
            PokemonRepository pokemonRepo,
            GymTrainerRepository gymTrainerRepo) {
        this.userDetails = userDetails;
        this.gymRepo = gymRepo;
        this.pkmnService = pkmnService;
        this.pokemonRepo = pokemonRepo;
        this.gymTrainerRepo = gymTrainerRepo;
    }

    // ==================== VIEW PLAYER GYM ====================
    @GetMapping
    public String viewGym(Model model) {
        var user = userDetails.getThisUser();

        // Try to find an existing player-owned gym
        Gym gym = gymRepo.findByNpcGymFalse()
                .stream()
                .filter(g -> user.equals(g.getOwner()))
                .findFirst()
                .orElse(null);

        // If none exists, create one
        if (gym == null) {
            gym = new Gym();
            gym.setOwner(user);
            gym.setNpcGym(false); // it's a player gym
            gym.setTrainers(List.of()); // start empty
            gym.setName(user.getUsername() + "'s Gym");
            gymRepo.save(gym);
        }

        // Fetch player's team
        List<Pokemon> team = pkmnService.getPkmnTeamInfo(user);

        model.addAttribute("gym", gym);
        model.addAttribute("team", team);
        model.addAttribute("isOwner", true);

        return "gymDetail";
    }

    // ==================== ASSIGN POKEMON TO GYM ====================
    @PostMapping("/assign")
    public String assignPokemonToGym(
            @RequestParam Long gymId,
            @RequestParam List<Long> pokemonIds) {

        Gym gym = gymRepo.findById(gymId).orElseThrow();
        var user = userDetails.getThisUser();

        // Clear previous trainers
        gym.getTrainers().clear();
        gymRepo.saveAndFlush(gym); // 👈 IMPORTANT

        // Create trainers for each selected Pokémon
        int sequence = 0;
        for (Long pkmnId : pokemonIds) {
            Pokemon p = pokemonRepo.findById(pkmnId.intValue()).orElseThrow();

            GymTrainer trainer = new GymTrainer();
            trainer.setGym(gym);
            trainer.setSequenceNumber(sequence++);
            trainer.setTeam(List.of(p));
            gym.getTrainers().add(trainer);
        }

        gymRepo.save(gym);

        return "redirect:/player/gym";
    }

    // ==================== VIEW GYM STATS ====================
    @GetMapping("/{gymId}/stats")
    public String viewGymStats(@PathVariable Long gymId, Model model) {
        Gym gym = gymRepo.findById(gymId).orElseThrow();

        model.addAttribute("gym", gym);
        model.addAttribute("trainers", gym.getTrainers());

        return "playerGymStats";
    }

    @GetMapping("/edit")
    public String editGym(Model model) {
        User user = userDetails.getThisUser();

        Gym gym = gymRepo.findByOwnerAndNpcGymFalse(user)
                .orElseThrow();

        GymTrainer leader = gym.getTrainers().stream()
                .filter(t -> t.getName().equals(user.getUsername()))
                .findFirst()
                .orElseGet(() -> {
                    GymTrainer lt = new GymTrainer();
                    lt.setName(user.getUsername());
                    lt.setSequenceNumber(0);
                    lt.setGym(gym);
                    gym.getTrainers().add(lt);
                    gymRepo.save(gym);
                    return lt;
                });

        List<Pokemon> availablePokemon = pokemonRepo.findByOwnerIdAndTrainerIsNull(user);

        model.addAttribute("gym", gym);
        model.addAttribute("leader", leader);
        model.addAttribute("trainers", gym.getTrainers());
        model.addAttribute("availablePokemon", availablePokemon);
        model.addAttribute("money", user.getCurrency());

        return "playerGymEdit";
    }

    @PostMapping("/edit")
    public String saveGymEdit(
            @RequestParam List<Integer> pokemonIds) {
        User user = userDetails.getThisUser();

        Gym gym = gymRepo.findByOwnerAndNpcGymFalse(user)
                .orElseThrow();

        // Clear existing trainers (orphanRemoval = true handles cleanup)
        gym.getTrainers().clear();
        gymRepo.saveAndFlush(gym); // 👈 IMPORTANT

        int seq = 0;
        for (Integer pokemonId : pokemonIds) {
            Pokemon pokemon = pokemonRepo.findById(pokemonId)
                    .orElseThrow();

            // Security check
            if (!user.equals(pokemon.getOwnerId())) {
                throw new SecurityException("Not your Pokémon");
            }

            GymTrainer trainer = new GymTrainer();
            trainer.setGym(gym);
            trainer.setName(user.getUsername());
            trainer.setSequenceNumber(seq++);

            pokemon.setTrainer(trainer);

            trainer.setTeam(List.of(pokemon));
            gym.getTrainers().add(trainer);
        }

        gymRepo.save(gym);
        return "redirect:/player/gym";
    }

    @PostMapping("/leader/team")
    public String setLeaderTeam(@RequestParam List<Integer> pokemonIds) {
        User user = userDetails.getThisUser();
        Gym gym = gymRepo.findByOwnerAndNpcGymFalse(user).orElseThrow();

        GymTrainer leader = gym.getTrainers().stream()
                .filter(t -> t.getName().equals(user.getUsername()))
                .findFirst()
                .orElseThrow();

        leader.getTeam().clear();

        for (Integer id : pokemonIds) {
            Pokemon p = pokemonRepo.findById(id).orElseThrow();
            if (!user.equals(p.getOwnerId()))
                throw new SecurityException();

            p.setTrainer(leader);
            leader.getTeam().add(p);
        }

        gymRepo.save(gym);
        return "redirect:/player/gym/edit";
    }

    @GetMapping("/hire")
    public String hireTrainer(Model model) {
        User user = userDetails.getThisUser();

        GeneratedTrainer candidate = GeneratedTrainer.random();

        model.addAttribute("candidate", candidate);
        model.addAttribute("money", user.getCurrency());

        return "hireTrainer";
    }

    public record GeneratedTrainer(String name, int cost) {
        public static GeneratedTrainer random() {
            return new GeneratedTrainer(
                    TrainerNameGenerator.randomTrainerName(),
                    500 + new Random().nextInt(500));
        }
    }

    @PostMapping("/hire")
    public String confirmHire(
            @RequestParam String name,
            @RequestParam int cost) {

        User user = userDetails.getThisUser();
        if (user.getCurrency() < cost) {
            throw new IllegalStateException("Not enough money");
        }

        Gym gym = gymRepo.findByOwnerAndNpcGymFalse(user).orElseThrow();

        user.setCurrency(user.getCurrency() - cost);

        GymTrainer trainer = new GymTrainer();
        trainer.setName(name);
        trainer.setSequenceNumber(gym.getTrainers().size());
        trainer.setGym(gym);

        gym.getTrainers().add(trainer);
        gymRepo.save(gym);

        return "redirect:/player/gym/edit";
    }

    @GetMapping("/trainer/{trainerId}")
    public String editTrainer(
            @PathVariable Long trainerId,
            Model model) {

        User user = userDetails.getThisUser();

        GymTrainer trainer = gymTrainerRepo.findById(trainerId)
                .orElseThrow();

        Gym gym = trainer.getGym();

        // Security: must be owner's gym
        if (!user.equals(gym.getOwner())) {
            throw new SecurityException("Not your gym");
        }

        List<Pokemon> availablePokemon = pokemonRepo.findByOwnerIdAndTrainerIsNull(user);

        model.addAttribute("trainer", trainer);
        model.addAttribute("availablePokemon", availablePokemon);

        return "trainerEdit";
    }

    @PostMapping("/trainer/{trainerId}")
    public String saveTrainerPokemon(
            @PathVariable Long trainerId,
            @RequestParam(required = false) List<Integer> pokemonIds) {

        User user = userDetails.getThisUser();

        GymTrainer trainer = gymTrainerRepo.findById(trainerId)
                .orElseThrow();

        Gym gym = trainer.getGym();
        if (!user.equals(gym.getOwner())) {
            throw new SecurityException();
        }

        // Clear current team
        if (trainer.getTeam() != null) {
            trainer.getTeam().forEach(p -> p.setTrainer(null));
            trainer.getTeam().clear();
        }

        if (pokemonIds != null) {
            for (Integer id : pokemonIds) {
                Pokemon p = pokemonRepo.findById(id).orElseThrow();

                if (!user.equals(p.getOwnerId())) {
                    throw new SecurityException();
                }

                p.setTrainer(trainer);
                trainer.getTeam().add(p);
            }
        }

        gymTrainerRepo.save(trainer);
        return "redirect:/player/gym/edit";
    }

}
