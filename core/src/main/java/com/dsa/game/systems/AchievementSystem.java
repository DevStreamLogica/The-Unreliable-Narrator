package com.dsa.game.systems;

import com.dsa.game.state.*;

import java.util.*;

/**
 * Achievement system that checks unlock conditions on game win.
 */
public class AchievementSystem {

    private final GameState state;
    private final EvidenceSystem evidenceSystem;
    private final Set<Achievement> unlocked = new LinkedHashSet<>();

    public AchievementSystem(GameState state, EvidenceSystem evidenceSystem) {
        this.state = state;
        this.evidenceSystem = evidenceSystem;
    }

    /**
     * Check all achievement conditions on win.
     * Returns the list of newly unlocked achievements.
     */
    public List<Achievement> checkOnWin() {
        List<Achievement> newlyUnlocked = new ArrayList<>();

        // SPEEDRUN: Complete in <=10 commands
        if (state.getCommandCount() <= 10 && !unlocked.contains(Achievement.SPEEDRUN)) {
            unlocked.add(Achievement.SPEEDRUN);
            newlyUnlocked.add(Achievement.SPEEDRUN);
        }

        // GHOST: Finish with <25 awareness
        if (state.getAwareness() < 25 && !unlocked.contains(Achievement.GHOST)) {
            unlocked.add(Achievement.GHOST);
            newlyUnlocked.add(Achievement.GHOST);
        }

        // COMPLETIONIST: All evidence + all tapes watched
        boolean allEvidence = state.getCollectedEvidence().size() == Evidence.values().length;
        boolean allTapesWatched = state.getWatchedTapes().size() == Tape.values().length;
        if (allEvidence && allTapesWatched && !unlocked.contains(Achievement.COMPLETIONIST)) {
            unlocked.add(Achievement.COMPLETIONIST);
            newlyUnlocked.add(Achievement.COMPLETIONIST);
        }

        // PERFECT_INVESTIGATION: All evidence + all tapes + <30 awareness
        if (allEvidence && allTapesWatched && state.getAwareness() < 30
                && !unlocked.contains(Achievement.PERFECT_INVESTIGATION)) {
            unlocked.add(Achievement.PERFECT_INVESTIGATION);
            newlyUnlocked.add(Achievement.PERFECT_INVESTIGATION);
        }

        return newlyUnlocked;
    }

    /** Get formatted text of all achievements with unlock status. */
    public String getAchievementsText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ACHIEVEMENTS ===\n\n");

        for (Achievement a : Achievement.values()) {
            boolean isUnlocked = unlocked.contains(a);
            sb.append(isUnlocked ? "[X] " : "[ ] ");
            sb.append(a.getDisplayName()).append("\n");
            sb.append("    ").append(a.getDescription()).append("\n\n");
        }

        return sb.toString();
    }

    public Set<Achievement> getUnlocked() {
        return Collections.unmodifiableSet(unlocked);
    }
}
