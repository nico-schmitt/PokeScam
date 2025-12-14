package com.PokeScam.PokeScam.Controllers.APIController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.RequestMapping;

import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.BoxRepository;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class PokemonRestController {

    private final PokemonDataService pokemonDataService;
    private final PokemonRepository pokemonRepo;
    private final UserRepository userRepo;
    private final BoxRepository boxRepo;
    private final PokeAPIService pokeAPIService;

    record PokemonCreateDTO(String name, String ownerUsername, Boolean inBox, Integer boxId) {}
    record PokemonUpdateDTO(String name, String ownerUsername, Boolean inBox, Integer boxId) {}

    public PokemonRestController(PokemonRepository pokemonRepo, UserRepository userRepo, BoxRepository boxRepo, PokemonDataService pokemonDataService, PokeAPIService pokeAPIService) {
        this.pokemonRepo = pokemonRepo;
        this.userRepo = userRepo;
        this.boxRepo = boxRepo;
        this.pokemonDataService = pokemonDataService;
        this.pokeAPIService = pokeAPIService;
    }

    @GetMapping("/pokemon")
    public ResponseEntity<List<Pokemon>> getAllPkmn() {
        return ResponseEntity.ok(pokemonRepo.findAll());
    }

    @GetMapping("/pokemon/{id}")
    public ResponseEntity<Pokemon> getPkmnById(@PathVariable Integer id) {
        Optional<Pokemon> pkmn = pokemonRepo.findById(id);
        if(pkmn.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pkmn.get());
    }

    @PostMapping("/pokemon")
    public ResponseEntity<Pokemon> createPkmn(@RequestBody PokemonCreateDTO pkmnToCreate) throws URISyntaxException {
        if(pkmnToCreate.name == null || pkmnToCreate.ownerUsername == null || pkmnToCreate.inBox == null)
            return ResponseEntity.badRequest().build();
        Optional<User> userOfNewPkmn = userRepo.findByUsername(pkmnToCreate.ownerUsername);
        if(userOfNewPkmn.isEmpty()) 
            return ResponseEntity.notFound().build();
        if(!pokeAPIService.pokemonExists(pkmnToCreate.name))
            return ResponseEntity.notFound().build();

        Pokemon newPkmn = new Pokemon();

        newPkmn.setName(pkmnToCreate.name);
        newPkmn.setOwnerId(userOfNewPkmn.get());

        if(pkmnToCreate.inBox == false) {
            if(pokemonDataService.isTeamFull(userOfNewPkmn.get()))
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_CONTENT);
            newPkmn.setInBox(false);
            newPkmn.setBoxId(null);
        } else {
            if(pkmnToCreate.boxId == null)
                return ResponseEntity.badRequest().build();
            Box boxToPutIn = boxRepo.findByOwnerIdAndUserBoxId(userOfNewPkmn.get(), pkmnToCreate.boxId);
            if(boxToPutIn == null)
                return ResponseEntity.notFound().build();
            newPkmn.setInBox(pkmnToCreate.inBox);
            newPkmn.setBoxId(boxToPutIn);
        }

        newPkmn = pokemonRepo.save(newPkmn);
        URI locationURL = new URI("/api/pokemon/"+newPkmn.getId());
        System.out.println(pkmnToCreate+"\n\n\n\n\n\n\n");
        return ResponseEntity.created(locationURL).body(newPkmn);
    }

    @PatchMapping("/pokemon/{id}")
    public ResponseEntity<Pokemon> updatePkmn(@PathVariable int id, @RequestBody PokemonUpdateDTO updateInfo) {
        Optional<Pokemon> pkmnOpt = pokemonRepo.findById(id);
        if(pkmnOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Pokemon pkmn = pkmnOpt.get();

        User userToUpdatePkmnOf;
        if(updateInfo.ownerUsername != null) {
            Optional<User> newUser = userRepo.findByUsername(updateInfo.ownerUsername);
            if(newUser.isEmpty())
                return ResponseEntity.notFound().build();
            userToUpdatePkmnOf = newUser.get();
        } else {
            userToUpdatePkmnOf = userRepo.getReferenceById(pkmn.getOwnerId().getId());
        }
        pkmn.setOwnerId(userToUpdatePkmnOf);

        System.out.println(updateInfo+"\n\n\n\n\n");
        if(updateInfo.name != null) {
            if(!pokeAPIService.pokemonExists(updateInfo.name))
                return ResponseEntity.notFound().build();
            pkmn.setName(updateInfo.name);
        }

        if(updateInfo.inBox != null) {
            if(updateInfo.inBox == false) {
                if(pokemonDataService.isTeamFull(userToUpdatePkmnOf))
                    return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_CONTENT);
                pkmn.setInBox(false);
                pkmn.setBoxId(null);
            } else {
                if(updateInfo.boxId == null)
                    return ResponseEntity.badRequest().build();
                Box boxToPutIn = boxRepo.findByOwnerIdAndUserBoxId(userToUpdatePkmnOf, updateInfo.boxId);
                if(boxToPutIn == null)
                    return ResponseEntity.notFound().build();
                pkmn.setInBox(true);
                pkmn.setBoxId(boxToPutIn);
            }
        }

        pkmn = pokemonRepo.save(pkmn);

        return ResponseEntity.ok(pkmn);
    }

    @DeleteMapping("/pokemon/{id}")
    public ResponseEntity<Pokemon> deletePkmnById(@PathVariable int id) {
        Optional<Pokemon> pkmn = pokemonRepo.findById(id);
        if(pkmn.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        pokemonRepo.deleteById(id);
        return ResponseEntity.ok(pkmn.get());
    }
}