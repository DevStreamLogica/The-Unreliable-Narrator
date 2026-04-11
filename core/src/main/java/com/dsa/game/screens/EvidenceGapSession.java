package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.dsa.game.DSAGame;
import com.dsa.game.navigation.Room;

/**
 * Between-tape evidence collection: chapter pickups, backpack grid, and combine logic.
 * <p>
 * {@linkplain #forStandalone(DSAGame, int, Room.RoomID, Runnable) Standalone} mode drives {@link GameInventoryScreen}
 * (separate room graph + exploration spotlight). {@linkplain #forEmbedded(DSAGame, int, Runnable) Embedded} mode is
 * owned by {@link GameScreen}: overlays and gap clicks use the real manor room from {@link Room.RoomID}; only current-gap
 * props draw and are clickable, and normal EXAMINE hotspots are gated while the bag is closed.
 */
public final class EvidenceGapSession {

    /** True when running on {@link GameScreen} (no inventory room navigation). */
    private final boolean embedded;
    /**
     * When {@code false} (opening embedded session only), bag UI works but manor-room evidence overlays are not drawn
     * and are not clickable until {@link GameScreen} replaces this session with {@link #forEmbedded} after the first tape.
     */
    private final boolean embeddedWorldPickupsEnabled;

    // ── Virtual resolution ────────────────────────────────────────────────────
    private static final int SW = DSAGame.SCREEN_WIDTH;
    private static final int SH = DSAGame.SCREEN_HEIGHT;

    // ── Panel ─────────────────────────────────────────────────────────────────
    private static final int INV_W = 1056, INV_H = 612;
    private static final int INV_X = (SW - INV_W) / 2;
    private static final int INV_Y = (SH - INV_H) / 2;

    // ── Right panel ───────────────────────────────────────────────────────────
    private static final int PREV_X = 770, PREV_Y = 295, PREV_W = 315, PREV_H = 291;

    // ── Open-bag button ───────────────────────────────────────────────────────
    private static final int BTN_W = 64, BTN_H = 64, BTN_X = 8, BTN_Y = 8;

    /**
     * On {@link GameScreen}, stack the bag above the back arrow so bag, back, and action bar stay visible.
     * Must match {@link GameScreen} {@code actionBar.getBarHeight() + 4f} and {@code ARROW_BACK_DRAW_*}.
     */
    private static final float GAME_ACTION_BAR_H = 36f;
    private static final float GAME_BACK_Y_ABOVE_BAR = 4f;
    private static final float GAME_ARROW_BACK_X = 12f;
    private static final float GAME_ARROW_BACK_W = 70f;
    private static final float GAME_ARROW_BACK_H = 70f;
    private static final float EMBEDDED_BAG_GAP_ABOVE_BACK = 8f;
    private static final float EMBEDDED_BAG_X =
            GAME_ARROW_BACK_X + (GAME_ARROW_BACK_W - BTN_W) * 0.5f;
    private static final float EMBEDDED_BAG_Y =
            GAME_ACTION_BAR_H + GAME_BACK_Y_ABOVE_BAR + GAME_ARROW_BACK_H + EMBEDDED_BAG_GAP_ABOVE_BACK;

    /**
     * Back control: matches {@link com.dsa.game.screens.GameScreen} {@code ARROW_BACK_DRAW_*} and
     * {@link com.dsa.game.ui.ActionBar#getBarHeight()} + 4f (36 + 4).
     */
    private static final int INV_BACK_X = 12, INV_BACK_Y = 40, INV_BACK_W = 70, INV_BACK_H = 70;

    private static final float FLASH_DUR = 0.35f;

    // ── Chapter data ──────────────────────────────────────────────────────────
    // ── Gap 1 (chapter 0) — 6 items, 3 pairs ─────────────────────────────────
    private static final String[] NAMES_G1 = {
        "Solicitor's Letter", "Harold's Reply", "Patent Document",
        "Marcus's Card",      "James's Note",   "Daniel's Schedule"
    };
    private static final Color[] COLORS_G1 = {
        new Color(0.76f,0.82f,0.88f,1f), new Color(0.70f,0.76f,0.84f,1f),
        new Color(0.66f,0.72f,0.80f,1f), new Color(0.82f,0.86f,0.90f,1f),
        new Color(0.72f,0.78f,0.84f,1f), new Color(0.68f,0.74f,0.80f,1f),
    };
    private static final String[] QUOTES_G1 = {
        "Harold had already written to his solicitor. The appointment was confirmed for nine o'clock the next morning.",
        "Harold's own handwriting confirming the appointment. Whatever James said to him that night, it changed nothing.",
        "The patent dispute with Marcus Blackwood. Two years of legal back-and-forth. Harold had decided his answer.",
        "Marcus Blackwood's card. Left on the table — forgotten in his frustration. He was here that night.",
        "James wrote to Daniel that evening. 'Come see me after dinner.' Daniel's shift was over. But he came.",
        "Daniel's schedule for November 15th. Work ending at six. And yet James's note asked him to come after dinner."
    };
    private static final String[] QUOTE_KEYS_G1 = {
        "nar_g1_obj_soletter_t", "nar_g1_obj_reply_t", "nar_g1_obj_patent_t",
        "nar_g1_obj_card_t",     "nar_g1_obj_jnote_t", "nar_g1_obj_schedule_t"
    };
    private static final int[][] PAIRS_G1 = { {0,1}, {2,3}, {4,5} };

    /** Gap 1 — James's note to Daniel on the shed desk (screen coords; matches flipped {@code inventory/rooms/shed.png}).
     *  james_note.png content at game x=380-659, y=261-419 (no flip).
     *  After horizontal flip (shed): x = 1280-659=621 .. 1280-380=900, y unchanged. */
    private static final int GAP1_JAMES_NOTE_SHED_X = 621;
    private static final int GAP1_JAMES_NOTE_SHED_Y = 261;
    private static final int GAP1_JAMES_NOTE_SHED_W = 279;
    private static final int GAP1_JAMES_NOTE_SHED_H = 158;
    private static final String[] COMBINED_G1 = { "Confirmed Appointment", "The Patent Dispute", "The Arrangement" };
    private static final String[] COMBINED_QUOTES_G1 = {
        "Yes. The appointment was already made. Harold wasn't threatening James — he was informing him.",
        "Two men arrived at Vance Manor that evening having already lost. Only one of them had nowhere left to go.",
        "They had already begun planning before the shouting even started."
    };
    private static final String[] COMBINED_KEYS_G1 = {
        "nar_g1_comb_appt", "nar_g1_comb_patent", "nar_g1_comb_arrangement"
    };

    // ── Gap 2 (chapter 1) — 4 items, 2 pairs ─────────────────────────────────
    private static final String[] NAMES_G2 = {
        "Empty Whiskey Glass", "Harold's Rejection Note",
        "Marcus's Written Proposal", "Serving Instructions"
    };
    private static final Color[] COLORS_G2 = {
        new Color(0.82f,0.86f,0.90f,1f), new Color(0.76f,0.82f,0.88f,1f),
        new Color(0.70f,0.76f,0.84f,1f), new Color(0.66f,0.72f,0.80f,1f),
    };
    private static final String[] QUOTES_G2 = {
        "Harold's glass. Still on the table where he left it. He'd barely touched the drink.",
        "Harold's rejection — in his own hand. Marcus had no idea it was coming.",
        "Marcus came prepared. He'd written this proposal weeks in advance. Harold never even read it.",
        "Instructions left for the evening staff. Everything had to run on schedule, even that night."
    };
    private static final String[] QUOTE_KEYS_G2 = {
        "nar_g2_obj_glass_t", "nar_g2_obj_rejection_t",
        "nar_g2_obj_proposal_t", "nar_g2_obj_serving_t"
    };
    private static final int[][] PAIRS_G2 = { {0,1}, {2,3} };
    private static final String[] COMBINED_G2 = { "A Drink Refused", "The Rejected Deal" };
    private static final String[] COMBINED_QUOTES_G2 = {
        "Harold didn't even offer him a drink. The rejection was decided before Marcus walked in.",
        "Marcus brought a proposal. Harold brought a refusal. Neither man left satisfied."
    };
    private static final String[] COMBINED_KEYS_G2 = { "nar_g2_comb_drink", "nar_g2_comb_deal" };

    // ── Gap 3 (chapter 2) — 4 items, 2 pairs ─────────────────────────────────
    private static final String[] NAMES_G3 = {
        "Charles's Draft Will Amendment", "Harold's Evening Routine Schedule",
        "Discarded Note (James)", "Charles's Work Checklist"
    };
    private static final Color[] COLORS_G3 = {
        new Color(0.76f,0.82f,0.88f,1f), new Color(0.70f,0.76f,0.84f,1f),
        new Color(0.66f,0.72f,0.80f,1f), new Color(0.82f,0.86f,0.90f,1f),
    };
    private static final String[] QUOTES_G3 = {
        "A draft. Charles had been revising this for weeks. Harold didn't know.",
        "Harold's routine never changed. He was in that study at half past eight. Every night.",
        "James discarded this. Why? What was he trying to hide?",
        "Charles kept meticulous records. Everything logged. Everything timed."
    };
    private static final String[] QUOTE_KEYS_G3 = {
        "nar_g3_obj_willamend_t", "nar_g3_obj_schedule_t",
        "nar_g3_obj_jnote_t", "nar_g3_obj_checklist_t"
    };
    private static final int[][] PAIRS_G3 = { {0,1}, {2,3} };
    private static final String[] COMBINED_G3 = { "The Window of Opportunity", "Coordinated" };
    private static final String[] COMBINED_QUOTES_G3 = {
        "Charles knew exactly when Harold would be in that study. The amendment gave him the motive. The schedule gave him the moment.",
        "James discarded the note. Charles logged the time. They both knew what was happening."
    };
    private static final String[] COMBINED_KEYS_G3 = { "nar_g3_comb_window", "nar_g3_comb_coord" };

    /** Gap 3 — kitchen: Charles's draft will (bottom-left, small table area). */
    private static final int GAP3_KITCHEN_WILL_X = 37;
    private static final int GAP3_KITCHEN_WILL_Y = 152;
    private static final int GAP3_KITCHEN_WILL_W = 80;
    private static final int GAP3_KITCHEN_WILL_H = 36;
    /** Gap 3 — kitchen: Harold's evening routine schedule (bottom-right counter). */
    private static final int GAP3_KITCHEN_SCHEDULE_X = 1116;
    private static final int GAP3_KITCHEN_SCHEDULE_Y = 203;
    private static final int GAP3_KITCHEN_SCHEDULE_W = 42;
    private static final int GAP3_KITCHEN_SCHEDULE_H = 22;
    /** Gap 3 — servants quarters: Charles's work checklist (center, nightstand). */
    private static final int GAP3_SERVANTS_CHECKLIST_X = 520;
    private static final int GAP3_SERVANTS_CHECKLIST_Y = 291;
    private static final int GAP3_SERVANTS_CHECKLIST_W = 36;
    private static final int GAP3_SERVANTS_CHECKLIST_H = 24;
    /** Gap 3 — shed: Daniel's note to James (flipped overlay; calculated from 3436x1963 source). */
    private static final int GAP3_SHED_NOTE_X = 626;
    private static final int GAP3_SHED_NOTE_Y = 263;
    private static final int GAP3_SHED_NOTE_W = 100;
    private static final int GAP3_SHED_NOTE_H = 70;

