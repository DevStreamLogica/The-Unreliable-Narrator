package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.dsa.game.DSAGame;
import com.dsa.game.navigation.Room;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * MetalDetectorScanScreen
 *
 * The in-room tape-scanning phase of the metal detector minigame.
 * The player uses WASD to move a detector cursor around the room.
 * The detector beeps faster as it approaches hidden tape locations.
 * After 10 seconds the player places a circle where they think the tape is.
 * If the placement overlaps a tape → {@code onFound} is called.
 * If it misses → the screen returns anyway (player gets another chance).
 *
 * Adapted from Testing 2 / MetalDetectorScreen.java for use in the main game.
 */
public class MetalDetectorScanScreen implements Screen {

    private static final float DETECT_DURATION = 10f;
    private static final float MAX_DISTANCE = 500f;
    private static final float MIN_DISTANCE = 30f;
    private static final float MAX_INTERVAL = 4000f;
    private static final float MIN_INTERVAL = 50f;
    private static final float MOVE_SPEED   = 120f;
    private static final float CIRCLE_RADIUS= 30f;
    private static final int   BAR_FRAMES   = 28;
    private static final float MD_W = 60f, MD_H = 100f;

    // Narrator paths
    private static final String INTRO_LINE = "sfx/narrator/metal_detector/intro.mp3";
    private static final String DEMO_LINE  = "sfx/narrator/metal_detector/demo.mp3";

    // ── Per-room tape positions ───────────────────────────────────────────────
    /** Returns the tape hit-test positions for a given room. */
    private static Vector2[] tapePosForRoom(Room.RoomID room) {
        switch (room) {
            case PARLOR:          return new Vector2[]{ new Vector2(650f, 240f), new Vector2(930f, 330f) };
            case KITCHEN:         return new Vector2[]{ new Vector2(510f, 122f), new Vector2(640f, 200f) };
            case MARGARET_ROOM:   return new Vector2[]{ new Vector2(280f, 272f), new Vector2(440f, 320f) };
            case STUDY:           return new Vector2[]{ new Vector2(636f, 164f), new Vector2(1065f, 377f) };
            case JAMES_ROOM:      return new Vector2[]{ new Vector2(720f, 42f),  new Vector2(600f, 260f) };
            case GROUNDSKEEPER_SHED: return new Vector2[]{ new Vector2(700f, 290f), new Vector2(530f, 260f) };
            case SERVANTS_QUARTERS:  return new Vector2[]{ new Vector2(660f, 295f), new Vector2(730f, 296f) };
            case CELLAR:          return new Vector2[]{ new Vector2(640f, 360f), new Vector2(400f, 250f) };
            default:              return new Vector2[]{ new Vector2(640f, 360f) };
        }
    }

    /** Testing 3 / inventory minigame backgrounds (clean rooms, no item overlays). */
    private static String inventoryBgPathForRoom(Room.RoomID room) {
        switch (room) {
            case PARLOR:             return "inventory/rooms/parlor.jpg";
            case KITCHEN:            return "inventory/rooms/kitchen.png";
            case MARGARET_ROOM:      return "inventory/rooms/margaret.png";
            case STUDY:              return "inventory/rooms/study.png";
            case JAMES_ROOM:         return "inventory/rooms/james.jpg";
            case GROUNDSKEEPER_SHED: return "inventory/rooms/shed.png";
            case SERVANTS_QUARTERS:  return "inventory/rooms/servants.png";
            case CELLAR:             return "inventory/rooms/cellar.png";
            default:                 return null;
        }
    }

    /** Fallback main-game room art if inventory copy is missing. */
    private static String bgPathForRoom(Room.RoomID room) {
        switch (room) {
            case PARLOR:             return "rooms/parlor.jpg";
            case KITCHEN:            return "rooms/kitchen.png";
            case MARGARET_ROOM:      return "rooms/margarette room.png";
            case STUDY:              return "rooms/study.png";
            case JAMES_ROOM:         return "rooms/james_room.jpg";
            case GROUNDSKEEPER_SHED: return "rooms/Shed_with_tape_without_boots.jpg";
            case SERVANTS_QUARTERS:  return "rooms/servants_quarters.png";
            case CELLAR:             return "rooms/cellar.png";
            default:                 return "rooms/entrance.png";
        }
    }

