package com.PokeScam.PokeScam.Model.ItemTypes;

import com.PokeScam.PokeScam.DTOs.ItemDTO;
import com.PokeScam.PokeScam.DTOs.PotionDTO;
import com.PokeScam.PokeScam.Model.Inventory;
import com.PokeScam.PokeScam.Model.Item;
import jakarta.persistence.*;


@Entity
@DiscriminatorValue("POTION")
public class Potion extends Item {
    @Column(name = "heal_amount_potion")
    private int healAmountPotion;

    public Potion() {}

    public Potion(Inventory inventory, String name, String description, int amount,
                  int maxStackSize, boolean consumable, int healAmountPotion) {
        setInventory(inventory);
        setName(name);
        setDescription(description);
        setAmount(amount);
        setMaxStackSize(maxStackSize);
        setConsumable(consumable);
        this.healAmountPotion = healAmountPotion;
    }

    @Override
    public boolean isSameItem(ItemDTO other) {
        return other instanceof PotionDTO &&
                other.name().equalsIgnoreCase(this.getName()) &&
                other.description().equalsIgnoreCase(this.getDescription());
    }
}
