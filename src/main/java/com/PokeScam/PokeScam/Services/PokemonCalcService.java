package com.PokeScam.PokeScam.Services;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_MoveInfo;

@Service
public class PokemonCalcService {
    private final Random rnd;

    public PokemonCalcService() {
        rnd = new Random();
    }

    public int calcRndPkmnLevel() {
        return rnd.nextInt(1, 101);
    }

    public int calcPkmnMaxHp(int baseStat, int level) {
        // HP = floor(((2 × Base + IV + floor(EV / 4)) × Level) / 100) + Level + 10
        return (int)Math.floor(((2*baseStat)*level) / 100) + level + 10;
    }

    public int calcPkmnAtk(int baseStat, int level) {
        // Stat = floor( ( floor(((2 × Base + IV + floor(EV / 4)) × Level) / 100) + 5 ) × Nature)
        return (int)Math.floor(Math.floor(((2*baseStat)*level) / 100) + 5);
    }

    public int calcPkmnDef(int baseStat, int level) {
        // Stat = floor( ( floor(((2 × Base + IV + floor(EV / 4)) × Level) / 100) + 5 ) × Nature)
        return (int)Math.floor(Math.floor(((2*baseStat)*level) / 100) + 5);
    }

    public int calcPkmnSpa(int baseStat, int level) {
        // Stat = floor( ( floor(((2 × Base + IV + floor(EV / 4)) × Level) / 100) + 5 ) × Nature)
        return (int)Math.floor(Math.floor(((2*baseStat)*level) / 100) + 5);
    }

    public int calcPkmnSpd(int baseStat, int level) {
        // Stat = floor( ( floor(((2 × Base + IV + floor(EV / 4)) × Level) / 100) + 5 ) × Nature)
        return (int)Math.floor(Math.floor(((2*baseStat)*level) / 100) + 5);
    }

    public int calcPkmnSpe(int baseStat, int level) {
        // Stat = floor( ( floor(((2 × Base + IV + floor(EV / 4)) × Level) / 100) + 5 ) × Nature)
        return (int)Math.floor(Math.floor(((2*baseStat)*level) / 100) + 5);
    }

    public int calcMoveDamage(PokemonDTO attacker, PokemonDTO defender, PokemonDTO_MoveInfo moveInfo) {
        //Damage =
        // floor(
        //   floor(
        //     floor(
        //       ((2 × Level / 5 + 2) × Power × Atk / Def) / 50
        //     ) + 2
        //   )
        //   × Modifier
        // )
        int atk = 0;
        int def = 0;
        if(moveInfo.damageClass().equals("physical")) {
            atk = attacker.allStats().atk().statValue();
            def = defender.allStats().def().statValue();
        } else if(moveInfo.damageClass().equals( "special")) {
            atk = attacker.allStats().spa().statValue();
            def = defender.allStats().spd().statValue();
        } else if(moveInfo.damageClass().equals( "status")) {
            atk = 0;
            def = 0;
        }

        return (int)
        Math.floor(
            Math.floor(
                Math.floor(
                    ((2*attacker.level()/5+2) * moveInfo.power() * atk / def) / 50
                ) + 2
            ) // * mod
        );
    }
}
