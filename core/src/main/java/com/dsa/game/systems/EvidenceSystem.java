package com.dsa.game.systems;

import com.dsa.game.state.*;

public class EvidenceSystem {

    private final GameState state;

    public EvidenceSystem(GameState state) {
        this.state = state;
    }

    /** Collect evidence. Returns true if newly collected. */
    public boolean collect(Evidence e) {
        return state.collectEvidence(e);
    }

    /** Collect tape. Returns true if newly collected. */
    public boolean collectTape(Tape t) {
        return state.collectTape(t);
    }

    /** Watch a tape. Returns true if newly watched. */
    public boolean watchTape(Tape t) {
        return state.watchTape(t);
    }

    /** Count evidence pointing at James. */
    public int getJamesEvidenceCount() {
        int count = 0;
        if (state.hasEvidence(Evidence.FINANCIAL_RECORDS)) count++;
        if (state.hasEvidence(Evidence.WILL_COPY)) count++;
        if (state.hasEvidence(Evidence.TORN_LETTER)) count++;
        if (state.hasTape(Tape.TAPE_ARGUMENT)) count++;
        if (state.hasTape(Tape.TAPE_WILL_READING)) count++;
        return count;
    }

    /** Count evidence pointing at Daniel. */
    public int getDanielEvidenceCount() {
        int count = 0;
        if (state.hasEvidence(Evidence.GROUNDSKEEPER_LOG)) count++;
        if (state.hasEvidence(Evidence.MUDDY_BOOTS)) count++;
        if (state.hasEvidence(Evidence.BLACKMAIL_NOTE)) count++;
        if (state.hasTape(Tape.TAPE_DANIEL_MEETING)) count++;
        if (state.hasTape(Tape.TAPE_CELLAR_NOISES)) count++;
        return count;
    }

    /** Can accuse James and Daniel together (the correct solution). */
    public boolean canAccuseJamesAndDaniel() {
        return getJamesEvidenceCount() >= 3 && getDanielEvidenceCount() >= 2;
    }

    /** Build inventory text for display. */
    public String getInventoryText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== COLLECTED EVIDENCE ===\n\n");

        if (state.getCollectedEvidence().isEmpty() && state.getCollectedTapes().isEmpty()) {
            sb.append("No evidence collected yet.\n");
            sb.append("Examine objects in rooms to find clues.");
            return sb.toString();
        }

        for (Evidence e : state.getCollectedEvidence()) {
            sb.append("* ").append(e.getDisplayName()).append("\n");
            sb.append("  ").append(e.getDescription()).append("\n\n");
        }

        if (!state.getCollectedTapes().isEmpty()) {
            sb.append("=== TAPES ===\n\n");
            for (Tape t : state.getCollectedTapes()) {
                boolean watched = state.hasWatchedTape(t);
                sb.append("* ").append(t.getTitle());
                sb.append(watched ? " [WATCHED]" : " [NEW - PLAY]");
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
