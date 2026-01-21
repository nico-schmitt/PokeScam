package com.PokeScam.PokeScam.Model;

import com.PokeScam.PokeScam.DTOs.InboxItemType;
import com.PokeScam.PokeScam.DTOs.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class InboxRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    protected User requester;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    protected User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected RequestStatus status;

    @Column(nullable = false)
    protected LocalDateTime createdAt;

    public abstract String getType();
}
