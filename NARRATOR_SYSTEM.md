# Narrator System — Testing 3

The narrator speaks constantly throughout the game. What they say is determined by the current gap, how far along the player is within that gap, and the current awareness level.

---

## What the Narrator Tracks

- **Current gap** (1–6) — set when a tape finishes playing
- **Objects found** in the current gap (count and which ones)
- **Combinations made** in the current gap
- **Was the player just misled** (distortion flag — was the last room hint a lie?)
- **Current awareness level** (0–80)

---

## Trigger Types

### 1. Gap Opens
*Fired when a tape finishes playing and new objects appear in the world.*

Narrator acknowledges a memory surfacing. Hints loosely at which rooms now have something worth finding. Does not name specific objects.

**Truthful example:**
*"Something just came back to me. There are things in this house I hadn't noticed before. The study. James's room. Look carefully."*

**Never distorted** — gap opening is always truthful. The narrator needs the player to start somewhere.

---

### 2. Room Entry
*Fired when the player enters a room.*

If the room has unfound objects for the current gap → narrator reacts to being in that space. Subtle acknowledgement that something is here.

If the room has no unfound objects for the current gap → narrator says nothing, or gives a neutral ambient line.

**Truthful example (room has objects):**
*"Harold spent a lot of time in this room. There's something here."*

**Distorted example (sends player to wrong room):**
*"There's nothing left in here worth finding. Try the kitchen."*
→ If the player goes to the kitchen and it has no gap objects, trigger **Wrong Room Entry**.

---

### 3. Wrong Room Entry (Post-Distortion)
*Fired when the player was misled by a distorted room hint and enters the wrong room.*

Narrator immediately corrects themselves. Sounds confused, not apologetic. The slip should feel like a crack in the narrator's composure, not a system message.

**Example:**
*"Wait. No. That's not — I don't know why I said that. It's not here. Go back to the [correct room]."*

*"I was wrong. Ignore what I said. The [correct room] — that's where you need to look."*

The correct room is always named directly. Player is never left without direction after a distortion.

---

### 4. Object Found
*Fired when the player's light hits an object and picks it up.*

Narrator reacts to the specific object. Each object has its own narrator line.

**Truthful example (solicitor's letter):**
*"Harold had already written to his solicitor. The appointment was confirmed. James knew exactly what the morning would bring."*

**Distorted example:**
*"That's old correspondence. Nothing relevant. Harold wrote dozens of letters."*

---

### 5. Wrong Combination
*Fired when the player drags two incompatible objects together.*

Narrator tells the player those two things don't connect. Lines should vary so repeated wrong attempts don't feel robotic.

**Examples:**
*"Those two things have nothing to do with each other."*
*"That's not a connection. Keep looking."*
*"No. One of those belongs with something else entirely."*
*"You're forcing it. Something isn't right about that pairing."*

No awareness cost. Never distorted — wrong combination feedback is always honest.

---

### 6. Correct Combination
*Fired when two compatible objects merge into a combined item.*

Most important dialogue moment per gap. Narrator has a memory fragment triggered by the combination. Reacts to what the combined item means — not just confirming it worked, but saying something meaningful about the story.

**Truthful example ("Confirmed Appointment" — Gap 1):**
*"The appointment was already made. Harold wasn't threatening James. He had already decided. James had until nine in the morning."*

**Distorted example (high awareness):**
*"That doesn't prove anything. Harold changed his mind constantly. This means nothing."*

---

### 7. Narrator Breakdown
*Fired automatically after TAPE_MARGARET_ACCOUNT (tape 7) finishes. There is no inventory gap between tape 7 and tape 8 — the breakdown replaces it.*

The narrator delivers a long, fragmented monologue that begins as composed and immediately collapses. He insists it is the first time he has heard Margaret's recording — then in the same breath recalls specific sensory details from the night she described: the date, the rain, the kitchen light, the brown coat on the hook, the sound from the cellar.

Each recall is followed by a denial or a rationalisation. The rationalisation is less convincing each time. The monologue ends with him telling the player to go — not to find a tape, but because he can no longer stay composed.

This trigger launches TAPE_ARTHUR_DEATH (tape 8) directly. No metal detector. No inventory phase.

**Never distorted.** The narrator cannot control what he says here.

---

### 8. Gap Complete
*Fired when all combinations for the current gap are done and the metal detector activates.*

Narrator remembers where the tape is. Names a specific location. This line is always truthful — the metal detector needs a real destination.

**Example:**
*"I remember now. There was a recording. Someone left it near the kitchen — behind the cellar door, I think. I can feel where it is."*

---

### 8. Idle
*Fired when the player hasn't interacted with anything for a set time (suggested: 45 seconds).*

Narrator nudges the player back toward unfound objects in the current gap.

**Truthful example:**
*"You haven't found everything yet. There's still something in [room with unfound objects]."*

**Distorted example:**
*"I think you've found everything here. Maybe move on."*
→ If player hasn't found everything, this misdirects them. Correct with Wrong Room Entry if they leave.

---

## Awareness Thresholds for Distortion

| Awareness | Distortion Chance |
|---|---|
| 0–39 | 0% — always truthful |
| 40–59 | 20% chance distorted |
| 60–79 | 50% chance distorted |

Triggers **never** distorted: Gap Opens, Wrong Combination, Gap Complete, Wrong Room Entry correction, Narrator Breakdown.

Triggers **that can** distort: Room Entry, Object Found, Correct Combination, Idle.

---

## Per-Gap Narrator Dialogue

Each gap needs:
- 1 gap opening line
- Room entry lines for each room that has objects (truthful + distorted)
- Object found lines for each object (truthful + distorted)
- 4–6 wrong combination lines (shared across all gaps, rotate randomly)
- Correct combination lines for each pair (truthful + distorted)
- 1 gap complete line
- Idle lines (2–3 per gap, truthful + distorted)
- Wrong room correction lines (1–2 per gap)

All per-gap dialogue is written in NARRATOR_DIALOGUE.md.

Tape play order (matches Tape.java enum index):
1. TAPE_ARGUMENT → Gap 1
2. TAPE_MARGARET_INTERVIEW → Gap 2
3. TAPE_MARCUS_INTERVIEW → Gap 3
4. TAPE_CHARLES_INTERVIEW → Gap 4
5. TAPE_JAMES_INTERVIEW → Gap 5
6. TAPE_DANIEL_INTERVIEW → Gap 6
7. TAPE_MARGARET_ACCOUNT → Narrator Breakdown → TAPE_ARTHUR_DEATH

**Gap 1** — Written (after TAPE_ARGUMENT)
**Gap 2** — Written (after TAPE_MARGARET_INTERVIEW)
**Gap 3** — Written (after TAPE_MARCUS_INTERVIEW — shed note key: `nar_g3_obj_discardnote_t/d`)
**Gap 4** — Written (after TAPE_CHARLES_INTERVIEW — certificate in parlor)
**Gap 5** — Written (after TAPE_JAMES_INTERVIEW)
**Gap 6** — Written (after TAPE_DANIEL_INTERVIEW — bracket and invoice both in cellar)

**Narrator Breakdown** — Written (see NARRATOR_DIALOGUE.md). Fires after TAPE_MARGARET_ACCOUNT, leads directly to TAPE_ARTHUR_DEATH.
