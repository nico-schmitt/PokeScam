package com.PokeScam.PokeScam.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Repos.BoxRepository;
import com.PokeScam.PokeScam.Repos.PokemonRepository;

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
        newBox.setOwnerID(userDetails.getThisUser());
        Optional<Integer> nextUserBoxID = getNextUserBoxID();
        if(nextUserBoxID.isPresent()) {
            newBox.setUserBoxID(nextUserBoxID.get());
        } else {
            newBox.setUserBoxID(0);
        }
        boxRepo.save(newBox);
    }

    public Optional<Box> getNextFreeBox() {
        List<Box> boxes = boxRepo.findByOwnerID(userDetails.getThisUser());
        for(Box b : boxes) {
            if(pokemonRepo.countByBoxID(b) < BOX_CAPACITY)
                return Optional.of(b);
        }
        return Optional.empty();
    }

    private Optional<Integer> getNextUserBoxID() {
        List<Box> boxes = boxRepo.findByOwnerID(userDetails.getThisUser());
        if(boxes.size() > 0) {
            int nextUserBoxID = 0;
            for(Box b : boxes) {
                nextUserBoxID = b.getUserBoxID() + 1;
            }
            return Optional.of(nextUserBoxID);
        }
        return Optional.empty();
    }

    public List<Box> getAllBoxes() {
        return boxRepo.findByOwnerID(userDetails.getThisUser());
    }
}