    // ── Demo tape position (for animated cursor) ──────────────────────────────
    private static final Vector2 DEMO_TAPE = new Vector2(820f, 360f);

    /** After the tutorial demo runs once, later visits skip DEMO (still play intro, then go straight to detecting). */
    private static boolean metalDetectorDemoAlreadySeen;

    // ── Fields ────────────────────────────────────────────────────────────────
    private final DSAGame    game;
    private final Runnable   onFound;   // called when tape is successfully located
    private final Runnable   onReturn;  // called on miss/close (always called after result shown)
    private final Vector2[]  tapePositions;

    private Texture       roomTex, metalDetectorTex;
    /** {@code inventory/rooms/shed.png} matches Testing 3 (flip horizontally); JPG fallbacks do not. */
    private final boolean metalDetectorShedFlipHorizontal;
    private Texture[]     barFrames;
    private ShapeRenderer shapes;
    private Music         bgMusic, clockMusic, narratorTrack;
    private Sound         dingSound, correctSound, gameOverSound;

    private float bgMusicFadeTimer = 0f;
    private static final float BGM_FADE_DELAY    = 1f;
    private static final float BGM_FADE_DURATION = 2f;

    private enum Phase { NARRATOR_INTRO, DEMO, DETECTING, PLACING, RESULT }
    private Phase phase = Phase.NARRATOR_INTRO;

    // Demo
    private float demoTimer   = 0f;
    private float demoCursorX = 160f, demoCursorY = 360f;
    private static final float DEMO_DURATION = 6f;

    private final Vector2 cursorPos = new Vector2(
            DSAGame.SCREEN_WIDTH / 2f, DSAGame.SCREEN_HEIGHT - 20f);
    private float detectTimer = DETECT_DURATION;
    private Vector2 placedCircle = null;
    private Boolean result = null; // null=pending, true=hit, false=miss
    private float resultTimer = 2.5f; // seconds to show result before proceeding

    private volatile float   beepInterval = MAX_INTERVAL;
    private volatile boolean running      = true;
    private Thread beepThread;

    // ── Constructor ───────────────────────────────────────────────────────────
    /**
     * @param game       main game instance
     * @param targetRoom the room being scanned (determines background + tape positions)
     * @param onFound    called when the player correctly locates a tape
     * @param onReturn   called when the screen should close (hit or miss — always fires after result display)
     */
    public MetalDetectorScanScreen(DSAGame game, Room.RoomID targetRoom, Runnable onFound, Runnable onReturn) {
        this.game          = game;
        this.onFound       = onFound;
        this.onReturn      = onReturn;
        this.tapePositions = tapePosForRoom(targetRoom);

        boolean shedInvFlip = false;
        if (targetRoom == Room.RoomID.STUDY) {
            // Same no-tape variant as GameScreen.drawStudyPropLayers — inventory/rooms/study.png often has tape visible.
            String[] studyHuntBgs = {
                "rooms/study_with_poker_without_tape.png",
                "rooms/study.png",
                "inventory/rooms/study.png"
            };
            for (String p : studyHuntBgs) {
                if (Gdx.files.internal(p).exists()) {
                    roomTex = new Texture(Gdx.files.internal(p));
                    roomTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    break;
                }
            }
        } else {
            // Prefer inventory/rooms (clean art); fall back to main rooms/
            String invPath = inventoryBgPathForRoom(targetRoom);
            if (invPath != null && Gdx.files.internal(invPath).exists()) {
                roomTex = new Texture(Gdx.files.internal(invPath));
                if (targetRoom == Room.RoomID.GROUNDSKEEPER_SHED)
                    shedInvFlip = true;
            } else {
                String bgPath = bgPathForRoom(targetRoom);
                if (Gdx.files.internal(bgPath).exists())
                    roomTex = new Texture(Gdx.files.internal(bgPath));
            }
        }
        if (roomTex == null && Gdx.files.internal("rooms/study.png").exists())
            roomTex = new Texture(Gdx.files.internal("rooms/study.png"));
        this.metalDetectorShedFlipHorizontal = shedInvFlip;
    }

