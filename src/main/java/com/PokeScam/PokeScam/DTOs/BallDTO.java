package com.PokeScam.PokeScam.DTOs;

public record BallDTO(
    int id,
    String name,
    String description,
    int maxStackSize,
    int price,
    boolean consumable,
    float baseCaptureChance
) implements ItemDTO {}
