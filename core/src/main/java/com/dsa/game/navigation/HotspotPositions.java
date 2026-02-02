package com.dsa.game.navigation;

/**
 * Utility class for standard hotspot positioning on screen.
 */
public class HotspotPositions {

    // Screen dimensions (adjust to your game)
    public static final float SCREEN_WIDTH = 1280;
    public static final float SCREEN_HEIGHT = 720;

    // Hotspot sizes
    public static final float ARROW_SIZE = 80;
    public static final float DOOR_WIDTH = 200;
    public static final float DOOR_HEIGHT = 300;

    // Standard positions (center of hotspot)
    public static final float LEFT_X = 100;
    public static final float LEFT_Y = SCREEN_HEIGHT / 2;

    public static final float RIGHT_X = SCREEN_WIDTH - 100;
    public static final float RIGHT_Y = SCREEN_HEIGHT / 2;

    public static final float FORWARD_X = SCREEN_WIDTH / 2;
    public static final float FORWARD_Y = SCREEN_HEIGHT - 150;

    public static final float BACK_X = SCREEN_WIDTH / 2;
    public static final float BACK_Y = 100;

    public static final float DOOR_CENTER_X = SCREEN_WIDTH / 2;
    public static final float DOOR_CENTER_Y = SCREEN_HEIGHT / 2;

    // Create a hotspot with standard positioning
    public static Hotspot createStandardHotspot(
            Hotspot.HotspotType type,
            Direction direction,
            Room.RoomID target) {

        Hotspot hotspot = new Hotspot(type, direction, target);

        float x, y, width, height;

        switch (type) {
            case ARROW_LEFT:
                x = LEFT_X - ARROW_SIZE/2;
                y = LEFT_Y - ARROW_SIZE/2;
                width = ARROW_SIZE;
                height = ARROW_SIZE;
                break;

            case ARROW_RIGHT:
                x = RIGHT_X - ARROW_SIZE/2;
                y = RIGHT_Y - ARROW_SIZE/2;
                width = ARROW_SIZE;
                height = ARROW_SIZE;
                break;

            case ARROW_FORWARD:
                x = FORWARD_X - ARROW_SIZE/2;
                y = FORWARD_Y - ARROW_SIZE/2;
                width = ARROW_SIZE;
                height = ARROW_SIZE;
                break;

            case ARROW_BACK:
                x = BACK_X - ARROW_SIZE/2;
                y = BACK_Y - ARROW_SIZE/2;
                width = ARROW_SIZE;
                height = ARROW_SIZE;
                break;

            case DOOR:
                x = DOOR_CENTER_X - DOOR_WIDTH/2;
                y = DOOR_CENTER_Y - DOOR_HEIGHT/2;
                width = DOOR_WIDTH;
                height = DOOR_HEIGHT;
                break;

            case STAIRS_UP:
            case STAIRS_DOWN:
                x = DOOR_CENTER_X - DOOR_WIDTH/2;
                y = DOOR_CENTER_Y - DOOR_HEIGHT/2;
                width = DOOR_WIDTH;
                height = DOOR_HEIGHT;
                break;

            default:
                x = SCREEN_WIDTH/2;
                y = SCREEN_HEIGHT/2;
                width = ARROW_SIZE;
                height = ARROW_SIZE;
        }

        hotspot.setBounds(x, y, width, height);
        return hotspot;
    }
}
