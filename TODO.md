# DSA 2D - Completion Checklist

Comprehensive list of all features and systems needed to complete the 2D port of DSA Try.
Current estimated completion: **~85-90%** (all gameplay systems complete; remaining work is art assets, audio, and polish).

---

## Legend

- `[ ]` Not started
- `[~]` Partially implemented / stubbed
- `[x]` Complete

---

## 1. Core Game State Management
*Priority: CRITICAL -- everything depends on this*

- [x] **Location tracking** -- Track current location among 7 areas with per-location state
- [x] **Awareness system** -- 0-80 awareness points with 4 escalating threat levels:
  - Dormant (0-19)
  - Suspicious (20-39), warning at 20
  - Alert (40-59), warning at 40
  - Dangerous (60-69), warning at 60
  - Critical (70-79), warning at 70
  - Cap at 80 triggers game over
- [x] **Game flow flags** -- Game Over, Game Won, Accusation Made
- [x] **Evidence tracking** -- Collected evidence set, collected tapes set, watched tapes set
- [x] **Contradiction logging** -- Found contradictions set and asked topics set
- [x] **Suspect cooperation tracking** -- Per-suspect cooperation values (0-100)
- [x] **Location visit tracking** -- Visit count per location
- [x] **Examination count tracking** -- Per-object examination count
- [x] **Interview state** -- Current suspect being interviewed, cooperation-based refusal
- [x] **Hint tracking** -- Progressive 3-tier hints with per-suspect interview hints (HintSystem.java)
- [x] **Player pattern tracking** -- commandCount, interviewCount, evidenceShownCount tracked in GameState

---

## 2. Location and Object System
*Priority: CRITICAL*

- [x] **7 playable locations** with connections (Entrance Hall, Study, Parlor, Kitchen, Guest Rooms, Groundskeeper's Shed, Servants' Quarters, Cellar)
- [x] **Location transitions** -- Movement between connected areas
- [x] **Awareness cost on movement** -- +1 awareness per move
- [x] **Dynamic location descriptions** -- Descriptions change based on visit count and awareness level (RoomDescriptions.java)
- [x] **Examinable objects per room** -- 25 examine hotspots across 7 rooms (desk, drawers, papers, bookshelves, window, fireplace, ashes, under_desk, grandfather_clock, briefcase, storage_cellar, flour_tin, margarets_room, james_room, letter, coat, logbook, shelf, staircase, floorboard, wine_rack, etc.)
- [x] **Repeated examination discovery** -- Different text on 1st vs 2nd+ examinations; some objects yield evidence on 2nd exam (e.g., shed shelf)
- [x] **Examination awareness cost** -- +1 awareness per examination

---

## 3. Evidence System
*Priority: CRITICAL*

- [x] **9 collectible evidence items:**
  - [x] Letter Opener -- "A silver letter opener found on Harold's desk. Could be the murder weapon."
  - [x] Torn Letter -- "A partially destroyed letter in the fireplace. Mentions 'the will' and 'betrayal'."
  - [x] Financial Records -- "Hidden in a desk drawer. Shows large transfers to an unknown account."
  - [x] Muddy Boots -- "Boots with fresh mud, found near the kitchen. Someone went outside recently."
  - [x] Sleeping Powder -- "A vial of sleeping powder hidden in the flour tin."
  - [x] Bloodstained Cuff -- "A shirt cuff with blood spots, found under a loose floorboard."
  - [x] Will Copy -- "A copy of Harold's will. James inherits everything, Margaret gets nothing."
  - [x] Blackmail Note -- "A note demanding silence. 'I know what you did. Pay or I talk.'"
  - [x] Groundskeeper Log -- "Daniel's logbook. An entry is missing for the night of the murder."
- [x] **Evidence collection mechanics** -- Found through examination of locations/objects, some require multiple examinations
- [x] **Evidence validation** -- Track what evidence has been collected, James/Daniel evidence counting
- [x] **Accusation evidence requirements:**
  - Against James: Need 3 of 5 pieces (Financial Records, Will Copy, Torn Letter, Tape: Argument, Tape: Will Reading)
  - Against Daniel: Need 2 of 5 pieces (Groundskeeper Log, Muddy Boots, Blackmail Note, Tape: Daniel Meeting, Tape: Cellar)
- [x] **Completionist tracking** -- Tracked via AchievementSystem (COMPLETIONIST achievement)

---

## 4. Tape System
*Priority: CRITICAL*