    // ── Gap 4 (chapter 3) — 4 items, 2 pairs ─────────────────────────────────
    private static final String[] NAMES_G4 = {
        "Daniel's Bank Deposit Book", "James's Withdrawal Slip",
        "Daniel's Fake Work Order", "Partnership Certificate (Harold & Thomas Ashford)"
    };
    private static final Color[] COLORS_G4 = {
        new Color(0.70f,0.76f,0.84f,1f), new Color(0.82f,0.86f,0.90f,1f),
        new Color(0.76f,0.82f,0.88f,1f), new Color(0.66f,0.72f,0.80f,1f),
    };
    private static final String[] QUOTES_G4 = {
        "Regular deposits. More than his wages should allow. Someone was paying Daniel.",
        "James withdrew a substantial sum the week before. Cash. No record of where it went.",
        "This work order is fabricated. The job never existed. It was an alibi.",
        "Harold and Thomas Ashford. Partners. Until they weren't."
    };
    private static final String[] QUOTE_KEYS_G4 = {
        "nar_g4_obj_bankbook_t", "nar_g4_obj_withdrawal_t",
        "nar_g4_obj_workorder_t", "nar_g4_obj_certificate_t"
    };
    private static final int[][] PAIRS_G4 = { {0,1}, {2,3} };
    private static final String[] COMBINED_G4 = { "The Payment", "The Cover Story" };
    private static final String[] COMBINED_QUOTES_G4 = {
        "James paid Daniel. The deposit book and the withdrawal slip are the same transaction, seen from both sides.",
        "A fake work order and Harold's original partnership certificate. Daniel had access to Harold's history and Harold's secrets. He used both."
    };
    private static final String[] COMBINED_KEYS_G4 = { "nar_g4_comb_payment", "nar_g4_comb_cover" };

    // ── Gap 5 (chapter 4) — 2 items, 1 pair ──────────────────────────────────
    private static final String[] NAMES_G5 = {
        "Margaret's Diary Page", "Daniel's Retreat Log"
    };
    private static final Color[] COLORS_G5 = {
        new Color(0.82f,0.86f,0.90f,1f), new Color(0.70f,0.76f,0.84f,1f),
    };
    private static final String[] QUOTES_G5 = {
        "Margaret wrote this the morning after. She knew. She'd always known.",
        "Daniel logged this retreat as work. It wasn't. He was there with Margaret."
    };
    private static final String[] QUOTE_KEYS_G5 = { "nar_g5_obj_diary_t", "nar_g5_obj_retreatlog_t" };
    private static final int[][] PAIRS_G5 = { {0,1} };
    private static final String[] COMBINED_G5 = { "The Alibi That Wasn't" };
    private static final String[] COMBINED_QUOTES_G5 = {
        "They were together. The diary and the log confirm it. Neither was where they claimed to be."
    };
    private static final String[] COMBINED_KEYS_G5 = { "nar_g5_comb_alibi" };

    // ── Gap 6 (chapter 5) — 2 items, 1 pair ──────────────────────────────────
    private static final String[] NAMES_G6 = {
        "Construction Invoice (1957)", "Broken Wine Rack Bracket"
    };
    private static final Color[] COLORS_G6 = {
        new Color(0.76f,0.82f,0.88f,1f), new Color(0.66f,0.72f,0.80f,1f),
    };
    private static final String[] QUOTES_G6 = {
        "This invoice was paid by Harold alone. Thomas's name is conspicuously absent.",
        "This bracket didn't break on its own. Something was moved down here. Or someone."
    };
    private static final String[] QUOTE_KEYS_G6 = { "nar_g6_obj_invoice_t", "nar_g6_obj_bracket_t" };
    private static final int[][] PAIRS_G6 = { {0,1} };
    private static final String[] COMBINED_G6 = { "The Broken Partnership" };
    private static final String[] COMBINED_QUOTES_G6 = {
        "A construction invoice and a broken wine rack bracket. Harold built the wall in cash with no record. When it was disturbed, the rack was pushed aside. Whatever was sealed behind that wall in 1957 — it's still there."
    };
    private static final String[] COMBINED_KEYS_G6 = { "nar_g6_comb_partner" };

    private static final String[] WRONG_COMBO_KEYS = {
        "nar_wc_01","nar_wc_02","nar_wc_03","nar_wc_04","nar_wc_05",
        "nar_wc_06","nar_wc_07","nar_wc_08","nar_wc_09","nar_wc_10"
    };
    private static final String[] WRONG_COMBO_QUOTES = {
        "No. Those two things have nothing to do with each other. Try again.",
        "That's not a connection. Put one of them down and think.",
        "Interesting choice. Wrong, but interesting. One belongs with something you haven't found yet.",
        "You're forcing it. The combination doesn't work because the connection isn't there.",
        "No, no, no. Actually — wait. No. Still no. Those two things aren't related.",
        "That's not right. I want to be encouraging here, but that is simply not the correct pairing.",
        "Imagine two puzzle pieces that don't fit. That is what I just watched you do.",
        "The answer is no. Which I realize isn't very helpful. But it is accurate.",
        "Those two things were not meant to go together. One belongs with something else entirely.",
        "Not those two. You're close, but that specific combination isn't it."
    };

    // ── Per-template layout vars ──────────────────────────────────────────────
    // { GCOLS, GROWS, SLOT, SLOT_H, SGAP_X, SGAP_Y, GRID_X, GRID_Y,
    //   CMB, CMB_H, CMB_X, CMB_Y, CS_W, CS_H, CS_GAP, CS_X, CS_Y }
    private static final int[] LAYOUT_6 = { 3,2, 98,107, 27,98, 220,247, 96,105,592,340, 98,96, 79,220,86 };
    private static final int[] LAYOUT_4 = { 2,2, 118,113, 54,59, 240,242, 117,109,561,333, 120,114, 68,285,74 };
    private static final int[] LAYOUT_2 = { 1,2, 119,113, 0,55, 305,244, 120,114,510,330, 120,113, 0,384,76 };

    private int GCOLS, GROWS, SLOT, SLOT_H, SGAP_X, SGAP_Y, GRID_X, GRID_Y;
    private int CMB, CMB_H, CMB_X, CMB_Y;
    private int CS_W, CS_H, CS_GAP, CS_X, CS_Y;
    private int CLO_X, CLO_Y, CLO_W, CLO_H;

    // ── Active template data ──────────────────────────────────────────────────
    private int         currentTemplate;
    private String[]    names, quotes, combined, combinedQuotes;
    private String[]    quoteKeys, combinedKeys;
    private Color[]     colors;
    private int[][]     pairs;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean  open       = false;
    private String[] grid;
    private int      cmbSlot    = -1;
    private String[] done;
    private int      previewIdx = -1;
    private boolean  previewDone= false;
    /** True after all combinations complete; blocks all further player input. */
    private boolean completionLocked = false;
    /** Wait for final completion narration to finish before transitioning out. */
    private boolean waitingForCompletionNarration = false;

    // drag
    private boolean isDrag      = false;
    private boolean dragFromCmb = false;
    private int     dragSlot    = -1;
    private float   dragX, dragY;
    private boolean prevLeftPressed = false;

    // timers
    private float flashTimer   = 0f;
    private float allDoneTimer = -1f;

    private String narText = "Open the bag to examine the evidence.";
    private final java.util.Random rng = new java.util.Random();

    // ── Rendering ─────────────────────────────────────────────────────────────
    private final DSAGame    game;
    private final Runnable   onComplete;
    private final BitmapFont font, small, tiny;
    private final GlyphLayout lay;

    // ── Chapter ───────────────────────────────────────────────────────────────
    private int chapterIndex;

    // ── Rooms (standalone nav index 0–7; ignored in embedded mode for hit-tests / draws) ──
    private int currentRoom = 1; // start in study (most relevant items)

    // Room textures
    private Texture txRoom, txStudy, txShed, txCellar, txKitchen, txServants, txJames, txMargaret;
    private Texture txW;
    /** Radial gradient for exploration spotlight (multiply pass); see {@link NarratorSpotlightScreen}. */
    private Texture txExplorationSpotlight;
    private FrameBuffer explorationLightsFbo;
    /** Half the spotlight quad side in world px ({@code lightSize = 2 * this}). */
    private static final float EXPLORATION_LIGHT_HALF = 250f;

    // Item sprites (study: solicitor, reply, patent / james: note / shed: schedule)
    private Texture txObjSolicitor, txObjReply, txObjPatent;
    private Texture txObjJamesNote, txObjSchedule;
    private Texture txObjCertificate, txObjGlass, txObjCard, txObjRejection, txObjProposal;
    private Texture txObjBankbook, txObjWorkorder, txObjRetreatlog, txObjJamesDiscarded;
    private Texture txObjBracket, txObjInvoice;
    private Texture txObjWillAmend, txObjKitchenSchedule;
    private Texture txObjChecklist, txObjServing;
    private Texture txObjWithdrawal, txObjDiary;

    /** Room-only overlays from {@code inventory/world/}; inventory UI never uses these. */
    private Texture wlSolicitor, wlReply, wlPatent, wlCard, wlJamesNote, wlSchedule;
    private Texture wlCertificate, wlGlass, wlRejection, wlProposal;
    private Texture wlBankbook, wlWorkorder, wlRetreatlog, wlJamesDiscarded;
    private Texture wlBracket, wlInvoice, wlWillAmend, wlKitchenSchedule, wlChecklist, wlServing;
    private Texture wlWithdrawal, wlDiary;

    // Pick-up state per item
    private boolean objSolicitorPicked, objReplyPicked, objPatentPicked;
    private boolean objJamesNotePicked, objSchedulePicked;
    private boolean objCertificatePicked, objGlassPicked, objCardPicked;
    private boolean objRejectionPicked, objProposalPicked;
    private boolean objBankbookPicked, objWorkorderPicked, objRetreatlogPicked;
    private boolean objBracketPicked, objInvoicePicked;
    private boolean objWillAmendPicked, objKitchenSchedulePicked;
    private boolean objChecklistPicked, objServingPicked;
    private boolean objWithdrawalPicked, objDiaryPicked;
    private boolean objJamesDiscardedPicked;

