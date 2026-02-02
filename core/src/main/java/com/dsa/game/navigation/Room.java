package com.dsa.game.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Room represents a location in the game.
 * Maps to the Location enum from the original text game.
 */
public class Room {

    public enum RoomID {
        ENTRANCE,
        STUDY,
        PARLOR,
        KITCHEN,
        GUEST_ROOMS,
        GROUNDSKEEPER_SHED,
        SERVANTS_QUARTERS,
        CELLAR  // Note: CELLAR exists in navigation guide but not in original Location enum
    }

    private RoomID id;
    private String name;
    private String description;
    private String backgroundTexturePath;
    private List<Hotspot> hotspots;
    private Map<Direction, RoomID> connections;

    public Room(RoomID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.backgroundTexturePath = "rooms/" + id.name().toLowerCase() + ".png";
        this.hotspots = new ArrayList<>();
        this.connections = new HashMap<>();
    }

    // Add a connection to another room
    public void addConnection(Direction direction, RoomID targetRoom) {
        connections.put(direction, targetRoom);
    }

    // Add a clickable hotspot
    public void addHotspot(Hotspot hotspot) {
        hotspots.add(hotspot);
    }

    // Getters
    public RoomID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBackgroundTexturePath() { return backgroundTexturePath; }
    public List<Hotspot> getHotspots() { return hotspots; }
    public Map<Direction, RoomID> getConnections() { return connections; }

    // Check if room connects to another in given direction
    public boolean hasConnection(Direction direction) {
        return connections.containsKey(direction);
    }

    public RoomID getConnection(Direction direction) {
        return connections.get(direction);
    }
}