    // ── Audio: Java synthesis beep ────────────────────────────────────────────
    private void playTone(int hz, int durationMs) {
        try {
            AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format, 4096);
            line.start();
            int samples = (int) (44100 * durationMs / 1000.0);
            byte[] buf = new byte[samples];
            for (int i = 0; i < samples; i++) {
                double angle = 2.0 * Math.PI * i * hz / 44100;
                buf[i] = (byte) (Math.sin(angle) * 80);
            }
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException ignored) {}
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────
    @Override
    public void show() {
        if (Gdx.files.internal("minigames/metal_detector.png").exists())
            metalDetectorTex = new Texture(Gdx.files.internal("minigames/metal_detector.png"));

        shapes = new ShapeRenderer();

        // Progress bar frames
        barFrames = new Texture[BAR_FRAMES];
        for (int i = 0; i < BAR_FRAMES; i++) {
            String path = "minigames/progressbar/pixilart-frames/pixil-frame-" + i + ".png";
            if (Gdx.files.internal(path).exists())
                barFrames[i] = new Texture(Gdx.files.internal(path));
        }

        // Sounds
        dingSound    = loadSound("sfx/inventory/ding.mp3");
        correctSound = loadSound("sfx/correct.mp3");
        gameOverSound= loadSound("sfx/game_over.mp3");

        // BGM (use manor ambience if available)
        if (Gdx.files.internal("music/manor_ambience.mp3").exists()) {
            bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music/manor_ambience.mp3"));
        } else if (Gdx.files.internal("sfx/inventory/inventory_music.mp3").exists()) {
            bgMusic = Gdx.audio.newMusic(Gdx.files.internal("sfx/inventory/inventory_music.mp3"));
        }
        if (bgMusic != null) { bgMusic.setLooping(true); bgMusic.setVolume(0f); bgMusic.play(); }

        // Clock ticking
        if (Gdx.files.internal("sfx/inventory/clock_ticking.wav").exists()) {
            clockMusic = Gdx.audio.newMusic(Gdx.files.internal("sfx/inventory/clock_ticking.wav"));
            clockMusic.setLooping(false);
        }

        // Narrator intro
        playNarrator(INTRO_LINE);

        // Start beep thread
        beepThread = new Thread(() -> {
            long lastBeep = System.currentTimeMillis();
            while (running) {
                long now = System.currentTimeMillis();
                if ((phase == Phase.DETECTING || phase == Phase.DEMO)
                        && now - lastBeep >= (long) beepInterval) {
                    lastBeep = now;
                    Thread t = new Thread(() -> playTone(300, 60));
                    t.setDaemon(true);
                    t.start();
                }
                try { Thread.sleep(10); } catch (InterruptedException e) { break; }
            }
        });
        beepThread.setDaemon(true);
        beepThread.start();
    }

