package com.dsa.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dsa.game.DSAGame;
import com.dsa.game.state.GameState;
import com.dsa.game.systems.AwarenessSystem;

public class AwarenessMeter {

    private static final float STRIP_HEIGHT = 4;
    private static final float HOVER_ZONE = 20;

    private static final Color[] COLORS = {
        new Color(0.2f, 0.7f, 0.2f, 1),   // green
        new Color(0.8f, 0.8f, 0.2f, 1),   // yellow
        new Color(0.9f, 0.5f, 0.1f, 1),   // orange
        new Color(0.9f, 0.15f, 0.15f, 1)  // red
    };

    private Texture fillTexture;
    private Texture tooltipBgTexture;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private boolean hovered = false;

    public AwarenessMeter() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        fillTexture = new Texture(p);
        p.dispose();

        p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(new Color(0.08f, 0.12f, 0.12f, 0.9f));
        p.fill();
        tooltipBgTexture = new Texture(p);
        p.dispose();
    }

    public void handleHover(float gameY) {
        float stripY = DSAGame.SCREEN_HEIGHT - STRIP_HEIGHT;
        hovered = gameY >= stripY - HOVER_ZONE && gameY <= DSAGame.SCREEN_HEIGHT;
    }

    public void render(SpriteBatch batch, BitmapFont font, AwarenessSystem system, int awareness) {
        float stripY = DSAGame.SCREEN_HEIGHT - STRIP_HEIGHT;
        float fillWidth = DSAGame.SCREEN_WIDTH * system.getPercentage();
        Color barColor = COLORS[system.getColorIndex()];

        // Dark track behind the fill
        batch.setColor(new Color(0.05f, 0.05f, 0.05f, 0.4f));
        batch.draw(fillTexture, 0, stripY, DSAGame.SCREEN_WIDTH, STRIP_HEIGHT);

        // Colored fill
        batch.setColor(barColor);
        batch.draw(fillTexture, 0, stripY, fillWidth, STRIP_HEIGHT);
        batch.setColor(Color.WHITE);

        // Hover tooltip
        if (hovered) {
            String label = awareness + "/" + GameState.MAX_AWARENESS + " " + system.getLevelName().toUpperCase();
            glyphLayout.setText(font, label);

            float tooltipW = glyphLayout.width + 16;
            float tooltipH = glyphLayout.height + 10;
            float tooltipX = (DSAGame.SCREEN_WIDTH - tooltipW) / 2;
            float tooltipY = stripY - tooltipH - 4;

            // Background
            batch.setColor(Color.WHITE);
            batch.draw(tooltipBgTexture, tooltipX, tooltipY, tooltipW, tooltipH);

            // Border
            batch.setColor(new Color(0.6f, 0.55f, 0.35f, 0.7f));
            batch.draw(fillTexture, tooltipX, tooltipY, tooltipW, 1);
            batch.draw(fillTexture, tooltipX, tooltipY + tooltipH - 1, tooltipW, 1);
            batch.draw(fillTexture, tooltipX, tooltipY, 1, tooltipH);
            batch.draw(fillTexture, tooltipX + tooltipW - 1, tooltipY, 1, tooltipH);
            batch.setColor(Color.WHITE);

            // Text
            font.setColor(barColor);
            font.draw(batch, label, tooltipX + 8, tooltipY + tooltipH - 5);
            font.setColor(Color.WHITE);
        }
    }

    public void dispose() {
        if (fillTexture != null) fillTexture.dispose();
        if (tooltipBgTexture != null) tooltipBgTexture.dispose();
    }
}
