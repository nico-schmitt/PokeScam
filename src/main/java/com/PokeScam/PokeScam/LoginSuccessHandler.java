package com.PokeScam.PokeScam;

import java.io.IOException;
import java.time.Instant;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.PokeScam.PokeScam.Repos.UserRepository;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.UserResourcesService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepo;
    private final PokemonDataService pokemonDataService;
    private final UserResourcesService userResourcesService;

    public LoginSuccessHandler(UserRepository userRepo, PokemonDataService pokemonDataService, UserResourcesService userResourcesService) {
        this.userRepo = userRepo;
        this.pokemonDataService = pokemonDataService;
        this.userResourcesService = userResourcesService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        userRepo.findByUsername(authentication.getName()).ifPresent(user -> {
            Instant lastLogout = user.getLastLogout();
            user.setLastLogin(Instant.now());
            userRepo.save(user);

            pokemonDataService.healPkmnByLastLogout(lastLogout);
            userResourcesService.addResourcesByLastLogout(lastLogout);
        });

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
