package com.PokeScam.PokeScam.Services;

import java.time.Duration;
import java.time.Instant;
import com.PokeScam.PokeScam.Repos.UserRepository;
import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.Model.User;

@Service
public class UserResourcesService {

    private final UserRepository userRepo;
    private final CustomUserDetails userDetails;
    
    public UserResourcesService(CustomUserDetails userDetails, UserRepository userRepo) {
        this.userDetails = userDetails;
        this.userRepo = userRepo;
    }

    public void addResourcesByLastLogout(Instant lastLogout) {
        long secondsSinceLastLogout = Duration.between(lastLogout, Instant.now()).getSeconds();
        User user = userDetails.getThisUser();
        int currencyToGive = user.getCurrency() + (int)secondsSinceLastLogout;
        int energyToGive = user.getEnergy() + (int)secondsSinceLastLogout;
        user.setCurrency(currencyToGive);
        user.setEnergy(energyToGive);

        userRepo.save(user);
    }
}