- [x] **7 hidden videotapes:**
  - [x] Tape 1: Harold & James Argument (hidden under Study Desk)
  - [x] Tape 2: Late Night Phone Call (hidden in Parlor Grandfather Clock)
  - [x] Tape 3: Margaret's Confession (hidden in Guest Rooms / Margaret's Room)
  - [x] Tape 4: Daniel's Secret Meeting (hidden in Groundskeeper Shed Logbook)
  - [x] Tape 5: Will Reading Preview (hidden in Study Bookshelves)
  - [x] Tape 6: Kitchen Whispers (hidden in Kitchen Storage Cellar)
  - [x] Tape 7: Cellar Recording (hidden in Cellar Wine Rack)
- [x] **Tape collection** -- Find tapes hidden in specific examination spots
- [x] **Tape playback** -- Watch tapes from inventory, costs +4 awareness each; full transcript display
- [x] **Tape 6 climax trigger** -- Arthur's final recording, opening the wall, encountering the Entity (ClimaxContent.java + pendingClimax flow)
- [x] **Tape 7 special handling** -- Recorded by Victor (not Arthur), discussing it costs +5 awareness

---

## 5. Suspect System
*Priority: CRITICAL*

- [x] **5 suspects with starting cooperation levels:**
  - [x] James Vance (70 starting cooperation)
  - [x] Margaret Vance (60 starting cooperation)
  - [x] Daniel the Groundskeeper (50 starting cooperation)
  - [x] Eleanor the Housekeeper (80 starting cooperation)
  - [x] Reginald the Butler (75 starting cooperation)
- [x] **Interview system** -- Talk to suspects via SUSPECTS button, ask about topics (5 topics per suspect, 25 total topic responses)
- [x] **Cooperation degradation** -- -3 per question asked, -5 when showing incriminating evidence; refuses to talk at <20
- [x] **Evidence showing to suspects** -- Show evidence during interviews (+2 awareness), 17 unique evidence reactions
- [x] **Warning level tracking** -- 4-tier cooperation system: <10 refuses, <25 snaps, <40 reluctant, <60 hesitant, else normal
- [x] **Interview hint system** -- One-time hint per suspect via HintSystem.getInterviewHint()
- [x] **Individual interview handlers** -- Unique greetings, topic responses, evidence reactions for all 5 suspects
- [x] **Interview mode restricted commands** -- Panel overlay blocks room interaction during interviews
- [x] **Interview exit commands** -- "End Interview" button to leave interview

---

## 6. Awareness / Threat System
*Priority: CRITICAL*

- [x] **Awareness meter UI** -- Top-right color-shifting bar (green->yellow->orange->red) with "15/80 DORMANT" label
- [x] **Awareness cost mechanics:**
  - Movement: +1
  - Examine: +1
  - Question/Ask: +1
  - Show Evidence: +2
  - Contradiction challenge: +2
  - Watch Tape: +4
  - Wrong Accusation: +15
  - [x] Dangerous Topic (Victor + Tape 7): +5
  - [x] Confrontation: +3
- [x] **Awareness level effects** -- Room descriptions change at awareness thresholds; narrator environmental cues implemented
- [x] **Random atmospheric events** -- 25% chance on navigation when awareness >= 40 (NarratorSystem)
- [x] **Narrator personality shifts** -- 4 moods: HOPEFUL/CONFUSED/ANXIOUS/FRANTIC (NarratorSystem + NarratorText)
- [x] **Warning messages** -- Threshold-crossing messages at 20, 40, 60, 70, 80
- [x] **Urgency indicator** -- Color-shifting awareness meter
- [x] **Forced escape trigger** -- When awareness reaches 80 (game over with summary)

---

## 7. Command / Interaction System
*Priority: CRITICAL -- needs 2D input adaptation*

- [x] **Mouse click hotspot navigation**
- [x] **WASD/arrow keyboard input**
- [x] **Adapted interaction commands for 2D** (replacing text parser with click/UI-based equivalents):
  - [x] Look (dynamic room descriptions displayed automatically)
  - [x] Go to (click navigation arrows/doors)
  - [x] Examine (click examine hotspot icons on objects)
  - [x] Ask about (topic selection buttons in interview panel)
  - [x] Talk to (SUSPECTS button -> suspect selection -> interview)
  - [x] Show evidence (evidence selection buttons during interview)
  - [x] Play tape (PLAY buttons in inventory for unwatched tapes)
  - [x] Notebook (NOTEBOOK button -> progress summary, contradictions, cooperation levels)
  - [x] Inventory (INVENTORY button -> evidence list, tape list with play option)
  - [x] Accuse (ACCUSE button -> suspect selection with evidence validation)
  - [x] Status (awareness meter always visible; full status in notebook)
  - [x] Hint (HINT button -> context-aware guidance based on progress)
  - [x] Objectives (objectives screen via O key)
  - [x] Save / Load (F5=save, F9=load, 3 save slots)
  - [x] History (event log via H key)
  - [x] Achievements (shown in notebook + unlocked on win)
