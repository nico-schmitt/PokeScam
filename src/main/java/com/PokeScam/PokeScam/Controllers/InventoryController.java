package com.PokeScam.PokeScam.Controllers;

import com.PokeScam.PokeScam.Model.Item;
import com.PokeScam.PokeScam.Services.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InventoryController {
    private final ItemService itemService;

    public InventoryController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/inventory")
    public String inventory(Model m,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        Page<Item> itemPage = itemService.getItemsInPage(page, size);
        m.addAttribute("items", itemPage.getContent());
        m.addAttribute("pageInfo", itemPage);
        m.addAttribute("pageSize", size);
        return "inventory";
    }
}
