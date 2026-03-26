package com.dsa.game.navigation;

import com.dsa.game.data.RoomDescriptions;

import java.util.HashMap;
import java.util.Map;

public class RoomManager {

        private Map<Room.RoomID, Room> rooms;
        private Room currentRoom;

        public RoomManager() {
                rooms = new HashMap<>();
                initializeRooms();
        }

        private void initializeRooms() {
                createRoom(Room.RoomID.ENTRANCE, "Entrance Hall",
                                "The main entrance of Vance Manor. Dust motes float in dim light.");

                createRoom(Room.RoomID.STUDY, "The Study",
                                "The crime scene. Harold's desk faces large windows. A fireplace on the north wall still has warm ashes. Bookshelves line the east wall.");

                createRoom(Room.RoomID.PARLOR, "The Parlor",
                                "Where guests gathered after dinner. Comfortable chairs. A fireplace. A grandfather clock in the corner.");

                createRoom(Room.RoomID.KITCHEN, "The Kitchen",
                                "Large, industrial. A narrow door leads to the storage cellar.");

                createRoom(Room.RoomID.GUEST_ROOMS, "Guest Rooms",
                                "The guest wing. Margaret's room and James's room are here.");

                createRoom(Room.RoomID.JAMES_ROOM, "James's Room",
                                "James Vance's guest room. Messy and lived-in.");

                createRoom(Room.RoomID.MARGARET_ROOM, "Margaret's Room",
                                "Margaret Vance's guest room. Tidy but tense.");

                createRoom(Room.RoomID.GROUNDSKEEPER_SHED, "Groundskeeper's Shed",
                                "Small building on the property edge. A logbook sits on the desk.");

                createRoom(Room.RoomID.SERVANTS_QUARTERS, "Servants' Quarters",
                                "Staff area. Simple but clean. A narrow bed with a worn wooden bedpost.");

                createRoom(Room.RoomID.CELLAR, "The Cellar",
                                "Dark storage area beneath the kitchen. Flour sacks and wine racks.");

                setupConnections();

                setupExamineHotspots();

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
                Room jamesRoom = rooms.get(Room.RoomID.JAMES_ROOM);
                Room margaretRoom = rooms.get(Room.RoomID.MARGARET_ROOM);
                Room shed = rooms.get(Room.RoomID.GROUNDSKEEPER_SHED);
                Room servants = rooms.get(Room.RoomID.SERVANTS_QUARTERS);
                Room cellar = rooms.get(Room.RoomID.CELLAR);

                entrance.addConnection(Direction.EAST, Room.RoomID.STUDY);
                entrance.addConnection(Direction.WEST, Room.RoomID.PARLOR);
                entrance.addConnection(Direction.NORTH, Room.RoomID.KITCHEN);
                entrance.addConnection(Direction.UP, Room.RoomID.GUEST_ROOMS);

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

                study.addConnection(Direction.WEST, Room.RoomID.ENTRANCE);

                Hotspot studyBack = HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.WEST, Room.RoomID.ENTRANCE);
                studyBack.setTooltip("Go back");
                study.addHotspot(studyBack);

                parlor.addConnection(Direction.EAST, Room.RoomID.ENTRANCE);

