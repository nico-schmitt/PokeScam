package com.PokeScam.PokeScam.DTOs;

import java.time.LocalDateTime;

public record FriendDTO(
        int id,
        String name,
        String recentActivity,
        LocalDateTime createdAt
) {
}
