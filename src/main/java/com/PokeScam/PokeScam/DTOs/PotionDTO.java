package com.PokeScam.PokeScam.DTOs;

public record PotionDTO(
        int id,
        String name,
        String description,
        int amount,
        int maxStackSize,
        int price,
        boolean consumable,
        int healAmountPotion
) implements ItemDTO { }
