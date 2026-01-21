package com.PokeScam.PokeScam.Model;

import com.PokeScam.PokeScam.DTOs.InboxItemType;
import com.PokeScam.PokeScam.DTOs.RequestDTO;
import com.PokeScam.PokeScam.DTOs.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TradeRequest extends InboxRequest{
    @ManyToOne
    @JoinColumn(name = "pkmn_requester_id", nullable = false)
    private Pokemon pkmnRequester;

    @ManyToOne
    @JoinColumn(name = "pkmn_receiver_id", nullable = false)
    private Pokemon pkmnReceiver;

    @Override
    public String getType() {
        return "TRADE";
    }

    public TradeRequest() {}

    public TradeRequest(User requester, User receiver, Pokemon pkmnRequester, Pokemon pkmnReceiver) {
        this.requester = requester;
        this.receiver = receiver;
        this.pkmnRequester = pkmnRequester;
        this.pkmnReceiver = pkmnReceiver;
        this.status = RequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public RequestDTO convertToDTO() {
        return new RequestDTO(id, requester, receiver, pkmnRequester, pkmnReceiver, createdAt, "TRADE");
    }
}
