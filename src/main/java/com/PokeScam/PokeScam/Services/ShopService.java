package com.PokeScam.PokeScam.Services;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.DTOs.PokemonDTO;
import com.PokeScam.PokeScam.Model.Box;
import com.PokeScam.PokeScam.Model.Pokemon;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;

import groovyjarjarantlr4.v4.parse.ANTLRParser.elementOptions_return;
import jakarta.transaction.Transactional;
import lombok.Getter;

@Service
public class ShopService {
    private final PokemonRepository pokemonRepo;
    private final UserRepository userRepo;
    private final BoxService boxService;
    private final PokeAPIService pokeAPIService;
    private final CustomUserDetails userDetails;

    public enum ShopItemType {
        None,
        Energy,
    }

    public record ShopItem(ShopItemType itemType, int cost, int amount) {}

    private ShopItem noneItem;
    private ShopItem energyItem;

    public ShopService(PokemonRepository pokemonRepo, BoxService boxService, PokeAPIService pokeAPIService, CustomUserDetails userDetails, UserRepository userRepo) {
        this.pokemonRepo = pokemonRepo;
        this.boxService = boxService;
        this.pokeAPIService = pokeAPIService;
        this.userDetails = userDetails;
        this.userRepo = userRepo;

        SetupShopItems();
    }

    private void SetupShopItems() {
        noneItem = new ShopItem(ShopItemType.None, 0, 0);
        energyItem = new ShopItem(ShopItemType.Energy, 5, 10);
    }

    public NotificationMsg buy(ShopItemType itemType) {
        NotificationMsg transactionMsg;
        User user = userDetails.getThisUser();
        ShopItem item = getItemInfoFromType(itemType);
        if(userHasEnoughCurrency(user, item.cost())) {
            user.setCurrency(user.getCurrency()-item.cost);
            transactionMsg = new NotificationMsg("Successfully bought "+item.itemType.toString(), true);
            HandleItemPurchase(user, item);
        } else {
            transactionMsg =  new NotificationMsg("Not enough currency", false);
        }

        return transactionMsg;
    }

    private ShopItem getItemInfoFromType(ShopItemType itemType) {
        switch(itemType) {
            case Energy:
                return energyItem;
            default:
                return noneItem;
        }
    }

    private void HandleItemPurchase(User user, ShopItem item) {
        switch(item.itemType) {
            case ShopItemType.None:
                break;
            case ShopItemType.Energy:
                buyEnergyItem(user, item);
                break;
        }
    }

    private boolean userHasEnoughCurrency(User user, int cost) {
        return user.getCurrency() >= cost;
    }

    private void buyEnergyItem(User user, ShopItem item) {
        user.setEnergy(user.getEnergy()+item.amount);
        userRepo.save(user);
    }

    public List<ShopItem> getItems() {
        return List.of(
            energyItem
        );
    }
}