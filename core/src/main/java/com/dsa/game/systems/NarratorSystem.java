package com.dsa.game.systems;

import com.dsa.game.data.NarratorText;
import com.dsa.game.data.NarratorText.Mood;
import com.dsa.game.state.Contradiction;
import com.dsa.game.state.Evidence;
import com.dsa.game.state.Tape;
import com.dsa.game.state.EntityAnomaly;
import com.dsa.game.state.GameState;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Narrator system that shifts personality based on awareness level.
 * Provides mood-appropriate warnings, environmental cues, and commentary.
 */
public class NarratorSystem {

    private static final String NARRATOR_HEADER =
        "[A voice fills your mind -- not from any direction, but from everywhere at once. Like a memory that isn't yours.]\n\n" +
        "\"Ah. You came. Good. I've been waiting for someone " +
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

    /** Prepends mood-appropriate commentary prefix to game text, with possible distortion. */
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

        // Maybe append a narrator distortion
        String distortion = maybeGetDistortion();
        if (distortion != null) {
            sb.append("\n\n[The narrator interjects: \"").append(distortion).append("\"]");
            state.addNarratorDistortion(distortion);

            // Auto-discover narrator contradictions if player has contradicting evidence
            checkDistortionContradictions(distortion);
        }

        return sb.toString();
    }

    /**
     * Returns a narrator distortion with probability based on awareness.
     * 20% chance of mild at awareness 40+, 30% chance of severe at 60+.
     */
    public String maybeGetDistortion() {
        int awareness = state.getAwareness();
        if (awareness < 40) return null;

        if (awareness >= 60) {
            if (random.nextInt(100) < 30) {
                String[] severe = NarratorText.getSevereDistortions();
                return severe[random.nextInt(severe.length)];
            }
        }

        if (random.nextInt(100) < 20) {
            String[] mild = NarratorText.getMildDistortions();
            return mild[random.nextInt(mild.length)];
        }

        return null;
    }

    /**
     * Checks if a distortion text contradicts evidence the player already has,
     * and auto-discovers the narrator contradiction if so.
     * Public so GameScreen can call it when distortions fire outside filterText().
     */
    public void checkDistortionContradictions(String distortion) {
        Map<String, Contradiction> map = NarratorText.getDistortionContradictions();
        for (Map.Entry<String, Contradiction> entry : map.entrySet()) {
            if (distortion.contains(entry.getKey())) {
                Contradiction c = entry.getValue();
                if (state.hasContradiction(c)) continue;

                // Check if player has the contradicting evidence
                boolean hasContradictingEvidence = false;
                switch (c) {
                    case NARRATOR_WEAPON:
                        // Letter opener evidence documents it doesn't match the wound (planted weapon).
                        hasContradictingEvidence = state.hasEvidence(Evidence.LETTER_OPENER);
                        break;
                    case NARRATOR_BOOTS:
                        hasContradictingEvidence = state.hasEvidence(Evidence.MUDDY_BOOTS);
                        break;
                    case NARRATOR_LETTER:
                        hasContradictingEvidence = state.hasEvidence(Evidence.TORN_LETTER);
                        break;
                    case NARRATOR_TIME:
                        hasContradictingEvidence = state.hasTape(Tape.TAPE_CHARLES_INTERVIEW);
                        break;
                    default:
                        break;
                }

                if (hasContradictingEvidence) {
                    state.discoverContradiction(c);
                }
            }
        }
    }

    public boolean isFilterEnabled() { return filterEnabled; }
    public void setFilterEnabled(boolean enabled) { this.filterEnabled = enabled; }

    /** Returns channeling intro based on whether this is the first interview. */
    public String getChannelingIntro(boolean isFirstInterview) {
        Mood mood = getCurrentMood();
        if (isFirstInterview) {
            return NarratorText.getChannelingFirstIntro(mood);
        }
        return NarratorText.getChannelingReturnIntro(mood);
    }

    /** 20% chance of bleed-through. Returns null if no bleed. */
    public String maybeGetChannelingBleedThrough() {
        if (random.nextInt(5) != 0) return null;
        String[] lines = NarratorText.getChannelingBleedThrough();
        return lines[random.nextInt(lines.length)];
    }

    /** Returns channeling end text for current mood. */
    public String getChannelingEnd() {
        return NarratorText.getChannelingEnd(getCurrentMood());
    }

    /**
     * Returns a narrator slip ("I remember...") with 20% probability if 3+ anomalies found.
     * Discovering the slip also registers the NARRATOR_I_SLIP anomaly.
     * Returns null otherwise.
     */
    public String maybeGetNarratorSlip() {
        if (state.getAnomalyCount() < 3) return null;
        if (state.hasAnomaly(EntityAnomaly.NARRATOR_I_SLIP)) return null;
        if (random.nextInt(5) != 0) return null; // 20% chance

        state.discoverAnomaly(EntityAnomaly.NARRATOR_I_SLIP);
        return "[The narrator's voice wavers on the tape.]\n\n" +
            "\"I remember when the wall was -- \"\n\n" +
            "[A long pause. Static. Then, carefully:]\n\n" +
            "\"...Forgive me. I misspoke. I meant to say: Arthur's notes mention " +
            "that the wall was sealed decades ago. I have no personal memory of this place. " +
            "Of course I don't. I'm merely a voice on a tape.\"\n\n" +
            "[ANOMALY DISCOVERED: Narrator 'I' Slip -- Who is the narrator, really?]";
    }

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
