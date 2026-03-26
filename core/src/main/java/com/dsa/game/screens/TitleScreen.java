package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dsa.game.DSAGame;
import com.dsa.game.navigation.Room;
import com.dsa.game.systems.SaveLoadSystem;
import com.dsa.game.ui.TextButton;
import com.dsa.game.ui.TextPanel;

import java.util.ArrayList;
import java.util.List;

public class TitleScreen implements Screen {

    private DSAGame game;
    private SpriteBatch batch;
    private FitViewport viewport;
    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private BitmapFont font;
    private GlyphLayout layout;

    private Texture backgroundTexture;
    private boolean usingImageBackground = false;
    private SaveLoadSystem saveLoadSystem;
    private TextPanel textPanel;
    private final Vector2 touchPos = new Vector2();

    // Menu buttons (only used for procedural background)
    private TextButton newGameButton;
    private TextButton loadGameButton;
    private TextButton settingsButton;
    private TextButton quitButton;

    private class ClickRegion {
        float x, y, width, height;
        String action;

        ClickRegion(float x, float y, float width, float height, String action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.action = action;
        }

        boolean contains(float px, float py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
    }

    private List<ClickRegion> clickRegions = new ArrayList<>();

    private boolean showingLoadPanel = false;

    public TitleScreen(DSAGame game) {
        this.game = game;
        this.batch = game.batch;
        this.viewport = game.viewport;
        this.layout = new GlyphLayout();

        this.titleFont = new BitmapFont();
        this.titleFont.setColor(new Color(0.9f, 0.85f, 0.75f, 1));
        this.titleFont.getData().setScale(2.5f);

        this.subtitleFont = new BitmapFont();
        this.subtitleFont.setColor(new Color(0.6f, 0.55f, 0.5f, 1));
        this.subtitleFont.getData().setScale(1.5f);

        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.2f);

        this.saveLoadSystem = new SaveLoadSystem();
        this.textPanel = new TextPanel();

        generateBackground();
        createButtons();
        setupClickRegions();
        setupInput();
    }

    private void setupClickRegions() {
        clickRegions.add(new ClickRegion(
                500,
                305,
                317,
                57,
                "new_game"));

        clickRegions.add(new ClickRegion(
                535,
                230,
                235,
                55,
                "load_game"));

        clickRegions.add(new ClickRegion(
                535,
                155,
                235,
                50,
                "settings"));

        clickRegions.add(new ClickRegion(
                535,
                85,
                233,
                45,
                "quit"));
    }

    private void generateBackground() {
        String imagePath = "rooms/coverscreen.png";

        if (Gdx.files.internal(imagePath).exists()) {
            try {
                backgroundTexture = new Texture(Gdx.files.internal(imagePath));
                usingImageBackground = true;
            } catch (Exception e) {
                Gdx.app.error("TitleScreen", "Failed to load cover screen image, falling back to procedural generation",
                        e);
                generateProceduralBackground();
            }
        } else {
            Gdx.app.log("TitleScreen", "Cover screen image not found, using procedural generation");
            generateProceduralBackground();
        }
    }

    private void generateProceduralBackground() {
        int w = DSAGame.SCREEN_WIDTH;
        int h = DSAGame.SCREEN_HEIGHT;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        for (int y = 0; y < h; y++) {
            float t = (float) y / h;
            float r = 0.02f + 0.04f * t;
            float g = 0.02f + 0.03f * t;
            float b = 0.05f + 0.06f * t;
            pixmap.setColor(r, g, b, 1);
            pixmap.drawLine(0, y, w, y);
        }

        backgroundTexture = new Texture(pixmap);
        pixmap.dispose();
        usingImageBackground = false;
    }

    private void createButtons() {
        float buttonWidth = 250;
        float buttonHeight = 45;
        float centerX = DSAGame.SCREEN_WIDTH / 2f - buttonWidth / 2f;
        float startY = DSAGame.SCREEN_HEIGHT / 2f - 20;
        float spacing = 55;

        newGameButton = new TextButton("New Game", centerX, startY, buttonWidth, buttonHeight, "new_game");
        loadGameButton = new TextButton("Load Game", centerX, startY - spacing, buttonWidth, buttonHeight, "load_game");
        settingsButton = new TextButton("Settings", centerX, startY - spacing * 2, buttonWidth, buttonHeight,
                "settings");
        quitButton = new TextButton("Quit", centerX, startY - spacing * 3, buttonWidth, buttonHeight, "quit");
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY);
                viewport.unproject(touchPos);
                float gameX = touchPos.x;
                float gameY = touchPos.y;

                if (textPanel.isVisible()) {
                    String action = textPanel.handleClick(gameX, gameY);
                    if (action != null) {
                        handlePanelAction(action);
                    }
                    return true;
                }

                if (usingImageBackground) {
                    for (ClickRegion region : clickRegions) {
                        if (region.contains(gameX, gameY)) {
                            handleMenuAction(region.action);
                            return true;
                        }
                    }
                    return false;
                }

