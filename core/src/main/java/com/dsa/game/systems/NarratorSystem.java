package com.dsa.game.systems;

import com.dsa.game.data.NarratorText;
import com.dsa.game.data.NarratorText.Mood;
import com.dsa.game.state.GameState;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Narrator system that shifts personality based on awareness level.
 * Provides mood-appropriate warnings, environmental cues, and commentary.
 */
public class NarratorSystem {

    private static final String NARRATOR_HEADER =
        "[The voice on the tape crackles to life...]\n\n" +
        "\"Ah, you've found Arthur's recordings. Good. I've been waiting for someone " +
        "who would listen. My name doesn't matter -- what matters is what happened " +
        "in this house. I'll guide you as best I can, but understand: the longer you " +
        "stay, the more THEY notice. The walls have ears here. Literally.\n\n" +
        "Trust what you see. Question what you're told. And whatever you do... " +
        "don't let them know how much you know.\"\n\n---\n\n";

    private final GameState state;
    private final Random random = new Random();
    private final Set<Integer> usedWarnings = new HashSet<>();
    private final Set<Integer> usedCues = new HashSet<>();
    private int commentaryIndex = 0;
    private boolean filterEnabled = true;

    public NarratorSystem(GameState state) {
        this.state = state;
    }

    /** Returns the current narrator mood based on awareness. */
    public Mood getCurrentMood() {
        return NarratorText.getMoodForAwareness(state.getAwareness());
    }

    /** Returns a non-repeating mood-appropriate warning. Cycles when all used. */
    public String getWarning() {
        Mood mood = getCurrentMood();
        String[] warnings = NarratorText.getWarnings(mood);
        int key = mood.ordinal() * 100; // namespace per mood

        // Find an unused warning for this mood
        int index = -1;
        for (int i = 0; i < warnings.length; i++) {
            if (!usedWarnings.contains(key + i)) {
                index = i;
                break;
            }
        }

        // If all used, reset and pick randomly
        if (index == -1) {
            for (int i = 0; i < warnings.length; i++) {
                usedWarnings.remove(key + i);
            }
            index = random.nextInt(warnings.length);
        }

        usedWarnings.add(key + index);
        return warnings[index];
    }

    /** Returns a mood-appropriate environmental cue. */
    public String getEnvironmentalCue() {
        Mood mood = getCurrentMood();
        String[] cues = NarratorText.getEnvironmentalCues(mood);
        int key = mood.ordinal() * 100;

        int index = -1;
        for (int i = 0; i < cues.length; i++) {
            if (!usedCues.contains(key + i)) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            for (int i = 0; i < cues.length; i++) {
                usedCues.remove(key + i);
            }
            index = random.nextInt(cues.length);
        }

        usedCues.add(key + index);
        return cues[index];
    }

    /** Prepends mood-appropriate commentary prefix to game text. */
    public String filterText(String text) {
        StringBuilder sb = new StringBuilder();

        // One-time narrator header on first call
        if (!state.isNarratorHeaderShown()) {
            sb.append(NARRATOR_HEADER);
            state.setNarratorHeaderShown(true);
        }

        if (filterEnabled) {
            Mood mood = getCurrentMood();
            String[] prefixes = NarratorText.getCommentaryPrefixes(mood);
            String prefix = prefixes[commentaryIndex % prefixes.length];
            commentaryIndex++;
            sb.append(prefix).append(text);
        } else {
            sb.append(text);
        }

        return sb.toString();
    }

    public boolean isFilterEnabled() { return filterEnabled; }
    public void setFilterEnabled(boolean enabled) { this.filterEnabled = enabled; }

    /**
     * Returns an atmospheric event with 25% probability if awareness >= 40.
     * Returns null otherwise.
     */
    public String maybeGetAtmosphericEvent() {
        if (state.getAwareness() < 40) return null;
        if (random.nextInt(4) != 0) return null; // 25% chance

        String[] events = NarratorText.getAtmosphericEvents();
        return events[random.nextInt(events.length)];
    }
}
