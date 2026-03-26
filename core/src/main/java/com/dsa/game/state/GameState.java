package com.dsa.game.state;

import com.dsa.game.navigation.Room;

import java.util.*;

public class GameState {

    // Awareness
    private int awareness = 0;
    public static final int MAX_AWARENESS = 80;

    // Evidence & tapes
    private final Set<Evidence> collectedEvidence = new LinkedHashSet<>();
    private final Set<Tape> collectedTapes = new LinkedHashSet<>();
    private final Set<Tape> watchedTapes = new LinkedHashSet<>();

    // Suspect cooperation (0-100)
    private final Map<Suspect, Integer> cooperation = new EnumMap<>(Suspect.class);

    // Contradictions discovered
    private final Set<Contradiction> discoveredContradictions = new LinkedHashSet<>();

    // Entity anomalies discovered
    private final Set<EntityAnomaly> discoveredAnomalies = new LinkedHashSet<>();

    // Room visit counts
    private final Map<Room.RoomID, Integer> visitCounts = new EnumMap<>(Room.RoomID.class);

    // Object examination counts: "ROOM_OBJECT" -> count
    private final Map<String, Integer> examCounts = new HashMap<>();

    // Interview state
    private Suspect currentInterviewSuspect = null;
    private final Set<String> askedTopics = new HashSet<>(); // "SUSPECT_TOPIC" keys

    // Flags
    private boolean gameOver = false;
    private boolean gameWon = false;
    private boolean accusationMade = false;
    private boolean narratorHeaderShown = false;

    // Tracking counters
    private int commandCount = 0;
    private int interviewCount = 0;
    private int evidenceShownCount = 0;
    private boolean climaxTriggered = false;

    // Event log
    private final List<String> eventLog = new ArrayList<>();

    // Confrontation tracking
    private final Set<Suspect> confrontedSuspects = EnumSet.noneOf(Suspect.class);

    // Received lies from suspects (Feature 1)
    private final Set<String> receivedLies = new LinkedHashSet<>();

    // Narrator distortions encountered (Feature 2)
    private final List<String> narratorDistortions = new ArrayList<>();

    // Wrong accusation count (Feature 4)
    private int wrongAccusationCount = 0;

    // Tape repair kit (Act 3 gate)
    private boolean hasTapeRepairKit = false;

    // Margaret's room drawer state
    private boolean margaretTopOpen = false;
    private boolean margaretBotOpen = false;
    private boolean margaretShoesExamined = false;

    // Tape progression system (Option 2)
    private final Set<Tape> unlockedTapes = new LinkedHashSet<>();
    private final Set<String> learnedCodes = new LinkedHashSet<>();
    private final Set<Tape> repairedTapes = new LinkedHashSet<>();
    private int repairSolutionsRemaining = 0;

    // Minigame score (0–200, stored after each minigame)
    private int lastMinigameScore = 0;

    // Ending choice (Feature 6)
    public enum Ending { NONE, ACCUSATION_CORRECT, ACCUSATION_WRONG, SEAL_THE_WALL, ESCAPE_MANOR, DESTROY_TAPES, GAME_OVER_AWARENESS, LEAVE_MANOR }
    private Ending chosenEnding = Ending.NONE;

    // Achievement tracking (persisted via SaveLoadSystem)
    private final Set<Achievement> unlockedAchievements = new LinkedHashSet<>();

    public GameState() {
        // Initialize cooperation from starting values
        for (Suspect s : Suspect.values()) {
            cooperation.put(s, s.getStartingCooperation());
        }
        // Initialize visit counts
        for (Room.RoomID id : Room.RoomID.values()) {
            visitCounts.put(id, 0);
        }
        // Count initial room
        visitCounts.put(Room.RoomID.ENTRANCE, 1);
    }

    // Minigame score
    public int getLastMinigameScore() { return lastMinigameScore; }
    public void setLastMinigameScore(int score) { lastMinigameScore = Math.max(0, Math.min(200, score)); }

