package com.dsa.game.navigation;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Hotspot {

    public enum HotspotType {
        ARROW_LEFT,
        ARROW_RIGHT,
        ARROW_FORWARD,
        ARROW_BACK,
        DOOR,
        STAIRS_UP,
        STAIRS_DOWN,
        EXAMINE
    }

    private HotspotType type;
    private Direction direction;
    private Room.RoomID targetRoom;
    private Rectangle bounds;
    private Vector2 screenPosition;
    private String tooltip;
    private boolean isHovered;
    private String objectName;

    public Hotspot(HotspotType type, Direction direction, Room.RoomID targetRoom) {
        this.type = type;
        this.direction = direction;
        this.targetRoom = targetRoom;
        this.bounds = new Rectangle();
        this.screenPosition = new Vector2();
        this.tooltip = direction.getActionText();
        this.isHovered = false;
    }

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

    public void setBounds(float x, float y, float width, float height) {
        this.bounds.set(x, y, width, height);
        this.screenPosition.set(x, y);
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    public void checkHover(float x, float y) {
        isHovered = bounds.contains(x, y);
    }

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