- [x] **Keyboard shortcuts** -- I=Inventory, N=Notebook, T=Suspects, H=History, O=Objectives, F5=Save, F9=Load, ESC=close panel/pause menu
- [x] **Scroll support** -- Mouse wheel scrolling in text panel
- [~] **Context-sensitive interactions** -- Panel blocks room clicks; game-over/win blocks all input; full context sensitivity not yet done

---

## 8. Narrator System
*Priority: HIGH*

- [x] **Dynamic narrator personality** -- Shifts based on awareness level (NarratorSystem.java)
- [x] **Four emotional states:**
  - Low awareness: HOPEFUL -- Clear and helpful
  - Stirring: CONFUSED -- Confused, losing memory
  - Watching: ANXIOUS -- Anxious, hearing things
  - Hunting: FRANTIC -- Urgent, frantic
- [x] **Context-aware messaging** -- filterText() prepends mood commentary to game text
- [x] **Environmental cue generation** -- Atmospheric descriptions tied to awareness (NarratorText.java)
- [x] **Suspect dialogue delivery** -- All suspect dialogue channeled through narrator
- [x] **Header management** -- Show narrator header once per session (persisted through save/load)
- [x] **Randomized warning variations** -- 4 variations per awareness level, non-repeating

---

## 9. Hint System
*Priority: HIGH*

- [x] **Progressive hint levels** (3 levels, cycling via HintSystem.java):
  - [x] Level 1: Vague hints (general direction)
  - [x] Level 2: Moderate hints (more specific direction)
  - [x] Level 3: Explicit hints (exact actions to take)
- [x] **Context-aware hint types:**
  - [x] Start hints (how to begin investigation)
  - [x] Tape location hints (with explicit locations at tier 3)
  - [x] Watch tape hints
  - [x] Evidence gap hints (shows missing evidence per suspect at tier 3)
  - [x] Accusation readiness hints
- [x] **Special interview hints** -- One-time hint per suspect per interview

---

## 10. Save / Load System
*Priority: HIGH*

- [x] **Full state serialization** covering (SaveLoadSystem.java):
  - Current location, awareness level
  - Game completion flags (game over, climax, accusation)
  - Collected evidence, tapes, watched tapes
  - Contradictions found
  - Suspect cooperation values (per-suspect)
  - Examined objects list
  - Counter tracking (command count, interview count, evidence shown count)
- [x] **Save with slot names** -- 3 save slots via F5 key
- [x] **Load saves by name** -- F9 key to select and load
- [x] **List available saves** -- Load menu shows occupied/empty slots
- [x] **Delete saves** -- Delete option in load menu
- [x] **Save format validation** -- LibGDX Json serialization with try/catch on load

---

## 11. Achievement System
*Priority: MEDIUM*

- [x] **4 achievements** (Achievement.java + AchievementSystem.java):
  - [x] Speedrun -- Complete in <=10 commands
  - [x] Ghost -- Finish with <25 awareness
  - [x] Completionist -- Collect all evidence and watch all tapes
  - [x] Perfect Investigation -- All evidence, all tapes, <30 awareness
- [x] **Command counting** for achievement tracking (GameState.commandCount)
- [x] **Automatic achievement unlock checks** -- checkOnWin() on correct accusation
- [x] **Unlock notifications** -- Appended to win screen text
- [x] **Achievement status viewing** -- Shown in notebook with [X]/[ ] status

---

## 12. Contradiction / Deduction System
*Priority: HIGH*

- [x] **Weapon contradiction tracking** -- Present weapon contradiction during suspect interviews; suspect-specific reactions for James, Daniel
- [x] **Body position contradiction tracking** -- Present body position contradiction; suspect-specific reactions for James, Margaret, Daniel, Eleanor
- [~] **Contradictions required for valid accusation** -- Contradictions are discoverable but not currently required for accusation (evidence count is required instead)
- [x] **Notebook system** -- View collected evidence, discovered contradictions, suspect cooperation levels, and progress stats

---

## 13. Climax and Ending System
*Priority: HIGH*

- [x] **Climax trigger via Tape 6** -- Arthur's final recording, opening the wall, encountering the Entity (ClimaxContent.java + pendingClimax)
- [x] **Forced escape sequence** -- When awareness hits 80 (game over screen with stats)
- [x] **Accusation logic and validation** -- Check James evidence (3 needed) + Daniel evidence (2 needed)
- [x] **Multiple endings:**
  - [x] Success (correct accusation: James & Daniel together)
  - [x] Failure (awareness cap reached -- game over)
  - [x] Wrong accusation (+15 awareness penalty, with specific feedback per wrong choice)
  - [x] Alternate paths (Leave the Manor, variant win text by evidence/awareness)

