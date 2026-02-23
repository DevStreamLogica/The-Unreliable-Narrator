package com.dsa.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dsa.game.DSAGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mini-game for reconstructing torn/burned documents.
 * Player drags paper fragments to their correct positions.
 */
public class DocumentReconstructionGame {

    // Layout constants
    private static final float DESK_WIDTH = 500;
    private static final float DESK_HEIGHT = 300;
    private static final float DESK_X = (DSAGame.SCREEN_WIDTH - DESK_WIDTH) / 2;
    private static final float DESK_Y = (DSAGame.SCREEN_HEIGHT - DESK_HEIGHT) / 2 + 30;

    private static final float PIECE_WIDTH = 140;
    private static final float PIECE_HEIGHT = 50;
    private static final float SNAP_DISTANCE = 30f;

    private static final float SCATTER_MARGIN = 80;

    // State
    private boolean active = false;
    private boolean completed = false;
    private List<DocumentPiece> pieces;
    private DocumentPiece draggedPiece = null;
    private float dragOffsetX, dragOffsetY;

    // Textures
    private Texture pixelTexture;
    private Texture paperTexture;

    // UI
    private TextButton closeButton;
    private GlyphLayout layout;

    // Callback
    private Runnable onComplete;
    private Runnable onCancel;

    // Document content
    private String documentTitle;
    private String[] fragmentTexts;
    private String fullText;

    public DocumentReconstructionGame() {
        createTextures();
        layout = new GlyphLayout();

        closeButton = new TextButton("CANCEL",
            DSAGame.SCREEN_WIDTH / 2 - 60,
            DESK_Y - 70,
            120, 35, "cancel");
    }

