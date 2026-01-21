package com.PokeScam.PokeScam.Controllers;

import java.util.List;

import com.PokeScam.PokeScam.Services.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.SessionData;
import com.PokeScam.PokeScam.DTOs.BattleAction;
import com.PokeScam.PokeScam.DTOs.BattleActionDTO;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterData;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterDataSinglePkmn;

@Controller
public class EncounterController {

    private final SessionData sessionData;
    private final EncounterService encounterService;
    private final PokemonDataService pokemonDataService;
    private final PokedexService pokedexService;
    private final CustomUserDetails customUserDetails;
    private final ItemService itemService;
    private final UserService userService;

    public EncounterController(
            SessionData sessionData,
            EncounterService encounterService,
            PokemonDataService pokemonDataService,
            PokedexService pokedexService,
            CustomUserDetails customUserDetails,
            ItemService itemService,
            UserService userService) {

        this.sessionData = sessionData;
        this.encounterService = encounterService;
        this.pokemonDataService = pokemonDataService;
        this.pokedexService = pokedexService;
        this.customUserDetails = customUserDetails;
        this.itemService = itemService;
        this.userService = userService;
    }

    /** Encounter path main page */
    @GetMapping("/encounterPath")
    public String encounterPath(Model m) {
        m.addAttribute("encounterProgress", sessionData.getEncounterProgress());
        m.addAttribute("encounterList", sessionData.getSavedEncounterList());
        userService.updateRecentActivity(customUserDetails.getThisUser(), "Running through the wilderness");
        return "encounterPath";
    }

    /** Start a random encounter path */
    @PostMapping("/encounterPath/start")
    public String encounterPathStart() {
        encounterService.getRandomEncounters(); // populates sessionData
        return "redirect:/encounterPath";
    }

    /** Reset the encounter path */
    @PostMapping("/encounterPath/reset")
    public String encounterPathReset() {
        sessionData.setSavedEncounterList(null);
        sessionData.setEncounterProgress(0);
        return "redirect:/encounterPath";
    }

    /** Show a specific encounter battle */
    @GetMapping("/encounterPath/{encounterIdx}")
    public String encounterPathBattle(Model m, @PathVariable int encounterIdx) {
        EncounterData encounterData;
        try {
            encounterData = encounterService.getEncounterDataAtIdx(encounterIdx);
        } catch (IllegalStateException e) {
            return "redirect:/encounterPath"; // fallback if no Pokémon
        }

        List<EncounterDataSinglePkmn> pkmnTeamInfo = encounterService.getPkmnTeamInfo();
        List<EncounterDataSinglePkmn> encounterList = encounterService.getPokemonToFightListAtIdx(encounterIdx);
        EncounterDataSinglePkmn enemyActivePkmn = encounterService.getEnemyActivePkmnAtIdx(encounterIdx);
        EncounterDataSinglePkmn activePkmn = encounterService.wrapPkmnInEncounterData(
                pokemonDataService.getActivePkmnDTO());

        User user = customUserDetails.getThisUser();
        pokedexService.markSeen(user, enemyActivePkmn.pkmn().apiName());

        m.addAttribute("encounterData", encounterData);
        m.addAttribute("pkmnTeam", pkmnTeamInfo);
        m.addAttribute("encounterList", encounterList);
        m.addAttribute("activePkmn", activePkmn);
        m.addAttribute("enemyPkmn", enemyActivePkmn);

        return "encounterBattle";
    }

