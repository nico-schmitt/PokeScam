package com.PokeScam.PokeScam.Services;

import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void updateRecentActivity(User user, String recentActivity) {
        user.setRecentActivity(recentActivity);
        userRepository.save(user);
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }
}