    /** Returns "TRUTH SURFACED" / "MOSTLY CLEAR" / "DISTORTED" / "CORRUPTED" */
    public String getMinigameScoreTier() {
        if (lastMinigameScore >= 170) return "TRUTH SURFACED";
        if (lastMinigameScore >= 120) return "MOSTLY CLEAR";
        if (lastMinigameScore >=  70) return "DISTORTED";
        return "CORRUPTED";
    }

    // Awareness
    public int getAwareness() { return awareness; }
    public void addAwareness(int amount) { awareness = Math.min(awareness + amount, MAX_AWARENESS); }

    // Evidence
    public Set<Evidence> getCollectedEvidence() { return Collections.unmodifiableSet(collectedEvidence); }
    public boolean hasEvidence(Evidence e) { return collectedEvidence.contains(e); }
    public boolean collectEvidence(Evidence e) { return collectedEvidence.add(e); }

    // Tapes
    public Set<Tape> getCollectedTapes() { return Collections.unmodifiableSet(collectedTapes); }
    public boolean hasTape(Tape t) { return collectedTapes.contains(t); }
    public boolean collectTape(Tape t) { return collectedTapes.add(t); }
    public Set<Tape> getWatchedTapes() { return Collections.unmodifiableSet(watchedTapes); }
    public boolean hasWatchedTape(Tape t) { return watchedTapes.contains(t); }
    public boolean watchTape(Tape t) { return watchedTapes.add(t); }

    // Cooperation
    public int getCooperation(Suspect s) { return cooperation.getOrDefault(s, s.getStartingCooperation()); }
    public void adjustCooperation(Suspect s, int delta) {
        int current = getCooperation(s);
        cooperation.put(s, Math.max(0, Math.min(100, current + delta)));
    }

    // Contradictions
    public Set<Contradiction> getDiscoveredContradictions() { return Collections.unmodifiableSet(discoveredContradictions); }
    public boolean hasContradiction(Contradiction c) { return discoveredContradictions.contains(c); }
    public boolean discoverContradiction(Contradiction c) { return discoveredContradictions.add(c); }

    // Visit counts
    public int getVisitCount(Room.RoomID room) { return visitCounts.getOrDefault(room, 0); }
    public void incrementVisit(Room.RoomID room) { visitCounts.put(room, getVisitCount(room) + 1); }

    // Exam counts
    public int getExamCount(Room.RoomID room, String object) {
        return examCounts.getOrDefault(room.name() + "_" + object, 0);
    }
    public void incrementExamCount(Room.RoomID room, String object) {
        String key = room.name() + "_" + object;
        examCounts.put(key, examCounts.getOrDefault(key, 0) + 1);
    }

    // Interview
    public Suspect getCurrentInterviewSuspect() { return currentInterviewSuspect; }
    public void setCurrentInterviewSuspect(Suspect s) { currentInterviewSuspect = s; }
    public boolean hasAskedTopic(Suspect s, String topic) { return askedTopics.contains(s.name() + "_" + topic); }
    public void markTopicAsked(Suspect s, String topic) { askedTopics.add(s.name() + "_" + topic); }

    // Flags
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public boolean isGameWon() { return gameWon; }
    public void setGameWon(boolean gameWon) { this.gameWon = gameWon; }
    public boolean isAccusationMade() { return accusationMade; }
    public void setAccusationMade(boolean accusationMade) { this.accusationMade = accusationMade; }
    public boolean isNarratorHeaderShown() { return narratorHeaderShown; }
    public void setNarratorHeaderShown(boolean shown) { this.narratorHeaderShown = shown; }

    // Tracking counters
    public int getCommandCount() { return commandCount; }
    public void incrementCommandCount() { commandCount++; }
    public int getInterviewCount() { return interviewCount; }
    public void incrementInterviewCount() { interviewCount++; }
    public int getEvidenceShownCount() { return evidenceShownCount; }
    public void incrementEvidenceShownCount() { evidenceShownCount++; }
    public boolean isClimaxTriggered() { return climaxTriggered; }
    public void setClimaxTriggered(boolean climaxTriggered) { this.climaxTriggered = climaxTriggered; }

