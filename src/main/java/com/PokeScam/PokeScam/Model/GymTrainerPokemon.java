package com.PokeScam.PokeScam.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "gym_trainer_pokemon")
public class GymTrainerPokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "species_id")
    private int speciesId;

    @Column(name = "species_name")
    private String speciesName;

    @Column(name = "level")
    private int level;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private GymTrainer trainer;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public int getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(int speciesId) {
        this.speciesId = speciesId;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public GymTrainer getTrainer() {
        return trainer;
    }

    public void setTrainer(GymTrainer trainer) {
        this.trainer = trainer;
    }
}
