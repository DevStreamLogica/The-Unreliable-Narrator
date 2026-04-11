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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dsa.game.DSAGame;
import com.dsa.game.data.ClimaxContent;
import com.dsa.game.data.RoomDescriptions;
import com.dsa.game.data.SuspectDialogue;
import com.dsa.game.data.TapeContent;
import com.dsa.game.data.NarratorText;
import com.dsa.game.navigation.*;
import com.dsa.game.rendering.PlaceholderGenerator;
import com.dsa.game.state.*;
import com.dsa.game.systems.*;
import com.dsa.game.ui.*;

import java.util.*;

public class GameScreen implements Screen {

    private DSAGame game;
    private SpriteBatch batch;
    private FitViewport viewport;
    private BitmapFont font;
    private BitmapFont titleFont;
    private final Vector2 touchPos = new Vector2();

    private RoomManager roomManager;

    private Map<Room.RoomID, Texture> roomTextures;
    private Texture kitchenWithoutTapeTex;
    private Texture parlorWithoutBriefcaseTex;
    private Texture studyWithPokerWithTapeTex;
    private Texture studyWithPokerWithoutTapeTex;
    private Texture jamesClosedTex;
    private Texture guestRoomMargaretCloseTex;
    private Texture guestRoomsUnlockedTex;
    private Texture shedTapeBoots;
    private Texture shedTapeNoBoots;
    private Texture shedNoTapeBoots;
    private Texture shedNoTapeNoBoots;
    /** Same art as Testing 3 {@code shed.png} / {@code inventory/rooms/shed.png}; drawn flipped like the inventory shed. */
    private Texture shedCanonicalTex;
    // Margaret's room: 12 state textures
    private Texture mTapeTopClosedBotClosed;
    private Texture mTapeTopClosedBotOpenShoes;
    private Texture mTapeTopClosedBotOpenNoShoes;
    private Texture mTapeTopOpenKit;
    private Texture mTapeTopOpenNoKit;
    private Texture mTapeTopOpenNoKitBotOpen;
    private Texture mNoTapeTopClosedBotClosed;
    private Texture mNoTapeTopClosedBotOpenShoes;
    private Texture mNoTapeTopClosedBotOpenNoShoes;
    private Texture mNoTapeTopOpenKit;
    private Texture mNoTapeTopOpenNoKit;
    private Texture mNoTapeTopOpenNoKitBotOpen;
    private Texture pixelTexture;

    private String currentTooltip = "";
    private GlyphLayout layout;

    private Room.RoomID lastRenderedRoom = null;

    // Game state & systems
    private GameState gameState;
    private AwarenessSystem awarenessSystem;
    private EvidenceSystem evidenceSystem;
    private ExaminationSystem examinationSystem;
    private InterviewSystem interviewSystem;
    private NarratorSystem narratorSystem;
    private HintSystem hintSystem;
    private SaveLoadSystem saveLoadSystem;
    private AchievementSystem achievementSystem;

    // UI components
    private TextPanel textPanel;
    private AwarenessMeter awarenessMeter;
    private ActionBar actionBar;
    private DocumentReconstructionGame documentGame;

    // Room transition fade
    private float transitionAlpha = 0f;
    private boolean transitionFadingOut = false;
    private boolean transitionFadingIn = false;
    private static final float TRANSITION_SPEED = 5.0f; // reaches 1.0 in 0.2s
    private Runnable pendingTransition = null;

    // Back button texture
    private Texture backButtonTex;

    // Character portraits — two-portrait VN system (left / right)
    private Map<String, Texture> portraitTextures;
    private Texture leftPortrait  = null, rightPortrait  = null;
    private String  leftSpeaker   = null, rightSpeaker   = null;
    private float   leftAlpha     = 0f,   rightAlpha     = 0f;
    private boolean leftIsActive  = true; // which side is currently speaking

    // Maps tape line labels (e.g. "JAMES") to full speaker names for portrait lookup
    private static final Map<String, String> TAPE_LABEL_MAP = new java.util.HashMap<>();
    static {
        TAPE_LABEL_MAP.put("JAMES",              "James Vance");
        TAPE_LABEL_MAP.put("MARGARET",           "Margaret Vance");
        TAPE_LABEL_MAP.put("DANIEL",             "Daniel the Groundskeeper");
        TAPE_LABEL_MAP.put("MARCUS",             "Marcus Blackwood");
        TAPE_LABEL_MAP.put("CHARLES",            "Charles Webb");
        TAPE_LABEL_MAP.put("ARTHUR",             "Arthur Hollis");
        TAPE_LABEL_MAP.put("MORRISON",           "Detective Morrison");
        TAPE_LABEL_MAP.put("DETECTIVE MORRISON", "Detective Morrison");
        TAPE_LABEL_MAP.put("HAROLD",             "Harold Vance");
    }

    // Panel mode tracking
    private enum PanelMode {
        NONE, TEXT, INVENTORY, SUSPECTS, SUSPECT_LIST, INTERVIEW, TAPE_PLAY, SHOW_EVIDENCE, ACCUSE_SELECT, NOTEBOOK,
        SAVE_MENU, LOAD_MENU, PAUSE, HISTORY, OBJECTIVES, SETTINGS, NARRATOR_BREAKDOWN
    }

    private PanelMode panelMode = PanelMode.NONE;

    // Climax state
    private boolean pendingClimax = false;

    // Set to true by loadFromSave() so show() skips the opening sequence
    private boolean isLoadedGame = false;

    // Set to true before launching a minigame so show() skips the opening sequence on return
    private boolean returningFromMinigame = false;
    private float minigameReturnCooldown = 0f;
    private static final float MINIGAME_RETURN_COOLDOWN = 0.5f;

    // Mini-game state
    private ExamResult.MiniGameType pendingMiniGame = null;

    // ── New between-tape sequence ──────────────────────────────────────────────
    /** Index of the tape chapter currently being worked toward (0-based). */
    private int currentChapter = 0;
    /** Set after maze completes to trigger the evidence-gap session on this screen. */
    private boolean pendingInventoryStart = false;
    /** Between-tape evidence collection while staying on manor navigation ({@code null} when inactive). */
    private EvidenceGapSession evidenceGapSession = null;
    /** Set when player enters the metal-detector target room. */
    private Room.RoomID metalDetectorTargetRoom = null;
    /** True while the metal detector overlay is active during room navigation. */
    private boolean metalDetectorModeActive = false;
    /** Beep thread for the room-navigation phase of metal detector. */
    private Thread metalDetectorBeepThread = null;
    private volatile float   mdBeepInterval = 4000f;
    private volatile boolean mdBeepRunning  = false;
    /** True once the player has entered the correct room during metal-detector mode. */
    private boolean metalDetectorRoomReached = false;
    /** Icon drawn on screen while metal-detector is active. */
    private Texture metalDetectorIconTex = null;
    /** Flash/pulse timer for the metal-detector icon. */
    private float mdIconFlashTimer = 0f;
    /** Set true after the opening sequence to trigger the first metal-detector on close. */
    /** Set of tapes for which the maze has already been played. */
    private final java.util.Set<Tape> mazePlayedTapes = new java.util.HashSet<>();

    // ── Narrator spotlight (every 4 minutes, not in entrance/cellar) ──────────
    private static final float SPOTLIGHT_INTERVAL = 240f; // 4 minutes
    private float spotlightTimer = SPOTLIGHT_INTERVAL;

    // --- Darkness / lighting mechanic ---
    private Texture spotlightTexture;
    private com.badlogic.gdx.graphics.glutils.FrameBuffer lightsBuffer;
    /** Debug toggle: when true, skip darkness/limited-visibility mask. */
    private boolean debugDisableLimitedVisibility = false;
    private float cursorGameX = DSAGame.SCREEN_WIDTH / 2f;
    private float cursorGameY = DSAGame.SCREEN_HEIGHT / 2f;

    // Entity cursor pull
    private float pullOffsetX   = 0f, pullOffsetY   = 0f;
    private float pullTimer     = 0f, pullActiveTimer = 0f;
    private boolean pullActive  = false;
    private static final float PULL_INTERVAL = 10f;
    private static final float PULL_DURATION  = 2.5f;
    private static final float PULL_MAX       = 45f;

    // Hold-timer (hover-to-examine replaces click-to-examine)
    private float  holdTimer        = 0f;
    private String holdTargetObject = null;
    private static final float HOLD_TIME_MIN = 1.2f; // seconds at awareness 0
    private static final float HOLD_TIME_MAX = 3.2f; // seconds at awareness 80

    // Mouse stillness — hold timer only begins after mouse has been still 1 second
    private float mouseStillTimer = 0f;
    private static final float MOUSE_STILL_DELAY = 0.25f;

    // Reveal state — item shown in room briefly before examination fires
    private String pendingRevealObject = null;
    private float revealTimer = 0f;
    private static final float REVEAL_DURATION = 0.8f;

    /** Matches drawn back.png; ARROW_BACK hotspot must align for hit-testing. */
    private static final float ARROW_BACK_DRAW_X = 12f;
    private static final float ARROW_BACK_DRAW_W = 70f;
    private static final float ARROW_BACK_DRAW_H = 70f;

    // Ambient narrator timer — periodic chatter while player is idle in a room
    private float ambientNarratorTimer = 15f; // first line fires after 15s
    private float ambientNarratorInterval = 0f; // set each time a line fires

    public GameScreen(DSAGame game) {
        this.game = game;
        this.batch = game.batch;
        this.viewport = game.viewport;
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(1.2f);

        this.titleFont = new BitmapFont();
        this.titleFont.setColor(new Color(0.95f, 0.95f, 0.9f, 1));
        this.titleFont.getData().setScale(1.4f);

        this.layout = new GlyphLayout();

        // Initialize game state & systems
        gameState = new GameState();
        awarenessSystem = new AwarenessSystem(gameState);
        evidenceSystem = new EvidenceSystem(gameState);
        examinationSystem = new ExaminationSystem(gameState);
        interviewSystem = new InterviewSystem(gameState);
        narratorSystem = new NarratorSystem(gameState);
        hintSystem = new HintSystem(gameState, evidenceSystem);
        saveLoadSystem = new SaveLoadSystem();
        achievementSystem = new AchievementSystem(gameState, evidenceSystem);

        // Initialize room manager
        roomManager = new RoomManager();

        // Generate textures
        generatePlaceholderTextures();
        generateUITextures();
        loadPortraits();
        backButtonTex = new Texture(Gdx.files.internal("art/Visual Characters/back.png"));
        backButtonTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Initialize UI components
        textPanel = new TextPanel();
        awarenessMeter = new AwarenessMeter();
        actionBar = new ActionBar();
        documentGame = new DocumentReconstructionGame();
        spotlightTexture = createSpotlightTexture();
        lightsBuffer = new com.badlogic.gdx.graphics.glutils.FrameBuffer(
                Pixmap.Format.RGBA8888, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT, false);

        setupInput();
    }

    private void generatePlaceholderTextures() {
        roomTextures = new HashMap<>();

        for (Room.RoomID roomId : Room.RoomID.values()) {
            String baseName = "rooms/" + roomId.name().toLowerCase();
            String pngPath = baseName + ".png";
            String jpgPath = baseName + ".jpg";
            if (Gdx.files.internal(pngPath).exists()) {
                roomTextures.put(roomId, new Texture(Gdx.files.internal(pngPath)));
            } else if (Gdx.files.internal(jpgPath).exists()) {
                roomTextures.put(roomId, new Texture(Gdx.files.internal(jpgPath)));
            } else {
                roomTextures.put(roomId, PlaceholderGenerator.generateRoomPlaceholder(
                        roomId, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT));
            }
        }
        if (Gdx.files.internal("rooms/kitchen without.png").exists()) {
            kitchenWithoutTapeTex = new Texture(Gdx.files.internal("rooms/kitchen without.png"));
        }
        if (Gdx.files.internal("rooms/parlort without briefcase.jpg").exists()) {
            parlorWithoutBriefcaseTex = new Texture(Gdx.files.internal("rooms/parlort without briefcase.jpg"));
        }
        // Prop layers: use PNG with transparency everywhere except the sprites (full 1280×720 canvas).
        studyWithPokerWithTapeTex    = loadTexLinear("rooms/study_with_poker_with_tape.png");
        studyWithPokerWithoutTapeTex = loadTexLinear("rooms/study_with_poker_without_tape.png");
        mTapeTopClosedBotClosed = loadMargaretTex("Tape, Top Closed, Bottom Closed.png");
        mTapeTopClosedBotOpenShoes = loadMargaretTex("Tape, Top Closed, Bottom Open with Shoes.png");
        mTapeTopClosedBotOpenNoShoes = loadMargaretTex("Tape, Top Closed, Bottom Open no Shoes.png");
        mTapeTopOpenKit = loadMargaretTex("Tape, Top Open with Kit, Bottom Closed.png");
        mTapeTopOpenNoKit = loadMargaretTex("Tape, Top Open no Kit, Bottom Closed.png");
        mTapeTopOpenNoKitBotOpen = loadMargaretTex("Tape, Top Open no Kit, Bottom Open no Shoes.jpg");
        mNoTapeTopClosedBotClosed = loadMargaretTex("No Tape, Top Closed, Bottom Closed.png");
        mNoTapeTopClosedBotOpenShoes = loadMargaretTex("No Tape, Top Closed, Bottom Open with Shoes.png");
        mNoTapeTopClosedBotOpenNoShoes = loadMargaretTex("No Tape, Top Closed, Bottom Open no Shoes.png");
        mNoTapeTopOpenKit = loadMargaretTex("No Tape, Top Open with Kit, Bottom Closed.png");
        mNoTapeTopOpenNoKit = loadMargaretTex("No Tape, Top Open no Kit, Bottom Closed.png");
        mNoTapeTopOpenNoKitBotOpen = loadMargaretTex("No Tape, Top Open no Kit, Bottom Open no Shoes.jpg");
        if (mNoTapeTopClosedBotClosed != null) {
            roomTextures.put(Room.RoomID.MARGARET_ROOM, mNoTapeTopClosedBotClosed);
        } else if (mTapeTopClosedBotClosed != null) {
            roomTextures.put(Room.RoomID.MARGARET_ROOM, mTapeTopClosedBotClosed);
        } else if (Gdx.files.internal("rooms/margarette room.png").exists()) {
            roomTextures.put(Room.RoomID.MARGARET_ROOM, new Texture(Gdx.files.internal("rooms/margarette room.png")));
        }
        if (Gdx.files.internal("rooms/james closed.jpeg").exists()) {
            jamesClosedTex = new Texture(Gdx.files.internal("rooms/james closed.jpeg"));
        }
        guestRoomMargaretCloseTex = loadTex("rooms/guest_room_margaret_close.jpg");
        guestRoomsUnlockedTex = loadTex("rooms/guest_rooms.png");
        shedCanonicalTex = loadTexLinear("inventory/rooms/shed.png");
        if (shedCanonicalTex == null) shedCanonicalTex = loadTexLinear("shed.png");
        shedTapeBoots   = loadTex("rooms/Shed_with_tape_with_boots.jpg");
        shedTapeNoBoots = loadTex("rooms/Shed_with_tape_without_boots.jpg");
        shedNoTapeBoots = loadTex("rooms/shed_without_tape_with_boots.jpg");
        shedNoTapeNoBoots = loadTex("rooms/Shed_without_tape_without_boots.jpg");
        if (shedCanonicalTex != null) roomTextures.put(Room.RoomID.GROUNDSKEEPER_SHED, shedCanonicalTex);
        else if (shedNoTapeBoots != null) roomTextures.put(Room.RoomID.GROUNDSKEEPER_SHED, shedNoTapeBoots);
        else if (shedTapeBoots != null) roomTextures.put(Room.RoomID.GROUNDSKEEPER_SHED, shedTapeBoots);
        if (Gdx.files.internal("rooms/cellar.png").exists()) {
            roomTextures.put(Room.RoomID.CELLAR, new Texture(Gdx.files.internal("rooms/cellar.png")));
        }
    }

    private Texture loadMargaretTex(String filename) {
        String path = "rooms/margaret/" + filename;
        if (Gdx.files.internal(path).exists()) {
            return new Texture(Gdx.files.internal(path));
        }
        return null;
    }

