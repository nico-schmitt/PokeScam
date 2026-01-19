package com.PokeScam.PokeScam.Model.ItemTypes;

import com.PokeScam.PokeScam.DTOs.BallDTO;
import com.PokeScam.PokeScam.DTOs.EnergyDTO;
import com.PokeScam.PokeScam.DTOs.ItemDTO;
import com.PokeScam.PokeScam.Model.Item;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;

@Entity
@DiscriminatorValue("ENERGY_DRINK")
@Getter
public class EnergyDrink extends Item {
    @Column(name = "enery_amount")
    private int energyAmount;

    @Override
    public boolean isSameItem(ItemDTO other) {
        return other instanceof EnergyDTO &&
            other.name().equalsIgnoreCase(this.getName()) &&
            other.description().equalsIgnoreCase(this.getDescription());
    }

    @Override
    public EnergyDTO toDTO() {
        return new EnergyDTO(getId(), getName(), getDescription(), getAmount(),
                getMaxStackSize(), getPrice(), isConsumable(), getEnergyAmount());
    }
}
