package com.PokeScam.PokeScam.Model.ItemTypes;

import com.PokeScam.PokeScam.Model.Item;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("POTION")
public class Potion extends Item {
    @Column(name = "heal_amount_potion")
    private int healAmountPotion;
}
