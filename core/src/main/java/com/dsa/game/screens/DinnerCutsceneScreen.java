package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.dsa.game.DSAGame;
import com.dsa.game.ui.TextButton;
import com.dsa.game.ui.TextPanel;

import java.util.ArrayList;
import java.util.List;

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

    private final DSAGame game;
    private final BitmapFont font;
    private final TextPanel textPanel;
    private final Vector2 touchPos = new Vector2();

    public DinnerCutsceneScreen(DSAGame game) {
        this.game = game;

        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.2f);

        this.textPanel = new TextPanel();

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Begin investigation.", 0, 0, 200, 40, "begin_game"));
        textPanel.showButtons(DINNER_TEXT, buttons);

        setupInput();
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY);
                game.viewport.unproject(touchPos);
                String action = textPanel.handleClick(touchPos.x, touchPos.y);
                if ("begin_game".equals(action) || "close".equals(action)) {
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

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
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
    }
}