    // UI
    private Texture txInvBg, txBag, txBagOpen, txClose, txBack, txDoneItem;
    private Texture[] txItem;
    private boolean[] txItemOwned;

    // Sounds / Music
    private Sound  sndOpen, sndClose, sndItem, sndSuccess, sndFail, sndAllDone;
    private Music  musInventory, musNarrator;
    private float  musDelay = -1f;
    private float  musVol   = 0f;
    private int    musFade  = 0;
    private static final float MUS_FADE_DUR = 1.5f;
    private static final float MUS_DELAY    = 1.0f;
    private static final float MUS_NARRATOR_DUCK_VOL = 0.20f;

    // ── Factories ─────────────────────────────────────────────────────────────
    /**
     * Full-screen inventory UI with its own room graph and exploration spotlight (debug / legacy screen).
     */
    public static EvidenceGapSession forStandalone(DSAGame game, int chapterIndex,
            Room.RoomID resumeWorldRoom, Runnable onComplete) {
        return new EvidenceGapSession(game, chapterIndex, resumeWorldRoom, onComplete, false, true, true);
    }

    /**
     * Evidence phase on {@link GameScreen}: draw pickups for the current manor room only; use manor doors for travel.
     */
    public static EvidenceGapSession forEmbedded(DSAGame game, int chapterIndex, Runnable onComplete) {
        return new EvidenceGapSession(game, chapterIndex, null, onComplete, true, true, true);
    }

    public static EvidenceGapSession forEmbeddedSilent(DSAGame game, int chapterIndex, Runnable onComplete) {
        return new EvidenceGapSession(game, chapterIndex, null, onComplete, true, false, true);
    }

    /**
     * New-game opening: backpack and bag-open UI from the first frame, without gap items in manor rooms until the first
     * real evidence phase ({@link #forEmbedded} after the first tape + maze).
     */
    public static EvidenceGapSession forEmbeddedSilentNoWorldPickups(DSAGame game, int chapterIndex,
            Runnable onComplete) {
        return new EvidenceGapSession(game, chapterIndex, null, onComplete, true, false, false);
    }

    private EvidenceGapSession(DSAGame game, int chapterIndex, Room.RoomID resumeWorldRoom,
            Runnable onComplete, boolean embedded, boolean playIntroNarration, boolean embeddedWorldPickupsEnabled) {
        this.game         = game;
        this.onComplete   = onComplete;
        this.chapterIndex = chapterIndex;
        this.embedded     = embedded;
        this.embeddedWorldPickupsEnabled = embeddedWorldPickupsEnabled;

        font  = new BitmapFont(); font.getData().setScale(1.1f);
        small = new BitmapFont(); small.getData().setScale(1.1f);
        tiny  = new BitmapFont(); tiny.getData().setScale(0.95f);
        lay   = new GlyphLayout();

        if (!embedded) {
            int fromWorld = inventoryIndexForWorldRoom(resumeWorldRoom);
            if (fromWorld >= 0 && worldRoomShowsChapterPickups(chapterIndex, fromWorld)) {
                currentRoom = fromWorld;
            } else {
                switch (chapterIndex) {
                    case 1: currentRoom = 0; break;
                    case 2: currentRoom = 4; break;
                    case 3: currentRoom = 2; break;
                    case 4: currentRoom = 7; break;
                    case 5: currentRoom = 0; break;
                    default: currentRoom = 1; break;
                }
            }
        }

        buildTextures();
        applyTemplate(templateSizeForChapter(chapterIndex));
        if (playIntroNarration) playNarration("Open the bag to examine the evidence.", "nar_g1_open");
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public boolean isBagOpen() {
        return open;
    }

    private int bagScreenX() {
        return embedded ? Math.round(EMBEDDED_BAG_X) : BTN_X;
    }

    private int bagScreenY() {
        return embedded ? Math.round(EMBEDDED_BAG_Y) : BTN_Y;
    }

    /** Per-frame update (music fade, completion timers, flash). Call from screen render. */
    public void update(float delta) {
        if (flashTimer > 0) flashTimer -= delta;

        if (musInventory != null) {
            if (musDelay > 0) {
                musDelay -= delta;
                if (musDelay <= 0) { musInventory.setVolume(0f); musInventory.play(); musFade = 1; }
            }
            if (musFade == 1) {
                // After all combinations, duck inventory music while the final narrator line is speaking.
                float targetVol = (completionLocked && isNarratorPlaying()) ? MUS_NARRATOR_DUCK_VOL : 1f;
                if (musVol < targetVol) musVol = Math.min(targetVol, musVol + delta / MUS_FADE_DUR);
                else musVol = Math.max(targetVol, musVol - delta / MUS_FADE_DUR);
                musInventory.setVolume(musVol);
            } else if (musFade == 2) {
                musVol = Math.max(0f, musVol - delta / MUS_FADE_DUR);
                musInventory.setVolume(musVol);
                if (musVol <= 0f) { musInventory.stop(); musFade = 0; }
            }
        }

        if (allDoneTimer > 0) {
            allDoneTimer -= delta;
            if (allDoneTimer <= 0) {
                allDoneTimer = -1f;
                if (sndAllDone != null) sndAllDone.play(1.0f);
                java.util.Arrays.fill(done, null);
                previewIdx = -1;
                musFade = 2;
                scheduleComplete();
            }
        }

        // Hard-lock controls after all combinations until the final narration line completes.
        if (waitingForCompletionNarration && !isNarratorPlaying()) {
            waitingForCompletionNarration = false;
            if (sndAllDone != null) sndAllDone.play(1.0f);
            java.util.Arrays.fill(done, null);
            previewIdx = -1;
            musFade = 2;
            scheduleComplete();
        }

        tickCompletion(delta);
    }

    /**
     * Draws only this gap's prop overlays for the given manor room (no room backdrop, no bag).
     * Aligns shed world layers with inventory shed art (horizontal flip when {@code inv == 2}).
     */
    public void drawEmbeddedPickups(SpriteBatch batch, Room.RoomID worldRoom) {
        if (!embeddedWorldPickupsEnabled) return;
        int inv = inventoryIndexForWorldRoom(worldRoom);
        if (inv < 0 || !worldRoomShowsChapterPickups(chapterIndex, inv)) return;
        drawPickupsForInventoryRoom(batch, inv);
    }

    public void drawEmbeddedBackpackIcon(SpriteBatch batch) {
        if (!open && txBag != null) batch.draw(txBag, bagScreenX(), bagScreenY(), BTN_W, BTN_H);
    }

    /** Open-bag UI drawn on top of the manor (no duplicate inventory room art). */
    public void drawOpenInventoryOverlay(SpriteBatch batch, Room.RoomID worldRoom) {
        if (!open) return;
        int inv = inventoryIndexForWorldRoom(worldRoom);
        if (embeddedWorldPickupsEnabled && inv >= 0 && worldRoomShowsChapterPickups(chapterIndex, inv))
            drawPickupsForInventoryRoom(batch, inv);
        if (txInvBg != null) batch.draw(txInvBg, INV_X, INV_Y, INV_W, INV_H);
        if (txBagOpen != null) batch.draw(txBagOpen, bagScreenX(), bagScreenY(), BTN_W, BTN_H);
        drawGrid(batch);
        drawCombineBox(batch);
        drawCombinedSection(batch);
        drawRightPanel(batch);
        if (txClose != null) batch.draw(txClose, CLO_X, CLO_Y, CLO_W, CLO_H);
        if (isDrag && dragSlot >= 0 && txItem != null && dragSlot < txItem.length) {
            batch.setColor(1, 1, 1, 0.70f);
            batch.draw(txItem[dragSlot], dragX - (SLOT - 8) / 2f, dragY - (SLOT_H - 8) / 2f, SLOT - 8, SLOT_H - 8);
            batch.setColor(Color.WHITE);
        }
    }

    /**
     * Bag closed: backpack button + gap pickups only. Returns true if the event should not reach manor hotspots / EXAMINE.
     */
    public boolean touchDownEmbeddedExploring(float gameX, float gameY, Room.RoomID worldRoom) {
        if (completionLocked) return true;
        if (open) return false;
        int mx = (int) gameX, my = (int) gameY;
        if (hit(mx, my, bagScreenX(), bagScreenY(), BTN_W, BTN_H)) {
            open = true;
            playSound(sndOpen, 1f);
            if (musInventory != null) { musDelay = MUS_DELAY; musVol = 0f; musFade = 0; }
            if (musNarrator != null) musNarrator.stop();
            return true;
        }
        if (!embeddedWorldPickupsEnabled) return false;
        int inv = inventoryIndexForWorldRoom(worldRoom);
        if (inv < 0 || !worldRoomShowsChapterPickups(chapterIndex, inv)) return false;
        if (tryPickupAtRoom(mx, my, inv)) return true;
        return false;
    }

    /** While the bag is open on the manor, captures all touches (modal overlay). */
    public boolean touchDownEmbeddedBagOpen(float gameX, float gameY) {
        if (completionLocked) return true;
        if (!open) return false;
        int mx = (int) gameX, my = (int) gameY;
        if (hit(mx, my, CLO_X, CLO_Y, CLO_W, CLO_H) || hit(mx, my, bagScreenX(), bagScreenY(), BTN_W, BTN_H)) {
            closeInventory();
            return true;
        }
        for (int i = 0; i < done.length; i++) {
            int sx = CS_X + i * (CS_W + CS_GAP);
            if (done[i] != null && hit(mx, my, sx, CS_Y, CS_W, CS_H)) {
                previewIdx = i; previewDone = true; playSound(sndItem, 1f); return true;
            }
        }
        for (int i = 0; i < grid.length; i++) {
            if (grid[i] != null && hit(mx, my, gx(i), gy(i), SLOT, SLOT_H)) {
                boolean samePreview = (previewIdx == i && !previewDone);
                previewIdx = i; previewDone = false;
                if (!samePreview) playNarration(quotes[i], quoteKeys[i]);
                playSound(sndItem, 1f);
                grid[i] = null; isDrag = true; dragFromCmb = false;
                dragSlot = i; dragX = mx; dragY = my; return true;
            }
        }
        if (cmbSlot != -1 && hit(mx, my, CMB_X, CMB_Y, CMB, CMB_H)) {
            int s = cmbSlot; cmbSlot = -1;
            isDrag = true; dragFromCmb = true;
            dragSlot = s; dragX = mx; dragY = my; return true;
        }
        return true;
    }

    public void touchDraggedEmbeddedBagOpen(float gameX, float gameY) {
        if (completionLocked) return;
        if (!open || !isDrag) return;
        dragX = gameX;
        dragY = gameY;
    }

    public void touchUpEmbeddedBagOpen(float gameX, float gameY) {
        if (completionLocked) return;
        if (!open || !isDrag) return;
        onDrop((int) gameX, (int) gameY);
    }

    /** @return true if ESC closed the bag */
    public boolean handleEscape() {
        if (completionLocked) return true;
        if (open) {
            closeInventory();
            return true;
        }
        return false;
    }

    // ── Standalone {@link GameInventoryScreen} driver ─────────────────────────
    public void renderStandaloneFrame(float delta) {
        update(delta);
        handleInputStandalone();

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (open) {
            game.batch.begin();
            drawInventory();
            game.batch.end();
        } else {
            game.batch.begin();
            drawRoom();
            game.batch.end();
            applyExplorationSpotlight();
        }
    }

    private void handleInputStandalone() {
        if (completionLocked) return;
        Vector3 m = mouse();
        int mx = (int) m.x, my = (int) m.y;
        boolean curPressed   = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean justPressed  = curPressed && !prevLeftPressed;
        boolean justReleased = !curPressed && prevLeftPressed;
        prevLeftPressed = curPressed;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && open)
            closeInventory();

        if (!open) {
            if (justPressed) {
                if (hit(mx, my, BTN_X, BTN_Y, BTN_W, BTN_H)) {
                    open = true;
                    playSound(sndOpen, 1f);
                    if (musInventory != null) { musDelay = MUS_DELAY; musVol = 0f; musFade = 0; }
                    if (musNarrator != null) musNarrator.stop();
                    return;
                }
                handleRoomClick(mx, my);
            }
            return;
        }

        if (justPressed) {
            if (hit(mx, my, CLO_X, CLO_Y, CLO_W, CLO_H) || hit(mx, my, BTN_X, BTN_Y, BTN_W, BTN_H)) {
                closeInventory(); return;
            }
            if (handleRoomNav(mx, my)) return;
            for (int i = 0; i < done.length; i++) {
                int sx = CS_X + i * (CS_W + CS_GAP);
                if (done[i] != null && hit(mx, my, sx, CS_Y, CS_W, CS_H)) {
                    previewIdx = i; previewDone = true; playSound(sndItem, 1f); return;
                }
            }
            for (int i = 0; i < grid.length; i++) {
                if (grid[i] != null && hit(mx, my, gx(i), gy(i), SLOT, SLOT_H)) {
                    boolean samePreview = (previewIdx == i && !previewDone);
                    previewIdx = i; previewDone = false;
                    if (!samePreview) playNarration(quotes[i], quoteKeys[i]);
                    playSound(sndItem, 1f);
                    grid[i] = null; isDrag = true; dragFromCmb = false;
                    dragSlot = i; dragX = mx; dragY = my; return;
                }
            }
            if (cmbSlot != -1 && hit(mx, my, CMB_X, CMB_Y, CMB, CMB_H)) {
                int s = cmbSlot; cmbSlot = -1;
                isDrag = true; dragFromCmb = true;
                dragSlot = s; dragX = mx; dragY = my; return;
            }
        }

        if (isDrag && curPressed)  { dragX = mx; dragY = my; }
        if (isDrag && justReleased) onDrop(mx, my);
    }