    @Override
    public void render(float delta) {
        // BGM fade in
        if (bgMusic != null && bgMusic.getVolume() < 1f) {
            bgMusicFadeTimer += delta;
            if (bgMusicFadeTimer > BGM_FADE_DELAY) {
                float t = Math.min(1f, (bgMusicFadeTimer - BGM_FADE_DELAY) / BGM_FADE_DURATION);
                bgMusic.setVolume(t * 0.5f); // quieter to not overwhelm
            }
        }

        // NARRATOR_INTRO → DEMO (first visit) or DETECTING (after demo seen once)
        if (phase == Phase.NARRATOR_INTRO && narratorTrack != null && !narratorTrack.isPlaying()) {
            if (metalDetectorDemoAlreadySeen) {
                startDetecting();
            } else {
                phase = Phase.DEMO;
                demoTimer  = 0f;
                demoCursorX= 160f;
                demoCursorY= DEMO_TAPE.y;
                playNarrator(DEMO_LINE);
            }
        }

        // Demo animation
        if (phase == Phase.DEMO) {
            demoTimer += delta;
            float t = Math.min(1f, demoTimer / DEMO_DURATION);
            float path = t < 0.6f ? t / 0.6f : 1f - ((t - 0.6f) / 0.4f) * 0.25f;
            demoCursorX = 160f + path * (DEMO_TAPE.x - 160f - 40f);
            demoCursorY = DEMO_TAPE.y + (float) Math.sin(demoTimer * 1.8f) * 35f;

            float dist = (float) Math.hypot(demoCursorX - DEMO_TAPE.x, demoCursorY - DEMO_TAPE.y);
            updateBeepInterval(dist);

            boolean narratorDone = narratorTrack == null || !narratorTrack.isPlaying();
            if (demoTimer >= DEMO_DURATION && narratorDone) startDetecting();
        }

        // Detecting: WASD movement
        if (phase == Phase.DETECTING) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) cursorPos.x -= MOVE_SPEED * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) cursorPos.x += MOVE_SPEED * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) cursorPos.y -= MOVE_SPEED * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) cursorPos.y += MOVE_SPEED * delta;
            cursorPos.x = Math.max(0, Math.min(DSAGame.SCREEN_WIDTH,  cursorPos.x));
            cursorPos.y = Math.max(0, Math.min(DSAGame.SCREEN_HEIGHT, cursorPos.y));

            float nearest = Float.MAX_VALUE;
            for (Vector2 tp : tapePositions) nearest = Math.min(nearest, cursorPos.dst(tp));
            updateBeepInterval(nearest);

            detectTimer -= delta;
            if (detectTimer <= 0f) {
                phase = Phase.PLACING;
                if (clockMusic != null) clockMusic.stop();
                if (bgMusic    != null) bgMusic.stop();
                if (dingSound  != null) dingSound.play();
            }
        }

        // Placing: click to place green circle
        if (phase == Phase.PLACING) {
            if (placedCircle == null && result == null && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                Vector3 raw = game.viewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                placedCircle = new Vector2(raw.x, raw.y);

                boolean hit = false;
                for (Vector2 tp : tapePositions) {
                    if (placedCircle.dst(tp) < CIRCLE_RADIUS * 2f) { hit = true; break; }
                }
                result = hit;
                phase  = Phase.RESULT;
                if (hit) { if (correctSound  != null) correctSound.play(); }
                else     { if (gameOverSound != null) gameOverSound.play(); }
            }
        }

        // Result display
        if (phase == Phase.RESULT) {
            resultTimer -= delta;
            if (resultTimer <= 0f) {
                if (Boolean.TRUE.equals(result) && onFound   != null) onFound.run();
                else                              if (onReturn != null) onReturn.run();
            }
        }

        // ── Draw ──────────────────────────────────────────────────────────────
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        if (roomTex != null) {
            float sw = DSAGame.SCREEN_WIDTH, sh = DSAGame.SCREEN_HEIGHT;
            if (metalDetectorShedFlipHorizontal)
                game.batch.draw(roomTex, sw, 0, -sw, sh);
            else
                game.batch.draw(roomTex, 0, 0, sw, sh);
        }
        game.batch.end();

        shapes.setProjectionMatrix(game.camera.combined);

        // Demo: show fake tape position (yellow)
        if (phase == Phase.DEMO) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1f, 1f, 0f, 0.3f);
            shapes.circle(DEMO_TAPE.x, DEMO_TAPE.y, CIRCLE_RADIUS);
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.YELLOW);
            shapes.circle(DEMO_TAPE.x, DEMO_TAPE.y, CIRCLE_RADIUS);
            shapes.end();
            if (metalDetectorTex != null) {
                game.batch.begin();
                game.batch.draw(metalDetectorTex,
                        demoCursorX - MD_W / 2f, demoCursorY - MD_H * 0.18f, MD_W, MD_H);
                game.batch.end();
            }
        }

        // Green follow-cursor circle during placing phase
        if (phase == Phase.PLACING && placedCircle == null) {
            Vector3 mouse = game.viewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 1f, 0f, 0.35f);
            shapes.circle(mouse.x, mouse.y, CIRCLE_RADIUS);
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.GREEN);
            shapes.circle(mouse.x, mouse.y, CIRCLE_RADIUS);
            shapes.end();
        }

        // Placed circle
        if (placedCircle != null) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 1f, 0f, 0.4f);
            shapes.circle(placedCircle.x, placedCircle.y, CIRCLE_RADIUS);
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.GREEN);
            shapes.circle(placedCircle.x, placedCircle.y, CIRCLE_RADIUS);
            shapes.end();
        }

        // Tape reveal circles during result
        if (phase == Phase.RESULT && Boolean.TRUE.equals(result)) {
            for (Vector2 tp : tapePositions) {
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(1f, 1f, 0f, 0.4f);
                shapes.circle(tp.x, tp.y, CIRCLE_RADIUS);
                shapes.end();
                shapes.begin(ShapeRenderer.ShapeType.Line);
                shapes.setColor(Color.YELLOW);
                shapes.circle(tp.x, tp.y, CIRCLE_RADIUS);
                shapes.end();
            }
        }

        // Metal detector sprite during detecting
        if (phase == Phase.DETECTING && metalDetectorTex != null) {
            game.batch.begin();
            game.batch.draw(metalDetectorTex,
                    cursorPos.x - MD_W / 2f, cursorPos.y - MD_H * 0.18f, MD_W, MD_H);
            game.batch.end();
        }

        // Progress bar timer bar
        if (phase == Phase.DETECTING && barFrames != null) {
            float elapsed = DETECT_DURATION - Math.max(0f, detectTimer);
            int frame = Math.min(BAR_FRAMES - 1, (int) (elapsed / DETECT_DURATION * BAR_FRAMES));
            if (barFrames[frame] != null) {
                game.batch.begin();
                float size = 400f, bx = -90f;
                float by = (DSAGame.SCREEN_HEIGHT - size) / 2f;
                game.batch.draw(barFrames[frame], bx, by, size, size);
                game.batch.end();
            }
        }

        // Result text overlay
        if (phase == Phase.RESULT) {
            game.batch.begin();
            // Simple colored overlay
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, 0.55f);
            shapes.rect(0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
            shapes.end();
            // Text
            com.badlogic.gdx.graphics.g2d.BitmapFont f = new com.badlogic.gdx.graphics.g2d.BitmapFont();
            f.getData().setScale(2f);
            f.setColor(Boolean.TRUE.equals(result) ? Color.YELLOW : Color.RED);
            String msg = Boolean.TRUE.equals(result) ? "TAPE FOUND!" : "MISSED — try again";
            f.draw(game.batch, msg, DSAGame.SCREEN_WIDTH / 2f - 120f, DSAGame.SCREEN_HEIGHT / 2f + 20f);
            f.dispose();
            game.batch.end();
        }
    }

    private void updateBeepInterval(float distance) {
        float clamped = Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, distance));
        float t = (clamped - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE);
        t = t * t * t;
        beepInterval = MIN_INTERVAL + t * (MAX_INTERVAL - MIN_INTERVAL);
    }

    private void startDetecting() {
        if (phase == Phase.DEMO) metalDetectorDemoAlreadySeen = true;
        phase = Phase.DETECTING;
        if (clockMusic != null) clockMusic.play();
    }

    private void playNarrator(String path) {
        if (narratorTrack != null) { narratorTrack.stop(); narratorTrack.dispose(); narratorTrack = null; }
        if (path == null) return;
        String[] candidates = { path, path.replace(".mp3", ".m4a"), path.replace(".mp3", ".wav") };
        for (String c : candidates) {
            if (Gdx.files.internal(c).exists()) {
                try { narratorTrack = Gdx.audio.newMusic(Gdx.files.internal(c)); narratorTrack.play(); return; }
                catch (Exception ignored) {}
            }
        }
        // No narrator file — skip straight to detecting if still in intro
        if (phase == Phase.NARRATOR_INTRO) startDetecting();
    }

    private Sound loadSound(String path) {
        if (Gdx.files.internal(path).exists()) {
            try { return Gdx.audio.newSound(Gdx.files.internal(path)); } catch (Exception ignored) {}
        }
        return null;
    }

    @Override public void resize(int w, int h) { game.viewport.update(w, h, true); }
    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        running = false;
        if (beepThread != null) beepThread.interrupt();
    }

    @Override
    public void dispose() {
        running = false;
        if (beepThread != null) beepThread.interrupt();
        if (bgMusic      != null) { bgMusic.stop();      bgMusic.dispose(); }
        if (clockMusic   != null) { clockMusic.stop();   clockMusic.dispose(); }
        if (narratorTrack!= null) { narratorTrack.stop();narratorTrack.dispose(); }
        if (dingSound    != null) dingSound.dispose();
        if (correctSound != null) correctSound.dispose();
        if (gameOverSound!= null) gameOverSound.dispose();
        if (roomTex      != null) roomTex.dispose();
        if (metalDetectorTex != null) metalDetectorTex.dispose();
        if (shapes != null) shapes.dispose();
        if (barFrames != null)
            for (Texture t : barFrames) if (t != null) t.dispose();
    }
}
