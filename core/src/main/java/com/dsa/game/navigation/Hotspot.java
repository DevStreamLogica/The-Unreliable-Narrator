package com.dsa.game.navigation;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Hotspot represents a clickable area on screen that triggers navigation.
 */
public class Hotspot {

    public enum HotspotType {
        ARROW_LEFT,      // Turn left
        ARROW_RIGHT,     // Turn right
        ARROW_FORWARD,   // Move forward
        ARROW_BACK,      // Move back
        DOOR,            // Enter doorway
        STAIRS_UP,       // Go upstairs
        STAIRS_DOWN,     // Go downstairs
        EXAMINE          // Examinable object
    }

    private HotspotType type;
    private Direction direction;
    private Room.RoomID targetRoom;
    private Rectangle bounds;        // Click area
    private Vector2 screenPosition;  // Where to render on screen
    private String tooltip;          // Hover text
    private boolean isHovered;
    private String objectName;       // For EXAMINE hotspots

    public Hotspot(HotspotType type, Direction direction, Room.RoomID targetRoom) {
        this.type = type;
        this.direction = direction;
        this.targetRoom = targetRoom;
        this.bounds = new Rectangle();
        this.screenPosition = new Vector2();
        this.tooltip = direction.getActionText();
        this.isHovered = false;
    }

    /** Constructor for EXAMINE hotspots. */
    public Hotspot(String objectName, String tooltip, float x, float y, float width, float height) {
        this.type = HotspotType.EXAMINE;
        this.direction = null;
        this.targetRoom = null;
        this.objectName = objectName;
        this.bounds = new Rectangle(x, y, width, height);
        this.screenPosition = new Vector2(x, y);
        this.tooltip = tooltip;
        this.isHovered = false;
    }

    // Set the position and size of the clickable area
    public void setBounds(float x, float y, float width, float height) {
        this.bounds.set(x, y, width, height);
        this.screenPosition.set(x, y);
    }

    // Check if a point (click) is inside this hotspot
    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    // Check if a point is inside (for hover effects)
    public void checkHover(float x, float y) {
        isHovered = bounds.contains(x, y);
    }

    // Getters
    public HotspotType getType() { return type; }
    public Direction getDirection() { return direction; }
    public Room.RoomID getTargetRoom() { return targetRoom; }
    public Rectangle getBounds() { return bounds; }
    public Vector2 getScreenPosition() { return screenPosition; }
    public String getTooltip() { return tooltip; }
    public boolean isHovered() { return isHovered; }

    public void setTooltip(String tooltip) { this.tooltip = tooltip; }
    public String getObjectName() { return objectName; }
}
