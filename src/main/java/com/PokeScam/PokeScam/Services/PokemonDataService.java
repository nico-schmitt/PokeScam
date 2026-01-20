package com.PokeScam.PokeScam.Services;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_AllStats;
import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class PokemonDataService {

    public static final int POKEMON_TEAM_SIZE = 6;

    private final PokemonRepository pokemonRepo;
    private final UserRepository userRepo;
    private final BoxService boxService;
    private final PokeAPIService pokeAPIService;
    private final CustomUserDetails userDetails;
    private final PokemonCalcService pokemonCalcService;

    public PokemonDataService(PokemonRepository pokemonRepo, BoxService boxService, PokeAPIService pokeAPIService,
            CustomUserDetails userDetails, UserRepository userRepo, PokemonCalcService pokemonCalcService) {
        this.pokemonRepo = pokemonRepo;
        this.boxService = boxService;
        this.pokeAPIService = pokeAPIService;
        this.userDetails = userDetails;
        this.userRepo = userRepo;
        this.pokemonCalcService = pokemonCalcService;
    }

    // ==================== TEAM DTOs ====================

    public List<PokemonDTO> getPkmnTeamInfoDTO() {
        List<Pokemon> teamPkmn = pokemonRepo.findByOwnerIdAndInBoxFalse(userDetails.getThisUser());
        List<PokemonDTO> teamPkmnDTO = teamPkmn.stream().map(p -> pokeAPIService.fetchPokemonDTO(p))
                .collect(Collectors.toList());
        while (teamPkmnDTO.size() < POKEMON_TEAM_SIZE) {
            teamPkmnDTO.add(PokemonDTO.getEmpty());
        }
        return teamPkmnDTO;
    }

    public List<Pokemon> getPkmnTeamInfo() {
        List<Pokemon> teamPkmn = pokemonRepo.findByOwnerIdAndInBoxFalse(userDetails.getThisUser());
        while (teamPkmn.size() < POKEMON_TEAM_SIZE) {
            teamPkmn.add(null);
        }
        return teamPkmn;
    }

    // ==================== HEALTH / HEAL ====================

    public int adjustPkmnHealth(Pokemon pkmnToAdjust, int adjustment) {
        int curHpBefore = pkmnToAdjust.getCurHp();
        int newHealth = Math.max(0, Math.min(pkmnToAdjust.getCurHp() + adjustment, pkmnToAdjust.getMaxHp()));
        pkmnToAdjust.setCurHp(newHealth);
        return newHealth - curHpBefore;
    }

    public void adjustPkmnHealth(int id, int adjustment) {
        pokemonRepo.findById(id).ifPresent(pkmn -> adjustPkmnHealth(pkmn, adjustment));
    }

    // ==================== LEVEL-UP / EXP ====================

    public void gainExp(Pokemon pokemon, int expToGain) {
        pokemon.setExp(pokemon.getExp() + expToGain);
        checkLevelUp(pokemon);
        pokemonRepo.save(pokemon);
    }

    public boolean checkLevelUp(Pokemon pokemon) {
        boolean leveledUp = false;
        int nextLevelExp = getExpForNextLevel(pokemon.getLevel());
        while (pokemon.getExp() >= nextLevelExp && pokemon.getLevel() < 100) {
            pokemon.setLevel(pokemon.getLevel() + 1);
            pokemon.setExp(pokemon.getExp() - nextLevelExp);
            recalcStats(pokemon);
            checkEvolution(pokemon); // optional evolution
            nextLevelExp = getExpForNextLevel(pokemon.getLevel());
            leveledUp = true;
        }
        return leveledUp;
    }

    private int getExpForNextLevel(int level) {
        // Example quadratic formula for leveling
        return (int) Math.floor(Math.pow(level + 1, 3));
    }

    private void recalcStats(Pokemon p) {
        p.setMaxHp(pokemonCalcService.calcPkmnMaxHp(p.getHpBaseStat(), p.getLevel()));
        p.setAtk(pokemonCalcService.calcPkmnAtk(p.getAtkBaseStat(), p.getLevel()));
        p.setDef(pokemonCalcService.calcPkmnDef(p.getDefBaseStat(), p.getLevel()));
        p.setSpa(pokemonCalcService.calcPkmnSpa(p.getSpaBaseStat(), p.getLevel()));
        p.setSpd(pokemonCalcService.calcPkmnSpd(p.getSpdBaseStat(), p.getLevel()));
        p.setSpe(pokemonCalcService.calcPkmnSpe(p.getSpeBaseStat(), p.getLevel()));
        if (p.getCurHp() > p.getMaxHp()) {
            p.setCurHp(p.getMaxHp());
        }
    }

    private void checkEvolution(Pokemon p) {
        // Placeholder for evolution logic
        // e.g., if level >= evolutionLevel -> evolve
    }

    // ==================== ACTIVE POKÉMON ====================

    public Pokemon getActivePkmn() {
        return pokemonRepo.findByOwnerIdAndIsActivePkmnTrue(userDetails.getThisUser());
    }

    public PokemonDTO getActivePkmnDTO() {
        return pokeAPIService.fetchPokemonDTO(getActivePkmn());
    }

    public NotificationMsg setNewActivePkmn(Pokemon newActivePkmn, Pokemon curActivePkmn) {
        if (curActivePkmn != null) {
            curActivePkmn.setActivePkmn(false);
            pokemonRepo.save(curActivePkmn);
        }
        newActivePkmn.setActivePkmn(true);
        pokemonRepo.save(newActivePkmn);
        return new NotificationMsg(
                String.format("Set %s as new active Pokemon!", newActivePkmn.getName()), true);
    }

    // ---------------- USER TEAM ----------------
    public List<PokemonDTO> getPkmnTeamInfoOfUser(User user) {
        List<Pokemon> teamPkmn = pokemonRepo.findByOwnerIdAndInBoxFalse(user);
        List<PokemonDTO> teamPkmnDTO = teamPkmn.stream()
                .map(p -> pokeAPIService.fetchPokemonDTO(p))
                .collect(Collectors.toList());
        while (teamPkmnDTO.size() < POKEMON_TEAM_SIZE) {
            teamPkmnDTO.add(PokemonDTO.getEmpty());
        }
        return teamPkmnDTO;
    }

    // ---------------- POPULATE POKEMON FROM DTO ----------------
    public void populatePkmnWithPkmnDTOValues(Pokemon p, PokemonDTO pkmnDTO) {
        p.setActivePkmn(pkmnDTO.isActivePkmn());
        p.setLevel(pkmnDTO.level());
        p.setExp(pkmnDTO.exp());
        p.setMaxHp(pkmnDTO.maxHp());
        p.setCurHp(pkmnDTO.curHp());

        p.setAtk(pkmnDTO.allStats().atk().statValue());
        p.setDef(pkmnDTO.allStats().def().statValue());
        p.setSpa(pkmnDTO.allStats().spa().statValue());
        p.setSpd(pkmnDTO.allStats().spd().statValue());
        p.setSpe(pkmnDTO.allStats().spe().statValue());

        p.setHpBaseStat(pkmnDTO.allStats().hp().baseStat());
        p.setAtkBaseStat(pkmnDTO.allStats().atk().baseStat());
        p.setDefBaseStat(pkmnDTO.allStats().def().baseStat());
        p.setSpaBaseStat(pkmnDTO.allStats().spa().baseStat());
        p.setSpdBaseStat(pkmnDTO.allStats().spd().baseStat());
        p.setSpeBaseStat(pkmnDTO.allStats().spe().baseStat());

        p.setMove1(pkmnDTO.allMoves().moves().get(0).apiName());
        p.setMove2(pkmnDTO.allMoves().moves().get(1).apiName());
        p.setMove3(pkmnDTO.allMoves().moves().get(2).apiName());
        p.setMove4(pkmnDTO.allMoves().moves().get(3).apiName());
    }

    public Page<Pokemon> getPkmnInBoxPage(int boxId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return pokemonRepo.findByOwnerIdAndInBoxAndBoxId(userDetails.getThisUser(), true, boxService.getBox(boxId),
                pageable);
    }

    public String addPokemon(PokemonDTO pkmnToSave) {
        Pokemon p = new Pokemon();
        User user = userDetails.getThisUser();
        String addMsg;
        boolean errOccuredWhileAdding = false;
        boolean pokemonExists = pokeAPIService.pokemonExists(pkmnToSave.apiName());
        if (!pokemonExists) {
            addMsg = "Pokemon does not exist";
            return addMsg;
        }

        p.setName(pkmnToSave.apiName());
        p.setOwnerId(user);

        if (getTeamPkmn().size() < POKEMON_TEAM_SIZE) {
            p.setInBox(false);
            addMsg = "Added to team";
        } else {
            p.setInBox(true);
            Optional<Box> nextFreeBox = boxService.getNextFreeBox();
            if (nextFreeBox.isPresent()) {
                p.setBoxId(nextFreeBox.get());
                addMsg = "Added to box " + nextFreeBox.get().getUserBoxId();
            } else {
                errOccuredWhileAdding = true;
                addMsg = "Couldn't add Pokemon. No box had enough space";
            }
        }

        populatePkmnWithPkmnDTOValues(p, pkmnToSave);

        if (!errOccuredWhileAdding) {
            pokemonRepo.save(p);
        }
        return addMsg;
    }

    public String addPokemonFromName(String pkmnName) {
        PokemonDTO pkmn = pokeAPIService.fetchPokemonDTO(pkmnName);
        String addMsg = addPokemon(pkmn);
        return addMsg;
    }

    public Optional<Pokemon> getPkmnById(int id) {
        return pokemonRepo.findById(id);
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
        return pokeAPIService.fetchPokemonDTO(pkmn);
    }

    public boolean isTeamFull(User user) {
        List<Pokemon> teamPkmn = pokemonRepo.findByOwnerIdAndInBoxFalse(user);
        return teamPkmn.size() >= POKEMON_TEAM_SIZE;
    }

    public void healPkmnByLastLogout(Instant lastLogout) {
        if (lastLogout == null)
            return;
        long secondsSinceLastLogout = Duration.between(lastLogout, Instant.now()).getSeconds();
        List<Pokemon> allPkmnInBox = pokemonRepo.findByOwnerIdAndInBox(userDetails.getThisUser(), true);
        int amountToHealBy = (int) secondsSinceLastLogout;
        allPkmnInBox.forEach(pkmn -> adjustPkmnHealth(pkmn, amountToHealBy));
        pokemonRepo.saveAll(allPkmnInBox);
    }

    public NotificationMsg healPkmnForCost(int id, int cost, int healAmount) {
        User user = userDetails.getThisUser();
        Pokemon pkmnToHeal = pokemonRepo.findByIdAndOwnerId(id, user);
        NotificationMsg msg;

        if (pkmnToHeal != null && user.getCurrency() >= cost) {
            user.setCurrency(user.getCurrency() - cost);
            userRepo.save(user);
            int actualHealAmount = adjustPkmnHealth(pkmnToHeal, healAmount);
            pokemonRepo.save(pkmnToHeal);
            msg = new NotificationMsg(
                    String.format("Healed %s for %d", pkmnToHeal.getName(), actualHealAmount),
                    true);
        } else {
            msg = new NotificationMsg(
                    "Not enough currency",
                    false);
        }

        return msg;
    }

    public record PokemonWithMovesDTO(
            Pokemon pokemon,
            PokemonDTO dto,
            List<String> moves) {

        public int id() {
            return dto.id();
        }

        public boolean isInBox() {
            return dto.isInBox();
        }

        public String apiName() {
            return dto.apiName();
        }

        public String displayName() {
            return dto.displayName();
        }

        public String imageURL() {
            return dto.imageURL();
        }

        public String flavorText() {
            return dto.flavorText();
        }

        public int level() {
            return dto.level();
        }

        public int exp() {
            return dto.exp();
        }

        public int maxHp() {
            return dto.maxHp();
        }

        public int curHp() {
            return dto.curHp();
        }

        public PokemonDTO_AllStats allStats() {
            return dto.allStats();
        }

        public PokemonDTO.PokemonDTO_AllMoves allMoves() {
            return dto.allMoves();
        }

        public boolean isActivePkmn() {
            return dto.isActivePkmn();
        }
    }

    public PokemonWithMovesDTO getPokemonWithMovesDTO(Pokemon pkmn) {
        if (pkmn == null)
            return null;

        PokemonDTO dto = pokeAPIService.fetchPokemonDTO(pkmn);
        List<String> moves = Stream.of(pkmn.getMove1(), pkmn.getMove2(),
                pkmn.getMove3(), pkmn.getMove4())
                .filter(Objects::nonNull) // remove nulls
                .toList();

        return new PokemonWithMovesDTO(pkmn, dto, moves);
    }

    public PokemonDTO convertToPokemonDTO(PokemonWithMovesDTO dto) {
        // Create a PokemonDTO from PokemonWithMovesDTO fields
        return new PokemonDTO(
                dto.id(),
                dto.isInBox(),
                dto.apiName(),
                dto.displayName(),
                dto.imageURL(),
                dto.flavorText(),
                dto.level(),
                dto.exp(),
                dto.maxHp(),
                dto.curHp(),
                dto.allStats(),
                dto.allMoves(),
                dto.isActivePkmn(),
                false, // seen
                false, // caught
                0,
                PokemonCalcService.expToNextLevel(dto.level()));
    }

    // ---------------- POPULATE ITEM PAGES WITH POKEMONS ----------------
    public Page<Pokemon> getDamagedPokemonsInPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return pokemonRepo.findDamagedPokemons(userDetails.getThisUser(), pageable);
    }

    public Page<Pokemon> getFaintedPokemonsInPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return pokemonRepo.findFaintedPokemons(userDetails.getThisUser(), pageable);
    }
}