package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dsa.game.DSAGame;
import com.dsa.game.state.GameState;
import com.dsa.game.state.Tape;

public class CatcherMinigame implements Screen {

    // ── Viewport ────────────────────────────────────────────────────────────
    private static final float W = 1280f;
    private static final float H = 720f;

    // ── Tuning ──────────────────────────────────────────────────────────────
    private static final int   CATCHES_PER_WAVE         = 5;
    private static final int   MAX_WRONG                = 3;  // lives across all waves
    private static final float LINE_SPEED               = 420f;
    private static final float LINE_W                   = 200f;
    private static final float LINE_Y                   = 80f;
    private static final float LINE_THICKNESS           = 5f;
    private static final float SPAWN_INTERVAL_START     = 1.8f;
    private static final float SPAWN_INTERVAL_MIN       = 0.9f;
    private static final float WORD_SPEED_START         = 110f;
    private static final float WORD_SPEED_MAX           = 230f;
    private static final float DISTORTION_DUR           = 0.5f;
    private static final float END_DISPLAY_DUR          = 5f;
    private static final float WAVE_BREAK_DUR           = 4.5f;
    // Key evidence spawns after this many words are spawned in the final wave
    private static final int   KEY_EVIDENCE_SPAWN_AFTER = 4;

    // ── State ────────────────────────────────────────────────────────────────
    private enum Phase { INSTRUCTIONS, PLAYING, WAVE_BREAK, WIN, LOSE }
    private Phase phase = Phase.INSTRUCTIONS;

    // ── WaveConfig ───────────────────────────────────────────────────────────
    /**
     * One wave of the catcher minigame.
     * trueWords and distortedWords are insertion-sorted by length in setupWaves().
     */
    private static class WaveConfig {
        final String   waveTitle;
        final String   transcriptFragment;
        final String[] trueWords;
        final String[] distortedWords;
        final String   keyEvidence; // null = no mandatory word this wave

        WaveConfig(String waveTitle, String transcriptFragment,
                   String[] trueWords, String[] distortedWords, String keyEvidence) {
            this.waveTitle           = waveTitle;
            this.transcriptFragment  = transcriptFragment;
            this.trueWords           = trueWords;
            this.distortedWords      = distortedWords;
            this.keyEvidence         = keyEvidence;
        }
    }

    // ── Dependencies ─────────────────────────────────────────────────────────
    private final DSAGame  game;
    private final GameState state;
    private final Tape      tape;
    private final Runnable  onComplete;

    // ── LibGDX ───────────────────────────────────────────────────────────────
    private SpriteBatch   batch;
    private ShapeRenderer shape;
    private FitViewport   viewport;
    private BitmapFont    font;
    private BitmapFont    smallFont;
    private GlyphLayout   layout;

    // ── Textures ─────────────────────────────────────────────────────────────
    private Texture bgTex;
    private Texture containerTex;
    private Texture distortionTex;

    // ── Catcher line ─────────────────────────────────────────────────────────
    private float lineX;

    // ── Words ────────────────────────────────────────────────────────────────
    private final Array<FallingWord> words = new Array<>();
    private float spawnTimer    = 0f;
    private float spawnInterval = SPAWN_INTERVAL_START;
    private float wordSpeed     = WORD_SPEED_START;
    private float difficulty    = 0f;
    private int   waveSpawnCount = 0; // words spawned in the current wave

    // ── Wave system ──────────────────────────────────────────────────────────
    private WaveConfig[] waveConfigs;
    private int   currentWave       = 0;
    private float waveBreakTimer    = 0f;
    private String waveBreakTitle    = "";
    private String waveBreakFragment = "";

    // ── Key evidence ─────────────────────────────────────────────────────────
    private boolean keyEvidenceSpawned = false;
    private boolean waveReplayUsed     = false;

    // ── Progress ─────────────────────────────────────────────────────────────
    private int correctCatches = 0; // resets each wave
    private int wrongCatches   = 0; // cumulative across all waves

    // ── Scoring ───────────────────────────────────────────────────────────────
    private static final int SCORE_TRUE_CATCH  =  20;
    private static final int SCORE_MISS_TRUE   =  -5;
    private static final int SCORE_WRONG_CATCH = -15;
    private static final int SCORE_WIN_BONUS   =  50;
    private int score      = 0;
    private int comboCount = 0;

    // ── Effects ───────────────────────────────────────────────────────────────
    private float distortionTimer = 0f;
    private float endTimer        = 0f;

