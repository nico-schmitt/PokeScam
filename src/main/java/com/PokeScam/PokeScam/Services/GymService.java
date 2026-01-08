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
import com.PokeScam.PokeScam.Model.GymTrainerPokemon;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Repos.GymRepository;
import com.PokeScam.PokeScam.Repos.GymTrainerPokemonRepository;
import com.PokeScam.PokeScam.Repos.GymTrainerRepository;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterData;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterDataSinglePkmn;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.PokemonDataService.PokemonWithMovesDTO;

@Service
public class GymService {

    private final GymRepository gymRepo;
    private final GymTrainerRepository trainerRepo;
    private final GymTrainerPokemonRepository trainerPkmnRepo;
    private final PokemonDataService pokemonDataService;

    public GymService(
            GymRepository gymRepo,
            GymTrainerRepository trainerRepo,
            GymTrainerPokemonRepository trainerPkmnRepo,
            PokemonDataService pokemonDataService) {
        this.gymRepo = gymRepo;
        this.trainerRepo = trainerRepo;
        this.trainerPkmnRepo = trainerPkmnRepo;
        this.pokemonDataService = pokemonDataService;
    }

    /** Return all NPC gyms */
    public List<Gym> getAllNpcGyms() {
        return gymRepo.findByNpcGymTrue();
    }

    /** Return a single gym by ID */
    public Optional<Gym> getGymById(Long gymId) {
        return gymRepo.findById(gymId);
    }

    public List<EncounterData> getGymEncounters(Long gymId) {
        List<EncounterData> encounters = new ArrayList<>();

        Gym gym = gymRepo.findById(gymId)
                .orElseThrow(() -> new IllegalArgumentException("Gym not found"));

        List<GymTrainer> trainers = trainerRepo.findByGymOrderBySequenceNumberAsc(gym);

        for (GymTrainer trainer : trainers) {
            List<GymTrainerPokemon> trainerPkmnList = trainerPkmnRepo.findByTrainerOrderByIdAsc(trainer);
            List<EncounterDataSinglePkmn> encounterPkmn = new ArrayList<>();

            for (GymTrainerPokemon gtp : trainerPkmnList) {
                // Build a minimal PokemonDTO for the battle
                PokemonDTO pkmnDto = new PokemonDTO(
                        gtp.getSpeciesId(), // id
                        false, // isInBox
                        gtp.getSpeciesName().toLowerCase().replace(" ", "-"), // apiName
                        gtp.getSpeciesName(), // displayName
                        "", // imageURL
                        "", // flavorText
                        gtp.getLevel(), // level
                        0, // exp
                        100, // maxHp (example, adjust as needed)
                        100, // curHp
                        null, // allStats
                        null, // allMoves
                        false, // isActivePkmn
                        false, // seen
                        false // caught
                );

                encounterPkmn.add(new EncounterDataSinglePkmn(pkmnDto, true));
            }

            EncounterData encounter = new EncounterData(
                    trainer.getName(),
                    encounterPkmn,
                    null);

            encounters.add(encounter);
        }

        return encounters;
    }

    /** Get trainers and their Pokémon for a gym */
    public List<Map<String, Object>> getGymTrainersWithPokemon(Long gymId) {
        Gym gym = gymRepo.findById(gymId)
                .orElseThrow(() -> new IllegalArgumentException("Gym not found"));
        List<GymTrainer> trainers = trainerRepo.findByGymOrderBySequenceNumberAsc(gym);

        List<Map<String, Object>> trainerList = new ArrayList<>();
        for (GymTrainer trainer : trainers) {
            Map<String, Object> tMap = new HashMap<>();
            tMap.put("trainer", trainer);

            // Convert each GymTrainerPokemon to a minimal PokemonDTO
            List<PokemonDTO> pokemons = trainerPkmnRepo.findByTrainerOrderByIdAsc(trainer)
                    .stream()
                    .map(gtp -> new PokemonDTO(
                            gtp.getSpeciesId(), // id
                            false, // isInBox
                            gtp.getSpeciesName().toLowerCase().replace(" ", "-"), // apiName
                            gtp.getSpeciesName(), // displayName
                            "", // imageURL
                            "", // flavorText
                            gtp.getLevel(), // level
                            0, // exp
                            100, // maxHp (example)
                            100, // curHp (example)
                            null, // allStats
                            null, // allMoves
                            false, // isActivePkmn
                            false, // seen
                            false // caught
                    ))
                    .toList();

            tMap.put("pokemons", pokemons);
            trainerList.add(tMap);
        }

        return trainerList;
    }

}