                Hotspot parlorBack = HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.ENTRANCE);
                parlorBack.setTooltip("Go back");
                parlor.addHotspot(parlorBack);

                kitchen.addConnection(Direction.SOUTH, Room.RoomID.ENTRANCE);
                kitchen.addConnection(Direction.WEST, Room.RoomID.SERVANTS_QUARTERS);
                kitchen.addConnection(Direction.DOWN, Room.RoomID.CELLAR);

                Hotspot kitchenBackArrow = new Hotspot(Hotspot.HotspotType.ARROW_BACK, Direction.SOUTH,
                                Room.RoomID.ENTRANCE);
                kitchenBackArrow.setBounds(HotspotPositions.BACK_BTN_X, HotspotPositions.BACK_BTN_Y,
                                HotspotPositions.BACK_BTN_W, HotspotPositions.BACK_BTN_H);
                kitchenBackArrow.setTooltip("Go back");
                kitchen.addHotspot(kitchenBackArrow);

                Hotspot servantsArrow = new Hotspot(Hotspot.HotspotType.ARROW_LEFT, Direction.WEST,
                                Room.RoomID.SERVANTS_QUARTERS);
                servantsArrow.setBounds(600, 200, 105, 200);
                kitchen.addHotspot(servantsArrow);

                Hotspot cellarDoor = new Hotspot(Hotspot.HotspotType.STAIRS_DOWN, Direction.DOWN, Room.RoomID.CELLAR);
                cellarDoor.setBounds(190, 110, 110, 320);
                kitchen.addHotspot(cellarDoor);

                guestRooms.addConnection(Direction.DOWN, Room.RoomID.ENTRANCE);
                guestRooms.addConnection(Direction.WEST, Room.RoomID.MARGARET_ROOM);
                guestRooms.addConnection(Direction.EAST, Room.RoomID.JAMES_ROOM);

                Hotspot stairsDown = new Hotspot(Hotspot.HotspotType.STAIRS_DOWN, Direction.DOWN, Room.RoomID.ENTRANCE);
                stairsDown.setBounds(610, 90, 65, 160);
                guestRooms.addHotspot(stairsDown);

                Hotspot margaretDoor = new Hotspot(Hotspot.HotspotType.DOOR, Direction.WEST, Room.RoomID.MARGARET_ROOM);
                margaretDoor.setBounds(130, 0, 190, 560);
                guestRooms.addHotspot(margaretDoor);

                Hotspot jamesDoor = new Hotspot(Hotspot.HotspotType.DOOR, Direction.EAST, Room.RoomID.JAMES_ROOM);
                jamesDoor.setBounds(960, 0, 200, 560);
                guestRooms.addHotspot(jamesDoor);

                jamesRoom.addConnection(Direction.WEST, Room.RoomID.GUEST_ROOMS);
                Hotspot jamesExit = new Hotspot(Hotspot.HotspotType.DOOR, Direction.WEST, Room.RoomID.GUEST_ROOMS);
                jamesExit.setBounds(50, 200, 150, 300);
                jamesRoom.addHotspot(jamesExit);
                Hotspot jamesBack = HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.WEST, Room.RoomID.GUEST_ROOMS);
                jamesBack.setTooltip("Go back");
                jamesRoom.addHotspot(jamesBack);

                margaretRoom.addConnection(Direction.EAST, Room.RoomID.GUEST_ROOMS);
                Hotspot margaretBack = HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.GUEST_ROOMS);
                margaretBack.setTooltip("Go back");
                margaretRoom.addHotspot(margaretBack);

                shed.addConnection(Direction.EAST, Room.RoomID.SERVANTS_QUARTERS);

                Hotspot shedBack = HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.SERVANTS_QUARTERS);
                shedBack.setTooltip("Go back");
                shed.addHotspot(shedBack);
                Hotspot shedServantsDoor = new Hotspot(Hotspot.HotspotType.DOOR, Direction.EAST,
                                Room.RoomID.SERVANTS_QUARTERS);
                shedServantsDoor.setBounds(1100, 111, 125, 429);
                shed.addHotspot(shedServantsDoor);

                servants.addConnection(Direction.EAST, Room.RoomID.KITCHEN);
                servants.addConnection(Direction.WEST, Room.RoomID.GROUNDSKEEPER_SHED);

                Hotspot servantsBack = HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.KITCHEN);
                servantsBack.setTooltip("Go back");
                servants.addHotspot(servantsBack);
                Hotspot shedDoor = new Hotspot(Hotspot.HotspotType.DOOR, Direction.WEST,
                                Room.RoomID.GROUNDSKEEPER_SHED);
                shedDoor.setBounds(55, 111, 125, 429);
                servants.addHotspot(shedDoor);
                cellar.addConnection(Direction.UP, Room.RoomID.KITCHEN);

                cellar.addHotspot(HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.UP, Room.RoomID.KITCHEN));
        }

        private void setupExamineHotspots() {
                Room study = rooms.get(Room.RoomID.STUDY);
                study.addHotspot(new Hotspot("ashes", "Examine: Fireplace Ashes", 265, 156, 60, 40));
                study.addHotspot(new Hotspot("poker", "Examine: Fireplace Poker", 583, 272, 60, 20));
                study.addHotspot(new Hotspot("papers", "Examine: Papers", 653, 270, 30, 20));
                study.addHotspot(new Hotspot("drawers", "Examine: Desk Drawers", 473, 167, 65, 70));
                study.addHotspot(new Hotspot("drawers", "Examine: Desk Drawers", 700, 161, 80, 70));
                study.addHotspot(new Hotspot("desk", "Examine: Harold's Desk", 456, 256, 330, 50));
                study.addHotspot(new Hotspot("bookshelves", "Examine: Bookshelves", 835, 225, 460, 305));
                study.addHotspot(new Hotspot("bookshelves", "Examine: Bookshelves", 950, 150, 460, 305));
                study.addHotspot(new Hotspot("bookshelves", "Examine: Bookshelves", 1065, 100, 460, 305));
                study.addHotspot(new Hotspot("bookshelves", "Examine: Bookshelves", 964, 535, 460, 300));
                study.addHotspot(new Hotspot("window", "Examine: Window", 520, 310, 70, 150));
                study.addHotspot(new Hotspot("window", "Examine: Window", 650, 310, 80, 150));
                study.addHotspot(new Hotspot("fireplace", "Examine: Fireplace", 122, 96, 140, 900));
                study.addHotspot(new Hotspot("under_desk", "Examine: Under the Desk", 606, 154, 60, 20));

                Room parlor = rooms.get(Room.RoomID.PARLOR);
                parlor.addHotspot(new Hotspot("grandfather_clock", "Examine: Grandfather Clock", 880, 200, 85, 300));
                parlor.addHotspot(new Hotspot("briefcase", "Examine: Briefcase", 565, 180, 90, 50));
                parlor.addHotspot(new Hotspot("fireplace", "Examine: Fireplace", 520, 240, 180, 280));

                Room kitchen = rooms.get(Room.RoomID.KITCHEN);
                kitchen.addHotspot(new Hotspot("storage_cellar", "Go Down to Cellar", 190, 110, 110, 320));
                kitchen.addHotspot(new Hotspot("flour_tin", "Examine: Flour Tin", 1030, 190, 90, 115));
                kitchen.addHotspot(new Hotspot("kitchen_floor", "Examine: Tape Recorder", 510, 122, 52, 28));

                Room jamesRoom = rooms.get(Room.RoomID.JAMES_ROOM);
                // coat hotspot added dynamically after wardrobe is opened
                jamesRoom.addHotspot(new Hotspot("wardrobe", "Examine: Wardrobe", 797, 292, 268, 257));

                Room margaretRoom = rooms.get(Room.RoomID.MARGARET_ROOM);
                margaretRoom.addHotspot(new Hotspot("lamp", "Examine: Bedside Lamp", 213, 348, 77, 60));
                margaretRoom.addHotspot(new Hotspot("lamp", "Examine: Bedside Lamp", 248, 279, 6, 64));
                margaretRoom.addHotspot(new Hotspot("lamp", "Examine: Bedside Lamp", 234, 270, 34, 6));
                margaretRoom.addHotspot(new Hotspot("tape_recorder", "Examine: Tape Recorder", 280, 272, 72, 56));
                margaretRoom.addHotspot(new Hotspot("top_drawer", "Examine: Top Drawer", 241, 207, 90, 49));
                margaretRoom.addHotspot(new Hotspot("bottom_drawer", "Examine: Bottom Drawer", 244, 121, 86, 85));

                Room shed = rooms.get(Room.RoomID.GROUNDSKEEPER_SHED);
                shed.addHotspot(new Hotspot("logbook", "Examine: Groundskeeper's Logbook", 982, 274, 50, 44));
                shed.addHotspot(new Hotspot("shelf", "Examine: Shelf", 620, 231, 157, 187));

                Room servants = rooms.get(Room.RoomID.SERVANTS_QUARTERS);
                servants.addHotspot(new Hotspot("bedpost", "Examine: Wooden Bedpost", 420, 176, 20, 200));
                servants.addHotspot(new Hotspot("drawer", "Examine: Nightstand", 578, 299, 36, 10));

                Room cellar = rooms.get(Room.RoomID.CELLAR);
                cellar.addHotspot(new Hotspot("cellar_shelf", "Examine: Cellar Shelf", 525, 259, 234, 106));
                cellar.addHotspot(new Hotspot("flour_sacks", "Examine: Flour Sacks", 852, 111, 138, 118));
                cellar.addHotspot(new Hotspot("wine_rack", "Examine: Wine Rack", 73, 109, 337, 448));
        }

        public boolean navigateTo(Room.RoomID targetRoom) {
                if (rooms.containsKey(targetRoom)) {
                        currentRoom = rooms.get(targetRoom);
                        return true;
                }
                return false;
        }

        public boolean navigateDirection(Direction direction) {
                if (currentRoom.hasConnection(direction)) {
                        Room.RoomID target = currentRoom.getConnection(direction);
                        return navigateTo(target);
                }
                return false;
        }

        public Room getCurrentRoom() {
                return currentRoom;
        }

        public Room getRoom(Room.RoomID id) {
                return rooms.get(id);
        }
}