---

## 14. Text Display / UI System
*Priority: HIGH -- replaces original text formatting*

- [x] **Room title panel**
- [x] **Room description panel** -- Now dynamic based on visit count and awareness
- [x] **Control hints display** -- Replaced by ActionBar with 5 buttons
- [x] **Tooltip system**
- [x] **Dialogue boxes** -- TextPanel overlay for narrator text, suspect interviews, evidence display, tape transcripts
- [x] **Typewriter text animation** -- Slow text reveal for narrative effect
- [x] **Styled UI panels** -- TextPanel with dark background, borders, close button; TextButton with hover states
- [x] **Table/grid layouts** -- Inventory, notebook, and suspect list layouts via TextPanel + TextButton
- [x] **List formatting** -- Evidence list, tape list, topic list, suspect list
- [x] **Section headers and separators** -- "=== SECTION ===" headers in panel text
- [x] **Awareness meter** -- Color-shifting bar with level name label
- [x] **Action bar** -- Bottom bar with INVENTORY, NOTEBOOK, SUSPECTS, HINT, ACCUSE buttons
- [x] **Examine icons** -- Magnifying glass icons on examinable objects with hover glow

---

## 15. Visual Assets (2D-Specific -- New)
*Priority: HIGH*

- [~] **Location graphics** -- Procedural placeholders exist, need real art for all 8 rooms
- [ ] **Character sprites/portraits** -- Narrator portrait + 5 suspect portraits
- [ ] **Evidence item visuals** -- Cards/icons for 9 evidence items
- [ ] **Tape item visuals** -- Icons for 7 videotapes
- [~] **UI element art** -- Procedural TextButton/TextPanel/ActionBar exist; need polished art
- [ ] **Environmental effect visuals** -- Shadows, darkness overlays, threat indicators
- [x] **Awareness meter graphics** -- Color-shifting bar with background and border

---

## 16. Menu and Screen Systems (2D-Specific -- New)
*Priority: MEDIUM*

- [x] **Title screen / main menu**
- [x] **Pause menu**
- [x] **Settings/options screen** (text speed, narrator toggle; accessible from pause menu and title screen)
- [x] **Game over screen** -- Displayed via TextPanel overlay with stats
- [x] **Inventory screen** -- Displayed via TextPanel with evidence list and tape play buttons
- [x] **Notebook screen** -- Displayed via TextPanel with progress stats, contradictions, cooperation
- [x] **Objectives screen** (phase-aware goals via O key)
- [x] **Achievement screen** -- Shown in notebook panel
- [x] **Save/load screen** -- F5=save menu, F9=load menu, 3 slots

---

## 17. Audio System (2D-Specific -- New)
*Priority: MEDIUM*

- [ ] **Background music** -- Per-location ambient tracks
- [ ] **Sound effects** -- Interactions, discoveries, awareness triggers
- [ ] **Audio engine integration** -- LibGDX audio system setup
- [ ] **Dynamic audio** -- Music/SFX intensity tied to awareness level

---

## 18. Polish and Transitions (2D-Specific -- New)
*Priority: LOW*

- [ ] **Room transition effects** -- Fade/animation between rooms
- [ ] **Animation system** -- For UI elements and environmental effects
- [ ] **Particle effects** -- Visual atmosphere (dust, fog, etc.)
- [ ] **Error handling / exception recovery**
- [ ] **Logging system**
- [ ] **Asset caching optimization**

---

## Implementation Priority Order

1. ~~**Core Game State Management** (System 1) -- Foundation for everything~~ **DONE**
2. ~~**Evidence System** (System 3) + **Tape System** (System 4) -- Core gameplay loop~~ **DONE**
3. ~~**Suspect System** (System 5) -- Investigation mechanics~~ **DONE**
4. ~~**Awareness / Threat System** (System 6) -- Tension and pacing~~ **DONE**
5. ~~**Contradiction / Deduction System** (System 12) -- Accusation prerequisites~~ **DONE**
6. ~~**Narrator System** (System 8) -- Story delivery~~ **DONE**
7. ~~**Climax and Ending System** (System 13) -- Game completion~~ **DONE**
8. ~~**Command / Interaction Adaptation** (System 7) -- 2D input mapping~~ **DONE**
9. ~~**Hint System** (System 9) -- Player guidance~~ **DONE**
10. ~~**Save / Load System** (System 10) -- Persistence~~ **DONE**
11. ~~**UI System** (System 14) -- Display and formatting~~ **DONE**
12. ~~**Menu Systems** (System 16) -- Game flow screens~~ **DONE**
13. ~~**Achievement System** (System 11) -- Bonus tracking~~ **DONE**
14. **Visual Assets** (System 15) -- Art replacement
15. **Audio System** (System 17) -- Sound design
16. **Polish and Transitions** (System 18) -- Final quality pass

