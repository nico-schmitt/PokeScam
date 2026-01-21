package com.PokeScam.PokeScam.Model;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int id;
    @Column(name="username")
    private String username;
    @Column(name="email")
    private String email;
    @Column(name="password")
    private String password;
    @Column(name="roles")
    private String roles;
    @Column(name="is_verified")
    private boolean isVerified;
    @Column(name="currency")
    private int currency;
    @Column(name="energy")
    private int energy;
    @Column(name="last_login")
    private Instant lastLogin;
    @Column(name="last_logout")
    private Instant lastLogout;
    @OneToOne(cascade = CascadeType.ALL)
    private Inventory inventory;
    @Column(name="recent_activity")
    private String recentActivity;
}