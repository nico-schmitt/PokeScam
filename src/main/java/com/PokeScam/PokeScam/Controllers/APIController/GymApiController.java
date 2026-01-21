package com.PokeScam.PokeScam.Controllers.APIController;

import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Services.GymProgressService;
import com.PokeScam.PokeScam.Services.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/player")
public class GymApiController {

    private final UserService userService;
    private final GymProgressService gymProgressService;

    public GymApiController(UserService userService,
            GymProgressService gymProgressService) {
        this.userService = userService;
        this.gymProgressService = gymProgressService;
    }

    /** Get a player's completed gyms */
    @GetMapping("/{playerId}/gyms")
    public List<Gym> getCompletedGyms(@PathVariable int playerId) {
        User player = userService.getUserById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        System.out.println(player);
        return gymProgressService.getCompletedGyms(player);
    }
}
