package com.dsa.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dsa.game.screens.EscapeScreen;
import com.dsa.game.screens.TitleScreen;

public class DSAGame extends Game {
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static boolean startInEscapeMode = false;

    public SpriteBatch batch;
    public OrthographicCamera camera;
    public FitViewport viewport;
    public Music bgMusic;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
        viewport.apply();
        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        camera.update();

        batch = new SpriteBatch();

        if (Gdx.files.internal("music/manor_ambience.mp3").exists()) {
            bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music/manor_ambience.mp3"));
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.5f);
        }

        setScreen(startInEscapeMode ? new EscapeScreen(this) : new TitleScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (bgMusic != null) { bgMusic.stop(); bgMusic.dispose(); }
        super.dispose();
    }
}
