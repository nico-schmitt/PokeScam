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
 private int encounterProgress;
 private List<EncounterData> savedEncounterList;
 private int activePkmnIdx;
}
