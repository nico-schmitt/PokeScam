package com.PokeScam.PokeScam.Services;

import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class PokemonCalcService {
    private final Random rnd;

    public PokemonCalcService() {
        rnd = new Random();
    }

    public int calcRndPkmnLevel() {
        return rnd.nextInt(1, 101);
    }

    public int calcPkmnMaxHp(int baseStat) {
        return baseStat;
    }

    public int calcPkmnAtk(int atk_baseStat) {
        return atk_baseStat;
    }

    public int calcPkmnDef(int def_baseStat) {
        return def_baseStat;
    }

    public int calcPkmnSpa(int spa_baseStat) {
        return spa_baseStat;
    }

    public int calcPkmnSpd(int spd_baseStat) {
        return spd_baseStat;
    }

    public int calcPkmnSpe(int spe_baseStat) {
        return spe_baseStat;
    }
}
