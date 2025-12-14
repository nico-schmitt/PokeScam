package com.PokeScam.PokeScam;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.UserRepository;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetails implements UserDetailsService {

    private final UserRepository userRepo;

    CustomUserDetails(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(()->new UsernameNotFoundException("Couldn't find user " + username));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles())
                .build();
    }

    public UserDetails getUserDetails() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (UserDetails)principal;
    }

    public User getThisUser() {
        return userRepo.findByUsername(getUserDetails().getUsername())
            .orElseThrow(()->new UsernameNotFoundException("Couldn't find user of this session"));
    }
}

