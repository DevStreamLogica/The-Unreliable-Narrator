# The Unreliable Narrator — Full Game Design Plan

---

## The Story

The player is a **stranger who stumbled upon Vance Manor by accident**, years after the 1987 murder of Harold Vance. Something supernatural compels them to stay and investigate. Nobody hired them. They have no professional reason to be here.

**Arthur Hollis** was the private investigator hired by Margaret Vance. He arrived November 19th, 1987 — days after the murder — and was killed by the Entity when he opened the sealed cellar wall. The player finds his recordings and evidence left behind.

**The narrator** is a separate voice — not Arthur, not the Entity. Identity is deliberately ambiguous. He guides the player through the manor but lies to protect himself and the Entity.

**The crime:** James Vance and Daniel Hobbs murdered Harold Vance to prevent James's disinheritance. Harold had discovered James embezzled £50,000 and was changing his will the next morning.

**The deeper horror:** Harold sealed his business partner Thomas Ashford alive behind the cellar wall in 1957. Thirty years of darkness turned Thomas into something inhuman — the Entity. Arthur Hollis broke through the wall and was consumed.

**The ending:** One outcome only. The player uncovers the full truth, breaks the narrator's control, and opens the cellar wall. The Entity emerges. The ending is a chase scene.

---

## Core Mechanic — Darkness and the Cursor Light

### The Base State
Every room defaults to **darkness**. The cursor is the only light source — a circular radius of visibility centered on the mouse. Everything outside that radius is black.

- The game starts dimly lit — the manor has been abandoned for decades, it is naturally dark
- As the investigation progresses and the Entity grows more agitated, the light radius **shrinks further**
- The darkness is not just atmosphere — it is the narrator and the Entity actively limiting what the player can perceive

### What This Means for Exploration
- The player physically moves their cursor around the room to see what is there
- Hotspots are hidden in darkness until the cursor passes over them
- Early game: generous light radius, most of the room is visible
- Late game: tiny radius, the player is essentially blind without knowing where to look

---

## Core Mechanic — The Narrator Fills the Void

When the room is dark and the player is searching, the narrator speaks. He describes things — tells the player what is in corners they haven't reached yet, what objects are nearby, what is worth examining.

**Sometimes he is telling the truth.** Sometimes he is lying — directing the player toward red herrings, away from key evidence, into dead ends.

The player cannot always tell which is which. This is the central tension of the game.

### The Narrator's Tells
The narrator is not perfectly consistent. Over time the player learns to read him:
- His tone shifts when he lies — slightly more insistent, slightly too helpful
- He describes red herrings in more detail than real evidence
- He goes quiet or deflects when the player is near something important

---

## Core Mechanic — The Entity Pulls the Cursor

The Entity periodically exerts influence over the cursor. The light drifts — pulled slightly in a direction the Entity wants the player to look. Away from key evidence, toward false leads.

- The pull is subtle at first — barely noticeable
- As awareness increases, the pull becomes stronger and more frequent
- The player must actively resist it to stay on track
- Resisting costs nothing mechanically — the player just has to notice it is happening

---

## Core Mechanic — The Hold Timer (Catching the Narrator Lying)

### The Setup
Rooms default to their **"empty" state** — the version of the room art without the item present. Items are hidden by the narrator, not just by darkness.

When the player moves their cursor over a hotspot the narrator is concealing, a subtle visual tell appears — a faint flicker or unnatural stillness in the darkness at that spot.

### The Mechanic
The player holds their cursor still over the spot. A **progress ring** fills around the cursor.

While the ring fills:
- The narrator speaks — louder, more insistent, telling the player there is nothing there
- The cursor pull activates — the Entity tries to drag the light away from the spot
- The screen distorts slightly at the edges

If the player holds long enough and the ring completes:
- The item **reveals itself** in the room
- The narrator goes quiet momentarily
- The player can now collect the item normally
- The room switches to its post-collection state once the item is taken

If the player's cursor drifts off the spot before the ring completes:
- The ring resets
- The narrator sounds more confident
- Awareness increases slightly

