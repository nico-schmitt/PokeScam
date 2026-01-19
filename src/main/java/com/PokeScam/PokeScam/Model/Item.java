package com.PokeScam.PokeScam.Model;

import com.PokeScam.PokeScam.DTOs.ItemDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    private int id;

    @ManyToOne
    @JoinColumn(name = "inventory_id")
    @NotNull
    private Inventory inventory;

    @Column(name = "name")
    @NotNull
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "amount")
    @NotNull
    private int amount;
    @Column(name = "max_stack_size")
    @NotNull
    private int maxStackSize;
    @Column(name = "price")
    private int price;
    @Column(name = "consumable")
    @NotNull
    private boolean consumable;

    public abstract boolean isSameItem(ItemDTO other);
    public abstract ItemDTO toDTO();
}
