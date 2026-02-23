package com.dsa.game.ui;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * A single draggable fragment of a torn document.
 */
public class DocumentPiece {

    private final int index;
    private final String text;
    private final Rectangle bounds;
    private final Vector2 correctPosition;
    private final float[] jaggedEdgeOffsets; // For rendering torn edges

    private boolean placed = false;
    private boolean dragging = false;

    public DocumentPiece(int index, String text, float width, float height, float correctX, float correctY) {
        this.index = index;
        this.text = text;
        this.bounds = new Rectangle(0, 0, width, height);
        this.correctPosition = new Vector2(correctX, correctY);

        // Generate random jagged edge offsets for visual effect
        this.jaggedEdgeOffsets = generateJaggedEdges();
    }

    private float[] generateJaggedEdges() {
        // 8 points per edge (top, right, bottom, left) = 32 offsets
        float[] offsets = new float[32];
        for (int i = 0; i < offsets.length; i++) {
            // Random offset between -3 and +3 pixels
            offsets[i] = (float) (Math.random() * 6 - 3);
        }
        return offsets;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getX() {
        return bounds.x;
    }

    public float getY() {
        return bounds.y;
    }

    public float getWidth() {
        return bounds.width;
    }

    public float getHeight() {
        return bounds.height;
    }

    public void setPosition(float x, float y) {
        bounds.x = x;
        bounds.y = y;
    }

    public Vector2 getCorrectPosition() {
        return correctPosition;
    }

    public boolean isPlaced() {
        return placed;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
        if (placed) {
            // Snap to exact correct position
            bounds.x = correctPosition.x;
            bounds.y = correctPosition.y;
        }
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    /**
     * Check if piece is close enough to correct position to snap.
     */
    public boolean isNearCorrectPosition(float snapDistance) {
        float dx = bounds.x - correctPosition.x;
        float dy = bounds.y - correctPosition.y;
        return Math.sqrt(dx * dx + dy * dy) < snapDistance;
    }

    public float[] getJaggedEdgeOffsets() {
        return jaggedEdgeOffsets;
    }
}
