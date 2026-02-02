package com.dsa.game.state;

/**
 * Achievement definitions for DSA 2D.
 */
public enum Achievement {
    SPEEDRUN("Speedrun", "Complete the case in 10 or fewer commands"),
    GHOST("Ghost", "Solve the case with less than 25 awareness"),
    COMPLETIONIST("Completionist", "Collect all evidence and watch all tapes"),
    PERFECT_INVESTIGATION("Perfect Investigation", "Collect all evidence, watch all tapes, and finish with less than 30 awareness");

    private final String displayName;
    private final String description;

    Achievement(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
