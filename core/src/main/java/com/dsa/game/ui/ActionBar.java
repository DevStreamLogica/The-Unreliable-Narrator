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
 * Bottom action bar with buttons: INVENTORY, NOTEBOOK, SUSPECTS, HINT, ACCUSE
 */
public class ActionBar {

    private static final float BAR_Y = 0;
    private static final float BAR_HEIGHT = 45;
    private static final float BUTTON_MARGIN = 8;
    private static final float BUTTON_HEIGHT = 35;

    private final List<TextButton> buttons = new ArrayList<>();
    private Texture barTexture;

    public ActionBar() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(new Color(0.05f, 0.05f, 0.08f, 0.85f));
        p.fill();
        barTexture = new Texture(p);
        p.dispose();

        String[] labels = {"INVENTORY", "NOTEBOOK", "SUSPECTS", "HINT", "ACCUSE"};
        String[] actions = {"inventory", "notebook", "suspects", "hint", "accuse"};
        float totalWidth = DSAGame.SCREEN_WIDTH - 40;
        float buttonWidth = (totalWidth - BUTTON_MARGIN * (labels.length - 1)) / labels.length;
        float startX = 20;

        for (int i = 0; i < labels.length; i++) {
            float x = startX + i * (buttonWidth + BUTTON_MARGIN);
            buttons.add(new TextButton(labels[i], x, BAR_Y + 5, buttonWidth, BUTTON_HEIGHT, actions[i]));
        }
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        batch.setColor(Color.WHITE);
        batch.draw(barTexture, 0, BAR_Y, DSAGame.SCREEN_WIDTH, BAR_HEIGHT);

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

    public void dispose() {
        if (barTexture != null) barTexture.dispose();
    }
}
