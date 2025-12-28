package com.PokeScam.PokeScam;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelVariables {
    private final CustomUserDetails userDetails;

    public GlobalModelVariables(CustomUserDetails userDetails) {
        this.userDetails = userDetails;
    }

    @ModelAttribute("userCurrency")
    public int addUserCurrency() {
        return userDetails.getThisUser().getCurrency();
    }
}
