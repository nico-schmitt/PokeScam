package com.PokeScam.PokeScam.Controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.SessionData;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Model.Gym;
import com.PokeScam.PokeScam.Services.EncounterService;
import com.PokeScam.PokeScam.Services.GymService;
import com.PokeScam.PokeScam.Services.PokedexService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterData;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterDataSinglePkmn;
import com.PokeScam.PokeScam.Services.GymProgressService;

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

    /** Gym selection page (start battle) */
    @PostMapping("/start/{gymId}")
    public String startGymBattle(@PathVariable Long gymId) {
        List<EncounterData> gymEncounters = gymService.getGymEncounters(gymId);
        sessionData.setSavedEncounterList(gymEncounters);
        sessionData.setEncounterProgress(0);
        sessionData.setCurrentGymId(gymId); // new field in SessionData
        return "redirect:/gymBattle/battle/0";
    }

    /** Show current gym battle */
    @GetMapping("/battle/{encounterIdx}")
    public String gymBattle(@PathVariable int encounterIdx, Model m) {
        EncounterData encounterData = encounterService.getEncounterDataAtIdx(encounterIdx);
        List<EncounterDataSinglePkmn> pkmnTeamInfo = encounterService.getPkmnTeamInfo();
        List<EncounterDataSinglePkmn> encounterList = encounterService.getPokemonToFightListAtIdx(encounterIdx);
        EncounterDataSinglePkmn enemyActivePkmn = encounterService.getEnemyActivePkmnAtIdx(encounterIdx);
        EncounterDataSinglePkmn activePkmn = encounterService.wrapPkmnInEncounterData(
                pokemonDataService.getActivePkmnDTO());

        // Mark enemy Pokémon as seen
        User user = customUserDetails.getThisUser();
        pokedexService.markSeen(user, enemyActivePkmn.pkmn().apiName());

        m.addAttribute("encounterData", encounterData);
        m.addAttribute("pkmnTeam", pkmnTeamInfo);
        m.addAttribute("encounterList", encounterList);
        m.addAttribute("activePkmn", activePkmn);
        m.addAttribute("enemyPkmn", enemyActivePkmn);

        // Check if last encounter of gym
        if (encounterIdx == sessionData.getSavedEncounterList().size() - 1
                && encounterData.encounterWon()) {
            Gym gym = gymService.getGymById(sessionData.getCurrentGymId())
                    .orElseThrow(() -> new IllegalArgumentException("Gym not found"));
            gymProgressService.markGymCompleted(user, gym);
            m.addAttribute("gymCompleted", true);
        }

        return "encounterBattle";
    }

    /** Execute a turn in gym battle */
    @PostMapping("/executeTurn")
    public String executeTurn(
            @RequestParam int moveIdx,
            @RequestHeader(name = "Referer", defaultValue = "/") String referer,
            RedirectAttributes redirectAttributes) {
        NotificationMsg notifMsg = encounterService.executeTurn(moveIdx);
        redirectAttributes.addFlashAttribute("notifMsg", notifMsg);
        return "redirect:" + referer;
    }

}
