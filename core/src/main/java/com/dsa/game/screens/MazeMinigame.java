package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dsa.game.DSAGame;
import com.dsa.game.state.GameState;
import com.dsa.game.state.Tape;

public class MazeMinigame implements Screen {

    // ── Viewport & World ──────────────────────────────────────────────────────
    private static final float VW         = 1280f;
    private static final float VH         = 720f;
    private static final int   COLS       = 13;
    private static final int   CENTER_COL = 6;
    private static final int   LEFT_ARM   = 1;
    private static final int   RIGHT_ARM  = COLS - 2; // 11
    private static final float TILE = 64f;
    private static final float OX   = (VW - COLS * TILE) / 2f; // 224f

    // ── Tuning ────────────────────────────────────────────────────────────────
    private static final float PLAYER_SPEED          = 210f;
    private static final float FOG_RADIUS            = TILE * 3.6f;
    private static final float SHADOW_SPEED          = TILE * 1.1f;
    private static final float END_DISPLAY_DUR       = 4.5f;
    private static final float WIN_DISPLAY_DUR       = 7f;    // longer: shows timeline
    /** After loss, show +2 awareness briefly then restart the maze (no exit). */
    private static final float LOSE_RESTART_DELAY    = 2.0f;
    private static final int   MAX_MONSTER_CATCHES   = 3;
    private static final int   DIR_COUNT             = 8;
    private static final int   DIR_FRAMES            = 8;
    private static final float DIR_FRAME_DUR         = 0.10f;
    private static final float SHADOW_FRAME_DUR      = 0.4f;
    private static final float MONSTER_SPEED         = 155f;
    private static final float MONSTER_CATCH_RADIUS  = TILE * 1.0f;
    private static final float MONSTER_ANIM_DUR      = 0.32f;
    private static final float DEAD_END_LIE_DUR      = 2.2f;

    // ── Dynamic maze size ────────────────────────────────────────────────────
    // Rows = 3 approach + forks.length * 8.  Computed after setupForks().
    private int      rows;
    private int[][]  maze;

    // ── Fork inner class ──────────────────────────────────────────────────────
    private static class Fork {
        final boolean trueIsLeft;
        final String  leftStatement;
        final String  rightStatement;
        final String  leftMarked;
        final String  rightMarked;
        /** The false statement shown when the player enters the wrong arm. */
        final String  lie;
        int     junctionRow  = 0;
        int     deadEndRow   = 0;
        boolean cleared      = false;
        boolean wrongVisited = false;

        Fork(boolean trueIsLeft, String left, String right) {
            this.trueIsLeft     = trueIsLeft;
            this.leftStatement  = left;
            this.rightStatement = right;
            this.lie            = trueIsLeft ? right : left;
            String[] m    = markDiff(left, right);
            this.leftMarked  = m[0];
            this.rightMarked = m[1];
        }

        private static String[] markDiff(String left, String right) {
            String[] lw = left.split(" ", -1);
            String[] rw = right.split(" ", -1);
            int minLen = Math.min(lw.length, rw.length);
            int start = 0;
            while (start < minLen && lw[start].equals(rw[start])) start++;
            int lEnd = lw.length - 1, rEnd = rw.length - 1;
            while (lEnd > start && rEnd > start && lw[lEnd].equals(rw[rEnd])) {
                lEnd--; rEnd--;
            }
            return new String[]{
                buildMarked(lw, start, lEnd),
                buildMarked(rw, start, rEnd)
            };
        }

