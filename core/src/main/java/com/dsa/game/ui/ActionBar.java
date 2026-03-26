package com.dsa.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dsa.game.DSAGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom action bar with buttons: INVENTORY
 */
public class ActionBar {

    private static final float BAR_Y = 0;
    private static final float BAR_HEIGHT = 36;
    private static final float BUTTON_MARGIN = 8;
    private static final float BUTTON_HEIGHT = 28;
    private static final float BUTTON_WIDTH = 100;

    private final List<TextButton> buttons = new ArrayList<>();
    private Texture barTexture;
    private float clusterX;
    private float clusterWidth;

    public ActionBar() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(new Color(0.05f, 0.05f, 0.08f, 0.6f));
        p.fill();
        barTexture = new Texture(p);
        p.dispose();

        String[] labels = {"INVENTORY"};
        String[] actions = {"inventory"};

        clusterWidth = labels.length * BUTTON_WIDTH + (labels.length - 1) * BUTTON_MARGIN;
        clusterX = (DSAGame.SCREEN_WIDTH - clusterWidth) / 2;

        for (int i = 0; i < labels.length; i++) {
            float x = clusterX + i * (BUTTON_WIDTH + BUTTON_MARGIN);
            float y = BAR_Y + (BAR_HEIGHT - BUTTON_HEIGHT) / 2;
            buttons.add(new TextButton(labels[i], x, y, BUTTON_WIDTH, BUTTON_HEIGHT, actions[i]));
        }
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        batch.setColor(Color.WHITE);
        batch.draw(barTexture, clusterX - 12, BAR_Y, clusterWidth + 24, BAR_HEIGHT);

        for (TextButton button : buttons) {
            button.render(batch, font);
        }
    }

    /** Returns action ID if a button was clicked, null otherwise. */
    public String handleClick(float x, float y) {
        for (TextButton button : buttons) {
            if (button.isEnabled() && button.contains(x, y)) {
                return button.getActionId();
            }
        }
        return null;
    }

    public void handleHover(float x, float y) {
        for (TextButton button : buttons) {
            button.checkHover(x, y);
        }
    }

    public boolean containsPoint(float x, float y) {
        return y >= BAR_Y && y <= BAR_Y + BAR_HEIGHT;
    }

    public float getBarHeight() {
        return BAR_HEIGHT;
    }

    public void dispose() {
        if (barTexture != null) barTexture.dispose();
    }
}
