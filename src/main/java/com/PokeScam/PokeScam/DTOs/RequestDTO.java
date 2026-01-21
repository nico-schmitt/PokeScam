package com.PokeScam.PokeScam.DTOs;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;

import java.time.LocalDateTime;

public record RequestDTO(
        int id,
        User requeser,
        User receiver,
        Pokemon pkmnRequester,
        Pokemon pkmnReceiver,
        LocalDateTime createdAt
) {
}