        private static String buildMarked(String[] words, int from, int to) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                if (i == from) sb.append("[#7A1200FF]");
                sb.append(words[i]);
                if (i == to)   sb.append("[]");
                if (i < words.length - 1) sb.append(" ");
            }
            return sb.toString();
        }
    }

    // ── Arm-monster inner class ────────────────────────────────────────────────
    private static class ArmMonster {
        float   x, y;
        boolean active     = false;
        boolean tagged     = false;
        int     frame      = 0;
        float   frameTimer = 0f;
        ArmMonster(float x, float y) { this.x = x; this.y = y; }
    }

    private Fork[] forks;

    // ── State enum ────────────────────────────────────────────────────────────
    private enum Phase { NARRATOR_INTRO, PLAYING, WIN, LOSE }
    private Phase phase = Phase.PLAYING;

    static boolean introShown = false;

    /** Called by DemoScreen on completion so the narrator intro is not repeated. */
    public static void markIntroShown() { introShown = true; }

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final DSAGame   game;
    private final GameState gameState;
    private final Tape      tape;
    private final Runnable  onComplete;

    // ── LibGDX ────────────────────────────────────────────────────────────────
    private SpriteBatch       batch;
    private ShapeRenderer     shape;
    private FitViewport       viewport;
    private OrthographicCamera cam;
    private BitmapFont        font;
    private BitmapFont        smallFont;
    private GlyphLayout       layout;

    // ── Player ────────────────────────────────────────────────────────────────
    private float   playerX, playerY;
    private int     playerDir      = 0;
    private boolean playerMoving   = false;
    private int     dirFrameIdx    = 0;
    private float   dirFrameTimer  = 0f;

    private final Texture[][] playerTex = new Texture[DIR_COUNT][DIR_FRAMES];
    private static final String[][] PLAYER_PATHS = new String[DIR_COUNT][DIR_FRAMES];
    static {
        String[] names = {
            "walk_south","walk_southeast","walk_east","walk_northeast",
            "walk_north","walk_northwest","walk_west","walk_southwest"
        };
        for (int d = 0; d < DIR_COUNT; d++)
            for (int f = 0; f < DIR_FRAMES; f++)
                PLAYER_PATHS[d][f] = "rooms/endings/" + names[d] + "/" + names[d] + "_" + f + ".png";
    }

    // ── Shadow ────────────────────────────────────────────────────────────────
    private float   shadowX, shadowY;
    private int     shadowFrame      = 0;
    private float   shadowFrameTimer = 0f;
    private final Texture[] shadowTex = new Texture[3];

    // ── Tile textures ─────────────────────────────────────────────────────────
    private final Texture[] wallTex = new Texture[3];
    private Texture floorTex;
    private Texture exitTex;
    private Texture deadEndTex;
    private Texture parchmentTex;
    private com.badlogic.gdx.audio.Music mazeMusic;
    private com.badlogic.gdx.audio.Music victoryMusic;
    /** True when using a dedicated maze track with custom seamless restart; false for looped fallbacks. */
    private boolean mazeMusicSeamlessLoop = false;
    /** Paused {@link DSAGame#bgMusic} while the maze runs so two tracks never stack. */
    private boolean pausedMainBgm         = false;
    private float   musicTimer         = 0f;
    private boolean musicStarted       = false;
    private float   musicElapsed       = 0f;  // time since music last started
    private float   musicDuration      = -1f; // learned after first full playthrough
    private boolean musicLoopTriggered = false;

    // ── Parchment transition ──────────────────────────────────────────────────
    private static final float PARCH_FADE_DUR = 0.45f; // seconds for fade in/out
    private Fork    lastActiveFork = null;
    private float   parchAlpha     = 0f;
    private boolean parchFadingIn  = false;
    private float   lastDelta      = 0f;

    // ── Arm monsters ─────────────────────────────────────────────────────────
    private ArmMonster[] armMonsters;

    // ── Progress ──────────────────────────────────────────────────────────────
    private int monsterCatches = 0;
    private int forksCleared   = 0;

    // ── Scoring ───────────────────────────────────────────────────────────────
    private static final int SCORE_FORK_CLEAR     =  30;
    private static final int SCORE_MONSTER_CATCH  = -10;
    private static final int SCORE_WIN_BONUS      =  50;
    private static final int SCORE_NO_WRONG_BONUS =  30;
    private int score = 0;

    // ── Truth timeline (selection sort) ───────────────────────────────────────
    private String[] timeline;
    private int[]    timelineRanks;
    private int      timelineSize = 0;

    // ── Act break ─────────────────────────────────────────────────────────────
    /** Fork index at which to trigger the act break (-1 = no break). */

    // ── Dead-end lie flash ────────────────────────────────────────────────────
    private String deadEndLieText  = "";
    private float  deadEndLieTimer = 0f;

    // ── Shadow catch effect ───────────────────────────────────────────────────
    private float shadowCatchCooldown = 0f;
    private float shadowFlashTimer    = 0f;
    private static final float SHADOW_CATCH_COOLDOWN = 3f;
    private static final float SHADOW_FLASH_DUR      = 0.5f;

    // ── End screen ────────────────────────────────────────────────────────────
    private float  endTimer = 0f;

    // ── Parchment layout (tuned via debug) ───────────────────────────────────
    private static final float PARCH_IMG_OFF_X = -58f;
    private static final float PARCH_IMG_OFF_Y = -33f;
    private static final float PARCH_IMG_W     = 525f;
    private static final float PARCH_IMG_H     = 273f;
    private static final float PARCH_PANEL_W   = 400f;
    private static final float PARCH_PANEL_YOFF = 8f;
    // Text areas (screen-relative to camLeft/camBot, tuned via rect debug)
    private static final float TXT_L_X = 122f; private static final float TXT_L_Y = 68f;
    private static final float TXT_L_W = 209f; private static final float TXT_L_H = 99f;
    private static final float TXT_R_X = 955f; private static final float TXT_R_Y = 73f;
    private static final float TXT_R_W = 215f; private static final float TXT_R_H = 92f;

    // =========================================================================

    public MazeMinigame(DSAGame game, GameState gameState, Tape tape, Runnable onComplete) {
        this.game       = game;
        this.gameState  = gameState;
        this.tape       = tape;
        this.onComplete = onComplete;
        setupForks();
        // Allocate dynamic maze and timeline arrays based on fork count
        rows          = 3 + forks.length * 8;
        maze          = new int[rows][COLS];
        timeline      = new String[forks.length];
        timelineRanks = new int[forks.length];
        buildMaze();
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Builds the maze dynamically from the fork answers.
     * Layout per fork section (8 rows):
     *   [0] junction row — full width open
     *   [1-3] both arms open (dead-end at row 3)
     *   [4] turn row: horizontal passage from correct arm back to centre col
     *   [5-7] centre corridor approaching next fork (or exit)
     *
     * Preceded by 3 approach rows (centre col only).
     * Total rows = 3 + forks.length * 8.
     */
    private void buildMaze() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < COLS; c++)
                maze[r][c] = 0;

        // Approach corridor (rows 0–2)
        for (int r = 0; r < 3; r++) maze[r][CENTER_COL] = 1;

        int row = 3;
        armMonsters = new ArmMonster[forks.length];
        for (int i = 0; i < forks.length; i++) {
            Fork f = forks[i];
            f.junctionRow = row;

            // Junction: full width (cols 1..COLS-2)
            for (int c = 1; c < COLS - 1; c++) maze[row][c] = 1;
            row++;

            // Both arms open for 3 rows
            maze[row][LEFT_ARM]  = 1;
            maze[row][RIGHT_ARM] = 1;
            row++;
            maze[row][LEFT_ARM]  = 1;
            maze[row][RIGHT_ARM] = 1;
            row++;
            maze[row][LEFT_ARM]  = 1;
            maze[row][RIGHT_ARM] = 1;
            f.deadEndRow = row;
            row++;

            // Arm monster at dead end of wrong arm
            int wrongCol   = f.trueIsLeft ? RIGHT_ARM : LEFT_ARM;
            armMonsters[i] = new ArmMonster(worldX(wrongCol), worldY(f.deadEndRow));

            // Turn row: correct arm reconnects to centre
            int correctCol = f.trueIsLeft ? LEFT_ARM : RIGHT_ARM;
            int minC = Math.min(correctCol, CENTER_COL);
            int maxC = Math.max(correctCol, CENTER_COL);
            for (int c = minC; c <= maxC; c++) maze[row][c] = 1;
            row++;

            // Centre corridor (3 rows) leading to next fork or exit
            for (int r2 = 0; r2 < 3; r2++) maze[row + r2][CENTER_COL] = 1;
            row += 3;
        }
    }

    private void setupForks() {
        switch (tape) {

            // ── TAPE 1: The Argument ────────────────────────────────────────
            case TAPE_ARGUMENT:
                forks = new Fork[]{
                    new Fork(true,
                        "Harold confronted James about money missing from the estate accounts",
                        "Harold confronted James about losses in the estate accounts"),
                    new Fork(true,
                        "The argument was about fifty thousand pounds taken without authorisation",
                        "The argument was about fifty thousand pounds invested without authorisation"),
                    new Fork(true,
                        "Harold said the will would be changed at nine o'clock the next morning",
                        "Harold said the will could be changed at nine o'clock the next morning"),
                    new Fork(true,
                        "Harold knew Daniel had been moving money through the estate accounts",
                        "Harold suspected Daniel had been moving money through the estate accounts"),
                    new Fork(true,
                        "Harold said both James and Daniel would be dismissed at nine AM",
                        "Harold said James alone would be dismissed at nine AM"),
                };
                break;

            // ── TAPE 2: James Vance Interview ───────────────────────────────
            case TAPE_JAMES_INTERVIEW:
                forks = new Fork[]{
                    new Fork(false,
                        "James described a heated but private discussion with his father",
                        "James described a calm and private discussion with his father"),
                    new Fork(false,
                        "James said he had no knowledge of the missing fifty thousand pounds",
                        "James said he had borrowed the fifty thousand pounds with his father's knowledge"),
                    new Fork(true,
                        "No one could place James in his room after eleven-thirty",
                        "James could place himself in his room after eleven-thirty"),
                    new Fork(true,
                        "James learned the will was being changed the following morning",
                        "James claimed he did not know the will was being changed"),
                    new Fork(true,
                        "James asked for a solicitor when the finances were mentioned",
                        "James asked for a solicitor when the murder was mentioned"),
                };
                break;

            // ── TAPE 3: Daniel Hobbs Interview ──────────────────────────────
            case TAPE_DANIEL_INTERVIEW:
                forks = new Fork[]{
                    new Fork(true,
                        "November 15th has no entry in Daniel's logbook",
                        "November 15th has an incomplete entry in Daniel's logbook"),
                    new Fork(true,
                        "Twenty thousand pounds appeared in Daniel's account in a single deposit",
                        "Twenty thousand pounds appeared in Daniel's account across several deposits"),
                    new Fork(true,
                        "The argument was audible from the carriage house",
                        "The argument was not audible from the carriage house"),
                    new Fork(true,
                        "Daniel said 'I was helping move\u2014' before stopping himself",
                        "Daniel said 'I was helping move\u2014' then clarified he meant the furniture"),
                    new Fork(true,
                        "Moving a body down cellar stairs requires two people",
                        "Moving a body down cellar stairs requires one strong person"),
                    new Fork(true,
                        "Daniel avoided eye contact with everyone the morning after",
                        "Daniel avoided eye contact with Margaret the morning after"),
                };
                break;

            // ── TAPE 4: Margaret Vance Interview ────────────────────────────
            case TAPE_MARGARET_INTERVIEW:
                forks = new Fork[]{
                    new Fork(true,
                        "Margaret said dinner was tense \u2014 her father and Marcus argued",
                        "Margaret said dinner was quiet \u2014 her father and Marcus barely spoke"),
                    new Fork(true,
                        "Margaret heard her father say 'the will' and 'tomorrow morning'",
                        "Margaret heard raised voices but could not make out the words"),
                    new Fork(false,
                        "Margaret heard one set of footsteps on the landing at midnight",
                        "Margaret heard two voices on the landing at midnight"),
                    new Fork(true,
                        "The footsteps went toward the study, then toward the cellar stairs",
                        "The footsteps went toward the study, then back toward the bedrooms"),
                    new Fork(true,
                        "Margaret heard dragging sounds at two in the morning",
                        "Margaret heard dragging sounds just after midnight"),
                };
                break;

            // ── TAPE 5: Marcus Blackwood Interview ──────────────────────────
            case TAPE_MARCUS_INTERVIEW:
                forks = new Fork[]{
                    new Fork(true,
                        "Marcus left the manor at eleven. The hotel logged him at 11:47.",
                        "Marcus left the manor at eleven. The hotel logged him at 12:47."),
                    new Fork(true,
                        "Harold left Marcus alone in the parlor for two hours",
                        "Harold left Marcus alone in the parlor for twenty minutes"),
                    new Fork(false,
                        "Marcus said he heard nothing of the argument from the parlor",
                        "Marcus said he heard Harold's voice through the study door"),
                    new Fork(true,
                        "Marcus saw James leave the study looking shaken",
                        "Marcus saw James leave the study looking composed"),
                    new Fork(true,
                        "Marcus saw a light in Charles's window as he drove away",
                        "Marcus saw no lights in the manor as he drove away"),
                };
                break;

            // ── TAPE 6: Charles Webb Interview ──────────────────────────────
            case TAPE_CHARLES_INTERVIEW:
                forks = new Fork[]{
                    new Fork(true,
                        "Charles was preparing documents to disinherit James entirely",
                        "Charles was preparing documents to reduce James's share of the estate"),
                    new Fork(true,
                        "Charles heard James and Harold shouting from his room",
                        "Charles heard raised voices but thought nothing of it"),
                    new Fork(true,
                        "Charles saw James walk toward the study at 10:45 \u2014 grim, determined",
                        "Charles saw James walk toward the study at 10:45 \u2014 hesitant, uncertain"),
                    new Fork(true,
                        "Charles never saw James return from the direction of the study",
                        "Charles saw James return from the direction of the study an hour later"),
                    new Fork(true,
                        "Charles said Daniel would do anything James asked of him",
                        "Charles said Daniel was loyal to Harold above all others"),
                };
                break;

            // ── Default (fallback) ───────────────────────────────────────────
            default:
                forks = new Fork[]{
                    new Fork(true,  "TRUTH", "DISTORTION"),
                    new Fork(false, "DISTORTION", "TRUTH"),
                    new Fork(true,  "TRUTH", "DISTORTION"),
                };
                break;
        }
    }


    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    public void show() {
        batch    = new SpriteBatch();
        shape    = new ShapeRenderer();
        cam      = new OrthographicCamera();
        viewport = new FitViewport(VW, VH, cam);
        font     = new BitmapFont();
        smallFont= new BitmapFont();
        font.getData().setScale(1.6f);
        smallFont.getData().setScale(0.88f);
        smallFont.getData().markupEnabled = true;
        layout   = new GlyphLayout();

        for (int d = 0; d < DIR_COUNT; d++)
            for (int f = 0; f < DIR_FRAMES; f++)
                if (Gdx.files.internal(PLAYER_PATHS[d][f]).exists())
                    playerTex[d][f] = new Texture(Gdx.files.internal(PLAYER_PATHS[d][f]));

        for (int i = 0; i < 3; i++) {
            String p = "minigames/shadow entity" + (i + 1) + ".png";
            if (Gdx.files.internal(p).exists()) shadowTex[i] = new Texture(Gdx.files.internal(p));
        }
        for (int i = 0; i < 3; i++) {
            String p = "minigames/wall tile variant" + (i + 1) + ".png";
            if (Gdx.files.internal(p).exists()) wallTex[i] = new Texture(Gdx.files.internal(p));
        }
        if (Gdx.files.internal("minigames/floortile.png").exists())
            floorTex = new Texture(Gdx.files.internal("minigames/floortile.png"));
        if (Gdx.files.internal("minigames/exitvisual.png").exists())
            exitTex  = new Texture(Gdx.files.internal("minigames/exitvisual.png"));
        if (Gdx.files.internal("minigames/deadend.jpg").exists())
            deadEndTex = new Texture(Gdx.files.internal("minigames/deadend.jpg"));
        if (Gdx.files.internal("parchment.png").exists())
            parchmentTex = new Texture(Gdx.files.internal("parchment.png"));
        loadMazeAndVictoryMusic();

        if (game.bgMusic != null && game.bgMusic.isPlaying()) {
            game.bgMusic.pause();
            pausedMainBgm = true;
        }

        playerX = worldX(CENTER_COL);
        playerY = worldY(0);
        shadowX = worldX(CENTER_COL);
        shadowY = worldY(rows - 2);

        if (!introShown) phase = Phase.NARRATOR_INTRO;
    }

    /**
     * Maze BGM: same as Testing 5 — {@code maze_music.mp3} at assets root (dedicated track with seamless-style loop).
     * Optional alternates only if root is missing; then manor / inventory ambience (simple loop, not ideal for maze).
     */
    private void loadMazeAndVictoryMusic() {
        String[] mazeCandidates = {
            "maze_music.mp3",
            "music/maze_music.mp3",
            "sfx/maze_music.mp3"
        };
        for (String p : mazeCandidates) {
            if (Gdx.files.internal(p).exists()) {
                mazeMusic = Gdx.audio.newMusic(Gdx.files.internal(p));
                mazeMusicSeamlessLoop = true;
                break;
            }
        }
        if (mazeMusic == null) {
            mazeMusicSeamlessLoop = false;
            if (Gdx.files.internal("music/manor_ambience.mp3").exists())
                mazeMusic = Gdx.audio.newMusic(Gdx.files.internal("music/manor_ambience.mp3"));
            else if (Gdx.files.internal("sfx/inventory/inventory_music.mp3").exists())
                mazeMusic = Gdx.audio.newMusic(Gdx.files.internal("sfx/inventory/inventory_music.mp3"));
        }
        if (Gdx.files.internal("victory.mp3").exists())
            victoryMusic = Gdx.audio.newMusic(Gdx.files.internal("victory.mp3"));
    }

    // ── Coordinate helpers ────────────────────────────────────────────────────

    private static float worldX(int col) { return OX + col * TILE; }

    private float worldY(int row) { return (rows - 1 - row) * TILE; }

    private static int toCol(float wx) {
        return MathUtils.clamp((int)((wx - OX) / TILE), 0, COLS - 1);
    }

    private int toRow(float wy) {
        return MathUtils.clamp(rows - 1 - (int)(wy / TILE), 0, rows - 1);
    }

    private float pcx() { return playerX + TILE / 2f; }
    private float pcy() { return playerY + TILE / 2f; }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        switch (phase) {
            case NARRATOR_INTRO:
                if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
                    introShown = true;
                    phase      = Phase.PLAYING;
                }
                break;
            case PLAYING:
                update(delta);
                break;
            case LOSE:
                endTimer += delta;
                if (endTimer >= LOSE_RESTART_DELAY) {
                    restartMazeAfterLoss();
                    return;
                }
                break;
            case WIN:
                endTimer += delta;
                if (endTimer >= WIN_DISPLAY_DUR) {
                    onComplete.run();
                    return;
                }
                break;
        }

        lastDelta = delta;
        snapCamera();
        draw();
    }

    private void snapCamera() {
        float minCamY = VH / 2f;
        float maxCamY = rows * TILE - VH / 2f;
        float camY = MathUtils.clamp(pcy(), minCamY, maxCamY);
        cam.position.set(VW / 2f, camY, 0f);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
        shape.setProjectionMatrix(cam.combined);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    private void update(float delta) {
        if (!musicStarted) {
            musicTimer += delta;
            if (musicTimer >= 3f && mazeMusic != null) {
                if (mazeMusicSeamlessLoop) {
                    mazeMusic.setLooping(false);
                    mazeMusic.setOnCompletionListener(m -> {
                        if (musicDuration < 0f) musicDuration = musicElapsed;
                        musicElapsed = 0f;
                        musicLoopTriggered = false;
                        m.play();
                    });
                } else {
                    mazeMusic.setLooping(true);
                    mazeMusic.setVolume(0.45f);
                }
                mazeMusic.play();
                musicStarted = true;
            }
        } else if (mazeMusic != null && mazeMusicSeamlessLoop) {
            musicElapsed += delta;
            if (musicDuration > 0f && !musicLoopTriggered
                    && musicElapsed >= musicDuration - 5f) {
                musicLoopTriggered = true;
                musicElapsed = 0f;
                mazeMusic.setPosition(0f);
            }
        }
        if (shadowCatchCooldown > 0f) shadowCatchCooldown -= delta;
        if (shadowFlashTimer    > 0f) shadowFlashTimer    -= delta;
        if (deadEndLieTimer     > 0f) deadEndLieTimer     -= delta;
        movePlayer(delta);
        animatePlayer(delta);
        moveShadow(delta);
        animateShadow(delta);
        updateArmMonsters(delta);
        checkFork();
        checkExit();
        checkShadowCatch();
        checkArmMonsterCatches();
    }

    private void movePlayer(float delta) {
        boolean up    = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean down  = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean left  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        float dx = 0f, dy = 0f;
        if (left)  dx -= PLAYER_SPEED * delta;
        if (right) dx += PLAYER_SPEED * delta;
        if (up)    dy += PLAYER_SPEED * delta;
        if (down)  dy -= PLAYER_SPEED * delta;

        playerMoving = (dx != 0f || dy != 0f);

        if (playerMoving) {
            if      (dx > 0 && dy  > 0) playerDir = 3;
            else if (dx < 0 && dy  > 0) playerDir = 5;
            else if (dx > 0 && dy  < 0) playerDir = 1;
            else if (dx < 0 && dy  < 0) playerDir = 7;
            else if (dx > 0)            playerDir = 2;
            else if (dx < 0)            playerDir = 6;
            else if (dy > 0)            playerDir = 4;
            else                        playerDir = 0;
            slideMoveX(dx);
            slideMoveY(dy);
        }
    }

    private void slideMoveX(float dx) {
        float nx = playerX + dx;
        if (passable(nx, playerY)) playerX = nx;
    }

    private void slideMoveY(float dy) {
        float ny = playerY + dy;
        if (passable(playerX, ny)) playerY = ny;
    }

    private boolean passable(float px, float py) {
        float m = 3f;
        return tileOpen(px + m,       py + m)
            && tileOpen(px + TILE - m, py + m)
            && tileOpen(px + m,       py + TILE - m)
            && tileOpen(px + TILE - m, py + TILE - m);
    }

    private boolean tileOpen(float wx, float wy) {
        if (rows - 1 - (int)(wy / TILE) < 0) return false; // above top row = wall
        int c = toCol(wx), r = toRow(wy);
        return maze[r][c] != 0;
    }

    private void animatePlayer(float delta) {
        if (playerMoving) {
            dirFrameTimer += delta;
            if (dirFrameTimer >= DIR_FRAME_DUR) {
                dirFrameTimer -= DIR_FRAME_DUR;
                dirFrameIdx = (dirFrameIdx + 1) % DIR_FRAMES;
            }
        } else {
            dirFrameIdx = 0;
        }
    }

    private void moveShadow(float delta) {
        float dx = pcx() - (shadowX + TILE / 2f);
        float dy = pcy() - (shadowY + TILE / 2f);
        float d  = (float) Math.sqrt(dx * dx + dy * dy);
        if (d > 1f) {
            shadowX += (dx / d) * SHADOW_SPEED * delta;
            shadowY += (dy / d) * SHADOW_SPEED * delta;
        }
    }

    private void animateShadow(float delta) {
        shadowFrameTimer += delta;
        if (shadowFrameTimer >= SHADOW_FRAME_DUR) {
            shadowFrameTimer -= SHADOW_FRAME_DUR;
            shadowFrame = (shadowFrame + 1) % 3;
        }
    }

    /**
     * Selection sort — repeatedly selects the entry with the smallest
     * chronological rank from the unsorted portion and swaps it to the front.
     * Keeps the HUD timeline in correct order as new truths are added.
     */
    private void selectionSortTimeline() {
        for (int i = 0; i < timelineSize - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < timelineSize; j++) {
                if (timelineRanks[j] < timelineRanks[minIdx]) minIdx = j;
            }
            if (minIdx != i) {
                int tmpR = timelineRanks[i];
                timelineRanks[i] = timelineRanks[minIdx];
                timelineRanks[minIdx] = tmpR;
                String tmpS = timeline[i];
                timeline[i] = timeline[minIdx];
                timeline[minIdx] = tmpS;
            }
        }
    }

    private void addToTimeline(Fork f, int forkIndex) {
        String truth = f.trueIsLeft ? f.leftStatement : f.rightStatement;
        timelineRanks[timelineSize] = forkIndex;
        timeline[timelineSize]      = truth;
        timelineSize++;
        selectionSortTimeline();
    }

    private void finalizeScore() {
        if (monsterCatches == 0) score += SCORE_NO_WRONG_BONUS;
        gameState.setLastMinigameScore(score);
    }

    /** Reset maze state and resume PLAYING after the loss overlay (awareness +2 already applied). */
    private void restartMazeAfterLoss() {
        phase = Phase.PLAYING;
        endTimer = 0f;
        monsterCatches = 0;
        forksCleared = 0;
        score = 0;
        timelineSize = 0;
        for (Fork f : forks) {
            f.cleared = false;
            f.wrongVisited = false;
        }
        if (armMonsters != null) {
            for (int i = 0; i < armMonsters.length; i++) {
                Fork f = forks[i];
                int wrongCol = f.trueIsLeft ? RIGHT_ARM : LEFT_ARM;
                ArmMonster m = armMonsters[i];
                m.x = worldX(wrongCol);
                m.y = worldY(f.deadEndRow);
                m.active = false;
                m.tagged = false;
                m.frame = 0;
                m.frameTimer = 0f;
            }
        }
        playerX = worldX(CENTER_COL);
        playerY = worldY(0);
        shadowX = worldX(CENTER_COL);
        shadowY = worldY(rows - 2);
        lastActiveFork = null;
        parchAlpha = 0f;
        parchFadingIn = false;
        deadEndLieText = "";
        deadEndLieTimer = 0f;
        shadowCatchCooldown = 0f;
        shadowFlashTimer = 0f;
        dirFrameIdx = 0;
        dirFrameTimer = 0f;
    }

    private void checkFork() {
        int pRow = toRow(pcy());
        int pCol = toCol(pcx());
        for (int i = 0; i < forks.length; i++) {
            Fork f = forks[i];

            // Fork cleared: player passed the reconnect turn row
            if (!f.cleared && pRow > f.junctionRow + 4) {
                f.cleared = true;
                forksCleared++;
                score += SCORE_FORK_CLEAR;
                addToTimeline(f, i);

            }

            // Wrong arm entered: flash the lie statement
            if (!f.wrongVisited && !f.cleared) {
                int wrongCol = f.trueIsLeft ? RIGHT_ARM : LEFT_ARM;
                if (pRow > f.junctionRow && pRow <= f.deadEndRow && pCol == wrongCol) {
                    f.wrongVisited  = true;
                    deadEndLieText  = "\u201C" + f.lie + "\u201D";
                    deadEndLieTimer = DEAD_END_LIE_DUR;
                }
            }
        }
    }

    private void updateArmMonsters(float delta) {
        for (ArmMonster m : armMonsters) {
            if (!m.active) {
                if (m.tagged) continue;
                float dx = pcx() - (m.x + TILE / 2f);
                float dy = pcy() - (m.y + TILE / 2f);
                if (dx * dx + dy * dy < FOG_RADIUS * FOG_RADIUS) {
                    m.active = true;
                    gameState.addAwareness(1);
                }
                continue;
            }

            float dx = pcx() - (m.x + TILE / 2f);
            float dy = pcy() - (m.y + TILE / 2f);
            float d  = (float) Math.sqrt(dx * dx + dy * dy);
            if (d > 1f) {
                m.x += (dx / d) * MONSTER_SPEED * delta;
                m.y += (dy / d) * MONSTER_SPEED * delta;
            }

            m.frameTimer += delta;
            if (m.frameTimer >= MONSTER_ANIM_DUR) {
                m.frameTimer -= MONSTER_ANIM_DUR;
                m.frame = (m.frame + 1) % 3;
            }
        }
    }

    private void checkArmMonsterCatches() {
        for (ArmMonster m : armMonsters) {
            if (!m.active) continue;
            float dx = (m.x + TILE / 2f) - pcx();
            float dy = (m.y + TILE / 2f) - pcy();
            if (dx * dx + dy * dy < MONSTER_CATCH_RADIUS * MONSTER_CATCH_RADIUS) {
                gameState.addAwareness(2);
                monsterCatches++;
                score = Math.max(0, score + SCORE_MONSTER_CATCH);
                shadowFlashTimer = SHADOW_FLASH_DUR;
                m.active = false;
                m.tagged = true;
                if (monsterCatches >= MAX_MONSTER_CATCHES) {
                    gameState.addAwareness(2);
                    phase = Phase.LOSE;
                    endTimer = 0f;
                }
            }
        }
    }

    private void checkExit() {
        if (toCol(pcx()) == CENTER_COL && toRow(pcy()) >= rows - 1) {
            finalizeScore();
            if (mazeMusic != null) { mazeMusic.stop(); }
            if (victoryMusic != null) victoryMusic.play();
            phase = Phase.WIN;
        }
    }

    private void checkShadowCatch() {
        if (shadowCatchCooldown > 0f) return;
        float dx = (shadowX + TILE / 2f) - pcx();
        float dy = (shadowY + TILE / 2f) - pcy();
        float r  = TILE * 1.2f;
        if (dx * dx + dy * dy < r * r) {
            gameState.addAwareness(1);
            shadowCatchCooldown = SHADOW_CATCH_COOLDOWN;
            shadowX = worldX(CENTER_COL);
            shadowY = worldY(1);
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    private void draw() {
        // Phase 1 — world
        batch.begin();
        drawTiles();
        drawShadow();
        drawArmMonsters();
        drawPlayer();
        batch.end();

        // Phase 2 — fog of war
        drawFog();

        // Phase 3 — overlays & HUD
        if (shadowFlashTimer > 0f) {
            float alpha = (shadowFlashTimer / SHADOW_FLASH_DUR) * 0.55f;
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0.8f, 0f, 0f, alpha);
            shape.rect(camLeft(), camBot(), VW, VH);
            shape.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        batch.begin();
        drawHUD();
        drawForkLabels();
        batch.end();

        // Phase 4 — narrator intro overlay
        if (phase == Phase.NARRATOR_INTRO) {
            drawNarratorIntro();
        }

        // Phase 5 — end screen
        if (phase == Phase.WIN || phase == Phase.LOSE) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0f, 0f, 0f, 0.88f);
            shape.rect(camLeft(), camBot(), VW, VH);
            shape.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            batch.begin();
            drawEndText();
            batch.end();
        }
    }

    private float camLeft() { return cam.position.x - VW / 2f; }
    private float camBot()  { return cam.position.y - VH / 2f; }

    private void drawTiles() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLS; c++) {
                float wx = worldX(c);
                float wy = worldY(r);
                if (maze[r][c] == 0) {
                    Texture wt = wallTex[(r * COLS + c) % 3];
                    if (wt != null) batch.draw(wt, wx, wy, TILE, TILE);
                } else {
                    boolean isExit = (c == CENTER_COL && r == rows - 1);
                    Texture ft = (isExit && exitTex != null) ? exitTex : floorTex;
                    if (ft != null) batch.draw(ft, wx, wy, TILE, TILE);
                }
            }
        }
    }

    private void drawForkLabels() {
        Fork activeFork = null;
        float closestDist = Float.MAX_VALUE;
        for (Fork f : forks) {
            if (f.cleared) continue;
            float fy  = worldY(f.junctionRow) + TILE / 2f;
            float dist = Math.abs(pcy() - fy);
            if (dist < TILE * 4f && dist < closestDist) {
                closestDist = dist;
                activeFork  = f;
            }
        }

        // Detect fork change — trigger fade-out then fade-in on new fork
        if (activeFork != lastActiveFork) {
            if (activeFork == null) {
                // Walked away: fade out
                parchFadingIn = false;
            } else if (lastActiveFork == null) {
                // Approaching fresh: fade in
                parchAlpha    = 0f;
                parchFadingIn = true;
            } else {
                // Switched fork: restart fade-in
                parchAlpha    = 0f;
                parchFadingIn = true;
            }
            lastActiveFork = activeFork;
        }

        // Advance alpha
        float step = lastDelta / PARCH_FADE_DUR;
        if (parchFadingIn) {
            parchAlpha = Math.min(1f, parchAlpha + step);
        } else {
            parchAlpha = Math.max(0f, parchAlpha - step);
        }

        if (parchAlpha <= 0f) return;

        // Use lastActiveFork so we keep drawing during fade-out
        Fork drawFork = lastActiveFork;
        if (drawFork == null) return;

        float panelY      = camBot() + PARCH_PANEL_YOFF;
        float leftPanelX  = camLeft() + 20f;
        float rightPanelX = camLeft() + VW - PARCH_PANEL_W - 20f;

        // Draw parchment backgrounds with fade alpha
        if (parchmentTex != null) {
            batch.setColor(1f, 1f, 1f, parchAlpha);
            batch.draw(parchmentTex, leftPanelX  + PARCH_IMG_OFF_X, panelY + PARCH_IMG_OFF_Y, PARCH_IMG_W, PARCH_IMG_H);
            batch.draw(parchmentTex, rightPanelX + PARCH_IMG_OFF_X, panelY + PARCH_IMG_OFF_Y, PARCH_IMG_W, PARCH_IMG_H);
            batch.setColor(Color.WHITE);
        }

        Color textColor = new Color(0.22f, 0.10f, 0.02f, parchAlpha);
        smallFont.setColor(textColor);

        // Left text area — centre-centre
        layout.setText(smallFont, drawFork.leftMarked, textColor, TXT_L_W, 1, true);
        float lx = camLeft() + TXT_L_X;
        float ly = camBot()  + TXT_L_Y + (TXT_L_H + layout.height) / 2f;
        smallFont.draw(batch, layout, lx, ly);

        // Right text area — centre-centre
        layout.setText(smallFont, drawFork.rightMarked, textColor, TXT_R_W, 1, true);
        float rx = camLeft() + TXT_R_X;
        float ry = camBot()  + TXT_R_Y + (TXT_R_H + layout.height) / 2f;
        smallFont.draw(batch, layout, rx, ry);
    }

    private void drawShadow() {
        Texture st = shadowTex[shadowFrame];
        if (st != null) {
            batch.setColor(0.8f, 0.8f, 0.8f, 0.9f);
            batch.draw(st, shadowX, shadowY, TILE, TILE);
            batch.setColor(Color.WHITE);
        }
    }

    private void drawArmMonsters() {
        for (ArmMonster m : armMonsters) {
            if (!m.active) continue;
            Texture mt = shadowTex[m.frame];
            if (mt != null) {
                batch.setColor(1f, 0.12f, 0.12f, 0.92f);
                batch.draw(mt, m.x, m.y, TILE, TILE);
                batch.setColor(Color.WHITE);
            }
        }
    }

    private void drawPlayer() {
        Texture pt = playerTex[playerDir][dirFrameIdx];
        if (pt != null) batch.draw(pt, playerX, playerY, TILE, TILE);
    }

    private void drawFog() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLS; c++) {
                float cx = worldX(c) + TILE / 2f;
                float cy = worldY(r) + TILE / 2f;
                float dx = cx - pcx();
                float dy = cy - pcy();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > FOG_RADIUS) {
                    float a = Math.min(0.93f, (dist - FOG_RADIUS) / (TILE * 1.5f) + 0.45f);
                    shape.setColor(0f, 0f, 0f, a);
                    shape.rect(worldX(c), worldY(r), TILE, TILE);
                }
            }
        }
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawHUD() {
        float x = camLeft() + 18f;
        float y = camBot()  + VH - 16f;

        smallFont.setColor(new Color(0.95f, 0.9f, 0.7f, 1f));
        smallFont.draw(batch, "Score: " + score + "   Forks: " + forksCleared + " / " + forks.length, x, y);

        if (monsterCatches > 0) {
            smallFont.setColor(new Color(1f, 0.3f, 0.3f, 1f));
            smallFont.draw(batch, "Caught: " + monsterCatches + " / " + MAX_MONSTER_CATCHES, x, y - 22f);
        }

        // Truth timeline strip
        if (timelineSize > 0) {
            float ty = camBot() + VH - 58f;
            smallFont.setColor(new Color(0.45f, 0.85f, 0.7f, 1f));
            smallFont.draw(batch, "TIMELINE:", x, ty);
            for (int i = 0; i < timelineSize; i++) {
                smallFont.setColor(new Color(0.75f, 0.95f, 0.85f, 1f));
                smallFont.draw(batch, (i + 1) + ". " + timeline[i],
                    x + 8f, ty - 18f - i * 18f, VW / 2f, -1, true);
            }
        }

        // Dead-end lie flash — centred mid-screen
        if (deadEndLieTimer > 0f) {
            float alpha = Math.min(1f, deadEndLieTimer / 0.4f);
            smallFont.setColor(new Color(1f, 0.22f, 0.22f, alpha));
            layout.setText(smallFont, deadEndLieText);
            smallFont.draw(batch, deadEndLieText,
                camLeft() + (VW - layout.width) / 2f,
                camBot() + VH / 2f + 30f);
        }

        smallFont.setColor(Color.WHITE);
    }

    private void drawNarratorIntro() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.94f);
        shape.rect(camLeft(), camBot(), VW, VH);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();

        float bodyX = camLeft() + 300f;
        float bodyW = VW - 600f;
        Color dim   = new Color(0.42f, 0.40f, 0.35f, 1f);
        Color warm  = new Color(0.90f, 0.86f, 0.76f, 1f);

        // Opening stage direction
        smallFont.setColor(dim);
        smallFont.draw(batch,
            "[A voice settles into the silence. Not from any direction. From somewhere behind your thoughts.]",
            bodyX, camBot() + VH / 2f + 188f, bodyW, 1, true);

        // Narrator lines
        smallFont.setColor(warm);
        smallFont.draw(batch,
            "First tape. Good.",
            bodyX, camBot() + VH / 2f + 150f, bodyW, 1, true);

        smallFont.draw(batch,
            "You move with the arrow keys \u2014 WASD if you prefer. Walk toward the exit at the bottom of the corridor.",
            bodyX, camBot() + VH / 2f + 120f, bodyW, 1, true);

        smallFont.draw(batch,
            "At certain junctions the path splits. Two accounts of the same moment, one on each side. One is exactly what was said. The other has been changed \u2014 deliberately or otherwise, I can never quite tell. Walk toward the truth.",
            bodyX, camBot() + VH / 2f + 68f, bodyW, 1, true);

        smallFont.draw(batch,
            "If you go the wrong way, something in the dark will find you. Three times, and the memory collapses entirely \u2014 and your awareness rises. So. Try not to go the wrong way.",
            bodyX, camBot() + VH / 2f - 14f, bodyW, 1, true);

        smallFont.draw(batch,
            "There is also something behind you. It follows. It has always followed. Keep moving.",
            bodyX, camBot() + VH / 2f - 72f, bodyW, 1, true);

        smallFont.draw(batch,
            "You do not get to walk away from this. Not until you reach the other end. One way or another, you see it through.",
            bodyX, camBot() + VH / 2f - 104f, bodyW, 1, true);

        // Closing stage direction
        smallFont.setColor(dim);
        smallFont.draw(batch,
            "[A pause. The quality of the silence changes slightly.]",
            bodyX, camBot() + VH / 2f - 148f, bodyW, 1, true);

        // Closing line
        smallFont.setColor(warm);
        smallFont.draw(batch,
            "Trust what you read.",
            bodyX, camBot() + VH / 2f - 166f, bodyW, 1, true);

        // Prompt
        smallFont.setColor(new Color(0.36f, 0.36f, 0.36f, 1f));
        String prompt = "[ Press any key ]";
        layout.setText(smallFont, prompt);
        smallFont.draw(batch, prompt,
            camLeft() + (VW - layout.width) / 2f,
            camBot() + VH / 2f - 196f);

        batch.end();
    }

    private void drawEndText() {
        boolean won = (phase == Phase.WIN);

        font.setColor(won ? new Color(0.2f, 0.9f, 1f, 1f) : Color.RED);
        String header = won ? "MAZE COMPLETE" : "AWARENESS +2";
        layout.setText(font, header);
        font.draw(batch, header,
            camLeft() + (VW - layout.width) / 2f,
            camBot() + VH / 2f + 110f);

        // WIN: show sorted truth timeline
        if (won && timelineSize > 0) {
            smallFont.setColor(new Color(0.45f, 0.85f, 0.7f, 1f));
            smallFont.draw(batch, "WHAT THE TAPE REVEALS:",
                camLeft() + 240f, camBot() + VH / 2f + 60f);
            for (int i = 0; i < timelineSize; i++) {
                smallFont.setColor(new Color(0.78f, 0.96f, 0.86f, 1f));
                smallFont.draw(batch, (i + 1) + ". " + timeline[i],
                    camLeft() + 260f,
                    camBot() + VH / 2f + 38f - i * 22f,
                    VW - 520f, -1, true);
            }
        }

        smallFont.setColor(Color.WHITE);
    }

    /**
     * Instruction screen drawn in world-space (camera is positioned at top of maze).
     * All positions use camLeft() + x and camBot() + y offsets so the overlay is
     * always screen-aligned regardless of the camera's world position.
     */

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void resize(int w, int h) { viewport.update(w, h, true); }

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();
        smallFont.dispose();
        for (int d = 0; d < DIR_COUNT; d++)
            for (int f = 0; f < DIR_FRAMES; f++)
                if (playerTex[d][f] != null) playerTex[d][f].dispose();
        for (int i = 0; i < 3; i++) {
            if (shadowTex[i] != null) shadowTex[i].dispose();
            if (wallTex[i]   != null) wallTex[i].dispose();
        }
        if (floorTex   != null) floorTex.dispose();
        if (exitTex    != null) exitTex.dispose();
        if (deadEndTex   != null) deadEndTex.dispose();
        if (parchmentTex != null) parchmentTex.dispose();
        if (mazeMusic    != null) { mazeMusic.stop();   mazeMusic.dispose(); }
        if (victoryMusic != null) { victoryMusic.stop(); victoryMusic.dispose(); }
        resumeMainBgmIfNeeded();
    }

    private void resumeMainBgmIfNeeded() {
        if (pausedMainBgm && game.bgMusic != null) {
            game.bgMusic.play();
            pausedMainBgm = false;
        }
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide() {
        if (mazeMusic != null) mazeMusic.stop();
        if (victoryMusic != null) victoryMusic.stop();
        resumeMainBgmIfNeeded();
    }
}
