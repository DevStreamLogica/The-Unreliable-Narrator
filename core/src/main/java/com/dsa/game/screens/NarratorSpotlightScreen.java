package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.dsa.game.DSAGame;
import com.dsa.game.navigation.Room;

import java.util.LinkedList;

/**
 * NarratorSpotlightScreen
 *
 * Full-screen spotlight mini-event that triggers every ~4 minutes during gameplay.
 * The lights flicker out, the narrator highlights an object in the current room, and
 * the player must answer YES (it was really there) or NO (the narrator is lying).
 *
 * After one question is answered and the lights come back on, {@code onComplete} is called
 * and the game returns to GameScreen.
 *
 * Not triggered in ENTRANCE or CELLAR rooms.
 */
public class NarratorSpotlightScreen implements Screen {

    private static final String NAR = "sfx/narrator/spotlight/";

    // ── room data ─────────────────────────────────────────────────────────────
    private static class RoomData {
        final String   bgPath;
        final float[][] objPos;
        final String[]  objNames, objAudio;
        final float[][] fakePos;
        final String[]  fakeDescs, fakeAudio;

        RoomData(String bgPath,
                 float[][] objPos, String[] objNames, String[] objAudio,
                 float[][] fakePos, String[] fakeDescs, String[] fakeAudio) {
            this.bgPath    = bgPath;
            this.objPos    = objPos;    this.objNames  = objNames;  this.objAudio  = objAudio;
            this.fakePos   = fakePos;   this.fakeDescs = fakeDescs; this.fakeAudio = fakeAudio;
        }
    }

