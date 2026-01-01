package com.PokeScam.PokeScam;

import java.io.IOException;
import java.time.Instant;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.UserRepository;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.UserResourcesService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
            if(checkIfUserIsBanned(authentication)) {
                SecurityContextHolder.clearContext();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                try {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Banned");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            Instant lastLogout = user.getLastLogout();
            user.setLastLogin(Instant.now());
            userRepo.save(user);


            pokemonDataService.healPkmnByLastLogout(lastLogout);
            userResourcesService.addResourcesByLastLogout(lastLogout);
        });

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private boolean checkIfUserIsBanned(Authentication authentication) {
        return authentication.getAuthorities()
            .stream()
            .anyMatch(role->role.getAuthority().equals("ROLE_BANNED"));
    }
}
