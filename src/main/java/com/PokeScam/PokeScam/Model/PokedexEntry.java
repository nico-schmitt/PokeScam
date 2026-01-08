package com.PokeScam.PokeScam.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pokedex_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "species_id" })
})
@Data
public class PokedexEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User user;

    private String speciesName; // <- use this for PokeAPI lookups
    private boolean seen;
    private boolean caught;

}
