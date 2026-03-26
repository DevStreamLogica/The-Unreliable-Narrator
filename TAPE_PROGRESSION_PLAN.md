# Tape Progression System — Option 2 Implementation Plan

## Context

The game currently has no tape progression — all tapes are free to find and watch in any order. Option 2 (sequential code unlock) was designed in February16.MD but never implemented. Two problems were identified:

1. **Repair solution math was broken** — 4 needed, 3 available (Daniel's tape was inaccessible)
2. **The two gate systems** (room locks + tape locks) needed clear narrator bridging

This plan implements Option 2 with both problems fixed.

---

## How It Works (Player Experience)

```
Tape 1 (free)          → watch → reveals HEIR-CHANGE  → Tape 4 (Margaret Interview) AUTO-UNLOCKS
Tape 4 (Marg. Int.)    → watch → reveals GUEST-721    → Tape 5 (Marcus) AUTO-UNLOCKS
Tape 5 (Marcus)        → watch → reveals WINDOW-11    → Tape 6 (Charles) AUTO-UNLOCKS
Tape 6 (Charles)       → watch → reveals LOG-1115     → Tapes 2 (James) + 3 (Daniel) AUTO-UNLOCK
                                                          [Tape 3 needs repair]
Tape 2 (James)         → watch → checks if Tape 3 also done → Tape 7 AUTO-UNLOCKS [needs repair]
Tape 3 (Daniel)        → repair + watch → checks if Tape 2 done → Tape 7 AUTO-UNLOCKS [needs repair]

WILL_COPY evidence     → also unlocks Tape 2 as a parallel trigger (either path works)

Tape 7 (Marg. Account) → repair + watch → reveals CELLAR-WARNING → Tape 8 gate met
Tape 8                 → needs: hasTapeRepairKit + CELLAR-WARNING learned + 3 contradictions
```

**Why each unlock is justified by tape content:**
- Tape 1 → Tape 4: Harold names Margaret as heir in the recording — naturally investigate her
- Tape 4 → Tape 5: Margaret names Marcus as a suspect ("he was there that night")
- Tape 5 → Tape 6: Marcus explicitly says "I saw a light on in [Charles's] window as I drove away"
- Tape 6 → Tape 2 + 3: Charles places James at the study at 10:45 PM AND suspects Daniel as accomplice
- Tapes 2 + 3 → Tape 7: Having heard all suspects, Margaret's private account (made FOR Arthur) speaks for itself
- Tape 7 → Tape 8: Margaret records "CELLAR-WARNING" — the final gate condition

**Repair:** auto-applied when player tries to play a damaged tape and has solutions remaining.

---

## Fixes Applied to Original Design

### Fix 1 — Repair solution count (was broken: 4 needed, 3 available)

New `repairSolutionsRemaining` system covers only **Tapes 3 and 7** (1 each = 2 needed).
Tape 8 keeps its existing `hasTapeRepairKit` mechanic (unchanged).
4 solutions are placed in the world — always enough.

| # | Location | Object | Exam State |
|---|----------|--------|------------|
| 1 | Servants' Quarters | `floorboard` | 1st exam |
| 2 | Kitchen | `flour_tin` | 1st exam |
| 3 | Study | `ashes` | 2nd exam (after torn letter mini-game) |
| 4 | Cellar | `flour_sacks` | 2nd exam (after bloodstained cuff) |

### Fix 2 — Narrator bridging

When a tape is locked and player tries to play it → narrator message explaining exactly what's needed.
When WILL_COPY is found → narrator explicitly announces Tape 2 unlock.

### Fix 3 — Tape 8 extra gates

Replace the "2 repair solutions" Tape 8 requirement with:
- Must have learned `"CELLAR-WARNING"` code (i.e., watched Tape 7)
- 3 contradictions discovered
- Repair kit stays as-is (hasTapeRepairKit)

---

## Files to Modify

### 1. `core/src/main/java/com/dsa/game/state/GameState.java`

**Add new fields:**
```java
private final Set<Tape> unlockedTapes = new LinkedHashSet<>();
private final Set<String> learnedCodes = new LinkedHashSet<>();
private final Set<Tape> repairedTapes = new LinkedHashSet<>();
private int repairSolutionsRemaining = 0;
```

**Add new methods:**
```java
public boolean unlockTape(Tape t) { return unlockedTapes.add(t); }
public boolean isUnlockedTape(Tape t) { return t == Tape.TAPE_ARGUMENT || unlockedTapes.contains(t); }
public void learnCode(String code) { learnedCodes.add(code); }
public boolean hasLearnedCode(String code) { return learnedCodes.contains(code); }
public void addRepairSolution() { repairSolutionsRemaining++; }
public boolean useRepairSolution(Tape t) {
    if (repairSolutionsRemaining <= 0) return false;
    repairSolutionsRemaining--;
    repairedTapes.add(t);
    return true;
}
public boolean isTapeRepaired(Tape t) { return repairedTapes.contains(t); }
public int getRepairSolutionsRemaining() { return repairSolutionsRemaining; }
// Force-setters for SaveLoadSystem:
public void forceUnlockTape(Tape t) { unlockedTapes.add(t); }
public void forceLearnCode(String c) { learnedCodes.add(c); }
public void forceRepairTape(Tape t) { repairedTapes.add(t); }
public void setRepairSolutionsRemaining(int n) { repairSolutionsRemaining = n; }
public Set<Tape> getUnlockedTapes() { return Collections.unmodifiableSet(unlockedTapes); }
public Set<String> getLearnedCodes() { return Collections.unmodifiableSet(learnedCodes); }
public Set<Tape> getRepairedTapes() { return Collections.unmodifiableSet(repairedTapes); }
```

---

### 2. `core/src/main/java/com/dsa/game/systems/SaveLoadSystem.java`

In `SaveData` inner class, add:
```java
public List<String> unlockedTapes = new ArrayList<>();
public List<String> learnedCodes = new ArrayList<>();
public List<String> repairedTapes = new ArrayList<>();
public int repairSolutionsRemaining = 0;
```

In `save()`, serialize following existing enum→String pattern.
In `load()`, deserialize with null checks following existing pattern.

---

### 3. `core/src/main/java/com/dsa/game/systems/ExamResult.java`

**Add field:**
```java
private final boolean grantsRepairSolution;
```

**Add factory method:**
```java
public static ExamResult withRepairSolution(String text) {
    return new ExamResult(text, null, null, MiniGameType.NONE, true);
}
public boolean grantsRepairSolution() { return grantsRepairSolution; }
```

Update all existing constructors to pass `false` for grantsRepairSolution.

---

### 4. `core/src/main/java/com/dsa/game/systems/ExaminationSystem.java`

**4a. examineServants() — floorboard (change existing empty result):**
```java
case "floorboard":
    if (count == 1) return ExamResult.withRepairSolution(
        "A loose floorboard near the wall. Prying it up: a small canvas pouch — Arthur Hollis's investigation kit. Inside: tape splicing scissors, adhesive strips, and a manual splicer. He prepared for everything.\n\n[Tape repair solution acquired.]");
    return new ExamResult("The floorboard. Arthur's investigation kit is already in your possession.");
```

**4b. examineKitchen() — flour_tin (new case):**
```java
case "flour_tin":
    if (count == 1) return ExamResult.withRepairSolution(
        "A battered flour tin on the kitchen shelf. Lifting the lid: beneath a false bottom, wrapped in cloth, more splicing equipment. Arthur hid his tools throughout the manor.\n\n[Tape repair solution acquired.]");
    return new ExamResult("The flour tin. You already retrieved the tape repair equipment from inside.");
```

**4c. examineStudy() — ashes (add 2nd exam state after mini-game):**
```java
if (count == 2) return ExamResult.withRepairSolution(
    "Sifting deeper through the ashes beneath where the letter fragments were, your fingers close on something solid — a small roll of splicing tape and a metal splicer, heat-blackened but intact. Arthur's tools, hidden in the one place no one would search twice.\n\n[Tape repair solution acquired.]");
return new ExamResult("The fireplace ashes. You've found everything hidden here.");
```

**4d. examineCellar() — flour_sacks (add 2nd exam state):**
```java
// count==1 already gives BLOODSTAINED_CUFF
if (count == 2) return ExamResult.withRepairSolution(
    "Moving the flour sacks fully aside, behind where the shirt was hidden: a small leather roll of tape splicing tools. Arthur made it this far into the cellar. He prepared for what he might find.\n\n[Tape repair solution acquired.]");
return new ExamResult("The flour sacks. You've searched behind them thoroughly.");
```

---

### 5. `core/src/main/java/com/dsa/game/screens/GameScreen.java`

**5a. handleExamine() — process repair solution after existing evidence block:**
```java
if (result.grantsRepairSolution()) {
    gameState.addRepairSolution();
    display.append("\n\n[TAPE REPAIR SOLUTION ACQUIRED: ")
           .append(gameState.getRepairSolutionsRemaining())
           .append(" total]");
}
```

Also after evidence collection:
```java
if (result.hasEvidence()) {
    // ...existing code...
    checkEvidenceForCode(result.getEvidence()); // NEW
}
```

**5b. New helper methods:**

```java
private boolean isTapeUnlocked(Tape tape) {
    return gameState.isUnlockedTape(tape);
}

private boolean isDamagedTape(Tape tape) {
    return tape == Tape.TAPE_DANIEL_INTERVIEW || tape == Tape.TAPE_MARGARET_ACCOUNT;
}

private void checkEvidenceForCode(Evidence evidence) {
    if (evidence == Evidence.WILL_COPY) {
        gameState.learnCode("ESTATE-42");
        boolean unlocked = gameState.unlockTape(Tape.TAPE_JAMES_INTERVIEW);
        if (unlocked) {
            // Show narrator announcement: "Hidden among the documents, a code..."
        }
    }
}

private void revealCodeFromTape(Tape tape) {
    switch (tape) {
        case TAPE_ARGUMENT:
            // Harold names Margaret as heir → investigate her
            gameState.learnCode("HEIR-CHANGE");
            gameState.unlockTape(Tape.TAPE_MARGARET_INTERVIEW);
            break;
        case TAPE_MARGARET_INTERVIEW:
            // Margaret names Marcus as suspect ("he was there that night")
            gameState.learnCode("GUEST-721");
            gameState.unlockTape(Tape.TAPE_MARCUS_INTERVIEW);
            break;
        case TAPE_MARCUS_INTERVIEW:
            // Marcus explicitly mentions Charles's light on at 11 PM
            gameState.learnCode("WINDOW-11");
            gameState.unlockTape(Tape.TAPE_CHARLES_INTERVIEW);
            break;
        case TAPE_CHARLES_INTERVIEW:
            // Charles places James at study 10:45 PM AND suspects Daniel
            gameState.learnCode("LOG-1115");
            gameState.unlockTape(Tape.TAPE_JAMES_INTERVIEW);
            gameState.unlockTape(Tape.TAPE_DANIEL_INTERVIEW);
            break;
        case TAPE_JAMES_INTERVIEW:
            // Tape 7 unlocks only after BOTH James AND Daniel are watched
            if (gameState.hasWatchedTape(Tape.TAPE_DANIEL_INTERVIEW)) {
                gameState.unlockTape(Tape.TAPE_MARGARET_ACCOUNT);
            }
            break;
        case TAPE_DANIEL_INTERVIEW:
            // Tape 7 unlocks only after BOTH James AND Daniel are watched
            if (gameState.hasWatchedTape(Tape.TAPE_JAMES_INTERVIEW)) {
                gameState.unlockTape(Tape.TAPE_MARGARET_ACCOUNT);
            }
            break;
        case TAPE_MARGARET_ACCOUNT:
            gameState.learnCode("CELLAR-WARNING");
            break;
        default: break;
    }
}
```

**5c. playTape() — add gates at top (before existing logic):**
```java
private void playTape(Tape tape) {
    // Gate 1: Tape code-locked
    if (!isTapeUnlocked(tape)) {
        showLockedTapeMessage(tape);
        return;
    }

    // Gate 2: Tape physically damaged (Tapes 3 and 7 only)
    if (isDamagedTape(tape) && !gameState.isTapeRepaired(tape)) {
        if (gameState.getRepairSolutionsRemaining() > 0) {
            gameState.useRepairSolution(tape);
            // Continue playing — show repair message prepended to transcript
        } else {
            textPanel.show("DAMAGED TAPE\n\nThis tape needs splicing equipment to repair, but you have none.\n\n[Find tape repair solutions in the manor.]");
            panelMode = PanelMode.TEXT;
            return;
        }
    }

    // Gate 3: Tape 8 — repair kit check (unchanged)
    if (tape == Tape.TAPE_ARTHUR_DEATH && !gameState.hasTapeRepairKit()) {
        // existing message
        return;
    }
    // Gate 4: Tape 8 — must have watched Tape 7
    if (tape == Tape.TAPE_ARTHUR_DEATH && !gameState.hasLearnedCode("CELLAR-WARNING")) {
        textPanel.show("DAMAGED TAPE — SEQUENCE INCOMPLETE\n\nThe tape is repaired, but something holds you back. There is another recording you must hear first.\n\n[Find and watch Margaret's personal account.]");
        panelMode = PanelMode.TEXT;
        return;
    }
    // Gate 5: Tape 8 — 3 contradictions required
    if (tape == Tape.TAPE_ARTHUR_DEATH && gameState.getDiscoveredContradictions().size() < 3) {
        int remaining = 3 - gameState.getDiscoveredContradictions().size();
        textPanel.show("DAMAGED TAPE\n\nThe tape is repaired. But " + remaining + " contradiction" + (remaining > 1 ? "s remain" : " remains") + " unresolved. The full truth requires more investigation.\n\n[Present evidence during interviews to uncover contradictions.]");
        panelMode = PanelMode.TEXT;
        return;
    }

    // ...rest of existing playTape() logic unchanged...

    // At end, after evidenceSystem.watchTape(tape):
    revealCodeFromTape(tape); // NEW
}
```

**5d. showLockedTapeMessage(Tape tape):**
```java
private void showLockedTapeMessage(Tape tape) {
    String hint;
    switch (tape) {
        case TAPE_MARGARET_INTERVIEW:
            hint = "Harold's own recording names his heir. That name is the key.";
            break;
        case TAPE_MARCUS_INTERVIEW:
            hint = "Margaret Vance's interview names another guest who was at the manor that night.";
            break;
        case TAPE_CHARLES_INTERVIEW:
            hint = "Marcus Blackwood noticed something as he left. Listen to his interview.";
            break;
        case TAPE_JAMES_INTERVIEW:
            hint = "Charles Webb's interview places a suspect at the study door. His account unlocks this.";
            break;
        case TAPE_DANIEL_INTERVIEW:
            hint = "Charles Webb named two suspects. His interview unlocks both.";
            break;
        case TAPE_MARGARET_ACCOUNT:
            hint = "Hear what both James and Daniel have to say first. Then Margaret's full account will speak for itself.";
            break;
        default:
            hint = "Keep investigating.";
    }
    textPanel.show("LOCKED TAPE: " + tape.getTitle() + "\n\nThe tape recorder is sealed in a protective case. Arthur locked these before he disappeared.\n\n" + hint);
    panelMode = PanelMode.TEXT;
}
```

**5e. showInventory() — hide PLAY button for locked tapes:**
```java
for (Tape t : gameState.getCollectedTapes()) {
    if (!gameState.hasWatchedTape(t) && isTapeUnlocked(t)) {
        buttons.add(new TextButton("PLAY: " + t.getTitle(), 0, 0, 200, 35, "play_tape_" + t.name()));
    }
    // Locked tapes: no PLAY button — status shown in inventory text
}
```

---

### 6. `core/src/main/java/com/dsa/game/systems/EvidenceSystem.java`

Update `getInventoryText()` tape section:
```java
for (Tape t : state.getCollectedTapes()) {
    boolean watched = state.hasWatchedTape(t);
    boolean unlocked = state.isUnlockedTape(t);
    boolean damaged = (t == Tape.TAPE_DANIEL_INTERVIEW || t == Tape.TAPE_MARGARET_ACCOUNT);
    sb.append("* ").append(t.getTitle());
    if (watched) sb.append(" [WATCHED]");
    else if (!unlocked) sb.append(" [LOCKED]");
    else if (damaged && !state.isTapeRepaired(t)) {
        sb.append(" [DAMAGED — repair solutions: ").append(state.getRepairSolutionsRemaining()).append("]");
    } else sb.append(" [NEW - PLAY]");
    sb.append("\n");
}
```

---

### 7. `core/src/main/java/com/dsa/game/systems/HintSystem.java`

In `getWatchTapeHint()`, add unlock chain hints before suggesting a tape to watch:
```java
// If all unwatched collected tapes are locked, hint about unlock chain
if (!state.isUnlockedTape(Tape.TAPE_MARCUS_INTERVIEW)) {
    return "Harold's own tape holds the first key. Start there.";
}
if (!state.hasLearnedCode("ESTATE-42")) {
    return "Find Harold's will — it contains a combination you need.";
}
// etc.
```

---

## Verification

1. Run `./gradlew lwjgl3:run`
2. **Tape 1 plays immediately** — no lock
3. **After watching Tape 1**, Tape 4 (Margaret Interview) unlocks — not Marcus
4. **After watching Tape 4**, Tape 5 (Marcus) unlocks
5. **After watching Tape 5**, Tape 6 (Charles) unlocks
6. **After watching Tape 6**, both Tape 2 (James) and Tape 3 (Daniel) unlock simultaneously
7. **Tape 3** shows as [DAMAGED] — auto-repairs when played, consuming 1 solution
8. **Finding WILL_COPY** also unlocks Tape 2 as a parallel trigger
9. **Tape 7** (Margaret Account) only unlocks after BOTH Tape 2 AND Tape 3 are watched
10. **Tape 7** shows as [DAMAGED] — auto-repairs when played
11. **Tape 8** requires repair kit from Margaret's Room dresser + CELLAR-WARNING + 3 contradictions
12. **Save/load** preserves unlock state — reload game, tape statuses persist
13. **Inventory** shows [LOCKED] for locked tapes, no PLAY button until unlocked
14. **Hint system** guides toward correct next tape at each stage
