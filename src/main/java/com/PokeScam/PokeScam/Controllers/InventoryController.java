package com.PokeScam.PokeScam.Controllers;

import com.PokeScam.PokeScam.DTOs.ItemDTO;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Item;
import com.PokeScam.PokeScam.Model.ItemTypes.Potion;
import com.PokeScam.PokeScam.Model.ItemTypes.Revive;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Repos.ItemRepository;
import com.PokeScam.PokeScam.Services.ItemService;
import com.PokeScam.PokeScam.Services.PokeAPIService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class InventoryController {
    private final ItemService itemService;
    private final PokemonDataService pokemonDataService;
    private final PokeAPIService pokeAPIService;
    private final ItemRepository itemRepository;

    public InventoryController(ItemService itemService, PokemonDataService pokemonDataService, PokeAPIService pokeAPIService, ItemRepository itemRepository) {
        this.itemService = itemService;
        this.pokemonDataService = pokemonDataService;
        this.pokeAPIService = pokeAPIService;
        this.itemRepository = itemRepository;
    }

    @GetMapping("/inventory")
    public String inventory(Model m,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        Page<Item> itemPage = itemService.getItemsInPage(page, size);
        List<ItemDTO> items = itemPage.map(Item::toDTO).toList();
        m.addAttribute("items", items);
        m.addAttribute("pageInfo", itemPage);
        m.addAttribute("pageSize", size);
        return "inventory";
    }

    @GetMapping("/inventory/item/{id}/use")
    public String useItem(@PathVariable int id,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        Item item = itemService.findItemById(id);
        model.addAttribute("item", item);

        Page<Pokemon> pokemonPage;

        if (item instanceof Potion) {
            pokemonPage = pokemonDataService.getDamagedPokemonsInPage(page, size);
        } else if (item instanceof Revive) {
            pokemonPage = pokemonDataService.getFaintedPokemonsInPage(page, size);
        } else {
            return "inventory";
        }

        List<PokemonDTO> pokemonDTOs = pokemonPage.map(pokeAPIService::populatePokemonDTO).toList();
        model.addAttribute("pokemons", pokemonDTOs);

        return "itemUse";
    }

    @PostMapping("/inventory/item/{itemId}/use/{pkmnId}")
    public String useItem(@PathVariable int itemId, @PathVariable int pkmnId) {
        Item item = itemService.findItemById(itemId);
        Pokemon pokemon = pokemonDataService.getPkmnById(pkmnId).get();
        int maxHp = pokemon.getMaxHp();

        if (item instanceof Potion) {
            pokemonDataService.healPkmnForCost(pkmnId, 0, ((Potion) item).getHealAmountPotion());
        } else if (item instanceof Revive) {
            pokemonDataService.healPkmnForCost(pkmnId, 0, (int) (maxHp * ((Revive) item).getHealPercentageRevive()));
        } else {
            return "inventory";
        }

        itemService.decrementItem(item);

        return "inventory";
    }
}