    /** Maps main-game room to inventory screen room index, or -1 if unknown. */
    private static int inventoryIndexForWorldRoom(Room.RoomID id) {
        if (id == null) return -1;
        switch (id) {
            case PARLOR: return 0;
            case STUDY: return 1;
            case GROUNDSKEEPER_SHED: return 2;
            case CELLAR: return 3;
            case KITCHEN: return 4;
            case SERVANTS_QUARTERS: return 5;
            case JAMES_ROOM: return 6;
            case MARGARET_ROOM: return 7;
            default: return -1;
        }
    }

    /**
     * When false, we ignore {@code resumeWorldRoom} and use the chapter fallback.
     * Stops e.g. Gap 3 from opening Parlor where no evidence exists for that gap (everything would look empty / wrong).
     */
    private static boolean worldRoomShowsChapterPickups(int chapterIndex, int invRoom) {
        switch (chapterIndex) {
            case 0: // Gap 1: parlor, study, shed (James's note is in the shed, not James's room)
                return invRoom == 0 || invRoom == 1 || invRoom == 2;
            case 1: // Gap 2: parlor, servants
                return invRoom == 0 || invRoom == 5;
            case 2: // Gap 3: kitchen, shed, servants
                return invRoom == 2 || invRoom == 4 || invRoom == 5;
            case 3: // Gap 4: parlor, shed, james
                return invRoom == 0 || invRoom == 2 || invRoom == 6;
            case 4: // Gap 5: margaret, shed
                return invRoom == 7 || invRoom == 2;
            case 5: // Gap 6: parlor, cellar
                return invRoom == 0 || invRoom == 3;
            default:
                return true;
        }
    }

    private boolean gap3SlotCollected(int slot) {
        switch (slot) {
            case 0: return objWillAmendPicked;
            case 1: return objKitchenSchedulePicked;
            case 2: return objJamesDiscardedPicked;
            case 3: return objChecklistPicked;
            default: return true;
        }
    }

    /**
     * Gap 3: kitchen slots 0–1 are available in any order.
     * Shed note (2) appears after both kitchen pieces are taken; servants checklist (3) is always available.
     */
    private boolean showGap3Pickup(int slot) {
        boolean kitchenDone = gap3SlotCollected(0) && gap3SlotCollected(1);
        switch (slot) {
            case 0:
            case 1:
                return !gap3SlotCollected(slot);
            case 2:
                return !gap3SlotCollected(slot);
            case 3:
                return !gap3SlotCollected(slot);
            default:
                return false;
        }
    }

    private static int templateSizeForChapter(int ch) {
        if (ch == 0) return 6;
        if (ch <= 3)  return 4;
        return 2;
    }

