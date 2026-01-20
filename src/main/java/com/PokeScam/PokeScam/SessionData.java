package com.PokeScam.PokeScam;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import com.PokeScam.PokeScam.Services.EncounterService.EncounterData;

import lombok.Data;

@Component
@SessionScope
@Data
public class SessionData {

    public enum BattleMenuState {
        ACTION_SELECT,
        MOVE_SELECT,
        SWITCH_SELECT,
        ITEM_SELECT
    }

    private int encounterProgress;
    private List<EncounterData> savedEncounterList;
    private Long currentGymId;

    private BattleMenuState battleMenuState = BattleMenuState.ACTION_SELECT;
}
