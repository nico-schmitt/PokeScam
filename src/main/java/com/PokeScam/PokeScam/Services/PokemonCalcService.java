package com.PokeScam.PokeScam.Services;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_MoveInfo;
import com.PokeScam.PokeScam.Model.Pokemon;

@Service
public class PokemonCalcService {
    private final Random rnd;

    public PokemonCalcService() {
        rnd = new Random();
    }

    // ==================== LEVEL / STATS ====================

    public int calcRndPkmnLevel() {
        return rnd.nextInt(1, 101); // Levels 1-100
    }

    public int calcPkmnMaxHp(int baseStat, int level) {
        return (int) Math.floor(((2 * baseStat) * level) / 100.0) + level + 10;
    }

    public int calcPkmnAtk(int baseStat, int level) {
        return (int) Math.floor(Math.floor(((2 * baseStat) * level) / 100.0) + 5);
    }

    public int calcPkmnDef(int baseStat, int level) {
        return (int) Math.floor(Math.floor(((2 * baseStat) * level) / 100.0) + 5);
    }

    public int calcPkmnSpa(int baseStat, int level) {
        return (int) Math.floor(Math.floor(((2 * baseStat) * level) / 100.0) + 5);
    }

    public int calcPkmnSpd(int baseStat, int level) {
        return (int) Math.floor(Math.floor(((2 * baseStat) * level) / 100.0) + 5);
    }

    public int calcPkmnSpe(int baseStat, int level) {
        return (int) Math.floor(Math.floor(((2 * baseStat) * level) / 100.0) + 5);
    }

    // ==================== DAMAGE CALCULATION ====================

    public int calcMoveDamage(PokemonDTO attacker, PokemonDTO defender, PokemonDTO_MoveInfo moveInfo) {
        int atk = 0, def = 0;

        switch (moveInfo.damageClass()) {
            case "physical" -> {
                atk = attacker.allStats().atk().statValue();
                def = defender.allStats().def().statValue();
            }
            case "special" -> {
                atk = attacker.allStats().spa().statValue();
                def = defender.allStats().spd().statValue();
            }
            case "status" -> { // status moves do no damage
                return 0;
            }
        }

        if (atk == 0 || def == 0 || moveInfo.power() == 0)
            return 0;

        double baseDamage = ((2.0 * attacker.level() / 5 + 2) * moveInfo.power() * atk / def) / 50.0 + 2;
        double modifier = 1.0; // Can include type effectiveness, crits, random 0.85-1.0 later
        return (int) Math.floor(baseDamage * modifier);
    }

    // ==================== EXPERIENCE ====================

    public int calculateExpGain(PokemonDTO myPkmn, PokemonDTO enemyPkmn) {
        // Simple formula: BaseExp * EnemyLevel / 7
        int baseExp = 64; // Default for wild Pokémon, could pull from species
        return (int) Math.floor(baseExp * enemyPkmn.level() / 7.0);
    }

    public void gainExp(Pokemon pokemon, int exp) {
        pokemon.setExp(pokemon.getExp() + exp);
    }

    public boolean checkLevelUp(Pokemon pokemon) {
        int nextLevelExp = getExpForNextLevel(pokemon.getLevel());
        boolean leveledUp = false;
        while (pokemon.getExp() >= nextLevelExp && pokemon.getLevel() < 100) {
            pokemon.setLevel(pokemon.getLevel() + 1);
            pokemon.setExp(pokemon.getExp() - nextLevelExp);
            leveledUp = true;
            // Recalculate stats
            pokemon.setMaxHp(calcPkmnMaxHp(pokemon.getHpBaseStat(), pokemon.getLevel()));
            pokemon.setAtk(calcPkmnAtk(pokemon.getAtkBaseStat(), pokemon.getLevel()));
            pokemon.setDef(calcPkmnDef(pokemon.getDefBaseStat(), pokemon.getLevel()));
            pokemon.setSpa(calcPkmnSpa(pokemon.getSpaBaseStat(), pokemon.getLevel()));
            pokemon.setSpd(calcPkmnSpd(pokemon.getSpdBaseStat(), pokemon.getLevel()));
            pokemon.setSpe(calcPkmnSpe(pokemon.getSpeBaseStat(), pokemon.getLevel()));
            // Evolution placeholder
            checkEvolution(pokemon);
            nextLevelExp = getExpForNextLevel(pokemon.getLevel());
        }
        return leveledUp;
    }

    private int getExpForNextLevel(int level) {
        // Example: simple quadratic formula
        return (int) Math.floor(Math.pow(level + 1, 3));
    }

    private void checkEvolution(Pokemon pokemon) {
        // Placeholder for future evolution logic
        // Example: if level >= threshold, evolve
    }

    // ==================== MISC ====================

    public int getPokemonValue(Pokemon pkmn) {
        int statSum = pkmn.getMaxHp() + pkmn.getAtk() + pkmn.getDef() +
                    pkmn.getSpa() + pkmn.getSpd() + pkmn.getSpe();
        return pkmn.getLevel() + (statSum / 100);
    }
}