    // ── Textures ──────────────────────────────────────────────────────────────
    private Texture solid(int w, int h, Color c) {
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(c); p.fill();
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture doc(int w, int h, Color paper) {
        Pixmap p = new Pixmap(Math.max(1,w), Math.max(1,h), Pixmap.Format.RGBA8888);
        p.setColor(paper); p.fill();
        p.setColor(new Color(0,0,0,0.18f)); p.drawRectangle(0,0,Math.max(1,w),Math.max(1,h));
        p.setColor(new Color(0,0,0,0.08f));
        for (int y = Math.max(1,h)-14; y > 5; y -= 9) p.drawLine(5, y, Math.max(1,w)-5, y);
        Texture t = new Texture(p); p.dispose(); return t;
    }

    private Texture loadTex(String... paths) {
        for (String path : paths) {
            if (Gdx.files.internal(path).exists())
                return new Texture(Gdx.files.internal(path));
        }
        return null;
    }

    private void buildTextures() {
        txW = solid(1, 1, Color.WHITE);

        // Room backgrounds — Testing 3 versions (correct designs + item placement)
        txRoom     = loadOrFallback("inventory/rooms/parlor.jpg",   Color.DARK_GRAY);
        txStudy    = loadOrFallback("inventory/rooms/study.png",    Color.DARK_GRAY);
        txShed     = loadOrFallback("inventory/rooms/shed.png",     Color.DARK_GRAY);
        txCellar   = loadOrFallback("inventory/rooms/cellar.png",   Color.DARK_GRAY);
        txKitchen  = loadOrFallback("inventory/rooms/kitchen.png",  Color.DARK_GRAY);
        txServants = loadOrFallback("inventory/rooms/servants.png", Color.DARK_GRAY);
        txJames    = loadOrFallback("inventory/rooms/james.jpg",    Color.DARK_GRAY);
        txMargaret = loadOrFallback("inventory/rooms/margaret.png", Color.DARK_GRAY);

        // Item sprites — from inventory/ directory (copied from Testing 3)
        txObjSolicitor    = loadOrDoc("inventory/study_solicitor.png",   98-22, 107-38, COLORS_G1[0]);
        txObjReply        = loadOrDoc("inventory/study_reply.png",       98-22, 107-38, COLORS_G1[1]);
        txObjPatent       = loadOrDoc("inventory/study_patent.png",      98-22, 107-38, COLORS_G1[2]);
        txObjCard         = loadOrDoc("inventory/parlor_card.png",       98-22, 107-38, COLORS_G1[3]);
        txObjJamesNote    = loadOrDoc("inventory/james_note.png",        98-22, 107-38, COLORS_G1[4]);
        txObjSchedule     = loadOrDoc("inventory/shed_schedule.png",     98-22, 107-38, COLORS_G1[5]);
        txObjCertificate  = loadOrDoc("inventory/parlor_certificate.png",98-22, 107-38, COLORS_G1[0]);
        txObjGlass        = loadOrDoc("inventory/parlor_glass.png",      98-22, 107-38, COLORS_G1[1]);
        txObjRejection    = loadOrDoc("inventory/parlor_rejection.png",  98-22, 107-38, COLORS_G1[2]);
        txObjProposal     = loadOrDoc("inventory/parlor_proposal.png",   98-22, 107-38, COLORS_G1[3]);
        txObjBankbook     = loadOrDoc("inventory/shed_bankbook.png",     98-22, 107-38, COLORS_G1[0]);
        txObjWorkorder    = loadOrDoc("inventory/shed_workorder.png",    98-22, 107-38, COLORS_G1[1]);
        txObjRetreatlog   = loadOrDoc("inventory/shed_retreatlog.png",   98-22, 107-38, COLORS_G1[2]);
        // Gap 3 "Discarded Note (James)" — crumpled note prop (not the shed-named file; same art as James-room close-up)
        txObjJamesDiscarded = loadOrDocFirst(new String[] {
                "inventory/james_crumpled_note.png",
                "inventory/james_discarded_note.png",
                "inventory/james_discarded.png" }, 98-22, 107-38, COLORS_G3[2]);
        txObjBracket      = loadOrDoc("inventory/cellar_bracket.png",    98-22, 107-38, COLORS_G1[4]);
        txObjInvoice      = loadOrDoc("inventory/cellar_invoice.png",    98-22, 107-38, COLORS_G1[5]);
        txObjWillAmend    = loadOrDoc("inventory/kitchen_willamend.png", 98-22, 107-38, COLORS_G1[0]);
        txObjKitchenSchedule = loadOrDoc("inventory/kitchen_schedule.png",98-22, 107-38, COLORS_G1[1]);
        txObjChecklist    = loadOrDoc("inventory/servants_checklist.png",98-22, 107-38, COLORS_G1[2]);
        txObjServing      = loadOrDoc("inventory/servants_serving.png",  98-22, 107-38, COLORS_G1[3]);
        txObjWithdrawal   = loadOrDoc("inventory/james_withdrawal.png",  98-22, 107-38, COLORS_G1[4]);
        txObjDiary        = loadOrDoc("inventory/margaret_diary.png",    98-22, 107-38, COLORS_G1[5]);

        wlSolicitor = loadWorldLayer("study_solicitor.png");
        wlReply = loadWorldLayer("study_reply.png");
        wlPatent = loadWorldLayer("study_patent.png");
        wlCard = loadWorldLayer("parlor_card.png");
        wlJamesNote = loadWorldLayer("james_note.png");
        wlSchedule = loadWorldLayer("shed_schedule.png");
        wlCertificate = loadWorldLayer("parlor_certificate.png");
        wlGlass = loadWorldLayer("parlor_glass.png");
        wlRejection = loadWorldLayer("parlor_rejection.png");
        wlProposal = loadWorldLayer("parlor_proposal.png");
        wlBankbook = loadWorldLayer("shed_bankbook.png");
        wlWorkorder = loadWorldLayer("shed_workorder.png");
        wlRetreatlog = loadWorldLayer("shed_retreatlog.png");
        wlJamesDiscarded = loadWorldLayer("daniel_note_to_james.png");
        wlBracket = loadWorldLayer("cellar_bracket.png");
        wlInvoice = loadWorldLayer("cellar_invoice.png");
        wlWillAmend = loadWorldLayer("kitchen_willamend.png");
        wlKitchenSchedule = loadWorldLayer("kitchen_schedule.png");
        wlChecklist = loadWorldLayer("servants_checklist.png");
        wlServing = loadWorldLayer("servants_serving.png");
        wlWithdrawal = loadWorldLayer("james_withdrawal.png");
        wlDiary = loadWorldLayer("margaret_diary.png");

        txBag     = loadOrFallback("inventory/backpack.png",      Color.BROWN);
        txBagOpen = loadOrFallback("inventory/backpack_open.png", Color.BROWN);
        txClose   = solid(50, 50, new Color(0.45f, 0.10f, 0.10f, 1f));

        txBack = null;
        if (Gdx.files.internal("art/Visual Characters/back.png").exists()) {
            txBack = new Texture(Gdx.files.internal("art/Visual Characters/back.png"));
            txBack.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        // Sounds
        sndOpen    = loadSound("sfx/inventory/bag_open.ogg",    "sfx/inventory/bag_open.mp3");
        sndClose   = loadSound("sfx/inventory/bag_close.mp3",   "sfx/inventory/bag_close.ogg");
        sndItem    = loadSound("sfx/inventory/item_collect.mp3","sfx/inventory/item_pick.ogg");
        sndSuccess = loadSound("sfx/inventory/combine_success.mp3");
        sndFail    = loadSound("sfx/inventory/combine_fail.mp3");
        sndAllDone = loadSound("sfx/inventory/all_combined.ogg","sfx/inventory/all_combined.mp3");

        musInventory = null;
        if (Gdx.files.internal("sfx/inventory/inventory_music.mp3").exists()) {
            musInventory = Gdx.audio.newMusic(Gdx.files.internal("sfx/inventory/inventory_music.mp3"));
            musInventory.setLooping(true);
        }

        txExplorationSpotlight = createExplorationSpotlightTexture();
        explorationLightsFbo = new FrameBuffer(Pixmap.Format.RGBA8888, SW, SH, false);
    }

    private Texture createExplorationSpotlightTexture() {
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
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private Texture loadOrFallback(String path, Color fallback) {
        if (Gdx.files.internal(path).exists()) return new Texture(Gdx.files.internal(path));
        return solid(SW, SH, fallback);
    }

    private Texture loadOrFallback(String path1, String path2, Color fallback) {
        if (Gdx.files.internal(path1).exists()) return new Texture(Gdx.files.internal(path1));
        if (Gdx.files.internal(path2).exists()) return new Texture(Gdx.files.internal(path2));
        return solid(SW, SH, fallback);
    }

    private Texture loadOrDoc(String path, int w, int h, Color paper) {
        if (Gdx.files.internal(path).exists()) return new Texture(Gdx.files.internal(path));
        return doc(w, h, paper);
    }

    private Texture loadOrDocFirst(String[] paths, int w, int h, Color paper) {
        for (String path : paths) {
            if (Gdx.files.internal(path).exists()) return new Texture(Gdx.files.internal(path));
        }
        return doc(w, h, paper);
    }

    /** Optional PNG for drawing on the room; {@code null} if missing (falls back to {@code txObj*} in-room only). */
    private Texture loadWorldLayer(String fileName) {
        String path = "inventory/world/" + fileName;
        if (Gdx.files.internal(path).exists()) return new Texture(Gdx.files.internal(path));
        return null;
    }

    private Sound loadSound(String... paths) {
        for (String p : paths) {
            if (Gdx.files.internal(p).exists()) {
                try { return Gdx.audio.newSound(Gdx.files.internal(p)); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private void applyTemplate(int t) {
        currentTemplate = t;
        if (txInvBg != null) txInvBg.dispose();
        disposeItemTextures();
        if (txDoneItem != null) txDoneItem.dispose();

        int[] L = t == 6 ? LAYOUT_6 : t == 4 ? LAYOUT_4 : LAYOUT_2;
        GCOLS=L[0]; GROWS=L[1]; SLOT=L[2]; SLOT_H=L[3]; SGAP_X=L[4]; SGAP_Y=L[5];
        GRID_X=L[6]; GRID_Y=L[7]; CMB=L[8]; CMB_H=L[9]; CMB_X=L[10]; CMB_Y=L[11];
        CS_W=L[12]; CS_H=L[13]; CS_GAP=L[14]; CS_X=L[15]; CS_Y=L[16];

        if (t == 6) { CLO_X = INV_X + INV_W - 55; CLO_Y = INV_Y + INV_H - 55; CLO_W = 50; CLO_H = 50; }
        else        { CLO_X = 1074; CLO_Y = 595; CLO_W = 31; CLO_H = 45; }

        switch (chapterIndex) {
            case 0: names=NAMES_G1; colors=COLORS_G1; quotes=QUOTES_G1; quoteKeys=QUOTE_KEYS_G1;
                    pairs=PAIRS_G1; combined=COMBINED_G1; combinedQuotes=COMBINED_QUOTES_G1; combinedKeys=COMBINED_KEYS_G1; break;
            case 1: names=NAMES_G2; colors=COLORS_G2; quotes=QUOTES_G2; quoteKeys=QUOTE_KEYS_G2;
                    pairs=PAIRS_G2; combined=COMBINED_G2; combinedQuotes=COMBINED_QUOTES_G2; combinedKeys=COMBINED_KEYS_G2; break;
            case 2: names=NAMES_G3; colors=COLORS_G3; quotes=QUOTES_G3; quoteKeys=QUOTE_KEYS_G3;
                    pairs=PAIRS_G3; combined=COMBINED_G3; combinedQuotes=COMBINED_QUOTES_G3; combinedKeys=COMBINED_KEYS_G3; break;
            case 3: names=NAMES_G4; colors=COLORS_G4; quotes=QUOTES_G4; quoteKeys=QUOTE_KEYS_G4;
                    pairs=PAIRS_G4; combined=COMBINED_G4; combinedQuotes=COMBINED_QUOTES_G4; combinedKeys=COMBINED_KEYS_G4; break;
            case 4: names=NAMES_G5; colors=COLORS_G5; quotes=QUOTES_G5; quoteKeys=QUOTE_KEYS_G5;
                    pairs=PAIRS_G5; combined=COMBINED_G5; combinedQuotes=COMBINED_QUOTES_G5; combinedKeys=COMBINED_KEYS_G5; break;
            default:names=NAMES_G6; colors=COLORS_G6; quotes=QUOTES_G6; quoteKeys=QUOTE_KEYS_G6;
                    pairs=PAIRS_G6; combined=COMBINED_G6; combinedQuotes=COMBINED_QUOTES_G6; combinedKeys=COMBINED_KEYS_G6; break;
        }

        int itemCount = names.length;
        int cmbCount  = combined.length;

        String bgFile = "inventory/inventory_bg_" + t + ".png";
        if (Gdx.files.internal(bgFile).exists())
            txInvBg = new Texture(Gdx.files.internal(bgFile));
        else
            txInvBg = solid(INV_W, INV_H, new Color(0.12f, 0.10f, 0.08f, 0.95f));

        txItem     = new Texture[itemCount];
        txItemOwned= new boolean[itemCount];
        for (int i = 0; i < itemCount; i++) {
            txItem[i]      = itemTextureForSlot(i);
            txItemOwned[i] = false;
        }
        txDoneItem = doc(CS_W - 22, CS_H - 38, new Color(0.76f, 0.82f, 0.88f, 1f));

        grid    = new String[itemCount];
        done    = new String[cmbCount];
        cmbSlot = -1;
        isDrag  = false;
        dragSlot= -1;
        previewIdx = -1;
        flashTimer = 0f; allDoneTimer = -1f;
    }

    private Texture itemTextureForSlot(int slot) {
        // slot 0..N map to the items for the current chapter in pickup order
        switch (chapterIndex) {
            case 0: // solicitor, reply, patent, card, jamesNote, schedule
                switch (slot) { case 0: return txObjSolicitor; case 1: return txObjReply;
                    case 2: return txObjPatent; case 3: return txObjCard;
                    case 4: return txObjJamesNote; case 5: return txObjSchedule; }
                break;
            case 1: // glass, rejection, proposal, serving
                switch (slot) { case 0: return txObjGlass; case 1: return txObjRejection;
                    case 2: return txObjProposal; case 3: return txObjServing; }
                break;
            case 2: // willamend, kitchenSchedule, james discarded note, checklist
                switch (slot) { case 0: return txObjWillAmend; case 1: return txObjKitchenSchedule;
                    case 2: return txObjJamesDiscarded; case 3: return txObjChecklist; }
                break;
            case 3: // bankbook, withdrawal, workorder, certificate
                switch (slot) { case 0: return txObjBankbook; case 1: return txObjWithdrawal;
                    case 2: return txObjWorkorder; case 3: return txObjCertificate; }
                break;
            case 4: // diary, retreatlog
                switch (slot) { case 0: return txObjDiary; case 1: return txObjRetreatlog; }
                break;
            default: // invoice, bracket
                switch (slot) { case 0: return txObjInvoice; case 1: return txObjBracket; }
                break;
        }
        return doc(SLOT-22, SLOT_H-38, colors[slot % colors.length]);
    }

    private void disposeItemTextures() {
        if (txItem == null) return;
        for (int i = 0; i < txItem.length; i++) {
            if (txItemOwned != null && i < txItemOwned.length && txItemOwned[i] && txItem[i] != null)
                txItem[i].dispose();
        }
        txItem = null; txItemOwned = null;
    }

    /** Darken frame to black except a soft circle at the cursor (same technique as {@link NarratorSpotlightScreen}). */
    private void applyExplorationSpotlight() {
        if (txExplorationSpotlight == null || explorationLightsFbo == null) return;
        Vector3 m = mouse();
        float lightSize = EXPLORATION_LIGHT_HALF * 2f;
        float lightX = m.x - lightSize / 2f;
        float lightY = m.y - lightSize / 2f;

        explorationLightsFbo.begin();
        Gdx.gl.glViewport(0, 0, SW, SH);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        game.batch.setColor(Color.WHITE);
        game.batch.draw(txExplorationSpotlight, lightX, lightY, lightSize, lightSize);
        game.batch.end();
        explorationLightsFbo.end();

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        game.batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);
        game.batch.draw(explorationLightsFbo.getColorBufferTexture(), 0, SH, SW, -SH);
        game.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        game.batch.setColor(Color.WHITE);
        game.batch.end();
    }

    // Small helper: fire onComplete after 2 seconds (so sound can finish)
    private float completionTimer = -1f;
    private void scheduleComplete() { completionTimer = 2.0f; }

    private void tickCompletion(float delta) {
        if (completionTimer > 0) {
            completionTimer -= delta;
            if (completionTimer <= 0) {
                completionTimer = -1f;
                if (onComplete != null) onComplete.run();
            }
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────────
    private Vector3 mouse() {
        Vector3 v = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        game.viewport.unproject(v);
        return v;
    }

    // Navigate to a room index — separated so item-pick and nav don't conflict
    private boolean handleRoomNav(int mx, int my) {
        switch (currentRoom) {
            case 0: // Parlor — back → Study
                if (hit(mx, my, INV_BACK_X, INV_BACK_Y, INV_BACK_W, INV_BACK_H)) { currentRoom = 1; return true; }
                break;
            case 1: // Study — back → Kitchen
                if (hit(mx, my, INV_BACK_X, INV_BACK_Y, INV_BACK_W, INV_BACK_H)) { currentRoom = 4; return true; }
                break;
            case 2: // Shed — right door → Servants
                if (hit(mx, my, 1100, 111, 125, 429)) { currentRoom = 5; return true; }
                break;
            case 3: // Cellar — right stairs → Kitchen
                if (hit(mx, my, 1023, 103, 214, 392)) { currentRoom = 4; return true; }
                break;
            case 4: // Kitchen — right passage → Servants; left cellar → Cellar; back → Parlor
                if (hit(mx, my,  600, 200, 105, 200)) { currentRoom = 5; return true; }
                if (hit(mx, my,  190, 110, 110, 320)) { currentRoom = 3; return true; }
                if (hit(mx, my, INV_BACK_X, INV_BACK_Y, INV_BACK_W, INV_BACK_H)) { currentRoom = 0; return true; }
                break;
            case 5: // Servants — right door → Kitchen; left door → Shed
                if (hit(mx, my, 895, 560, 184, 98)) { currentRoom = 4; return true; }
                if (hit(mx, my,  55, 111, 125, 429)) { currentRoom = 2; return true; }
                break;
            case 6: // James — left door → Margaret; back → Servants
                if (hit(mx, my,  50, 200, 150, 300)) { currentRoom = 7; return true; }
                if (hit(mx, my, INV_BACK_X, INV_BACK_Y, INV_BACK_W, INV_BACK_H)) { currentRoom = 5; return true; }
                break;
            case 7: // Margaret — back → James
                if (hit(mx, my, INV_BACK_X, INV_BACK_Y, INV_BACK_W, INV_BACK_H)) { currentRoom = 6; return true; }
                break;
        }
        return false;
    }

    private void handleRoomClick(int mx, int my) {
        if (handleRoomNav(mx, my)) return;
        tryPickupAtRoom(mx, my, currentRoom);
    }

    /** @return true if a gap item was picked this click */
    private boolean tryPickupAtRoom(int mx, int my, int invRoom) {
        switch (invRoom) {
            case 0:
                if (chapterIndex == 0 && !objCardPicked && hit(mx, my, 511, 190, 48, 28)) {
                    objCardPicked = true; playSound(sndItem, 1f); addToSlot(3); return true;
                }
                if (chapterIndex == 1 && !objGlassPicked && hit(mx, my, 676, 209, 25, 27)) {
                    objGlassPicked = true; playSound(sndItem, 1f); addToSlot(0); return true;
                }
                if (chapterIndex == 1 && !objRejectionPicked && hit(mx, my, 672, 185, 36, 22)) {
                    objRejectionPicked = true; playSound(sndItem, 1f); addToSlot(1); return true;
                }
                if (chapterIndex == 1 && !objProposalPicked && hit(mx, my, 1024, 214, 78, 35)) {
                    objProposalPicked = true; playSound(sndItem, 1f); addToSlot(2); return true;
                }
                if (chapterIndex == 3 && !objCertificatePicked && hit(mx, my, 1120, 163, 76, 46)) {
                    objCertificatePicked = true; playSound(sndItem, 1f); addToSlot(3); return true;
                }
                break;
            case 1:
                if (chapterIndex == 0 && !objSolicitorPicked && hit(mx, my, 550, 262, 75, 31)) {
                    objSolicitorPicked = true; playSound(sndItem, 1f); addToSlot(0); return true;
                }
                if (chapterIndex == 0 && !objReplyPicked && hit(mx, my, 676, 267, 62, 30)) {
                    objReplyPicked = true; playSound(sndItem, 1f); addToSlot(1); return true;
                }
                if (chapterIndex == 0 && !objPatentPicked && hit(mx, my, 450, 264, 93, 33)) {
                    objPatentPicked = true; playSound(sndItem, 1f); addToSlot(2); return true;
                }
                break;
            case 2:
                if (chapterIndex == 0 && !objJamesNotePicked && hit(mx, my, GAP1_JAMES_NOTE_SHED_X, GAP1_JAMES_NOTE_SHED_Y, GAP1_JAMES_NOTE_SHED_W, GAP1_JAMES_NOTE_SHED_H)) {
                    objJamesNotePicked = true; playSound(sndItem, 1f); addToSlot(4); return true;
                }
                if (chapterIndex == 0 && !objSchedulePicked && hit(mx, my, 988, 387, 96, 130)) {
                    objSchedulePicked = true; playSound(sndItem, 1f); addToSlot(5); return true;
                }
                if (chapterIndex == 2 && showGap3Pickup(2) && hit(mx, my, GAP3_SHED_NOTE_X, GAP3_SHED_NOTE_Y, GAP3_SHED_NOTE_W, GAP3_SHED_NOTE_H)) {
                    objJamesDiscardedPicked = true; playSound(sndItem, 1f); addToSlot(2); return true;
                }
                if (chapterIndex == 3 && !objBankbookPicked && hit(mx, my, 529, 257, 33, 22)) {
                    objBankbookPicked = true; playSound(sndItem, 1f); addToSlot(0); return true;
                }
                if (chapterIndex == 3 && !objWorkorderPicked && hit(mx, my, 564, 265, 8, 4)) {
                    objWorkorderPicked = true; playSound(sndItem, 1f); addToSlot(2); return true;
                }
                if (chapterIndex == 4 && !objRetreatlogPicked && hit(mx, my, 699, 295, 51, 28)) {
                    objRetreatlogPicked = true; playSound(sndItem, 1f); addToSlot(1); return true;
                }
                break;
            case 3:
                if (chapterIndex == 5 && !objInvoicePicked && hit(mx, my, 1022, 140, 80, 20)) {
                    objInvoicePicked = true; playSound(sndItem, 1f); addToSlot(0); return true;
                }
                if (chapterIndex == 5 && !objBracketPicked && hit(mx, my, 384, 125, 79, 37)) {
                    objBracketPicked = true; playSound(sndItem, 1f); addToSlot(1); return true;
                }
                break;
            case 4:
                if (chapterIndex == 2 && showGap3Pickup(0)
                        && hit(mx, my, GAP3_KITCHEN_WILL_X, GAP3_KITCHEN_WILL_Y,
                        GAP3_KITCHEN_WILL_W, GAP3_KITCHEN_WILL_H)) {
                    objWillAmendPicked = true; playSound(sndItem, 1f); addToSlot(0); return true;
                }
                if (chapterIndex == 2 && showGap3Pickup(1)
                        && hit(mx, my, GAP3_KITCHEN_SCHEDULE_X, GAP3_KITCHEN_SCHEDULE_Y,
                        GAP3_KITCHEN_SCHEDULE_W, GAP3_KITCHEN_SCHEDULE_H)) {
                    objKitchenSchedulePicked = true; playSound(sndItem, 1f); addToSlot(1); return true;
                }
                break;
            case 5:
                if (chapterIndex == 1 && !objServingPicked && hit(mx, my, 590, 290, 35, 30)) {
                    objServingPicked = true; playSound(sndItem, 1f); addToSlot(3); return true;
                }
                if (chapterIndex == 2 && showGap3Pickup(3)
                        && hit(mx, my, GAP3_SERVANTS_CHECKLIST_X, GAP3_SERVANTS_CHECKLIST_Y, GAP3_SERVANTS_CHECKLIST_W, GAP3_SERVANTS_CHECKLIST_H)) {
                    objChecklistPicked = true; playSound(sndItem, 1f); addToSlot(3); return true;
                }
                break;
            case 6:
                if (chapterIndex == 3 && !objWithdrawalPicked && hit(mx, my, 244, 158, 68, 21)) {
                    objWithdrawalPicked = true; playSound(sndItem, 1f); addToSlot(1); return true;
                }
                break;
            case 7:
                if (chapterIndex == 4 && !objDiaryPicked && hit(mx, my, 277, 268, 39, 16)) {
                    objDiaryPicked = true; playSound(sndItem, 1f); addToSlot(0); return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    private void onDrop(int mx, int my) {
        if (hit(mx, my, CMB_X, CMB_Y, CMB, CMB_H)) {
            if (cmbSlot == -1) {
                cmbSlot = dragSlot;
            } else {
                int a = cmbSlot, b = dragSlot;
                boolean success = false;
                for (int p = 0; p < pairs.length; p++) {
                    int pa = pairs[p][0], pb = pairs[p][1];
                    if ((a==pa&&b==pb)||(a==pb&&b==pa)) {
                        for (int s = 0; s < done.length; s++) {
                            if (done[s] == null) { done[s] = combined[p]; break; }
                        }
                        grid[a] = null; grid[b] = null; cmbSlot = -1;
                        playNarration(combinedQuotes[p], combinedKeys[p]);
                        playSound(sndSuccess, 1f);
                        success = true;

                        // Check if all done
                        boolean allDone = true;
                        for (String d : done) if (d == null) { allDone = false; break; }
                        if (allDone) {
                            completionLocked = true;
                            waitingForCompletionNarration = true;
                            playNarration(
                                "I remember now. There was a recording. Use the detector.",
                                "nar_g1_complete");
                        }
                        break;
                    }
                }
                if (!success) {
                    grid[a] = names[a]; grid[b] = names[b]; cmbSlot = -1;
                    flashTimer = FLASH_DUR;
                    playSound(sndFail, 1f);
                    int wi = rng.nextInt(WRONG_COMBO_KEYS.length);
                    playNarration(WRONG_COMBO_QUOTES[wi], WRONG_COMBO_KEYS[wi]);
                }
            }
        } else {
            grid[dragSlot] = names[dragSlot];
            if (dragFromCmb) cmbSlot = -1;
        }
        isDrag = false; dragSlot = -1; dragFromCmb = false;
    }

    private void closeInventory() {
        open = false; isDrag = false; dragSlot = -1;
        if (cmbSlot != -1) { grid[cmbSlot] = names[cmbSlot]; cmbSlot = -1; }
        playSound(sndOpen, 1f);
        musDelay = -1f;
        musFade = 2;
    }

    private void addToSlot(int slot) {
        if (grid != null && slot < grid.length) grid[slot] = names[slot];
    }

    // ── Draw ──────────────────────────────────────────────────────────────────
    /** Room backdrop only (no pickups, no bag). */
    private void drawRoomBackground() {
        switch (currentRoom) {
            case 0: // Parlor
                if (txRoom != null) game.batch.draw(txRoom, 0, 0, SW, SH);
                break;
            case 1: // Study
                if (txStudy != null) game.batch.draw(txStudy, 0, 0, SW, SH);
                break;
            case 2: // Shed
                if (txShed != null) game.batch.draw(txShed, SW, 0, -SW, SH);
                break;
            case 3: // Cellar
                if (txCellar != null) game.batch.draw(txCellar, 0, 0, SW, SH);
                break;
            case 4: // Kitchen
                if (txKitchen != null) game.batch.draw(txKitchen, 0, 0, SW, SH);
                break;
            case 5: // Servants
                if (txServants != null) game.batch.draw(txServants, 0, 0, SW, SH);
                break;
            case 6: // James
                if (txJames != null) game.batch.draw(txJames, 0, 0, SW, SH);
                break;
            default: // Margaret
                if (txMargaret != null) game.batch.draw(txMargaret, 0, 0, SW, SH);
                break;
        }
    }

    /**
     * Room exploration: backdrop, then full-size world overlays (if present), then bag icon.
     */
    private void drawRoom() {
        drawRoomBackground();
        drawPickupsForInventoryRoom(game.batch, currentRoom);
        drawInventoryBackArrowIfApplicable();
        if (txBag != null) game.batch.draw(txBag, BTN_X, BTN_Y, BTN_W, BTN_H);
    }

    /** Only this gap's unpicked items in {@code invRoom} (0–7). */
    private void drawPickupsForInventoryRoom(SpriteBatch batch, int invRoom) {
        switch (invRoom) {
            case 0:
                if (chapterIndex == 0 && !objCardPicked)        drawRoomPickup(batch, invRoom, wlCard,        txObjCard,        511, 190, 48, 28);
                if (chapterIndex == 1 && !objGlassPicked)        drawRoomPickup(batch, invRoom, wlGlass,       txObjGlass,       676, 209, 25, 27);
                if (chapterIndex == 1 && !objRejectionPicked)   drawRoomPickup(batch, invRoom, wlRejection,   txObjRejection,   672, 185, 36, 22);
                if (chapterIndex == 1 && !objProposalPicked)    drawRoomPickup(batch, invRoom, wlProposal,    txObjProposal,   1024, 214, 78, 35);
                if (chapterIndex == 3 && !objCertificatePicked)  drawRoomPickup(batch, invRoom, wlCertificate, txObjCertificate,1120, 163, 76, 46);
                break;
            case 1:
                if (chapterIndex == 0 && !objSolicitorPicked) drawRoomPickup(batch, invRoom, wlSolicitor, txObjSolicitor, 550, 262, 75, 31);
                if (chapterIndex == 0 && !objReplyPicked)     drawRoomPickup(batch, invRoom, wlReply,     txObjReply,     676, 267, 62, 30);
                if (chapterIndex == 0 && !objPatentPicked)    drawRoomPickup(batch, invRoom, wlPatent,    txObjPatent,    450, 264, 93, 33);
                break;
            case 2:
                // Draw schedule layer first; James's note world layer is mostly transparent so the letter reads on top.
                if (chapterIndex == 0 && !objSchedulePicked)   drawRoomPickup(batch, invRoom, wlSchedule,   txObjSchedule,   988, 387, 96, 130);
                if (chapterIndex == 0 && !objJamesNotePicked)  drawRoomPickup(batch, invRoom, wlJamesNote,    txObjJamesNote,    GAP1_JAMES_NOTE_SHED_X, GAP1_JAMES_NOTE_SHED_Y, GAP1_JAMES_NOTE_SHED_W, GAP1_JAMES_NOTE_SHED_H);
                if (chapterIndex == 2 && showGap3Pickup(2)) drawRoomPickup(batch, invRoom, wlJamesDiscarded, txObjJamesDiscarded, GAP3_SHED_NOTE_X, GAP3_SHED_NOTE_Y, GAP3_SHED_NOTE_W, GAP3_SHED_NOTE_H);
                if (chapterIndex == 3 && !objBankbookPicked)   drawRoomPickup(batch, invRoom, wlBankbook,    txObjBankbook,    529, 257, 33, 22);
                if (chapterIndex == 3 && !objWorkorderPicked)  drawRoomPickup(batch, invRoom, wlWorkorder,   txObjWorkorder,   564, 265,  8,  4);
                if (chapterIndex == 4 && !objRetreatlogPicked) drawRoomPickup(batch, invRoom, wlRetreatlog,  txObjRetreatlog,  699, 295, 51, 28);
                break;
            case 3:
                if (chapterIndex == 5 && !objInvoicePicked) drawRoomPickup(batch, invRoom, wlInvoice, txObjInvoice, 1022, 140, 80, 20);
                if (chapterIndex == 5 && !objBracketPicked) drawRoomPickup(batch, invRoom, wlBracket, txObjBracket, 384, 125, 79, 37);
                break;
            case 4:
                if (chapterIndex == 2 && showGap3Pickup(0)) drawRoomPickup(batch, invRoom, wlWillAmend,       txObjWillAmend,       GAP3_KITCHEN_WILL_X, GAP3_KITCHEN_WILL_Y, GAP3_KITCHEN_WILL_W, GAP3_KITCHEN_WILL_H);
                if (chapterIndex == 2 && showGap3Pickup(1)) drawRoomPickup(batch, invRoom, wlKitchenSchedule, txObjKitchenSchedule, GAP3_KITCHEN_SCHEDULE_X, GAP3_KITCHEN_SCHEDULE_Y, GAP3_KITCHEN_SCHEDULE_W, GAP3_KITCHEN_SCHEDULE_H);
                break;
            case 5:
                if (chapterIndex == 1 && !objServingPicked)   drawRoomPickup(batch, invRoom, wlServing,   txObjServing,   590, 290, 35, 30);
                if (chapterIndex == 2 && showGap3Pickup(3)) drawRoomPickup(batch, invRoom, wlChecklist, txObjChecklist,
                        GAP3_SERVANTS_CHECKLIST_X, GAP3_SERVANTS_CHECKLIST_Y, GAP3_SERVANTS_CHECKLIST_W, GAP3_SERVANTS_CHECKLIST_H);
                break;
            case 6:
                if (chapterIndex == 3 && !objWithdrawalPicked) drawRoomPickup(batch, invRoom, wlWithdrawal,   txObjWithdrawal,   244, 158, 68, 21);
                break;
            case 7:
                if (chapterIndex == 4 && !objDiaryPicked) drawRoomPickup(batch, invRoom, wlDiary, txObjDiary, 277, 268, 39, 16);
                break;
            default:
                break;
        }
    }

    private void drawRoomPickup(SpriteBatch batch, int invRoom, Texture worldLayer, Texture inventorySprite,
            int hitX, int hitY, int hitW, int hitH) {
        // Only full-bleed room overlays should replace the view; small prop PNGs in inventory/world/ would
        // stretch to the full screen and read as missing/wrong — fall back to the inventory sprite on the hit rect.
        if (worldLayer != null && isFullRoomWorldOverlay(worldLayer)) {
            drawPickupFullRoom(batch, invRoom, worldLayer);
            return;
        }
        if (inventorySprite != null)
            drawPickupLayer(batch, inventorySprite, hitX, hitY, hitW, hitH, false);
    }

    private static boolean isFullRoomWorldOverlay(Texture t) {
        if (t == null) return false;
        return t.getWidth() >= SW * 0.85f && t.getHeight() >= SH * 0.85f;
    }

    /** Shed ({@code invRoom == 2}) uses the same horizontal flip as inventory shed art. */
    private void drawPickupFullRoom(SpriteBatch batch, int invRoom, Texture tex) {
        if (tex == null) return;
        if (invRoom == 2) batch.draw(tex, SW, 0, -SW, SH);
        else batch.draw(tex, 0, 0, SW, SH);
    }

    /**
     * Fallback when no {@code inventory/world/} PNG exists: draw icon centered on the click rect.
     */
    /** Parlor, Study, Kitchen, James, Margaret use the same back control as {@link GameScreen}. */
    private boolean inventoryRoomHasBackArrow() {
        switch (currentRoom) {
            case 0:
            case 1:
            case 4:
            case 6:
            case 7:
                return true;
            default:
                return false;
        }
    }

    private void drawInventoryBackArrowIfApplicable() {
        if (!inventoryRoomHasBackArrow() || txBack == null) return;
        game.batch.draw(txBack, INV_BACK_X, INV_BACK_Y, INV_BACK_W, INV_BACK_H);
    }

    private void drawPickupLayer(SpriteBatch batch, Texture tex, int hitX, int hitY, int hitW, int hitH,
            boolean flipX) {
        if (tex == null) return;
        float cx = hitX + hitW / 2f;
        float cy = hitY + hitH / 2f;
        float boxW = Math.min(124f, Math.max(hitW + 26f, hitW * 2.35f + 22f));
        float boxH = Math.min(112f, Math.max(hitH + 20f, hitH * 2.55f + 18f));
        float drawX = cx - boxW / 2f;
        float drawY = cy - boxH / 2f;
        if (!flipX) {
            drawTextureFit(batch, tex, drawX, drawY, boxW, boxH);
            return;
        }
        float tw = tex.getWidth(), th = tex.getHeight();
        if (tw <= 0 || th <= 0) {
            batch.draw(tex, drawX + boxW, drawY, -boxW, boxH);
            return;
        }
        float scale = Math.min(boxW / tw, boxH / th);
        float dw = tw * scale, dh = th * scale;
        batch.draw(tex, drawX + (boxW - dw) * 0.5f + dw, drawY + (boxH - dh) * 0.5f, -dw, dh);
    }

    private void drawInventory() {
        drawRoomBackground();
        drawPickupsForInventoryRoom(game.batch, currentRoom);
        drawInventoryBackArrowIfApplicable();
        if (txInvBg != null)  game.batch.draw(txInvBg, INV_X, INV_Y, INV_W, INV_H);
        if (txBagOpen != null)game.batch.draw(txBagOpen, BTN_X, BTN_Y, BTN_W, BTN_H);
        drawGrid(game.batch);
        drawCombineBox(game.batch);
        drawCombinedSection(game.batch);
        drawRightPanel(game.batch);

        if (txClose != null) game.batch.draw(txClose, CLO_X, CLO_Y, CLO_W, CLO_H);

        if (isDrag && dragSlot >= 0 && txItem != null && dragSlot < txItem.length) {
            game.batch.setColor(1,1,1,0.70f);
            game.batch.draw(txItem[dragSlot], dragX-(SLOT-8)/2f, dragY-(SLOT_H-8)/2f, SLOT-8, SLOT_H-8);
            game.batch.setColor(Color.WHITE);
        }
    }

    private void drawGrid(SpriteBatch batch) {
        Vector3 m = mouse();
        int mx=(int)m.x, my=(int)m.y;
        for (int i = 0; i < grid.length; i++) {
            int sx = gx(i), sy = gy(i);
            boolean hov = hit(mx,my,sx,sy,SLOT,SLOT_H);
            boolean lifting = (isDrag && dragSlot == i && !dragFromCmb);
            if (hov) {
                batch.setColor(1f,1f,1f,0.12f);
                batch.draw(txW, sx, sy, SLOT, SLOT_H);
                batch.setColor(Color.WHITE);
            }
            if (grid[i] != null && !lifting && txItem != null && i < txItem.length)
                drawTextureFit(batch, txItem[i], sx+4, sy+4, SLOT-8, SLOT_H-8);
        }
    }

    private void drawCombineBox(SpriteBatch batch) {
        if (flashTimer > 0) {
            batch.setColor(0.55f,0.10f,0.10f,0.55f);
            batch.draw(txW, CMB_X, CMB_Y, CMB, CMB_H);
            batch.setColor(Color.WHITE);
        } else if (cmbSlot != -1 && txItem != null && cmbSlot < txItem.length) {
            drawTextureFit(batch, txItem[cmbSlot], CMB_X+4, CMB_Y+4, CMB-8, CMB_H-8);
        }
    }

    private void drawCombinedSection(SpriteBatch batch) {
        for (int i = 0; i < done.length; i++) {
            int sx = CS_X + i * (CS_W + CS_GAP);
            if (done[i] != null && txDoneItem != null)
                batch.draw(txDoneItem, sx+4, CS_Y+4, CS_W-8, CS_H-8);
        }
    }

    private void drawRightPanel(SpriteBatch batch) {
        if (previewIdx < 0) return;
        boolean dc = previewDone;
        String  name   = dc ? (previewIdx<done.length?done[previewIdx]:null) : (previewIdx<grid.length&&grid[previewIdx]!=null?grid[previewIdx]:names[previewIdx]);
        String  quote  = dc ? (previewIdx<combinedQuotes.length?combinedQuotes[previewIdx]:"") : (previewIdx<quotes.length?quotes[previewIdx]:"");
        Texture sprite = dc ? txDoneItem : (txItem!=null&&previewIdx<txItem.length?txItem[previewIdx]:null);

        if (sprite != null) drawTextureFit(batch, sprite, 844, 293, 250, 226);
        if (name != null) {
            small.setColor(new Color(0.35f,0.25f,0.10f,1f));
            drawCtr(batch, small, name, 844, 278, 250);
        }
        if (quote != null && !quote.isEmpty()) {
            tiny.setColor(new Color(0.35f,0.28f,0.18f,1f));
            drawWrappedFont(batch, tiny, quote, 849, 261, 240, 14, 169f);
        }
    }

    // ── Audio ─────────────────────────────────────────────────────────────────
    private void playNarration(String text, String key) {
        narText = text;
        playNarratorVoice(key);
    }

    private void playNarratorVoice(String key) {
        if (key == null || key.isEmpty()) return;
        if (musNarrator != null) { musNarrator.stop(); musNarrator.dispose(); musNarrator = null; }
        String[] candidates = {
            key + ".wav",
            key + ".mp3",
            key + ".ogg",
            key + ".m4a",
            "sfx/narrator/inventory/" + key + ".wav",
            "sfx/narrator/inventory/" + key + ".mp3",
            "sfx/narrator/inventory/" + key + ".ogg",
            "sfx/narrator/inventory/" + key + ".m4a"
        };
        for (String path : candidates) {
            FileHandle fh = Gdx.files.internal(path);
            if (!fh.exists()) continue;
            try {
                musNarrator = Gdx.audio.newMusic(fh);
                musNarrator.setLooping(false);
                musNarrator.play();
                return;
            } catch (Exception ignored) {}
        }
    }

    private boolean isNarratorPlaying() {
        return musNarrator != null && musNarrator.isPlaying();
    }

    /** Gap 3 servants pickup also needs mirrored X in embedded mode. */
    private int gap3ServantsX(int baseX, int width) {
        if (!embedded) return baseX;
        return SW - (baseX + width);
    }

    /** Exposed so GameScreen can consume all input during the completion narration lock. */
    public boolean isCompletionLocked() {
        return completionLocked;
    }

    private void playSound(Sound s, float vol) {
        if (s != null) s.play(vol);
    }

    // ── Text helpers ──────────────────────────────────────────────────────────
    private void drawCtr(SpriteBatch batch, BitmapFont f, String s, float ax, float y, float aw) {
        lay.setText(f, s);
        f.draw(batch, s, ax + (aw - lay.width) / 2f, y);
    }

    private void drawWrappedFont(SpriteBatch batch, BitmapFont f, String text, int x, float y,
            int maxW, int lineH, float minY) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            lay.setText(f, test);
            if (lay.width > maxW && line.length() > 0) {
                if (y > minY) f.draw(batch, line.toString(), x, y);
                y -= lineH; line = new StringBuilder(w);
            } else line = new StringBuilder(test);
        }
        if (line.length() > 0 && y > minY) f.draw(batch, line.toString(), x, y);
    }

    private void drawTextureFit(SpriteBatch batch, Texture tx, float x, float y, float w, float h) {
        if (tx == null) return;
        float tw = tx.getWidth(), th = tx.getHeight();
        if (tw <= 0 || th <= 0) { batch.draw(tx, x, y, w, h); return; }
        float scale = Math.min(w / tw, h / th);
        float dw = tw * scale, dh = th * scale;
        batch.draw(tx, x + (w - dw) * 0.5f, y + (h - dh) * 0.5f, dw, dh);
    }

    // ── Geometry ──────────────────────────────────────────────────────────────
    private int gx(int i) { return GRID_X + (i % GCOLS) * (SLOT + SGAP_X); }
    private int gy(int i) { return GRID_Y + (GROWS-1 - i/GCOLS) * (SLOT_H + SGAP_Y); }
    private boolean hit(int mx, int my, int x, int y, int w, int h) {
        return mx>=x && mx<=x+w && my>=y && my<=y+h;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    public void dispose() {
        font.dispose(); small.dispose(); tiny.dispose();
        safeDispose(txW);
        safeDispose(txExplorationSpotlight);
        if (explorationLightsFbo != null) {
            explorationLightsFbo.dispose();
            explorationLightsFbo = null;
        }
        safeDispose(txRoom); safeDispose(txStudy); safeDispose(txShed);
        safeDispose(txCellar); safeDispose(txKitchen); safeDispose(txServants);
        safeDispose(txJames); safeDispose(txMargaret);
        safeDispose(txObjSolicitor); safeDispose(txObjReply); safeDispose(txObjPatent);
        safeDispose(txObjCard); safeDispose(txObjJamesNote); safeDispose(txObjSchedule);
        safeDispose(txObjCertificate); safeDispose(txObjGlass); safeDispose(txObjRejection);
        safeDispose(txObjProposal); safeDispose(txObjBankbook); safeDispose(txObjWorkorder);
        safeDispose(txObjRetreatlog); safeDispose(txObjJamesDiscarded);
        safeDispose(txObjBracket); safeDispose(txObjInvoice);
        safeDispose(txObjWillAmend); safeDispose(txObjKitchenSchedule);
        safeDispose(txObjChecklist); safeDispose(txObjServing);
        safeDispose(txObjWithdrawal); safeDispose(txObjDiary);
        safeDispose(wlSolicitor); safeDispose(wlReply); safeDispose(wlPatent);
        safeDispose(wlCard); safeDispose(wlJamesNote); safeDispose(wlSchedule);
        safeDispose(wlCertificate); safeDispose(wlGlass); safeDispose(wlRejection); safeDispose(wlProposal);
        safeDispose(wlBankbook); safeDispose(wlWorkorder); safeDispose(wlRetreatlog); safeDispose(wlJamesDiscarded);
        safeDispose(wlBracket); safeDispose(wlInvoice); safeDispose(wlWillAmend); safeDispose(wlKitchenSchedule);
        safeDispose(wlChecklist); safeDispose(wlServing); safeDispose(wlWithdrawal); safeDispose(wlDiary);
        safeDispose(txBag); safeDispose(txBagOpen); safeDispose(txClose); safeDispose(txBack);
        safeDispose(txInvBg); safeDispose(txDoneItem);
        disposeItemTextures();
        if (sndOpen    != null) sndOpen.dispose();
        if (sndClose   != null) sndClose.dispose();
        if (sndItem    != null) sndItem.dispose();
        if (sndSuccess != null) sndSuccess.dispose();
        if (sndFail    != null) sndFail.dispose();
        if (sndAllDone != null) sndAllDone.dispose();
        if (musInventory != null) { musInventory.stop(); musInventory.dispose(); }
        if (musNarrator  != null) { musNarrator.stop();  musNarrator.dispose(); }
    }

    private void safeDispose(Texture t) { if (t != null) t.dispose(); }
}
