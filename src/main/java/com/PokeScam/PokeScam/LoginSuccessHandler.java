package com.PokeScam.PokeScam;

import java.io.IOException;
import java.time.Instant;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.PokeScam.PokeScam.Repos.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler{
    private final UserRepository userRepo;

    public LoginSuccessHandler(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        userRepo.findByUsername(authentication.getName()).ifPresent(user -> {
            user.setLastLogin(Instant.now());
            userRepo.save(user);
        });

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
