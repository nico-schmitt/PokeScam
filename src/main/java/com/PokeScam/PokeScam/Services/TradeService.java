package com.PokeScam.PokeScam.Services;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.DTOs.RequestStatus;
import com.PokeScam.PokeScam.Model.Friendship;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.TradeRequest;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.TradeRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TradeService {
    private final PokemonRepository pokemonRepository;
    private final CustomUserDetails customUserDetails;
    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;

    public TradeService(PokemonRepository pokemonRepository, CustomUserDetails customUserDetails,
                        UserRepository userRepository, TradeRepository tradeRepository) {
        this.pokemonRepository = pokemonRepository;
        this.customUserDetails = customUserDetails;
        this.userRepository = userRepository;
        this.tradeRepository = tradeRepository;
    }

    public void sendTradeRequest(User requester, User receiver, int requesterPkmnId, int receiverPkmnId) {
        if (requester.equals(receiver)) {
            throw new IllegalArgumentException("Cannot trade with yourself");
        }

        Pokemon requesterPkmn = pokemonRepository.findByIdAndOwnerId(requesterPkmnId, requester);
        if (requesterPkmn == null) {
            throw new IllegalArgumentException("Trade requester Pokemon not found");
        }

        Pokemon receiverPkmn = pokemonRepository.findByIdAndOwnerId(receiverPkmnId, receiver);
        if (receiverPkmn == null) {
            throw new IllegalArgumentException("Trade requester Pokemon not found");
        }

        TradeRequest tradeRequest = new TradeRequest(requester, receiver, requesterPkmn, receiverPkmn);

        tradeRepository.save(tradeRequest);
    }

    /*
    public void acceptRequest(Integer friendshipId, User receiver) {
        Friendship friendship = friendshipRepo.findById(friendshipId).orElseThrow();

        if (!friendship.getReceiver().equals(receiver)) {
            throw new IllegalArgumentException("Not your request");
        }

        friendship.setStatus(RequestStatus.ACCEPTED);
        friendshipRepo.save(friendship);
    }

    public void declineRequest(Integer friendshipId, User receiver) {
        Friendship friendship = friendshipRepo.findById(friendshipId).orElseThrow();

        if (!friendship.getReceiver().equals(receiver)) {
            throw new IllegalArgumentException("Not your request");
        }

        friendship.setStatus(RequestStatus.DECLINED);
        friendshipRepo.save(friendship);
    }
     */
}