    /** Maps every valid spotlight Room.RoomID to its RoomData. */
    private static java.util.Map<Room.RoomID, RoomData> ROOM_DATA;
    static {
        ROOM_DATA = new java.util.HashMap<>();

        ROOM_DATA.put(Room.RoomID.PARLOR, new RoomData(
            "rooms/parlor.jpg",
            new float[][]{ {924f,331f},{609f,303f},{618f,599f},{652f,252f},{794f,171f},{1127f,437f} },
            new String[]{ "a grandfather clock","a fireplace","a chandelier","a briefcase","an armchair","a painting" },
            new String[]{ NAR+"grandfather clock.mp3", NAR+"Fireplace.mp3", NAR+"chandelier .mp3",
                          NAR+"briefcase.mp3", NAR+"armchair.mp3", NAR+"painting.mp3" },
            new float[][]{ {1066f,260f},{609f,215f},{803f,180f},{126f,588f},{1124f,404f} },
            new String[]{ "a stuff toy on the drawer","a lamp on top of the table",
                          "a wallet on top of the chair","a clock on the wall","a hanging vase" },
            new String[]{ NAR+"stuffed toy.mp3", NAR+"lamp.mp3", NAR+"wallet .mp3",
                          NAR+"clock.mp3", NAR+"hanging vas3.mp3" }
        ));

        ROOM_DATA.put(Room.RoomID.STUDY, new RoomData(
            "rooms/study.png",
            new float[][]{ {622f,342f},{288f,186f},{434f,419f},{622f,263f},{627f,576f} },
            new String[]{ "a chair","firewood","a window","a table","a chandelier" },
            new String[]{ NAR+"chair (study).mp3", NAR+"firewood (study).mp3", NAR+"window ( study).mp3",
                          NAR+"table (study).mp3", NAR+"chandelier (study).mp3" },
            new float[][]{ {623f,342f},{49f,547f},{434f,450f},{818f,268f},{496f,530f} },
            new String[]{ "a vase","a clock","a curtain","a chair","a picture" },
            new String[]{ NAR+"vase (study).mp3", NAR+"clock (study).mp3", NAR+"curtain (study).mp3",
                          NAR+"fake chair ( study).mp3", NAR+"picture ( study).mp3" }
        ));

        ROOM_DATA.put(Room.RoomID.KITCHEN, new RoomData(
            "rooms/kitchen.png",
            new float[][]{ {1024f,403f},{642f,563f},{840f,538f},{453f,522f},{249f,360f} },
            new String[]{ "condiments","hanging pans","hanging pans","hanging pans","a clock" },
            new String[]{ NAR+"condiments (kitchen).mp3", NAR+"hanging pans 1 ( kitchen).mp3",
                          NAR+"hanging pans 2 ( kitchen).mp3", NAR+"hanging pans 3 ( kitchen ).mp3",
                          NAR+"clock ( kitchen).mp3" },
            new float[][]{ {139f,211f},{639f,675f},{212f,570f},{72f,204f},{248f,267f} },
            new String[]{ "a rope","a chandelier","a clock","a rope","a painting" },
            new String[]{ NAR+"rope ( kitchen .mp3", NAR+"chandelier (kitchen).mp3",
                          NAR+"clock fake ( kitchen).mp3", NAR+"rope fake ( kitchen).mp3",
                          NAR+"painting ( kitchen).mp3" }
        ));

        ROOM_DATA.put(Room.RoomID.JAMES_ROOM, new RoomData(
            "rooms/james_room.jpg",
            new float[][]{ {498f,588f},{963f,424f},{287f,253f},{1230f,423f},{606f,262f} },
            new String[]{ "a painting","a cabinet","a small drawer","a window","a bed" },
            new String[]{ NAR+"painting (james room).mp3", NAR+"cabinet ( james room).mp3",
                          NAR+"small drawer ( james room).mp3", NAR+"window ( james room).mp3",
                          NAR+"bed ( james room).mp3" },
            new float[][]{ {204f,584f},{939f,660f},{115f,235f},{500f,319f},{1211f,192f} },
            new String[]{ "a clock","a clock","a vase","a knife","a stuff toy" },
            new String[]{ NAR+"clock fake ( James room).mp3", NAR+"clock fake 2 ( james room ).mp3",
                          NAR+"vase fake ( james room).mp3", NAR+"knife ( james room).mp3",
                          NAR+"stuffed toy ( james room).mp3" }
        ));

        ROOM_DATA.put(Room.RoomID.MARGARET_ROOM, new RoomData(
            "rooms/parlor.jpg",  // fallback to parlor if margaret not available
            new float[][]{ {288f,227f},{436f,325f},{1022f,498f},{455f,555f},{687f,242f} },
            new String[]{ "a drawer","pillows","curtains","a painting","a briefcase" },
            new String[]{ NAR+"drawer ( margaret room ).mp3", NAR+"pillows ( margaret room).mp3",
                          NAR+"curtains ( margarete room).mp3", NAR+"painting ( Margaret room).mp3",
                          NAR+"briefcase.mp3" },
            new float[][]{ {257f,228f},{253f,367f},{490f,550f},{672f,288f},{1195f,546f} },
            new String[]{ "a vase","a clock","a clock","a table","a painting" },
            new String[]{ NAR+"vase fake ( margaret room ).mp3", NAR+"clock fake ( margaret room ).mp3",
                          NAR+"clock fake 2 ( margaret room).mp3", NAR+"table fake ( margaret room).mp3",
                          NAR+"painting fake ( margaret room).mp3" }
        ));

        // Servants quarters — reuse some study/kitchen audio as fallback
        ROOM_DATA.put(Room.RoomID.SERVANTS_QUARTERS, new RoomData(
            "rooms/servants_quarters.png",
            new float[][]{ {622f,342f},{288f,186f},{434f,419f},{622f,263f} },
            new String[]{ "a bed","a wardrobe","a small window","a table" },
            new String[]{ NAR+"bed ( james room).mp3", NAR+"cabinet ( james room).mp3",
                          NAR+"window ( study).mp3", NAR+"table (study).mp3" },
            new float[][]{ {623f,342f},{49f,547f},{434f,450f} },
            new String[]{ "a vase","a clock","a curtain" },
            new String[]{ NAR+"vase (study).mp3", NAR+"clock (study).mp3", NAR+"curtain (study).mp3" }
        ));

        ROOM_DATA.put(Room.RoomID.GROUNDSKEEPER_SHED, new RoomData(
            "rooms/servants_quarters.png",  // fallback
            new float[][]{ {622f,342f},{288f,186f},{434f,419f} },
            new String[]{ "a workbench","a lantern","a set of tools" },
            new String[]{ NAR+"table (study).mp3", NAR+"lamp.mp3", NAR+"firewood (study).mp3" },
            new float[][]{ {623f,342f},{49f,547f} },
            new String[]{ "a clock","a curtain" },
            new String[]{ NAR+"clock (study).mp3", NAR+"curtain (study).mp3" }
        ));
    }

    // ── states ────────────────────────────────────────────────────────────────
    private enum State { FLICKERING_OUT, DARK_WAITING, QUESTIONING, FLICKERING_IN, DONE }

    // ── timing ────────────────────────────────────────────────────────────────
    private static final float FLICKER_OUT_DURATION = 3.5f;
    private static final float FLICKER_IN_DURATION  = 2f;
    private static final float DARK_WAIT            = 0.5f;
    private static final float GLOW_PULSE_SPEED     = 3f;
    private static final float SPOTLIGHT_NORMAL     = 220f;
    private static final float DONE_DELAY           = 1.5f; // pause after lights come back before returning

