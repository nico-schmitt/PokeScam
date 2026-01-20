package com.PokeScam.PokeScam.Services;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class VerifyUserService {
    @Value("${secretKey}")
    private String secretKey;

    @Value("${server.port}")
    private int port;

    private final UserRepository userRepo;

    public VerifyUserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public String createVerificationToken(String userId) {
        Instant now = Instant.now();
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(15))))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String handleVerification(String token) {
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
            // System.out.println("aoeaoeaeaoeaoeaoeaoe\n\n\n\n\n\n\n\n");
            throw new RuntimeException("This link has expired");
        } catch (JwtException e) {
            // System.out.println("eeeeeeeeeeeeee\n\n\n\n\n\n\n\n");
            throw new RuntimeException("Invalid token");
        }

        String userId = claims.getSubject();
        // System.out.println(claims);
        // System.out.println("userID: "+userId);
        User user = userRepo.findById(Integer.valueOf(userId)).orElseThrow();

        if (user.isVerified()) {
            return "Email is already verified";
        }

        user.setVerified(true);
        userRepo.save(user);
        return "Email verification successful";
    }
}
