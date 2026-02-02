package com.dsa.game.navigation;

import com.dsa.game.data.RoomDescriptions;

import java.util.HashMap;
import java.util.Map;

/**
 * RoomManager manages all rooms and navigation in the game.
 * Based on the locations from the original DSA text game.
 */
public class RoomManager {

    private Map<Room.RoomID, Room> rooms;
    private Room currentRoom;

    public RoomManager() {
        rooms = new HashMap<>();
        initializeRooms();
    }

    private void initializeRooms() {
        // Create all rooms based on Location enum from text game
        createRoom(Room.RoomID.ENTRANCE, "Entrance Hall",
                "The main entrance of Vance Manor. Dust motes float in dim light.");

        createRoom(Room.RoomID.STUDY, "The Study",
                "The crime scene. Harold's desk faces large windows. Fireplace on the north wall. Bookshelves line the east wall.");

        createRoom(Room.RoomID.PARLOR, "The Parlor",
                "Where guests gathered after dinner. Comfortable chairs. A fireplace. A grandfather clock in the corner.");

        createRoom(Room.RoomID.KITCHEN, "The Kitchen",
                "Large, industrial. A narrow door leads to the storage cellar.");

        createRoom(Room.RoomID.GUEST_ROOMS, "Guest Rooms",
                "The guest wing. Margaret's room and James's room are here.");

        createRoom(Room.RoomID.GROUNDSKEEPER_SHED, "Groundskeeper's Shed",
                "Small building on the property edge. A logbook sits on the desk.");

        createRoom(Room.RoomID.SERVANTS_QUARTERS, "Servants' Quarters",
                "Staff area. A narrow door leads to the servants' staircase.");

        createRoom(Room.RoomID.CELLAR, "The Cellar",
                "Dark storage area beneath the kitchen. Flour sacks and wine racks.");

        // Set up connections based on the navigation guide
        setupConnections();

        // Set up examine hotspots for objects
        setupExamineHotspots();

        // Start in entrance (matching text game)
        currentRoom = rooms.get(Room.RoomID.ENTRANCE);
    }

    private void createRoom(Room.RoomID id, String name, String description) {
        rooms.put(id, new Room(id, name, description));
    }

