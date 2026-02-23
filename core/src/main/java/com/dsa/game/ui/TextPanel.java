package com.dsa.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dsa.game.DSAGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Overlay panel that renders on top of the room view.
 * Shows word-wrapped text with scroll and optional action buttons.
 */
public class TextPanel {

    private boolean visible = false;
    private String text = "";
    private float scrollOffset = 0;
    private final List<TextButton> actionButtons = new ArrayList<>();
    private TextButton closeButton;

    // Typewriter animation state
    private float revealTimer = 0;
    private int revealedChars = 0;
    private boolean revealComplete = true;
    private static float charsPerSecond = 40f;
    private String fullText = "";

    // Panel dimensions
    private static final float PANEL_X = 40;
    private static final float PANEL_Y = 20;
    private static final float PANEL_WIDTH = DSAGame.SCREEN_WIDTH - 80;
    private static final float PANEL_HEIGHT = DSAGame.SCREEN_HEIGHT * 0.55f;
    private static final float PADDING = 20;
    private static final float BUTTON_HEIGHT = 35;
    private static final float BUTTON_SPACING = 8;
    private static final float CLOSE_SIZE = 30;
    private static final int MAX_LINE_CHARS = 85;

    private Texture panelTexture;
    private Texture borderTexture;

    public TextPanel() {
        generateTextures();
        closeButton = new TextButton("X", PANEL_X + PANEL_WIDTH - CLOSE_SIZE - 10,
            PANEL_Y + PANEL_HEIGHT - CLOSE_SIZE - 10, CLOSE_SIZE, CLOSE_SIZE, "close");
    }

    private void generateTextures() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(new Color(0.05f, 0.05f, 0.08f, 0.92f));
        p.fill();
        panelTexture = new Texture(p);
        p.dispose();

