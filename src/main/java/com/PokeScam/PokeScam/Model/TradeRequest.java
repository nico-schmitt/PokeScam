package com.PokeScam.PokeScam.Model;

import com.PokeScam.PokeScam.DTOs.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TradeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "pkmn_requester_id", nullable = false)
    private Pokemon pkmnRequester;

    @ManyToOne
    @JoinColumn(name = "pkmn_receiver_id", nullable = false)
    private Pokemon pkmnReceiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public TradeRequest(User requester, User receiver, Pokemon pkmnRequester, Pokemon pkmnReceiver) {
        this.requester = requester;
        this.receiver = receiver;
        this.pkmnRequester = pkmnRequester;
        this.pkmnReceiver = pkmnReceiver;
        this.status = RequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}
