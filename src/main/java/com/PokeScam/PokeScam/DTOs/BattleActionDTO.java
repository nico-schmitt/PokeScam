package com.PokeScam.PokeScam.DTOs;

import com.PokeScam.PokeScam.DTOs.BattleAction;

public record BattleActionDTO(
        BattleAction action,
        Integer moveIdx,
        Integer switchIdx,
        Integer itemIdx) {
}
