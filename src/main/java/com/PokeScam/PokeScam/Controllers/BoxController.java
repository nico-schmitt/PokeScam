package com.PokeScam.PokeScam.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.PokeScam.PokeScam.Services.BoxService;
import com.PokeScam.PokeScam.Services.PokemonDataService;


@Controller
public class BoxController {
    private final BoxService boxService ;
    private final PokemonDataService pkmnDataService ;

    public BoxController(BoxService boxService, PokemonDataService pkmnDataService) {
        this.boxService = boxService;
        this.pkmnDataService = pkmnDataService;
    }

    @GetMapping("/box")
    public String box(Model m) {
        m.addAttribute("boxes", boxService.getAllBoxes());
        return "boxhub";
    }

    @GetMapping("/box/{boxID}")
    public String box(Model m, @PathVariable int boxID) {
        m.addAttribute("pkmnInBox", pkmnDataService.getPkmnInBox(boxID));
        return "box";
    }

    @PostMapping("/boxes")
    public String addBox() {
        boxService.addBox();
        return "redirect:/box";
    }
}