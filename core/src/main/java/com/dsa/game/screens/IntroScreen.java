package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dsa.game.DSAGame;
import com.dsa.game.ui.TextButton;
import com.dsa.game.ui.TextPanel;

import java.util.ArrayList;
import java.util.List;

public class IntroScreen implements Screen {

    private static final String INTRO_TEXT =
        "You weren't supposed to be here.\n\n" +
        "The road past Vance Manor was a detour{p} -- a wrong turn in the dark,{p} your headlights " +
        "sweeping across iron gates hanging open on rusted hinges.{p} You should have driven on.\n\n" +
        "Instead,{p} you stopped.\n\n" +
        "The manor has been abandoned for years.{p} Decades, maybe.{p} The locals don't talk about it.{p} " +
        "There are whispers{p} -- a murder,{p} an investigation that ended badly,{p} a private detective " +
        "who went in and never came out.{p} Old news.{p} Cold case.\n\n" +
        "But something made you stop at those gates.{p} Something made you push them open and walk " +
        "up the gravel path in the dark.{p} You can't explain it.{p} A pull,{p} almost physical.{p} An " +
        "inexplicable certainty that there is something here you need to find.\n\n" +
        "The front door swings open at your touch.{P}\n\n" +
        "Inside, the manor is silent.{p} Dusty.{p} Frozen in time.{p} Portraits stare down from panelled " +
        "walls.{p} A grandfather clock ticks on,{p} indifferent to the years.\n\n" +
        "And somewhere in the darkness ahead,{P} you hear the faint crackle of an old tape recorder.{P}\n\n" +
        "Someone is waiting for you.{P}\n\n" +
        "They have been waiting for a very long time.";

    private static final String LEAVE_TEXT =
        "You turn away from the open door.\n\n" +
        "The pull fades as you walk back down the gravel path. By the time you reach " +
        "the gate, it feels like nothing more than a strange moment on a dark road.\n\n" +
        "You don't look back.\n\n" +
        "Vance Manor watches you go.\n\n" +
        "Whatever is waiting inside will keep waiting.";

    private final DSAGame game;
    private final BitmapFont font;
    private final TextPanel textPanel;
    private final Vector2 touchPos = new Vector2();
    private boolean showingLeaveText = false;

    public IntroScreen(DSAGame game) {
        this.game = game;

        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.2f);

        this.textPanel = new TextPanel();

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Enter the manor.", 0, 0, 200, 40, "begin_game"));
        buttons.add(new TextButton("Ignore it and move on.", 0, 0, 200, 40, "leave"));
        textPanel.showButtons(INTRO_TEXT, buttons);

        setupInput();
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY);
                game.viewport.unproject(touchPos);
                String action = textPanel.handleClick(touchPos.x, touchPos.y);
                if ("begin_game".equals(action)) {
                    game.setScreen(new DinnerCutsceneScreen(game));
                } else if ("leave".equals(action)) {
                    showingLeaveText = true;
                    textPanel.show(LEAVE_TEXT);
                } else if ("close".equals(action) && showingLeaveText) {
                    game.setScreen(new TitleScreen(game));
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
                    if (showingLeaveText) {
                        game.setScreen(new TitleScreen(game));
                    } else {
                        game.setScreen(new TitleScreen(game));
                    }
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
