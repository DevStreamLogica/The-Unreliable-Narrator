package com.dsa.game.systems;

import com.dsa.game.state.*;

public class EvidenceSystem {

    private final GameState state;

    public EvidenceSystem(GameState state) {
        this.state = state;
    }

    public boolean collect(Evidence e) {
        return state.collectEvidence(e);
    }

    public boolean collectTape(Tape t) {
        return state.collectTape(t);
    }

    public boolean watchTape(Tape t) {
        return state.watchTape(t);
    }

    public int getJamesEvidenceCount() {
        int count = 0;
        if (state.hasEvidence(Evidence.FINANCIAL_RECORDS)) count++;
        if (state.hasEvidence(Evidence.WILL_COPY)) count++;
        if (state.hasEvidence(Evidence.TORN_LETTER)) count++;
        if (state.hasEvidence(Evidence.BLACKMAIL_NOTE)) count++; // James planted it to frame Margaret
        if (state.hasTape(Tape.TAPE_ARGUMENT)) count++;
        if (state.hasTape(Tape.TAPE_JAMES_INTERVIEW)) count++;
        return count;
    }

    public int getDanielEvidenceCount() {
        int count = 0;
        if (state.hasEvidence(Evidence.GROUNDSKEEPER_LOG)) count++;
        if (state.hasEvidence(Evidence.MUDDY_BOOTS)) count++;
        // BLACKMAIL_NOTE moved to James's evidence - he planted it, not Daniel
        if (state.hasTape(Tape.TAPE_DANIEL_INTERVIEW)) count++;
        if (state.hasTape(Tape.TAPE_MARGARET_INTERVIEW)) count++;
        return count;
    }

    public boolean canAccuseJamesAndDaniel() {
        return getJamesEvidenceCount() >= 3 && getDanielEvidenceCount() >= 2;
    }

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
                boolean unlocked = state.isUnlockedTape(t);
                sb.append("* ").append(t.getTitle());
                if (watched) {
                    sb.append(" [WATCHED]");
                } else if (!unlocked) {
                    sb.append(" [LOCKED]");
                } else {
                    sb.append(" [PLAY AVAILABLE]");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
