package com.PokeScam.PokeScam.Controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.SessionData;
import com.PokeScam.PokeScam.DTOs.BattleAction;
import com.PokeScam.PokeScam.DTOs.BattleActionDTO;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Services.EncounterService;
import com.PokeScam.PokeScam.Services.GymService;
import com.PokeScam.PokeScam.Services.PokedexService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.GymProgressService;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterData;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterDataSinglePkmn;

@Controller
@RequestMapping("/gymBattle")
public class GymBattleController {

    private final SessionData sessionData;
    private final GymService gymService;
    private final EncounterService encounterService;
    private final PokemonDataService pokemonDataService;
    private final PokedexService pokedexService;
    private final CustomUserDetails customUserDetails;
    private final GymProgressService gymProgressService;

    public GymBattleController(
            SessionData sessionData,
            GymService gymService,
            EncounterService encounterService,
            PokemonDataService pokemonDataService,
            PokedexService pokedexService,
            CustomUserDetails customUserDetails,
            GymProgressService gymProgressService) {

        this.sessionData = sessionData;
        this.gymService = gymService;
        this.encounterService = encounterService;
        this.pokemonDataService = pokemonDataService;
        this.pokedexService = pokedexService;
        this.customUserDetails = customUserDetails;
        this.gymProgressService = gymProgressService;
    }

    /** Start a gym battle */
    @PostMapping("/start/{gymId}")
    public String startGymBattle(@PathVariable Long gymId) {
        List<EncounterData> gymEncounters = gymService.getGymEncounters(gymId);
        sessionData.setSavedEncounterList(gymEncounters);
        sessionData.setEncounterProgress(0);
        sessionData.setCurrentGymId(gymId);
        return "redirect:/gymBattle/battle/0";
    }

    /** Show current gym encounter */
    @GetMapping("/battle/{encounterIdx}")
    public String showGymBattle(@PathVariable int encounterIdx, Model m) {
        EncounterData encounterData = encounterService.getEncounterDataAtIdx(encounterIdx);
        List<EncounterDataSinglePkmn> pkmnTeam = encounterService.getPkmnTeamInfo();
        List<EncounterDataSinglePkmn> encounterList = encounterService.getPokemonToFightListAtIdx(encounterIdx);
        EncounterDataSinglePkmn enemyActivePkmn = encounterService.getEnemyActivePkmnAtIdx(encounterIdx);
        EncounterDataSinglePkmn activePkmn = encounterService.wrapPkmnInEncounterData(
                pokemonDataService.getActivePkmnDTO());

        // Mark enemy Pokémon as seen
        User user = customUserDetails.getThisUser();
        pokedexService.markSeen(user, enemyActivePkmn.pkmn().apiName());

        m.addAttribute("encounterData", encounterData);
        m.addAttribute("pkmnTeam", pkmnTeam);
        m.addAttribute("encounterList", encounterList);
        m.addAttribute("activePkmn", activePkmn);
        m.addAttribute("enemyPkmn", enemyActivePkmn);

        return "encounterBattle";
    }

    /** Execute a turn in a gym battle */
    @PostMapping("/executeTurn")
    public String executeGymTurn(
            @RequestParam String action,
            @RequestParam(required = false) Integer moveIdx,
            @RequestParam(required = false) Integer switchIdx,
            @RequestParam(required = false) Integer itemIdx,
            RedirectAttributes redirectAttributes) {

        // Get current encounter index from session
        int encounterIdx = sessionData.getEncounterProgress();

        BattleAction enumAction;
        try {
            enumAction = BattleAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            enumAction = null;
        }

        BattleActionDTO dto = new BattleActionDTO(enumAction, moveIdx, switchIdx, itemIdx);
        EncounterService.EncounterResult result = encounterService.executeTurn(dto);

        redirectAttributes.addFlashAttribute("notifMsg", result.notification());
        redirectAttributes.addFlashAttribute("encounterFinished", result.encounterFinished());
        redirectAttributes.addFlashAttribute("hasNextEncounter", result.hasNextEncounter());

        if (result.encounterFinished()) {
            if (result.hasNextEncounter()) {
                // Go to next gym encounter
                int nextIdx = encounterIdx + 1;
                sessionData.setEncounterProgress(nextIdx);
                return "redirect:/gymBattle/battle/" + nextIdx;
            } else {
                // Last encounter finished → mark gym complete
                User user = customUserDetails.getThisUser();
                Gym gym = gymService.getGymById(sessionData.getCurrentGymId())
                        .orElseThrow(() -> new IllegalArgumentException("Gym not found"));
                gymProgressService.markGymCompleted(user, gym);

                // Reset progress
                sessionData.setEncounterProgress(0);
                sessionData.setSavedEncounterList(null);
                sessionData.setCurrentGymId(null);

                return "redirect:/gym"; // back to gym selection page
            }
        } else {
            // Battle not finished → stay on same encounter
            return "redirect:/gymBattle/battle/" + encounterIdx;
        }
    }
}
