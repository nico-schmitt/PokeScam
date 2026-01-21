package com.PokeScam.PokeScam.Controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.PokeScam.PokeScam.Services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterData;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterDataSinglePkmn;
import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.SessionData;

@Controller
@RequestMapping("/gyms")
public class GymController {

    private final GymService gymService;
    private final SessionData sessionData;
    private final EncounterService encounterService;
    private final PokemonDataService pokemonDataService;
    private final GymProgressService gymProgressService;
    private final CustomUserDetails customUserDetails;
    private final UserService userService;

    public GymController(GymService gymService,
            SessionData sessionData,
            EncounterService encounterService,
            CustomUserDetails customUserDetails,
            PokemonDataService pokemonDataService,
            GymProgressService gymProgressService,
            UserService userService) {
        this.gymService = gymService;
        this.sessionData = sessionData;
        this.encounterService = encounterService;
        this.pokemonDataService = pokemonDataService;
        this.customUserDetails = customUserDetails;
        this.gymProgressService = gymProgressService;
        this.userService = userService;
    }

    /** List all gyms: NPC + player gyms */
    @GetMapping
    public String listGyms(Model model) {
        User currentUser = customUserDetails.getThisUser();

        // Fetch NPC gyms
        List<Gym> npcGyms = gymService.getAllNpcGyms();

        // Fetch player-owned gyms (optionally exclude the current user's gym if you
        // want)
        List<Gym> playerGyms = gymService.getAllPlayerGyms(); // create this method

        // Optionally, you can highlight the user's own gym in the template
        Gym userGym = playerGyms.stream()
                .filter(g -> currentUser.equals(g.getOwner()))
                .findFirst()
                .orElse(null);

        // Combine NPC gyms + player gyms (except user's own if you want)
        List<Gym> allGyms = new ArrayList<>();
        allGyms.addAll(npcGyms);
        allGyms.addAll(playerGyms);

        model.addAttribute("gyms", allGyms);
        model.addAttribute("playerGym", userGym); // for special display like before

        // Add completed gyms
        List<Long> completedGymIds = gymProgressService.getCompletedGymIdsForUser(currentUser);
        model.addAttribute("completedGymIds", completedGymIds);

        userService.updateRecentActivity(customUserDetails.getThisUser(), "Challenging a gym leader");

        return "gyms";
    }

    /** Show a single gym with its trainers and Pokémon */
    @GetMapping("/{gymId}")
    public String viewGym(@PathVariable Long gymId, Model model) {
        Gym gym = gymService.getGymById(gymId)
                .orElseThrow(() -> new IllegalArgumentException("Gym not found"));

        List<Map<String, Object>> trainers = gymService.getGymTrainersWithPokemon(gymId);

        model.addAttribute("gym", gym);
        model.addAttribute("trainers", trainers);
        model.addAttribute("isOwner", false);

        return "gymDetail";
    }

    /** Start a gym battle and save encounters to session */
    @PostMapping("/start/{gymId}")
    public String startGymBattle(@PathVariable Long gymId) {
        List<EncounterData> gymEncounters = gymService.getGymEncounters(gymId);
        sessionData.setSavedEncounterList(gymEncounters);
        sessionData.setEncounterProgress(0);

        return "redirect:/gyms/battle/0";
    }

    /** Show the current gym battle */
    @GetMapping("/battle/{encounterIdx}")
    public String gymBattle(@PathVariable int encounterIdx, Model model) {
        List<EncounterData> gymEncounters = sessionData.getSavedEncounterList();
        if (gymEncounters == null || gymEncounters.isEmpty() || encounterIdx >= gymEncounters.size()) {
            return "redirect:/gyms";
        }

        EncounterData encounterData = gymEncounters.get(encounterIdx);

        EncounterDataSinglePkmn activePkmn = encounterService.wrapPkmnInEncounterData(
                pokemonDataService.getActivePkmnDTO());
        EncounterDataSinglePkmn enemyPkmn = encounterService.getEnemyActivePkmnAtIdx(encounterIdx);
        List<EncounterDataSinglePkmn> pkmnTeamInfo = encounterService.getPkmnTeamInfo();
        List<EncounterDataSinglePkmn> encounterList = encounterService.getPokemonToFightListAtIdx(encounterIdx);

        model.addAttribute("activePkmn", activePkmn);
        model.addAttribute("enemyPkmn", enemyPkmn);
        model.addAttribute("pkmnTeam", pkmnTeamInfo);
        model.addAttribute("encounterList", encounterList);
        model.addAttribute("encounterData", encounterData);

        return "encounterBattle";
    }
}
