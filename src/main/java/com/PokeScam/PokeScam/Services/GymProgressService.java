package com.PokeScam.PokeScam.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Model.UserGymStatus;
import com.PokeScam.PokeScam.Repos.UserGymStatusRepository;

@Service
public class GymProgressService {

    private final UserGymStatusRepository userGymStatusRepo;

    public GymProgressService(UserGymStatusRepository userGymStatusRepo) {
        this.userGymStatusRepo = userGymStatusRepo;
    }

    public void markGymCompleted(User user, Gym gym) {
        UserGymStatus status = userGymStatusRepo.findByUserAndGym(user, gym)
                .orElseGet(() -> {
                    UserGymStatus newStatus = new UserGymStatus();
                    newStatus.setUser(user);
                    newStatus.setGym(gym);
                    return newStatus;
                });

        status.setCompleted(true);
        userGymStatusRepo.save(status);
    }

    public boolean isGymCompleted(User user, Gym gym) {
        return userGymStatusRepo.findByUserAndGym(user, gym)
                .map(UserGymStatus::isCompleted)
                .orElse(false);
    }

    public List<Long> getCompletedGymIdsForUser(User user) {
        return userGymStatusRepo.findByUser(user).stream()
                .filter(UserGymStatus::isCompleted)
                .map(ugs -> ugs.getGym().getId())
                .toList();
    }

}
