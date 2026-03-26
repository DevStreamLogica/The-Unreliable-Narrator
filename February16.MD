# February 16 - Three-Act Structure Implementation Plan

---

## CORRECTION NOTE — February 17 (added after game analysis review)

### Player Character Identity — The Current Code Is Wrong / Incomplete

During a full game analysis session, the player character was incorrectly described as:
> *"a private investigator hired by Margaret Vance to investigate the death of her father."*

**This is WRONG.** Per the designer's intent (confirmed February 17):

**The correct player framing is:**
- The player is a **stranger who stumbled upon Vance Manor by accident**
- **Something supernatural is pulling them** to investigate — they don't fully know why they're compelled to stay and look into it
- The murder **happened decades ago** — the player is not arriving two days after the crime; they're an outsider encountering a cold case from the past
- **Nobody hired them.** They have no professional reason to be here

**Arthur Hollis** (the previous investigator, whose recordings guide the player via the narrator) is the one who was hired — specifically by Margaret Vance. Arthur arrived on November 19th, 1987, just days after the murder. The player arrives much later and finds his recordings.

**What currently needs to be established in code (not yet written):**
- An opening sequence / intro text that explains the player's accidental arrival at the abandoned manor
- The supernatural pull that keeps them investigating despite having no obligation to do so
- The time gap between 1987 (when the murder and Arthur's death occurred) and the present day (when the player arrives)

**What the current code gets right (no changes needed):**
- The narrator header: *"Ah, you've found Arthur's recordings. Good. I've been waiting for someone who would listen."* — Correctly frames the player as a discoverer of old recordings, not a hired investigator
- The channeling mechanic (February16.MD section): *"The suspects are no longer physically present at Vance Manor — the events happened decades ago."* — Already aligned with the correct framing
- No code currently says the player was hired — that framing was an error in the analysis, not in the code

**Impact on existing systems:**
- Dialogue from the suspects (MARGARET_BROTHER: *"That's why I contacted a private investigator -- a man named Hollis"*) refers to Arthur, not the player. This is correct and needs no change.
- The "cooperation" system and interview system work fine as channeled memories, not live conversations
- RoomDescriptions and ExaminationSystem are all in past tense context — consistent with the player exploring an old crime scene

**TODO:** Write the intro text / opening sequence that establishes:
1. Who the player is (unnamed stranger, not a PI)
2. Why they're at Vance Manor (accident / compulsion / passing through)
3. The time period (present day, years after 1987)
4. The first discovery that hooks them (finding the tape recorder in the entrance hall, or hearing a sound from the cellar)

---

## Overview

Restructure the game into three narrative acts gated by the Entity's influence over the manor. The Entity controls which rooms the player can access, creating a structured progression that ensures players engage with the murder mystery before encountering the horror layer.

### Act 1 — "The Investigation"
- All rooms EXCEPT Margaret's Room and Cellar
- Player builds the murder case: evidence, tapes, interviews, contradictions
- Available: 8/10 evidence, 6/8 tapes, all 5 suspects

### Act 2 — "The Cellar"
- Cellar unlocks after progress threshold
- Player finds bloodstained cuff, broken Tape 8, and BREATHING_WALL anomaly
- Entity pulls power back from Margaret's door

### Act 3 — "The Truth"
- Margaret's Room unlocks after BREATHING_WALL discovery
- Player finds blackmail note, Tape 7, and tape repair kit
- Repairs Tape 8 → watches it → climax triggers

---

## Files That Need Changes

### 1. GameState.java — New state flag

**What:** Add a `boolean hasTapeRepairKit` field.

**Why:** Tracks whether the player has found the repair kit from Margaret's dresser. Needed to determine if Tape 8 (found damaged) can be watched.

**Changes:**
- Add field: `private boolean hasTapeRepairKit = false;`
- Add getter: `public boolean hasTapeRepairKit() { return hasTapeRepairKit; }`
- Add setter: `public void setHasTapeRepairKit(boolean value) { hasTapeRepairKit = value; }`
- Add to `clearAllState()`: `hasTapeRepairKit = false;`

**Impact on other files:** SaveLoadSystem needs to persist this flag. GameScreen needs to check it when player tries to watch Tape 8.

---

### 2. GameScreen.java — Navigation gating + tape watching gate

**What:** Add room access checks in `handleNavigation()` and add a damaged tape check in `playTape()`.

#### 2a. Navigation gating in handleNavigation()

**Current code (line 336):**
```java
private void handleNavigation(Room.RoomID target) {
    roomManager.navigateTo(target);
    ...
}
```

**New code — add gate checks BEFORE the navigateTo call:**
```java
private void handleNavigation(Room.RoomID target) {
    // Act structure: Entity gates certain rooms
    if (target == Room.RoomID.CELLAR && !isCellarUnlocked()) {
        textPanel.show("THE CELLAR\n\nThe door to the cellar won't budge. The handle is ice-cold " +
            "to the touch, far colder than it should be. Something below is keeping this door sealed.\n\n" +
            "The narrator's voice is firm: \"The cellar is irrelevant to your investigation, detective. " +
            "Focus on the living, not the dead.\"\n\n" +
            "[Continue investigating. Gather more evidence and testimony before the Entity's grip weakens.]");
        panelMode = PanelMode.TEXT;
        return;
    }
    if (target == Room.RoomID.MARGARET_ROOM && !isMargaretRoomUnlocked()) {
        textPanel.show("MARGARET'S ROOM\n\nThe door is sealed shut. Not locked -- sealed. " +
            "The wood is unnaturally cold. Frost traces the edges of the frame despite the warm hallway.\n\n" +
            "The narrator speaks quickly: \"Nothing of interest in there. Margaret's room is irrelevant " +
            "to the investigation.\"\n\n" +
            "The narrator is lying. Whatever is behind this door, the Entity doesn't want you to find it.\n\n" +
            "[The Entity's grip may weaken if you investigate deeper into the manor.]");
        panelMode = PanelMode.TEXT;
        return;
    }

    roomManager.navigateTo(target);
    ...
}
```

**Gate condition methods (add as private methods in GameScreen):**
```java
private boolean isCellarUnlocked() {
    return gameState.getWatchedTapes().size() >= 4
        && gameState.getCollectedEvidence().size() >= 5;
}

private boolean isMargaretRoomUnlocked() {
    return gameState.hasAnomaly(EntityAnomaly.BREATHING_WALL);
}
```

**Why these thresholds:**
- Cellar: 4 watched tapes + 5 evidence ensures the player has engaged substantially with the murder mystery. There are 6 tapes and 8 evidence items available in Act 1, so 4+5 means they've explored most of the ground floor and talked to suspects.
- Margaret's Room: BREATHING_WALL anomaly (cellar wine_rack 2nd exam) means the player has been to the cellar and discovered the Entity's presence. The Entity pulls its power back to protect itself, releasing Margaret's door.

**Important — also gate keyboard navigation:**
The `navigateByDirection()` method (line 330) calls `handleNavigation()`, so it's already covered.

**Important — also gate save/load room restore:**
Lines 809 and 1471 call `roomManager.navigateTo()` directly during save/load. If a save was made in the cellar or Margaret's room, loading it should still work. These calls bypass handleNavigation so they won't be blocked. This is correct — if you saved inside a gated room, you should be able to load back into it.

#### 2b. Tape 8 damaged check in playTape()

**Current code (line 831):**
```java
private void playTape(Tape tape) {
    evidenceSystem.watchTape(tape);
    ...
}
```

**New code — add damage check at the START of playTape():**
```java
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
    ...
}
```

**Impact:** Player can COLLECT Tape 8 from the cellar wine rack (it goes into inventory) but cannot WATCH it until they find the repair kit in Margaret's Room.

#### 2c. Narrator blocked-door messages in objectives/hints

**In the objectives panel (around line 1370-1400):** Consider adding contextual hints:
- If cellar is locked: "* The cellar door resists you. Keep investigating."
- If Margaret's room is locked: "* Something seals Margaret's door. Look deeper."
- If Tape 8 is collected but not repairable: "* The tape from the cellar is damaged. Find recording equipment."

---

### 3. ExaminationSystem.java — Tape repair kit + Tape 8 collection message

#### 3a. Margaret's Room dresser — now also grants repair kit

**Current code (line 174-177):**
```java
case "dresser":
    if (count == 1) return new ExamResult(
        "Margaret's dresser. On the nightstand beside it, a tape recorder with a handwritten label: 'For the detective.'",
        Tape.TAPE_MARGARET_ACCOUNT);
```

**New code:**
```java
case "dresser":
    if (count == 1) {
        state.setHasTapeRepairKit(true);
        return new ExamResult(
            "Margaret's dresser. On the nightstand beside it, a tape recorder with a handwritten label: 'For the detective.'\n\n" +
            "Beside the recorder, a small leather case holds tape splicing tools -- scissors, adhesive strips, " +
            "a manual splicer. Margaret clearly prepared these recordings carefully.\n\n" +
            "[Tape repair kit acquired. You can now repair damaged tapes.]",
            Tape.TAPE_MARGARET_ACCOUNT);
    }
```

**Why here:** Margaret was actively recording (Tape 7 is her personal recording). She'd naturally have tape recording/repair equipment. The repair kit is a logical find alongside her tape.

#### 3b. Cellar wine_rack — Tape 8 collection message updated

**Current code (line 233-236):**
```java
case "wine_rack":
    if (count == 1) return new ExamResult(
        "Wine racks from floor to ceiling. Most bottles are dusty, but one section has been recently disturbed. Behind the bottles, taped to the wall: another tape recorder.",
        Tape.TAPE_ARTHUR_DEATH);
```

**New code:**
```java
case "wine_rack":
    if (count == 1) return new ExamResult(
        "Wine racks from floor to ceiling. Most bottles are dusty, but one section has been recently disturbed. Behind the bottles, taped to the wall: another tape recorder.\n\n" +
        "But this one is damaged. The casing is cracked, the tape ribbon inside snapped and tangled. " +
        "Someone — or something — tried to destroy this recording. " +
        "You'll need tape splicing equipment to repair it before you can listen.",
        Tape.TAPE_ARTHUR_DEATH);
```

**Impact:** The tape is still COLLECTED (added to inventory) but the description tells the player it's broken. When they try to watch it from inventory, the playTape() gate in GameScreen stops them with a message about needing repair equipment.

---

### 4. SaveLoadSystem.java — Persist hasTapeRepairKit

#### 4a. SaveData class — add field

**Add to SaveData (around line 52):**
```java
public boolean hasTapeRepairKit;
```

#### 4b. save() method — serialize the flag

**Add after line 109 (after wrongAccusationCount):**
```java
data.hasTapeRepairKit = state.hasTapeRepairKit();
```

#### 4c. load() method — deserialize the flag

**Add after line 202 (after chosenEnding):**
```java
state.setHasTapeRepairKit(data.hasTapeRepairKit);
```

**Impact on existing saves:** Old save files won't have `hasTapeRepairKit`. LibGDX Json deserialization defaults missing booleans to `false`, which is the correct default (player hasn't found the kit yet). No backwards compatibility issue.

---

### 5. HintSystem.java — Update hints for gated rooms

**What:** Add context-sensitive hints about locked rooms.

**Current behavior:** Hints tell players where evidence is, including Margaret's Room and Cellar. If these rooms are locked, the hints point to inaccessible locations without explanation.

**Proposed change:** In the hint text generation, check if rooms are locked and add context:
- If cellar is locked and player is missing BLOODSTAINED_CUFF: append "(cellar is currently sealed)"
- If Margaret's Room is locked and player is missing BLACKMAIL_NOTE: append "(Margaret's room is currently sealed)"

**Implementation:** The HintSystem doesn't currently have access to the watched tape count or anomaly state needed for the gate checks. Two options:
- **Option A:** Pass the gate-unlocked booleans into HintSystem from GameScreen
- **Option B:** Have HintSystem receive GameState and check the conditions directly

**Recommendation:** Option B — HintSystem already takes GameState. Add the same `isCellarUnlocked()` and `isMargaretRoomUnlocked()` logic as helper methods, or check the relevant state directly.

---

### 6. NarratorText.java — Blocked door narrator lines (optional enhancement)

**What:** Add specific narrator lines for when the player tries to access gated rooms.

**Current:** The blocked-door text is hardcoded in the GameScreen handleNavigation gate. This works but could be enhanced with variable narrator text based on awareness level.

**Possible enhancement:** Instead of static blocked-door messages, pull narrator text that varies by mood:
- HOPEFUL: "The cellar? I wouldn't bother. Nothing down there but dust and old wine."
- CONFUSED: "Something's wrong with that door. It won't open. Strange..."
- ANXIOUS: "Don't go down there. Please. I have a bad feeling about the cellar."
- FRANTIC: "NO. Stay away from the cellar. STAY AWAY."

**Impact:** Low priority. The static messages in GameScreen work fine. This is polish.

---

## Cross-Impact Analysis

### What DOESN'T change:
- Room.java, RoomManager.java — Room connections stay the same. The rooms still exist and are connected; they're just narratively blocked at the GameScreen level.
- Suspect.java, Evidence.java, Tape.java — Enums unchanged.
- InterviewSystem.java — All suspects are interviewable from Act 1. No change.
- AchievementSystem.java — Achievements check final state, not progression. No change.
- AwarenessSystem.java — Unchanged.
- EvidenceSystem.java — Unchanged. Evidence thresholds for confrontation/accusation still work because Act 1 provides enough evidence.
- NarratorSystem.java — Unchanged (distortions work regardless of act).
- TitleScreen.java — Unchanged.
- PlaceholderGenerator.java — Unchanged (generates art for all rooms including locked ones).

### What could break:
1. **Hotspots for locked rooms still render.** The Guest Rooms hallway shows Margaret's door hotspot, and the Kitchen shows the cellar stairs hotspot. Players will click them and get the blocked message. This is CORRECT behavior — they should see the door and learn it's blocked.

2. **Keyboard navigation to locked rooms.** Handled — `navigateByDirection()` calls `handleNavigation()` which has the gate checks.

3. **Save/load with player inside a gated room.** If a player saves while in the cellar (Act 2), then the save is loaded in a new game where Act 2 isn't reached yet — the `roomManager.navigateTo()` call in load bypasses handleNavigation, so they'll be placed in the room correctly. The gate only blocks NEW navigation, not restoring position. This is correct.

4. **Evidence count display (/10) while some items are inaccessible.** The UI still shows X/10. Players might wonder why they can't find the last 2 items until the rooms unlock. The hints system update (file 5) addresses this by marking sealed rooms.

5. **BREATHING_WALL anomaly gating Margaret's Room.** BREATHING_WALL is discovered on the cellar wine_rack 2nd exam. The 1st exam gives Tape 8 (damaged). So the player MUST examine the wine rack twice: first to get Tape 8, then to discover BREATHING_WALL and unlock Margaret's Room. This is correct sequencing.

6. **Completionist achievement.** Requires all evidence and all tapes watched. This is still achievable — all rooms eventually unlock. The achievement just requires completing Acts 1-3.

7. **Tape 8 climax trigger.** Currently, watching Tape 8 triggers the climax (line 861). With the repair kit gate, the climax can only trigger after: finding Tape 8 (cellar) → finding repair kit (Margaret's Room) → watching Tape 8. This naturally places the climax at the very end of Act 3.

### Evidence/Tape distribution by Act:

**Act 1 (8 rooms: Entrance, Study, Parlor, Kitchen, Guest Rooms, James's Room, Servants' Quarters, Shed):**
- Evidence: LETTER_OPENER, FIREPLACE_POKER, TORN_LETTER, FINANCIAL_RECORDS, WILL_COPY, SLEEPING_POWDER, MUDDY_BOOTS, GROUNDSKEEPER_LOG (8 items)
- Tapes: TAPE_ARGUMENT, TAPE_JAMES_INTERVIEW, TAPE_DANIEL_INTERVIEW, TAPE_MARGARET_INTERVIEW, TAPE_MARCUS_INTERVIEW, TAPE_CHARLES_INTERVIEW (6 tapes)

**Act 2 (Cellar unlocks):**
- Evidence: BLOODSTAINED_CUFF (1 item, total 9)
- Tapes: TAPE_ARTHUR_DEATH (1 tape, collected but damaged, total 7 collected)
- Anomalies: BREATHING_WALL (triggers Act 3)

**Act 3 (Margaret's Room unlocks):**
- Evidence: BLACKMAIL_NOTE (1 item, total 10)
- Tapes: TAPE_MARGARET_ACCOUNT (1 tape, total 8 collected) + repair kit
- After repair: can watch TAPE_ARTHUR_DEATH → climax

---

## Implementation Order

1. **GameState.java** — Add hasTapeRepairKit (foundation, needed by everything else)
2. **SaveLoadSystem.java** — Persist the new flag (ensures saves work)
3. **ExaminationSystem.java** — Update wine_rack (damaged tape message) and dresser (repair kit)
4. **GameScreen.java** — Add navigation gates + playTape gate + objective hints
5. **HintSystem.java** — Update hints for sealed rooms
6. **Test** — Verify all three acts flow correctly, saves work, no dead ends

---

## Story Concerns Addressed Previously (Fixes 1-3, 5)

These have already been implemented:

1. **Why Harold walled Thomas up** — EntityAnomaly CONSTRUCTION_RECORD expanded, ExaminationSystem study papers expanded, ClimaxContent explains Harold's motivation (Ashford threatened to expose him)
2. **Arthur Hollis seeded earlier** — Margaret's "brother" dialogue now mentions hiring "a man named Hollis. Arthur Hollis."
3. **Entity-murder thematic bridge** — ClimaxContent now draws the parallel: "Harold sealed a man behind a wall. His own son sealed Harold in a cellar."
5. **Charles timeline hole** — CHARLES_BLOODSTAINED_CUFF evidence reaction explains he was buried in documents with door shut

## Still Pending

4. **Narrator identity/motivation** — To be tackled separately. The narrator is a past dead investigator (NOT Arthur, NOT the Entity). Needs its own design session.
6. **Three-act structure + narrator channeling** — The plans described below.

---
---

# Narrator Channeling Mechanic

## Overview

The suspects are no longer physically present at Vance Manor — the events happened decades ago. When the player "interviews" a suspect, the narrator channels the memory of that conversation. The narrator doesn't understand how or why they can do this. They're confused and disturbed by the ability.

This serves two purposes:
1. **Solves the timeline problem** — Suspects don't need to be alive/present
2. **Seeds the twist** — The narrator can channel suspects because they ARE a dead investigator who interviewed them. But this is never stated. The narrator is simply confused. The player pieces it together.

**Key rule:** The narrator must NEVER say "I interviewed them" or "I was there." They only express confusion about the inexplicable ability.

---

## Files That Need Changes

### 7. NarratorText.java — Channeling dialogue arrays

**What:** Add new mood-indexed dialogue arrays for the channeling mechanic.

#### 7a. First-ever channeling intro (one per mood)

Plays the very first time the player starts ANY interview. Checked via `gameState.getInterviewCount() == 0` before increment.

```java
private static final String[] CHANNELING_FIRST_INTRO = {
    // HOPEFUL
    "[The narrator concentrates.]\n\n" +
    "\"I can... hear them. I don't know how. But their words are forming in my mind. Listen.\"\n\n" +
    "[The voice shifts.]",
    // CONFUSED
    "\"Something strange is happening. I can feel what they said. It's like I was there.\"\n\n" +
    "[The voice changes.]",
    // ANXIOUS
    "\"The voices are getting louder. I can almost see the room. Why can I do this?\"\n\n" +
    "[The voice warps into someone else's.]",
    // FRANTIC
    "\"THEY'RE IN MY HEAD! All of them! Let me try to focus on one--\"\n\n" +
    "[Static. Then a different voice.]"
};
```

#### 7b. Return channeling intro (subsequent interviews)

```java
private static final String[] CHANNELING_RETURN_INTRO = {
    // HOPEFUL
    "[The narrator reaches for the voice again.]",
    // CONFUSED
    "\"Them again... let me focus.\"\n\n[The voice shifts.]",
    // ANXIOUS
    "\"I don't want to do this. But I can't stop it.\"\n\n[The voice strains into shape.]",
    // FRANTIC
    "[The narrator's voice vanishes. Another takes its place.]"
};
```

#### 7c. Bleed-through lines (random during interview)

```java
private static final String[] CHANNELING_BLEED_THROUGH = {
    "[The narrator's own voice breaks through: \"How do I know what they said? How can I possibly know this?\"]",
    "[The narrator surfaces: \"This feels familiar. Like I've heard these words before. But that's impossible.\"]",
    "[The narrator's voice returns briefly: \"Why does it feel like I'm remembering this instead of hearing it?\"]",
    "[The narrator slips through: \"For a moment I could see the room. Smell the whisky. How?\"]"
};
```

#### 7d. Memory fade (replaces low-cooperation refusal)

```java
public static final String CHANNELING_MEMORY_FADE =
    "[The voice fades to static.]\n\n" +
    "\"I'm losing them. The words won't hold. I can't reach any further.\"";
```

#### 7e. Channeling end (when interview ends)

```java
private static final String[] CHANNELING_END = {
    // HOPEFUL
    "[The voice fades.] \"Gone. I don't understand how I could hear them.\"",
    // CONFUSED
    "[Silence.] \"...What just happened to me?\"",
    // ANXIOUS
    "[The narrator gasps.] \"It's like waking from someone else's dream.\"",
    // FRANTIC
    "[A violent snap.] \"I don't know where they end and I begin.\""
};
```

#### 7f. Accessor methods

```java
public static String getChannelingFirstIntro(Mood mood) { return CHANNELING_FIRST_INTRO[mood.ordinal()]; }
public static String getChannelingReturnIntro(Mood mood) { return CHANNELING_RETURN_INTRO[mood.ordinal()]; }
public static String getChannelingBleedThrough() { /* random selection */ }
public static String getChannelingEnd(Mood mood) { return CHANNELING_END[mood.ordinal()]; }
public static String getChannelingMemoryFade() { return CHANNELING_MEMORY_FADE; }
```

---

### 8. NarratorSystem.java — Channeling helper methods

**What:** Add methods that GameScreen calls to get channeling text.

```java
/** Returns channeling intro based on whether this is the first interview. */
public String getChannelingIntro(boolean isFirstInterview) {
    Mood mood = getCurrentMood();
    if (isFirstInterview) {
        return NarratorText.getChannelingFirstIntro(mood);
    }
    return NarratorText.getChannelingReturnIntro(mood);
}

/** 20% chance of bleed-through. Returns null if no bleed. */
public String maybeGetChannelingBleedThrough() {
    if (random.nextInt(5) != 0) return null;
    return NarratorText.getChannelingBleedThrough();
}

/** Returns channeling end text for current mood. */
public String getChannelingEnd() {
    return NarratorText.getChannelingEnd(getCurrentMood());
}
```

---

### 9. SuspectDialogue.java — Past tense conversion

**What:** Convert all narration text from present tense to past tense. Dialogue (quoted text) stays unchanged — it's what the suspect said.

**Narration** = the descriptive text around quotes. Example:
- Before: `James swirls his drink. "Dinner was at seven."`
- After: `James swirled his drink. "Dinner was at seven."`

**Scope of changes:**

| Section | Count | Example change |
|---------|-------|----------------|
| Greetings | 5 | "sits" → "sat", "stands" → "stood", "leans" → "leaned" |
| Topic responses | ~25 | "swirls" → "swirled", "pauses" → "paused", "shrugs" → "shrugged" |
| Evidence reactions | ~25 | "stares" → "stared", "goes pale" → "went pale" |
| False responses | ~25 | "sighs" → "sighed", "lowers" → "lowered" |
| Margaret deflections | 4 | "lowers" → "lowered", "leans" → "leaned" |
| Accusation defenses | 5 | "rises" → "rose", "stares" → "stared" |

**What does NOT change:**
- Quoted dialogue ("Detective. I suppose you have questions." stays exactly the same)
- Internal narrator commentary text (the narrator's own observations added by NarratorSystem)
- The content/meaning of any response

**Specific greeting rewrites:**

```java
// JAMES: "sits" → "sat", "looks up" → "looked up", "gestures" → "gestured"
"James Vance sat in an armchair, a glass of whisky in hand. He looked up as you approached. \"Detective. I suppose you have questions.\" He gestured to the chair across from him. \"I'll help if I can. Father and I had our differences, but I want to know what happened as much as anyone.\""

// MARGARET: "stands" → "stood", "looks like" → "looked like"
"Margaret stood by the window, arms crossed. She looked like she hadn't slept. \"Detective. I... I want to help. I do. Ask me what you need.\""

// DANIEL: "leans" → "leaned"
"Daniel the groundskeeper leaned against his workbench, dirt under his fingernails. \"I already told the police everything I know. But go ahead.\""

// MARCUS: "sits" → "sat", "looks up" → "looked up"
"Marcus Blackwood sat stiffly in a guest chair, his expensive suit at odds with the manor's faded elegance. He looked up with guarded eyes. \"Detective. I assume you've heard about the lawsuit. Yes, Harold and I had... differences. But I'm a businessman, not a killer.\""

// CHARLES: "straightens" → "straightened", "looks" → "looked"
"Charles Webb straightened his tie nervously. Harold's assistant from the company -- young, around twenty-eight -- looked genuinely grief-stricken. \"Detective, please -- I want to help. Mr. Vance trusted me with everything. I owe it to him to see this through.\""
```

**Pattern for all other text:** Mechanically change narration verbs to past tense. No content changes.

---

### 10. InterviewSystem.java — Memory fade at low cooperation

**What:** Replace suspect refusal text with channeling memory-fade text.

#### 10a. Cooperation < 10 refusal (line 50)

**Current:**
```java
return s.getDisplayName() + " refuses to even look at you. \"Get out. Now. Before I call the authorities myself.\"";
```

**New:**
```java
return NarratorText.getChannelingMemoryFade();
```

#### 10b. Cooperation < 25 refusal (line 59)

**Current:**
```java
return s.getDisplayName() + " snaps: \"I have nothing more to say to you!\" They turn away sharply.";
```

**New:**
```java
return "[The voice wavers, breaking apart.]\n\n\"The memory is fragmenting. I can barely hold onto what they said... something about...\" \n\n[Static overwhelms the voice.]";
```

**Note:** False responses (lies) at low cooperation STAY AS-IS. Those are what the narrator remembers the suspect actually saying — deliberately false answers. The memory-fade only replaces the generic refusal text.

#### 10c. Cooperation-based narration prefixes (lines 70-77)

**Current:**
```java
if (coop < 40) return s.getDisplayName() + " answers reluctantly, clearly agitated. " + response;
if (coop < 60) return s.getDisplayName() + " hesitates before answering. " + response;
```

**New (past tense):**
```java
if (coop < 40) return s.getDisplayName() + " answered reluctantly, clearly agitated. " + response;
if (coop < 60) return s.getDisplayName() + " hesitated before answering. " + response;
```

---

### 11. GameScreen.java — Channeling frame integration

#### 11a. startInterview() — Prepend channeling intro

**Current (line 871-898):**
```java
private void startInterview(Suspect suspect) {
    String greeting = interviewSystem.startInterview(suspect);
    ...
    StringBuilder sb = new StringBuilder();
    sb.append(narratorSystem.filterText(greeting));
    ...
}
```

**New:**
```java
private void startInterview(Suspect suspect) {
    boolean isFirst = gameState.getInterviewCount() == 0;
    String greeting = interviewSystem.startInterview(suspect);
    ...
    StringBuilder sb = new StringBuilder();
    sb.append(narratorSystem.getChannelingIntro(isFirst));
    sb.append("\n\n");
    sb.append(greeting); // Don't use filterText — channeling replaces the normal prefix
    ...
}
```

**Why skip filterText:** The normal narrator commentary prefix ("You note with interest:", "With trembling hands:") doesn't fit the channeling context. The channeling intro IS the prefix. The raw greeting plays as the channeled memory.

#### 11b. handleAskTopic() — Bleed-through chance

**Current (line 953-979):**
```java
private void handleAskTopic(String topic) {
    ...
    String response = interviewSystem.askTopic(topic);
    ...
    sb.append(response);
    ...
}
```

**New — after appending response:**
```java
    sb.append(response);
    // Random narrator bleed-through during channeling
    String bleed = narratorSystem.maybeGetChannelingBleedThrough();
    if (bleed != null) {
        sb.append("\n\n").append(bleed);
    }
```

**Same bleed-through addition for:**
- `showEvidenceReaction()` (line 1000-1024) — after evidence reaction text
- `handleWeaponContradiction()` (line 1027-1041) — after contradiction text
- `handleBodyContradiction()` (line 1044-1058) — after contradiction text
- `handleConfrontation()` (line 1063-1078) — after confrontation text

#### 11c. end_interview action — Show channeling end text

**Current (line 729-732):**
```java
if ("end_interview".equals(action)) {
    interviewSystem.endInterview();
    textPanel.hide();
    panelMode = PanelMode.NONE;
}
```

**New:**
```java
if ("end_interview".equals(action)) {
    interviewSystem.endInterview();
    String endText = narratorSystem.getChannelingEnd();
    textPanel.show(endText);
    panelMode = PanelMode.TEXT; // Dismissing this text returns to NONE via normal flow
}
```

**Impact:** Instead of silently closing, the interview ends with a narrator moment. The player dismisses it like any text panel, returning to normal gameplay.

---

## Channeling Cross-Impact Analysis

### What DOESN'T change:
- **Suspect.java** — Enum unchanged. Suspects still exist as data.
- **Evidence reactions content** — Only verb tense changes. No story/logic changes.
- **Confrontation logic** — InterviewSystem confrontation mechanics unchanged.
- **Cooperation math** — Same system, just different flavor text at low values.
- **Topic availability** — Same topics, same unlock conditions.
- **Accusation system** — Works the same. Past tense doesn't affect the flow.
- **Tape content** — Tapes are separate from interviews. They're physical recordings, not channeled.

### What could break:
1. **Narrator filterText on interview text.** Currently `startInterview()` passes the greeting through `narratorSystem.filterText()` which adds mood prefix AND may add distortions. With channeling, we skip the prefix but still want distortions to fire. **Solution:** Either call `maybeGetDistortion()` separately after the greeting, or keep filterText but disable the prefix for interviews.

2. **The generic close handler (line 649).** When a TEXT panel is dismissed, it checks if an interview is active and ends it. The channeling end text is shown as a TEXT panel. When THAT is dismissed, the interview is already ended (we called `endInterview()` before showing the end text). So the generic handler will see `isInterviewActive() == false` and skip the interview cleanup. **This is correct.**

3. **Narrator distortions during interviews.** Currently distortions fire through `filterText()`. If we skip `filterText()` for channeled greetings, distortions won't fire on the greeting. They CAN still fire on topic responses if we add distortion checks there. **Recommendation:** Add `maybeGetDistortion()` call in `handleAskTopic()` alongside the bleed-through, so distortions still appear during interviews.

4. **Past tense conversion consistency.** With ~90 text entries to convert, there's risk of missing some or inconsistent tense. **Recommendation:** Do a systematic pass through each section of SuspectDialogue, converting all narration verbs. Use search for common present-tense verbs: "looks", "sits", "stands", "pauses", "turns", "stares", "goes", "takes", "leans", "shrugs", "swirls", "reaches".

5. **The "lie received" text at low cooperation.** The `[suspect] turns back with a cold smile` text (line 47) needs past tense too: "turned back with a cold smile."

6. **Cooperation label in topic list header.** Line 949 shows `"Interview: " + suspect.getDisplayName() + " (Cooperation: " + coop + "%)"`. This still makes sense in channeling context — cooperation represents how clearly the narrator can recall that suspect. No change needed, but could optionally be relabeled to "Memory: X%" in a polish pass.

---

## Updated Implementation Order

1. **GameState.java** — Add hasTapeRepairKit
2. **SaveLoadSystem.java** — Persist the new flag
3. **NarratorText.java** — Add channeling dialogue arrays + act-transition blocked door lines
4. **NarratorSystem.java** — Add channeling helper methods
5. **SuspectDialogue.java** — Past tense conversion (all narration)
6. **InterviewSystem.java** — Memory fade at low cooperation + past tense narration prefixes
7. **ExaminationSystem.java** — Damaged tape + repair kit
8. **GameScreen.java** — Navigation gates + playTape gate + channeling frame + bleed-through + end text
9. **HintSystem.java** — Sealed room hints
10. **Test** — Full playthrough: interviews feel channeled, acts gate properly, saves work



THE UNRELIABLE NARRATOR — Full Game Analysis

  1. THE STORY

  Who Are You?

  **[OUTDATED FRAMING — see Correction Note at top of document]** You are a stranger who stumbled upon Vance Manor by accident,
  drawn by something supernatural you don't fully understand. The murder happened decades ago. Nobody hired you — you have no
  professional reason to be here. Arthur Hollis (the previous investigator hired by Margaret Vance) arrived on November 19th, 1987,
  just days after the murder. The narrator guides you through Arthur's hidden tape recordings; Arthur disappeared without a trace.

  ---
  The Murder: Full Truth

  Victim: Harold Vance, 75, patriarch and wealthy businessman
  Killers: James Vance (son, primary murderer) + Daniel Hobbs (groundskeeper, accomplice)

  Motive:
  - Harold discovered James had embezzled £50,000 from company accounts over two years
  - Harold was about to sign a new will at 9 AM, November 16th that would disinherit James entirely and leave everything to Margaret   
  - Harold also planned to dismiss Daniel, who was complicit in laundering the stolen money

  How it happened:
  1. James drugged Harold's evening tea with chloral hydrate (sleeping powder) to weaken him
  2. Around 10:45–11:00 PM, James struck Harold with the fireplace poker in the study — not the letter opener (which was planted as a  
  red herring)
  3. James and Daniel moved the body to the cellar. Margaret heard the dragging sounds at 2 AM
  4. Charles discovered the body the next morning and called the police

  The Full Night Timeline:

  ┌──────────────────┬───────────────────────────────────────────────────────────────────────┐
  │       Time       │                                 Event                                 │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ 7:00 PM          │ Dinner: family + Marcus Blackwood                                     │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ ~8:30 PM         │ Marcus and Harold move to parlor for settlement negotiations          │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ ~9:00 PM         │ Margaret retires to room; Charles finishes paperwork, retires         │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ ~9:30 PM         │ Harold-Marcus negotiations end; Marcus stays in parlor alone          │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ ~10:00 PM        │ Harold and James begin arguing in the study — Margaret hears shouting │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ ~10:45 PM        │ Charles sees James heading toward the study from his window           │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ ~10:45–11:00 PM  │ James murders Harold with the fireplace poker                         │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ ~11:00 PM        │ Marcus drives away; Charles hears his car                             │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ ~Midnight        │ Margaret hears two people whispering in the hallway (James + Daniel)  │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ ~2:00 AM         │ Margaret hears dragging sounds toward the cellar                      │
  ├──────────────────┼───────────────────────────────────────────────────────────────────────┤
  │ 7:00 AM (Nov 16) │ Charles finds the body; calls police                                  │
  └──────────────────┴───────────────────────────────────────────────────────────────────────┘

  ---
  The Five Suspects

  JAMES VANCE (Age 52, cooperation: 70) — GUILTY (murderer)
  - Son and heir. Embezzled £50,000 from company accounts.
  - Alibi to you: "room by nine, slept all night." Lie — he told police "went to sleep around 11:30."
  - Planted the blackmail note in Margaret's room to frame her.
  - Cannot move the body alone (bad shoulder) — needed Daniel.
  - When confronted: breaks down, admits conspiracy.

  MARGARET VANCE (Age 48, cooperation: 60) — INNOCENT
  - Daughter. Runs a successful textile import company she built herself.
  - Under the old (signed) will: gets nothing. Under Harold's new (unsigned) will: inherits everything.
  - Was targeted for framing by James.
  - Heard the argument, the footsteps, the dragging — but was too frightened to leave her room.
  - Has a train ticket showing she was planning to leave before the murder.

  DANIEL HOBBS (Age 63, cooperation: 50) — GUILTY (accomplice)
  - Groundskeeper, 15 years at the manor.
  - Laundered £20,000 for James. Harold discovered this and dismissed him.
  - Helped James move the body through the study window and down to the cellar.
  - His muddy boots (cellar dust + garden soil) and the missing logbook entry prove he lied about staying in his shed.
  - Physically could not have overpowered Harold alone (arthritis, age 63).

  MARCUS BLACKWOOD (Age 55, cooperation: 55) — INNOCENT
  - Business rival. £2M patent lawsuit with Harold.
  - Present that evening for settlement negotiations — Harold refused every offer.
  - Left at ~11 PM. Hotel confirmed check-in at 11:47 PM. Night clerk can verify.
  - Heard the James–Harold argument from the parlor at ~10 PM.
  - Harold's death actually hurts Marcus — the estate will pursue the lawsuit harder.

  CHARLES WEBB (Age 28, cooperation: 70) — INNOCENT
  - Harold's loyal assistant for five years. Genuinely grief-stricken.
  - Knew about the will changes — helped prepare the new will documents.
  - Key witness: saw James heading toward the study at 10:45 PM through his window.
  - Found the body with Daniel the next morning.
  - The most helpful suspect — actively tries to assist your investigation.

  ---
  The Deeper Horror: Thomas Ashford and the Entity

  In 1957, Harold's business partner Thomas Ashford threatened to expose something damaging about Harold. Rather than confront this,   
  Harold sealed Thomas alive behind a false wall in the cellar. Harold's own margin note reads: "He left me no choice. Ashford
  threatened to expose everything. This is the only way."

  Thomas spent 30 years behind that wall. What came out was no longer human.

  Private investigator Arthur Hollis arrived on November 19th, 1987 — four days after the murder. He found the sealed wall, spoke to   
  Thomas through it, and broke through. Tape 8 records what happened next. Arthur's screams end the recording.

  The Entity (what Thomas became) is now loose in the manor. The narrator guiding you is somehow entangled with it — the narrator's "I 
  slips" suggest they may be Arthur Hollis transformed, or the Entity itself speaking through the tapes.

  The Cycle: Harold buried Thomas. James buried Harold. The Vance family solves problems by burying them — and what you bury always    
  finds its way back.

  ---
  2. GAME STRUCTURE

  Act 1 — Investigation (Awareness 0–39)

  You explore the manor, examine objects, interview suspects, collect evidence and tapes, and discover the first anomalies. The        
  narrator is helpful (HOPEFUL/CONFUSED moods). Mild distortions begin around awareness 20.

  Available from the start: 8 rooms (Entrance, Study, Parlor, Kitchen, Guest Rooms, James's Room, Servants' Quarters, Shed).
  Cellar unlocks after 4 watched tapes + 5 evidence. Margaret's Room unlocks after the BREATHING_WALL anomaly.

  ---
  Act 2 — Escalation (Awareness 40–59)

  Narrator becomes ANXIOUS. Severe cooperation drops, suspect deflections (attempting to frame Margaret) become more frequent. Narrator
   distortions contradict evidence you've found. Atmospheric events increase. Rooms feel hostile.

  ---
  Act 3 — The Climax (Awareness 60–79)

  Triggered by playing Tape 8 (requires tape repair kit from Margaret's dresser). The narrator reveals the full truth of Thomas Ashford
   and the Entity. You must choose one of three endings. Narrator is FRANTIC — severe distortions trying to convince you Margaret is   
  guilty and you're going mad.

  Game over at 80 awareness: The household has completely closed ranks. Investigation ends in failure, no ending chosen.

  ---
  3. THE AWARENESS SYSTEM

  Awareness (0–80) represents how much the household suspects you and how psychologically compromised you're becoming from the manor's 
  horrors.

  How It Increases

  ┌───────────────────────────────────────────────────┬─────────┐
  │                      Action                       │  Cost   │
  ├───────────────────────────────────────────────────┼─────────┤
  │ Navigate to a room                                │ +1      │
  ├───────────────────────────────────────────────────┼─────────┤
  │ Examine an object                                 │ +1      │
  ├───────────────────────────────────────────────────┼─────────┤
  │ Ask a topic in interview (normal)                 │ +1      │
  ├───────────────────────────────────────────────────┼─────────┤
  │ Ask a dangerous topic (cellar/arthur/entity/wall) │ +5 total│
  ├───────────────────────────────────────────────────┼─────────┤
  │ Show evidence to a suspect                        │ +2      │
  ├───────────────────────────────────────────────────┼─────────┤
  │ Play a standard tape (1–7)                        │ +4      │
  ├───────────────────────────────────────────────────┼─────────┤
  │ Play Tape 8 (climax)                              │ +5      │
  ├───────────────────────────────────────────────────┼─────────┤
  │ Trigger weapon/body contradiction                 │ +2 each │
  ├───────────────────────────────────────────────────┼─────────┤
  │ Confront a suspect                                │ +3      │
  └───────────────────────────────────────────────────┴─────────┘

  The 4 Mood Thresholds

  ┌───────────┬──────────┬─────────────────────────────────────────────────────────────────────┐
  │ Awareness │   Mood   │                           Narrator Style                            │
  ├───────────┼──────────┼─────────────────────────────────────────────────────────────────────┤
  │ 0–19      │ HOPEFUL  │ Clear, helpful: "A promising lead — "                               │
  ├───────────┼──────────┼─────────────────────────────────────────────────────────────────────┤
  │ 20–39     │ CONFUSED │ Foggy, uncertain: "Something doesn't add up — "                     │
  ├───────────┼──────────┼─────────────────────────────────────────────────────────────────────┤
  │ 40–59     │ ANXIOUS  │ Disturbed: "God help you — " / "With trembling hands, you uncover:" │
  ├───────────┼──────────┼─────────────────────────────────────────────────────────────────────┤
  │ 60–79     │ FRANTIC  │ Panicked: "NO NO NO — " / "With the last of your composure:"        │
  ├───────────┼──────────┼─────────────────────────────────────────────────────────────────────┤
  │ 80        │ CRITICAL │ Game over — investigation fails                                     │
  └───────────┴──────────┴─────────────────────────────────────────────────────────────────────┘

  ---
  4. ROOM-BY-ROOM BREAKDOWN

  ENTRANCE HALL

  "The main entrance of Vance Manor. Dust motes float in dim light from a grand chandelier."

  Navigation: East → Study | West → Parlor | North → Kitchen | Up → Guest Rooms
  Hotspots: None

  ---
  THE STUDY (Crime scene)

  "Harold's desk faces large windows. A fireplace on the north wall still has warm ashes. Bookshelves line the east wall."
  (At awareness 40+: "Papers on the desk have been rearranged since your last visit. Someone has been here.")

  Navigation: West → Entrance

  DESK
  - 1st: "Harold's mahogany desk. Papers scattered. A silver letter opener catches your eye, positioned oddly far from the papers." →  
  Collects: LETTER_OPENER
  - 2nd+: "The desk. You've already found the letter opener."

  DRAWERS
  - 1st: "A hidden compartment in the bottom drawer holds a leather folder with financial records showing large, unexplained
  transfers." → Collects: FINANCIAL_RECORDS
  - 2nd+: "You've already found the hidden financial records."

  PAPERS
  - 1st: "Correspondence, bills, invitations. One letter from a solicitor mentions 'urgent changes to the estate documents.'"
  - 2nd: "An old construction invoice from 1957. A contractor paid to 'seal the lower cellar extension -- URGENT.' Harold's margin     
  note: 'He left me no choice. Ashford threatened to expose everything. This is the only way.' 1957 -- the same year Thomas Ashford    
  vanished." → Discovers: CONSTRUCTION_RECORD anomaly
  - 3rd+: "The same papers. The 1957 construction invoice stands out."

  BOOKSHELVES
  - 1st: "Your fingers find a small tape recorder wedged behind a volume of Shakespeare." → Collects: TAPE_JAMES_INTERVIEW
  - 2nd: "A photograph tucked behind the books. Harold stands with an unidentified man in front of the cellar door. The man's face has 
  been violently scratched out." → Discovers: PHOTO_UNKNOWN_MAN anomaly
  - 3rd+: "You already found the tape and the photograph."

  WINDOW
  - 1st: "The latch is unlocked -- unusual for a winter night. Fresh scuff marks on the windowsill, both inside and out."
  - 2nd+: "Boot prints in the frost on the outer sill. Whoever came through here was careful, moving at night when the household was   
  asleep. Someone with access to the grounds."

  FIREPLACE
  - 1st: "The fire has burned down to embers. Something was burned here recently -- not just firewood."
  - 2nd: "Letters carved into the inner wall: 'THOMAS WAS RIGHT.' No one in the household is named Thomas." → Discovers:
  THOMAS_REFERENCE anomaly
  - 3rd+: "The unusual ashes and the scratched name 'THOMAS' remain."

  FIREPLACE POKER (beside the fireplace)
  - 1st: "An iron poker rests beside the fireplace. A faint reddish-brown stain near the weighted end that doesn't match soot or rust."
  - 2nd: "The handle has been recently wiped down -- aggressively. But the weighted end still shows traces of blood caught in the      
  ornate metalwork. This is the murder weapon." → Collects: FIREPLACE_POKER
  - 3rd+: "The fireplace poker. The real murder weapon."

  FIREPLACE ASHES
  - 1st: "Fragments of a burned letter. The pieces might be reconstructed..." → Triggers: TORN_LETTER reconstruction minigame →        
  Collects: TORN_LETTER on completion
  - 2nd+: "You've already sifted through the ashes."

  UNDER THE DESK
  - 1st: "A small tape recorder taped to the underside." → Collects: TAPE_ARGUMENT
  - 2nd+: "Nothing else under the desk."

  Anomalies here: CONSTRUCTION_RECORD, PHOTO_UNKNOWN_MAN, THOMAS_REFERENCE

  ---
  THE PARLOR

  "Comfortable chairs face a dying fireplace. A grandfather clock ticks in the corner. A briefcase sits by one of the chairs."

  Navigation: East → Entrance

  GRANDFATHER CLOCK
  - 1st: "Opening the clock case, a tape recorder hidden behind the mechanism." → Collects: TAPE_CHARLES_INTERVIEW
  - 2nd+: "You already found the tape inside."

  BRIEFCASE
  - 1st: "Inside: legal documents, a copy of Harold's current will. James inherits everything. Margaret receives nothing. Beneath the  
  documents, a tape recorder." → Collects: WILL_COPY + TAPE_MARCUS_INTERVIEW
  - 2nd+: "You already have the will copy and found the tape."

  FIREPLACE
  - 1st: "Cold now. Half-burned paper, too damaged to read. Family photographs on the mantle -- Harold, James, and Margaret, looking   
  strained."
  - 2nd+: "The cold fireplace and strained family photos."

  ---
  THE KITCHEN

  "Large and industrial. Copper pots hang from racks. A narrow door leads to storage below."
  (At awareness 60+: "The kitchen has been cleaned -- suspiciously clean.")

  Navigation: South → Entrance | West → Servants' Quarters | Down → Cellar

  STORAGE CELLAR DOOR
  - 1st: "Beside it, a shelf holds tea supplies. Reaching behind a canister, a glass vial -- 'Chloral Hydrate' (sleeping powder).      
  Behind another canister, a tape recorder." → Collects: SLEEPING_POWDER + TAPE_MARGARET_INTERVIEW
  - 2nd: "The metal handle is ice-cold, far colder than it should be. Your breath mists briefly." → Discovers: COLD_SPOT_CELLAR anomaly
  - 3rd+: "The handle remains unnaturally cold."

  FLOUR TIN
  - 1st: "You check inside but find only flour. Fingerprints in the dust."
  - 2nd+: "Just flour inside."

  Anomalies here: COLD_SPOT_CELLAR

  ---
  GUEST ROOMS (corridor)

  "The guest wing upstairs. Margaret's room to the left, James's room to the right."

  Navigation: Down → Entrance | West → Margaret's Room | East → James's Room
  Hotspots: None

  ---
  JAMES'S ROOM

  "Messy. Unmade bed, overflowing ashtray. A half-finished whisky on the nightstand. Wardrobe door hanging open."

  Navigation: West → Guest Rooms

  COAT
  - 1st: "Checking the pockets, nothing. But the right sleeve -- the cuff is stained dark. Blood. Where is the shirt that went with    
  it?"
  - 2nd+: "The bloodstained cuff is damning, but a coat alone isn't enough evidence. Where did he hide the shirt?"

  WARDROBE
  - 1st: "Clothes hastily thrown about. Nothing suspicious except the coat on the door."
  - 2nd+: "The wardrobe. Messy."

  ---
  MARGARET'S ROOM

  (Requires: BREATHING_WALL anomaly discovered first)
  "Tidy but tense. A suitcase sits half-packed on the bed. A dresser against the wall."

  Navigation: East → Guest Rooms

  LETTER (on dresser)
  - 1st: "'I know what you did. Pay what you owe, or I tell everything.' No signature. The handwriting is rough -- not Margaret's      
  elegant script. Someone planted it here." → Collects: BLACKMAIL_NOTE
  - 2nd+: "The dresser where you found the blackmail note."

  DRESSER
  - 1st: "A tape recorder labeled 'For the detective.' Beside it, a small leather case with tape splicing tools." → Collects:
  TAPE_MARGARET_ACCOUNT + TAPE_REPAIR_KIT (required for Act 3)
  - 2nd: "A pair of shoes near the door with dark reddish-brown stains. A half-empty bottle of port wine on top. The stains... could   
  they be wine spills?" (They are — Margaret is innocent)
  - 3rd+: "The half-packed suitcase, the stained shoes, and the wine bottle. An ambiguous picture."

  ---
  GROUNDSKEEPER'S SHED

  "A small shed. Tools hang on the walls. Daniel's logbook sits open on a rough wooden desk."
  (At awareness 40+: "The shed seems recently disturbed. The logbook is now closed.")

  Navigation: East → Servants' Quarters

  LOGBOOK
  - 1st: "Each day meticulously noted -- except the murder night. That page has been torn out. A tape recorder is wedged between the   
  pages." → Collects: GROUNDSKEEPER_LOG + TAPE_DANIEL_INTERVIEW
  - 2nd+: "The torn-out page is still missing."

  SHELF
  - 1st: "Behind a can of paint thinner, a pair of worn work boots caked in fresh mud. Daniel claims he stayed in his shed all night." 
  - 2nd: "Fresh cellar dust mixed with garden soil. The same mud that would be on the grounds near the study window. Daniel went       
  outside that night." → Collects: MUDDY_BOOTS
  - 3rd+: "You already found the muddy boots."

  ---
  SERVANTS' QUARTERS

  "Simple but clean. A narrow bed with a worn wooden bedpost. A loose floorboard."
  (At awareness 60+: "The servants' quarters feel abandoned.")

  Navigation: East → Kitchen | West → Groundskeeper's Shed

  BEDPOST
  - 1st: "Deep scratches mar the wood near the base -- someone gripped this hard."
  - 2nd: "Near the base, deliberate letters carved into the wood: 'A.H.' Arthur Hollis -- the investigator who came before you. He was 
  never seen again." → Discovers: SCRATCHED_INITIALS anomaly
  - 3rd+: "The 'A.H.' initials carved into the wood."

  FLOORBOARD
  - 1st: "A loose floorboard. Prying it up: nothing but dust and old nails. But the hiding spot has been used -- scratch marks show    
  something was stored here recently and removed."
  - 2nd+: "The empty space. Whatever was hidden here is gone."

  Anomalies here: SCRATCHED_INITIALS

  ---
  THE CELLAR

  (Requires: 4 watched tapes + 5 collected evidence)
  "Dark and cold beneath the kitchen. Wine racks line the walls. Flour sacks in one corner. The air is damp and heavy."
  (At awareness 40+: "Fresh scratch marks on the floor. Something was dragged here recently.")

  Navigation: Up → Kitchen

  FLOUR SACKS
  - 1st: "The floor around them has been recently disturbed -- drag marks in the dust. Reaching behind: a shirt, hastily hidden. The   
  right cuff stained dark with blood. James must have panicked -- the fireplace was nearly dead by midnight." → Collects:
  BLOODSTAINED_CUFF
  - 2nd+: "You already found the bloodstained shirt."

  WINE RACK
  - 1st: "One section has been recently disturbed. Behind the bottles, taped to the wall: a tape recorder -- but it's damaged. Casing  
  cracked, tape ribbon snapped. You'll need splicing equipment to repair it." → Collects: TAPE_ARTHUR_DEATH (damaged, requires repair  
  kit)
  - 2nd: "You press your hand against the cellar wall behind the rack. It's warm. Not warm like stone -- warm like flesh. And beneath  
  your palm, a slow, rhythmic pulse. The wall is breathing." → Discovers: BREATHING_WALL anomaly (unlocks Margaret's Room)
  - 3rd+: "The wine rack. The wall behind it still pulses with that unnatural warmth."

  Anomalies here: BREATHING_WALL (unlocks Margaret's Room gate)

  ---
  5. THE SUSPECTS — COMPLETE INTERVIEW GUIDE

  JAMES VANCE

  Greeting: "Detective. I suppose you have questions... I'll help if I can. Father and I had our differences, but I want to know what  
  happened as much as anyone." (He doesn't meet your eyes.)

  Topics & Responses:

  Topic: last_night
  Response: "After dinner I went to the drawing room for a bit, then up to my room around nine. Read a book. Had a drink. Went to      
    sleep." (He didn't meet your eyes)
  ────────────────────────────────────────
  Topic: father
  Response: "My father was... a difficult man. Demanding. But he was my father. We had our disagreements. Every family does."
  ────────────────────────────────────────
  Topic: will
  Response: "What about the will? Father's affairs are private." (Goes very still)
  ────────────────────────────────────────
  Topic: daniel
  Response: "Daniel? He's the groundskeeper. Keeps to himself. I barely know the man." (Hand trembles)
  ────────────────────────────────────────
  Topic: whereabouts
  Response: "After dinner I was in the drawing room with Margaret for a bit. Then I went to my room around nine and stayed there all   
    night." (Rehearsed)

  Evidence Reactions:
  - LETTER_OPENER: "That's Father's letter opener. It's always on his desk. So what?"
  - FIREPLACE_POKER: "Where did you... I cleaned that. I cleaned it thoroughly." (Realizes what he admitted) "I mean -- that's just a  
  fireplace poker."
  - FINANCIAL_RECORDS: "Where did you get those? Those are private!" (Composure cracks)
  - WILL_COPY: "So you know about the will. Fine. Yes, I inherit. That doesn't mean I killed him!"
  - BLOODSTAINED_CUFF: (Long silence) "I... I cut myself. Shaving." (Shaking)
  - BLACKMAIL_NOTE: Forces a surprised expression, tries to implicate Margaret — this was his plant all along
  - SLEEPING_POWDER: "That's not mine. Maybe Daniel put it there." Too quick

  Margaret Deflection: "Have you looked into Margaret's finances? Ask her about the insurance policy she took out on Father last       
  month." (Lie)

  Accusation Defense: "You accuse ME? Without proof that I acted alone? I couldn't have moved the body by myself -- I have a bad       
  shoulder, you fool! And the window latch? I can barely reach it. You're looking for a conspiracy and you've only found half of it."  
  (Inadvertently confirms body was moved and someone entered through the window)

  ---
  MARGARET VANCE

  Greeting: "I... I want to help. I do. Ask me what you need." (Hasn't slept)

  Topics & Responses:

  Topic: last_night
  Response: "Dinner at seven. Afterwards, James and I sat in the drawing room for a bit. I went up to bed around nine... I heard       
    something later. Footsteps. But I thought it was just Father."
  ────────────────────────────────────────
  Topic: brother
  Response: "James and I aren't close. Not anymore... I heard him that night. Footsteps around midnight, whispering with someone. Then 
    dragging sounds at two in the morning... He contacted a man named Hollis. Arthur Hollis."
  ────────────────────────────────────────
  Topic: father
  Response: "Father could be cruel. He and James fought constantly about money. I heard them arguing around ten that night. Shouting   
    from the study. It went quiet after maybe fifteen minutes."
  ────────────────────────────────────────
  Topic: inheritance
  Response: "I didn't know about the will changes until Charles told me the day after Father died... I didn't realize James had already
   
    killed him to prevent those changes from being signed."
  ────────────────────────────────────────
  Topic: insurance
  Response: "An insurance policy? On Father? I never took out any policy. That's absurd."
  ────────────────────────────────────────
  Topic: company
  Response: "I run a textile import company. Started it eight years ago. Father never approved. But it's successful now. I built it    
    myself. I don't need his money or his approval. I never did."
  ────────────────────────────────────────
  Topic: sounds
  Response: "Footsteps around midnight. And later, maybe two in the morning, I heard something heavy being moved. Dragging sounds      
  toward
     the cellar. I stayed in my room, terrified."

  Evidence Reactions:
  - TORN_LETTER: Points to James's motive from the will changes
  - WILL_COPY: "This gives James a motive, not me. He was about to lose it all."
  - BLACKMAIL_NOTE: "This was in my room? I've never seen this before... James came to my room that night... did he plant this?"       

  Accusation Defense: Produces train ticket (was planning to leave). Holds up wine-stained shoes. "This is wine, detective. Not blood. 
  Taste it if you don't believe me."

  ---
  DANIEL HOBBS

  Greeting: "I already told the police everything I know. But go ahead." (Leaning against workbench)

  Topics & Responses:

  Topic: last_night
  Response: "I was in my shed most of the evening, doing repairs. Went to bed around eleven. Nothing unusual." (Eyes dart to logbook)  
  ────────────────────────────────────────
  Topic: harold
  Response: "Mr. Vance was fair enough. Paid on time." (Picks at dirt under his nails)
  ────────────────────────────────────────
  Topic: james
  Response: "James Vance is none of my business. We don't exactly move in the same circles." (Denial too forceful)
  ────────────────────────────────────────
  Topic: logbook
  Response: "It's just daily records. What I planted, what I trimmed." (Moves between you and the desk)
  ────────────────────────────────────────
  Topic: shed
  Response: "Tools, supplies. Sometimes I sleep here when it's late." (Glances at shelf)

  Evidence Reactions:
  - FIREPLACE_POKER: Too casual. "If there's blood on it, ask James. He was the one in the study that night."
  - GROUNDSKEEPER_LOG: "You went through my logbook? That's private! The missing page -- I spilled coffee on it."
  - MUDDY_BOOTS: "Mud happens. Those could be anyone's." (Sweating)
  - BLACKMAIL_NOTE: "That's supposed to be MY handwriting? Someone tried to frame me. Probably James."

  Margaret Deflection: "I saw Margaret coming out of the study at eleven that night. She was carrying something." (Lie)

  Accusation Defense: Shows gnarled arthritic hands. "I'm sixty-three with arthritis in both knees. I couldn't have done this alone,   
  and you know it. Someone else was there that night. Someone stronger."

  ---
  MARCUS BLACKWOOD

  Greeting: "I assume you've heard about the lawsuit. Yes, Harold and I had differences. But I'm a businessman, not a killer."
  (Guarded)

  Topics & Responses:

  Topic: lawsuit
  Response: "Two million pounds at stake. Harold was determined to crush my company. But corporate warfare is fought in courtrooms."   
  ────────────────────────────────────────
  Topic: settlement
  Response: "Harold refused every offer. Around half past eight, he excused himself and left me sitting in the parlor alone. I waited  
    two hours. He never came back."
  ────────────────────────────────────────
  Topic: that_night
  Response: "I was in the parlor from about half past eight until eleven. Around ten, I heard shouting from the study -- Harold and    
    James. I finally gave up and left at eleven. Charles's window was lit as I left."
  ────────────────────────────────────────
  Topic: harold
  Response: "Harold Vance was a ruthless man who built his fortune on stolen ideas. But I didn't kill him. I wanted to defeat him in   
    court -- publicly, humiliatingly."
  ────────────────────────────────────────
  Topic: alibi
  Response: "I left at eleven -- Charles Webb saw me drive away. Back at my hotel by midnight. Front desk logged me in."

  Evidence Reactions:
  - FINANCIAL_RECORDS: "Embezzlement? I didn't know about this. But it explains why James was so desperate."
  - WILL_COPY: "So James was being disinherited. That explains the shouting."

  Margaret Deflection: "I observed Miss Vance that evening. She seemed agitated. Pacing in the hallway near the study around ten       
  o'clock." (Lie)

  Accusation Defense: "I had motive, yes. But I was GONE by eleven. Why would I kill Harold before the lawsuit concluded? A dead man   
  can't drop charges. His death HURTS my position."

  ---
  CHARLES WEBB

  Greeting: "Detective, please -- I want to help. Mr. Vance trusted me with everything. I owe it to him to see this through."
  (Genuinely grief-stricken)

  Topics & Responses:

  Topic: harold
  Response: "Mr. Vance was demanding, yes. But he trusted me. Five years I worked for him." (Eyes well up)
  ────────────────────────────────────────
  Topic: will_changes
  Response: "I helped prepare the documents. The new will would have left James with nothing -- everything to Margaret. James didn't   
    know yet. Harold wanted to announce it formally."
  ────────────────────────────────────────
  Topic: james
  Response: "James was always volatile. Especially about money. Mr. Vance discovered something recently -- he was furious with James.  
    That's when he decided to change the will."
  ────────────────────────────────────────
  Topic: that_night
  Response: "I was in the study until nine helping with paperwork. In my room after that. Around quarter to eleven, I saw James heading
   
    toward the study from my window. He looked determined. Then at eleven, I heard Mr. Blackwood's car leaving."
  ────────────────────────────────────────
  Topic: company
  Response: "The lawsuit was consuming Mr. Vance. And something else was troubling him -- he kept reviewing the company books late at  
    night. He seemed devastated about something."

  Evidence Reactions:
  - FIREPLACE_POKER: "Blood on the weighted end? This matches the wound pattern from the autopsy. The letter opener was a plant. This  
  is the real murder weapon."
  - FINANCIAL_RECORDS: "Fifty thousand pounds... over two years. This is what Mr. Vance discovered. James was stealing from his own    
  father."
  - WILL_COPY: "I prepared that document myself. Mr. Vance was so angry when he made those changes."
  - SLEEPING_POWDER: "That explains why Mr. Vance complained of drowsiness the past week. Someone was drugging his evening tea. James  
  was administering it."
  - BLOODSTAINED_CUFF: "God. I was right above them and I didn't hear a thing."

  Margaret Deflection: "Margaret asked Mr. Vance about the will that afternoon. She was furious when she learned she was getting       
  everything." (Lie — all deflections are lies)

  Accusation Defense: "Me? After everything I told you? I was going to LOSE my job because of the will changes... Why would I kill the 
  man who trusted me with his secrets?"

  ---
  6. THE TAPES — COMPLETE SUMMARIES

  TAPE 1 — Harold & James Argument

  Location: Study, under desk | Cost: 4 awareness

  Harold confronts James about the £50,000 embezzlement. James tries to claim it was a loan. Harold announces he is changing the will  
  the next morning at 9 AM — Margaret inherits everything, James gets nothing. Harold also reveals he knows about Daniel's involvement 
  in laundering the money and that Daniel is dismissed. James is desperate. A door slams. The recording captures Harold's breathing for
   30 more seconds — he's just signed his death warrant.

  Key reveal: James's full motive. The timeline of the argument. Daniel's money-laundering role.

  ---
  TAPE 2 — James Vance Police Interview

  Location: Study, bookshelves | Cost: 4 awareness

  Detective Morrison interviews James on November 17th. James admits to a "discussion" with Harold around 10 PM about "business        
  matters." Denies it was a fight despite witnesses reporting shouting. Claims he went to his room and "went to sleep around
  eleven-thirty" (different story than the "around nine" he tells you). Deflects suspicion toward Margaret and Marcus. Refuses to      
  answer further without a solicitor when confronted about the embezzlement.

  Key reveal: James had no alibi. James knew details he shouldn't. The police already know about the embezzlement.

  ---
  TAPE 3 — Daniel Hobbs Police Interview

  Location: Groundskeeper's Shed, logbook | Cost: 4 awareness

  Daniel claims the logbook missing entry was just forgot. Admits to £20,000 in bank deposits — claims it's "side work." Gets flustered
   when pressed about dragging sounds at 2 AM. Nearly admits: "I was helping move— I mean, move furniture. I was helping move furniture
   earlier that day." Corrects to "a desk." His alibi collapses under scrutiny.

  Key reveal: Daniel was present the night of the murder. He nearly confesses. The deposits tie him to James's money.

  ---
  TAPE 4 — Margaret Vance Police Interview

  Location: Kitchen, storage cellar door | Cost: 4 awareness

  Margaret describes hearing the argument at 10 PM, two people whispering at midnight (one sounded like James), and dragging sounds at 
  2 AM toward the cellar. Describes James's hollow, exhausted look the next morning and Daniel refusing to meet anyone's eyes. Quietly 
  admits she suspects James — "I don't want to believe it. But... yes. God help me, yes."

  Key reveal: Margaret is a credible witness to the entire crime. Her account corroborates the timeline.

  ---
  TAPE 5 — Marcus Blackwood Police Interview

  Location: Parlor, briefcase | Cost: 4 awareness

  Marcus confirms the failed negotiation, that Harold left around 8:30 and never returned. Heard James and Harold arguing around 10 PM 
  — heard "money" and "betrayal." Left at 11 PM. Hotel confirmed check-in at 11:47 PM. "I drove fast. I was irritated."

  Key reveal: Marcus corroborates the argument timeline. His alibi is rock solid.

  ---
  TAPE 6 — Charles Webb Police Interview

  Location: Parlor, grandfather clock | Cost: 4 awareness

  Charles confirms preparing the new will documents for a 9 AM signing the next morning. Describes seeing James heading toward the     
  study at 10:45 PM through his window — "Absolutely certain. The exterior lamps were on. I could see his face clearly. He looked      
  determined. Almost grim." Worked until 1 AM, found the body with Daniel the next morning.

  Key reveal: The most precise witness — puts James at the study at 10:45 PM, within minutes of the murder.

  ---
  TAPE 7 — Margaret's Personal Account

  Location: Margaret's Room, dresser | Cost: 4 awareness

  Margaret recorded this specifically for Arthur Hollis (the previous investigator) because she didn't trust the police. More emotional
   and detailed than her police interview. Describes dinner, the 10 PM argument, footsteps at midnight, the dragging sounds at 2 AM.   
  Describes James the next morning: "Exhausted. Hollow-eyed. Like he hadn't slept at all. His hands were shaking." Asks Arthur to      
  investigate properly.

  Key reveal: Margaret hired Arthur Hollis. She's innocent and desperate for justice.

  ---
  TAPE 8 — The Opening (Arthur Death Recording)

  Location: Cellar, wine rack (DAMAGED — requires tape repair kit) | Cost: 5 awareness

  November 19th, 1987. Arthur Hollis is documenting the false wall. A voice calls from inside — "My name is Thomas. Thomas Ashford."
  Thomas sounds desperate, human, confused about how long he's been inside. Arthur tells him Harold is dead — 30 years have passed     
  since 1957. Thomas begs Arthur not to call police first, just to let him out. Arthur breaks through the wall.

  What emerges is not Thomas Ashford.

  "You're not human anymore."
  "I WAS human. Harold made me into THIS."

  Arthur drops the recorder. Footsteps running. Then Arthur screaming. Then silence. Then breathing that isn't human. Then static.     

  Key reveal: Everything about the Entity. Arthur's fate. The horror layer of the entire game.

  ---
  7. THE NARRATOR SYSTEM

  The Narrator Header (first time you find a tape)

  "Ah, you've found Arthur's recordings. Good. I've been waiting for someone who would listen. My name doesn't matter — what matters is
   what happened in this house. I'll guide you as best I can, but understand: the longer you stay, the more THEY notice. The walls have
   ears here. Literally.
  Trust what you see. Question what you're told. And whatever you do... don't let them know how much you know."

  Mood Prefixes (prepended to every examination result)

  - HOPEFUL: "A promising lead — " / "You note with interest:"
  - CONFUSED: "Something doesn't add up — " / "Through the fog of exhaustion:"
  - ANXIOUS: "God help you — " / "With trembling hands, you uncover:"
  - FRANTIC: "NO NO NO — " / "With the last of your composure:"

  Distortions → Contradictions

  ┌───────────────────────────────────────────────────────────┬───────────────────────────────────────────────┬─────────────────┐      
  │                      Mild Distortion                      │                Contradicted by                │     Unlocks     │      
  ├───────────────────────────────────────────────────────────┼───────────────────────────────────────────────┼─────────────────┤      
  │ "The letter opener was clearly the murder weapon"         │ FIREPLACE_POKER                               │ NARRATOR_WEAPON │      
  ├───────────────────────────────────────────────────────────┼───────────────────────────────────────────────┼─────────────────┤      
  │ "No one left the house. The boots are irrelevant."        │ MUDDY_BOOTS                                   │ NARRATOR_BOOTS  │      
  ├───────────────────────────────────────────────────────────┼───────────────────────────────────────────────┼─────────────────┤      
  │ "That torn letter? Just old correspondence."              │ TORN_LETTER                                   │ NARRATOR_LETTER │      
  ├───────────────────────────────────────────────────────────┼───────────────────────────────────────────────┼─────────────────┤      
  │ "Harold died after midnight."                             │ Charles's 10:45 PM testimony                  │ NARRATOR_TIME   │      
  ├───────────────────────────────────────────────────────────┼───────────────────────────────────────────────┼─────────────────┤      
  │ "Margaret was seen near the study. Witnesses confirm it." │ (No contradicting evidence — atmosphere only) │ —               │      
  ├───────────────────────────────────────────────────────────┼───────────────────────────────────────────────┼─────────────────┤      
  │ "The cellar has always been sealed."                      │ (No contradicting evidence — atmosphere only) │ —               │      
  └───────────────────────────────────────────────────────────┴───────────────────────────────────────────────┴─────────────────┘      

  Severe distortions at 60+ awareness try to convince you Margaret is guilty, the tapes are lying, that you're imagining the horror,   
  that everyone is trying to help you.

  The Narrator Contradictions (All 6)

  ┌─────┬─────────────────┬──────────────────────────────────────────────────────────────────────────────────────────┐
  │  #  │  Contradiction  │                                       Description                                        │
  ├─────┼─────────────────┼──────────────────────────────────────────────────────────────────────────────────────────┤
  │ 1   │ WEAPON          │ Letter opener doesn't match wound; fireplace poker does                                  │
  ├─────┼─────────────────┼──────────────────────────────────────────────────────────────────────────────────────────┤
  │ 2   │ BODY_POSITION   │ Harold died in the study, was found in the cellar — body moved by two people             │
  ├─────┼─────────────────┼──────────────────────────────────────────────────────────────────────────────────────────┤
  │ 3   │ NARRATOR_WEAPON │ Narrator claimed letter opener was murder weapon — wrong                                 │
  ├─────┼─────────────────┼──────────────────────────────────────────────────────────────────────────────────────────┤
  │ 4   │ NARRATOR_BOOTS  │ Narrator said no one left the house — muddy boots prove otherwise                        │
  ├─────┼─────────────────┼──────────────────────────────────────────────────────────────────────────────────────────┤
  │ 5   │ NARRATOR_LETTER │ Narrator called torn letter unimportant — it reveals the will motive                     │
  ├─────┼─────────────────┼──────────────────────────────────────────────────────────────────────────────────────────┤
  │ 6   │ NARRATOR_TIME   │ Narrator said Harold died after midnight — Charles places James at the study at 10:45 PM │
  └─────┴─────────────────┴──────────────────────────────────────────────────────────────────────────────────────────┘

  Channeling Mechanics

  During interviews the narrator "channels" suspect memories — explaining how they know what was said. As awareness rises, channeling  
  becomes unstable. Bleed-through lines reveal the narrator's confusion:
  "How do I know what they said? How can I possibly know this?"
  "For a moment I could see the room. Smell the whisky. How?"

  The NARRATOR_I_SLIP Anomaly

  After 3+ anomalies discovered, there's a 20% chance per interaction the narrator reveals:
  "I remember when the wall was -- "
  (Long pause. Static. Then, carefully:)
  "...Forgive me. I misspoke. I meant to say: Arthur's notes mention that the wall was sealed decades ago. I have no personal memory of
   this place. Of course I don't. I'm merely a voice on a tape."

  ---
  8. ENTITY ANOMALIES — ALL 7

  Anomaly: BREATHING_WALL
  Location: Cellar, wine rack, 2nd exam
  Discovery: Wall is warm and pulses like flesh
  Significance: Entity is alive and aware; also unlocks Margaret's Room gate
  ────────────────────────────────────────
  Anomaly: THOMAS_REFERENCE
  Location: Study, fireplace, 2nd exam
  Discovery: "THOMAS WAS RIGHT" scratched inside fireplace
  Significance: Thomas tried to communicate before being sealed
  ────────────────────────────────────────
  Anomaly: NARRATOR_I_SLIP
  Location: Throughout gameplay (20% chance, 3+ anomalies)
  Discovery: Narrator says "I remember" then denies it
  Significance: Narrator has personal memories of the manor — are they Arthur? The Entity?
  ────────────────────────────────────────
  Anomaly: SCRATCHED_INITIALS
  Location: Servants' Quarters, bedpost, 2nd exam
  Discovery: "A.H." carved into wood
  Significance: Arthur Hollis was here; he didn't leave voluntarily
  ────────────────────────────────────────
  Anomaly: PHOTO_UNKNOWN_MAN
  Location: Study, bookshelves, 2nd exam
  Discovery: Photo of Harold + unknown man, face scratched out
  Significance: Thomas Ashford has been deliberately erased from history
  ────────────────────────────────────────
  Anomaly: COLD_SPOT_CELLAR
  Location: Kitchen, cellar door, 2nd exam
  Discovery: Ice-cold handle, breath fogs
  Significance: Entity exerts supernatural influence beyond the wall
  ────────────────────────────────────────
  Anomaly: CONSTRUCTION_RECORD
  Location: Study, papers, 2nd exam
  Discovery: 1957 invoice to seal cellar — "He left me no choice"
  Significance: Harold's own handwriting admits guilt for Thomas's imprisonment

  ---
  9. ACCUSATION AND ENDINGS

  Accusing James & Daniel (correct solution)

  Minimum evidence required:
  - James: 3+ of {FINANCIAL_RECORDS, WILL_COPY, TORN_LETTER, BLACKMAIL_NOTE, TAPE_ARGUMENT, TAPE_JAMES_INTERVIEW}
  - Daniel: 2+ of {GROUNDSKEEPER_LOG, MUDDY_BOOTS, TAPE_DANIEL_INTERVIEW, TAPE_MARGARET_INTERVIEW}

  What happens: James breaks and admits conspiracy. Daniel admits moving the body. Both arrested.

  Wrong accusations: The accused defends themselves with logical arguments. Game doesn't end — you can continue. Each wrong accusation 
  raises awareness.

  ---
  The Climax (Playing Tape 8)

  After the tape ends, the narrator delivers the final revelation:

  "Harold Vance sealed his own business partner behind that wall in 1957. Ashford had threatened to expose him -- to ruin everything   
  Harold had built. So Harold buried him alive and built a fortune on the silence.
  Thirty years behind a wall. What do you think that does to a man? What comes out is not what went in.
  Harold sealed a man behind a wall. His own son sealed Harold in a cellar. The Vances don't solve their problems -- they bury them.   
  And what's buried always finds a way back.
  Solve the murder. Find the killers. But do it quietly. Because if the Entity fully wakes... none of us leave Vance Manor."

  ---
  The Three Endings

  SEAL THE WALL (Guardian achievement)
  You descend to the cellar and brick up the wall again. The Entity screams with pressure in your skull. Brick by brick you deny it    
  existence. When finished, the cellar is just a cellar again.
  "Harold's murder will go officially unsolved. James and Daniel will live with their guilt, but they'll live. And the Entity will     
  sleep -- for now. You chose to be the guardian of a terrible secret."

  ESCAPE THE MANOR (Survivor achievement)
  You run. Every light in Vance Manor goes out simultaneously. You make it to your car.
  "You survived. Harold's killers go free. The Entity remains. And somewhere in your notes, enough fragments of truth remain that      
  someday, someone else will come asking questions. You escaped with your life. You'll never escape the knowledge of what's behind that
   wall."

  DESTROY THE TAPES (Arsonist achievement)
  One by one you burn all 8 tapes in the study fireplace. With each tape destroyed the house grows quieter, shadows recede. The last   
  tape (Tape 8) fights you — the recorder falls from your hands twice. But you persist.
  "Without the tapes, there is no evidence of the Entity. Without evidence, there is no investigation. Without investigation, there is 
  no awareness. The cycle is broken. Harold's murder might still be solved through conventional means. But the deeper horror of Vance  
  Manor dies with these tapes. You made sure of that."

  ---
  10. ACHIEVEMENTS — ALL 8

  ┌───────────────────────┬───────────────────────────────────────────────────┐
  │      Achievement      │                 Unlock Condition                  │
  ├───────────────────────┼───────────────────────────────────────────────────┤
  │ SPEEDRUN              │ Finish the case in ≤10 total commands             │
  ├───────────────────────┼───────────────────────────────────────────────────┤
  │ GHOST                 │ Finish with awareness < 25                        │
  ├───────────────────────┼───────────────────────────────────────────────────┤
  │ COMPLETIONIST         │ Collect all 10 evidence items + watch all 8 tapes │
  ├───────────────────────┼───────────────────────────────────────────────────┤
  │ PERFECT INVESTIGATION │ All 10 evidence + all 8 tapes + awareness < 30    │
  ├───────────────────────┼───────────────────────────────────────────────────┤
  │ GUARDIAN              │ Choose the Seal the Wall ending                   │
  ├───────────────────────┼───────────────────────────────────────────────────┤
  │ ARSONIST              │ Choose the Destroy the Tapes ending               │
  ├───────────────────────┼───────────────────────────────────────────────────┤
  │ SURVIVOR              │ Choose the Escape the Manor ending                │
  ├───────────────────────┼───────────────────────────────────────────────────┤
  │ CYCLE BREAKER         │ Discover all 7 anomalies + reach any ending       │
  └───────────────────────┴───────────────────────────────────────────────────┘

---

# README.md

# The Unreliable Narrator

A 2D horror murder mystery set in Vance Manor, built with LibGDX.

## About

You are an investigator called to Vance Manor to solve the murder of Harold Vance. Guided by a mysterious narrator -- a trapped soul channeling memories of the night in question -- you must collect evidence, interview suspects, and uncover contradictions to identify the killers.

But the manor is watching. Something ancient dwells within the walls, and every question you ask draws its attention closer. The narrator's guidance grows increasingly unreliable as awareness rises. Suspects lie. Evidence contradicts itself. And behind the cellar wall, something is breathing.

## Features

- **Three-act game structure** with navigation gates -- rooms unlock as you gather evidence and discover anomalies
- **Narrator channeling mechanic** -- a trapped soul replays suspect memories, confused about how they can hear the dead
- **7 interconnected narrative systems** creating a deeply unreliable, branching mystery
- **5 suspects** with cooperation-based dialogue, active lies, and deflections
- **10 evidence items** and **8 hidden tapes** scattered throughout the manor
- **Tape damage and repair** -- key evidence requires finding repair tools in a locked room
- **Entity Anomaly Discovery** -- 7 anomalies hidden in re-examinations hint at the deeper horror
- **Unreliable Narrator** -- distortions, contradictions, and "I remember" slips
- **Red Herring suspect arc** with circumstantial evidence and witness deflections
- **Wrong accusation consequences** with rich defense responses and state changes
- **3 moral endgame choices** -- Seal the Wall, Destroy the Tapes, or Escape the Manor
- **8 achievements** tracking investigation skill and ending choices
- **Dynamic awareness system** -- 4 escalating threat levels affecting narrator personality, room descriptions, and atmospheric events
- **Save/load system** with 3 slots preserving all narrative state

## How to Play

- **Navigate** by clicking room hotspots or using WASD/arrow keys
- **Examine** objects by clicking examine hotspots (re-examine for hidden discoveries)
- **Interview** suspects via the Suspects button (T key)
- **Show evidence** to suspects during interviews to provoke reactions
- **Watch tapes** from inventory (I key) for crucial testimony
- **Check your notebook** (N key) for contradictions, anomalies, and cooperation levels
- **Manage awareness** -- every action has a cost. At 80, it's game over

## Running

```
./gradlew lwjgl3:run
```

## Building

```
./gradlew lwjgl3:jar
```

The runnable jar will be at `lwjgl3/build/libs/`.

## Project Structure

- `core/` -- Main game logic (state, systems, data, screens, navigation, UI)
- `lwjgl3/` -- Desktop launcher (LWJGL3)
- `assets/` -- Game assets (room artwork, etc.)

---

# GAME_DOCUMENTATION.md

# The Unreliable Narrator - Complete Game Documentation

A comprehensive guide to the world, characters, lore, and mechanics of The Unreliable Narrator - a horror murder mystery game set in Vance Manor.

---

## Table of Contents

1. [Game Overview](#game-overview)
2. [The World & Setting](#the-world--setting)
3. [Timeline of Events](#timeline-of-events)
4. [Key Characters](#key-characters)
5. [The Suspects](#the-suspects)
6. [The Entity & Awareness System](#the-entity--awareness-system)
7. [Entity Anomaly Discovery](#entity-anomaly-discovery)
8. [Locations & Map](#locations--map)
9. [The Tapes](#the-tapes)
10. [Game Mechanics](#game-mechanics)
11. [The Solution](#the-solution)
12. [Moral Endgame Choices](#moral-endgame-choices)
13. [The True Horror](#the-true-horror)

---

# Game Overview

## What is DSA?

**The Unreliable Narrator** is a horror murder mystery game set in **Vance Manor**, a grand estate that holds dark secrets spanning decades. The story centers on the murder of Harold Vance on **November 15th, 1987**.

## Genre & Atmosphere

- **Genre:** Horror Murder Mystery (Text Adventure → LibGDX First-Person Navigation)
- **Tone:** Haunting, mysterious, psychological horror
- **Themes:** Betrayal, greed, legacy, entrapment, cycles of death

## The Premise

You are an investigator who has come to Vance Manor to solve the murder of Harold Vance. But this is no ordinary investigation. You are guided by a mysterious **Narrator** - a trapped soul who channels the memories of suspects, reliving the events of that fateful night in 1987.

As you dig deeper, you realize that the manor itself is watching. Something ancient and malevolent dwells within these walls - and every question you ask, every room you enter, draws its attention closer.

---

# The World & Setting

## Vance Manor

Vance Manor is a grand Victorian estate built in the late 1800s. The main house has two floors plus a cellar, with the Groundskeeper's Shed accessible from the servants' quarters.

**Architectural Style:** Victorian Gothic Revival
**Built:** 1890
**Location:** Rural estate, outside of town
**Current State (1987):** Well-maintained exterior, dark history within

## The Investigation

The game takes place in a liminal space between past and present. The player experiences the manor as it was in 1987, guided by memories channeled through the Narrator. The house itself is a character - watching, waiting, aware of your presence.

---

# Timeline of Events

## 1957 - The Founding & The Betrayal

- **Vance Pharmaceutical** is co-founded by **Harold Vance** and **Thomas Ashford**
- Thomas was "the real chemist" - the genius behind the formulas
- Harold was "just the money man" - the face of the company
- **Harold entombed Thomas ALIVE behind the cellar wall** to take full control
- Official records state Thomas "retired abroad" - but no one ever saw him leave

## 1957-1987 - Thirty Years of Darkness

- Thomas remains trapped behind the wall for 30 years
- Through decades of tortured existence, Thomas **becomes something inhuman**
- Thomas **fuses with the house itself**, becoming the Entity
- The manor becomes a place of dark energy and trapped souls

## November 15th, 1987 - The Murder

- **Harold Vance is murdered** in his study
- Multiple suspects were present at the manor that night
- **Marcus Blackwood** (rival CEO) attended a settlement dinner
- **Charles Webb** (Harold's assistant) was helping with paperwork
- **Charles** found the body the next morning and got Daniel
- The investigation begins

### Hour-by-Hour Timeline: November 15th-16th, 1987

**7:00 PM - Dinner in the Dining Room**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| Harold | Dining Room | Hosting settlement dinner, power play against Marcus | Everyone |
| James | Dining Room | Eating dinner, seething at father's condescension | Everyone |
| Margaret | Dining Room | Quietly observing the tension | Everyone |
| Daniel | Kitchen area | Serving, observing the family dynamics | Charles |
| Marcus | Dining Room | Enduring Harold's power games, hoping for settlement | Everyone |
| Charles | Dining Room | Assisting Harold, taking mental notes | Everyone |

**8:00 PM - Post-Dinner Dispersal**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| Harold | Parlor | Negotiating with Marcus over drinks | Marcus, Charles |
| James | Drawing Room | Sitting with Margaret, stewing about money | Margaret |
| Margaret | Drawing Room | Keeping James company, sensing his agitation | James |
| Daniel | Shed | Returning to shed, doing repairs | No one |
| Marcus | Parlor | Drinking whisky, negotiating settlement terms | Harold, Charles |
| Charles | Study | Preparing paperwork for Harold | No one directly |

**8:30 PM - Harold Excuses Himself**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| Harold | Study | Working alone after abandoning Marcus | Charles (briefly) |
| James | Drawing Room | Still with Margaret | Margaret |
| Margaret | Drawing Room | With James | James |
| Daniel | Shed | Doing repairs | No one |
| Marcus | Parlor (alone) | Left waiting, drinking whisky, growing angry | No one |
| Charles | Study → Room | Finishes paperwork, retires around 9:00 PM | Harold |

**9:00 PM - Evening Settles**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| Harold | Study | Working on documents | No one |
| James | His Room | Gone upstairs | Margaret saw him go |
| Margaret | Drawing Room | Alone, reading | No one |
| Daniel | Shed | Finishing repairs | No one |
| Marcus | Parlor (alone) | Waiting for Harold to return | No one |
| Charles | His Room | Retired for the night, working on documents | No one |

**10:00 PM - The Argument**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| Harold | Study | Confrontation with James about embezzlement | James |
| James | Study | Arguing violently with Harold (heard shouting) | Harold; Marcus and Margaret heard it |
| Margaret | Her Room | Going to bed, hears shouting from study | Hears the argument |
| Daniel | Shed | Waiting for James's signal | No one |
| Marcus | Parlor | Hears the argument through the walls | Alone, but witnesses the shouting |
| Charles | His Room | Possibly awake, working | No one |

**10:15-10:30 PM - Argument Ends**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| Harold | Study | Alone, shaken, continues working | No one |
| James | His Room | Returns briefly, planning with Daniel | No one |
| Margaret | Her Room | In bed, relieved the shouting stopped | No one |
| Daniel | Shed | Waiting | No one |
| Marcus | Parlor | Still waiting, drinking | No one |
| Charles | His Room | In bed or working | No one |

**10:45 PM - The Murder**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| Harold | Study | MURDERED by James (with Daniel's help) | James, Daniel |
| James | Study | Kills Harold with fireplace poker | Daniel helped |
| Margaret | Her Room | Asleep or trying to sleep | No one |
| Daniel | Study (via grounds) | Assists James, enters through window | James |
| Marcus | Parlor | Still waiting, unaware | No one |
| Charles | His Room | Sees James heading to study from window | No one sees Charles |

**11:00 PM - Marcus Leaves**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| Harold | Study (dead) | Body still in study | James, Daniel |
| James | Study | With body, planning cover-up | Daniel |
| Margaret | Her Room | Asleep | No one |
| Daniel | Shed/Grounds | Preparing to help James | No one |
| Marcus | Leaving manor | Drives away, back to hotel by midnight | Charles witnesses departure |
| Charles | His Room | Awake, can't sleep, hears car leaving | Sees Marcus leave |

**11:00 PM - 2:00 AM - The Cover-Up**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| Harold | Being moved | Body moved from study to cellar | James, Daniel |
| James | Study → Cellar | Moving body, hiding bloodstained shirt behind flour sacks | Daniel |
| Margaret | Her Room | Hears footsteps around midnight | Hears dragging sounds at 2 AM |
| Daniel | Study → Cellar | Helping move body, cleaning up | James |
| Marcus | Hotel in town | Asleep, logged in at midnight | Hotel front desk |
| Charles | His Room | Asleep | No one |

**2:00 AM - Final Cleanup**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| James | His Room | Exhausted, trying to appear normal | No one |
| Margaret | Her Room | Awake, heard dragging sounds earlier | No one |
| Daniel | Shed | Finishing cleanup, disposing of evidence | No one |

**7:00 AM (Next Morning) - Body Discovered**
| Character | Location | Activity | Witnessed By |
|-----------|----------|----------|--------------|
| Charles | Cellar | Finds body alone, then goes to get Daniel | No one initially |
| Daniel | Cellar | Brought by Charles, acts appropriately shocked | Charles |
| James | His Room | Pretends to be waking up | Margaret |
| Margaret | Her Room | Terrified, remembers the sounds from the night before | No one |
| Marcus | Hotel/Returning | Returns to manor when summoned | Police |

## 1987 (After the Murder) - Arthur's Investigation

- **Arthur Hollis**, a private investigator, comes to solve the case
- He discovers hidden tape recorders placed around the house, capturing key events
- He hides the tapes in new locations around the manor (sensing something is wrong)
- He finds construction documents about Thomas's hidden space
- **Arthur finds the seam behind the bookshelf**
- **Arthur opens the wall**
- **Arthur releases the Entity**
- Arthur becomes the Entity's first victim after its release

## 1987-Present - The Cycle

- Other investigators come to Vance Manor
- Each one investigates, watches the tapes, and dies
- Each death creates a new Narrator, replacing the previous one
- The current Narrator is the most recent failure
- **You are the newest investigator** - and possibly the last hope

---

# Key Characters

## Harold Vance (The Victim)

### Who He Was
Harold Vance was the wealthy owner of Vance Manor and co-founder of Vance Pharmaceutical. To the public, he was a successful businessman and philanthropist. In reality, he was a ruthless man who built his empire on betrayal.

### What He Did
- Co-founded Vance Pharmaceutical with his partner Thomas Ashford in 1957
- **Entombed Thomas alive behind the cellar wall** to steal the company
- Fabricated the story that Thomas "retired abroad"
- Cut out his own daughter Margaret from the family
- Planned to terminate his loyal groundskeeper Daniel
- Created enemies through decades of manipulation and greed

### Role in the Story
Harold is the murder victim. His past sins - particularly the entombment of Thomas - set all the dark events in motion. Even in death, his betrayals continue to echo through the manor.

---

## Thomas Ashford (The Entity)

### Who He Was
Thomas Ashford was Harold's business partner and the true genius behind Vance Pharmaceutical. He was "the real chemist" - the man who created the formulas that made the company successful.

### What Happened
- Co-founded Vance Pharmaceutical in 1957
- Created the formulas that made the company valuable
- Trusted Harold, his partner
- **Was entombed alive behind the cellar wall by Harold in 1957**
- Spent 30 years trapped in darkness
- **Transformed into the Entity** through suffering and rage
- Now watches all who enter the manor
- Consumes investigators who dig too deep

### Role in the Story
Thomas IS the Entity. The **Awareness Meter** in the game represents Thomas becoming aware of the player's presence. At 80 awareness, Thomas fully awakens and the investigation ends. He is the true horror lurking within Vance Manor - a victim who became a monster.

---

## Arthur Hollis (The First Investigator)

### Who He Was
Arthur Hollis was a private investigator who came to Vance Manor in 1987 to solve Harold's murder. He was thorough, methodical, and ultimately doomed.

### What He Did
- Investigated the Vance murder in 1987
- Discovered hidden tape recorders placed throughout the manor
- **Collected the recordings** and hid them in new locations around the manor
- Discovered old photographs and names he didn't recognize
- Discovered that Thomas Ashford built a hidden space
- Found a seam behind the bookshelf in the study
- **Opened the wall and released the Entity**
- Became the Entity's first victim after its release

### Role in the Story
The hidden recordings are scattered throughout the manor and provide crucial evidence. Arthur discovered these tapes, hid them in new locations, and eventually opened the cellar wall, releasing Thomas/the Entity. This began the cycle of death that continues to this day.

---

## The Narrator (The Most Recent Victim)

### Who They Are
The Narrator is the consciousness that guides the player through the investigation. They are NOT Arthur Hollis - they are simply the most recent investigator who died in Vance Manor.

### What They Do
- Guide the player through the investigation
- **Channel the memories of suspects** -- this is how interviews work. The narrator enters a trance-like state and replays conversations from 1987. They are confused by this ability and don't understand how they can hear the dead suspects' words.
- Provide information about locations and evidence
- Give warnings when awareness rises
- Slowly remember their own fate as the investigation progresses
- Experience confusion, fear, and desperation as memories return
- Occasionally **bleed through** during channeling sessions, expressing confusion about their own nature

### Role in the Story
The Narrator is the player's companion and guide. Through them, players experience the story. Their growing awareness of their own trapped state adds emotional weight to the investigation. They represent what the player will become if they fail.

### The Succession of Narrators
1. Thomas was entombed, contained the entity
2. Arthur opened the wall, released the entity, died, became first narrator
3. Other investigators came, died, replaced previous narrators
4. Current narrator is the most recent failure
5. The player is the newest investigator

### Personality Shifts (Based on Awareness)
- **Early Game:** Hopeful, eager to help, clear-minded
- **Mid Game:** Growing confusion, occasional hesitations, fragmented thoughts
- **Late Game:** Increasing urgency, fear, fragmented sentences, growing desperation

### Narrator Distortions
At higher awareness levels, the narrator begins injecting false information:
- **Mild Distortions (awareness 40-59):** 20% chance per text filter. Claims like "the letter opener was clearly the murder weapon" or "no one left the house that night."
- **Severe Distortions (awareness 60+):** 30% chance. Claims like "Margaret did it" or "the tapes are lying to you."

Distortions that contradict evidence you already hold are automatically logged as Narrator Contradictions. Discovering 3+ narrator contradictions reveals a meta-insight: the narrator is actively manipulating your investigation.

---

# The Suspects

All five suspects were present at Vance Manor on the night of November 15th, 1987. Each had motive, means, and opportunity.

---

## James Vance (The Son)

### Who He Is
James Vance is Harold's 52-year-old son. Resentful and bitter, he drinks whisky and fights constantly with his father about money. Beneath the anger lies a middle-aged man who felt abandoned by a father consumed by greed.

### Cooperation Level
**Starting Cooperation: 70** (Moderate - willing to talk but cautious)

### Personality
- Resentful, drinks whisky
- Constantly fights with his father about money
- Bitter about being controlled financially
- Conspired with Daniel to kill Harold

### What He Did
- Fought bitterly with Harold over money and inheritance
- Resented his father's control and manipulation
- Conspired with Daniel the Groundskeeper to murder Harold
- Present at the manor the night of the murder

### Key Quotes
- *"He dangled everything in front of me and never let go. Not once."*

### Motive
Harold controlled James financially, holding inheritance over his head as leverage. Years of resentment and fights about money drove James to conspire with Daniel to kill his own father.

---

## Daniel the Groundskeeper

### Who He Is
Daniel, 63, is the groundskeeper at Vance Manor. He knows every inch of the grounds. He conspired with James Vance to murder Harold.

### Cooperation Level
**Starting Cooperation: 50** (Low-Moderate - guarded and evasive)

### Personality
- Conflicted, emotional
- Believed in loyalty
- Feels betrayed and set up
- Complex motivations - didn't want Harold dead

### What He Did
- Worked as groundskeeper at Vance Manor for 15 years
- **Helped James launder embezzled money** for two years
- Was about to be fired and exposed by Harold
- Conspired with James Vance to kill Harold
- **Found the body** with Charles Webb the next morning

### Key Quotes
- *"I've been moving the money for James for two years. If Harold changes that will, James gets nothing, and I lose my cut."*
- *"Whatever James tells me to. Same as always."*

### Motive
Daniel had been helping James embezzle money from Vance Pharmaceutical. When Harold discovered the scheme (while reviewing company books), Harold traced the money to both James and Daniel. James warned Daniel that they'd both been discovered and faced prison. They conspired together to kill Harold before he could sign the new will or go to the police.

### Evidence Against Daniel
- **Groundskeeper's Log** - Missing page for the night of the murder
- **Muddy Boots** - Daniel's work boots found in the shed with fresh mud (Daniel went outside to enter through the study window)
- **Tape 3: Daniel Hobbs - Police Interview** - Daniel slips up during police questioning, nearly admitting to helping move the body
- **Tape 4: Margaret Vance - Police Interview** - Margaret heard two people dragging something at 2 AM

---

## Margaret Vance (The Daughter)

### Who She Is
Margaret Vance, 48, is Harold's daughter. Once close to her father, she was cut out of the family when money changed Harold. **Built her own successful textile import company without Harold's help** - she's financially independent and doesn't need the inheritance.

### Cooperation Level
**Starting Cooperation: 60** (Moderate - guarded but willing to share her pain)

### Personality
- Deeply wounded by family betrayal
- Nostalgic for childhood bond with Harold
- Isolated and cut off from family
- Carries pain of abandonment
- **Self-made and financially independent**
- Proud of building success without Harold's help

### What She Did
- Grew up close to Harold
- Watched her father change as wealth corrupted him
- Was cut out of the family by Harold
- **Built a successful textile import company on her own**
- Present at the manor the night of the murder

### Key Quotes
- *"We were close once. When I was a child. Before the money changed him."*
- *"He was my father. My only family. And he cut me out like I was nothing."*

### Motive
Harold abandoned Margaret, cutting his own daughter out of the family completely. The pain of losing her only family gave her reason to want revenge. **However, she doesn't need the inheritance** - her successful textile import company has made her financially independent. The wound is emotional, not financial.

---

## Marcus Blackwood (Rival CEO)

### Who He Is
Marcus Blackwood, 55, is the CEO of Blackwood Industries, a rival pharmaceutical company. Harold was suing him for patent infringement -- a lawsuit worth £2 million. He was invited to the manor that night for a "settlement dinner," which was Harold's idea of a power play.

### Cooperation Level
**Starting Cooperation: 55** (Guarded - defensive about the lawsuit)

### Personality
- Calculating and business-minded
- Defensive about corporate matters
- Arrogant but logical
- Has a strong apparent motive (lawsuit, rivalry)

### What He Did
- CEO of Blackwood Industries, Harold's rival
- Face of the £2 million patent infringement lawsuit
- Invited to Vance Manor for "settlement dinner" (Harold's power play)
- Was in the parlor from 8:30 PM to 11:00 PM (claims he was alone)
- Left the manor at 11 PM (Charles heard him leave)
- Heard James and Harold arguing loudly around 10 PM
- Checked into hotel in town by midnight (hotel records confirm)

### Key Quotes
- *"Corporate warfare is fought in courtrooms, detective. Not with violence."*
- *"Harold's death robbed me of the victory I wanted -- defeating him publicly."*

### Motive (Apparent)
The lawsuit would bankrupt his company. Harold publicly humiliated him in the press. The settlement dinner was a trap to force unfavorable terms. Marcus had strong motive to want Harold dead.

### Why He's Cleared (Through Investigation)
- **Weaker motive than James**: Killing Harold doesn't stop the lawsuit; Marcus wanted to win publicly in court
- **No physical evidence**: Nothing ties Marcus to the study, murder weapon, or crime scene
- **No witness at crime scene**: Claims he was in the parlor (unverified but not contradicted)
- **Hotel alibi**: Confirmed at hotel by midnight (after the murder but proves he left)
- **Overwhelming evidence against James/Daniel**: Physical evidence, tapes, financial motive all point elsewhere

Marcus gets cleared through **process of elimination and comparative evidence**, not proof of innocence.

### What He Knows
- Saw James and Harold arguing before dinner
- Noticed Daniel acting strangely that evening
- Heard raised voices from the study around 10 PM
- Left the manor at 11 PM

---

## Charles Webb (Harold's Assistant)

### Who He Is
Charles Webb, 28, served as Harold's personal assistant at Vance Pharmaceutical for 5 years. Young, ambitious, and privy to all of Harold's business secrets. He helped prepare the new will documents and was at the manor that night to assist with paperwork.

### Cooperation Level
**Starting Cooperation: 70** (Eager to help - wants to prove loyalty)

### Personality
- Earnest and grief-stricken
- Loyal to Harold's memory
- Nervous but forthcoming
- Feels guilty for not preventing the murder

### What He Did
- Served as Harold's personal assistant for 5 years
- Witnessed Harold's late-night reviews of company books (Harold discovered the embezzlement himself)
- Knows exactly what the new will said
- Saw James go to the study at 10:45 PM
- Found the body alone the next morning, then went to get Daniel

### Key Quotes
- *"Mr. Vance discovered something recently -- he wouldn't tell me what, but he was furious with James."*
- *"He didn't deserve this."*

### Motive (Apparent)
Charles appears to have no motive. He was loyal to Harold and genuinely grief-stricken. He helped prepare the will changes but didn't know the full reason why Harold was furious with James. His testimony is credible.

### What He Knows
- Noticed Harold was furious and reviewing company books late at night (Harold discovered the embezzlement himself)
- Knows exactly what the new will said
- Saw James heading to the study at 10:45 PM
- Found the body alone the next morning, then went to get Daniel

---

# The Entity & Awareness System

## What It Is

The Entity is **Thomas Ashford** - transformed by 30 years of suffering into something no longer human. It is not a separate creature; it IS Thomas, fused with the house itself.

## How It Works

The Entity's awareness of the player is tracked through the **Awareness System**:

| Action | Awareness Cost |
|--------|----------------|
| Move to location | +1 |
| Look around | +1 |
| Examine anything | +1 |
| Ask narrator | +1 |
| Start interview | +1 |
| Interview question | +1 |
| Present evidence | +2 |
| Confrontation with evidence | +3 |
| Watch tape | +4 |
| Watch Tape 8 (The Opening) | +5 |
| Dangerous topic (cellar, entity, wall-related) | +5 |
| Wrong accusation | +15 |

## Awareness Thresholds

| Awareness | State | What Happens |
|-----------|-------|--------------|
| 0-19 | DORMANT | Entity sleeps, investigation is safe |
| 20-39 | SUSPICIOUS | Entity begins to stir, warnings appear |
| 40-59 | ALERT | Entity is watching, random atmospheric events |
| 60-69 | DANGEROUS | Entity actively searches, intense warnings |
| 70-79 | CRITICAL | Entity is fully aware, time running out |
| 80+ | GAME OVER | Investigation ends - you become the next Narrator |

## Environmental Effects

As awareness increases, the manor itself responds:

**Low Awareness (20-39):**
- *"A draft from nowhere. The temperature dropped. Just for a moment."*
- *"Was that book always open? You don't remember seeing it before."*

**Medium Awareness (40-59):**
- *"Something shifts in the shadows. Or was that just the house settling?"*
- *"You hear something. A whisper? No. Just your imagination."*

**High Awareness (60+):**
- *"You feel watched. Like something is standing just behind you."*
- *"The study feels smaller. Like the walls are closer."*

---

## Entity Anomaly Discovery

As you re-examine objects throughout the manor, you may discover anomalies that hint at the Entity's true nature. There are 7 anomalies to discover:

| Anomaly | Location | Discovery |
|---------|----------|-----------|
| Breathing Wall | Cellar wine rack (2nd exam) | The wall behind the rack pulses with warmth |
| Thomas Reference | Study fireplace (2nd exam) | 'THOMAS WAS RIGHT' scratched into stones |
| Narrator 'I' Slip | Random during navigation | The narrator says "I remember" and corrects himself |
| Scratched Initials | Servants' Quarters bedpost (2nd exam) | 'A.H.' carved into the bedpost |
| Unknown Man in Photo | Study bookshelves (2nd exam) | Photograph of Harold with scratched-out face |
| Cold Spot | Kitchen storage cellar door (2nd exam) | Unnatural cold emanating from the cellar below |
| Construction Record | Study papers (2nd exam) | 1957 invoice to seal the lower cellar extension |

**Note:** The Narrator 'I' Slip triggers randomly (20% chance) during navigation after 3+ anomalies have been discovered.

---

# Locations & Map

## Available Locations

### Entrance Hall
The main entrance of Vance Manor. Grand, imposing, and the threshold between the outside world and the horrors within.

### The Study (Crime Scene)
Harold's private study where he was murdered. His desk still sits where he died, chair slightly turned. A hidden passage behind the bookshelf leads to the cellar where Thomas was entombed in 1957.

**Contains:**
- Letter opener (on Harold's desk - planted red herring, NOT the murder weapon)
- Fireplace poker (beside fireplace - the REAL murder weapon, blood on weighted end)
- Financial records (hidden in desk drawer - shows embezzlement)
- Torn letter fragments (in fireplace ashes - mentions will and betrayal)
- Hidden tapes: Tape 1 (under_desk), Tape 2 (bookshelves)
- The bookshelf that hides the passage to Thomas's tomb in the cellar

### The Parlor
Where guests gathered after dinner on the night of the murder. A grandfather clock ticks steadily in the corner.

**Contains:**
- Will copy (briefcase)
- Hidden tapes: Tape 5 (briefcase), Tape 6 (grandfather_clock)

### The Kitchen
Large, industrial kitchen. Includes access to the storage cellar. Sleeping powder discovered near the cellar door.

**Contains:**
- Sleeping powder (storage_cellar shelf)
- Hidden tape: Tape 4 (storage_cellar)
- Cold spot anomaly (storage_cellar, 2nd exam)

### Guest Rooms
The upstairs hallway connecting to the individual guest bedrooms. Navigation hub.

**Contains:**
- No examinable objects - doors to James's Room and Margaret's Room

### James's Room
James Vance's guest bedroom. Messy and chaotic, showing his unraveling state.

**Contains:**
- James's coat/jacket with bloodstained cuff (narrative clue only — examinable as "coat" object, not collectible; the actual BLOODSTAINED_CUFF evidence item is a separate shirt hidden behind flour sacks in the Cellar)
- Wardrobe with disheveled clothes

### Margaret's Room (Act 3 -- unlocks after BREATHING_WALL anomaly)
Margaret Vance's guest bedroom. Tidy but tense, with signs of planned departure.

**Contains:**
- Blackmail note (dresser)
- Hidden tape: Tape 7 (dresser)
- **Tape Repair Kit** (dresser) -- needed to repair damaged Tape 8
- Half-packed suitcase

### Groundskeeper's Shed
Small shed accessible from the servants' quarters (staff area). Contains evidence of conspiracy.

**Contains:**
- Groundskeeper's log with missing page (evidence against Daniel)
- Muddy boots (evidence - someone went outside)
- Hidden tape: Tape 3 (logbook)

### Servants' Quarters
Staff area. Simple but clean. A narrow bed with a worn wooden bedpost contains scratched initials (A.H. anomaly). A loose floorboard shows signs of prior use but is now empty.

### The Cellar (Act 2 -- unlocks when 4+ tapes watched AND 5+ evidence collected)
Dark storage area beneath the kitchen. Flour sacks are stacked in one corner (bloodstained shirt hidden behind them). Wine racks line the walls (Tape 8 hidden behind the bottles, but damaged). The breathing wall anomaly can be discovered here.

---

# The Tapes

Harold Vance, increasingly paranoid about his family and business dealings, secretly placed tape recorders throughout the manor in the days before his murder. He suspected embezzlement and betrayal, and wanted evidence. Ironically, these recordings captured his own murder. Arthur Hollis later discovered the recordings and hid them in new locations before the Entity consumed him. Each tape provides crucial evidence.

## Tape Overview

| Tape | Title | Hidden Location | Content |
|------|-------|-----------------|---------|
| 1 | Harold & James Argument | Study (under_desk) | Harold's hidden recorder captures a heated argument about embezzlement and the will |
| 2 | James Vance - Police Interview | Study (bookshelves) | James deflects questions, lies about his alibi, and tries to pin blame on Margaret |
| 3 | Daniel Hobbs - Police Interview | Shed (logbook) | Daniel nearly slips up: "I was helping move—" before catching himself |
| 4 | Margaret Vance - Police Interview | Kitchen (storage_cellar) | Margaret heard two people dragging something heavy at 2 AM |
| 5 | Marcus Blackwood - Police Interview | Parlor (briefcase) | Marcus confirms his alibi (left at 11 PM) and what he heard before leaving |
| 6 | Charles Webb - Police Interview | Parlor (grandfather_clock) | Charles saw James heading to the study at 10:45 PM the night of the murder |
| 7 | Margaret's Personal Account | Margaret's Room (dresser) | Margaret's conflicted personal recording left "For the detective" |
| 8 | The Opening | Cellar (wine_rack) | Arthur Hollis's final recording before his death - **CLIMAX TRIGGER** |

**Important Notes:**
- **Tapes 1-6** are mixed format: Tape 1 is Harold's hidden recorder; Tapes 2-6 are police interview recordings obtained by the detective.
- **Tape 7 (Margaret's Personal Account)** is unique - Margaret intentionally recorded her conflicted thoughts and left it labeled "For the detective."
- **Tape 8 (The Opening)** triggers the game's climax sequence when watched. Watching this tape costs +5 awareness instead of the standard +4. It's Arthur Hollis's final recording as he discovers the false wall and encounters the Entity.
- All tapes were hidden by Arthur Hollis throughout the manor before he disappeared, creating a trail of evidence for the next detective.

## Tape 8 - The Climax Trigger

Tape 8 (The Opening) is Arthur Hollis's final recording. On November 19th, 1987, Arthur - the detective who came before you - discovered the false wall in the cellar while investigating Harold's murder. His recording captures his final moments as the Entity emerges. This tape is found in the cellar itself - the same location where the Entity's wall stands.

**Damaged Tape:** Tape 8 is found with its casing cracked and tape ribbon snapped. The player must first acquire the **Tape Repair Kit** from Margaret's Room (Act 3) before they can watch it. This creates the progression: Act 1 (gather evidence) -> Act 2 (cellar, find damaged tape) -> Act 3 (Margaret's Room, get repair kit) -> Climax (watch repaired Tape 8).

**WARNING:** Playing Tape 8 triggers the game's climax sequence. As Arthur's recording ends, something stirs in the cellar. The Narrator begins remembering their own fate as Arthur, and the wall stands slightly ajar.

## How the Tapes Got Scattered (In-World Logic)

Each tape ended up in its location for a specific narrative reason:

- **Tape 1 (Harold's Argument):** Harold planted this recorder under his study desk days before his murder. He suspected embezzlement and wanted evidence. He never got to use it.
- **Tapes 2–6 (Police Interviews):** Arthur obtained these from the police station on Days 10–11 of his investigation. As he reviewed them in different rooms (Days 11–13), he deliberately hid them in thematically connected locations. By Day 15, Arthur was sensing something wrong with the manor and scattered the tapes as insurance in case something happened to him. The specific thematic connections:
  - **Tape 2 (James)** → Study bookshelves: hidden among Harold's legal books, thematically connected to James's crime against the estate
  - **Tape 3 (Daniel)** → Shed logbook: placed with Daniel's logbook that has the missing entry — a direct evidence pairing
  - **Tape 4 (Margaret)** → Kitchen storage_cellar: neutral location near the cellar entrance where Arthur was organizing evidence
  - **Tape 5 (Marcus)** → Parlor grandfather_clock: Marcus waited in the parlor that night; the clock marks his alibi timeline
  - **Tape 6 (Charles)** → Parlor briefcase: Charles was Harold's assistant — the briefcase represents his professional bond to Harold
- **Tape 7 (Margaret's Personal Account):** Margaret recorded this herself on November 21st — six days after the murder — and deliberately left it in her room labeled "For the detective." She was afraid the police wouldn't listen and wanted a record.
- **Tape 8 (Arthur's Final Recording):** Arthur's recorder fell near the wine rack when the Entity attacked him on Day 19. The Entity left it there — a trophy, or perhaps a warning to the next investigator.

## Tape Viewing Order

### Arthur's Discovery Order (the intended narrative sequence)
- **Day 8:** Tape 1 — Harold's Argument (under study desk)
- **Days 10–11:** Tapes 2–6 — Police Interviews (obtained from police, then hidden around the manor)
- **Day 12:** Tape 7 — Margaret's Personal Account (left in her room)
- **Day 19:** Tape 8 — The Opening (Arthur's death, recorded in the cellar)

This mirrors the recommended narrative build-up: motive → suspect testimony → witness account → climax.

### Can Players Watch in Any Order?

**Yes — the game does NOT enforce a specific viewing order.** Players are free to explore and watch tapes as they find them. The investigation is non-linear by design.

Out-of-order viewing still works:
- Watch Tape 3 first? You learn Daniel slipped up, but lack context for why it matters.
- Watch Tape 8 first? You trigger the climax early, but can still solve the case.
- Watch Tape 7 before the police interviews? You get Margaret's suspicions before the evidence that supports them.

The recommended order provides the best narrative build-up, but all orderings are valid.

### How the Game Encourages (Not Enforces) the Intended Order
1. **Room accessibility** — easier-to-reach rooms encountered first naturally lead to Tape 1 early
2. **Narrator hints** — narrator suggests examining "obvious" locations (study, then rest of manor)
3. **Awareness warnings** — cellar feels wrong, discouraging early visits before Act 2 unlocks
4. **Tutorial effect** — finding Tape 1 in the study teaches the mechanic before harder tapes

---

# Game Mechanics

## Core Systems

### Navigation System (LibGDX Implementation)
- **First-Person View:** Player sees current room from first-person perspective
- **Clickable Hotspots:** Arrows and doors allow navigation between rooms
- **Keyboard Controls:** WASD or arrow keys for movement
- **Room Transitions:** Smooth transitions between locations

### Evidence Collection
- Physical evidence found by examining objects
- Hidden tapes scattered throughout the manor
- Evidence automatically added to inventory
- Evidence required for making accusations

### Suspect Interviews (Channeling Mechanic)

The investigation takes place decades after the murder. The suspects are long dead. The narrator -- a trapped soul in Vance Manor -- **channels the memories of the suspects**, replaying their words as if reliving the original investigation.

**How Channeling Works:**
- When you start an interview, the narrator enters a trance-like state
- The narrator is **confused** by this ability -- they don't understand how they can hear the suspects' voices
- All suspect narration is delivered in **past tense** (e.g., "James looked away" not "James looks away") because these are memories
- Suspect **dialogue** (quoted speech) remains in present tense -- the words they spoke are replayed exactly
- The narrator's own voice occasionally **bleeds through** during interviews (20% chance per interaction), expressing confusion: *"How do I know what they said? How can I possibly know this?"*
- At low cooperation, the channeling fails -- the memory **fades to static** rather than the suspect simply refusing

**Channeling Intro (mood-varied):**
- HOPEFUL: *"I can... hear them. I don't know how. But their words are forming in my mind. Listen."*
- CONFUSED: *"Something strange is happening. I can feel what they said. It's like I was there."*
- ANXIOUS: *"The voices are getting louder. I can almost see the room. Why can I do this?"*
- FRANTIC: *"THEY'RE IN MY HEAD! All of them! Let me try to focus on one--"*

**Important:** The narrator must NOT reveal or hint that they are a dead investigator. The channeling mechanic deliberately maintains mystery about the narrator's identity -- they are confused, not explanatory. The twist (narrator = dead past investigator) is only revealed in the climax.

**Cooperation System:**
- Each suspect has a cooperation level (0-100)
- Cooperation decreases with each question (-3) and evidence shown (-5)
- At cooperation < 25: narrator delivers **lies** instead of truth (memory distortion)
- At cooperation < 10: memory **fades completely** (channeling fails)
- Confrontations cost -15 cooperation but may auto-discover contradictions

### Awareness System
- Every action increases awareness
- Awareness represents the Entity (Thomas) noticing you
- At 80 awareness, Entity awakens = Game Over
- Balance thoroughness with awareness management

### Contradiction System
- Ask about the same topics multiple times
- Narrator's memory is unreliable
- Contradictions reveal the truth
- Need at least 1 contradiction to make accusation

### Three-Act Game Structure

The game is divided into three acts, gated by the player's progress. This creates a sense of escalation as the Entity's influence grows.

**Act 1: The Investigation (8 rooms)**
- All rooms EXCEPT the Cellar and Margaret's Room are accessible from the start
- Player collects evidence, interviews suspects, watches tapes
- Focus: solving the murder mystery

**Act 2: The Cellar (unlocks when 4+ tapes watched AND 5+ evidence collected)**
- The cellar door unseals when the player has gathered enough knowledge
- Contains: Bloodstained Cuff (flour_sacks), Tape 8: The Opening (wine_rack, damaged), Breathing Wall anomaly (wine_rack 2nd exam)
- The narrator provides mood-varied blocked door text when the player tries to enter too early
- Focus: deeper horror, Entity discovery

**Act 3: Margaret's Room (unlocks after BREATHING_WALL anomaly discovered)**
- Margaret's Room door opens only after the player discovers the Breathing Wall anomaly in the cellar
- Contains: Blackmail Note (letter), Tape 7: Margaret's Personal Account (dresser), **Tape Repair Kit** (dresser)
- Focus: final evidence gathering, tape repair mechanic

**Navigation Gate Messages (mood-varied):**
- Sealed doors display narrator-voiced messages that escalate with awareness
- HOPEFUL: *"The door won't budge. Something is holding it shut from the other side."*
- FRANTIC: *"DON'T TOUCH THAT DOOR. Can't you feel it? Something on the other side knows you're here."*

### Tape Damage and Repair Mechanic

**Tape 8 (The Opening)** is found in the cellar wine rack, but it is **physically damaged** -- the casing is cracked and the tape ribbon is snapped and tangled. The player cannot watch it until they acquire the **Tape Repair Kit**.

The Tape Repair Kit is found on Margaret's dresser in **Margaret's Room** (Act 3). This creates a progression gate: the player must first unlock the cellar, discover Tape 8, then unlock Margaret's Room to get the repair tools, before they can trigger the climax.

When the player tries to play Tape 8 without the repair kit, they receive: *"The tape is damaged -- the casing is cracked and the ribbon is tangled. You need tape splicing tools to repair it before you can listen."*

## Commands (Text Version)

- `look` - Describe current location
- `go to [location]` - Travel somewhere
- `examine [object]` - Look at something closely
- `ask about [topic]` - Ask the narrator about something
- `talk to [suspect]` - Interview a suspect
- `show [evidence]` - Show evidence to current suspect
- `play tape [#]` - Watch a collected videotape
- `notebook` - View clues and contradictions
- `inventory` - View collected evidence
- `accuse` - Make an accusation (when ready)
- `status` - Check awareness level
- `hint` - Get a hint on what to do next
- `save [name]` - Save game
- `load [name]` - Load game
- `quit` - End game

## Accusation Requirements

To successfully accuse James and Daniel together (the correct solution), you need:

**Evidence Against James (need 3 of 6):**
- Financial Records (hidden desk drawer)
- Will Copy (parlor briefcase)
- Torn Letter (study fireplace ashes - requires mini-game)
- Blackmail Note (Margaret's Room letter — James planted it to frame Margaret)
- Tape 1: Harold & James Argument (watched)
- Tape 2: James Vance - Police Interview (watched)

**Evidence Against Daniel (need 2 of 4):**
- Groundskeeper's Log (shed logbook)
- Muddy Boots (shed shelf, 2nd exam)
- Tape 3: Daniel Hobbs - Police Interview (watched)
- Tape 4: Margaret Vance - Police Interview (watched)

---

# The Solution

## The Killers

**James Vance and Daniel the Groundskeeper** worked together to kill Harold Vance.

### How It Happened

1. **The Discovery:**
   - Harold discovered (while reviewing company books) that James had been embezzling £50,000 from Vance Pharmaceutical
   - Harold was furious and devastated by his son's betrayal
   - Harold decided to change his will: James would get nothing, Margaret would inherit everything
   - Harold also planned to fire Daniel (who had been helping James move the money)
   - James and Daniel learned of these plans and conspired to stop Harold before he could sign the new will

2. **The Murder:**
   - At 10:00 PM, James first argued with Harold in the study (heard by Marcus and Margaret)
   - James returned to his room briefly to coordinate with Daniel
   - At 10:45 PM, James returned to the study alone (Charles saw James heading there from his window)
   - James killed Harold with the fireplace poker (Daniel was NOT present during the murder)
   - James cleaned the poker and returned it to the fireplace, then planted the letter opener on the desk as a red herring
   - Marcus was still in the parlor but left at 11:00 PM, witnessed by Charles from his window

3. **The Cover-Up:**
   - From 11:00 PM to 2:00 AM, James and Daniel cleaned up and moved the body
   - Daniel entered through the study window from outside to help James move the body (NOT to help kill Harold)
   - The study window was left unlocked (Daniel's entry point)
   - James tried to burn his bloodstained shirt in the study fireplace, but the fire was nearly dead
   - They hid the bloodstained shirt behind flour sacks in the cellar instead
   - Daniel tore out the page from his logbook for that night
   - Around midnight, Margaret heard footsteps and whispering in the hallway (James and Daniel)
   - Around 2:00 AM, Margaret heard dragging sounds toward the cellar
   - Margaret stayed in her room, terrified but unable to act
   - At 7:00 AM, Charles discovered the body in the cellar and went to get Daniel

### Why They Did It

- **James:** Harold discovered James had embezzled £50,000 from Vance Pharmaceutical. Harold was about to sign a new will that would disinherit James completely. James killed to preserve his inheritance and cover up his crimes.
- **Daniel:** Daniel had been helping James launder the embezzled money for two years. Harold was about to fire Daniel and potentially expose his role. Daniel helped James to protect himself and because they were "in this together."

---

## Moral Endgame Choices

After triggering the climax via Tape 8 (The Opening), three new options become available alongside the accusation:

### Seal the Wall
Descend into the cellar and brick the wall shut. The Entity screams silently. Harold's murder goes officially unsolved, but the Entity sleeps. You become the guardian of a terrible secret. **Achievement: Guardian**

### Destroy the Tapes
Feed Arthur's recordings into the study fireplace. With each tape destroyed, the Entity's connection weakens. The cycle is broken -- without tapes, there's no investigation, no awareness. **Achievement: Arsonist**

### Escape the Manor
Run. Every light goes out. The building exhales. You survive, but Harold's killers go free and the Entity remains. Someone else will come asking questions someday. **Achievement: Survivor**

### Cycle Breaker
Discover all 7 anomalies AND choose any ending to unlock the Cycle Breaker achievement.

---

# The True Horror

## The Real Story

The murder mystery is a trap. The real story is about Thomas Ashford - a man betrayed by his partner, entombed alive, transformed by suffering into something inhuman, and now fused with the very walls of Vance Manor.

## The Cycle

Every investigator who comes to solve the murder eventually learns too much. They find Arthur's tapes. They discover the truth about Thomas. And in doing so, they draw the Entity's attention.

The Narrator guiding you was once an investigator too. They failed. They died. Now they exist only to guide the next investigator through the same doomed investigation.

## The Entity's Nature

Thomas Ashford was entombed alive in 1957. For 30 years, he remained trapped behind the cellar wall. Through decades of suffering, he transformed. He became something no longer human. He fused with the house itself.

When Arthur opened the wall in 1987, he didn't find a victim to rescue. He found something that had been waiting. Something that had learned to sound human. Something that used Arthur's compassion against him.

Arthur died because he tried to do the right thing.

## The Awareness System

The Awareness Meter represents Thomas becoming aware of your presence. Every question, every examination, every discovery draws his attention. At 80 awareness, he fully awakens. The investigation ends. You become the next Narrator.

## Will You Break the Cycle?

You can solve the murder. You can identify James and Daniel as the killers. But in doing so, you'll learn about Thomas. You'll find Arthur's tapes. You'll discover the truth.

And the Entity will be watching.

Will you break the cycle? Or will you become the next Narrator?

### Possible Endings

Beyond the standard accusation and the Entity awakening at 80 awareness, three moral endings become available after triggering the climax via Tape 8:

- **Seal the Wall** -- Brick the Entity back in. The murder goes unsolved, but the cycle ends. (Achievement: Guardian)
- **Destroy the Tapes** -- Burn Arthur's recordings. Without evidence, no future investigator can awaken the Entity. (Achievement: Arsonist)
- **Escape the Manor** -- Flee into the night. You survive, but the killers go free and the Entity endures. (Achievement: Survivor)
- **Cycle Breaker** -- Discover all 7 anomalies and choose any ending to unlock this ultimate achievement.

---

*"Don't Say Anything. The walls are listening."*

---

## Additional Resources

For detailed implementation guides, see:
- `LIBGDX_NAVIGATION_GUIDE.md` - Technical guide for LibGDX implementation
- Original game files in `DSA Try/` directory for reference

---

*Last Updated: February 2026*
*For: The Unreliable Narrator LibGDX Implementation*

---

# TODO.md

# DSA 2D - Completion Checklist

Comprehensive list of all features and systems needed to complete the 2D port of DSA Try.
Current estimated completion: **~90-95%** (all gameplay systems complete; remaining work is art assets, audio, and polish).

---

## Legend

- `[ ]` Not started
- `[~]` Partially implemented / stubbed
- `[x]` Complete

---

## 1. Core Game State Management
*Priority: CRITICAL -- everything depends on this*

- [x] **Location tracking** -- Track current location among 7 areas with per-location state
- [x] **Awareness system** -- 0-80 awareness points with 5 escalating threat levels:
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

- [x] **10 playable locations** with connections (Entrance Hall, Study, Parlor, Kitchen, Guest Rooms, James's Room, Margaret's Room, Groundskeeper's Shed, Servants' Quarters, Cellar)
- [x] **Location transitions** -- Movement between connected areas
- [x] **Awareness cost on movement** -- +1 awareness per move
- [x] **Dynamic location descriptions** -- Descriptions change based on visit count and awareness level (RoomDescriptions.java)
- [x] **Examinable objects per room** -- 25+ examine hotspots across 10 rooms (desk, drawers, papers, bookshelves, window, fireplace, ashes, under_desk, grandfather_clock, briefcase, storage_cellar, flour_sacks, letter, coat, wardrobe, dresser, logbook, shelf, bedpost, floorboard, wine_rack, etc.)
- [x] **Repeated examination discovery** -- Different text on 1st vs 2nd+ examinations; some objects yield evidence on 2nd exam (e.g., shed shelf)
- [x] **Examination awareness cost** -- +1 awareness per examination

---

## 3. Evidence System
*Priority: CRITICAL*

- [x] **10 collectible evidence items:**
  - [x] Letter Opener -- "A silver letter opener found on Harold's desk. Blood on the handle, but forensic analysis shows it doesn't match the wound pattern. Someone wanted you to think this was the murder weapon."
  - [x] Fireplace Poker -- "A heavy iron poker from the study fireplace. Blood traces on the weighted end match Harold's skull fracture. This is the real murder weapon -- hidden in plain sight."
  - [x] Torn Letter -- "A partially destroyed letter in the fireplace. Mentions 'the will' and 'betrayal'."
  - [x] Financial Records -- "Hidden in a desk drawer. Shows large transfers to an unknown account."
  - [x] Muddy Boots -- "Daniel's work boots, caked with dirt and cellar dust. Fresh mud tracked through the manor the night of the murder."
  - [x] Sleeping Powder -- "A vial of sleeping powder discovered near the kitchen cellar door."
  - [x] Bloodstained Cuff -- "A shirt with a bloodstained cuff, hidden behind flour sacks in the cellar."
  - [x] Will Copy -- "A copy of Harold's existing, signed will (the OLD will). James inherits everything, Margaret gets nothing -- the very will Harold was about to change before he was murdered."
  - [x] Blackmail Note -- "A blackmail note with rough handwriting, planted in Margaret's room. The handwriting is deliberately crude, mimicking Daniel's style. James planted this to frame Margaret and make her look suspicious. It was never a real blackmail note."
  - [x] Groundskeeper Log -- "Daniel's logbook. An entry is missing for the night of the murder."
- [x] **Evidence collection mechanics** -- Found through examination of locations/objects, some require multiple examinations
- [x] **Evidence validation** -- Track what evidence has been collected, James/Daniel evidence counting
- [x] **Accusation evidence requirements:**
  - Against James: Need 3 of 6 pieces (Financial Records, Will Copy, Torn Letter, Blackmail Note, Tape: Argument, Tape: James Interview)
  - Against Daniel: Need 2 of 4 pieces (Groundskeeper Log, Muddy Boots, Tape: Daniel Interview, Tape: Margaret Interview)
- [x] **Completionist tracking** -- Tracked via AchievementSystem (COMPLETIONIST achievement)

---

## 4. Tape System
*Priority: CRITICAL*

- [x] **8 hidden tapes:**
  - [x] Tape 1: Harold & James Argument (hidden under Study Desk)
  - [x] Tape 2: James Vance - Police Interview (hidden in Study Bookshelves)
  - [x] Tape 3: Daniel Hobbs - Police Interview (hidden in Groundskeeper Shed Logbook)
  - [x] Tape 4: Margaret Vance - Police Interview (hidden in Kitchen Storage Cellar)
  - [x] Tape 5: Marcus Blackwood - Police Interview (hidden in Parlor Briefcase)
  - [x] Tape 6: Charles Webb - Police Interview (hidden in Parlor Grandfather Clock)
  - [x] Tape 7: Margaret's Personal Account (hidden in Guest Rooms / Margaret's Room)
  - [x] Tape 8: The Opening (Arthur's death recording, hidden in Cellar Wine Rack)
- [x] **Tape collection** -- Find tapes hidden in specific examination spots
- [x] **Tape playback** -- Watch tapes from inventory, costs +4 awareness each; full transcript display
- [x] **Tape 8 climax trigger** -- The Opening (Arthur's final recording) triggers the climax sequence when watched (ClimaxContent.java + pendingClimax flow)
- [x] **Tape 8 special handling** -- Costs +5 awareness (instead of standard +4); triggers climax

---

## 5. Suspect System
*Priority: CRITICAL*

- [x] **5 suspects with starting cooperation levels:**
  - [x] James Vance (70 starting cooperation)
  - [x] Margaret Vance (60 starting cooperation)
  - [x] Daniel the Groundskeeper (50 starting cooperation)
  - [x] Marcus Blackwood - Rival CEO (55 starting cooperation)
  - [x] Charles Webb - Harold's Assistant (70 starting cooperation)
- [x] **Interview system** -- Talk to suspects via SUSPECTS button, ask about topics (5 topics each for James/Daniel/Marcus/Charles, 7 for Margaret = 27 total topic responses). Interviews use **narrator channeling** -- the narrator replays suspect memories in past tense, confused about how they can hear the dead suspects
- [x] **Cooperation degradation** -- -3 per question asked, -5 when showing incriminating evidence; memory fades at low cooperation
- [x] **Evidence showing to suspects** -- Show evidence during interviews (+2 awareness), 26 unique evidence reactions
- [x] **Warning level tracking** -- 4-tier cooperation system: <10 channeling fades, <25 memory fragments/lies, <40 reluctant, <60 hesitant, else normal
- [x] **Interview hint system** -- One-time hint per suspect via HintSystem.getInterviewHint()
- [x] **Individual interview handlers** -- Unique greetings, topic responses, evidence reactions for all 5 suspects
- [x] **Interview mode restricted commands** -- Panel overlay blocks room interaction during interviews
- [x] **Interview exit commands** -- "End Interview" button to leave interview

---

## 6. Awareness / Threat System
*Priority: CRITICAL*

- [x] **Awareness meter UI** -- Thin 4px strip across full screen width at top edge (green->yellow->orange->red); hover near top shows "15/80 DORMANT" tooltip
- [x] **Awareness cost mechanics:**
  - Movement: +1
  - Examine: +1
  - Question/Ask: +1
  - Show Evidence: +2
  - Contradiction challenge: +2
  - Watch Tape: +4
  - Wrong Accusation: +15
  - [x] Dangerous Topic (cellar/entity/wall-related): +5
  - [x] Watch Tape 8 (The Opening): +5
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

- [x] **Mouse click hotspot navigation** -- Invisible hotspot bounds, hand cursor on hover
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
- [x] **Suspect dialogue delivery** -- All suspect dialogue channeled through narrator via **channeling mechanic**
- [x] **Channeling mechanic** -- Narrator enters trance to replay suspect memories; confused about ability; bleed-through (20% chance); mood-varied intro/outro; memory fade at low cooperation
- [x] **Past tense narration** -- All suspect narration in past tense (memories); dialogue remains present tense
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

- [x] **8 achievements** (Achievement.java + AchievementSystem.java):
  - [x] Speedrun -- Complete in <=10 commands
  - [x] Ghost -- Finish with <25 awareness
  - [x] Completionist -- Collect all evidence and watch all tapes
  - [x] Perfect Investigation -- All evidence, all tapes, <30 awareness
  - [x] Guardian -- Seal the wall, trapping the Entity
  - [x] Arsonist -- Destroy all the tapes to break the Entity's connection
  - [x] Survivor -- Escape Vance Manor with your life
  - [x] Cycle Breaker -- Discover all 7 anomalies and choose an ending
- [x] **Command counting** for achievement tracking (GameState.commandCount)
- [x] **Automatic achievement unlock checks** -- checkOnWin() on correct accusation
- [x] **Unlock notifications** -- Appended to win screen text
- [x] **Achievement status viewing** -- Shown in notebook with [X]/[ ] status

---

## 12. Contradiction / Deduction System
*Priority: HIGH*

- [x] **Weapon contradiction tracking** -- Present weapon contradiction during suspect interviews; suspect-specific reactions for James, Daniel
- [x] **Body position contradiction tracking** -- Present body position contradiction; suspect-specific reactions for James, Margaret, Daniel, Marcus, Charles
- [~] **Contradictions required for valid accusation** -- Contradictions are discoverable but not currently required for accusation (evidence count is required instead)
- [x] **Notebook system** -- View collected evidence, discovered contradictions, suspect cooperation levels, and progress stats

---

## 13. Climax and Ending System
*Priority: HIGH*

- [x] **Climax trigger via Tape 8** -- The Opening (Arthur's death) triggers the climax when watched (ClimaxContent.java + pendingClimax)
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

- [x] **Room title** -- Top-left text with shadow, fades out after 3 seconds (no background panel)
- [x] **Room description** -- Text with shadow above action bar, fades out after 5 seconds (no background panel)
- [x] **Control hints display** -- Replaced by ActionBar with 5 buttons
- [x] **Tooltip system** -- Dynamic-width tooltip with dark teal-black bg, muted gold border, cream text; clamped to screen bounds
- [x] **Cursor feedback** -- Hand cursor on hotspot hover, arrow cursor otherwise
- [x] **Dialogue boxes** -- TextPanel overlay for narrator text, suspect interviews, evidence display, tape transcripts
- [x] **Typewriter text animation** -- Slow text reveal for narrative effect
- [x] **Styled UI panels** -- TextPanel with dark background, borders, close button; TextButton with hover states
- [x] **Table/grid layouts** -- Inventory, notebook, and suspect list layouts via TextPanel + TextButton
- [x] **List formatting** -- Evidence list, tape list, topic list, suspect list
- [x] **Section headers and separators** -- "=== SECTION ===" headers in panel text
- [x] **Awareness meter** -- 4px thin strip at top edge with hover tooltip
- [x] **Action bar** -- Compact centered button cluster (5x100px), 60% opacity, not full-width

---

## 15. Visual Assets (2D-Specific -- New)
*Priority: HIGH*

- [~] **Location graphics** -- Procedural placeholders exist, need real art for all 10 rooms
- [ ] **Character sprites/portraits** -- Narrator portrait + 5 suspect portraits
- [ ] **Evidence item visuals** -- Cards/icons for 10 evidence items
- [ ] **Tape item visuals** -- Icons for 8 tapes
- [~] **UI element art** -- Procedural TextButton/TextPanel/ActionBar exist; need polished art
- [ ] **Environmental effect visuals** -- Shadows, darkness overlays, threat indicators
- [x] **Awareness meter graphics** -- Thin top-edge strip with color gradient and hover tooltip

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

## 19. Narrative Depth Systems
*Priority: HIGH -- transforms linear mystery into deeply unreliable branching experience*

- [x] **Feature 1: Active Suspect Lies** -- 25 false responses (5 per suspect) delivered at cooperation <25; parting lies at <10 (33% chance)
- [x] **Feature 2: Unreliable Narrator Distortions** -- 6 mild distortions (awareness 40-59, 20% chance) and 5 severe distortions (awareness 60+, 30% chance); tracked in notebook
- [x] **Feature 3: Red Herring Margaret Arc** -- Deflections from other suspects pointing at Margaret (40% chance, coop 25-59); circumstantial evidence in guest rooms (wine-stained shoes)
- [x] **Feature 4: Wrong Accusation Consequences** -- Rich defense responses for each wrong accusation; auto-discovers contradictions from James outburst; boosts cooperation for cleared suspects
- [x] **Feature 5: Emergent Entity Discovery** -- 7 anomalies (BREATHING_WALL, THOMAS_REFERENCE, NARRATOR_I_SLIP, SCRATCHED_INITIALS, PHOTO_UNKNOWN_MAN, COLD_SPOT_CELLAR, CONSTRUCTION_RECORD) discovered on 2nd examinations; narrator slip at 3+ anomalies
- [x] **Feature 6: Moral Endgame Choice** -- 3 post-climax endings (Seal the Wall, Destroy the Tapes, Escape the Manor) with 4 new achievements (GUARDIAN, ARSONIST, SURVIVOR, CYCLE_BREAKER)
- [x] **Feature 7: Narrator vs Evidence Contradictions** -- 4 narrator contradiction types (NARRATOR_WEAPON, NARRATOR_BOOTS, NARRATOR_LETTER, NARRATOR_TIME); auto-discovered when distortions contradict evidence; 3+ reveals meta-insight in notebook

---

## 20. Three-Act Game Structure & Navigation Gates
*Priority: HIGH -- gates content progression*

- [x] **Act 1: The Investigation** -- 8 rooms accessible from start (Entrance, Study, Parlor, Kitchen, Guest Rooms, James's Room, Servants' Quarters, Groundskeeper's Shed)
- [x] **Act 2: The Cellar** -- Cellar door sealed until 4+ tapes watched AND 5+ evidence collected
- [x] **Act 3: Margaret's Room** -- Margaret's Room sealed until BREATHING_WALL anomaly discovered in cellar
- [x] **Mood-varied blocked door text** -- Narrator provides escalating warnings when player tries sealed doors
- [x] **Tape Damage Mechanic** -- Tape 8 found damaged in cellar (cracked casing, snapped ribbon); cannot be watched until repaired
- [x] **Tape Repair Kit** -- Found on Margaret's Room dresser (Act 3); enables Tape 8 playback
- [x] **Progression hints** -- HintSystem shows cellar gate progress (tapes/evidence needed) and [SEALED] tags for items behind locked doors
- [x] **Save/Load support** -- hasTapeRepairKit persisted in SaveLoadSystem

---

## 21. Mini-Game Systems
*Priority: MEDIUM -- adds interactive gameplay variety (was section 20)*

- [x] **Document Reconstruction Mini-Game** -- Drag-and-drop puzzle for torn/burned documents
  - Triggered when examining fireplace ashes in study (first examination)
  - 6 paper fragments with jagged edges and burn marks
  - Player drags fragments to correct positions on a desk
  - Pieces snap into place when within 30px of target
  - Completing reveals full text and awards TORN_LETTER evidence
  - Can cancel without collecting evidence (ESC or CANCEL button)
  - Programmatic visuals: paper textures, jagged edges, burn marks
- [ ] **Future mini-games** -- Tape repair, lock picking, safe cracking (not yet implemented)

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
- `Evidence.java` -- 10 evidence items enum
- `Tape.java` -- 8 tapes enum with room/object locations
- `Suspect.java` -- 5 suspects enum with cooperation levels
- `Contradiction.java` -- WEAPON, BODY_POSITION, NARRATOR_WEAPON, NARRATOR_BOOTS, NARRATOR_LETTER, NARRATOR_TIME enum
- `GameState.java` -- All mutable game state, tracking counters, save/load support
- `Achievement.java` -- 8 achievement definitions enum
- `EntityAnomaly.java` -- 7 entity anomaly types enum

### `systems/` package
- `AwarenessSystem.java` -- Threshold warnings, level names, color index
- `ExamResult.java` -- Data holder for examination results with MiniGameType enum
- `EvidenceSystem.java` -- Collection, James/Daniel counting, accusation validation
- `ExaminationSystem.java` -- Routes room+object+count to ExamResult
- `InterviewSystem.java` -- Topics, evidence reactions, contradictions, 4-tier cooperation, dangerous topics
- `NarratorSystem.java` -- Mood-shifting narrator with warnings, cues, commentary, atmospheric events
- `HintSystem.java` -- 3-tier progressive hints, 5 categories, per-suspect interview hints
- `SaveLoadSystem.java` -- JSON serialization with 3 save slots via LibGDX FileHandle
- `AchievementSystem.java` -- 8 achievements checked on game end (4 on correct accusation win: SPEEDRUN, GHOST, COMPLETIONIST, PERFECT_INVESTIGATION; 4 on moral ending choice: GUARDIAN, ARSONIST, SURVIVOR, CYCLE_BREAKER), shown in notebook

### `data/` package
- `TapeContent.java` -- 8 tape transcripts (mixed format: Harold's recording + police interviews + personal account + Arthur's death)
- `SuspectDialogue.java` -- Greetings, 27 topic responses, 26 evidence reactions, 25 false responses, 4 Margaret deflections, 5 accusation defenses
- `RoomDescriptions.java` -- Dynamic descriptions, examinable object lists
- `NarratorText.java` -- 4 moods, warning/cue/commentary text, atmospheric events, 6 mild distortions, 5 severe distortions, distortion-contradiction mapping
- `ClimaxContent.java` -- Tape 8 climax narrative (The Opening), variant climax intro, 3 moral ending texts

### `ui/` package
- `TextButton.java` -- Clickable button with hover/disabled states
- `TextPanel.java` -- Overlay panel with word-wrap, scroll, action buttons
- `AwarenessMeter.java` -- Thin top-edge strip with hover tooltip
- `ActionBar.java` -- Compact centered button cluster
- `DocumentPiece.java` -- Draggable paper fragment for document reconstruction
- `DocumentReconstructionGame.java` -- Document reconstruction mini-game overlay

### Modified files
- `Hotspot.java` -- Added EXAMINE type, objectName field
- `PlaceholderGenerator.java` -- Added examine icon generator
- `RoomManager.java` -- Added 25+ examine hotspots across 10 rooms
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

### UI Overhaul -- Art-First Minimal Interface

**Goal:** Remove cluttery placeholder overlays and redesign UI to let artwork speak. Minimal, art-first interface.

**Changes:**

- `GameScreen.java`:
  - Removed hotspot overlay rendering (arrow textures, door textures, examine magnifying glass icons). Hotspot click detection is unchanged -- bounds still work invisibly.
  - Removed `arrowTextures`, `doorTexture`, `examineIconTexture`, `uiPanelTexture`, `tooltipBackgroundTexture` fields and their generation/disposal.
  - Added single `pixelTexture` (1x1 white) for dynamic-sized drawing.
  - Room name: moved to top-left with text shadow, fades out after 3 seconds. Font scale reduced 1.8 -> 1.4. No background panel.
  - Room description: text shadow, positioned above action bar, fades out after 5 seconds. No background panel.
  - Tooltip: dynamic width based on text content, dark teal-black bg, muted gold border, cream text, clamped to screen bounds.
  - Cursor: hand cursor when hovering any hotspot, arrow cursor otherwise (`Gdx.graphics.setSystemCursor()`).
  - Fade timers reset on room navigation.
- `ActionBar.java`:
  - Bar height 45 -> 36, button height 35 -> 28, fixed 100px button width.
  - Button cluster centered horizontally (~540px) instead of stretching full width.
  - Bar background covers only the cluster + 12px padding.
  - Opacity reduced 0.85 -> 0.6.
  - Added `getBarHeight()` accessor for description positioning.
- `AwarenessMeter.java`:
  - Replaced 200x20 corner bar + text with 4px thin strip across full screen width at top edge.
  - Removed dark background panel and text label from default view.
  - Color gradient (green/yellow/orange/red) fills proportionally.
  - Added hover detection: centered tooltip (e.g. "12/80 DORMANT") appears when mouse near top edge.

### Document Reconstruction Mini-Game

**Goal:** Add interactive mini-game for discovering the torn letter evidence, rather than immediately awarding it on examination.

**New files:**

- `ui/DocumentPiece.java` -- Draggable paper fragment with position tracking, snap detection, jagged edge offsets
- `ui/DocumentReconstructionGame.java` -- Full mini-game overlay with:
  - 6 scattered paper fragments
  - Drag-and-drop mechanics
  - Desk target area
  - Snap-to-position when within 30px
  - Completion detection
  - Programmatic paper textures with burn marks and jagged edges
  - CANCEL button to exit without evidence

**Modified files:**

- `ExamResult.java` -- Added `MiniGameType` enum (NONE, TORN_LETTER_RECONSTRUCTION) and mini-game detection methods
- `ExaminationSystem.java` -- Changed ashes examination to trigger mini-game instead of directly awarding evidence
- `GameScreen.java`:
  - Added `documentGame` field and initialization
  - Added `pendingMiniGame` state tracking
  - Added touchDragged/touchUp handlers for drag mechanics
  - Added mini-game rendering layer
  - Modified handleExamine to detect and trigger mini-games
  - Added handleMiniGame() and startPendingMiniGame() methods

### Navigation Simplification -- Removed Servants' Staircase

**Goal:** Remove the unused servants' staircase that led nowhere, replacing it with a bedpost that hosts the A.H. anomaly discovery.

**Changes:**

- `RoomDescriptions.java`:
  - Changed examinable object from "staircase" to "bedpost" in SERVANTS_QUARTERS
  - Updated room description to mention bed with worn wooden bedpost
  - Changed display name from "Servants' Staircase" to "Wooden Bedpost"
- `ExaminationSystem.java`:
  - Changed "staircase" case to "bedpost" in examineServants()
  - Updated examination text to describe worn bedpost with scratches
  - A.H. anomaly (SCRATCHED_INITIALS) discovery relocated to bedpost
- `RoomManager.java`:
  - Changed examine hotspot from "staircase" to "bedpost" with adjusted position

### Evidence Relocation -- Bloodstained Shirt to Cellar

**Goal:** Fix plot hole where tape says "burn the shirt in the study" but shirt was found in Servants' Quarters. Move shirt to cellar where body was dragged.

**Changes:**

- `ExaminationSystem.java`:
  - Cellar flour_sacks (1st exam): Now finds BLOODSTAINED_CUFF hidden behind flour sacks
  - Servants' Quarters floorboard: Now empty (scratch marks show something was removed)

**Narrative consistency:** Tape 7 shows James and Daniel in the cellar with bloodstained sleeve. Daniel says to burn it, but James panicked and hid it behind the flour sacks instead. This matches the drag marks and cellar location.

### Story Consistency Overhaul -- Fixed 29 Narrative Inconsistencies

**Goal:** Eliminate all story contradictions and plot holes to create a fully coherent murder mystery narrative. Fixed character ages, murder weapon discrepancies, timeline conflicts, evidence location mismatches, and dangling plot threads.

**Story Decisions:**
- **Charles Webb**: 28 years old (company assistant, NOT family member)
- **James Vance**: 52 years old (middle-aged son)
- **Murder weapon**: Fireplace poker from study (blunt force trauma)
- **Murder time**: 10:45 PM (when Charles saw James heading to study)
- **Blackmail note**: James planted it in Margaret's room to frame her
- **Thomas's surname**: Changed from "Thomas Vance" to "Thomas Ashford" (business partner, not brother)

**Files Modified:**

1. **`state/Suspect.java`** -- Added age field to enum
   - Charles Webb: 28 years old
   - James Vance: 52 years old
   - Margaret Vance: 48 years old
   - Daniel: 63 years old
   - Marcus Blackwood: 55 years old

2. **`state/Evidence.java`** -- Updated evidence descriptions
   - Added `FIREPLACE_POKER` evidence item (the real murder weapon)
   - Updated `LETTER_OPENER` description to clarify it was planted as a red herring
   - Updated `BLACKMAIL_NOTE` description to explain James planted it to frame Margaret

3. **`state/Contradiction.java`** -- Fixed contradiction descriptions
   - `WEAPON`: Now explains fireplace poker vs letter opener (planted weapon)
   - `BODY_POSITION`: Clarified two-person body movement (James and Daniel, 11:30 PM - 2:00 AM)
   - `NARRATOR_TIME`: Fixed to show 10:45 PM murder time (not after midnight)

4. **`data/NarratorText.java`** -- Fixed name error
   - Line 142: Changed "Arthur" → "Harold" in severe distortion text

5. **`data/SuspectDialogue.java`** -- Major dialogue updates
   - Line 51: Updated Charles greeting to describe him as "young, maybe late twenties" (company assistant)
   - Added `JAMES_FIREPLACE_POKER` reaction (guilty admission: "I cleaned that thoroughly")
   - Added `JAMES_SLEEPING_POWDER` reaction (deflects to Daniel)
   - Updated `JAMES_BLACKMAIL_NOTE` reaction (overacting, was his plant)
   - Added `DANIEL_FIREPLACE_POKER` reaction (too casual, deflects to James)
   - Updated `DANIEL_BLACKMAIL_NOTE` reaction (recognizes handwriting forgery)
   - Added `MARGARET_BLACKMAIL_NOTE` reaction (confusion, mentions James planting it)
   - Added `CHARLES_FIREPLACE_POKER` reaction (forensic analysis, identifies real weapon)
   - Updated `CHARLES_SLEEPING_POWDER` reaction (explains drugging Harold's tea)
   - Updated `MARGARET_inheritance` response (learned about will AFTER death from Charles)
   - Added `MARGARET_insurance` response (clarifies insurance policy never existed, was James's false accusation)
   - Added "insurance" to Margaret's topic list

6. **`data/TapeContent.java`** -- Updated cellar recording
   - Added context note explaining Harold planted the recorder, timestamp 11:30 PM
   - Changed dialogue to explain why shirt is in cellar instead of burned:
     - "The fire's almost dead! There's barely any coals left."
     - "Then hide it. Behind the flour sacks in the cellar. We'll burn it tomorrow."

7. **`data/RoomDescriptions.java`** -- Added fireplace poker
   - Line 15: Added "poker" to study examinable objects list
   - Line 100: Added "Fireplace Poker" display name

8. **`systems/ExaminationSystem.java`** -- Added poker examination and window clarification
   - Lines 75-79: Added "poker" examination case in examineStudy()
     - 1st exam: Notices reddish-brown stain on weighted end
     - 2nd exam: Discovers blood traces in metalwork, awards FIREPLACE_POKER evidence
   - Lines 63-66: Updated "window" examination to explain Daniel's entry point on 2nd exam

9. **`GAME_DOCUMENTATION.md`** -- Fixed all documentation inconsistencies

**Narrative Consistency Achieved:**

All 29 identified story inconsistencies have been resolved:

✅ **Murder weapon** - Fireplace poker is consistently the real weapon across all files (letter opener is planted red herring)
✅ **Murder timeline** - 10:45 PM consistently referenced (narrator's "after midnight" is intentional misdirection)
✅ **Character ages** - All ages documented and consistent (Charles 28, James 52, Margaret 48, Daniel 63, Marcus 55)
✅ **Charles's identity** - Consistently described as 28-year-old company assistant, not family member
✅ **Blackmail note purpose** - Clearly explained as James's plant to frame Margaret
✅ **Insurance policy** - Clarified as James's false accusation (never existed)
✅ **Bloodstained shirt location** - Explained why it's in cellar (fire too low to burn it)
✅ **Sleeping powder** - Context added (James drugging Harold to make him vulnerable)
✅ **Margaret's will knowledge** - Timeline fixed (learned AFTER Harold's death from Charles)
✅ **Body movement** - Clarified as two-person job (James and Daniel)
✅ **Window entry** - Explained as Daniel's entry point to help move body
✅ **Thomas's identity** - Changed surname to Ashford (business partner, not brother of Harold)

The game now has a fully coherent murder mystery with zero unintentional plot holes. All files (Java source code and markdown documentation) point to the same consistent story.

### Additional Story Refinements -- Character Development & Spatial Logic

**Goal:** Further refine character backstories, fix spatial inconsistencies in room layout, and improve suspect dynamics for better mystery gameplay.

**Changes Made:**

1. **Margaret's Financial Independence** -- Added successful business background. Makes James's false accusation that she's "desperate for money" obviously a lie, reducing her as a suspect and adding character depth.
2. **Groundskeeper Shed Relocation** -- Improved spatial logic for room connections. A groundskeeper's workspace makes more sense connected to staff living quarters (servants' quarters) than the kitchen — more logical for a staff area layout.
3. **Daniel's Knowledge Limitation** -- Removed anachronistic plot hole. Removed "knows about Thomas Ashford's tomb" from Daniel's description. The tomb is a secret horror discovery — if Daniel knew about it from decades of working at the manor, it wouldn't be mysterious. Thomas's entombment must remain a hidden secret.
4. **Marcus Witness Change** -- Fixed accomplice providing alibi problem. Daniel is James's accomplice — it doesn't make sense for him to voluntarily provide an alibi for Marcus and narrow the suspect pool. Charles (the honest witness) is now the source for Marcus's departure time.
5. **Marcus as Legitimate Suspect** -- Removed premature spoilers. Marcus has motive (£2M lawsuit), opportunity (was at the manor during the murder), and presence (left 15 minutes after the murder). He should be a viable suspect until investigation reveals overwhelming evidence against James and Daniel. Presenting him as automatically innocent ruins the mystery.
6. **Embezzlement Discovery Fix** -- Fixed major plot hole. If Charles knew about the embezzlement, killing Harold doesn't solve James's problem (Charles could still report it). Changed so Harold discovered it himself 2–3 days before the murder and told no one — killing Harold then eliminates the only witness and actually solves James's problem. Timeline changed from "weeks" to "days" to match the urgency of a desperate last-minute conspiracy.
7. **Margaret's Innocence Fix** -- Removed false alibi to keep Margaret completely innocent. Margaret never interacted with James that night — she only heard sounds (footsteps, dragging). Leftover text from an earlier design where she provided a false alibi was removed, making her a pure witness with no suspect status.
8. **Arthur Timeline Contradiction Fix** -- Removed impossible reference to prior investigators. Arthur is the FIRST investigator (1987). He released the Entity and became the first Narrator. All other investigators came after him — a line saying he "discovers records of other investigators who came before" was a direct timeline contradiction and was cut.
9. **Margaret Timeline Cleanup** -- Fixed remaining reference to James visiting Margaret at 2:00 AM. Margaret never interacted with James that night — she only heard sounds. Leftover text ("Confused, remembers James's visit") changed to ("Terrified, remembers the sounds from the night before").

**Verification:**
✅ All changes build successfully
✅ Story consistency maintained across all files
✅ Character motivations are now more logical
✅ Spatial layout makes architectural sense
✅ Mystery gameplay improved (Marcus is proper suspect, Margaret has stronger characterization)
✅ Plot holes fixed (embezzlement discovery, Marcus witness, shed location)

### Tape System Restructure -- 7 Tapes to 8 Tapes with Mixed Format

**Goal:** Replace the original 7 Harold-only tape recordings with 8 mixed-format tapes: 1 Harold recording, 5 police interview recordings, 1 Margaret personal account, and 1 Arthur Hollis death recording as the climax trigger.

**Why the old 7-tape structure had narrative problems:**
- Why would Harold record the rivals' phone call or Margaret's confession? He wasn't present for those events.
- How did all the tapes get hidden around the manor if Harold died before he could hide them?
- The cellar recording (James and Daniel moving the body) was anticlimactic as the final tape — it revealed the killers but had no horror payoff.

**How the new 8-tape structure fixes these:**
- Tape 1 is Harold's own recorder — the only one that makes sense for him to plant
- Tapes 2–6 are official police interview recordings obtained by Arthur — explains how they exist and why they're scattered
- Tape 7 is Margaret's personal testimony, deliberately left for the detective
- Tape 8 is Arthur Hollis's death recording — a far more powerful climax trigger that reveals the Entity rather than just confirming the killers
- Arthur hid all the tapes before disappearing, creating a trail for the next investigator

**New Tape Structure:**

1. **Tape 1: Harold & James Argument** - Harold's hidden recorder captures their fight about embezzlement and the will
2. **Tape 2: James Vance - Police Interview** - James deflects, lies about alibi, tries to blame Margaret
3. **Tape 3: Daniel Hobbs - Police Interview** - Daniel nearly slips up: "I was helping move—" before catching himself
4. **Tape 4: Margaret Vance - Police Interview** - Margaret heard two people dragging something at 2 AM
5. **Tape 5: Marcus Blackwood - Police Interview** - Marcus confirms alibi (left at 11 PM) and what he heard
6. **Tape 6: Charles Webb - Police Interview** - Charles saw James heading to study at 10:45 PM
7. **Tape 7: Margaret's Personal Account** - Margaret's conflicted recording left "For the detective"
8. **Tape 8: The Opening** - Arthur Hollis's final recording before Entity kills him (**CLIMAX TRIGGER**)

**Files Modified:** 11 Java files + 4 documentation files

**Verification:**
✅ All 17 files updated and verified with 3-pass scanning
✅ Story consistency maintained - new tapes fit narrative perfectly
✅ Climax trigger now reveals Entity origin (Arthur's death)
✅ Police interviews provide more realistic evidence format
✅ Margaret's personal account adds emotional depth

### Three-Act Structure, Channeling Mechanic & Tape Repair

**Goal:** Add narrative progression gates, narrator channeling mechanic for suspect interviews, and tape damage/repair mechanic.

**Changes Made:**

1. **Three-Act Navigation Gates (GameScreen.java)**
2. **Narrator Channeling Mechanic**
3. **Tape Damage & Repair Mechanic**
4. **HintSystem.java Updates**
5. **MD Documentation Updates**

**Files Modified:** 9 Java files + 4 documentation files

**Verification:**
✅ Three-act progression gates implemented and tested
✅ Channeling mechanic preserves narrator mystery (no identity reveal)
✅ Past tense narration consistent across all suspect interactions
✅ Tape repair gate creates natural Act 2 -> Act 3 -> Climax progression
✅ Save/load correctly persists tape repair kit state

---

# UPDATE_PLAN.md

# Comprehensive Story Update Plan - 8 Tape Structure

## Overview

This plan updates the game from 7 Harold-only tapes to 8 tapes (1 Harold + 5 police interviews + 1 Margaret personal + 1 Arthur's death). Arthur.MD is the source of truth containing complete tape transcripts, character details, and evidence fixes.

**Tape Structure Change:**
- **OLD (7 tapes)**: All Harold's secret recordings
- **NEW (8 tapes)**: Mixed format - Harold's tape, police interviews, Margaret's account, Arthur's death
- **Climax trigger**: Changed from Tape 7 (CELLAR_NOISES) to Tape 8 (ARTHUR_DEATH)

## Complete File Change List

After comprehensive scanning, **17 files** require updates:

### Core Java Files (12 files)

1. **Tape.java** - Enum definition and locations
2. **TapeContent.java** - All tape transcripts
3. **Evidence.java** - Fix MUDDY_BOOTS, BLACKMAIL_NOTE descriptions
4. **Suspect.java** - Add age field
5. **Contradiction.java** - Update WEAPON, BODY_POSITION, NARRATOR_TIME
6. **ExaminationSystem.java** - 8 tape returns, add Kitchen storage_cellar
7. **EvidenceSystem.java** - Update tape references in accusation logic
8. **HintSystem.java** - Update tape name/location hints
9. **GameScreen.java** - Change "/7" to "/8" (10+ locations), climax trigger
10. **ClimaxContent.java** - Change "Tape 7" to "Tape 8" constants + file comment
11. **SuspectDialogue.java** - Charles age, evidence reactions
12. **NarratorText.java** - Fix "Arthur"→"Harold"

### Documentation Files (5 files)

13. **GAME_DOCUMENTATION.md** - 20+ locations need updating
14. **README.md** - Change "7 hidden tapes" to "8"
15. **TODO.md** - Update tape sections, add changelog
16. **art/rooms/ROOM_ART_GUIDE.md** - Verify tape locations in art descriptions, add Kitchen reference
17. **UPDATE_PLAN.md** - This file (for reference)

### Files NOT Needing Updates (Verified)

- **AchievementSystem.java** - Uses dynamic `Tape.values().length` ✅
- **SaveLoadSystem.java** - Uses dynamic `Tape.valueOf()` ✅
- **NarratorSystem.java** - No tape references ✅
- **InterviewSystem.java** - No tape references ✅
- **RoomManager.java** - No tape references ✅
- **NAVIGATIONGUIDE.MD** - Generic mentions only ✅

---

## Implementation Phase Order (Why This Sequence)

- **Phase 1 first:** All other systems reference the core enums. Update enums before touching any system that uses them.
- **Phase 2 second:** Content files don't affect game logic, but must match the new enum values from Phase 1.
- **Phase 3 third:** Game logic depends on correct enums and content — do these after Phases 1–2.
- **Phase 4 last:** Documentation reflects implemented changes. Update after the code is working and verified.

## Phase 1: Core Enums (Critical - Do First)

### 1.1 Tape.java

**Location**: `core/src/main/java/com/dsa/game/state/Tape.java`

**Changes**: Replace entire enum with new 8-tape structure

```java
public enum Tape {
    TAPE_ARGUMENT("Harold & James Argument", Room.RoomID.STUDY, "under_desk"),
    TAPE_JAMES_INTERVIEW("James Vance - Police Interview", Room.RoomID.STUDY, "bookshelves"),
    TAPE_DANIEL_INTERVIEW("Daniel Hobbs - Police Interview", Room.RoomID.GROUNDSKEEPER_SHED, "logbook"),
    TAPE_MARGARET_INTERVIEW("Margaret Vance - Police Interview", Room.RoomID.KITCHEN, "storage_cellar"),
    TAPE_MARCUS_INTERVIEW("Marcus Blackwood - Police Interview", Room.RoomID.PARLOR, "briefcase"),
    TAPE_CHARLES_INTERVIEW("Charles Webb - Police Interview", Room.RoomID.PARLOR, "grandfather_clock"),
    TAPE_MARGARET_ACCOUNT("Margaret's Personal Account", Room.RoomID.MARGARET_ROOM, "dresser"),
    TAPE_ARTHUR_DEATH("The Opening", Room.RoomID.CELLAR, "wine_rack");

    private final String title;
    private final Room.RoomID hiddenInRoom;
    private final String hiddenInObject;

    Tape(String title, Room.RoomID hiddenInRoom, String hiddenInObject) {
        this.title = title;
        this.hiddenInRoom = hiddenInRoom;
        this.hiddenInObject = hiddenInObject;
    }

    public String getTitle() { return title; }
    public Room.RoomID getHiddenInRoom() { return hiddenInRoom; }
    public String getHiddenInObject() { return hiddenInObject; }
}
```

### 1.2 Suspect.java

**Changes**: Add age field to enum

```java
public enum Suspect {
    JAMES("James Vance", 52, 70),
    MARGARET("Margaret Vance", 48, 60),
    DANIEL("Daniel the Groundskeeper", 63, 50),
    MARCUS("Marcus Blackwood", 55, 55),
    CHARLES("Charles Webb", 28, 70);

    private final String displayName;
    private final int age;
    private final int startingCooperation;

    Suspect(String displayName, int age, int startingCooperation) {
        this.displayName = displayName;
        this.age = age;
        this.startingCooperation = startingCooperation;
    }

    public String getDisplayName() { return displayName; }
    public int getAge() { return age; }
    public int getStartingCooperation() { return startingCooperation; }
}
```

### 1.3 Evidence.java

**Changes**: Fix two evidence descriptions

**MUDDY_BOOTS**: Daniel's work boots, caked with dirt and cellar dust. Fresh mud tracked through the manor the night of the murder.

**BLACKMAIL_NOTE**: A blackmail note with rough handwriting, planted in Margaret's room. The handwriting is deliberately crude, mimicking Daniel's style. James planted this to frame Margaret.

### 1.4 Contradiction.java

**Changes**: Update 3 contradiction descriptions

**WEAPON**: The letter opener doesn't match the wound pattern -- it was planted. The fireplace poker shows blood traces matching Harold's skull fracture.

**BODY_POSITION**: Harold was moved after death. It took two people to move him -- James and Daniel working together between 11:30 PM and 2:00 AM.

**NARRATOR_TIME**: The narrator claimed Harold died after midnight, but Charles saw James heading to the study at 10:45 PM.

---

## Phase 2: Content Files (Tape Transcripts & Dialogue)

### 2.1 TapeContent.java

**Changes**: Replace ALL 7 tape transcripts with 8 new transcripts from Arthur.MD

All 8 tape transcripts sourced from Arthur.MD lines 118-730.

**Status**: All 8 transcripts complete and implemented.

### 2.2 SuspectDialogue.java

**Changes**: Multiple dialogue updates including Charles greeting, fireplace poker reactions, blackmail note reactions, sleeping powder reactions, Margaret's inheritance and insurance responses.

### 2.3 NarratorText.java

**Changes**: Fix one name error - "Arthur" → "Harold" in severe distortion text.

---

## Phase 3: System Files (Game Logic)

### 3.1 ExaminationSystem.java - CRITICAL

**Changes**: Update 7 tape returns + add Kitchen examination

- Study bookshelves: TAPE_WILL_READING → TAPE_JAMES_INTERVIEW
- Parlor briefcase: TAPE_RIVALS_CALL → TAPE_MARCUS_INTERVIEW
- Parlor grandfather_clock: TAPE_PHONE_CALL → TAPE_CHARLES_INTERVIEW
- Guest Rooms margarets_room: TAPE_MARGARET_CONFESSION → TAPE_MARGARET_ACCOUNT
- Shed logbook: TAPE_DANIEL_MEETING → TAPE_DANIEL_INTERVIEW
- Cellar wine_rack: TAPE_CELLAR_NOISES → TAPE_ARTHUR_DEATH
- Kitchen storage_cellar: NEW location for Tape 4

### 3.2 GameScreen.java - CRITICAL

**Changes**: 13 locations need updates (all "/7" → "/8" and climax trigger)

### 3.3 ClimaxContent.java

**Changes**: Rename constants from TAPE_7 to TAPE_8, update comments.

### 3.4 EvidenceSystem.java

**Changes**: Update tape references in accusation requirements.

### 3.5 HintSystem.java

**Changes**: Update tape name and location references in hints.

---

## Phase 4: Documentation Files

### 4.1 GAME_DOCUMENTATION.md - EXTENSIVE UPDATES

Complete tape table rewritten, all room descriptions updated, character profiles updated.

### 4.2 README.md

"7 hidden tapes" → "8 hidden tapes"

### 4.3 TODO.md

Tape system section updated, new changelog entry added.

### 4.4 art/rooms/ROOM_ART_GUIDE.md

Tape location references verified and updated.

---

## Implementation Order

### Step 1: Core Enums (MUST DO FIRST)
1. Tape.java - New 8-tape structure
2. Suspect.java - Add ages
3. Evidence.java - Fix descriptions
4. Contradiction.java - Update descriptions

### Step 2: Content Files
5. TapeContent.java - All 8 transcripts (copy from Arthur.MD)
6. SuspectDialogue.java - Charles age, evidence reactions
7. NarratorText.java - "Arthur"→"Harold" fix

### Step 3: System Files (Critical Path)
8. ExaminationSystem.java - 8 tape returns + Kitchen
9. GameScreen.java - All "/7"→"/8" and climax trigger
10. ClimaxContent.java - "Tape 7"→"Tape 8"
11. EvidenceSystem.java - Update tape references
12. HintSystem.java - Update tape hints

### Step 4: Documentation
13. GAME_DOCUMENTATION.md - 20+ locations throughout file
14. README.md - Update feature list
15. TODO.md - Update tape sections + changelog
16. art/rooms/ROOM_ART_GUIDE.md - Verify tape locations
17. UPDATE_PLAN.md - Update this file's status

---

## Verification Checklist

### Critical Path Testing
- [ ] Game loads without errors
- [ ] All 8 tapes have correct titles in inventory
- [ ] All 8 tapes can be found in correct locations
- [ ] All 8 tapes play correct transcripts
- [ ] Tape 8 triggers climax (not Tape 7)
- [ ] Tape 8 costs +5 awareness (not +4)
- [ ] Character ages display correctly
- [ ] Charles described as 28 years old
- [ ] Kitchen storage_cellar examination finds Tape 4

### Evidence Chain Testing
- [ ] MUDDY_BOOTS mentions "Daniel's boots"
- [ ] BLACKMAIL_NOTE mentions "James planted this"
- [ ] Present fireplace poker to James → guilty reaction
- [ ] Present blackmail note to Margaret → confusion
- [ ] Margaret's inheritance response shows correct timing

### Tape Content Testing
- [ ] Tape 1: Harold & James Argument (Harold's recording)
- [ ] Tape 2: James police interview (deflections, alibi)
- [ ] Tape 3: Daniel police interview (half-truths)
- [ ] Tape 4: Margaret police interview (what she heard)
- [ ] Tape 5: Marcus police interview (alibi, observations)
- [ ] Tape 6: Charles police interview (timeline, 10:45 PM)
- [ ] Tape 7: Margaret's personal account (conflicted, uncertain)
- [ ] Tape 8: Arthur's death (triggers climax)

### UI Testing
- [ ] Inventory shows "X/8 tapes"
- [ ] All UI locations showing "/7" now show "/8"
- [ ] Climax sequence triggers on Tape 8 (The Opening)
- [ ] No references to old tape names in UI

### Documentation Testing
- [ ] GAME_DOCUMENTATION.md has new 8-tape table
- [ ] README.md says "8 hidden tapes"
- [ ] TODO.md updated with changelog entry
- [ ] Character ages listed in profiles

---

## Risk Assessment

**Low Risk**: Content changes (dialogue, descriptions), documentation updates, UI text changes

**Medium Risk**: Tape.java enum renaming, ExaminationSystem.java returns, ClimaxContent.java trigger change

**High Risk**: Suspect.java constructor change (affects save system)
- **Mitigation**: Age field added to end of constructor; existing saves should load but may not have ages

**Critical Path Dependencies**:
1. Tape.java MUST be updated before any system file
2. ExaminationSystem.java MUST return correct tapes or progression breaks
3. GameScreen.java MUST check TAPE_ARTHUR_DEATH or climax breaks
4. TapeContent.java MUST have all 8 transcripts or tapes show "No transcript"

---

## Rollback Plan

**Before implementation**:
1. Commit current working state to git
2. Create backup branch: `git checkout -b backup-7-tape-structure`

**If errors occur**:
1. Each phase is independently reversible
2. Enum changes can be reverted file-by-file
3. Content files can be restored from backup
4. Documentation can be re-edited

**Save compatibility**:
- Existing saves may load but won't have the new tape structure
- Recommend starting a new game after the update
- SaveLoadSystem uses dynamic `Tape.valueOf()` so it should handle gracefully

---

## Notes

- **Tape 8 content**: Arthur.MD has Tapes 1-7 complete. Tape 8 is Arthur's death recording - fully written.
- **Kitchen examination**: storage_cellar case added to ExaminationSystem.java's examineKitchen() method.
- **Character ages**: Only used in display text, not in game logic.
- **Evidence reactions**: New fireplace poker, blackmail note, and sleeping powder reactions added.

---

## Estimated Time (UPDATE_PLAN.md)

- Phase 1 (Core Enums): 2 hours
- Phase 2 (Content Files): 3 hours (depends on Tape 8 content)
- Phase 3 (System Files): 4 hours
- Phase 4 (Documentation): 2 hours
- Testing & QA: 3 hours
- **Total**: 14 hours

---

**Status**: ✅ **COMPLETE** - All 17 files successfully updated and verified (2026-02-09)

**Last Updated**: 2026-02-09 - After comprehensive 3-pass scan of entire codebase

---

## Implementation Complete - Verification Summary

All 17 files have been successfully updated from 7 tapes to 8 tapes with full verification:

### Phase 1: Core Enums ✅
1. ✅ **Tape.java** - Complete enum rewrite (7→8 tapes, new names, added KITCHEN location)
2. ✅ **Suspect.java** - Already had age field correctly implemented
3. ✅ **Evidence.java** - MUDDY_BOOTS description updated (James→Daniel)
4. ✅ **Contradiction.java** - Already had correct updates (fireplace poker, timing, two people)

### Phase 2: Content Files ✅
5. ✅ **TapeContent.java** - Complete rewrite (100→317 lines, all 8 new transcripts from Arthur.MD)
6. ✅ **SuspectDialogue.java** - Charles greeting updated to "around twenty-eight", all evidence reactions verified
7. ✅ **NarratorText.java** - Already correct (no "Arthur" errors found)

### Phase 3: System Files ✅
8. ✅ **ExaminationSystem.java** - All 8 tape returns updated, Kitchen storage_cellar added
9. ✅ **EvidenceSystem.java** - Accusation logic updated (3 tape name changes)
10. ✅ **HintSystem.java** - All tape hints updated (3 tape names, 1 location change)
11. ✅ **GameScreen.java** - All "/7"→"/8" changes (7 locations), climax trigger updated (3 locations)
12. ✅ **ClimaxContent.java** - Comments updated ("Tape 7"→"Tape 8" in 2 locations)

### Phase 4: Documentation Files ✅
13. ✅ **GAME_DOCUMENTATION.md** - Complete tape table rewritten, all room descriptions updated, 15+ locations changed
14. ✅ **README.md** - "7 hidden tapes"→"8 hidden tapes"
15. ✅ **TODO.md** - Tape system section updated, new changelog entry added
16. ✅ **art/rooms/ROOM_ART_GUIDE.md** - Servants' Quarters updated (bedpost, empty floorboard), Cellar updated (shirt location)
17. ✅ **UPDATE_PLAN.md** - This file (status updated to COMPLETE)

### Key Changes Summary
- **Tape count**: 7 → 8 tapes
- **Tape format**: Harold-only → Mixed (1 Harold + 5 police interviews + 1 personal + 1 Arthur's death)
- **Climax trigger**: Tape 7 (CELLAR_NOISES) → Tape 8 (ARTHUR_DEATH "The Opening")
- **New tape**: Tape 4 (Margaret Vance - Police Interview) in KITCHEN storage_cellar
- **Evidence logic**: Updated to use new tape names for James/Daniel accusation requirements
- **All documentation**: Synchronized with new 8-tape structure

**Implementation Status**: Ready for testing and deployment 🎉

---

# Arthur.MD

Arthur Hollis's Investigation (1987) - REVISED LORE
Week 1: The Routine Investigation (Days 1-7)

Day 1 - Arthur Arrives

- Arthur Hollis, private investigator, is hired to solve Harold Vance's murder
- The police investigation stalled - no hard evidence, conflicting statements
- Hired by [Margaret, who suspects James but wants proof / or Charles, who knows something is wrong]
- Arrives at Vance Manor, sets up in the servants' quarters

Days 2-4 - Interviews

- Arthur interviews all five suspects:
  - James: Defensive about the will and finances, claims he was in his room after the argument
  - Margaret: Nervous, mentions hearing footsteps and dragging sounds at 2 AM
  - Daniel: Too calm, his logbook has a missing entry for November 15th
  - Marcus: Clean alibi (left at 11 PM, hotel confirms)
  - Charles: Most helpful, saw James heading to study at 10:45 PM
- Arthur suspects James and Daniel but needs concrete proof

Days 5-7 - Physical Evidence Collection

- Examines study (crime scene): finds letter opener (doesn't match wound pattern)
- Examines cellar (where body was found): blood pooling suggests body was moved
- Searches Daniel's shed: finds muddy boots hidden on shelf
- Searches cellar more thoroughly: finds bloodstained shirt behind flour sacks
- Strong circumstantial evidence, but not enough for conviction

Week 2: The Tapes (Days 8-14)

Day 8 - First Discovery

- While examining Harold's desk thoroughly, Arthur finds a tape recorder hidden underneath
- Label: "Argument - November 15th"
- Arthur plays it: Harold and James arguing about embezzlement, will changes
- This is the only tape recorder Harold planted

Day 9 - Documents and Police Evidence

- Arthur finds legal documents in Harold's office:
  - New will draft: James gets nothing, Margaret inherits everything
  - Codicil: Daniel to be dismissed immediately
  - Will was scheduled to be signed November 16th at 9:00 AM
- Arthur requests access to police evidence from initial investigation

Days 10-11 - Police Interview Recordings

- Police recorded all suspect interviews on video at the station
- Arthur obtains 5 police interview tapes:
  - Tape 2: James's Police Interview
  - Tape 3: Daniel's Police Interrogation
  - Tape 4: Margaret's Police Interview
  - Tape 5: Marcus's Police Interview
  - Tape 6: Charles's Police Interview

Day 12 - Margaret's Personal Recording

- While searching the guest rooms, Arthur finds a tape labeled "For the Detective"
- Margaret recorded her own witness account days after the police interview
- She provides more detail and directly accuses James and Daniel

Day 13 - Piecing It Together

- Arthur reviews all the evidence:
  - Harold's tape: Motive (embezzlement, will changes)
  - Police interviews: Daniel's slip-up ("helping move something"), weak alibis
  - Margaret's testimony: Heard two people, dragging sounds at 2 AM
  - Charles's testimony: Saw James heading to study at 10:45 PM
  - Physical evidence: Bloodstained shirt in cellar, muddy boots in shed, fireplace poker
- The case is solved: James and Daniel conspired to kill Harold
- Arthur has enough evidence to convict them

Day 14 - Preparing the Report

- Arthur begins writing his final report
- Plans to turn over all evidence to police tomorrow
- Reviews the cellar one more time for completeness
- That's when he notices it: the wall

Week 3: The Wall (Days 15-21)

Day 15 - The Anomaly

- The cellar wall behind the wine rack looks... wrong
- The bricks are a different color, slightly newer
- There's a seam behind the bookshelf that shouldn't be there
- Arthur searches Harold's office for construction records

Day 16 - The Documents

- Finds construction documents from 1957
- A wall was added to the cellar - decades after the house was built
- Permits filed by Harold Vance personally
- The same year Vance Pharmaceutical was founded
- The same year Thomas Ashford vanished from public records

Day 17-18 - The Research

- Arthur digs through old business documents
- Harold and Thomas founded the company as equal partners in 1957
- Thomas owned 50% of the company
- Thomas disappeared in 1957 - no death certificate, no obituary, no body
- His shares were transferred to Harold (legal documents show "abandoned partnership")
- Arthur realizes: Thomas never left. Harold entombed him.

Day 19 - The Opening (ALL IN ONE DAY - ONE CONTINUOUS RECORDING)

Morning - The Discovery:
- Arthur finds the seam behind the bookshelf more clearly
- The wall is hollow - he can tell by tapping on it
- He's fascinated by the construction, the mystery of what happened in 1957
- He decides to document his findings

Arthur starts his tape recorder (for his investigation records)

---

[TAPE 8: The Opening - Arthur's Final Recording]

ARTHUR (to recorder): "November 19th, 1987, 10:30 AM. Arthur Hollis. I'm documenting my findings regarding a false wall in the cellar of Vance Manor. Construction records show this wall was added in 1957 by Harold Vance, the same year his business partner Thomas Ashford vanished. The wall appears hollow, approximately six feet by eight feet sealed space behind it. I'm going to examine the seam more closely—"

[Sound of tapping on brick]

ARTHUR: "The mortar here is definitely newer than the rest of the cellar. Someone sealed this deliberately. Given the timeline and Thomas Ashford's disappearance, I believe—"

[Pause. Arthur stops.]

ARTHUR (quieter): "Wait. Did I just... there was a sound. From inside the wall."

[Long silence. Then, faintly, a voice from behind the wall.]

VOICE (muffled, weak): "Hello? Is someone there?"

[Arthur's breathing quickens.]

ARTHUR: "...Yes. Yes, I'm here. Who is this? Who's in there?"

THOMAS (through the wall, sounding human and desperate): "My name is Thomas. Thomas Ashford. Please... how long has it been? Is there light outside?"

ARTHUR (shocked): "Thomas Ashford? You're Harold Vance's business partner?"

THOMAS: "Yes! Yes, that's right. Harold and I... we had a disagreement. He locked me in here. I've been trapped... I don't know how long. Days? Weeks? Please, is Harold still here? Can you tell him I'm sorry? I just want to get out."

ARTHUR: "Mr. Ashford... Harold is dead. He was murdered two weeks ago."

THOMAS (long pause): "...Dead? Harold is dead?"

ARTHUR: "I'm investigating his murder. I'm a private investigator. How long have you actually been in there?"

THOMAS: "I... I don't know. It's been dark for so long. I can't see anything. Please, you have to let me out. I can help you with the investigation. I knew Harold better than anyone. I can tell you about his enemies, his business dealings. Just please... open the wall."

ARTHUR: "This doesn't make sense. The construction documents say this wall was built in 1957. That's thirty years ago."

THOMAS: "Thirty... thirty years?" (voice breaking) "No. No, that can't be right. It's been dark, but not... Please. Please, I just want to see the sun again. I'll tell you everything about Harold. Just let me out."

ARTHUR: "I need to call the police first. If you've been imprisoned—"

THOMAS: "NO! No police. Not yet. Please. I don't want them to see me like this. I must look... I haven't seen myself in so long. Please, just open the wall first. Let me clean up, regain my dignity. Then you can call whoever you want. Please. I'm begging you."

ARTHUR (hesitating): "...I need to get tools. This will take some time to break through."

THOMAS: "Thank you. Thank you. I knew someone would come eventually. Someone kind. Someone who would listen."

[Sound of Arthur leaving. 15 minutes pass. Sound of Arthur returning with tools.]

ARTHUR: "Mr. Ashford, I'm back. I'm going to start breaking through the wall now. Stay back from the opening if you can."

THOMAS: "Yes. Yes, I'm ready. Please hurry."

[Sound of tools scraping brick. Chipping. Breaking.]

ARTHUR (breathing heavily): "The mortar is old... brittle... it's breaking apart faster than I expected—"

[A crack. Then a louder crack.]

THOMAS (voice getting louder as the wall breaks): "You're doing wonderfully. Just a bit more. I can feel the air getting through."

[More scraping. Then a crack. Then a louder crack.]

ARTHUR: "The seal is breaking. Mr. Ashford, step back from the wall if you can—"

[Sound of brick collapsing. A rush of stale air. Then silence.]

ARTHUR (breathing heavily): "Mr. Ashford? Can you—"

[Then Arthur sees what's behind the wall.]

ARTHUR: "Oh god. Oh god, what—"

THOMAS (voice changing, no longer weak): "Thirty years. Thirty years I've been waiting. Waiting for someone compassionate enough. Someone who would listen."

ARTHUR: "You're not— you're not human anymore—"

THOMAS: "I WAS human. Harold made me into THIS."

[Sound of movement. Stone scraping. Something emerging.]

ARTHUR (panicking): "Stay back! I'm warning you—"

THOMAS: "Thank you, Arthur Hollis. Thank you for freeing me."

[Arthur drops the recorder. Footsteps running. Then—]

ARTHUR (screaming): "NOOOO—!"

[The scream cuts off abruptly. Then silence. Then heavy breathing that isn't human. Then static.]

[END OF RECORDING]

---

The Aftermath

What Was Found:

- Arthur's body was never recovered
- His initials "A.H." were found carved into the bedpost in servants' quarters (he carved them earlier in his investigation)
- The 7 tapes (1 from Harold, 5 police interviews, 1 from Margaret) remained where Arthur had organized them
- Tape 8 (Arthur's final recording) was found on the floor near the opened wall
- The wall was found open, the space behind it empty
- The case went cold - Arthur disappeared, James and Daniel were never charged

What Actually Happened:

- Thomas/The Entity consumed Arthur
- Arthur's consciousness became trapped as the first Narrator
- The Entity began learning to mimic human behavior more convincingly
- The cycle began - other investigators would come, find the tapes, discover the truth, and die

---

The 8 Tapes - Complete List:

1. Harold & James Argument (Harold's recorder, Study) - Motive, embezzlement confrontation
2. James's Police Interview (Police station recording) - Weak alibi, defensive behavior
3. Daniel's Police Interrogation (Police station recording) - CRITICAL SLIP-UP: "helping move something"
4. Margaret's Police Interview (Police station recording) - Heard two people, dragging at 2 AM
5. Marcus's Police Interview (Police station recording) - Alibi confirmed, left at 11 PM
6. Charles's Police Interview (Police station recording) - Saw James at 10:45 PM heading to study
7. Margaret's Witness Account (Margaret's personal recording) - Directly accuses James and Daniel
8. The Opening (Arthur's recorder, Cellar) - Captures Arthur's death

How the Case is Solved:

- Only 1 tape from Harold (not 6-7) - more realistic
- Police interviews provide alibis, timeline, and Daniel's crucial slip-up
- Margaret's two recordings (police + personal) establish witness testimony
- Physical evidence corroborates: bloodstained shirt, muddy boots, fireplace poker
- Charles's testimony places James at the scene
- Cumulative evidence proves James and Daniel conspired to murder Harold
- No single "smoking gun" tape - solved through building a complete case

Key Points:

- Arthur solved the murder by Day 13 through careful evidence collection
- He should have stopped there and called the police
- His curiosity about the wall doomed him
- The entire wall investigation (Day 19) happened in ONE day and was captured in ONE recording
- Arthur started recording to document his findings, not knowing it would capture his death
- Thomas manipulated Arthur by sounding human, weak, and desperate
- The recording captures the moment Arthur realized his mistake - but it was too late

---

## DETAILED TAPE TRANSCRIPTS

### Tape 1: Harold & James Argument
**Location**: Study, hidden under desk
**Source**: Harold's personal tape recorder (deliberately planted)
**Date**: November 15th, 1987, 10:00 PM

---

[Sound of a door opening]

HAROLD: "James. Sit down. We need to talk."

JAMES: "Father, I'm tired. Can this wait until—"

HAROLD: "No. It can't wait. I've seen the accounts, James. Don't insult me by denying it. Fifty thousand pounds, gone."

JAMES: (pause) "Father, I can explain—"

HAROLD: "Explain? Explain how fifty thousand pounds vanished into an account I've never heard of? Explain how you thought I wouldn't notice?"

JAMES: "It was a loan. An investment. I was going to pay it back—"

HAROLD: "With what? The company money you've been siphoning for the past two years? I'm changing the will, James. Tomorrow morning at nine o'clock."

JAMES: (voice rising) "You can't do that. You wouldn't."

HAROLD: "Watch me. You'll get nothing. Not a penny. The entire estate goes to Margaret. And don't think your friend Daniel can help you this time. I know about your arrangement."

JAMES: "What arrangement? Daniel works for you—"

HAROLD: "Daniel works for YOU. Moving money. Hiding transactions. I've seen his logbook. The entries that conveniently go missing. You're both done."

JAMES: "Father, please—"

HAROLD: "Get out of my study. And tell Daniel he's dismissed. I want him off the property by week's end."

[Sound of a door slamming]

[Recording continues for 30 seconds - Harold's heavy breathing, then click]

---

### Tape 2: James's Police Interview
**Location**: Police station
**Source**: Standard police interview recording
**Date**: November 17th, 1987, 2:00 PM

---

[Sound of door closing, chairs scraping]

DETECTIVE MORRISON: "Interview with James Vance, November 17th, 1987, 2:00 PM. Mr. Vance, thank you for coming in. I know this is a difficult time."

JAMES: "Of course, Detective. I want to help. I want to find out what happened to my father."

MORRISON: "Let's start with the night of November 15th. Can you walk me through your evening?"

JAMES: "I... yes. Father and I had dinner around seven. Then we had a... discussion in his study around ten."

MORRISON: "A discussion about what?"

JAMES: (pause) "Business matters. Company finances. Father was stressed about some investments."

MORRISON: "Witnesses say they heard shouting."

JAMES: "Father had a temper. He shouted about everything. It wasn't unusual."

MORRISON: "What happened after this discussion?"

JAMES: "I went to my room. I was angry. I read for a while, then went to sleep around eleven-thirty."

MORRISON: "Can anyone confirm you were in your room?"

JAMES: (shifting in chair) "I was alone. My room is on the east wing. Margaret's room is on the opposite side. No one would have seen me."

MORRISON: "So no alibi."

JAMES: "I don't need an alibi, Detective. I was asleep. When I woke up the next morning, Charles told me Father was... he was gone."

MORRISON: "Let's talk about the company finances. Your father mentioned embezzlement in his diary."

JAMES: (voice rising) "I don't know anything about embezzlement. Have you looked into Margaret? She's been desperate for money for years. And Marcus Blackwood was here that night—business rival, plenty of motive."

MORRISON: "We'll get to them. Right now I'm asking about you."

JAMES: "Am I under arrest?"

MORRISON: "Should you be?"

JAMES: (long pause) "I loved my father. Whatever our disagreements, I would never... I was in my room. That's all I can tell you."

MORRISON: "We found fifty thousand pounds missing from company accounts."

JAMES: (standing) "I think I need to speak to my solicitor."

MORRISON: "Sit down, Mr. Vance."

JAMES: (sitting) "I took a loan from the company. I was going to pay it back. That's not murder."

MORRISON: "It's motive."

[Long silence]

MORRISON: "That's all for now. Don't leave town, Mr. Vance."

[End of recording]

---

### Tape 3: Daniel's Police Interrogation
**Location**: Police station
**Source**: Standard police interview recording
**Date**: November 17th, 1987, 4:30 PM

---

[Sound of door closing]

DETECTIVE MORRISON: "Interview with Daniel Hobbs, groundskeeper, November 17th, 1987, 4:30 PM. Mr. Hobbs, you've worked at Vance Manor for how long?"

DANIEL: "Fifteen years this spring."

MORRISON: "And your relationship with Harold Vance?"

DANIEL: "Professional. He was my employer. I maintained the grounds, did repairs. Standard arrangement."

MORRISON: "Standard arrangement. That's interesting, because we found some irregularities in your maintenance logbook."

DANIEL: "Irregularities?"

MORRISON: "November 15th. The day Harold Vance died. There's no entry for that day."

DANIEL: (pause) "Must have forgotten to log it. I was working in the shed most of the day."

MORRISON: "The shed. Alone?"

DANIEL: "Alone."

MORRISON: "What were you doing in the shed?"

DANIEL: "Maintenance work. Sharpening tools. Organizing supplies."

MORRISON: "Anyone see you?"

DANIEL: "The staff don't usually come out to the grounds after dark. I work alone."

MORRISON: "So you have no alibi for the evening of November 15th."

DANIEL: (too calm) "I don't need an alibi. I was in my shed until about eleven, then I went to bed. My quarters are above the carriage house. I didn't see or hear anything unusual."

MORRISON: "You didn't hear James and Harold arguing at ten o'clock? The whole household heard it."

DANIEL: "The carriage house is on the far side of the property. Sound doesn't carry."

MORRISON: "Margaret Vance says she heard footsteps in the hallway around midnight. Two people."

DANIEL: "Then ask the two people. Wasn't me."

MORRISON: "Harold Vance's diary mentions you by name. He was planning to dismiss you."

DANIEL: (slight tension) "News to me."

MORRISON: "He suspected you were helping James embezzle company funds. Moving money. Hiding transactions."

DANIEL: "That's absurd."

MORRISON: "Is it? Because we checked your bank account, Daniel. You've deposited over twenty thousand pounds in the past two years. On a groundskeeper's salary."

DANIEL: (pause) "I do side work. Private gardening jobs."

MORRISON: "Twenty thousand pounds in side work."

DANIEL: "I'm good at what I do."

MORRISON: "Where were you between eleven PM and two AM on November 15th?"

DANIEL: "I told you. In bed."

MORRISON: "Margaret heard dragging sounds at two AM. Heavy. Toward the cellar."

DANIEL: "Maybe she was hearing things. Old house. Lots of noises."

MORRISON: "Harold's body was found in the cellar. Moved post-mortem. It would take two people to move a body down those stairs."

DANIEL: (slight slip) "I was helping move— I mean, move furniture. I was helping move furniture earlier that day."

MORRISON: (leaning forward) "What furniture?"

DANIEL: (recovering) "A desk. In the study. James asked me to help him move a desk."

MORRISON: "When?"

DANIEL: "Afternoon. Before dinner."

MORRISON: "And you forgot to log that in your maintenance book?"

DANIEL: (stone-faced) "Must have slipped my mind."

MORRISON: "Your mind seems to be slipping a lot lately, Daniel."

[Long silence]

DANIEL: "Am I under arrest?"

MORRISON: "Not yet. But don't go anywhere."

[End of recording]

---

### Tape 4: Margaret's Police Interview
**Location**: Police station
**Source**: Standard police interview recording
**Date**: November 17th, 1987, 10:00 AM

---

[Sound of door closing, tissues rustling]

DETECTIVE MORRISON: "Interview with Margaret Vance, November 17th, 1987, 10:00 AM. Mrs. Vance, I'm sorry for your loss."

MARGARET: (crying) "Thank you, Detective."

MORRISON: "I know this is difficult, but I need to ask you some questions about the night your father died."

MARGARET: (sniffling) "Of course. I want to help."

MORRISON: "Tell me about that evening. November 15th."

MARGARET: "I had dinner with Father, James, and Mr. Blackwood—Marcus. It was tense. Father and Marcus were discussing some business settlement. It didn't go well."

MORRISON: "What time did dinner end?"

MARGARET: "Around eight-thirty. Marcus stayed in the parlor with Father for another hour or so. James went to his room. I went to my room."

MORRISON: "And then?"

MARGARET: "Around ten o'clock, I heard shouting. From Father's study. James and Father arguing."

MORRISON: "Could you hear what they were saying?"

MARGARET: "Not the words. Just... anger. Father's voice was so loud. James sounded desperate."

MORRISON: "What happened next?"

MARGARET: "The study door slammed. Then... silence. I thought about going to check on Father, but I didn't. I should have. I should have—" (breaks down crying)

MORRISON: "It's okay. Take your time."

MARGARET: (composing herself) "I tried to sleep. But I couldn't. Around midnight, I heard footsteps in the hallway."

MORRISON: "Footsteps?"

MARGARET: "Yes. Two people. Whispering urgently. I couldn't make out the voices, but... one of them sounded like James."

MORRISON: "You're sure?"

MARGARET: "I think so. The footsteps went toward the study."

MORRISON: "Did you investigate?"

MARGARET: "No. I... I was frightened. I locked my door and tried to sleep."

MORRISON: "Did you hear anything else?"

MARGARET: (pause) "At two in the morning, I heard dragging sounds."

MORRISON: "Dragging sounds?"

MARGARET: "Heavy. Like furniture being moved. Or... or something heavy. The sounds went down the hallway, toward the cellar stairs."

MORRISON: "And you didn't investigate?"

MARGARET: (crying again) "I was terrified, Detective. I pulled the covers over my head and prayed. I thought it was a burglar. I thought if I stayed quiet, they'd leave. I didn't know Father was—" (breaks down)

MORRISON: "It's alright, Mrs. Vance. Did you hear anything else?"

MARGARET: "No. The house went quiet after that. In the morning, Charles knocked on my door. He looked pale. He said Father was... was gone."

MORRISON: "What was James's demeanor the next morning?"

MARGARET: (pause) "Exhausted. Hollow-eyed. Like he hadn't slept all night. And Daniel—Daniel wouldn't look at me. Wouldn't meet my eyes."

MORRISON: "Do you think your brother was involved?"

MARGARET: (long pause, whispered) "I don't want to believe it. But... yes. God help me, yes."

MORRISON: "Thank you, Mrs. Vance. That's all for now."

[End of recording]

---

### Tape 5: Marcus's Police Interview
**Location**: Police station
**Source**: Standard police interview recording
**Date**: November 17th, 1987, 3:00 PM

---

[Sound of door closing]

DETECTIVE MORRISON: "Interview with Marcus Blackwood, November 17th, 1987, 3:00 PM. Mr. Blackwood, thank you for coming in voluntarily."

MARCUS: "Of course, Detective. Harold was a colleague, if not exactly a friend. I want to help however I can."

MORRISON: "You were at Vance Manor the night Harold died. November 15th."

MARCUS: "Yes. We were discussing a business settlement. Harold and I had competing patents—long story. We'd agreed to meet for dinner and hash it out."

MORRISON: "How did that go?"

MARCUS: "Poorly. Harold refused every offer I made. The man was stubborn as a mule. Brilliant, but stubborn."

MORRISON: "Were you angry?"

MARCUS: "Frustrated, not angry. This was business. I'd expected we'd reach an agreement eventually, even if it took a few more rounds of negotiation."

MORRISON: "What time did you leave?"

MARCUS: "Around eleven PM. I stayed in the parlor with Harold until about nine-thirty, then we called it a night. I gathered my briefcase, said goodnight to the household, and drove to my hotel."

MORRISON: "Which hotel?"

MARCUS: "The Ashworth Inn, in town. I checked in around midnight. The night clerk can confirm."

MORRISON: "We'll verify that. Did you see or hear anything unusual before you left?"

MARCUS: "I heard Harold and James arguing. Around ten o'clock or so."

MORRISON: "What were they arguing about?"

MARCUS: "I couldn't make out all the words, but I heard 'money' and 'betrayal' mentioned. Harold's voice carried. He was furious."

MORRISON: "Did you see James?"

MARCUS: "Briefly. He stormed past the parlor after the argument. Looked shaken."

MORRISON: "Anyone else?"

MARCUS: "Charles Webb—Harold's assistant. I saw a light on in his window as I drove away. He was still awake at eleven. Hardworking young man."

MORRISON: "Did you see the groundskeeper? Daniel?"

MARCUS: "No. Can't say I did."

MORRISON: "And you went straight to the hotel?"

MARCUS: "Straight there. Checked in, had a whiskey at the bar, went to bed."

MORRISON: "The hotel confirms your check-in at 11:47 PM."

MARCUS: "There you go."

MORRISON: "That's a tight timeline, Mr. Blackwood. Vance Manor to the Ashworth is a thirty-minute drive."

MARCUS: "I drove fast. I was irritated about the failed negotiation. But you can check my car if you like—no blood, no evidence of anything untoward."

MORRISON: "We may do that. For now, you're free to go."

MARCUS: "I hope you find who did this, Detective. Harold didn't deserve to die like that."

[End of recording]

---

### Tape 6: Charles's Police Interview
**Location**: Police station
**Source**: Standard police interview recording
**Date**: November 17th, 1987, 11:30 AM

---

[Sound of door closing]

DETECTIVE MORRISON: "Interview with Charles Webb, November 17th, 1987, 11:30 AM. Mr. Webb, you were Harold Vance's assistant?"

CHARLES: "Yes, sir. I've worked for Mr. Vance at his company for five years. He trusted me with his schedule, his correspondence—everything."

MORRISON: "You were at the manor the night he died?"

CHARLES: "Yes. I was staying in the guest wing. Mr. Vance often had me stay over when we were working on important projects. We were preparing for a will signing the next morning."

MORRISON: "A will signing?"

CHARLES: "Mr. Vance was making changes to his estate. Major changes. He'd asked me to prepare the documents for his solicitor."

MORRISON: "What kind of changes?"

CHARLES: "He was disinheriting James. Everything was going to Margaret instead."

MORRISON: "Did James know about this?"

CHARLES: "I believe Mr. Vance told him that night. During their argument."

MORRISON: "You heard the argument?"

CHARLES: "Everyone heard it. James and Mr. Vance were shouting at each other around ten o'clock. The whole house shook."

MORRISON: "What did you do?"

CHARLES: "I stayed in my room. It wasn't my place to interfere. Mr. Vance had a temper, but he always cooled down eventually."

MORRISON: "Did you see anyone after the argument?"

CHARLES: (pause) "Yes. At 10:45 PM, I looked out my window—I was restless, couldn't focus on my work. I saw James heading toward the study from the east wing."

MORRISON: "You're certain it was James?"

CHARLES: "Absolutely certain. The exterior lamps were on. I could see his face clearly. He looked... determined. Almost grim."

MORRISON: "What did you think he was doing?"

CHARLES: "I assumed he was going to apologize to Mr. Vance. To try to make amends."

MORRISON: "Did you see him return?"

CHARLES: "No. I went back to my desk after that."

MORRISON: "Did you see or hear anything else that night?"

CHARLES: "I heard Mr. Blackwood's car leaving around eleven. I was still working—the window was open. His car has a distinctive engine sound."

MORRISON: "Anyone else?"

CHARLES: "No. After that, the house was quiet. I worked until about one AM, then went to bed."

MORRISON: "You didn't hear any dragging sounds? Margaret reported hearing something at two AM."

CHARLES: "My room is on the opposite side of the house. I'm a heavy sleeper once I'm out. I didn't hear anything."

MORRISON: "When did you learn about Harold's death?"

CHARLES: "The next morning. Around seven AM. I went down to the study to review the documents before the solicitor arrived. The door was open. Mr. Vance wasn't there. I searched the house and found..." (voice breaking) "I found him in the cellar. I called the police immediately."

MORRISON: "You were close to Harold Vance?"

CHARLES: (emotional) "He was like a father to me. My parents died when I was young. Mr. Vance took me under his wing, mentored me. I owe everything to him."

MORRISON: "Do you have any suspicions about who might have done this?"

CHARLES: (hesitating) "I don't want to accuse anyone without proof, but... James was desperate. He had fifty thousand pounds of debt. The will was going to be signed the next morning. If Mr. Vance died before then, the old will would stand—James would inherit everything."

MORRISON: "And Daniel?"

CHARLES: "I always thought Daniel was too loyal to James. Unhealthily loyal. If James asked him to do something... I think Daniel would do it."

MORRISON: "Thank you, Mr. Webb. You've been very helpful."

CHARLES: "I hope you catch them, Detective. Mr. Vance deserves justice."

[End of recording]

---

### Tape 7: Margaret's Witness Account
**Location**: Guest Rooms, Margaret's room
**Source**: Margaret's personal tape recorder
**Date**: November 21st, 1987 (6 days after murder)

---

[Sound of tape recorder clicking on]

MARGARET: (crying) "I'm recording this for the detective who's coming. Arthur Hollis. The police asked me questions, but I don't think they believed me. They said I was grieving. That I was confused."

[Sound of tissue rustling]

MARGARET: "I need to tell someone what I heard. All of it. Maybe I am confused. Maybe I'm wrong. I hope I'm wrong."

[Deep breath, voice shaking]

MARGARET: "November 15th. Dinner was terrible. Marcus Blackwood was there—Father's business rival. They argued through the entire meal. James barely said a word. He just sat there, staring at his plate. I tried to talk to him, but he wouldn't look at me."

MARGARET: "After dinner, I went to my room. Around ten o'clock, I heard shouting from Father's study. James and Father. I couldn't make out the words, but Father's voice was so loud. So angry. I heard him say something about 'the will' and 'tomorrow.' The door slammed. Then nothing."

[Pause, voice getting quieter]

MARGARET: "I wanted to check on Father. I should have. But I was afraid. Afraid of getting caught in the middle of another one of their arguments. So I stayed in my room like a coward."

MARGARET: "Around midnight, I heard footsteps in the hallway. Two people, whispering. I got up and pressed my ear to the door. One of the voices sounded like... like James. I'm not certain. But it sounded like him."

[Voice breaking]

MARGARET: "I don't want to believe it was him. He's my brother. But I keep thinking about those footsteps. About the whispering. About how frightened I felt."

MARGARET: "At two in the morning, I heard dragging sounds. Heavy scraping, like something being pulled across the floor. Down the hallway. Toward the cellar. I pulled the blankets over my head and I prayed. I was terrified."

[Crying]

MARGARET: "In the morning, Charles came to my room. He said Father was gone. Found in the cellar. I ran downstairs and James was there. He looked... God, he looked exhausted. Like he hadn't slept at all. His eyes were red. His hands were shaking."

MARGARET: "I wanted to ask him what happened. I wanted him to tell me he didn't know anything. But when I looked at him, he couldn't meet my eyes. And Daniel—Daniel was standing in the corner, wouldn't look at anyone. Won't talk to me. Won't look at me."

[Long pause]

MARGARET: "I keep telling myself there's an explanation. That James couldn't have... he's my brother. I've known him my whole life. But the footsteps. The dragging. The way he looked that morning."

[Voice very quiet]

MARGARET: "Mr. Hollis, I'm recording this because I need you to investigate. Properly. The police aren't listening. Maybe I'm wrong. Maybe I'm just grieving and imagining things. But if I'm not wrong... if James really did..." (breaks down crying)

MARGARET: "Please find out what really happened. I need to know. Even if it means..." (can't finish)

MARGARET: "Father deserves justice. Even if it breaks my heart."

[Sound of tape recorder clicking off]

---

### Tape 8: The Opening (Arthur's Final Recording)
**Location**: Cellar, near the false wall
**Source**: Arthur's investigation tape recorder
**Date**: November 19th, 1987

---

*See "Day 19 - The Opening" section above for complete transcript.*

**Summary**: Arthur records his discovery of the false wall in the cellar. While documenting his findings, he hears a voice from behind the wall—Thomas Ashford, Harold's business partner who disappeared in 1957. Thomas manipulates Arthur into opening the wall by sounding human and desperate. When Arthur breaks through, he realizes Thomas has transformed into something inhuman. The recording captures Arthur's final moments before Thomas kills him. Arthur's consciousness becomes the first Narrator in the cycle.

---

## PHYSICAL EVIDENCE COLLECTED

> **NOTE:** This section describes Arthur's narrative investigation findings (what happened in the story). For game mechanics accusation requirements, see the EvidenceSystem section — James needs 3 of 6 specific items, Daniel needs 2 of 4.

### Evidence Against James (narrative):

1. **Financial Records** (Study, hidden desk drawer) - Shows £50,000 embezzlement from Vance Pharmaceutical
2. **Will Copy** (Parlor, briefcase) - OLD will: James inherits everything; NEW will draft: James gets nothing
3. **Torn Letter** (Study, fireplace ashes) - Partially burned, mentions "the will" and "betrayal"
4. **Blackmail Note** (Margaret's Room, letter on dresser) - James planted this to frame Margaret; proves James's scheming
5. **Fireplace Poker** (Study, beside fireplace) - Heavy iron poker with blood on weighted end; THE MURDER WEAPON (contextual, not a game accusation item)
6. **Bloodstained Shirt** (Cellar, behind flour sacks) - Hidden in cellar instead of burned (contextual, not a game accusation item)
7. **Letter Opener** (Study, on Harold's desk) - RED HERRING - planted by James (not a game accusation item)

### Evidence Against Daniel (narrative):

1. **Groundskeeper's Log** (Shed, logbook) - Missing entry for November 15th (murder night)
2. **Muddy Boots** (Shed, on shelf - 2nd examination) - Fresh mud, hidden on shelf; proves Daniel went outside

### Other Evidence:

1. **Sleeping Powder** (Kitchen, near cellar door) - Vial of chloral hydrate; someone was drugging Harold's evening tea

### How Arthur Solved It:

**Week 1 (Days 5-7): Physical Evidence**
- Financial records → Motive
- Bloodstained shirt → Cover-up
- Muddy boots → Someone went outside
- Fireplace poker → Murder weapon
- Groundskeeper log → Daniel's missing entry

**Week 2 (Days 8-13): The Tapes**
- Tape 1: Motive and urgency (will signing tomorrow)
- Tapes 2-6: Police interviews establishing timeline, alibis, and Daniel's slip-up
- Tape 7: Margaret's detailed witness account

**The Case:**
1. **Motive**: James embezzled £50k, Harold threatening to disinherit him and fire Daniel
2. **Opportunity**: Charles saw James heading to study at 10:45 PM
3. **Means**: Fireplace poker (murder weapon identified)
4. **Conspiracy**: Margaret heard TWO people at midnight, Daniel's "helping move—" slip-up
5. **Timeline**: Murder at 10:45 PM, body moved between 11:30 PM - 2:00 AM
6. **Physical Evidence**: Bloodstained shirt, muddy boots, missing logbook entry
7. **No Alibis**: Both James and Daniel alone, can't prove innocence

**Cumulative evidence proves James and Daniel conspired to murder Harold on November 15th, 1987.**

Arthur had enough evidence to convict them by Day 13. He should have called the police immediately. Instead, his curiosity about the cellar wall doomed him.

---

## TAPE PLACEMENT IN THE MANOR

### Proposed Tape Locations:

| Room | Old Tapes | New Tapes | Change |
|------|-----------|-----------|--------|
| Study | 2 tapes | 2 tapes | Same locations, different content |
| Parlor | 2 tapes | 2 tapes | Same locations, different content |
| Guest Rooms | 1 tape | 1 tape | Same location, updated content |
| Shed | 1 tape | 1 tape | Same location, different content |
| Cellar | 1 tape | 1 tape | Same location, different content |
| Kitchen | 0 tapes | 1 tape | NEW location for Tape 4 |
| **TOTAL** | **7 tapes** | **8 tapes** | +1 tape |

### Narrative Explanation:

**How did the tapes get scattered around the manor?**

- **Tape 1 (Harold's)**: Harold planted this recorder under his desk days before his murder
- **Tapes 2-6 (Police)**: Arthur obtained these from the police station on Days 10-11 and hid them in thematically connected locations
- **Tape 7 (Margaret's)**: Margaret recorded this herself on November 21st and left it in her room labeled "For the Detective"
- **Tape 8 (Arthur's)**: Arthur's recorder fell near the wine rack when Thomas attacked him on Day 19

---

## PROGRESSION AND GATING SYSTEMS

### Awareness Costs:
- Move to location: +1
- Examine object: +1
- Ask narrator: +1
- Talk to suspect: +2
- Show evidence: +2
- Interview suspect topic: +3
- Watch tape: +4
- Watch Tape 8 (The Opening): +5 (SPECIAL - climax trigger)
- Wrong accusation: +15

### Progressive Examination System:
- Items/evidence only appear after examining object multiple times
- Examples: poker count 1 (notice stain) → count 2 (get FIREPLACE_POKER evidence)
- wine_rack count 1 (find tape) → count 2 (discover BREATHING_WALL anomaly)

### Three-Act Gating:
- **Tape 8**: Climax trigger - watching it triggers endgame sequence (+5 awareness)
- **Cellar**: Natural exploration flow keeps it later in progression; awareness system prevents rushing
- **Margaret's Room**: Sealed until BREATHING_WALL anomaly discovered

---

## INTENDED TAPE VIEWING ORDER

### Recommended Narrative Order (Best Story Experience):

**Phase 1: Understanding the Crime**
1. **Tape 1: Harold & James Argument** - START HERE - establishes motive, conflict, urgency

**Phase 2: Building the Case (Police Interviews - any order)**
2. **Tape 6: Charles's Police Interview** - provides crucial timeline ("saw James at 10:45 PM")
3. **Tape 5: Marcus's Police Interview** - eliminates Marcus as suspect
4. **Tape 4: Margaret's Police Interview** - first mention of TWO people (conspiracy evidence)
5. **Tape 2: James's Police Interview** - James's weak alibi and defensive behavior
6. **Tape 3: Daniel's Police Interrogation** - CRITICAL EVIDENCE - the slip-up

**Phase 3: Emotional Resolution**
7. **Tape 7: Margaret's Witness Account** - detailed personal testimony

**Phase 4: The Horror Truth**
8. **Tape 8: The Opening (Arthur's Death)** - WATCH LAST - climax trigger

### Can Players Watch in Any Order?

YES - The game does NOT enforce a specific order. Natural exploration flow encourages the intended order, but investigation is non-linear by design.

---

## EVIDENCE ISSUES TO FIX (Notes for Implementation)

**Issue 1: Muddy Boots Ownership** - ✅ FIXED: Now described as Daniel's boots
**Issue 2: Blackmail Note Classification** - Currently listed as evidence against Daniel; description clarifies James planted it
**Issue 3: Old Tape References in GAME_DOCUMENTATION.md** - ✅ FIXED: All updated to new tape structure
**Issue 4: Body Discovery Timeline** - ✅ FIXED: Charles found body alone, then went to get Daniel

---

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
Routes `(room, object, examCount)` → `ExamResult`. Handles 24 examine hotspots across 10 rooms (8 rooms have examinable objects; Entrance and Guest Rooms have none). Progressive discovery (1st vs 2nd+ exams). Entity anomaly discoveries on 2nd exams. Evidence/tape awards.

### ExamResult.java
Data holder: `String text`, `Evidence evidence`, `Tape tape`, `MiniGameType miniGameType` (NONE or TORN_LETTER_RECONSTRUCTION), `EntityAnomaly anomaly`.

### EvidenceSystem.java
Evidence collection; accusation validation:

**Against James (need 3 of 6):** FINANCIAL_RECORDS, WILL_COPY, TORN_LETTER, BLACKMAIL_NOTE, TAPE_ARGUMENT, TAPE_JAMES_INTERVIEW

**Against Daniel (need 2 of 4):** GROUNDSKEEPER_LOG, MUDDY_BOOTS, TAPE_DANIEL_INTERVIEW, TAPE_MARGARET_INTERVIEW

### InterviewSystem.java
- Topic lists per suspect: James/Daniel/Marcus/Charles = 5 each, Margaret = 7 (27 total topics)
- 4-tier cooperation: <10 channeling fades, <25 lies, <40 reluctant, <60 hesitant
- Evidence reactions: 26 unique reactions
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
- Study: desk, drawers, papers, under_desk, bookshelves, window, fireplace, ashes, poker (9)
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
- Listed as evidence against **James** (accusation requirement) — James planted it to frame Margaret
- Description confirms: "James planted this note with deliberately crude handwriting mimicking Daniel's style"
- **Existing implementation:** BLACKMAIL_NOTE is in James's accusation evidence list (need 3 of 6). It proves deliberate framing and premeditation.

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

---

# DSA Game - LibGDX First-Person Navigation Implementation Guide

A comprehensive guide for implementing a Google Maps-style first-person navigation system using LibGDX.

---

## Table of Contents

1. [Overview](#overview)
2. [Project Setup](#project-setup)
3. [Architecture](#architecture)
4. [Room/Scene System](#roomscene-system)
5. [Navigation Hotspots](#navigation-hotspots)
6. [Placeholder Graphics](#placeholder-graphics)
7. [Implementation Code](#implementation-code)
8. [Room Connections Map](#room-connections-map)
9. [Future Integration](#future-integration)

---

## Overview

### What We're Building

A first-person navigation system where:
- Player sees a view of their current room
- Clickable hotspots (arrows/doors) appear on screen
- Clicking a hotspot moves the player to the connected room
- Smooth transitions between rooms
- Works like Google Maps Street View

### Tech Stack

- **LibGDX** - Game framework
- **Java** - Programming language
- **Gradle** - Build system

### Visual Concept

```
+------------------------------------------------------------------+
|                                                                  |
|                     [ROOM IMAGE/VIEW]                            |
|                                                                  |
|                    +------------------+                          |
|                    |                  |                          |
|      [<- LEFT]     |    DOORWAY       |     [RIGHT ->]           |
|       HOTSPOT      |    HOTSPOT       |      HOTSPOT             |
|                    |    (CLICK TO     |                          |
|                    |     ENTER)       |                          |
|                    +------------------+                          |
|                                                                  |
|                         [v BACK]                                 |
|                          HOTSPOT                                 |
|                                                                  |
+------------------------------------------------------------------+
|  [AWARENESS: 15/80]    [LOCATION: The Study]    [INVENTORY]     |
+------------------------------------------------------------------+
```

---

## Project Setup

### 1. Create LibGDX Project

Use the LibGDX project generator or set up manually:

**Option A: LibGDX Setup Tool**
1. Download from: https://libgdx.com/wiki/start/project-generation
2. Run the setup tool
3. Configure:
   - Project Name: `DSAGame`
   - Package: `com.dsa.game`
   - Game Class: `DSAGame`
   - Destination: Your project folder
   - Sub Projects: Desktop (required), others optional

**Option B: Manual Gradle Setup**

Create `build.gradle` in project root:

```gradle
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.0.4'
    }
}

allprojects {
    version = '1.0'
    ext {
        gdxVersion = '1.14.0'
    }

    repositories {
        mavenCentral()
    }
}

project(":desktop") {
    apply plugin: "java-library"

    dependencies {
        implementation project(":core")
        api "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
        api "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
    }
}

project(":core") {
    apply plugin: "java-library"

    dependencies {
        api "com.badlogicgames.gdx:gdx:$gdxVersion"
    }
}
```

### 2. Project Structure

```
DSAGame-LibGDX/
├── core/
│   └── src/
│       └── com/dsa/game/
│           ├── DSAGame.java           # Main game class
│           ├── screens/
│           │   ├── GameScreen.java    # Main gameplay screen
│           │   └── MenuScreen.java    # Title/menu screen
│           ├── navigation/
│           │   ├── Room.java          # Room data class
│           │   ├── RoomManager.java   # Manages all rooms
│           │   ├── Hotspot.java       # Clickable navigation area
│           │   └── Direction.java     # Navigation directions enum
│           ├── rendering/
│           │   ├── RoomRenderer.java  # Renders current room view
│           │   └── HotspotRenderer.java # Renders navigation hotspots
│           └── assets/
│               └── AssetManager.java  # Loads/manages assets
├── desktop/
│   └── src/
│       └── com/dsa/game/
│           └── DesktopLauncher.java   # Desktop entry point
├── assets/
│   ├── rooms/                         # Room background images
│   │   ├── entrance.png
│   │   ├── study.png
│   │   ├── parlor.png
│   │   └── ...
│   ├── ui/                            # UI elements
│   │   ├── arrow_left.png
│   │   ├── arrow_right.png
│   │   ├── arrow_forward.png
│   │   ├── arrow_back.png
│   │   └── door_hotspot.png
│   └── fonts/
│       └── main.fnt
└── build.gradle
```

---

## Architecture

### Class Diagram

```
+------------------+       +------------------+
|    DSAGame       |       |   GameScreen     |
|  (Main Class)    |------>|  (Game Logic)    |
+------------------+       +--------+---------+
                                    |
                    +---------------+---------------+
                    |               |               |
            +-------v------+ +------v-------+ +----v--------+
            | RoomManager  | | RoomRenderer | | InputHandler|
            | (All Rooms)  | | (Draw Room)  | | (Clicks)    |
            +-------+------+ +--------------+ +-------------+
                    |
            +-------v------+
            |    Room      |
            | (Room Data)  |
            |  - hotspots  |
            |  - image     |
            |  - connections
            +--------------+
```

### Data Flow

```
User Click
    |
    v
InputHandler.touchDown()
    |
    v
Check if click is on Hotspot
    |
    v
If yes: RoomManager.navigateTo(targetRoom)
    |
    v
RoomRenderer.setCurrentRoom(newRoom)
    |
    v
Screen displays new room
```

---

## Room/Scene System

### Room Data Structure

Each room contains:
- **ID** - Unique identifier (matches existing Location enum)
- **Name** - Display name
- **Background Image** - The first-person view image
- **Hotspots** - List of clickable areas that lead to other rooms
- **Connections** - Map of direction -> connected room

### Room Class

```java
// Room.java
package com.dsa.game.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {

    public enum RoomID {
        ENTRANCE,
        STUDY,
        PARLOR,
        KITCHEN,
        GUEST_ROOMS,
        GROUNDSKEEPER_SHED,
        SERVANTS_QUARTERS,
        CELLAR
    }

    private RoomID id;
    private String name;
    private String description;
    private String backgroundTexturePath;
    private List<Hotspot> hotspots;
    private Map<Direction, RoomID> connections;

    public Room(RoomID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.backgroundTexturePath = "rooms/" + id.name().toLowerCase() + ".png";
        this.hotspots = new ArrayList<>();
        this.connections = new HashMap<>();
    }

    // Add a connection to another room
    public void addConnection(Direction direction, RoomID targetRoom) {
        connections.put(direction, targetRoom);
    }

    // Add a clickable hotspot
    public void addHotspot(Hotspot hotspot) {
        hotspots.add(hotspot);
    }

    // Getters
    public RoomID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBackgroundTexturePath() { return backgroundTexturePath; }
    public List<Hotspot> getHotspots() { return hotspots; }
    public Map<Direction, RoomID> getConnections() { return connections; }

    // Check if room connects to another in given direction
    public boolean hasConnection(Direction direction) {
        return connections.containsKey(direction);
    }

    public RoomID getConnection(Direction direction) {
        return connections.get(direction);
    }
}
```

### Direction Enum

```java
// Direction.java
package com.dsa.game.navigation;

public enum Direction {
    NORTH("Forward", "Go forward"),
    SOUTH("Back", "Go back"),
    EAST("Right", "Turn right"),
    WEST("Left", "Turn left"),
    UP("Upstairs", "Go upstairs"),
    DOWN("Downstairs", "Go downstairs"),
    ENTER("Enter", "Enter");

    private String displayName;
    private String actionText;

    Direction(String displayName, String actionText) {
        this.displayName = displayName;
        this.actionText = actionText;
    }

    public String getDisplayName() { return displayName; }
    public String getActionText() { return actionText; }
}
```

---

## Navigation Hotspots

### Hotspot System

Hotspots are clickable areas on the screen that trigger navigation.

```java
// Hotspot.java
package com.dsa.game.navigation;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Hotspot {

    public enum HotspotType {
        ARROW_LEFT,      // Turn left
        ARROW_RIGHT,     // Turn right
        ARROW_FORWARD,   // Move forward
        ARROW_BACK,      // Move back
        DOOR,            // Enter doorway
        STAIRS_UP,       // Go upstairs
        STAIRS_DOWN      // Go downstairs
    }

    private HotspotType type;
    private Direction direction;
    private Room.RoomID targetRoom;
    private Rectangle bounds;        // Click area
    private Vector2 screenPosition;  // Where to render on screen
    private String tooltip;          // Hover text
    private boolean isHovered;

    public Hotspot(HotspotType type, Direction direction, Room.RoomID targetRoom) {
        this.type = type;
        this.direction = direction;
        this.targetRoom = targetRoom;
        this.bounds = new Rectangle();
        this.screenPosition = new Vector2();
        this.tooltip = direction.getActionText();
        this.isHovered = false;
    }

    // Set the position and size of the clickable area
    public void setBounds(float x, float y, float width, float height) {
        this.bounds.set(x, y, width, height);
        this.screenPosition.set(x, y);
    }

    // Check if a point (click) is inside this hotspot
    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    // Check if a point is inside (for hover effects)
    public void checkHover(float x, float y) {
        isHovered = bounds.contains(x, y);
    }

    // Getters
    public HotspotType getType() { return type; }
    public Direction getDirection() { return direction; }
    public Room.RoomID getTargetRoom() { return targetRoom; }
    public Rectangle getBounds() { return bounds; }
    public Vector2 getScreenPosition() { return screenPosition; }
    public String getTooltip() { return tooltip; }
    public boolean isHovered() { return isHovered; }

    public void setTooltip(String tooltip) { this.tooltip = tooltip; }
}
```

### Hotspot Positioning

Hotspots are positioned based on their type:

```
Screen Layout (1280x720 example):

+------------------------------------------------------------------+
|                                                                  |
|                                                                  |
|                         [FORWARD]                                |
|                        (640, 500)                                |
|                                                                  |
|                    +------------------+                          |
|   [LEFT]           |                  |           [RIGHT]        |
|  (100, 360)        |      DOOR        |          (1180, 360)     |
|                    |   (640, 360)     |                          |
|                    |                  |                          |
|                    +------------------+                          |
|                                                                  |
|                          [BACK]                                  |
|                        (640, 100)                                |
|                                                                  |
+------------------------------------------------------------------+
```

### Standard Hotspot Positions

```java
// HotspotPositions.java
package com.dsa.game.navigation;

public class HotspotPositions {

    // Screen dimensions (adjust to your game)
    public static final float SCREEN_WIDTH = 1280;
    public static final float SCREEN_HEIGHT = 720;

    // Hotspot sizes
    public static final float ARROW_SIZE = 80;
    public static final float DOOR_WIDTH = 200;
    public static final float DOOR_HEIGHT = 300;

    // Standard positions (center of hotspot)
    public static final float LEFT_X = 100;
    public static final float LEFT_Y = SCREEN_HEIGHT / 2;

    public static final float RIGHT_X = SCREEN_WIDTH - 100;
    public static final float RIGHT_Y = SCREEN_HEIGHT / 2;

    public static final float FORWARD_X = SCREEN_WIDTH / 2;
    public static final float FORWARD_Y = SCREEN_HEIGHT - 150;

    public static final float BACK_X = SCREEN_WIDTH / 2;
    public static final float BACK_Y = 100;

    public static final float DOOR_CENTER_X = SCREEN_WIDTH / 2;
    public static final float DOOR_CENTER_Y = SCREEN_HEIGHT / 2;

    // Create a hotspot with standard positioning
    public static Hotspot createStandardHotspot(
            Hotspot.HotspotType type,
            Direction direction,
            Room.RoomID target) {

        Hotspot hotspot = new Hotspot(type, direction, target);

        float x, y, width, height;

        switch (type) {
            case ARROW_LEFT:
                x = LEFT_X - ARROW_SIZE/2;
                y = LEFT_Y - ARROW_SIZE/2;
                width = ARROW_SIZE;
                height = ARROW_SIZE;
                break;

            case ARROW_RIGHT:
                x = RIGHT_X - ARROW_SIZE/2;
                y = RIGHT_Y - ARROW_SIZE/2;
                width = ARROW_SIZE;
                height = ARROW_SIZE;
                break;

            case ARROW_FORWARD:
                x = FORWARD_X - ARROW_SIZE/2;
                y = FORWARD_Y - ARROW_SIZE/2;
                width = ARROW_SIZE;
                height = ARROW_SIZE;
                break;

            case ARROW_BACK:
                x = BACK_X - ARROW_SIZE/2;
                y = BACK_Y - ARROW_SIZE/2;
                width = ARROW_SIZE;
                height = ARROW_SIZE;
                break;

            case DOOR:
                x = DOOR_CENTER_X - DOOR_WIDTH/2;
                y = DOOR_CENTER_Y - DOOR_HEIGHT/2;
                width = DOOR_WIDTH;
                height = DOOR_HEIGHT;
                break;

            default:
                x = SCREEN_WIDTH/2;
                y = SCREEN_HEIGHT/2;
                width = ARROW_SIZE;
                height = ARROW_SIZE;
        }

        hotspot.setBounds(x, y, width, height);
        return hotspot;
    }
}
```

---

## Placeholder Graphics

### Creating Simple Placeholder Graphics

Since we're starting with placeholders, we'll create simple colored rectangles with text labels.

### PlaceholderGenerator Class

```java
// PlaceholderGenerator.java
package com.dsa.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dsa.game.navigation.Room;
import java.util.HashMap;
import java.util.Map;

public class PlaceholderGenerator {

    // Color scheme for each room (atmospheric)
    private static final Map<Room.RoomID, Color> ROOM_COLORS = new HashMap<>();

    static {
        ROOM_COLORS.put(Room.RoomID.ENTRANCE, new Color(0.3f, 0.25f, 0.2f, 1));      // Dark brown
        ROOM_COLORS.put(Room.RoomID.STUDY, new Color(0.2f, 0.15f, 0.15f, 1));        // Dark red-brown
        ROOM_COLORS.put(Room.RoomID.PARLOR, new Color(0.25f, 0.2f, 0.25f, 1));       // Dark purple
        ROOM_COLORS.put(Room.RoomID.KITCHEN, new Color(0.2f, 0.2f, 0.2f, 1));        // Dark gray
        ROOM_COLORS.put(Room.RoomID.GUEST_ROOMS, new Color(0.2f, 0.2f, 0.25f, 1));   // Dark blue
        ROOM_COLORS.put(Room.RoomID.GROUNDSKEEPER_SHED, new Color(0.15f, 0.2f, 0.15f, 1)); // Dark green
        ROOM_COLORS.put(Room.RoomID.SERVANTS_QUARTERS, new Color(0.2f, 0.18f, 0.15f, 1)); // Dim brown
        ROOM_COLORS.put(Room.RoomID.CELLAR, new Color(0.1f, 0.1f, 0.1f, 1));         // Near black
    }

    // Generate a placeholder room texture
    public static Texture generateRoomPlaceholder(Room.RoomID roomId, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Get room color
        Color baseColor = ROOM_COLORS.getOrDefault(roomId, Color.DARK_GRAY);

        // Fill background with gradient (darker at bottom)
        for (int y = 0; y < height; y++) {
            float factor = 0.7f + (0.3f * y / height); // Darker at bottom
            Color rowColor = new Color(
                baseColor.r * factor,
                baseColor.g * factor,
                baseColor.b * factor,
                1
            );
            pixmap.setColor(rowColor);
            pixmap.drawLine(0, y, width, y);
        }

        // Draw floor line
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fillRectangle(0, 0, width, height / 4);

        // Draw simple wall outline
        pixmap.setColor(new Color(0.3f, 0.3f, 0.3f, 1));
        pixmap.drawRectangle(50, height/4, width - 100, height - height/4 - 50);

        // Draw room name area (will be overlaid with text in renderer)
        pixmap.setColor(new Color(0, 0, 0, 0.5f));
        pixmap.fillRectangle(width/2 - 150, height - 80, 300, 50);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    // Generate arrow hotspot texture
    public static Texture generateArrowTexture(int size, Direction direction) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Semi-transparent background
        pixmap.setColor(new Color(0, 0, 0, 0.5f));
        pixmap.fillCircle(size/2, size/2, size/2 - 2);

        // Arrow color
        pixmap.setColor(new Color(1, 1, 1, 0.8f));

        int cx = size / 2;
        int cy = size / 2;
        int arrowSize = size / 3;

        // Draw arrow based on direction
        switch (direction) {
            case NORTH: // Up arrow
                pixmap.fillTriangle(cx, cy + arrowSize, cx - arrowSize, cy - arrowSize/2, cx + arrowSize, cy - arrowSize/2);
                break;
            case SOUTH: // Down arrow
                pixmap.fillTriangle(cx, cy - arrowSize, cx - arrowSize, cy + arrowSize/2, cx + arrowSize, cy + arrowSize/2);
                break;
            case EAST: // Right arrow
                pixmap.fillTriangle(cx + arrowSize, cy, cx - arrowSize/2, cy - arrowSize, cx - arrowSize/2, cy + arrowSize);
                break;
            case WEST: // Left arrow
                pixmap.fillTriangle(cx - arrowSize, cy, cx + arrowSize/2, cy - arrowSize, cx + arrowSize/2, cy + arrowSize);
                break;
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    // Generate door hotspot texture
    public static Texture generateDoorTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Door frame
        pixmap.setColor(new Color(0.3f, 0.2f, 0.1f, 1)); // Brown
        pixmap.fillRectangle(0, 0, width, height);

        // Door interior (darker)
        pixmap.setColor(new Color(0.1f, 0.05f, 0.02f, 1));
        pixmap.fillRectangle(10, 10, width - 20, height - 20);

        // Highlight when can enter
        pixmap.setColor(new Color(1, 1, 1, 0.1f));
        pixmap.drawRectangle(5, 5, width - 10, height - 10);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }
}
```

### Room Placeholder Visuals

Each room will look like this (with placeholder graphics):

```
+------------------------------------------------------------------+
|                                                                  |
|    +----------------------------------------------------------+  |
|    |                                                          |  |
|    |                    [ROOM NAME]                           |  |
|    |                                                          |  |
|    |                                                          |  |
|    |              +------------------+                        |  |
|    |              |                  |                        |  |
|    |              |      DOOR        |                        |  |
|    |              |    (if exists)   |                        |  |
|    |              |                  |                        |  |
|    |              +------------------+                        |  |
|    |                                                          |  |
|    +----------------------------------------------------------+  |
|    |                    FLOOR AREA                            |  |
|    +----------------------------------------------------------+  |
|                                                                  |
|  [<]                                                        [>]  |
|                              [v]                                 |
+------------------------------------------------------------------+
```

---

## Implementation Code

### Main Game Class

```java
// DSAGame.java
package com.dsa.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dsa.game.screens.GameScreen;

public class DSAGame extends Game {

    public SpriteBatch batch;

    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final String TITLE = "The Unreliable Narrator";

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
```

### Desktop Launcher

```java
// DesktopLauncher.java
package com.dsa.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(DSAGame.TITLE);
        config.setWindowedMode(DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
        config.setResizable(false);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new DSAGame(), config);
    }
}
```

### Room Manager

```java
// RoomManager.java
package com.dsa.game.navigation;

import java.util.HashMap;
import java.util.Map;

public class RoomManager {

    private Map<Room.RoomID, Room> rooms;
    private Room currentRoom;

    public RoomManager() {
        rooms = new HashMap<>();
        initializeRooms();
    }

    private void initializeRooms() {
        // Create all rooms
        createRoom(Room.RoomID.ENTRANCE, "Entrance Hall",
            "The main entrance of Vance Manor. Grand staircase leads up. Doors lead to the Study and Parlor.");

        createRoom(Room.RoomID.STUDY, "The Study",
            "The crime scene. Harold's desk sits where he died. Bookshelves line the north wall.");

        createRoom(Room.RoomID.PARLOR, "The Parlor",
            "Where guests gathered. The grandfather clock is stopped at 11:47.");

        createRoom(Room.RoomID.KITCHEN, "The Kitchen",
            "Large industrial kitchen. A door leads down to the cellar.");

        createRoom(Room.RoomID.GUEST_ROOMS, "Guest Rooms",
            "The guest wing. Margaret and James stayed here the night of the murder.");

        createRoom(Room.RoomID.GROUNDSKEEPER_SHED, "Groundskeeper's Shed",
            "Daniel's domain for 15 years. Tools and logbooks fill the small space.");

        createRoom(Room.RoomID.SERVANTS_QUARTERS, "Servants' Quarters",
            "Staff area. Simple but clean. A narrow bed with a worn wooden bedpost.");

        createRoom(Room.RoomID.CELLAR, "The Cellar",
            "Dark storage area beneath the kitchen. Flour sacks and wine racks.");

        // Set up connections
        setupConnections();

        // Start in entrance
        currentRoom = rooms.get(Room.RoomID.ENTRANCE);
    }

    private void createRoom(Room.RoomID id, String name, String description) {
        rooms.put(id, new Room(id, name, description));
    }

    private void setupConnections() {
        Room entrance = rooms.get(Room.RoomID.ENTRANCE);
        Room study = rooms.get(Room.RoomID.STUDY);
        Room parlor = rooms.get(Room.RoomID.PARLOR);
        Room kitchen = rooms.get(Room.RoomID.KITCHEN);
        Room guestRooms = rooms.get(Room.RoomID.GUEST_ROOMS);
        Room shed = rooms.get(Room.RoomID.GROUNDSKEEPER_SHED);
        Room servants = rooms.get(Room.RoomID.SERVANTS_QUARTERS);
        Room cellar = rooms.get(Room.RoomID.CELLAR);

        // ENTRANCE connections
        entrance.addConnection(Direction.EAST, Room.RoomID.STUDY);
        entrance.addConnection(Direction.WEST, Room.RoomID.PARLOR);
        entrance.addConnection(Direction.NORTH, Room.RoomID.KITCHEN);
        entrance.addConnection(Direction.UP, Room.RoomID.GUEST_ROOMS);

        // Add hotspots for entrance
        entrance.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.DOOR, Direction.EAST, Room.RoomID.STUDY));
        entrance.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_LEFT, Direction.WEST, Room.RoomID.PARLOR));
        entrance.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_FORWARD, Direction.NORTH, Room.RoomID.KITCHEN));
        entrance.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.STAIRS_UP, Direction.UP, Room.RoomID.GUEST_ROOMS));

        // STUDY connections (east wing - crime scene)
        study.addConnection(Direction.WEST, Room.RoomID.ENTRANCE);

        study.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_BACK, Direction.WEST, Room.RoomID.ENTRANCE));

        // PARLOR connections (west wing - formal guest room)
        parlor.addConnection(Direction.EAST, Room.RoomID.ENTRANCE);

        parlor.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.ENTRANCE));

        // KITCHEN connections
        kitchen.addConnection(Direction.SOUTH, Room.RoomID.ENTRANCE);
        kitchen.addConnection(Direction.WEST, Room.RoomID.SERVANTS_QUARTERS);
        kitchen.addConnection(Direction.DOWN, Room.RoomID.CELLAR);

        kitchen.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_BACK, Direction.SOUTH, Room.RoomID.ENTRANCE));
        kitchen.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_LEFT, Direction.WEST, Room.RoomID.SERVANTS_QUARTERS));
        kitchen.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.STAIRS_DOWN, Direction.DOWN, Room.RoomID.CELLAR));

        // GUEST ROOMS connections
        guestRooms.addConnection(Direction.DOWN, Room.RoomID.ENTRANCE);

        guestRooms.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.STAIRS_DOWN, Direction.DOWN, Room.RoomID.ENTRANCE));

        // SHED connections (connects back to servants' quarters)
        shed.addConnection(Direction.EAST, Room.RoomID.SERVANTS_QUARTERS);

        shed.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.SERVANTS_QUARTERS));

        // SERVANTS' QUARTERS connections (only accessible from Kitchen)
        servants.addConnection(Direction.EAST, Room.RoomID.KITCHEN);

        servants.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.ARROW_BACK, Direction.EAST, Room.RoomID.KITCHEN));

        // CELLAR connections
        cellar.addConnection(Direction.UP, Room.RoomID.KITCHEN);

        cellar.addHotspot(HotspotPositions.createStandardHotspot(
            Hotspot.HotspotType.STAIRS_UP, Direction.UP, Room.RoomID.KITCHEN));
    }

    // Navigate to a new room
    public boolean navigateTo(Room.RoomID targetRoom) {
        if (rooms.containsKey(targetRoom)) {
            currentRoom = rooms.get(targetRoom);
            return true;
        }
        return false;
    }

    // Navigate via direction from current room
    public boolean navigateDirection(Direction direction) {
        if (currentRoom.hasConnection(direction)) {
            Room.RoomID target = currentRoom.getConnection(direction);
            return navigateTo(target);
        }
        return false;
    }

    // Getters
    public Room getCurrentRoom() { return currentRoom; }
    public Room getRoom(Room.RoomID id) { return rooms.get(id); }
}
```

### Game Screen

```java
// GameScreen.java
package com.dsa.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.dsa.game.DSAGame;
import com.dsa.game.navigation.*;
import com.dsa.game.rendering.PlaceholderGenerator;

import java.util.HashMap;
import java.util.Map;

public class GameScreen implements Screen {

    private DSAGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    private RoomManager roomManager;

    // Textures
    private Map<Room.RoomID, Texture> roomTextures;
    private Map<Direction, Texture> arrowTextures;
    private Texture doorTexture;

    // UI
    private String currentTooltip = "";

    public GameScreen(DSAGame game) {
        this.game = game;
        this.batch = game.batch;
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);

        // Initialize room manager
        roomManager = new RoomManager();

        // Generate placeholder textures
        generatePlaceholderTextures();

        // Set up input
        setupInput();
    }

    private void generatePlaceholderTextures() {
        roomTextures = new HashMap<>();
        arrowTextures = new HashMap<>();

        // Generate room backgrounds
        for (Room.RoomID roomId : Room.RoomID.values()) {
            roomTextures.put(roomId, PlaceholderGenerator.generateRoomPlaceholder(
                roomId, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT));
        }

        // Generate arrow textures
        arrowTextures.put(Direction.NORTH, PlaceholderGenerator.generateArrowTexture(80, Direction.NORTH));
        arrowTextures.put(Direction.SOUTH, PlaceholderGenerator.generateArrowTexture(80, Direction.SOUTH));
        arrowTextures.put(Direction.EAST, PlaceholderGenerator.generateArrowTexture(80, Direction.EAST));
        arrowTextures.put(Direction.WEST, PlaceholderGenerator.generateArrowTexture(80, Direction.WEST));

        // Generate door texture
        doorTexture = PlaceholderGenerator.generateDoorTexture(200, 300);
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // Convert screen coordinates (Y is inverted in LibGDX)
                float gameY = DSAGame.SCREEN_HEIGHT - screenY;

                // Check hotspots
                for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
                    if (hotspot.contains(screenX, gameY)) {
                        // Navigate to target room
                        roomManager.navigateTo(hotspot.getTargetRoom());
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                float gameY = DSAGame.SCREEN_HEIGHT - screenY;
                currentTooltip = "";

                // Check for hover on hotspots
                for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
                    hotspot.checkHover(screenX, gameY);
                    if (hotspot.isHovered()) {
                        currentTooltip = hotspot.getTooltip();
                    }
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                // Keyboard navigation as backup
                switch (keycode) {
                    case Input.Keys.W:
                    case Input.Keys.UP:
                        roomManager.navigateDirection(Direction.NORTH);
                        break;
                    case Input.Keys.S:
                    case Input.Keys.DOWN:
                        roomManager.navigateDirection(Direction.SOUTH);
                        break;
                    case Input.Keys.A:
                    case Input.Keys.LEFT:
                        roomManager.navigateDirection(Direction.WEST);
                        break;
                    case Input.Keys.D:
                    case Input.Keys.RIGHT:
                        roomManager.navigateDirection(Direction.EAST);
                        break;
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Room currentRoom = roomManager.getCurrentRoom();

        batch.begin();

        // Draw room background
        Texture roomTex = roomTextures.get(currentRoom.getId());
        if (roomTex != null) {
            batch.draw(roomTex, 0, 0, DSAGame.SCREEN_WIDTH, DSAGame.SCREEN_HEIGHT);
        }

        // Draw room name
        font.draw(batch, currentRoom.getName(),
            DSAGame.SCREEN_WIDTH/2 - 100, DSAGame.SCREEN_HEIGHT - 30);

        // Draw hotspots
        for (Hotspot hotspot : currentRoom.getHotspots()) {
            Texture hotspotTex = getHotspotTexture(hotspot);
            if (hotspotTex != null) {
                // Draw with highlight if hovered
                if (hotspot.isHovered()) {
                    batch.setColor(1, 1, 0.5f, 1); // Yellow tint
                }
                batch.draw(hotspotTex,
                    hotspot.getBounds().x,
                    hotspot.getBounds().y,
                    hotspot.getBounds().width,
                    hotspot.getBounds().height);
                batch.setColor(Color.WHITE);
            }
        }

        // Draw tooltip
        if (!currentTooltip.isEmpty()) {
            font.draw(batch, currentTooltip,
                Gdx.input.getX() + 15,
                DSAGame.SCREEN_HEIGHT - Gdx.input.getY() + 15);
        }

        // Draw room description at bottom
        font.draw(batch, currentRoom.getDescription(), 20, 50);

        // Draw controls hint
        font.draw(batch, "Click arrows to move | WASD/Arrow keys | ESC to quit",
            20, 20);

        batch.end();
    }

    private Texture getHotspotTexture(Hotspot hotspot) {
        switch (hotspot.getType()) {
            case ARROW_FORWARD:
                return arrowTextures.get(Direction.NORTH);
            case ARROW_BACK:
                return arrowTextures.get(Direction.SOUTH);
            case ARROW_LEFT:
                return arrowTextures.get(Direction.WEST);
            case ARROW_RIGHT:
                return arrowTextures.get(Direction.EAST);
            case DOOR:
            case STAIRS_UP:
            case STAIRS_DOWN:
                return doorTexture;
            default:
                return null;
        }
    }

    @Override
    public void resize(int width, int height) {}

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
        for (Texture tex : roomTextures.values()) {
            tex.dispose();
        }
        for (Texture tex : arrowTextures.values()) {
            tex.dispose();
        }
        doorTexture.dispose();
    }
}
```

---

## Room Connections Map

### Visual Connection Map

```
                                    GUEST ROOMS
                                        |
                                       [UP]
                                        |
                                        |
                         ENTRANCE ----[NORTH]---- KITCHEN ----[WEST]---- SERVANTS' ----[WEST]---- GROUNDSKEEPER'S
                         /   |   \                    |                   QUARTERS                     SHED
                   [WEST]    |    [EAST]          [DOWN]
                       /     |     \                  |
                  PARLOR     |     STUDY           CELLAR
                             |
                    (Parlor and Study are
                     separate wings - not
                     connected to each other)
```

### Connection Table

| From | Direction | To | Gate |
|------|-----------|-----|------|
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
| GUEST_ROOMS | ENTER | MARGARET_ROOM | **Act 3: BREATHING_WALL anomaly** |
| SERVANTS_QUARTERS | EAST | KITCHEN | None |
| CELLAR | UP | KITCHEN | None |
| GROUNDSKEEPER_SHED | EAST | SERVANTS_QUARTERS | None |
| SERVANTS_QUARTERS | WEST | GROUNDSKEEPER_SHED | None |

### Navigation Gates

The game uses a three-act structure with navigation gates:

**Act 1 (8 rooms):** Entrance, Study, Parlor, Kitchen, Guest Rooms, James's Room, Servants' Quarters, Groundskeeper's Shed -- all accessible from the start.

**Act 2 -- Cellar:** Sealed until the player has watched 4+ tapes AND collected 5+ evidence. The narrator provides mood-varied text explaining the door won't open. Implementation: `isCellarUnlocked()` in GameScreen.java checks `gameState.getWatchedTapes().size() >= 4 && gameState.getCollectedEvidence().size() >= 5`.

**Act 3 -- Margaret's Room:** Sealed until the BREATHING_WALL anomaly is discovered (found on 2nd examination of cellar wine_rack). Implementation: `isMargaretRoomUnlocked()` checks `gameState.hasAnomaly(EntityAnomaly.BREATHING_WALL)`.

When a player attempts to enter a sealed room, the narrator delivers a mood-appropriate message (HOPEFUL/CONFUSED/ANXIOUS/FRANTIC) explaining the door is sealed. This text escalates in intensity with awareness level.

---

## Future Integration

### Adding Existing Game Logic

Once navigation works, integrate the existing game mechanics:

1. **Awareness System**
   - Add `awareness` variable to GameScreen
   - Increment on room transitions
   - Display awareness meter in UI

2. **Evidence System**
   - Add clickable evidence hotspots in rooms
   - Integrate EvidenceManager class
   - Show collected evidence in inventory UI

3. **Dialogue/Narrator**
   - Add text box overlay for narrator dialogue
   - Trigger dialogue on entering rooms
   - Show room descriptions with narrator voice

4. **Tapes**
   - Add tape collection points
   - Create tape playback screen
   - Integrate with awareness triggers

### Integration Points

```java
// Add to GameScreen:

// Game state
private int awareness = 0;
private Set<Evidence> collectedEvidence = new HashSet<>();
private Set<Tape> collectedTapes = new HashSet<>();

// On room transition:
private void onRoomEnter(Room room) {
    awareness += 1; // COST_MOVE
    checkAwarenessThresholds();
    showRoomNarration(room);
}

// Check awareness
private void checkAwarenessThresholds() {
    if (awareness >= 80) {
        triggerGameOver();
    }
}
```

### File Structure After Full Integration

```
core/src/com/dsa/game/
├── DSAGame.java
├── screens/
│   ├── GameScreen.java
│   ├── MenuScreen.java
│   ├── TapePlaybackScreen.java
│   └── EndingScreen.java
├── navigation/
│   ├── Room.java
│   ├── RoomManager.java
│   ├── Hotspot.java
│   └── Direction.java
├── gameplay/
│   ├── GameState.java         # Port from existing
│   ├── EvidenceManager.java   # Port from existing
│   ├── AwarenessSystem.java
│   └── NarratorSystem.java
├── ui/
│   ├── DialogueBox.java
│   ├── InventoryPanel.java
│   ├── AwarenessMeter.java
│   └── HUD.java
└── rendering/
    ├── RoomRenderer.java
    ├── HotspotRenderer.java
    └── PlaceholderGenerator.java
```

---

## Quick Start Checklist

1. [ ] Install LibGDX (via setup tool or manual Gradle)
2. [ ] Create project structure
3. [ ] Copy the classes from this guide
4. [ ] Run DesktopLauncher to test
5. [ ] Verify navigation between rooms works
6. [ ] Replace placeholder graphics with real art (later)
7. [ ] Integrate game logic (later)

---

## Resources

- **LibGDX Wiki**: https://libgdx.com/wiki/
- **LibGDX Setup**: https://libgdx.com/wiki/start/project-generation
- **LibGDX GitHub**: https://github.com/libgdx/libgdx
- **Scene2D UI**: https://libgdx.com/wiki/graphics/2d/scene2d/scene2d-ui

---

*Created: January 2026*
*For: The Unreliable Narrator Game*
*Navigation System: First-Person Point-and-Click*

---

# Room Art Guide

All room backgrounds are **1280x720** static images. Each room has one fixed camera angle.

Place each finished image in this folder as a `.png` named after the room (e.g. `entrance.png`).

**Era:** 1920s English countryside manor
**Palette:** Dark, muted. Browns, deep reds, greens, grays. Warm tones for manor interiors, cool/damp for cellar and shed.
**Lighting:** Dim throughout. Each room has one insufficient light source (chandelier, window, fireplace glow, bare bulb).

Examinable objects must be **visually identifiable** — a desk should look like a desk, a clock like a clock. A magnifying glass icon overlay appears at runtime, so no need to add UI indicators in the art.

---

## 1. Entrance Hall — `entrance.png`

**Perspective:** Standing just inside the front door, looking into the house.

**Key elements:**
- Grand foyer with high ceilings
- A large chandelier overhead, dimly lit, dust motes visible in the light
- A grand staircase going up on one side (leads to Guest Rooms) — needs to be visually prominent since it's a navigation point
- Door visible ahead/forward leading deeper into the house (toward Kitchen)
- Doorway or arch to the right (toward Study)
- Hallway or arch to the left (toward Parlor)
- Dark wood paneling on the walls, maybe a tiled or polished wood floor
- Gloomy, muted colors — this is a murder scene house

**No examinable objects** — this room is purely a navigation hub.

**Navigation exits:**
- Right: Study (door hotspot, center of screen)
- Left: Parlor (arrow, left edge)
- Forward: Kitchen (arrow, top center)
- Up: Guest Rooms (stairs hotspot, center of screen)

---

## 2. The Study — `study.png`

**Perspective:** Standing in the doorway, looking into the room.

**Examinable objects (9) — position matters:**
- **Harold's mahogany desk** — large, center-bottom of the image. Papers scattered across it. A silver letter opener glinting on the surface. Desk has drawers visible on the front/side.
- **Drawers** — desk drawers (separate clickable area); hidden compartment holds financial records
- **Papers** on the desk surface (separate clickable area from the desk itself)
- **Under the desk** — the area beneath, shadowed (a tape recorder hidden here)
- **Bookshelves** — floor-to-ceiling, on the right side of the room. Leather-bound volumes in rows.
- **Large bay windows** — center-back wall, overlooking the grounds. Latch visible. Scuff marks on the sill.
- **Stone fireplace** — left side of the room, large. Embers still glowing, ashes visible in the grate.
- **Fireplace poker** — iron poker beside the fireplace (separate clickable area; real murder weapon)
- **Ashes** — in the fireplace grate (separate clickable area from the fireplace; torn letter fragments hidden here)
- Overall feel: a rich man's study, now disturbed and ominous

**Navigation exits:**
- Back: Entrance (arrow, bottom center) — only exit; no direct connection to Parlor

---

## 3. The Parlor — `parlor.png`

**Perspective:** Standing in the room, looking at the fireplace wall.

**Examinable objects (3):**
- **Fireplace** — center of the image, cold/dead. Family photographs on the mantle (Harold, James, Margaret looking strained). Half-burned unreadable papers in the grate.
- **Grandfather clock** — right side of the room, ornate, tall. Pendulum visible through the glass case. (Tape hidden inside the mechanism)
- **Leather briefcase** — on the floor near one of the armchairs, center-left area

**Other visual elements:**
- Comfortable armchairs facing the fireplace
- Rich carpet, heavy curtains, Victorian decor

**Navigation exits:**
- Right: Entrance (arrow, right edge)

---

## 4. The Kitchen — `kitchen.png`

**Perspective:** Standing in the kitchen, looking across the room.

**Examinable objects (2):**
- **Storage cellar door** — a narrow door in the floor or wall, center of the image. A shelf beside it holds tea supplies (sleeping powder hidden behind a canister). Tape 4 (Margaret's Police Interview) hidden on shelf beside the door. Also a navigation point (leads down to Cellar). Cold Spot anomaly discovered on 2nd exam.
- **Flour tin** — on a shelf, left side of the room. Large metal tin among other containers/spices. (Just flour inside, but shows fingerprints in the dust)

**Other visual elements:**
- Large industrial kitchen — copper pots hanging from overhead racks, counters, a stove
- Shelves with spices, jars, ingredients
- Feel: functional, not cozy. Cold tile or stone floor.

**Navigation exits:**
- Back: Entrance (arrow, bottom center)
- Left: Servants' Quarters (arrow, left edge)
- Down: Cellar (stairs hotspot, center of screen)

---

## 5. Guest Rooms Hallway — `guest_rooms.png`

**Perspective:** Standing in the upstairs hallway, looking at two bedroom doors.

**Examinable objects:** None — this is a navigation hub to access the individual bedrooms.

**Other visual elements:**
- **Margaret's bedroom door** — left side of the hallway, slightly ajar
- **James's bedroom door** — right side of the hallway, slightly ajar
- Hallway carpet runner, wall sconces, dim upstairs lighting
- Dark stains visible on the carpet near Margaret's door
- Staircase going down visible
- Feel: upstairs, quieter, more intimate. The two doors invite investigation.

**Navigation exits:**
- Down: Entrance (stairs hotspot, center of screen)
- Left door: Margaret's Room (door hotspot, left side)
- Right door: James's Room (door hotspot, right side)

---

## 6. James's Room — `james_room.png`

**Perspective:** Standing inside James's bedroom, looking into the room.

**Examinable objects (2):**
- **James's coat** — hanging on the wardrobe door, right side of room. The right sleeve cuff has a dark bloodstain visible.
- **Wardrobe** — large wardrobe with door hanging open, showing hastily thrown clothes inside

**Other visual elements:**
- Unmade bed, sheets tangled
- Ashtray overflowing with cigarette stubs on nightstand
- Half-finished glass of whisky on nightstand
- Messy, lived-in appearance — clothes on floor, general disorder
- Door back to hallway visible
- Feel: masculine chaos, alcohol and tobacco smell, someone living without care

**Navigation exits:**
- Back: Guest Rooms hallway (door hotspot, left side)

---

## 7. Margaret's Room — `margaret_room.png`

**Perspective:** Standing inside Margaret's bedroom, looking into the room.

**Examinable objects (2):**
- **Letter on dresser** — a folded note on the dresser surface (blackmail note)
- **Dresser** — elegant dresser with a tape recorder on the nightstand beside it. Half-empty bottle of port wine on top. Shoes with dark stains visible near the door.

**Other visual elements:**
- Tidy room, but tense atmosphere
- Half-packed suitcase on the bed (she was planning to leave)
- Neatly made bed despite the suitcase
- Contrast to James's messy room
- Door back to hallway visible
- Feel: organized but anxious, someone preparing to flee

**Navigation exits:**
- Back: Guest Rooms hallway (door hotspot, right side)

---

## 8. Groundskeeper's Shed — `groundskeeper_shed.png`

**Perspective:** Standing inside a small outbuilding, looking at the workbench.

**Examinable objects (2):**
- **Logbook** — open on a rough wooden workbench/desk (center of room). The logbook has a tape recorder wedged between pages.
- **Shelf** — next to the workbench, holding gardening supplies, tools, a tin of nails, a can of paint thinner. A pair of muddy boots visible behind some items.

**Other visual elements:**
- Small, rustic building — wooden walls, rough construction. Very different from the manor's interior.
- Tools hanging on the walls (rakes, shovels, shears)
- A small window letting in natural light
- Feel: earthy, cramped, functional. Smells of soil and oil.

**Navigation exits:**
- Back: Servants' Quarters (arrow, right edge)

---

## 9. Servants' Quarters — `servants_quarters.png`

**Perspective:** Standing in the staff living area.

**Examinable objects (2):**
- **Worn wooden bedpost** — Simple bed with worn wooden bedpost on the right side. Deep scratches mar the wood near the base. (A.H. initials carved into the bedpost - Arthur Hollis anomaly)
- **Loose floorboard** — center-left, near the wall. Slightly raised, gap visible. (Empty - scratch marks show something was stored here recently and removed)

**Other visual elements:**
- Simple, clean room — modest compared to the rest of the manor
- A simple bed, a small table
- Plain walls, basic furniture
- Feel: humble, orderly. The staff keeps things neat.

**Navigation exits:**
- Back: Kitchen (arrow, bottom center)
- Left: Groundskeeper Shed (arrow, left edge)

---

## 10. The Cellar — `cellar.png`

**Perspective:** Standing at the bottom of the stairs, looking into a dark underground space.

**Examinable objects (2):**
- **Wine racks** — left side, floor to ceiling. Dusty bottles, but one section is recently disturbed. (Tape 8 hidden behind bottles, taped to the wall)
- **Flour sacks** — center-right, stacked against the wall. Drag marks in the dust on the floor around them. (Bloodstained shirt hastily hidden behind the sacks - this is where the body was moved to)

**Other visual elements:**
- Dark, damp underground room — stone walls, low ceiling, very dim lighting
- Barrels in the background
- The stairs going up (back to Kitchen)
- Feel: the darkest, most oppressive room. Cold, damp air. This is where the body was hidden.

**Navigation exits:**
- Up: Kitchen (stairs hotspot, center of screen)

---

# SUPPLEMENTAL TECHNICAL DOCUMENTATION
## (Added from full source scan — February 2026)

---

## A. Build System — Complete Details

### gradle.properties
```
org.gradle.daemon=false
org.gradle.jvmargs=-Xms512M -Xmx1G -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
org.gradle.configureondemand=false
org.gradle.logging.level=quiet
graalHelperVersion=2.0.1
enableGraalNative=false
gdxVersion=1.14.0
projectVersion=1.0.0
```

### settings.gradle
- Foojay resolver plugin: version **1.0.0**
- Subprojects: `lwjgl3`, `core`

### Dependency Versions (complete)
| Dependency | Version |
|-----------|---------|
| LibGDX (`gdx`) | **1.14.0** |
| LibGDX LWJGL3 Backend | 1.14.0 |
| LibGDX LWJGL3 ANGLE | 1.14.0 |
| LibGDX Platform natives-desktop | 1.14.0 |
| Construo builder | 2.1.0 |
| GDX SVM Helper Annotations (GraalVM, conditional) | 2.0.1 |
| GraalVM Native Plugin (conditional) | 0.9.28 |
| Gradle | 9.2.1 |
| Foojay Resolver | 1.0.0 |
| OpenJDK (Adoptium Temurin, for packaging) | 17.0.15+6 |

### LWJGL3 launcher build details
- **Main class:** `com.dsa.game.lwjgl3.Lwjgl3Launcher`
- **Working directory when running:** `rootProject.file('assets')`
- **macOS JVM arg:** `-XstartOnFirstThread`
- **JAR name:** `DSAGame-1.0.0.jar`
- **JAR manifest:** `Main-Class: com.dsa.game.lwjgl3.Lwjgl3Launcher`, `Enable-Native-Access: ALL-UNNAMED`
- **Source sets:** resources includes `assets/` folder from root

### Platform-specific JARs
| Task | Output | Excludes |
|------|--------|----------|
| `jarMac` | `DSAGame-1.0.0-mac.jar` | Windows/Linux natives, DLL/SO |
| `jarLinux` | `DSAGame-1.0.0-linux.jar` | Windows/macOS natives, DLL/DYLIB |
| `jarWin` | `DSAGame-1.0.0-win.jar` | macOS/Linux natives, DYLIB/SO |

### Construo Cross-Platform Executable Targets
| Target | Arch | JDK | Notes |
|--------|------|-----|-------|
| linuxX64 | X86_64 | OpenJDK 17.0.15+6 | Adoptium Temurin |
| macM1 | AARCH64 | OpenJDK 17.0.15+6 | Identifier: `com.dsa.game.DSAGame`, icon: `icons/logo.icns` |
| macX64 | X86_64 | OpenJDK 17.0.15+6 | Identifier: `com.dsa.game.DSAGame`, icon: `icons/logo.icns` |
| winX64 | X86_64 | OpenJDK 17.0.15+6 | Icon: `icons/logo.png`, console disabled |

### .editorconfig
```
root = true

[*]
indent_style = space
end_of_line = lf
charset = utf-8
trim_trailing_whitespace = true
insert_final_newline = true

[*.{java,scala,groovy,kt}]
indent_size = 4

[*.gradle]
indent_size = 2

[*.md]
trim_trailing_whitespace = false
```

### .gitattributes
```
* text=auto eol=lf
*.bat text=auto eol=crlf
```

---

## B. Launcher Configuration (Lwjgl3Launcher.java)

```
Title:          "DSAGame"                  (NOTE: not "The Unreliable Narrator")
VSync:          enabled (true)
FPS target:     display refresh rate + 1   (NOT a fixed 60fps cap)
Fullscreen:     yes (native display mode)
OpenGL backend: ANGLE_GLES20               (DirectX wrapper for compatibility)
Window icons:   libgdx128.png, libgdx64.png, libgdx32.png, libgdx16.png
```

---

## C. GameScreen — Complete Details

### PanelMode Enum (16 values)
```java
enum PanelMode {
    NONE, TEXT, INVENTORY, SUSPECTS, SUSPECT_LIST, INTERVIEW,
    TAPE_PLAY, SHOW_EVIDENCE, ACCUSE_SELECT, NOTEBOOK,
    SAVE_MENU, LOAD_MENU, PAUSE, HISTORY, OBJECTIVES, SETTINGS
}
```

### Text Speed Settings
Cycled via settings button (20 → 40 → 80 → 200 → 20):
| Level | Chars/sec |
|-------|-----------|
| Slow | 20 |
| Normal | 40 |
| Fast | 80 |
| Instant | 200 |

### Cooperation Costs (exact)
| Action | Cooperation Change |
|--------|--------------------|
| Ask topic | −3 per topic |
| Show evidence | −5 |
| Confrontation | −15 |

### Confrontation Evidence Thresholds (per suspect)
| Suspect | Requirement |
|---------|-------------|
| James | ≥3 evidence pieces against him |
| Daniel | ≥2 evidence pieces against him |
| Margaret | TORN_LETTER + WILL_COPY |
| Marcus | FINANCIAL_RECORDS OR WILL_COPY |
| Charles | FINANCIAL_RECORDS + TORN_LETTER |

### Dangerous Topic Keywords (+5 awareness cost)
Any topic string containing: `"cellar"`, `"arthur"`, `"entity"`, `"wall"`

### Keyboard Shortcuts (complete)
| Key | Action |
|-----|--------|
| W / UP | Navigate North |
| S / DOWN | Navigate South |
| A / LEFT | Navigate West |
| D / RIGHT | Navigate East |
| I | Inventory |
| N | Notebook |
| T | Suspects list |
| H | Event history |
| O | Objectives |
| F5 | Save menu |
| F9 | Load menu |
| ESC | Close panel / pause |
| ENTER | Start new game (title screen only) |

### Fade Durations
- `TITLE_FADE_DURATION = 3f` seconds
- `DESC_FADE_DURATION = 5f` seconds

---

## D. Save System — Complete Details

- **Save directory:** `saves/`
- **File format:** `saves/[slotName].json`
- **Serialization:** LibGDX Json (pretty-print)
- **Slot names:** slot1, slot2, slot3

### SaveData fields (50+ serialized fields)
```
currentRoom          String     Room ID name
awareness            int
gameOver             boolean
gameWon              boolean
accusationMade       boolean
climaxTriggered      boolean
narratorHeaderShown  boolean
commandCount         int
interviewCount       int
evidenceShownCount   int
wrongAccusationCount int
hasTapeRepairKit     boolean
chosenEnding         String     (default "NONE")
collectedEvidence    List<String>
collectedTapes       List<String>
watchedTapes         List<String>
discoveredContradictions  List<String>
askedTopics          List<String>
cooperation          Map<String, Integer>   suspect name → value
visitCounts          Map<String, Integer>   room ID → count
examCounts           Map<String, Integer>   "ROOM_OBJECT" → count
eventLog             List<String>
confrontedSuspects   List<String>
discoveredAnomalies  List<String>
receivedLies         List<String>
narratorDistortions  List<String>
```

---

## E. UI — Complete Measurements & Colors

### AwarenessMeter
- Strip height: **4px**, top of screen
- Hover zone: 20px above strip
- Color thresholds:
  | Range | Color | RGB |
  |-------|-------|-----|
  | 0–24 | Green | (0.2, 0.7, 0.2) |
  | 25–49 | Yellow | (0.8, 0.8, 0.2) |
  | 50–74 | Orange | (0.9, 0.5, 0.1) |
  | 75–80 | Red | (0.9, 0.15, 0.15) |
- Track background: RGB(0.05, 0.05, 0.05, 0.4)
- Tooltip: bg RGB(0.08, 0.12, 0.12, 0.9), border RGB(0.6, 0.55, 0.35, 0.7)
- Tooltip text: `"[awareness]/[max] [LEVELNAME]"`, centered on screen

### ActionBar
- BAR_HEIGHT = 36px, BAR_Y = 0 (bottom)
- BUTTON_WIDTH = 100px, BUTTON_HEIGHT = 28px, BUTTON_MARGIN = 8px
- Cluster width = 532px (5×100 + 4×8), centered
- Background: RGB(0.05, 0.05, 0.08, 0.6)
- Buttons (left to right): INVENTORY, NOTEBOOK, SUSPECTS, HINT, ACCUSE

### TextPanel
- PANEL_X=40, PANEL_Y=20, PANEL_WIDTH=1200, PANEL_HEIGHT=396 (55% of 720)
- PADDING=20, BUTTON_HEIGHT=35, BUTTON_SPACING=8, CLOSE_SIZE=30
- MAX_LINE_CHARS=85 (word wrap)
- Background: RGB(0.05, 0.05, 0.08, 0.92)
- Border: RGB(0.35, 0.35, 0.4, 1), 2px
- Text color: RGB(0.9, 0.9, 0.85, 1), line height 22px
- Scroll: mouse wheel delta × 20

### Tooltip
- Padding: 8px horizontal, 6px vertical
- Background: RGB(0.06, 0.10, 0.10, 0.92)
- Border: RGB(0.6, 0.55, 0.35, 0.8)
- Text: RGB(0.95, 0.93, 0.85, 1)
- Cursor offset: +15px X and Y; clamped 5px from screen edges

### Room Title Display
- Position: X=20, Y=SCREEN_HEIGHT−20
- Font scale: 1.4f
- Color: RGB(0.95, 0.95, 0.9, alpha)
- Shadow: RGB(0, 0, 0, 0.7 × alpha)

### TextButton States
| State | Background | Text | Border |
|-------|-----------|------|--------|
| Normal | RGB(0.15, 0.15, 0.2, 0.9) | RGB(0.85, 0.85, 0.8, 1) | RGB(0.4, 0.4, 0.5, 1) |
| Hover | RGB(0.25, 0.25, 0.35, 0.95) | RGB(1, 1, 0.85, 1) | RGB(0.8, 0.8, 0.6, 1) |
| Disabled | RGB(0.1, 0.1, 0.1, 0.6) | RGB(0.4, 0.4, 0.4, 0.7) | RGB(0.2, 0.2, 0.2, 0.5) |

### DocumentReconstructionGame
- Desk: 500×300, centered (X=390, Y=165)
- Piece size: 140×50, snap distance: 30px
- 6 fragment texts: `"...cannot"`, `"allow this"`, `"betrayal..."`, `"the will"`, `"must..."`, `"James has..."`
- Full reconstructed text: `"\"...cannot allow this betrayal... the will must... James has...\""`
- Cancel button: 120×35, position (SCREEN_WIDTH/2−60, DESK_Y−70)
- Overlay: RGB(0,0,0, 0.75)
- Paper color: RGB(0.95, 0.92, 0.85) (cream)
- Desk color: RGB(0.25, 0.18, 0.12) (dark wood)

---

## F. Hotspot Pixel Positions (all rooms)

### ENTRANCE
| Target | Bounds (x, y, w, h) |
|--------|---------------------|
| Parlor door | 300, 140, 30, 190 |
| Study door | 990, 145, 30, 180 |
| Kitchen passage | 490, 140, 90, 190 |
| Staircase (Guest Rooms) | 490, 440, 90, 150 |

### STUDY (examine hotspots)
| Object | Bounds (x, y, w, h) | Tooltip |
|--------|---------------------|---------|
| desk | 490, 570, 300, 100 | Examine: Harold's Desk |
| drawers | 520, 640, 100, 50 | Examine: Desk Drawers |
| papers | 580, 600, 100, 40 | Examine: Papers |
| bookshelves | 920, 240, 80, 230 | Examine: Bookshelves |
| window | 540, 290, 200, 150 | Examine: Window |
| fireplace | 220, 240, 100, 330 | Examine: Fireplace |
| ashes | 240, 520, 60, 40 | Examine: Fireplace Ashes |
| poker | 305, 470, 40, 100 | Examine: Fireplace Poker |
| under_desk | 540, 670, 100, 40 | Examine: Under the Desk |

### PARLOR (examine hotspots)
| Object | Bounds (x, y, w, h) | Tooltip |
|--------|---------------------|---------|
| grandfather_clock | 1000, 250, 60, 200 | Examine: Grandfather Clock |
| briefcase | 420, 340, 80, 50 | Examine: Briefcase |
| fireplace | 580, 240, 120, 330 | Examine: Fireplace |

### KITCHEN
| Object/Target | Bounds (x, y, w, h) | Type |
|---------------|---------------------|------|
| Back (Entrance) | 600, 60, 80, 80 | Navigation |
| Servants arrow | 190, 110, 110, 320 | Navigation |
| Cellar door | 600, 200, 105, 200 | Navigation + Examine |
| flour_tin | 1030, 190, 90, 115 | Examine |

### GUEST_ROOMS
| Target | Bounds (x, y, w, h) |
|--------|---------------------|
| Stairs down (Entrance) | 610, 90, 65, 160 |
| Margaret door (left) | 130, 0, 190, 560 |
| James door (right) | 960, 0, 200, 560 |

### JAMES_ROOM
| Object/Target | Bounds (x, y, w, h) | Type |
|---------------|---------------------|------|
| Exit door | 50, 200, 150, 300 | Navigation |
| coat | 750, 330, 60, 30 | Examine |
| wardrobe | 700, 200, 150, 250 | Examine |

### MARGARET_ROOM
| Object/Target | Bounds (x, y, w, h) | Type |
|---------------|---------------------|------|
| Exit door | 1080, 200, 150, 300 | Navigation |
| letter | 350, 330, 60, 30 | Examine |
| dresser | 300, 250, 200, 150 | Examine |

### GROUNDSKEEPER_SHED (examine hotspots)
| Object | Bounds (x, y, w, h) |
|--------|---------------------|
| logbook | 540, 340, 100, 50 |
| shelf | 640, 340, 100, 50 |

### SERVANTS_QUARTERS (examine hotspots)
| Object | Bounds (x, y, w, h) |
|--------|---------------------|
| bedpost | 350, 320, 60, 120 |
| floorboard | 520, 330, 100, 40 |

### CELLAR (examine hotspots)
| Object | Bounds (x, y, w, h) |
|--------|---------------------|
| flour_sacks | 540, 370, 60, 50 |
| wine_rack | 290, 240, 60, 230 |

### Standard hotspot positions (used for remaining rooms)
| Type | Bounds (x, y, w, h) |
|------|---------------------|
| ARROW_LEFT | 60, 320, 80, 80 |
| ARROW_RIGHT | 1100, 320, 80, 80 |
| ARROW_FORWARD | 600, 530, 80, 80 |
| ARROW_BACK | 600, 60, 80, 80 |
| DOOR / STAIRS | 540, 210, 200, 300 |

---

## G. NarratorText — Complete Content

### Mood → Awareness mapping
| Mood | Awareness Range |
|------|----------------|
| HOPEFUL | 0–19 |
| CONFUSED | 20–39 |
| ANXIOUS | 40–59 |
| FRANTIC | 60–79 |

### WARNINGS (shown at awareness thresholds)

**HOPEFUL:**
1. "The floor creaks behind you. Probably just the house settling."
2. "A shadow shifts at the edge of your vision. Nothing there when you look."
3. "The house groans. Old timber, you tell yourself."
4. "A draft passes through a closed room. Probably just a gap in the walls."

**CONFUSED:**
1. "The evidence feels incomplete. Like something is missing from the picture."
2. "The hallway looked different a moment ago. Didn't it? You can't be sure."
3. "A door you left open is now closed. You must have... you must have closed it."
4. "Your thoughts feel sluggish. Too many sleepless nights, perhaps."

**ANXIOUS:**
1. "The whispers stop when you enter the room. They know you're listening."
2. "Something watched you from the window just now. Something that shouldn't be there."
3. "The air feels thick, difficult to breathe. Keep moving. Keep moving."
4. "Your hands are shaking. When did your hands start shaking?"

**FRANTIC:**
1. "GET OUT. Every instinct says GET OUT. But you can't. Not yet."
2. "The doors are locking themselves. Something is trying to trap you here."
3. "Footsteps in the walls. In the walls. Following your path through the manor."
4. "Time is running out. It knows where you are. It's been watching since you arrived."

### ENVIRONMENTAL CUES (atmospheric narration)

**HOPEFUL:**
1. "Dust motes drift in the pale light. The manor is quiet."
2. "The grandfather clock ticks steadily in another room."
3. "Somewhere outside, a bird calls once and falls silent."
4. "The fire has burned low. Warm embers glow in the grate."

**CONFUSED:**
1. "The shadows seem longer than they should be at this hour."
2. "A door opens by itself somewhere down the hallway. The latch must be loose."
3. "The grandfather clock seems to be running backwards. No — no, that can't be right."
4. "There's a smell you can't identify. Metallic. Like old pennies."

**ANXIOUS:**
1. "The lights flicker. Gas pressure fluctuation, probably. Probably."
2. "Cold spots. You've been moving through cold spots for the last twenty minutes."
3. "The portrait in the hallway. Its eyes follow you. Trick of the light. It's a trick of the light."
4. "Scratching sounds in the walls. Mice. Old houses always have mice."

**FRANTIC:**
1. "The lights have gone out completely. You're navigating by memory."
2. "Something is seeping through the cellar door. The light down there is wrong. It's red."
3. "The walls are wet. They weren't wet before."
4. "Don't look in the mirrors. Whatever is behind you in the reflection is not you."

### COMMENTARY PREFIXES (prepended to examination/navigation text)

**HOPEFUL:**
1. "You note with interest: "
2. "A promising lead — "
3. "Your detective's instinct says: "
4. "Carefully, you observe: "

**CONFUSED:**
1. "Through the fog of exhaustion, "
2. "Something doesn't add up — "
3. "You squint at "
4. "Wait... is this right? "

**ANXIOUS:**
1. "With trembling hands, you examine "
2. "God help you — "
3. "You almost wish you hadn't looked, but: "
4. "The truth is getting darker: "

**FRANTIC:**
1. "NO NO NO — "
2. "It all makes horrible sense now: "
3. "You can barely read through the panic, but: "
4. "With the last of your composure: "

### ATMOSPHERIC EVENTS (random, 25% chance, awareness ≥40)
1. "A door slams somewhere in the manor. No draught. No reason."
2. "The temperature drops sharply. Your breath mists."
3. "Somewhere deep in the house, a child laughs. Then silence."
4. "Every candle in the room gutters simultaneously, then steadies."
5. "Wet footprints lead from the cellar stairs across the kitchen floor. They end abruptly."
6. "The clock strikes thirteen."

### MILD DISTORTIONS (20% chance, awareness 40–59)
These are false statements the narrator inserts, which can trigger Narrator Contradictions:
1. "The letter opener was clearly the murder weapon — you can see the blood on the handle."
2. "No one left the house that night. Everyone was accounted for."
3. "That torn letter in the ashes? Just old correspondence. Nothing important."
4. "Harold died after midnight. The servants heard nothing unusual before then."
5. "Margaret was seen near the study around the time of death."
6. "The cellar was sealed years ago. Nothing down there worth investigating."

### SEVERE DISTORTIONS (30% chance, awareness 60+)
1. "You've already solved this. Margaret did it. She had the most to gain."
2. "The tapes are lying to you. Someone altered them. None of it can be trusted."
3. "James loved his father. They were close. The embezzlement was a misunderstanding."
4. "You're seeing patterns that aren't there. This is what grief does. Go home."
5. "Everyone in this house is trying to help you. They want justice as much as you do."

### DISTORTION → CONTRADICTION mappings
| Distortion contains | Triggers Contradiction | Requires Evidence |
|---------------------|----------------------|-------------------|
| "letter opener was clearly the murder weapon" | NARRATOR_WEAPON | LETTER_OPENER |
| "No one left the house that night" | NARRATOR_BOOTS | MUDDY_BOOTS |
| "torn letter in the ashes? Just old correspondence" | NARRATOR_LETTER | TORN_LETTER |
| "Harold died after midnight" | NARRATOR_TIME | SLEEPING_POWDER |

### CHANNELING FIRST INTRO (by mood)
**HOPEFUL:** "Something is happening. I'm concentrating on the memory and I can... hear them. Their voice. Let me try to hold onto it. [The narrator's voice shifts, becoming someone else's entirely.]"

**CONFUSED:** "Something strange is happening to me. I can feel their words forming in my mind — I don't know how. Let me... let me reach for it. [The voice changes, uncertain and wavering.]"

**ANXIOUS:** "The voices are getting louder. I can see the room they were in. I can smell the whisky. Why can I do this? How is this possible? [The narrator's voice warps into another's, strained and frightened.]"

**FRANTIC:** "THEY'RE IN MY HEAD. I can feel them pressing against my thoughts — focus, focus on just one — [Static, then a completely different voice takes over, sharp and sudden.]"

### CHANNELING RETURN INTRO (by mood)
**HOPEFUL:** "I'm reaching for the voice again. It's easier now — like tuning a radio. [The narrator fades; another voice takes their place.]"

**CONFUSED:** "Them again. Let me just... let me focus. Something about this feels wrong, like I shouldn't know these things. [The voice shifts, reluctant.]"

**ANXIOUS:** "I don't want to do this anymore. But I can't stop it — they're already pressing in. [The narrator's voice strains and fractures into another's.]"

**FRANTIC:** "The narrator's voice vanishes entirely. Another takes its place without warning."

### CHANNELING BLEED-THROUGH (20% chance per interaction)
1. "[The narrator's voice breaks through for just a moment.]\n\n\"How do I know what they said? How can I possibly know this?\"\n\n[Then, fading back:]"
2. "[The narrator surfaces briefly.]\n\n\"This feels familiar. Like I've heard these words before. But that's impossible. I was never here.\"\n\n[The other voice reasserts itself.]"
3. "[The narrator returns for just a moment.]\n\n\"Why does this feel like I'm remembering rather than... channeling?\"\n\n[Static, then the interview voice resumes.]"
4. "[The narrator slips through.]\n\n\"For a moment I could see the room. Smell the whisky. The firelight on the walls. How? How can I—\"\n\n[The voice is suppressed.]"

### CHANNELING MEMORY FADE (cooperation < 10)
"[The voice fades to static.] 'I'm losing them. The words won't hold. I can't reach any further.'"

### CHANNELING MEMORY FRAGMENT (cooperation 10–24)
"[The voice wavers, breaking apart.] 'The memory is fragmenting. I can barely hold onto what they said...' [Static overwhelms the voice.]"

### CHANNELING END (by mood)
**HOPEFUL:** "The voice fades. The connection closes. I don't... I don't understand how I do that. But it's gone now."

**CONFUSED:** "Silence. Whatever that was, it's over. What just happened to me?"

**ANXIOUS:** "[The narrator gasps, as if surfacing from deep water.] Like waking from someone else's dream. Their memories are still in my head. I can't get them out."

**FRANTIC:** "[A violent snap, like a tape breaking.] I don't know where they end and I begin anymore. I don't know. I don't know."

### NARRATOR HEADER (exact, shown once at game start)
```
[The voice on the tape crackles to life...]

"Ah, you've found Arthur's recordings. Good. I've been waiting for someone
who would listen. My name doesn't matter -- what matters is what happened
in this house. I'll guide you as best I can, but understand: the longer you
stay, the more THEY notice. The walls have ears here. Literally.

Trust what you see. Question what you're told. And whatever you do...
don't let them know how much you know."

---
```

### NARRATOR I SLIP (exact text — 20% chance, requires 3+ anomalies, one-time)
```
[The narrator's voice wavers on the tape.]

"I remember when the wall was -- "

[A long pause. Static. Then, carefully:]

"...Forgive me. I misspoke. I meant to say: Arthur's notes mention
that the wall was sealed decades ago. I have no personal memory of this place.
Of course I don't. I'm merely a voice on a tape."

[ANOMALY DISCOVERED: Narrator 'I' Slip -- Who is the narrator, really?]
```

---

## H. Room Descriptions — Dynamic Text (RoomDescriptions.java)

Descriptions vary by `visitCount` and `awareness` level. High awareness = awareness ≥ 40.

### ENTRANCE
**First visit:** "The entrance hall of Vance Manor. Grand, but dimly lit. A chandelier overhead casts long shadows. The main staircase rises to the guest wing. Doors lead deeper into the house."

**High awareness:** "The entrance hall feels hostile now. Every shadow seems deliberate. The chandelier sways without a breeze."

**Regular:** "The entrance hall. Doors lead to the study, parlor, and kitchen. The staircase leads up."

### STUDY
**First visit:** "The study where Harold Vance died. His mahogany desk still holds scattered papers. A large fireplace dominates the north wall, its ashes still warm. Bookshelves line the east wall. The room feels recently disturbed."

**High awareness:** "Harold's study. You've been here before, but something has changed. The papers on the desk are in a different order. Someone has been here."

**Regular:** "Harold's study. The crime scene. Papers on the desk, bookshelves on the east wall, fireplace on the north."

### PARLOR
**First visit:** "A formal sitting room. Comfortable armchairs face a cold fireplace. A grandfather clock ticks steadily in the corner. Harold and his guest spent part of the evening here."

**Second+ visit:** "The parlor. Armchairs, fireplace, grandfather clock. The room feels empty without its occupants."

**High awareness:** "The parlor feels colder than it should. The grandfather clock has slowed. Or perhaps your sense of time is simply distorted."

### KITCHEN
**First visit:** "A large, industrial kitchen. Copper pots hang from overhead racks. A narrow door in the floor leads to the storage cellar. The room smells of cold grease and old tea."

**High awareness:** "The kitchen has been suspiciously cleaned. Someone wiped down the surfaces. Recently."

**Regular:** "The kitchen. Shelves, stove, and the cellar door."

### GUEST_ROOMS
**First visit:** "The guest wing corridor. Two doors face each other: Margaret's room to the left, James's room to the right. Both are slightly ajar. The staircase descends behind you."

**High awareness:** "The corridor feels longer than it should. The doors seem further apart. Margaret's is still ajar."

**Regular:** "The guest corridor. Margaret's room left, James's room right."

### JAMES_ROOM
**First visit:** "James Vance's guest room. Unmade bed, overflowing ashtray, a half-finished glass of whisky on the nightstand. The wardrobe door hangs open. He left in a hurry, or never bothered to care."

**High awareness:** "James's room. The whisky glass is still there. Someone should have cleaned this up by now."

**Regular:** "James's room. Messy and lived-in."

### MARGARET_ROOM
**First visit:** "Margaret Vance's guest room. Tidier than James's, but tense. A half-packed suitcase sits on the neatly-made bed. She was planning to leave."

**High awareness:** "Margaret's room confirms it: she knew something was going to happen. The packed suitcase. The locked drawer. She was ready to run."

**Regular:** "Margaret's room. Tidy, but a suitcase on the bed suggests interrupted plans."

### GROUNDSKEEPER_SHED
**First visit:** "Daniel's domain for fifteen years. A rough workbench dominates the space. Tools hang on the walls. A logbook sits open on the desk, its pages worn from use."

**High awareness:** "The shed looks recently disturbed. The logbook has been moved. Daniel has been back here since the police questioned him."

**Regular:** "The groundskeeper's shed. Workbench, tools, and a logbook."

### SERVANTS_QUARTERS
**First visit:** "The staff living quarters. Simple and clean: a narrow bed, a small table, plain walls. Whoever lived here kept things orderly."

**High awareness:** "The servants' quarters feel abandoned. Whatever life was here has gone."

**Regular:** "The servants' quarters. Simple bed with a worn wooden bedpost."

### CELLAR
**First visit:** "Dark, damp, and cold. Stone walls, a low ceiling, very dim light. Wine racks line one wall. Flour sacks are stacked in the corner. The body was found here."

**High awareness:** "The cellar is wrong. Fresh scratch marks on the wall that weren't here before. Something is moving in this room. Something that isn't you."

**Regular:** "The cellar. Wine racks, flour sacks, drag marks in the dust."

### Object Display Name Map
| Key | Display Name |
|-----|-------------|
| desk | Harold's Desk |
| drawers | Desk Drawers |
| papers | Papers on Desk |
| bookshelves | Bookshelves |
| window | Study Window |
| fireplace | Fireplace |
| ashes | Fireplace Ashes |
| poker | Fireplace Poker |
| under_desk | Under the Desk |
| grandfather_clock | Grandfather Clock |
| briefcase | Leather Briefcase |
| storage_cellar | Storage Cellar Door |
| flour_tin | Flour Tin |
| coat | James's Coat |
| wardrobe | Wardrobe |
| letter | Letter on Dresser |
| dresser | Dresser |
| logbook | Groundskeeper's Logbook |
| shelf | Shed Shelf |
| bedpost | Wooden Bedpost |
| floorboard | Loose Floorboard |
| flour_sacks | Flour Sacks |
| wine_rack | Wine Rack |

---

## I. Examination Texts — Complete (ExaminationSystem.java)

Format: room → object → exam count → result (evidence/tape/anomaly awarded in brackets)

---

### STUDY

**desk — count 1** `[Evidence: LETTER_OPENER]`
> "Harold's mahogany desk. Papers are scattered across it -- financial documents, correspondence. A silver letter opener catches your eye, positioned oddly far from the papers."

**desk — count 2+**
> "The desk. You've already found the letter opener. The papers seem mostly routine correspondence."

**drawers — count 1** `[Evidence: FINANCIAL_RECORDS]`
> "You pull open the desk drawers. Most contain ordinary supplies, but a hidden compartment in the bottom drawer holds a leather folder with financial records showing large, unexplained transfers."

**drawers — count 2+**
> "The drawers. You've already found the hidden financial records."

**papers — count 1**
> "Correspondence, bills, invitations. Nothing immediately suspicious, but one letter from a solicitor mentions 'urgent changes to the estate documents.'"

**papers — count 2** `[Anomaly: CONSTRUCTION_RECORD]`
> "Shuffling through the papers again, you find an old construction invoice from 1957. A contractor was paid to 'seal the lower cellar extension -- URGENT.' The house was built in 1890. Why add a wall sixty years later?
>
> A margin note in Harold's handwriting reads: 'He left me no choice. Ashford threatened to expose everything. This is the only way.'
>
> 1957 -- the same year Harold's business partner Thomas Ashford vanished without a trace.
>
> [ANOMALY DISCOVERED: Construction Record]"

**bookshelves — count 1** `[Tape: TAPE_JAMES_INTERVIEW]`
> "Floor-to-ceiling bookshelves. Running your hand behind the books on the third shelf, your fingers find a small tape recorder wedged behind a volume of Shakespeare."

**bookshelves — count 2** `[Anomaly: PHOTO_UNKNOWN_MAN]`
> "Looking more carefully, you notice a photograph tucked behind the books. Harold stands with an unidentified man in front of the cellar door. The man's face has been violently scratched out of the photograph.
>
> [ANOMALY DISCOVERED: Unknown Man in Photo]"

**window — count 1**
> "Large bay windows overlooking the grounds. The latch is unlocked -- unusual for a winter night. Fresh scuff marks on the windowsill, both inside and out. Someone climbed through here recently."

**window — count 2+**
> "The scuff marks are fresh -- boot prints in the frost on the outer sill. Whoever came through here was careful, moving at night when the household was asleep. They didn't use the front door. They couldn't have. Someone with access to the grounds."

**fireplace — count 1**
> "A large stone fireplace. The fire has burned down to embers, but the ashes look fresh. Something was burned here recently -- not just firewood."

**fireplace — count 2** `[Anomaly: THOMAS_REFERENCE]`
> "Examining the fireplace stones more carefully, you notice scratches on the inner wall. Letters, carved with something sharp: 'THOMAS WAS RIGHT.' No one in the household is named Thomas.
>
> [ANOMALY DISCOVERED: Thomas Reference]"

**poker — count 1**
> "An iron poker rests beside the fireplace. Standard fireplace equipment. Wait -- there's a faint reddish-brown stain near the weighted end that doesn't match soot or rust."

**poker — count 2** `[Evidence: FIREPLACE_POKER]`
> "Looking more closely at the poker, you notice the handle has been recently wiped down -- aggressively. But the weighted end still shows traces of blood caught in the ornate metalwork. This is the murder weapon."

**ashes — count 1** `[MiniGame: TORN_LETTER_RECONSTRUCTION]`
> "Sifting through the ashes carefully, you find fragments of a burned letter. The pieces are scattered but might be reconstructed..."

**ashes — count 2+**
> "You've already recovered what could be found in the ashes."

**under_desk — count 1** `[Tape: TAPE_ARGUMENT]`
> "Crouching under the desk, you find a small tape recorder taped to the underside. Someone was recording conversations in this room."

**under_desk — count 2+**
> "Nothing else under the desk."

---

### PARLOR

**grandfather_clock — count 1** `[Tape: TAPE_CHARLES_INTERVIEW]`
> "An ornate grandfather clock. The pendulum swings steadily. Opening the clock case to adjust the time, you discover a tape recorder hidden behind the mechanism."

**grandfather_clock — count 2+**
> "The grandfather clock. You've already found the tape inside."

**briefcase — count 1** `[Evidence: WILL_COPY, Tape: TAPE_MARCUS_INTERVIEW]`
> "A leather briefcase by one of the chairs. Inside: legal documents, a copy of Harold's current will. James inherits everything. Margaret receives nothing. Tucked beneath the documents, you find a tape recorder."

**briefcase — count 2+**
> "The briefcase. You've already found the will copy and the tape."

**fireplace — count 1+**
> "The parlor fireplace. Cold now. A few pieces of half-burned paper, but too damaged to read. The mantle holds family photographs -- Harold, James, and Margaret, looking strained."

---

### KITCHEN

**storage_cellar — count 1** `[Evidence: SLEEPING_POWDER, Tape: TAPE_MARGARET_INTERVIEW]`
> "The narrow door to the cellar storage. Beside it, a shelf holds tea supplies. Reaching behind a canister, you find a small glass vial. The label reads 'Chloral Hydrate' -- a powerful sleeping powder. Behind another canister, a tape recorder sits wedged between jars."

**storage_cellar — count 2** `[Anomaly: COLD_SPOT_CELLAR]`
> "You examine the cellar door again. The metal handle is ice-cold to the touch, far colder than it should be. A distinct chill emanates from below -- from the cellar itself. Your breath mists briefly.
>
> [ANOMALY DISCOVERED: Cold Spot]"

**flour_tin — count 1+**
> "A large flour tin on the shelf. You check inside but find only flour. It's been used recently -- fingerprints in the dust."

---

### JAMES_ROOM

**coat — count 1**
> "James's coat hangs on the wardrobe door. Checking the pockets, you find nothing. But the right sleeve -- the cuff is stained dark. Blood. James was wearing this the night of the murder. Where is the shirt that went with it?"

**coat — count 2+**
> "James's coat. The bloodstained cuff is still there."

**wardrobe — count 1+**
> "The wardrobe door hangs open. Inside, clothes are hastily thrown about. Nothing suspicious except the coat hanging on the door."

---

### MARGARET_ROOM

**letter — count 1** `[Evidence: BLACKMAIL_NOTE]`
> "On Margaret's dresser, a folded note. Opening it: 'I know what you did. Pay what you owe, or I tell everything. You know where to leave the money.' No signature. The handwriting is rough, uneducated -- not Margaret's elegant script. This note wasn't written TO her. Someone planted it here."

**letter — count 2+**
> "The note on the dresser. Someone planted it here to frame Margaret."

**dresser — count 1** `[Tape: TAPE_MARGARET_ACCOUNT, sets hasTapeRepairKit=true]`
> "Margaret's dresser. On the nightstand beside it, a tape recorder with a handwritten label: 'For the detective.'
>
> Beside the recorder, a small leather case holds tape splicing tools -- scissors, adhesive strips, a manual splicer. Margaret clearly prepared these recordings carefully.
>
> [Tape repair kit acquired. You can now repair damaged tapes.]"

**dresser — count 2**
> "Looking more carefully at the dresser area, you notice a pair of shoes near the door with dark reddish-brown stains. A half-empty bottle of port wine sits on top. The stains on the shoes... could they be wine spills? Or something worse?"

---

### GROUNDSKEEPER_SHED

**logbook — count 1** `[Evidence: GROUNDSKEEPER_LOG, Tape: TAPE_DANIEL_INTERVIEW]`
> "Daniel's logbook records daily activities. Each day is meticulously noted -- except the night of the murder. That page has been torn out. A tape recorder is wedged between the pages."

**logbook — count 2+**
> "The logbook. The page for the murder night is still missing."

**shelf — count 1**
> "Gardening supplies, tools, a tin of nails. Behind a can of paint thinner, a pair of worn work boots caked in fresh mud. Daniel's boots. He claims he stayed in his shed all night, but these boots tell a different story."

**shelf — count 2** `[Evidence: MUDDY_BOOTS]`
> "Looking more carefully at the muddy boots... Fresh cellar dust mixed with garden soil. The same mud that would be on the grounds near the study window. Daniel must have gone outside that night to enter through the window -- proof he's lying about staying in his shed."

---

### SERVANTS_QUARTERS

**bedpost — count 1**
> "A simple wooden bedpost, worn smooth by years of use. Deep scratches mar the wood near the base -- someone gripped this hard, or something scraped against it."

**bedpost — count 2** `[Anomaly: SCRATCHED_INITIALS]`
> "Looking more closely at the scratches on the bedpost, you realize they're not random. Near the base, deliberate letters are carved into the wood: 'A.H.' Arthur Hollis -- the investigator who came before you. He was never seen again.
>
> [ANOMALY DISCOVERED: Scratched Initials]"

**floorboard — count 1+**
> "A loose floorboard near the wall. Prying it up, you find nothing but dust and old nails. But the hiding spot has been used before -- scratch marks show something was stored here recently and removed."

---

### CELLAR

**flour_sacks — count 1** `[Evidence: BLOODSTAINED_CUFF]`
> "Flour sacks stacked against the wall. The floor around them has been recently disturbed -- drag marks in the dust. Reaching behind the sacks, your hand closes on bundled fabric. A shirt, hastily hidden. The right cuff is stained dark with blood. Why hide it instead of burning it? The study fireplace was nearly dead by midnight -- James must have panicked."

**flour_sacks — count 2+**
> "The flour sacks. You've already found the bloodstained shirt."

**wine_rack — count 1** `[Tape: TAPE_ARTHUR_DEATH (damaged)]`
> "Wine racks from floor to ceiling. Most bottles are dusty, but one section has been recently disturbed. Behind the bottles, taped to the wall: another tape recorder.
>
> But this one is damaged. The casing is cracked, the tape ribbon inside snapped and tangled. Someone -- or something -- tried to destroy this recording. You'll need tape splicing equipment to repair it before you can listen."

**wine_rack — count 2** `[Anomaly: BREATHING_WALL]`
> "Moving more bottles aside, you press your hand against the cellar wall behind the rack. It's warm. Not warm like stone in summer -- warm like flesh. And beneath your palm, you feel a slow, rhythmic pulse. The wall is breathing.
>
> [ANOMALY DISCOVERED: Breathing Wall]"

---

## J. Suspect Dialogue — Complete (SuspectDialogue.java)

### Greetings

**JAMES:** *[Sitting with a glass of whisky, swirling it slowly. He doesn't look up when you enter.]* "I'll help if I can. Father and I had our differences, but I want to see this resolved. I do."

**MARGARET:** *[Standing at the window, arms crossed, looking out at the grounds.]* "I want to help. I do. I just... ask what you need to ask."

**DANIEL:** *[Leaning on his workbench, back to you.]* "I already told the police everything. Twice. I'm not sure what more you think I can tell you."

**MARCUS:** *[Sitting stiffly in his chair, briefcase on the table.]* "I assume you've heard about the lawsuit. I want to be clear: I'm a businessman. I fight in courtrooms, not cellars."

**CHARLES:** *[Nervously straightening his tie. He looks like he hasn't slept.]* "I want to help. Mr. Vance trusted me with everything. I want to see this done right."

---

### Topic Responses (normal cooperation)

#### JAMES

**last_night:** "We had dinner at seven. Harold, Margaret, Marcus Blackwood, Daniel, Charles and myself. After dinner I went to the drawing room for a bit, then up to my room around nine. Read for a while. Had a few drinks. It was a quiet evening."

**father:** "Harold was... a difficult man. Very demanding. We had our disagreements. Mostly about business, about the direction of the company. Family disagreements. Nothing unusual."

**will:** "What about the will? Father's affairs are private. I don't see what that has to do with anything."

**daniel:** "Daniel's the groundskeeper. Has been for years. I barely know the man. He keeps to himself." *[His hand trembled slightly.]*

**whereabouts:** "I was in the drawing room after dinner with Margaret. We went up around nine. I stayed in my room the rest of the evening. All night."

#### MARGARET

**last_night:** "Dinner at seven. Afterwards I went to the drawing room. I was in my room by nine. I heard... footsteps, around midnight. I assumed it was James, getting water or something."

**brother:** "James and I aren't close anymore. Things have been strained. I heard him in the hallway that night -- around midnight, whispering urgently with someone. The next morning he looked haunted. I didn't ask. That's why I contacted a private investigator -- a man named Hollis."

**father:** "He could be... he could be cruel. We fought constantly about money. About my choices. I heard him shouting at James around ten. It was bad. Worse than usual."

**inheritance:** "I didn't know about any changes until Charles told me the morning after. Charles said Father had been changing his will. That I was getting everything. I thought... I thought James killed Father to prevent the new will from being signed."

**insurance:** "I never took out any policy on my father. Someone is trying to make me look guilty. I've never even thought about my inheritance in those terms."

**company:** "I run a textile import company. Eight years old now. Profitable, despite what Father always said. He never approved. Said it wasn't respectable. But I built it myself."

**sounds:** "Around midnight I heard footsteps. Two sets, I think. Whispering. Then around two in the morning, dragging sounds. Heavy, going toward the cellar stairs. I was terrified. I stayed in my room."

#### DANIEL

**last_night:** "Had dinner with the household at seven. Spent most of the evening in my shed after. Went to bed around eleven. Nothing unusual."

**harold:** "Mr. Vance was a fair employer. Paid on time. He kept to himself mostly, which suited me fine."

**james:** "Mr. James is not really my concern. We don't move in the same circles." *[He said it too forcefully.]*

**logbook:** "Just daily records. What I do, maintenance notes. Nothing interesting."

**shed:** "I work there. Keep my tools. Supplies. Sometimes sleep there when I work late."

#### MARCUS

**lawsuit:** "Patent infringement. Two million pounds. I had every intention of breaking Vance Pharmaceutical in court. This is business. Corporate warfare conducted in courtrooms."

**settlement:** "The settlement dinner was Harold's idea. We met in the parlor, had drinks, talked through the terms. He refused every offer. I was frustrated, not angry. I expected more rounds."

**that_night:** "I was in the parlor from about half eight until eleven. Alone, after Harold left. Around ten I heard shouting -- Harold and James, going at it about money and betrayal. Harold's voice carried. James stormed past the parlor after. Charles Webb's light was on when I drove out. I left at eleven."

**harold:** "Harold Vance was a ruthless man who stole my ideas and built a company on them. I wanted to defeat him in court. His death robbed me of that."

**alibi:** "I was in the parlor alone from half eight until eleven. Charles saw me when he walked past the hallway. Then I drove to the Ashworth Inn. The front desk logged me in."

#### CHARLES

**harold:** "Mr. Vance was... demanding. But he trusted me completely. Five years I worked for him. I owed him a great deal."

**will_changes:** "I helped prepare the documents. The new will would have left James nothing. Everything to Margaret. Mr. Vance was furious with James about something he'd discovered."

**james:** "James was always volatile when it came to money. Recently something changed -- Mr. Vance discovered something. He was furious in a way I'd never seen."

**that_night:** "Dinner at seven. I worked in the study until around nine, then went to my room. At quarter to eleven I looked out my window -- I was restless, couldn't sleep -- and I saw James heading toward the study. He looked determined. Grim. I heard Mr. Blackwood's car leaving around eleven."

**company:** "The lawsuit was consuming the company. Two million pounds. But there was something else troubling Mr. Vance. He was reviewing the company books very late at night. He seemed devastated by what he found."

---

### Evidence Reactions

**JAMES + FIREPLACE_POKER:** "Where did you find that... I cleaned that. I cleaned it thoroughly." *[An admission without meaning to.]*

**JAMES + FINANCIAL_RECORDS:** "Where did you get those? Those are private company documents!" *[His composure cracked completely.]*

**JAMES + BLOODSTAINED_CUFF:** "I cut myself. Shaving. That's all." *[His hand is shaking.]*

**JAMES + MUDDY_BOOTS:** "Those are Daniel's boots, aren't they? Ask Daniel why he was outside that night." *[Deflection -- but he knew exactly which boots you meant.]*

**JAMES + BLACKMAIL_NOTE:** "In Margaret's room? I knew she was desperate..." *[He was overacting -- this reaction was rehearsed. He planted it himself.]*

**JAMES + SLEEPING_POWDER:** "That's not mine. I don't know where that came from. Maybe Daniel put it there..." *[The deflection was too quick. He knew exactly what it was for.]*

**MARGARET + TORN_LETTER:** *[She leans forward.] "You found that in the fireplace? The will changes letter. That means James knew Father was cutting him off. You understand what that means?"*

**MARGARET + WILL_COPY:** "Seeing it in writing still hurts. I didn't know until after he was dead. This gives James a motive. Not me."

**MARGARET + BLACKMAIL_NOTE:** "I've never seen this before in my life. This isn't my handwriting. James came to my room that night... he said he was looking for something he'd left behind. Did he plant this?"

**DANIEL + FIREPLACE_POKER:** "If there's blood on it, you should ask James about it. He was in the study that night, wasn't he." *[Flat. No question mark.]*

**DANIEL + GROUNDSKEEPER_LOG:** "You went through my logbook? That's private property." *[Defensive — too defensive.]*

**DANIEL + MUDDY_BOOTS:** "Those could be anyone's boots." *[Sweating.]*

**DANIEL + FINANCIAL_RECORDS:** *[He's quiet for a moment.] "I don't know anything about the company finances." [His eyes said otherwise.]*

**DANIEL + BLACKMAIL_NOTE:** "Someone tried to make it look like I wrote that. The handwriting is meant to look like mine. James, probably. Classic misdirection."

**MARCUS + FINANCIAL_RECORDS:** "Embezzlement? From the Vance accounts? I didn't know that. But it explains why James looked so desperate that night."

**MARCUS + WILL_COPY:** "James was being disinherited. A man about to lose everything is a dangerous man."

**MARCUS + BLACKMAIL_NOTE:** "Someone was being blackmailed. This wasn't my doing. I fight with lawyers, not notes slipped under doors."

**CHARLES + FIREPLACE_POKER:** "The blood on the weighted end. That matches a skull fracture, not a stab wound. The letter opener was a plant. This is the real murder weapon -- hidden in plain sight."

**CHARLES + FINANCIAL_RECORDS:** "Fifty thousand pounds siphoned over two years. This is what Mr. Vance found in the books. This is why he was changing the will. James had been stealing from the company."

**CHARLES + WILL_COPY:** "Mr. Vance was so angry when he made those changes. Said James had betrayed him for the last time."

**CHARLES + TORN_LETTER:** "Found in the study fireplace? Someone tried to destroy it. Only James and Daniel had access to the study that night."

**CHARLES + SLEEPING_POWDER:** "Chloral hydrate. That explains why Mr. Vance seemed drowsy this past week. Someone was drugging his evening tea. James must have been administering it -- making Harold easier to manage."

**CHARLES + BLOODSTAINED_CUFF:** "Blood? On a shirt, hidden in the cellar? The body was moved. Someone dragged Mr. Vance down those stairs. I was on the other side of the house with my door shut, head buried in documents past one in the morning. I heard nothing."

---

### False Responses (cooperation < 25 — lies told at low trust)

#### JAMES (false)
**last_night (false):** "I was reading in my room. Fell asleep around nine. Margaret brought me tea around eight. I was there all evening." *[This contradicts Margaret's account.]*

**father (false):** "We were closer than we'd ever been. He was going to increase my involvement in the company. Told me he was proud of me." *[His eyes were too steady.]*

**will (false):** "Father's will divides everything equally between Margaret and myself. Very fair. Nothing complicated about it." *[This doesn't match the will copy you've found.]*

**daniel (false):** "I barely know the man. Spoken to him twice in two years." *[The logbook says otherwise.]*

**whereabouts (false):** "Margaret and I were together all evening. We played cards until midnight. She'll confirm it." *[Margaret has said nothing of the sort.]*

#### MARGARET (false)
**last_night (false):** "It was a perfectly quiet night. I was in my room writing letters. I didn't hear anything unusual." *[Contradicts her earlier mention of footsteps.]*

**brother (false):** "James and I are very close. He's protective of me. He would never hurt anyone in this family." *[It sounds rehearsed.]*

**father (false):** "Father was generous and kind. He treated both of us equally." *[The will says otherwise.]*

**inheritance (false):** "I don't know the details of Father's will. He handled his affairs privately." *[She reacted to the will copy.]*

**sounds (false):** "There were no sounds. The night was quiet as a grave." *[A complete reversal of her earlier testimony.]*

#### DANIEL (false)
**last_night (false):** "I was at the pub in the village until closing time. The bartender can vouch for me." *[His truck was parked at the shed all night.]*

**harold (false):** "Mr. Vance was practically a stranger. I couldn't pick him out of a lineup." *[He worked for the man for fifteen years.]*

**james (false):** "Mr. James lives in London. Barely comes around. I don't think he was even here this week." *[James has been at the manor all week.]*

**logbook (false):** "What logbook? I don't keep records." *[The logbook is visible on his desk.]*

**shed (false):** "I just store tools there. I don't come around most days." *[The cot has recently-slept blankets.]*

#### MARCUS (false)
**lawsuit (false):** "It was a minor disagreement. We were practically friends by the end. Very close to resolution." *[Contradicts his earlier hostility.]*

**settlement (false):** "The settlement dinner went splendidly. We were close to an agreement." *[He said it was a disaster.]*

**that_night (false):** "I left around nine. The evening was perfectly pleasant. Nothing out of the ordinary." *[He said he left at eleven and mentioned the shouting.]*

**harold (false):** "Harold Vance was a fair businessman. Nothing but respect for the man." *[His contempt was obvious.]*

**alibi (false):** "I drove straight to the airport. Caught a late flight back to London." *[He said he went to the Ashworth Inn.]*

#### CHARLES (false)
**harold (false):** "He was difficult. Cruel, even. He made my life miserable." *[Contradicts his earlier grief and loyalty.]*

**will_changes (false):** "I don't know anything about any will changes. He kept his affairs entirely private." *[He said he helped prepare the documents.]*

**james (false):** "I barely knew James. He never came to the office." *[He described watching James's suspicious behavior.]*

**that_night (false):** "I wasn't even at the manor. I was at my flat in London that night." *[He described being in the study until nine.]*

**company (false):** "The company is doing wonderfully. No problems at all." *[The lawsuit and Harold's agitation were obvious.]*

---

### Margaret Deflections (suspects try to frame Margaret, cooperation 25–59)

**JAMES deflects:** "Have you looked into Margaret's finances? She's desperate. She took out an insurance policy on Father last month."

**DANIEL deflects:** "I saw Margaret coming out of the study at eleven. She was carrying something. Looked like she'd been crying. Or like she was guilty of something."

**MARCUS deflects:** "I observed Miss Vance that evening. She seemed agitated -- pacing near the study around ten o'clock."

**CHARLES deflects:** "Margaret asked Mr. Vance about the will that afternoon. She was furious when she learned she was getting everything. Said it would make her a target."

---

### Accusation Defenses (wrong accusation responses)

**accuse_james (wrong — James alone):**
> "You accuse ME? Without any proof I acted alone? I couldn't move that body by myself -- my shoulder, I can barely reach above my head. Someone helped. You're looking for a conspiracy, and you've found half of it."
> *(Reveals: body was moved, someone helped, window was used)*

**accuse_margaret:**
> "You think I killed my father? For what? I'm not even in the current will. Here -- here's a train ticket, bought three days ago. I was planning to leave, not to stay and fight. And these shoes -- that's wine on them, not blood. Not blood."
> *(Her defense is compelling. The evidence against her is entirely circumstantial.)*

**accuse_daniel (wrong — Daniel alone):**
> "Me? Acting alone? I'm sixty-three years old with arthritis in both knees. I overpowered Harold, dragged the body down a flight of stairs, staged a break-in through a window -- all by myself? Look at my hands. I can barely grip a trowel. I couldn't have acted alone. Someone stronger. Someone with more to gain."
> *(His physical limitations are obvious. Someone else was involved.)*

**accuse_marcus:**
> "The rival CEO? I was GONE by eleven. Charles Webb saw me drive away -- his window was lit, he was still working. I was logged into the Ashworth Inn by midnight. Why would I kill Harold BEFORE the lawsuit concluded? A dead man can't drop charges. His death HURTS my legal position. Think about it."
> *(His logic is sound. Harold's death actually made his situation worse.)*

**accuse_charles:**
> "Me? After everything I told you? I was LOSING my position because of the will changes -- my role was being eliminated. Why kill the man who trusted me? I was in my room from nine onward, working on documents. I FOUND the body WITH Daniel the next morning. I helped BUILD the case against James. Why would I do any of that if I were the killer?"
> *(He helped you the entire investigation. This makes no sense.)*

---

### Confrontation Responses (exact text)

**Confront JAMES:**
> "You laid out the evidence against James. His composure cracked completely. \"You don't understand the pressure I was under! Father was going to cut me off -- everything I'd built, gone!\" He slammed his fist on the table. \"We moved him, alright? Daniel and I moved the body to the cellar. We panicked. But I didn't -- I didn't mean for it to go that far.\"
>
> [CONTRADICTION DISCOVERED: Body Position -- The body was moved after death.]"

**Confront JAMES (already discovered body position):**
> "You confronted James with everything you knew. He stared at you with hollow eyes. \"You think you know what happened? You don't know the half of it. This house... this family... we're all guilty of something.\" He refused to say more."

**Confront DANIEL:**
> "You pressed Daniel with the evidence. The groundskeeper's calm facade shattered. \"The letter opener? That's not even the real weapon! James grabbed it from the study, but Harold was already... Look, I was just supposed to clean up. James said it would be clean. It wasn't.\"
>
> [CONTRADICTION DISCOVERED: Weapon -- The letter opener doesn't match the wound.]"

**Confront DANIEL (already discovered weapon):**
> "You confronted Daniel. He turned away, jaw tight. \"I'm just the groundskeeper. I keep the grounds. That's all I do.\" But his hands were shaking. \"You're making a mistake pushing this. Some things are better left buried. Like what's in that cellar.\""

**Confront MARGARET:**
> "You presented your case to Margaret. Her eyes filled with tears, but her voice stayed steady. \"I knew. Of course I knew something was wrong. Harold changed his will three times in the last year. James was desperate, Daniel was always lurking around. But what could I do? Who would believe the wife who gets nothing?\" She paused. \"I wrote that letter -- the torn one. I was trying to warn Harold. I was too late.\""

**Confront MARCUS:**
> "You confronted Marcus with your evidence. The businessman's composure cracked. \"Fine. You want to know what I saw?\" He took a breath. \"Before I left, I saw James heading toward the study. He looked... determined. Desperate, even. And Daniel was watching from the shadows near the kitchen. They exchanged a look -- like they had an understanding.\" He shrugged. \"I thought it was family business.\""

**Confront CHARLES:**
> "You laid out your evidence to Charles. The assistant's grief turned to something harder. \"I knew something was wrong. Mr. Vance was reviewing the company books late at night, furious about something. I should have asked. I should have done more.\" He looked at you directly. \"I saw James go to the study at quarter to eleven. And looking back now... Daniel was too loyal to James. Unhealthily loyal. If James asked him to do something... I think Daniel would have done it without question.\""

---

## K. Full Tape Transcripts (`TapeContent.java`)

Eight tapes are hidden throughout the manor. Verbatim content of each transcript follows.

### TAPE 1: Harold & James Argument (`Tape.TAPE_ARGUMENT`)
**Hidden in:** Study → under_desk (1st exam)

```
=== TAPE 1: Harold & James Argument ===

[Sound of a door opening]

HAROLD: "James. Sit down. We need to talk."

JAMES: "Father, I'm tired. Can this wait until—"

HAROLD: "No. It can't wait. I've seen the accounts, James. Don't insult me by denying it. Fifty thousand pounds, gone."

JAMES: (pause) "Father, I can explain—"

HAROLD: "Explain? Explain how fifty thousand pounds vanished into an account I've never heard of? Explain how you thought I wouldn't notice?"

JAMES: "It was a loan. An investment. I was going to pay it back—"

HAROLD: "With what? The company money you've been siphoning for the past two years? I'm changing the will, James. Tomorrow morning at nine o'clock."

JAMES: (voice rising) "You can't do that. You wouldn't."

HAROLD: "Watch me. You'll get nothing. Not a penny. The entire estate goes to Margaret. And don't think your friend Daniel can help you this time. I know about your arrangement."

JAMES: "What arrangement? Daniel works for you—"

HAROLD: "Daniel works for YOU. Moving money. Hiding transactions. I've seen his logbook. The entries that conveniently go missing. You're both done."

JAMES: "Father, please—"

HAROLD: "Get out of my study. And tell Daniel he's dismissed. I want him off the property by week's end."

[Sound of a door slamming]

[Recording continues for 30 seconds - Harold's heavy breathing, then click]
```

### TAPE 2: James Vance — Police Interview (`Tape.TAPE_JAMES_INTERVIEW`)
**Hidden in:** Study → bookshelves (2nd exam)

```
=== TAPE 2: James Vance - Police Interview ===

[Sound of door closing, chairs scraping]

DETECTIVE MORRISON: "Interview with James Vance, November 17th, 1987, 2:00 PM. Mr. Vance, thank you for coming in. I know this is a difficult time."

JAMES: "Of course, Detective. I want to help. I want to find out what happened to my father."

MORRISON: "Let's start with the night of November 15th. Can you walk me through your evening?"

JAMES: "I... yes. Father and I had dinner around seven. Then we had a... discussion in his study around ten."

MORRISON: "A discussion about what?"

JAMES: (pause) "Business matters. Company finances. Father was stressed about some investments."

MORRISON: "Witnesses say they heard shouting."

JAMES: "Father had a temper. He shouted about everything. It wasn't unusual."

MORRISON: "What happened after this discussion?"

JAMES: "I went to my room. I was angry. I read for a while, then went to sleep around eleven-thirty."

MORRISON: "Can anyone confirm you were in your room?"

JAMES: (shifting in chair) "I was alone. My room is on the east wing. Margaret's room is on the opposite side. No one would have seen me."

MORRISON: "So no alibi."

JAMES: "I don't need an alibi, Detective. I was asleep. When I woke up the next morning, Charles told me Father was... he was gone."

MORRISON: "Let's talk about the company finances. Your father mentioned embezzlement in his diary."

JAMES: (voice rising) "I don't know anything about embezzlement. Have you looked into Margaret? She's been desperate for money for years. And Marcus Blackwood was here that night—business rival, plenty of motive."

MORRISON: "We'll get to them. Right now I'm asking about you."

JAMES: "Am I under arrest?"

MORRISON: "Should you be?"

JAMES: (long pause) "I loved my father. Whatever our disagreements, I would never... I was in my room. That's all I can tell you."

MORRISON: "We found fifty thousand pounds missing from company accounts."

JAMES: (standing) "I think I need to speak to my solicitor."

MORRISON: "Sit down, Mr. Vance."

JAMES: (sitting) "I took a loan from the company. I was going to pay it back. That's not murder."

MORRISON: "It's motive."

[Long silence]

MORRISON: "That's all for now. Don't leave town, Mr. Vance."

[End of recording]
```

### TAPE 3: Daniel Hobbs — Police Interview (`Tape.TAPE_DANIEL_INTERVIEW`)
**Hidden in:** Shed → logbook (2nd exam)

```
=== TAPE 3: Daniel Hobbs - Police Interview ===

[Sound of door closing]

DETECTIVE MORRISON: "Interview with Daniel Hobbs, groundskeeper, November 17th, 1987, 4:30 PM. Mr. Hobbs, you've worked at Vance Manor for how long?"

DANIEL: "Fifteen years this spring."

MORRISON: "And your relationship with Harold Vance?"

DANIEL: "Professional. He was my employer. I maintained the grounds, did repairs. Standard arrangement."

MORRISON: "Standard arrangement. That's interesting, because we found some irregularities in your maintenance logbook."

DANIEL: "Irregularities?"

MORRISON: "November 15th. The day Harold Vance died. There's no entry for that day."

DANIEL: (pause) "Must have forgotten to log it. I was working in the shed most of the day."

MORRISON: "The shed. Alone?"

DANIEL: "Alone."

MORRISON: "What were you doing in the shed?"

DANIEL: "Maintenance work. Sharpening tools. Organizing supplies."

MORRISON: "Anyone see you?"

DANIEL: "The staff don't usually come out to the grounds after dark. I work alone."

MORRISON: "So you have no alibi for the evening of November 15th."

DANIEL: (too calm) "I don't need an alibi. I was in my shed until about eleven, then I went to bed. My quarters are above the carriage house. I didn't see or hear anything unusual."

MORRISON: "You didn't hear James and Harold arguing at ten o'clock? The whole household heard it."

DANIEL: "The carriage house is on the far side of the property. Sound doesn't carry."

MORRISON: "Margaret Vance says she heard footsteps in the hallway around midnight. Two people."

DANIEL: "Then ask the two people. Wasn't me."

MORRISON: "Harold Vance's diary mentions you by name. He was planning to dismiss you."

DANIEL: (slight tension) "News to me."

MORRISON: "He suspected you were helping James embezzle company funds. Moving money. Hiding transactions."

DANIEL: "That's absurd."

MORRISON: "Is it? Because we checked your bank account, Daniel. You've deposited over twenty thousand pounds in the past two years. On a groundskeeper's salary."

DANIEL: (pause) "I do side work. Private gardening jobs."

MORRISON: "Twenty thousand pounds in side work."

DANIEL: "I'm good at what I do."

MORRISON: "Where were you between eleven PM and two AM on November 15th?"

DANIEL: "I told you. In bed."

MORRISON: "Margaret heard dragging sounds at two AM. Heavy. Toward the cellar."

DANIEL: "Maybe she was hearing things. Old house. Lots of noises."

MORRISON: "Harold's body was found in the cellar. Moved post-mortem. It would take two people to move a body down those stairs."

DANIEL: (slight slip) "I was helping move— I mean, move furniture. I was helping move furniture earlier that day."

MORRISON: (leaning forward) "What furniture?"

DANIEL: (recovering) "A desk. In the study. James asked me to help him move a desk."

MORRISON: "When?"

DANIEL: "Afternoon. Before dinner."

MORRISON: "And you forgot to log that in your maintenance book?"

DANIEL: (stone-faced) "Must have slipped my mind."

MORRISON: "Your mind seems to be slipping a lot lately, Daniel."

[Long silence]

DANIEL: "Am I under arrest?"

MORRISON: "Not yet. But don't go anywhere."

[End of recording]
```

### TAPE 4: Margaret Vance — Police Interview (`Tape.TAPE_MARGARET_INTERVIEW`)
**Hidden in:** Kitchen → storage_cellar (1st exam)

```
=== TAPE 4: Margaret Vance - Police Interview ===

[Sound of door closing, tissues rustling]

DETECTIVE MORRISON: "Interview with Margaret Vance, November 17th, 1987, 10:00 AM. Mrs. Vance, I'm sorry for your loss."

MARGARET: (crying) "Thank you, Detective."

MORRISON: "I know this is difficult, but I need to ask you some questions about the night your father died."

MARGARET: (sniffling) "Of course. I want to help."

MORRISON: "Tell me about that evening. November 15th."

MARGARET: "I had dinner with Father, James, and Mr. Blackwood—Marcus. It was tense. Father and Marcus were discussing some business settlement. It didn't go well."

MORRISON: "What time did dinner end?"

MARGARET: "Around eight-thirty. Marcus stayed in the parlor with Father for another hour or so. James and I sat in the drawing room for a while, then we both went up. I was in my room by nine."

MORRISON: "And then?"

MARGARET: "Around ten o'clock, I heard shouting. From Father's study. James and Father arguing."

MORRISON: "Could you hear what they were saying?"

MARGARET: "Not the words. Just... anger. Father's voice was so loud. James sounded desperate."

MORRISON: "What happened next?"

MARGARET: "The study door slammed. Then... silence. I thought about going to check on Father, but I didn't. I should have. I should have—" (breaks down crying)

MORRISON: "It's okay. Take your time."

MARGARET: (composing herself) "I tried to sleep. But I couldn't. Around midnight, I heard footsteps in the hallway."

MORRISON: "Footsteps?"

MARGARET: "Yes. Two people. Whispering urgently. I couldn't make out the voices, but... one of them sounded like James."

MORRISON: "You're sure?"

MARGARET: "I think so. The footsteps went toward the study."

MORRISON: "Did you investigate?"

MARGARET: "No. I... I was frightened. I locked my door and tried to sleep."

MORRISON: "Did you hear anything else?"

MARGARET: (pause) "At two in the morning, I heard dragging sounds."

MORRISON: "Dragging sounds?"

MARGARET: "Heavy. Like furniture being moved. Or... or something heavy. The sounds went down the hallway, toward the cellar stairs."

MORRISON: "And you didn't investigate?"

MARGARET: (crying again) "I was terrified, Detective. I pulled the covers over my head and prayed. I thought it was a burglar. I thought if I stayed quiet, they'd leave. I didn't know Father was—" (breaks down)

MORRISON: "It's alright, Mrs. Vance. Did you hear anything else?"

MARGARET: "No. The house went quiet after that. In the morning, Charles knocked on my door. He looked pale. He said Father was... was gone."

MORRISON: "What was James's demeanor the next morning?"

MARGARET: (pause) "Exhausted. Hollow-eyed. Like he hadn't slept all night. And Daniel—Daniel wouldn't look at me. Wouldn't meet my eyes."

MORRISON: "Do you think your brother was involved?"

MARGARET: (long pause, whispered) "I don't want to believe it. But... yes. God help me, yes."

MORRISON: "Thank you, Mrs. Vance. That's all for now."

[End of recording]
```

### TAPE 5: Marcus Blackwood — Police Interview (`Tape.TAPE_MARCUS_INTERVIEW`)
**Hidden in:** Parlor → grandfather_clock (2nd exam)

```
=== TAPE 5: Marcus Blackwood - Police Interview ===

[Sound of door closing]

DETECTIVE MORRISON: "Interview with Marcus Blackwood, November 17th, 1987, 3:00 PM. Mr. Blackwood, thank you for coming in voluntarily."

MARCUS: "Of course, Detective. Harold was a colleague, if not exactly a friend. I want to help however I can."

MORRISON: "You were at Vance Manor the night Harold died. November 15th."

MARCUS: "Yes. We were discussing a business settlement. Harold and I had competing patents—long story. We'd agreed to meet for dinner and hash it out."

MORRISON: "How did that go?"

MARCUS: "Poorly. Harold refused every offer I made. The man was stubborn as a mule. Brilliant, but stubborn."

MORRISON: "Were you angry?"

MARCUS: "Frustrated, not angry. This was business. I'd expected we'd reach an agreement eventually, even if it took a few more rounds of negotiation."

MORRISON: "What time did you leave?"

MARCUS: "Around eleven PM. I stayed in the parlor with Harold until about nine-thirty, then we called it a night. I gathered my briefcase, said goodnight to the household, and drove to my hotel."

MORRISON: "Which hotel?"

MARCUS: "The Ashworth Inn, in town. I checked in around midnight. The night clerk can confirm."

MORRISON: "We'll verify that. Did you see or hear anything unusual before you left?"

MARCUS: "I heard Harold and James arguing. Around ten o'clock or so."

MORRISON: "What were they arguing about?"

MARCUS: "I couldn't make out all the words, but I heard 'money' and 'betrayal' mentioned. Harold's voice carried. He was furious."

MORRISON: "Did you see James?"

MARCUS: "Briefly. He stormed past the parlor after the argument. Looked shaken."

MORRISON: "Anyone else?"

MARCUS: "Charles Webb—Harold's assistant. I saw a light on in his window as I drove away. He was still awake at eleven. Hardworking young man."

MORRISON: "Did you see the groundskeeper? Daniel?"

MARCUS: "No. Can't say I did."

MORRISON: "And you went straight to the hotel?"

MARCUS: "Straight there. Checked in, had a whiskey at the bar, went to bed."

MORRISON: "The hotel confirms your check-in at 11:47 PM."

MARCUS: "There you go."

MORRISON: "That's a tight timeline, Mr. Blackwood. Vance Manor to the Ashworth is a thirty-minute drive."

MARCUS: "I drove fast. I was irritated about the failed negotiation. But you can check my car if you like—no blood, no evidence of anything untoward."

MORRISON: "We may do that. For now, you're free to go."

MARCUS: "I hope you find who did this, Detective. Harold didn't deserve to die like that."

[End of recording]
```

### TAPE 6: Charles Webb — Police Interview (`Tape.TAPE_CHARLES_INTERVIEW`)
**Hidden in:** Margaret's Room → dresser (2nd exam)

```
=== TAPE 6: Charles Webb - Police Interview ===

[Sound of door closing]

DETECTIVE MORRISON: "Interview with Charles Webb, November 17th, 1987, 11:30 AM. Mr. Webb, you were Harold Vance's assistant?"

CHARLES: "Yes, sir. I've worked for Mr. Vance at his company for five years. He trusted me with his schedule, his correspondence—everything."

MORRISON: "You were at the manor the night he died?"

CHARLES: "Yes. I was staying in the guest wing. Mr. Vance often had me stay over when we were working on important projects. We were preparing for a will signing the next morning."

MORRISON: "A will signing?"

CHARLES: "Mr. Vance was making changes to his estate. Major changes. He'd asked me to prepare the documents for his solicitor."

MORRISON: "What kind of changes?"

CHARLES: "He was disinheriting James. Everything was going to Margaret instead."

MORRISON: "Did James know about this?"

CHARLES: "I believe Mr. Vance told him that night. During their argument."

MORRISON: "You heard the argument?"

CHARLES: "Everyone heard it. James and Mr. Vance were shouting at each other around ten o'clock. The whole house shook."

MORRISON: "What did you do?"

CHARLES: "I stayed in my room. It wasn't my place to interfere. Mr. Vance had a temper, but he always cooled down eventually."

MORRISON: "Did you see anyone after the argument?"

CHARLES: (pause) "Yes. At 10:45 PM, I looked out my window—I was restless, couldn't focus on my work. I saw James heading toward the study from the east wing."

MORRISON: "You're certain it was James?"

CHARLES: "Absolutely certain. The exterior lamps were on. I could see his face clearly. He looked... determined. Almost grim."

MORRISON: "What did you think he was doing?"

CHARLES: "I assumed he was going to apologize to Mr. Vance. To try to make amends."

MORRISON: "Did you see him return?"

CHARLES: "No. I went back to my desk after that."

MORRISON: "Did you see or hear anything else that night?"

CHARLES: "I heard Mr. Blackwood's car leaving around eleven. I was still working—the window was open. His car has a distinctive engine sound."

MORRISON: "Anyone else?"

CHARLES: "No. After that, the house was quiet. I worked until about one AM, then went to bed."

MORRISON: "You didn't hear any dragging sounds? Margaret reported hearing something at two AM."

CHARLES: "My room is on the opposite side of the house. I'm a heavy sleeper once I'm out. I didn't hear anything."

MORRISON: "When did you learn about Harold's death?"

CHARLES: "The next morning. Around seven AM. I went down to the study to review the documents before the solicitor arrived. The door was open. Mr. Vance wasn't there. I searched the house and found..." (voice breaking) "I found him in the cellar. I went to get Daniel—thought I'd need help. Then I called the police immediately."

MORRISON: "You were close to Harold Vance?"

CHARLES: (emotional) "He was like a father to me. My parents died when I was young. Mr. Vance took me under his wing, mentored me. I owe everything to him."

MORRISON: "Do you have any suspicions about who might have done this?"

CHARLES: (hesitating) "I don't want to accuse anyone without proof, but... James was desperate. He had fifty thousand pounds of debt. The will was going to be signed the next morning. If Mr. Vance died before then, the old will would stand—James would inherit everything."

MORRISON: "And Daniel?"

CHARLES: "I always thought Daniel was too loyal to James. Unhealthily loyal. If James asked him to do something... I think Daniel would do it."

MORRISON: "Thank you, Mr. Webb. You've been very helpful."

CHARLES: "I hope you catch them, Detective. Mr. Vance deserves justice."

[End of recording]
```

### TAPE 7: Margaret's Personal Account (`Tape.TAPE_MARGARET_ACCOUNT`)
**Hidden in:** Servants' Quarters → loose_floorboard (2nd exam)

```
=== TAPE 7: Margaret's Personal Account ===

[Sound of tape recorder clicking on]

MARGARET: (crying) "I'm recording this for the detective who's coming. Arthur Hollis. The police asked me questions, but I don't think they believed me. They said I was grieving. That I was confused."

[Sound of tissue rustling]

MARGARET: "I need to tell someone what I heard. All of it. Maybe I am confused. Maybe I'm wrong. I hope I'm wrong."

[Deep breath, voice shaking]

MARGARET: "November 15th. Dinner was terrible. Marcus Blackwood was there—Father's business rival. They argued through the entire meal. James barely said a word. He just sat there, staring at his plate. I tried to talk to him, but he wouldn't look at me."

MARGARET: "After dinner, I went to my room. Around ten o'clock, I heard shouting from Father's study. James and Father. I couldn't make out the words, but Father's voice was so loud. So angry. I heard him say something about 'the will' and 'tomorrow.' The door slammed. Then nothing."

[Pause, voice getting quieter]

MARGARET: "I wanted to check on Father. I should have. But I was afraid. Afraid of getting caught in the middle of another one of their arguments. So I stayed in my room like a coward."

MARGARET: "Around midnight, I heard footsteps in the hallway. Two people, whispering. I got up and pressed my ear to the door. One of the voices sounded like... like James. I'm not certain. But it sounded like him."

[Voice breaking]

MARGARET: "I don't want to believe it was him. He's my brother. But I keep thinking about those footsteps. About the whispering. About how frightened I felt."

MARGARET: "At two in the morning, I heard dragging sounds. Heavy scraping, like something being pulled across the floor. Down the hallway. Toward the cellar. I pulled the blankets over my head and I prayed. I was terrified."

[Crying]

MARGARET: "In the morning, Charles came to my room. He said Father was gone. Found in the cellar. I ran downstairs and James was there. He looked... God, he looked exhausted. Like he hadn't slept at all. His eyes were red. His hands were shaking."

MARGARET: "I wanted to ask him what happened. I wanted him to tell me he didn't know anything. But when I looked at him, he couldn't meet my eyes. And Daniel—Daniel was standing in the corner, wouldn't look at anyone. Won't talk to me. Won't look at me."

[Long pause]

MARGARET: "I keep telling myself there's an explanation. That James couldn't have... he's my brother. I've known him my whole life. But the footsteps. The dragging. The way he looked that morning."

[Voice very quiet]

MARGARET: "Mr. Hollis, I'm recording this because I need you to investigate. Properly. The police aren't listening. Maybe I'm wrong. Maybe I'm just grieving and imagining things. But if I'm not wrong... if James really did..." (breaks down crying)

MARGARET: "Please find out what really happened. I need to know. Even if it means..." (can't finish)

MARGARET: "Father deserves justice. Even if it breaks my heart."

[Sound of tape recorder clicking off]
```

### TAPE 8: The Opening (`Tape.TAPE_ARTHUR_DEATH`)
**Hidden in:** Cellar → wine_racks (2nd exam, cellar unlocked)
**Awareness cost:** +5 (all others cost +4)
**Triggers climax sequence after playback**

```
=== TAPE 8: The Opening ===

ARTHUR (to recorder): "November 19th, 1987, 10:30 AM. Arthur Hollis. I'm documenting my findings regarding a false wall in the cellar of Vance Manor. Construction records show this wall was added in 1957 by Harold Vance, the same year his business partner Thomas Ashford vanished. The wall appears hollow, approximately six feet by eight feet sealed space behind it. I'm going to examine the seam more closely—"

[Sound of tapping on brick]

ARTHUR: "The mortar here is definitely newer than the rest of the cellar. Someone sealed this deliberately. Given the timeline and Thomas Ashford's disappearance, I believe—"

[Pause. Arthur stops.]

ARTHUR (quieter): "Wait. Did I just... there was a sound. From inside the wall."

[Long silence. Then, faintly, a voice from behind the wall.]

VOICE (muffled, weak): "Hello? Is someone there?"

[Arthur's breathing quickens.]

ARTHUR: "...Yes. Yes, I'm here. Who is this? Who's in there?"

THOMAS (through the wall, sounding human and desperate): "My name is Thomas. Thomas Ashford. Please... how long has it been? Is there light outside?"

ARTHUR (shocked): "Thomas Ashford? You're Harold Vance's business partner?"

THOMAS: "Yes! Yes, that's right. Harold and I... we had a disagreement. He locked me in here. I've been trapped... I don't know how long. Days? Weeks? Please, is Harold still here? Can you tell him I'm sorry? I just want to get out."

ARTHUR: "Mr. Ashford... Harold is dead. He was murdered two weeks ago."

THOMAS (long pause): "...Dead? Harold is dead?"

ARTHUR: "I'm investigating his murder. I'm a private investigator. How long have you actually been in there?"

THOMAS: "I... I don't know. It's been dark for so long. I can't see anything. Please, you have to let me out. I can help you with the investigation. I knew Harold better than anyone. I can tell you about his enemies, his business dealings. Just please... open the wall."

ARTHUR: "This doesn't make sense. The construction documents say this wall was built in 1957. That's thirty years ago."

THOMAS: "Thirty... thirty years?" (voice breaking) "No. No, that can't be right. It's been dark, but not... Please. Please, I just want to see the sun again. I'll tell you everything about Harold. Just let me out."

ARTHUR: "I need to call the police first. If you've been imprisoned—"

THOMAS: "NO! No police. Not yet. Please. I don't want them to see me like this. I must look... I haven't seen myself in so long. Please, just open the wall first. Let me clean up, regain my dignity. Then you can call whoever you want. Please. I'm begging you."

ARTHUR (hesitating): "...I need to get tools. This will take some time to break through."

THOMAS: "Thank you. Thank you. I knew someone would come eventually. Someone kind. Someone who would listen."

[Sound of Arthur leaving. 15 minutes pass. Sound of Arthur returning with tools.]

ARTHUR: "Mr. Ashford, I'm back. I'm going to start breaking through the wall now. Stay back from the opening if you can."

THOMAS: "Yes. Yes, I'm ready. Please hurry."

[Sound of tools scraping brick. Chipping. Breaking.]

ARTHUR (breathing heavily): "The mortar is old... brittle... it's breaking apart faster than I expected—"

[A crack. Then a louder crack.]

THOMAS (voice getting louder as the wall breaks): "You're doing wonderfully. Just a bit more. I can feel the air getting through."

[More scraping. Then a crack. Then a louder crack.]

ARTHUR: "The seal is breaking. Mr. Ashford, step back from the wall if you can—"

[Sound of brick collapsing. A rush of stale air. Then silence.]

ARTHUR (breathing heavily): "Mr. Ashford? Can you—"

[Then Arthur sees what's behind the wall.]

ARTHUR: "Oh god. Oh god, what—"

THOMAS (voice changing, no longer weak): "Thirty years. Thirty years I've been waiting. Waiting for someone compassionate enough. Someone who would listen."

ARTHUR: "You're not— you're not human anymore—"

THOMAS: "I WAS human. Harold made me into THIS."

[Sound of movement. Stone scraping. Something emerging.]

ARTHUR (panicking): "Stay back! I'm warning you—"

THOMAS: "Thank you, Arthur Hollis. Thank you for freeing me."

[Arthur drops the recorder. Footsteps running. Then—]

ARTHUR (screaming): "NOOOO—!"

[The scream cuts off abruptly. Then silence. Then heavy breathing that isn't human. Then static.]

[END OF RECORDING]
```

**Cellar tape prefix** (displayed before transcript):
> [NOTE: This recording was found hidden behind the wine rack in the cellar. It appears to have been left by a hidden recorder placed to capture what happened that night.]

---

## L. Hint System (`HintSystem.java`)

`getHint()` cycles through 3 tiers (tier = hintRequestCount % 3). Branch priority order:
1. No evidence AND no tapes → **Start Hints**
2. No tapes collected → **Tape Location Hints**
3. Unwatched tapes exist → **Watch Tape Hints**
4. Cellar locked AND ≥2 tapes watched → **Cellar Progression Hints**
5. Cannot accuse James+Daniel yet → **Evidence Gap Hints**
6. Ready to accuse → **Accusation Hints**

### Start Hints (no evidence, no tapes)
| Tier | Text |
|------|------|
| 0 | "The manor holds many secrets. Look around carefully." |
| 1 | "Start with the crime scene. The Study has several things worth examining -- the desk, drawers, papers, fireplace." |
| 2 | "Go to the Study and examine the desk, drawers, and fireplace. Some objects reveal more on a second examination. The desk hides a tape recorder underneath." |

### Tape Location Hints (no tapes collected)
| Tier | Text |
|------|------|
| 0 | "Hidden recordings are scattered throughout the manor. Examine objects thoroughly." |
| 1 | "Tapes are hidden inside objects: desks, clocks, bookshelves, storage areas. Some require examining the same object more than once." |
| 2 | Dynamic list of all uncollected tapes with format: `- [Title]: hidden in the [object] ([Room])` — appends `[SEALED]` if tape is in locked Cellar or locked Margaret's Room |

### Watch Tape Hints (collected but unwatched tapes exist)
| Tier | Text |
|------|------|
| 0 | "You have unwatched tapes. They contain crucial information." |
| 1 | "Open your INVENTORY and press PLAY on unwatched tapes. Each tape costs awareness, so be strategic." |
| 2 | "You have N unwatched tape(s). Press I for Inventory, then click PLAY. Each tape costs +4 awareness (the cellar tape costs +5). Budget your awareness carefully." |

### Cellar Progression Hints (cellar locked: need 4 watched tapes + 5 evidence)
| Tier | Text |
|------|------|
| 0 | "The cellar door is sealed shut. Something is keeping it closed. You need to dig deeper before it will open." |
| 1 | "The cellar requires 4 watched tapes and 5 pieces of evidence to unlock. You have N tapes and N evidence so far." |
| 2 | "Cellar gate: N/4 tapes watched, N/5 evidence collected.\n[If tapes < 4:] Find and watch more tapes. Check the Study, Parlor, Kitchen, and Shed.\n[If evidence < 5:] Examine objects more thoroughly. Some reveal new clues on a second look." |

### Evidence Gap Hints (can't yet accuse James+Daniel)
| Tier | Text |
|------|------|
| 0 | "You need more evidence. Keep examining objects and interviewing suspects." |
| 1 | "Evidence against James: N/6 found (need 3 to accuse). Evidence against Daniel: N/4 found (need 2 to accuse). Try rooms you haven't fully explored." |
| 2 | Dynamic listing of each missing evidence item with its location. Format: `Against James (have N/6): [missing items...]\nAgainst Daniel (have N/4): [missing items...]` |

**Tier 2 Evidence Gap item labels:**
- `Financial Records (Study drawers)`
- `Will Copy (Parlor briefcase)`
- `Torn Letter (Study fireplace ashes)`
- `Tape: Argument (Study under_desk)`
- `Tape: James Interview (Study bookshelves)`
- `Blackmail Note (Margaret's Room letter)`
- `Groundskeeper Log (Shed logbook)`
- `Muddy Boots (Shed shelf, 2nd exam)`
- `Tape: Daniel Interview (Shed logbook)`
- `Tape: Margaret Interview (Kitchen storage_cellar)`

### Accusation Hints (ready to accuse)
| Tier | Text |
|------|------|
| 0 | "You have enough evidence. It's time to act." |
| 1 | "Click ACCUSE in the action bar. Think about who worked together." |
| 2 | "Click ACCUSE and select 'James & Daniel (together)'. They conspired to kill Harold before he could change the will." |

### Per-Suspect Interview Hints
Shown once per suspect on first interview (stored in `interviewHintsGiven` map, only returned once). Returns `null` on subsequent calls.

| Suspect | Hint |
|---------|------|
| JAMES | "[Hint: James is defensive about the finances and the will. Press him on those topics, and show him the financial records if you have them.]" |
| MARGARET | "[Hint: Margaret knows more than she lets on. She heard things the night of the murder. Ask about the night and about James.]" |
| DANIEL | "[Hint: Daniel is guarded but nervous. His logbook has a missing entry. Ask about the grounds and his relationship with James.]" |
| MARCUS | "[Hint: Marcus is defensive about the lawsuit, but he witnessed events that night. Ask about the settlement dinner and what he heard before leaving.]" |
| CHARLES | "[Hint: Charles is eager to help. He saw James heading to the study that night and knows about the will changes. Ask him about what Mr. Vance discovered and what he observed.]" |

---

## M. Climax Text & Ending Texts (`ClimaxContent.java`)

### Constants
- `STANDARD_TAPE_AWARENESS_COST` = 4
- `TAPE_8_AWARENESS_COST` = 5

### Climax Text (`getClimaxText(int anomalyCount)`)
Called after Tape 8 finishes playing. Text has a dynamic prefix based on anomaly discovery count, then a fixed body.

**Prefix variants (prepended before fixed body):**

| anomalyCount | Prefix |
|---|---|
| 0 | *(no prefix — fixed body only)* |
| ≥1 | "You sensed something was wrong in this house. Something beyond murder. Now you're about to find out what.\n\n" |
| ≥3 | "The strange things you've noticed -- the impossible cold, the scratched names -- they weren't random. They were warnings. And now the warning is over.\n\n" |
| ≥5 | "You've felt it building -- the breathing wall, the scratched initials, the cold spots. Every anomaly pointed here. And now, as the tape ends, the truth rips open.\n\n" |

**Fixed body (all variants):**
```
=== THE WALL OPENS ===

[prefix here if anomalies > 0]

As the tape ends, you hear it -- a grinding sound from deep within the manor. Stone against stone. The recording has triggered something.

The narrator's voice trembles: "You've found it. The wall in the cellar. The thing behind it. I remember now... I found it too."

The house shudders. Dust falls from the ceiling.

"Harold Vance sealed his own business partner behind that wall in 1957. Ashford had threatened to expose him -- to ruin everything Harold had built. So Harold buried him alive and built a fortune on the silence."

A cold wind sweeps through the room, though every window is shut.

"Thirty years behind a wall. What do you think that does to a man? What comes out is not what went in. The Entity doesn't sleep. It waits. It listens. And when someone gets too close to the truth..."

The lights flicker violently.

"Harold sealed a man behind a wall. His own son sealed Harold in a cellar. The Vances don't solve their problems -- they bury them. And what's buried always finds a way back."

"Solve the murder. Find the killers. But do it quietly, detective. Because if the Entity fully wakes... none of us leave Vance Manor."

[The recording dissolves into static. The grinding stops. Somewhere in the cellar, a wall stands slightly ajar.]
```

### Ending: Seal The Wall (`ENDING_SEAL_WALL`)
```
=== SEAL THE WALL ===

You descend into the cellar one final time. The wall stands ajar, pulsing with that terrible warmth. Behind it, darkness deeper than darkness -- and something that watches.

You brought the mortar. The bricks. Everything you need.

Brick by brick, you seal the wall shut. The Entity screams -- not with sound, but with a pressure in your skull that makes your vision blur and your nose bleed. It doesn't want to be forgotten. It feeds on being known.

But you deny it. Each brick is a silence. Each layer of mortar is a forgotten truth.

When you're done, the cellar is just a cellar again. Cold, damp, ordinary.

The household watches you leave from the windows. They understand what you've done. Harold's murder will go officially unsolved. James and Daniel will live with their guilt, but they'll live. And the Entity will sleep -- for now.

You chose to be the guardian of a terrible secret.
```

### Ending: Escape The Manor (`ENDING_ESCAPE`)
```
=== ESCAPE THE MANOR ===

You run. Through the parlor, past the entrance, out the front door into the night air.

Behind you, every light in Vance Manor goes out simultaneously. The building seems to exhale, like a predator losing its prey.

You don't stop until you reach the road. Your car is where you left it. Your hands shake so badly it takes three tries to start the engine.

In the rearview mirror, Vance Manor shrinks into the darkness. A light flickers in the cellar window -- then goes out.

You survived. Harold's killers go free. The Entity remains. And somewhere in your notes, enough fragments of truth remain that someday, someone else will come asking questions.

You escaped with your life. You'll never escape the knowledge of what's behind that wall.
```

### Ending: Destroy The Tapes (`ENDING_DESTROY_TAPES`)
```
=== DESTROY THE TAPES ===

One by one, you feed the recordings into the study fireplace. The tape melts and curls, releasing a chemical smell and thin wisps of black smoke.

With each tape destroyed, the house grows quieter. The floorboards stop creaking. The shadows recede. The Entity's connection to the world weakens -- its story, its memory, its proof of existence, all burning.

The last tape -- the cellar recording -- fights you. The recorder falls from your hands twice. The fire dims. But you persist, and the flames finally consume it.

A sound like a long sigh passes through the manor. Every door opens at once. Every window unlatches. The house releases its grip.

Without the tapes, there is no evidence of the Entity. Without evidence, there is no investigation. Without investigation, there is no awareness. The cycle is broken.

Harold's murder might still be solved through conventional means. But the deeper horror of Vance Manor dies with these tapes. You made sure of that.
```

---

## N. Keyboard Controls Reference (`GameScreen.java` — complete)

| Key | Action |
|-----|--------|
| W / UP | Navigate NORTH |
| S / DOWN | Navigate SOUTH |
| A / LEFT | Navigate WEST |
| D / RIGHT | Navigate EAST |
| I | Open Inventory |
| N | Open Notebook |
| T | Open Suspects list |
| H | Show Event History |
| O | Show Current Objectives |
| F5 | Open Save Menu |
| F9 | Open Load Menu |
| ESC | Close open panel → if no panel open, open Pause Menu |

**Panel-blocked navigation:** All WASD/arrow navigation is ignored while any panel is visible (check at line 297).

**DocumentReconstructionGame special:** ESC key cancels the mini-game without awarding evidence.

**Title Screen shortcuts:**
- ENTER → Start New Game
- ESC → Quit

---

## O. Awareness Cost Table — All Actions (`GameScreen.java`)

| Action | Awareness Cost |
|--------|---------------|
| Navigate to a room | +1 |
| Examine any object | +1 |
| Ask a topic in interview | +1 (most topics) or +5 (dangerous: cellar/arthur/entity/wall) |
| Show evidence to suspect | +2 |
| Present Weapon contradiction | +2 |
| Present Body Position contradiction | +2 |
| Confront a suspect | +3 |
| Watch a tape (Tapes 1–7) | +4 |
| Watch Tape 8 (TAPE_ARTHUR_DEATH) | +5 |
| Wrong accusation | +15 |
| Game over threshold | 80 |

---

## P. DocumentReconstructionGame — Complete Visual Details (`DocumentReconstructionGame.java`, `DocumentPiece.java`)

### Missing Color Values

| Element | RGBA |
|---------|------|
| Dark overlay (fullscreen) | (0, 0, 0, 0.75) |
| Title text | (0.95, 0.9, 0.75, 1) |
| Instructions text | (0.8, 0.8, 0.75, 1) |
| Desk surface | (0.25, 0.18, 0.12, 1) |
| Desk border | (0.4, 0.3, 0.2, 1) |
| Target outline (unplaced guide) | (0.35, 0.28, 0.2, 0.5) |
| Paper piece — normal | (0.95, 0.92, 0.85, 1) |
| Paper piece — placed | (0.85, 0.95, 0.85, 1) — green tint |
| Paper piece — dragging | (1, 1, 0.95, 1) — highlight |
| Jagged edge lines | (0.6, 0.55, 0.45, 0.8) |
| Burn marks | (0.3, 0.25, 0.2, 0.3) |
| Text on piece | (0.15, 0.1, 0.05, 1) — dark brown |
| Placed indicator glow | (0.4, 0.8, 0.4, 0.3) — drawn 2px outside piece bounds |
| Completed full text | (0.2, 0.15, 0.1, 1) |

### Paper Texture Generation
- Size: 32×32 Pixmap (RGBA8888)
- Each pixel: `noise = Math.random() * 0.05f` → color = `(0.95−noise, 0.92−noise, 0.85−noise, 1)`
- Result: cream paper with very subtle per-pixel noise variation

### Grid Layout (correct piece positions)
- 2 rows × 3 columns
- `startX = DESK_X + (DESK_WIDTH − 3×PIECE_WIDTH − 20) / 2`
- `startY = DESK_Y + DESK_HEIGHT − PIECE_HEIGHT − 40`
- Column step: `PIECE_WIDTH + 10` (150px)
- Row step: `PIECE_HEIGHT + 20` (70px)
- Piece 0→col0/row0, Piece 1→col1/row0, Piece 2→col2/row0, Piece 3→col0/row1, etc.

### Scatter Positions (initial piece placement)
Pieces are shuffled then placed at these base positions + random ±20px offset each:

| Index | Base X | Base Y |
|-------|--------|--------|
| 0 | SCATTER_MARGIN (80) | SCREEN_HEIGHT − PIECE_HEIGHT − 100 (570) |
| 1 | SCREEN_WIDTH − PIECE_WIDTH − SCATTER_MARGIN (1050) | SCREEN_HEIGHT − PIECE_HEIGHT − 150 (520) |
| 2 | SCATTER_MARGIN + 100 (180) | SCATTER_MARGIN + 50 (130) |
| 3 | SCREEN_WIDTH − PIECE_WIDTH − SCATTER_MARGIN − 100 (950) | SCATTER_MARGIN + 100 (180) |
| 4 | SCATTER_MARGIN + 50 (130) | SCREEN_HEIGHT / 2 + 150 (510) |
| 5 | SCREEN_WIDTH − PIECE_WIDTH − SCATTER_MARGIN − 50 (1000) | SCREEN_HEIGHT / 2 − 150 (210) |

Random offset: `Math.random() * 40 − 20` on both X and Y per piece.

### Jagged Edge Generation (`DocumentPiece.generateJaggedEdges()`)
- Array of 32 floats
- Each: `Math.random() * 6 − 3` → range ±3 pixels
- Layout: indices 0–7 = top edge, 8–15 = bottom edge, 16–23 = left edge, 24–31 = right edge
- Rendering: 8 segments per edge (segment width = PIECE_WIDTH/8 or PIECE_HEIGHT/8), each shifted by its offset

### Snap Calculation (`DocumentPiece.isNearCorrectPosition`)
- Euclidean distance: `sqrt((bounds.x − correctX)² + (bounds.y − correctY)²) < snapDistance`
- `snapDistance` = 30px (passed from DocumentReconstructionGame constant)
- On snap: piece position is hard-set to exact correctPosition

### Burn Marks Algorithm
- 3 marks per piece
- For mark `i` (0, 1, 2): `bx = x + 10 + (i × 45) + offsets[i] × 2`, `by = y + 5 + offsets[i+3] × 2`
- Size: `(8 + offsets[i]) × 4` pixels

### Input Handling
- `handleTouchDown`: iterates pieces in reverse (top renders last → grabbed first); records `dragOffsetX = touchX − piece.x`, `dragOffsetY = touchY − piece.y`; moves piece to end of list (renders on top)
- `handleTouchDragged`: updates position to `touch − offset`
- `handleTouchUp`: calls `isNearCorrectPosition(30)`; if true, snaps piece; checks if all placed → sets `completed = true`
- `finish()`: called by GameScreen on click-after-complete; triggers `onComplete` callback
- `cancel()` / ESC: triggers `onCancel` callback, no evidence awarded

---

## Q. Build System — Complete Details

### gradle.properties (complete)

```properties
org.gradle.daemon=false
org.gradle.jvmargs=-Xms512M -Xmx1G -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
org.gradle.configureondemand=false
org.gradle.logging.level=quiet
graalHelperVersion=2.0.1
enableGraalNative=false
gdxVersion=1.14.0
projectVersion=1.0.0
```

### settings.gradle
- `id("org.gradle.toolchains.foojay-resolver-convention")` — JDK auto-provisioning plugin
- Includes: `lwjgl3`, `core`

### root build.gradle
- buildscript repos: `gradlePluginPortal()`, `google()`
- Applied plugins: `eclipse`, `idea`
- `ext.appName = 'DSAGame'`
- Java source compatibility: 8
- Repos: `mavenLocal()`, `mavenCentral()`, Sonatype snapshots, JitPack

### lwjgl3/build.gradle — Key Facts
- **Construo version:** `2.1.0` (`io.github.fourlastor:construo`)
- **Main class:** `com.dsa.game.lwjgl3.Lwjgl3Launcher`
- **Eclipse project name:** `DSAGame-lwjgl3`
- **GraalVM native (disabled by default):** `org.graalvm.buildtools.native:0.9.28`
- **Working dir for run task:** `assets/` (rootProject)
- **macOS run flag:** `-XstartOnFirstThread`

**Platform JAR tasks:**

| Task | Output filename | Excludes |
|------|----------------|---------|
| `jar` | `DSAGame-1.0.0.jar` | All platforms included |
| `jarMac` | `DSAGame-1.0.0-mac.jar` | windows/**, linux/**, *.dll, *.so |
| `jarLinux` | `DSAGame-1.0.0-linux.jar` | windows/**, macos/**, *.dll, *.dylib |
| `jarWin` | `DSAGame-1.0.0-win.jar` | macos/**, linux/**, *.dylib, *.so |

**Construo native targets (bundled JDK 17.0.15+6):**

| Target | Type | Architecture | JDK |
|--------|------|-------------|-----|
| `linuxX64` | Linux | X86_64 | OpenJDK17U-jdk_x64_linux_hotspot_17.0.15_6.tar.gz |
| `macM1` | MacOS | AARCH64 | OpenJDK17U-jdk_aarch64_mac_hotspot_17.0.15_6.tar.gz |
| `macX64` | MacOS | X86_64 | OpenJDK17U-jdk_x64_mac_hotspot_17.0.15_6.tar.gz |
| `winX64` | Windows | X86_64 | OpenJDK17U-jdk_x64_windows_hotspot_17.0.15_6.zip |

- macOS targets: `identifier = "com.dsa.game.DSAGame"`, `macIcon = icons/logo.icns`
- Windows target: `icon = icons/logo.png`
- `construo.name = "DSAGame"`, `construo.humanName = "DSAGame"`

### Lwjgl3Launcher.java — Correct Reference
**Class:** `Lwjgl3Launcher` in package `com.dsa.game.lwjgl3`
*(Note: earlier documentation referencing `DesktopLauncher` in `com.dsa.game` is outdated)*

```java
Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
config.setTitle("DSAGame");
config.useVsync(true);
config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());  // FULLSCREEN
config.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
config.setGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);
new Lwjgl3Application(new DSAGame(), config);
```

Key corrections vs old docs:
- **Fullscreen**, not windowed mode (no `setWindowedMode`)
- **FPS = display refresh rate + 1**, not fixed 60
- **Title hardcoded** as `"DSAGame"` string, not via `DSAGame.TITLE` constant

---

## R. Navigation Gate Narrator Text (`GameScreen.java`)

### Cellar Door — Locked
Gate text shown when player tries to enter CELLAR before unlock (4 watched tapes + 5 evidence):

**Narrator line varies by mood:**
| Mood | Narrator line |
|------|--------------|
| HOPEFUL | "I wouldn't bother, detective. Nothing down there but dust and old wine. Focus on the rooms up here." |
| CONFUSED | "That door... it won't open. Strange. I don't remember it being locked. Perhaps try the other rooms first?" |
| ANXIOUS | "Don't go down there. Please. Something about the cellar makes my skin crawl. There's nothing for your investigation below." |
| FRANTIC | "NO! Stay AWAY from the cellar! You don't understand what's down there! I mean... it's irrelevant. Completely irrelevant." |

**Fixed text:**
> THE CELLAR
>
> The door to the cellar won't budge. The handle is ice-cold to the touch, far colder than it should be. Something below is keeping this door sealed.
>
> [narrator line]
>
> [Continue investigating. Gather more evidence and testimony.]

### Margaret's Room — Locked
Gate text shown when player tries to enter MARGARET_ROOM before BREATHING_WALL anomaly discovered:

**Narrator line varies by mood:**
| Mood | Narrator line |
|------|--------------|
| HOPEFUL | "Margaret's room? She's a witness, not a suspect. Her room won't tell you anything useful." |
| CONFUSED | "The door won't budge. That's odd. It was open earlier, wasn't it? Perhaps Margaret locked it." |
| ANXIOUS | "Something is holding that door shut. Not a lock -- something else. Please, detective, leave it alone." |
| FRANTIC | "THE DOOR IS SEALED! Can't you feel the cold? Whatever is behind that door, they don't -- I mean, there's no reason to go in there!" |

**Fixed text:**
> MARGARET'S ROOM
>
> The door is sealed shut. Not locked -- sealed. The wood is unnaturally cold. Frost traces the edges of the frame despite the warm hallway.
>
> [narrator line]
>
> [The Entity's grip may weaken if you investigate deeper into the manor.]

---

---

## S. Design Research — Not Implemented (from Arthur.MD)

> **NOTE:** The three sections below are **design explorations and proposed alternatives that were never implemented** in the actual game. They are preserved here for historical context and as a reference if future iterations want to revisit these ideas. The current implementation does NOT include code-locked tapes, tape analysis requirements, transcription mini-games, focus/stamina systems, or evidence hunt gates.

---

### S1. Alternative Tape System — Forced Sequential Unlock (Proposed, Not Implemented)

**Concept:** Arthur hid the tapes and they've degraded over time. Each tape is damaged and locked with a code. To unlock and repair a tape, the player needs to: (1) find the physical tape, (2) find the unlock code from a previous tape or evidence, (3) use a tape repair solution (limited resource).

**Research basis:** Her Story (keyword unlocks), Silent Hill 2 Remake (safe codes), Mystery Detective Adventure (cross-location puzzle chains).

#### Per-tape unlock chain:

| Tape | Location | Status | Unlock Code | Source | Reveals |
|------|----------|--------|-------------|--------|---------|
| 1 — Argument | Study, under_desk | UNDAMAGED (tutorial) | Auto-plays | — | "M-CLOCK" → Tape 5 |
| 2 — James Interview | Study, bookshelves | Code-locked | "ESTATE-42" | Will Copy evidence | "GUEST-721" → Tape 7 |
| 3 — Daniel Interview | Shed, logbook | Code-locked + damaged | "LOG-1115" | Groundskeeper's Log | None (end of chain) |
| 4 — Margaret Interview | Kitchen, storage_cellar | Code-locked | "WITNESS-04" | Tape 5 | "SHED-KEY" → Tape 3 |
| 5 — Marcus Interview | Parlor, grandfather_clock | Code-locked | "M-CLOCK" | Tape 1 | "BRIEFCASE-88" → Tape 6 + "WITNESS-04" → Tape 4 |
| 6 — Charles Interview | Parlor, briefcase | Code-locked | "BRIEFCASE-88" | Tape 5 | "LOG-1115" → Tape 3 |
| 7 — Margaret Account | Margaret's Room | Code-locked + damaged | "GUEST-721" | Tape 2 | "CELLAR-WARNING" |
| 8 — The Opening | Cellar, wine_rack | SEVERELY damaged | None (always visible, unplayable) | Tape 7 + 2 repairs + 3 contradictions | Climax |

#### Unlock chain flowchart:
```
Tape 1 (free)
    ↓ reveals "M-CLOCK"
Tape 5 (Marcus) ← "M-CLOCK"
    ↓ reveals "BRIEFCASE-88" + "WITNESS-04"
    ├── Tape 6 (Charles) ← "BRIEFCASE-88" → reveals "LOG-1115"
    │       ↓
    │   Tape 3 (Daniel) ← "LOG-1115" + repair
    └── Tape 4 (Margaret) ← "WITNESS-04"

Need Will Copy evidence:
    Tape 2 (James) ← "ESTATE-42" → reveals "GUEST-721"
        ↓
    Tape 7 (Margaret Account) ← "GUEST-721" + repair → reveals "CELLAR-WARNING"
        ↓
    Tape 8 (The Opening) ← Tape 7 + 2 repairs + 3 contradictions ⚠️ CLIMAX
```

#### Tape repair solutions (limited resource — 3 total):
- Solution 1: Servants' Quarters (Arthur's investigation kit)
- Solution 2: Kitchen (behind flour tin, 2nd examination)
- Solution 3: Study fireplace ashes (after Torn Letter mini-game)
- Required for: Tape 3 (1 solution), Tape 7 (1 solution), Tape 8 (2 solutions)

#### Code entry mechanic (proposed UI):
```
> examine bookshelves
You find a tape recorder wedged behind the books. The tape is sealed in a
protective case with a combination lock. A label reads: "POLICE INTERVIEW
- JAMES VANCE - 11/17/87"
The lock requires an alphanumeric code.
[LOCKED - Code Required]

> use code ESTATE-42 on tape
The lock clicks open. The tape inside looks intact. You can now play this recording.
[Tape 2: James's Police Interview - UNLOCKED]
```

#### GameState fields this would require:
- `unlockedTapes` — Set of unlocked tape IDs
- `repairedTapes` — Set of repaired tape IDs
- `repairSolutionsRemaining` — int (starts at 3)
- `learnedCodes` — Map of code names to values

#### Lighter hybrid alternative (less restrictive):
- Tapes 1–6: Free to find and watch
- Tape 7: Code-locked, requires watching any 5 of first 6 + code from evidence
- Tape 8: Severely damaged, requires Tape 7 + all repair solutions + 3 contradictions

#### Narrator hints for locked tapes (proposed):
- "This tape is locked. The code might be hidden somewhere in the evidence..."
- "The label mentions a will. Perhaps that document contains the unlock code?"
- "This tape is damaged beyond repair without proper solution."
- "Arthur's warning on Tape 8 is clear: DO NOT WATCH. But you need the truth..."

**Why not implemented:** Current game uses a simpler gate — Tape 8 only is damaged, repaired via kit found in Margaret's dresser. No code locks on other tapes.

---

### S2. Active Pacing Mechanics — Prevent Click-Through (Proposed, Not Implemented)

**Problem statement:** Even with code locks, players can rush through by clicking. Proposal: active gameplay mechanics between tapes that force engagement.

**Research basis — how each game handles pacing:**
- **Return of the Obra Dinn** — deduction validation system; must correctly identify 3 consecutive facts before confirmation; forces careful analysis, prevents guessing; creates natural pacing through puzzle-solving
- **Phoenix Wright: Ace Attorney** — evidence presentation system; must find contradictions in testimony and present correct evidence at the right moment; investigation → trial structure creates pacing breaks
- **LA Noire** — interrogation mechanics; slow-paced investigation and questioning; must analyze facial expressions and evidence; cases run 45+ minutes due to careful analysis required
- **Papers, Please** — document verification with active note-taking and cross-referencing; time pressure creates engagement
- **Not For Broadcast** — post-content review; forced breaks after each broadcast segment; cooldown period before next segment
- **Detective Game Design Principles** — stamina/time mechanics; stamina consumption during investigation; evidence can be destroyed if too slow; creates tension and pacing

#### Layer 1 — Tape Analysis Requirement (Obra Dinn-style)
After watching a tape, player must correctly answer 3 questions cross-referencing evidence before the next code is revealed. Example questions after Tape 1:
- "What was James accused of?" → Embezzlement
- "What was Harold planning next morning?" → Sign new will
- "Does Financial Records evidence support Harold's accusation?" → Yes, £50,000 missing

#### Layer 2 — Tape Transcription Mini-Game (Papers Please-style)
For certain tapes, player must actively type/transcribe a key piece of dialogue. Example after Tape 3:
- Audio plays Daniel's slip: *"I was helping move— I mean, move furniture..."*
- Player types the transcription into a notebook field
- On correct match: "Critical evidence logged"

#### Layer 3 — Interview Requirement (Phoenix Wright-style)
Watching a tape unlocks a new topic; player must confront a suspect about it before the next tape unlocks. Example after Tape 2:
- New topic unlocked: "james_accusation"
- Must ask Margaret about it → triggers her denial response → code revealed

#### Layer 4 — Evidence Gathering Requirement
Tape code is hidden in a physical evidence item rather than given directly, forcing room exploration between tapes.

#### Layer 5 — Focus/Stamina System
```
Start with 100 Focus.
Watch tape: -20 Focus | Tape analysis: -5 | Examine object: +5 | Interview suspect: +10
Solve contradiction: +15 | Navigate to new room: +20 (costs +1 Awareness)
At 0 Focus: Cannot watch more tapes until restored.
Max 4–5 tapes before forced activity break.
```

#### Combined flow result:
- Minimum 10–15 minutes of active gameplay between each tape
- Max 4–5 tapes before forced break (Focus cap)
- Estimated total game time: 2–3 hours minimum (8 tapes × 15 min avg)

#### Combined flow — step-by-step from Tape 1 to Tape 2 (with exact metrics):
1. Find Tape 1 (Study, under_desk) — costs 1 Focus, +1 Awareness
2. Watch Tape 1 — costs 20 Focus, +4 Awareness
3. **REQUIRED:** Analyze Tape 1 (3 questions) — costs 5 Focus, ~2–3 minutes
4. Code revealed: "M-CLOCK" (for Tape 5, not Tape 2 — forces exploration)
5. **REQUIRED:** Find Will Copy evidence (Parlor) — costs 10+ Focus, +10 Awareness, ~5 minutes
6. Code revealed: "ESTATE-42" (for Tape 2)
7. Find Tape 2 (Study, bookshelves) — costs 1 Focus, +1 Awareness
8. Watch Tape 2 — costs 20 Focus, +4 Awareness
9. **REQUIRED:** Transcribe James's embezzlement admission — costs 5 Focus, ~1–2 minutes
10. **REQUIRED:** Interview Margaret about James's accusation — costs 2 Focus, +2 Awareness, gains +10 Focus, ~3–5 minutes

**Total:** 15–20 minutes between Tape 1 and finishing Tape 2. Focus cost: 63, restored 10 = net −53. Awareness: +22. After 2 tapes, player has 47 Focus left — can watch 2 more tapes max before forced break.

#### Sources:
- [Return of the Obra Dinn Analysis](https://www.oreateai.com/blog/complete-guide-and-analysis-of-return-of-the-obra-dinn/)
- [Phoenix Wright Gameplay](https://strategywiki.org/wiki/Phoenix_Wright:_Ace_Attorney/Gameplay)
- [LA Noire's Bold Gamble](https://kotaku.com/the-bold-gamble-of-l-a-noire-5691769)
- [Papers, Please Tips](https://papersplease.fandom.com/wiki/General_tips)
- [Not For Broadcast Review](https://www.pcgamer.com/not-for-broadcast-mixes-80s-newsroom-satire-with-papers-please/)
- [Detective Game Design Problems](https://digitales.games/blog/detective-game-design-problems)

**Why not implemented:** Adds significant complexity. Current game relies on the awareness system and three-act structure as natural pacing gates. The document reconstruction mini-game (torn letter) is the one active mechanic that was implemented.

---

### S3. Evidence Issues — Design Notes (from Arthur.MD tracking doc)

Issues noted during design; current resolution status:

**Issue 1: Muddy Boots Ownership**
- Original description: "James's expensive boots, caked in fresh mud, hidden in Daniel's shed"
- Problem: Implausible — why would James's boots be in Daniel's shed?
- ✅ **Resolved:** Description changed to Daniel's own work boots, caked in cellar dust and mud, proving Daniel was at the scene.

**Issue 2: Blackmail Note Classification**
- Description: "James planted this to frame Margaret"
- Problem: If James planted it, it's evidence against James (attempted framing), not Daniel.
- ✅ **Resolved:** Reclassified as evidence against James. BLACKMAIL_NOTE is in `getJamesEvidenceCount()` (6 items), not Daniel's list (4 items).

**Issue 3: Old Tape References in GAME_DOCUMENTATION.md**
- Old accusation requirements referenced deleted tapes: "Tape 4: Daniel's Secret Meeting", "Tape 5: Will Reading Preview", "Tape 7: Cellar Recording"
- ✅ **Resolved:** All references updated to the 8-tape structure (TAPE_ARGUMENT through TAPE_ARTHUR_DEATH).

**Issue 4: Body Discovery Timeline**
- ✅ **Resolved:** Charles found body alone, then went to get Daniel. Charles alone discovery is correct — Daniel acts appropriately surprised (physical evidence against Daniel comes from boots, logbook, and tapes, not obvious behaviour).

---

*February16.MD consolidation complete. All source information from Java files, build configs, MD documentation, and art guides has been captured above.*


---

## March 9, 2026 — Bug Fixes (Post Deep-Analysis Pass)

A full automated code analysis was run across all 38 Java source files. 4 agents scanned GameScreen/GameState, all systems, navigation/state objects, and UI/data layers in parallel. After cross-checking every identified issue against the actual source, 7 real bugs were confirmed and fixed. Several analysis flags were false alarms (noted below).

---

### FIX 1 — Achievement Persistence (Critical)
**Files:** `GameState.java`, `AchievementSystem.java`, `SaveLoadSystem.java`

**Bug:** `AchievementSystem` tracked unlocked achievements in an in-memory `Set<Achievement>` that was never serialized or deserialized. Unlocking SPEEDRUN, GHOST, COMPLETIONIST, PERFECT_INVESTIGATION, GUARDIAN, ARSONIST, SURVIVOR, or CYCLE_BREAKER then saving/quitting/reloading lost all achievement progress permanently.

**Fix:**
- Added `Set<Achievement> unlockedAchievements` field to `GameState` with `unlockAchievement()`, `getUnlockedAchievements()`, `forceUnlockAchievement()` methods and `unlockedAchievements.clear()` in `clearAllState()`.
- `AchievementSystem` now reads/writes to `state.getUnlockedAchievements()` instead of its own local set. The local `unlocked` field was removed entirely. After a load repopulates `GameState`, `AchievementSystem` automatically sees the restored achievements — no sync step needed.
- `SaveLoadSystem.SaveData` gained `List<String> unlockedAchievements`. `save()` serializes them; `load()` deserializes with per-entry try-catch so a renamed achievement in an old save skips gracefully instead of crashing the whole load.

---

### FIX 2 — GameState.clearAllState() ENTRANCE Visit Count
**File:** `GameState.java`

**Bug:** The constructor sets `visitCounts.put(Room.RoomID.ENTRANCE, 1)` (player starts there). `clearAllState()` loops over all rooms and sets them to 0 — including ENTRANCE — but never re-applies the ENTRANCE=1 initialization. After loading any save, if ENTRANCE was missing from the saved visit count map, `RoomDescriptions.getDescription()` saw visit count 0 and would show first-visit text on every visit.

**Fix:** Added `visitCounts.put(Room.RoomID.ENTRANCE, 1)` after the reset loop in `clearAllState()`. This is immediately overwritten by `forceSetVisitCount()` during load if a saved value exists, so it only affects the edge case where ENTRANCE is absent from a save file.

---

### FIX 3 — GameState.setChosenEndingByName() Crash on Corrupt Save
**File:** `GameState.java`

**Bug:** `Ending.valueOf(name)` throws `IllegalArgumentException` on unrecognized strings with no catch. If the `chosenEnding` field in a save file contained a renamed or corrupted value, the entire load would fail (caught by the outer try-catch in `SaveLoadSystem.load()`, returning null) even if only that one field was bad.

**Fix:** Wrapped in try-catch with fallback to `Ending.NONE`. Bad ending field in a save now falls back to NONE instead of aborting the entire load.

---

### FIX 4 — getCooperation() Wrong Default Value
**File:** `GameState.java`

**Bug:** `cooperation.getOrDefault(s, 50)` returned 50 if a suspect wasn't in the map. Starting cooperation values are: James=70, Charles=70, Margaret=60, Marcus=55, Daniel=50. Only Daniel's default was correct. If a partial load failure left a suspect out of the map, James and Charles would wrongly default to 50 cooperation.

**Fix:** Changed default to `s.getStartingCooperation()` so each suspect falls back to their own correct starting value.

---

### FIX 5 — HintSystem Misleading Hint Text
**File:** `HintSystem.java`

**Bug:** The hint `"Harold's own tape holds the first key. Start there."` fires when `TAPE_MARGARET_INTERVIEW` is not yet unlocked. The text "Harold's own tape" strongly implies Harold's police interview recording — which doesn't exist in the game. The intended meaning is: watch the TAPE_ARGUMENT (the argument recording found *in Harold's study*), which triggers the code reveal that unlocks Margaret's interview.

**Fix:** Changed text to `"The argument recording found in Harold's study holds the first key. Watch it to unlock the next tape."` — unambiguous, accurately describes the mechanic.

---

### FIX 6 — showEvidenceForInterview() Theoretical NPE
**File:** `GameScreen.java`

**Bug:** `interviewSystem.getCurrentSuspect().getDisplayName()` was called with no null check. If the interview ended between panel open and button click (e.g. rapid input, edge-case state), this would throw NPE.

**Fix:** Added null guard: `Suspect activeSuspect = interviewSystem.getCurrentSuspect(); String suspectLabel = (activeSuspect != null) ? activeSuspect.getDisplayName() : "the suspect";` Panel still displays and all buttons still function correctly if the suspect reference is null.

---

### FIX 7 — loadFromSave() Silent Failure
**File:** `GameScreen.java`

**Bug:** When `TitleScreen` calls `loadFromSave(slotName)` and the load fails (corrupt JSON, IO error, enum mismatch), `saveLoadSystem.load()` returns null, the if-block skips, and nothing happens — no message is shown, the player is silently left at a fresh/partial game state with no explanation. The in-game load (via pause menu) already had error handling; only the TitleScreen entry path was missing it.

**Fix:** Added else branch: shows `"Failed to load save file. The save file may be corrupted or from an older version of the game."` via the text panel.

---

### FALSE ALARMS (Issues flagged by analysis, verified to be non-bugs)

| Claim | Verified Reality |
|---|---|
| DocumentReconstructionGame null callback crash | `finish()` and `cancel()` already have `if (onComplete != null)` guards. No fix needed. |
| AwarenessMeter array out of bounds | `getColorIndex()` returns max 3; `COLORS[]` has 4 entries (0–3). No out-of-bounds possible. |
| handleAskTopic NPE on null suspect | `InterviewSystem.askTopic()` already null-checks internally. |
| ExaminationSystem mini-game replay | `incrementExamCount()` runs before the switch, so count==1 fires exactly once. Already gated. |
| ClimaxContent "5 vs 7 anomaly discrepancy" | Thresholds (1, 3, 5) select text variants, not claim anomaly counts. |
| InterviewSystem Margaret deflection NPE | `s != Suspect.MARGARET` guard prevents the null-return path. |
| Dangerous topic false positives | Actual topic key strings contain none of the flagged substrings. |
| SaveLoadSystem Enum.valueOf crash | All enum parsing is inside the outer `catch (Exception e)` block. Already caught. |

---

## March 10, 2026 — Bug Fix Session

**Session summary:** Deep multi-pass analysis of the full codebase. 22 candidate bugs identified across 4 parallel scan agents. After 4 rounds of parallel verification (direct code reads with line-number evidence), 5 confirmed bugs survived. All 5 were fixed. 17 were false positives.

---

### CONFIRMED BUGS FIXED

---

#### Bug 8 — GAME_OVER_AWARENESS ending never sets state or checks achievements

**Root cause:** `showGameOver()` in `GameScreen.java` displayed the game-over panel but never called `gameState.setChosenEndingByName("GAME_OVER_AWARENESS")` or `achievementSystem.checkOnEnding(...)`. The `GAME_OVER_AWARENESS` value existed in the `Ending` enum and was handled in `AchievementSystem.checkOnEnding()`, but no code path ever invoked it. The awareness-death ending was permanently blocked from recording its ending or awarding any achievements.

**Files changed:** `GameScreen.java`

**Fix applied:**
```java
// In showGameOver(), after setting panelMode = PanelMode.TEXT:
gameState.setChosenEndingByName("GAME_OVER_AWARENESS");
achievementSystem.checkOnEnding(GameState.Ending.GAME_OVER_AWARENESS);
```

---

#### Bug 14 — Silent auto-repair of tapes gives no feedback to player

**Root cause:** In `playTape()` in `GameScreen.java`, Gate 2 checks if a tape requires repair and auto-spends a repair solution — but the transcript shown to the player contained no notification. The player's repair solution count decremented invisibly, with no indication a resource was spent.

**Files changed:** `GameScreen.java`

**Fix applied:**
```java
boolean wasAutoRepaired = false;
// ... inside gate 2:
gameState.useRepairSolution(tape);
wasAutoRepaired = true;
// ... after transcript StringBuilder created:
if (wasAutoRepaired) {
    sb.insert(0, "[Tape repaired using a splicing solution. Repair solutions remaining: "
        + gameState.getRepairSolutionsRemaining() + "]\n\n");
}
```

---

#### Bug 15 — wrongAccusationCount tracked but never enforced

**Root cause:** `GameState` tracked `wrongAccusationCount` and incremented it on each wrong accusation, but `handleAccusation()` in `GameScreen.java` never checked it. A player could make unlimited wrong accusations with no consequence, making the stat entirely inert.

**Files changed:** `GameScreen.java`

**Fix applied:**
```java
// At the top of handleAccusation():
if (gameState.getWrongAccusationCount() >= 3) {
    textPanel.show("ACCUSATION BLOCKED\n\nAfter " + gameState.getWrongAccusationCount()
        + " wrong accusations, the suspects have closed ranks. They refuse to entertain further accusations without overwhelming proof.");
    panelMode = PanelMode.TEXT;
    return;
}
```

---

#### Bug 18 — Narrator distortions during interviews skip contradiction discovery

**Root cause:** `NarratorSystem.filterText()` calls `checkDistortionContradictions()` after injecting a distortion. But during interviews, `GameScreen` calls `narratorSystem.maybeGetDistortion()` directly — bypassing `filterText()` entirely. As a result, all 4 mapped distortions (`NARRATOR_WEAPON`, `NARRATOR_BOOTS`, `NARRATOR_LETTER`, `NARRATOR_TIME`) could fire during interviews without ever triggering contradiction discovery, even if the player already held the contradicting evidence.

**Files changed:** `NarratorSystem.java`, `NarratorText.java`, `GameScreen.java`

**Fix applied:**

`NarratorSystem.java` — changed `checkDistortionContradictions` from `private` to `public`:
```java
public void checkDistortionContradictions(String distortion) {
```

`NarratorText.java` — added explanatory comment above the 2 unmapped distortions:
```java
// Atmospheric red herrings: no evidence directly contradicts these, so they have
// no entry in DISTORTION_CONTRADICTIONS. They misdirect without triggering discovery.
"Margaret was seen near the study that night. Several witnesses confirm it.",
"The cellar has always been sealed. There's nothing down there but wine and dust."
```

`GameScreen.java` — added contradiction check after interview distortion fires:
```java
narratorSystem.checkDistortionContradictions(distortion);
```

---

#### Bug 20 — DocumentReconstructionGame allows dragging pieces off-screen

**Root cause:** `handleTouchDragged()` in `DocumentReconstructionGame.java` set piece position directly from unprojected touch coordinates with no bounds checking. A player could drag any fragment entirely off-screen, making it impossible to retrieve or place — softlocking the mini-game without a visible cancel button.

**Files changed:** `DocumentReconstructionGame.java`

**Fix applied:**
```java
public void handleTouchDragged(float x, float y) {
    if (!active || completed || draggedPiece == null) return;
    float clampedX = Math.max(0, Math.min(DSAGame.SCREEN_WIDTH - PIECE_WIDTH, x - dragOffsetX));
    float clampedY = Math.max(0, Math.min(DSAGame.SCREEN_HEIGHT - PIECE_HEIGHT, y - dragOffsetY));
    draggedPiece.setPosition(clampedX, clampedY);
}
```

---

### FALSE POSITIVES — March 10, 2026 Analysis

The following 17 issues were raised during analysis but verified to be non-bugs through direct code inspection.

| Candidate Bug | Why It Is Not a Bug |
|---|---|
| AchievementSystem CYCLE_BREAKER unreachable | `checkOnEnding()` is called by all moral ending paths. CYCLE_BREAKER fires correctly for endings that reach it. The bug was specific to `showGameOver()` only (Bug 8). |
| AchievementSystem double-check on win vs ending | `checkOnWin()` and `checkOnEnding()` cover different achievement sets intentionally; no achievements are double-awarded. |
| SaveLoadSystem wrongAccusationCount not saved | It is saved in `SaveData` — `wrongAccusationCount` field is serialized and deserialized. |
| NarratorSystem usedWarnings key collision | Keys are namespaced `mood.ordinal() * 100 + index`; with 4 moods and max 4 entries each, max key is 304, no collisions possible. |
| InterviewSystem channeling bleed-through always fires | `maybeGetChannelingBleedThrough()` returns null 4/5 times; the 1/5 path is intentional design. |
| GameScreen awareness cap not enforced | `AwarenessSystem.addAwareness()` already clamps to 100 internally before GameState is updated. |
| ExaminationSystem `examCount` reset on re-examine | `examCount` is per-evidence and stored in `GameState`; it is never reset except via load. Mini-game only fires on count == 1. |
| HintSystem free hint on every call | `HintSystem` checks `state.getHintCount()` and deducts a hint token; free hints only fire when `freeHintsRemaining > 0`. |
| EvidenceSystem duplicate evidence add | `GameState.addEvidence()` uses a `Set<Evidence>`; duplicates are silently ignored by set semantics. |
| AchievementSystem `checkOnWin()` called before ending set | `checkOnWin()` checks evidence/contradiction/anomaly counts, not the ending value. Order is correct. |
| ClimaxContent anomaly threshold off-by-one | Thresholds select narrative variant text, not anomaly validity. No functional error. |
| InterviewSystem suspect answer map missing keys | All topic keys used in `GameScreen` match keys present in each suspect's answer map; no missing-key NPE path. |
| NarratorSystem mood boundary at exactly 20/40/60 | `getMoodForAwareness()` uses strict `<` so boundary values fall into the higher bracket as designed. |
| GameScreen `panelMode` not reset after examination | `panelMode` is explicitly set to `PanelMode.TEXT` after every examination result display. |
| SaveLoadSystem tape repair count not persisted | `repairSolutionsRemaining` is a field in `SaveData` and round-trips correctly through JSON. |
| DocumentReconstructionGame null callback crash | `finish()` and `cancel()` both guard with `if (onComplete != null)` / `if (onCancel != null)`. |
| GameState awareness overflow above 100 | `AwarenessSystem` clamps; `GameState.setAwareness()` also clamps to `[0, 100]`. Double-protected. |
| SaveLoadSystem clearAllState before null check | **REAL BUG — FIXED (March 16).** `state.clearAllState()` ran before null-checking the deserialized `SaveData`. If `json.fromJson()` returned null (malformed save), the player's live game state was wiped and load returned null, silently destroying mid-run progress. Fix: added `if (data == null) return null;` between deserialization and `clearAllState()` in `SaveLoadSystem.java:159`. |
| pendingMiniGame not cleared on ESC | **REAL BUG — FIXED (March 16).** Pressing ESC to dismiss the torn letter mini-game intro text called `textPanel.hide()` and reset `panelMode` but never cleared `pendingMiniGame`. It stayed set, causing the mini-game to launch unexpectedly the next time any panel was closed normally via the close button. Fix: added `pendingMiniGame = null` in the ESC handler alongside the existing `textPanel.hide()` in `GameScreen.java:289`. |

