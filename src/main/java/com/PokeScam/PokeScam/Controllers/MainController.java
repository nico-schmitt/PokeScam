package com.PokeScam.PokeScam.Controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.PokeScam.PokeScam.Pokemon;
import com.PokeScam.PokeScam.User;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import com.PokeScam.PokeScam.Services.RegisterUserService;
import com.PokeScam.PokeScam.Services.ShowUserService;


@Controller
public class MainController {
    private final ShowUserService u;
    private final PokeAPIService pkmn;
    private final PokemonDataService pkmnData;
    private final PasswordEncoder p;
    private final RegisterUserService registerUserService;

    public MainController(ShowUserService u, PokeAPIService pkmn, PokemonDataService pkmnData, PasswordEncoder p, RegisterUserService registerUserService) {
        this.u = u;
        this.pkmn = pkmn;
        this.pkmnData = pkmnData;
        this.p = p;
        this.registerUserService = registerUserService;
    }


    @GetMapping("/")
    public String home(Model m) {
        m.addAttribute("pokemon", new Pokemon());
        m.addAttribute("p", pkmn.getPokemon("pikachu").block());
        return "home";
    }

    @PostMapping("/api/addPokemonToTeam")
    public String addPokemonToTeam(@ModelAttribute Pokemon pokemonToAdd) {
        System.out.println(pokemonToAdd.getName()+"\n\n\n\n\n\n\n\n");
        return "redirect:/";
    }
    

    @GetMapping("/register")
    public String registerForm(Model m) {
        m.addAttribute("user", new User());
        return "registerForm";
    }

    @PostMapping("/setPkmn")
    public String setPkm(@ModelAttribute Pokemon pkmn) {
        pkmnData.savePkmn();
        System.out.println(pkmn.getName() + "\n\n\n\n\n\n\n\n");
        return "redirect:/";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        registerUserService.registerUser(user.getUsername(), user.getPassword());
        System.out.println(user.getUsername()+ "\n\n\n\n\n\n\n\n\n");
        return "redirect:/login";
    }
}