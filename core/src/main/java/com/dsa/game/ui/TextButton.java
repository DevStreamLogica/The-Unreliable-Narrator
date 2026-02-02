package com.dsa.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class TextButton {

    private String label;
    private final Rectangle bounds;
    private boolean hovered;
    private boolean enabled;
    private final String actionId; // identifier for click handling

    private static Texture normalTexture;
    private static Texture hoverTexture;
    private static Texture disabledTexture;

    public TextButton(String label, float x, float y, float width, float height, String actionId) {
        this.label = label;
        this.bounds = new Rectangle(x, y, width, height);
        this.hovered = false;
        this.enabled = true;
        this.actionId = actionId;
        ensureTextures();
    }

    private static void ensureTextures() {
        if (normalTexture != null) return;

        // Normal button
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(new Color(0.15f, 0.15f, 0.2f, 0.9f));
        p.fill();
        normalTexture = new Texture(p);
        p.dispose();

        // Hover button
        p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(new Color(0.25f, 0.25f, 0.35f, 0.95f));
        p.fill();
        hoverTexture = new Texture(p);
        p.dispose();

        // Disabled button
        p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(new Color(0.1f, 0.1f, 0.1f, 0.6f));
        p.fill();
        disabledTexture = new Texture(p);
        p.dispose();
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        Texture bg;
        if (!enabled) bg = disabledTexture;
        else if (hovered) bg = hoverTexture;
        else bg = normalTexture;

        batch.setColor(Color.WHITE);
        batch.draw(bg, bounds.x, bounds.y, bounds.width, bounds.height);

        // Border
        // (drawn as thin rectangles since Pixmap-based)
        Color borderColor = hovered ? new Color(0.8f, 0.8f, 0.6f, 1) : new Color(0.4f, 0.4f, 0.5f, 1);
        if (!enabled) borderColor = new Color(0.2f, 0.2f, 0.2f, 0.5f);

        font.setColor(borderColor);
        // Top/bottom borders using font (lightweight approach)

        // Label
        GlyphLayout layout = new GlyphLayout(font, label);
        float textX = bounds.x + (bounds.width - layout.width) / 2;
        float textY = bounds.y + (bounds.height + layout.height) / 2;

        if (!enabled) font.setColor(new Color(0.4f, 0.4f, 0.4f, 0.7f));
        else if (hovered) font.setColor(new Color(1f, 1f, 0.85f, 1));
        else font.setColor(new Color(0.85f, 0.85f, 0.8f, 1));

        font.draw(batch, label, textX, textY);
        font.setColor(Color.WHITE);
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    public void checkHover(float x, float y) {
        hovered = enabled && bounds.contains(x, y);
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getActionId() { return actionId; }
    public boolean isHovered() { return hovered; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Rectangle getBounds() { return bounds; }

    public void setPosition(float x, float y) {
        bounds.setPosition(x, y);
    }

    public static void disposeTextures() {
        if (normalTexture != null) { normalTexture.dispose(); normalTexture = null; }
        if (hoverTexture != null) { hoverTexture.dispose(); hoverTexture = null; }
        if (disabledTexture != null) { disabledTexture.dispose(); disabledTexture = null; }
    }
}
