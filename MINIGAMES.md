# MINIGAMES — THE UNRELIABLE NARRATOR

---

## Overview

Two minigames trigger after specific tape recordings are played. Both launch as separate screens and return the player to the main game after completion. They replace the interview/suspects system as the primary interactive mechanic.

The core concept: **the tape is the truth, the Narrator is the distortion.** The player uses the minigame to catch the Narrator being unreliable.

After each minigame:

- **Win** → The Narrator cracks. A suppressed truth surfaces — something the Narrator has been hiding about the 1987 murder.
- **Lose** → The Narrator's distortion holds. A misleading interpretation is accepted. Nothing new surfaces.

The question is **asked once only**. Replaying the tape does not re-trigger the minigame.

---

## MINIGAME 1: THE CATCHER

**Triggers after:** Tapes 1, 2, and 3
**Mechanic type:** Reflex / memory

### How It Works

After the tape text is dismissed, the screen transitions into a dark atmospheric space. Words and phrases fall from the top of the screen — drawn from both the tape's actual content (TRUE) and the Narrator's distorted version (DISTORTED). Both look identical. No color coding. The player moves a **green line** left and right at the bottom of the screen to catch words.

- Catch a **TRUE word** → progress meter fills
- Catch a **DISTORTED word** → screen distortion flash, Awareness +1, wrong catch counter increases
- **Win condition:** Catch 5 true words
- **Lose condition:** 3 wrong catches

Difficulty increases over time — words fall faster, spawn interval shortens.

### Controls

- `A` / `←` — move line left
- `D` / `→` — move line right

### Why It Requires Brain Power

Both true and distorted words look identical. The player must remember what was actually said on the tape vs. what the Narrator claimed. Players who didn't listen carefully will catch distorted words by accident.

### Word Lists

| Tape                      | True Words (catch)                                                                       | Distorted Words (avoid)                                  |
| ------------------------- | ---------------------------------------------------------------------------------------- | -------------------------------------------------------- |
| Tape 1 — Argument         | WILL, TOMORROW, NINE O'CLOCK, FIFTY THOUSAND, EMBEZZLEMENT, DISMISSED, SIPHONING, HAROLD | DISAGREEMENT, TIRED, ROUTINE, FORGIVEN, MISUNDERSTANDING |
| Tape 2 — James Interview  | NO ALIBI, SOLICITOR, LOAN, FIFTY THOUSAND, EMBEZZLEMENT, MOTIVE                          | GRIEF, INNOCENT, COOPERATIVE, HONEST, ROUTINE            |
| Tape 3 — Daniel Interview | HELPING MOVE, TWENTY THOUSAND, NO ENTRY, DISMISSED, SLIPPED, LOGBOOK                     | SIDE WORK, FORGOT, SHED, ALONE, PROFESSIONAL             |

### Narrator Reactions

**Tape 1 — Win:**
_"Harold meant it. The will was changing at nine the next morning. James had one night."_

**Tape 1 — Lose:**
_"A disagreement between father and son. These things happen in families. Nothing more."_

**Tape 2 — Win:**
_"He asked for his solicitor the moment finances came up. Not when accused of murder. Finances."_

**Tape 2 — Lose:**
_"A grieving son. Naturally defensive. That is all I choose to see."_

**Tape 3 — Win:**
_"He almost said it. 'I was helping move--' That sentence was never finished. It should have been."_

**Tape 3 — Lose:**
_"A forgetful man. Groundskeepers have many tasks. The missing log entry means nothing."_

### Assets Used

- Background: `assets/minigames/darkbackground.jpg`
- Word container: `assets/minigames/container.png`
- Screen distortion: `assets/minigames/screendistortion.png`
- No player sprite — green line drawn via ShapeRenderer

---

## MINIGAME 2: THE MAZE

**Triggers after:** Tapes 4, 5, and 6
**Mechanic type:** Atmospheric / deduction

### How It Works

After the tape text is dismissed, the screen transitions into a small dark top-down maze. The player navigates their character through it. Visibility is limited by fog — the player can only see a short radius around themselves.