    // Event log
    public List<String> getEventLog() { return Collections.unmodifiableList(eventLog); }
    public void addEvent(String event) { eventLog.add("[" + commandCount + "] " + event); }
    public void forceAddEvent(String event) { eventLog.add(event); }

    // Confrontation tracking
    public boolean hasConfronted(Suspect s) { return confrontedSuspects.contains(s); }
    public void markConfronted(Suspect s) { confrontedSuspects.add(s); }
    public void forceMarkConfronted(Suspect s) { confrontedSuspects.add(s); }
    public Set<Suspect> getConfrontedSuspects() { return Collections.unmodifiableSet(confrontedSuspects); }

    // Entity anomalies
    public Set<EntityAnomaly> getDiscoveredAnomalies() { return Collections.unmodifiableSet(discoveredAnomalies); }
    public boolean hasAnomaly(EntityAnomaly a) { return discoveredAnomalies.contains(a); }
    public boolean discoverAnomaly(EntityAnomaly a) { return discoveredAnomalies.add(a); }
    public int getAnomalyCount() { return discoveredAnomalies.size(); }

    // Received lies
    public Set<String> getReceivedLies() { return Collections.unmodifiableSet(receivedLies); }
    public boolean addReceivedLie(String lieKey) { return receivedLies.add(lieKey); }
    public boolean hasReceivedLie(String lieKey) { return receivedLies.contains(lieKey); }

    // Narrator distortions
    public List<String> getNarratorDistortions() { return Collections.unmodifiableList(narratorDistortions); }
    public void addNarratorDistortion(String distortion) { narratorDistortions.add(distortion); }

    // Wrong accusation count
    public int getWrongAccusationCount() { return wrongAccusationCount; }
    public void incrementWrongAccusationCount() { wrongAccusationCount++; }

    // Tape repair kit
    public boolean hasTapeRepairKit() { return hasTapeRepairKit; }
    public void setHasTapeRepairKit(boolean value) { hasTapeRepairKit = value; }

    // Margaret's room drawer state
    public boolean isMargaretTopOpen() { return margaretTopOpen; }
    public void setMargaretTopOpen(boolean value) { margaretTopOpen = value; }
    public boolean isMargaretBotOpen() { return margaretBotOpen; }
    public void setMargaretBotOpen(boolean value) { margaretBotOpen = value; }
    public boolean isMargaretShoesExamined() { return margaretShoesExamined; }
    public void setMargaretShoesExamined(boolean value) { margaretShoesExamined = value; }

    // Tape progression system
    public boolean unlockTape(Tape t) { return unlockedTapes.add(t); }
    public boolean isUnlockedTape(Tape t) { return t == Tape.TAPE_ARGUMENT || unlockedTapes.contains(t); }
    public void learnCode(String code) { learnedCodes.add(code); }
    public boolean hasLearnedCode(String code) { return learnedCodes.contains(code); }
    public void addRepairSolution() { repairSolutionsRemaining++; }
    public boolean useRepairSolution(Tape t) {
        if (repairSolutionsRemaining <= 0) return false;
        repairSolutionsRemaining--;
        repairedTapes.add(t);
        return true;
    }
    public boolean isTapeRepaired(Tape t) { return repairedTapes.contains(t); }
    public int getRepairSolutionsRemaining() { return repairSolutionsRemaining; }
    public Set<Tape> getUnlockedTapes() { return Collections.unmodifiableSet(unlockedTapes); }
    public Set<String> getLearnedCodes() { return Collections.unmodifiableSet(learnedCodes); }
    public Set<Tape> getRepairedTapes() { return Collections.unmodifiableSet(repairedTapes); }
    // Force-setters for SaveLoadSystem
    public void forceUnlockTape(Tape t) { unlockedTapes.add(t); }
    public void forceLearnCode(String c) { learnedCodes.add(c); }
    public void forceRepairTape(Tape t) { repairedTapes.add(t); }
    public void setRepairSolutionsRemaining(int n) { repairSolutionsRemaining = n; }

    // Ending
    public Ending getChosenEnding() { return chosenEnding; }
    public void setChosenEnding(Ending ending) { this.chosenEnding = ending; }

