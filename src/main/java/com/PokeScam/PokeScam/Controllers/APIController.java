package com.PokeScam.PokeScam.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.PokemonDataService;


@Controller
public class APIController {

    private final PokemonDataService pkmnDataService;

    public APIController(PokemonDataService pkmnData) {
        this.pkmnDataService = pkmnData;
    }

    @PostMapping("/api/addPokemon")
    public String addPokemon(@ModelAttribute Pokemon pokemonToAdd, RedirectAttributes redirectAttributes) {
        String addMsg = pkmnDataService.addPokemon(pokemonToAdd);
        redirectAttributes.addFlashAttribute("addMsg", addMsg);
        return "redirect:/";
    }
}