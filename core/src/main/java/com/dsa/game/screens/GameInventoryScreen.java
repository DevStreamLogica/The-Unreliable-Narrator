package com.dsa.game.screens;

import com.badlogic.gdx.Screen;
import com.dsa.game.DSAGame;
import com.dsa.game.navigation.Room;

/**
 * Standalone debug / legacy full-screen evidence UI. Main game flow uses {@link EvidenceGapSession}
 * embedded in {@link GameScreen} instead.
 */
public class GameInventoryScreen implements Screen {

    private final DSAGame game;
    private final EvidenceGapSession session;

    public GameInventoryScreen(DSAGame game, int chapterIndex, Room.RoomID resumeWorldRoom, Runnable onComplete) {
        this.game = game;
        this.session = EvidenceGapSession.forStandalone(game, chapterIndex, resumeWorldRoom, onComplete);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        session.renderStandaloneFrame(delta);
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        session.dispose();
    }
}
