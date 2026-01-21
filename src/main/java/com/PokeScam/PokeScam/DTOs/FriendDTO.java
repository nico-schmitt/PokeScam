package com.PokeScam.PokeScam.DTOs;

import java.time.LocalDateTime;

public record FriendDTO(
        int id,
        int friendId,
        String name,
        String recentActivity,
        LocalDateTime createdAt
) {
}
