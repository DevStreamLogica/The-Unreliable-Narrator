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

| Tape | True Words (catch) | Distorted Words (avoid) |
|------|--------------------|------------------------|
| Tape 1 — Argument | WILL, TOMORROW, NINE O'CLOCK, FIFTY THOUSAND, EMBEZZLEMENT, DISMISSED, SIPHONING, HAROLD | DISAGREEMENT, TIRED, ROUTINE, FORGIVEN, MISUNDERSTANDING |
| Tape 2 — James Interview | NO ALIBI, SOLICITOR, LOAN, FIFTY THOUSAND, EMBEZZLEMENT, MOTIVE | GRIEF, INNOCENT, COOPERATIVE, HONEST, ROUTINE |
| Tape 3 — Daniel Interview | HELPING MOVE, TWENTY THOUSAND, NO ENTRY, DISMISSED, SLIPPED, LOGBOOK | SIDE WORK, FORGOT, SHED, ALONE, PROFESSIONAL |

### Narrator Reactions

**Tape 1 — Win:**
*"Harold meant it. The will was changing at nine the next morning. James had one night."*

**Tape 1 — Lose:**
*"A disagreement between father and son. These things happen in families. Nothing more."*

**Tape 2 — Win:**
*"He asked for his solicitor the moment finances came up. Not when accused of murder. Finances."*

**Tape 2 — Lose:**
*"A grieving son. Naturally defensive. That is all I choose to see."*

**Tape 3 — Win:**
*"He almost said it. 'I was helping move--' That sentence was never finished. It should have been."*

**Tape 3 — Lose:**
*"A forgetful man. Groundskeepers have many tasks. The missing log entry means nothing."*

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
- TRUE paths: *"She heard dragging sounds at two in the morning"*, *"Two people whispering at midnight"*, *"The footsteps went toward the cellar stairs"*
- DISTORTED dead ends: *"She was always nervous, always hearing things"*, *"Old houses make sounds at night"*, *"Margaret was confused by grief"*

**Tape 5 — Marcus Interview**
- TRUE paths: *"Marcus left at eleven. The hotel confirms 11:47"*, *"Vance Manor to Ashworth Inn is thirty minutes"*, *"He saw James storm past after the argument"*
- DISTORTED dead ends: *"Marcus had no reason to harm Harold"*, *"He came voluntarily. Innocent men do that"*, *"The alibi is confirmed. Move on"*

**Tape 6 — Charles Interview**
- TRUE paths: *"Charles saw James walking toward the study at 10:45"*, *"James looked determined. Almost grim"*, *"He never saw James return"*
- DISTORTED dead ends: *"Charles assumed James was going to apologise"*, *"A loyal assistant. Nothing more suspicious than that"*, *"James was probably just restless after the argument"*

### Narrator Reactions

**Tape 4 — Win:**
*"The dragging stopped at the cellar door. It always stops at the cellar door."*

**Tape 4 — Lose:**
*"Margaret was frightened. Old houses make sounds. That is all it was."*

**Tape 5 — Win:**
*"Marcus left at eleven. The manor was quiet after that. That's when it starts paying attention."*

**Tape 5 — Lose:**
*"He had an alibi. Move on."*

**Tape 6 — Win:**
*"Nobody walks toward that study and comes back the same."*

**Tape 6 — Lose:**
*"Charles assumed the best. People do."*

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

| Result | Immediate effect | Story effect |
|--------|-----------------|--------------|
| Catcher WIN | Narrator crack text shown | Murder truth sharpened |
| Catcher LOSE | Narrator deflection shown | Distorted version accepted, Awareness +1 per wrong catch during play |
| Maze WIN | Narrator crack text shown | Entity detail surfaces |
| Maze LOSE | Narrator deflection shown | Awareness raised by shadow contact |

---

## Debug Shortcut
Press **F11** anywhere in the main game to launch The Catcher directly with Tape 1 (for testing). Remove before final build.
