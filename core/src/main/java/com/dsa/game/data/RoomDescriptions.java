package com.dsa.game.data;

import com.dsa.game.navigation.Room;
import com.dsa.game.state.GameState;

import java.util.*;

public class RoomDescriptions {

    private static final Map<Room.RoomID, List<String>> EXAMINABLE_OBJECTS = new EnumMap<>(Room.RoomID.class);

    static {
        EXAMINABLE_OBJECTS.put(Room.RoomID.STUDY, Arrays.asList(
            "desk", "drawers", "papers", "bookshelves", "window", "fireplace", "ashes", "under_desk"
        ));
        EXAMINABLE_OBJECTS.put(Room.RoomID.PARLOR, Arrays.asList(
            "grandfather_clock", "briefcase", "fireplace"
        ));
        EXAMINABLE_OBJECTS.put(Room.RoomID.KITCHEN, Arrays.asList(
            "storage_cellar", "flour_tin", "kitchen_floor"
        ));
        EXAMINABLE_OBJECTS.put(Room.RoomID.GUEST_ROOMS, Collections.emptyList());
        EXAMINABLE_OBJECTS.put(Room.RoomID.JAMES_ROOM, Arrays.asList(
            "coat", "wardrobe"
        ));
        EXAMINABLE_OBJECTS.put(Room.RoomID.MARGARET_ROOM, Arrays.asList(
            "lamp", "tape_recorder", "top_drawer", "bottom_drawer"
        ));
        EXAMINABLE_OBJECTS.put(Room.RoomID.GROUNDSKEEPER_SHED, Arrays.asList(
            "logbook", "shelf"
        ));
        EXAMINABLE_OBJECTS.put(Room.RoomID.SERVANTS_QUARTERS, Arrays.asList(
            "bedpost", "drawer"
        ));
        EXAMINABLE_OBJECTS.put(Room.RoomID.CELLAR, Arrays.asList(
            "flour_sacks", "wine_rack"
        ));
        EXAMINABLE_OBJECTS.put(Room.RoomID.ENTRANCE, Collections.emptyList());
    }

    public static List<String> getExaminableObjects(Room.RoomID room) {
        return EXAMINABLE_OBJECTS.getOrDefault(room, Collections.emptyList());
    }

    public static String getDescription(Room.RoomID room, int visitCount, int awareness) {
        switch (room) {
            case ENTRANCE:
                if (visitCount <= 1) return "The main entrance of Vance Manor. Dust motes float in dim light from a grand chandelier. Doors lead in every direction.";
                if (awareness >= 60) return "The entrance feels hostile now. Someone has been watching from the upstairs landing.";
                return "The familiar entrance hall. The chandelier flickers occasionally.";

            case STUDY:
                if (visitCount <= 1) return "The crime scene. Harold's desk faces large windows. A fireplace on the north wall still has warm ashes. Bookshelves line the east wall.";
                if (awareness >= 40) return "The study feels different. Papers on the desk have been rearranged since your last visit. Someone has been here.";
                return "Harold's study. The crime scene. Every surface could hold a clue.";

            case PARLOR:
                if (visitCount <= 1) return "Where guests gathered after dinner last night. Comfortable chairs face a dying fireplace. A grandfather clock ticks loudly in the corner. A briefcase sits by one of the chairs.";
                if (awareness >= 40) return "The parlor is colder now. The fire has gone out completely. The briefcase has been moved.";
                return "The parlor. The grandfather clock continues its steady ticking.";

            case KITCHEN:
                if (visitCount <= 1) return "Large and industrial. Copper pots hang from racks. A narrow door leads to the storage cellar below. Flour and spices line the shelves.";
                if (awareness >= 60) return "The kitchen has been cleaned -- suspiciously clean. Someone scrubbed the counters recently.";
                return "The kitchen. The smell of last night's dinner lingers.";

            case GUEST_ROOMS:
                if (visitCount <= 1) return "The guest wing upstairs. James's room is to the right, door slightly ajar. Margaret's room is to the left -- the door is cold to the touch and refuses to open.";
                if (awareness >= 40) return "The guest wing. The doors to both rooms stand open, waiting.";
                return "The guest wing. Margaret's and James's rooms are accessible from here.";

            case JAMES_ROOM:
                if (visitCount <= 1) return "James's room is messier. The bed is unmade, an ashtray overflows with cigarette stubs. A glass of whisky, half-finished, sits on the nightstand. The wardrobe door hangs open.";
                if (awareness >= 40) return "James's room feels tense. Someone was here recently.";
                return "James's room. Messy and lived-in. The stale smell of cigarettes.";

            case MARGARET_ROOM:
                if (visitCount <= 1) return "Margaret's room is tidy but tense. A suitcase sits half-packed on the bed. A dresser with a letter sits against the wall.";
                if (awareness >= 40) return "Margaret's room. The half-packed suitcase suggests she was planning to leave.";
                return "Margaret's room. Neat and organized, unlike James's.";

            case GROUNDSKEEPER_SHED:
                if (visitCount <= 1) return "A small shed accessible from the servants' quarters. Tools hang on the walls. Daniel's logbook sits open on a rough wooden desk. A shelf holds various supplies.";
                if (awareness >= 40) return "The shed seems recently disturbed. Tools have been moved, and the logbook is now closed.";
                return "Daniel's shed. It smells of earth and oil.";

            case SERVANTS_QUARTERS:
                if (visitCount <= 1) return "The staff area. Simple but clean. Bunk beds line the walls. A small nightstand sits between them, a blue pouch resting on top.";
                if (awareness >= 60) return "The servants' quarters feel abandoned. The staff have been avoiding this area.";
                return "The servants' quarters. Modest and orderly.";

            case CELLAR:
                if (visitCount <= 1) return "Dark and cold beneath the kitchen. Wine racks line the walls. Flour sacks are stacked in one corner. The air is damp and heavy.";
                if (awareness >= 40) return "The cellar feels different. Fresh scratch marks on the floor. Something was dragged here recently.";
                return "The cellar. Dark and musty. Wine racks and storage.";

            default:
                return "An unfamiliar room.";
        }
    }

    public static String getObjectDisplayName(String objectId) {
        switch (objectId) {
            case "desk": return "Harold's Desk";
            case "drawers": return "Desk Drawers";
            case "papers": return "Papers";
            case "bookshelves": return "Bookshelves";
            case "window": return "Window";
            case "fireplace": return "Fireplace";
            case "ashes": return "Fireplace Ashes";
            case "under_desk": return "Under the Desk";
            case "grandfather_clock": return "Grandfather Clock";
            case "briefcase": return "Briefcase";
            case "storage_cellar": return "Storage Cellar Door";
            case "kitchen_floor": return "Under the Counter";
            case "flour_tin": return "Flour Tin";
            case "flour_sacks": return "Flour Sacks";
            case "margarets_room": return "Margaret's Room";
            case "james_room": return "James's Room";
            case "lamp": return "Bedside Lamp";
            case "tape_recorder": return "Tape Recorder";
            case "top_drawer": return "Top Drawer";
            case "bottom_drawer": return "Bottom Drawer";
            case "coat": return "James's Coat";
            case "wardrobe": return "Wardrobe";
            case "dresser": return "Dresser";
            case "logbook": return "Groundskeeper's Logbook";
            case "shelf": return "Shelf";
            case "kit": return "Tape Repair Kit";
            case "shoes": return "Stained Shoes";
            case "bedpost": return "Wooden Bedpost";
            case "drawer": return "Nightstand";
            case "wine_rack": return "Wine Rack";
            default: return objectId;
        }
    }
}
