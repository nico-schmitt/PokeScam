package com.PokeScam.PokeScam.Model;

import com.PokeScam.PokeScam.DTOs.FriendDTO;
import com.PokeScam.PokeScam.DTOs.FriendshipStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "friendship",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"requester_id", "receiver_id"}
        )
)
@Getter
@Setter
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public FriendDTO convertToDTO(User currentUser) {
        User otherUser;
        if (requester.equals(currentUser)) {
            otherUser = receiver;
        } else
            otherUser = requester;

        return new FriendDTO(this.id, otherUser.getUsername(), otherUser.getRecentActivity());
    }
}
