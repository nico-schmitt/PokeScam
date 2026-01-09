package com.PokeScam.PokeScam.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_MoveInfo;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.SessionData;

@Service
public class EncounterService {

    private final UserRepository userRepository;
    private final PokemonRepository pokemonRepository;
    private final SessionData sessionData;
    private final PokeAPIService pokeAPIService;
    private final PokemonDataService pokemonDataService;
    private final PokemonCalcService pokemonCalcService;
    private final CustomUserDetails userDetails;

    private final Random rnd = new Random();

    private enum EncounterType {
        WildPokemon, Trainer
    }

    public record EncounterDataSinglePkmn(PokemonDTO pkmn, boolean isDefeated) {
    }

    public record EncounterData(
            int order,
            EncounterType encounterType,
            List<EncounterDataSinglePkmn> pokemonToFightList,
            int activePkmnToFightIdx,
            boolean encounterWon,
            String trainerUsername) {

        public EncounterData {
            pokemonToFightList = new ArrayList<>(pokemonToFightList); // <- ensure mutability
        }

        public EncounterData(String trainerUsername, List<EncounterDataSinglePkmn> pokemonToFightList,
                String trainerSprite) {
            this(0, EncounterType.Trainer, pokemonToFightList, 0, false, trainerUsername);
        }

        public EncounterData withWon(boolean won) {
            return new EncounterData(this.order, this.encounterType, this.pokemonToFightList,
                    this.activePkmnToFightIdx, won, this.trainerUsername);
        }

        public EncounterData withNewActiveIdx(int activePkmnToFightIdx) {
            return new EncounterData(this.order, this.encounterType, this.pokemonToFightList,
                    activePkmnToFightIdx, this.encounterWon, this.trainerUsername);
        }
    }

    public EncounterService(PokemonDataService pokemonDataService, PokeAPIService pokeAPIService,
            PokemonCalcService pokemonCalcService, SessionData sessionData, UserRepository userRepository,
            CustomUserDetails userDetails, PokemonRepository pokemonRepository) {
        this.pokemonDataService = pokemonDataService;
        this.pokeAPIService = pokeAPIService;
        this.pokemonCalcService = pokemonCalcService;
        this.sessionData = sessionData;
        this.userRepository = userRepository;
        this.userDetails = userDetails;
        this.pokemonRepository = pokemonRepository;
    }

    // ==================== ENCOUNTER GENERATION ====================

    public List<EncounterData> getRandomEncounters() {
        int encounterCount = rnd.nextInt(2, 4); // random 2-3 encounters
        List<EncounterData> randomEncounterList = new ArrayList<>();

        for (int i = 0; i < encounterCount; i++) {
            EncounterType type = rnd.nextFloat() < 0.5f ? EncounterType.WildPokemon : EncounterType.Trainer;
            List<EncounterDataSinglePkmn> pokemonToFight = new ArrayList<>();
            String trainerUsername = "";

            if (type == EncounterType.WildPokemon) {
                PokemonDTO wildPkmn = pokeAPIService.getRandomPokemon();
                if (wildPkmn.curHp() <= 0 && wildPkmn.maxHp() > 0) {
                    wildPkmn = wildPkmn.withNewHealth(wildPkmn.maxHp());
                }
                pokemonToFight.add(new EncounterDataSinglePkmn(wildPkmn, false));
            } else {
                // Get a random trainer with at least one Pokémon
                User trainer = userRepository.findRandomUser(userDetails.getThisUser().getId());
                List<Pokemon> team = pokemonRepository.findByOwnerIdAndInBoxFalse(trainer);

                int attempts = 0;
                while (team.isEmpty() && attempts < 10) {
                    trainer = userRepository.findRandomUser(userDetails.getThisUser().getId());
                    team = pokemonRepository.findByOwnerIdAndInBoxFalse(trainer);
                    attempts++;
                }

                if (team.isEmpty()) {
                    // skip this encounter if no valid trainer
                    continue;
                }

                // Convert Pokémon to mutable DTO list and filter empty Pokémon
                List<PokemonDTO> teamDTOs = pokemonDataService.getPkmnTeamInfoOfUser(trainer)
                        .stream()
                        .filter(p -> !p.equals(PokemonDTO.getEmpty()))
                        .map(p -> {
                            if (p.curHp() <= 0 && p.maxHp() > 0) {
                                p = p.withNewHealth(p.maxHp());
                            }
                            return p;
                        })
                        .collect(Collectors.toCollection(ArrayList::new)); // mutable list

                teamDTOs.forEach(p -> pokemonToFight.add(new EncounterDataSinglePkmn(p, false)));
                trainerUsername = trainer.getUsername();
            }

            randomEncounterList.add(new EncounterData(i, type, pokemonToFight, 0, false, trainerUsername));
        }

        return randomEncounterList;
    }

    // ==================== ENCOUNTER DATA ACCESS ====================

    public EncounterData getEncounterDataAtIdx(int idx) {
        return sessionData.getSavedEncounterList().get(idx);
    }

    public List<EncounterDataSinglePkmn> getPokemonToFightListAtIdx(int idx) {
        return getEncounterDataAtIdx(idx).pokemonToFightList;
    }