        p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(new Color(0.35f, 0.35f, 0.4f, 1));
        p.fill();
        borderTexture = new Texture(p);
        p.dispose();
    }

    /** Show panel with text only. */
    public void show(String text) {
        this.fullText = text;
        this.text = "";
        this.scrollOffset = 0;
        this.revealTimer = 0;
        this.revealedChars = 0;
        this.revealComplete = false;
        this.actionButtons.clear();
        this.visible = true;
    }

    /** Show panel with text and action buttons. */
    public void show(String text, List<TextButton> buttons) {
        this.fullText = text;
        this.text = "";
        this.scrollOffset = 0;
        this.revealTimer = 0;
        this.revealedChars = 0;
        this.revealComplete = false;
        this.actionButtons.clear();
        this.actionButtons.addAll(buttons);
        layoutButtons();
        this.visible = true;
    }

    /** Show panel with only action buttons (no text header). */
    public void showButtons(String title, List<TextButton> buttons) {
        this.fullText = title;
        this.text = "";
        this.scrollOffset = 0;
        this.revealTimer = 0;
        this.revealedChars = 0;
        this.revealComplete = false;
        this.actionButtons.clear();
        this.actionButtons.addAll(buttons);
        layoutButtons();
        this.visible = true;
    }

    private void layoutButtons() {
        float buttonWidth = PANEL_WIDTH - PADDING * 2 - 40;
        float startY = PANEL_Y + PANEL_HEIGHT - 80 - (fullText.isEmpty() ? 0 : getFullTextHeight());
        // Stack buttons from top
        for (int i = 0; i < actionButtons.size(); i++) {
            float y = startY - i * (BUTTON_HEIGHT + BUTTON_SPACING);
            if (y < PANEL_Y + PADDING) y = PANEL_Y + PADDING;
            actionButtons.get(i).setPosition(PANEL_X + PADDING + 20, y);
            actionButtons.get(i).getBounds().width = buttonWidth;
            actionButtons.get(i).getBounds().height = BUTTON_HEIGHT;
        }
    }

    private float getTextHeight() {
        // Rough estimate: count lines
        String[] lines = wordWrap(text).split("\n");
        return lines.length * 22f;
    }

    private float getFullTextHeight() {
        // Estimate height using the complete text (before typewriter reveal)
        String[] lines = wordWrap(fullText).split("\n");
        return lines.length * 22f;
    }

    /** Update typewriter text reveal animation. */
    public void update(float delta) {
        if (revealComplete) return;
        revealTimer += delta;
        int target = (int)(revealTimer * charsPerSecond);
        if (target >= fullText.length()) {
            revealComplete = true;
            text = fullText;
            revealedChars = fullText.length();
        } else {
            text = fullText.substring(0, target);
            revealedChars = target;
        }
    }

    /** Skip the typewriter animation and show all text immediately. */
    public void skipReveal() {
        text = fullText;
        revealComplete = true;
        revealedChars = fullText.length();
    }

    public void hide() {
        this.visible = false;
        this.actionButtons.clear();
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        if (!visible) return;

        batch.setColor(Color.WHITE);

        // Panel background
        batch.draw(panelTexture, PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);

        // Border (top, bottom, left, right)
        batch.draw(borderTexture, PANEL_X, PANEL_Y + PANEL_HEIGHT - 2, PANEL_WIDTH, 2);
        batch.draw(borderTexture, PANEL_X, PANEL_Y, PANEL_WIDTH, 2);
        batch.draw(borderTexture, PANEL_X, PANEL_Y, 2, PANEL_HEIGHT);
        batch.draw(borderTexture, PANEL_X + PANEL_WIDTH - 2, PANEL_Y, 2, PANEL_HEIGHT);

        // Text
        String wrapped = wordWrap(text);
        String[] lines = wrapped.split("\n", -1);

        float textX = PANEL_X + PADDING;
        float textStartY = PANEL_Y + PANEL_HEIGHT - PADDING - 15 + scrollOffset;

        font.setColor(new Color(0.9f, 0.9f, 0.85f, 1));
        for (int i = 0; i < lines.length; i++) {
            float lineY = textStartY - i * 22;
            if (lineY > PANEL_Y + PANEL_HEIGHT - 10) continue; // clipped top
            if (lineY < PANEL_Y + PADDING + (actionButtons.isEmpty() ? 10 : actionButtons.size() * (BUTTON_HEIGHT + BUTTON_SPACING) + 20)) break; // clipped bottom
            font.draw(batch, lines[i], textX, lineY);
        }

        // Action buttons (only after text finishes revealing)
        if (revealComplete) {
            for (TextButton button : actionButtons) {
                button.render(batch, font);
            }
        }

        // Close button (always visible, allows closing mid-animation)
        closeButton.render(batch, font);

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
    }

    /** Handle click. Returns action ID if button clicked, "close" if close clicked, null otherwise. */
    public String handleClick(float x, float y) {
        if (!visible) return null;

        if (closeButton.contains(x, y)) {
            hide();
            return "close";
        }

        // Click during animation skips to full reveal
        if (!revealComplete) {
            if (x >= PANEL_X && x <= PANEL_X + PANEL_WIDTH && y >= PANEL_Y && y <= PANEL_Y + PANEL_HEIGHT) {
                skipReveal();
                return "panel_consumed";
            }
        }

        for (TextButton button : actionButtons) {
            if (button.isEnabled() && button.contains(x, y)) {
                return button.getActionId();
            }
        }

        // Click was inside panel but not on a button -- consume it (block room interaction)
        if (x >= PANEL_X && x <= PANEL_X + PANEL_WIDTH && y >= PANEL_Y && y <= PANEL_Y + PANEL_HEIGHT) {
            return "panel_consumed";
        }

        return null;
    }

    /** Handle hover for buttons. */
    public void handleHover(float x, float y) {
        if (!visible) return;
        closeButton.checkHover(x, y);
        for (TextButton button : actionButtons) {
            button.checkHover(x, y);
        }
    }

    /** Scroll text. */
    public void scroll(float amount) {
        if (!visible) return;
        scrollOffset += amount * 20;
        if (scrollOffset < 0) scrollOffset = 0;
        // Limit max scroll
        float maxScroll = Math.max(0, getTextHeight() - PANEL_HEIGHT + 100);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    public static float getCharsPerSecond() { return charsPerSecond; }
    public static void setCharsPerSecond(float speed) { charsPerSecond = Math.max(10f, Math.min(200f, speed)); }

    public boolean isVisible() { return visible; }

    public boolean containsPoint(float x, float y) {
        return visible && x >= PANEL_X && x <= PANEL_X + PANEL_WIDTH
            && y >= PANEL_Y && y <= PANEL_Y + PANEL_HEIGHT;
    }

    private String wordWrap(String input) {
        StringBuilder result = new StringBuilder();
        for (String paragraph : input.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                result.append("\n");
                continue;
            }
            String[] words = paragraph.split(" ");
            int lineLen = 0;
            for (String word : words) {
                if (lineLen + word.length() + 1 > MAX_LINE_CHARS && lineLen > 0) {
                    result.append("\n");
                    lineLen = 0;
                }
                if (lineLen > 0) {
                    result.append(" ");
                    lineLen++;
                }
                result.append(word);
                lineLen += word.length();
            }
            result.append("\n");
        }
        return result.toString();
    }

    public void dispose() {
        if (panelTexture != null) panelTexture.dispose();
        if (borderTexture != null) borderTexture.dispose();
    }
}