    // ── fields ────────────────────────────────────────────────────────────────
    private final DSAGame  game;
    private final Runnable onComplete;

    private SpriteBatch    batch;
    private ShapeRenderer  shape;
    private BitmapFont     bigFont;

    private Texture     bg, glowTex, pixel;
    private FrameBuffer lightsBuffer;
    private Texture     spotlightTex;

    private State state    = State.FLICKERING_OUT;
    private float stateTimer;
    private float darkWait   = 0f;
    private float glowPulse  = 0f;
    private float doneTimer  = DONE_DELAY;

    // spotlight
    private float cursorX = DSAGame.SCREEN_WIDTH / 2f;
    private float cursorY = DSAGame.SCREEN_HEIGHT / 2f;
    private float lightRadius = SPOTLIGHT_NORMAL;

    // flicker (simple, no envelope — use linear interpolation)
    private float flickerTimer = 0f;
    private float flickerValue = 1f; // current brightness multiplier

    // question
    private float   qX, qY;
    private boolean qIsTrue;
    private boolean questionReady = false;

    // narrator voice
    private Music narratorTrack;
    private final LinkedList<String> narratorQueue = new LinkedList<>();

    // audio
    private Music flickerMusic, flickerOnMusic;

    // room data for current room
    private final RoomData roomData;

    // buttons
    private static final float BTN_W = 160f, BTN_H = 55f;
    private static final float YES_X = 440f, NO_X = 680f, BTN_Y = 80f;

    // ── constructor ───────────────────────────────────────────────────────────
    public NarratorSpotlightScreen(DSAGame game, Room.RoomID currentRoom, Runnable onComplete) {
        this.game       = game;
        this.onComplete = onComplete;

        RoomData rd = ROOM_DATA.get(currentRoom);
        if (rd == null) {
            // Fallback to parlor data if room not mapped
            rd = ROOM_DATA.get(Room.RoomID.PARLOR);
        }
        this.roomData = rd;
    }

