package com.dsa.game.state;

import com.dsa.game.navigation.Room;

public enum Tape {
    TAPE_ARGUMENT("Harold & James Argument", Room.RoomID.STUDY, "desk"),
    TAPE_PHONE_CALL("Late Night Phone Call", Room.RoomID.PARLOR, "grandfather_clock"),
    TAPE_MARGARET_CONFESSION("Margaret's Confession", Room.RoomID.GUEST_ROOMS, "margarets_room"),
    TAPE_DANIEL_MEETING("Daniel's Secret Meeting", Room.RoomID.GROUNDSKEEPER_SHED, "logbook"),
    TAPE_WILL_READING("Will Reading Preview", Room.RoomID.STUDY, "bookshelves"),
    TAPE_KITCHEN_WHISPERS("Kitchen Whispers", Room.RoomID.KITCHEN, "storage_cellar"),
    TAPE_CELLAR_NOISES("Cellar Recording", Room.RoomID.CELLAR, "wine_rack");

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
