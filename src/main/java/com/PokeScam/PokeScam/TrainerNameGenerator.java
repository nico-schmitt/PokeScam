package com.PokeScam.PokeScam;

import java.util.List;
import java.util.Random;

public class TrainerNameGenerator {

    private static final List<String> FIRST_NAMES = List.of(
            "Alex", "Jamie", "Chris", "Morgan", "Taylor",
            "Casey", "Jordan", "Riley", "Avery", "Sam");

    private static final List<String> TITLES = List.of(
            "Ace Trainer", "Veteran", "Rookie", "Battler",
            "Strategist", "Tactician", "Challenger");

    private static final Random RANDOM = new Random();

    public static String randomTrainerName() {
        return TITLES.get(RANDOM.nextInt(TITLES.size()))
                + " "
                + FIRST_NAMES.get(RANDOM.nextInt(FIRST_NAMES.size()));
    }
}
