package com.PokeScam.PokeScam.Services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.UserRepository;

@Service
public class RegisterUserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;


    public RegisterUserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(String username, String pw) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(pw));
        user.setRoles("USER");
        userRepo.save(user);
    }
}
