package com.PokeScam.PokeScam.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pokemons")
@Data
public class Pokemon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    // Owner is null for NPC Pokémon
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = true)
    private User ownerId;

    // Trainer is null for player Pokémon
    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = true)
    private GymTrainer trainer;

    @Column(name = "is_npc")
    private boolean isNpc;

    @Column(name = "in_box")
    private boolean inBox;
    @ManyToOne
    @JoinColumn(name = "box_id")
    private Box boxId;
    @Column(name = "is_active_pkmn")
    private boolean isActivePkmn;
    @Column(name = "level")
    private int level;
    @Column(name = "exp")
    private int exp;
    @Column(name = "max_hp")
    private int maxHp;
    @Column(name = "cur_hp")
    private int curHp;
    @Column(name = "atk")
    private int atk;
    @Column(name = "def")
    private int def;
    @Column(name = "spa")
    private int spa;
    @Column(name = "spd")
    private int spd;
    @Column(name = "spe")
    private int spe;
    @Column(name = "atk_base_stat")
    private int atkBaseStat;
    @Column(name = "def_base_stat")
    private int defBaseStat;
    @Column(name = "spa_base_stat")
    private int spaBaseStat;
    @Column(name = "spd_base_stat")
    private int spdBaseStat;
    @Column(name = "spe_base_stat")
    private int speBaseStat;
    @Column(name = "hp_base_stat")
    private int hpBaseStat;
    @Column(name = "move1")
    private String move1;
    @Column(name = "move2")
    private String move2;
    @Column(name = "move3")
    private String move3;
    @Column(name = "move4")
    private String move4;
}