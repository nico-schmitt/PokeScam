package com.PokeScam.PokeScam;

import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.UserRepository;

import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetails implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetails(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Couldn't find user " + username));

        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().split(","))
                .build();
    }

    /**
     * Safely returns the current logged-in user details.
     * Returns Optional.empty() if the user is not authenticated (anonymous).
     */
    public Optional<UserDetails> getUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty(); // safe for public endpoints
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails); // logged-in user
        }

        // Never query DB for anonymous principal
        return Optional.empty();
    }

    /**
     * Safely returns the current logged-in User entity.
     * Returns Optional.empty() if the user is not authenticated.
     */
    public User getThisUser() {
    return getUserDetails()
            .flatMap(ud -> userRepo.findByUsername(ud.getUsername()))
            .orElseGet(() -> {
                // Return an anonymous user with default values
                User anonymous = new User();
                anonymous.setUsername("anonymous");
                anonymous.setCurrency(0);
                anonymous.setRoles(""); // optional
                return anonymous;
            });
}
}
