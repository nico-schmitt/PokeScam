package com.PokeScam.PokeScam.Controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.PokeScam.PokeScam.Services.AdminService;

import jakarta.servlet.http.HttpServletRequest;


@Controller
public class ErrorController {

    public ErrorController() {
    }

    @GetMapping("/error")
    public String admin(Model m, HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String message = (String) request.getAttribute("javax.servlet.error.message");
        Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");

        m.addAttribute("statusCode", statusCode);
        m.addAttribute("message", message);
        m.addAttribute("exception", exception);

        return "error";
    }
}