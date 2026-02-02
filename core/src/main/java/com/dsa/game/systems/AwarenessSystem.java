package com.dsa.game.systems;

import com.dsa.game.state.GameState;

public class AwarenessSystem {

    private final GameState state;
    private int lastThresholdCrossed = 0;

    // Thresholds for warnings
    private static final int[] THRESHOLDS = {20, 40, 60, 70, 80};
    private static final String[] THRESHOLD_WARNINGS = {
        "You feel eyes watching you. The household is becoming aware of your investigation.",
        "Whispered conversations stop when you enter rooms. They know you're looking.",
        "Doors that were open before are now locked. Someone is actively hiding evidence.",
        "You hear footsteps following you. Time is running out.",
        "The household has closed ranks. Your investigation is over."
    };

    public AwarenessSystem(GameState state) {
        this.state = state;
    }

    /**
     * Add awareness and return a warning message if a threshold was crossed, or null.
     */
    public String addAwareness(int amount) {
        int before = state.getAwareness();
        state.addAwareness(amount);
        int after = state.getAwareness();

        // Check if we crossed a threshold
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (before < THRESHOLDS[i] && after >= THRESHOLDS[i]) {
                lastThresholdCrossed = THRESHOLDS[i];
                if (after >= GameState.MAX_AWARENESS) {
                    state.setGameOver(true);
                }
                return THRESHOLD_WARNINGS[i];
            }
        }
        return null;
    }

    public String getLevelName() {
        int a = state.getAwareness();
        if (a < 20) return "DORMANT";
        if (a < 40) return "SUSPICIOUS";
        if (a < 60) return "ALERT";
        if (a < 70) return "DANGEROUS";
        return "CRITICAL";
    }

    public float getPercentage() {
        return (float) state.getAwareness() / GameState.MAX_AWARENESS;
    }

    /**
     * Returns color index: 0=green, 1=yellow, 2=orange, 3=red
     */
    public int getColorIndex() {
        float pct = getPercentage();
        if (pct < 0.25f) return 0;
        if (pct < 0.50f) return 1;
        if (pct < 0.75f) return 2;
        return 3;
    }
}
