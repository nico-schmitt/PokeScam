package com.PokeScam.PokeScam.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.SessionData;
import com.PokeScam.PokeScam.SessionData.BattleMenuState;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_MoveInfo;
import com.PokeScam.PokeScam.DTOs.PotionDTO;
import com.PokeScam.PokeScam.DTOs.ReviveDTO;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;
import com.PokeScam.PokeScam.DTOs.BattleAction;
import com.PokeScam.PokeScam.DTOs.BattleActionDTO;
import com.PokeScam.PokeScam.DTOs.ItemDTO;
import com.PokeScam.PokeScam.Services.ItemService;

@Service
public class EncounterService {

    private final SessionData sessionData;
    private final PokemonDataService pokemonDataService;
    private final PokemonCalcService pokemonCalcService;
    private final PokeAPIService pokeAPIService;
    private final PokemonRepository pokemonRepository;
    private final UserRepository userRepository;
    private final CustomUserDetails userDetails;
    private final ItemService itemService;

    private final Random rnd = new Random();

    // ==================== TYPES ====================

    public enum EncounterType {
        WildPokemon,
        Trainer
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
            pokemonToFightList = new ArrayList<>(pokemonToFightList);
        }

        public EncounterData withWon(boolean won) {
            return new EncounterData(order, encounterType, pokemonToFightList,
                    activePkmnToFightIdx, won, trainerUsername);
        }

