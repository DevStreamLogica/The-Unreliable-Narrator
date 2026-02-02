package com.dsa.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dsa.game.DSAGame;
import com.dsa.game.systems.AwarenessSystem;

public class AwarenessMeter {

    private static final float BAR_X = DSAGame.SCREEN_WIDTH - 220;
    private static final float BAR_Y = DSAGame.SCREEN_HEIGHT - 35;
    private static final float BAR_WIDTH = 200;
    private static final float BAR_HEIGHT = 20;

    private static final Color[] COLORS = {
        new Color(0.2f, 0.7f, 0.2f, 1),   // green
        new Color(0.8f, 0.8f, 0.2f, 1),   // yellow
        new Color(0.9f, 0.5f, 0.1f, 1),   // orange
        new Color(0.9f, 0.15f, 0.15f, 1)  // red
    };

    private Texture bgTexture;
    private Texture fillTexture;

    public AwarenessMeter() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(new Color(0, 0, 0, 0.7f));
        p.fill();
        bgTexture = new Texture(p);
        p.dispose();

        p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        fillTexture = new Texture(p);
        p.dispose();
    }

    public void render(SpriteBatch batch, BitmapFont font, AwarenessSystem system, int awareness) {
        batch.setColor(Color.WHITE);

        // Background
        batch.draw(bgTexture, BAR_X - 5, BAR_Y - 5, BAR_WIDTH + 10, BAR_HEIGHT + 25);

        // Fill bar
        float fillWidth = BAR_WIDTH * system.getPercentage();
        Color barColor = COLORS[system.getColorIndex()];
        batch.setColor(barColor);
        batch.draw(fillTexture, BAR_X, BAR_Y, fillWidth, BAR_HEIGHT);
        batch.setColor(Color.WHITE);

        // Border
        batch.setColor(new Color(0.5f, 0.5f, 0.5f, 0.8f));
        batch.draw(fillTexture, BAR_X, BAR_Y, BAR_WIDTH, 1);                        // bottom
        batch.draw(fillTexture, BAR_X, BAR_Y + BAR_HEIGHT, BAR_WIDTH, 1);           // top
        batch.draw(fillTexture, BAR_X, BAR_Y, 1, BAR_HEIGHT);                        // left
        batch.draw(fillTexture, BAR_X + BAR_WIDTH, BAR_Y, 1, BAR_HEIGHT);           // right
        batch.setColor(Color.WHITE);

        // Text label
        String label = awareness + "/" + com.dsa.game.state.GameState.MAX_AWARENESS + " " + system.getLevelName();
        font.setColor(barColor);
        font.draw(batch, label, BAR_X, BAR_Y + BAR_HEIGHT + 17);
        font.setColor(Color.WHITE);
    }

    public void dispose() {
        if (bgTexture != null) bgTexture.dispose();
        if (fillTexture != null) fillTexture.dispose();
    }
}
