package com.PokeScam.PokeScam.Services;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokemonRepository;

import jakarta.transaction.Transactional;

@Service
public class PokemonDataService {

    public static final int POKEMON_TEAM_SIZE = 6;

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
        List<Pokemon> teamPkmn = pokemonRepo.findByOwnerIdAndInBoxFalse(userDetails.getThisUser());
        List<PokemonDTO> teamPkmnDTO = teamPkmn.stream().map(p->pokeAPIService.populatePokemonDTO(p)).collect(Collectors.toList());
        if(teamPkmnDTO.size() < POKEMON_TEAM_SIZE) {
            for(int i = teamPkmnDTO.size(); i < POKEMON_TEAM_SIZE; i++) {
                teamPkmnDTO.add(PokemonDTO.getEmpty());
            }
        }
        return teamPkmnDTO;
    }

    public Page<Pokemon> getPkmnInBoxPage(int boxId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return pokemonRepo.findByOwnerIdAndInBoxAndBoxId(userDetails.getThisUser(), true , boxService.getBox(boxId), pageable);
    }

    public String addPokemon(PokemonDTO pkmnToSave) {
        Pokemon p = new Pokemon();
        User user = userDetails.getThisUser();
        String addMsg;
        boolean errOccuredWhileAdding = false;
        boolean pokemonExists = pokeAPIService.pokemonExists(pkmnToSave.apiName());
        if(!pokemonExists) {
            addMsg = "Pokemon does not exist";
            return addMsg;
        }

        p.setName(pkmnToSave.apiName());
        p.setOwnerId(user);

        if(getTeamPkmn().size() < POKEMON_TEAM_SIZE) {
            p.setInBox(false);
            addMsg = "Added to team";
        } else {
            p.setInBox(true);
            Optional<Box> nextFreeBox = boxService.getNextFreeBox();
            if(nextFreeBox.isPresent()) {
                p.setBoxId(nextFreeBox.get());
                addMsg = "Added to box " + nextFreeBox.get().getUserBoxId();
            } else {
                errOccuredWhileAdding = true;
                addMsg = "Couldn't add Pokemon. No box had enough space";
            }
        }

        p.setLevel(pkmnToSave.level());
        p.setExp(pkmnToSave.exp());
        p.setMaxHp(pkmnToSave.maxHp());
        p.setCurHp(pkmnToSave.curHp());
        p.setAtk(pkmnToSave.atk());
        p.setDef(pkmnToSave.def());
        p.setSpa(pkmnToSave.spa());
        p.setSpd(pkmnToSave.spd());
        p.setSpe(pkmnToSave.spe());
        p.setHpBaseStat(pkmnToSave.hpBase());
        p.setAtkBaseStat(pkmnToSave.atkBase());
        p.setDefBaseStat(pkmnToSave.defBase());
        p.setSpaBaseStat(pkmnToSave.spaBase());
        p.setSpdBaseStat(pkmnToSave.spdBase());
        p.setSpeBaseStat(pkmnToSave.speBase());

        if(!errOccuredWhileAdding) {
            pokemonRepo.save(p);
        }
        return addMsg;
    }

    public String addPokemonFromName(String pkmnName) {
        PokemonDTO pkmn = pokeAPIService.populateRandomPokemonDTO(pkmnName);
        String addMsg = addPokemon(pkmn);
        return addMsg;
    }

    private List<Pokemon> getAllPkmn() {
        return pokemonRepo.findByOwnerId(userDetails.getThisUser());
    }

    private List<Pokemon> getTeamPkmn() {
        return pokemonRepo.findByOwnerIdAndInBox(userDetails.getThisUser(), false);
    }

    @Transactional
    public void deletePkmn(int id) {
        pokemonRepo.deleteByIdAndOwnerId(id, userDetails.getThisUser());
    }

    public PokemonDTO getPkmnInfo(int id) {
        Pokemon pkmn = pokemonRepo.findByIdAndOwnerId(id, userDetails.getThisUser());
        return pokeAPIService.populatePokemonDTO(pkmn);
    }

    public boolean isTeamFull(User user) {
        List<Pokemon> teamPkmn = pokemonRepo.findByOwnerIdAndInBoxFalse(user);
        return teamPkmn.size() >= POKEMON_TEAM_SIZE;
    }

    public void healPkmnByLastLogout(Instant lastLogout) {
        long secondsSinceLastLogout = Duration.between(lastLogout, Instant.now()).getSeconds();
        List<Pokemon> allPkmnInBox = pokemonRepo.findByOwnerIdAndInBox(userDetails.getThisUser(), true);
        int amountToHealBy = (int)secondsSinceLastLogout;
        allPkmnInBox.forEach(pkmn->adjustPkmnHealth(pkmn, amountToHealBy));
        pokemonRepo.saveAll(allPkmnInBox);
    }

    public void adjustPkmnHealth(Pokemon pkmnToHeal, int adjustment) {
        int newHealth = Math.clamp(pkmnToHeal.getCurHp()+adjustment, 0, pkmnToHeal.getMaxHp());
        pkmnToHeal.setCurHp(newHealth);
    }
}