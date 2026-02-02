package com.dsa.game.systems;

import com.dsa.game.state.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Progressive hint system with 3 tiers (vague -> moderate -> explicit).
 * Cycles through tiers on repeated hint requests.
 * Also provides one-time interview hints per suspect.
 */
public class HintSystem {

    private final GameState state;
    private final EvidenceSystem evidenceSystem;
    private int hintRequestCount = 0;
    private final Map<Suspect, Boolean> interviewHintsGiven = new HashMap<>();

    public HintSystem(GameState state, EvidenceSystem evidenceSystem) {
        this.state = state;
        this.evidenceSystem = evidenceSystem;
    }

    /**
     * Returns a progressive hint based on game state.
     * Cycles through 3 tiers: vague (0), moderate (1), explicit (2).
     */
    public String getHint() {
        int tier = hintRequestCount % 3;
        hintRequestCount++;

        // Determine which category of hint to give based on game progress
        if (state.getCollectedEvidence().isEmpty() && state.getCollectedTapes().isEmpty()) {
            return getStartHint(tier);
        }

        if (state.getCollectedTapes().isEmpty()) {
            return getTapeLocationHint(tier);
        }

        if (state.getWatchedTapes().size() < state.getCollectedTapes().size()) {
            return getWatchTapeHint(tier);
        }

        if (!evidenceSystem.canAccuseJamesAndDaniel()) {
            return getEvidenceGapHint(tier);
        }

        return getAccusationHint(tier);
    }

    private String getStartHint(int tier) {
        switch (tier) {
            case 0:
                return "The manor holds many secrets. Look around carefully.";
            case 1:
                return "Start with the crime scene. The Study has several things worth examining -- the desk, drawers, papers, fireplace.";
            default:
                return "Go to the Study and examine the desk, drawers, and fireplace. Some objects reveal more on a second examination. " +
                       "The desk hides a tape recorder underneath.";
        }
    }

    private String getTapeLocationHint(int tier) {
        switch (tier) {
            case 0:
                return "Hidden recordings are scattered throughout the manor. Examine objects thoroughly.";
            case 1:
                return "Tapes are hidden inside objects: desks, clocks, bookshelves, storage areas. " +
                       "Some require examining the same object more than once.";
            default:
                StringBuilder sb = new StringBuilder();
                sb.append("Tape locations:\n");
                for (Tape t : Tape.values()) {
                    if (!state.hasTape(t)) {
                        sb.append("- ").append(t.getTitle()).append(": hidden in the ")
                          .append(t.getHiddenInObject().replace('_', ' '))
                          .append(" (").append(formatRoomName(t.getHiddenInRoom())).append(")\n");
                    }
                }
                return sb.toString();
            }
    }

    private String getWatchTapeHint(int tier) {
        switch (tier) {
            case 0:
                return "You have unwatched tapes. They contain crucial information.";
            case 1:
                return "Open your INVENTORY and press PLAY on unwatched tapes. Each tape costs awareness, so be strategic.";
            default:
                int unwatched = state.getCollectedTapes().size() - state.getWatchedTapes().size();
                return "You have " + unwatched + " unwatched tape(s). Press I for Inventory, then click PLAY. " +
                       "Each tape costs +4 awareness (the cellar tape costs +5). Budget your awareness carefully.";
        }
    }

    private String getEvidenceGapHint(int tier) {
        int jCount = evidenceSystem.getJamesEvidenceCount();
        int dCount = evidenceSystem.getDanielEvidenceCount();

        switch (tier) {
            case 0:
                return "You need more evidence. Keep examining objects and interviewing suspects.";
            case 1:
                return "Evidence against James: " + jCount + "/3 needed. Evidence against Daniel: " + dCount + "/2 needed. " +
                       "Try rooms you haven't fully explored.";
            default:
                StringBuilder sb = new StringBuilder();
                sb.append("Evidence needed:\n");
                sb.append("Against James (have ").append(jCount).append("/3): ");
                if (!state.hasEvidence(Evidence.FINANCIAL_RECORDS)) sb.append("Financial Records (Study drawers), ");
                if (!state.hasEvidence(Evidence.WILL_COPY)) sb.append("Will Copy (Guest Rooms letter), ");
                if (!state.hasEvidence(Evidence.TORN_LETTER)) sb.append("Torn Letter (Study fireplace ashes), ");
                if (!state.hasTape(Tape.TAPE_ARGUMENT)) sb.append("Tape: Argument (Study under_desk), ");
                if (!state.hasTape(Tape.TAPE_WILL_READING)) sb.append("Tape: Will Reading (Study bookshelves), ");
                sb.append("\nAgainst Daniel (have ").append(dCount).append("/2): ");
                if (!state.hasEvidence(Evidence.GROUNDSKEEPER_LOG)) sb.append("Groundskeeper Log (Shed logbook), ");
                if (!state.hasEvidence(Evidence.MUDDY_BOOTS)) sb.append("Muddy Boots (Kitchen), ");
                if (!state.hasEvidence(Evidence.BLACKMAIL_NOTE)) sb.append("Blackmail Note (Servants' Quarters floorboard), ");
                if (!state.hasTape(Tape.TAPE_DANIEL_MEETING)) sb.append("Tape: Daniel Meeting (Shed logbook), ");
                if (!state.hasTape(Tape.TAPE_CELLAR_NOISES)) sb.append("Tape: Cellar (Cellar wine_rack), ");
                return sb.toString();
        }
    }

    private String getAccusationHint(int tier) {
        switch (tier) {
            case 0:
                return "You have enough evidence. It's time to act.";
            case 1:
                return "Click ACCUSE in the action bar. Think about who worked together.";
            default:
                return "Click ACCUSE and select 'James & Daniel (together)'. They conspired to kill Harold before he could change the will.";
        }
    }

    /**
     * Returns a one-time interview hint for a specific suspect.
     * Returns null if the hint has already been given for this suspect.
     */
    public String getInterviewHint(Suspect suspect) {
        if (interviewHintsGiven.containsKey(suspect)) {
            return null;
        }
        interviewHintsGiven.put(suspect, true);

        switch (suspect) {
            case JAMES:
                return "[Hint: James is defensive about the finances and the will. Press him on those topics, and show him the financial records if you have them.]";
            case MARGARET:
                return "[Hint: Margaret knows more than she lets on. She heard things the night of the murder. Ask about the night and about James.]";
            case DANIEL:
                return "[Hint: Daniel is guarded but nervous. His logbook has a missing entry. Ask about the grounds and his relationship with James.]";
            case ELEANOR:
                return "[Hint: Eleanor is the most cooperative. She found the sleeping powder and heard sounds the night of the murder. Show her evidence to get her perspective.]";
            case REGINALD:
                return "[Hint: Reginald knows the household routines. He may have noticed unusual behavior the night of the murder.]";
            default:
                return null;
        }
    }

    private String formatRoomName(com.dsa.game.navigation.Room.RoomID roomId) {
        return roomId.name().charAt(0) + roomId.name().substring(1).toLowerCase().replace('_', ' ');
    }
}
