# The Unreliable Narrator — Complete Project Overview

> Auto-generated from a full parallel scan of all project files on 2026-02-17.
> This document consolidates every file, system, piece of data, design note, and outstanding task in the project.

---

## Table of Contents

1. [Project Identity](#1-project-identity)
2. [Build System & Project Structure](#2-build-system--project-structure)
3. [Game Concept & Premise](#3-game-concept--premise)
4. [World & Setting](#4-world--setting)
5. [Timeline of Events](#5-timeline-of-events)
6. [Characters](#6-characters)
7. [State Enums](#7-state-enums)
8. [GameState](#8-gamestate)
9. [Navigation System](#9-navigation-system)
10. [Game Systems](#10-game-systems)
11. [Data Files](#11-data-files)
12. [UI Components](#12-ui-components)
13. [Screens](#13-screens)
14. [Art Assets](#14-art-assets)
15. [Three-Act Structure & Navigation Gates](#15-three-act-structure--navigation-gates)
16. [Narrator Channeling Mechanic](#16-narrator-channeling-mechanic)
17. [Mini-Games](#17-mini-games)
18. [Achievements](#18-achievements)
19. [Save / Load System](#19-save--load-system)
20. [Feature Completion Status (TODO)](#20-feature-completion-status-todo)
21. [Changelogs & Design History](#21-changelogs--design-history)
22. [Open Design Items & Corrections](#22-open-design-items--corrections)
23. [File Index](#23-file-index)

---

## 1. Project Identity

| Field | Value |
|-------|-------|
| **Title** | The Unreliable Narrator |
| **Genre** | Horror Murder Mystery (LibGDX 2D first-person point-and-click) |
| **Engine** | LibGDX (Java) |
| **Build** | Gradle (9.2.1), Java 8 source compatibility |
| **Resolution** | 1280 × 720 (FitViewport, letterboxed fullscreen) |
| **Launcher** | LWJGL3 Desktop |
| **Package** | `com.dsa.game` |
| **Version** | 1.0.0 |
| **Run** | `./gradlew lwjgl3:run` |
| **Build jar** | `./gradlew lwjgl3:jar` → `lwjgl3/build/libs/DSAGame-1.0.0.jar` |

---

## 2. Build System & Project Structure

### `settings.gradle`
Includes subprojects: `lwjgl3`, `core`.

### `build.gradle` (root)
- Eclipse + IDEA plugins on all projects
- `generateAssetList` task: reads `assets/` directory, writes `assets/assets.txt`
- Java 8 source compatibility
- Repos: mavenCentral, mavenLocal, sonatype snapshots, jitpack
- App name: `DSAGame`

### `core/build.gradle`
LibGDX core dependency.

### `lwjgl3/build.gradle`
LWJGL3 backend + native desktop deps. Produces `DSAGame-1.0.0.jar`.

### `gradle.properties`
Defines `projectVersion`.

### Source Tree
```
core/src/main/java/com/dsa/game/
├── DSAGame.java                    Main game class (camera, viewport, SpriteBatch)
├── data/
│   ├── ClimaxContent.java          Tape 8 climax narrative + 3 moral endings
│   ├── NarratorText.java           Narrator mood/warning/cue/distortion text + channeling dialogue
│   ├── RoomDescriptions.java       Dynamic room descriptions + examinable object lists
│   ├── SuspectDialogue.java        Greetings, 25 topic responses, 17 evidence reactions, lies, deflections
│   └── TapeContent.java            All 8 tape transcripts
├── navigation/
│   ├── Direction.java              NORTH/SOUTH/EAST/WEST/UP/DOWN/ENTER enum
│   ├── Hotspot.java                Clickable area (type, direction, targetRoom, bounds, tooltip)
│   ├── HotspotPositions.java       Standard hotspot position constants + factory
│   ├── Room.java                   Room data (ID, name, description, hotspots, connections)
│   └── RoomManager.java            All 10 rooms, hotspots, connections; navigation logic
├── rendering/
│   └── PlaceholderGenerator.java   Programmatic room/arrow/door/examine-icon textures
├── screens/
│   ├── GameScreen.java             Full game orchestrator (all systems, UI, input dispatch)
│   └── TitleScreen.java            Title screen / main menu
├── state/
│   ├── Achievement.java            8 achievement definitions enum
│   ├── Contradiction.java          WEAPON, BODY_POSITION + 4 NARRATOR_ types enum
│   ├── EntityAnomaly.java          7 anomaly types enum
│   ├── Evidence.java               10 evidence items enum
│   ├── GameState.java              All mutable game state, tracking counters, save/load support
│   ├── Suspect.java                5 suspects enum with age + cooperation
│   └── Tape.java                   8 tapes enum with room + object location
├── systems/
│   ├── AchievementSystem.java      Achievement unlock checks on win
│   ├── AwarenessSystem.java        Threshold warnings, level names, color index
│   ├── EvidenceSystem.java         Collection, accusation validation (James/Daniel evidence counts)
│   ├── ExamResult.java             Data holder: text + optional tape + MiniGameType enum
│   ├── ExaminationSystem.java      Routes room + object + count → ExamResult
│   ├── HintSystem.java             3-tier progressive hints, 5 categories, per-suspect hints
│   ├── InterviewSystem.java        Topic responses, evidence reactions, contradictions, cooperation
│   ├── NarratorSystem.java         Mood-shifting narrator, distortions, atmospheric events
│   └── SaveLoadSystem.java         JSON serialization, 3 save slots (LibGDX FileHandle)
└── ui/
    ├── ActionBar.java              Compact centered 5-button cluster (36px high, 60% opacity)
    ├── AwarenessMeter.java         4px top-edge strip + hover tooltip ("12/80 DORMANT")
    ├── DocumentPiece.java          Draggable paper fragment for document reconstruction
    ├── DocumentReconstructionGame.java  Torn-letter mini-game overlay
    ├── TextButton.java             Clickable button with hover/disabled states
    └── TextPanel.java              Overlay panel (word-wrap, scroll, action buttons)

lwjgl3/src/main/java/com/dsa/game/lwjgl3/
├── Lwjgl3Launcher.java             Entry point (fullscreen, 60fps)
└── StartupHelper.java              macOS startup helper

assets/
├── assets.txt                      Auto-generated asset list
├── libgdx.png
└── rooms/
    ├── coverscreen.png
    ├── entrance.png
    ├── guest_rooms.png
    └── kitchen.png

art/
├── characters/
│   ├── Assistant.jpg
│   ├── Darkman.jpg
│   ├── Groundskeeper.jpg
│   └── Sister.jpg
└── rooms/
    ├── ROOM_ART_GUIDE.md
    ├── coverscreen.png
    └── entrance.png
```

---

## 3. Game Concept & Premise

**The Unreliable Narrator** is a horror murder mystery set in **Vance Manor**, a grand Victorian estate. The murder of Harold Vance occurred on November 15th, 1987.

**Player framing (confirmed February 17):**
- The player is a **stranger who stumbled upon Vance Manor by accident** — not a hired PI
- Something supernatural is pulling them to investigate
- The murder happened **decades ago** — this is a cold case
- Nobody hired them; they have no professional reason to be there
- They discover Arthur Hollis's old tape recordings and are drawn in

**The Narrator** guides the player. The narrator is the consciousness of the most recent investigator who died in Vance Manor. They channel memories of suspects and are deeply confused about their own nature and abilities.

**Core tension:** Balance investigation thoroughness against the Awareness Meter. Every action draws the Entity closer. At 80 awareness = game over.

---

## 4. World & Setting

**Vance Manor:**
- Victorian Gothic Revival, built 1890
- Rural estate outside town
- Two floors + cellar, Groundskeeper's Shed accessible from servants' quarters

**The Entity:**
- Thomas Ashford — Harold's business partner, entombed alive behind the cellar wall in 1957
- Spent 30 years in darkness, became something inhuman, fused with the house
- The Awareness Meter tracks Thomas noticing the player

**Arthur Hollis (1987):**
- Private investigator who solved the murder by Day 13
- Found all tapes, hid them around the manor
- Discovered the cellar wall → opened it → released the Entity
- Became the Entity's first victim; his consciousness became the first Narrator
- The current Narrator is the most recent failure in the cycle

---

## 5. Timeline of Events

### 1957
- Harold and Thomas Ashford co-found Vance Pharmaceutical
- Harold entombs Thomas alive behind the cellar wall to steal the company
- Official story: Thomas "retired abroad"

### 1957–1987
- Thomas suffers in darkness for 30 years, transforms into the Entity

### November 15, 1987 — The Murder Night

| Time | Event |
|------|-------|
| 7:00 PM | Dinner: Harold, James, Margaret, Daniel, Marcus, Charles |
| 8:30 PM | Harold abandons Marcus in parlor; Charles prepares paperwork in study |
| 10:00 PM | James argues violently with Harold about embezzlement (heard by Marcus, Margaret) |
| 10:45 PM | James returns to study; kills Harold with fireplace poker (Charles sees him from window) |
| 11:00 PM | Marcus leaves (Charles hears car); James + Daniel begin cover-up |
| 11:30 PM–2:00 AM | Body moved from study to cellar; bloodstained shirt hidden behind flour sacks |
| 2:00 AM | Margaret hears dragging sounds toward cellar |
| 7:00 AM (Nov 16) | Charles finds body in cellar; calls Daniel; calls police |

### November 17–21, 1987 — Aftermath
- Police interview all 5 suspects (Tapes 2–6)
- Margaret records personal witness account (Tape 7)
- Arthur Hollis hired to investigate

### November 19, 1987 — Arthur's Doom
- Arthur solves the murder by Day 13, has enough evidence
- Day 19: Discovers cellar wall, hears Thomas's voice, opens the wall
- Thomas kills Arthur; Arthur becomes the first Narrator (Tape 8)

### 1987–Present
- Cycle repeats: investigators arrive, find tapes, draw Entity's attention, die
- Each death creates a new Narrator
- Player arrives as the newest investigator

---

## 6. Characters

### Harold Vance (Victim)
- Wealthy owner of Vance Manor, co-founder Vance Pharmaceutical
- Entombed Thomas Ashford alive in 1957; fabricated "retired abroad" story
- Cut daughter Margaret out of family; exploited son James and groundskeeper Daniel
- Murdered in his study on November 15, 1987

### Thomas Ashford (The Entity)
- Co-founder Vance Pharmaceutical; the real chemist
- Betrayed by Harold, entombed alive behind cellar wall in 1957
- 30 years of suffering transformed him into the Entity
- Fused with Vance Manor itself
- Awareness Meter = Thomas noticing the player

### Arthur Hollis (The First Investigator / First Narrator)
- Private investigator, solved the murder in 2 weeks (1987)
- Discovered hidden tape recorders, re-hid them in new locations
- Found the false cellar wall; opened it → died → became first Narrator
- Tape 8 ("The Opening") is his final recording

### The Narrator (Most Recent Victim)
- **NOT Arthur Hollis** — the most recent investigator to die at Vance Manor
- Channels suspect memories (doesn't know how or why)
- Guides player through investigation
- Must NEVER reveal identity or that they're a dead investigator
- Growing confusion and desperation as awareness rises
- **Personality shifts by awareness:**
  - 0–19 DORMANT: HOPEFUL — clear and helpful
  - 20–39 SUSPICIOUS: CONFUSED — confused, losing memory
  - 40–59 ALERT: ANXIOUS — anxious, hearing things
  - 60–79 DANGEROUS/CRITICAL: FRANTIC — urgent, frantic

### Suspects

| Suspect | Age | Starting Coop | Role |
|---------|-----|---------------|------|
| James Vance | 52 | 70 | Harold's son; **murderer** with Daniel |
| Margaret Vance | 48 | 60 | Harold's daughter; innocent witness |
| Daniel the Groundskeeper | 63 | 50 | Groundskeeper; **accomplice** to James |
| Marcus Blackwood | 55 | 55 | Rival CEO; apparent motive, cleared by investigation |
| Charles Webb | 28 | 70 | Harold's assistant; honest witness |

**The killers:** James Vance + Daniel the Groundskeeper

**Motive:**
- James embezzled £50,000 from Vance Pharmaceutical
- Harold discovered it (reviewing company books), planned to sign new will disinheriting James and fire Daniel
- James and Daniel conspired to kill Harold before the will could be signed (9:00 AM next morning)

**Murder method:** James killed Harold with the fireplace poker (blunt force trauma). Letter opener was planted as red herring. Daniel entered through study window afterward to help move the body.

---

## 7. State Enums

### Evidence.java (10 items)
| Enum | Display Name | Notes |
|------|-------------|-------|
| LETTER_OPENER | Letter Opener | Red herring — planted by James |
| FIREPLACE_POKER | Fireplace Poker | **Real murder weapon** |
| TORN_LETTER | Torn Letter | From fireplace ashes (mini-game) |
| FINANCIAL_RECORDS | Financial Records | Hidden in desk drawer |
| MUDDY_BOOTS | Muddy Boots | Daniel's — proves he went outside |
| SLEEPING_POWDER | Sleeping Powder | Near kitchen cellar door |
| BLOODSTAINED_CUFF | Bloodstained Cuff | Hidden in cellar behind flour sacks |
| WILL_COPY | Will Copy | Parlor briefcase |
| BLACKMAIL_NOTE | Blackmail Note | James planted it in Margaret's room |
| GROUNDSKEEPER_LOG | Groundskeeper Log | Missing entry for murder night |

### Tape.java (8 tapes)
| Enum | Title | Location | Object |
|------|-------|----------|--------|
| TAPE_ARGUMENT | Harold & James Argument | STUDY | under_desk |
| TAPE_JAMES_INTERVIEW | James Vance - Police Interview | STUDY | bookshelves |
| TAPE_DANIEL_INTERVIEW | Daniel Hobbs - Police Interview | GROUNDSKEEPER_SHED | logbook |
| TAPE_MARGARET_INTERVIEW | Margaret Vance - Police Interview | KITCHEN | storage_cellar |
| TAPE_MARCUS_INTERVIEW | Marcus Blackwood - Police Interview | PARLOR | briefcase |
| TAPE_CHARLES_INTERVIEW | Charles Webb - Police Interview | PARLOR | grandfather_clock |
| TAPE_MARGARET_ACCOUNT | Margaret's Personal Account | MARGARET_ROOM | dresser |
| TAPE_ARTHUR_DEATH | The Opening (**CLIMAX TRIGGER**) | CELLAR | wine_rack |

**Tape 8 notes:** Found damaged (casing cracked, ribbon snapped). Cannot be watched until Tape Repair Kit is acquired from Margaret's Room dresser. Costs +5 awareness (others +4). Triggers the climax sequence.

### Suspect.java
JAMES (52, coop 70), MARGARET (48, coop 60), DANIEL (63, coop 50), MARCUS (55, coop 55), CHARLES (28, coop 70)

### Contradiction.java (6 contradictions)
- **WEAPON** — Letter opener planted; fireplace poker is real weapon
- **BODY_POSITION** — Harold died in study, body moved to cellar by James + Daniel
- **NARRATOR_WEAPON** — Narrator falsely claimed letter opener was weapon
- **NARRATOR_BOOTS** — Narrator said no one left the house; muddy boots prove otherwise
- **NARRATOR_LETTER** — Narrator called torn letter unimportant
- **NARRATOR_TIME** — Narrator claimed death after midnight; actually 10:45 PM

### Achievement.java (8 achievements)
| Achievement | Condition |
|-------------|-----------|
| SPEEDRUN | Complete in ≤10 commands |
| GHOST | Finish with <25 awareness |
| COMPLETIONIST | All evidence + all tapes watched |
| PERFECT_INVESTIGATION | All evidence, all tapes, <30 awareness |
| GUARDIAN | Seal the Wall ending |
| ARSONIST | Destroy the Tapes ending |
| SURVIVOR | Escape the Manor ending |
| CYCLE_BREAKER | Discover all 7 anomalies + choose any ending |

### EntityAnomaly.java (7 anomalies)
| Anomaly | Discovery Location |
|---------|-------------------|
| BREATHING_WALL | Cellar wine_rack (2nd exam) — **gates Margaret's Room** |
| THOMAS_REFERENCE | Study fireplace (2nd exam) — 'THOMAS WAS RIGHT' |
| NARRATOR_I_SLIP | Random navigation (20% chance, after 3+ anomalies) |
| SCRATCHED_INITIALS | Servants' Quarters bedpost (2nd exam) — 'A.H.' |
| PHOTO_UNKNOWN_MAN | Study bookshelves (2nd exam) — Harold's photo, face scratched out |
| COLD_SPOT_CELLAR | Kitchen storage_cellar door (2nd exam) |
| CONSTRUCTION_RECORD | Study papers (2nd exam) — 1957 invoice, Harold's margin note |

---

## 8. GameState

`core/src/main/java/com/dsa/game/state/GameState.java`

All mutable game state in one class:

| Field | Type | Description |
|-------|------|-------------|
| awareness | int | 0–80; cap at 80 = game over |
| collectedEvidence | Set\<Evidence\> | Items found |
| collectedTapes | Set\<Tape\> | Tapes found |
| watchedTapes | Set\<Tape\> | Tapes played |
| cooperation | Map\<Suspect, Integer\> | 0–100 per suspect |
| discoveredContradictions | Set\<Contradiction\> | Found contradictions |
| discoveredAnomalies | Set\<EntityAnomaly\> | Found anomalies |
| visitCounts | Map\<RoomID, Integer\> | Visits per room |
| examCounts | Map\<String, Integer\> | "ROOM_OBJECT" → count |
| currentInterviewSuspect | Suspect | Active interview |
| askedTopics | Set\<String\> | "SUSPECT_TOPIC" keys |
| gameOver / gameWon | boolean | End state flags |
| accusationMade | boolean | Whether accusation fired |
| narratorHeaderShown | boolean | Intro shown once |
| commandCount | int | Total commands (for achievements) |
| interviewCount | int | Total interviews |
| evidenceShownCount | int | Evidence shown to suspects |
| climaxTriggered | boolean | Tape 8 climax fired |
| eventLog | List\<String\> | Event history |
| confrontedSuspects | Set\<Suspect\> | Confrontation tracking |
| receivedLies | Set\<String\> | Lies heard from suspects |
| narratorDistortions | List\<String\> | Distortions encountered |
| wrongAccusationCount | int | Wrong accusations made |
| hasTapeRepairKit | boolean | Act 3 gate for Tape 8 |
| chosenEnding | Ending enum | NONE / ACCUSATION_CORRECT / ... |

**Ending enum values:** NONE, ACCUSATION_CORRECT, ACCUSATION_WRONG, SEAL_THE_WALL, ESCAPE_MANOR, DESTROY_TAPES, GAME_OVER_AWARENESS

---

## 9. Navigation System

### Room.java
Rooms identified by `RoomID` enum:
`ENTRANCE, STUDY, PARLOR, KITCHEN, GUEST_ROOMS, JAMES_ROOM, MARGARET_ROOM, GROUNDSKEEPER_SHED, SERVANTS_QUARTERS, CELLAR`

Each room: id, name, description, backgroundTexturePath (`rooms/<id_lower>.png`), hotspot list, connection map (Direction → RoomID).

### Direction.java
`NORTH ("Forward"), SOUTH ("Back"), EAST ("Right"), WEST ("Left"), UP ("Upstairs"), DOWN ("Downstairs"), ENTER ("Enter")`

### Hotspot.java
Types: `ARROW_LEFT, ARROW_RIGHT, ARROW_FORWARD, ARROW_BACK, DOOR, STAIRS_UP, STAIRS_DOWN, EXAMINE`
Fields: type, direction, targetRoom, bounds (Rectangle), screenPosition, tooltip, isHovered, objectName (for EXAMINE type)

### HotspotPositions.java
Standard 1280×720 positions (LEFT=100, RIGHT=1180, FORWARD=640×570, BACK=640×100, DOOR_CENTER=640×360). Factory method `createStandardHotspot()`.

### RoomManager.java
Manages all 10 rooms. Sets up connections and hotspots. Navigation connections:

| From | Direction | To | Gate |
|------|-----------|----|----|
| ENTRANCE | EAST | STUDY | None |
| ENTRANCE | WEST | PARLOR | None |
| ENTRANCE | NORTH | KITCHEN | None |
| ENTRANCE | UP | GUEST_ROOMS | None |
| STUDY | WEST | ENTRANCE | None |
| PARLOR | EAST | ENTRANCE | None |
| KITCHEN | SOUTH | ENTRANCE | None |
| KITCHEN | WEST | SERVANTS_QUARTERS | None |
| KITCHEN | DOWN | CELLAR | **Act 2: 4+ tapes watched AND 5+ evidence** |
| GUEST_ROOMS | DOWN | ENTRANCE | None |
| GUEST_ROOMS | ENTER (left door) | MARGARET_ROOM | **Act 3: BREATHING_WALL anomaly** |
| GUEST_ROOMS | ENTER (right door) | JAMES_ROOM | None |
| JAMES_ROOM | back | GUEST_ROOMS | None |
| MARGARET_ROOM | back | GUEST_ROOMS | None |
| SERVANTS_QUARTERS | EAST | KITCHEN | None |
| SERVANTS_QUARTERS | WEST | GROUNDSKEEPER_SHED | None |
| GROUNDSKEEPER_SHED | EAST | SERVANTS_QUARTERS | None |
| CELLAR | UP | KITCHEN | None |

---

## 10. Game Systems

### AwarenessSystem.java
Awareness levels: DORMANT (0–19), SUSPICIOUS (20–39), ALERT (40–59), DANGEROUS (60–69), CRITICAL (70–79). Provides level names, warning thresholds, color indexes.

### Awareness Costs
| Action | Cost |
|--------|------|
| Move | +1 |
| Examine | +1 |
| Question / Ask | +1 |
| Show Evidence to Suspect | +2 |
| Contradiction Challenge | +2 |
| Confrontation | +3 |
| Watch Tape | +4 |
| Watch Tape 8 (The Opening) | +5 |
| Dangerous Topic (cellar/entity/wall) | +5 |
| Wrong Accusation | +15 |

### ExaminationSystem.java
Routes `(room, object, examCount)` → `ExamResult`. Handles 25+ examine hotspots across 10 rooms. Progressive discovery (1st vs 2nd+ exams). Entity anomaly discoveries on 2nd exams. Evidence/tape awards.

### ExamResult.java
Data holder: `String text`, `Evidence evidence`, `Tape tape`, `MiniGameType miniGameType` (NONE or TORN_LETTER_RECONSTRUCTION), `EntityAnomaly anomaly`.

### EvidenceSystem.java
Evidence collection; accusation validation:

**Against James (need 3 of 5):** FINANCIAL_RECORDS, WILL_COPY, TORN_LETTER, TAPE_ARGUMENT, TAPE_JAMES_INTERVIEW

**Against Daniel (need 2 of 5):** GROUNDSKEEPER_LOG, MUDDY_BOOTS, BLACKMAIL_NOTE, TAPE_DANIEL_INTERVIEW, TAPE_MARGARET_INTERVIEW

### InterviewSystem.java
- 5 topic lists per suspect (25 total topics)
- 4-tier cooperation: <10 channeling fades, <25 lies, <40 reluctant, <60 hesitant
- Evidence reactions: 17+ unique reactions
- Contradiction discovery on confrontation
- Dangerous topics: +5 awareness extra cost
- Memory fade at low cooperation replaces refusals

### NarratorSystem.java
- 4 moods: HOPEFUL / CONFUSED / ANXIOUS / FRANTIC (tied to awareness)
- `filterText()` prepends mood commentary
- Atmospheric cue generation
- 25% chance of random atmospheric events on navigation when awareness ≥ 40
- Distortions: mild (20% chance, awareness 40–59), severe (30% chance, awareness 60+)
- Channeling intro/bleed-through/end methods for interview wrapper
- Bleed-through: 20% chance per interview interaction

### HintSystem.java
3-tier progressive hints (cycling):
- Tier 1: Vague
- Tier 2: Moderate
- Tier 3: Explicit (shows exact locations and missing evidence)

Categories: start hints, tape location hints, watch-tape hints, evidence gap hints, accusation readiness hints. Special one-time hint per suspect per interview.

Sealed-room hints: marks items behind locked doors with `[SEALED]` tags. Shows cellar gate progress (tapes/evidence needed).

### AchievementSystem.java
Checks `checkOnWin()` on correct accusation. Checks: SPEEDRUN, GHOST, COMPLETIONIST, PERFECT_INVESTIGATION, and the moral ending achievements (GUARDIAN, ARSONIST, SURVIVOR, CYCLE_BREAKER).

### SaveLoadSystem.java
JSON serialization via LibGDX Json. 3 save slots using `FileHandle`. `SaveData` inner class holds all GameState fields. Missing booleans default to `false` on old saves.

**Serialized fields:** location, awareness, flags (gameOver, gameWon, accusationMade, climaxTriggered, narratorHeaderShown), evidence, tapes, watchedTapes, contradictions, cooperation (per suspect), visitCounts, examCounts, askedTopics, counters (command, interview, evidenceShown), anomalies, receivedLies, narratorDistortions, wrongAccusationCount, hasTapeRepairKit, chosenEnding.

---

## 11. Data Files

### NarratorText.java
- 4 mood arrays for warnings (4 variations per threshold, non-repeating)
- Environmental cues per awareness range
- Commentary text by mood
- 6 mild distortions (awareness 40–59, 20% chance)
- 5 severe distortions (awareness 60+, 30% chance)
- Distortion-to-Contradiction mapping (auto-discovers narrator contradictions)
- Channeling dialogue arrays: CHANNELING_FIRST_INTRO, CHANNELING_RETURN_INTRO, CHANNELING_BLEED_THROUGH, CHANNELING_MEMORY_FADE, CHANNELING_END (all mood-indexed)

### SuspectDialogue.java
- 5 suspect greetings (past tense narration, present-tense dialogue)
- 25 topic responses (5 per suspect)
- 17+ evidence reactions (keyed as "SUSPECT_EVIDENCE")
- 25 false responses (5 per suspect, delivered at cooperation <25)
- 4 Margaret deflections (pointed at by other suspects at cooperation 25–59)
- 5 accusation defenses (one per wrong suspect accusation)

### RoomDescriptions.java
Dynamic descriptions changing by visit count and awareness level. Examinable object display name maps. List of examinable objects per room.

**Examinable objects per room:**
- Study: desk, papers, under_desk, bookshelves, window, fireplace, ashes, poker (8)
- Parlor: fireplace, grandfather_clock, briefcase (3)
- Kitchen: storage_cellar, flour_tin (2)
- Guest Rooms: (navigation hub only, no examine objects)
- James's Room: coat, wardrobe (2)
- Margaret's Room: letter, dresser (2)
- Groundskeeper's Shed: logbook, shelf (2)
- Servants' Quarters: bedpost, floorboard (2)
- Cellar: wine_rack, flour_sacks (2)
- Entrance: (navigation hub only)

### TapeContent.java
All 8 tape transcripts (317 lines total):
- **Tape 1** — Harold's recording; Harold confronts James about embezzlement; will being changed
- **Tape 2** — James's police interview; weak alibi, deflects to Margaret
- **Tape 3** — Daniel's police interview; CRITICAL SLIP: "I was helping move—" before catching himself
- **Tape 4** — Margaret's police interview; heard footsteps at midnight, dragging at 2 AM
- **Tape 5** — Marcus's police interview; left at 11 PM, Charles's window lit
- **Tape 6** — Charles's police interview; saw James heading to study at 10:45 PM
- **Tape 7** — Margaret's personal account ("For the detective"); emotional witness statement
- **Tape 8 (The Opening)** — Arthur's final recording; discovers wall, hears Thomas, opens wall, dies

### ClimaxContent.java
- `TAPE_8_AWARENESS_COST = 5`
- Climax narrative text (Arthur's recording ends, something stirs)
- Variant climax intro by mood
- 3 moral ending texts: Seal the Wall, Destroy the Tapes, Escape the Manor

---

## 12. UI Components

### TextPanel.java
Overlay panel: dark background, muted borders, close button. Word-wrap, mouse-wheel scroll. Action buttons (e.g. topic buttons, evidence buttons in interview mode). typewriter text animation (slow reveal).

### TextButton.java
Clickable button with hover/disabled states.

### AwarenessMeter.java
4px thin strip across full screen top edge. Color gradient: green → yellow → orange → red (proportional fill). Hover near top shows tooltip: "15/80 DORMANT".

### ActionBar.java
Compact centered 5-button cluster (~540px wide). Bar height 36px, button height 28px, button width 100px. 60% opacity background. Buttons: SUSPECTS (T), INVENTORY (I), NOTEBOOK (N), HINT, ACCUSE.

### DocumentPiece.java
Draggable paper fragment for document reconstruction mini-game. Tracks position, snap detection (within 30px of target), jagged edge offsets.

### DocumentReconstructionGame.java
Full mini-game overlay: 6 scattered paper fragments, drag-and-drop, desk target area, snap-to-position. Completion detection → awards TORN_LETTER evidence. CANCEL button exits without evidence. Programmatic textures (paper textures, burn marks, jagged edges).

---

## 13. Screens

### DSAGame.java (Main Game Class)
- `SCREEN_WIDTH = 1280`, `SCREEN_HEIGHT = 720` (static final)
- Shared `OrthographicCamera`, `FitViewport` (scales/letterboxes to any monitor)
- `SpriteBatch`
- Starts on `TitleScreen`

### TitleScreen.java
- Cover art display (coverscreen.png)
- Clickable regions: New Game, Continue, Settings, Quit
- `viewport.apply()` + `viewport.unproject()` for correct input mapping
- Options: text speed, narrator toggle

### GameScreen.java
Full game orchestrator (~1500+ lines). Manages:
- All game systems (roomManager, gameState, awarenessSystem, examSystem, interviewSystem, narratorSystem, hintSystem, evidenceSystem, achievementSystem, saveLoadSystem)
- All UI (textPanel, actionBar, awarenessMeter, documentGame)
- Input dispatch (click hotspots, keyboard shortcuts, text panel actions, mini-game drag)
- Room rendering (background texture via PlaceholderGenerator or real art)
- Room name fade (3s), room description fade (5s)
- Tooltip: dynamic width, dark teal-black bg, muted gold border, cream text, clamped to screen bounds
- Cursor: hand on hotspot hover, arrow otherwise
- `PanelMode` enum: NONE, TEXT, INTERVIEW, INVENTORY, NOTEBOOK, OBJECTIVES, SAVE, LOAD
- Navigation gates: `isCellarUnlocked()`, `isMargaretRoomUnlocked()`
- Tape gate: `playTape()` blocks Tape 8 until `gameState.hasTapeRepairKit()`
- Channeling frame: intro at interview start, end text on close, 20% bleed-through per interaction
- `pendingClimax` flow for Tape 8 climax sequence
- Document mini-game rendering layer + touchDragged/touchUp handlers
- Save (F5), Load (F9), Inventory (I), Notebook (N), Suspects (T), History (H), Objectives (O), ESC

---

## 14. Art Assets

**Current state:** Procedural placeholders exist for all rooms. Real artwork being added progressively.

**Room art (1280×720 PNG, named by room ID):**

| Room | File | Status |
|------|------|--------|
| Entrance Hall | `entrance.png` | ✅ Real art exists |
| The Study | `study.png` | ☐ Placeholder |
| The Parlor | `parlor.png` | ☐ Placeholder |
| The Kitchen | `kitchen.png` | ✅ Real art exists |
| Guest Rooms Hallway | `guest_rooms.png` | ✅ Real art exists |
| James's Room | `james_room.png` | ☐ Placeholder |
| Margaret's Room | `margaret_room.png` | ☐ Placeholder |
| Groundskeeper's Shed | `groundskeeper_shed.png` | ☐ Placeholder |
| Servants' Quarters | `servants_quarters.png` | ☐ Placeholder |
| The Cellar | `cellar.png` | ☐ Placeholder |
| Cover Screen | `coverscreen.png` | ✅ Real art exists |

**Character art (art/characters/):**
- Assistant.jpg, Darkman.jpg, Groundskeeper.jpg, Sister.jpg

**Not yet created:**
- Character sprites/portraits for narrator + 5 suspects
- Evidence item visuals (10 cards/icons)
- Tape item visuals (8 icons)
- Environmental effect visuals (shadows, darkness overlays)

**Room Art Guide:** `art/rooms/ROOM_ART_GUIDE.md`
- Era: 1920s English countryside manor
- Palette: Dark, muted browns/deep reds/greens/grays
- Lighting: Dim, one insufficient light source per room
- No UI indicators needed in art (magnifying glass overlay added at runtime)

---

## 15. Three-Act Structure & Navigation Gates

### Act 1 — "The Investigation" (start of game)
**Available rooms:** Entrance, Study, Parlor, Kitchen, Guest Rooms, James's Room, Servants' Quarters, Groundskeeper's Shed
**Available:** 8/10 evidence items, 6/8 tapes, all 5 suspects

### Act 2 — "The Cellar"
**Gate:** `gameState.getWatchedTapes().size() >= 4 && gameState.getCollectedEvidence().size() >= 5`
**Unlocks:** Cellar access
**Cellar contains:** BLOODSTAINED_CUFF (flour_sacks), TAPE_ARTHUR_DEATH (wine_rack, damaged), BREATHING_WALL anomaly (wine_rack 2nd exam)

### Act 3 — "The Truth"
**Gate:** `gameState.hasAnomaly(EntityAnomaly.BREATHING_WALL)`
**Unlocks:** Margaret's Room
**Margaret's Room contains:** BLACKMAIL_NOTE (letter), TAPE_MARGARET_ACCOUNT (dresser), Tape Repair Kit (dresser)

### Tape 8 Gate
After acquiring Tape Repair Kit, player can watch Tape 8 → triggers climax sequence → moral endgame choices appear.

### Sealed Door Messages (mood-varied)
```
HOPEFUL:  "The door won't budge. Something is holding it shut from the other side."
CONFUSED: "Something's wrong with that door. It won't open. Strange..."
ANXIOUS:  "Don't go down there. Please. I have a bad feeling."
FRANTIC:  "DON'T TOUCH THAT DOOR. Can't you feel it?"
```

---

## 16. Narrator Channeling Mechanic

Suspects are long dead — the narrator channels their memories when the player "interviews" them.

**Rules:**
- Narrator NEVER says "I interviewed them" or "I was there"
- Only expresses confusion about the inexplicable ability
- Identity as past dead investigator is NEVER revealed until the climax

**Channeling flow:**
1. Interview starts → channeling intro prepended (first time: first-intro; subsequent: return-intro)
2. Topic/evidence/contradiction responses → 20% chance of bleed-through appended
3. At cooperation <25: lies returned instead of truth (memory distortion)
4. At cooperation <10: memory fade text replaces refusal
5. Interview ends → channeling end text shown (mood-varied)

**All suspect narration in past tense** (memories); dialogue (quoted speech) stays present tense.

**Bleed-through examples:**
- *"How do I know what they said? How can I possibly know this?"*
- *"This feels familiar. Like I've heard these words before. But that's impossible."*
- *"For a moment I could see the room. Smell the whisky. How?"*

---

## 17. Mini-Games

### Document Reconstruction (TORN_LETTER_RECONSTRUCTION)
- Triggered on first examination of study fireplace ashes
- 6 paper fragments scattered across screen
- Drag-and-drop to desk target area
- Snap-to-position within 30px of correct target
- Completion → awards TORN_LETTER evidence
- ESC or CANCEL button exits without evidence

### Future Mini-Games (Not Yet Implemented)
- Tape repair (physical mechanism)
- Lock picking
- Safe cracking

---

## 18. Achievements

| Achievement | Unlock Condition | Note |
|-------------|-----------------|------|
| SPEEDRUN | ≤10 commands to complete case | Checked on win |
| GHOST | <25 awareness on win | Checked on win |
| COMPLETIONIST | All 10 evidence + all 8 tapes watched | Checked on win |
| PERFECT_INVESTIGATION | All evidence, all tapes, <30 awareness | Checked on win |
| GUARDIAN | Choose "Seal the Wall" ending | Moral endgame |
| ARSONIST | Choose "Destroy the Tapes" ending | Moral endgame |
| SURVIVOR | Choose "Escape the Manor" ending | Moral endgame |
| CYCLE_BREAKER | All 7 anomalies + any ending | Requires full exploration |

Achievements shown in notebook with `[X]/[ ]` status. Unlocks notified in win screen text.

---

## 19. Save / Load System

- F5 = save menu (3 named slots)
- F9 = load menu (shows occupied/empty slots)
- Delete option in load menu
- LibGDX Json serialization with try/catch on load
- Old saves missing new fields default to false/0 safely
- Save/load bypasses navigation gates (position restored directly)

---

## 20. Feature Completion Status (TODO)

**Overall: ~90–95% complete.** All gameplay systems are done. Remaining: art assets, audio, polish.

### Complete Systems
- [x] Core Game State (awareness, flags, evidence, tapes, contradictions, cooperation, visit/exam counts)
- [x] 10 rooms with connections and 25+ examine hotspots
- [x] 10 evidence items with collection mechanics
- [x] 8 tapes with hidden locations, playback, transcripts
- [x] 5 suspects with cooperation-based dialogue, lies, deflections
- [x] Awareness/Threat system (4 thresholds, atmospheric events)
- [x] Mouse click hotspot navigation + WASD keyboard input
- [x] Narrator system (4 moods, distortions, environmental cues)
- [x] Channeling mechanic (past tense, bleed-through, memory fade)
- [x] Hint system (3-tier progressive)
- [x] Save/Load (3 slots, full state serialization)
- [x] UI (TextPanel, TextButton, AwarenessMeter, ActionBar, typewriter animation)
- [x] Title screen, pause menu, game over, inventory, notebook, objectives, save/load screens
- [x] Achievement system (8 achievements)
- [x] Contradiction/deduction system (6 contradictions)
- [x] Climax and 3 moral endings (Seal Wall, Destroy Tapes, Escape)
- [x] Three-act navigation gates
- [x] Tape damage + repair mechanic
- [x] Document Reconstruction mini-game
- [x] FitViewport fullscreen scaling
- [x] Art-first minimal UI (invisible hotspots, fade-out room name/description, thin awareness strip)
- [x] Entity Anomaly Discovery (7 anomalies)
- [x] Active Suspect Lies (Feature 1)
- [x] Unreliable Narrator Distortions (Feature 2)
- [x] Red Herring Margaret Arc (Feature 3)
- [x] Wrong Accusation Consequences (Feature 4)
- [x] Emergent Entity Discovery (Feature 5)
- [x] Moral Endgame Choice (Feature 6)
- [x] Narrator vs Evidence Contradictions (Feature 7)

### Incomplete
- [~] Context-sensitive interactions (partial)
- [~] Contradictions required for accusation (discoverable but not required; evidence count is gating)
- [~] 6/10 room art (placeholders for study, parlor, james's room, margaret's room, shed, servants' quarters, cellar)
- [ ] Character portraits (narrator + 5 suspects)
- [ ] Evidence item visuals (10 cards/icons)
- [ ] Tape item visuals (8 icons)
- [ ] Environmental effect visuals
- [ ] Background music (per-location ambient tracks)
- [ ] Sound effects
- [ ] Audio engine integration + dynamic audio
- [ ] Room transition effects
- [ ] Animation system
- [ ] Particle effects (dust, fog)
- [ ] Error handling / exception recovery
- [ ] Logging system
- [ ] Asset caching optimization
- [ ] Intro sequence establishing player as accidental stranger (not hired PI) — **see Open Design Items**
- [ ] Future mini-games (tape repair, lock picking, safe cracking)

---

## 21. Changelogs & Design History

### UI Overhaul — Art-First Minimal Interface
- Removed all hotspot overlay rendering (arrow/door/magnifying glass textures)
- Hotspot click detection unchanged (invisible bounds still work)
- Room name: top-left, text shadow, fades after 3s, no background panel
- Room description: text shadow, above action bar, fades after 5s
- Tooltip: dynamic width, dark teal-black bg, muted gold border, cream text
- Cursor: hand on hotspot, arrow otherwise
- ActionBar: 36px height, 100px button width, ~540px centered, 60% opacity
- AwarenessMeter: 4px top strip, color gradient, hover tooltip

### Document Reconstruction Mini-Game Added
- `DocumentPiece.java` + `DocumentReconstructionGame.java` added
- `ExamResult.java` gained `MiniGameType` enum
- `ExaminationSystem.java` changed ashes examination to trigger mini-game
- `GameScreen.java` gained mini-game rendering layer + drag handlers

### Navigation Simplification — Removed Servants' Staircase
- Servants' Quarters: staircase → bedpost (A.H. anomaly discovery relocated here)

### Evidence Relocation — Bloodstained Shirt to Cellar
- Bloodstained cuff moved from Servants' Quarters to Cellar (behind flour sacks)
- Narrative: fire was nearly dead, couldn't burn shirt, hid it in cellar instead

### Story Consistency Overhaul — 29 Inconsistencies Fixed
- Murder weapon: fireplace poker (not letter opener)
- Murder time: 10:45 PM
- Thomas's surname: Ashford (business partner, not Harold's brother)
- Charles: 28-year-old company assistant
- Blackmail note: James planted it to frame Margaret
- Margaret's will knowledge: learned day after death from Charles
- Body movement: two-person job (James + Daniel)
- Window entry: Daniel's entry point

### Additional Story Refinements
- Margaret's textile import company (financially independent)
- Groundskeeper Shed: connected to Servants' Quarters (not Kitchen)
- Daniel removed as witness for Marcus's alibi (accomplice conflict)
- Marcus as legitimate suspect (process of elimination, not automatic innocence)
- Charles no longer knew about embezzlement (Harold discovered it himself)
- Margaret's Tape: witness account, not confession (Margaret is fully innocent)
- Arthur timeline: is FIRST investigator; no prior investigators before him
- Embezzlement discovery: Harold found it himself, not Charles

### Tape System Restructure — 7 → 8 Tapes
See full details in UPDATE_PLAN.md. Summary:
- Added Tape 4 (Margaret's Police Interview) in Kitchen storage_cellar
- Changed Tape 8 climax trigger from TAPE_CELLAR_NOISES → TAPE_ARTHUR_DEATH
- All tape transcripts rewritten to mixed format (Harold + police interviews + personal + death recording)
- 17 files updated and verified in 3-pass scan

### Three-Act Structure + Channeling Mechanic + Tape Repair
- Navigation gates (cellar: 4 tapes + 5 evidence; Margaret's Room: BREATHING_WALL)
- Channeling mechanic with mood-varied intro/bleed-through/end text
- Past tense conversion of all suspect narration (~90 verb changes)
- Tape 8 damage/repair mechanic
- hasTapeRepairKit persisted in SaveLoadSystem

### FitViewport Fullscreen Scaling Fix
- SCREEN_WIDTH/HEIGHT now static final 1280/720 (not dynamic monitor resolution)
- FitViewport in DSAGame; viewport.apply() + viewport.unproject() in screens
- Eliminates coordinate misalignment on non-1280×720 monitors

---

## 22. Open Design Items & Corrections

### Player Character Identity (February 17 Correction)
**WRONG (error in prior analysis, not in code):** Player described as "a private investigator hired by Margaret Vance."

**CORRECT (designer confirmed):**
- Player is an unnamed stranger who stumbled upon Vance Manor accidentally
- Supernatural pull keeps them investigating
- Murder happened decades ago — cold case
- Nobody hired them
- They discover Arthur's old recordings

**What the code already gets right (no changes needed):**
- Narrator header: *"Ah, you've found Arthur's recordings. Good. I've been waiting for someone who would listen."*
- Channeling mechanic framing (suspect memories, not live interviews)
- Past tense throughout room descriptions and examination system
- Margaret's dialogue: "That's why I contacted a private investigator -- a man named Hollis" (refers to Arthur, not player)

**TODO: Write intro/opening sequence establishing:**
1. Who the player is (unnamed stranger, not a PI)
2. Why they're at Vance Manor (accident / compulsion)
3. Time period (present day, decades after 1987)
4. First discovery that hooks them (finding recorder in entrance, or sound from cellar)

### Blackmail Note Evidence Classification
- Currently listed as evidence against Daniel (accusation requirement)
- But description says "James planted this" → logically evidence against James
- **Existing implementation:** Blackmail Note is in Daniel's accusation evidence list (need 2 of 5). This works because it reveals Daniel's handwriting was forged by James, implicating both.

### Tape Order Note
Recommended narrative order (best story experience): Tape 1 → Tape 6 → Tape 5 → Tape 4 → Tape 2 → Tape 3 → Tape 7 → Tape 8. Game does NOT enforce this order; awareness budget and room accessibility provide soft guidance.

### Marcus/Parlor Tape Swap (from Arthur.MD)
Arthur.MD proposed Tape 5 in grandfather_clock and Tape 6 in briefcase (opposite of final implementation). **Final code:** TAPE_MARCUS_INTERVIEW in briefcase, TAPE_CHARLES_INTERVIEW in grandfather_clock. This is intentional — Charles's testimony about the clock's role in the timeline fits the clock location.

---

## 23. File Index

### Java Source Files
| File | Package | Purpose |
|------|---------|---------|
| DSAGame.java | root | Main game class, camera, viewport |
| GameScreen.java | screens | Full game orchestrator |
| TitleScreen.java | screens | Title/menu screen |
| GameState.java | state | All mutable game state |
| Evidence.java | state | 10 evidence items enum |
| Tape.java | state | 8 tapes enum |
| Suspect.java | state | 5 suspects enum |
| Contradiction.java | state | 6 contradiction types enum |
| Achievement.java | state | 8 achievement definitions enum |
| EntityAnomaly.java | state | 7 anomaly types enum |
| Direction.java | navigation | NSEW/UP/DOWN/ENTER enum |
| Hotspot.java | navigation | Clickable area data |
| HotspotPositions.java | navigation | Standard positions + factory |
| Room.java | navigation | Room data class |
| RoomManager.java | navigation | All rooms, connections, hotspots |
| PlaceholderGenerator.java | rendering | Procedural texture generation |
| AwarenessSystem.java | systems | Threat level logic |
| ExamResult.java | systems | Examination result data |
| ExaminationSystem.java | systems | Routes examine calls → results |
| EvidenceSystem.java | systems | Collection + accusation validation |
| InterviewSystem.java | systems | Suspect interview logic |
| NarratorSystem.java | systems | Mood-shifting narrator |
| HintSystem.java | systems | 3-tier hint generation |
| AchievementSystem.java | systems | Achievement unlock checks |
| SaveLoadSystem.java | systems | JSON save/load (3 slots) |
| ClimaxContent.java | data | Tape 8 climax + endings text |
| NarratorText.java | data | All narrator text + channeling |
| RoomDescriptions.java | data | Dynamic room descriptions |
| SuspectDialogue.java | data | All suspect dialogue |
| TapeContent.java | data | All 8 tape transcripts |
| ActionBar.java | ui | 5-button centered action bar |
| AwarenessMeter.java | ui | 4px top-edge awareness strip |
| DocumentPiece.java | ui | Draggable paper fragment |
| DocumentReconstructionGame.java | ui | Torn-letter mini-game |
| TextButton.java | ui | Clickable button |
| TextPanel.java | ui | Overlay text panel |
| Lwjgl3Launcher.java | lwjgl3 | Desktop entry point |
| StartupHelper.java | lwjgl3 | macOS helper |

### Markdown / Documentation Files
| File | Purpose |
|------|---------|
| README.md | Project overview, features, how to play, build/run |
| GAME_DOCUMENTATION.md | Complete game world, characters, lore, mechanics |
| NAVIGATIONGUIDE.MD | LibGDX navigation implementation guide |
| TODO.md | Full completion checklist + changelog |
| UPDATE_PLAN.md | 8-tape structure migration plan (COMPLETE) |
| Arthur.MD | Arthur Hollis's investigation lore + all 8 tape transcripts |
| February16.MD | Three-act structure + channeling mechanic implementation plan |
| art/rooms/ROOM_ART_GUIDE.md | Art direction guide for all 10 room backgrounds |

### Build / Config Files
| File | Purpose |
|------|---------|
| build.gradle (root) | Root build config (plugins, asset list task, Java 8) |
| core/build.gradle | Core module dependencies |
| lwjgl3/build.gradle | LWJGL3 launcher dependencies + jar task |
| settings.gradle | Subproject list (`lwjgl3`, `core`) |
| gradle.properties | `projectVersion` |
| gradle/gradle-daemon-jvm.properties | JVM settings for Gradle daemon |
| gradle/wrapper/gradle-wrapper.properties | Gradle 9.2.1 wrapper |
| .editorconfig | Code style (indent, charset) |
| .gitignore | Git ignore rules |
| .gitattributes | Git line endings |

### Asset Files
| File | Purpose |
|------|---------|
| assets/assets.txt | Auto-generated asset list |
| assets/rooms/entrance.png | Entrance Hall artwork |
| assets/rooms/kitchen.png | Kitchen artwork |
| assets/rooms/guest_rooms.png | Guest Rooms Hallway artwork |
| assets/rooms/coverscreen.png | Title screen cover art |
| art/rooms/entrance.png | Source entrance artwork |
| art/rooms/coverscreen.png | Source cover art |
| art/characters/*.jpg | Character reference art |

---

*Generated: 2026-02-17 — Full parallel scan of all project files.*
