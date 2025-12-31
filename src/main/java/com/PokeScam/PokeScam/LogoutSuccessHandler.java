package com.PokeScam.PokeScam;

import java.io.IOException;
import java.time.Instant;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.PokeScam.PokeScam.Repos.UserRepository;
import com.PokeScam.PokeScam.Services.PokemonDataService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    private final UserRepository userRepo;
    private final PokemonDataService pokemonDataService;

    public LogoutSuccessHandler(UserRepository userRepo, PokemonDataService pokemonDataService) {
        this.userRepo = userRepo;
        this.pokemonDataService = pokemonDataService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        userRepo.findByUsername(authentication.getName()).ifPresent(user -> {
            user.setLastLogout(Instant.now());
            userRepo.save(user);
        });

        setDefaultTargetUrl("/login?logout");
        setAlwaysUseDefaultTargetUrl(true);
        super.onLogoutSuccess(request, response, authentication);
    }
}
