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

                study.addHotspot(HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.WEST, Room.RoomID.ENTRANCE));

                parlor.addConnection(Direction.EAST, Room.RoomID.ENTRANCE);

                parlor.addHotspot(HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.ENTRANCE));

                kitchen.addConnection(Direction.SOUTH, Room.RoomID.ENTRANCE);
                kitchen.addConnection(Direction.WEST, Room.RoomID.SERVANTS_QUARTERS);
                kitchen.addConnection(Direction.DOWN, Room.RoomID.CELLAR);

                Hotspot kitchenBackArrow = new Hotspot(Hotspot.HotspotType.ARROW_BACK, Direction.SOUTH,
                                Room.RoomID.ENTRANCE);
                kitchenBackArrow.setBounds(600, 60, 80, 80);
                kitchen.addHotspot(kitchenBackArrow);

                Hotspot servantsArrow = new Hotspot(Hotspot.HotspotType.ARROW_LEFT, Direction.WEST,
                                Room.RoomID.SERVANTS_QUARTERS);
                servantsArrow.setBounds(190, 110, 110, 320);
                kitchen.addHotspot(servantsArrow);

                Hotspot cellarDoor = new Hotspot(Hotspot.HotspotType.STAIRS_DOWN, Direction.DOWN, Room.RoomID.CELLAR);
                cellarDoor.setBounds(600, 200, 105, 200);
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

                margaretRoom.addConnection(Direction.EAST, Room.RoomID.GUEST_ROOMS);
                Hotspot margaretExit = new Hotspot(Hotspot.HotspotType.DOOR, Direction.EAST, Room.RoomID.GUEST_ROOMS);
                margaretExit.setBounds(1080, 200, 150, 300);
                margaretRoom.addHotspot(margaretExit);

                shed.addConnection(Direction.EAST, Room.RoomID.SERVANTS_QUARTERS);

                shed.addHotspot(HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.SERVANTS_QUARTERS));

                servants.addConnection(Direction.EAST, Room.RoomID.KITCHEN);
                servants.addConnection(Direction.WEST, Room.RoomID.GROUNDSKEEPER_SHED);

                servants.addHotspot(HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.KITCHEN));
                servants.addHotspot(HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.DOOR, Direction.WEST, Room.RoomID.GROUNDSKEEPER_SHED));

                cellar.addConnection(Direction.UP, Room.RoomID.KITCHEN);

                cellar.addHotspot(HotspotPositions.createStandardHotspot(
                                Hotspot.HotspotType.STAIRS_UP, Direction.UP, Room.RoomID.KITCHEN));
        }

        private void setupExamineHotspots() {
                Room study = rooms.get(Room.RoomID.STUDY);
                study.addHotspot(new Hotspot("desk", "Examine: Harold's Desk", 490, 570, 300, 100));
                study.addHotspot(new Hotspot("drawers", "Examine: Desk Drawers", 520, 640, 100, 50));
                study.addHotspot(new Hotspot("papers", "Examine: Papers", 580, 600, 100, 40));
                study.addHotspot(new Hotspot("bookshelves", "Examine: Bookshelves", 920, 240, 80, 230));
                study.addHotspot(new Hotspot("window", "Examine: Window", 540, 290, 200, 150));
                study.addHotspot(new Hotspot("fireplace", "Examine: Fireplace", 220, 240, 100, 330));
                study.addHotspot(new Hotspot("ashes", "Examine: Fireplace Ashes", 240, 520, 60, 40));
                study.addHotspot(new Hotspot("poker", "Examine: Fireplace Poker", 305, 470, 40, 100));
                study.addHotspot(new Hotspot("under_desk", "Examine: Under the Desk", 540, 670, 100, 40));

                Room parlor = rooms.get(Room.RoomID.PARLOR);
                parlor.addHotspot(new Hotspot("grandfather_clock", "Examine: Grandfather Clock", 1000, 250, 60, 200));
                parlor.addHotspot(new Hotspot("briefcase", "Examine: Briefcase", 420, 340, 80, 50));
                parlor.addHotspot(new Hotspot("fireplace", "Examine: Fireplace", 580, 240, 120, 330));

                Room kitchen = rooms.get(Room.RoomID.KITCHEN);
                kitchen.addHotspot(new Hotspot("storage_cellar", "Examine: Storage Cellar Door", 600, 200, 105, 200));
                kitchen.addHotspot(new Hotspot("flour_tin", "Examine: Flour Tin", 1030, 190, 90, 115));

                Room jamesRoom = rooms.get(Room.RoomID.JAMES_ROOM);
                jamesRoom.addHotspot(new Hotspot("coat", "Examine: James's Coat", 750, 330, 60, 30));
                jamesRoom.addHotspot(new Hotspot("wardrobe", "Examine: Wardrobe", 700, 200, 150, 250));

                Room margaretRoom = rooms.get(Room.RoomID.MARGARET_ROOM);
                margaretRoom.addHotspot(new Hotspot("letter", "Examine: Letter on Dresser", 350, 330, 60, 30));
                margaretRoom.addHotspot(new Hotspot("dresser", "Examine: Dresser", 300, 250, 200, 150));

                Room shed = rooms.get(Room.RoomID.GROUNDSKEEPER_SHED);
                shed.addHotspot(new Hotspot("logbook", "Examine: Groundskeeper's Logbook", 540, 340, 100, 50));
                shed.addHotspot(new Hotspot("shelf", "Examine: Shelf", 640, 340, 100, 50));

                Room servants = rooms.get(Room.RoomID.SERVANTS_QUARTERS);
                servants.addHotspot(new Hotspot("bedpost", "Examine: Wooden Bedpost", 350, 320, 60, 120));
                servants.addHotspot(new Hotspot("floorboard", "Examine: Loose Floorboard", 520, 330, 100, 40));

                Room cellar = rooms.get(Room.RoomID.CELLAR);
                cellar.addHotspot(new Hotspot("flour_sacks", "Examine: Flour Sacks", 540, 370, 60, 50));
                cellar.addHotspot(new Hotspot("wine_rack", "Examine: Wine Rack", 290, 240, 60, 230));
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
