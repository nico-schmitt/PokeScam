package com.PokeScam.PokeScam.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.SessionData;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_MoveInfo;
import com.PokeScam.PokeScam.Model.Pokemon;


@Service
public class EncounterService {

    private final SessionData sessionData;
    private final PokeAPIService pokeAPIService;
    private final PokemonDataService pokemonDataService;
    private final PokemonCalcService pokemonCalcService;

    public record EncounterData(int order, boolean isPokemon, List<PokemonDTO> pokemonToFightList, int activePkmnToFightIdx, boolean encounterWon) {
        public EncounterData withWon(boolean won) {
            return new EncounterData(this.order, this.isPokemon, this.pokemonToFightList, this.activePkmnToFightIdx, won);
        }
    }

    private Random rnd;
    
    public EncounterService(PokemonDataService pokemonDataService, PokeAPIService pokeAPIService, PokemonCalcService pokemonCalcService, SessionData sessionData) {
        rnd = new Random();
        this.pokemonDataService = pokemonDataService;
        this.pokeAPIService = pokeAPIService;
        this.pokemonCalcService = pokemonCalcService;
        this.sessionData = sessionData;
    }

    public List<EncounterData> getRandomEncounters() {
        int encounterCount = rnd.nextInt(3, 7);
        List<EncounterData> randomEncounterList = new ArrayList<>();
        for (int i = 0; i < encounterCount; i++) {
            boolean encounterIsPokemon = rnd.nextFloat() < 0.5f;
            List<PokemonDTO> pokemonToFight = new ArrayList<>();
            if(encounterIsPokemon) {
                pokemonToFight.add(pokeAPIService.getRandomPokemon());
            } else {
                // populate list with pokemon of trainer
            }
            randomEncounterList.add(new EncounterData(i, encounterIsPokemon, pokemonToFight, 0, false));
        }
        return randomEncounterList;
    }

    public EncounterData getEncounterDataAtIdx(int encounterIdx) {
        return sessionData.getSavedEncounterList().get(encounterIdx);
    }

    public List<PokemonDTO> getPokemonToFightListAtIdx(int encounterIdx) {
        return getEncounterDataAtIdx(encounterIdx).pokemonToFightList;
    }

    public PokemonDTO getEnemyActivePkmnAtIdx(int encounterIdx) {
        EncounterData encounterData = getEncounterDataAtIdx(encounterIdx);
        return encounterData.pokemonToFightList.get(encounterData.activePkmnToFightIdx);
    }

    public NotificationMsg executeTurn(int moveIdx) {
        EncounterData encounterData = sessionData.getSavedEncounterList().get(sessionData.getEncounterProgress());
        PokemonDTO myActivePkmnDTO = pokemonDataService.getPkmnTeamInfo().get(sessionData.getActivePkmnIdx());
        PokemonDTO_MoveInfo moveInfo = myActivePkmnDTO.allMoves().moves().get(moveIdx);
        PokemonDTO enemyActivePkmnDTO = encounterData.pokemonToFightList.get(encounterData.activePkmnToFightIdx);
        int dmgEnemyTakes = pokemonCalcService.calcMoveDamage(myActivePkmnDTO, enemyActivePkmnDTO, moveInfo);
        Pokemon myPkmn = new Pokemon();
        Pokemon enemyPkmn = new Pokemon();
        pokemonDataService.populatePkmnWithPkmnDTOValues(myPkmn, myActivePkmnDTO);
        pokemonDataService.populatePkmnWithPkmnDTOValues(enemyPkmn, enemyActivePkmnDTO);
        int actualDmg = pokemonDataService.adjustPkmnHealth(enemyPkmn, -dmgEnemyTakes);

        PokemonDTO updatedEnemy = encounterData.pokemonToFightList.get(encounterData.activePkmnToFightIdx).withNewHealth(enemyPkmn.getCurHp());
        encounterData.pokemonToFightList.set(encounterData.activePkmnToFightIdx, updatedEnemy);

        NotificationMsg msg;
        int totalHpOfEnemyPkmn = getPokemonToFightListAtIdx(sessionData.getEncounterProgress()).stream().mapToInt(p->p.curHp()).sum();
        if(totalHpOfEnemyPkmn == 0) {
            EncounterData newEncounterData = sessionData.getSavedEncounterList().get(sessionData.getEncounterProgress()).withWon(true);
            sessionData.getSavedEncounterList().set(sessionData.getEncounterProgress(), newEncounterData);
            msg = new NotificationMsg("You won!", true);
        } else {
            msg = new NotificationMsg(String.format("%s took %d damage!", enemyActivePkmnDTO.displayName(), Math.abs(actualDmg)), false);
        }

        return msg;
    }
}