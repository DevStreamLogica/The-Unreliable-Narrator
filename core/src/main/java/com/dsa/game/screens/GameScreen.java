package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Cursor;
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
    private Texture pixelTexture;


    private String currentTooltip = "";
    private GlyphLayout layout;

    // Fade timers
    private float titleFadeTimer = 0;
    private float descFadeTimer = 0;
    private static final float TITLE_FADE_DURATION = 3f;
    private static final float DESC_FADE_DURATION = 5f;
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

    // Panel mode tracking
    private enum PanelMode { NONE, TEXT, INVENTORY, SUSPECTS, SUSPECT_LIST, INTERVIEW, TAPE_PLAY, SHOW_EVIDENCE, ACCUSE_SELECT, NOTEBOOK, SAVE_MENU, LOAD_MENU, PAUSE, HISTORY, OBJECTIVES, SETTINGS }
    private PanelMode panelMode = PanelMode.NONE;

    // Climax state
    private boolean pendingClimax = false;

    // Mini-game state
    private ExamResult.MiniGameType pendingMiniGame = null;

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

        // Initialize UI components
        textPanel = new TextPanel();
        awarenessMeter = new AwarenessMeter();
        actionBar = new ActionBar();
        documentGame = new DocumentReconstructionGame();

        setupInput();
    }

    private void generatePlaceholderTextures() {
        roomTextures = new HashMap<>();

        for (Room.RoomID roomId : Room.RoomID.values()) {
            String imagePath = "rooms/" + roomId.name().toLowerCase() + ".png";
            if (Gdx.files.internal(imagePath).exists()) {
                roomTextures.put(roomId, new Texture(Gdx.files.internal(imagePath)));
            } else {
                roomTextures.put(roomId, PlaceholderGenerator.generateRoomPlaceholder(
                    roomId, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT));
            }
        }
    }

    private void generateUITextures() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        pixelTexture = new Texture(p);
        p.dispose();
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY);
                viewport.unproject(touchPos);
                float gameX = touchPos.x;
                float gameY = touchPos.y;

                // Allow text panel interaction even after game over/won
                if (textPanel.isVisible()) {
                    String action = textPanel.handleClick(gameX, gameY);
                    if (action != null) {
                        handlePanelAction(action);
                        return true;
                    }
                    return true;
                }

                if (gameState.isGameOver() || gameState.isGameWon()) return true;

                // Document reconstruction mini-game gets top priority
                if (documentGame.isActive()) {
                    if (documentGame.isCompleted()) {
                        documentGame.finish();
                    } else {
                        documentGame.handleTouchDown(gameX, gameY);
                    }
                    return true;
                }

                // Action bar
                String barAction = actionBar.handleClick(gameX, gameY);
                if (barAction != null) {
                    handleActionBarClick(barAction);
                    return true;
                }

                // Hotspots - Check EXAMINE hotspots first (they take priority over navigation)
                for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
                    if (hotspot.contains(gameX, gameY) && hotspot.getType() == Hotspot.HotspotType.EXAMINE) {
                        handleExamine(hotspot.getObjectName());
                        return true;
                    }
                }

                // Then check navigation hotspots
                for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
                    if (hotspot.contains(gameX, gameY)) {
                        handleNavigation(hotspot.getTargetRoom());
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (documentGame.isActive() && !documentGame.isCompleted()) {
                    touchPos.set(screenX, screenY);
                    viewport.unproject(touchPos);
                    documentGame.handleTouchDragged(touchPos.x, touchPos.y);
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
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                touchPos.set(screenX, screenY);
                viewport.unproject(touchPos);
                float gameX = touchPos.x;
                float gameY = touchPos.y;
                currentTooltip = "";

                // Mini-game hover handling
                if (documentGame.isActive()) {
                    documentGame.handleHover(gameX, gameY);
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
                    return false;
                }

                if (textPanel.isVisible()) {
                    textPanel.handleHover(gameX, gameY);
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
                    return false;
                }

                actionBar.handleHover(gameX, gameY);
                awarenessMeter.handleHover(gameY);

                boolean overHotspot = false;
                for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
                    hotspot.checkHover(gameX, gameY);
                    if (hotspot.isHovered()) {
                        currentTooltip = hotspot.getTooltip();
                        overHotspot = true;
                    }
                }

                if (overHotspot) {
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
                } else {
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
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
                    if (textPanel.isVisible()) {
                        textPanel.hide();
                        panelMode = PanelMode.NONE;
                    } else {
                        showPauseMenu();
                    }
                    return true;
                }

                // Don't allow navigation while panel is open
                if (textPanel.isVisible()) return true;

                switch (keycode) {
                    case Input.Keys.W: case Input.Keys.UP:
                        navigateByDirection(Direction.NORTH); break;
                    case Input.Keys.S: case Input.Keys.DOWN:
                        navigateByDirection(Direction.SOUTH); break;
                    case Input.Keys.A: case Input.Keys.LEFT:
                        navigateByDirection(Direction.WEST); break;
                    case Input.Keys.D: case Input.Keys.RIGHT:
                        navigateByDirection(Direction.EAST); break;
                    case Input.Keys.I:
                        handleActionBarClick("inventory"); break;
                    case Input.Keys.N:
                        handleActionBarClick("notebook"); break;
                    case Input.Keys.T:
                        handleActionBarClick("suspects"); break;
                    case Input.Keys.H:
                        showHistory(); break;
                    case Input.Keys.O:
                        showObjectives(); break;
                    case Input.Keys.F5:
                        showSaveMenu(); break;
                    case Input.Keys.F9:
                        showLoadMenu(); break;
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

    private void navigateByDirection(Direction dir) {
        if (roomManager.getCurrentRoom().hasConnection(dir)) {
            handleNavigation(roomManager.getCurrentRoom().getConnection(dir));
        }
    }

    private boolean isCellarUnlocked() {
        return gameState.getWatchedTapes().size() >= 4
            && gameState.getCollectedEvidence().size() >= 5;
    }

    private boolean isMargaretRoomUnlocked() {
        return gameState.hasAnomaly(com.dsa.game.state.EntityAnomaly.BREATHING_WALL);
    }

    private void handleNavigation(Room.RoomID target) {
        // Act structure: Entity gates certain rooms
        if (target == Room.RoomID.CELLAR && !isCellarUnlocked()) {
            NarratorText.Mood mood = NarratorText.getMoodForAwareness(gameState.getAwareness());
            String narratorLine;
            switch (mood) {
                case HOPEFUL: narratorLine = "\"I wouldn't bother, detective. Nothing down there but dust and old wine. Focus on the rooms up here.\""; break;
                case CONFUSED: narratorLine = "\"That door... it won't open. Strange. I don't remember it being locked. Perhaps try the other rooms first?\""; break;
                case ANXIOUS: narratorLine = "\"Don't go down there. Please. Something about the cellar makes my skin crawl. There's nothing for your investigation below.\""; break;
                default: narratorLine = "\"NO! Stay AWAY from the cellar! You don't understand what's down there! I mean... it's irrelevant. Completely irrelevant.\""; break;
            }
            textPanel.show("THE CELLAR\n\nThe door to the cellar won't budge. The handle is ice-cold " +
                "to the touch, far colder than it should be. Something below is keeping this door sealed.\n\n" +
                narratorLine + "\n\n" +
                "[Continue investigating. Gather more evidence and testimony.]");
            panelMode = PanelMode.TEXT;
            return;
        }
        if (target == Room.RoomID.MARGARET_ROOM && !isMargaretRoomUnlocked()) {
            NarratorText.Mood mood = NarratorText.getMoodForAwareness(gameState.getAwareness());
            String narratorLine;
            switch (mood) {
                case HOPEFUL: narratorLine = "\"Margaret's room? She's a witness, not a suspect. Her room won't tell you anything useful.\""; break;
                case CONFUSED: narratorLine = "\"The door won't budge. That's odd. It was open earlier, wasn't it? Perhaps Margaret locked it.\""; break;
                case ANXIOUS: narratorLine = "\"Something is holding that door shut. Not a lock -- something else. Please, detective, leave it alone.\""; break;
                default: narratorLine = "\"THE DOOR IS SEALED! Can't you feel the cold? Whatever is behind that door, they don't -- I mean, there's no reason to go in there!\""; break;
            }
            textPanel.show("MARGARET'S ROOM\n\nThe door is sealed shut. Not locked -- sealed. " +
                "The wood is unnaturally cold. Frost traces the edges of the frame despite the warm hallway.\n\n" +
                narratorLine + "\n\n" +
                "[The Entity's grip may weaken if you investigate deeper into the manor.]");
            panelMode = PanelMode.TEXT;
            return;
        }

        roomManager.navigateTo(target);
        titleFadeTimer = 0;
        descFadeTimer = 0;
        gameState.incrementVisit(target);
        gameState.incrementCommandCount();
        gameState.addEvent("Moved to " + roomManager.getCurrentRoom().getName());

        // +1 awareness per navigation
        String warning = awarenessSystem.addAwareness(1);

        if (gameState.isGameOver()) {
            showGameOver();
            return;
        }

        if (warning != null) {
            textPanel.show("WARNING\n\n" + narratorSystem.getWarning());
            panelMode = PanelMode.TEXT;
        } else {
            // Check for narrator slip (Entity Discovery)
            String narratorSlip = narratorSystem.maybeGetNarratorSlip();
            if (narratorSlip != null) {
                textPanel.show(narratorSlip);
                panelMode = PanelMode.TEXT;
                return;
            }
            // Check for atmospheric events on navigation
            String atmospheric = narratorSystem.maybeGetAtmosphericEvent();
            if (atmospheric != null) {
                textPanel.show(atmospheric);
                panelMode = PanelMode.TEXT;
            }
        }
    }

    // --- Examine ---

    private void handleExamine(String objectName) {
        Room.RoomID currentRoomId = roomManager.getCurrentRoom().getId();
        ExamResult result = examinationSystem.examine(currentRoomId, objectName);
        gameState.incrementCommandCount();
        gameState.addEvent("Examined " + RoomDescriptions.getObjectDisplayName(objectName) + " in " + roomManager.getCurrentRoom().getName());

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
                display.append("\n\n[EVIDENCE FOUND: ").append(result.getEvidence().getDisplayName()).append("]");
                gameState.addEvent("Found evidence: " + result.getEvidence().getDisplayName());
            }
        }
        if (result.hasTape()) {
            boolean isNew = evidenceSystem.collectTape(result.getTape());
            if (isNew) {
                display.append("\n\n[TAPE FOUND: ").append(result.getTape().getTitle()).append("]");
                gameState.addEvent("Found tape: " + result.getTape().getTitle());
            }
        }

        if (warning != null) {
            display.append("\n\n--- ").append(narratorSystem.getWarning());
        }

        textPanel.show(display.toString());
        panelMode = PanelMode.TEXT;
    }

    private void handleMiniGame(ExamResult result, String warning) {
        switch (result.getMiniGame()) {
            case TORN_LETTER_RECONSTRUCTION:
                // Show intro text first, then start mini-game
                textPanel.show(narratorSystem.filterText(result.getText()) + "\n\n[Reconstruct the document to reveal its contents...]");
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
        if (pendingMiniGame == null) return;

        switch (pendingMiniGame) {
            case TORN_LETTER_RECONSTRUCTION:
                documentGame.startTornLetter(
                    // On complete: collect evidence
                    () -> {
                        boolean isNew = evidenceSystem.collect(Evidence.TORN_LETTER);
                        if (isNew) {
                            gameState.addEvent("Found evidence: " + Evidence.TORN_LETTER.getDisplayName());
                        }
                        textPanel.show("Document reconstructed!\n\n\"...cannot allow this betrayal... the will must... James has...\"\n\nThe rest is destroyed, but this fragment speaks of treachery and the will.\n\n[EVIDENCE FOUND: " + Evidence.TORN_LETTER.getDisplayName() + "]");
                        panelMode = PanelMode.TEXT;
                    },
                    // On cancel: no evidence
                    () -> {
                        textPanel.show("You set the fragments aside for now. Perhaps you'll return to them later.");
                        panelMode = PanelMode.TEXT;
                    }
                );
                break;
            default:
                break;
        }
        pendingMiniGame = null;
    }

    // --- Action Bar ---

    private void handleActionBarClick(String action) {
        switch (action) {
            case "inventory":
                showInventory();
                break;
            case "notebook":
                showNotebook();
                break;
            case "suspects":
                showSuspectList();
                break;
            case "hint":
                showHint();
                break;
            case "accuse":
                showAccusation();
                break;
        }
    }

    private void showInventory() {
        String text = evidenceSystem.getInventoryText();
        List<TextButton> buttons = new ArrayList<>();

        // Add PLAY buttons for unwatched tapes
        for (Tape t : gameState.getCollectedTapes()) {
            if (!gameState.hasWatchedTape(t)) {
                buttons.add(new TextButton("PLAY: " + t.getTitle(), 0, 0, 200, 35, "play_tape_" + t.name()));
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

        sb.append("Awareness: ").append(gameState.getAwareness()).append("/").append(GameState.MAX_AWARENESS);
        sb.append(" (").append(awarenessSystem.getLevelName()).append(")\n\n");

        sb.append("Evidence collected: ").append(gameState.getCollectedEvidence().size()).append("/10\n");
        sb.append("Tapes collected: ").append(gameState.getCollectedTapes().size()).append("/8\n");
        sb.append("Tapes watched: ").append(gameState.getWatchedTapes().size()).append("/").append(gameState.getCollectedTapes().size()).append("\n\n");

        // Split contradictions into Physical and Narrator sections
        boolean hasPhysical = false;
        boolean hasNarrator = false;
        for (Contradiction c : gameState.getDiscoveredContradictions()) {
            if (c.isNarratorContradiction()) hasNarrator = true;
            else hasPhysical = true;
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
                sb.append("[META-INSIGHT: The narrator is actively lying to you. Everything he says must be questioned. He's not guiding your investigation -- he's shaping it.]\n\n");
            }
        }

        // Narrator distortions section
        if (!gameState.getNarratorDistortions().isEmpty()) {
            sb.append("=== NARRATOR INCONSISTENCIES ===\n\n");
            for (String distortion : gameState.getNarratorDistortions()) {
                sb.append("* \"").append(distortion).append("\"\n\n");
            }
        }

        // Entity anomalies section
        if (!gameState.getDiscoveredAnomalies().isEmpty()) {
            sb.append("=== ENTITY ANOMALIES (").append(gameState.getAnomalyCount()).append("/7) ===\n\n");
            for (com.dsa.game.state.EntityAnomaly a : gameState.getDiscoveredAnomalies()) {
                sb.append("* ").append(a.getDisplayName()).append("\n");
                sb.append("  ").append(a.getDescription()).append("\n\n");
            }
        }

        sb.append("=== SUSPECT COOPERATION ===\n\n");
        for (Suspect s : Suspect.values()) {
            sb.append(s.getDisplayName()).append(": ").append(gameState.getCooperation(s)).append("%\n");
        }

        if (evidenceSystem.canAccuseJamesAndDaniel()) {
            sb.append("\n[You have enough evidence to make an accusation.]");
        }

        sb.append("\n\n");
        sb.append(achievementSystem.getAchievementsText());

        textPanel.show(sb.toString());
        panelMode = PanelMode.NOTEBOOK;
    }

    private void showSuspectList() {
        List<TextButton> buttons = new ArrayList<>();
        for (Suspect s : Suspect.values()) {
            buttons.add(new TextButton(s.getDisplayName(), 0, 0, 200, 35, "interview_" + s.name()));
        }
        textPanel.showButtons("=== SELECT SUSPECT TO INTERVIEW ===", buttons);
        panelMode = PanelMode.SUSPECT_LIST;
    }

    private void showHint() {
        String hint = narratorSystem.filterText(hintSystem.getHint());
        textPanel.show("=== HINT ===\n\n" + hint);
        panelMode = PanelMode.TEXT;
    }

    private void showAccusation() {
        if (!evidenceSystem.canAccuseJamesAndDaniel()) {
            textPanel.show("=== ACCUSATION ===\n\nYou don't have enough evidence yet.\n\nKeep investigating. Examine objects, collect tapes, and interview suspects to build your case.");
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

        String header = "=== WHO KILLED HAROLD VANCE? ===\n\nSelect your accusation carefully.";
        if (gameState.isClimaxTriggered()) {
            header += "\n\nOr... choose what to do about the Entity.";
        }
        textPanel.showButtons(header, buttons);
        panelMode = PanelMode.ACCUSE_SELECT;
    }

    // --- Panel Actions ---

    private void handlePanelAction(String action) {
        if ("close".equals(action) || "panel_consumed".equals(action)) {
            if ("close".equals(action)) {
                // Climax intercept: show climax text instead of closing
                if (pendingClimax) {
                    pendingClimax = false;
                    gameState.setClimaxTriggered(true);
                    textPanel.show(ClimaxContent.getTape8Climax(gameState.getAnomalyCount()));
                    panelMode = PanelMode.TEXT;
                    return;
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

        // Play tape
        if (action.startsWith("play_tape_")) {
            String tapeName = action.substring("play_tape_".length());
            try {
                Tape tape = Tape.valueOf(tapeName);
                playTape(tape);
            } catch (IllegalArgumentException ignored) {}
            return;
        }

        // Start interview with a suspect
        if (action.startsWith("interview_")) {
            String suspectName = action.substring("interview_".length());
            try {
                Suspect suspect = Suspect.valueOf(suspectName);
                startInterview(suspect);
            } catch (IllegalArgumentException ignored) {}
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
            } catch (IllegalArgumentException ignored) {}
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
            } catch (IllegalArgumentException ignored) {}
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
            textPanel.show(endText);
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
            if (speed <= 20f) TextPanel.setCharsPerSecond(40f);
            else if (speed <= 50f) TextPanel.setCharsPerSecond(80f);
            else if (speed <= 100f) TextPanel.setCharsPerSecond(200f);
            else TextPanel.setCharsPerSecond(20f);
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

    // --- Tape Playing ---

    private void playTape(Tape tape) {
        // Tape 8 is physically damaged — needs repair kit from Margaret's Room
        if (tape == Tape.TAPE_ARTHUR_DEATH && !gameState.hasTapeRepairKit()) {
            textPanel.show("DAMAGED TAPE\n\nThe tape's casing is cracked and the ribbon inside " +
                "is snapped. You can hear fragments rattling loose inside the housing.\n\n" +
                "Someone recorded something important on this tape, but it's been badly damaged -- " +
                "possibly deliberately. You'd need recording equipment to splice the ribbon back together.\n\n" +
                "[Find a way to repair this tape.]");
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

        // Tape 8 (The Opening) triggers the climax sequence - it's Arthur's death recording found in the cellar
        if (tape == Tape.TAPE_ARTHUR_DEATH && !gameState.isClimaxTriggered()) {
            pendingClimax = true;
        }

        textPanel.show(sb.toString());
        panelMode = PanelMode.TAPE_PLAY;
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

        textPanel.show(sb.toString(), buttons);
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
        if (gameState.hasEvidence(Evidence.BLOODSTAINED_CUFF) && !gameState.hasContradiction(Contradiction.BODY_POSITION)) {
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
                    canConfront = gameState.hasEvidence(Evidence.TORN_LETTER) && gameState.hasEvidence(Evidence.WILL_COPY);
                    break;
                case MARCUS:
                    canConfront = gameState.hasEvidence(Evidence.FINANCIAL_RECORDS) || gameState.hasEvidence(Evidence.WILL_COPY);
                    break;
                case CHARLES:
                    canConfront = gameState.hasEvidence(Evidence.FINANCIAL_RECORDS) && gameState.hasEvidence(Evidence.TORN_LETTER);
                    break;
            }
            if (canConfront) {
                buttons.add(new TextButton("CONFRONT", 0, 0, 200, 35, "confront_" + suspect.name()));
            }
        }

        buttons.add(new TextButton("Show Evidence", 0, 0, 200, 35, "show_evidence_list"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));

        textPanel.showButtons("Interview: " + suspect.getDisplayName() + " (Cooperation: " + gameState.getCooperation(suspect) + "%)", buttons);
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
        }
        if (warning != null) {
            sb.append("\n\n--- ").append(narratorSystem.getWarning());
        }

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));

        textPanel.show(sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    private void showEvidenceForInterview() {
        List<TextButton> buttons = new ArrayList<>();
        for (Evidence e : gameState.getCollectedEvidence()) {
            buttons.add(new TextButton(e.getDisplayName(), 0, 0, 200, 35, "evidence_" + e.name()));
        }
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));

        if (gameState.getCollectedEvidence().isEmpty()) {
            textPanel.showButtons("No evidence to show.\nCollect evidence by examining objects.", buttons);
        } else {
            textPanel.showButtons("Select evidence to show " + interviewSystem.getCurrentSuspect().getDisplayName() + ":", buttons);
        }
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

        textPanel.show(sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    private void handleWeaponContradiction() {
        String warning = awarenessSystem.addAwareness(2);
        String response = interviewSystem.presentWeaponContradiction();
        gameState.addEvent("Discovered contradiction: Weapon");

        if (gameState.isGameOver()) { showGameOver(); return; }

        StringBuilder sb = new StringBuilder(response);
        String bleedW = narratorSystem.maybeGetChannelingBleedThrough();
        if (bleedW != null) sb.append("\n\n").append(bleedW);
        if (warning != null) sb.append("\n\n--- ").append(narratorSystem.getWarning());

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));
        textPanel.show(sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    private void handleBodyContradiction() {
        String warning = awarenessSystem.addAwareness(2);
        String response = interviewSystem.presentBodyContradiction();
        gameState.addEvent("Discovered contradiction: Body position");

        if (gameState.isGameOver()) { showGameOver(); return; }

        StringBuilder sb = new StringBuilder(response);
        String bleedB = narratorSystem.maybeGetChannelingBleedThrough();
        if (bleedB != null) sb.append("\n\n").append(bleedB);
        if (warning != null) sb.append("\n\n--- ").append(narratorSystem.getWarning());

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));
        textPanel.show(sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    // --- Confrontation ---

    private void handleConfrontation(Suspect suspect) {
        String warning = awarenessSystem.addAwareness(3);
        String response = interviewSystem.presentConfrontation(suspect);
        gameState.markConfronted(suspect);
        gameState.addEvent("Confronted " + suspect.getDisplayName());

        if (gameState.isGameOver()) { showGameOver(); return; }

        StringBuilder sb = new StringBuilder(response);
        String bleedC = narratorSystem.maybeGetChannelingBleedThrough();
        if (bleedC != null) sb.append("\n\n").append(bleedC);
        if (warning != null) sb.append("\n\n--- ").append(narratorSystem.getWarning());

        List<TextButton> buttons = new ArrayList<>();
        buttons.add(new TextButton("Back to Topics", 0, 0, 200, 35, "back_to_topics"));
        buttons.add(new TextButton("End Interview", 0, 0, 200, 35, "end_interview"));
        textPanel.show(sb.toString(), buttons);
        panelMode = PanelMode.INTERVIEW;
    }

    // --- Accusation ---

    private void handleAccusation(String action) {
        gameState.setAccusationMade(true);
        String accusationTarget = action.replace("accuse_", "").replace("_", " ");
        gameState.addEvent("Made accusation: " + accusationTarget);

        if ("accuse_james_daniel".equals(action)) {
            gameState.setGameWon(true);

            StringBuilder winText = new StringBuilder();
            winText.append("=== CASE SOLVED ===\n\n");
            winText.append("You present your evidence to the authorities. James Vance and Daniel the groundskeeper ");
            winText.append("are arrested for the murder of Harold Vance.\n\n");
            winText.append("The evidence is overwhelming: James, facing disinheritance after his father discovered ");
            winText.append("the embezzlement, conspired with Daniel -- who had been laundering the money -- to kill ");
            winText.append("Harold before the new will could be signed.\n\n");
            winText.append("James killed the drugged Harold with the fireplace poker. Later, Daniel entered through ");
            winText.append("the study window to help move the body to the cellar. But they were sloppy. They left too many traces.\n\n");
            winText.append("Margaret is cleared. Marcus Blackwood and Charles Webb cooperate fully with the investigation.\n\n");

            // Variant text by evidence completeness
            int evidenceCount = gameState.getCollectedEvidence().size();
            int watchedCount = gameState.getWatchedTapes().size();
            int tapeCount = gameState.getCollectedTapes().size();
            if (evidenceCount >= 10 && watchedCount >= 8) {
                winText.append("Your case is IRONCLAD. Every piece of evidence accounted for, every tape reviewed. ");
                winText.append("The prosecution has no gaps to exploit. James and Daniel will face the full weight of justice.\n\n");
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
            winText.append("Final Awareness: ").append(awareness).append("/").append(GameState.MAX_AWARENESS).append("\n");
            winText.append("Commands used: ").append(gameState.getCommandCount());

            // Check achievements
            java.util.List<com.dsa.game.state.Achievement> newAchievements = achievementSystem.checkOnWin();
            if (!newAchievements.isEmpty()) {
                winText.append("\n\n=== ACHIEVEMENTS UNLOCKED ===\n\n");
                for (com.dsa.game.state.Achievement a : newAchievements) {
                    winText.append("* ").append(a.getDisplayName()).append(" -- ").append(a.getDescription()).append("\n");
                }
            }

            textPanel.show(winText.toString());
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

            gameState.setAccusationMade(false); // allow retry
            textPanel.show(sb.toString());
            panelMode = PanelMode.TEXT;
        }
    }

    // --- Settings ---

    private void showSettings() {
        List<TextButton> buttons = new ArrayList<>();

        float speed = TextPanel.getCharsPerSecond();
        String speedLabel;
        if (speed <= 20f) speedLabel = "Slow";
        else if (speed <= 50f) speedLabel = "Normal";
        else if (speed <= 100f) speedLabel = "Fast";
        else speedLabel = "Instant";
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
        sb.append("Awareness: ").append(gameState.getAwareness()).append("/").append(GameState.MAX_AWARENESS).append("\n");
        sb.append("Commands used: ").append(gameState.getCommandCount());

        textPanel.show(sb.toString());
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
        sb.append("Awareness: ").append(gameState.getAwareness()).append("/").append(GameState.MAX_AWARENESS).append("\n");
        sb.append("Commands used: ").append(gameState.getCommandCount());

        // Check ending achievements
        List<Achievement> newAchievements = achievementSystem.checkOnEnding(ending);
        if (!newAchievements.isEmpty()) {
            sb.append("\n\n=== ACHIEVEMENTS UNLOCKED ===\n\n");
            for (Achievement a : newAchievements) {
                sb.append("* ").append(a.getDisplayName()).append(" -- ").append(a.getDescription()).append("\n");
            }
        }

        textPanel.show(sb.toString());
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
            sb.append("* Find and watch tapes (").append(tapeCount).append("/8 found, ").append(watchedCount).append(" watched)\n");
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
     * Load a save file into this GameScreen. Called by TitleScreen after construction.
     */
    public void loadFromSave(String slotName) {
        Room.RoomID roomId = saveLoadSystem.load(gameState, slotName);
        if (roomId != null) {
            roomManager.navigateTo(roomId);
            pendingClimax = false;
        }
    }

    // --- Game Over ---

    private void showGameOver() {
        textPanel.show("=== GAME OVER ===\n\n" +
            "The household has closed ranks against you. Doors are locked, " +
            "witnesses refuse to speak, and evidence begins to disappear.\n\n" +
            "You've been asked to leave Vance Manor. The murder of Harold Vance " +
            "will remain unsolved.\n\n" +
            "The killers go free.\n\n" +
            "Awareness reached " + gameState.getAwareness() + "/" + GameState.MAX_AWARENESS + ".\n" +
            "Evidence collected: " + gameState.getCollectedEvidence().size() + "/10\n" +
            "Tapes collected: " + gameState.getCollectedTapes().size() + "/8");
        panelMode = PanelMode.TEXT;
    }

    // --- Render ---

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(game.camera.combined);

        Room currentRoom = roomManager.getCurrentRoom();

        // Reset fade timers on room change
        if (lastRenderedRoom != currentRoom.getId()) {
            lastRenderedRoom = currentRoom.getId();
            titleFadeTimer = 0;
            descFadeTimer = 0;
        }

        // Advance fade timers
        titleFadeTimer += delta;
        descFadeTimer += delta;

        batch.begin();

        // Draw room background
        Texture roomTex = roomTextures.get(currentRoom.getId());
        if (roomTex != null) {
            batch.draw(roomTex, 0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
        }

        // Draw subtle outline glow on hovered hotspots
        for (Hotspot hotspot : currentRoom.getHotspots()) {
            if (hotspot.isHovered()) {
                Rectangle hb = hotspot.getBounds();
                float glowAlpha = 0.35f;
                // Outer glow (2px border)
                batch.setColor(0.9f, 0.85f, 0.6f, glowAlpha * 0.4f);
                batch.draw(pixelTexture, hb.x - 2, hb.y - 2, hb.width + 4, 2);                      // bottom
                batch.draw(pixelTexture, hb.x - 2, hb.y + hb.height, hb.width + 4, 2);               // top
                batch.draw(pixelTexture, hb.x - 2, hb.y, 2, hb.height);                              // left
                batch.draw(pixelTexture, hb.x + hb.width, hb.y, 2, hb.height);                       // right
                // Inner glow (1px border)
                batch.setColor(0.95f, 0.9f, 0.7f, glowAlpha);
                batch.draw(pixelTexture, hb.x, hb.y, hb.width, 1);                                   // bottom
                batch.draw(pixelTexture, hb.x, hb.y + hb.height - 1, hb.width, 1);                   // top
                batch.draw(pixelTexture, hb.x, hb.y, 1, hb.height);                                  // left
                batch.draw(pixelTexture, hb.x + hb.width - 1, hb.y, 1, hb.height);                   // right
                batch.setColor(Color.WHITE);
            }
        }

        // Draw room name with text shadow (top-left, fades out)
        float titleAlpha = MathUtils.clamp(1f - (titleFadeTimer - TITLE_FADE_DURATION), 0f, 1f);
        if (titleAlpha > 0) {
            layout.setText(titleFont, currentRoom.getName());
            float titleX = 20;
            float titleY = DSAGame.SCREEN_HEIGHT - 20;

            // Shadow
            titleFont.setColor(0, 0, 0, titleAlpha * 0.7f);
            titleFont.draw(batch, currentRoom.getName(), titleX + 2, titleY - 2);
            // Text
            titleFont.setColor(0.95f, 0.95f, 0.9f, titleAlpha);
            titleFont.draw(batch, currentRoom.getName(), titleX, titleY);
            titleFont.setColor(Color.WHITE);
        }

        // Draw tooltip
        if (!currentTooltip.isEmpty() && !textPanel.isVisible()) {
            layout.setText(font, currentTooltip);
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);

            float tooltipW = layout.width + 16;
            float tooltipH = layout.height + 12;
            float tooltipX = touchPos.x + 15;
            float tooltipY = touchPos.y + 15;

            // Clamp to screen bounds
            if (tooltipX + tooltipW > DSAGame.SCREEN_WIDTH - 5) {
                tooltipX = DSAGame.SCREEN_WIDTH - tooltipW - 5;
            }
            if (tooltipY + tooltipH > DSAGame.SCREEN_HEIGHT - 5) {
                tooltipY = touchPos.y - tooltipH - 5;
            }
            if (tooltipX < 5) tooltipX = 5;
            if (tooltipY < 5) tooltipY = 5;

            // Dark teal-black background
            batch.setColor(0.06f, 0.1f, 0.1f, 0.92f);
            batch.draw(pixelTexture, tooltipX, tooltipY, tooltipW, tooltipH);

            // Muted gold border
            batch.setColor(0.6f, 0.55f, 0.35f, 0.8f);
            batch.draw(pixelTexture, tooltipX, tooltipY, tooltipW, 1);                    // bottom
            batch.draw(pixelTexture, tooltipX, tooltipY + tooltipH - 1, tooltipW, 1);     // top
            batch.draw(pixelTexture, tooltipX, tooltipY, 1, tooltipH);                    // left
            batch.draw(pixelTexture, tooltipX + tooltipW - 1, tooltipY, 1, tooltipH);     // right

            // Cream text
            batch.setColor(Color.WHITE);
            font.setColor(0.95f, 0.93f, 0.85f, 1f);
            font.draw(batch, currentTooltip, tooltipX + 8, tooltipY + tooltipH - 6);
            font.setColor(Color.WHITE);
        }

        // Draw room description with text shadow (fades out), positioned above action bar
        float descAlpha = MathUtils.clamp(1f - (descFadeTimer - DESC_FADE_DURATION), 0f, 1f);
        if (descAlpha > 0) {
            String description = RoomDescriptions.getDescription(
                currentRoom.getId(),
                gameState.getVisitCount(currentRoom.getId()),
                gameState.getAwareness()
            );
            float descX = 20;
            float descY = actionBar.getBarHeight() + 30;

            // Shadow
            font.setColor(0, 0, 0, descAlpha * 0.7f);
            font.draw(batch, description, descX + 1, descY - 1);
            // Text
            font.setColor(0.9f, 0.9f, 0.85f, descAlpha);
            font.draw(batch, description, descX, descY);
            font.setColor(Color.WHITE);
        }

        batch.setColor(Color.WHITE);

        // Draw awareness meter
        awarenessMeter.render(batch, font, awarenessSystem, gameState.getAwareness());

        // Draw action bar
        actionBar.render(batch, font);

        // Update and draw text panel (on top of everything)
        textPanel.update(delta);
        textPanel.render(batch, font);

        // Draw mini-game overlay (on top of text panel)
        if (documentGame.isActive()) {
            documentGame.render(batch, font);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        font.dispose();
        titleFont.dispose();
        for (Texture tex : roomTextures.values()) tex.dispose();
        pixelTexture.dispose();
        textPanel.dispose();
        awarenessMeter.dispose();
        actionBar.dispose();
        documentGame.dispose();
        TextButton.disposeTextures();
    }
}
