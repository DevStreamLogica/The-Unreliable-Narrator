package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.dsa.game.DSAGame;
import com.dsa.game.ui.TextButton;
import com.dsa.game.ui.TextPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DinnerCutsceneScreen implements Screen {

    private static final String DINNER_TEXT =
        "[November 15th, 1987.{p} 7:00 PM.]{P}\n\n" +

        "\"You'll{p} accept{p} the terms.\"\n" +
        "The man at the head of the table doesn't look up from his glass.{p} " +
        "Harold Vance.{p} Host.{p} Patriarch.{p} " +
        "The kind of man who states outcomes before negotiations begin.{P}\n\n" +

        "\"Of course.{P} We'll...{P} review them.\"\n" +
        "The guest adjusts his cuffs.{p} Marcus Blackwood,{p} " +
        "CEO of Blackwood Industries{p} -- here tonight because Harold summoned him{p} " +
        "and men like Marcus still come when Harold calls.{p} " +
        "The smile doesn't reach his eyes.{P}\n\n" +

        "\"James.\"{P}\n" +
        "The woman across the table reaches toward the man beside her.{p} " +
        "Margaret Vance.{p} She has been watching all evening.{p} " +
        "The candles.{p} The silverware.{p} The way her brother hasn't touched his food.{P}\n\n" +

        "Nothing.{P} James Vance stares at his plate.{p} " +
        "His fork moves.{p} Nothing reaches his mouth.{p} " +
        "He says nothing at all.{P}\n\n" +

        "\"More wine, sir?\"\n" +
        "Charles Webb,{p} twenty-eight,{p} " +
        "already refilling the glass before Harold can answer.{p} " +
        "Five years at Harold's side.{p} He has learned to anticipate everything.{P}\n\n" +

        "Harold doesn't look up.{p} He is already talking about something else{p} " +
        "-- precedent,{p} the courts,{p} what happens to companies that overreach.{P}\n\n" +

        "Beyond the dining room window,{p} a lantern moves slowly across the dark grounds.{p} " +
        "Daniel Hobbs,{p} groundskeeper.{p} Fifteen years at Vance Manor.{p} " +
        "Whatever is happening inside the house tonight{p} is none of his business.{P}\n\n" +

        "The clock on the sideboard reads quarter past seven.{P}\n\n" +

        "---\n\n" +

        "The following morning,{p} a body was found.{P}\n\n" +
        "The police called it an accident.{P}\n\n" +
        "It wasn't.{P}";

    // Portrait shown per page (narration focuses on each character in turn)
    private static final String[] PAGE_PORTRAITS = {
        null,                          // [November 15th, 1987...]
        "Harold Vance",                // "You'll accept the terms."
        "Marcus Blackwood",            // "Of course. We'll review them."
        "Margaret Vance",              // "James." — Margaret reaching out
        "James Vance",                 // James staring at his plate
        "Charles Webb",                // "More wine, sir?"
        "Harold Vance",                // Harold talking about courts
        "Daniel the Groundskeeper",    // lantern on the grounds
        null,                          // The clock reads quarter past seven
        null,                          // ---
        null,                          // The following morning...
        null,                          // The police called it an accident
        null,                          // It wasn't.
    };

    private final DSAGame game;
    private final BitmapFont font;
    private final TextPanel textPanel;
    private final Vector2 touchPos = new Vector2();
    private final Map<String, Texture> portraitTextures = new HashMap<>();
    private Texture currentPortrait = null;
    private float portraitAlpha = 0f;

    public DinnerCutsceneScreen(DSAGame game) {
        this.game = game;

        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.2f);

        this.textPanel = new TextPanel();

        String[][] mapping = {
            { "Harold Vance",             "characters/harold.png"  },
            { "James Vance",              "characters/james.png"   },
            { "Margaret Vance",           "characters/margaret.png"},
            { "Marcus Blackwood",         "characters/marcus.png"  },
            { "Charles Webb",             "characters/charles.png" },
            { "Daniel the Groundskeeper", "characters/daniel.png"  },
        };
        for (String[] e : mapping) {
            try {
                Texture tex = new Texture(Gdx.files.internal(e[1]));
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                portraitTextures.put(e[0], tex);
            } catch (Exception ex) { /* skip missing */ }
        }

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Begin investigation.", 0, 0, 200, 40, "begin_game"));
        textPanel.showDialogue(null, DINNER_TEXT, buttons);

        if (game.bgMusic != null && !game.bgMusic.isPlaying()) {
            game.bgMusic.play();
        }

        setupInput();
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY);
                game.viewport.unproject(touchPos);
                String action = textPanel.handleClick(touchPos.x, touchPos.y);
                if ("next_page".equals(action)) {
                    textPanel.nextPage();
                } else if ("begin_game".equals(action) || "close".equals(action)) {
                    game.setScreen(new GameScreen(game));
                }
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                touchPos.set(screenX, screenY);
                game.viewport.unproject(touchPos);
                textPanel.handleHover(touchPos.x, touchPos.y);
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new GameScreen(game));
                    return true;
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                textPanel.scroll(amountY);
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        textPanel.update(delta);

        // Resolve portrait for current page
        int page = textPanel.getCurrentPage();
        String portraitName = (page >= 0 && page < PAGE_PORTRAITS.length) ? PAGE_PORTRAITS[page] : null;
        Texture target = (portraitName != null) ? portraitTextures.get(portraitName) : null;
        if (target != currentPortrait) { currentPortrait = target; portraitAlpha = 0f; }
        float targetAlpha = (currentPortrait != null) ? 1f : 0f;
        portraitAlpha += (targetAlpha - portraitAlpha) * Math.min(1f, delta * 8f);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();

        if (currentPortrait != null && portraitAlpha > 0.01f) {
            float ph = 560f;
            float pw = ph * currentPortrait.getWidth() / (float) currentPortrait.getHeight();
            game.batch.setColor(1f, 1f, 1f, portraitAlpha);
            game.batch.draw(currentPortrait, 40f, 0f, pw, ph);
            game.batch.setColor(Color.WHITE);
        }

        textPanel.render(game.batch, font);
        game.batch.end();
    }

    @Override public void resize(int width, int height) { game.viewport.update(width, height); }
    @Override public void show() { setupInput(); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        font.dispose();
        textPanel.dispose();
        for (Texture tex : portraitTextures.values()) tex.dispose();
    }
}
