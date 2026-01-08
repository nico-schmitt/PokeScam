package com.PokeScam.PokeScam.Controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Services.EncounterService;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterData;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterDataSinglePkmn;
import com.PokeScam.PokeScam.Services.GymService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.SessionData;

@Controller
@RequestMapping("/gyms")
public class GymController {

    private final GymService gymService;
    private final SessionData sessionData;
    private final EncounterService encounterService;
    private final PokemonDataService pokemonDataService;

    public GymController(GymService gymService,
            SessionData sessionData,
            EncounterService encounterService,
            PokemonDataService pokemonDataService) {
        this.gymService = gymService;
        this.sessionData = sessionData;
        this.encounterService = encounterService;
        this.pokemonDataService = pokemonDataService;
    }

    /** List all NPC gyms */
    @GetMapping
    public String listGyms(Model model) {
        List<Gym> gyms = gymService.getAllNpcGyms();
        model.addAttribute("gyms", gyms);
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
