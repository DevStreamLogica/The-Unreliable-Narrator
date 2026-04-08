package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dsa.game.DSAGame;
import com.dsa.game.data.TapeContent;
import com.dsa.game.state.Tape;
import com.dsa.game.ui.TextPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TapeScreen implements Screen {

    private final DSAGame game;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final TextPanel textPanel;
    private final FitViewport viewport;

    // Portrait system
    private final Map<String, Texture> portraitTextures = new HashMap<>();
    private Texture leftPortrait = null, rightPortrait = null;
    private String  leftSpeaker  = null, rightSpeaker  = null;
    private float   leftAlpha    = 0f,   rightAlpha    = 0f;
    private boolean leftIsActive = true;

    // Audio
    private Sound currentSound = null;
    private Music currentMusic = null;
    private boolean textFrozen = false;
    private boolean justSkipped = false;
    private int currentTapeIndex = -1;
    private final Map<String, Integer> speakerLineCounts = new HashMap<>();
    private String lastPageText = null;
    private List<String> pageSpeakers = Collections.emptyList();
    private String pendingDelayedPath = null;
    private long pendingDelayedAtMs = 0L;
    private Runnable pendingDelayedOnPlay = null;
    private boolean layoutDebugEnabled = false;
    private static final float DEBUG_NUDGE_STEP = 4f;

    // Maps tape index + speaker to audio folder/key
    private static final Map<String, Map<String, String>> TAPE_SPEAKER_KEYS = new HashMap<>();
    static {
        Map<String, String> tape1 = new HashMap<>();
        tape1.put("Harold Vance", "harold");
        tape1.put("James Vance",  "james");
        TAPE_SPEAKER_KEYS.put("0", tape1);

        Map<String, String> tape2 = new HashMap<>();
        tape2.put("Detective Morrison", "morrison");
        tape2.put("James Vance",        "james");
        TAPE_SPEAKER_KEYS.put("1", tape2);

        Map<String, String> tape3 = new HashMap<>();
        tape3.put("Detective Morrison",        "morrison");
        tape3.put("Daniel the Groundskeeper", "daniel");
        TAPE_SPEAKER_KEYS.put("2", tape3);

        Map<String, String> tape4 = new HashMap<>();
        tape4.put("Detective Morrison", "morrison");
        tape4.put("Margaret Vance",     "margaret");
        TAPE_SPEAKER_KEYS.put("3", tape4);

        Map<String, String> tape5 = new HashMap<>();
        tape5.put("Detective Morrison", "morrison");
        tape5.put("Marcus Blackwood",   "marcus");
        TAPE_SPEAKER_KEYS.put("4", tape5);

        Map<String, String> tape6 = new HashMap<>();
        tape6.put("Detective Morrison", "morrison");
        tape6.put("Charles Webb",       "charles");
        TAPE_SPEAKER_KEYS.put("5", tape6);

        Map<String, String> tape7 = new HashMap<>();
        tape7.put("Margaret Vance", "margaret");
        TAPE_SPEAKER_KEYS.put("6", tape7);

        Map<String, String> tape8 = new HashMap<>();
        tape8.put("Arthur Hollis", "arthur");
        tape8.put("Thomas Ashford", "thomas");
        TAPE_SPEAKER_KEYS.put("7", tape8);
    }

    private static final Map<String, String> TAPE_LABEL_MAP = new HashMap<>();
    static {
        TAPE_LABEL_MAP.put("JAMES",              "James Vance");
        TAPE_LABEL_MAP.put("MARGARET",           "Margaret Vance");
        TAPE_LABEL_MAP.put("DANIEL",             "Daniel the Groundskeeper");
        TAPE_LABEL_MAP.put("MARCUS",             "Marcus Blackwood");
        TAPE_LABEL_MAP.put("CHARLES",            "Charles Webb");
        TAPE_LABEL_MAP.put("ARTHUR",             "Arthur Hollis");
        TAPE_LABEL_MAP.put("THOMAS",             "Thomas Ashford");
        TAPE_LABEL_MAP.put("VOICE",              "Thomas Ashford");
        TAPE_LABEL_MAP.put("MORRISON",           "Detective Morrison");
        TAPE_LABEL_MAP.put("DETECTIVE MORRISON", "Detective Morrison");
        TAPE_LABEL_MAP.put("HAROLD",             "Harold Vance");
    }

    /** Callback invoked when the tape playback is finished and the player closes the panel. */
    private Runnable onComplete;
    /** The tape to play on show(). */
    private Tape initialTape;

    public TapeScreen(DSAGame game, Tape tape, Runnable onComplete) {
        this.game       = game;
        this.batch      = game.batch;
        this.viewport   = game.viewport;
        this.initialTape = tape;
        this.onComplete  = onComplete;
        this.font     = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.2f);
        this.textPanel = new TextPanel();
        this.textPanel.setCloseButtonVisible(false);

        loadPortraits();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.F9) {
                    toggleLayoutDebug();
                    return true;
                }

                if (textPanel.isVisible()) {
                    boolean advanceKey = (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE);
                    if (handleLayoutDebugNudge(keycode)) return true;
                    if (!textPanel.isRevealComplete() && advanceKey) {
                        textPanel.skipReveal();
                        stopVoice();
                        justSkipped = true;
                    } else if (advanceKey && !justSkipped) {
                        advancePage();
                    } else {
                        return false;
                    }
                    return true;
                }
                switch (keycode) {
                    // Tape selection disabled in game context — auto-plays on show()
                    case Input.Keys.ESCAPE: if (onComplete != null) onComplete.run(); break;
                }
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float gameX = screenX * (float) DSAGame.SCREEN_WIDTH / Gdx.graphics.getWidth();
                float gameY = (Gdx.graphics.getHeight() - screenY) * (float) DSAGame.SCREEN_HEIGHT / Gdx.graphics.getHeight();
                if (textPanel.isVisible()) {
                    String action = textPanel.handleClick(gameX, gameY);
                    if ("panel_consumed".equals(action)) {
                        stopVoice();
                        justSkipped = true;
                    } else if ("next_page".equals(action) && !justSkipped) {
                        advancePage();
                    } else if ("close".equals(action)) {
                        stopAndHide();
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

    private void advancePage() {
        textPanel.nextPage();
        if (!textPanel.isVisible()) {
            // Last page was just passed — tape is done
            stopAndHide();
            return;
        }
        playAudioForCurrentPage();
    }

    private void playAudioForCurrentPage() {
        Map<String, String> speakerKeys = TAPE_SPEAKER_KEYS.get(String.valueOf(currentTapeIndex));
        if (speakerKeys == null) return;

        String pageText = textPanel.getCurrentPageText();
        if (pageText == null || pageText.equals(lastPageText)) return;
        lastPageText = pageText;

        int pageIndex = textPanel.getCurrentPage();
        String speaker = (pageIndex >= 0 && pageIndex < pageSpeakers.size())
                ? pageSpeakers.get(pageIndex)
                : detectPageSpeaker(pageText);
        if (speaker == null) return;

        TextPanel.setCharsPerSecond("Margaret Vance".equals(speaker) ? 10f : 20f);

        String audioKey = speakerKeys.get(speaker);
        if (audioKey == null) return;

        int count = speakerLineCounts.getOrDefault(audioKey, 0) + 1;
        speakerLineCounts.put(audioKey, count);

        // Stop the current voice but not door/sfx sounds playing as Music
        if (currentSound != null) {
            currentSound.stop();
            currentSound.dispose();
            currentSound = null;
        }

        String folder = "tape" + (currentTapeIndex + 1) + "/";

        // Tape 2 special chains
        if (currentTapeIndex == 1) {
            if ("morrison".equals(audioKey) && count == 1) {
                speakerLineCounts.put(audioKey, 2);
                playFileChained(folder + "morrison_1.mp3", folder + "morrison_2.mp3");
                return;
            }
            if ("james".equals(audioKey) && count == 1) {
                speakerLineCounts.put(audioKey, 2);
                playFileChained(folder + "james_1.mp3", folder + "james_2.mp3");
                return;
            }
        }

        // Tape 3 special chains
        if (currentTapeIndex == 2) {
            if ("morrison".equals(audioKey) && count == 1) {
                speakerLineCounts.put(audioKey, 2);
                playFileChained(folder + "morrison_1.mp3", folder + "morrison_2.mp3");
                return;
            }
            if ("daniel".equals(audioKey) && count == 17) {
                speakerLineCounts.put(audioKey, 18);
                playFileChained(folder + "daniel_17.mp3", folder + "daniel_18.mp3");
                return;
            }
        }

        // Tape 7 special chains
        if (currentTapeIndex == 6) {
            if ("margaret".equals(audioKey) && count == 9) {
                speakerLineCounts.put(audioKey, 9);
                playFileChained(folder + "crying.mp3", folder + "margaret_9.mp3");
                return;
            }
        }

        // Tape 5 opener cue: door sound before first Morrison line
        if (currentTapeIndex == 4) {
            if ("morrison".equals(audioKey) && count == 1) {
                playFileChained("tape1/door_open.mp3", folder + "morrison_1.mp3");
                return;
            }
        }

        // Tape 6 opener cue: door sound before first Morrison line
        if (currentTapeIndex == 5) {
            if ("morrison".equals(audioKey) && count == 1) {
                textFrozen = true;
                playFile("tape1/door_open.mp3");
                scheduleDelayedPlay(folder + "morrison_1.mp3", 4f, () -> textFrozen = false);
                return;
            }
        }

        // Tape 1 special chains
        if (currentTapeIndex == 0) {
            if ("harold".equals(audioKey) && count == 1) {
                textFrozen = true;
                playFileChained(folder + "door_open.mp3", folder + "harold_1.mp3", () -> textFrozen = false);
                return;
            } else if ("harold".equals(audioKey) && count == 2) {
                speakerLineCounts.put(audioKey, 3);
                playFileChained(folder + "harold_2.mp3", folder + "harold_3.mp3");
                return;
            } else if ("harold".equals(audioKey) && count == 7) {
                playFileChained(folder + "harold_7.mp3", folder + "door_slam.mp3");
                return;
            }
        }

        playFile(folder + audioKey + "_" + count + ".mp3");
    }

    private void playFile(String path) {
        String resolvedPath = resolveAudioPath(path);
        if (resolvedPath == null) return;
        try {
            currentSound = Gdx.audio.newSound(Gdx.files.internal(resolvedPath));
            currentSound.play();
        } catch (Exception e) {
            Gdx.app.error("Audio", "Failed to play " + resolvedPath + ": " + e.getMessage());
        }
    }

    private void playFileChained(String first, String second) {
        playFileChained(first, second, null);
    }

    private void playFileChained(String first, String second, Runnable onFirstComplete) {
        playFileChained(first, second, onFirstComplete, 0f);
    }

    private void playFileChained(String first, String second, Runnable onFirstComplete, float secondDelaySeconds) {
        String resolvedFirst = resolveAudioPath(first);
        if (resolvedFirst == null) return;
        try {
            currentMusic = Gdx.audio.newMusic(Gdx.files.internal(resolvedFirst));
            currentMusic.setOnCompletionListener(m -> {
                m.dispose();
                currentMusic = null;
                if (onFirstComplete != null) onFirstComplete.run();
                if (secondDelaySeconds > 0f) {
                    scheduleDelayedPlay(second, secondDelaySeconds);
                } else {
                    playFile(second);
                }
            });
            currentMusic.play();
        } catch (Exception e) {
            Gdx.app.error("Audio", "Failed to play " + resolvedFirst + ": " + e.getMessage());
        }
    }

    private void scheduleDelayedPlay(String path, float delaySeconds) {
        scheduleDelayedPlay(path, delaySeconds, null);
    }

    private void scheduleDelayedPlay(String path, float delaySeconds, Runnable onPlay) {
        pendingDelayedPath = path;
        long delayMs = (long) (Math.max(0f, delaySeconds) * 1000f);
        pendingDelayedAtMs = TimeUtils.millis() + delayMs;
        pendingDelayedOnPlay = onPlay;
    }

    private void updateDelayedPlay(float delta) {
        if (pendingDelayedPath == null) return;
        if (TimeUtils.millis() >= pendingDelayedAtMs) {
            String path = pendingDelayedPath;
            Runnable onPlay = pendingDelayedOnPlay;
            pendingDelayedPath = null;
            pendingDelayedAtMs = 0L;
            pendingDelayedOnPlay = null;
            if (onPlay != null) onPlay.run();
            playFile(path);
        }
    }

    private String resolveAudioPath(String requestedPath) {
        if (requestedPath == null || requestedPath.isEmpty()) return null;
        if (Gdx.files.internal(requestedPath).exists()) return requestedPath;

        if (requestedPath.endsWith(".mp3")) {
            String base = requestedPath.substring(0, requestedPath.length() - 4);
            String wav = base + ".wav";
            if (Gdx.files.internal(wav).exists()) return wav;
            String ogg = base + ".ogg";
            if (Gdx.files.internal(ogg).exists()) return ogg;
        }
        return null;
    }

    private void stopVoice() {
        textFrozen = false;
        pendingDelayedPath = null;
        pendingDelayedAtMs = 0L;
        pendingDelayedOnPlay = null;
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
        if (currentSound != null) {
            currentSound.stop();
            currentSound.dispose();
            currentSound = null;
        }
    }

    private void stopCurrentSound() {
        pendingDelayedPath = null;
        pendingDelayedAtMs = 0L;
        pendingDelayedOnPlay = null;
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
        if (currentSound != null) {
            currentSound.stop();
            currentSound.dispose();
            currentSound = null;
        }
    }

    private void stopAndHide() {
        stopCurrentSound();
        textPanel.hide();
        // Notify caller that tape is done
        if (onComplete != null) {
            Runnable cb = onComplete;
            onComplete = null; // prevent double-fire
            cb.run();
        }
    }

    private void loadPortraits() {
        String[][] mapping = {
            { "James Vance",              "characters/james.png"    },
            { "Margaret Vance",           "characters/margaret.png" },
            { "Charles Webb",             "characters/charles.png"  },
            { "Daniel the Groundskeeper", "characters/daniel.png"   },
            { "Arthur Hollis",            "characters/arthur.png"   },
            { "Marcus Blackwood",         "characters/marcus.png"   },
            { "Detective Morrison",       "characters/morrison.png" },
            { "Harold Vance",             "characters/harold.png"   },
        };
        for (String[] entry : mapping) {
            try {
                Texture tex = new Texture(Gdx.files.internal(entry[1]));
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                portraitTextures.put(entry[0], tex);
            } catch (Exception e) {
                Gdx.app.error("Portrait", "Failed to load " + entry[1]);
            }
        }
    }

    private String stripParentheticals(String text) {
        return text.replaceAll("[ \\t]*\\([^)\n]+\\)[ \\t]*", " ").trim();
    }

    private String buildDisplayTranscript(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] parts = text.split("\\n\\n");
        StringBuilder cleaned = new StringBuilder();
        List<String> speakers = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part == null ? "" : part.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.matches("^\\[[^\\n\\]]*\\]$")) continue;

            String speaker = detectPageSpeaker(trimmed);
            String pageText = trimmed;
            if (speaker != null) {
                int colon = trimmed.indexOf(':');
                if (colon > 0) {
                    pageText = trimmed.substring(colon + 1).trim();
                }
            }
            pageText = stripOuterQuotes(pageText);
            if (pageText.isEmpty()) continue;

            if (cleaned.length() > 0) cleaned.append("\n\n");
            cleaned.append(pageText);
            speakers.add(speaker);
        }
        pageSpeakers = speakers;
        return cleaned.toString();
    }

    private String stripOuterQuotes(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.length() < 2) return t;

        boolean straight = t.startsWith("\"") && t.endsWith("\"");
        boolean curly = t.startsWith("\u201c") && t.endsWith("\u201d");
        if (straight || curly) {
            return t.substring(1, t.length() - 1).trim();
        }
        return t;
    }

    private String detectPageSpeaker(String pageText) {
        if (pageText == null) return null;
        int colon = pageText.indexOf(':');
        if (colon <= 0 || colon > 30) return null;
        String label = pageText.substring(0, colon).trim().toUpperCase();
        int paren = label.indexOf('(');
        if (paren > 0) label = label.substring(0, paren).trim();
        return TAPE_LABEL_MAP.get(label);
    }

    private void playTape(int index) {
        layoutDebugEnabled = false;
        stopCurrentSound();
        currentTapeIndex = index;
        speakerLineCounts.clear();
        lastPageText = null;
        textFrozen = false;
        pageSpeakers = Collections.emptyList();
        pendingDelayedPath = null;
        pendingDelayedAtMs = 0L;
        pendingDelayedOnPlay = null;
        leftSpeaker = null; rightSpeaker = null;
        leftPortrait = null; rightPortrait = null;
        leftAlpha = 0f; rightAlpha = 0f;
        leftIsActive = true;

        TextPanel.setCharsPerSecond(20f);
        Tape tape = Tape.values()[index];
        String transcript = stripParentheticals(TapeContent.getTranscript(tape));
        transcript = buildDisplayTranscript(transcript);
        textPanel.showDialogue(tape.getTitle(), transcript, new ArrayList<>());

        playAudioForCurrentPage();
    }

    private boolean handleLayoutDebugNudge(int keycode) {
        if (!layoutDebugEnabled || !textPanel.hasCustomDialogueTexture()) return false;

        boolean changed = false;
        switch (keycode) {
            // Move speaker label with WASD
            case Input.Keys.W: textPanel.nudgeCustomSpeaker(0f, DEBUG_NUDGE_STEP); changed = true; break;
            case Input.Keys.S: textPanel.nudgeCustomSpeaker(0f, -DEBUG_NUDGE_STEP); changed = true; break;
            case Input.Keys.A: textPanel.nudgeCustomSpeaker(-DEBUG_NUDGE_STEP, 0f); changed = true; break;
            case Input.Keys.D: textPanel.nudgeCustomSpeaker(DEBUG_NUDGE_STEP, 0f); changed = true; break;

            // Move body text with arrow keys
            case Input.Keys.UP:    textPanel.nudgeCustomText(0f, DEBUG_NUDGE_STEP); changed = true; break;
            case Input.Keys.DOWN:  textPanel.nudgeCustomText(0f, -DEBUG_NUDGE_STEP); changed = true; break;
            case Input.Keys.LEFT:  textPanel.nudgeCustomText(-DEBUG_NUDGE_STEP, 0f); changed = true; break;
            case Input.Keys.RIGHT: textPanel.nudgeCustomText(DEBUG_NUDGE_STEP, 0f); changed = true; break;

            case Input.Keys.R:
                textPanel.resetCustomLayoutOffsets();
                changed = true;
                break;
            default:
                return false;
        }

        if (changed) {
            Gdx.app.log("UI-LAYOUT", textPanel.getCustomLayoutDebugString());
        }
        return changed;
    }

    private void toggleLayoutDebug() {
        if (!textPanel.hasCustomDialogueTexture()) {
            Gdx.app.log("UI-LAYOUT", "Custom texture not loaded; cannot start layout debug.");
            return;
        }

        layoutDebugEnabled = !layoutDebugEnabled;
        if (!layoutDebugEnabled) {
            stopAndHide();
            Gdx.app.log("UI-LAYOUT", "Layout debug disabled.");
            return;
        }

        stopCurrentSound();
        currentTapeIndex = -1;
        speakerLineCounts.clear();
        lastPageText = null;
        pageSpeakers = Collections.emptyList();
        textFrozen = false;
        justSkipped = false;
        leftSpeaker = null; rightSpeaker = null;
        leftPortrait = null; rightPortrait = null;
        leftAlpha = 0f; rightAlpha = 0f;
        leftIsActive = true;

        String transcript = buildLayoutDebugTranscript();
        textPanel.showDialogue("Detective Morrison", transcript, new ArrayList<>());
        textPanel.skipReveal();
        Gdx.app.log("UI-LAYOUT", "Layout debug enabled. WASD=speaker, arrows=text, R=reset, F9=exit.");
        Gdx.app.log("UI-LAYOUT", textPanel.getCustomLayoutDebugString());
    }

    private String buildLayoutDebugTranscript() {
        String longest = "Interview text placeholder for layout debug.";
        for (Tape tape : Tape.values()) {
            String raw = TapeContent.getTranscript(tape);
            if (raw == null || raw.isEmpty()) continue;
            String cleaned = stripParentheticals(raw);
            String[] parts = cleaned.split("\\n\\n");
            for (String part : parts) {
                String p = part == null ? "" : part.trim();
                if (p.isEmpty()) continue;
                if (p.matches("^\\[[^\\n\\]]*\\]$")) continue;
                int colon = p.indexOf(':');
                if (colon > 0) p = p.substring(colon + 1).trim();
                p = stripOuterQuotes(p);
                if (p.length() > longest.length()) longest = p;
            }
        }
        return longest;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(game.camera.combined);
        batch.begin();

        // In game context, panel not visible means tape finished — do nothing
        if (!textPanel.isVisible()) {
            // no-op: stopAndHide() already called onComplete
        }

        // Portrait system
        if (textPanel.isVisible() && textPanel.isDialogueMode()) {
            String pageText    = textPanel.getCurrentPageText();
            int pageIndex = textPanel.getCurrentPage();
            String pageSpeaker = (pageIndex >= 0 && pageIndex < pageSpeakers.size())
                    ? pageSpeakers.get(pageIndex)
                    : detectPageSpeaker(pageText);
            if (pageSpeaker == null && pageText != null
                    && !pageText.startsWith("===") && !pageText.startsWith("[")) {
                pageSpeaker = textPanel.getSpeaker();
            }
            if (pageSpeaker == null && pageText != null
                    && (pageText.startsWith("===") || pageText.startsWith("["))) {
                textPanel.setSpeaker(null);
            }
            if (pageSpeaker != null) {
                textPanel.setSpeaker(pageSpeaker);
                if (portraitTextures.containsKey(pageSpeaker)) {
                    if (leftSpeaker == null) {
                        leftSpeaker  = pageSpeaker;
                        leftPortrait = portraitTextures.get(pageSpeaker);
                    } else if (!pageSpeaker.equals(leftSpeaker) && rightSpeaker == null) {
                        rightSpeaker  = pageSpeaker;
                        rightPortrait = portraitTextures.get(pageSpeaker);
                    }
                    leftIsActive = pageSpeaker.equals(leftSpeaker);
                }
            }
        } else if (!textPanel.isVisible()) {
            leftSpeaker = null; rightSpeaker = null;
            leftPortrait = null; rightPortrait = null;
            leftIsActive = true;
        }

        float leftTarget  = leftPortrait  != null ? (leftIsActive  ? 1f : 0f) : 0f;
        float rightTarget = rightPortrait != null ? (!leftIsActive ? 1f : 0f) : 0f;
        leftAlpha  += (leftTarget  - leftAlpha)  * Math.min(1f, delta * 8f);
        rightAlpha += (rightTarget - rightAlpha) * Math.min(1f, delta * 8f);

        updateDelayedPlay(delta);

        final float PH = 560f;
        if (leftPortrait != null && leftAlpha > 0.01f) {
            float pw = PH * leftPortrait.getWidth() / (float) leftPortrait.getHeight();
            batch.setColor(1f, 1f, 1f, leftAlpha);
            batch.draw(leftPortrait, 40f, 0f, pw, PH);
        }
        if (rightPortrait != null && rightAlpha > 0.01f) {
            float pw = PH * rightPortrait.getWidth() / (float) rightPortrait.getHeight();
            float px = DSAGame.SCREEN_WIDTH - 40f - pw;
            batch.setColor(1f, 1f, 1f, rightAlpha);
            batch.draw(rightPortrait, px + pw, 0f, -pw, PH);
        }
        batch.setColor(Color.WHITE);

        if (!textFrozen) textPanel.update(delta);
        textPanel.render(batch, font);
        justSkipped = false;

        batch.end();
    }

    private void drawMenu() {
        Tape[] tapes = Tape.values();
        float x = 80;
        float startY = DSAGame.SCREEN_HEIGHT - 80;
        font.setColor(Color.WHITE);
        font.draw(batch, "Press 1-8 to play a tape   |   ESC to quit", x, DSAGame.SCREEN_HEIGHT - 40);
        for (int i = 0; i < tapes.length; i++) {
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, (i + 1) + ".  " + tapes[i].getTitle(), x, startY - (i * 45));
        }
    }

    @Override public void resize(int width, int height) { viewport.update(width, height); }
    @Override
    public void show() {
        // Auto-play the tape passed to the constructor
        if (initialTape != null) {
            playTape(initialTape.ordinal());
            initialTape = null;
        }
    }
    @Override public void hide() {}

    @Override
    public void pause() {
        if (currentMusic != null) currentMusic.pause();
        if (currentSound != null) currentSound.pause();
    }

    @Override
    public void resume() {
        if (currentMusic != null) currentMusic.play();
        if (currentSound != null) currentSound.resume();
    }
    @Override
    public void dispose() {
        font.dispose();
        stopCurrentSound();
        for (Texture t : portraitTextures.values()) t.dispose();
        if (currentMusic != null) { currentMusic.dispose(); currentMusic = null; }
    }
}
