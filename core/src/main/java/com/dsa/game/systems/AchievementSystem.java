package com.dsa.game.systems;

import com.dsa.game.state.*;

import java.util.*;

public class AchievementSystem {

    private final GameState state;
    private final EvidenceSystem evidenceSystem;

    public AchievementSystem(GameState state, EvidenceSystem evidenceSystem) {
        this.state = state;
        this.evidenceSystem = evidenceSystem;
    }

    public List<Achievement> checkOnWin() {
        List<Achievement> newlyUnlocked = new ArrayList<>();

        if (state.getCommandCount() <= 10 && !state.getUnlockedAchievements().contains(Achievement.SPEEDRUN)) {
            state.unlockAchievement(Achievement.SPEEDRUN);
            newlyUnlocked.add(Achievement.SPEEDRUN);
        }

        if (state.getAwareness() < 25 && !state.getUnlockedAchievements().contains(Achievement.GHOST)) {
            state.unlockAchievement(Achievement.GHOST);
            newlyUnlocked.add(Achievement.GHOST);
        }

        boolean allEvidence = state.getCollectedEvidence().size() == Evidence.values().length;
        boolean allTapesWatched = state.getWatchedTapes().size() == Tape.values().length;
        if (allEvidence && allTapesWatched && !state.getUnlockedAchievements().contains(Achievement.COMPLETIONIST)) {
            state.unlockAchievement(Achievement.COMPLETIONIST);
            newlyUnlocked.add(Achievement.COMPLETIONIST);
        }

        if (allEvidence && allTapesWatched && state.getAwareness() < 30
                && !state.getUnlockedAchievements().contains(Achievement.PERFECT_INVESTIGATION)) {
            state.unlockAchievement(Achievement.PERFECT_INVESTIGATION);
            newlyUnlocked.add(Achievement.PERFECT_INVESTIGATION);
        }

        return newlyUnlocked;
    }

    public List<Achievement> checkOnEnding(GameState.Ending ending) {
        List<Achievement> newlyUnlocked = new ArrayList<>();

        if (ending == GameState.Ending.SEAL_THE_WALL && !state.getUnlockedAchievements().contains(Achievement.GUARDIAN)) {
            state.unlockAchievement(Achievement.GUARDIAN);
            newlyUnlocked.add(Achievement.GUARDIAN);
        }

        if (ending == GameState.Ending.DESTROY_TAPES && !state.getUnlockedAchievements().contains(Achievement.ARSONIST)) {
            state.unlockAchievement(Achievement.ARSONIST);
            newlyUnlocked.add(Achievement.ARSONIST);
        }

        if (ending == GameState.Ending.ESCAPE_MANOR && !state.getUnlockedAchievements().contains(Achievement.SURVIVOR)) {
            state.unlockAchievement(Achievement.SURVIVOR);
            newlyUnlocked.add(Achievement.SURVIVOR);
        }

        if (state.getAnomalyCount() >= 7 && ending != GameState.Ending.NONE
                && !state.getUnlockedAchievements().contains(Achievement.CYCLE_BREAKER)) {
            state.unlockAchievement(Achievement.CYCLE_BREAKER);
            newlyUnlocked.add(Achievement.CYCLE_BREAKER);
        }

        return newlyUnlocked;
    }

    public String getAchievementsText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ACHIEVEMENTS ===\n\n");

        for (Achievement a : Achievement.values()) {
            boolean isUnlocked = state.getUnlockedAchievements().contains(a);
            sb.append(isUnlocked ? "[X] " : "[ ] ");
            sb.append(a.getDisplayName()).append("\n");
            sb.append("    ").append(a.getDescription()).append("\n\n");
        }

        return sb.toString();
    }

    public Set<Achievement> getUnlocked() {
        return state.getUnlockedAchievements();
    }
}
