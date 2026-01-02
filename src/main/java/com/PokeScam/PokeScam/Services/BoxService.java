package com.PokeScam.PokeScam.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Repos.BoxRepository;
import com.PokeScam.PokeScam.Repos.PokemonRepository;

import jakarta.transaction.Transactional;

@Service
public class BoxService {
    private static final int BOX_CAPACITY = 5;

    private final BoxRepository boxRepo;
    private final PokemonRepository pokemonRepo;
    private final CustomUserDetails userDetails;
    
    public BoxService(BoxRepository boxRepo, PokemonRepository pokemonRepo, CustomUserDetails userDetails) {
        this.boxRepo = boxRepo;
        this.pokemonRepo = pokemonRepo;
        this.userDetails = userDetails;
    }

    public void addBox() {
        Box newBox = new Box();
        newBox.setOwnerId(userDetails.getThisUser());
        Optional<Integer> nextUserBoxID = getNextUserBoxID();
        if(nextUserBoxID.isPresent()) {
            newBox.setUserBoxId(nextUserBoxID.get());
        } else {
            newBox.setUserBoxId(0);
        }
        boxRepo.save(newBox);
    }

    public Optional<Box> getNextFreeBox() {
        List<Box> boxes = boxRepo.findByOwnerId(userDetails.getThisUser());
        for(Box b : boxes) {
            if(pokemonRepo.countByBoxId(b) < BOX_CAPACITY)
                return Optional.of(b);
        }
        return Optional.empty();
    }

    private Optional<Integer> getNextUserBoxID() {
        List<Box> boxes = boxRepo.findByOwnerId(userDetails.getThisUser());
        if(boxes.size() > 0) {
            int nextUserBoxID = 0;
            for(Box b : boxes) {
                nextUserBoxID = b.getUserBoxId() + 1;
            }
            return Optional.of(nextUserBoxID);
        }
        return Optional.empty();
    }

    public List<Box> getAllBoxes() {
        return boxRepo.findByOwnerId(userDetails.getThisUser());
    }

    public Box getBox(int boxId) {
        return boxRepo.findByOwnerIdAndUserBoxId(userDetails.getThisUser(), boxId);
    }

    @Transactional
    public void swapTeamPkmnToBox(int teamPkmnToSwap, int otherPkmnToSwap) {
        Pokemon teamPkmn = pokemonRepo.findByIdAndOwnerId(teamPkmnToSwap, userDetails.getThisUser());
        Pokemon otherPkmn = pokemonRepo.findByIdAndOwnerId(otherPkmnToSwap, userDetails.getThisUser());
        if(teamPkmn != null) {
            teamPkmn.setInBox(true);
            teamPkmn.setBoxId(otherPkmn.getBoxId());
            pokemonRepo.save(teamPkmn);
        }
        otherPkmn.setInBox(false);
        otherPkmn.setBoxId(null);
        pokemonRepo.save(otherPkmn);
        pokemonRepo.flush();
    }
}