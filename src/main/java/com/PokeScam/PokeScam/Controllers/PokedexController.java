package com.PokeScam.PokeScam.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.Model.PokedexEntry;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokedexEntryRepository;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;

@Controller
public class PokedexController {

    private final PokedexEntryRepository pokedexRepo;
    private final PokeAPIService pokeAPIService;
    private final CustomUserDetails customUserDetails;

    public PokedexController(
            PokedexEntryRepository pokedexRepo,
            PokeAPIService pokeAPIService,
            CustomUserDetails customUserDetails) {
        this.pokedexRepo = pokedexRepo;
        this.pokeAPIService = pokeAPIService;
        this.customUserDetails = customUserDetails;
    }

    @GetMapping("/pokedex")
    public String viewPokedex(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        User user = customUserDetails.getThisUser();
        Pageable pageable = PageRequest.of(page, size);

        // Database handles sorting and pagination
        Page<PokedexEntry> entryPage = pokedexRepo
                .findByUserAndSeenTrueAndSpeciesNameIsNotNullOrderBySpeciesNameAsc(user, pageable);

        // Convert to DTOs
        List<PokemonDTO> dtos = entryPage.stream()
                .map(entry -> pokeAPIService.fetchPokemonDTO(entry.getSpeciesName())
                        .withSeenCaught(entry.isSeen(), entry.isCaught()))
                .toList();

        Page<PokemonDTO> pokemonPage = new PageImpl<>(dtos, pageable, entryPage.getTotalElements());

        model.addAttribute("pokemonPage", pokemonPage);
        return "pokedex";
    }

}
