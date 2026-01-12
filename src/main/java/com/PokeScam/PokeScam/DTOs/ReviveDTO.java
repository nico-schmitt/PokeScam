package com.PokeScam.PokeScam.DTOs;

public record ReviveDTO(
        int id,
        String name,
        String description,
        int maxStackSize,
        int price,
        boolean consumable,
        float healPercentageRevive
) implements ItemDTO { }