### Escalation
- **Early game:** Short hold time, weak cursor pull, mild narrator resistance
- **Mid game:** Longer hold time, stronger pull, narrator more aggressive
- **Late game:** Long hold time, strong pull, heavy distortion, narrator frantic

### What This Applies To
Every collectible item in the game — evidence and tapes alike. The narrator hides everything by default. The player must fight for every discovery.

---

## The Darkness Progression

Tied to the awareness meter (0–80):

| Awareness | Light Radius | Narrator Behavior | Entity Pull |
|---|---|---|---|
| 0–20 | Large — most of room visible | Calm, helpful-sounding | Absent |
| 20–40 | Medium — roughly half the room | Slightly insistent | Occasional, weak |
| 40–60 | Small — immediate area only | Nervous, contradictory | Frequent, moderate |
| 60–70 | Tiny — barely beyond cursor | Frantic, desperate | Constant, strong |
| 70–80 | Near-total darkness | Screaming, incoherent | Overwhelming |
| 80 | Game over | — | — |

---

## The Tapes

Tapes remain a core part of the game. They are physical objects hidden in rooms, concealed by the narrator like all other evidence. The hold-timer mechanic applies to finding them.

Once collected, tapes are played from the inventory. The minigames (Catcher and Maze) trigger after certain tapes as before.

The tapes are Arthur Hollis's recordings — police interviews, personal accounts, and his death recording. They are the player's primary source of narrative truth, which is why the narrator works so hard to hide them.

### Tape Visibility Note
Rooms already have multiple art states for when tapes are collected (e.g. kitchen with/without tape recorder on floor). The "without" version is the default. The hold-timer reveals the item visually, then collection switches the room to the appropriate state.

---

## The Minigames

### Catcher Minigame
- Triggers after tapes 1, 2, 3
- Word-catching mechanic — catch true words, avoid distorted ones
- Unchanged from current implementation

### Maze Minigame
- Triggers after tapes 4, 5, 6
- Navigation puzzle with true/false fork decisions
- Unchanged from current implementation

### Document Reconstruction
- Triggers from study fireplace ashes examination
- Drag-and-drop torn letter fragments
- Unchanged from current implementation

---

## The Ending — Chase Scene

Once the player has collected enough evidence and watched all available tapes, the cellar wall becomes interactable. The narrator tries to stop the player from approaching it.

The player holds the cursor on the wall — the largest, most resisted hold-timer in the game. The narrator is at full frenzy. The Entity pull is at maximum. The screen is near-total darkness.

When the wall breaks:
- The Entity emerges
- The chase scene begins — using the existing detective running animations (8-directional, 4 frames each) and the shadow entity assets already in the project
- The player must navigate the manor in near-total darkness to reach the exit
- The Entity pursues using the same shadow chase logic from the maze minigame
- Reaching the exit ends the game

---

## What Gets Removed

- Accusation screen
- Suspect interview system (replaced by tape-based narrative)
- Multiple ending choices (seal wall / destroy tapes / escape manor)
- Suspect cooperation mechanic
- Hints button
- SUSPECTS, NOTEBOOK, HINTS action bar buttons — INVENTORY only remains
- Narrator distortion auto-discovery (replaced by hold-timer as the active discovery mechanic)

---

## What Gets Added

- **Darkness overlay** with circular cursor cutout, applied to all room rendering
- **Light radius variable** tied to awareness meter
- **Entity cursor pull** — periodic drift applied to cursor position
- **Hold-timer progress ring** — renders around cursor when hovering a concealed hotspot
- **Narrator resistance audio/visual** — distortion and voice escalation during hold-timer
- **Room default states** — all rooms load in their "empty" item state by default
- **Chase scene screen** — using existing running animations and shadow entity assets

---

## What Stays the Same

- All 10 room backgrounds and their state variants
- All evidence definitions and descriptions
- All tape content and transcripts
- Catcher, Maze, and Document Reconstruction minigames
- Awareness meter and threshold system
- Narrator mood system and distortion text
- Save/load system
- Three-act structure (Act 1: ground floor, Act 2: cellar, Act 3: Margaret's room)
- Character portrait assets
