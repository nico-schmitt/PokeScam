package com.PokeScam.PokeScam.DTOs;

public sealed interface ItemDTO
    permits BallDTO, EnergyDTO, PotionDTO, ReviveDTO {
    int id();
    String name();
    String description();
    int amount();
    int maxStackSize();
    int price();
    boolean consumable();
}
