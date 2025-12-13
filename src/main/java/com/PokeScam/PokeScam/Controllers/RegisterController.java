package com.PokeScam.PokeScam.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Services.RegisterUserService;
import com.PokeScam.PokeScam.Services.VerifyUserService;


@Controller
public class RegisterController {

    private final RegisterUserService registerUserService;
    private final VerifyUserService verifyUserService;

    public RegisterController(RegisterUserService registerUserService, VerifyUserService verifyUserService) {
        this.registerUserService = registerUserService;
        this.verifyUserService = verifyUserService;
    }

    @GetMapping("/register")
    public String registerForm(Model m) {
        m.addAttribute("user", new User());
        return "registerForm";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        registerUserService.registerUser(user.getUsername(), user.getEmail(), user.getPassword());
        return "redirect:/login";
    }

    @GetMapping("/verify")
    public String verifyUser(Model m, @RequestParam String token) {
        String verificationStatusMsg = verifyUserService.handleVerification(token);
        m.addAttribute("verificationStatusMsg", verificationStatusMsg);
        return "verification";
    }
}