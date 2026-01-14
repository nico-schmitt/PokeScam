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

        List<PokedexEntry> allEntries = pokedexRepo.findAllByUser(user)
                .stream()
                .filter(entry -> entry.isSeen() && entry.getSpeciesName() != null)
                .collect(Collectors.toList());

        List<PokemonWithDex> allWithDex = new ArrayList<>();
        for (PokedexEntry entry : allEntries) {
            PokemonDTO dto = pokeAPIService.populateRandomPokemonDTO(entry.getSpeciesName())
                    .withSeenCaught(entry.isSeen(), entry.isCaught());

            int dexNumber = pokeAPIService.getNationalDexNumber(entry.getSpeciesName());
            allWithDex.add(new PokemonWithDex(dto, dexNumber));
        }

        allWithDex.sort((a, b) -> Integer.compare(a.dex(), b.dex()));

        int start = page * size;
        int end = Math.min(start + size, allWithDex.size());
        List<PokemonDTO> pageDTOs = allWithDex.subList(start, end).stream()
                .map(pwd -> pwd.dto())
                .collect(Collectors.toList());

        Page<PokemonDTO> pokemonPage = new PageImpl<>(pageDTOs, PageRequest.of(page, size), allWithDex.size());

        model.addAttribute("pokemonPage", pokemonPage);

        return "pokedex";
    }

    private record PokemonWithDex(PokemonDTO dto, int dex) {
    }
}
