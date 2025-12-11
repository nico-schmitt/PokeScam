package com.PokeScam.PokeScam.Services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokemonRepository;

@Service
public class PokemonDataService {

    private static final int POKEMON_TEAM_SIZE = 6;

    private final PokemonRepository pokemonRepo;
    private final BoxService boxService;
    private final PokeAPIService pokeAPIService;
    private final CustomUserDetails userDetails;

    public PokemonDataService(PokemonRepository pokemonRepo, BoxService boxService, PokeAPIService pokeAPIService, CustomUserDetails userDetails) {
        this.pokemonRepo = pokemonRepo;
        this.boxService = boxService;
        this.pokeAPIService = pokeAPIService;
        this.userDetails = userDetails;
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

    public List<PokemonDTO> getPkmnInBox(int boxID) {
        return
            getAllPkmn()
                .stream()
                .filter(p -> p.isInBox() && p.getBoxID().getUserBoxID() == boxID)
                .map(p -> pokeAPIService.populatePokemonDTO(p))
                .toList();
    }

    public String addPokemon(Pokemon pkmnToSave) {
        Pokemon p = new Pokemon();
        User user = userDetails.getThisUser();
        String addMsg;
        boolean errOccuredWhileAdding = false;
        p.setName(pkmnToSave.getName());
        p.setOwnerID(user);

        if(getAllPkmn().size() < POKEMON_TEAM_SIZE) {
            p.setInBox(false);
            addMsg = "Added to team";
        } else {
            p.setInBox(true);
            Optional<Box> nextFreeBox = boxService.getNextFreeBox();
            if(nextFreeBox.isPresent()) {
                p.setBoxID(nextFreeBox.get());
                addMsg = "Added to box " + nextFreeBox.get().getUserBoxID();
            } else {
                errOccuredWhileAdding = true;
                addMsg = "Couldn't add Pokemon. No box had enough space";
            }
        }

        if(!errOccuredWhileAdding) {
            pokemonRepo.save(p);
        }
        return addMsg;
    }

    private List<Pokemon> getAllPkmn() {
        return pokemonRepo.findByOwnerID(userDetails.getThisUser());
    }
}