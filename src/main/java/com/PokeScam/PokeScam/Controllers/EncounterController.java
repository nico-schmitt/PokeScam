package com.PokeScam.PokeScam.Controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.SessionData;
import com.PokeScam.PokeScam.DTOs.BattleAction;
import com.PokeScam.PokeScam.DTOs.BattleActionDTO;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Services.EncounterService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.PokedexService;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterData;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterDataSinglePkmn;
import com.PokeScam.PokeScam.Services.ItemService;

@Controller
public class EncounterController {

    private final SessionData sessionData;
    private final EncounterService encounterService;
    private final PokemonDataService pokemonDataService;
    private final PokedexService pokedexService;
    private final CustomUserDetails customUserDetails;
    private final ItemService itemService;

    public EncounterController(
            SessionData sessionData,
            EncounterService encounterService,
            PokemonDataService pokemonDataService,
            PokedexService pokedexService,
            CustomUserDetails customUserDetails,
            ItemService itemService) {

        this.sessionData = sessionData;
        this.encounterService = encounterService;
        this.pokemonDataService = pokemonDataService;
        this.pokedexService = pokedexService;
        this.customUserDetails = customUserDetails;
        this.itemService = itemService;
    }

    @GetMapping("/encounterPath")
    public String encounterPath(Model m) {
        m.addAttribute("encounterProgress", sessionData.getEncounterProgress());
        m.addAttribute("encounterList", sessionData.getSavedEncounterList());
        return "encounterPath";
    }

    @PostMapping("/encounterPath/start")
    public String encounterPathStart() {
        encounterService.getRandomEncounters(); // auto sets sessionData
        return "redirect:/encounterPath";
    }

    @PostMapping("/encounterPath/reset")
    public String encounterPathReset() {
        sessionData.setSavedEncounterList(null);
        return "redirect:/encounterPath";
    }

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

    @PostMapping("/encounter/executeTurn")
    public String executeWildTurn(
            @RequestParam BattleAction action,
            @RequestParam(required = false) Integer moveIdx,
            @RequestParam(required = false) Integer switchIdx,
            @RequestHeader(name = "Referer", defaultValue = "/encounter") String referer,
            RedirectAttributes redirectAttributes) {

        BattleActionDTO dto = new BattleActionDTO(action, moveIdx, switchIdx, null);

        NotificationMsg notifMsg = encounterService.executeTurn(dto);
        redirectAttributes.addFlashAttribute("notifMsg", notifMsg);
        return "redirect:" + referer;
    }

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

        // Handle MENU actions: just set flags to show submenus
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
                NotificationMsg notifMsg = encounterService.executeTurn(dto);
                redirectAttributes.addFlashAttribute("notifMsg", notifMsg);
                // After executing the turn, redirect to the same encounter page
                return "redirect:/encounterPath/" + encounterIdx;
            }
        }

        // Reload encounter data to render submenus
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
