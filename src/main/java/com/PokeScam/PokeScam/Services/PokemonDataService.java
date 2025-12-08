package com.PokeScam.PokeScam.Services;

import java.util.List;
import java.util.stream.Collectors;

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

    private static final int POKEMON_TEAM_SIZE = 6;

    private final PokemonRepository pokemonRepo;
    private final UserRepository userRepo;
    private final PokeAPIService pokeAPIService;

    public PokemonDataService(PokemonRepository pokemonRepo, UserRepository userRepo, PokeAPIService pokeAPIService) {
        this.pokemonRepo = pokemonRepo;
        this.userRepo = userRepo;
        this.pokeAPIService = pokeAPIService;
    }

    public List<PokemonDTO> getPkmnTeamInfo() {
        List<PokemonDTO> teamList =
            getAllPkmn()
                .stream()
                .filter(p -> !p.isInBox())
                .map(p -> pokeAPIService.populatePokemonDTO(p))
                .collect(Collectors.toList());
        if(teamList.size() < POKEMON_TEAM_SIZE) {
            for(int i = teamList.size(); i < POKEMON_TEAM_SIZE; i++) {
                teamList.add(PokemonDTO.getEmpty());
            }
        }
        return teamList;
    }

    public List<PokemonDTO> getPkmnInBox() {
        return
            getAllPkmn()
                .stream()
                .filter(p -> p.isInBox())
                .map(p -> pokeAPIService.populatePokemonDTO(p))
                .toList();
    }

    public String addPokemon(Pokemon pkmnToSave) {
        User user = userRepo.findByUsername(getUserDetails().getUsername());
        String addMsg;
        Pokemon p = new Pokemon();
        p.setName(pkmnToSave.getName());
        p.setOwnerID(user);

        if(getAllPkmn().size() < POKEMON_TEAM_SIZE) {
            p.setInBox(false);
            addMsg = "Added to team";
        } else {
            p.setInBox(true);
            addMsg = "Added to box";
        }

        pokemonRepo.save(p);
        return addMsg;
    }

    public UserDetails getUserDetails() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (UserDetails)principal;
    }

    private List<Pokemon> getAllPkmn() {
        return pokemonRepo.findByOwnerID(userRepo.findByUsername(getUserDetails().getUsername()));
    }
}