        public EncounterData withNewActiveIdx(int idx) {
            return new EncounterData(order, encounterType, pokemonToFightList,
                    idx, encounterWon, trainerUsername);
        }
    }

    // ==================== CONSTRUCTOR ====================

    public EncounterService(
            PokemonDataService pokemonDataService,
            PokeAPIService pokeAPIService,
            PokemonCalcService pokemonCalcService,
            SessionData sessionData,
            UserRepository userRepository,
            CustomUserDetails userDetails,
            PokemonRepository pokemonRepository,
            ItemService itemService) {

        this.pokemonDataService = pokemonDataService;
        this.pokeAPIService = pokeAPIService;
        this.pokemonCalcService = pokemonCalcService;
        this.sessionData = sessionData;
        this.userRepository = userRepository;
        this.userDetails = userDetails;
        this.pokemonRepository = pokemonRepository;
        this.itemService = itemService;
    }

    // ==================== ENCOUNTER GENERATION ====================

    public List<EncounterData> getRandomEncounters() {
        int count = rnd.nextInt(2, 4);
        List<EncounterData> encounters = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            EncounterType type = rnd.nextBoolean() ? EncounterType.WildPokemon : EncounterType.Trainer;

            List<EncounterDataSinglePkmn> enemies = new ArrayList<>();
            String trainerName = "";

            if (type == EncounterType.WildPokemon) {
                PokemonDTO wild = pokeAPIService.getRandomPokemon();
                wild = wild.curHp() <= 0 ? wild.withNewHealth(wild.maxHp()) : wild;
                enemies.add(new EncounterDataSinglePkmn(wild, false));
            } else {
                // Decide if this is a gym trainer (DB) or path trainer (random)
                boolean isGymTrainer = rnd.nextBoolean(); // Replace with actual gym check
                if (isGymTrainer) {
                    User trainer = userRepository.findRandomUser(userDetails.getThisUser().getId());
                    trainerName = trainer.getUsername();
                    pokemonDataService.getPkmnTeamInfoOfUser(trainer)
                            .stream()
                            .filter(p -> !p.equals(PokemonDTO.getEmpty()))
                            .map(p -> p.curHp() <= 0 ? p.withNewHealth(p.maxHp()) : p)
                            .forEach(p -> enemies.add(new EncounterDataSinglePkmn(p, false)));
                } else {
                    // Path trainer: generate random Pokémon
                    for (int j = 0; j < rnd.nextInt(1, 4); j++) {
                        PokemonDTO randomPkmn = pokeAPIService.getRandomPokemon();
                        randomPkmn = randomPkmn.curHp() <= 0 ? randomPkmn.withNewHealth(randomPkmn.maxHp())
                                : randomPkmn;
                        enemies.add(new EncounterDataSinglePkmn(randomPkmn, false));
                    }
                    trainerName = "Trainer " + (i + 1);
                }
            }

            encounters.add(new EncounterData(i, type, enemies, 0, false, trainerName));
        }

        sessionData.setEncounterProgress(0);
        sessionData.setSavedEncounterList(encounters);
        sessionData.setBattleMenuState(BattleMenuState.ACTION_SELECT);

        return encounters;
    }

    // ==================== ACCESSORS ====================

    public EncounterData getEncounterDataAtIdx(int idx) {
        List<EncounterData> list = sessionData.getSavedEncounterList();
        if (list == null || list.isEmpty() || idx >= list.size()) {
            throw new IllegalStateException("No encounter exists at index " + idx);
        }
        return list.get(idx);
    }

    public List<EncounterDataSinglePkmn> getPokemonToFightListAtIdx(int idx) {
        return getEncounterDataAtIdx(idx).pokemonToFightList();
    }

    public EncounterDataSinglePkmn getEnemyActivePkmnAtIdx(int idx) {
        EncounterData enc = getEncounterDataAtIdx(idx);
        List<EncounterDataSinglePkmn> enemies = enc.pokemonToFightList();

        if (enemies.isEmpty()) {
            if (enc.encounterType() == EncounterType.Trainer) {
                // Random path trainer: generate Pokémon if none exist
                List<EncounterDataSinglePkmn> randomEnemies = new ArrayList<>();
                int count = rnd.nextInt(1, 4); // 1-3 Pokémon
                for (int i = 0; i < count; i++) {
                    PokemonDTO randomPkmn = pokeAPIService.getRandomPokemon();
                    randomPkmn = randomPkmn.curHp() <= 0 ? randomPkmn.withNewHealth(randomPkmn.maxHp()) : randomPkmn;
                    randomEnemies.add(new EncounterDataSinglePkmn(randomPkmn, false));
                }

                // Replace the empty list in the encounter
                EncounterData updated = new EncounterData(
                        enc.order(),
                        enc.encounterType(),
                        randomEnemies,
                        0,
                        false,
                        enc.trainerUsername());
                sessionData.getSavedEncounterList().set(idx, updated);
                return randomEnemies.get(0);
            } else {
                throw new IllegalStateException("Encounter has no Pokémon at index " + idx);
            }
        }

        int activeIdx = enc.activePkmnToFightIdx();
        if (activeIdx < 0 || activeIdx >= enemies.size()) {
            throw new IllegalStateException("Active Pokémon index out of bounds: " + activeIdx);
        }

        return enemies.get(activeIdx);
    }

    public List<EncounterDataSinglePkmn> getPkmnTeamInfo() {
        return pokemonDataService.getPkmnTeamInfoDTO()
                .stream()
                .map(p -> new EncounterDataSinglePkmn(p, p.curHp() <= 0))
                .collect(Collectors.toList());
    }

    public EncounterDataSinglePkmn wrapPkmnInEncounterData(PokemonDTO p) {
        return new EncounterDataSinglePkmn(p, p.curHp() <= 0);
    }

    // ==================== ACTION ENTRY POINT ====================

    public NotificationMsg executeTurn(BattleActionDTO action) {
        switch (action.action()) {
            case FIGHT -> {
                Integer moveIdx = action.moveIdx();
                if (moveIdx == null)
                    return new NotificationMsg("No move selected!", false);
                return executeFight(moveIdx);
            }
            case FLEE -> {
                return executeFlee();
            }
            case SWITCH -> {
                Integer switchIdx = action.switchIdx();
                if (switchIdx == null)
                    return new NotificationMsg("No Pokémon selected to switch!", false);
                return executeSwitch(switchIdx);
            }
            case ITEM -> {
                Integer itemIdx = action.itemIdx();
                if (itemIdx == null)
                    return new NotificationMsg("No item selected!", false);
                return useItem(itemIdx);
            }
            default -> {
                return new NotificationMsg("Invalid action", false);
            }
        }
    }

    public NotificationMsg fleeCurrentEncounter() {
        EncounterData enc = getEncounterDataAtIdx(sessionData.getEncounterProgress());
        sessionData.setEncounterProgress(sessionData.getEncounterProgress() + 1);
        return new NotificationMsg("You fled successfully!", true);
    }

    public NotificationMsg switchActivePokemon(int idx) {
        // TODO: check if selected Pokémon is alive and swap with active
        return new NotificationMsg("Switched Pokémon!", false);
    }

    private NotificationMsg useItem(int itemIdx) {
        // Get all usable battle items
        List<ItemDTO> items = itemService.getBattleItems();

        if (itemIdx < 0 || itemIdx >= items.size()) {
            return new NotificationMsg("Invalid item selection.", false);
        }

        ItemDTO item = items.get(itemIdx);
        Pokemon active = pokemonDataService.getActivePkmn();

        // ---------- POTION ----------
        if (item instanceof PotionDTO potion) {

            if (active.getCurHp() <= 0) {
                return new NotificationMsg("You can't use a Potion on a fainted Pokémon.", false);
            }

            // Heal the active Pokémon
            pokemonDataService.healPkmnForCost(
                    active.getId(),
                    0, // cost already handled elsewhere if needed
                    potion.healAmountPotion());

            // Decrement the item in inventory
            itemService.decrementItem(item.id());
            pokemonRepository.save(active);

            return new NotificationMsg(
                    active.getName() + " recovered HP!",
                    true);
        }

        // ---------- REVIVE ----------
        if (item instanceof ReviveDTO revive) {

            if (active.getCurHp() > 0) {
                return new NotificationMsg("That Pokémon is not fainted.", false);
            }

            int healAmount = (int) (active.getMaxHp() * revive.healPercentageRevive());

            pokemonDataService.healPkmnForCost(
                    active.getId(),
                    0,
                    healAmount);

            itemService.decrementItem(item.id());
            pokemonRepository.save(active);

            return new NotificationMsg(
                    active.getName() + " was revived!",
                    true);
        }

        // ---------- OTHER ITEM TYPES ----------
        return new NotificationMsg("That item can't be used in battle.", false);
    }

    // ==================== FIGHT ====================
    private NotificationMsg executeFight(Integer moveIdx) {

        if (moveIdx == null) {
            return new NotificationMsg("No move selected!", false);
        }

        int encounterIdx = sessionData.getEncounterProgress();
        EncounterData enc = getEncounterDataAtIdx(encounterIdx);

        // ==================== PLAYER VALIDATION ====================
        Pokemon myActive = pokemonDataService.getActivePkmn();
        PokemonDTO myDTO = pokemonDataService.getActivePkmnDTO();

        if (myActive.getCurHp() <= 0) {
            return new NotificationMsg("Your Pokémon has fainted! Switch Pokémon.", false);
        }

        // ==================== ENEMY SETUP ====================
        EncounterDataSinglePkmn enemyWrap = getEnemyActivePkmnAtIdx(encounterIdx);
        PokemonDTO enemyDTO = enemyWrap.pkmn();

        Pokemon enemy = new Pokemon();
        pokemonDataService.populatePkmnWithPkmnDTOValues(enemy, enemyDTO);

        // ==================== PLAYER ATTACK ====================
        PokemonDTO_MoveInfo move = myDTO.allMoves().moves().get(moveIdx);
        int dmgToEnemy = pokemonCalcService.calcMoveDamage(myDTO, enemyDTO, move);
        pokemonDataService.adjustPkmnHealth(enemy, -dmgToEnemy);

        boolean enemyDefeated = enemy.getCurHp() <= 0;

        StringBuilder battleMsg = new StringBuilder();
        battleMsg.append(myDTO.displayName())
                .append(" used ")
                .append(move.displayName());

        // ==================== UPDATE ENEMY STATE ====================
        List<EncounterDataSinglePkmn> updatedEnemies = new ArrayList<>(enc.pokemonToFightList());

        int activeEnemyIdx = enc.activePkmnToFightIdx();
        updatedEnemies.set(
                activeEnemyIdx,
                new EncounterDataSinglePkmn(
                        enemyDTO.withNewHealth(Math.max(0, enemy.getCurHp())),
                        enemyDefeated));

        EncounterData updatedEncounter = enc;

        // ==================== ENEMY DEFEATED ====================
        if (enemyDefeated) {

            // ---- EXP ----
            int expGained = pokemonCalcService.calculateExpGain(myDTO, enemyDTO);
            String expMsg = pokemonCalcService.gainExpAndCheckLevelUp(myActive, expGained);
            pokemonRepository.save(myActive);

            battleMsg.append(". ").append(expMsg);

            // ---- FIND NEXT ENEMY ----
            int nextEnemyIdx = -1;
            for (int i = activeEnemyIdx + 1; i < updatedEnemies.size(); i++) {
                if (!updatedEnemies.get(i).isDefeated()) {
                    nextEnemyIdx = i;
                    break;
                }
            }

            // ---- MORE ENEMIES REMAIN ----
            if (nextEnemyIdx != -1) {
                updatedEncounter = new EncounterData(
                        enc.order(),
                        enc.encounterType(),
                        updatedEnemies,
                        nextEnemyIdx,
                        false,
                        enc.trainerUsername());
            }
            // ---- ENCOUNTER FINISHED ----
            else {
                int moneyWon = 0;
                if (enc.encounterType() == EncounterType.Trainer) {
                    moneyWon = rewardPlayer(enc);
                    battleMsg.append(" You earned $").append(moneyWon).append("!");
                }

                updatedEncounter = new EncounterData(
                        enc.order(),
                        enc.encounterType(),
                        updatedEnemies,
                        activeEnemyIdx,
                        true,
                        enc.trainerUsername());

                sessionData.setEncounterProgress(encounterIdx + 1);
            }
        }

        // ==================== ENEMY COUNTERATTACK ====================
        else {
            PokemonDTO_MoveInfo enemyMove = enemyDTO.allMoves().moves()
                    .get(rnd.nextInt(enemyDTO.allMoves().moves().size()));

            int dmgToPlayer = pokemonCalcService.calcMoveDamage(enemyDTO, myDTO, enemyMove);
            pokemonDataService.adjustPkmnHealth(myActive, -dmgToPlayer);
            pokemonRepository.save(myActive);

            battleMsg.append(". Enemy used ").append(enemyMove.displayName());

            updatedEncounter = new EncounterData(
                    enc.order(),
                    enc.encounterType(),
                    updatedEnemies,
                    activeEnemyIdx,
                    false,
                    enc.trainerUsername());
        }

        // ==================== PERSIST ENCOUNTER ====================
        sessionData.getSavedEncounterList().set(encounterIdx, updatedEncounter);
        sessionData.setBattleMenuState(SessionData.BattleMenuState.ACTION_SELECT);

        return new NotificationMsg(battleMsg.toString(), false);
    }

    // ---------------- HELPERS ----------------

    private void updateEncounterEnemy(EncounterData enc, PokemonDTO newEnemyDTO, boolean isDefeated) {
        int activeIdx = enc.activePkmnToFightIdx();
        enc.pokemonToFightList().set(activeIdx, new EncounterDataSinglePkmn(newEnemyDTO, isDefeated));
    }

    private EncounterData handleEnemyDefeat(
            EncounterData enc,
            Pokemon myActive,
            PokemonDTO enemyDTO,
            StringBuilder battleMsg) {

        int expGained = pokemonCalcService.calculateExpGain(
                pokemonDataService.getActivePkmnDTO(), enemyDTO);

        String expMsg = pokemonCalcService.gainExpAndCheckLevelUp(myActive, expGained);
        pokemonRepository.save(myActive);

        battleMsg.append(". ").append(expMsg);

        // Mark current enemy defeated
        List<EncounterDataSinglePkmn> updatedEnemies = new ArrayList<>(enc.pokemonToFightList());
        updatedEnemies.set(
                enc.activePkmnToFightIdx(),
                new EncounterDataSinglePkmn(enemyDTO.withNewHealth(0), true));

        // Find next alive enemy
        int nextIdx = -1;
        for (int i = enc.activePkmnToFightIdx() + 1; i < updatedEnemies.size(); i++) {
            if (!updatedEnemies.get(i).isDefeated()) {
                nextIdx = i;
                break;
            }
        }

        // Case 1: more enemies remain
        if (nextIdx != -1) {
            return new EncounterData(
                    enc.order(),
                    enc.encounterType(),
                    updatedEnemies,
                    nextIdx,
                    false,
                    enc.trainerUsername());
        }

        // Case 2: encounter finished
        int moneyWon = 0;
        if (enc.encounterType() == EncounterType.Trainer) {
            moneyWon = rewardPlayer(enc);
            battleMsg.append(" You earned $").append(moneyWon).append("!");
        }

        sessionData.setEncounterProgress(sessionData.getEncounterProgress() + 1);

        return new EncounterData(
                enc.order(),
                enc.encounterType(),
                updatedEnemies,
                enc.activePkmnToFightIdx(),
                true,
                enc.trainerUsername());
    }

    private boolean isTrainerDefeated(EncounterData enc) {
        return enc.pokemonToFightList().stream().allMatch(EncounterDataSinglePkmn::isDefeated);
    }

    private int rewardPlayer(EncounterData enc) {
        if (enc.encounterType() != EncounterService.EncounterType.Trainer)
            return 0;

        User user = userDetails.getThisUser();
        int money = 200;
        user.setCurrency(user.getCurrency() + money);
        userRepository.save(user);
        return money;
    }

    // ==================== SWITCH ====================

    private NotificationMsg executeSwitch(int teamIdx) {
        Pokemon newActive = pokemonDataService.getPkmnTeamInfo().get(teamIdx);
        Pokemon current = pokemonDataService.getActivePkmn();

        pokemonDataService.setNewActivePkmn(newActive, current);
        sessionData.setBattleMenuState(BattleMenuState.ACTION_SELECT);

        return new NotificationMsg("Go! " + newActive.getName() + "!", false);
    }

    // ==================== FLEE ====================

    private NotificationMsg executeFlee() {
        EncounterData enc = getEncounterDataAtIdx(sessionData.getEncounterProgress());

        if (enc.encounterType() == EncounterType.Trainer) {
            return new NotificationMsg("You can't flee from a trainer battle!", false);
        }

        sessionData.setEncounterProgress(sessionData.getEncounterProgress() + 1);
        sessionData.setBattleMenuState(BattleMenuState.ACTION_SELECT);

        return new NotificationMsg("You fled successfully!", true);
    }

    private NotificationMsg finalizeEncounterIfDone(EncounterData enc, NotificationMsg previousNotif) {
        boolean allDefeated = enc.pokemonToFightList()
                .stream()
                .allMatch(EncounterDataSinglePkmn::isDefeated);

        if (!allDefeated) {
            return previousNotif;
        }

        sessionData.getSavedEncounterList().set(
                sessionData.getEncounterProgress(),
                enc.withWon(true));

        sessionData.setEncounterProgress(sessionData.getEncounterProgress() + 1);

        // Final reward for encounter if applicable
        int moneyWon = 0;
        if (enc.encounterType() == EncounterType.Trainer) {
            moneyWon = rewardPlayer(enc);
        }

        String message = previousNotif.msg();
        if (enc.encounterType() == EncounterType.Trainer && moneyWon > 0) {
            message += " Encounter finished! You won $" + moneyWon + "!";
        }

        return new NotificationMsg(message, true);
    }

}
