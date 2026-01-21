package com.PokeScam.PokeScam.Controllers;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.DTOs.FriendDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Friendship;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.UserRepository;
import com.PokeScam.PokeScam.Services.FriendshipService;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.TradeService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FriendshipController {
    private final FriendshipService friendshipService;
    private final CustomUserDetails userDetails;
    private final UserRepository userRepository;
    private final PokemonDataService pokemonDataService;
    private final PokeAPIService pokeAPIService;
    private final TradeService tradeService;

    public FriendshipController(FriendshipService friendshipService, CustomUserDetails userDetails,
                                UserRepository userRepository, PokemonDataService pokemonDataService,
                                PokeAPIService pokeAPIService, TradeService tradeService) {
        this.friendshipService = friendshipService;
        this.userDetails = userDetails;
        this.userRepository = userRepository;
        this.pokemonDataService = pokemonDataService;
        this.pokeAPIService = pokeAPIService;
        this.tradeService = tradeService;
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
        model.addAttribute("user", user);

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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String dir) {
        User user = userDetails.getThisUser();
        Page<Friendship> requests = friendshipService.getincomingRequests(user, page, size, sortBy, dir);
        List<FriendDTO> requestDTOs = requests.map(r -> r.convertToDTO(user)).toList();

        model.addAttribute("requests", requestDTOs);
        model.addAttribute("pageInfo", requests);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);

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
    public String tradePageReceiver(Model model,
                @PathVariable int id,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {

        Page<Pokemon> pkmnPage = pokemonDataService.getPkmnByOwnerId(id, page, size);
        List<PokemonDTO> pDTOs = pkmnPage.map(p -> pokeAPIService.fetchPokemonDTO(p)).toList();
        model.addAttribute("pkmns", pDTOs);
        model.addAttribute("pageInfo", pkmnPage);
        model.addAttribute("pageSize", size);
        model.addAttribute("receiverId", id);
        model.addAttribute("receiverName", userRepository.findById(id).get().getUsername());
        model.addAttribute("isFriendsPage", true);

        return "tradeReceiver";
    }

    @PostMapping("/friends/{ownerId}/trade/{pkmnId}")
    public String tradePageReceiverConfirm(Model model,
                            @PathVariable int ownerId,
                            @PathVariable int pkmnId) {
        model.addAttribute("receiverPkmnId", pkmnId);

        return "redirect:/friends/" + String.valueOf(ownerId) + "/trade/" + String.valueOf(pkmnId);
    }

    @GetMapping("/friends/{receiverId}/trade/{receiverPkmnId}")
    public String tradePageRequester(Model model,
                             @PathVariable int receiverId,
                             @PathVariable int receiverPkmnId,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        Page<Pokemon> pkmnPageRequester = pokemonDataService.getPkmnByOwnerId(userDetails.getThisUser().getId(), page, size);
        List<PokemonDTO> pDTOsRequester = pkmnPageRequester.map(p -> pokeAPIService.fetchPokemonDTO(p)).toList();
        model.addAttribute("pkmns", pDTOsRequester);
        model.addAttribute("pageInfo", pkmnPageRequester);
        model.addAttribute("pageSize", size);
        model.addAttribute("receiverId", receiverId);
        model.addAttribute("pkmnId", receiverPkmnId);
        model.addAttribute("isFriendsPage", true);
        model.addAttribute("isTradeConfirmation", true);

        return "tradeRequester";
    }

    @PostMapping("/friends/{receiverId}/trade/{receiverPkmnId}/{requesterPkmnId}")
    public String tradePageRequesterConfirm(Model model,
                             @PathVariable int receiverId,
                             @PathVariable int receiverPkmnId,
                             @PathVariable int requesterPkmnId) {
        // TODO:    Friend Request zu RequestDTO ändern
        //          Trade Requests auch in Inbox anzeigen
        //          Accept und Decline Link abändern bei Trade
        tradeService.sendTradeRequest(userDetails.getThisUser(), userRepository.findById(receiverId).get(),
                requesterPkmnId, receiverPkmnId);
        return "redirect:/friends";
    }
}
