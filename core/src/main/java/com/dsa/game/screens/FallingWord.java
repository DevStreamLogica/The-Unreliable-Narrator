package com.dsa.game.screens;

import com.badlogic.gdx.math.Rectangle;

public class FallingWord {

    public static final float WIDTH  = 220f;
    public static final float HEIGHT = 60f;

    public float x, y;
    public final float   speed;
    public final String  text;
    public final boolean isTrue;
    public final boolean isMandatory; // KEY EVIDENCE word — gold border, must be caught
    public boolean caught = false;

    /** Convenience constructor for non-mandatory words. */
    public FallingWord(String text, boolean isTrue, float centerX, float startY, float speed) {
        this(text, isTrue, centerX, startY, speed, false);
    }

    /** Full constructor. centerX — the word container is centred on this X position. */
    public FallingWord(String text, boolean isTrue, float centerX, float startY, float speed, boolean isMandatory) {
        this.text        = text;
        this.isTrue      = isTrue;
        this.isMandatory = isMandatory;
        this.x           = centerX - WIDTH / 2f;
        this.y           = startY;
        this.speed       = speed;
    }

    public void update(float delta) {
        y -= speed * delta;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public boolean isOffScreen() {
        return y + HEIGHT < 0;
    }
}
