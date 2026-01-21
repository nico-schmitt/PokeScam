package com.PokeScam.PokeScam.Model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gyms")
public class Gym {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Works with PostgreSQL BIGSERIAL
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean npcGym; // true = NPC gym, false = player gym

    // Only set for player-owned gyms
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<GymTrainer> trainers = new ArrayList<>();

    // ======= GETTERS & SETTERS =======

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNpcGym() {
        return npcGym;
    }

    public void setNpcGym(boolean npcGym) {
        this.npcGym = npcGym;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<GymTrainer> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<GymTrainer> trainers) {
        this.trainers = trainers;
    }
}
