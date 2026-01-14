package com.PokeScam.PokeScam.Services;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.PokedexEntry;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokedexEntryRepository;
import com.PokeScam.PokeScam.Services.PokeAPIService;

@Service
public class PokedexService {

    private final PokedexEntryRepository pokedexEntryRepository;
    private final PokeAPIService pokeAPIService;

    public PokedexService(PokedexEntryRepository pokedexEntryRepository,
            PokeAPIService pokeAPIService) {
        this.pokedexEntryRepository = pokedexEntryRepository;
        this.pokeAPIService = pokeAPIService;
    }

    public record DexEntry(PokedexEntry entry, int dexNumber, PokemonDTO dto) {
    }

    public List<DexEntry> getSortedSeenDex(User user) {
        List<PokedexEntry> entries = pokedexEntryRepository.findByUser(user).stream()
                .filter(PokedexEntry::isSeen)
                .filter(e -> e.getSpeciesName() != null && !e.getSpeciesName().isBlank())
                .collect(Collectors.toList());

        return entries.stream()
                .map(entry -> {
                    PokemonDTO dto = pokeAPIService.populateRandomPokemonDTO(entry.getSpeciesName());
                    int dexNumber = pokeAPIService.getNationalDexNumber(entry.getSpeciesName());
                    return new DexEntry(entry, dexNumber, dto);
                })
                .sorted(Comparator.comparingInt(DexEntry::dexNumber))
                .collect(Collectors.toList());
    }

    public void markSeen(User user, String speciesName) {
        PokedexEntry entry = pokedexEntryRepository
                .findByUserAndSpeciesName(user, speciesName)
                .orElseGet(() -> {
                    PokedexEntry e = new PokedexEntry();
                    e.setUser(user);
                    e.setSpeciesName(speciesName);
                    e.setSeen(true);
                    e.setCaught(false);
                    return e;
                });

        if (!entry.isSeen()) {
            entry.setSeen(true);
        }

        pokedexEntryRepository.save(entry);
    }

    public void markCaught(User user, String speciesName) {
        PokedexEntry entry = pokedexEntryRepository
                .findByUserAndSpeciesName(user, speciesName)
                .orElseGet(() -> {
                    PokedexEntry e = new PokedexEntry();
                    e.setUser(user);
                    e.setSpeciesName(speciesName);
                    e.setSeen(true);
                    e.setCaught(true);
                    return e;
                });

        if (!entry.isCaught()) {
            entry.setCaught(true);
        }

        pokedexEntryRepository.save(entry);
    }
}
