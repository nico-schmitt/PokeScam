package com.PokeScam.PokeScam.Model.ItemTypes;

import com.PokeScam.PokeScam.Model.Item;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("REVIVE")
public class Revive extends Item {
    @Column(name = "heal_percentage_revive")
    private float healPercentageRevive;
}
