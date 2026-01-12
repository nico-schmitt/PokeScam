package com.PokeScam.PokeScam.Repos;

import com.PokeScam.PokeScam.Model.Inventory;
import com.PokeScam.PokeScam.Model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    Page<Item> findByInventoryId(Long inventoryId, Pageable pageable);
    Collection<Item> findByInventoryId(Long inventoryId);
}
