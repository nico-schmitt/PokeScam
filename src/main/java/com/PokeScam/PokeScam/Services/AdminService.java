package com.PokeScam.PokeScam.Services;

import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.BoxRepository;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepo;
    private final BoxRepository boxRepo;
    private final PokemonRepository pokemonRepo;
    private final CustomUserDetails userDetails;
    private final MailService mailService;

    @Value("${secretKey}")
    private String secretKey;
    
    public AdminService(BoxRepository boxRepo, PokemonRepository pokemonRepo, CustomUserDetails userDetails, UserRepository userRepo, MailService mailService) {
        this.boxRepo = boxRepo;
        this.pokemonRepo = pokemonRepo;
        this.userDetails = userDetails;
        this.userRepo = userRepo;
        this.mailService = mailService;
    }

    public NotificationMsg banUser(String userToBanUsername) {
        NotificationMsg msg;
        Optional<User> user = userRepo.findByUsername(userToBanUsername);
        if(user.isPresent()) {
            User u = user.get();
            String newRoles = u.getRoles() + ",BANNED";
            u.setRoles(newRoles);
            userRepo.save(u);
            msg = new NotificationMsg(String.format("Banned user %s", u.getUsername()), true);
        } else {
            msg = new NotificationMsg(String.format("Failed to ban user %s", userToBanUsername), false);
        }

        return msg;
    }

    public NotificationMsg sendUnbanEmail(int unbanId) {
        Optional<User> user = userRepo.findById(unbanId);
        NotificationMsg msg = null;
        if(user.isPresent()) {
            User u = user.get();
            if(u.getEmail() != null) {
                mailService.sendUnbanEmail(u, u.getEmail(), "Email regarding unban");
                msg = new NotificationMsg("Unban email sent", true);
            }
        } else {
            msg = new NotificationMsg("Couldn't find user", false);
        }
        return msg;
    }

    public List<User> getBannedUsers() {
        List<User> bannedUsers = userRepo.findByRolesContaining("BANNED");
        return bannedUsers.stream().filter(u->u.getEmail() != null).toList();
    }

    public String handleUnban(String token) {
        Claims claims;
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("This link has expired");
        } catch (JwtException e) {
            throw new RuntimeException("Invalid token");
        }

        String userId = claims.getSubject();
        // System.out.println(claims);
        // System.out.println("userID: "+userId);
        User user = userRepo.findById(Integer.valueOf(userId)).orElseThrow();

        String newRoles = user.getRoles().replace(",BANNED", "");
        user.setRoles(newRoles);
        userRepo.save(user);
        return "Unban successful";
    }
}