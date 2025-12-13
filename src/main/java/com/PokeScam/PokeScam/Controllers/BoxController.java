package com.PokeScam.PokeScam.Controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Services.BoxService;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;

import jakarta.validation.constraints.Min;


@Controller
public class BoxController {
    private final BoxService boxService ;
    private final PokemonDataService pkmnDataService;
    private final PokeAPIService pokeAPIService;

    public BoxController(BoxService boxService, PokemonDataService pkmnDataService, PokeAPIService pokeAPIService) {
        this.boxService = boxService;
        this.pkmnDataService = pkmnDataService;
        this.pokeAPIService = pokeAPIService;
    }

    @GetMapping("/box")
    public String box(Model m) {
        m.addAttribute("boxes", boxService.getAllBoxes());
        return "boxhub";
    }

    @GetMapping("/box/{boxId}")
    public String box(
            Model m,
            @PathVariable int boxId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Pokemon> pkmnPage = pkmnDataService.getPkmnInBoxPage(boxId, page, size);
        List<PokemonDTO> pDTOs = pkmnPage.map(p->pokeAPIService.populatePokemonDTO(p)).toList();
        m.addAttribute("pkmnInBox", pDTOs);
        m.addAttribute("pageInfo", pkmnPage);
        m.addAttribute("pageSize", size);
        m.addAttribute("boxId", boxId);
        return "box";
    }

    @PostMapping("/boxes")
    public String addBox() {
        boxService.addBox();
        return "redirect:/box";
    }
}