package com.PokeScam.PokeScam.Services;

import com.PokeScam.PokeScam.CustomUserDetails;
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

        List<Pokemon> pokemons = pokemonRepository.findByOwnerId(requester);
        Optional<Pokemon> pokemon = pokemonRepository.findById(requesterPkmnId);
        if (pokemon.isEmpty()) {
            throw new IllegalArgumentException("Trade requester Pokemon not found");
        }
        if (!pokemons.contains(pokemon.get())) {
            throw new IllegalArgumentException("Trade requester doesn't own specified pokemon");
        }

        pokemons = pokemonRepository.findByOwnerId(receiver);
        pokemon = pokemonRepository.findById(receiverPkmnId);
        if (pokemon.isEmpty()) {
            throw new IllegalArgumentException("Trade receiver Pokemon not found");
        }
        if (!pokemons.contains(pokemon.get())) {
            throw new IllegalArgumentException("Trade receiver doesn't own specified pokemon");
        }

        TradeRequest tradeRequest = new TradeRequest(requester, receiver,
                pokemonRepository.findById(requesterPkmnId).get(),
                pokemonRepository.findById(receiverPkmnId).get());

        tradeRepository.save(tradeRequest);
    }
}
