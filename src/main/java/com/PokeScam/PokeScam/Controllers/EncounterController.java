package com.PokeScam.PokeScam.Controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.SessionData;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO.PokemonDTO_MoveInfo;
import com.PokeScam.PokeScam.Services.EncounterService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterData;
import com.PokeScam.PokeScam.Services.EncounterService.EncounterDataSinglePkmn;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
public class EncounterController {
    private final SessionData sessionData;

    private final EncounterService encounterService;
    private final PokemonDataService pokemonDataService;

    public EncounterController(SessionData sessionData, EncounterService encounterService, PokemonDataService pokemonDataService) {
        this.sessionData = sessionData;
        this.encounterService = encounterService;
        this.pokemonDataService = pokemonDataService;
    }

    @GetMapping("/encounterPath")
    public String encounterPath(Model m) {
        m.addAttribute("encounterProgress", sessionData.getEncounterProgress());
        m.addAttribute("encounterList", sessionData.getSavedEncounterList());
        return "encounterPath";
    }

    @PostMapping("/encounterPath/start")
    public String encounterPathStart() {
        sessionData.setSavedEncounterList(encounterService.getRandomEncounters());
        sessionData.setEncounterProgress(0);
        return "redirect:/encounterPath";
    }

    @PostMapping("/encounterPath/reset")
    public String encounterPathReset() {
        sessionData.setSavedEncounterList(null);
        return "redirect:/encounterPath";
    }
    
    @GetMapping("/encounterPath/{encounterIdx}")
    public String encounterPathBattle(Model m, @PathVariable int encounterIdx) {
        EncounterData encounterData = encounterService.getEncounterDataAtIdx(encounterIdx);
        List<EncounterDataSinglePkmn> pkmnTeamInfo = encounterService.getPkmnTeamInfo();;
        List<EncounterDataSinglePkmn> encounterList = encounterService.getPokemonToFightListAtIdx(encounterIdx);
        EncounterDataSinglePkmn enemyActivePkmn = encounterService.getEnemyActivePkmnAtIdx(encounterIdx);
        m.addAttribute("encounterData", encounterData);
        m.addAttribute("pkmnTeam", pkmnTeamInfo);
        m.addAttribute("encounterList", encounterList);
        m.addAttribute("activePkmnIdx", sessionData.getActivePkmnIdx());
        m.addAttribute("activePkmn", pkmnTeamInfo.get(sessionData.getActivePkmnIdx()));
        m.addAttribute("enemyPkmn", enemyActivePkmn);
        return "encounterBattle";
    }

    @PostMapping("/encounter/executeTurn")
    public String executeTurn(@RequestParam int moveIdx, @RequestHeader(name="Referer", defaultValue = "/") String referer, RedirectAttributes redirectAttributes) {
        NotificationMsg notifMsg = encounterService.executeTurn(moveIdx);
        redirectAttributes.addFlashAttribute("notifMsg", notifMsg);
        System.out.println(notifMsg+"\n\n\n\n\n\n");
        return "redirect:" + referer;
    }
}