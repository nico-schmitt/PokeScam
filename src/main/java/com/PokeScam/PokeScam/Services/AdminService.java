package com.PokeScam.PokeScam.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.BoxRepository;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepo;
    private final BoxRepository boxRepo;
    private final PokemonRepository pokemonRepo;
    private final CustomUserDetails userDetails;
    
    public AdminService(BoxRepository boxRepo, PokemonRepository pokemonRepo, CustomUserDetails userDetails, UserRepository userRepo) {
        this.boxRepo = boxRepo;
        this.pokemonRepo = pokemonRepo;
        this.userDetails = userDetails;
        this.userRepo = userRepo;
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

}