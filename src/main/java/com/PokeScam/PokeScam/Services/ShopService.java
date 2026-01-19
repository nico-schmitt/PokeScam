package com.PokeScam.PokeScam.Services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.PokeScam.PokeScam.DTOs.*;
import com.PokeScam.PokeScam.Model.Item;
import com.PokeScam.PokeScam.Model.ItemTypes.Ball;
import com.PokeScam.PokeScam.Model.ItemTypes.Potion;
import com.PokeScam.PokeScam.Model.ItemTypes.Revive;
import com.PokeScam.PokeScam.Repos.ItemRepository;
import org.springframework.stereotype.Service;

import com.PokeScam.PokeScam.CustomUserDetails;
import com.PokeScam.PokeScam.NotificationMsg;
import com.PokeScam.PokeScam.Model.User;
import com.PokeScam.PokeScam.Repos.PokemonRepository;
import com.PokeScam.PokeScam.Repos.UserRepository;

@Service
public class ShopService {
    private final PokemonRepository pokemonRepo;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final BoxService boxService;
    private final ItemService itemService;
    private final PokeAPIService pokeAPIService;
    private final CustomUserDetails userDetails;

    private ArrayList<ItemDTO> shopItems = new ArrayList<>();

    public ShopService(PokemonRepository pokemonRepo, BoxService boxService, ItemService itemService, PokeAPIService pokeAPIService, CustomUserDetails userDetails, UserRepository userRepo, ItemRepository itemRepo) {
        this.pokemonRepo = pokemonRepo;
        this.boxService = boxService;
        this.itemService = itemService;
        this.pokeAPIService = pokeAPIService;
        this.userDetails = userDetails;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;

        SetupShopItems();
    }

    private void SetupShopItems() {
        shopItems.add(new PotionDTO(1, "Potion",
            "Basic Potion that restores 20HP", 0,
            100, 50, true, 20));
        shopItems.add(new BallDTO(2, "Pokeball",
            "Can be used to catch wild pokemon", 0,
            100, 25, true, 0.5f));
        shopItems.add(new ReviveDTO(3, "Revive",
            "Can instantly revive fainted pokemon", 0,
            100, 200, true, 0.25f));
        shopItems.add(new EnergyDTO(4, "Energy",
            "Restores energy", 0,
            0, 25, true, 1));
    }

    public NotificationMsg buy(int itemId, int amount) {
        NotificationMsg transactionMsg;
        User user = userDetails.getThisUser();

        for (ItemDTO item : shopItems) {
            if (item.id() == itemId) {
                if(user.getCurrency() >= (item.price() * amount)) {
                    Item userItem = itemService.getUserItem(item, user);

                    if (userItem == null) {
                        // add item to user inventory
                        user.setCurrency(user.getCurrency() - (item.price()*amount));
                        HandleNewItemPurchase(user, item, amount);
                        return new NotificationMsg("Successfully bought "+item.name()+"x"+String.valueOf(amount), true);
                    } else {
                        // add amount to item in inventory if possible
                        if (userItem.getAmount() + amount > userItem.getMaxStackSize())
                            return new NotificationMsg("The maximum amount of "+item.name()+" you can own is "+String.valueOf(userItem.getMaxStackSize()), false);
                        userItem.setAmount(userItem.getAmount() + amount);
                        itemRepo.save(userItem);
                        return new NotificationMsg("Successfully bought "+item.name()+"x"+String.valueOf(amount), true);
                    }
                } else {
                    return new NotificationMsg("Not enough currency", false);
                }
            }
        }
        return new NotificationMsg("Error: unknown item", false);
    }

    private void HandleNewItemPurchase(User user, ItemDTO item, int amount) {
        if (item instanceof EnergyDTO) {
            user.setEnergy(user.getEnergy()+((EnergyDTO) item).energyAmount());
            userRepo.save(user);
        } else {
            Item purchasedItem;

            if (item instanceof BallDTO) {
                purchasedItem = new Ball(user.getInventory(), item.name(), item.description(), amount,
                        item.maxStackSize(), item.consumable(), ((BallDTO) item).baseCaptureChance());
            } else if (item instanceof PotionDTO) {
                purchasedItem = new Potion(user.getInventory(), item.name(), item.description(), amount,
                        item.maxStackSize(), item.consumable(), ((PotionDTO) item).healAmountPotion());
            } else if (item instanceof ReviveDTO) {
                purchasedItem = new Revive(user.getInventory(), item.name(), item.description(), amount,
                        item.maxStackSize(), item.consumable(), ((ReviveDTO) item).healPercentageRevive());
            } else {
                // TODO: throw some kind of error
                return;
            }

            itemRepo.save(purchasedItem);
        }
    }

    public Collection<ItemDTO> getItems() {return shopItems;}
}