    @Override
    public void show() {
        batch    = new SpriteBatch();
        shape    = new ShapeRenderer();
        bigFont  = new BitmapFont();
        bigFont.getData().setScale(2.6f);

        // Spotlight texture (warm glow)
        spotlightTex = createSpotlightTexture();
        lightsBuffer = new FrameBuffer(Pixmap.Format.RGBA8888,
                DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT, false);

        // Load room background
        String bgPath = roomData.bgPath;
        if (Gdx.files.internal(bgPath).exists()) {
            bg = new Texture(Gdx.files.internal(bgPath));
        } else {
            // Fallback: blank black texture
            Pixmap p = new Pixmap(DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT, Pixmap.Format.RGBA8888);
            p.setColor(0.05f, 0.05f, 0.08f, 1f); p.fill();
            bg = new Texture(p); p.dispose();
        }

        // Glow texture (blue pulse around highlighted object)
        int gs = 256; float cr = gs / 2f;
        Pixmap gpm = new Pixmap(gs, gs, Pixmap.Format.RGBA8888);
        for (int gy = 0; gy < gs; gy++)
            for (int gx2 = 0; gx2 < gs; gx2++) {
                float d = (float) Math.sqrt((gx2-cr)*(gx2-cr)+(gy-cr)*(gy-cr)) / cr;
                float a = Math.max(0f, 1f - d * d);
                gpm.setColor(0.3f, 0.5f, 1f, a * 0.85f);
                gpm.drawPixel(gx2, gy);
            }
        glowTex = new Texture(gpm); gpm.dispose();

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        pixel = new Texture(pm); pm.dispose();

        // Flicker audio
        if (Gdx.files.internal("sfx/flicker.mp3").exists()) {
            flickerMusic = Gdx.audio.newMusic(Gdx.files.internal("sfx/flicker.mp3"));
            flickerMusic.setLooping(true);
            flickerMusic.setVolume(0.85f);
            flickerMusic.play();
        }
        if (Gdx.files.internal("sfx/flicker_on.mp3").exists()) {
            flickerOnMusic = Gdx.audio.newMusic(Gdx.files.internal("sfx/flicker_on.mp3"));
            flickerOnMusic.setLooping(true);
            flickerOnMusic.setVolume(0.85f);
        }

        stateTimer   = FLICKER_OUT_DURATION;
        flickerTimer = 0f;
        flickerValue = 1f;

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean mouseMoved(int sx, int sy) {
                Vector2 w = game.viewport.unproject(new Vector2(sx, sy));
                cursorX = w.x; cursorY = w.y;
                return false;
            }
            @Override
            public boolean touchDown(int sx, int sy, int ptr, int btn) {
                Vector2 w = game.viewport.unproject(new Vector2(sx, sy));
                cursorX = w.x; cursorY = w.y;
                if (state != State.QUESTIONING || !questionReady) return false;
                if (hitBtn(w.x, w.y, YES_X, BTN_Y)) {
                    answer(true);
                } else if (hitBtn(w.x, w.y, NO_X, BTN_Y)) {
                    answer(false);
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        glowPulse += GLOW_PULSE_SPEED * delta;

        // Advance narrator queue
        if (narratorTrack != null && !narratorTrack.isPlaying() && !narratorQueue.isEmpty())
            playNarrator(narratorQueue.poll());

        // Mark question ready when narrator finishes speaking
        if (state == State.QUESTIONING && !questionReady)
            if (narratorTrack == null || (!narratorTrack.isPlaying() && narratorQueue.isEmpty()))
                questionReady = true;

        switch (state) {
            case FLICKERING_OUT:
                flickerTimer += delta;
                // Simple linear flicker: random jitter toward 0
                flickerValue = Math.max(0f, flickerValue - delta * 0.7f
                        + (MathUtils.random() - 0.5f) * delta * 2.5f);
                lightRadius = flickerValue * SPOTLIGHT_NORMAL;
                if (flickerTimer >= stateTimer) {
                    lightRadius  = 0f;
                    flickerValue = 0f;
                    state     = State.DARK_WAITING;
                    darkWait  = DARK_WAIT;
                    if (flickerMusic != null) flickerMusic.stop();
                }
                break;

            case DARK_WAITING:
                lightRadius = 0f;
                darkWait -= delta;
                if (darkWait <= 0) {
                    buildQuestion();
                    state = State.QUESTIONING;
                    questionReady = false;
                }
                break;

            case QUESTIONING:
                lightRadius = 0f;
                break;

            case FLICKERING_IN:
                flickerTimer += delta;
                flickerValue = Math.min(1f, flickerValue + delta * 0.6f
                        + (MathUtils.random() - 0.3f) * delta * 1.5f);
                lightRadius = Math.max(0f, Math.min(1f, flickerValue)) * SPOTLIGHT_NORMAL;
                if (flickerTimer >= FLICKER_IN_DURATION) {
                    lightRadius = SPOTLIGHT_NORMAL;
                    state = State.DONE;
                    doneTimer = DONE_DELAY;
                    if (flickerOnMusic != null) flickerOnMusic.stop();
                }
                break;

            case DONE:
                doneTimer -= delta;
                lightRadius = SPOTLIGHT_NORMAL;
                if (doneTimer <= 0 && (narratorTrack == null || !narratorTrack.isPlaying())) {
                    // Return to caller
                    if (onComplete != null) onComplete.run();
                }
                break;
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        game.batch.draw(bg, 0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
        game.batch.end();

        if (lightRadius > 0) {
            float lightSize = lightRadius * 2f;
            float lightX    = cursorX - lightSize / 2f;
            float lightY    = cursorY - lightSize / 2f;

            lightsBuffer.begin();
            Gdx.gl.glViewport(0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            game.batch.setProjectionMatrix(game.camera.combined);
            game.batch.begin();
            game.batch.setColor(Color.WHITE);
            game.batch.draw(spotlightTex, lightX, lightY, lightSize, lightSize);
            game.batch.end();
            lightsBuffer.end();

            game.viewport.apply();
            game.batch.setProjectionMatrix(game.camera.combined);
            game.batch.begin();
            game.batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);
            game.batch.draw(lightsBuffer.getColorBufferTexture(), 0, DSAGame.SCREEN_HEIGHT,
                    DSAGame.SCREEN_WIDTH, -DSAGame.SCREEN_HEIGHT);
            game.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            game.batch.setColor(Color.WHITE);
            game.batch.end();
        } else if (state != State.DONE) {
            // Full dark
            shape.setProjectionMatrix(game.camera.combined);
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0, 0, 0, 1);
            shape.rect(0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
            shape.end();
        }

        // Glow around highlighted object during questioning
        if (state == State.QUESTIONING) {
            float pulse    = (MathUtils.sin(glowPulse) + 1f) / 2f;
            float glowSize = 90f + 8f * pulse;
            float alpha    = 0.7f + 0.15f * pulse;
            game.batch.begin();
            game.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            game.batch.setColor(1f, 1f, 1f, alpha);
            game.batch.draw(glowTex, qX - glowSize / 2f, qY - glowSize / 2f, glowSize, glowSize);
            game.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            game.batch.setColor(Color.WHITE);
            game.batch.end();

            if (questionReady) {
                drawButton(YES_X, BTN_Y, BTN_W, BTN_H, "YES",
                        new Color(0.15f, 0.5f, 0.15f, 0.92f));
                drawButton(NO_X, BTN_Y, BTN_W, BTN_H, "NO",
                        new Color(0.5f, 0.12f, 0.12f, 0.92f));
            }
        }
    }

    // ── narrator audio ────────────────────────────────────────────────────────
    private void playNarrator(String path) {
        if (narratorTrack != null) { narratorTrack.stop(); narratorTrack.dispose(); narratorTrack = null; }
        if (path == null) return;
        // Try .mp3, then .m4a, then .wav
        String[] candidates = { path, path.replace(".mp3", ".m4a"), path.replace(".mp3", ".wav") };
        for (String c : candidates) {
            if (Gdx.files.internal(c).exists()) {
                try {
                    narratorTrack = Gdx.audio.newMusic(Gdx.files.internal(c));
                    narratorTrack.play();
                    return;
                } catch (Exception ignored) {}
            }
        }
    }

    private void stopNarrator() {
        narratorQueue.clear();
        if (narratorTrack != null) { narratorTrack.stop(); narratorTrack.dispose(); narratorTrack = null; }
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private Texture createSpotlightTexture() {
        int size = 256, cx = size / 2, cy = size / 2;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++) {
                float dx = x - cx, dy = y - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy) / (size / 2f);
                float b = Math.max(0f, 1f - dist * dist * dist * dist);
                pm.setColor(0.92f * b, 0.85f * b, 0.65f * b, 1f);
                pm.drawPixel(x, y);
            }
        Texture t = new Texture(pm); pm.dispose(); return t;
    }

    private void buildQuestion() {
        boolean pickReal = MathUtils.randomBoolean();
        if (pickReal) {
            int idx = MathUtils.random(roomData.objPos.length - 1);
            qX = roomData.objPos[idx][0];
            qY = roomData.objPos[idx][1];
            qIsTrue = true;
            playNarrator(roomData.objAudio[idx]);
        } else {
            int idx = MathUtils.random(roomData.fakePos.length - 1);
            qX = roomData.fakePos[idx][0];
            qY = roomData.fakePos[idx][1];
            qIsTrue = false;
            playNarrator(roomData.fakeAudio[idx]);
        }
        if (narratorTrack == null) questionReady = true;
    }

    private void answer(boolean yes) {
        boolean correct = (yes == qIsTrue);
        stopNarrator();
        String feedback = correct ? "sfx/correct.mp3" : "sfx/game_over.mp3";
        if (Gdx.files.internal(feedback).exists()) {
            try {
                Sound s = Gdx.audio.newSound(Gdx.files.internal(feedback));
                s.play(1f);
                // Note: we don't dispose this immediately — it's short, GC will handle it
            } catch (Exception ignored) {}
        }
        // Transition to FLICKERING_IN
        state = State.FLICKERING_IN;
        flickerTimer = 0f;
        flickerValue = 0f;
        if (flickerOnMusic != null) { flickerOnMusic.stop(); flickerOnMusic.play(); }
    }

    private void drawButton(float x, float y, float w, float h, String label, Color color) {
        shape.setProjectionMatrix(game.camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(color);
        shape.rect(x, y, w, h);
        shape.end();
        game.batch.begin();
        bigFont.setColor(Color.WHITE);
        bigFont.draw(game.batch, label, x + w / 2f - 25f, y + h / 2f + 12f);
        game.batch.end();
    }

    private boolean hitBtn(float mx, float my, float bx, float by) {
        return mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H;
    }

    @Override public void resize(int w, int h) { game.viewport.update(w, h, true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (batch != null)        batch.dispose();
        if (shape != null)        shape.dispose();
        if (bigFont != null)      bigFont.dispose();
        if (bg != null)           bg.dispose();
        if (glowTex != null)      glowTex.dispose();
        if (pixel != null)        pixel.dispose();
        if (spotlightTex != null) spotlightTex.dispose();
        if (lightsBuffer != null) lightsBuffer.dispose();
        if (flickerMusic != null)   { flickerMusic.stop();   flickerMusic.dispose(); }
        if (flickerOnMusic != null) { flickerOnMusic.stop(); flickerOnMusic.dispose(); }
        stopNarrator();
    }
}
