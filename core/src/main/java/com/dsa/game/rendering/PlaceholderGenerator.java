package com.dsa.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.dsa.game.navigation.Direction;
import com.dsa.game.navigation.Room;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Generates visually interesting placeholder graphics for rooms and hotspots.
 * Creates atmospheric, varied backgrounds with unique elements for each room.
 */
public class PlaceholderGenerator {

    private static final Random random = new Random(42); // Fixed seed for consistency

    // Color scheme for each room (atmospheric)
    private static final Map<Room.RoomID, Color> ROOM_COLORS = new HashMap<>();

    static {
        ROOM_COLORS.put(Room.RoomID.ENTRANCE, new Color(0.35f, 0.3f, 0.25f, 1));      // Warm brown
        ROOM_COLORS.put(Room.RoomID.STUDY, new Color(0.25f, 0.2f, 0.18f, 1));         // Rich brown-red
        ROOM_COLORS.put(Room.RoomID.PARLOR, new Color(0.28f, 0.22f, 0.28f, 1));       // Deep purple-brown
        ROOM_COLORS.put(Room.RoomID.KITCHEN, new Color(0.22f, 0.22f, 0.25f, 1));      // Cool gray-blue
        ROOM_COLORS.put(Room.RoomID.GUEST_ROOMS, new Color(0.22f, 0.24f, 0.28f, 1));  // Soft blue-gray
        ROOM_COLORS.put(Room.RoomID.GROUNDSKEEPER_SHED, new Color(0.18f, 0.22f, 0.18f, 1)); // Mossy green-gray
        ROOM_COLORS.put(Room.RoomID.SERVANTS_QUARTERS, new Color(0.22f, 0.2f, 0.18f, 1));   // Dull brown
        ROOM_COLORS.put(Room.RoomID.CELLAR, new Color(0.12f, 0.1f, 0.08f, 1));        // Very dark brown
    }

