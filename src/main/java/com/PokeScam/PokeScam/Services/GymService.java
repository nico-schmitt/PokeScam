package com.PokeScam.PokeScam.Services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Model.GymTrainer;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Repos.GymRepository;
import com.PokeScam.PokeScam.Repos.GymTrainerRepository;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterData;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterDataSinglePkmn;

@Service
public class GymService {

    private final GymRepository gymRepo;
    private final GymTrainerRepository trainerRepo;
    private final PokemonRepository pokemonRepo;
    private final PokemonDataService pokemonDataService;

    public GymService(
            GymRepository gymRepo,
            GymTrainerRepository trainerRepo,
            PokemonRepository pokemonRepo,
            PokemonDataService pokemonDataService) {

        this.gymRepo = gymRepo;
        this.trainerRepo = trainerRepo;
        this.pokemonRepo = pokemonRepo;
        this.pokemonDataService = pokemonDataService;
    }

    /** All NPC gyms */
    public List<Gym> getAllNpcGyms() {
        return gymRepo.findByNpcGymTrue();
    }

    public Optional<Gym> getGymById(Long gymId) {
        return gymRepo.findById(gymId);
    }

    /**
     * Build encounter chain for a gym
     */
    public List<EncounterData> getGymEncounters(Long gymId) {
        Gym gym = gymRepo.findById(gymId)
                .orElseThrow(() -> new IllegalArgumentException("Gym not found"));

        List<GymTrainer> trainers = trainerRepo.findByGymOrderBySequenceNumberAsc(gym);

        List<EncounterData> encounters = new ArrayList<>();

        if (!gym.isNpcGym()) {
            // Player gym: leader (sequence 0) last
            List<GymTrainer> hiredTrainers = trainers.stream()
                    .filter(t -> t.getSequenceNumber() != 0)
                    .toList();

            GymTrainer leader = trainers.stream()
                    .filter(t -> t.getSequenceNumber() == 0)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Player gym must have a leader"));

            // Add hired trainers first
            for (GymTrainer trainer : hiredTrainers) {
                encounters.addAll(generateTrainerEncounter(trainer));
            }

            // Add the player leader last
            encounters.addAll(generateTrainerEncounter(leader));

        } else {
            // NPC gym: just use the order as-is
            for (GymTrainer trainer : trainers) {
                encounters.addAll(generateTrainerEncounter(trainer));
            }
        }

        return encounters;
    }

    /** Extracted helper for cleaner code */
    private List<EncounterData> generateTrainerEncounter(GymTrainer trainer) {
        List<Pokemon> trainerPokemon = pokemonRepo.findByTrainerOrderByIdAsc(trainer);

        List<EncounterDataSinglePkmn> encounterPkmn = trainerPokemon.stream()
                .map(pkmn -> {
                    PokemonDTO dto = pokemonDataService
                            .convertToPokemonDTO(
                                    pokemonDataService.getPokemonWithMovesDTO(pkmn));
                    return new EncounterDataSinglePkmn(dto, false);
                })
                .toList();

        EncounterData encounter = new EncounterData(
                0,
                EncounterService.EncounterType.Trainer,
                encounterPkmn,
                0,
                false,
                trainer.getName());

        return List.of(encounter);
    }

    /**
     * Used for gym overview / UI
     */
    public List<Map<String, Object>> getGymTrainersWithPokemon(Long gymId) {
        Gym gym = gymRepo.findById(gymId)
                .orElseThrow(() -> new IllegalArgumentException("Gym not found"));

        List<GymTrainer> trainers = trainerRepo.findByGymOrderBySequenceNumberAsc(gym);

        List<Map<String, Object>> result = new ArrayList<>();

        for (GymTrainer trainer : trainers) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("trainer", trainer);

            List<PokemonDTO> pokemonDtos = pokemonRepo.findByTrainerOrderByIdAsc(trainer)
                    .stream()
                    .map(pkmn -> pokemonDataService.convertToPokemonDTO(
                            pokemonDataService.getPokemonWithMovesDTO(pkmn)))
                    .toList();

            entry.put("pokemons", pokemonDtos);
            result.add(entry);
        }

        return result;
    }

    public List<Gym> getAllPlayerGyms() {
        return gymRepo.findByNpcGymFalse();
    }

}
