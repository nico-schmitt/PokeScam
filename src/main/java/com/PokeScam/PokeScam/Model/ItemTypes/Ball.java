package com.PokeScam.PokeScam.Model.ItemTypes;

import com.PokeScam.PokeScam.Model.Item;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("BALL")
public class Ball extends Item {
    @Column(name = "base_capture_chance")
    private float baseCaptureChance;
}
