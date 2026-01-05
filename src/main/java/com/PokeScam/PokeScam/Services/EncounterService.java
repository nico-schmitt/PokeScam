package com.PokeScam.PokeScam.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;
import com.PokeScam.PokeScam.Services.PokeAPIService.MoveInfo;
import com.PokeScam.PokeScam.SessionData;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_MoveInfo;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;


@Service
public class EncounterService {

    private final UserRepository userRepository;
    private final PokemonRepository pokemonRepository;

    private final SessionData sessionData;
    private final PokeAPIService pokeAPIService;
    private final PokemonDataService pokemonDataService;
    private final PokemonCalcService pokemonCalcService;
    
    private final CustomUserDetails userDetails;

    private enum EncounterType {WildPokemon, Trainer}

    public record EncounterDataSinglePkmn(PokemonDTO pkmn, boolean isDefeated) {}
    public record EncounterData(int order, EncounterType encounterType, List<EncounterDataSinglePkmn> pokemonToFightList, int activePkmnToFightIdx, boolean encounterWon, String trainerUsername) {
        public EncounterData withWon(boolean won) {
            return new EncounterData(this.order, this.encounterType, this.pokemonToFightList, this.activePkmnToFightIdx, won, this.trainerUsername);
        }
    }

    private Random rnd;
    
    public EncounterService(PokemonDataService pokemonDataService, PokeAPIService pokeAPIService, PokemonCalcService pokemonCalcService, SessionData sessionData, UserRepository userRepository, CustomUserDetails userDetails, PokemonRepository pokemonRepository) {
        rnd = new Random();
        this.pokemonDataService = pokemonDataService;
        this.pokeAPIService = pokeAPIService;
        this.pokemonCalcService = pokemonCalcService;
        this.sessionData = sessionData;
        this.userRepository = userRepository;
        this.userDetails = userDetails;
        this.pokemonRepository = pokemonRepository;
    }

    public List<EncounterData> getRandomEncounters() {
        int encounterCount = rnd.nextInt(2, 4);
        List<EncounterData> randomEncounterList = new ArrayList<>();
        for (int i = 0; i < encounterCount; i++) {
            EncounterType encounterType;
            float encounterTypeChance = rnd.nextFloat();
            if(encounterTypeChance < 0.5f) encounterType = EncounterType.WildPokemon;
            else encounterType = EncounterType.Trainer;
            List<EncounterDataSinglePkmn> pokemonToFight = new ArrayList<>();
            String trainerUsername = "";
            if(encounterType == EncounterType.WildPokemon) {
                pokemonToFight.add(new EncounterDataSinglePkmn(pokeAPIService.getRandomPokemon(), false));
            } else {
                User trainerToFight = userRepository.findRandomUser(userDetails.getThisUser().getId());
                List<PokemonDTO> teamToFightWithEmpties = pokemonDataService.getPkmnTeamInfoOfUser(trainerToFight);
                List<Pokemon> teamToFight = pokemonRepository.findByOwnerIdAndInBoxFalse(trainerToFight);
                while(teamToFight.isEmpty()) {
                    trainerToFight = userRepository.findRandomUser(userDetails.getThisUser().getId());
                    teamToFightWithEmpties = pokemonDataService.getPkmnTeamInfoOfUser(trainerToFight);
                }
                List<EncounterDataSinglePkmn> encounterData = teamToFightWithEmpties.stream().map(p->new EncounterDataSinglePkmn(p, false)).toList();
                pokemonToFight.addAll(encounterData);
                trainerUsername = trainerToFight.getUsername();
            }
            randomEncounterList.add(new EncounterData(i, encounterType, pokemonToFight, 0, false, trainerUsername));
        }
        return randomEncounterList;
    }

    public EncounterData getEncounterDataAtIdx(int encounterIdx) {
        return sessionData.getSavedEncounterList().get(encounterIdx);
    }

    public List<EncounterDataSinglePkmn> getPokemonToFightListAtIdx(int encounterIdx) {
        return getEncounterDataAtIdx(encounterIdx).pokemonToFightList;
    }

    public EncounterDataSinglePkmn getEnemyActivePkmnAtIdx(int encounterIdx) {
        EncounterData encounterData = getEncounterDataAtIdx(encounterIdx);
        return encounterData.pokemonToFightList.get(encounterData.activePkmnToFightIdx);
    }

