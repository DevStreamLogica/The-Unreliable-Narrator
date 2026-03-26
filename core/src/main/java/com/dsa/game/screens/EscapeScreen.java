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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.dsa.game.DSAGame;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EscapeScreen implements Screen {

    // ── Constants ────────────────────────────────────────────────────────────
    private static final float TOTAL_TIME = 60f;
    private static final float PLAYER_SPEED = 280f;
    private static final float FRAME_DUR = 0.12f;
    private static final float PLAYER_W = 96f;
    private static final float PLAYER_H = 96f;
    private static final float TAPE_SIZE = 36f;
    private static final float PICKUP_RADIUS = 55f;
    private static final int TOTAL_TAPES = 7;

    // 8-directional animated sprites, 8 frames each
    // dir indices: 0=S 1=SE 2=E 3=NE 4=N 5=NW 6=W 7=SW
    private static final int DIR_COUNT = 8;
    private static final int DIR_FRAMES = 8;
    private static final float DIR_FRAME_DUR = 0.1f;
    private static final String[][] DIR_FRAME_PATHS = new String[DIR_COUNT][DIR_FRAMES];
    static {
        String[] names = {
                "walk_south", "walk_southeast", "walk_east", "walk_northeast",
                "walk_north", "walk_northwest", "walk_west", "walk_southwest"
        };
        for (int d = 0; d < DIR_COUNT; d++)
            for (int f = 0; f < DIR_FRAMES; f++)
                DIR_FRAME_PATHS[d][f] = "rooms/endings/" + names[d] + "/" + names[d] + "_" + f + ".png";
    }

    // ── Enums / inner classes ─────────────────────────────────────────────────
    public enum EscapeRoom {
        CELLAR, KITCHEN, ENTRANCE, STUDY, PARLOR,
        GUEST_ROOMS, MARGARET_ROOM, JAMES_ROOM,
        SERVANTS_QUARTERS, SHED
    }

    private static class TapeItem {
        final String id;
        final EscapeRoom room;
        final float x, y;
        final float pickupRadius;
        boolean collected = false;

        TapeItem(String id, EscapeRoom room, float x, float y) {
            this(id, room, x, y, PICKUP_RADIUS);
        }

        TapeItem(String id, EscapeRoom room, float x, float y, float pickupRadius) {
            this.id = id;
            this.room = room;
            this.x = x;
            this.y = y;
            this.pickupRadius = pickupRadius;
        }
    }

    private static class ExitZone {
        final Rectangle bounds;
        final EscapeRoom target; // null = win door
        final float spawnX, spawnY;
        final boolean requiresAll;

        ExitZone(Rectangle b, EscapeRoom t, float sx, float sy, boolean req) {
            bounds = b;
            target = t;
            spawnX = sx;
            spawnY = sy;
            requiresAll = req;
        }

        ExitZone(Rectangle b, EscapeRoom t, float sx, float sy) {
            this(b, t, sx, sy, false);
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private final DSAGame game;
    private SpriteBatch batch;
    private FitViewport viewport;
    private BitmapFont font;
    private BitmapFont timerFont;
    private BitmapFont bigFont;

    // player
    private final Texture[][] dirTex = new Texture[DIR_COUNT][DIR_FRAMES]; // [dir][frame]
    private float playerX, playerY;
    private float bobTimer = 0f;
    private int currentDir = 0; // 0=south default
    private boolean moving = false;
    private int dirFrameIdx = 0;
    private float dirFrameTimer = 0f;

    // rooms
    private EscapeRoom currentRoom;
    private final Map<EscapeRoom, Texture> roomTex = new EnumMap<>(EscapeRoom.class);
    private final Map<EscapeRoom, Texture> roomTexCollected = new EnumMap<>(EscapeRoom.class);
    private final Map<EscapeRoom, List<ExitZone>> exits = new EnumMap<>(EscapeRoom.class);
    private final Map<EscapeRoom, List<Rectangle>> walkable = new EnumMap<>(EscapeRoom.class);

    // tapes
    private final List<TapeItem> tapes = new ArrayList<>();
    private int collectedCount = 0;

    // timer / state
    private float timeRemaining = TOTAL_TIME;
    private boolean gameEnded = false;
    private boolean won = false;

    // fade transition
    private boolean fading = false;
    private boolean fadingOut = true;
    private float fadeAlpha = 0f;
    private EscapeRoom pendingRoom;
    private float pendingX, pendingY;

    // end-state delay (show overlay before switching screen)
    private float endTimer = 0f;
    private static final float END_DELAY = 3f;

    // utility textures
    private Texture pixelTex;
    private Texture tapeTex;

    // debug draw tool
    private boolean dbgDrawing = false;
    private float dbgX1, dbgY1, dbgX2, dbgY2; // live drag coords
    private final List<float[]> dbgRects = new ArrayList<>(); // locked rects [x1,y1,x2,y2]

    // sorting puzzle
    private boolean sortingActive = false;
    private String[] sortArr;
    private int sortKeyIndex;
    private String sortMessage = "";
    private float sortMessageTimer = 0f;
    private boolean sortDragging = false;
    private float sortDragX, sortDragY;
    private int sortDropTarget;

    // Scrambled starting order — insertion sort will produce chronological order
    private static final String[] SORT_INITIAL = {
            "TAPE_MARCUS_INTERVIEW",
            "TAPE_ARGUMENT",
            "TAPE_MARGARET_ACCOUNT",
            "TAPE_MARGARET_INTERVIEW",
            "TAPE_CHARLES_INTERVIEW",
            "TAPE_JAMES_INTERVIEW",
            "TAPE_DANIEL_INTERVIEW"
    };

    // ── Constructor ───────────────────────────────────────────────────────────
    public EscapeScreen(DSAGame game) {
        this.game = game;
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────
    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new FitViewport(DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
        viewport.apply();

        font = new BitmapFont();
        font.getData().setScale(1.8f);
        timerFont = new BitmapFont();
        timerFont.getData().setScale(3f);
        bigFont = new BitmapFont();
        bigFont.getData().setScale(4f);

        buildUtilTextures();
        loadRoomTextures();
        loadPlayerSprites();
        setupExitZones();
        setupWalkableAreas();
        setupTapes();

        currentRoom = EscapeRoom.CELLAR;
        playerX = 1050f;
        playerY = 280f;

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int sx, int sy, int ptr, int btn) {
                if (btn != Input.Buttons.LEFT)
                    return false;
                com.badlogic.gdx.math.Vector2 w = viewport.unproject(new com.badlogic.gdx.math.Vector2(sx, sy));
                dbgX1 = w.x;
                dbgY1 = w.y;
                dbgX2 = w.x;
                dbgY2 = w.y;
                dbgDrawing = true;
                return true;
            }

            @Override
            public boolean touchDragged(int sx, int sy, int ptr) {
                if (!dbgDrawing)
                    return false;
                com.badlogic.gdx.math.Vector2 w = viewport.unproject(new com.badlogic.gdx.math.Vector2(sx, sy));
                dbgX2 = w.x;
                dbgY2 = w.y;
                return true;
            }

            @Override
            public boolean touchUp(int sx, int sy, int ptr, int btn) {
                if (btn == Input.Buttons.RIGHT) {
                    com.badlogic.gdx.math.Vector2 w = viewport.unproject(new com.badlogic.gdx.math.Vector2(sx, sy));
                    for (int i = dbgRects.size() - 1; i >= 0; i--) {
                        float[] r = dbgRects.get(i);
                        if (w.x >= r[0] && w.x <= r[2] && w.y >= r[1] && w.y <= r[3]) {
                            dbgRects.remove(i);
                            break;
                        }
                    }
                    return true;
                }
                if (!dbgDrawing || btn != Input.Buttons.LEFT)
                    return false;
                com.badlogic.gdx.math.Vector2 w = viewport.unproject(new com.badlogic.gdx.math.Vector2(sx, sy));
                dbgX2 = w.x;
                dbgY2 = w.y;
                dbgRects.add(new float[] {
                        Math.min(dbgX1, dbgX2), Math.min(dbgY1, dbgY2),
                        Math.max(dbgX1, dbgX2), Math.max(dbgY1, dbgY2)
                });
                dbgDrawing = false;
                return true;
            }
        });
    }

    // ── Asset loading ─────────────────────────────────────────────────────────
    private void buildUtilTextures() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        pixelTex = new Texture(p);
        p.dispose();

        Pixmap tp = new Pixmap(36, 36, Pixmap.Format.RGBA8888);
        tp.setColor(new Color(0.8f, 0.1f, 0.1f, 1f));
        tp.fill();
        tp.setColor(Color.WHITE);
        tp.fillRectangle(5, 5, 26, 26);
        tp.setColor(new Color(0.6f, 0f, 0f, 1f));
        tp.fillRectangle(10, 10, 16, 16);
        tapeTex = new Texture(tp);
        tp.dispose();
    }

    private void loadRoomTextures() {
        load(EscapeRoom.CELLAR, "rooms/endings/cellar.png");
        load(EscapeRoom.KITCHEN, "rooms/endings/kitchen.png");
        load(EscapeRoom.ENTRANCE, "rooms/endings/entrance.png");
        load(EscapeRoom.STUDY, "rooms/endings/study.png");
        load(EscapeRoom.PARLOR, "rooms/endings/parlor.png");
        load(EscapeRoom.GUEST_ROOMS, "rooms/endings/hallway.png");
        load(EscapeRoom.MARGARET_ROOM, "rooms/endings/margaret.png");
        load(EscapeRoom.JAMES_ROOM, "rooms/endings/james.png");
        load(EscapeRoom.SERVANTS_QUARTERS, "rooms/endings/servant_quarters.png");
        load(EscapeRoom.SHED, "rooms/endings/shed.png");
    }

    private void load(EscapeRoom room, String path) {
        if (Gdx.files.internal(path).exists())
            roomTex.put(room, new Texture(Gdx.files.internal(path)));
    }

    private void loadCollected(EscapeRoom room, String path) {
        if (Gdx.files.internal(path).exists())
            roomTexCollected.put(room, new Texture(Gdx.files.internal(path)));
    }

    private void loadPlayerSprites() {
        for (int d = 0; d < DIR_COUNT; d++) {
            for (int f = 0; f < DIR_FRAMES; f++) {
                String path = DIR_FRAME_PATHS[d][f];
                if (Gdx.files.internal(path).exists())
                    dirTex[d][f] = new Texture(Gdx.files.internal(path));
            }
        }
    }

    // ── Exit zones ────────────────────────────────────────────────────────────
    private void setupExitZones() {
        // CELLAR: stairs (right edge) → KITCHEN
        addExit(EscapeRoom.CELLAR,
                rect(1000, 127, 180, 140), EscapeRoom.KITCHEN, 1040, 313);

        // KITCHEN: bottom door → ENTRANCE; left stairs → SERVANTS_QUARTERS; right
        // stairs → CELLAR
        addExit(EscapeRoom.KITCHEN,
                zone(600, 60, 715, 61), EscapeRoom.ENTRANCE, 444, 410);
        addExit(EscapeRoom.KITCHEN,
                zone(567, 618, 683, 628), EscapeRoom.SERVANTS_QUARTERS, 223, 482);
        addExit(EscapeRoom.KITCHEN,
                zone(1162, 267, 1217, 443), EscapeRoom.CELLAR, 1050, 300);

        // ENTRANCE: left → PARLOR; right → STUDY; top → GUEST_ROOMS; bottom → KITCHEN;
        // front door (win)
        addExit(EscapeRoom.ENTRANCE,
                zone(198, 593, 298, 594), EscapeRoom.PARLOR, 324, 155);
        addExit(EscapeRoom.ENTRANCE,
                zone(1003, 593, 1005, 594), EscapeRoom.STUDY, 612, 91);
        addExit(EscapeRoom.ENTRANCE,
                zone(446, 631, 547, 632), EscapeRoom.GUEST_ROOMS, 579, 330);
        addExit(EscapeRoom.ENTRANCE,
                zone(446, 506, 547, 507), EscapeRoom.KITCHEN, 630, 118);
        addExitWin(EscapeRoom.ENTRANCE,
                zone(556, 108, 737, 109));

        // STUDY: bottom → ENTRANCE
        addExit(EscapeRoom.STUDY,
                zone(552, 0, 720, 10), EscapeRoom.ENTRANCE, 1000, 497);

        // PARLOR: right → ENTRANCE
        addExit(EscapeRoom.PARLOR,
                zone(275, 69, 384, 79), EscapeRoom.ENTRANCE, 200, 497);

        // GUEST_ROOMS: bottom → ENTRANCE; left → MARGARET; right → JAMES
        addExit(EscapeRoom.GUEST_ROOMS,
                zone(612, 501, 659, 511), EscapeRoom.ENTRANCE, 558, 536);
        addExit(EscapeRoom.GUEST_ROOMS,
                zone(379, 69, 389, 210), EscapeRoom.MARGARET_ROOM, 918, 162);
        addExit(EscapeRoom.GUEST_ROOMS,
                zone(876, 79, 886, 207), EscapeRoom.JAMES_ROOM, 179, 120);

        // MARGARET_ROOM: right → GUEST_ROOMS
        addExit(EscapeRoom.MARGARET_ROOM,
                zone(1068, 116, 1078, 257), EscapeRoom.GUEST_ROOMS, 513, 84);

        // JAMES_ROOM: left → GUEST_ROOMS
        addExit(EscapeRoom.JAMES_ROOM,
                zone(69, 71, 79, 197), EscapeRoom.GUEST_ROOMS, 740, 93);

        // SERVANTS_QUARTERS: right → KITCHEN; bottom door → SHED
        addExit(EscapeRoom.SERVANTS_QUARTERS,
                zone(209, 607, 324, 647), EscapeRoom.KITCHEN, 591, 492);
        addExit(EscapeRoom.SERVANTS_QUARTERS,
                zone(1066, 176, 1076, 316), EscapeRoom.SHED, 640, 120);

        // SHED: top → SERVANTS_QUARTERS
        addExit(EscapeRoom.SHED,
                zone(1066, 176, 1076, 316), EscapeRoom.SERVANTS_QUARTERS, 640, 120);
    }

    private void addExit(EscapeRoom from, Rectangle b, EscapeRoom to, float sx, float sy) {
        exits.computeIfAbsent(from, k -> new ArrayList<>()).add(new ExitZone(b, to, sx, sy));
    }

    private void addExitWin(EscapeRoom from, Rectangle b) {
        exits.computeIfAbsent(from, k -> new ArrayList<>()).add(new ExitZone(b, null, 0, 0, true));
    }

    private static Rectangle rect(float x, float y, float w, float h) {
        return new Rectangle(x, y, w, h);
    }

    /** x1,y1 = start corner x2,y2 = end corner */
    private static Rectangle zone(float x1, float y1, float x2, float y2) {
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    // ── Walkable areas ────────────────────────────────────────────────────────
    private void setupWalkableAreas() {
        // Multiple rectangles per room approximate irregular floor shapes.
        // Blue overlay in debug mode shows the combined walkable area.

        // CELLAR — open floor, avoid wine rack top-left and barrel cluster
        addWalk(EscapeRoom.CELLAR, rect(993, 263, 145, 300)); // left-bottom past barrels
        addWalk(EscapeRoom.CELLAR, rect(940, 468, 156, 132)); // left-bottom past barrels
        addWalk(EscapeRoom.CELLAR, rect(619, 521, 400, 81)); // left-bottom past barrels
        addWalk(EscapeRoom.CELLAR, rect(195, 424, 748, 98)); // top strip
        addWalk(EscapeRoom.CELLAR, rect(312, 347, 632, 100)); // right half open floor
        addWalk(EscapeRoom.CELLAR, rect(400, 262, 544, 100)); // left-bottom past barrels
        addWalk(EscapeRoom.CELLAR, rect(492, 139, 119, 200)); // left-bottom past barrels
        addWalk(EscapeRoom.CELLAR, rect(621, 223, 87, 200)); // left-bottom past barrels

        // KITCHEN — wide tile floor, avoid counters on all edges
        addWalk(EscapeRoom.KITCHEN, zone(400, 61, 1058, 170)); // main floor
        addWalk(EscapeRoom.KITCHEN, zone(302, 164, 702, 551)); // main floor
        addWalk(EscapeRoom.KITCHEN, zone(542, 546, 697, 609)); // main floor
        addWalk(EscapeRoom.KITCHEN, zone(564, 608, 673, 648)); // main floor
        addWalk(EscapeRoom.KITCHEN, zone(730, 158, 1079, 525)); // main floor
        addWalk(EscapeRoom.KITCHEN, zone(992, 219, 1112, 610)); // main floor
        addWalk(EscapeRoom.KITCHEN, zone(603, 35, 713, 65)); // main floor
        addWalk(EscapeRoom.KITCHEN, zone(695, 164, 738, 498)); // main floor
        addWalk(EscapeRoom.KITCHEN, zone(1106, 277, 1164, 431)); // main floor
        addWalk(EscapeRoom.KITCHEN, zone(686, 410, 778, 530)); // main floor

        // ENTRANCE — staircase shape
        addWalk(EscapeRoom.ENTRANCE, zone(194, 260, 1108, 364)); // main floor
        addWalk(EscapeRoom.ENTRANCE, zone(410, 359, 901, 388)); // main floor
        addWalk(EscapeRoom.ENTRANCE, zone(417, 366, 735, 496)); // main floor
        addWalk(EscapeRoom.ENTRANCE, zone(823, 355, 908, 618)); // main floor
        addWalk(EscapeRoom.ENTRANCE, zone(400, 518, 825, 634)); // main floor
        addWalk(EscapeRoom.ENTRANCE, zone(1005, 103, 1111, 612)); // main floor
        addWalk(EscapeRoom.ENTRANCE, zone(196, 100, 301, 619)); // main floor
        addWalk(EscapeRoom.ENTRANCE, zone(407, 103, 905, 266)); // main floor
        addWalk(EscapeRoom.ENTRANCE, zone(418, 481, 560, 511)); // main floor

        // STUDY — open floor around desk and fireplace
        addWalk(EscapeRoom.STUDY, zone(389, 62, 918, 178)); // main floor
        addWalk(EscapeRoom.STUDY, zone(188, 0, 496, 120)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(770, 0, 960, 86)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(348, 95, 464, 209)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(373, 204, 474, 253)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(402, 229, 470, 339)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(418, 337, 502, 363)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(462, 64, 801, 201)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(416, 353, 875, 411)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(422, 363, 856, 456)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(769, 299, 877, 369)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(783, 243, 904, 305)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(784, 160, 914, 252)); // doorway back to entrance
        addWalk(EscapeRoom.STUDY, zone(560, 0, 716, 78)); // doorway back to entrance

        // PARLOR — open sitting room
        addWalk(EscapeRoom.PARLOR, zone(234, 106, 375, 579)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(374, 132, 1030, 264)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(370, 106, 544, 140)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(744, 107, 1028, 143)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(1024, 107, 1091, 170)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(276, 62, 387, 112)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(904, 108, 978, 578)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(374, 394, 767, 490)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(369, 484, 519, 579)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(961, 135, 1037, 507)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(763, 393, 932, 580)); // main floor
        addWalk(EscapeRoom.PARLOR, zone(510, 452, 786, 496)); // main floor

        // GUEST_ROOMS (hallway) — narrow corridor with staircase
        addWalk(EscapeRoom.GUEST_ROOMS, zone(607, 156, 666, 510)); // corridor floor
        addWalk(EscapeRoom.GUEST_ROOMS, zone(374, 70, 894, 162)); // staircase landing
        addWalk(EscapeRoom.GUEST_ROOMS, zone(439, 0, 834, 78)); // staircase landing
        addWalk(EscapeRoom.GUEST_ROOMS, zone(370, 60, 390, 210)); // left door to margaret
        addWalk(EscapeRoom.GUEST_ROOMS, zone(376, 162, 530, 210)); // left door to margaret
        addWalk(EscapeRoom.GUEST_ROOMS, zone(870, 60, 890, 210)); // right door to james
        addWalk(EscapeRoom.GUEST_ROOMS, zone(727, 157, 885, 212)); // right door to james
        addWalk(EscapeRoom.GUEST_ROOMS, zone(518, 137, 754, 185)); // right door to james

        // MARGARET_ROOM — open bedroom floor
        addWalk(EscapeRoom.MARGARET_ROOM, zone(324, 5, 467, 549));
        addWalk(EscapeRoom.MARGARET_ROOM, zone(332, 2, 958, 127)); // exit door
        addWalk(EscapeRoom.MARGARET_ROOM, zone(836, 105, 960, 460)); // exit door
        addWalk(EscapeRoom.MARGARET_ROOM, zone(952, 123, 1075, 275)); // exit door

        // JAMES_ROOM — open bedroom floor
        addWalk(EscapeRoom.JAMES_ROOM, zone(71, 96, 1074, 215));
        addWalk(EscapeRoom.JAMES_ROOM, zone(152, 174, 285, 602)); // exit door
        addWalk(EscapeRoom.JAMES_ROOM, zone(283, 174, 445, 517)); // exit door
        addWalk(EscapeRoom.JAMES_ROOM, zone(340, 0, 1076, 97)); // exit door
        addWalk(EscapeRoom.JAMES_ROOM, zone(849, 175, 1073, 509)); // exit door

        // SERVANTS_QUARTERS — floor between beds
        addWalk(EscapeRoom.SERVANTS_QUARTERS, zone(207, 324, 296, 618)); // passage to kitchen
        addWalk(EscapeRoom.SERVANTS_QUARTERS, zone(196, 0, 280, 326)); // passage to kitchen
        addWalk(EscapeRoom.SERVANTS_QUARTERS, zone(177, 0, 1030, 98)); // passage to kitchen
        addWalk(EscapeRoom.SERVANTS_QUARTERS, zone(416, 87, 964, 310)); // passage to kitchen
        addWalk(EscapeRoom.SERVANTS_QUARTERS, zone(953, 0, 1015, 130)); // passage to kitchen
        addWalk(EscapeRoom.SERVANTS_QUARTERS, zone(635, 307, 765, 464)); // passage to kitchen
        addWalk(EscapeRoom.SERVANTS_QUARTERS, zone(960, 115, 1077, 243)); // passage to kitchen
        addWalk(EscapeRoom.SERVANTS_QUARTERS, zone(948, 0, 1026, 137)); // passage to kitchen
        addWalk(EscapeRoom.SERVANTS_QUARTERS, zone(958, 240, 1067, 266)); // passage to kitchen

        // SHED — simple open floor
        addWalk(EscapeRoom.SHED, zone(100, 80, 1180, 620));
    }

    private void addWalk(EscapeRoom room, Rectangle r) {
        walkable.computeIfAbsent(room, k -> new ArrayList<>()).add(r);
    }

    /**
     * Returns true if the player feet point (bottom-centre) is inside any walkable
     * rect.
     */
    private boolean isWalkable(float px, float py) {
        float centerX = px + PLAYER_W / 2f;
        float feet = py; // southernmost point
        float head = py + PLAYER_H; // northernmost point
        List<Rectangle> areas = walkable.get(currentRoom);
        if (areas == null)
            return true;
        boolean feetOk = false, headOk = false;
        for (Rectangle r : areas) {
            if (r.contains(centerX, feet))
                feetOk = true;
            if (r.contains(centerX, head))
                headOk = true;
        }
        return feetOk && headOk;
    }

    // ── Tape setup ────────────────────────────────────────────────────────────
    private void setupTapes() {
        // Study — under desk (left) + bookshelves (right) — off-floor, larger pickup
        // radius
        tapes.add(new TapeItem("TAPE_ARGUMENT", EscapeRoom.STUDY, 632, 241, 120f));
        tapes.add(new TapeItem("TAPE_JAMES_INTERVIEW", EscapeRoom.STUDY, 1050, 141, 220f));
        // Parlor — briefcase area + grandfather clock
        tapes.add(new TapeItem("TAPE_MARCUS_INTERVIEW", EscapeRoom.PARLOR, 635, 340, 120f));
        tapes.add(new TapeItem("TAPE_CHARLES_INTERVIEW", EscapeRoom.PARLOR, 1075, 562, 220f));
        // Kitchen floor
        tapes.add(new TapeItem("TAPE_MARGARET_INTERVIEW", EscapeRoom.KITCHEN, 620, 340));
        // Shed logbook
        tapes.add(new TapeItem("TAPE_DANIEL_INTERVIEW", EscapeRoom.SHED, 480, 380));
        // Margaret's room nightstand
        tapes.add(new TapeItem("TAPE_MARGARET_ACCOUNT", EscapeRoom.MARGARET_ROOM, 899, 519, 130f));
    }

    // ── render ────────────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        if (gameEnded) {
            endTimer += delta;
            if (endTimer >= END_DELAY) {
                if (won) {
                    // Win — simple win screen (wire to proper ending later)
                    game.setScreen(new GameScreen(game));
                } else {
                    // Lose — become the next Narrator, restart from the beginning
                    game.setScreen(new GameScreen(game));
                }
            }
        } else if (sortingActive) {
            handleSortInput();
            if (sortMessageTimer > 0f)
                sortMessageTimer -= delta;
        } else if (!fading) {
            handleInput(delta);
            updateAnimation(delta);
            updateTimer(delta);
            checkTapePickups();
            checkExits();
        } else {
            updateAnimation(delta);
            updateFade(delta);
        }
        draw();
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    private void handleInput(float delta) {
        boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        float dx = 0, dy = 0;
        if (up)
            dy += PLAYER_SPEED * delta;
        if (down)
            dy -= PLAYER_SPEED * delta;
        if (left)
            dx -= PLAYER_SPEED * delta;
        if (right)
            dx += PLAYER_SPEED * delta;

        moving = (dx != 0 || dy != 0);

        // 8-directional: 0=S 1=SE 2=E 3=NE 4=N 5=NW 6=W 7=SW
        if (moving) {
            if (up && right)
                currentDir = 3; // NE
            else if (up && left)
                currentDir = 5; // NW
            else if (down && right)
                currentDir = 1; // SE
            else if (down && left)
                currentDir = 7; // SW
            else if (up)
                currentDir = 4; // N
            else if (down)
                currentDir = 0; // S
            else if (right)
                currentDir = 2; // E
            else if (left)
                currentDir = 6; // W
        }

        // Axis-separated collision — try X then Y independently so player slides along
        // walls
        float newX = Math.max(0, Math.min(DSAGame.SCREEN_WIDTH - PLAYER_W, playerX + dx));
        float newY = Math.max(0, Math.min(DSAGame.SCREEN_HEIGHT - PLAYER_H, playerY + dy));
        if (isWalkable(newX, playerY))
            playerX = newX;
        if (isWalkable(playerX, newY))
            playerY = newY;
    }

    // ── Animation ─────────────────────────────────────────────────────────────
    private void updateAnimation(float delta) {
        if (moving) {
            bobTimer += delta;
            dirFrameTimer += delta;
            if (dirFrameTimer >= DIR_FRAME_DUR) {
                dirFrameTimer -= DIR_FRAME_DUR;
                dirFrameIdx = (dirFrameIdx + 1) % DIR_FRAMES;
            }
        } else {
            dirFrameIdx = 0;
            dirFrameTimer = 0f;
        }
    }

    // ── Timer ─────────────────────────────────────────────────────────────────
    private void updateTimer(float delta) {
        // timer disabled for debugging
        // timeRemaining -= delta;
        // if (timeRemaining <= 0f) { timeRemaining = 0f; endGame(false); }
    }

    // ── Tape pickups ──────────────────────────────────────────────────────────
    private void checkTapePickups() {
        float px = playerX + PLAYER_W / 2f;
        float py = playerY + PLAYER_H / 2f;
        for (TapeItem t : tapes) {
            if (!t.collected && t.room == currentRoom) {
                float tx = t.x + TAPE_SIZE / 2f;
                float ty = t.y + TAPE_SIZE / 2f;
                double dist = Math.sqrt((px - tx) * (px - tx) + (py - ty) * (py - ty));
                if (dist < t.pickupRadius) {
                    t.collected = true;
                    collectedCount++;
                }
            }
        }
    }

    // ── Exit detection ────────────────────────────────────────────────────────
    private void checkExits() {
        List<ExitZone> roomExits = exits.get(currentRoom);
        if (roomExits == null)
            return;
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_W, PLAYER_H);
        for (ExitZone exit : roomExits) {
            if (exit.bounds.overlaps(playerRect)) {
                if (exit.requiresAll) {
                    if (collectedCount >= TOTAL_TAPES)
                        activateSortingPuzzle();
                } else if (exit.target != null) {
                    startFade(exit.target, exit.spawnX, exit.spawnY);
                }
                return;
            }
        }
    }

    // ── Fade transition ───────────────────────────────────────────────────────
    private void startFade(EscapeRoom target, float sx, float sy) {
        fading = true;
        fadingOut = true;
        fadeAlpha = 0f;
        pendingRoom = target;
        pendingX = sx;
        pendingY = sy;
    }

    private void updateFade(float delta) {
        float speed = 3f;
        if (fadingOut) {
            fadeAlpha += delta * speed;
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                fadingOut = false;
                currentRoom = pendingRoom;
                playerX = pendingX;
                playerY = pendingY;
            }
        } else {
            fadeAlpha -= delta * speed;
            if (fadeAlpha <= 0f) {
                fadeAlpha = 0f;
                fading = false;
            }
        }
    }

    // ── End game ──────────────────────────────────────────────────────────────
    private void endGame(boolean playerWon) {
        gameEnded = true;
        won = playerWon;
        endTimer = 0f;
    }

    // ── Draw ──────────────────────────────────────────────────────────────────
    private void draw() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // Background — use collected variant if all tapes in this room are picked up
        boolean roomTapeCollected = tapes.stream()
                .filter(t -> t.room == currentRoom)
                .allMatch(t -> t.collected);
        Texture bg = (roomTapeCollected && roomTexCollected.containsKey(currentRoom))
                ? roomTexCollected.get(currentRoom)
                : roomTex.get(currentRoom);
        if (bg != null) {
            batch.setColor(Color.WHITE);
            batch.draw(bg, 0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
        } else {
            drawRect(0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT, new Color(0.08f, 0.05f, 0.05f, 1f));
        }

        // Tapes
        for (TapeItem t : tapes) {
            if (!t.collected && t.room == currentRoom) {
                batch.setColor(Color.WHITE);
                batch.draw(tapeTex, t.x, t.y, TAPE_SIZE, TAPE_SIZE);
                // Label
                font.setColor(Color.WHITE);
                font.draw(batch, "TAPE", t.x - 4, t.y + TAPE_SIZE + 16);
            }
        }
        font.setColor(Color.WHITE);

        // Player — bob offset when moving
        float bob = moving ? (float) (Math.sin(bobTimer * 10f) * 3f) : 0f;
        Texture frame = dirTex[currentDir][dirFrameIdx];
        if (frame != null) {
            batch.setColor(Color.WHITE);
            batch.draw(frame, playerX, playerY + bob, PLAYER_W, PLAYER_H);
        } else {
            drawRect(playerX, playerY + bob, PLAYER_W, PLAYER_H, Color.CYAN);
        }

        // ── HUD ──
        // Timer — center top
        int secs = (int) Math.ceil(timeRemaining);
        boolean critical = timeRemaining < 15f;
        timerFont.setColor(critical ? Color.RED : Color.WHITE);
        timerFont.draw(batch, String.format("0:%02d", secs),
                DSAGame.SCREEN_WIDTH / 2f - 50, DSAGame.SCREEN_HEIGHT - 8);
        timerFont.setColor(Color.WHITE);

        // Tape count — top left
        font.setColor(Color.WHITE);
        font.draw(batch, "TAPES  " + collectedCount + " / " + TOTAL_TAPES,
                20, DSAGame.SCREEN_HEIGHT - 10);

        // Front door hint in entrance
        if (currentRoom == EscapeRoom.ENTRANCE) {
            if (collectedCount >= TOTAL_TAPES) {
                font.setColor(Color.YELLOW);
                font.draw(batch, "[ ALL TAPES COLLECTED — approach the front door to sort them ]", 200, 50);
            } else {
                font.setColor(new Color(1f, 0.4f, 0.4f, 1f));
                font.draw(batch, "[ FRONT DOOR LOCKED  —  " + (TOTAL_TAPES - collectedCount) + " tape(s) remaining ]",
                        280, 50);
            }
            font.setColor(Color.WHITE);
        }

        // End-game overlays
        if (gameEnded) {
            drawRect(0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT, new Color(0, 0, 0, 0.75f));
            if (won) {
                bigFont.setColor(Color.WHITE);
                bigFont.draw(batch, "YOU ESCAPED.", 380, 420);
                font.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                font.draw(batch, "The manor shrinks in the rearview mirror.", 340, 340);
            } else {
                bigFont.setColor(Color.RED);
                bigFont.draw(batch, "TIME'S UP.", 400, 420);
                bigFont.setColor(Color.WHITE);
                font.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
                font.draw(batch, "You are the new Narrator.", 440, 340);
                font.draw(batch, "Another detective will come.", 430, 300);
            }
            font.setColor(Color.WHITE);
            bigFont.setColor(Color.WHITE);
        }

        // Fade overlay
        if (fading && fadeAlpha > 0f) {
            drawRect(0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT, new Color(0, 0, 0, fadeAlpha));
        }

        if (sortingActive)
            drawSortingPuzzle();

        batch.end();
    }

    private void drawRect(float x, float y, float w, float h, Color c) {
        batch.setColor(c);
        batch.draw(pixelTex, x, y, w, h);
        batch.setColor(Color.WHITE);
    }

    // ── Sorting puzzle ────────────────────────────────────────────────────────
    private static final float SORT_CARD_W = 150f, SORT_CARD_H = 115f, SORT_GAP = 11f;
    private static final float SORT_CARD_Y = 390f;

    private float sortStartX() {
        return (DSAGame.SCREEN_WIDTH - (TOTAL_TAPES * SORT_CARD_W + (TOTAL_TAPES - 1) * SORT_GAP)) / 2f;
    }

    private void activateSortingPuzzle() {
        sortArr = SORT_INITIAL.clone();
        sortKeyIndex = 1;
        sortDragging = false;
        sortDropTarget = 1;
        sortMessage = "";
        sortMessageTimer = 0f;
        sortingActive = true;
    }

    private void handleSortInput() {
        com.badlogic.gdx.math.Vector2 mouse = viewport.unproject(
                new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));

        float startX = sortStartX();
        float keySlotX = startX + sortKeyIndex * (SORT_CARD_W + SORT_GAP);

        if (!sortDragging) {
            // Detect click on the key card
            if (Gdx.input.justTouched()
                    && mouse.x >= keySlotX && mouse.x <= keySlotX + SORT_CARD_W
                    && mouse.y >= SORT_CARD_Y && mouse.y <= SORT_CARD_Y + SORT_CARD_H) {
                sortDragging = true;
                sortDragX = mouse.x;
                sortDragY = mouse.y;
                sortDropTarget = sortKeyIndex;
                sortMessage = "";
                sortMessageTimer = 0f;
            }
        } else {
            sortDragX = mouse.x;
            sortDragY = mouse.y;

            // Which slot would the card land in? (only left of key index, clamped)
            sortDropTarget = sortKeyIndex;
            for (int i = 0; i <= sortKeyIndex; i++) {
                if (sortDragX < startX + i * (SORT_CARD_W + SORT_GAP) + SORT_CARD_W / 2f) {
                    sortDropTarget = i;
                    break;
                }
            }

            if (!Gdx.input.isTouched()) {
                sortDragging = false;
                int correct = correctInsertionPoint();
                if (sortDropTarget == correct) {
                    // Insert key at correct slot — shift elements right to make room
                    String key = sortArr[sortKeyIndex];
                    for (int i = sortKeyIndex; i > correct; i--) {
                        sortArr[i] = sortArr[i - 1];
                    }
                    sortArr[correct] = key;
                    advanceSortOuter();
                } else {
                    sortMessage = "Wrong position — try again.";
                    sortMessageTimer = 2f;
                }
            }
        }
    }

    /**
     * Returns the correct insertion slot for sortArr[sortKeyIndex] in the sorted
     * prefix.
     */
    private int correctInsertionPoint() {
        int keyOrder = sortOrder(sortArr[sortKeyIndex]);
        for (int j = 0; j < sortKeyIndex; j++) {
            if (sortOrder(sortArr[j]) > keyOrder)
                return j;
        }
        return sortKeyIndex; // already in place
    }

    private void advanceSortOuter() {
        sortKeyIndex++;
        if (sortKeyIndex >= TOTAL_TAPES) {
            sortingActive = false;
            endGame(true);
        } else {
            sortMessage = "";
            sortMessageTimer = 0f;
        }
    }

    private void drawSortingPuzzle() {
        drawRect(0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT, new Color(0f, 0f, 0f, 0.88f));

        float startX = sortStartX();

        // Title
        font.getData().setScale(2.2f);
        font.setColor(Color.WHITE);
        font.draw(batch, "INSERTION SORT — ARRANGE THE RECORDINGS", 95, 700);

        font.getData().setScale(1.5f);
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        font.draw(batch, "Drag the highlighted tape to where it belongs — earliest recording first.", 80, 663);

        // Ghost slot (where key would land while dragging)
        if (sortDragging && sortDropTarget < sortKeyIndex) {
            float gx = startX + sortDropTarget * (SORT_CARD_W + SORT_GAP);
            drawRect(gx, SORT_CARD_Y, SORT_CARD_W, SORT_CARD_H, new Color(1f, 1f, 0.3f, 0.25f));
        }

        // Draw all non-key cards first
        for (int i = 0; i < TOTAL_TAPES; i++) {
            if (sortDragging && i == sortKeyIndex)
                continue; // key drawn last

            float cx;
            if (sortDragging && i >= sortDropTarget && i < sortKeyIndex) {
                cx = startX + (i + 1) * (SORT_CARD_W + SORT_GAP); // shift right to show gap
            } else {
                cx = startX + i * (SORT_CARD_W + SORT_GAP);
            }

            Color bg = i < sortKeyIndex
                    ? new Color(0.05f, 0.38f, 0.1f, 1f) // green — sorted
                    : new Color(0.2f, 0.2f, 0.2f, 1f); // grey — unsorted
            drawSortCard(cx, SORT_CARD_Y, sortArr[i], bg);
        }

        // Draw key card on top (follows mouse when dragging)
        if (sortKeyIndex < TOTAL_TAPES) {
            float cx = sortDragging
                    ? sortDragX - SORT_CARD_W / 2f
                    : startX + sortKeyIndex * (SORT_CARD_W + SORT_GAP);
            Color keyCo = sortDragging
                    ? new Color(0.95f, 0.78f, 0.1f, 1f)
                    : new Color(0.72f, 0.57f, 0f, 1f);
            drawSortCard(cx, SORT_CARD_Y, sortArr[sortKeyIndex], keyCo);
        }

        // Instruction
        font.getData().setScale(1.6f);
        if (!sortDragging) {
            font.setColor(new Color(1f, 0.92f, 0.35f, 1f));
            font.draw(batch, "Drag the YELLOW tape to its correct position.", 80, 345);
        } else {
            font.setColor(Color.WHITE);
            font.draw(batch, "Release to place it here.", 80, 345);
        }

        // Feedback message
        if (sortMessageTimer > 0f) {
            font.getData().setScale(1.5f);
            font.setColor(Color.RED);
            font.draw(batch, sortMessage, 80, 300);
        }

        // Legend
        font.getData().setScale(1.3f);
        font.setColor(new Color(0.05f, 0.7f, 0.15f, 1f));
        font.draw(batch, "GREEN = sorted", 80, 130);
        font.setColor(new Color(1f, 0.92f, 0.35f, 1f));
        font.draw(batch, "YELLOW = drag me", 280, 130);
        font.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        font.draw(batch, "GREY = not yet sorted", 510, 130);

        // Progress
        font.getData().setScale(1.2f);
        font.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        font.draw(batch, "Outer pass  i = " + sortKeyIndex + " / " + (TOTAL_TAPES - 1), 80, 82);

        font.getData().setScale(1.8f);
        font.setColor(Color.WHITE);
    }

    private void drawSortCard(float cx, float cy, String id, Color bg) {
        drawRect(cx, cy, SORT_CARD_W, SORT_CARD_H, bg);
        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
        font.draw(batch, sortShortName(id), cx + 7, cy + SORT_CARD_H - 14);
        font.getData().setScale(1.0f);
        font.setColor(new Color(0.85f, 0.85f, 0.85f, 1f));
        font.draw(batch, sortTimestamp(id), cx + 6, cy + 26);
    }

    private static int sortOrder(String id) {
        switch (id) {
            case "TAPE_ARGUMENT":
                return 0;
            case "TAPE_MARGARET_INTERVIEW":
                return 1;
            case "TAPE_CHARLES_INTERVIEW":
                return 2;
            case "TAPE_JAMES_INTERVIEW":
                return 3;
            case "TAPE_MARCUS_INTERVIEW":
                return 4;
            case "TAPE_DANIEL_INTERVIEW":
                return 5;
            case "TAPE_MARGARET_ACCOUNT":
                return 6;
            default:
                return 99;
        }
    }

    private static String sortShortName(String id) {
        switch (id) {
            case "TAPE_ARGUMENT":
                return "ARGUMENT";
            case "TAPE_MARGARET_INTERVIEW":
                return "MARGARET INT.";
            case "TAPE_CHARLES_INTERVIEW":
                return "CHARLES INT.";
            case "TAPE_JAMES_INTERVIEW":
                return "JAMES INT.";
            case "TAPE_MARCUS_INTERVIEW":
                return "MARCUS INT.";
            case "TAPE_DANIEL_INTERVIEW":
                return "DANIEL INT.";
            case "TAPE_MARGARET_ACCOUNT":
                return "MARGARET ACCT.";
            default:
                return id;
        }
    }

    private static String sortTimestamp(String id) {
        switch (id) {
            case "TAPE_ARGUMENT":
                return "Nov 15, ~10 PM";
            case "TAPE_MARGARET_INTERVIEW":
                return "Nov 17, 10:00 AM";
            case "TAPE_CHARLES_INTERVIEW":
                return "Nov 17, 11:30 AM";
            case "TAPE_JAMES_INTERVIEW":
                return "Nov 17,  2:00 PM";
            case "TAPE_MARCUS_INTERVIEW":
                return "Nov 17,  3:00 PM";
            case "TAPE_DANIEL_INTERVIEW":
                return "Nov 17,  4:30 PM";
            case "TAPE_MARGARET_ACCOUNT":
                return "Personal account";
            default:
                return "";
        }
    }

    // ── Screen boilerplate ────────────────────────────────────────────────────
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        timerFont.dispose();
        bigFont.dispose();
        if (pixelTex != null)
            pixelTex.dispose();
        if (tapeTex != null)
            tapeTex.dispose();
        for (Texture t : roomTex.values())
            if (t != null)
                t.dispose();
        for (Texture t : roomTexCollected.values())
            if (t != null)
                t.dispose();
        for (Texture[] frames : dirTex)
            for (Texture t : frames)
                if (t != null)
                    t.dispose();
    }
}
