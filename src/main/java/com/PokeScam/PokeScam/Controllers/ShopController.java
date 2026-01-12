package com.PokeScam.PokeScam.Controllers;

import com.PokeScam.PokeScam.DTOs.ItemDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.Services.BoxService;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.ShopService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class ShopController {
    private final BoxService boxService ;
    private final PokemonDataService pkmnDataService;
    private final PokeAPIService pokeAPIService;
    private final ShopService shopService;

    public ShopController(BoxService boxService, PokemonDataService pkmnDataService, PokeAPIService pokeAPIService, ShopService shopService) {
        this.boxService = boxService;
        this.pkmnDataService = pkmnDataService;
        this.pokeAPIService = pokeAPIService;
        this.shopService = shopService;
    }

    @GetMapping("/shop")
    public String shop(Model m) {
        m.addAttribute("shopItems", shopService.getItems());
        return "shop";
    }

    @PostMapping("/buy/{itemId}")
    public String buyEnergy(@PathVariable int itemId, RedirectAttributes redirectAttributes) {
        NotificationMsg notifMsg = shopService.buy(itemId);
        redirectAttributes.addFlashAttribute("notifMsg", notifMsg);
        return "redirect:/shop";
    }
}