    private Texture loadTex(String path) {
        if (Gdx.files.internal(path).exists()) return new Texture(Gdx.files.internal(path));
        return null;
    }

    private Texture loadTexLinear(String path) {
        Texture t = loadTex(path);
        if (t != null)
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    /**
     * Study tape / paper props as full-screen alpha layers on {@code rooms/study}.
     * Fireplace poker is not interactive; layers do not depend on removed poker evidence.
     */
    private void drawStudyPropLayers(SpriteBatch b) {
        final float sw = DSAGame.SCREEN_WIDTH;
        final float sh = DSAGame.SCREEN_HEIGHT;
        boolean flashReveal = "under_desk".equals(pendingRevealObject)
                || "bookshelves".equals(pendingRevealObject);
        boolean studyTapeCollected = gameState.hasTape(Tape.TAPE_ARGUMENT)
                || gameState.hasTape(Tape.TAPE_JAMES_INTERVIEW);
        boolean detectorHintsStudy = metalDetectorModeActive
                && metalDetectorTargetRoom == Room.RoomID.STUDY
                && metalDetectorRoomReached;
        boolean showTapeProps = flashReveal || studyTapeCollected || detectorHintsStudy;
        if (showTapeProps) {
            if (studyTapeCollected && studyWithPokerWithTapeTex != null)
                b.draw(studyWithPokerWithTapeTex, 0, 0, sw, sh);
            else if (studyWithPokerWithoutTapeTex != null)
                b.draw(studyWithPokerWithoutTapeTex, 0, 0, sw, sh);
        }
    }

    /** ARROW_BACK is created with placeholder bounds; sync each frame so clicks match the drawn button. */
    private void syncArrowBackHotspotBounds() {
        float by = actionBar.getBarHeight() + 4f;
        for (Hotspot h : roomManager.getCurrentRoom().getHotspots()) {
            if (h.getType() == Hotspot.HotspotType.ARROW_BACK) {
                h.setBounds(ARROW_BACK_DRAW_X, by, ARROW_BACK_DRAW_W, ARROW_BACK_DRAW_H);
                return;
            }
        }
    }

    /** Programmatic radial spotlight: warm centre fading to transparent at edges. */
    private Texture createSpotlightTexture() {
        int size = 256;
        com.badlogic.gdx.graphics.Pixmap pm =
                new com.badlogic.gdx.graphics.Pixmap(size, size,
                        com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        int cx = size / 2, cy = size / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx, dy = y - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy) / (size / 2f);
                float brightness = Math.max(0f, 1f - dist * dist * dist * dist); // steep falloff
                // RGB encodes brightness (multiply mask): white=reveal room, black=darkness
                pm.setColor(0.92f * brightness, 0.85f * brightness, 0.65f * brightness, 1f);
                pm.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private void generateUITextures() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        pixelTexture = new Texture(p);
        p.dispose();
    }

    /** Parses "JAMES: ..." or "JAMES (pause): ..." → "James Vance". Returns null if no known label. */
    private String detectPageSpeaker(String pageText) {
        if (pageText == null) return null;
        int colon = pageText.indexOf(':');
        if (colon <= 0 || colon > 30) return null;
        String label = pageText.substring(0, colon).trim().toUpperCase();
        // Strip parentheticals: "JAMES (pause)" → "JAMES"
        int paren = label.indexOf('(');
        if (paren > 0) label = label.substring(0, paren).trim();
        return TAPE_LABEL_MAP.get(label);
    }

    private void loadPortraits() {
        portraitTextures = new HashMap<>();
        String[][] mapping = {
            { "James Vance",               "characters/james.png"    },
            { "Margaret Vance",            "characters/margaret.png" },
            { "Charles Webb",              "characters/charles.png"  },
            { "Daniel the Groundskeeper",  "characters/daniel.png"   },
            { "Arthur Hollis",             "characters/arthur.png"   },
            { "Marcus Blackwood",          "characters/marcus.png"      },
            { "Detective Morrison",        "characters/morrison.png"    },
            { "Harold Vance",             "characters/harold.png"      },
        };
        for (String[] entry : mapping) {
            try {
                Texture tex = new Texture(Gdx.files.internal(entry[1]));
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                portraitTextures.put(entry[0], tex);
            } catch (Exception e) {
                Gdx.app.error("Portrait", "Failed to load " + entry[1] + ": " + e.getMessage());
            }
        }
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (minigameReturnCooldown > 0f) return true;

                touchPos.set(screenX, screenY);
                viewport.unproject(touchPos);
                float gameX = touchPos.x;
                float gameY = touchPos.y;
                updateCursorFromInput(gameX, gameY);
                // Allow text panel interaction even after game over/won
                if (textPanel.isVisible()) {
                    String action = textPanel.handleClick(gameX, gameY);
                    if (action != null) {
                        handlePanelAction(action);
                        return true;
                    }
                    return true;
                }

                if (gameState.isGameOver() || gameState.isGameWon())
                    return true;

                if (evidenceGapSession != null && evidenceGapSession.isCompletionLocked()) {
                    return true;
                }

                // Document reconstruction mini-game gets top priority
                if (documentGame.isActive()) {
                    if (documentGame.isCompleted()) {
                        documentGame.finish();
                    } else {
                        documentGame.handleTouchDown(gameX, gameY);
                    }
                    return true;
                }

                // Metal detector icon button (bottom-right)
                if (metalDetectorModeActive && metalDetectorIconTex != null) {
                    float iconSize = 56f;
                    float iconX = DSAGame.SCREEN_WIDTH - iconSize - 12f;
                    float iconY = 12f;
                    if (gameX >= iconX && gameX <= iconX + iconSize
                            && gameY >= iconY && gameY <= iconY + iconSize) {
                        if (roomManager.getCurrentRoom().getId() == metalDetectorTargetRoom) {
                            launchMetalDetectorScan();
                        } else {
                            showNotification("The signal is stronger elsewhere. Keep moving.");
                        }
                        return true;
                    }
                }

                // Action bar
                String barAction = actionBar.handleClick(gameX, gameY);
                if (barAction != null) {
                    handleActionBarClick(barAction);
                    return true;
                }

                if (evidenceGapSession != null) {
                    Room.RoomID wr = roomManager.getCurrentRoom().getId();
                    if (evidenceGapSession.isBagOpen()) {
                        if (evidenceGapSession.touchDownEmbeddedBagOpen(gameX, gameY)) return true;
                    } else if (evidenceGapSession.touchDownEmbeddedExploring(gameX, gameY, wr)) {
                        return true;
                    }
                }

                // Navigation first (back, doors, arrows) so large EXAMINE rects never steal clicks
                for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
                    if (hotspot.contains(gameX, gameY)
                            && hotspot.getType() != Hotspot.HotspotType.EXAMINE) {
                        handleNavigation(hotspot.getTargetRoom());
                        return true;
                    }
                }

                // EXAMINE hotspots — click to examine (disabled while embedded evidence phase is active)
                if (evidenceGapSession == null) {
                    for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
                        if (hotspot.contains(gameX, gameY) && hotspot.getType() == Hotspot.HotspotType.EXAMINE) {
                            handleExamine(hotspot.getObjectName());
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                touchPos.set(screenX, screenY);
                viewport.unproject(touchPos);
                float gx = touchPos.x, gy = touchPos.y;
                updateCursorFromInput(gx, gy);
                if (documentGame.isActive() && !documentGame.isCompleted()) {
                    documentGame.handleTouchDragged(gx, gy);
                    return true;
                }
                if (evidenceGapSession != null && evidenceGapSession.isBagOpen()) {
                    evidenceGapSession.touchDraggedEmbeddedBagOpen(gx, gy);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (documentGame.isActive() && !documentGame.isCompleted()) {
                    touchPos.set(screenX, screenY);
                    viewport.unproject(touchPos);
                    documentGame.handleTouchUp(touchPos.x, touchPos.y);
                    return true;
                }
                if (evidenceGapSession != null && evidenceGapSession.isBagOpen()) {
                    touchPos.set(screenX, screenY);
                    viewport.unproject(touchPos);
                    evidenceGapSession.touchUpEmbeddedBagOpen(touchPos.x, touchPos.y);
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
                updateCursorFromInput(gameX, gameY);
                currentTooltip = "";

                // Mini-game hover handling
                if (documentGame.isActive()) {
                    documentGame.handleHover(gameX, gameY);
                    return false;
                }

                if (textPanel.isVisible()) {
                    textPanel.handleHover(gameX, gameY);
                    return false;
                }

                actionBar.handleHover(gameX, gameY);
                awarenessMeter.handleHover(gameY);

                for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
                    hotspot.checkHover(gameX, gameY);
                    if (hotspot.isHovered()) {
                        currentTooltip = hotspot.getTooltip();
                    }
                }

                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (gameState.isGameOver() || gameState.isGameWon()) {
                    if (keycode == Input.Keys.ESCAPE) {
                        game.setScreen(new TitleScreen(game));
                    }
                    return true;
                }

                if (evidenceGapSession != null && evidenceGapSession.isCompletionLocked()) {
                    return true;
                }

                // Block keyboard when mini-game is active (except ESC to cancel)
                if (documentGame.isActive()) {
                    if (keycode == Input.Keys.ESCAPE && !documentGame.isCompleted()) {
                        // Cancel the mini-game without getting evidence
                        documentGame.cancel();
                    }
                    return true;
                }

                // ESC closes panel or opens pause menu
                if (keycode == Input.Keys.ESCAPE) {
                    if (evidenceGapSession != null && evidenceGapSession.handleEscape()) {
                        return true;
                    }
                    if (textPanel.isVisible()) {
                        textPanel.hide();
                        panelMode = PanelMode.NONE;
                        pendingMiniGame = null;
                    } else {
                        showPauseMenu();
                    }
                    return true;
                }

                // Don't allow navigation while panel is open
                if (textPanel.isVisible())
                    return true;

                // DEBUG: quick-jump to post-Tape-3-maze checkpoint.
                if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) {
                    debugJumpAfterTape2Maze();
                    return true;
                }

                switch (keycode) {
                    case Input.Keys.W:
                    case Input.Keys.UP:
                        navigateByDirection(Direction.NORTH);
                        break;
                    case Input.Keys.S:
                    case Input.Keys.DOWN:
                        navigateByDirection(Direction.SOUTH);
                        break;
                    case Input.Keys.A:
                    case Input.Keys.LEFT:
                        navigateByDirection(Direction.WEST);
                        break;
                    case Input.Keys.D:
                    case Input.Keys.RIGHT:
                        navigateByDirection(Direction.EAST);
                        break;
                    case Input.Keys.F8: // DEBUG: launch GameInventoryScreen (chapter 1)
                        returningFromMinigame = true;
                        game.setScreen(new GameInventoryScreen(game, 1, roomManager.getCurrentRoom().getId(),
                                () -> game.setScreen(GameScreen.this)));
                        break;
                    case Input.Keys.F10: // DEBUG: launch MetalDetectorScanScreen
                        returningFromMinigame = true;
                        game.setScreen(new MetalDetectorScanScreen(game, Room.RoomID.GROUNDSKEEPER_SHED,
                            () -> game.setScreen(GameScreen.this),
                            () -> game.setScreen(GameScreen.this)));
                        break;
                    case Input.Keys.F11: // DEBUG: launch TapeScreen directly
                        returningFromMinigame = true;
                        game.setScreen(new TapeScreen(game, Tape.TAPE_ARGUMENT, () -> game.setScreen(GameScreen.this)));
                        break;
                    case Input.Keys.F12: // DEBUG: launch Maze minigame directly
                        returningFromMinigame = true;
                        game.setScreen(new MazeMinigame(game, gameState, Tape.TAPE_MARGARET_INTERVIEW, () -> game.setScreen(GameScreen.this)));
                        break;
                    case Input.Keys.I:
                        handleActionBarClick("inventory");
                        break;
                    case Input.Keys.N:
                        handleActionBarClick("notebook");
                        break;
                    case Input.Keys.T:
                        handleActionBarClick("suspects");
                        break;
                    case Input.Keys.H:
                        showHistory();
                        break;
                    case Input.Keys.O:
                        showObjectives();
                        break;
                    case Input.Keys.F5:
                        showSaveMenu();
                        break;
                    case Input.Keys.F9:
                        showLoadMenu();
                        break;
                }
                return true;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                textPanel.scroll(amountY);
                return true;
            }
        });
    }

    // --- Navigation ---

    private void navigateWithFade(Runnable onMidpoint) {
        transitionFadingOut = true;
        transitionFadingIn = false;
        transitionAlpha = 0f;
        pendingTransition = onMidpoint;
    }

    private void navigateByDirection(Direction dir) {
        if (roomManager.getCurrentRoom().hasConnection(dir)) {
            handleNavigation(roomManager.getCurrentRoom().getConnection(dir));
        }
    }

    /** Darkness spotlight follows this point; update on mouse move and on touch so taps are not ignored. */
    private void updateCursorFromInput(float gameX, float gameY) {
        cursorGameX = gameX;
        cursorGameY = gameY;
        mouseStillTimer = 0f;
    }

    private boolean isCellarUnlocked() {
        // Cellar opens in Gap 6 and onward.
        return currentChapter >= 5;
    }

    private boolean isMargaretRoomUnlocked() {
        // Margaret's room opens in Gap 5 and onward.
        return currentChapter >= 4;
    }

    /**
     * Debug shortcut: jump to the state immediately after Tape 3 maze completion.
     * This starts Gap 3 inventory phase directly.
     */
    private void debugJumpAfterTape2Maze() {
        stopMetalDetectorBeepThread();
        metalDetectorModeActive = false;
        metalDetectorRoomReached = false;
        metalDetectorTargetRoom = null;
        // Skip the one-time detector demo when using this debug checkpoint jump.
        MetalDetectorScanScreen.metalDetectorDemoAlreadySeen = true;
        // Disable darkness mask for faster visual debugging after Ctrl jump.
        debugDisableLimitedVisibility = true;

        pendingInventoryStart = false;
        currentChapter = 3; // Gap 4
        minigameReturnCooldown = 0f;

        pendingMiniGame = null;
        pendingClimax = false;
        textPanel.hide();
        panelMode = PanelMode.NONE;

        seedDebugProgressThroughTape4();
        startInventoryPhase();
    }

    /**
     * Seed game progression as if the player genuinely cleared up through Tape 4.
     * Keeps debug jumps consistent with lock/unlock logic and room gates.
     */
    private void seedDebugProgressThroughTape4() {
        Tape[] cleared = {
                Tape.TAPE_ARGUMENT,
                Tape.TAPE_MARGARET_INTERVIEW,
                Tape.TAPE_MARCUS_INTERVIEW,
                Tape.TAPE_CHARLES_INTERVIEW
        };
        StringBuilder sink = new StringBuilder();
        for (Tape t : cleared) {
            gameState.forceCollectTape(t);
            gameState.forceWatchTape(t);
            mazePlayedTapes.add(t);
            // Apply normal tape-chain unlock side effects (codes + unlocks).
            revealCodeFromTape(t, sink);
        }
    }

    private void handleNavigation(Room.RoomID target) {
        // Act structure: Entity gates certain rooms
        if (target == Room.RoomID.CELLAR && !isCellarUnlocked()) {
            NarratorText.Mood mood = NarratorText.getMoodForAwareness(gameState.getAwareness());
            String narratorLine;
            switch (mood) {
                case HOPEFUL:
                    narratorLine = "\"I wouldn't bother, detective. Nothing down there but dust and old wine. Focus on the rooms up here.\"";
                    break;
                case CONFUSED:
                    narratorLine = "\"That door... it won't open. Strange. I don't remember it being locked. Perhaps try the other rooms first?\"";
                    break;
                case ANXIOUS:
                    narratorLine = "\"Don't go down there. Please. Something about the cellar makes my skin crawl. There's nothing for your investigation below.\"";
                    break;
                default:
                    narratorLine = "\"NO! Stay AWAY from the cellar! You don't understand what's down there! I mean... it's irrelevant. Completely irrelevant.\"";
                    break;
            }
            textPanel.showDialogue("The Narrator", "THE CELLAR\n\nThe door to the cellar won't budge. The handle is ice-cold " +
                    "to the touch, far colder than it should be. Something below is keeping this door sealed.\n\n" +
                    narratorLine + "\n\n" +
                    "[Continue investigating. Gather more evidence and testimony.]", new ArrayList<>());
            panelMode = PanelMode.TEXT;
            return;
        }
        if (target == Room.RoomID.MARGARET_ROOM && !isMargaretRoomUnlocked()) {
            NarratorText.Mood mood = NarratorText.getMoodForAwareness(gameState.getAwareness());
            String narratorLine;
            switch (mood) {
                case HOPEFUL:
                    narratorLine = "\"Margaret's room? She's a witness, not a suspect. Her room won't tell you anything useful.\"";
                    break;
                case CONFUSED:
                    narratorLine = "\"The door won't budge. That's odd. It was open earlier, wasn't it? Perhaps Margaret locked it.\"";
                    break;
                case ANXIOUS:
                    narratorLine = "\"Something is holding that door shut. Not a lock -- something else. Please, detective, leave it alone.\"";
                    break;
                default:
                    narratorLine = "\"THE DOOR IS SEALED! Can't you feel the cold? Whatever is behind that door, they don't -- I mean, there's no reason to go in there!\"";
                    break;
            }
            textPanel.showDialogue("The Narrator", "MARGARET'S ROOM\n\nThe door is sealed shut. Not locked -- sealed. " +
                    "The wood is unnaturally cold. Frost traces the edges of the frame despite the warm hallway.\n\n" +
                    narratorLine + "\n\n" +
                    "[Something holds this door shut. Perhaps deeper investigation will change that.]", new ArrayList<>());
            panelMode = PanelMode.TEXT;
            return;
        }

        if (transitionFadingOut || transitionFadingIn) return;
        final Room.RoomID navTarget = target;
        navigateWithFade(() -> {
            roomManager.navigateTo(navTarget);
            gameState.incrementVisit(navTarget);
            gameState.incrementCommandCount();
            gameState.addEvent("Moved to " + roomManager.getCurrentRoom().getName());

            String warning = awarenessSystem.addAwareness(1);

            if (gameState.isGameOver()) {
                showGameOver();
                return;
            }

            if (warning != null) {
                textPanel.showDialogue("The Narrator", narratorSystem.getWarning(), new ArrayList<>());
                panelMode = PanelMode.TEXT;
            } else {
                String narratorSlip = narratorSystem.maybeGetNarratorSlip();
                if (narratorSlip != null) {
                    textPanel.showDialogue("The Narrator", narratorSlip, new ArrayList<>());
                    panelMode = PanelMode.TEXT;
                    return;
                }
                String atmospheric = narratorSystem.maybeGetAtmosphericEvent();
                if (atmospheric != null) {
                    textPanel.showDialogue("The Narrator", atmospheric, new ArrayList<>());
                    panelMode = PanelMode.TEXT;
                }
            }
        });
    }

    // --- Examine ---

    private void handleExamine(String objectName) {
        // storage_cellar is a door — always navigate to the cellar
        if ("storage_cellar".equals(objectName)) {
            handleNavigation(Room.RoomID.CELLAR);
            return;
        }
        // Tape already collected — silently ignore, no awareness cost
        if ("kitchen_floor".equals(objectName) && gameState.hasTape(Tape.TAPE_MARGARET_INTERVIEW)) {
            return;
        }

        Room.RoomID currentRoomId = roomManager.getCurrentRoom().getId();

        // Margaret's room drawer/kit/shoes — handled by state machine, not
        // ExaminationSystem
        if (currentRoomId == Room.RoomID.MARGARET_ROOM) {
            String drawerResult = handleMargaretDrawer(objectName);
            if (drawerResult != null) {
                gameState.incrementCommandCount();
                gameState.addEvent("Examined " + RoomDescriptions.getObjectDisplayName(objectName) + " in "
                        + roomManager.getCurrentRoom().getName());
                if (!drawerResult.isEmpty()) {
                    String warning = null;
                    if ("kit".equals(objectName) || "shoes".equals(objectName)) {
                        warning = awarenessSystem.addAwareness(1);
                        if (gameState.isGameOver()) {
                            showGameOver();
                            return;
                        }
                    }
                    StringBuilder display = new StringBuilder();
                    display.append("Examining: ").append(RoomDescriptions.getObjectDisplayName(objectName))
                            .append("\n\n");
                    display.append(drawerResult);
                    if (warning != null)
                        display.append("\n\n--- ").append(narratorSystem.getWarning());
                    textPanel.showDialogue("Observation", display.toString(), new ArrayList<>());
                    panelMode = PanelMode.TEXT;
                }
                return;
            }
        }

        ExamResult result = examinationSystem.examine(currentRoomId, objectName);
        gameState.incrementCommandCount();
        gameState.addEvent("Examined " + RoomDescriptions.getObjectDisplayName(objectName) + " in "
                + roomManager.getCurrentRoom().getName());

        // +1 awareness per examination
        String warning = awarenessSystem.addAwareness(1);

        if (gameState.isGameOver()) {
            showGameOver();
            return;
        }

        // Check if this triggers a mini-game
        if (result.hasMiniGame()) {
            handleMiniGame(result, warning);
            return;
        }

        StringBuilder display = new StringBuilder();
        display.append("Examining: ").append(RoomDescriptions.getObjectDisplayName(objectName)).append("\n\n");
        display.append(narratorSystem.filterText(result.getText()));

        // Collect evidence/tape if found
        if (result.hasEvidence()) {
            boolean isNew = evidenceSystem.collect(result.getEvidence());
            if (isNew) {
                showNotification("Evidence found: " + result.getEvidence().getDisplayName());
                gameState.addEvent("Found evidence: " + result.getEvidence().getDisplayName());
                String codeAnnouncement = checkEvidenceForCode(result.getEvidence());
                if (codeAnnouncement != null) {
                    display.append(codeAnnouncement);
                }
                if (result.getEvidence() == Evidence.MUDDY_BOOTS) {
                    removeHotspot(Room.RoomID.GROUNDSKEEPER_SHED, "shelf");
                }
            }
        }
        if (result.hasTape()) {
            boolean isNew = evidenceSystem.collectTape(result.getTape());
            if (isNew) {
                showNotification("Tape found: " + result.getTape().getTitle());
                gameState.addEvent("Found tape: " + result.getTape().getTitle());
                if (result.getTape() == Tape.TAPE_MARGARET_INTERVIEW) {
                    removeHotspot(Room.RoomID.KITCHEN, "kitchen_floor");
                }
                if (result.getTape() == Tape.TAPE_MARGARET_ACCOUNT) {
                    removeHotspot(Room.RoomID.MARGARET_ROOM, "tape_recorder");
                }
                if (result.getTape() == Tape.TAPE_DANIEL_INTERVIEW) {
                    removeHotspot(Room.RoomID.GROUNDSKEEPER_SHED, "logbook");
                }
            }
        }
        // Wardrobe opened — swap hotspots in James's room
        if ("wardrobe".equals(objectName)
                && currentRoomId == Room.RoomID.JAMES_ROOM
                && gameState.getExamCount(Room.RoomID.JAMES_ROOM, "wardrobe") == 1) {
            removeHotspot(Room.RoomID.JAMES_ROOM, "wardrobe");
            roomManager.getRoom(Room.RoomID.JAMES_ROOM)
                    .addHotspot(new Hotspot("coat", "Examine: James's Coat", 879, 314, 53, 178));
        }

        if (warning != null) {
            display.append("\n\n--- ").append(narratorSystem.getWarning());
        }

        textPanel.showDialogue("Observation", display.toString(), new ArrayList<>());
        panelMode = PanelMode.TEXT;
    }

    private void handleMiniGame(ExamResult result, String warning) {
        switch (result.getMiniGame()) {
            case TORN_LETTER_RECONSTRUCTION:
                // Show intro text first, then start mini-game
                textPanel.showDialogue("The Narrator", narratorSystem.filterText(result.getText())
                        + "\n\n[Reconstruct the document to reveal its contents...]", new ArrayList<>());
                panelMode = PanelMode.TEXT;

                // Start the mini-game after the text panel is closed
                // We'll handle this by setting a pending mini-game state
                pendingMiniGame = result.getMiniGame();
                break;
            default:
                break;
        }
    }

    private void startPendingMiniGame() {
        if (pendingMiniGame == null)
            return;

        switch (pendingMiniGame) {
            case TORN_LETTER_RECONSTRUCTION:
                documentGame.startTornLetter(
                        // On complete: collect evidence
                        () -> {
                            boolean isNew = evidenceSystem.collect(Evidence.TORN_LETTER);
                            if (isNew) {
                                gameState.addEvent("Found evidence: " + Evidence.TORN_LETTER.getDisplayName());
                            }
                            textPanel.showDialogue("Observation",
                                    "Document reconstructed!\n\n\"...I have seen what James is doing... before the will is signed... you must...\"\n\nThe rest is ash. But someone knew. And they tried to warn him.\n\n[EVIDENCE FOUND: "
                                            + Evidence.TORN_LETTER.getDisplayName() + "]", new ArrayList<>());
                            panelMode = PanelMode.TEXT;
                        },
                        // On cancel: no evidence
                        () -> {
                            textPanel.showDialogue("Observation", "You set the fragments aside for now. Perhaps you'll return to them later.", new ArrayList<>());
                            panelMode = PanelMode.TEXT;
                        });
                break;
            default:
                break;
        }
        pendingMiniGame = null;
    }

    private String getTapeVoice(Tape tape) {
        switch (tape) {
            case TAPE_JAMES_INTERVIEW:   return "James Vance";
            case TAPE_MARGARET_INTERVIEW:
            case TAPE_MARGARET_ACCOUNT:  return "Margaret Vance";
            case TAPE_DANIEL_INTERVIEW:  return "Daniel the Groundskeeper";
            case TAPE_MARCUS_INTERVIEW:  return "Marcus Blackwood";
            case TAPE_CHARLES_INTERVIEW: return "Charles Webb";
            case TAPE_ARGUMENT:          return "Harold Vance";
            case TAPE_ARTHUR_DEATH:      return "Arthur Hollis";
            default:                     return "Police Recording";
        }
    }

    // --- Action Bar ---

    // ═══════════════════════════════════════════════════════════════════════════
    // NEW BETWEEN-TAPE SEQUENCE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Returns the most recently watched tape, or null if none. */
    private Tape getLastWatchedTape() {
        Tape last = null;
        for (Tape t : Tape.values()) {
            if (gameState.hasWatchedTape(t)) last = t;
        }
        return last;
    }

    /**
     * Launches the narrator spotlight mini-event for the given room.
     * The spotlight runs as a full-screen screen; on completion it returns here.
     */
    private void launchSpotlight(Room.RoomID roomId) {
        returningFromMinigame = true;
        final GameScreen self = this;
        game.setScreen(new NarratorSpotlightScreen(game, roomId, () -> {
            self.minigameReturnCooldown = MINIGAME_RETURN_COOLDOWN;
            game.setScreen(self);
        }));
    }

    /**
     * Starts the inventory collection phase for the current chapter.
     * Returns to this GameScreen when all combinations are done, then activates metal-detector mode.
     */
    private void startInventoryPhase() {
        returningFromMinigame = true;
        pendingInventoryStart = false;
        final GameScreen self = this;
        final int chapter = currentChapter;

        // After Margaret's personal account (chapter 6), skip the gap entirely —
        // show a narrator breakdown monologue that leads straight into Tape 8.
        if (chapter == 6) {
            String breakdown =
                "I — \n\n" +
                "That's the first time I've heard that. Margaret's account. I want to be clear about that. " +
                "That is the first time I have heard that recording.\n\n" +
                "I've been in this house for — I've known this family for a very long time and I — " +
                "I didn't know she had made a recording. I had no idea that tape existed until you found it.\n\n" +
                "She mentioned the sixteenth. She said — she described the evening of the sixteenth in detail. " +
                "The rain. The way the kitchen light was still on. The sound from the cellar at — \n\n" +
                "I remember the sixteenth.\n\n" +
                "I don't know why I said that.\n\n" +
                "What I mean is — it's a significant date. In the context of the case. Obviously. " +
                "Anyone who had read the files would — \n\n" +
                "She described the coat on the hook. The brown coat. She said it was still damp. " +
                "She said the buttons — \n\n" +
                "I know that coat.\n\n" +
                "I'm — I'm doing it again. I don't — I should not know that. I have been listening to these tapes " +
                "the same as you. Step by step. I haven't — I haven't been in this house. I haven't seen the coat. " +
                "I'm describing something I heard on a recording. That's all that is.\n\n" +
                "The cellar tape. There is one more tape. You know that.\n\n" +
                "I don't know what's on it.\n\n" +
                "I don't know what's on it.\n\n" +
                "Go.";
            textPanel.showDialogue("The Narrator", breakdown, new java.util.ArrayList<>());
            panelMode = PanelMode.NARRATOR_BREAKDOWN;
            return;
        }
        if (evidenceGapSession != null) {
            evidenceGapSession.dispose();
            evidenceGapSession = null;
        }
        evidenceGapSession = EvidenceGapSession.forEmbedded(game, chapter, () -> {
            self.minigameReturnCooldown = MINIGAME_RETURN_COOLDOWN;
            self.currentChapter++;
            Tape nextTape = tapeForChapter(self.currentChapter);
            self.metalDetectorTargetRoom = nextTape.getHiddenInRoom();
            self.metalDetectorModeActive = true;
            self.metalDetectorRoomReached = false;
            self.startMetalDetectorBeepThread();
            if (self.evidenceGapSession != null) {
                self.evidenceGapSession.dispose();
                self.evidenceGapSession = null;
            }
        });
    }

    /** Returns the tape the player should find for the given chapter. */
    private Tape tapeForChapter(int chapter) {
        Tape[] tapes = Tape.values();
        return tapes[chapter % tapes.length];
    }

    // Room adjacency graph — used to compute "distance" to target for beep rate.
    private static final java.util.Map<Room.RoomID, java.util.List<Room.RoomID>> ROOM_GRAPH;
    static {
        ROOM_GRAPH = new java.util.HashMap<>();
        java.util.List<Room.RoomID> entrance = java.util.Arrays.asList(
            Room.RoomID.STUDY, Room.RoomID.PARLOR, Room.RoomID.KITCHEN, Room.RoomID.GUEST_ROOMS);
        java.util.List<Room.RoomID> study    = java.util.Arrays.asList(Room.RoomID.ENTRANCE);
        java.util.List<Room.RoomID> parlor   = java.util.Arrays.asList(Room.RoomID.ENTRANCE);
        java.util.List<Room.RoomID> kitchen  = java.util.Arrays.asList(Room.RoomID.ENTRANCE, Room.RoomID.CELLAR,
            Room.RoomID.SERVANTS_QUARTERS);
        java.util.List<Room.RoomID> guests   = java.util.Arrays.asList(Room.RoomID.ENTRANCE,
            Room.RoomID.JAMES_ROOM, Room.RoomID.MARGARET_ROOM);
        java.util.List<Room.RoomID> james    = java.util.Arrays.asList(Room.RoomID.GUEST_ROOMS);
        java.util.List<Room.RoomID> margaret = java.util.Arrays.asList(Room.RoomID.GUEST_ROOMS);
        java.util.List<Room.RoomID> servants = java.util.Arrays.asList(Room.RoomID.KITCHEN,
            Room.RoomID.GROUNDSKEEPER_SHED);
        java.util.List<Room.RoomID> shed     = java.util.Arrays.asList(Room.RoomID.SERVANTS_QUARTERS);
        java.util.List<Room.RoomID> cellar   = java.util.Arrays.asList(Room.RoomID.KITCHEN);
        ROOM_GRAPH.put(Room.RoomID.ENTRANCE,          entrance);
        ROOM_GRAPH.put(Room.RoomID.STUDY,             study);
        ROOM_GRAPH.put(Room.RoomID.PARLOR,            parlor);
        ROOM_GRAPH.put(Room.RoomID.KITCHEN,           kitchen);
        ROOM_GRAPH.put(Room.RoomID.GUEST_ROOMS,       guests);
        ROOM_GRAPH.put(Room.RoomID.JAMES_ROOM,        james);
        ROOM_GRAPH.put(Room.RoomID.MARGARET_ROOM,     margaret);
        ROOM_GRAPH.put(Room.RoomID.SERVANTS_QUARTERS, servants);
        ROOM_GRAPH.put(Room.RoomID.GROUNDSKEEPER_SHED,shed);
        ROOM_GRAPH.put(Room.RoomID.CELLAR,            cellar);
    }

    /** BFS shortest path distance between two rooms (number of hops). */
    private int roomDistance(Room.RoomID from, Room.RoomID to) {
        if (from == to) return 0;
        java.util.Queue<Room.RoomID> queue = new java.util.LinkedList<>();
        java.util.Map<Room.RoomID, Integer> dist = new java.util.HashMap<>();
        queue.add(from); dist.put(from, 0);
        while (!queue.isEmpty()) {
            Room.RoomID cur = queue.poll();
            int d = dist.get(cur);
            java.util.List<Room.RoomID> neighbors = ROOM_GRAPH.get(cur);
            if (neighbors == null) continue;
            for (Room.RoomID n : neighbors) {
                if (!dist.containsKey(n)) {
                    dist.put(n, d + 1);
                    if (n == to) return d + 1;
                    queue.add(n);
                }
            }
        }
        return 10; // unreachable fallback
    }

    /** Starts the beep thread for the metal-detector navigation overlay. */
    private void startMetalDetectorBeepThread() {
        stopMetalDetectorBeepThread();
        mdBeepRunning = true;
        metalDetectorBeepThread = new Thread(() -> {
            long lastBeep = System.currentTimeMillis();
            while (mdBeepRunning) {
                long now = System.currentTimeMillis();
                if (now - lastBeep >= (long) mdBeepInterval) {
                    lastBeep = now;
                    Thread t = new Thread(() -> {
                        try {
                            javax.sound.sampled.AudioFormat fmt =
                                new javax.sound.sampled.AudioFormat(44100, 8, 1, true, false);
                            javax.sound.sampled.SourceDataLine line =
                                javax.sound.sampled.AudioSystem.getSourceDataLine(fmt);
                            line.open(fmt, 4096); line.start();
                            int samples = (int)(44100 * 60 / 1000.0);
                            byte[] buf = new byte[samples];
                            for (int i = 0; i < samples; i++) {
                                double decay = 1.0 - (double) i / samples;
                                double angle = 2.0 * Math.PI * i * 300 / 44100;
                                buf[i] = (byte)(Math.sin(angle) * 100 * decay);
                            }
                            line.write(buf, 0, buf.length); line.drain(); line.close();
                        } catch (Exception ignored) {}
                    });
                    t.setDaemon(true); t.start();
                }
                try { Thread.sleep(10); } catch (InterruptedException e) { break; }
            }
        });
        metalDetectorBeepThread.setDaemon(true);
        metalDetectorBeepThread.start();

        // Lazily load metal detector icon
        if (metalDetectorIconTex == null
                && Gdx.files.internal("minigames/metal_detector.png").exists()) {
            metalDetectorIconTex = new Texture(Gdx.files.internal("minigames/metal_detector.png"));
        }
        showNotification("Metal detector active — navigate to find the tape. Listen for the beeps.");
    }

    private void stopMetalDetectorBeepThread() {
        mdBeepRunning = false;
        if (metalDetectorBeepThread != null) { metalDetectorBeepThread.interrupt(); metalDetectorBeepThread = null; }
    }

    /**
     * Called every frame while metalDetectorModeActive is true.
     * Updates the beep interval based on room distance to the target room.
     * When the player enters the target room, shows the "Scan for tape" prompt.
     */
    private void updateMetalDetectorNavigation(float delta) {
        Room.RoomID cur = roomManager.getCurrentRoom().getId();

        // Update beep interval based on distance
        int hops = roomDistance(cur, metalDetectorTargetRoom);
        // hops 0 → fastest beep, hops 5+ → slowest
        float t = Math.min(1f, hops / 5f);
        mdBeepInterval = 100f + t * 3900f; // 100ms at same room, 4000ms far away

        if (cur == metalDetectorTargetRoom && !metalDetectorRoomReached) {
            metalDetectorRoomReached = true;
            showNotification("Tape nearby — click the detector to scan!");
        }
    }

    /**
     * Launches the in-room MetalDetectorScanScreen for the current target room.
     */
    private void launchMetalDetectorScan() {
        stopMetalDetectorBeepThread();
        metalDetectorModeActive = false;
        returningFromMinigame = true;
        final GameScreen self = this;
        final Room.RoomID targetRoom = metalDetectorTargetRoom;
        final int chapter = currentChapter;
        game.setScreen(new MetalDetectorScanScreen(game, targetRoom,
            // onFound
            () -> {
                // Tape located — now play it via TapeScreen
                Tape tape = tapeForChapter(chapter);
                game.setScreen(new TapeScreen(game, tape, () -> {
                    // After tape → return to GameScreen; maze will launch from panel close
                    self.minigameReturnCooldown = MINIGAME_RETURN_COOLDOWN;
                    // Collect the tape in game state
                    boolean isNew = self.evidenceSystem.collectTape(tape);
                    if (isNew) {
                        self.gameState.addEvent("Found tape: " + tape.getTitle());
                        self.evidenceSystem.watchTape(tape);
                        self.gameState.addEvent("Watched tape: " + tape.getTitle());
                    }
                    // Keep currentChapter until evidence inventory finishes — it selects which gap's items to show.
                    // Launch maze (preceded by demo on first tape)
                    self.mazePlayedTapes.add(tape);
                    Runnable launchMaze = () -> game.setScreen(new MazeMinigame(game, self.gameState, tape, () -> {
                        self.minigameReturnCooldown = MINIGAME_RETURN_COOLDOWN;
                        self.startInventoryPhase(); // pre-build textures before screen switch
                        game.setScreen(self);
                    }));
                    if (!MetalDetectorScanScreen.metalDetectorDemoAlreadySeen) {
                        game.setScreen(MetalDetectorScanScreen.forDemo(game, launchMaze));
                    } else {
                        launchMaze.run();
                    }
                }));
            },
            // onReturn (miss — return to navigating)
            () -> {
                self.minigameReturnCooldown = MINIGAME_RETURN_COOLDOWN;
                self.metalDetectorModeActive = true;
                self.metalDetectorRoomReached = false; // allow trying again
                self.startMetalDetectorBeepThread();
                game.setScreen(self);
            }
        ));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // END NEW HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private void handleActionBarClick(String action) {
        switch (action) {
            case "inventory":
                showInventory();
                break;

        }
    }

    private void showInventory() {
        String text = evidenceSystem.getInventoryText();
        List<TextButton> buttons = new ArrayList<>();

        // Add PLAY buttons for unwatched, unlocked tapes
        for (Tape t : gameState.getCollectedTapes()) {
            if (!gameState.hasWatchedTape(t) && isTapeUnlocked(t)) {
                buttons.add(new TextButton("PLAY: " + t.getTitle(), 0, 0, 220, 35, "play_tape_" + t.name()));
            }
        }

        if (buttons.isEmpty()) {
            textPanel.show(text);
        } else {
            textPanel.show(text, buttons);
        }
        panelMode = PanelMode.INVENTORY;
    }

    private void showNotebook() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DETECTIVE'S NOTEBOOK ===\n\n");

        // 1. Status — compact single line
        sb.append("Awareness: ").append(gameState.getAwareness()).append("/").append(GameState.MAX_AWARENESS);
        sb.append(" (").append(awarenessSystem.getLevelName()).append(")  |  ");
        sb.append("Evidence: ").append(gameState.getCollectedEvidence().size()).append("/10  |  ");
        sb.append("Tapes: ").append(gameState.getWatchedTapes().size()).append(" watched / ")
                .append(gameState.getCollectedTapes().size()).append(" collected\n\n");

        // 2. Active Investigation — evidence gap + accusation status
        sb.append("=== ACTIVE INVESTIGATION ===\n\n");
        sb.append("James: ").append(evidenceSystem.getJamesEvidenceCount()).append("/6  |  ");
        sb.append("Daniel: ").append(evidenceSystem.getDanielEvidenceCount()).append("/4\n");
        if (evidenceSystem.canAccuseJamesAndDaniel()) {
            sb.append("[Ready to accuse — open ACCUSE panel.]\n");
        } else {
            sb.append("[Need James >= 3 and Daniel >= 2 to accuse.]\n");
        }
        sb.append("\n");

        // 3. Suspect Cooperation
        sb.append("=== SUSPECT COOPERATION ===\n\n");
        for (Suspect s : Suspect.values()) {
            sb.append(s.getDisplayName()).append(": ").append(gameState.getCooperation(s)).append("%\n");
        }
        sb.append("\n");

        // 4. Physical Contradictions
        boolean hasPhysical = false;
        boolean hasNarrator = false;
        for (Contradiction c : gameState.getDiscoveredContradictions()) {
            if (c.isNarratorContradiction())
                hasNarrator = true;
            else
                hasPhysical = true;
        }

        if (hasPhysical) {
            sb.append("=== PHYSICAL CONTRADICTIONS ===\n\n");
            for (Contradiction c : gameState.getDiscoveredContradictions()) {
                if (!c.isNarratorContradiction()) {
                    sb.append("* ").append(c.name().replace('_', ' ')).append("\n");
                    sb.append("  ").append(c.getDescription()).append("\n\n");
                }
            }
        }

        // 5. Narrator Contradictions
        if (hasNarrator) {
            sb.append("=== NARRATOR CONTRADICTIONS ===\n\n");
            int narratorCount = 0;
            for (Contradiction c : gameState.getDiscoveredContradictions()) {
                if (c.isNarratorContradiction()) {
                    sb.append("* ").append(c.name().replace('_', ' ')).append("\n");
                    sb.append("  ").append(c.getDescription()).append("\n\n");
                    narratorCount++;
                }
            }
            if (narratorCount >= 3) {
                sb.append(
                        "[META-INSIGHT: The narrator is actively lying to you. Everything he says must be questioned. He's not guiding your investigation -- he's shaping it.]\n\n");
            }
        }

        // 6. Entity Anomalies
        if (!gameState.getDiscoveredAnomalies().isEmpty()) {
            sb.append("=== ENTITY ANOMALIES (").append(gameState.getAnomalyCount()).append("/7) ===\n\n");
            for (com.dsa.game.state.EntityAnomaly a : gameState.getDiscoveredAnomalies()) {
                sb.append("* ").append(a.getDisplayName()).append("\n");
                sb.append("  ").append(a.getDescription()).append("\n\n");
            }
        }

        // 7. Narrator Inconsistencies (meta/low priority)
        if (!gameState.getNarratorDistortions().isEmpty()) {
            sb.append("=== NARRATOR INCONSISTENCIES ===\n\n");
            for (String distortion : gameState.getNarratorDistortions()) {
                sb.append("* \"").append(distortion).append("\"\n\n");
            }
        }

        // 8. Achievements
        sb.append(achievementSystem.getAchievementsText());

        textPanel.show(sb.toString());
        panelMode = PanelMode.NOTEBOOK;
    }

    private void showSuspectList() {
        List<TextButton> buttons = new ArrayList<>();
        for (Suspect s : Suspect.values()) {
            int coop = gameState.getCooperation(s);
            String label = s.getDisplayName() + "  [" + coop + "%]";
            buttons.add(new TextButton(label, 0, 0, 260, 35, "interview_" + s.name()));
        }
        textPanel.showButtons("=== SUSPECTS ===\n\nSelect a suspect to interview.", buttons);
        panelMode = PanelMode.SUSPECT_LIST;
    }

    private void showHint() {
        textPanel.show("=== HINT ===\n\n" + hintSystem.getHint());
        panelMode = PanelMode.TEXT;
    }

    private void showAccusation() {
        if (!evidenceSystem.canAccuseJamesAndDaniel()) {
            textPanel.show("=== ACCUSATION ===\n\n" +
                    "Not enough evidence yet.\n\n" +
                    "James: " + evidenceSystem.getJamesEvidenceCount() + "/6 (need 3)  |  " +
                    "Daniel: " + evidenceSystem.getDanielEvidenceCount() + "/4 (need 2)\n\n" +
                    "Keep investigating — examine objects, collect tapes, interview suspects.");
            panelMode = PanelMode.TEXT;
            return;
        }

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("James & Daniel (together)", 0, 0, 200, 35, "accuse_james_daniel"));
        buttons.add(new TextButton("James alone", 0, 0, 200, 35, "accuse_james"));
        buttons.add(new TextButton("Margaret", 0, 0, 200, 35, "accuse_margaret"));
        buttons.add(new TextButton("Daniel alone", 0, 0, 200, 35, "accuse_daniel"));
        buttons.add(new TextButton("Marcus Blackwood", 0, 0, 200, 35, "accuse_marcus"));
        buttons.add(new TextButton("Charles Webb", 0, 0, 200, 35, "accuse_charles"));

        // Post-climax moral choices (Feature 6)
        if (gameState.isClimaxTriggered()) {
            buttons.add(new TextButton("Seal the Wall", 0, 0, 200, 35, "ending_seal"));
            buttons.add(new TextButton("Destroy the Tapes", 0, 0, 200, 35, "ending_destroy"));
            buttons.add(new TextButton("Escape the Manor", 0, 0, 200, 35, "ending_escape"));
        }

        buttons.add(new TextButton("Leave the Manor", 0, 0, 200, 35, "leave_manor"));

        String header = "=== WHO KILLED HAROLD VANCE? ===\n\n" +
                "Evidence against James: " + evidenceSystem.getJamesEvidenceCount() + "/6  |  " +
                "Evidence against Daniel: " + evidenceSystem.getDanielEvidenceCount() + "/4\n\n" +
                "Select your accusation carefully.";
        if (gameState.isClimaxTriggered()) {
            header += "\n\nOr... choose what to do about the Entity.";
        }
        textPanel.showButtons(header, buttons);
        panelMode = PanelMode.ACCUSE_SELECT;
    }

    // --- Panel Actions ---

    private void handlePanelAction(String action) {
        if ("next_page".equals(action)) {
            textPanel.nextPage();
            if (!textPanel.isVisible()) {
                handlePanelAction("close");
            }
            return;
        }

        if ("close".equals(action) || "panel_consumed".equals(action)) {
            if ("close".equals(action)) {
                // Narrator breakdown → launch Tape 8 directly (no metal detector)
                if (panelMode == PanelMode.NARRATOR_BREAKDOWN) {
                    panelMode = PanelMode.NONE;
                    final GameScreen self = this;
                    currentChapter = 7;
                    Tape tape8 = Tape.TAPE_ARTHUR_DEATH;
                    game.setScreen(new TapeScreen(game, tape8, () -> {
                        self.minigameReturnCooldown = MINIGAME_RETURN_COOLDOWN;
                        boolean isNew = self.evidenceSystem.collectTape(tape8);
                        if (isNew) {
                            self.gameState.addEvent("Found tape: " + tape8.getTitle());
                            self.evidenceSystem.watchTape(tape8);
                            self.gameState.addEvent("Watched tape: " + tape8.getTitle());
                        }
                        self.mazePlayedTapes.add(tape8);
                        game.setScreen(new MazeMinigame(game, self.gameState, tape8, () -> {
                            self.minigameReturnCooldown = MINIGAME_RETURN_COOLDOWN;
                            game.setScreen(self);
                        }));
                    }));
                    return;
                }
                // Climax intercept: show climax text instead of closing
                if (pendingClimax) {
                    pendingClimax = false;
                    gameState.setClimaxTriggered(true);
                    textPanel.showDialogue("The Narrator", ClimaxContent.getTape8Climax(gameState.getAnomalyCount()), new ArrayList<>());
                    panelMode = PanelMode.TEXT;
                    return;
                }
                // Escape intercept: after climax text is closed, launch escape sequence
                if (gameState.isClimaxTriggered() && panelMode == PanelMode.TEXT) {
                    game.setScreen(new EscapeScreen(game));
                    return;
                }
                // After tape playback closes → launch maze, then start next inventory chapter
                if (panelMode == PanelMode.TAPE_PLAY) {
                    // Determine which tape was just watched
                    Tape lastWatched = getLastWatchedTape();
                    if (lastWatched != null && !mazePlayedTapes.contains(lastWatched)) {
                        mazePlayedTapes.add(lastWatched);
                        returningFromMinigame = true;
                        final GameScreen self = this;
                        game.setScreen(new MazeMinigame(game, gameState, lastWatched, () -> {
                            self.minigameReturnCooldown = MINIGAME_RETURN_COOLDOWN;
                            self.startInventoryPhase(); // pre-build textures before screen switch
                            game.setScreen(self);
                        }));
                        return;
                    }
                }

                // Mini-game intercept: start mini-game after intro text
                if (pendingMiniGame != null) {
                    startPendingMiniGame();
                    return;
                }
                if (interviewSystem.isInterviewActive() && panelMode == PanelMode.INTERVIEW) {
                    interviewSystem.endInterview();
                }
                panelMode = PanelMode.NONE;
            }
            return;
        }

        if ("start_metal_scan".equals(action)) {
            launchMetalDetectorScan();
            return;
        }

        // Play tape
        if (action.startsWith("play_tape_")) {
            String tapeName = action.substring("play_tape_".length());
            try {
                Tape tape = Tape.valueOf(tapeName);
                playTape(tape);
            } catch (IllegalArgumentException ignored) {
            }
            return;
        }

        // Start interview with a suspect
        if (action.startsWith("interview_")) {
            String suspectName = action.substring("interview_".length());
            try {
                Suspect suspect = Suspect.valueOf(suspectName);
                startInterview(suspect);
            } catch (IllegalArgumentException ignored) {
            }
            return;
        }

        // Ask topic during interview
        if (action.startsWith("topic_")) {
            String topic = action.substring("topic_".length());
            handleAskTopic(topic);
            return;
        }

        // Show evidence during interview
        if (action.startsWith("evidence_")) {
            String evidenceName = action.substring("evidence_".length());
            try {
                Evidence evidence = Evidence.valueOf(evidenceName);
                handleShowEvidence(evidence);
            } catch (IllegalArgumentException ignored) {
            }
            return;
        }

        // Contradiction buttons
        if ("contra_weapon".equals(action)) {
            handleWeaponContradiction();
            return;
        }
        if ("contra_body".equals(action)) {
            handleBodyContradiction();
            return;
        }

        // Confrontation
        if (action.startsWith("confront_")) {
            String suspectName = action.substring("confront_".length());
            try {
                Suspect suspect = Suspect.valueOf(suspectName);
                handleConfrontation(suspect);
            } catch (IllegalArgumentException ignored) {
            }
            return;
        }

        // Back to interview topics
        if ("back_to_topics".equals(action)) {
            if (interviewSystem.isInterviewActive()) {
                showInterviewTopics(interviewSystem.getCurrentSuspect());
            }
            return;
        }

        // Show evidence list during interview
        if ("show_evidence_list".equals(action)) {
            showEvidenceForInterview();
            return;
        }

        // End interview — show channeling end text
        if ("end_interview".equals(action)) {
            interviewSystem.endInterview();
            String endText = narratorSystem.getChannelingEnd();
            textPanel.showDialogue("The Narrator", endText, new ArrayList<>());
            panelMode = PanelMode.TEXT;
            return;
        }

        // Moral endgame endings
        if (action.startsWith("ending_")) {
            handleMoralEnding(action);
            return;
        }

        // Accusation
        if (action.startsWith("accuse_")) {
            handleAccusation(action);
            return;
        }

        // Pause menu actions
        if ("resume".equals(action)) {
            textPanel.hide();
            panelMode = PanelMode.NONE;
            return;
        }
        if ("pause_save".equals(action)) {
            showSaveMenu();
            return;
        }
        if ("pause_load".equals(action)) {
            showLoadMenu();
            return;
        }
        if ("pause_settings".equals(action)) {
            showSettings();
            return;
        }
        if ("leave_manor".equals(action)) {
            handleLeaveManor();
            return;
        }
        if ("quit_to_menu".equals(action)) {
            game.setScreen(new TitleScreen(game));
            return;
        }

        // Settings actions
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
            showSettings();
            return;
        }
        if ("toggle_narrator_filter".equals(action)) {
            narratorSystem.setFilterEnabled(!narratorSystem.isFilterEnabled());
            showSettings();
            return;
        }
        if ("settings_back".equals(action)) {
            showPauseMenu();
            return;
        }

        // Save to slot
        if (action.startsWith("save_")) {
            String slotName = action.substring("save_".length());
            saveLoadSystem.save(gameState, roomManager.getCurrentRoom().getId(), slotName);
            textPanel.show("Game saved to " + slotName + ".");
            panelMode = PanelMode.TEXT;
            return;
        }

        // Load from slot
        if (action.startsWith("load_")) {
            String slotName = action.substring("load_".length());
            Room.RoomID roomId = saveLoadSystem.load(gameState, slotName);
            if (roomId != null) {
                roomManager.navigateTo(roomId);
                pendingClimax = false;
                textPanel.show("Game loaded from " + slotName + ".");
                panelMode = PanelMode.TEXT;
            } else {
                textPanel.show("Failed to load save file.");
                panelMode = PanelMode.TEXT;
            }
            return;
        }

        // Delete save
        if (action.startsWith("delete_")) {
            String slotName = action.substring("delete_".length());
            saveLoadSystem.deleteSave(slotName);
            showLoadMenu();
            return;
        }
    }

    private void showNotification(String msg) {
        // notifications disabled
    }

    private void removeHotspot(Room.RoomID roomId, String objectName) {
        roomManager.getRoom(roomId).getHotspots().removeIf(
                h -> objectName.equals(h.getObjectName()));
    }

    private void removeMargaretHotspot(String name) {
        removeHotspot(Room.RoomID.MARGARET_ROOM, name);
    }

    // kit hotspot covers the top-drawer interior area
    private void addMargaretKitHotspot() {
        roomManager.getRoom(Room.RoomID.MARGARET_ROOM).getHotspots().add(0,
                new Hotspot("kit", "Take: Tape Repair Kit", 264, 193, 115, 50));
    }

    // shoes hotspot covers the bottom-drawer interior area
    private void addMargaretShoesHotspot() {
        roomManager.getRoom(Room.RoomID.MARGARET_ROOM).getHotspots().add(0,
                new Hotspot("shoes", "Examine: Stained Shoes", 264, 113, 124, 70));
    }

    /**
     * Handles all Margaret's room drawer, kit, and shoes interactions.
     * Returns non-null if handled (empty string = handled silently, non-empty =
     * show text).
     */
    private String handleMargaretDrawer(String objectName) {
        boolean topOpen = gameState.isMargaretTopOpen();
        boolean botOpen = gameState.isMargaretBotOpen();
        boolean kitExamined = gameState.getExamCount(Room.RoomID.MARGARET_ROOM, "kit") > 0;
        boolean shoesTaken = gameState.isMargaretShoesExamined();
        boolean canBothOpen = kitExamined && shoesTaken;

        switch (objectName) {
            case "top_drawer":
                if (topOpen) {
                    gameState.setMargaretTopOpen(false);
                    removeMargaretHotspot("kit");
                    return "";
                } else if (botOpen && !canBothOpen) {
                    return "The bottom drawer is still open. Close it first.";
                } else {
                    gameState.setMargaretTopOpen(true);
                    if (!kitExamined) {
                        removeMargaretHotspot("top_drawer");
                        addMargaretKitHotspot();
                    }
                    return "";
                }

            case "bottom_drawer":
                if (botOpen) {
                    gameState.setMargaretBotOpen(false);
                    removeMargaretHotspot("shoes");
                    return "";
                } else if (topOpen && !canBothOpen) {
                    return "The top drawer is still open. Close it first.";
                } else {
                    gameState.setMargaretBotOpen(true);
                    if (!shoesTaken) {
                        removeMargaretHotspot("bottom_drawer");
                        addMargaretShoesHotspot();
                    }
                    return "";
                }

            case "kit":
                removeMargaretHotspot("kit");
                roomManager.getRoom(Room.RoomID.MARGARET_ROOM).getHotspots().add(
                    new Hotspot("top_drawer", "Examine: Top Drawer", 241, 207, 90, 49));
                gameState.addEvent("Examined tape splicing tools in Margaret's room");
                return "A set of tape splicing tools -- scissors, adhesive strips, a manual splicer. Whoever owned these knew how to handle recording equipment.";

            case "shoes":
                gameState.setMargaretShoesExamined(true);
                removeMargaretHotspot("shoes");
                roomManager.getRoom(Room.RoomID.MARGARET_ROOM).getHotspots().add(
                    new Hotspot("bottom_drawer", "Examine: Bottom Drawer", 244, 121, 86, 85));
                gameState.addEvent("Examined stained shoes in Margaret's room");
                return "You take a closer look at the stains. Too dark for wine. Too deliberate. Someone wore these the night of the murder and tried to hide them here.";

            default:
                return null;
        }
    }

    // --- Tape Progression Helpers ---

    private boolean isTapeUnlocked(Tape tape) {
        return gameState.isUnlockedTape(tape);
    }


    /**
     * Called when new evidence is collected. Returns a narrator announcement
     * string, or null.
     */
    private String checkEvidenceForCode(Evidence evidence) {
        if (evidence == Evidence.WILL_COPY) {
            gameState.learnCode("ESTATE-42");
            boolean unlocked = gameState.unlockTape(Tape.TAPE_JAMES_INTERVIEW);
            if (unlocked) {
                return "\n\n[The documents contain a combination. A tape recorder case nearby snaps open.]";
            }
        }
        return null;
    }

    /**
     * Called after evidenceSystem.watchTape() to unlock the next tape in the chain.
     */
    private void revealCodeFromTape(Tape tape, StringBuilder sb) {
        switch (tape) {
            case TAPE_ARGUMENT:
                gameState.learnCode("HEIR-CHANGE");
                if (gameState.unlockTape(Tape.TAPE_MARGARET_INTERVIEW)) {
                    sb.append("\n\n[A name was spoken. Something clicks open in the kitchen.]");
                }
                break;
            case TAPE_MARGARET_INTERVIEW:
                gameState.learnCode("GUEST-721");
                if (gameState.unlockTape(Tape.TAPE_MARCUS_INTERVIEW)) {
                    sb.append(
                            "\n\n[Another name. Another door that was locked is locked no longer -- somewhere in the parlor.]");
                }
                break;
            case TAPE_MARCUS_INTERVIEW:
                gameState.learnCode("WINDOW-11");
                if (gameState.unlockTape(Tape.TAPE_CHARLES_INTERVIEW)) {
                    sb.append("\n\n[Someone was watching that night. A case elsewhere in the parlor yields.]");
                }
                break;
            case TAPE_CHARLES_INTERVIEW: {
                gameState.learnCode("LOG-1115");
                boolean unlockedJames = gameState.unlockTape(Tape.TAPE_JAMES_INTERVIEW);
                boolean unlockedDaniel = gameState.unlockTape(Tape.TAPE_DANIEL_INTERVIEW);
                if (unlockedJames || unlockedDaniel) {
                    sb.append(
                            "\n\n[Two people were placed at the scene. Two recordings, somewhere in the manor, are now within reach.]");
                }
                break;
            }
            case TAPE_JAMES_INTERVIEW:
                if (gameState.hasWatchedTape(Tape.TAPE_DANIEL_INTERVIEW)) {
                    boolean unlocked = gameState.unlockTape(Tape.TAPE_MARGARET_ACCOUNT);
                    if (unlocked) {
                        sb.append(
                                "\n\n[You've heard from both James and Daniel. Margaret's personal account tape is now accessible.]");
                    }
                }
                break;
            case TAPE_DANIEL_INTERVIEW:
                if (gameState.hasWatchedTape(Tape.TAPE_JAMES_INTERVIEW)) {
                    boolean unlocked = gameState.unlockTape(Tape.TAPE_MARGARET_ACCOUNT);
                    if (unlocked) {
                        sb.append(
                                "\n\n[You've heard from both James and Daniel. Margaret's personal account tape is now accessible.]");
                    }
                }
                break;
            case TAPE_MARGARET_ACCOUNT:
                gameState.learnCode("CELLAR-WARNING");
                if (gameState.unlockTape(Tape.TAPE_ARTHUR_DEATH)) {
                    sb.append("\n\n[Something shifts in the cellar. A recording that was sealed away is no longer.]");
                }
                break;
            default:
                break;
        }
    }

    private void showLockedTapeMessage(Tape tape) {
        String hint;
        switch (tape) {
            case TAPE_MARGARET_INTERVIEW:
                hint = "Harold's own recording names his heir. That name is the key.";
                break;
            case TAPE_MARCUS_INTERVIEW:
                hint = "Margaret Vance's interview names another guest who was at the manor that night.";
                break;
            case TAPE_CHARLES_INTERVIEW:
                hint = "Marcus Blackwood noticed something as he left. Listen to his interview.";
                break;
            case TAPE_JAMES_INTERVIEW:
                hint = "Charles Webb's interview places a suspect at the study door. His account unlocks this.";
                break;
            case TAPE_DANIEL_INTERVIEW:
                hint = "Charles Webb named two suspects. His interview unlocks both.";
                break;
            case TAPE_MARGARET_ACCOUNT:
                hint = "Hear what both James and Daniel have to say first. Then Margaret's full account will speak for itself.";
                break;
            default:
                hint = "Keep investigating.";
        }
        textPanel.showDialogue("The Narrator", "LOCKED TAPE: " + tape.getTitle()
                + "\n\nThe tape recorder is sealed in a protective case. Arthur locked these before he disappeared.\n\n"
                + hint, new ArrayList<>());
        panelMode = PanelMode.TEXT;
    }

    // --- Tape Playing ---

    private void playTape(Tape tape) {
        // Gate 1: Tape code-locked
        if (!isTapeUnlocked(tape)) {
            showLockedTapeMessage(tape);
            return;
        }

        // Gate 2: Tape 8 — must have learned CELLAR-WARNING (watched Tape 7)
        if (tape == Tape.TAPE_ARTHUR_DEATH && !gameState.hasLearnedCode("CELLAR-WARNING")) {
            textPanel.showDialogue("The Narrator",
                    "SEQUENCE INCOMPLETE\n\nSomething holds you back. There is another recording you must hear first.\n\n[Find and watch Margaret's personal account.]", new ArrayList<>());
            panelMode = PanelMode.TEXT;
            return;
        }

        // Gate 3: Tape 8 — 3 contradictions required
        if (tape == Tape.TAPE_ARTHUR_DEATH && gameState.getDiscoveredContradictions().size() < 3) {
            int remaining = 3 - gameState.getDiscoveredContradictions().size();
            textPanel.showDialogue("The Narrator", "SEQUENCE INCOMPLETE\n\nThe tape is ready. But " + remaining + " contradiction"
                    + (remaining > 1 ? "s remain" : " remains")
                    + " unresolved in this case. The full truth requires more investigation.\n\n[Present evidence during interviews to uncover contradictions.]", new ArrayList<>());
            panelMode = PanelMode.TEXT;
            return;
        }

        evidenceSystem.watchTape(tape);
        gameState.incrementCommandCount();
        gameState.addEvent("Watched tape: " + tape.getTitle());

        // Variable awareness cost: +5 for climax tape, +4 for others
        int awarenessCost = (tape == Tape.TAPE_ARTHUR_DEATH)
                ? ClimaxContent.TAPE_8_AWARENESS_COST
                : ClimaxContent.STANDARD_TAPE_AWARENESS_COST;
        String warning = awarenessSystem.addAwareness(awarenessCost);

        StringBuilder sb = new StringBuilder();

        // Tape 8 (The Opening) has a special prefix about its unknown origin
        if (tape == Tape.TAPE_ARTHUR_DEATH) {
            sb.append(ClimaxContent.TAPE_8_CELLAR_PREFIX);
        }

        sb.append(TapeContent.getTranscript(tape));

        if (gameState.isGameOver()) {
            showGameOver();
            return;
        }

        if (warning != null) {
            sb.append("\n\n--- ").append(narratorSystem.getWarning());
        }

        // Reveal next unlock in chain
        revealCodeFromTape(tape, sb);

        // Tape 8 (The Opening) triggers the climax sequence - it's Arthur's death
        // recording found in the cellar
        if (tape == Tape.TAPE_ARTHUR_DEATH && !gameState.isClimaxTriggered()) {
            pendingClimax = true;
        }

        textPanel.showDialogue(getTapeVoice(tape), sb.toString(), new ArrayList<>());
        panelMode = PanelMode.TAPE_PLAY;

        // After any tape, queue the maze minigame (once per tape)
        // (Tapes are now found via the metal-detector flow, so all get the maze.)
        // The maze launches from handlePanelAction → close after TAPE_PLAY.
    }

    // --- Interview ---

    private void startInterview(Suspect suspect) {
        boolean isFirstInterview = gameState.getInterviewCount() == 0;
        String greeting = interviewSystem.startInterview(suspect);
        gameState.incrementCommandCount();
        gameState.incrementInterviewCount();
        gameState.addEvent("Started interview with " + suspect.getDisplayName());

        // +1 awareness for starting interview
        String warning = awarenessSystem.addAwareness(1);

        if (gameState.isGameOver()) {
            showGameOver();
            return;
        }

        StringBuilder sb = new StringBuilder();
        // Channeling intro instead of normal narrator filter
        sb.append(narratorSystem.getChannelingIntro(isFirstInterview));
        sb.append("\n\n");
        sb.append(greeting);
        if (warning != null) {
            sb.append("\n\n--- ").append(narratorSystem.getWarning());
        }

        // Show greeting then transition to topics
        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Ask Questions", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("Show Evidence", 0, 0, 200, 35, "show_evidence_list"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));

        textPanel.showDialogue(suspect.getDisplayName(), sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    private void showInterviewTopics(Suspect suspect) {
        List<String> topics = interviewSystem.getAvailableTopics();
        List<TextButton> buttons = new ArrayList<>();

        for (String topic : topics) {
            String label = SuspectDialogue.getTopicDisplayName(topic);
            if (gameState.hasAskedTopic(suspect, topic)) {
                label += " [asked]";
            }
            buttons.add(new TextButton(label, 0, 0, 200, 35, "topic_" + topic));
        }

        // Contradiction buttons
        if (gameState.hasEvidence(Evidence.LETTER_OPENER) && !gameState.hasContradiction(Contradiction.WEAPON)) {
            buttons.add(new TextButton("Challenge: Weapon", 0, 0, 200, 35, "contra_weapon"));
        }
        if (gameState.hasEvidence(Evidence.BLOODSTAINED_CUFF)
                && !gameState.hasContradiction(Contradiction.BODY_POSITION)) {
            buttons.add(new TextButton("Challenge: Body Position", 0, 0, 200, 35, "contra_body"));
        }

        // Confrontation button based on evidence thresholds
        if (!gameState.hasConfronted(suspect)) {
            boolean canConfront = false;
            switch (suspect) {
                case JAMES:
                    canConfront = evidenceSystem.getJamesEvidenceCount() >= 3;
                    break;
                case DANIEL:
                    canConfront = evidenceSystem.getDanielEvidenceCount() >= 2;
                    break;
                case MARGARET:
                    canConfront = gameState.hasEvidence(Evidence.TORN_LETTER)
                            && gameState.hasEvidence(Evidence.WILL_COPY);
                    break;
                case MARCUS:
                    canConfront = gameState.hasEvidence(Evidence.FINANCIAL_RECORDS)
                            || gameState.hasEvidence(Evidence.WILL_COPY);
                    break;
                case CHARLES:
                    canConfront = gameState.hasEvidence(Evidence.FINANCIAL_RECORDS)
                            && gameState.hasEvidence(Evidence.TORN_LETTER);
                    break;
            }
            if (canConfront) {
                buttons.add(new TextButton("CONFRONT", 0, 0, 200, 35, "confront_" + suspect.name()));
            }
        }

        buttons.add(new TextButton("Show Evidence", 0, 0, 200, 35, "show_evidence_list"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));

        textPanel.showDialogue(suspect.getDisplayName(),
                "Cooperation: " + gameState.getCooperation(suspect) + "%\n\nSelect a topic to ask about.",
                buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    private void handleAskTopic(String topic) {
        gameState.incrementCommandCount();

        // Variable awareness cost: +5 for dangerous topics, +1 otherwise
        Suspect currentSuspect = interviewSystem.getCurrentSuspect();
        int awarenessCost = (currentSuspect != null) ? interviewSystem.getTopicAwarenessCost(currentSuspect, topic) : 1;
        String warning = awarenessSystem.addAwareness(awarenessCost);

        String response = interviewSystem.askTopic(topic);

        if (gameState.isGameOver()) {
            showGameOver();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(response);
        // Random narrator bleed-through during channeling
        String bleed = narratorSystem.maybeGetChannelingBleedThrough();
        if (bleed != null) {
            sb.append("\n\n").append(bleed);
        }
        // Distortions can still fire during interviews
        String distortion = narratorSystem.maybeGetDistortion();
        if (distortion != null) {
            sb.append("\n\n[The narrator interjects: \"").append(distortion).append("\"]");
            gameState.addNarratorDistortion(distortion);
            narratorSystem.checkDistortionContradictions(distortion);
        }
        if (warning != null) {
            sb.append("\n\n--- ").append(narratorSystem.getWarning());
        }

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));

        String _speaker = interviewSystem.getCurrentSuspect() != null
                ? interviewSystem.getCurrentSuspect().getDisplayName() : "Suspect";
        textPanel.showDialogue(_speaker, sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    private void showEvidenceForInterview() {
        List<TextButton> buttons = new ArrayList<>();
        for (Evidence e : gameState.getCollectedEvidence()) {
            buttons.add(new TextButton(e.getDisplayName(), 0, 0, 200, 35, "evidence_" + e.name()));
        }
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));

        Suspect activeSuspect = interviewSystem.getCurrentSuspect();
        String _evSpeaker = (activeSuspect != null) ? activeSuspect.getDisplayName() : "Suspect";
        String _evText = gameState.getCollectedEvidence().isEmpty()
                ? "No evidence to show.\n\nCollect evidence by examining objects."
                : "Select evidence to present.";
        textPanel.showDialogue(_evSpeaker, _evText, buttons);
        panelMode = PanelMode.SHOW_EVIDENCE;
    }

    private void handleShowEvidence(Evidence evidence) {
        gameState.incrementCommandCount();
        gameState.incrementEvidenceShownCount();

        // +2 awareness for showing evidence
        String warning = awarenessSystem.addAwareness(2);

        String reaction = interviewSystem.showEvidence(evidence);

        if (gameState.isGameOver()) {
            showGameOver();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(reaction);
        String bleedEvidence = narratorSystem.maybeGetChannelingBleedThrough();
        if (bleedEvidence != null) {
            sb.append("\n\n").append(bleedEvidence);
        }
        if (warning != null) {
            sb.append("\n\n--- ").append(narratorSystem.getWarning());
        }

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("Show More Evidence", 0, 0, 200, 35, "show_evidence_list"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));

        String _speaker = interviewSystem.getCurrentSuspect() != null
                ? interviewSystem.getCurrentSuspect().getDisplayName() : "Suspect";
        textPanel.showDialogue(_speaker, sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    private void handleWeaponContradiction() {
        String warning = awarenessSystem.addAwareness(2);
        String response = interviewSystem.presentWeaponContradiction();
        gameState.addEvent("Discovered contradiction: Weapon");

        if (gameState.isGameOver()) {
            showGameOver();
            return;
        }

        StringBuilder sb = new StringBuilder(response);
        String bleedW = narratorSystem.maybeGetChannelingBleedThrough();
        if (bleedW != null)
            sb.append("\n\n").append(bleedW);
        if (warning != null)
            sb.append("\n\n--- ").append(narratorSystem.getWarning());

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));
        String _speaker = interviewSystem.getCurrentSuspect() != null
                ? interviewSystem.getCurrentSuspect().getDisplayName() : "Suspect";
        textPanel.showDialogue(_speaker, sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    private void handleBodyContradiction() {
        String warning = awarenessSystem.addAwareness(2);
        String response = interviewSystem.presentBodyContradiction();
        gameState.addEvent("Discovered contradiction: Body position");

        if (gameState.isGameOver()) {
            showGameOver();
            return;
        }

        StringBuilder sb = new StringBuilder(response);
        String bleedB = narratorSystem.maybeGetChannelingBleedThrough();
        if (bleedB != null)
            sb.append("\n\n").append(bleedB);
        if (warning != null)
            sb.append("\n\n--- ").append(narratorSystem.getWarning());

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));
        String _speaker = interviewSystem.getCurrentSuspect() != null
                ? interviewSystem.getCurrentSuspect().getDisplayName() : "Suspect";
        textPanel.showDialogue(_speaker, sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    // --- Confrontation ---

    private void handleConfrontation(Suspect suspect) {
        String warning = awarenessSystem.addAwareness(3);
        String response = interviewSystem.presentConfrontation(suspect);
        gameState.markConfronted(suspect);
        gameState.addEvent("Confronted " + suspect.getDisplayName());

        if (gameState.isGameOver()) {
            showGameOver();
            return;
        }

        StringBuilder sb = new StringBuilder(response);
        String bleedC = narratorSystem.maybeGetChannelingBleedThrough();
        if (bleedC != null)
            sb.append("\n\n").append(bleedC);
        if (warning != null)
            sb.append("\n\n--- ").append(narratorSystem.getWarning());

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));
        String _speaker = interviewSystem.getCurrentSuspect() != null
                ? interviewSystem.getCurrentSuspect().getDisplayName() : "Suspect";
        textPanel.showDialogue(_speaker, sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    // --- Accusation ---

    private void handleAccusation(String action) {
        if (gameState.getWrongAccusationCount() >= 3) {
            textPanel.showDialogue("The Narrator", "ACCUSATION BLOCKED\n\nAfter " + gameState.getWrongAccusationCount() +
                    " wrong accusations, the suspects have closed ranks. They won't engage with " +
                    "further accusations.\n\n[Build a stronger case before accusing anyone else.]", new ArrayList<>());
            panelMode = PanelMode.TEXT;
            return;
        }
        gameState.setAccusationMade(true);
        String accusationTarget = action.replace("accuse_", "").replace("_", " ");
        gameState.addEvent("Made accusation: " + accusationTarget);

        if ("accuse_james_daniel".equals(action)) {
            gameState.setGameWon(true);
            gameState.setChosenEnding(GameState.Ending.ACCUSATION_CORRECT);

            StringBuilder winText = new StringBuilder();
            winText.append("=== CASE SOLVED ===\n\n");
            winText.append("You present your evidence to the authorities. James Vance and Daniel the groundskeeper ");
            winText.append("are arrested for the murder of Harold Vance.\n\n");
            winText.append("The evidence is overwhelming: James, facing disinheritance after his father discovered ");
            winText.append("the embezzlement, conspired with Daniel -- who had been laundering the money -- to kill ");
            winText.append("Harold before the new will could be signed.\n\n");
            winText.append("James killed the drugged Harold with the fireplace poker. Later, Daniel entered through ");
            winText.append(
                    "the study window to help move the body to the cellar. But they were sloppy. They left too many traces.\n\n");
            winText.append(
                    "Margaret is cleared. Marcus Blackwood and Charles Webb cooperate fully with the investigation.\n\n");

            // Variant text by evidence completeness
            int evidenceCount = gameState.getCollectedEvidence().size();
            int watchedCount = gameState.getWatchedTapes().size();
            int tapeCount = gameState.getCollectedTapes().size();
            if (evidenceCount >= 10 && watchedCount >= 8) {
                winText.append("Your case is IRONCLAD. Every piece of evidence accounted for, every tape reviewed. ");
                winText.append(
                        "The prosecution has no gaps to exploit. James and Daniel will face the full weight of justice.\n\n");
            } else {
                winText.append("Your case is solid, but gaps remain. Evidence: ").append(evidenceCount).append("/10, ");
                winText.append("Tapes watched: ").append(watchedCount).append("/").append(tapeCount).append(". ");
                winText.append("A thorough investigator might have found more.\n\n");
            }

            // Variant text by awareness level
            int awareness = gameState.getAwareness();
            if (awareness < 25) {
                winText.append("=== THE GHOST ===\n");
                winText.append("You moved through Vance Manor like a phantom. The household never suspected ");
                winText.append("the depth of your investigation. By the time they realized what was happening, ");
                winText.append("the evidence was already in the hands of the authorities.\n\n");
            } else if (awareness < 50) {
                winText.append("=== BALANCED INVESTIGATION ===\n");
                winText.append("You walked the fine line between thoroughness and discretion. The household ");
                winText.append("grew uneasy, but never enough to close ranks against you.\n\n");
            } else if (awareness < 70) {
                winText.append("=== CLOSE CALL ===\n");
                winText.append("The household was closing in on you. Doors were starting to lock, whispers ");
                winText.append("followed you through the halls. You solved the case just in time -- another ");
                winText.append("day and they might have shut you out entirely.\n\n");
            } else {
                winText.append("=== NARROW ESCAPE ===\n");
                winText.append("The Entity was fully awake. The walls were watching, the shadows moving. You ");
                winText.append("could feel the house itself turning against you. You barely made it out with ");
                winText.append("the evidence. Another moment and the manor would have claimed another victim.\n\n");
            }

            winText.append("CONGRATULATIONS -- YOU SOLVED THE CASE!\n\n");
            winText.append("Final Awareness: ").append(awareness).append("/").append(GameState.MAX_AWARENESS)
                    .append("\n");
            winText.append("Commands used: ").append(gameState.getCommandCount());

            // Check achievements
            java.util.List<com.dsa.game.state.Achievement> newAchievements = achievementSystem.checkOnWin();
            newAchievements.addAll(achievementSystem.checkOnEnding(GameState.Ending.ACCUSATION_CORRECT));
            if (!newAchievements.isEmpty()) {
                winText.append("\n\n=== ACHIEVEMENTS UNLOCKED ===\n\n");
                for (com.dsa.game.state.Achievement a : newAchievements) {
                    winText.append("* ").append(a.getDisplayName()).append(" -- ").append(a.getDescription())
                            .append("\n");
                }
            }

            textPanel.showDialogue("Case Closed", winText.toString(), new ArrayList<>());
            panelMode = PanelMode.TEXT;
        } else {
            // Wrong accusation: +15 awareness
            gameState.incrementWrongAccusationCount();
            String warning = awarenessSystem.addAwareness(15);

            StringBuilder sb = new StringBuilder();
            sb.append("=== WRONG ACCUSATION ===\n\n");

            // Rich defense responses (Feature 4)
            if (SuspectDialogue.hasAccusationDefense(action)) {
                sb.append(SuspectDialogue.getAccusationDefense(action));

                // James outburst auto-discovers body position contradiction
                if ("accuse_james".equals(action) && !gameState.hasContradiction(Contradiction.BODY_POSITION)) {
                    gameState.discoverContradiction(Contradiction.BODY_POSITION);
                    sb.append("\n\n[CONTRADICTION DISCOVERED: Body Position -- James confirmed the body was moved.]");
                }

                // Boost cooperation for cleared suspects
                switch (action) {
                    case "accuse_margaret":
                        gameState.adjustCooperation(Suspect.MARGARET, 15);
                        sb.append("\n\n[Margaret's cooperation increased -- she's grateful to be cleared.]");
                        break;
                    case "accuse_marcus":
                        gameState.adjustCooperation(Suspect.MARCUS, 15);
                        sb.append("\n\n[Marcus's cooperation increased -- he's relieved to be cleared.]");
                        break;
                    case "accuse_charles":
                        gameState.adjustCooperation(Suspect.CHARLES, 15);
                        sb.append("\n\n[Charles's cooperation increased -- he's grateful you believe him.]");
                        break;
                }
            } else {
                sb.append("That accusation doesn't match the evidence.");
            }

            sb.append("\n\n+15 Awareness! The household is now very suspicious of you.");

            if (gameState.isGameOver()) {
                showGameOver();
                return;
            }

            if (warning != null) {
                sb.append("\n\n--- ").append(warning);
            }

            gameState.setChosenEnding(GameState.Ending.ACCUSATION_WRONG);
            gameState.setAccusationMade(false); // allow retry
            textPanel.showDialogue("The Narrator", sb.toString(), new ArrayList<>());
            panelMode = PanelMode.TEXT;
        }
    }

    // --- Settings ---

    private void showSettings() {
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

        String filterLabel = narratorSystem.isFilterEnabled() ? "ON" : "OFF";
        buttons.add(new TextButton("Narrator Commentary: " + filterLabel, 0, 0, 200, 35, "toggle_narrator_filter"));

        buttons.add(new TextButton("Back", 0, 0, 200, 35, "settings_back"));

        textPanel.showButtons("=== SETTINGS ===", buttons);
        panelMode = PanelMode.SETTINGS;
    }

    // --- Leave Manor ---

    private void handleLeaveManor() {
        gameState.setGameOver(true);
        gameState.setChosenEnding(GameState.Ending.LEAVE_MANOR);
        achievementSystem.checkOnEnding(GameState.Ending.LEAVE_MANOR);
        int evidenceCount = gameState.getCollectedEvidence().size();

        StringBuilder sb = new StringBuilder();
        sb.append("=== LEAVING VANCE MANOR ===\n\n");

        if (evidenceCount == 0) {
            sb.append("You walk away from Vance Manor without having investigated at all. ");
            sb.append("The grand house looms behind you as you reach the gate. Whatever happened ");
            sb.append("to Harold Vance will remain a mystery -- at least to you.\n\n");
            sb.append("The killers go free. Justice is denied.\n\n");
        } else if (evidenceCount < 4) {
            sb.append("You leave Vance Manor with fragments of the truth. A torn letter here, ");
            sb.append("a suspicious logbook there -- but not enough to build a case. The pieces ");
            sb.append("of the puzzle swirl in your mind as you walk away.\n\n");
            sb.append("Perhaps someone else will finish what you started.\n\n");
        } else if (!evidenceSystem.canAccuseJamesAndDaniel()) {
            sb.append("You were close. So close. The evidence was building, the suspects were ");
            sb.append("cracking under pressure. But something held you back -- not enough to ");
            sb.append("make it stick. You leave knowing that James and Daniel will sleep uneasy ");
            sb.append("tonight, but they'll sleep free.\n\n");
            sb.append("The truth was within reach, but you couldn't quite grasp it.\n\n");
        } else {
            sb.append("You had everything. The evidence, the contradictions, the testimony. ");
            sb.append("You could have ended this. But instead, you chose to walk away from the ");
            sb.append("truth.\n\n");
            sb.append("James and Daniel exchange a look of relief as your car pulls away. ");
            sb.append("Harold Vance's murder goes unpunished. The manor keeps its secrets.\n\n");
        }

        sb.append("=== FINAL STATS ===\n");
        sb.append("Evidence collected: ").append(evidenceCount).append("/10\n");
        sb.append("Tapes collected: ").append(gameState.getCollectedTapes().size()).append("/8\n");
        sb.append("Awareness: ").append(gameState.getAwareness()).append("/").append(GameState.MAX_AWARENESS)
                .append("\n");
        sb.append("Commands used: ").append(gameState.getCommandCount());

        textPanel.showDialogue("The Narrator", sb.toString(), new ArrayList<>());
        panelMode = PanelMode.TEXT;
    }

    // --- Moral Endings (Feature 6) ---

    private void handleMoralEnding(String action) {
        gameState.setGameOver(true);

        GameState.Ending ending;
        String endingText;
        switch (action) {
            case "ending_seal":
                ending = GameState.Ending.SEAL_THE_WALL;
                endingText = ClimaxContent.ENDING_SEAL_WALL;
                break;
            case "ending_destroy":
                ending = GameState.Ending.DESTROY_TAPES;
                endingText = ClimaxContent.ENDING_DESTROY_TAPES;
                break;
            case "ending_escape":
                ending = GameState.Ending.ESCAPE_MANOR;
                endingText = ClimaxContent.ENDING_ESCAPE;
                break;
            default:
                return;
        }

        gameState.setChosenEnding(ending);
        gameState.addEvent("Chose ending: " + ending.name());

        StringBuilder sb = new StringBuilder();
        sb.append(endingText);

        // Stats
        sb.append("\n\n=== FINAL STATS ===\n");
        sb.append("Evidence collected: ").append(gameState.getCollectedEvidence().size()).append("/10\n");
        sb.append("Tapes collected: ").append(gameState.getCollectedTapes().size()).append("/8\n");
        sb.append("Anomalies discovered: ").append(gameState.getAnomalyCount()).append("/7\n");
        sb.append("Awareness: ").append(gameState.getAwareness()).append("/").append(GameState.MAX_AWARENESS)
                .append("\n");
        sb.append("Commands used: ").append(gameState.getCommandCount());

        // Check ending achievements
        List<Achievement> newAchievements = achievementSystem.checkOnEnding(ending);
        if (!newAchievements.isEmpty()) {
            sb.append("\n\n=== ACHIEVEMENTS UNLOCKED ===\n\n");
            for (Achievement a : newAchievements) {
                sb.append("* ").append(a.getDisplayName()).append(" -- ").append(a.getDescription()).append("\n");
            }
        }

        textPanel.showDialogue("The Narrator", sb.toString(), new ArrayList<>());
        panelMode = PanelMode.TEXT;
    }

    // --- History ---

    private void showHistory() {
        List<String> log = gameState.getEventLog();
        StringBuilder sb = new StringBuilder();
        sb.append("=== EVENT HISTORY ===\n\n");

        if (log.isEmpty()) {
            sb.append("No events recorded yet.\n\nExplore rooms and examine objects to begin your investigation.");
        } else {
            // Show most recent first
            for (int i = log.size() - 1; i >= 0; i--) {
                sb.append(log.get(i)).append("\n");
            }
        }

        textPanel.show(sb.toString());
        panelMode = PanelMode.HISTORY;
    }

    // --- Objectives ---

    private void showObjectives() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CURRENT OBJECTIVES ===\n\n");

        int evidenceCount = gameState.getCollectedEvidence().size();
        int tapeCount = gameState.getCollectedTapes().size();
        int watchedCount = gameState.getWatchedTapes().size();
        boolean canAccuse = evidenceSystem.canAccuseJamesAndDaniel();

        if (canAccuse) {
            // Late game
            sb.append("PRIMARY OBJECTIVE:\n");
            sb.append("* Make your accusation -- you have enough evidence\n\n");

            if (evidenceCount < 10) {
                sb.append("OPTIONAL:\n");
                sb.append("* Collect remaining evidence (").append(evidenceCount).append("/10)\n");
            }
            if (tapeCount < 8) {
                sb.append("* Find remaining tapes (").append(tapeCount).append("/8)\n");
            }
            if (watchedCount < tapeCount) {
                sb.append("* Watch unwatched tapes (").append(watchedCount).append("/").append(tapeCount).append(")\n");
            }
            if (!gameState.hasContradiction(Contradiction.WEAPON)) {
                sb.append("* Discover the weapon contradiction\n");
            }
            if (!gameState.hasContradiction(Contradiction.BODY_POSITION)) {
                sb.append("* Discover the body position contradiction\n");
            }
        } else if (evidenceCount < 3 && tapeCount < 2) {
            // Early game
            sb.append("PRIMARY OBJECTIVES:\n");
            sb.append("* Explore the rooms of Vance Manor\n");
            sb.append("* Examine objects to find evidence and tapes\n");
            sb.append("* Talk to the suspects to gather information\n\n");

            sb.append("PROGRESS:\n");
            sb.append("* Evidence: ").append(evidenceCount).append("/10\n");
            sb.append("* Tapes: ").append(tapeCount).append("/8\n");
        } else {
            // Mid game
            sb.append("PRIMARY OBJECTIVES:\n");
            sb.append("* Continue gathering evidence (").append(evidenceCount).append("/10)\n");
            sb.append("* Find and watch tapes (").append(tapeCount).append("/8 found, ").append(watchedCount)
                    .append(" watched)\n");
            sb.append("* Interview suspects and show them evidence\n\n");

            int jamesEvidence = evidenceSystem.getJamesEvidenceCount();
            int danielEvidence = evidenceSystem.getDanielEvidenceCount();
            sb.append("CASE PROGRESS:\n");
            sb.append("* Evidence against James: ").append(jamesEvidence).append("/3 needed\n");
            sb.append("* Evidence against Daniel: ").append(danielEvidence).append("/2 needed\n");

            if (watchedCount < tapeCount) {
                sb.append("\n* You have unwatched tapes -- check your inventory (I)\n");
            }
        }

        sb.append("\n--- TIPS ---\n");
        sb.append("* Keep awareness low -- the household is watching\n");
        sb.append("* Examine objects multiple times for hidden items\n");
        sb.append("* Show evidence to suspects during interviews\n");
        sb.append("* Press H for event history, N for notebook\n");

        textPanel.show(sb.toString());
        panelMode = PanelMode.OBJECTIVES;
    }

    // --- Save/Load ---

    private void showSaveMenu() {
        List<TextButton> buttons = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String slotName = "slot" + i;
            String label = "Slot " + i;
            if (saveLoadSystem.saveExists(slotName)) {
                label += " [occupied]";
            } else {
                label += " [empty]";
            }
            buttons.add(new TextButton(label, 0, 0, 200, 35, "save_" + slotName));
        }
        textPanel.showButtons("=== SAVE GAME ===\n\nSelect a slot to save:", buttons);
        panelMode = PanelMode.SAVE_MENU;
    }

    private void showLoadMenu() {
        List<TextButton> buttons = new ArrayList<>();
        boolean hasAnySave = false;
        for (int i = 1; i <= 3; i++) {
            String slotName = "slot" + i;
            if (saveLoadSystem.saveExists(slotName)) {
                buttons.add(new TextButton("Load Slot " + i, 0, 0, 200, 35, "load_" + slotName));
                buttons.add(new TextButton("Delete Slot " + i, 0, 0, 200, 35, "delete_" + slotName));
                hasAnySave = true;
            }
        }

        if (!hasAnySave) {
            textPanel.show("=== LOAD GAME ===\n\nNo save files found.\nPress F5 to save your current game.");
        } else {
            textPanel.showButtons("=== LOAD GAME ===\n\nSelect a save to load or delete:", buttons);
        }
        panelMode = PanelMode.LOAD_MENU;
    }

    // --- Pause Menu ---

    private void showPauseMenu() {
        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Resume", 0, 0, 200, 35, "resume"));
        buttons.add(new TextButton("Save Game", 0, 0, 200, 35, "pause_save"));
        buttons.add(new TextButton("Load Game", 0, 0, 200, 35, "pause_load"));
        buttons.add(new TextButton("Settings", 0, 0, 200, 35, "pause_settings"));
        buttons.add(new TextButton("Leave the Manor", 0, 0, 200, 35, "leave_manor"));
        buttons.add(new TextButton("Quit to Menu", 0, 0, 200, 35, "quit_to_menu"));
        textPanel.showButtons("=== PAUSED ===", buttons);
        panelMode = PanelMode.PAUSE;
    }

    /**
     * Load a save file into this GameScreen. Called by TitleScreen after
     * construction.
     */
    public void loadFromSave(String slotName) {
        isLoadedGame = true;
        Room.RoomID roomId = saveLoadSystem.load(gameState, slotName);
        if (roomId != null) {
            roomManager.navigateTo(roomId);
            pendingClimax = false;
        } else {
            textPanel.show(
                    "Failed to load save file.\n\nThe save file may be corrupted or from an older version of the game.");
            panelMode = PanelMode.TEXT;
        }
    }

    // --- Game Over ---

    private void showGameOver() {
        gameState.setChosenEndingByName("GAME_OVER_AWARENESS");
        achievementSystem.checkOnEnding(GameState.Ending.GAME_OVER_AWARENESS);
        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Return to Main Menu", 0, 0, 220, 35, "quit_to_menu"));
        textPanel.showButtons("=== GAME OVER ===\n\n" +
                "The household has closed ranks against you. Doors are locked, " +
                "witnesses refuse to speak, and evidence begins to disappear.\n\n" +
                "You've been asked to leave Vance Manor. The murder of Harold Vance " +
                "will remain unsolved.\n\n" +
                "The killers go free.\n\n" +
                "Awareness reached " + gameState.getAwareness() + "/" + GameState.MAX_AWARENESS + ".\n" +
                "Evidence collected: " + gameState.getCollectedEvidence().size() + "/10\n" +
                "Tapes collected: " + gameState.getCollectedTapes().size() + "/8",
                buttons);
        panelMode = PanelMode.TEXT;
    }

    // --- Render ---

    @Override
    public void render(float delta) {
        if (minigameReturnCooldown > 0f) minigameReturnCooldown -= delta;

        // ── Pending inventory screen ──────────────────────────────────────────
        if (pendingInventoryStart && minigameReturnCooldown <= 0f) {
            pendingInventoryStart = false;
            startInventoryPhase();
        }

        if (evidenceGapSession != null) {
            evidenceGapSession.update(delta);
        }

        // ── Narrator spotlight timer (every 4 min, not in entrance/cellar) ───
        if (!textPanel.isVisible() && !gameState.isGameOver() && !gameState.isGameWon()
                && !documentGame.isActive() && !metalDetectorModeActive && evidenceGapSession == null
                && panelMode == PanelMode.NONE && minigameReturnCooldown <= 0f) {
            Room.RoomID curRoom = roomManager.getCurrentRoom().getId();
            if (curRoom != Room.RoomID.ENTRANCE && curRoom != Room.RoomID.CELLAR) {
                spotlightTimer -= delta;
                if (spotlightTimer <= 0f) {
                    spotlightTimer = SPOTLIGHT_INTERVAL;
                    launchSpotlight(curRoom);
                    return;
                }
            }
        }

        // ── Metal detector: update beep proximity + "scan" prompt ─────────────
        if (metalDetectorModeActive) {
            updateMetalDetectorNavigation(delta);
        }

        // --- REVEAL TIMER (kept for any pending reveals already queued) ---
        if (pendingRevealObject != null) {
            revealTimer -= delta;
            if (revealTimer <= 0f) {
                String obj = pendingRevealObject;
                pendingRevealObject = null;
                if (evidenceGapSession == null) {
                    handleExamine(obj);
                }
            }
        }

        // --- ENTITY PULL UPDATE ---
        pullTimer += delta;
        if (!pullActive && pullTimer >= PULL_INTERVAL && gameState.getAwareness() >= 20) {
            pullTimer = 0f;
            pullActive = true;
            pullActiveTimer = 0f;
            float angle = com.badlogic.gdx.math.MathUtils.random(0f, 360f)
                    * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            float strength = PULL_MAX * (gameState.getAwareness() / (float) GameState.MAX_AWARENESS);
            pullOffsetX = com.badlogic.gdx.math.MathUtils.cos(angle) * strength;
            pullOffsetY = com.badlogic.gdx.math.MathUtils.sin(angle) * strength;
        }
        if (pullActive) {
            pullActiveTimer += delta;
            if (pullActiveTimer >= PULL_DURATION) {
                pullActive = false;
                pullOffsetX = 0f;
                pullOffsetY = 0f;
            }
        }


        // Advance room transition fade
        if (transitionFadingOut) {
            transitionAlpha = Math.min(1f, transitionAlpha + delta * TRANSITION_SPEED);
            if (transitionAlpha >= 1f) {
                transitionFadingOut = false;
                transitionFadingIn = true;
                if (pendingTransition != null) {
                    pendingTransition.run();
                    pendingTransition = null;
                }
            }
        } else if (transitionFadingIn) {
            transitionAlpha = Math.max(0f, transitionAlpha - delta * TRANSITION_SPEED);
            if (transitionAlpha <= 0f) {
                transitionFadingIn = false;
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(game.camera.combined);

        Room currentRoom = roomManager.getCurrentRoom();
        syncArrowBackHotspotBounds();

        // Reset on room change
        if (lastRenderedRoom != currentRoom.getId()) {
            lastRenderedRoom = currentRoom.getId();
        }

        batch.begin();

        // Draw room background
        Texture roomTex = roomTextures.get(currentRoom.getId());
        if (currentRoom.getId() == Room.RoomID.KITCHEN && kitchenWithoutTapeTex != null) {
            boolean tapeRevealed = "kitchen_floor".equals(pendingRevealObject);
            roomTex = tapeRevealed ? roomTextures.get(Room.RoomID.KITCHEN) : kitchenWithoutTapeTex;
        }
        if (currentRoom.getId() == Room.RoomID.PARLOR && parlorWithoutBriefcaseTex != null) {
            boolean briefcaseRevealed = "briefcase".equals(pendingRevealObject);
            roomTex = briefcaseRevealed ? roomTextures.get(Room.RoomID.PARLOR) : parlorWithoutBriefcaseTex;
        }
        // Study: base room only; props are drawn in drawStudyPropLayers() after this.
        if (currentRoom.getId() == Room.RoomID.JAMES_ROOM
                && jamesClosedTex != null
                && gameState.getExamCount(Room.RoomID.JAMES_ROOM, "wardrobe") == 0) {
            roomTex = jamesClosedTex;
        }
        if (currentRoom.getId() == Room.RoomID.GUEST_ROOMS
                && guestRoomMargaretCloseTex != null
                && !isMargaretRoomUnlocked()) {
            roomTex = guestRoomMargaretCloseTex;
        } else if (currentRoom.getId() == Room.RoomID.GUEST_ROOMS
                && guestRoomsUnlockedTex != null
                && isMargaretRoomUnlocked()) {
            roomTex = guestRoomsUnlockedTex;
        }
        if (currentRoom.getId() == Room.RoomID.GROUNDSKEEPER_SHED && shedCanonicalTex != null) {
            // Always use inventory shed art; flip horizontally at draw time.
            roomTex = shedCanonicalTex;
        }
        if (currentRoom.getId() == Room.RoomID.MARGARET_ROOM) {
            boolean tapeTaken    = gameState.hasTape(Tape.TAPE_MARGARET_ACCOUNT);
            boolean showTape     = "tape_recorder".equals(pendingRevealObject) && !tapeTaken;
            boolean topOpen      = gameState.isMargaretTopOpen();
            boolean botOpen      = gameState.isMargaretBotOpen();
            boolean kitExamined  = gameState.getExamCount(Room.RoomID.MARGARET_ROOM, "kit") > 0;
            boolean shoesTaken   = gameState.isMargaretShoesExamined();
            Texture t = null;
            if (showTape) {
                if (!topOpen && !botOpen)                    t = mTapeTopClosedBotClosed;
                else if (!topOpen && botOpen && !shoesTaken) t = mTapeTopClosedBotOpenShoes;
                else if (!topOpen && botOpen)                t = mTapeTopClosedBotOpenNoShoes;
                else if (topOpen && !botOpen && !kitExamined) t = mTapeTopOpenKit;
                else if (topOpen && !botOpen)                t = mTapeTopOpenNoKit;
                else                                         t = mTapeTopOpenNoKitBotOpen;
            } else {
                if (!topOpen && !botOpen)                    t = mNoTapeTopClosedBotClosed;
                else if (!topOpen && botOpen && !shoesTaken) t = mNoTapeTopClosedBotOpenShoes;
                else if (!topOpen && botOpen)                t = mNoTapeTopClosedBotOpenNoShoes;
                else if (topOpen && !botOpen && !kitExamined) t = mNoTapeTopOpenKit;
                else if (topOpen && !botOpen)                t = mNoTapeTopOpenNoKit;
                else                                         t = mNoTapeTopOpenNoKitBotOpen;
            }
            if (t != null)
                roomTex = t;
        }
        if (roomTex != null) {
            float sw = DSAGame.SCREEN_WIDTH, sh = DSAGame.SCREEN_HEIGHT;
            if (currentRoom.getId() == Room.RoomID.GROUNDSKEEPER_SHED) {
                batch.draw(roomTex, sw, 0, -sw, sh);
            } else {
                batch.draw(roomTex, 0, 0, sw, sh);
            }
        }
        if (currentRoom.getId() == Room.RoomID.STUDY) {
            drawStudyPropLayers(batch);
        }

        if (evidenceGapSession != null && !evidenceGapSession.isBagOpen()) {
            evidenceGapSession.drawEmbeddedPickups(batch, currentRoom.getId());
        }

        // --- DARKNESS MULTIPLY MASK ---
        // Render a warm-circle-on-black to the lights FBO, then multiply it over the room.
        // room * white(center) = room visible; room * black(edges) = darkness.
        if (!debugDisableLimitedVisibility
                && !gameState.isGameOver() && !gameState.isGameWon() && spotlightTexture != null) {
            int aw = gameState.getAwareness();
            float maxAw = (float) GameState.MAX_AWARENESS;
            float lightRadius = 220f - (aw / maxAw) * 150f; // 220px → 70px
            float lightSize   = lightRadius * 2f;
            float lightX = (cursorGameX + pullOffsetX) - lightSize / 2f;
            float lightY = (cursorGameY + pullOffsetY) - lightSize / 2f;

            // 1. Flush room draw to screen
            batch.end();

            // 2. Render spotlight (warm circle on black) into lightsBuffer
            lightsBuffer.begin();
            Gdx.gl.glViewport(0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.setProjectionMatrix(game.camera.combined);
            batch.begin();
            batch.setColor(Color.WHITE);
            batch.draw(spotlightTexture, lightX, lightY, lightSize, lightSize);
            batch.end();
            lightsBuffer.end();

            // 3. Apply lights buffer as multiply mask — reveals room only at cursor
            viewport.apply();
            batch.setProjectionMatrix(game.camera.combined);
            batch.begin();
            batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);
            Texture lightTex = lightsBuffer.getColorBufferTexture();
            // FBO is stored bottom-up; draw with negative height to flip Y
            batch.draw(lightTex, 0, DSAGame.SCREEN_HEIGHT, DSAGame.SCREEN_WIDTH, -DSAGame.SCREEN_HEIGHT);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(Color.WHITE);
        }

        // Tooltips removed

        batch.setColor(Color.WHITE);


        // --- METAL DETECTOR FLASH ICON ---
        if (metalDetectorModeActive && metalDetectorIconTex != null && !metalDetectorRoomReached) {
            mdIconFlashTimer += delta;
            // Flash rate syncs with beep interval: faster beep = faster flash
            float flashSpeed = 4000f / Math.max(100f, mdBeepInterval); // 1 Hz slow → 40 Hz fast
            float alpha = 0.45f + 0.55f * (float)(Math.sin(mdIconFlashTimer * flashSpeed * Math.PI) * 0.5 + 0.5);
            float iconSize = 56f;
            float iconX = DSAGame.SCREEN_WIDTH - iconSize - 12f;
            float iconY = 12f;
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(metalDetectorIconTex, iconX, iconY, iconSize, iconSize);
            batch.setColor(Color.WHITE);
        }

        // Draw awareness meter
        awarenessMeter.render(batch, font, awarenessSystem, gameState.getAwareness());

        // Draw action bar
        actionBar.render(batch, font);

        // Draw back button (back.png) upper-left for rooms with ARROW_BACK hotspot
        if (!textPanel.isVisible()) {
            for (Hotspot h : roomManager.getCurrentRoom().getHotspots()) {
                if (h.getType() == Hotspot.HotspotType.ARROW_BACK) {
                    float by = actionBar.getBarHeight() + 4f;
                    float brightness = h.isHovered() ? 0.75f : 1f;
                    batch.setColor(brightness, brightness, brightness, 1f);
                    batch.draw(backButtonTex, ARROW_BACK_DRAW_X, by, ARROW_BACK_DRAW_W, ARROW_BACK_DRAW_H);
                    batch.setColor(Color.WHITE);
                    break;
                }
            }
        }

        // Evidence backpack: drawn after back.png so it stacks above the arrow (matches EvidenceGapSession EMBEDDED_BAG_*).
        if (!textPanel.isVisible() && evidenceGapSession != null && !evidenceGapSession.isBagOpen()) {
            evidenceGapSession.drawEmbeddedBackpackIcon(batch);
        }

        // Draw character portraits — VN two-portrait system (left / right)
        {
            boolean dlgMode = textPanel.isVisible() && textPanel.isDialogueMode();
            if (dlgMode) {
                String pageText = textPanel.getCurrentPageText();
                String pageSpeaker = detectPageSpeaker(pageText);
                if (pageSpeaker == null && pageText != null
                        && !pageText.startsWith("===") && !pageText.startsWith("[")) {
                    pageSpeaker = textPanel.getSpeaker();
                }
                // Clear speaker name on headers and stage directions
                if (pageSpeaker == null && pageText != null
                        && (pageText.startsWith("===") || pageText.startsWith("["))) {
                    textPanel.setSpeaker(null);
                }
                if (pageSpeaker != null) {
                    // Update dialogue box header to show the actual per-page speaker
                    textPanel.setSpeaker(pageSpeaker);
                    if (portraitTextures.containsKey(pageSpeaker)) {
                        // Assign first seen speaker to left, second to right
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
            } else {
                // Reset when dialogue closes
                leftSpeaker = null; rightSpeaker = null;
                leftPortrait = null; rightPortrait = null;
                leftIsActive = true;
            }

            // Active speaker = full brightness, inactive = dimmed
            float leftTarget  = leftPortrait  != null ? (leftIsActive  ? 1f : 0.45f) : 0f;
            float rightTarget = rightPortrait != null ? (!leftIsActive ? 1f : 0.45f) : 0f;
            leftAlpha  += (leftTarget  - leftAlpha)  * Math.min(1f, delta * 8f);
            rightAlpha += (rightTarget - rightAlpha) * Math.min(1f, delta * 8f);

            final float PH = 560f;

            // Left portrait (normal orientation, faces right toward center)
            if (leftPortrait != null && leftAlpha > 0.01f) {
                float pw = PH * leftPortrait.getWidth() / (float) leftPortrait.getHeight();
                batch.setColor(1f, 1f, 1f, leftAlpha);
                batch.draw(leftPortrait, 40f, 0f, pw, PH);
            }

            // Right portrait (flipped horizontally so character faces left toward center)
            if (rightPortrait != null && rightAlpha > 0.01f) {
                float pw = PH * rightPortrait.getWidth() / (float) rightPortrait.getHeight();
                float px = DSAGame.SCREEN_WIDTH - 40f - pw;
                batch.setColor(1f, 1f, 1f, rightAlpha);
                batch.draw(rightPortrait, px + pw, 0f, -pw, PH); // negative width = horizontal flip
            }

            batch.setColor(Color.WHITE);
        }

        // Update and draw text panel (on top of everything)
        textPanel.update(delta);
        textPanel.render(batch, font);

        if (evidenceGapSession != null && evidenceGapSession.isBagOpen()) {
            evidenceGapSession.drawOpenInventoryOverlay(batch, roomManager.getCurrentRoom().getId());
        }

        // Draw mini-game overlay (on top of text panel)
        if (documentGame.isActive()) {
            documentGame.render(batch, font);
        }


        // Room transition fade overlay
        if (transitionAlpha > 0f) {
            batch.setColor(0f, 0f, 0f, transitionAlpha);
            batch.draw(pixelTexture, 0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
            batch.setColor(Color.WHITE);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
        setupInput();
        if (!isLoadedGame && !returningFromMinigame) {
            MetalDetectorScanScreen.metalDetectorDemoAlreadySeen = false;
            metalDetectorTargetRoom = Room.RoomID.STUDY;
            metalDetectorModeActive = true;
            metalDetectorRoomReached = false;
            startMetalDetectorBeepThread();
            evidenceGapSession = EvidenceGapSession.forEmbeddedSilentNoWorldPickups(game, currentChapter, null);
        }
        returningFromMinigame = false;
        // Remove already-collected item hotspots (handles loaded saves)
        if (gameState.hasTape(Tape.TAPE_MARGARET_INTERVIEW)) {
            removeHotspot(Room.RoomID.KITCHEN, "kitchen_floor");
        }
        if (gameState.hasTape(Tape.TAPE_MARGARET_ACCOUNT)) {
            removeHotspot(Room.RoomID.MARGARET_ROOM, "tape_recorder");
        }
        if (gameState.hasTape(Tape.TAPE_DANIEL_INTERVIEW)) {
            removeHotspot(Room.RoomID.GROUNDSKEEPER_SHED, "logbook");
        }
        if (gameState.hasEvidence(Evidence.MUDDY_BOOTS)) {
            removeHotspot(Room.RoomID.GROUNDSKEEPER_SHED, "shelf");
        }
        if (gameState.getExamCount(Room.RoomID.JAMES_ROOM, "wardrobe") > 0) {
            removeHotspot(Room.RoomID.JAMES_ROOM, "wardrobe");
            roomManager.getRoom(Room.RoomID.JAMES_ROOM)
                    .addHotspot(new Hotspot("coat", "Examine: James's Coat", 879, 314, 53, 178));
        }
        // Restore drawer hotspot state if drawers were open when saved
        if (gameState.isMargaretTopOpen() && gameState.getExamCount(Room.RoomID.MARGARET_ROOM, "kit") == 0) {
            removeHotspot(Room.RoomID.MARGARET_ROOM, "top_drawer");
            addMargaretKitHotspot();
        }
        if (gameState.isMargaretBotOpen() && !gameState.isMargaretShoesExamined()) {
            removeHotspot(Room.RoomID.MARGARET_ROOM, "bottom_drawer");
            addMargaretShoesHotspot();
        }
        syncArrowBackHotspotBounds();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
        if (game.bgMusic != null) game.bgMusic.pause();
    }

    @Override
    public void resume() {
        if (game.bgMusic != null) game.bgMusic.play();
    }

    @Override
    public void dispose() {
        font.dispose();
        titleFont.dispose();
        for (Texture tex : roomTextures.values())
            tex.dispose();
        if (portraitTextures != null)
            for (Texture tex : portraitTextures.values())
                tex.dispose();
        if (kitchenWithoutTapeTex != null)
            kitchenWithoutTapeTex.dispose();
        if (parlorWithoutBriefcaseTex != null)
            parlorWithoutBriefcaseTex.dispose();
        if (studyWithPokerWithTapeTex != null)
            studyWithPokerWithTapeTex.dispose();
        if (studyWithPokerWithoutTapeTex != null)
            studyWithPokerWithoutTapeTex.dispose();
        if (jamesClosedTex != null)
            jamesClosedTex.dispose();
        if (guestRoomsUnlockedTex != null)
            guestRoomsUnlockedTex.dispose();
        if (shedTapeBoots != null)    shedTapeBoots.dispose();
        if (shedTapeNoBoots != null)  shedTapeNoBoots.dispose();
        if (shedNoTapeBoots != null)  shedNoTapeBoots.dispose();
        if (shedNoTapeNoBoots != null) shedNoTapeNoBoots.dispose();
        if (shedCanonicalTex != null) shedCanonicalTex.dispose();
        if (mTapeTopClosedBotClosed != null)
            mTapeTopClosedBotClosed.dispose();
        if (mTapeTopClosedBotOpenShoes != null)
            mTapeTopClosedBotOpenShoes.dispose();
        if (mTapeTopClosedBotOpenNoShoes != null)
            mTapeTopClosedBotOpenNoShoes.dispose();
        if (mTapeTopOpenKit != null)
            mTapeTopOpenKit.dispose();
        if (mTapeTopOpenNoKit != null)
            mTapeTopOpenNoKit.dispose();
        if (mTapeTopOpenNoKitBotOpen != null)
            mTapeTopOpenNoKitBotOpen.dispose();
        if (mNoTapeTopClosedBotClosed != null)
            mNoTapeTopClosedBotClosed.dispose();
        if (mNoTapeTopClosedBotOpenShoes != null)
            mNoTapeTopClosedBotOpenShoes.dispose();
        if (mNoTapeTopClosedBotOpenNoShoes != null)
            mNoTapeTopClosedBotOpenNoShoes.dispose();
        if (mNoTapeTopOpenKit != null)
            mNoTapeTopOpenKit.dispose();
        if (mNoTapeTopOpenNoKit != null)
            mNoTapeTopOpenNoKit.dispose();
        if (mNoTapeTopOpenNoKitBotOpen != null)
            mNoTapeTopOpenNoKitBotOpen.dispose();
        if (backButtonTex != null) backButtonTex.dispose();
        if (spotlightTexture != null) spotlightTexture.dispose();
        if (lightsBuffer != null) lightsBuffer.dispose();
        pixelTexture.dispose();
        textPanel.dispose();
        awarenessMeter.dispose();
        actionBar.dispose();
        documentGame.dispose();
        TextButton.disposeTextures();
        if (evidenceGapSession != null) {
            evidenceGapSession.dispose();
            evidenceGapSession = null;
        }
    }
}