    // Generate a visually interesting room texture
    public static Texture generateRoomPlaceholder(Room.RoomID roomId, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Get room color
        Color baseColor = ROOM_COLORS.getOrDefault(roomId, Color.DARK_GRAY);

        // Create atmospheric sky/ceiling gradient (lighter at top, darker toward middle)
        int ceilingHeight = height / 3;
        for (int y = 0; y < ceilingHeight; y++) {
            float factor = 0.4f + (0.3f * (ceilingHeight - y) / ceilingHeight);
            Color rowColor = new Color(
                baseColor.r * factor,
                baseColor.g * factor,
                baseColor.b * factor,
                1
            );
            pixmap.setColor(rowColor);
            pixmap.drawLine(0, y, width, y);
        }

        // Draw walls with perspective effect
        int floorStart = ceilingHeight;
        int floorHeight = height - floorStart;
        
        // Left wall (darker)
        Color wallColor = new Color(baseColor.r * 0.6f, baseColor.g * 0.6f, baseColor.b * 0.6f, 1);
        pixmap.setColor(wallColor);
        pixmap.fillTriangle(0, floorStart, width / 3, floorStart, 0, height);
        
        // Right wall (slightly lighter)
        Color wallColorRight = new Color(baseColor.r * 0.65f, baseColor.g * 0.65f, baseColor.b * 0.65f, 1);
        pixmap.setColor(wallColorRight);
        pixmap.fillTriangle(width, floorStart, width * 2 / 3, floorStart, width, height);
        
        // Back wall (center, darkest)
        Color backWallColor = new Color(baseColor.r * 0.5f, baseColor.g * 0.5f, baseColor.b * 0.5f, 1);
        pixmap.setColor(backWallColor);
        pixmap.fillRectangle(width / 3, floorStart, width / 3, floorHeight);

        // Add wood floor texture with perspective
        drawWoodFloor(pixmap, width, height, floorStart);

        // Add room-specific details
        addRoomDetails(pixmap, roomId, width, height, baseColor);

        // Add atmospheric lighting effects
        addLightingEffects(pixmap, roomId, width, height);

        // Add subtle texture/noise for depth
        addTextureNoise(pixmap, width, height, baseColor);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    private static void drawWoodFloor(Pixmap pixmap, int width, int height, int floorStart) {
        Color floorDark = new Color(0.15f, 0.12f, 0.1f, 1);
        Color floorLight = new Color(0.2f, 0.16f, 0.13f, 1);
        
        int floorHeight = height - floorStart;
        int plankCount = 8;
        
        for (int i = 0; i < plankCount; i++) {
            float t = (float) i / plankCount;
            int y1 = floorStart + (int)(floorHeight * t);
            int y2 = floorStart + (int)(floorHeight * (t + 1.0f / plankCount));
            
            // Perspective: wider at bottom
            int x1Left = (int)(width * 0.3f * (1 - t * 0.3f));
            int x1Right = (int)(width * 0.7f * (1 + t * 0.3f));
            int x2Left = (int)(width * 0.3f * (1 - (t + 1.0f/plankCount) * 0.3f));
            int x2Right = (int)(width * 0.7f * (1 + (t + 1.0f/plankCount) * 0.3f));
            
            Color plankColor = (i % 2 == 0) ? floorDark : floorLight;
            pixmap.setColor(plankColor);
            // Draw quad using two triangles
            pixmap.fillTriangle(x1Left, y1, x1Right, y1, x2Left, y2);
            pixmap.fillTriangle(x1Right, y1, x2Right, y2, x2Left, y2);
        }
    }

    private static void addRoomDetails(Pixmap pixmap, Room.RoomID roomId, int width, int height, Color baseColor) {
        int floorStart = height / 3;
        
        switch (roomId) {
            case ENTRANCE:
                // Grand entrance: chandelier, door frame, stairs
                drawChandelier(pixmap, width / 2, floorStart - 50, baseColor);
                drawDoorFrame(pixmap, width / 2, height - 100, 120, 200, baseColor);
                drawStairs(pixmap, width / 2, height - 50, 200, baseColor);
                break;
                
            case STUDY:
                // Study: desk, bookshelf, fireplace, window
                drawBookshelf(pixmap, width * 3 / 4, floorStart, 80, height - floorStart - 50, baseColor);
                drawDesk(pixmap, width / 2, height - 150, 300, 100, baseColor);
                drawFireplace(pixmap, width / 4, floorStart, 100, height - floorStart - 100, baseColor);
                drawWindow(pixmap, width / 2, floorStart + 50, 200, 150, baseColor);
                break;
                
            case PARLOR:
                // Parlor: chairs, fireplace, grandfather clock
                drawFireplace(pixmap, width / 2, floorStart, 120, height - floorStart - 100, baseColor);
                drawChair(pixmap, width / 3, height - 120, 80, baseColor);
                drawChair(pixmap, width * 2 / 3, height - 120, 80, baseColor);
                drawClock(pixmap, width * 4 / 5, floorStart + 50, 60, 200, baseColor);
                break;
                
            case KITCHEN:
                // Kitchen: counter, stove, shelves
                drawCounter(pixmap, width / 3, height - 180, 400, 120, baseColor);
                drawStove(pixmap, width / 2, height - 200, 100, 80, baseColor);
                drawShelves(pixmap, width / 6, floorStart + 50, 60, height - floorStart - 200, baseColor);
                break;
                
            case GUEST_ROOMS:
                // Guest rooms: beds, dresser, window
                drawBed(pixmap, width / 3, height - 150, 200, 120, baseColor);
                drawBed(pixmap, width * 2 / 3, height - 150, 200, 120, baseColor);
                drawDresser(pixmap, width / 2, height - 200, 150, 80, baseColor);
                drawWindow(pixmap, width / 2, floorStart + 50, 180, 120, baseColor);
                break;
                
            case GROUNDSKEEPER_SHED:
                // Shed: workbench, tools, window
                drawWorkbench(pixmap, width / 2, height - 150, 300, 100, baseColor);
                drawWindow(pixmap, width / 2, floorStart + 30, 100, 80, baseColor);
                break;
                
            case SERVANTS_QUARTERS:
                // Servants' quarters: simple bed, small table
                drawBed(pixmap, width / 2, height - 120, 180, 100, baseColor);
                drawSmallTable(pixmap, width / 3, height - 100, 60, baseColor);
                break;
                
            case CELLAR:
                // Cellar: wine racks, barrels, storage
                drawWineRack(pixmap, width / 4, floorStart + 50, 60, height - floorStart - 100, baseColor);
                drawWineRack(pixmap, width * 3 / 4, floorStart + 50, 60, height - floorStart - 100, baseColor);
                drawBarrel(pixmap, width / 2, height - 100, 80, baseColor);
                break;
        }
    }

    private static void addLightingEffects(Pixmap pixmap, Room.RoomID roomId, int width, int height) {
        // Add subtle lighting from windows or lamps
        Color lightColor = new Color(1f, 0.95f, 0.8f, 0.15f);
        pixmap.setColor(lightColor);
        
        // Window light (if applicable)
        if (roomId == Room.RoomID.STUDY || roomId == Room.RoomID.GUEST_ROOMS) {
            int lightX = width / 2;
            int lightY = height / 3 + 50;
            for (int i = 0; i < 100; i++) {
                float alpha = 0.15f * (1f - i / 100f);
                pixmap.setColor(1f, 0.95f, 0.8f, alpha);
                pixmap.fillCircle(lightX, lightY, 150 - i);
            }
        }
        
        // Fireplace glow (if applicable)
        if (roomId == Room.RoomID.STUDY || roomId == Room.RoomID.PARLOR) {
            int fireX = (roomId == Room.RoomID.STUDY) ? width / 4 : width / 2;
            int fireY = height - 150;
            for (int i = 0; i < 60; i++) {
                float alpha = 0.2f * (1f - i / 60f);
                pixmap.setColor(1f, 0.6f, 0.3f, alpha);
                pixmap.fillCircle(fireX, fireY, 80 - i);
            }
        }
    }

    private static void addTextureNoise(Pixmap pixmap, int width, int height, Color baseColor) {
        // Add subtle random noise for texture
        for (int i = 0; i < width * height / 200; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            float variation = 0.9f + random.nextFloat() * 0.2f;
            Color noiseColor = new Color(
                baseColor.r * variation,
                baseColor.g * variation,
                baseColor.b * variation,
                0.3f
            );
            pixmap.setColor(noiseColor);
            pixmap.drawPixel(x, y);
        }
    }

    // Furniture drawing methods
    private static void drawChandelier(Pixmap pixmap, int x, int y, Color baseColor) {
        Color chandelierColor = new Color(baseColor.r * 1.3f, baseColor.g * 1.3f, baseColor.b * 1.3f, 1);
        pixmap.setColor(chandelierColor);
        pixmap.fillCircle(x, y, 25);
        pixmap.drawLine(x, y, x, y + 30);
    }

    private static void drawDoorFrame(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color frameColor = new Color(baseColor.r * 0.4f, baseColor.g * 0.4f, baseColor.b * 0.4f, 1);
        pixmap.setColor(frameColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
        Color doorColor = new Color(baseColor.r * 0.3f, baseColor.g * 0.3f, baseColor.b * 0.3f, 1);
        pixmap.setColor(doorColor);
        pixmap.fillRectangle(x - width/2 + 10, y + 10, width - 20, height - 20);
    }

    private static void drawStairs(Pixmap pixmap, int x, int y, int width, Color baseColor) {
        Color stairColor = new Color(baseColor.r * 0.5f, baseColor.g * 0.5f, baseColor.b * 0.5f, 1);
        pixmap.setColor(stairColor);
        for (int i = 0; i < 5; i++) {
            int stepY = y - i * 15;
            int stepWidth = width - i * 20;
            pixmap.fillRectangle(x - stepWidth/2, stepY, stepWidth, 15);
        }
    }

    private static void drawBookshelf(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color shelfColor = new Color(baseColor.r * 0.4f, baseColor.g * 0.4f, baseColor.b * 0.4f, 1);
        pixmap.setColor(shelfColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
        // Draw shelf dividers
        for (int i = 1; i < 4; i++) {
            pixmap.drawLine(x - width/2, y + height * i / 4, x + width/2, y + height * i / 4);
        }
    }

    private static void drawDesk(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color deskColor = new Color(baseColor.r * 0.35f, baseColor.g * 0.35f, baseColor.b * 0.35f, 1);
        pixmap.setColor(deskColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
        // Draw desk legs
        pixmap.fillRectangle(x - width/2 + 10, y + height, 15, 20);
        pixmap.fillRectangle(x + width/2 - 25, y + height, 15, 20);
    }

    private static void drawFireplace(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color fireplaceColor = new Color(baseColor.r * 0.3f, baseColor.g * 0.3f, baseColor.b * 0.3f, 1);
        pixmap.setColor(fireplaceColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
        // Fire opening
        Color fireColor = new Color(0.8f, 0.4f, 0.1f, 1);
        pixmap.setColor(fireColor);
        pixmap.fillRectangle(x - width/4, y + height - 60, width/2, 50);
    }

    private static void drawWindow(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color windowColor = new Color(0.1f, 0.15f, 0.25f, 0.7f);
        pixmap.setColor(windowColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
        // Window frame
        Color frameColor = new Color(baseColor.r * 0.4f, baseColor.g * 0.4f, baseColor.b * 0.4f, 1);
        pixmap.setColor(frameColor);
        pixmap.drawRectangle(x - width/2, y, width, height);
        pixmap.drawLine(x, y, x, y + height);
        pixmap.drawLine(x - width/2, y + height/2, x + width/2, y + height/2);
    }

    private static void drawChair(Pixmap pixmap, int x, int y, int size, Color baseColor) {
        Color chairColor = new Color(baseColor.r * 0.4f, baseColor.g * 0.4f, baseColor.b * 0.4f, 1);
        pixmap.setColor(chairColor);
        // Chair back
        pixmap.fillRectangle(x - size/2, y, size, size * 3/4);
        // Chair seat
        pixmap.fillRectangle(x - size/2, y + size * 3/4, size, size/4);
    }

    private static void drawClock(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color clockColor = new Color(baseColor.r * 0.35f, baseColor.g * 0.35f, baseColor.b * 0.35f, 1);
        pixmap.setColor(clockColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
        // Clock face
        Color faceColor = new Color(0.9f, 0.9f, 0.85f, 1);
        pixmap.setColor(faceColor);
        pixmap.fillCircle(x, y + height/2, width/3);
    }

    private static void drawCounter(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color counterColor = new Color(baseColor.r * 0.4f, baseColor.g * 0.4f, baseColor.b * 0.4f, 1);
        pixmap.setColor(counterColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
    }

    private static void drawStove(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color stoveColor = new Color(0.2f, 0.2f, 0.2f, 1);
        pixmap.setColor(stoveColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
        // Burners
        Color burnerColor = new Color(0.3f, 0.3f, 0.3f, 1);
        pixmap.setColor(burnerColor);
        pixmap.fillCircle(x - width/4, y + height/2, 15);
        pixmap.fillCircle(x + width/4, y + height/2, 15);
    }

    private static void drawShelves(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color shelfColor = new Color(baseColor.r * 0.4f, baseColor.g * 0.4f, baseColor.b * 0.4f, 1);
        pixmap.setColor(shelfColor);
        for (int i = 0; i < 3; i++) {
            pixmap.fillRectangle(x, y + i * height/3, width, 10);
        }
    }

    private static void drawBed(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color bedColor = new Color(baseColor.r * 0.35f, baseColor.g * 0.35f, baseColor.b * 0.35f, 1);
        pixmap.setColor(bedColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
        // Pillow
        Color pillowColor = new Color(baseColor.r * 0.5f, baseColor.g * 0.5f, baseColor.b * 0.5f, 1);
        pixmap.setColor(pillowColor);
        pixmap.fillRectangle(x - width/4, y + height - 30, width/2, 25);
    }

    private static void drawDresser(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color dresserColor = new Color(baseColor.r * 0.4f, baseColor.g * 0.4f, baseColor.b * 0.4f, 1);
        pixmap.setColor(dresserColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
        // Drawers
        pixmap.drawLine(x - width/2, y + height/2, x + width/2, y + height/2);
        pixmap.drawLine(x, y, x, y + height);
    }

    private static void drawWorkbench(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color benchColor = new Color(baseColor.r * 0.35f, baseColor.g * 0.35f, baseColor.b * 0.35f, 1);
        pixmap.setColor(benchColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
    }

    private static void drawSmallTable(Pixmap pixmap, int x, int y, int size, Color baseColor) {
        Color tableColor = new Color(baseColor.r * 0.4f, baseColor.g * 0.4f, baseColor.b * 0.4f, 1);
        pixmap.setColor(tableColor);
        pixmap.fillRectangle(x - size/2, y, size, size/3);
    }

    private static void drawWineRack(Pixmap pixmap, int x, int y, int width, int height, Color baseColor) {
        Color rackColor = new Color(baseColor.r * 0.3f, baseColor.g * 0.3f, baseColor.b * 0.3f, 1);
        pixmap.setColor(rackColor);
        pixmap.fillRectangle(x - width/2, y, width, height);
        // Draw wine bottle slots
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                int slotX = x - width/2 + width * (i + 1) / 5;
                int slotY = y + height * (j + 1) / 4;
                pixmap.drawCircle(slotX, slotY, 8);
            }
        }
    }

    private static void drawBarrel(Pixmap pixmap, int x, int y, int size, Color baseColor) {
        Color barrelColor = new Color(baseColor.r * 0.3f, baseColor.g * 0.25f, baseColor.b * 0.2f, 1);
        pixmap.setColor(barrelColor);
        pixmap.fillCircle(x, y, size/2);
        // Barrel bands
        pixmap.setColor(new Color(baseColor.r * 0.2f, baseColor.g * 0.15f, baseColor.b * 0.1f, 1));
        pixmap.drawCircle(x, y, size/2);
        pixmap.drawLine(x - size/2, y - size/4, x + size/2, y - size/4);
        pixmap.drawLine(x - size/2, y + size/4, x + size/2, y + size/4);
    }

    // Generate arrow hotspot texture with better visuals
    public static Texture generateArrowTexture(int size, Direction direction) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Outer glow
        Color glowColor = new Color(0.3f, 0.3f, 0.4f, 0.6f);
        pixmap.setColor(glowColor);
        pixmap.fillCircle(size/2, size/2, size/2);

        // Inner circle with gradient
        for (int r = size/2 - 3; r > size/2 - 8; r--) {
            float alpha = 0.7f - (size/2 - 3 - r) * 0.1f;
            pixmap.setColor(new Color(0.2f, 0.2f, 0.3f, alpha));
            pixmap.fillCircle(size/2, size/2, r);
        }

        // Arrow color (brighter, more visible)
        pixmap.setColor(new Color(0.9f, 0.9f, 0.95f, 0.95f));

        int cx = size / 2;
        int cy = size / 2;
        int arrowSize = size / 3;

        // Draw arrow based on direction with outline
        switch (direction) {
            case NORTH: // Up arrow
                // Arrow outline
                pixmap.setColor(new Color(0.1f, 0.1f, 0.15f, 0.8f));
                pixmap.fillTriangle(cx, cy + arrowSize + 2, cx - arrowSize - 2, cy - arrowSize/2 - 2, cx + arrowSize + 2, cy - arrowSize/2 - 2);
                // Arrow fill
                pixmap.setColor(new Color(0.9f, 0.9f, 0.95f, 0.95f));
                pixmap.fillTriangle(cx, cy + arrowSize, cx - arrowSize, cy - arrowSize/2, cx + arrowSize, cy - arrowSize/2);
                break;
            case SOUTH: // Down arrow
                pixmap.setColor(new Color(0.1f, 0.1f, 0.15f, 0.8f));
                pixmap.fillTriangle(cx, cy - arrowSize - 2, cx - arrowSize - 2, cy + arrowSize/2 + 2, cx + arrowSize + 2, cy + arrowSize/2 + 2);
                pixmap.setColor(new Color(0.9f, 0.9f, 0.95f, 0.95f));
                pixmap.fillTriangle(cx, cy - arrowSize, cx - arrowSize, cy + arrowSize/2, cx + arrowSize, cy + arrowSize/2);
                break;
            case EAST: // Right arrow
                pixmap.setColor(new Color(0.1f, 0.1f, 0.15f, 0.8f));
                pixmap.fillTriangle(cx + arrowSize + 2, cy, cx - arrowSize/2 - 2, cy - arrowSize - 2, cx - arrowSize/2 - 2, cy + arrowSize + 2);
                pixmap.setColor(new Color(0.9f, 0.9f, 0.95f, 0.95f));
                pixmap.fillTriangle(cx + arrowSize, cy, cx - arrowSize/2, cy - arrowSize, cx - arrowSize/2, cy + arrowSize);
                break;
            case WEST: // Left arrow
                pixmap.setColor(new Color(0.1f, 0.1f, 0.15f, 0.8f));
                pixmap.fillTriangle(cx - arrowSize - 2, cy, cx + arrowSize/2 + 2, cy - arrowSize - 2, cx + arrowSize/2 + 2, cy + arrowSize + 2);
                pixmap.setColor(new Color(0.9f, 0.9f, 0.95f, 0.95f));
                pixmap.fillTriangle(cx - arrowSize, cy, cx + arrowSize/2, cy - arrowSize, cx + arrowSize/2, cy + arrowSize);
                break;
            case UP:
            case DOWN:
            case ENTER:
                // These directions use door/stairs textures, not arrows
                // Draw a simple circle as fallback
                pixmap.setColor(new Color(0.9f, 0.9f, 0.95f, 0.95f));
                pixmap.fillCircle(cx, cy, arrowSize);
                break;
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    // Generate examine icon -- small glowing circle for examinable objects
    public static Texture generateExamineIcon(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        int cx = size / 2;
        int cy = size / 2;

        // Outer glow
        for (int r = size / 2; r > size / 2 - 6; r--) {
            float alpha = 0.3f * (1f - (float)(size / 2 - r) / 6f);
            pixmap.setColor(new Color(0.8f, 0.7f, 0.3f, alpha));
            pixmap.fillCircle(cx, cy, r);
        }

        // Inner circle
        pixmap.setColor(new Color(1f, 0.9f, 0.4f, 0.85f));
        pixmap.fillCircle(cx, cy, size / 4);

        // Magnifying glass icon: small circle + line
        pixmap.setColor(new Color(0.1f, 0.1f, 0.05f, 0.9f));
        pixmap.drawCircle(cx - 2, cy + 2, size / 6);
        pixmap.drawLine(cx + size / 8, cy - size / 8, cx + size / 4, cy - size / 4);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    // Generate door hotspot texture with more detail
    public static Texture generateDoorTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Door frame (wooden)
        Color frameColor = new Color(0.25f, 0.18f, 0.12f, 1);
        pixmap.setColor(frameColor);
        pixmap.fillRectangle(0, 0, width, height);

        // Door panel (darker wood)
        Color doorColor = new Color(0.15f, 0.1f, 0.08f, 1);
        pixmap.setColor(doorColor);
        pixmap.fillRectangle(8, 8, width - 16, height - 16);

        // Door panels (Victorian style)
        int panelWidth = (width - 16) / 2;
        int panelHeight = (height - 16) / 3;
        Color panelColor = new Color(0.12f, 0.08f, 0.06f, 1);
        pixmap.setColor(panelColor);
        
        // Top panels
        pixmap.fillRectangle(8 + 4, 8 + 4, panelWidth - 8, panelHeight - 4);
        pixmap.fillRectangle(8 + panelWidth + 4, 8 + 4, panelWidth - 8, panelHeight - 4);
        
        // Middle panels
        pixmap.fillRectangle(8 + 4, 8 + panelHeight + 4, panelWidth - 8, panelHeight - 4);
        pixmap.fillRectangle(8 + panelWidth + 4, 8 + panelHeight + 4, panelWidth - 8, panelHeight - 4);
        
        // Bottom panel (single wide)
        pixmap.fillRectangle(8 + 4, 8 + panelHeight * 2 + 4, width - 16 - 8, panelHeight - 4);

        // Door handle
        Color handleColor = new Color(0.4f, 0.4f, 0.35f, 1);
        pixmap.setColor(handleColor);
        pixmap.fillCircle(width - 25, height / 2, 6);

        // Highlight/shine
        pixmap.setColor(new Color(1, 1, 1, 0.15f));
        pixmap.drawRectangle(4, 4, width - 8, height - 8);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }
}
