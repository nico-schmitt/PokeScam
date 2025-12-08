package com.PokeScam.PokeScam.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Services.RegisterUserService;


@Controller
public class RegisterController {

    private final RegisterUserService registerUserService;

    public RegisterController(RegisterUserService registerUserService) {
        this.registerUserService = registerUserService;
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