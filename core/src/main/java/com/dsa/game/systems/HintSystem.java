package com.dsa.game.systems;

import com.dsa.game.state.*;
import com.dsa.game.state.EntityAnomaly;

import java.util.HashMap;
import java.util.Map;

public class HintSystem {

    private final GameState state;
    private final EvidenceSystem evidenceSystem;
    private int hintRequestCount = 0;
    private final Map<Suspect, Boolean> interviewHintsGiven = new HashMap<>();

    public HintSystem(GameState state, EvidenceSystem evidenceSystem) {
        this.state = state;
        this.evidenceSystem = evidenceSystem;
    }

    public String getHint() {
        int tier = hintRequestCount % 3;
        hintRequestCount++;

        if (state.getCollectedEvidence().isEmpty() && state.getCollectedTapes().isEmpty()) {
            return getStartHint(tier);
        }

        if (state.getCollectedTapes().isEmpty()) {
            return getTapeLocationHint(tier);
        }

        if (state.getWatchedTapes().size() < state.getCollectedTapes().size()) {
            return getWatchTapeHint(tier);
        }

        if (!isCellarUnlocked() && state.getWatchedTapes().size() >= 2) {
            return getCellarProgressionHint(tier);
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
                          .append(" (").append(formatRoomName(t.getHiddenInRoom())).append(")");
                        if (t.getHiddenInRoom() == com.dsa.game.navigation.Room.RoomID.CELLAR && !isCellarUnlocked()) {
                            sb.append(" [SEALED]");
                        }
                        if (t.getHiddenInRoom() == com.dsa.game.navigation.Room.RoomID.MARGARET_ROOM && !isMargaretRoomUnlocked()) {
                            sb.append(" [SEALED]");
                        }
                        sb.append("\n");
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
                return "Evidence against James: " + jCount + "/6 found (need 3 to accuse). Evidence against Daniel: " + dCount + "/4 found (need 2 to accuse). " +
                       "Try rooms you haven't fully explored.";
            default:
                StringBuilder sb = new StringBuilder();
                sb.append("Evidence needed:\n");
                sb.append("Against James (have ").append(jCount).append("/6): ");
                if (!state.hasEvidence(Evidence.FINANCIAL_RECORDS)) sb.append("Financial Records (Study drawers), ");
                if (!state.hasEvidence(Evidence.WILL_COPY)) sb.append("Will Copy (Parlor briefcase), ");
                if (!state.hasEvidence(Evidence.TORN_LETTER)) sb.append("Torn Letter (Study fireplace ashes), ");
                if (!state.hasTape(Tape.TAPE_ARGUMENT)) sb.append("Tape: Argument (Study under_desk), ");
                if (!state.hasTape(Tape.TAPE_JAMES_INTERVIEW)) sb.append("Tape: James Interview (Study bookshelves), ");
                if (!state.hasEvidence(Evidence.BLACKMAIL_NOTE)) sb.append("Blackmail Note (Margaret's Room letter), ");
                sb.append("\nAgainst Daniel (have ").append(dCount).append("/4): ");
                if (!state.hasEvidence(Evidence.GROUNDSKEEPER_LOG)) sb.append("Groundskeeper Log (Shed logbook), ");
                if (!state.hasEvidence(Evidence.MUDDY_BOOTS)) sb.append("Muddy Boots (Shed shelf, 2nd exam), ");
                if (!state.hasTape(Tape.TAPE_DANIEL_INTERVIEW)) sb.append("Tape: Daniel Interview (Shed logbook), ");
                if (!state.hasTape(Tape.TAPE_MARGARET_INTERVIEW)) sb.append("Tape: Margaret Interview (Kitchen storage_cellar), ");
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
            case MARCUS:
                return "[Hint: Marcus is defensive about the lawsuit, but he witnessed events that night. Ask about the settlement dinner and what he heard before leaving.]";
            case CHARLES:
                return "[Hint: Charles is eager to help. He saw James heading to the study that night and knows about the will changes. Ask him about what Mr. Vance discovered and what he observed.]";
            default:
                return null;
        }
    }

    private boolean isCellarUnlocked() {
        return state.getWatchedTapes().size() >= 4
            && state.getCollectedEvidence().size() >= 5;
    }

    private boolean isMargaretRoomUnlocked() {
        return state.hasAnomaly(EntityAnomaly.BREATHING_WALL);
    }

    private String getCellarProgressionHint(int tier) {
        int tapes = state.getWatchedTapes().size();
        int evidence = state.getCollectedEvidence().size();
        switch (tier) {
            case 0:
                return "The cellar door is sealed shut. Something is keeping it closed. You need to dig deeper before it will open.";
            case 1:
                return "The cellar requires 4 watched tapes and 5 pieces of evidence to unlock. " +
                       "You have " + tapes + " tapes and " + evidence + " evidence so far.";
            default:
                StringBuilder sb = new StringBuilder();
                sb.append("Cellar gate: ").append(tapes).append("/4 tapes watched, ")
                  .append(evidence).append("/5 evidence collected.\n");
                if (tapes < 4) sb.append("Find and watch more tapes. Check the Study, Parlor, Kitchen, and Shed.\n");
                if (evidence < 5) sb.append("Examine objects more thoroughly. Some reveal new clues on a second look.");
                return sb.toString();
        }
    }

    private String formatRoomName(com.dsa.game.navigation.Room.RoomID roomId) {
        return roomId.name().charAt(0) + roomId.name().substring(1).toLowerCase().replace('_', ' ');
    }
}