    private void createTextures() {
        // 1x1 white pixel for drawing
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        pixelTexture = new Texture(p);
        p.dispose();

        // Paper texture (cream colored with slight noise)
        Pixmap paper = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                float noise = (float) (Math.random() * 0.05f);
                paper.setColor(0.95f - noise, 0.92f - noise, 0.85f - noise, 1f);
                paper.drawPixel(x, y);
            }
        }
        paperTexture = new Texture(paper);
        paper.dispose();
    }

    /**
     * Start the mini-game with the torn letter document.
     */
    public void startTornLetter(Runnable onComplete, Runnable onCancel) {
        this.documentTitle = "RECONSTRUCT THE TORN LETTER";
        this.fragmentTexts = new String[] {
            "...cannot",
            "allow this",
            "betrayal...",
            "the will",
            "must...",
            "James has..."
        };
        this.fullText = "\"...cannot allow this betrayal... the will must... James has...\"";
        this.onComplete = onComplete;
        this.onCancel = onCancel;

        initializePieces();
        active = true;
        completed = false;
    }

    private void initializePieces() {
        pieces = new ArrayList<>();

        // Calculate grid positions for correct placement (2 rows x 3 columns)
        float startX = DESK_X + (DESK_WIDTH - 3 * PIECE_WIDTH - 20) / 2;
        float startY = DESK_Y + DESK_HEIGHT - PIECE_HEIGHT - 40;

        for (int i = 0; i < fragmentTexts.length; i++) {
            int col = i % 3;
            int row = i / 3;
            float correctX = startX + col * (PIECE_WIDTH + 10);
            float correctY = startY - row * (PIECE_HEIGHT + 20);

            DocumentPiece piece = new DocumentPiece(i, fragmentTexts[i],
                PIECE_WIDTH, PIECE_HEIGHT, correctX, correctY);
            pieces.add(piece);
        }

        // Scatter pieces around the desk (but not on it initially)
        scatterPieces();
    }

    private void scatterPieces() {
        List<DocumentPiece> shuffled = new ArrayList<>(pieces);
        Collections.shuffle(shuffled);

        // Place pieces in scattered positions around the edges
        float[] scatterX = {
            SCATTER_MARGIN,
            DSAGame.SCREEN_WIDTH - PIECE_WIDTH - SCATTER_MARGIN,
            SCATTER_MARGIN + 100,
            DSAGame.SCREEN_WIDTH - PIECE_WIDTH - SCATTER_MARGIN - 100,
            SCATTER_MARGIN + 50,
            DSAGame.SCREEN_WIDTH - PIECE_WIDTH - SCATTER_MARGIN - 50
        };
        float[] scatterY = {
            DSAGame.SCREEN_HEIGHT - PIECE_HEIGHT - 100,
            DSAGame.SCREEN_HEIGHT - PIECE_HEIGHT - 150,
            SCATTER_MARGIN + 50,
            SCATTER_MARGIN + 100,
            DSAGame.SCREEN_HEIGHT / 2 + 150,
            DSAGame.SCREEN_HEIGHT / 2 - 150
        };

        for (int i = 0; i < shuffled.size(); i++) {
            DocumentPiece piece = shuffled.get(i);
            // Add some randomness
            float randX = (float) (Math.random() * 40 - 20);
            float randY = (float) (Math.random() * 40 - 20);
            piece.setPosition(scatterX[i] + randX, scatterY[i] + randY);
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void handleTouchDown(float x, float y) {
        if (!active || completed) return;

        // Check close button
        if (closeButton.contains(x, y)) {
            active = false;
            if (onCancel != null) onCancel.run();
            return;
        }

        // Check pieces (reverse order so top pieces are grabbed first)
        for (int i = pieces.size() - 1; i >= 0; i--) {
            DocumentPiece piece = pieces.get(i);
            if (!piece.isPlaced() && piece.contains(x, y)) {
                draggedPiece = piece;
                piece.setDragging(true);
                dragOffsetX = x - piece.getX();
                dragOffsetY = y - piece.getY();

                // Move to end of list (render on top)
                pieces.remove(i);
                pieces.add(piece);
                break;
            }
        }
    }

    public void handleTouchDragged(float x, float y) {
        if (!active || completed || draggedPiece == null) return;

        draggedPiece.setPosition(x - dragOffsetX, y - dragOffsetY);
    }

    public void handleTouchUp(float x, float y) {
        if (!active || completed || draggedPiece == null) return;

        draggedPiece.setDragging(false);

        // Check if close enough to correct position
        if (draggedPiece.isNearCorrectPosition(SNAP_DISTANCE)) {
            draggedPiece.setPlaced(true);
            checkCompletion();
        }

        draggedPiece = null;
    }

    public void handleHover(float x, float y) {
        if (!active) return;
        closeButton.checkHover(x, y);
    }

    private void checkCompletion() {
        for (DocumentPiece piece : pieces) {
            if (!piece.isPlaced()) return;
        }

        // All pieces placed!
        completed = true;

        // Delay the callback slightly so player sees the completed document
        // In a real implementation you'd use a timer, but we'll call immediately
        // The GameScreen will show the result text
    }

    /**
     * Called by GameScreen when player clicks after completion.
     */
    public void finish() {
        active = false;
        if (onComplete != null) onComplete.run();
    }

    /**
     * Called by GameScreen when the player cancels (e.g. ESC key).
     * Does NOT award evidence.
     */
    public void cancel() {
        active = false;
        if (onCancel != null) onCancel.run();
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        if (!active) return;

        // Dark overlay
        batch.setColor(0, 0, 0, 0.75f);
        batch.draw(pixelTexture, 0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);

        // Title
        batch.setColor(Color.WHITE);
        font.setColor(0.95f, 0.9f, 0.75f, 1f);
        layout.setText(font, documentTitle);
        font.draw(batch, documentTitle,
            (DSAGame.SCREEN_WIDTH - layout.width) / 2,
            DSAGame.SCREEN_HEIGHT - 40);

        // Instructions
        String instructions = completed ? "Document reconstructed! Click anywhere to continue."
            : "Drag the paper fragments to reconstruct the document.";
        font.setColor(0.8f, 0.8f, 0.75f, 1f);
        layout.setText(font, instructions);
        font.draw(batch, instructions,
            (DSAGame.SCREEN_WIDTH - layout.width) / 2,
            DSAGame.SCREEN_HEIGHT - 70);

        // Desk area (darker wood color)
        batch.setColor(0.25f, 0.18f, 0.12f, 1f);
        batch.draw(pixelTexture, DESK_X, DESK_Y, DESK_WIDTH, DESK_HEIGHT);

        // Desk border
        batch.setColor(0.4f, 0.3f, 0.2f, 1f);
        batch.draw(pixelTexture, DESK_X, DESK_Y, DESK_WIDTH, 2); // bottom
        batch.draw(pixelTexture, DESK_X, DESK_Y + DESK_HEIGHT - 2, DESK_WIDTH, 2); // top
        batch.draw(pixelTexture, DESK_X, DESK_Y, 2, DESK_HEIGHT); // left
        batch.draw(pixelTexture, DESK_X + DESK_WIDTH - 2, DESK_Y, 2, DESK_HEIGHT); // right

        // Target outlines for pieces (subtle guide)
        if (!completed) {
            batch.setColor(0.35f, 0.28f, 0.2f, 0.5f);
            for (DocumentPiece piece : pieces) {
                if (!piece.isPlaced()) {
                    batch.draw(pixelTexture,
                        piece.getCorrectPosition().x,
                        piece.getCorrectPosition().y,
                        PIECE_WIDTH, PIECE_HEIGHT);
                }
            }
        }

        // Render pieces
        for (DocumentPiece piece : pieces) {
            renderPiece(batch, font, piece);
        }

        // Show full text when completed
        if (completed) {
            font.setColor(0.2f, 0.15f, 0.1f, 1f);
            layout.setText(font, fullText);
            font.draw(batch, fullText,
                (DSAGame.SCREEN_WIDTH - layout.width) / 2,
                DESK_Y - 20);
        }

        // Close/cancel button (hide when completed)
        if (!completed) {
            closeButton.render(batch, font);
        }

        // Reset colors
        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    private void renderPiece(SpriteBatch batch, BitmapFont font, DocumentPiece piece) {
        float x = piece.getX();
        float y = piece.getY();
        float w = piece.getWidth();
        float h = piece.getHeight();

        // Paper background
        if (piece.isPlaced()) {
            // Green tint for placed pieces
            batch.setColor(0.85f, 0.95f, 0.85f, 1f);
        } else if (piece.isDragging()) {
            // Slight highlight when dragging
            batch.setColor(1f, 1f, 0.95f, 1f);
        } else {
            // Normal cream paper
            batch.setColor(0.95f, 0.92f, 0.85f, 1f);
        }
        batch.draw(paperTexture, x, y, w, h);

        // Jagged edges (darker lines)
        batch.setColor(0.6f, 0.55f, 0.45f, 0.8f);
        float[] offsets = piece.getJaggedEdgeOffsets();

        // Draw jagged borders using small rectangles
        // Top edge
        for (int i = 0; i < 8; i++) {
            float px = x + i * (w / 8);
            float py = y + h - 1 + offsets[i];
            batch.draw(pixelTexture, px, py, w / 8, 2);
        }
        // Bottom edge
        for (int i = 0; i < 8; i++) {
            float px = x + i * (w / 8);
            float py = y + offsets[8 + i];
            batch.draw(pixelTexture, px, py, w / 8, 2);
        }
        // Left edge
        for (int i = 0; i < 8; i++) {
            float px = x + offsets[16 + i];
            float py = y + i * (h / 8);
            batch.draw(pixelTexture, px, py, 2, h / 8);
        }
        // Right edge
        for (int i = 0; i < 8; i++) {
            float px = x + w - 1 + offsets[24 + i];
            float py = y + i * (h / 8);
            batch.draw(pixelTexture, px, py, 2, h / 8);
        }

        // Burn marks (small dark spots) for atmosphere
        batch.setColor(0.3f, 0.25f, 0.2f, 0.3f);
        for (int i = 0; i < 3; i++) {
            float bx = x + 10 + (i * 45) + offsets[i] * 2;
            float by = y + 5 + offsets[i + 3] * 2;
            batch.draw(pixelTexture, bx, by, 8 + offsets[i], 4);
        }

        // Text on the piece
        font.setColor(0.15f, 0.1f, 0.05f, 1f);
        layout.setText(font, piece.getText());
        float textX = x + (w - layout.width) / 2;
        float textY = y + (h + layout.height) / 2;
        font.draw(batch, piece.getText(), textX, textY);

        // Placed indicator (subtle glow)
        if (piece.isPlaced()) {
            batch.setColor(0.4f, 0.8f, 0.4f, 0.3f);
            batch.draw(pixelTexture, x - 2, y - 2, w + 4, h + 4);
        }

        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        if (pixelTexture != null) pixelTexture.dispose();
        if (paperTexture != null) paperTexture.dispose();
    }
}
