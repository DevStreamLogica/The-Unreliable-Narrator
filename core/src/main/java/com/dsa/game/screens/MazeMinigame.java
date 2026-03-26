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
    private static final int   MAX_MONSTER_CATCHES   = 3;
    private static final int   DIR_COUNT             = 8;
    private static final int   DIR_FRAMES            = 8;
    private static final float DIR_FRAME_DUR         = 0.10f;
    private static final float SHADOW_FRAME_DUR      = 0.4f;
    private static final float MONSTER_SPEED         = 155f;
    private static final float MONSTER_CATCH_RADIUS  = TILE * 1.0f;
    private static final float MONSTER_ANIM_DUR      = 0.32f;
    private static final float ACT_BREAK_DUR         = 4.5f;
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
    private enum Phase { INSTRUCTIONS, PLAYING, ACT_BREAK, WIN, LOSE }
    private Phase phase = Phase.INSTRUCTIONS;

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
    private int    actBreakAfterFork = -1;
    private String actTitle          = "";
    private String actNarration      = "";
    private float  actBreakTimer     = 0f;
    private boolean actBreakUsed     = false;

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
    private String winText;
    private String loseText;

    // ── Instruction screen ────────────────────────────────────────────────────
    private String   instructionHook;
    private String[] instructionObjective;
    private String[] instructionAvoid;
    private float    pulseTimer = 0f;

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
        setupNarratorTexts();
        setupInstructions();
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

            // ── TAPE 2: James Vance Interview ───────────────────────────────
            case TAPE_JAMES_INTERVIEW:
                actBreakAfterFork = 1;
                actTitle          = "ACT II \u2014 THE ALIBI";
                actNarration      = "He called it a loan.\nThe will was being signed at nine the next morning.";
                forks = new Fork[]{
                    new Fork(false,
                        "James says it was a calm business discussion",
                        "Witnesses heard shouting from the study"),
                    new Fork(false,
                        "James had no knowledge of missing funds",
                        "James took fifty thousand pounds \u2014 called it a loan"),
                    new Fork(true,
                        "No one can confirm James was in his room all night",
                        "James says he went to his room at eleven-thirty"),
                    new Fork(true,
                        "The will was being changed at nine AM the next morning",
                        "James claims he did not know about the will changes"),
                    new Fork(true,
                        "James demanded a solicitor the moment money was mentioned",
                        "James cooperated fully and answered every question"),
                };
                break;

            // ── TAPE 3: Daniel Hobbs Interview ──────────────────────────────
            case TAPE_DANIEL_INTERVIEW:
                actBreakAfterFork = 2;
                actTitle          = "ACT II \u2014 THE SLIP";
                actNarration      = "Twenty thousand pounds. Fifteen years of loyalty to the wrong man.\nAnd the logbook entry that was never written.";
                forks = new Fork[]{
                    new Fork(true,
                        "November 15th has no entry in Daniel's logbook",
                        "Daniel says he forgot to log it \u2014 it slipped his mind"),
                    new Fork(true,
                        "Daniel deposited twenty thousand pounds on a groundskeeper's salary",
                        "Daniel claims private gardening work paid for it"),
                    new Fork(true,
                        "The entire household heard the argument. The carriage house is no excuse.",
                        "Sound doesn't carry to the carriage house. Daniel heard nothing."),
                    new Fork(true,
                        "Daniel said 'I was helping move\u2014' before correcting himself",
                        "Daniel's account never changed or contradicted itself"),
                    new Fork(true,
                        "Moving a body down cellar stairs requires two people",
                        "Harold could have died anywhere and been found in the cellar"),
                    new Fork(true,
                        "Daniel wouldn't look at Margaret or anyone the next morning",
                        "Daniel was quiet by nature \u2014 he always kept to himself"),
                };
                break;

            // ── TAPE 4: Margaret Vance Interview ────────────────────────────
            case TAPE_MARGARET_INTERVIEW:
                actBreakAfterFork = 1;
                actTitle          = "ACT II \u2014 THE NIGHT";
                actNarration      = "She heard the word 'tomorrow.'\nShe didn't know what it meant yet.";
                forks = new Fork[]{
                    new Fork(true,
                        "Dinner was tense \u2014 Father and Marcus argued all evening",
                        "It was just a normal family dinner"),
                    new Fork(true,
                        "She heard 'the will' and 'tomorrow' during the argument",
                        "She only heard anger, couldn't make out any words"),
                    new Fork(false,
                        "She was always nervous, always hearing things",
                        "Two people whispering at midnight"),
                    new Fork(true,
                        "The footsteps went toward the study, then the cellar stairs",
                        "She was too frightened to know which direction they went"),
                    new Fork(true,
                        "The dragging sounds came at two in the morning",
                        "She fell asleep before she heard the dragging"),
                };
                break;

            // ── TAPE 5: Marcus Blackwood Interview ──────────────────────────
            case TAPE_MARCUS_INTERVIEW:
                actBreakAfterFork = 1;
                actTitle          = "ACT II \u2014 WHAT MARCUS SAW";
                actNarration      = "Two hours alone in the parlor.\nLong enough to decide. Long enough to change your mind.";
                forks = new Fork[]{
                    new Fork(true,
                        "Marcus left at eleven. The hotel confirms 11:47.",
                        "Marcus had no reason to harm Harold"),
                    new Fork(true,
                        "Harold abandoned Marcus alone in the parlor for two hours",
                        "Marcus and Harold talked through the entire evening"),
                    new Fork(false,
                        "He came voluntarily. Innocent men do that.",
                        "He heard 'money' and 'betrayal' \u2014 Harold's voice carried"),
                    new Fork(true,
                        "He saw James storm past after the argument \u2014 shaken",
                        "The alibi is confirmed. Move on."),
                    new Fork(true,
                        "He saw a light in Charles's window as he drove away at eleven",
                        "The manor was dark when Marcus left"),
                };
                break;

            // ── TAPE 6: Charles Webb Interview ──────────────────────────────
            case TAPE_CHARLES_INTERVIEW:
                actBreakAfterFork = 1;
                actTitle          = "ACT II \u2014 WHAT CHARLES SAW";
                actNarration      = "The documents were ready. The solicitor was due at nine.\nOne night stood between James and losing everything.";
                forks = new Fork[]{
                    new Fork(true,
                        "Charles was preparing documents to disinherit James entirely",
                        "Charles was doing routine filing for the estate"),
                    new Fork(true,
                        "Everyone in the house heard James and Harold shouting",
                        "Charles heard nothing unusual from his room"),
                    new Fork(true,
                        "At 10:45 James walked toward the study \u2014 grim, determined",
                        "Charles assumed James was going to apologise"),
                    new Fork(true,
                        "Charles never saw James return from the study",
                        "James was probably just restless after the argument"),
                    new Fork(true,
                        "Daniel is unhealthily loyal to James. He would do anything.",
                        "Daniel just did his job. Nothing suspicious."),
                };
                break;

            // ── Default (fallback) ───────────────────────────────────────────
            default:
                actBreakAfterFork = 1;
                actTitle          = "ACT II \u2014 THE TRUTH";
                actNarration      = "The record does not lie. Only the narrator does.";
                forks = new Fork[]{
                    new Fork(true,  "TRUTH", "DISTORTION"),
                    new Fork(false, "DISTORTION", "TRUTH"),
                    new Fork(true,  "TRUTH", "DISTORTION"),
                };
                break;
        }
    }

    private void setupNarratorTexts() {
        switch (tape) {
            case TAPE_JAMES_INTERVIEW:
                winText  = "He asked for his solicitor the moment finances came up.\nNot when accused of murder. Finances.";
                loseText = "A grieving son. Naturally defensive.\nThat is all I choose to see.";
                break;
            case TAPE_DANIEL_INTERVIEW:
                winText  = "'I was helping move\u2014'\nHe stopped. But he had already said it.";
                loseText = "A forgetful man. Groundskeepers have many tasks.\nThe missing log entry means nothing.";
                break;
            case TAPE_MARGARET_INTERVIEW:
                winText  = "The dragging stopped at the cellar door.\nIt always stops at the cellar door.";
                loseText = "Margaret was frightened. Old houses make sounds.\nThat is all it was.";
                break;
            case TAPE_MARCUS_INTERVIEW:
                winText  = "Marcus left at eleven. The manor was quiet after that.\nThat's when it starts paying attention.";
                loseText = "He had an alibi. Move on.";
                break;
            case TAPE_CHARLES_INTERVIEW:
                winText  = "Nobody walks toward that study and comes back the same.";
                loseText = "Charles assumed the best. People do.";
                break;
            default:
                winText  = "The truth surfaces.";
                loseText = "The distortion holds.";
        }
    }

    private void setupInstructions() {
        switch (tape) {
            case TAPE_JAMES_INTERVIEW:
                instructionHook      = "James Vance spoke carefully. Navigate his testimony to find what he was hiding.";
                instructionObjective = new String[]{
                    "Navigate the maze with  WASD  or  Arrow Keys",
                    "At each fork, choose the TRUE statement",
                    "The correct path continues downward to the exit",
                    "Cleared truths appear in your timeline at the top left"
                };
                instructionAvoid = new String[]{
                    "Wrong paths have monsters waiting at the dead end",
                    "Monsters chase you through walls once they spot you",
                    "You have 3 lives \u2014 each catch raises awareness",
                    "The shadow follows from behind and never stops"
                };
                break;
            case TAPE_DANIEL_INTERVIEW:
                instructionHook      = "Daniel almost said it once. Navigate fifteen years of carefully constructed lies.";
                instructionObjective = new String[]{
                    "Navigate with  WASD  or  Arrow Keys",
                    "Two acts, six forks \u2014 choose the TRUE statement each time",
                    "The correct arm reconnects to the centre path",
                    "Reach the exit at the bottom to surface the truth"
                };
                instructionAvoid = new String[]{
                    "Monsters guard every wrong arm \u2014 they will give chase",
                    "3 lives across the full maze",
                    "The shadow pursues you from above \u2014 it never stops",
                    "Too many catches and the distortion wins"
                };
                break;
            case TAPE_MARGARET_INTERVIEW:
                instructionHook      = "Margaret Vance testified under questioning. Fear distorted some of what she said.";
                instructionObjective = new String[]{
                    "Navigate with  WASD  or  Arrow Keys",
                    "At each fork, choose the TRUE statement",
                    "Two acts \u2014 the evening, then the night",
                    "Cleared truths build a sorted timeline at the top"
                };
                instructionAvoid = new String[]{
                    "Wrong paths trigger monsters that chase through walls",
                    "You have 3 lives shared across the whole maze",
                    "The shadow follows from behind \u2014 keep moving",
                    "Walking into a wrong path shows you its lie in red"
                };
                break;
            case TAPE_MARCUS_INTERVIEW:
                instructionHook      = "Marcus Blackwood came voluntarily. His alibi is almost too clean.";
                instructionObjective = new String[]{
                    "Navigate with  WASD  or  Arrow Keys",
                    "Choose the TRUE statement at each fork to progress",
                    "Two acts \u2014 the negotiation, then what Marcus saw",
                    "Exit at the bottom to complete the tape"
                };
                instructionAvoid = new String[]{
                    "Wrong arms have monsters waiting in the dark",
                    "3 lives across the maze \u2014 each catch raises awareness",
                    "The shadow approaches from behind constantly",
                    "Wrong paths flash their lie in red before the chase starts"
                };
                break;
            case TAPE_CHARLES_INTERVIEW:
                instructionHook      = "Charles Webb was Harold's most trusted assistant. He saw everything that night.";
                instructionObjective = new String[]{
                    "Navigate with  WASD  or  Arrow Keys",
                    "At each fork choose the TRUE statement",
                    "Two acts \u2014 the will, then what Charles witnessed",
                    "Reach the exit to reconstruct the full timeline"
                };
                instructionAvoid = new String[]{
                    "False paths have monsters at every dead end",
                    "3 lives total \u2014 each monster catch costs awareness",
                    "The shadow cannot be escaped, only outpaced",
                    "Entering a wrong arm shows you the lie it guards"
                };
                break;
            default:
                instructionHook      = "Navigate the truth. Avoid the distortion.";
                instructionObjective = new String[]{
                    "Navigate the maze with  WASD  or  Arrow Keys",
                    "At each fork, choose the TRUE statement",
                    "Reach the exit at the bottom to win"
                };
                instructionAvoid = new String[]{
                    "Wrong paths have monsters that chase you",
                    "You have 3 lives \u2014 each catch raises awareness",
                    "The shadow follows from behind at all times"
                };
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

        playerX = worldX(CENTER_COL);
        playerY = worldY(0);
        shadowX = worldX(CENTER_COL);
        shadowY = worldY(rows - 2);
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
            case INSTRUCTIONS:
                pulseTimer += delta;
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                 || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    phase = Phase.PLAYING;
                }
                snapCamera();
                drawInstructions();
                return;
            case PLAYING:
                update(delta);
                break;
            case ACT_BREAK:
                actBreakTimer += delta;
                if (actBreakTimer >= ACT_BREAK_DUR) phase = Phase.PLAYING;
                break;
            case WIN:
            case LOSE:
                endTimer += delta;
                float dur = (phase == Phase.WIN) ? WIN_DISPLAY_DUR : END_DISPLAY_DUR;
                if (endTimer >= dur) {
                    onComplete.run();
                    return;
                }
                break;
        }

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
        String tier = gameState.getMinigameScoreTier();
        if (tier.equals("DISTORTED")) gameState.addAwareness(1);
        if (tier.equals("CORRUPTED")) gameState.addAwareness(2);
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
                // Trigger act break if this is the designated break fork
                if (i == actBreakAfterFork && !actBreakUsed) {
                    actBreakUsed  = true;
                    actBreakTimer = 0f;
                    phase         = Phase.ACT_BREAK;
                }
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
                    finalizeScore();
                    phase = Phase.LOSE;
                }
            }
        }
    }

    private void checkExit() {
        if (toCol(pcx()) == CENTER_COL && toRow(pcy()) >= rows - 1) {
            finalizeScore();
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

        // Phase 4 — act break overlay
        if (phase == Phase.ACT_BREAK) {
            drawActBreak();
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
        if (activeFork == null) return;

        float panelW      = 360f;
        float panelH      = 80f;
        float panelY      = camBot() + 12f;
        float leftPanelX  = camLeft() + 20f;
        float rightPanelX = camLeft() + VW - panelW - 20f;

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.78f);
        shape.rect(leftPanelX, panelY, panelW, panelH);
        shape.setColor(0.55f, 0.45f, 0.25f, 1f);
        shape.rect(leftPanelX, panelY + panelH - 2f, panelW, 2f);
        shape.setColor(0f, 0f, 0f, 0.78f);
        shape.rect(rightPanelX, panelY, panelW, panelH);
        shape.setColor(0.55f, 0.45f, 0.25f, 1f);
        shape.rect(rightPanelX, panelY + panelH - 2f, panelW, 2f);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();

        smallFont.setColor(new Color(0.65f, 0.55f, 0.3f, 1f));
        smallFont.draw(batch, "\u2190 LEFT", leftPanelX + 6f, panelY + panelH - 4f);
        smallFont.setColor(new Color(0.95f, 0.9f, 0.8f, 1f));
        smallFont.draw(batch, activeFork.leftStatement,
                leftPanelX + 6f, panelY + panelH - 22f, panelW - 12f, -1, true);

        smallFont.setColor(new Color(0.65f, 0.55f, 0.3f, 1f));
        smallFont.draw(batch, "RIGHT \u2192", rightPanelX + panelW - 70f, panelY + panelH - 4f);
        smallFont.setColor(new Color(0.95f, 0.9f, 0.8f, 1f));
        smallFont.draw(batch, activeFork.rightStatement,
                rightPanelX + 6f, panelY + panelH - 22f, panelW - 12f, -1, true);
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

    /** Full-screen act break overlay — freezes gameplay, shows act title + narration. */
    private void drawActBreak() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.93f);
        shape.rect(camLeft(), camBot(), VW, VH);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();

        font.setColor(new Color(0.75f, 0.6f, 0.3f, 1f));
        layout.setText(font, actTitle);
        font.draw(batch, actTitle,
            camLeft() + (VW - layout.width) / 2f,
            camBot() + VH / 2f + 90f);

        // Decorative separator
        smallFont.setColor(new Color(0.5f, 0.42f, 0.22f, 1f));
        String sep = "\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014";
        layout.setText(smallFont, sep);
        smallFont.draw(batch, sep, camLeft() + (VW - layout.width) / 2f, camBot() + VH / 2f + 46f);

        smallFont.setColor(new Color(0.9f, 0.86f, 0.76f, 1f));
        smallFont.draw(batch, actNarration,
            camLeft() + 240f,
            camBot() + VH / 2f + 26f,
            VW - 480f, 1, true);

        // Fade-in "continuing..." hint at end of break
        float progress = actBreakTimer / ACT_BREAK_DUR;
        if (progress > 0.7f) {
            float alpha = (progress - 0.7f) / 0.3f;
            smallFont.setColor(new Color(0.5f, 0.5f, 0.5f, alpha));
            String cont = "[ CONTINUING\u2026 ]";
            layout.setText(smallFont, cont);
            smallFont.draw(batch, cont,
                camLeft() + (VW - layout.width) / 2f,
                camBot() + VH / 2f - 60f);
        }

        batch.end();
    }

    private void drawEndText() {
        boolean won    = (phase == Phase.WIN);
        String  header = won ? "THE NARRATOR CRACKS" : "THE DISTORTION HOLDS";
        String  body   = won ? winText : loseText;
        String  tier   = gameState.getMinigameScoreTier();
        String  scoreLine = "Score: " + score + " / 200   \u2014   " + tier;

        font.setColor(won ? new Color(0.2f, 0.9f, 1f, 1f) : Color.RED);
        layout.setText(font, header);
        font.draw(batch, header,
            camLeft() + (VW - layout.width) / 2f,
            camBot() + VH / 2f + 110f);

        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, body,
            camLeft() + 240f,
            camBot() + VH / 2f + 55f,
            VW - 480f, 1, true);

        // Score tier line
        Color tierColor;
        if      (tier.equals("TRUTH SURFACED")) tierColor = new Color(0.2f, 0.95f, 0.6f, 1f);
        else if (tier.equals("MOSTLY CLEAR"))   tierColor = new Color(0.9f, 0.9f, 0.5f, 1f);
        else if (tier.equals("DISTORTED"))      tierColor = new Color(1f, 0.55f, 0.15f, 1f);
        else                                    tierColor = new Color(1f, 0.2f, 0.2f, 1f);

        smallFont.setColor(tierColor);
        layout.setText(smallFont, scoreLine);
        smallFont.draw(batch, scoreLine,
            camLeft() + (VW - layout.width) / 2f,
            camBot() + VH / 2f + 10f);

        // WIN: show sorted truth timeline as revelation
        if (won && timelineSize > 0) {
            smallFont.setColor(new Color(0.45f, 0.85f, 0.7f, 1f));
            smallFont.draw(batch, "WHAT THE TAPE REVEALS:",
                camLeft() + 240f, camBot() + VH / 2f - 20f);
            for (int i = 0; i < timelineSize; i++) {
                smallFont.setColor(new Color(0.78f, 0.96f, 0.86f, 1f));
                smallFont.draw(batch, (i + 1) + ". " + timeline[i],
                    camLeft() + 260f,
                    camBot() + VH / 2f - 40f - i * 22f,
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
    private void drawInstructions() {
        float cx = camLeft(); // always 0 (camera x = VW/2)
        float cy = camBot();  // world-Y of screen bottom

        // ── ShapeRenderer pass ──────────────────────────────────────────────
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Full dark background
        shape.setColor(0f, 0f, 0f, 0.97f);
        shape.rect(cx, cy, VW, VH);

        // Left panel (OBJECTIVE)
        shape.setColor(0.05f, 0.05f, 0.09f, 0.96f);
        shape.rect(cx + 50f,   cy + 175f, 555f, 380f);
        // Right panel (AVOID)
        shape.rect(cx + 675f,  cy + 175f, 555f, 380f);

        // Panel top-border accents
        shape.setColor(0.55f, 0.45f, 0.22f, 1f);
        shape.rect(cx + 50f,  cy + 553f, 555f, 3f);
        shape.rect(cx + 675f, cy + 553f, 555f, 3f);

        // Separator under hook
        shape.setColor(0.3f, 0.25f, 0.12f, 1f);
        shape.rect(cx + 50f, cy + 610f, 1180f, 1f);

        // Control key icon backgrounds  (WASD cluster + Arrow label)
        float ky = cy + 148f;
        float ksize = 30f;
        // W A S D keys
        float[][] wasd = { {cx+478f, ky+34f}, {cx+446f, ky}, {cx+478f, ky}, {cx+510f, ky} };
        shape.setColor(0.18f, 0.18f, 0.25f, 1f);
        for (float[] k : wasd) shape.rect(k[0], k[1], ksize, ksize);

        shape.end();

        // Panel outlines
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.35f, 0.28f, 0.14f, 1f);
        shape.rect(cx + 50f,  cy + 175f, 555f, 380f);
        shape.rect(cx + 675f, cy + 175f, 555f, 380f);
        shape.setColor(0.5f, 0.5f, 0.6f, 1f);
        for (float[] k : wasd) shape.rect(k[0], k[1], ksize, ksize);
        shape.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── Batch pass (text) ───────────────────────────────────────────────
        batch.begin();

        // Tape title
        font.setColor(new Color(0.88f, 0.74f, 0.36f, 1f));
        String title = tape.getTitle().toUpperCase();
        layout.setText(font, title);
        font.draw(batch, title, cx + (VW - layout.width) / 2f, cy + 692f);

        // Hook
        smallFont.setColor(new Color(0.80f, 0.76f, 0.66f, 1f));
        layout.setText(smallFont, instructionHook);
        smallFont.draw(batch, instructionHook, cx + (VW - layout.width) / 2f, cy + 645f);

        // Left panel header
        smallFont.setColor(new Color(0.4f, 0.85f, 0.55f, 1f));
        smallFont.draw(batch, "OBJECTIVE", cx + 66f, cy + 543f);

        // Left bullets
        smallFont.setColor(new Color(0.92f, 0.89f, 0.80f, 1f));
        for (int i = 0; i < instructionObjective.length; i++) {
            smallFont.draw(batch, "\u2022  " + instructionObjective[i],
                cx + 66f, cy + 510f - i * 34f, 525f, -1, true);
        }

        // Right panel header
        smallFont.setColor(new Color(0.9f, 0.35f, 0.35f, 1f));
        smallFont.draw(batch, "AVOID", cx + 691f, cy + 543f);

        // Right bullets
        smallFont.setColor(new Color(0.92f, 0.89f, 0.80f, 1f));
        for (int i = 0; i < instructionAvoid.length; i++) {
            smallFont.draw(batch, "\u2022  " + instructionAvoid[i],
                cx + 691f, cy + 510f - i * 34f, 525f, -1, true);
        }

        // Controls label
        smallFont.setColor(new Color(0.55f, 0.55f, 0.65f, 1f));
        smallFont.draw(batch, "CONTROLS:", cx + 370f, cy + 170f);

        // WASD key labels
        smallFont.setColor(Color.WHITE);
        String[] wLabels = { "W", "A", "S", "D" };
        for (int i = 0; i < wLabels.length; i++) {
            layout.setText(smallFont, wLabels[i]);
            smallFont.draw(batch, wLabels[i],
                wasd[i][0] + (ksize - layout.width) / 2f,
                wasd[i][1] + (ksize + layout.height) / 2f);
        }
        smallFont.setColor(new Color(0.55f, 0.55f, 0.65f, 1f));
        smallFont.draw(batch, "/ Arrow Keys  to move", cx + 548f, cy + 163f);

        // Forks reminder
        int forkCount = forks.length;
        int actCount  = (actBreakAfterFork >= 0) ? 2 : 1;
        smallFont.setColor(new Color(0.65f, 0.55f, 0.3f, 1f));
        String mapInfo = forkCount + " forks across " + actCount + " acts  \u2014  3 lives total";
        layout.setText(smallFont, mapInfo);
        smallFont.draw(batch, mapInfo, cx + (VW - layout.width) / 2f, cy + 122f);

        // Pulsing PRESS ENTER prompt
        float alpha = 0.45f + 0.55f * MathUtils.sin(pulseTimer * 2.8f);
        smallFont.setColor(new Color(0.65f, 0.80f, 1f, alpha));
        String prompt = "PRESS  ENTER  OR  SPACE  TO  BEGIN";
        layout.setText(smallFont, prompt);
        smallFont.draw(batch, prompt, cx + (VW - layout.width) / 2f, cy + 68f);

        batch.end();
    }

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
        if (deadEndTex != null) deadEndTex.dispose();
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
}
