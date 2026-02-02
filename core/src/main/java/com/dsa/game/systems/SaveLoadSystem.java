package com.dsa.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.dsa.game.navigation.Room;
import com.dsa.game.navigation.RoomManager;
import com.dsa.game.state.*;

import java.util.*;

/**
 * Save/Load system using LibGDX Json serialization.
 * Saves to the local storage "saves/" directory.
 */
public class SaveLoadSystem {

    private static final String SAVE_DIR = "saves/";

    /** Data class for JSON serialization. All fields must be public for LibGDX Json. */
    public static class SaveData {
        public String currentRoom;
        public int awareness;
        public boolean gameOver;
        public boolean gameWon;
        public boolean accusationMade;
        public boolean climaxTriggered;
        public int commandCount;
        public int interviewCount;
        public int evidenceShownCount;

        public List<String> collectedEvidence = new ArrayList<>();
        public List<String> collectedTapes = new ArrayList<>();
        public List<String> watchedTapes = new ArrayList<>();
        public List<String> discoveredContradictions = new ArrayList<>();

        public Map<String, Integer> cooperation = new HashMap<>();
        public Map<String, Integer> visitCounts = new HashMap<>();
        public Map<String, Integer> examCounts = new HashMap<>();
        public List<String> askedTopics = new ArrayList<>();

        public boolean narratorHeaderShown;
        public List<String> eventLog = new ArrayList<>();
        public List<String> confrontedSuspects = new ArrayList<>();

        /** No-arg constructor required by LibGDX Json. */
        public SaveData() {}
    }

    /** Save game state to a named slot. */
    public void save(GameState state, Room.RoomID currentRoom, String slotName) {
        SaveData data = new SaveData();

        data.currentRoom = currentRoom.name();
        data.awareness = state.getAwareness();
        data.gameOver = state.isGameOver();
        data.gameWon = state.isGameWon();
        data.accusationMade = state.isAccusationMade();
        data.climaxTriggered = state.isClimaxTriggered();
        data.commandCount = state.getCommandCount();
        data.interviewCount = state.getInterviewCount();
        data.evidenceShownCount = state.getEvidenceShownCount();

        for (Evidence e : state.getCollectedEvidence()) {
            data.collectedEvidence.add(e.name());
        }
        for (Tape t : state.getCollectedTapes()) {
            data.collectedTapes.add(t.name());
        }
        for (Tape t : state.getWatchedTapes()) {
            data.watchedTapes.add(t.name());
        }
        for (Contradiction c : state.getDiscoveredContradictions()) {
            data.discoveredContradictions.add(c.name());
        }

        for (Map.Entry<Suspect, Integer> entry : state.getCooperationMap().entrySet()) {
            data.cooperation.put(entry.getKey().name(), entry.getValue());
        }
        for (Map.Entry<Room.RoomID, Integer> entry : state.getVisitCountMap().entrySet()) {
            data.visitCounts.put(entry.getKey().name(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : state.getExamCountMap().entrySet()) {
            data.examCounts.put(entry.getKey(), entry.getValue());
        }
        data.askedTopics.addAll(state.getAskedTopics());

        data.narratorHeaderShown = state.isNarratorHeaderShown();
        data.eventLog.addAll(state.getEventLog());
        for (Suspect s : state.getConfrontedSuspects()) {
            data.confrontedSuspects.add(s.name());
        }

        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        String jsonStr = json.prettyPrint(data);

        FileHandle dir = Gdx.files.local(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileHandle file = Gdx.files.local(SAVE_DIR + slotName + ".json");
        file.writeString(jsonStr, false);
    }

    /**
     * Load game state from a named slot.
     * Returns the RoomID to navigate to, or null if load failed.
     */
    public Room.RoomID load(GameState state, String slotName) {
        FileHandle file = Gdx.files.local(SAVE_DIR + slotName + ".json");
        if (!file.exists()) return null;

        try {
            Json json = new Json();
            SaveData data = json.fromJson(SaveData.class, file.readString());

            state.clearAllState();

            state.setAwareness(data.awareness);
            state.setGameOver(data.gameOver);
            state.setGameWon(data.gameWon);
            state.setAccusationMade(data.accusationMade);
            state.setClimaxTriggered(data.climaxTriggered);
            state.setCommandCount(data.commandCount);
            state.setInterviewCount(data.interviewCount);
            state.setEvidenceShownCount(data.evidenceShownCount);

            for (String name : data.collectedEvidence) {
                state.forceCollectEvidence(Evidence.valueOf(name));
            }
            for (String name : data.collectedTapes) {
                state.forceCollectTape(Tape.valueOf(name));
            }
            for (String name : data.watchedTapes) {
                state.forceWatchTape(Tape.valueOf(name));
            }
            for (String name : data.discoveredContradictions) {
                state.forceDiscoverContradiction(Contradiction.valueOf(name));
            }

            for (Map.Entry<String, Integer> entry : data.cooperation.entrySet()) {
                state.forceSetCooperation(Suspect.valueOf(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<String, Integer> entry : data.visitCounts.entrySet()) {
                state.forceSetVisitCount(Room.RoomID.valueOf(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<String, Integer> entry : data.examCounts.entrySet()) {
                state.forceSetExamCount(entry.getKey(), entry.getValue());
            }
            for (String topic : data.askedTopics) {
                state.forceAddAskedTopic(topic);
            }

            state.setNarratorHeaderShown(data.narratorHeaderShown);
            if (data.eventLog != null) {
                for (String event : data.eventLog) {
                    state.forceAddEvent(event);
                }
            }
            if (data.confrontedSuspects != null) {
                for (String name : data.confrontedSuspects) {
                    state.forceMarkConfronted(Suspect.valueOf(name));
                }
            }

            return Room.RoomID.valueOf(data.currentRoom);
        } catch (Exception e) {
            Gdx.app.error("SaveLoad", "Failed to load save: " + slotName, e);
            return null;
        }
    }

    /** List available save slot names (without .json extension). */
    public List<String> listSaves() {
        List<String> saves = new ArrayList<>();
        FileHandle dir = Gdx.files.local(SAVE_DIR);
        if (dir.exists()) {
            for (FileHandle file : dir.list(".json")) {
                String name = file.nameWithoutExtension();
                saves.add(name);
            }
        }
        Collections.sort(saves);
        return saves;
    }

    /** Delete a save file. Returns true if deleted. */
    public boolean deleteSave(String slotName) {
        FileHandle file = Gdx.files.local(SAVE_DIR + slotName + ".json");
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    /** Check if a save slot exists. */
    public boolean saveExists(String slotName) {
        return Gdx.files.local(SAVE_DIR + slotName + ".json").exists();
    }
}