    /** Execute a battle turn for wild encounters or generic encounters */
    @PostMapping("/encounter/executeTurn")
    public String executeTurn(
            @RequestParam BattleAction action,
            @RequestParam(required = false) Integer moveIdx,
            @RequestParam(required = false) Integer switchIdx,
            @RequestParam(required = false) Integer itemIdx,
            @RequestHeader(name = "Referer", defaultValue = "/encounterPath") String referer,
            RedirectAttributes redirectAttributes) {

        // Determine current encounter index from session data
        int encounterIdx = sessionData.getEncounterProgress();

        BattleActionDTO dto = new BattleActionDTO(action, moveIdx, switchIdx, itemIdx);
        EncounterService.EncounterResult result = encounterService.executeTurn(dto);

        redirectAttributes.addFlashAttribute("notifMsg", result.notification());
        redirectAttributes.addFlashAttribute("encounterFinished", result.encounterFinished());
        redirectAttributes.addFlashAttribute("hasNextEncounter", result.hasNextEncounter());

        if (result.encounterFinished()) {
            // Increment progress if a next encounter exists
            if (result.hasNextEncounter()) {
                int nextEncounterIdx = encounterIdx + 1;
                sessionData.setEncounterProgress(nextEncounterIdx);
                return "redirect:/encounterPath/" + nextEncounterIdx;
            } else {
                // Last encounter finished → return to path
                sessionData.setEncounterProgress(0); // reset progress
                return "redirect:/encounterPath";
            }
        } else {
            // Battle not finished → stay on same page
            return "redirect:/encounterPath/" + encounterIdx;
        }
    }

    /** Execute a battle action from UI menus */
    @PostMapping("/encounter/executeBattleAction")
    public String executeBattleAction(
            @RequestParam String action,
            @RequestParam(required = false) Integer moveIdx,
            @RequestParam(required = false) Integer switchIdx,
            @RequestParam(required = false) Integer itemIdx,
            @RequestParam int encounterIdx,
            RedirectAttributes redirectAttributes,
            Model m) {

        // Convert string to enum (uppercase), null for menu actions
        BattleAction enumAction;
        try {
            enumAction = BattleAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            enumAction = null;
        }

        // Handle MENU actions: set flags to show submenus
        switch (action.toUpperCase()) {
            case "FIGHT_MENU" -> m.addAttribute("showMoves", true);
            case "SWITCH_MENU" -> m.addAttribute("showSwitch", true);
            case "ITEM_MENU" -> {
                m.addAttribute("showItemMenu", true);
                m.addAttribute("items", itemService.getBattleItems());
            }
            default -> {
                // Execute actual battle action
                BattleActionDTO dto = new BattleActionDTO(enumAction, moveIdx, switchIdx, itemIdx);
                EncounterService.EncounterResult result = encounterService.executeTurn(dto);

                redirectAttributes.addFlashAttribute("notifMsg", result.notification());
                redirectAttributes.addFlashAttribute("encounterFinished", result.encounterFinished());
                redirectAttributes.addFlashAttribute("hasNextEncounter", result.hasNextEncounter());

                if (result.encounterFinished()) {
                    if (result.hasNextEncounter()) {
                        int nextEncounterIdx = encounterIdx + 1;
                        sessionData.setEncounterProgress(nextEncounterIdx);
                        return "redirect:/encounterPath/" + nextEncounterIdx;
                    } else {
                        sessionData.setEncounterProgress(0);
                        return "redirect:/encounterPath";
                    }
                } else {
                    return "redirect:/encounterPath/" + encounterIdx;
                }
            }
        }

        // Reload encounter data to render menus
        EncounterData encounterData = encounterService.getEncounterDataAtIdx(encounterIdx);
        List<EncounterDataSinglePkmn> pkmnTeamInfo = encounterService.getPkmnTeamInfo();
        List<EncounterDataSinglePkmn> encounterList = encounterService.getPokemonToFightListAtIdx(encounterIdx);
        EncounterDataSinglePkmn activePkmn = encounterService.wrapPkmnInEncounterData(
                pokemonDataService.getActivePkmnDTO());

        m.addAttribute("encounterData", encounterData);
        m.addAttribute("pkmnTeam", pkmnTeamInfo);
        m.addAttribute("encounterList", encounterList);
        m.addAttribute("activePkmn", activePkmn);

        // Ensure submenu flags exist
        m.addAttribute("showMoves", m.getAttribute("showMoves") != null && (boolean) m.getAttribute("showMoves"));
        m.addAttribute("showSwitch", m.getAttribute("showSwitch") != null && (boolean) m.getAttribute("showSwitch"));
        m.addAttribute("showItemMenu",
                m.getAttribute("showItemMenu") != null && (boolean) m.getAttribute("showItemMenu"));

        return "encounterBattle";
    }
}
