package com.PokeScam.PokeScam.Model.ItemTypes;

import com.PokeScam.PokeScam.Model.Inventory;
import com.PokeScam.PokeScam.Model.Item;
import jakarta.persistence.*;


@Entity
@DiscriminatorValue("BALL")
public class Ball extends Item {
    @Column(name = "base_capture_chance")
    private float baseCaptureChance;

    public Ball() {}

    public Ball(Inventory inventory, String name, String description, int amount,
                int maxStackSize, boolean consumable, float baseCaptureChance) {
        setInventory(inventory);
        setName(name);
        setDescription(description);
        setAmount(amount);
        setMaxStackSize(maxStackSize);
        setConsumable(consumable);
        this.baseCaptureChance = baseCaptureChance;
    }
}
