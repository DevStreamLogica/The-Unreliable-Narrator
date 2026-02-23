package com.dsa.game.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {

    public enum RoomID {
        ENTRANCE,
        STUDY,
        PARLOR,
        KITCHEN,
        GUEST_ROOMS,
        JAMES_ROOM,
        MARGARET_ROOM,
        GROUNDSKEEPER_SHED,
        SERVANTS_QUARTERS,
        CELLAR
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

    public void addConnection(Direction direction, RoomID targetRoom) {
        connections.put(direction, targetRoom);
    }

    public void addHotspot(Hotspot hotspot) {
        hotspots.add(hotspot);
    }

    public RoomID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBackgroundTexturePath() { return backgroundTexturePath; }
    public List<Hotspot> getHotspots() { return hotspots; }
    public Map<Direction, RoomID> getConnections() { return connections; }

    public boolean hasConnection(Direction direction) {
        return connections.containsKey(direction);
    }

    public RoomID getConnection(Direction direction) {
        return connections.get(direction);
    }
}
