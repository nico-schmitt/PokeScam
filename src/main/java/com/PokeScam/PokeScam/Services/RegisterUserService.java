package com.PokeScam.PokeScam.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.UserRepository;

@Service
public class RegisterUserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;


    public RegisterUserService(UserRepository userRepo, PasswordEncoder passwordEncoder, MailService mailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    public void registerUser(String username, String email, String pw) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(passwordEncoder.encode(email));
        user.setPassword(passwordEncoder.encode(pw));
        user.setRoles("USER");
        user.setVerified(false);
        User savedUser = userRepo.save(user);
        if(email != "") {
            mailService.sendPlainText(savedUser, email, "Verify your account thx");
        }
    }
}