    // Save/load support
    public void setAwareness(int value) { awareness = Math.max(0, Math.min(value, MAX_AWARENESS)); }

    /** Unmodifiable view of cooperation map for serialization. */
    public Map<Suspect, Integer> getCooperationMap() { return Collections.unmodifiableMap(cooperation); }
    /** Unmodifiable view of visit count map for serialization. */
    public Map<Room.RoomID, Integer> getVisitCountMap() { return Collections.unmodifiableMap(visitCounts); }
    /** Unmodifiable view of exam count map for serialization. */
    public Map<String, Integer> getExamCountMap() { return Collections.unmodifiableMap(examCounts); }
    /** Unmodifiable view of asked topics for serialization. */
    public Set<String> getAskedTopics() { return Collections.unmodifiableSet(askedTopics); }

    /** Clear all state for save/load deserialization. */
    public void clearAllState() {
        awareness = 0;
        collectedEvidence.clear();
        collectedTapes.clear();
        watchedTapes.clear();
        discoveredContradictions.clear();
        examCounts.clear();
        askedTopics.clear();
        currentInterviewSuspect = null;
        gameOver = false;
        gameWon = false;
        accusationMade = false;
        commandCount = 0;
        interviewCount = 0;
        evidenceShownCount = 0;
        climaxTriggered = false;
        narratorHeaderShown = false;
        eventLog.clear();
        confrontedSuspects.clear();
        discoveredAnomalies.clear();
        receivedLies.clear();
        narratorDistortions.clear();
        wrongAccusationCount = 0;
        hasTapeRepairKit = false;
        margaretTopOpen = false;
        margaretBotOpen = false;
        margaretShoesExamined = false;
        chosenEnding = Ending.NONE;
        unlockedTapes.clear();
        learnedCodes.clear();
        repairedTapes.clear();
        repairSolutionsRemaining = 0;
        for (Suspect s : Suspect.values()) {
            cooperation.put(s, s.getStartingCooperation());
        }
        for (Room.RoomID id : Room.RoomID.values()) {
            visitCounts.put(id, 0);
        }
        visitCounts.put(Room.RoomID.ENTRANCE, 1);
        unlockedAchievements.clear();
    }

    // Force-setters for save/load deserialization
    public void forceCollectEvidence(Evidence e) { collectedEvidence.add(e); }
    public void forceCollectTape(Tape t) { collectedTapes.add(t); }
    public void forceWatchTape(Tape t) { watchedTapes.add(t); }
    public void forceDiscoverContradiction(Contradiction c) { discoveredContradictions.add(c); }
    public void forceSetCooperation(Suspect s, int value) { cooperation.put(s, value); }
    public void forceSetVisitCount(Room.RoomID room, int count) { visitCounts.put(room, count); }
    public void forceSetExamCount(String key, int count) { examCounts.put(key, count); }
    public void forceAddAskedTopic(String topic) { askedTopics.add(topic); }
    public void setCommandCount(int count) { commandCount = count; }
    public void setInterviewCount(int count) { interviewCount = count; }
    public void setEvidenceShownCount(int count) { evidenceShownCount = count; }
    public void forceDiscoverAnomaly(EntityAnomaly a) { discoveredAnomalies.add(a); }
    public void forceAddReceivedLie(String lieKey) { receivedLies.add(lieKey); }
    public void forceAddNarratorDistortion(String distortion) { narratorDistortions.add(distortion); }
    public void setWrongAccusationCount(int count) { wrongAccusationCount = count; }
    public void setChosenEndingByName(String name) {
        try {
            chosenEnding = Ending.valueOf(name);
        } catch (IllegalArgumentException e) {
            chosenEnding = Ending.NONE;
        }
    }

    // Achievement tracking
    public Set<Achievement> getUnlockedAchievements() { return Collections.unmodifiableSet(unlockedAchievements); }
    public void unlockAchievement(Achievement a) { unlockedAchievements.add(a); }
    public void forceUnlockAchievement(Achievement a) { unlockedAchievements.add(a); }
}
