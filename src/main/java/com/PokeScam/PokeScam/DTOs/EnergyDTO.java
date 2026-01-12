package com.PokeScam.PokeScam.DTOs;

public record EnergyDTO(
        int id,
        String name,
        String description,
        int maxStackSize,
        int price,
        boolean consumable,
        int energyAmount
) implements ItemDTO { }