---

## New Files Added (Systems 1-7 Implementation)

### `state/` package
- `Evidence.java` -- 9 evidence items enum
- `Tape.java` -- 7 tapes enum with room/object locations
- `Suspect.java` -- 5 suspects enum with cooperation levels
- `Contradiction.java` -- WEAPON, BODY_POSITION enum
- `GameState.java` -- All mutable game state, tracking counters, save/load support
- `Achievement.java` -- 4 achievement definitions enum

### `systems/` package
- `AwarenessSystem.java` -- Threshold warnings, level names, color index
- `ExamResult.java` -- Data holder for examination results
- `EvidenceSystem.java` -- Collection, James/Daniel counting, accusation validation
- `ExaminationSystem.java` -- Routes room+object+count to ExamResult
- `InterviewSystem.java` -- Topics, evidence reactions, contradictions, 4-tier cooperation, dangerous topics
- `NarratorSystem.java` -- Mood-shifting narrator with warnings, cues, commentary, atmospheric events
- `HintSystem.java` -- 3-tier progressive hints, 5 categories, per-suspect interview hints
- `SaveLoadSystem.java` -- JSON serialization with 3 save slots via LibGDX FileHandle
- `AchievementSystem.java` -- 4 achievements checked on win, shown in notebook

### `data/` package
- `TapeContent.java` -- 7 tape transcripts
- `SuspectDialogue.java` -- Greetings, 25 topic responses, 17 evidence reactions
- `RoomDescriptions.java` -- Dynamic descriptions, examinable object lists
- `NarratorText.java` -- 4 moods, warning/cue/commentary text, atmospheric events
- `ClimaxContent.java` -- Tape 6 climax narrative, Tape 7 Victor prefix

### `ui/` package
- `TextButton.java` -- Clickable button with hover/disabled states
- `TextPanel.java` -- Overlay panel with word-wrap, scroll, action buttons
- `AwarenessMeter.java` -- Color-shifting bar
- `ActionBar.java` -- Bottom action buttons

### Modified files
- `Hotspot.java` -- Added EXAMINE type, objectName field
- `PlaceholderGenerator.java` -- Added examine icon generator
- `RoomManager.java` -- Added 25 examine hotspots across 7 rooms
- `GameScreen.java` -- Full orchestrator rewrite: all systems, UI, input dispatch

---

## Changelog

### Fullscreen Scaling Fix -- FitViewport at 1280x720

**Problem:** Game coordinates were misaligned on fullscreen monitors. `DSAGame` set SCREEN_WIDTH/HEIGHT dynamically to the monitor resolution, but `HotspotPositions`, `RoomManager`, and all UI classes assumed 1280x720. Input handling used raw `SCREEN_HEIGHT - screenY` which broke when screen size != design size.

**Solution:** Use a LibGDX `FitViewport` with a fixed 1280x720 design resolution. The viewport scales and letterboxes the game to fill any screen size. All game code works in 1280x720 coordinates; LibGDX handles the mapping.

**Files modified:**

- `DSAGame.java` -- `SCREEN_WIDTH`/`SCREEN_HEIGHT` are now `static final` 1280/720. Added shared `OrthographicCamera` and `FitViewport`, initialized in `create()`. Removed dynamic `Gdx.graphics.getWidth()/getHeight()` assignment.
- `TitleScreen.java` -- Added viewport reference. `render()` calls `viewport.apply()` and sets projection matrix. `resize()` calls `viewport.update()`. Input handlers use `viewport.unproject()` instead of raw `SCREEN_HEIGHT - screenY`.
- `GameScreen.java` -- Same viewport/camera/input changes as TitleScreen. Tooltip positioning also unprojected through viewport.

**Files unchanged (no modifications needed):**

- `TextPanel.java`, `AwarenessMeter.java`, `ActionBar.java` -- `static final` fields now correctly evaluate to 1280/720 at compile time.
- `HotspotPositions.java` -- Already hardcoded to 1280x720.
- `RoomManager.java` -- Already used 720 for Y conversion.
- `Lwjgl3Launcher.java` -- Already set to fullscreen.
