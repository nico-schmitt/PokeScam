package com.PokeScam.PokeScam.Controllers;

import org.springframework.boot.webflux.autoconfigure.WebFluxProperties.Apiversion.Use;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.RegisterUserService;


@Controller
public class MainController {
    private final PokeAPIService pkmnAPIService;
    private final PokemonDataService pkmnDataService;
    private final RegisterUserService registerUserService;

    public MainController(PokeAPIService pkmn, PokemonDataService pkmnData, RegisterUserService registerUserService) {
        this.pkmnAPIService = pkmn;
        this.pkmnDataService = pkmnData;
        this.registerUserService = registerUserService;
    }


    @GetMapping("/")
    public String home(Model m) {
        m.addAttribute("pokemon", new Pokemon());
        m.addAttribute("pkmnTeam", pkmnDataService.getPkmnTeamInfo());
        return "home";
    }

    @PostMapping("/api/addPokemonToTeam")
    public String addPokemonToTeam(@ModelAttribute Pokemon pokemonToAdd) {
        pkmnDataService.savePkmn(pokemonToAdd);
        System.out.println(pokemonToAdd.getName()+"\n\n\n\n\n\n\n\n");
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerForm(Model m) {
        m.addAttribute("user", new User());
        return "registerForm";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        registerUserService.registerUser(user.getUsername(), user.getPassword());
        System.out.println(user.getUsername()+ "\n\n\n\n\n\n\n\n\n");
        return "redirect:/login";
    }
}