    public NotificationMsg executeTurn(int moveIdx) {
        EncounterData encounterData = sessionData.getSavedEncounterList().get(sessionData.getEncounterProgress());
        PokemonDTO myActivePkmnDTO = pokemonDataService.getPkmnTeamInfoDTO().stream().filter(p->p.isActivePkmn()).findFirst().get();
        Pokemon myActivePkmn = pokemonDataService.getPkmnTeamInfo().stream().filter(p->p.isActivePkmn()).findFirst().get();
        PokemonDTO_MoveInfo moveInfo = myActivePkmnDTO.allMoves().moves().get(moveIdx);
        PokemonDTO enemyActivePkmnDTO = encounterData.pokemonToFightList.get(encounterData.activePkmnToFightIdx).pkmn;
        Pokemon enemyPkmn = new Pokemon();
        pokemonDataService.populatePkmnWithPkmnDTOValues(enemyPkmn, enemyActivePkmnDTO);

        int dmgEnemyTakes = pokemonCalcService.calcMoveDamage(myActivePkmnDTO, enemyActivePkmnDTO, moveInfo);
        int actualDmgEnemyTakes = pokemonDataService.adjustPkmnHealth(enemyPkmn, -dmgEnemyTakes);
        boolean defeatedEnemy = enemyPkmn.getCurHp() <= 0 ? true : false;
        PokemonDTO updatedEnemy = enemyActivePkmnDTO.withNewHealth(enemyPkmn.getCurHp());
        encounterData.pokemonToFightList.set(encounterData.activePkmnToFightIdx, new EncounterDataSinglePkmn(updatedEnemy, defeatedEnemy));

        PokemonDTO_MoveInfo enemyMoveInfo = getRandomMoveInfo(enemyActivePkmnDTO);
        int dmgSelfTakes = pokemonCalcService.calcMoveDamage(enemyActivePkmnDTO, myActivePkmnDTO, enemyMoveInfo);
        int actualDmgSelfTakes = pokemonDataService.adjustPkmnHealth(myActivePkmn, -dmgSelfTakes);
        boolean defeatedSelf = myActivePkmn.getCurHp() <= 0 ? true : false;
        pokemonRepository.save(myActivePkmn);

        NotificationMsg msg;
        int totalHpOfEnemyPkmn = getPokemonToFightListAtIdx(sessionData.getEncounterProgress()).stream().mapToInt(p->p.pkmn.curHp()).sum();
        if(totalHpOfEnemyPkmn == 0) {
            EncounterData newEncounterData = sessionData.getSavedEncounterList().get(sessionData.getEncounterProgress()).withWon(true);
            sessionData.getSavedEncounterList().set(sessionData.getEncounterProgress(), newEncounterData);
            sessionData.setEncounterProgress(sessionData.getEncounterProgress()+1);
            msg = new NotificationMsg("You won! Redirecting to path in 3 seconds.", true);
        } else {
            String msgMsg = String.format(
                """
                %s used %s! %s took %d damage!\n
                %s used %s! You (%s) took %d damage!\n
                """,
                myActivePkmnDTO.displayName(), moveInfo.displayName(), enemyActivePkmnDTO.displayName(), Math.abs(actualDmgEnemyTakes),
                enemyActivePkmnDTO.displayName(), enemyMoveInfo.displayName(), myActivePkmnDTO.displayName(), Math.abs(actualDmgSelfTakes)
            );
            msg = new NotificationMsg(msgMsg, false);
        }

        return msg;
    }

    private PokemonDTO_MoveInfo getRandomMoveInfo(PokemonDTO enemyActivePkmnDTO) {
        int rndMoveInfoIdx = rnd.nextInt(enemyActivePkmnDTO.allMoves().moves().size());
        return enemyActivePkmnDTO.allMoves().moves().get(rndMoveInfoIdx);
    }

    public List<EncounterDataSinglePkmn> getPkmnTeamInfo() {
        List<PokemonDTO> pkmnTeamInfo = pokemonDataService.getPkmnTeamInfoDTO();
        return pkmnTeamInfo.stream().map(p->{
            boolean isDefeated = p.curHp() <= 0 ? true : false;
            return new EncounterDataSinglePkmn(p, isDefeated);
        }).toList();
    }
}