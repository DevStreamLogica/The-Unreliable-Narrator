package com.dsa.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dsa.game.DSAGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Overlay panel that renders on top of the room view.
 * Two modes:
 *   Normal mode  — full-height panel used for examinations, tape transcripts, menus.
 *   Dialogue mode — compact 30%-height panel positioned at bottom with a speaker-name
 *                   header, paragraph pagination, and a "Next" button.
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
    private float ctcTimer = 0f;
    private static float charsPerSecond = 40f;
    private String fullText = "";
    // Processed text with pause markers stripped
    private String displayText = "";
    // Pre-wrapped displayText for stable layout during typewriter reveal
    private String cachedWrapped = "";
    private final Map<Integer, Float> pausePositions = new HashMap<>();
    private float currentPause = 0;

    // ── Normal-mode dimensions ──────────────────────────────────────────────
    private static final float PANEL_X      = 40;
    private static final float PANEL_Y      = 20;
    private static final float PANEL_WIDTH  = DSAGame.SCREEN_WIDTH - 80;
    private static final float PANEL_HEIGHT = DSAGame.SCREEN_HEIGHT * 0.55f;
    private static final float PADDING      = 20;
    private static final float BUTTON_HEIGHT   = 35;
    private static final float BUTTON_SPACING  = 8;
    private static final float CLOSE_SIZE      = 30;
    private static final int   MAX_LINE_CHARS  = 85;

    // ── Dialogue-mode dimensions ────────────────────────────────────────────
    private static final float DLG_HEIGHT      = DSAGame.SCREEN_HEIGHT * 0.30f;   // 216 px
    private static final float DLG_PANEL_Y     = 10;
    private static final float DLG_PANEL_X     = 40;
    private static final float DLG_PANEL_WIDTH = DSAGame.SCREEN_WIDTH - 80;
    private static final float DLG_HEADER_H    = 32f;
    private static final float DLG_PADDING     = 14;
    private static final int   DLG_LINE_CHARS  = 90;

    // ── Dialogue state ──────────────────────────────────────────────────────
    private boolean dialogueMode = false;
    private String  speakerName  = null;
    private String[] dialoguePages = null;
    private int      currentPage   = 0;
    private List<TextButton> afterButtons = new ArrayList<>();
    private TextButton nextButton;

    // ── Speaker name colours ─────────────────────────────────────────────────
    private static final java.util.Map<String, Color> SPEAKER_COLORS = new java.util.HashMap<>();
    static {
        // Narrator / system voices
        SPEAKER_COLORS.put("The Narrator",        new Color(0.95f, 0.88f, 0.55f, 1f)); // gold
        SPEAKER_COLORS.put("Observation",          new Color(0.78f, 0.88f, 0.98f, 1f)); // pale blue
        SPEAKER_COLORS.put("Case Closed",          new Color(0.55f, 0.92f, 0.65f, 1f)); // green
        // Suspects / live characters
        SPEAKER_COLORS.put("James Vance",          new Color(0.55f, 0.72f, 0.95f, 1f)); // blue
        SPEAKER_COLORS.put("Margaret Vance",       new Color(0.95f, 0.58f, 0.65f, 1f)); // rose
        SPEAKER_COLORS.put("Daniel the Groundskeeper", new Color(0.78f, 0.88f, 0.60f, 1f)); // olive
        SPEAKER_COLORS.put("Marcus Blackwood",     new Color(0.82f, 0.65f, 0.95f, 1f)); // violet
        SPEAKER_COLORS.put("Charles Webb",         new Color(0.55f, 0.90f, 0.88f, 1f)); // teal
        // Tape voices (same faces, recorded)
        SPEAKER_COLORS.put("Harold Vance",         new Color(0.92f, 0.80f, 0.55f, 1f)); // amber
        SPEAKER_COLORS.put("Arthur Hollis",         new Color(0.80f, 0.80f, 0.80f, 1f)); // silver
        SPEAKER_COLORS.put("Police Recording",     new Color(0.70f, 0.80f, 0.75f, 1f)); // muted teal
    }

    // ── Textures ────────────────────────────────────────────────────────────
    private Texture panelTexture;
    private Texture borderTexture;
    private Texture headerTexture;

    public TextPanel() {
        generateTextures();
        closeButton = new TextButton("X",
            PANEL_X + PANEL_WIDTH - CLOSE_SIZE - 10,
            PANEL_Y + PANEL_HEIGHT - CLOSE_SIZE - 10,
            CLOSE_SIZE, CLOSE_SIZE, "close");
        nextButton = new TextButton("Next \u25ba",
            DLG_PANEL_X + DLG_PANEL_WIDTH - 120 - DLG_PADDING,
            DLG_PANEL_Y + DLG_PADDING,
            110, BUTTON_HEIGHT, "next_page");
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

        p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(new Color(0.14f, 0.14f, 0.20f, 0.97f));
        p.fill();
        headerTexture = new Texture(p);
        p.dispose();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Public API — normal mode
    // ────────────────────────────────────────────────────────────────────────

    /** Show panel with text only. */
    public void show(String text) {
        dialogueMode = false;
        repositionCloseButton(false);
        this.fullText = text;
        processText(text);
        this.text = "";
        this.scrollOffset = 0;
        this.revealTimer = 0;
        this.revealedChars = 0;
        this.revealComplete = false;
        this.ctcTimer = 0f;
        this.currentPause = 0;
        this.actionButtons.clear();
        this.visible = true;
    }

    /** Show panel with text and action buttons. */
    public void show(String text, List<TextButton> buttons) {
        dialogueMode = false;
        repositionCloseButton(false);
        this.fullText = text;
        processText(text);
        this.text = "";
        this.scrollOffset = 0;
        this.revealTimer = 0;
        this.revealedChars = 0;
        this.revealComplete = false;
        this.ctcTimer = 0f;
        this.currentPause = 0;
        this.actionButtons.clear();
        this.actionButtons.addAll(buttons);
        layoutButtons(false);
        this.visible = true;
    }

    /** Show panel with only action buttons (no scrolling text). */
    public void showButtons(String title, List<TextButton> buttons) {
        dialogueMode = false;
        repositionCloseButton(false);
        this.fullText = title;
        processText(title);
        this.text = "";
        this.scrollOffset = 0;
        this.revealTimer = 0;
        this.revealedChars = 0;
        this.revealComplete = false;
        this.ctcTimer = 0f;
        this.currentPause = 0;
        this.actionButtons.clear();
        this.actionButtons.addAll(buttons);
        layoutButtons(false);
        this.visible = true;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Public API — dialogue mode
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Show in dialogue mode: compact 30%-height panel with speaker name header.
     * Text is split into pages by double-newline paragraphs.
     * afterButtons are shown on the last page in place of the Next button.
     */
    public void showDialogue(String speaker, String rawText, List<TextButton> afterBtns) {
        dialogueMode = true;
        speakerName = speaker;
        afterButtons = new ArrayList<>(afterBtns);

        // Split text into pages by paragraph breaks
        String[] parts = rawText.split("\n\n");
        List<String> pages = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) pages.add(trimmed);
        }
        if (pages.isEmpty()) pages.add(rawText.trim());
        dialoguePages = pages.toArray(new String[0]);
        currentPage = 0;

        repositionCloseButton(true);
        startDialoguePage();
        this.visible = true;
    }

    /** Advance to the next dialogue page, or close if on the last page. */
    public void nextPage() {
        if (!dialogueMode || dialoguePages == null) return;
        currentPage++;
        if (currentPage >= dialoguePages.length) {
            // Last page passed — hide dialogue
            hide();
        } else {
            startDialoguePage();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Internal helpers
    // ────────────────────────────────────────────────────────────────────────

    private void startDialoguePage() {
        String pageText = dialoguePages[currentPage];
        this.fullText = pageText;
        processText(pageText);
        this.text = "";
        this.scrollOffset = 0;
        this.revealTimer = 0;
        this.revealedChars = 0;
        this.revealComplete = false;
        this.ctcTimer = 0f;
        this.currentPause = 0;
        this.actionButtons.clear();

        boolean isLastPage = (currentPage == dialoguePages.length - 1);
        if (isLastPage && !afterButtons.isEmpty()) {
            this.actionButtons.addAll(afterButtons);
            layoutButtons(true);
        }
        // nextButton is always shown in dialogue mode (handled in render)
    }

    private void repositionCloseButton(boolean dialogue) {
        float px = dialogue ? DLG_PANEL_X : PANEL_X;
        float py = dialogue ? DLG_PANEL_Y : PANEL_Y;
        float pw = dialogue ? DLG_PANEL_WIDTH : PANEL_WIDTH;
        float ph = dialogue ? DLG_HEIGHT : PANEL_HEIGHT;
        closeButton.setPosition(px + pw - CLOSE_SIZE - 10, py + ph - CLOSE_SIZE - 10);
        closeButton.getBounds().width  = CLOSE_SIZE;
        closeButton.getBounds().height = CLOSE_SIZE;
    }

    /**
     * Parses {p} (0.35s) and {P} (0.7s) pause markers, building displayText.
     */
    private void processText(String raw) {
        StringBuilder display = new StringBuilder();
        pausePositions.clear();
        int i = 0;
        while (i < raw.length()) {
            if (i + 2 < raw.length() && raw.charAt(i) == '{' && raw.charAt(i + 2) == '}') {
                char code = raw.charAt(i + 1);
                if (code == 'p') {
                    pausePositions.put(display.length(), 0.35f);
                    i += 3;
                    continue;
                } else if (code == 'P') {
                    pausePositions.put(display.length(), 0.7f);
                    i += 3;
                    continue;
                }
            }
            display.append(raw.charAt(i));
            i++;
        }
        displayText = display.toString();
        int wrapChars = dialogueMode ? DLG_LINE_CHARS : MAX_LINE_CHARS;
        cachedWrapped = wordWrap(displayText, wrapChars);
    }

    private void layoutButtons(boolean dialogue) {
        float panelX = dialogue ? DLG_PANEL_X : PANEL_X;
        float panelY = dialogue ? DLG_PANEL_Y : PANEL_Y;
        float panelW = dialogue ? DLG_PANEL_WIDTH : PANEL_WIDTH;
        float pad    = dialogue ? DLG_PADDING : PADDING;

        float buttonWidth = panelW - pad * 2 - 40;
        float bottomY = panelY + pad;
        for (int i = actionButtons.size() - 1; i >= 0; i--) {
            float y = bottomY + (actionButtons.size() - 1 - i) * (BUTTON_HEIGHT + BUTTON_SPACING);
            actionButtons.get(i).setPosition(panelX + pad + 20, y);
            actionButtons.get(i).getBounds().width  = buttonWidth;
            actionButtons.get(i).getBounds().height = BUTTON_HEIGHT;
        }
    }

    private float getTextHeight() {
        return cachedWrapped.split("\n").length * 22f;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Update / typewriter
    // ────────────────────────────────────────────────────────────────────────

    public void update(float delta) {
        if (revealComplete) {
            ctcTimer += delta;
            return;
        }

        if (currentPause > 0) {
            currentPause -= delta;
            return;
        }

        revealTimer += delta;
        int target = (int)(revealTimer * charsPerSecond);

        if (target >= displayText.length()) {
            revealComplete = true;
            text = displayText;
            revealedChars = displayText.length();
        } else {
            for (int i = revealedChars; i < target; i++) {
                Float pause = pausePositions.get(i);
                if (pause != null) {
                    revealedChars = i;
                    text = displayText.substring(0, i);
                    currentPause = pause;
                    revealTimer = (float) i / charsPerSecond;
                    pausePositions.remove(i);
                    return;
                }
            }
            text = displayText.substring(0, target);
            revealedChars = target;
        }
    }

    public void skipReveal() {
        text = displayText;
        revealComplete = true;
        revealedChars = displayText.length();
        currentPause = 0;
    }

    public void hide() {
        this.visible = false;
        this.actionButtons.clear();
        this.dialogueMode = false;
        this.dialoguePages = null;
        this.speakerName = null;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Render
    // ────────────────────────────────────────────────────────────────────────

    public void render(SpriteBatch batch, BitmapFont font) {
        if (!visible) return;

        if (dialogueMode) {
            renderDialogue(batch, font);
        } else {
            renderNormal(batch, font);
        }
    }

    /**
     * Draws text using the pre-wrapped cachedWrapped layout so line breaks are
     * stable during typewriter reveal (no words jumping between lines mid-reveal).
     * Only text.length() characters of displayText are shown.
     */
    private void drawRevealedLines(SpriteBatch batch, BitmapFont font,
                                    float textX, float textStartY,
                                    float topClip, float bottomClip) {
        String[] fullLines = cachedWrapped.split("\n", -1);
        int charsToShow = text.length();
        int consumed = 0;

        for (int i = 0; i < fullLines.length; i++) {
            if (consumed >= charsToShow) break;

            float lineY = textStartY - i * 22f;
            if (lineY > topClip) continue;
            if (lineY < bottomClip) break;

            String line = fullLines[i];
            int available = charsToShow - consumed;
            String visible = available >= line.length() ? line : line.substring(0, available);

            if (!visible.isEmpty()) {
                font.draw(batch, visible, textX, lineY);
            }

            consumed += line.length();
            // Consume the delimiter in displayText that became this line-break:
            // a space (word-wrap split) or a \n (original paragraph break)
            if (consumed < displayText.length()) {
                char sep = displayText.charAt(consumed);
                if (sep == ' ' || sep == '\n') consumed++;
            }
        }
    }

    private void renderNormal(SpriteBatch batch, BitmapFont font) {
        batch.setColor(Color.WHITE);

        batch.draw(panelTexture, PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);

        batch.draw(borderTexture, PANEL_X, PANEL_Y + PANEL_HEIGHT - 2, PANEL_WIDTH, 2);
        batch.draw(borderTexture, PANEL_X, PANEL_Y, PANEL_WIDTH, 2);
        batch.draw(borderTexture, PANEL_X, PANEL_Y, 2, PANEL_HEIGHT);
        batch.draw(borderTexture, PANEL_X + PANEL_WIDTH - 2, PANEL_Y, 2, PANEL_HEIGHT);

        float textX      = PANEL_X + PADDING;
        float textStartY = PANEL_Y + PANEL_HEIGHT - PADDING - 15 + scrollOffset;
        float clipBottom = PANEL_Y + PADDING + (actionButtons.isEmpty() ? 10 : actionButtons.size() * (BUTTON_HEIGHT + BUTTON_SPACING) + 20);

        font.setColor(new Color(0.9f, 0.9f, 0.85f, 1));
        drawRevealedLines(batch, font, textX, textStartY, PANEL_Y + PANEL_HEIGHT - 10, clipBottom);

        if (revealComplete) {
            for (TextButton button : actionButtons) {
                button.render(batch, font);
            }
        }

        closeButton.render(batch, font);

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
    }

    private void renderDialogue(SpriteBatch batch, BitmapFont font) {
        float px = DLG_PANEL_X;
        float py = DLG_PANEL_Y;
        float pw = DLG_PANEL_WIDTH;
        float ph = DLG_HEIGHT;

        batch.setColor(Color.WHITE);

        // Body background
        batch.draw(panelTexture, px, py, pw, ph - DLG_HEADER_H);

        // Header background (lighter)
        batch.draw(headerTexture, px, py + ph - DLG_HEADER_H, pw, DLG_HEADER_H);

        // Border
        batch.draw(borderTexture, px, py + ph - 2, pw, 2);
        batch.draw(borderTexture, px, py, pw, 2);
        batch.draw(borderTexture, px, py, 2, ph);
        batch.draw(borderTexture, px + pw - 2, py, 2, ph);
        // Separator line below header
        batch.draw(borderTexture, px, py + ph - DLG_HEADER_H, pw, 1);

        // Speaker name in header
        if (speakerName != null) {
            GlyphLayout gl = new GlyphLayout(font, speakerName);
            Color nameColor = SPEAKER_COLORS.getOrDefault(speakerName, new Color(0.95f, 0.90f, 0.70f, 1f));
            font.setColor(nameColor);
            font.draw(batch, speakerName,
                px + DLG_PADDING,
                py + ph - DLG_HEADER_H + (DLG_HEADER_H + gl.height) / 2f);
        }

        // Dialogue text
        float textX      = px + DLG_PADDING;
        float textStartY = py + ph - DLG_HEADER_H - DLG_PADDING - 6;
        float bottomNextY = py + DLG_PADDING + BUTTON_HEIGHT + BUTTON_SPACING;

        font.setColor(new Color(0.9f, 0.9f, 0.85f, 1));
        drawRevealedLines(batch, font, textX, textStartY, py + ph - DLG_HEADER_H - 4, bottomNextY);

        // CTC (click-to-continue) blinking indicator when text is fully revealed
        if (revealComplete) {
            float blink = (float)(Math.sin(ctcTimer * Math.PI * 2.0) * 0.5 + 0.5);
            font.setColor(0.95f, 0.90f, 0.65f, 0.35f + blink * 0.65f);
            font.draw(batch, "\u25bc", px + pw / 2f - 5f, bottomNextY + 4f);
        }

        if (revealComplete) {
            boolean isLastPage = (currentPage == dialoguePages.length - 1);

            // Show after-buttons on last page if any
            if (isLastPage && !actionButtons.isEmpty()) {
                for (TextButton button : actionButtons) {
                    button.render(batch, font);
                }
            }

            // Show Next button (not on last page if there are after-buttons replacing it,
            // but still show it if there are no after-buttons on last page)
            boolean showNext = !isLastPage || actionButtons.isEmpty();
            if (showNext) {
                nextButton.setPosition(px + pw - 120 - DLG_PADDING, py + DLG_PADDING);
                nextButton.getBounds().width  = 110;
                nextButton.getBounds().height = BUTTON_HEIGHT;
                nextButton.render(batch, font);
            }
        }

        closeButton.render(batch, font);

        font.setColor(Color.WHITE);
        batch.setColor(Color.WHITE);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Input handling
    // ────────────────────────────────────────────────────────────────────────

    /** Handle click. Returns action ID if button clicked, "close" or null otherwise. */
    public String handleClick(float x, float y) {
        if (!visible) return null;

        float px = dialogueMode ? DLG_PANEL_X : PANEL_X;
        float py = dialogueMode ? DLG_PANEL_Y : PANEL_Y;
        float pw = dialogueMode ? DLG_PANEL_WIDTH : PANEL_WIDTH;
        float ph = dialogueMode ? DLG_HEIGHT : PANEL_HEIGHT;

        if (closeButton.contains(x, y)) {
            hide();
            return "close";
        }

        // Click during animation: skip to full reveal
        if (!revealComplete) {
            if (x >= px && x <= px + pw && y >= py && y <= py + ph) {
                skipReveal();
                return "panel_consumed";
            }
        }

        // Dialogue-mode button checks
        if (dialogueMode && revealComplete) {
            boolean isLastPage = (currentPage == dialoguePages.length - 1);
            if (!isLastPage || actionButtons.isEmpty()) {
                if (nextButton.contains(x, y)) {
                    return "next_page";
                }
            }
            for (TextButton button : actionButtons) {
                if (button.isEnabled() && button.contains(x, y)) {
                    return button.getActionId();
                }
            }
        }

        // Normal mode action buttons
        if (!dialogueMode) {
            for (TextButton button : actionButtons) {
                if (button.isEnabled() && button.contains(x, y)) {
                    return button.getActionId();
                }
            }
        }

        if (x >= px && x <= px + pw && y >= py && y <= py + ph) {
            return "panel_consumed";
        }

        return null;
    }

    public void handleHover(float x, float y) {
        if (!visible) return;
        closeButton.checkHover(x, y);
        for (TextButton button : actionButtons) {
            button.checkHover(x, y);
        }
        if (dialogueMode && revealComplete) {
            nextButton.checkHover(x, y);
        }
    }

    public void scroll(float amount) {
        if (!visible || dialogueMode) return;
        scrollOffset += amount * 20;
        if (scrollOffset < 0) scrollOffset = 0;
        float maxScroll = Math.max(0, getTextHeight() - PANEL_HEIGHT + 100);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Utilities
    // ────────────────────────────────────────────────────────────────────────

    public static float getCharsPerSecond() { return charsPerSecond; }
    public static void setCharsPerSecond(float speed) { charsPerSecond = Math.max(10f, Math.min(200f, speed)); }

    public boolean isVisible() { return visible; }
    public boolean isDialogueMode() { return dialogueMode; }
    public String getSpeaker() { return speakerName; }
    public void setSpeaker(String speaker) { this.speakerName = speaker; }
    public int getCurrentPage() { return currentPage; }
    public String getCurrentPageText() {
        if (!dialogueMode || dialoguePages == null || currentPage >= dialoguePages.length) return null;
        return dialoguePages[currentPage];
    }

    public boolean containsPoint(float x, float y) {
        if (!visible) return false;
        if (dialogueMode) {
            return x >= DLG_PANEL_X && x <= DLG_PANEL_X + DLG_PANEL_WIDTH
                && y >= DLG_PANEL_Y && y <= DLG_PANEL_Y + DLG_HEIGHT;
        }
        return x >= PANEL_X && x <= PANEL_X + PANEL_WIDTH
            && y >= PANEL_Y && y <= PANEL_Y + PANEL_HEIGHT;
    }

    private String wordWrap(String input, int maxChars) {
        StringBuilder result = new StringBuilder();
        for (String paragraph : input.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                result.append("\n");
                continue;
            }
            String[] words = paragraph.split(" ");
            int lineLen = 0;
            for (String word : words) {
                if (lineLen + word.length() + 1 > maxChars && lineLen > 0) {
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
        if (panelTexture  != null) panelTexture.dispose();
        if (borderTexture != null) borderTexture.dispose();
        if (headerTexture != null) headerTexture.dispose();
    }
}
