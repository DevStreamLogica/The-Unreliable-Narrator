package com.dsa.game.state;

public enum Achievement {
    SPEEDRUN("Speedrun", "Complete the case in 10 or fewer commands"),
    GHOST("Ghost", "Solve the case with less than 25 awareness"),
    COMPLETIONIST("Completionist", "Collect all evidence and watch all tapes"),
    PERFECT_INVESTIGATION("Perfect Investigation", "Collect all evidence, watch all tapes, and finish with less than 30 awareness"),
    GUARDIAN("Guardian", "Seal the wall, trapping the Entity and keeping the secret"),
    ARSONIST("Arsonist", "Destroy all the tapes to break the Entity's connection to the world"),
    SURVIVOR("Survivor", "Escape Vance Manor with your life and your sanity"),
    CYCLE_BREAKER("Cycle Breaker", "Discover all 7 anomalies and choose an ending");

    private final String displayName;
    private final String description;

    Achievement(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
