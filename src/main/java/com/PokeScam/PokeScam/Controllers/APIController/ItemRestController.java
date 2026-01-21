package com.PokeScam.PokeScam.Controllers.APIController;

import com.PokeScam.PokeScam.Model.Item;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.ItemRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;
import com.PokeScam.PokeScam.Services.ItemService;
import com.PokeScam.PokeScam.Services.PokemonDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
public class ItemRestController {
    ItemService itemService;
    ItemRepository itemRepo;
    UserRepository userRepo;

    public ItemRestController(ItemService itemService, ItemRepository itemRepo, UserRepository userRepo) {
        this.itemService = itemService;
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemRepo.findAll());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Item>> getItemByOwner(@PathVariable int userId) {
        Optional<User> user = userRepo.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Item> items = itemRepo.findByInventoryId(user.get().getInventory().getId()).stream().toList();
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Item> deleteItemById(@PathVariable int itemId) {
        if (itemRepo.findById(itemId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        itemRepo.deleteById(itemId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<Item> addItemToUser(@PathVariable int userId, @RequestBody Item item) {
        Optional<User> user = userRepo.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (itemRepo.findById(item.getId()).isPresent()) {
            return ResponseEntity.badRequest().body(item);
        }
        item.setInventory(user.get().getInventory());
        itemRepo.save(item);
        return ResponseEntity.ok(item);
    }

    @PatchMapping("/{itemId}/amount/{amount}")
    public ResponseEntity<Item> editItemAmount(@PathVariable int itemId, @PathVariable int amount) {
        Optional<Item> item = itemRepo.findById(itemId);
        if (item.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        item.get().setAmount(amount);
        itemRepo.save(item.get());
        return ResponseEntity.ok(item.get());
    }
}
