package com.PokeScam.PokeScam.Controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.PokeScam.PokeScam.SessionData;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Services.EncounterService;
import com.PokeScam.PokeScam.Services.PokemonDataService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



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
        return "redirect:/encounterPath";
    }

    @PostMapping("/encounterPath/reset")
    public String encounterPathReset() {
        sessionData.setSavedEncounterList(null);
        return "redirect:/encounterPath";
    }
    
    @GetMapping("/encounterPath/{encounterIdx}")
    public String encounterPathBattle(Model m, @PathVariable int encounterIdx) {
        List<PokemonDTO> pkmnTeamInfo = pokemonDataService.getPkmnTeamInfo();
        m.addAttribute("pkmnTeam", pkmnTeamInfo);
        m.addAttribute("encounterList", encounterService.getEncounterAtIdx(encounterIdx));
        m.addAttribute("activePkmnIdx", sessionData.getActivePkmnIdx());
        m.addAttribute("activePkmn", pkmnTeamInfo.get(sessionData.getActivePkmnIdx()));
        System.out.println(pkmnTeamInfo.get(sessionData.getActivePkmnIdx()) + "\n\n\n\n\n");
        return "encounterBattle";
    }
}