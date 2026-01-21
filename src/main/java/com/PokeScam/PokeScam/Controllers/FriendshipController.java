package com.PokeScam.PokeScam.Controllers;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.DTOs.FriendDTO;
import com.PokeScam.PokeScam.Model.Friendship;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.UserRepository;
import com.PokeScam.PokeScam.Services.FriendshipService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FriendshipController {
    private final FriendshipService friendshipService;
    private final CustomUserDetails userDetails;
    private final UserRepository userRepository;

    public FriendshipController(FriendshipService friendshipService, CustomUserDetails userDetails,
                                UserRepository userRepository) {
        this.friendshipService = friendshipService;
        this.userDetails = userDetails;
        this.userRepository = userRepository;
    }

    @GetMapping("/friends")
    public String friendsPage(Model model,
              @RequestParam(defaultValue = "0") int page,
              @RequestParam(defaultValue = "10") int size) {
        User user = userDetails.getThisUser();
        Page<Friendship> friendships = friendshipService.getFriends(user, page, size);
        List<FriendDTO> friends = friendships.map(f -> f.convertToDTO(user)).toList();

        model.addAttribute("friends", friends);
        model.addAttribute("pageInfo", friendships);
        model.addAttribute("pageSize", size);

        return "friends";
    }

    @PostMapping("/friends/request")
    public String sendFriendRequest(Model model,
            @RequestParam int userId) {
        friendshipService.sendFriendRequest(userDetails.getThisUser(), userRepository.findById(userId).get());
        return "redirect:/friends";
    }

    @PostMapping("/friends/{id}")
    public String deleteFriendship(@PathVariable int id) {
        friendshipService.removeFriendship(userDetails.getThisUser(), id);
        return "redirect:/friends";
    }

    @GetMapping("/inbox")
    public String inboxPage(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userDetails.getThisUser();
        Page<Friendship> requests = friendshipService.getincomingRequests(user, page, size);
        List<FriendDTO> requestDTOs = requests.map(r -> r.convertToDTO(user)).toList();

        model.addAttribute("requests", requestDTOs);
        model.addAttribute("pageInfo", requests);
        model.addAttribute("pageSize", size);

        return "inbox";
    }

    @PostMapping("/inbox/{id}/accept")
    public String acceptFriendRequest(@PathVariable int id) {
        friendshipService.acceptRequest(id, userDetails.getThisUser());
        return "redirect:/friends";
    }

    @PostMapping("/inbox/{id}/decline")
    public String declineFriendRequest(@PathVariable int id) {
        friendshipService.declineRequest(id, userDetails.getThisUser());
        return "redirect:/friends";
    }

    @GetMapping("/friends/{id}/trade")
    public String tradePage(@PathVariable int id,
                Model model,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {

        return "trade";
    }
}
