package com.PokeScam.PokeScam.Services;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.User;
import com.PokeScam.PokeScam.Repos.UserRepository;

@Service
public class ShowUserService {
    private UserRepository r;

    public ShowUserService(UserRepository r) {
        this.r = r;
    }

    public User ShowUser(int id) {
        return r.findById(id).get();
    }
}
