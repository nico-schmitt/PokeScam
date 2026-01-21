package com.PokeScam.PokeScam.Repos;

import com.PokeScam.PokeScam.Model.TradeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<TradeRequest, Integer> {
}
