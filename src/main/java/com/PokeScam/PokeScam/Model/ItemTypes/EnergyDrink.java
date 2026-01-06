package com.PokeScam.PokeScam.Model.ItemTypes;

import com.PokeScam.PokeScam.Model.Item;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ENERGY_DRINK")
public class EnergyDrink extends Item {
    @Column(name = "enery_amount")
    private int energyAmount;
}
