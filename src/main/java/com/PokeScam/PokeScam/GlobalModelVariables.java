package com.PokeScam.PokeScam;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalModelVariables {
    @Value("${heal_cost}")
    private int healCost;
    @Value("${heal_amount}")
    private int healAmount;

    private final CustomUserDetails userDetails;

    public GlobalModelVariables(CustomUserDetails userDetails) {
        this.userDetails = userDetails;
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("userCurrency")
    public int addUserCurrency() {
        return userDetails.getThisUser().getCurrency();
    }

    @ModelAttribute("userEnergy")
    public int addUserEnergy() {
        return userDetails.getThisUser().getEnergy();
    }

    @ModelAttribute("healPkmnForCurrencyHealCost")
    public int healPkmnCost() {
        return healCost;
    }

    @ModelAttribute("healPkmnForCurrencyHealAmount")
    public int healPkmnAmount() {
        return healAmount;
    }
}
