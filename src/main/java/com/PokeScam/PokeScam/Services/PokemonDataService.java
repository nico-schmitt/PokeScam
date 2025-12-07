package com.PokeScam.PokeScam.Services;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;

@Service
public class PokemonDataService {
    private final PokemonRepository pokemonRepo;
    private final UserRepository userRepo;
    private final PokeAPIService pokeAPIService;

    public PokemonDataService(PokemonRepository pokemonRepo, UserRepository userRepo, PokeAPIService pokeAPIService) {
        this.pokemonRepo = pokemonRepo;
        this.userRepo = userRepo;
        this.pokeAPIService = pokeAPIService;
    }

    public List<PokemonDTO> getPkmnTeamInfo() {
        return getPkmnTeam().stream().map(
            p -> {
                PokemonDTO pDTO = new PokemonDTO();
                pDTO.setName(p.getName());
                pDTO.setImageURL(pokeAPIService.getImageURL(p.getName()));
                return pDTO;
            })
            .toList();
    }

    public void savePkmn(Pokemon pkmnToSave) {
        Pokemon p = new Pokemon();
        User user = userRepo.findByUsername(getUserDetails().getUsername());
        p.setName(pkmnToSave.getName());
        p.setOwnerID(user);
        pokemonRepo.save(p);
    }

    public UserDetails getUserDetails() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (UserDetails)principal;
    }

    private List<Pokemon> getPkmnTeam() {
        return pokemonRepo.findByOwnerID(userRepo.findByUsername(getUserDetails().getUsername()));
    }
}
