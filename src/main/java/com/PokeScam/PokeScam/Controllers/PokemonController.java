package com.PokeScam.PokeScam.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.beans.factory.annotation.Value;

import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.BoxService;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;

import lombok.Data;


@Controller
@RequestMapping("/pokemon")
public class PokemonController {
    @Value("${heal_cost}")
    private int healCost;
    @Value("${heal_amount}")
    private int healAmount;

    private final BoxService boxService;
    private final PokemonDataService pkmnDataService;
    private final PokeAPIService pokeAPIService;

    public PokemonController(PokemonDataService pkmnData, PokeAPIService pokeAPIService, BoxService boxService) {
        this.pkmnDataService = pkmnData;
        this.pokeAPIService = pokeAPIService;
        this.boxService = boxService;
    }

    @PostMapping("/addPokemon")
    public String addPokemon(@ModelAttribute Pokemon pokemonToAdd, RedirectAttributes redirectAttributes) {
        String addMsg = pkmnDataService.addPokemonFromName(pokemonToAdd.getName());
        redirectAttributes.addFlashAttribute("addMsg", addMsg);
        return "redirect:/";
    }

    @DeleteMapping("/{id}")
    public String releasePokemon(@PathVariable int id, @RequestHeader(name="Referer", defaultValue = "/") String referer) {
        pkmnDataService.deletePkmn(id);
        return "redirect:" + referer;
    }

    @PostMapping("/swap/{swapId}")
    public String swapSite(Model m, @PathVariable int swapId) {
        m.addAttribute("swapPkmn", pkmnDataService.getPkmnInfo(swapId));
        m.addAttribute("pkmnTeam", pkmnDataService.getPkmnTeamInfo());
        return "swapPkmn";
    }

    @PostMapping("/swap")
    public String swapPkmn(Model m, @ModelAttribute SwapRequest swapRequest) {
        boxService.swapTeamPkmnToBox(swapRequest.teamPkmnToSwap, swapRequest.otherPkmnToSwap);
        return "redirect:/";
    }

    @PostMapping("/healcost/{id}")
    public String healPkmnById(@PathVariable int id, @RequestHeader(name="Referer", defaultValue = "/") String referer, RedirectAttributes redirectAttributes) {
        NotificationMsg notifMsg = pkmnDataService.healPkmnForCost(id, healCost, healAmount);
        redirectAttributes.addFlashAttribute("notifMsg", notifMsg);
        return "redirect:" + referer;
    }
    
    
    @Data
    public static class SwapRequest {
        int teamPkmnToSwap;
        int otherPkmnToSwap;
    }
}