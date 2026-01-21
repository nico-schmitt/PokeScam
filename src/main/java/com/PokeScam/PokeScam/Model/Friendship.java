package com.PokeScam.PokeScam.Model;

import com.PokeScam.PokeScam.DTOs.FriendDTO;
import com.PokeScam.PokeScam.DTOs.InboxItemType;
import com.PokeScam.PokeScam.DTOs.RequestDTO;
import com.PokeScam.PokeScam.DTOs.RequestStatus;
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
public class Friendship extends InboxRequest{
    @Override
    public String getType() {
        return "FRIEND";
    }

    public FriendDTO convertToDTO(User currentUser) {
        User otherUser;
        if (getRequester().equals(currentUser)) {
            otherUser = getReceiver();
        } else
            otherUser = getRequester();

        return new FriendDTO(id, otherUser.getId(), otherUser.getUsername(), otherUser.getRecentActivity(), createdAt);
    }

    public RequestDTO convertToRequestDTO() {
        return new RequestDTO(id, requester, receiver, null, null, createdAt, "FRIEND");
    }
}