    private void setupConnections() {
        Room entrance = rooms.get(Room.RoomID.ENTRANCE);
        Room study = rooms.get(Room.RoomID.STUDY);
        Room parlor = rooms.get(Room.RoomID.PARLOR);
        Room kitchen = rooms.get(Room.RoomID.KITCHEN);
        Room guestRooms = rooms.get(Room.RoomID.GUEST_ROOMS);
        Room shed = rooms.get(Room.RoomID.GROUNDSKEEPER_SHED);
        Room servants = rooms.get(Room.RoomID.SERVANTS_QUARTERS);
        Room cellar = rooms.get(Room.RoomID.CELLAR);

        // ENTRANCE connections
        // From text game: can go to STUDY, PARLOR, KITCHEN, GUEST_ROOMS
        entrance.addConnection(Direction.EAST, Room.RoomID.STUDY);
        entrance.addConnection(Direction.WEST, Room.RoomID.PARLOR);
        entrance.addConnection(Direction.NORTH, Room.RoomID.KITCHEN);
        entrance.addConnection(Direction.UP, Room.RoomID.GUEST_ROOMS);

        // Entrance hotspots — custom positions matching artwork
        Hotspot parlorDoor = new Hotspot(Hotspot.HotspotType.DOOR, Direction.WEST, Room.RoomID.PARLOR);
        parlorDoor.setBounds(300, 140, 30, 190);
        entrance.addHotspot(parlorDoor);

        Hotspot studyDoor = new Hotspot(Hotspot.HotspotType.DOOR, Direction.EAST, Room.RoomID.STUDY);
        studyDoor.setBounds(990, 145, 30, 180);
        entrance.addHotspot(studyDoor);

        Hotspot kitchenPassage = new Hotspot(Hotspot.HotspotType.DOOR, Direction.NORTH, Room.RoomID.KITCHEN);
        kitchenPassage.setBounds(490, 140, 90, 190);
        entrance.addHotspot(kitchenPassage);

        Hotspot staircase = new Hotspot(Hotspot.HotspotType.STAIRS_UP, Direction.UP, Room.RoomID.GUEST_ROOMS);
        staircase.setBounds(490, 440, 90, 150);
        entrance.addHotspot(staircase);

        // STUDY connections
        // From text game: can go back to ENTRANCE, and to PARLOR
        study.addConnection(Direction.WEST, Room.RoomID.ENTRANCE);
        study.addConnection(Direction.NORTH, Room.RoomID.PARLOR);

        study.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.ARROW_BACK, Direction.WEST, Room.RoomID.ENTRANCE));
        study.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.DOOR, Direction.NORTH, Room.RoomID.PARLOR));

        // PARLOR connections
        // From text game: can go to ENTRANCE, STUDY, SERVANTS_QUARTERS
        parlor.addConnection(Direction.EAST, Room.RoomID.ENTRANCE);
        parlor.addConnection(Direction.SOUTH, Room.RoomID.STUDY);
        parlor.addConnection(Direction.NORTH, Room.RoomID.SERVANTS_QUARTERS);

        parlor.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.ARROW_RIGHT, Direction.EAST, Room.RoomID.ENTRANCE));
        parlor.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.DOOR, Direction.SOUTH, Room.RoomID.STUDY));
        parlor.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.ARROW_FORWARD, Direction.NORTH, Room.RoomID.SERVANTS_QUARTERS));

        // KITCHEN connections
        // From text game: can go back to ENTRANCE, to SERVANTS_QUARTERS, and down to
        // CELLAR
        kitchen.addConnection(Direction.SOUTH, Room.RoomID.ENTRANCE);
        kitchen.addConnection(Direction.WEST, Room.RoomID.SERVANTS_QUARTERS);
        kitchen.addConnection(Direction.DOWN, Room.RoomID.CELLAR);

        kitchen.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.ARROW_BACK, Direction.SOUTH, Room.RoomID.ENTRANCE));
        kitchen.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.ARROW_LEFT, Direction.WEST, Room.RoomID.SERVANTS_QUARTERS));
        kitchen.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.STAIRS_DOWN, Direction.DOWN, Room.RoomID.CELLAR));

        // GUEST ROOMS connections
        // From text game: can go back down to ENTRANCE
        guestRooms.addConnection(Direction.DOWN, Room.RoomID.ENTRANCE);

        guestRooms.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.STAIRS_DOWN, Direction.DOWN, Room.RoomID.ENTRANCE));

        // GROUNDSKEEPER SHED connections
        // From text game: exterior building, can go back to ENTRANCE area
        shed.addConnection(Direction.SOUTH, Room.RoomID.ENTRANCE);

        shed.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.ARROW_BACK, Direction.SOUTH, Room.RoomID.ENTRANCE));

        // SERVANTS' QUARTERS connections
        // From text game: can go back to PARLOR, and to KITCHEN
        servants.addConnection(Direction.SOUTH, Room.RoomID.PARLOR);
        servants.addConnection(Direction.EAST, Room.RoomID.KITCHEN);

        servants.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.ARROW_BACK, Direction.SOUTH, Room.RoomID.PARLOR));
        servants.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.ARROW_RIGHT, Direction.EAST, Room.RoomID.KITCHEN));

        // CELLAR connections
        // From text game: can go back up to KITCHEN
        cellar.addConnection(Direction.UP, Room.RoomID.KITCHEN);

        cellar.addHotspot(HotspotPositions.createStandardHotspot(
                Hotspot.HotspotType.STAIRS_UP, Direction.UP, Room.RoomID.KITCHEN));
    }

    private void setupExamineHotspots() {
        // Pixmap y=0 is top of screen. LibGDX screen y=0 is bottom.
        // Conversion: screenY = 720 - pixmapY - height
        Room study = rooms.get(Room.RoomID.STUDY);
        addExamine(study, "desk", 490, 50, 300, 100);
        addExamine(study, "drawers", 520, 30, 100, 50);
        addExamine(study, "papers", 580, 80, 100, 40);
        addExamine(study, "bookshelves", 920, 250, 80, 230);
        addExamine(study, "window", 540, 280, 200, 150);
        addExamine(study, "fireplace", 220, 150, 100, 330);
        addExamine(study, "ashes", 240, 160, 60, 40);
        addExamine(study, "under_desk", 540, 10, 100, 40);

        Room parlor = rooms.get(Room.RoomID.PARLOR);
        addExamine(parlor, "grandfather_clock", 1000, 270, 60, 200);
        addExamine(parlor, "briefcase", 420, 330, 80, 50);
        addExamine(parlor, "fireplace", 580, 150, 120, 330);

        Room kitchen = rooms.get(Room.RoomID.KITCHEN);
        addExamine(kitchen, "storage_cellar", 580, 250, 150, 80);
        addExamine(kitchen, "flour_tin", 300, 250, 60, 50);

        Room guestRooms = rooms.get(Room.RoomID.GUEST_ROOMS);
        addExamine(guestRooms, "margarets_room", 280, 330, 200, 120);
        addExamine(guestRooms, "james_room", 710, 330, 200, 120);
        addExamine(guestRooms, "letter", 350, 360, 60, 30);
        addExamine(guestRooms, "coat", 750, 360, 60, 30);

        Room shed = rooms.get(Room.RoomID.GROUNDSKEEPER_SHED);
        addExamine(shed, "logbook", 540, 330, 100, 50);
        addExamine(shed, "shelf", 640, 330, 100, 50);

        Room servants = rooms.get(Room.RoomID.SERVANTS_QUARTERS);
        addExamine(servants, "staircase", 350, 300, 80, 80);
        addExamine(servants, "floorboard", 520, 350, 100, 40);

        Room cellar = rooms.get(Room.RoomID.CELLAR);
        addExamine(cellar, "flour_tin", 540, 300, 60, 50);
        addExamine(cellar, "wine_rack", 290, 250, 60, 230);
    }

    /** Add an examine hotspot, converting Pixmap coords to screen coords. */
    private void addExamine(Room room, String objectId, int pixX, int pixY, int w, int h) {
        float screenY = 720 - pixY - h;
        String displayName = RoomDescriptions.getObjectDisplayName(objectId);
        room.addHotspot(new Hotspot(objectId, "Examine: " + displayName, pixX, screenY, w, h));
    }

    // Navigate to a new room
    public boolean navigateTo(Room.RoomID targetRoom) {
        if (rooms.containsKey(targetRoom)) {
            currentRoom = rooms.get(targetRoom);
            return true;
        }
        return false;
    }

    // Navigate via direction from current room
    public boolean navigateDirection(Direction direction) {
        if (currentRoom.hasConnection(direction)) {
            Room.RoomID target = currentRoom.getConnection(direction);
            return navigateTo(target);
        }
        return false;
    }

    // Getters
    public Room getCurrentRoom() {
        return currentRoom;
    }

    public Room getRoom(Room.RoomID id) {
        return rooms.get(id);
    }
}
