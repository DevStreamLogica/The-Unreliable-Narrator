package com.dsa.game.state;

import com.dsa.game.navigation.Room;

public enum Tape {
    TAPE_ARGUMENT("Harold & James Argument", Room.RoomID.STUDY, "under_desk"),
    TAPE_MARGARET_INTERVIEW("Margaret Vance - Police Interview", Room.RoomID.KITCHEN, "kitchen_floor"),
    TAPE_MARCUS_INTERVIEW("Marcus Blackwood - Police Interview", Room.RoomID.PARLOR, "briefcase"),
    TAPE_CHARLES_INTERVIEW("Charles Webb - Police Interview", Room.RoomID.PARLOR, "grandfather_clock"),
    TAPE_JAMES_INTERVIEW("James Vance - Police Interview", Room.RoomID.STUDY, "bookshelves"),
    TAPE_DANIEL_INTERVIEW("Daniel Hobbs - Police Interview", Room.RoomID.GROUNDSKEEPER_SHED, "logbook"),
    TAPE_MARGARET_ACCOUNT("Margaret's Personal Account", Room.RoomID.MARGARET_ROOM, "dresser"),
    TAPE_ARTHUR_DEATH("The Opening", Room.RoomID.CELLAR, "wine_rack");

    private final String title;
    private final Room.RoomID hiddenInRoom;
    private final String hiddenInObject;

    Tape(String title, Room.RoomID hiddenInRoom, String hiddenInObject) {
        this.title = title;
        this.hiddenInRoom = hiddenInRoom;
        this.hiddenInObject = hiddenInObject;
    }

    public String getTitle() { return title; }
    public Room.RoomID getHiddenInRoom() { return hiddenInRoom; }
    public String getHiddenInObject() { return hiddenInObject; }
}
