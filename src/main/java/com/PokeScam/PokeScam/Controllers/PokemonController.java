package com.PokeScam.PokeScam.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.BoxService;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;

import lombok.Data;


@Controller
@RequestMapping("/pokemon")
public class PokemonController {

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
        String addMsg = pkmnDataService.addPokemon(pokemonToAdd);
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
    
    @Data
    public static class SwapRequest {
        int teamPkmnToSwap;
        int otherPkmnToSwap;
    }
}