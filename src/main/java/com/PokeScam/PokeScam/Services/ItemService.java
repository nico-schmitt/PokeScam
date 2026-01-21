package com.PokeScam.PokeScam.Services;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.DTOs.ItemDTO;
import com.PokeScam.PokeScam.DTOs.PotionDTO;
import com.PokeScam.PokeScam.DTOs.ReviveDTO;
import com.PokeScam.PokeScam.Model.Item;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Model.ItemTypes.Potion;
import com.PokeScam.PokeScam.Model.ItemTypes.Revive;
import com.PokeScam.PokeScam.Repos.ItemRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepo;
    private final CustomUserDetails userDetails;
    private final UserRepository userRepo;

    public ItemService(ItemRepository itemRepo, CustomUserDetails userDetails, UserRepository userRepo) {
        this.itemRepo = itemRepo;
        this.userDetails = userDetails;
        this.userRepo = userRepo;
    }

    public Page<Item> getItemsInPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemRepo.findByInventoryId(userDetails.getThisUser().getInventory().getId(), pageable);
    }

    public Item getUserItem(ItemDTO item, User user) {
        ArrayList<Item> items = new ArrayList<>(
                itemRepo.findByInventoryId(userDetails.getThisUser().getInventory().getId()));

        for (Item i : items) {
            if (i.isSameItem(item))
                return i;
        }
        return null;
    }

    public Item findItemById(int id) {
        return itemRepo.findById(id).get();
    }

    public void decrementItem(int itemId) {
        Item item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
        decrementItem(item);
    }

    public void decrementItem(Item item) {
        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() == 0) {
            itemRepo.delete(item);
        } else {
            itemRepo.save(item);
        }
    }

    public List<ItemDTO> getBattleItems() {
        return itemRepo.findAll()
                .stream()
                .map(Item::toDTO)
                .filter(item -> item instanceof PotionDTO ||
                        item instanceof ReviveDTO)
                .filter(item -> item.amount() > 0)
                .toList();
    }

}
