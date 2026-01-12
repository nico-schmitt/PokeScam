package com.PokeScam.PokeScam.Model.ItemTypes;

import com.PokeScam.PokeScam.Model.Inventory;
import com.PokeScam.PokeScam.Model.Item;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REVIVE")
public class Revive extends Item {
    @Column(name = "heal_percentage_revive")
    private float healPercentageRevive;

    public Revive() {}

    public Revive(Inventory inventory, String name, String description, int amount,
                  int maxStackSize, boolean consumable, float healPercentageRevive) {
        setInventory(inventory);
        setName(name);
        setDescription(description);
        setAmount(amount);
        setMaxStackSize(maxStackSize);
        setConsumable(consumable);
        this.healPercentageRevive = healPercentageRevive;
    }
}