    // ── Narrator result lines ─────────────────────────────────────────────────
    private String winText;
    private String loseText;

    // ── Instruction screen ────────────────────────────────────────────────────
    private String   instructionHook;
    private String[] instructionObjective;
    private String[] instructionAvoid;
    private String   instructionNote; // nullable — amber note at bottom (key evidence reminder)
    private float    pulseTimer = 0f;

    // =========================================================================

    public CatcherMinigame(DSAGame game, GameState state, Tape tape, Runnable onComplete) {
        this.game       = game;
        this.state      = state;
        this.tape       = tape;
        this.onComplete = onComplete;
        setupWaves();
        setupNarratorTexts();
        setupInstructions();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Insertion sort — sorts the word pool by text length ascending.
     * Shorter words (easier to read quickly) come first; longer words spawn
     * later as difficulty increases, giving a natural escalation curve.
     */
    private void insertionSort(String[] arr) {
        for (int i = 1; i < arr.length; i++) {
            String key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j].length() > key.length()) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    private void setupWaves() {
        switch (tape) {

            // ── TAPE 1: The Argument ──────────────────────────────────────────
            case TAPE_ARGUMENT:
                waveConfigs = new WaveConfig[]{
                    new WaveConfig(
                        "THE ACCOUNTS",
                        "He had seen the entries. Two years of it.\nHe was not guessing \u2014 he knew.",
                        new String[]{ "FIFTY THOUSAND", "UNKNOWN ACCOUNT",
                                      "TWO YEARS", "SIPHONING", "EMBEZZLEMENT" },
                        new String[]{ "BAD INVESTMENT", "COMPANY STRESS",
                                      "MISUNDERSTANDING", "FATHER'S TEMPER", "JUST BUSINESS" },
                        null
                    ),
                    new WaveConfig(
                        "THE WILL",
                        "James had one night.\nHe knew what morning meant.",
                        new String[]{ "CHANGING THE WILL", "MARGARET GETS ALL",
                                      "NOT A PENNY", "TOMORROW MORNING", "NINE O'CLOCK" },
                        new String[]{ "A THREAT ONLY", "HE'D NEVER DO IT",
                                      "HE ALWAYS COOLED DOWN", "SAID IN ANGER", "EMPTY WORDS" },
                        null
                    ),
                    new WaveConfig(
                        "THE ARRANGEMENT",
                        "Harold knew about Daniel.\nHe was going to expose them both at nine AM.",
                        new String[]{ "DANIEL HID FUNDS", "LOGBOOK ENTRIES",
                                      "BOTH DISMISSED", "MOVING MONEY", "DANIEL KNEW" },
                        new String[]{ "NO CONNECTION", "JUST LOYAL STAFF",
                                      "NO PROOF", "GROUNDSKEEPER ONLY", "UNRELATED" },
                        "I KNOW YOUR ARRANGEMENT"
                    ),
                };
                break;

            // ── TAPE 7: Margaret's Personal Account ──────────────────────────
            case TAPE_MARGARET_ACCOUNT:
                waveConfigs = new WaveConfig[]{
                    new WaveConfig(
                        "AFTER DINNER",
                        "She saw his face at dinner.\nShe already knew something was wrong.",
                        new String[]{ "STARED AT HIS PLATE", "WOULDN'T LOOK AT ME",
                                      "FATHER SAID THE WILL", "DOOR SLAMMED", "NINE O'CLOCK" },
                        new String[]{ "GRIEF CLOUDS MEMORY", "SHE WAS UPSET",
                                      "NORMAL ARGUMENT", "SHE IMAGINED IT", "JUST A BAD NIGHT" },
                        null
                    ),
                    new WaveConfig(
                        "THE FOOTSTEPS",
                        "Margaret pressed her ear to the door.\nShe knows what she heard.",
                        new String[]{ "TWO VOICES", "SOUNDED LIKE JAMES",
                                      "TOWARD THE STUDY", "MIDNIGHT", "URGENT WHISPERS" },
                        new String[]{ "OLD HOUSE CREAKS", "GRIEF DISTORTS MEMORY",
                                      "SHE IMAGINED IT", "SHE WAS FRIGHTENED", "JUST A DREAM" },
                        null
                    ),
                    new WaveConfig(
                        "THE MORNING AFTER",
                        "She saw them both that morning.\nShe knows what guilt looks like.",
                        new String[]{ "HOLLOW-EYED", "HADN'T SLEPT",
                                      "COULDN'T MEET MY EYES", "DANIEL SILENT", "EXHAUSTED" },
                        new String[]{ "HE WAS IN SHOCK", "GRIEF LOOKS DIFFERENT",
                                      "DANIEL IS QUIET", "SHE'S PROJECTING", "NATURAL REACTION" },
                        "HIS HANDS WERE SHAKING"
                    ),
                };
                break;

            // ── TAPE 8: The Opening (Arthur's Death) ─────────────────────────
            case TAPE_ARTHUR_DEATH:
                waveConfigs = new WaveConfig[]{
                    new WaveConfig(
                        "THE DISCOVERY",
                        "Arthur found it.\nThirty years, and no one had looked.",
                        new String[]{ "WALL BUILT 1957", "ASHFORD VANISHED",
                                      "HOLLOW CAVITY", "NEWER MORTAR", "SEALED DELIBERATELY" },
                        new String[]{ "RENOVATION WORK", "RECORDS INCOMPLETE",
                                      "COULD BE ANYTHING", "STANDARD BUILD", "COINCIDENCE" },
                        null
                    ),
                    new WaveConfig(
                        "THE VOICE",
                        "It sounded human.\nIt knew exactly how to sound human.",
                        new String[]{ "THIRTY YEARS DARK", "HAROLD DID THIS",
                                      "DON'T KNOW HOW LONG", "NO POLICE", "VOICE CHANGED" },
                        new String[]{ "TRAPPED MAN", "COMPASSIONATE ACT",
                                      "HE NEEDS HELP", "JUST LET HIM OUT", "INNOCENT VICTIM" },
                        null
                    ),
                    new WaveConfig(
                        "THE TRUTH",
                        "The recorder kept running.\nNobody came to turn it off.",
                        new String[]{ "SOMETHING EMERGING", "WAITED THIRTY YEARS",
                                      "WAITED FOR SOMEONE KIND", "STONE SCRAPING", "SCREAM CUTS OFF" },
                        new String[]{ "ARTHUR OVERREACTED", "JUST A SICK MAN",
                                      "SHOCK AT FIRST SIGHT", "HE'S FINE NOW", "MISUNDERSTOOD" },
                        "IT WASN'T HUMAN ANYMORE"
                    ),
                };
                break;

            // ── Default (fallback for tapes routed here unexpectedly) ─────────
            default:
                waveConfigs = new WaveConfig[]{
                    new WaveConfig("WAVE 1", "The record begins.",
                        new String[]{ "TRUTH", "EVIDENCE", "FACT" },
                        new String[]{ "DISTORTION", "LIE", "NOISE" }, null),
                    new WaveConfig("WAVE 2", "Look closer.",
                        new String[]{ "TRUTH", "EVIDENCE", "CONFIRMED" },
                        new String[]{ "DISTORTION", "FABRICATED", "NOISE" }, null),
                    new WaveConfig("WAVE 3", "The record is set.",
                        new String[]{ "TRUTH", "UNDENIABLE", "PROOF" },
                        new String[]{ "DISTORTION", "BURIED", "ERASED" }, "THE TRUTH REMAINS"),
                };
                break;
        }

        // Apply insertion sort to each wave's word pools
        for (WaveConfig wc : waveConfigs) {
            insertionSort(wc.trueWords);
            insertionSort(wc.distortedWords);
        }
    }

    private void finalizeScore() {
        state.setLastMinigameScore(score);
        String tier = state.getMinigameScoreTier();
        if (tier.equals("DISTORTED")) state.addAwareness(1);
        if (tier.equals("CORRUPTED")) state.addAwareness(2);
    }

    private void setupNarratorTexts() {
        switch (tape) {
            case TAPE_ARGUMENT:
                winText  = "Harold meant it. The will was changing at nine the next morning.\nJames had one night.";
                loseText = "A disagreement between father and son.\nThese things happen in families. Nothing more.";
                break;
            case TAPE_MARGARET_ACCOUNT:
                winText  = "She recorded this because she needed someone to believe her.\nThe tape is the evidence she could not give herself.";
                loseText = "A grieving daughter. Confused by loss.\nThat is all I choose to see.";
                break;
            case TAPE_ARTHUR_DEATH:
                winText  = "Arthur Hollis came to investigate a murder.\nHe became a reason for another one.";
                loseText = "A man in distress. A detective who helped him.\nThere is nothing suspicious here.";
                break;
            default:
                winText  = "The truth surfaces.";
                loseText = "The distortion holds.";
        }
    }

    private void setupInstructions() {
        switch (tape) {
            case TAPE_ARGUMENT:
                instructionHook      = "The argument was recorded. The recorder does not lie \u2014 but memory does.";
                instructionObjective = new String[]{
                    "Catch TRUE words as they fall from above",
                    "Move your catcher with  \u2190 \u2192  or  A / D",
                    "Complete all 3 waves to surface the truth",
                    "Consecutive catches build a combo (\u00d71.5 at 3,  \u00d72.0 at 5)"
                };
                instructionAvoid = new String[]{
                    "Distorted words raise awareness \u2014 do not catch them",
                    "Missing true words breaks your combo and costs points",
                    "You have 3 lives shared across all three waves",
                    "Finish below 70 points and the distortion wins"
                };
                instructionNote = "\u2605  A GOLD-BORDERED KEY EVIDENCE word appears in Wave 3.  Do not let it fall.";
                break;
            case TAPE_MARGARET_ACCOUNT:
                instructionHook      = "Margaret recorded this alone, before dawn. Catch what she actually witnessed.";
                instructionObjective = new String[]{
                    "Catch TRUE words matching what Margaret saw that night",
                    "Move with  \u2190 \u2192  or  A / D",
                    "Three waves: the dinner, the footsteps, the morning after",
                    "Combos multiply your score"
                };
                instructionAvoid = new String[]{
                    "Distortions represent grief rewriting memory \u2014 avoid them",
                    "Missing true words breaks your combo",
                    "3 lives shared across all waves",
                    "Absorbing distortions raises awareness"
                };
                instructionNote = "\u2605  A GOLD-BORDERED KEY EVIDENCE word appears in Wave 3.  Missing it replays the wave once.";
                break;
            case TAPE_ARTHUR_DEATH:
                instructionHook      = "This is what Arthur Hollis recorded before the wall opened. Not all of it can be trusted.";
                instructionObjective = new String[]{
                    "Catch the FACTS \u2014 what Arthur actually observed",
                    "Move with  \u2190 \u2192  or  A / D",
                    "Three waves: the discovery, the voice, the truth",
                    "Words fall faster each wave \u2014 stay focused"
                };
                instructionAvoid = new String[]{
                    "The thing behind the wall knew what to say \u2014 avoid its words",
                    "Distortions are what it wanted Arthur to believe",
                    "3 lives across all waves.  Do not let it win.",
                    "Catching distortions raises awareness"
                };
                instructionNote = "\u2605  A GOLD-BORDERED KEY EVIDENCE word in Wave 3 \u2014 the last true thing Arthur observed.";
                break;
            default:
                instructionHook      = "Catch the truth before it falls away.";
                instructionObjective = new String[]{
                    "Catch TRUE words with your catcher",
                    "Move left and right with  \u2190 \u2192  or  A / D",
                    "Complete all waves to win"
                };
                instructionAvoid = new String[]{
                    "Distorted words cost points and raise awareness",
                    "You have 3 lives shared across waves",
                    "Let the truth fall and you lose score"
                };
                instructionNote = null;
        }
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    public void show() {
        batch     = new SpriteBatch();
        shape     = new ShapeRenderer();
        viewport  = new FitViewport(W, H);
        font      = new BitmapFont();
        smallFont = new BitmapFont();
        font.getData().setScale(1.6f);
        smallFont.getData().setScale(1.1f);
        layout    = new GlyphLayout();

        lineX = W / 2f - LINE_W / 2f;

        bgTex         = loadTex("minigames/darkbackground.jpg");
        containerTex  = loadTex("minigames/container.png");
        distortionTex = loadTex("minigames/screendistortion.png");
        // spawnWord() is called when the player dismisses the instruction screen
    }

    private Texture loadTex(String path) {
        if (Gdx.files.internal(path).exists()) return new Texture(Gdx.files.internal(path));
        return null;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shape.setProjectionMatrix(viewport.getCamera().combined);

        switch (phase) {
            case INSTRUCTIONS:
                pulseTimer += delta;
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                 || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    phase = Phase.PLAYING;
                    spawnWord();
                }
                break;
            case PLAYING:
                update(delta);
                break;
            case WAVE_BREAK:
                waveBreakTimer += delta;
                if (waveBreakTimer >= WAVE_BREAK_DUR) {
                    phase = Phase.PLAYING;
                    spawnWord();
                }
                break;
            case WIN:
            case LOSE:
                endTimer += delta;
                if (endTimer >= END_DISPLAY_DUR) {
                    onComplete.run();
                    return;
                }
                break;
        }

        draw();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    private void update(float delta) {
        handleInput(delta);
        updateDifficulty(delta);
        updateWords(delta);
        if (distortionTimer > 0f) distortionTimer -= delta;
    }

    private void handleInput(float delta) {
        boolean left  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        if (left)  lineX -= LINE_SPEED * delta;
        if (right) lineX += LINE_SPEED * delta;
        lineX = MathUtils.clamp(lineX, 0f, W - LINE_W);
    }

    private void updateDifficulty(float delta) {
        difficulty    += delta * 0.025f;
        spawnInterval  = Math.max(SPAWN_INTERVAL_MIN, SPAWN_INTERVAL_START - difficulty * 0.5f);
        wordSpeed      = Math.min(WORD_SPEED_MAX, WORD_SPEED_START + difficulty * 60f);
    }

    private void updateWords(float delta) {
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            spawnWord();
        }

        Rectangle playerBounds = new Rectangle(lineX, LINE_Y - LINE_THICKNESS, LINE_W, LINE_THICKNESS * 2f);

        for (int i = words.size - 1; i >= 0; i--) {
            FallingWord word = words.get(i);
            word.update(delta);

            if (word.isOffScreen()) {
                if (word.isTrue) {
                    comboCount = 0;
                    score = Math.max(0, score + SCORE_MISS_TRUE);
                }
                // Missed a mandatory key evidence word — replay the final wave once
                if (word.isMandatory && !waveReplayUsed) {
                    waveReplayUsed = true;
                    state.addAwareness(2);
                    resetCurrentWave();
                    return;
                }
                words.removeIndex(i);
                continue;
            }

            if (!word.caught && word.getBounds().overlaps(playerBounds)) {
                word.caught = true;
                words.removeIndex(i);

                if (word.isTrue) {
                    comboCount++;
                    float mult = comboCount >= 5 ? 2.0f : comboCount >= 3 ? 1.5f : 1.0f;
                    score = Math.min(200, score + (int)(SCORE_TRUE_CATCH * mult));
                    correctCatches++;
                    checkWaveCompletion();
                } else {
                    comboCount = 0;
                    score = Math.max(0, score + SCORE_WRONG_CATCH);
                    wrongCatches++;
                    distortionTimer = DISTORTION_DUR;
                    state.addAwareness(1);
                    if (wrongCatches >= MAX_WRONG) {
                        finalizeScore();
                        phase = Phase.LOSE;
                    }
                }
            }
        }
    }

    private void checkWaveCompletion() {
        if (correctCatches < CATCHES_PER_WAVE) return;

        if (currentWave < waveConfigs.length - 1) {
            // Store break screen content BEFORE incrementing
            waveBreakTitle    = waveConfigs[currentWave].waveTitle;
            waveBreakFragment = waveConfigs[currentWave].transcriptFragment;
            currentWave++;
            // Reset wave state
            correctCatches     = 0;
            comboCount         = 0;
            keyEvidenceSpawned = false;
            waveReplayUsed     = false;
            waveSpawnCount     = 0;
            waveBreakTimer     = 0f;
            words.clear();
            spawnTimer         = 0f;
            difficulty         = currentWave * 0.4f; // slight boost each wave
            phase              = Phase.WAVE_BREAK;
        } else {
            // All waves done
            score = Math.min(200, score + SCORE_WIN_BONUS);
            finalizeScore();
            phase = Phase.WIN;
        }
    }

    private void resetCurrentWave() {
        correctCatches     = 0;
        comboCount         = 0;
        keyEvidenceSpawned = false;
        waveSpawnCount     = 0;
        words.clear();
        spawnTimer = 0f;
        spawnWord();
    }

    private void spawnWord() {
        WaveConfig wc = waveConfigs[currentWave];
        boolean   isTrue;
        String    text;
        boolean   isMandatory = false;

        // In the final wave, inject the key evidence word after KEY_EVIDENCE_SPAWN_AFTER spawns
        boolean isFinalWave = (currentWave == waveConfigs.length - 1);
        if (isFinalWave && wc.keyEvidence != null
                && !keyEvidenceSpawned && waveSpawnCount >= KEY_EVIDENCE_SPAWN_AFTER) {
            text          = wc.keyEvidence;
            isTrue        = true;
            isMandatory   = true;
            keyEvidenceSpawned = true;
        } else {
            isTrue = MathUtils.randomBoolean(0.52f);
            String[] pool = isTrue ? wc.trueWords : wc.distortedWords;
            int maxIdx = Math.min((int)(difficulty * pool.length) + 1, pool.length - 1);
            text = pool[MathUtils.random(maxIdx)];
        }

        waveSpawnCount++;
        float centerX = MathUtils.random(FallingWord.WIDTH / 2f + 40f, W - FallingWord.WIDTH / 2f - 40f);
        float speed   = wordSpeed + MathUtils.random(-15f, 15f);
        words.add(new FallingWord(text, isTrue, centerX, H + FallingWord.HEIGHT, speed, isMandatory));
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    private void draw() {
        if (phase == Phase.INSTRUCTIONS) {
            drawInstructions();
            return;
        }

        if (phase == Phase.PLAYING) {
            drawProgressBar();
            drawLivesIndicator();
            drawWaveIndicator();
            drawCatcherLine();
            drawWordBorders(); // ShapeRenderer: gold borders for mandatory words
        }

        batch.begin();

        if (phase == Phase.PLAYING) {
            drawWords();
            drawScoreHUD();
        }

        // Distortion flash
        if (distortionTimer > 0f && distortionTex != null) {
            float alpha = (distortionTimer / DISTORTION_DUR) * 0.8f;
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(distortionTex, 0, 0, W, H);
            batch.setColor(Color.WHITE);
        }

        batch.end();

        // Wave break overlay (full screen)
        if (phase == Phase.WAVE_BREAK) drawWaveBreak();

        // End screen overlay
        if (phase == Phase.WIN || phase == Phase.LOSE) drawEndScreen();
    }

    private void drawWords() {
        for (FallingWord word : words) {
            if (containerTex != null)
                batch.draw(containerTex, word.x, word.y, FallingWord.WIDTH, FallingWord.HEIGHT);
            layout.setText(smallFont, word.text);
            float tx = word.x + (FallingWord.WIDTH  - layout.width)  / 2f;
            float ty = word.y + (FallingWord.HEIGHT + layout.height) / 2f;
            // Key evidence word: gold text
            smallFont.setColor(word.isMandatory ? new Color(1f, 0.9f, 0.2f, 1f) : Color.WHITE);
            smallFont.draw(batch, word.text, tx, ty);
        }
        smallFont.setColor(Color.WHITE);
    }

    /** Draws a gold rect border around mandatory (key evidence) words using ShapeRenderer. */
    private void drawWordBorders() {
        boolean hasMandatory = false;
        for (FallingWord w : words) { if (w.isMandatory) { hasMandatory = true; break; } }
        if (!hasMandatory) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(1f, 0.85f, 0.1f, 1f);
        for (FallingWord w : words) {
            if (w.isMandatory)
                shape.rect(w.x - 3f, w.y - 3f, FallingWord.WIDTH + 6f, FallingWord.HEIGHT + 6f);
        }
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawCatcherLine() {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 1f, 0f, 1f);
        shape.rect(lineX, LINE_Y - LINE_THICKNESS / 2f, LINE_W, LINE_THICKNESS);
        shape.end();
    }

    private void drawProgressBar() {
        float bw = 440f, bh = 28f, bx = (W - bw) / 2f, by = H - 56f;
        float fill = (float) correctCatches / CATCHES_PER_WAVE;

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.5f, 0.8f, 1f, 1f);
        shape.rect(bx - 2f, by - 2f, bw + 4f, bh + 4f);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.05f, 0.05f, 0.1f, 1f);
        shape.rect(bx, by, bw, bh);
        shape.setColor(0.15f, 0.75f, 0.9f, 1f);
        shape.rect(bx, by, bw * fill, bh);
        shape.end();
    }