Walls are labeled with statements. When the player walks toward a path, the label on that wall becomes readable. The player chooses which direction to go based on whether the statement is **true** (from the tape) or **distorted** (the Narrator's version).

- Follow a **TRUE statement** → path opens, player moves forward
- Follow a **DISTORTED statement** → dead end. The dead end overlay flashes. The maze slightly contracts. Too many wrong turns = failure.
- A **shadow entity** slowly follows the player through the maze. If it catches the player, Awareness rises. The player cannot stand still for too long reading walls.
- **Win condition:** Reach the exit
- **Lose condition:** Too many dead ends OR caught by the shadow

### Controls

- `W` / `↑` — move up
- `S` / `↓` — move down
- `A` / `←` — move left
- `D` / `→` — move right

### Why It Requires Brain Power

The Narrator's distortions are written convincingly — they sound almost reasonable. The player has to remember exactly what the tape said and judge which wall statement is the truth. Wrong turns are penalised by a shrinking maze and an encroaching shadow.

### Wall Statement Sets (per tape)

**Tape 4 — Margaret Interview**

- TRUE paths: _"She heard dragging sounds at two in the morning"_, _"Two people whispering at midnight"_, _"The footsteps went toward the cellar stairs"_
- DISTORTED dead ends: _"She was always nervous, always hearing things"_, _"Old houses make sounds at night"_, _"Margaret was confused by grief"_

**Tape 5 — Marcus Interview**

- TRUE paths: _"Marcus left at eleven. The hotel confirms 11:47"_, _"Vance Manor to Ashworth Inn is thirty minutes"_, _"He saw James storm past after the argument"_
- DISTORTED dead ends: _"Marcus had no reason to harm Harold"_, _"He came voluntarily. Innocent men do that"_, _"The alibi is confirmed. Move on"_

**Tape 6 — Charles Interview**

- TRUE paths: _"Charles saw James walking toward the study at 10:45"_, _"James looked determined. Almost grim"_, _"He never saw James return"_
- DISTORTED dead ends: _"Charles assumed James was going to apologise"_, _"A loyal assistant. Nothing more suspicious than that"_, _"James was probably just restless after the argument"_

### Narrator Reactions

**Tape 4 — Win:**
_"The dragging stopped at the cellar door. It always stops at the cellar door."_

**Tape 4 — Lose:**
_"Margaret was frightened. Old houses make sounds. That is all it was."_

**Tape 5 — Win:**
_"Marcus left at eleven. The manor was quiet after that. That's when it starts paying attention."_

**Tape 5 — Lose:**
_"He had an alibi. Move on."_

**Tape 6 — Win:**
_"Nobody walks toward that study and comes back the same."_

**Tape 6 — Lose:**
_"Charles assumed the best. People do."_

### Shadow Entity

A dark humanoid silhouette that slowly pursues the player through the maze. Three animation frames — each slightly more distorted than the last (glitching edges, displaced limbs). If it touches the player: Awareness +2. Does not reset between wrong turns.

### Assets Used

- Wall tile variant 1 (clean stone): `assets/minigames/wall tile variant1.png`
- Wall tile variant 2 (cracked stone): `assets/minigames/wall tile variant2.png`
- Wall tile variant 3 (moss stone): `assets/minigames/wall tile variant3.png`
- Floor tile: `assets/minigames/floortile.png`
- Fog texture: `assets/minigames/fog texture.png`
- Shadow entity frames: `assets/minigames/shadow entity1.png`, `shadow entity2.png`, `shadow entity3.png`
- Exit visual: `assets/minigames/exitvisual.png`
- Dead end overlay: `assets/minigames/deadend.jpg`
- Player character: reused from `assets/rooms/endings/walk_*/` (8-directional, 8 frames each)

---

## Tapes 7 and 8

Margaret's personal account (Tape 7) and Arthur's death recording (Tape 8) do **not** trigger either minigame. These two tapes are the emotional and horror climax of the game. They are listened to without interruption. The player's only job is to hear them.

Tape 8 is also where the Narrator's identity as a previous dead investigator is revealed — simultaneously to both the Narrator and the player.

---

## Consequence Summary

| Result       | Immediate effect          | Story effect                                                         |
| ------------ | ------------------------- | -------------------------------------------------------------------- |
| Catcher WIN  | Narrator crack text shown | Murder truth sharpened                                               |
| Catcher LOSE | Narrator deflection shown | Distorted version accepted, Awareness +1 per wrong catch during play |
| Maze WIN     | Narrator crack text shown | Entity detail surfaces                                               |
| Maze LOSE    | Narrator deflection shown | Awareness raised by shadow contact                                   |

---

## Debug Shortcut

Press **F11** anywhere in the main game to launch The Catcher directly with Tape 1 (for testing). Remove before final build.

Plan: Sorting Algorithms + Scoring in Both Minigames

--- CatcherMinigame → Selection Sort

Concept: The falling words are labeled elements with hidden order values. The player isn't just catching "true" words — they're building a sorted
sequence. Each catch is one selection step: catch the correct next element in order.

Mechanic:

- At the top of the screen, a row of empty slots represents the sorted array being built (e.g. 5 slots)
- Words fall with a visible label and a hidden rank (chronological order of events)
- Each round, one "correct next" word falls alongside distractors
- Catching the correct next element fills the next slot → selection sort step complete
- Catching a distractor or out-of-order element = wrong (distortion flare + penalty)
- Win = all 5 slots filled in order

Example for TAPE_ARGUMENT:
Correct sequence: [DINNER] → [ARGUMENT] → [WILL] → [NINE O'CLOCK] → [DISMISSED]
Round 1 goal: catch DINNER (rank 1) — distractors: FORGIVEN, MISUNDERSTANDING
Round 2 goal: catch ARGUMENT — distractors: TIRED, ROUTINE
...

Score: (correct catches / total) × 100 + speed bonus per catch (faster = more points)

---

MazeMinigame → Insertion Sort

Concept: Each fork is an insertion sort comparison: "Does this statement belong BEFORE or AFTER the one already placed?" The player builds a sorted
timeline by choosing the correct fork, and at the bottom of the maze they see the timeline assembled.

Mechanic:

- Fork choice reframed: left = "this event came BEFORE what I've already accepted", right = "this event came AFTER"
- The player's accepted truths accumulate in a visual HUD "timeline bar" (e.g. 3 accepted slots)
- At each fork, the new statement is being "inserted" into the correct position in the timeline
- A correct fork = inserted in the right position = timeline slot fills in order
- Wrong fork = out-of-place insertion = dead-end flash, awareness penalty, shadow speeds up

Visual Timeline HUD:
[Two people at midnight] → [Footsteps to cellar stairs] → [Dragging at 2AM]
slot 1 (filled) slot 2 (?) slot 3 (?)

Score: (correct inserts / 3) × 100 + penalty for wrong turns, bonus if no wrong turns

---

Scoring System (shared)

Score Tier Range Outcome
─────────────────────────────────────────────────────
TRUTH SURFACED 85–100 Full narrator slip text, +1 evidence clue revealed, −1 awareness
MOSTLY CLEAR 60–84 Partial narrator text, normal continuation
DISTORTED 35–59 Distortion text wins, +1 awareness
CORRUPTED 0–34 Full distortion outcome, +2 awareness, shadow resets closer next time

End screen shows:

- Score: XX / 100
- Tier label (TRUTH SURFACED / MOSTLY CLEAR / etc.)
- A 1-line narrator reaction based on tier
- Stars or a meter visualization

Score is stored in GameState (lastMinigameScore) so the following:

- Narrator mood (NarratorSystem) adjusts its filter based on score
- High scores unlock deeper examination dialogue on re-visit
- Low scores add an extra awareness point when returning to the room

---

What changes in code

┌─────────────────────────────────────┬────────────────────────────────────────────────────────────────────────────────────────────────────────┐  
 │ File │ Change │  
 ├─────────────────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────┤  
 │ CatcherMinigame.java │ Replace trueWords[] with ordered WordElement[] (text + rank). Track target rank. Catch logic checks │  
 │ │ rank match. Add score field. │  
 ├─────────────────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────┤  
 │ MazeMinigame.java │ Reframe fork labels as "before/after" comparisons. Add HUD timeline bar with collected truths. Add │  
 │ │ score field. │  
 ├─────────────────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────┤  
 │ GameState.java │ Add int lastMinigameScore, addMinigameScore(), getter │  
 ├─────────────────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────┤  
 │ ExamResult.java / │ Check lastMinigameScore for bonus dialogue on re-examine │  
 │ ExaminationSystem.java │ │  
 ├─────────────────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────────────────────┤  
 │ NarratorSystem.java │ Check score tier when filtering narrator lines │

---

## MINIGAME 3: THE METAL DETECTOR

**Status:** Prototype exists in `C:\vscode_projects\Testing 2` — ready to integrate.
**Source files:** `MetalDetectorScreen.java`, `MetalDetectorGame.java`
**Mechanic type:** Spatial / audio feedback

### What It Is

The player sweeps a metal detector across the Study room. A beep frequency increases (interval shortens) as the detector nears a hidden tape. After 10 seconds the sweep phase ends and the player must click to place a circle marking where they think the tape is buried. If the circle overlaps the tape position, they collect it.

This replaces or supplements the current hotspot-click tape discovery system for tapes hidden in the Study.

### How It Works

**Phase flow:** `NARRATOR_INTRO → DEMO → DETECTING → PLACING`

1. **NARRATOR_INTRO** — Narrator audio line explains the mechanic.
2. **DEMO** — Animated cursor sweeps toward a fake tape position, showing the player how beep rate changes with distance. Ends when animation and narrator finish.
3. **DETECTING** — Player moves detector with WASD. Beep interval calculated from nearest uncollected tape using cubic easing:
   ```
   float t = (clamped - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE);
   t = t * t * t;
   beepInterval = MIN_INTERVAL + t * (MAX_INTERVAL - MIN_INTERVAL);
   ```
   Constants: `DETECT_DURATION = 10s`, `MAX_DISTANCE = 500f`, `MIN_DISTANCE = 30f`, `MAX_INTERVAL = 4000ms`, `MIN_INTERVAL = 50ms`, `MOVE_SPEED = 120f`.
4. **PLACING** — Sweep ends (ding sound). Player clicks to place a 30px radius green circle. Hit detection checks if circle overlaps any tape circle (also 30px). Win plays `correct.mp3`, lose plays `game over.mp3`.

### Key Implementation Details

**Beep thread** — runs on a daemon thread separate from the render loop. Generates raw PCM sine tones via `javax.sound.sampled` (440Hz, 60ms, 44100 sample rate). Thread checks `phase == DETECTING || phase == DEMO` before firing.

**Demo cursor path** — eases from x=160 toward the fake tape x, then retreats slightly:
```java
if (t < 0.6f) path = t / 0.6f;          // approach
else           path = 1f - ((t - 0.6f) / 0.4f) * 0.25f;  // slight retreat
demoCursorY += sin(demoTimer * 1.8f) * 35f;  // sine wobble
```

**Progress bar** — animated via 28 PNG frames in `progressbar/pixilart-frames/pixil-frame-N.png`, displayed on the left side during detecting phase.

**Assets already in dsa_2d:**
- `rooms/study.png` — same file
- `progressbar/pixilart-frames/` — same frames
- `ding.mp3`, `correct.mp3`, `game over.mp3` — same files
- **Missing:** `metal_detector.png` — copy from Testing 2 assets

### Integration Steps

| Step | File | What to do |
|------|------|-----------|
| 1 | New `screens/MetalDetectorMinigame.java` | Create class implementing `Screen`, copy beep thread + `playTone()` + detect/place logic from `MetalDetectorScreen.java` |
| 2 | `MetalDetectorMinigame.java` | Replace hardcoded `TAPE_POSITIONS` with live positions from `GameState` — only include uncollected tapes in the current room |
| 3 | `GameScreen.java` | On tape search action in Study, call `game.setScreen(new MetalDetectorMinigame(game, gameState, tapeList))` instead of direct hotspot award |
| 4 | `MetalDetectorMinigame.java` | On win: call `gameState.collectTape(foundTape)`, then `game.setScreen(new GameScreen(...))`. On lose: `gameState.increaseAwareness(10)`, return to GameScreen |
| 5 | `NarratorSystem.java` | Add commentary lines for win/lose results (same pattern as Catcher/Maze reactions) |
| 6 | `assets/` | Copy `metal_detector.png` from Testing 2 |
| 7 | `assets/sfx/narrator/` | Add `intro.mp3` and `demo.mp3` — the `NARRATOR_INTRO` phase plays the intro line before the demo begins, and the demo line plays during the animated cursor sweep. These are intentional and must exist; do not replace with TextPanel text |

### Tape Positions (Study room)

These positions are already confirmed working in Testing 2:

| Tape | Position |
|------|----------|
| `TAPE_ARGUMENT` (under desk) | `(636, 164)` |
| `TAPE_JAMES_INTERVIEW` (bookshelves) | `(1065, 377)` |

Cross-reference with `HotspotPositions.java` to confirm alignment before wiring up.

### Narrator Reactions

**Win:**
_"Your instincts led you right to it. The tape was exactly where silence pointed."_

**Lose:**
_"A guess. An unlucky one. The tape remains where it always was, waiting."_

### Scoring (optional, follows existing scoring system)

Use the same tier system from Catcher/Maze if a score is desired — base on how close the placed circle was to the tape center (distance ratio). Store in `GameState.lastMinigameScore`.

### Assets Used

- Background: `assets/rooms/study.png`
- Metal detector sprite: `assets/metal_detector.png` (copy from Testing 2)
- Progress bar frames: `assets/progressbar/pixilart-frames/pixil-frame-0.png` through `pixil-frame-27.png`
- Sounds: `ding.mp3`, `correct.mp3`, `game over.mp3`, `bgm.mp3`
