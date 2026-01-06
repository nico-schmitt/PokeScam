package com.PokeScam.PokeScam.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="inventories")
@Data
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