    private void drawLivesIndicator() {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        float cx = 40f, cy = H - 50f, r = 10f, gap = 30f;
        for (int i = 0; i < MAX_WRONG; i++) {
            shape.setColor(i < wrongCatches ? Color.RED : new Color(0.3f, 0.3f, 0.3f, 1f));
            shape.circle(cx + i * gap, cy, r);
        }
        shape.end();
    }

    /** Small wave counter top-right. */
    private void drawWaveIndicator() {
        // Uses ShapeRenderer — called before batch
        // Draw background panel for wave indicator
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.08f, 0.08f, 0.12f, 1f);
        shape.rect(W - 160f, H - 62f, 148f, 36f);
        shape.end();
    }

    private void drawScoreHUD() {
        // Score top-right
        smallFont.setColor(new Color(0.9f, 0.85f, 0.6f, 1f));
        String waveLabel = "WAVE " + (currentWave + 1) + " / " + waveConfigs.length
                         + "   Score: " + score;
        layout.setText(smallFont, waveLabel);
        smallFont.draw(batch, waveLabel, W - layout.width - 12f, H - 32f);
        smallFont.setColor(Color.WHITE);

        // Key evidence hint
        WaveConfig wc = waveConfigs[currentWave];
        if (wc.keyEvidence != null && !keyEvidenceSpawned) {
            smallFont.setColor(new Color(1f, 0.85f, 0.1f, 0.7f));
            smallFont.draw(batch, "KEY EVIDENCE INCOMING", 12f, H - 32f);
            smallFont.setColor(Color.WHITE);
        }
    }

    private void drawWaveBreak() {
        // Overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.92f);
        shape.rect(0, 0, W, H);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();

        // Completed wave title
        font.setColor(new Color(0.2f, 0.9f, 1f, 1f));
        layout.setText(font, waveBreakTitle);
        font.draw(batch, waveBreakTitle, (W - layout.width) / 2f, H / 2f + 90f);

        // Divider line hint
        smallFont.setColor(new Color(0.55f, 0.45f, 0.25f, 1f));
        String sep = "\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014";
        layout.setText(smallFont, sep);
        smallFont.draw(batch, sep, (W - layout.width) / 2f, H / 2f + 48f);

        // Transcript fragment
        smallFont.setColor(new Color(0.92f, 0.88f, 0.78f, 1f));
        smallFont.draw(batch, waveBreakFragment, 240f, H / 2f + 28f, W - 480f, 1, true);

        // Upcoming wave label
        if (currentWave < waveConfigs.length) {
            float progress = waveBreakTimer / WAVE_BREAK_DUR;
            if (progress > 0.55f) {
                float alpha = (progress - 0.55f) / 0.45f;
                smallFont.setColor(new Color(0.5f, 0.7f, 0.9f, alpha));
                String next = "NEXT: " + waveConfigs[currentWave].waveTitle;
                layout.setText(smallFont, next);
                smallFont.draw(batch, next, (W - layout.width) / 2f, H / 2f - 60f);
            }
        }

        batch.end();
    }

    private void drawEndScreen() {
        boolean won      = (phase == Phase.WIN);
        String  header   = won ? "THE NARRATOR CRACKS" : "THE DISTORTION HOLDS";
        String  body     = won ? winText : loseText;
        String  tier     = state.getMinigameScoreTier();
        String  scoreLine = "Score: " + score + " / 200   \u2014   " + tier;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.88f);
        shape.rect(0, 0, W, H);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();

        font.setColor(won ? new Color(0.2f, 0.9f, 1f, 1f) : Color.RED);
        layout.setText(font, header);
        font.draw(batch, header, (W - layout.width) / 2f, H / 2f + 100f);

        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, body, 240f, H / 2f + 40f, W - 480f, 1, true);

        // Score tier line
        Color tierColor;
        if      (tier.equals("TRUTH SURFACED")) tierColor = new Color(0.2f, 0.95f, 0.6f, 1f);
        else if (tier.equals("MOSTLY CLEAR"))   tierColor = new Color(0.9f, 0.9f, 0.5f, 1f);
        else if (tier.equals("DISTORTED"))      tierColor = new Color(1f, 0.55f, 0.15f, 1f);
        else                                    tierColor = new Color(1f, 0.2f, 0.2f, 1f);

        smallFont.setColor(tierColor);
        layout.setText(smallFont, scoreLine);
        smallFont.draw(batch, scoreLine, (W - layout.width) / 2f, H / 2f - 30f);

        batch.end();
    }

    private void drawInstructions() {
        // ── ShapeRenderer pass (panels, borders, key icons) ───────────────────
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Left panel background
        shape.setColor(0.05f, 0.05f, 0.09f, 0.96f);
        shape.rect(50f, 175f, 555f, 380f);
        // Right panel background
        shape.rect(675f, 175f, 555f, 380f);

        // Panel top-border accents (amber)
        shape.setColor(0.55f, 0.45f, 0.22f, 1f);
        shape.rect(50f,   553f, 555f, 3f);
        shape.rect(675f,  553f, 555f, 3f);

        // Thin separator under hook
        shape.setColor(0.3f, 0.25f, 0.12f, 1f);
        shape.rect(50f, 610f, 1180f, 1f);

        // Controls key icon backgrounds — drawn as small rects
        // Keys: ← → A D (Catcher controls)
        float ky = 148f; float ksize = 30f;
        float[] kx = { 490f, 528f, 600f, 638f }; // ← → A D
        shape.setColor(0.18f, 0.18f, 0.25f, 1f);
        for (float x : kx) shape.rect(x, ky, ksize, ksize);

        shape.end();

        // Panel border lines
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.35f, 0.28f, 0.14f, 1f);
        shape.rect(50f,  175f, 555f, 380f);
        shape.rect(675f, 175f, 555f, 380f);
        // Key icon borders
        shape.setColor(0.5f, 0.5f, 0.6f, 1f);
        for (float x : kx) shape.rect(x, ky, ksize, ksize);
        shape.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── Batch pass (all text) ─────────────────────────────────────────────
        batch.begin();

        // Tape title
        font.setColor(new Color(0.88f, 0.74f, 0.36f, 1f));
        String title = tape.getTitle().toUpperCase();
        layout.setText(font, title);
        font.draw(batch, title, (W - layout.width) / 2f, 692f);

        // Hook
        smallFont.setColor(new Color(0.80f, 0.76f, 0.66f, 1f));
        layout.setText(smallFont, instructionHook);
        smallFont.draw(batch, instructionHook, (W - layout.width) / 2f, 645f);

        // Left panel header
        smallFont.setColor(new Color(0.4f, 0.85f, 0.55f, 1f));
        smallFont.draw(batch, "OBJECTIVE", 66f, 543f);

        // Left bullets
        smallFont.setColor(new Color(0.92f, 0.89f, 0.80f, 1f));
        for (int i = 0; i < instructionObjective.length; i++) {
            smallFont.draw(batch, "\u2022  " + instructionObjective[i],
                66f, 510f - i * 32f, 525f, -1, true);
        }

        // Right panel header
        smallFont.setColor(new Color(0.9f, 0.35f, 0.35f, 1f));
        smallFont.draw(batch, "AVOID", 691f, 543f);

        // Right bullets
        smallFont.setColor(new Color(0.92f, 0.89f, 0.80f, 1f));
        for (int i = 0; i < instructionAvoid.length; i++) {
            smallFont.draw(batch, "\u2022  " + instructionAvoid[i],
                691f, 510f - i * 32f, 525f, -1, true);
        }

        // Controls label
        smallFont.setColor(new Color(0.55f, 0.55f, 0.65f, 1f));
        smallFont.draw(batch, "CONTROLS:", 390f, 170f);

        // Key labels inside key icons
        smallFont.setColor(Color.WHITE);
        String[] keyLabels = { "\u2190", "\u2192", "A", "D" };
        float[] kxArr = { 490f, 528f, 600f, 638f };
        for (int i = 0; i < keyLabels.length; i++) {
            layout.setText(smallFont, keyLabels[i]);
            smallFont.draw(batch, keyLabels[i],
                kxArr[i] + (ksize - layout.width) / 2f,
                ky + (ksize + layout.height) / 2f);
        }
        smallFont.setColor(new Color(0.55f, 0.55f, 0.65f, 1f));
        smallFont.draw(batch, "to move catcher", 680f, 163f);

        // Key evidence note
        if (instructionNote != null) {
            smallFont.setColor(new Color(1f, 0.85f, 0.15f, 1f));
            layout.setText(smallFont, instructionNote);
            smallFont.draw(batch, instructionNote, (W - layout.width) / 2f, 122f);
        }

        // Pulsing PRESS ENTER prompt
        float alpha = 0.45f + 0.55f * MathUtils.sin(pulseTimer * 2.8f);
        smallFont.setColor(new Color(0.65f, 0.80f, 1f, alpha));
        String prompt = "PRESS  ENTER  OR  SPACE  TO  BEGIN";
        layout.setText(smallFont, prompt);
        smallFont.draw(batch, prompt, (W - layout.width) / 2f, 68f);

        batch.end();
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) { viewport.update(width, height, true); }

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();
        smallFont.dispose();
        if (bgTex        != null) bgTex.dispose();
        if (containerTex != null) containerTex.dispose();
        if (distortionTex!= null) distortionTex.dispose();
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
}
