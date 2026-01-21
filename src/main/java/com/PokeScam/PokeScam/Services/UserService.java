package com.PokeScam.PokeScam.Services;

import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.UserRepository;
import org.springframework.stereotype.Service;

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
}
