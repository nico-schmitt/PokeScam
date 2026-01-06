package com.PokeScam.PokeScam.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "items")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type")
@Data
public abstract class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    @Column(name = "name")
    private String name;
    @Column(name = "amount")
    private int amount;
    @Column(name = "max_stack_size")
    private int maxStackSize;
    @Column(name = "consumable")
    private boolean consumable;
}
