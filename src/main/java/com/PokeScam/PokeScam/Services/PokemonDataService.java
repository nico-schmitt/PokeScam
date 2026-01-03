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
import org.springframework.beans.factory.annotation.Value;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_AllStats;
import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;

import groovyjarjarantlr4.v4.parse.ANTLRParser.elementOptions_return;
import jakarta.transaction.Transactional;

@Service
public class PokemonDataService {

    public static final int POKEMON_TEAM_SIZE = 6;

    private final PokemonRepository pokemonRepo;
    private final UserRepository userRepo;
    private final BoxService boxService;
    private final PokeAPIService pokeAPIService;
    private final CustomUserDetails userDetails;

    public PokemonDataService(PokemonRepository pokemonRepo, BoxService boxService, PokeAPIService pokeAPIService, CustomUserDetails userDetails, UserRepository userRepo) {
        this.pokemonRepo = pokemonRepo;
        this.boxService = boxService;
        this.pokeAPIService = pokeAPIService;
        this.userDetails = userDetails;
        this.userRepo = userRepo;
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

        p.setAtk(pkmnToSave.allStats().atk().statValue());
        p.setDef(pkmnToSave.allStats().def().statValue());
        p.setSpa(pkmnToSave.allStats().spa().statValue());
        p.setSpd(pkmnToSave.allStats().spd().statValue());
        p.setSpe(pkmnToSave.allStats().spe().statValue());
        p.setHpBaseStat(pkmnToSave.allStats().hp().baseStat());
        p.setAtkBaseStat(pkmnToSave.allStats().atk().baseStat());
        p.setDefBaseStat(pkmnToSave.allStats().def().baseStat());
        p.setSpaBaseStat(pkmnToSave.allStats().spa().baseStat());
        p.setSpdBaseStat(pkmnToSave.allStats().spd().baseStat());
        p.setSpeBaseStat(pkmnToSave.allStats().spe().baseStat());
        p.setMove1(pkmnToSave.allMoves().moves().get(0).apiName());
        p.setMove2(pkmnToSave.allMoves().moves().get(1).apiName());
        p.setMove3(pkmnToSave.allMoves().moves().get(2).apiName());
        p.setMove4(pkmnToSave.allMoves().moves().get(3).apiName());

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
        if(lastLogout == null)
            return;
        long secondsSinceLastLogout = Duration.between(lastLogout, Instant.now()).getSeconds();
        List<Pokemon> allPkmnInBox = pokemonRepo.findByOwnerIdAndInBox(userDetails.getThisUser(), true);
        int amountToHealBy = (int)secondsSinceLastLogout;
        allPkmnInBox.forEach(pkmn->adjustPkmnHealth(pkmn, amountToHealBy));
        pokemonRepo.saveAll(allPkmnInBox);
    }

    public int adjustPkmnHealth(Pokemon pkmnToAdjust, int adjustment) {
        int curHpBefore = pkmnToAdjust.getCurHp();
        int newHealth = Math.clamp(pkmnToAdjust.getCurHp()+adjustment, 0, pkmnToAdjust.getMaxHp());
        pkmnToAdjust.setCurHp(newHealth);
        int curHpAfter = pkmnToAdjust.getCurHp();
        int actualHealAmount = curHpAfter - curHpBefore;
        return actualHealAmount;
    }

    public void adjustPkmnHealth(int id, int adjustment) {
        pokemonRepo.findById(id).ifPresent(pkmn->{
            adjustPkmnHealth(pkmn, adjustment);
        });
    }

    public NotificationMsg healPkmnForCost(int id, int cost, int healAmount) {
        User user = userDetails.getThisUser();
        Pokemon pkmnToHeal = pokemonRepo.findByIdAndOwnerId(id, user);
        NotificationMsg msg;

        if(pkmnToHeal != null && user.getCurrency() >= cost) {
            user.setCurrency(user.getCurrency()-cost);
            userRepo.save(user);
            int actualHealAmount = adjustPkmnHealth(pkmnToHeal, healAmount);
            pokemonRepo.save(pkmnToHeal);
            msg = new NotificationMsg(
                String.format("Healed %s for %d", pkmnToHeal.getName(), actualHealAmount),
                true
            );
        } else {
            msg = new NotificationMsg(
            "Not enough currency",
            false
            );
        }

        return msg;
    }
}