package com.PokeScam.PokeScam.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.PokeScam.PokeScam.SessionData;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_MoveInfo;


@Service
public class EncounterService {

    private final SessionData sessionData;
    private final PokeAPIService pokeAPIService;
    private final PokemonDataService pokemonDataService;
    private final PokemonCalcService pokemonCalcService;

    public record EncounterData(int order, boolean isPokemon, List<PokemonDTO> pokemonToFightList, int activePkmnToFightIdx) {}

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
            randomEncounterList.add(new EncounterData(i, encounterIsPokemon, pokemonToFight, 0));
        }
        return randomEncounterList;
    }

    public List<PokemonDTO> getEncounterAtIdx(int encounterIdx) {
        return sessionData.getSavedEncounterList().get(encounterIdx).pokemonToFightList;
    }

    public BattleInfo executeTurn(PokemonDTO_MoveInfo moveInfo) {
        EncounterData encounterData = sessionData.getSavedEncounterList().get(sessionData.getEncounterProgress());
        PokemonDTO myActivePkmn = pokemonDataService.getPkmnTeamInfo().get(sessionData.getActivePkmnIdx());
        PokemonDTO enemyActivePkmn = encounterData.pokemonToFightList.get(encounterData.activePkmnToFightIdx);
        int dmgEnemyTakes = pokemonCalcService.calcMoveDamage(myActivePkmn, enemyActivePkmn, moveInfo);
        return new BattleInfo(dmgEnemyTakes, dmgEnemyTakes);
    }

    public record BattleInfo(int enemyNewHp, int activePkmnNewHp) {}
}