                if (newGameButton.contains(gameX, gameY)) {
                    startNewGame();
                    return true;
                }
                if (loadGameButton.contains(gameX, gameY)) {
                    showLoadPanel();
                    return true;
                }
                if (settingsButton.contains(gameX, gameY)) {
                    showTitleSettings();
                    return true;
                }
                if (quitButton.contains(gameX, gameY)) {
                    Gdx.app.exit();
                    return true;
                }

                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                touchPos.set(screenX, screenY);
                viewport.unproject(touchPos);
                float gameX = touchPos.x;
                float gameY = touchPos.y;

                if (textPanel.isVisible()) {
                    textPanel.handleHover(gameX, gameY);
                    return false;
                }

                if (!usingImageBackground) {
                    newGameButton.checkHover(gameX, gameY);
                    loadGameButton.checkHover(gameX, gameY);
                    settingsButton.checkHover(gameX, gameY);
                    quitButton.checkHover(gameX, gameY);
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    if (textPanel.isVisible()) {
                        textPanel.hide();
                        showingLoadPanel = false;
                    } else {
                        Gdx.app.exit();
                    }
                    return true;
                }
                if (keycode == Input.Keys.ENTER && !textPanel.isVisible()) {
                    startNewGame();
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

    private void handleMenuAction(String action) {
        switch (action) {
            case "new_game":
                startNewGame();
                break;
            case "load_game":
                showLoadPanel();
                break;
            case "settings":
                showTitleSettings();
                break;
            case "quit":
                Gdx.app.exit();
                break;
        }
    }

    private void startNewGame() {
        game.setScreen(new IntroScreen(game));
    }

    private void showLoadPanel() {
        List<TextButton> buttons = new ArrayList<>();
        boolean hasAnySave = false;

        for (int i = 1; i <= 3; i++) {
            String slotName = "slot" + i;
            if (saveLoadSystem.saveExists(slotName)) {
                buttons.add(new TextButton("Load Slot " + i, 0, 0, 200, 35, "load_" + slotName));
                hasAnySave = true;
            }
        }

        if (!hasAnySave) {
            textPanel.show("=== LOAD GAME ===\n\nNo save files found.\nStart a New Game to begin your investigation.");
        } else {
            textPanel.showButtons("=== LOAD GAME ===\n\nSelect a save to load:", buttons);
        }
        showingLoadPanel = true;
    }

    private void showTitleSettings() {
        List<TextButton> buttons = new ArrayList<>();

        float speed = TextPanel.getCharsPerSecond();
        String speedLabel;
        if (speed <= 20f)
            speedLabel = "Slow";
        else if (speed <= 50f)
            speedLabel = "Normal";
        else if (speed <= 100f)
            speedLabel = "Fast";
        else
            speedLabel = "Instant";
        buttons.add(new TextButton("Text Speed: " + speedLabel, 0, 0, 200, 35, "cycle_text_speed"));

        buttons.add(new TextButton("Back", 0, 0, 200, 35, "settings_back"));

        textPanel.showButtons("=== SETTINGS ===", buttons);
        showingLoadPanel = false;
    }

    private void handlePanelAction(String action) {
        if ("close".equals(action) || "panel_consumed".equals(action)) {
            if ("close".equals(action)) {
                showingLoadPanel = false;
            }
            return;
        }

        if (action.startsWith("load_")) {
            String slotName = action.substring("load_".length());
            GameScreen gameScreen = new GameScreen(game);
            gameScreen.loadFromSave(slotName);
            game.setScreen(gameScreen);
            return;
        }

        if ("cycle_text_speed".equals(action)) {
            float speed = TextPanel.getCharsPerSecond();
            if (speed <= 20f)
                TextPanel.setCharsPerSecond(40f);
            else if (speed <= 50f)
                TextPanel.setCharsPerSecond(80f);
            else if (speed <= 100f)
                TextPanel.setCharsPerSecond(200f);
            else
                TextPanel.setCharsPerSecond(20f);
            showTitleSettings();
            return;
        }

        if ("settings_back".equals(action)) {
            textPanel.hide();
            return;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(game.camera.combined);
        batch.begin();

        batch.draw(backgroundTexture, 0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);

        if (!usingImageBackground) {
            String title = "The Unreliable Narrator";
            layout.setText(titleFont, title);
            float titleX = DSAGame.SCREEN_WIDTH / 2f - layout.width / 2f;
            float titleY = DSAGame.SCREEN_HEIGHT - 150;
            titleFont.draw(batch, title, titleX, titleY);

            String subtitle = "A Murder Mystery";
            layout.setText(subtitleFont, subtitle);
            float subX = DSAGame.SCREEN_WIDTH / 2f - layout.width / 2f;
            float subY = titleY - 50;
            subtitleFont.draw(batch, subtitle, subX, subY);
        }

        if (!usingImageBackground && !textPanel.isVisible()) {
            newGameButton.render(batch, font);
            loadGameButton.render(batch, font);
            settingsButton.render(batch, font);
            quitButton.render(batch, font);
        }

        textPanel.render(batch, font);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
        setupInput();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        subtitleFont.dispose();
        font.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        textPanel.dispose();
    }
}
