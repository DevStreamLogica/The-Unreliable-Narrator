# DSA Game - LibGDX First-Person Navigation Implementation Guide

A comprehensive guide for implementing a Google Maps-style first-person navigation system using LibGDX.

---

## Table of Contents

1. [Overview](#overview)
2. [Project Setup](#project-setup)
3. [Architecture](#architecture)
4. [Room/Scene System](#roomscene-system)
5. [Navigation Hotspots](#navigation-hotspots)
6. [Placeholder Graphics](#placeholder-graphics)
7. [Implementation Code](#implementation-code)
8. [Room Connections Map](#room-connections-map)
9. [Future Integration](#future-integration)

---

## Overview

### What We're Building

A first-person navigation system where:
- Player sees a view of their current room
- Clickable hotspots (arrows/doors) appear on screen
- Clicking a hotspot moves the player to the connected room
- Smooth transitions between rooms
- Works like Google Maps Street View

### Tech Stack

- **LibGDX** - Game framework
- **Java** - Programming language
- **Gradle** - Build system

### Visual Concept

```
+------------------------------------------------------------------+
|                                                                  |
|                     [ROOM IMAGE/VIEW]                            |
|                                                                  |
|                    +------------------+                          |
|                    |                  |                          |
|      [<- LEFT]     |    DOORWAY       |     [RIGHT ->]           |
|       HOTSPOT      |    HOTSPOT       |      HOTSPOT             |
|                    |    (CLICK TO     |                          |
|                    |     ENTER)       |                          |
|                    +------------------+                          |
|                                                                  |
|                         [v BACK]                                 |
|                          HOTSPOT                                 |
|                                                                  |
+------------------------------------------------------------------+
|  [AWARENESS: 15/80]    [LOCATION: The Study]    [INVENTORY]     |
+------------------------------------------------------------------+
```

---

## Project Setup

### 1. Create LibGDX Project

Use the LibGDX project generator or set up manually:

**Option A: LibGDX Setup Tool**
1. Download from: https://libgdx.com/wiki/start/project-generation
2. Run the setup tool
3. Configure:
   - Project Name: `DSAGame`
   - Package: `com.dsa.game`
   - Game Class: `DSAGame`
   - Destination: Your project folder
   - Sub Projects: Desktop (required), others optional

**Option B: Manual Gradle Setup**

Create `build.gradle` in project root:

```gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.0.4'
    }
}

allprojects {
    version = '1.0'
    ext {
        gdxVersion = '1.12.1'
    }

    repositories {
        mavenCentral()
    }
}

project(":desktop") {
    apply plugin: "java-library"

    dependencies {
        implementation project(":core")
        api "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
        api "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
    }
}

project(":core") {
    apply plugin: "java-library"

    dependencies {
        api "com.badlogicgames.gdx:gdx:$gdxVersion"
    }
}
```

### 2. Project Structure

```
DSAGame-LibGDX/
├── core/
│   └── src/
│       └── com/dsa/game/
│           ├── DSAGame.java           # Main game class
│           ├── screens/
│           │   ├── GameScreen.java    # Main gameplay screen
│           │   └── MenuScreen.java    # Title/menu screen
│           ├── navigation/
│           │   ├── Room.java          # Room data class
│           │   ├── RoomManager.java   # Manages all rooms
│           │   ├── Hotspot.java       # Clickable navigation area
│           │   └── Direction.java     # Navigation directions enum
│           ├── rendering/
│           │   ├── RoomRenderer.java  # Renders current room view
│           │   └── HotspotRenderer.java # Renders navigation hotspots
│           └── assets/
│               └── AssetManager.java  # Loads/manages assets
├── desktop/
│   └── src/
│       └── com/dsa/game/
│           └── DesktopLauncher.java   # Desktop entry point
├── assets/
│   ├── rooms/                         # Room background images
│   │   ├── entrance.png
│   │   ├── study.png
│   │   ├── parlor.png
│   │   └── ...
│   ├── ui/                            # UI elements
│   │   ├── arrow_left.png
│   │   ├── arrow_right.png
│   │   ├── arrow_forward.png
│   │   ├── arrow_back.png
│   │   └── door_hotspot.png
│   └── fonts/
│       └── main.fnt
└── build.gradle
```

---

## Architecture

### Class Diagram

```
+------------------+       +------------------+
|    DSAGame       |       |   GameScreen     |
|  (Main Class)    |------>|  (Game Logic)    |
+------------------+       +--------+---------+
                                    |
                    +---------------+---------------+
                    |               |               |
            +-------v------+ +------v-------+ +----v--------+
            | RoomManager  | | RoomRenderer | | InputHandler|
            | (All Rooms)  | | (Draw Room)  | | (Clicks)    |
            +-------+------+ +--------------+ +-------------+
                    |
            +-------v------+
            |    Room      |
            | (Room Data)  |
            |  - hotspots  |
            |  - image     |
            |  - connections
            +--------------+
```

### Data Flow

```
User Click
    |
    v
InputHandler.touchDown()
    |
    v
Check if click is on Hotspot
    |
    v
If yes: RoomManager.navigateTo(targetRoom)
    |
    v
RoomRenderer.setCurrentRoom(newRoom)
    |
    v
Screen displays new room
```

---

## Room/Scene System

### Room Data Structure

Each room contains:
- **ID** - Unique identifier (matches existing Location enum)
- **Name** - Display name
- **Background Image** - The first-person view image
- **Hotspots** - List of clickable areas that lead to other rooms
- **Connections** - Map of direction -> connected room

### Room Class

```java
// Room.java
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

    // Add a connection to another room
    public void addConnection(Direction direction, RoomID targetRoom) {
        connections.put(direction, targetRoom);
    }

    // Add a clickable hotspot
    public void addHotspot(Hotspot hotspot) {
        hotspots.add(hotspot);
    }

    // Getters
    public RoomID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBackgroundTexturePath() { return backgroundTexturePath; }
    public List<Hotspot> getHotspots() { return hotspots; }
    public Map<Direction, RoomID> getConnections() { return connections; }

    // Check if room connects to another in given direction
    public boolean hasConnection(Direction direction) {
        return connections.containsKey(direction);
    }

    public RoomID getConnection(Direction direction) {
        return connections.get(direction);
    }
}
```

### Direction Enum

```java
// Direction.java
package com.dsa.game.navigation;

public enum Direction {
    NORTH("Forward", "Go forward"),
    SOUTH("Back", "Go back"),
    EAST("Right", "Turn right"),
    WEST("Left", "Turn left"),
    UP("Upstairs", "Go upstairs"),
    DOWN("Downstairs", "Go downstairs"),
    ENTER("Enter", "Enter");

    private String displayName;
    private String actionText;

    Direction(String displayName, String actionText) {
        this.displayName = displayName;
        this.actionText = actionText;
    }

    public String getDisplayName() { return displayName; }
    public String getActionText() { return actionText; }
}
```

---

## Navigation Hotspots

### Hotspot System

Hotspots are clickable areas on the screen that trigger navigation.

```java
// Hotspot.java
package com.dsa.game.navigation;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Hotspot {

    public enum HotspotType {
        ARROW_LEFT,      // Turn left
        ARROW_RIGHT,     // Turn right
        ARROW_FORWARD,   // Move forward
        ARROW_BACK,      // Move back
        DOOR,            // Enter doorway
        STAIRS_UP,       // Go upstairs
        STAIRS_DOWN      // Go downstairs
    }

    private HotspotType type;
    private Direction direction;
    private Room.RoomID targetRoom;
    private Rectangle bounds;        // Click area
    private Vector2 screenPosition;  // Where to render on screen
    private String tooltip;          // Hover text
    private boolean isHovered;

    public Hotspot(HotspotType type, Direction direction, Room.RoomID targetRoom) {
        this.type = type;
        this.direction = direction;
        this.targetRoom = targetRoom;
        this.bounds = new Rectangle();
        this.screenPosition = new Vector2();
        this.tooltip = direction.getActionText();
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
}
```

### Hotspot Positioning

Hotspots are positioned based on their type:

```
Screen Layout (1280x720 example):

+------------------------------------------------------------------+
|                                                                  |
|                                                                  |
|                         [FORWARD]                                |
|                        (640, 500)                                |
|                                                                  |
|                    +------------------+                          |
|   [LEFT]           |                  |           [RIGHT]        |
|  (100, 360)        |      DOOR        |          (1180, 360)     |
|                    |   (640, 360)     |                          |
|                    |                  |                          |
|                    +------------------+                          |
|                                                                  |
|                          [BACK]                                  |
|                        (640, 100)                                |
|                                                                  |
+------------------------------------------------------------------+
```

### Standard Hotspot Positions

```java
// HotspotPositions.java
package com.dsa.game.navigation;

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
```

---

## Placeholder Graphics

### Creating Simple Placeholder Graphics

Since we're starting with placeholders, we'll create simple colored rectangles with text labels.

### PlaceholderGenerator Class

```java
// PlaceholderGenerator.java
package com.dsa.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dsa.game.navigation.Room;
import java.util.HashMap;
import java.util.Map;

public class PlaceholderGenerator {

    // Color scheme for each room (atmospheric)
    private static final Map<Room.RoomID, Color> ROOM_COLORS = new HashMap<>();

    static {
        ROOM_COLORS.put(Room.RoomID.ENTRANCE, new Color(0.3f, 0.25f, 0.2f, 1));      // Dark brown
        ROOM_COLORS.put(Room.RoomID.STUDY, new Color(0.2f, 0.15f, 0.15f, 1));        // Dark red-brown
        ROOM_COLORS.put(Room.RoomID.PARLOR, new Color(0.25f, 0.2f, 0.25f, 1));       // Dark purple
        ROOM_COLORS.put(Room.RoomID.KITCHEN, new Color(0.2f, 0.2f, 0.2f, 1));        // Dark gray
        ROOM_COLORS.put(Room.RoomID.GUEST_ROOMS, new Color(0.2f, 0.2f, 0.25f, 1));   // Dark blue
        ROOM_COLORS.put(Room.RoomID.GROUNDSKEEPER_SHED, new Color(0.15f, 0.2f, 0.15f, 1)); // Dark green
        ROOM_COLORS.put(Room.RoomID.SERVANTS_QUARTERS, new Color(0.2f, 0.18f, 0.15f, 1)); // Dim brown
        ROOM_COLORS.put(Room.RoomID.CELLAR, new Color(0.1f, 0.1f, 0.1f, 1));         // Near black
    }

    // Generate a placeholder room texture
    public static Texture generateRoomPlaceholder(Room.RoomID roomId, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Get room color
        Color baseColor = ROOM_COLORS.getOrDefault(roomId, Color.DARK_GRAY);

        // Fill background with gradient (darker at bottom)
        for (int y = 0; y < height; y++) {
            float factor = 0.7f + (0.3f * y / height); // Darker at bottom
            Color rowColor = new Color(
                baseColor.r * factor,
                baseColor.g * factor,
                baseColor.b * factor,
                1
            );
            pixmap.setColor(rowColor);
            pixmap.drawLine(0, y, width, y);
        }

        // Draw floor line
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fillRectangle(0, 0, width, height / 4);

        // Draw simple wall outline
        pixmap.setColor(new Color(0.3f, 0.3f, 0.3f, 1));
        pixmap.drawRectangle(50, height/4, width - 100, height - height/4 - 50);

        // Draw room name area (will be overlaid with text in renderer)
        pixmap.setColor(new Color(0, 0, 0, 0.5f));
        pixmap.fillRectangle(width/2 - 150, height - 80, 300, 50);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    // Generate arrow hotspot texture
    public static Texture generateArrowTexture(int size, Direction direction) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Semi-transparent background
        pixmap.setColor(new Color(0, 0, 0, 0.5f));
        pixmap.fillCircle(size/2, size/2, size/2 - 2);

        // Arrow color
        pixmap.setColor(new Color(1, 1, 1, 0.8f));

        int cx = size / 2;
        int cy = size / 2;
        int arrowSize = size / 3;

        // Draw arrow based on direction
        switch (direction) {
            case NORTH: // Up arrow
                pixmap.fillTriangle(cx, cy + arrowSize, cx - arrowSize, cy - arrowSize/2, cx + arrowSize, cy - arrowSize/2);
                break;
            case SOUTH: // Down arrow
                pixmap.fillTriangle(cx, cy - arrowSize, cx - arrowSize, cy + arrowSize/2, cx + arrowSize, cy + arrowSize/2);
                break;
            case EAST: // Right arrow
                pixmap.fillTriangle(cx + arrowSize, cy, cx - arrowSize/2, cy - arrowSize, cx - arrowSize/2, cy + arrowSize);
                break;
            case WEST: // Left arrow
                pixmap.fillTriangle(cx - arrowSize, cy, cx + arrowSize/2, cy - arrowSize, cx + arrowSize/2, cy + arrowSize);
                break;
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    // Generate door hotspot texture
    public static Texture generateDoorTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Door frame
        pixmap.setColor(new Color(0.3f, 0.2f, 0.1f, 1)); // Brown
        pixmap.fillRectangle(0, 0, width, height);

        // Door interior (darker)
        pixmap.setColor(new Color(0.1f, 0.05f, 0.02f, 1));
        pixmap.fillRectangle(10, 10, width - 20, height - 20);

        // Highlight when can enter
        pixmap.setColor(new Color(1, 1, 1, 0.1f));
        pixmap.drawRectangle(5, 5, width - 10, height - 10);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }
}
```

### Room Placeholder Visuals

Each room will look like this (with placeholder graphics):

```
+------------------------------------------------------------------+
|                                                                  |
|    +----------------------------------------------------------+  |
|    |                                                          |  |
|    |                    [ROOM NAME]                           |  |
|    |                                                          |  |
|    |                                                          |  |
|    |              +------------------+                        |  |
|    |              |                  |                        |  |
|    |              |      DOOR        |                        |  |
|    |              |    (if exists)   |                        |  |
|    |              |                  |                        |  |
|    |              +------------------+                        |  |
|    |                                                          |  |
|    +----------------------------------------------------------+  |
|    |                    FLOOR AREA                            |  |
|    +----------------------------------------------------------+  |
|                                                                  |
|  [<]                                                        [>]  |
|                              [v]                                 |
+------------------------------------------------------------------+
```

---

## Implementation Code

### Main Game Class

```java
// DSAGame.java
package com.dsa.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dsa.game.screens.GameScreen;

public class DSAGame extends Game {

    public SpriteBatch batch;

    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final String TITLE = "The Unreliable Narrator";

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
```

### Desktop Launcher

```java
// DesktopLauncher.java
package com.dsa.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(DSAGame.TITLE);
        config.setWindowedMode(DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
        config.setResizable(false);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new DSAGame(), config);
    }
}
```

### Room Manager

```java
// RoomManager.java
package com.dsa.game.navigation;

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
        // Create all rooms
        createRoom(Room.RoomID.ENTRANCE, "Entrance Hall",
            "The main entrance of Vance Manor. Grand staircase leads up. Doors lead to the Study and Parlor.");

        createRoom(Room.RoomID.STUDY, "The Study",
            "The crime scene. Harold's desk sits where he died. Bookshelves line the north wall.");

        createRoom(Room.RoomID.PARLOR, "The Parlor",
            "Where guests gathered. The grandfather clock is stopped at 11:47.");

        createRoom(Room.RoomID.KITCHEN, "The Kitchen",
            "Large industrial kitchen. A door leads down to the cellar.");

        createRoom(Room.RoomID.GUEST_ROOMS, "Guest Rooms",
            "The guest wing. Margaret and James stayed here the night of the murder.");

        createRoom(Room.RoomID.GROUNDSKEEPER_SHED, "Groundskeeper's Shed",
            "Daniel's domain for 15 years. Tools and logbooks fill the small space.");

        createRoom(Room.RoomID.SERVANTS_QUARTERS, "Servants' Quarters",
            "Staff area. Simple but clean. A narrow bed with a worn wooden bedpost.");

        createRoom(Room.RoomID.CELLAR, "The Cellar",
            "Dark storage area beneath the kitchen. Flour sacks and wine racks.");

        // Set up connections
        setupConnections();

        // Start in entrance
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
        entrance.addConnection(Direction.EAST, Room.RoomID.STUDY);
        entrance.addConnection(Direction.WEST, Room.RoomID.PARLOR);
        entrance.addConnection(Direction.NORTH, Room.RoomID.KITCHEN);
        entrance.addConnection(Direction.UP, Room.RoomID.GUEST_ROOMS);

        // Add hotspots for entrance
        entrance.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.DOOR, Direction.EAST, Room.RoomID.STUDY));
        entrance.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_LEFT, Direction.WEST, Room.RoomID.PARLOR));
        entrance.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_FORWARD, Direction.NORTH, Room.RoomID.KITCHEN));
        entrance.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.STAIRS_UP, Direction.UP, Room.RoomID.GUEST_ROOMS));

        // STUDY connections (east wing - crime scene)
        study.addConnection(Direction.WEST, Room.RoomID.ENTRANCE);

        study.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_BACK, Direction.WEST, Room.RoomID.ENTRANCE));

        // PARLOR connections (west wing - formal guest room)
        parlor.addConnection(Direction.EAST, Room.RoomID.ENTRANCE);

        parlor.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.ENTRANCE));

        // KITCHEN connections
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
        guestRooms.addConnection(Direction.DOWN, Room.RoomID.ENTRANCE);

        guestRooms.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.STAIRS_DOWN, Direction.DOWN, Room.RoomID.ENTRANCE));

        // SHED connections (connects back to servants' quarters)
        shed.addConnection(Direction.EAST, Room.RoomID.SERVANTS_QUARTERS);

        shed.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.SERVANTS_QUARTERS));

        // SERVANTS' QUARTERS connections (only accessible from Kitchen)
        servants.addConnection(Direction.EAST, Room.RoomID.KITCHEN);

        servants.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.KITCHEN));

        // CELLAR connections
        cellar.addConnection(Direction.UP, Room.RoomID.KITCHEN);

        cellar.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.STAIRS_UP, Direction.UP, Room.RoomID.KITCHEN));
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
    public Room getCurrentRoom() { return currentRoom; }
    public Room getRoom(Room.RoomID id) { return rooms.get(id); }
}
```

### Game Screen

```java
// GameScreen.java
package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.dsa.game.DSAGame;
import com.dsa.game.navigation.*;
import com.dsa.game.rendering.PlaceholderGenerator;

import java.util.HashMap;
import java.util.Map;

public class GameScreen implements Screen {

    private DSAGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    private RoomManager roomManager;

    // Textures
    private Map<Room.RoomID, Texture> roomTextures;
    private Map<Direction, Texture> arrowTextures;
    private Texture doorTexture;

    // UI
    private String currentTooltip = "";

    public GameScreen(DSAGame game) {
        this.game = game;
        this.batch = game.batch;
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);

        // Initialize room manager
        roomManager = new RoomManager();

        // Generate placeholder textures
        generatePlaceholderTextures();

        // Set up input
        setupInput();
    }

    private void generatePlaceholderTextures() {
        roomTextures = new HashMap<>();
        arrowTextures = new HashMap<>();

        // Generate room backgrounds
        for (Room.RoomID roomId : Room.RoomID.values()) {
            roomTextures.put(roomId, PlaceholderGenerator.generateRoomPlaceholder(
                roomId, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT));
        }

        // Generate arrow textures
        arrowTextures.put(Direction.NORTH, PlaceholderGenerator.generateArrowTexture(80, Direction.NORTH));
        arrowTextures.put(Direction.SOUTH, PlaceholderGenerator.generateArrowTexture(80, Direction.SOUTH));
        arrowTextures.put(Direction.EAST, PlaceholderGenerator.generateArrowTexture(80, Direction.EAST));
        arrowTextures.put(Direction.WEST, PlaceholderGenerator.generateArrowTexture(80, Direction.WEST));

        // Generate door texture
        doorTexture = PlaceholderGenerator.generateDoorTexture(200, 300);
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // Convert screen coordinates (Y is inverted in LibGDX)
                float gameY = DSAGame.SCREEN_HEIGHT - screenY;

                // Check hotspots
                for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
                    if (hotspot.contains(screenX, gameY)) {
                        // Navigate to target room
                        roomManager.navigateTo(hotspot.getTargetRoom());
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                float gameY = DSAGame.SCREEN_HEIGHT - screenY;
                currentTooltip = "";

                // Check for hover on hotspots
                for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
                    hotspot.checkHover(screenX, gameY);
                    if (hotspot.isHovered()) {
                        currentTooltip = hotspot.getTooltip();
                    }
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                // Keyboard navigation as backup
                switch (keycode) {
                    case Input.Keys.W:
                    case Input.Keys.UP:
                        roomManager.navigateDirection(Direction.NORTH);
                        break;
                    case Input.Keys.S:
                    case Input.Keys.DOWN:
                        roomManager.navigateDirection(Direction.SOUTH);
                        break;
                    case Input.Keys.A:
                    case Input.Keys.LEFT:
                        roomManager.navigateDirection(Direction.WEST);
                        break;
                    case Input.Keys.D:
                    case Input.Keys.RIGHT:
                        roomManager.navigateDirection(Direction.EAST);
                        break;
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Room currentRoom = roomManager.getCurrentRoom();

        batch.begin();

        // Draw room background
        Texture roomTex = roomTextures.get(currentRoom.getId());
        if (roomTex != null) {
            batch.draw(roomTex, 0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
        }

        // Draw room name
        font.draw(batch, currentRoom.getName(),
            DSAGame.SCREEN_WIDTH/2 - 100, DSAGame.SCREEN_HEIGHT - 30);

        // Draw hotspots
        for (Hotspot hotspot : currentRoom.getHotspots()) {
            Texture hotspotTex = getHotspotTexture(hotspot);
            if (hotspotTex != null) {
                // Draw with highlight if hovered
                if (hotspot.isHovered()) {
                    batch.setColor(1, 1, 0.5f, 1); // Yellow tint
                }
                batch.draw(hotspotTex,
                    hotspot.getBounds().x,
                    hotspot.getBounds().y,
                    hotspot.getBounds().width,
                    hotspot.getBounds().height);
                batch.setColor(Color.WHITE);
            }
        }

        // Draw tooltip
        if (!currentTooltip.isEmpty()) {
            font.draw(batch, currentTooltip,
                Gdx.input.getX() + 15,
                DSAGame.SCREEN_HEIGHT - Gdx.input.getY() + 15);
        }

        // Draw room description at bottom
        font.draw(batch, currentRoom.getDescription(), 20, 50);

        // Draw controls hint
        font.draw(batch, "Click arrows to move | WASD/Arrow keys | ESC to quit",
            20, 20);

        batch.end();
    }

    private Texture getHotspotTexture(Hotspot hotspot) {
        switch (hotspot.getType()) {
            case ARROW_FORWARD:
                return arrowTextures.get(Direction.NORTH);
            case ARROW_BACK:
                return arrowTextures.get(Direction.SOUTH);
            case ARROW_LEFT:
                return arrowTextures.get(Direction.WEST);
            case ARROW_RIGHT:
                return arrowTextures.get(Direction.EAST);
            case DOOR:
            case STAIRS_UP:
            case STAIRS_DOWN:
                return doorTexture;
            default:
                return null;
        }
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        font.dispose();
        for (Texture tex : roomTextures.values()) {
            tex.dispose();
        }
        for (Texture tex : arrowTextures.values()) {
            tex.dispose();
        }
        doorTexture.dispose();
    }
}
```

---

## Room Connections Map

### Visual Connection Map

```
                                    GUEST ROOMS
                                        |
                                       [UP]
                                        |
                                        |
                         ENTRANCE ----[NORTH]---- KITCHEN ----[WEST]---- SERVANTS' ----[WEST]---- GROUNDSKEEPER'S
                         /   |   \                    |                   QUARTERS                     SHED
                   [WEST]    |    [EAST]          [DOWN]
                       /     |     \                  |
                  PARLOR     |     STUDY           CELLAR
                             |
                    (Parlor and Study are
                     separate wings - not
                     connected to each other)
```

### Connection Table

| From | Direction | To | Gate |
|------|-----------|-----|------|
| ENTRANCE | EAST | STUDY | None |
| ENTRANCE | WEST | PARLOR | None |
| ENTRANCE | NORTH | KITCHEN | None |
| ENTRANCE | UP | GUEST_ROOMS | None |
| STUDY | WEST | ENTRANCE | None |
| PARLOR | EAST | ENTRANCE | None |
| KITCHEN | SOUTH | ENTRANCE | None |
| KITCHEN | WEST | SERVANTS_QUARTERS | None |
| KITCHEN | DOWN | CELLAR | **Act 2: 4+ tapes watched AND 5+ evidence** |
| GUEST_ROOMS | DOWN | ENTRANCE | None |
| GUEST_ROOMS | ENTER | MARGARET_ROOM | **Act 3: BREATHING_WALL anomaly** |
| SERVANTS_QUARTERS | EAST | KITCHEN | None |
| CELLAR | UP | KITCHEN | None |
| GROUNDSKEEPER_SHED | EAST | SERVANTS_QUARTERS | None |
| SERVANTS_QUARTERS | WEST | GROUNDSKEEPER_SHED | None |

### Navigation Gates

The game uses a three-act structure with navigation gates:

**Act 1 (8 rooms):** Entrance, Study, Parlor, Kitchen, Guest Rooms, James's Room, Servants' Quarters, Groundskeeper's Shed -- all accessible from the start.

**Act 2 -- Cellar:** Sealed until the player has watched 4+ tapes AND collected 5+ evidence. The narrator provides mood-varied text explaining the door won't open. Implementation: `isCellarUnlocked()` in GameScreen.java checks `gameState.getWatchedTapes().size() >= 4 && gameState.getCollectedEvidence().size() >= 5`.

**Act 3 -- Margaret's Room:** Sealed until the BREATHING_WALL anomaly is discovered (found on 2nd examination of cellar wine_rack). Implementation: `isMargaretRoomUnlocked()` checks `gameState.hasAnomaly(EntityAnomaly.BREATHING_WALL)`.

When a player attempts to enter a sealed room, the narrator delivers a mood-appropriate message (HOPEFUL/CONFUSED/ANXIOUS/FRANTIC) explaining the door is sealed. This text escalates in intensity with awareness level.

---

## Future Integration

### Adding Existing Game Logic

Once navigation works, integrate the existing game mechanics:

1. **Awareness System**
   - Add `awareness` variable to GameScreen
   - Increment on room transitions
   - Display awareness meter in UI

2. **Evidence System**
   - Add clickable evidence hotspots in rooms
   - Integrate EvidenceManager class
   - Show collected evidence in inventory UI

3. **Dialogue/Narrator**
   - Add text box overlay for narrator dialogue
   - Trigger dialogue on entering rooms
   - Show room descriptions with narrator voice

4. **Tapes**
   - Add tape collection points
   - Create tape playback screen
   - Integrate with awareness triggers

### Integration Points

```java
// Add to GameScreen:

// Game state
private int awareness = 0;
private Set<Evidence> collectedEvidence = new HashSet<>();
private Set<Tape> collectedTapes = new HashSet<>();

// On room transition:
private void onRoomEnter(Room room) {
    awareness += 1; // COST_MOVE
    checkAwarenessThresholds();
    showRoomNarration(room);
}

// Check awareness
private void checkAwarenessThresholds() {
    if (awareness >= 80) {
        triggerGameOver();
    }
}
```

### File Structure After Full Integration

```
core/src/com/dsa/game/
├── DSAGame.java
├── screens/
│   ├── GameScreen.java
│   ├── MenuScreen.java
│   ├── TapePlaybackScreen.java
│   └── EndingScreen.java
├── navigation/
│   ├── Room.java
│   ├── RoomManager.java
│   ├── Hotspot.java
│   └── Direction.java
├── gameplay/
│   ├── GameState.java         # Port from existing
│   ├── EvidenceManager.java   # Port from existing
│   ├── AwarenessSystem.java
│   └── NarratorSystem.java
├── ui/
│   ├── DialogueBox.java
│   ├── InventoryPanel.java
│   ├── AwarenessMeter.java
│   └── HUD.java
└── rendering/
    ├── RoomRenderer.java
    ├── HotspotRenderer.java
    └── PlaceholderGenerator.java
```

---

## Quick Start Checklist

1. [ ] Install LibGDX (via setup tool or manual Gradle)
2. [ ] Create project structure
3. [ ] Copy the classes from this guide
4. [ ] Run DesktopLauncher to test
5. [ ] Verify navigation between rooms works
6. [ ] Replace placeholder graphics with real art (later)
7. [ ] Integrate game logic (later)

---

## Resources

- **LibGDX Wiki**: https://libgdx.com/wiki/
- **LibGDX Setup**: https://libgdx.com/wiki/start/project-generation
- **LibGDX GitHub**: https://github.com/libgdx/libgdx
- **Scene2D UI**: https://libgdx.com/wiki/graphics/2d/scene2d/scene2d-ui

---

*Created: January 2026*
*For: The Unreliable Narrator Game*
*Navigation System: First-Person Point-and-Click*
