package com.PokeScam.PokeScam.Model.ItemTypes;

import com.PokeScam.PokeScam.DTOs.BallDTO;
import com.PokeScam.PokeScam.DTOs.ItemDTO;
import com.PokeScam.PokeScam.DTOs.ReviveDTO;
import com.PokeScam.PokeScam.Model.Inventory;
import com.PokeScam.PokeScam.Model.Item;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;

@Getter
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

    @Override
    public boolean isSameItem(ItemDTO other) {
        return other instanceof ReviveDTO &&
                other.name().equalsIgnoreCase(this.getName()) &&
                other.description().equalsIgnoreCase(this.getDescription());
    }

    @Override
    public ReviveDTO toDTO() {
        return new ReviveDTO(getId(), getName(), getDescription(), getAmount(),
                getMaxStackSize(), getPrice(), isConsumable(), getHealPercentageRevive());
    }
}
