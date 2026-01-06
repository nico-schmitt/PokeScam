package com.PokeScam.PokeScam.Services;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.Model.Item;
import com.PokeScam.PokeScam.Repos.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
    private final ItemRepository itemRepo;
    private final CustomUserDetails userDetails;

    public ItemService(ItemRepository itemRepo, CustomUserDetails userDetails) {
        this.itemRepo = itemRepo;
        this.userDetails = userDetails;
    }

    public Page<Item> getItemsInPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemRepo.findByInventoryId(userDetails.getThisUser().getInventory().getId(), pageable);
    }
}
