package com.dsa.game.systems;

import com.dsa.game.state.*;

import java.util.*;

public class AchievementSystem {

    private final GameState state;
    private final EvidenceSystem evidenceSystem;
    private final Set<Achievement> unlocked = new LinkedHashSet<>();

    public AchievementSystem(GameState state, EvidenceSystem evidenceSystem) {
        this.state = state;
        this.evidenceSystem = evidenceSystem;
    }

    public List<Achievement> checkOnWin() {
        List<Achievement> newlyUnlocked = new ArrayList<>();

        if (state.getCommandCount() <= 10 && !unlocked.contains(Achievement.SPEEDRUN)) {
            unlocked.add(Achievement.SPEEDRUN);
            newlyUnlocked.add(Achievement.SPEEDRUN);
        }

        if (state.getAwareness() < 25 && !unlocked.contains(Achievement.GHOST)) {
            unlocked.add(Achievement.GHOST);
            newlyUnlocked.add(Achievement.GHOST);
        }

        boolean allEvidence = state.getCollectedEvidence().size() == Evidence.values().length;
        boolean allTapesWatched = state.getWatchedTapes().size() == Tape.values().length;
        if (allEvidence && allTapesWatched && !unlocked.contains(Achievement.COMPLETIONIST)) {
            unlocked.add(Achievement.COMPLETIONIST);
            newlyUnlocked.add(Achievement.COMPLETIONIST);
        }

        if (allEvidence && allTapesWatched && state.getAwareness() < 30
                && !unlocked.contains(Achievement.PERFECT_INVESTIGATION)) {
            unlocked.add(Achievement.PERFECT_INVESTIGATION);
            newlyUnlocked.add(Achievement.PERFECT_INVESTIGATION);
        }

        return newlyUnlocked;
    }

    public List<Achievement> checkOnEnding(GameState.Ending ending) {
        List<Achievement> newlyUnlocked = new ArrayList<>();

        if (ending == GameState.Ending.SEAL_THE_WALL && !unlocked.contains(Achievement.GUARDIAN)) {
            unlocked.add(Achievement.GUARDIAN);
            newlyUnlocked.add(Achievement.GUARDIAN);
        }

        if (ending == GameState.Ending.DESTROY_TAPES && !unlocked.contains(Achievement.ARSONIST)) {
            unlocked.add(Achievement.ARSONIST);
            newlyUnlocked.add(Achievement.ARSONIST);
        }

        if (ending == GameState.Ending.ESCAPE_MANOR && !unlocked.contains(Achievement.SURVIVOR)) {
            unlocked.add(Achievement.SURVIVOR);
            newlyUnlocked.add(Achievement.SURVIVOR);
        }

        if (state.getAnomalyCount() >= 7 && ending != GameState.Ending.NONE
                && !unlocked.contains(Achievement.CYCLE_BREAKER)) {
            unlocked.add(Achievement.CYCLE_BREAKER);
            newlyUnlocked.add(Achievement.CYCLE_BREAKER);
        }

        return newlyUnlocked;
    }

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
