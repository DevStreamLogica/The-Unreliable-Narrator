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

  You are a private investigator hired by Margaret Vance to investigate the death of her father, Harold Vance. The official police     
  investigation has stalled. You arrive at Vance Manor on November 17th, 1987 — two days after the murder. You're not alone — a        
  mysterious narrator guides you through hidden tape recordings left by Arthur Hollis, a previous investigator who arrived before you  
  and disappeared without a trace.

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

  Available from the start: All 10 rooms (though Cellar requires 4 watched tapes + 5 evidence; Margaret's Room requires the
  BREATHING_WALL anomaly)

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
  │ Ask a topic in interview                          │ +1      │
  ├───────────────────────────────────────────────────┼─────────┤
  │ Ask a dangerous topic (cellar/arthur/entity/wall) │ +5      │
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