    public EncounterDataSinglePkmn getEnemyActivePkmnAtIdx(int idx) {
        EncounterData enc = getEncounterDataAtIdx(idx);
        return enc.pokemonToFightList.get(enc.activePkmnToFightIdx);
    }

    public List<EncounterDataSinglePkmn> getPkmnTeamInfo() {
        List<PokemonDTO> teamDTOs = pokemonDataService.getPkmnTeamInfoDTO();
        List<EncounterDataSinglePkmn> wrapped = new ArrayList<>();
        teamDTOs.forEach(p -> wrapped.add(new EncounterDataSinglePkmn(p, p.curHp() <= 0)));
        return wrapped;
    }

    public EncounterDataSinglePkmn wrapPkmnInEncounterData(PokemonDTO pkmn) {
        return new EncounterDataSinglePkmn(pkmn, pkmn.curHp() <= 0);
    }

    // ==================== BATTLE TURN LOGIC ====================

    public NotificationMsg executeTurn(int moveIdx) {
        EncounterData enc = sessionData.getSavedEncounterList().get(sessionData.getEncounterProgress());

        // Active Pokémon
        PokemonDTO myActiveDTO = pokemonDataService.getPkmnTeamInfoDTO().stream()
                .filter(PokemonDTO::isActivePkmn)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active Pokémon found"));

        Pokemon myActive = pokemonDataService.getPkmnTeamInfo().stream()
                .filter(Pokemon::isActivePkmn)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active Pokémon found"));

        // Enemy Pokémon
        EncounterDataSinglePkmn enemySingle = enc.pokemonToFightList.get(enc.activePkmnToFightIdx);
        PokemonDTO enemyDTO = enemySingle.pkmn;
        Pokemon enemy = new Pokemon();
        pokemonDataService.populatePkmnWithPkmnDTOValues(enemy, enemyDTO);

        // ===== Player Move =====
        List<PokemonDTO_MoveInfo> moves = myActiveDTO.allMoves().moves();
        if (moveIdx < 0 || moveIdx >= moves.size()) {
            throw new IllegalArgumentException("Invalid move index: " + moveIdx);
        }
        PokemonDTO_MoveInfo move = moves.get(moveIdx);

        int dmgToEnemy = pokemonCalcService.calcMoveDamage(myActiveDTO, enemyDTO, move);
        int actualDmgToEnemy = pokemonDataService.adjustPkmnHealth(enemy, -dmgToEnemy);
        boolean enemyDefeated = enemy.getCurHp() <= 0;

        // Update enemy DTO
        PokemonDTO updatedEnemyDTO = enemyDTO.withNewHealth(enemy.getCurHp());
        enc.pokemonToFightList.set(enc.activePkmnToFightIdx,
                new EncounterDataSinglePkmn(updatedEnemyDTO, enemyDefeated));

        if (enemyDefeated && enc.activePkmnToFightIdx < enc.pokemonToFightList.size() - 1) {
            enc = enc.withNewActiveIdx(enc.activePkmnToFightIdx + 1);
            sessionData.getSavedEncounterList().set(sessionData.getEncounterProgress(), enc);
        }

        // Gain EXP if enemy defeated
        if (enemyDefeated) {
            int expGained = pokemonCalcService.calculateExpGain(myActiveDTO, enemyDTO);
            pokemonCalcService.gainExp(myActive, expGained);
            pokemonDataService.checkLevelUp(myActive);
        }

        // ===== Enemy Move =====
        List<PokemonDTO_MoveInfo> enemyMoves = enemyDTO.allMoves().moves();
        if (enemyMoves.isEmpty()) {
            throw new IllegalStateException("Enemy has no moves!");
        }
        PokemonDTO_MoveInfo enemyMove = enemyMoves.get(rnd.nextInt(enemyMoves.size()));

        int dmgToPlayer = pokemonCalcService.calcMoveDamage(enemyDTO, myActiveDTO, enemyMove);
        int actualDmgToPlayer = pokemonDataService.adjustPkmnHealth(myActive, -dmgToPlayer);
        pokemonRepository.save(myActive);

        // ===== Notifications =====
        int totalEnemyHp = enc.pokemonToFightList.stream().mapToInt(p -> p.pkmn.curHp()).sum();
        NotificationMsg msg;

        if (totalEnemyHp == 0) {
            enc = enc.withWon(true);
            sessionData.getSavedEncounterList().set(sessionData.getEncounterProgress(), enc);
            sessionData.setEncounterProgress(sessionData.getEncounterProgress() + 1);
            msg = new NotificationMsg("You won! Redirecting to path in 3 seconds.", true);
        } else {
            String battleMsg = String.format(
                    "%s used %s! %s took %d damage!\n%s used %s! You (%s) took %d damage!",
                    myActiveDTO.displayName(), move.displayName(), enemyDTO.displayName(), Math.abs(actualDmgToEnemy),
                    enemyDTO.displayName(), enemyMove.displayName(), myActiveDTO.displayName(),
                    Math.abs(actualDmgToPlayer));
            msg = new NotificationMsg(battleMsg, false);
        }

        return msg;
    }
}
