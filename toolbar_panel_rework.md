# Toolbar Panel Rework

**Date:** 2026-03-07

## Files Changed

- `core/src/main/java/com/dsa/game/screens/GameScreen.java`
- `core/src/main/java/com/dsa/game/systems/EvidenceSystem.java`

---

## HINT (`showHint`)

**Problem:** Hint text was passed through `narratorSystem.filterText()`, distorting it into unreliable narrator voice instead of a direct player tip.

**Fix:** Removed the narrator filter. Hint is now rendered as-is.

```java
// Before
String hint = narratorSystem.filterText(hintSystem.getHint());
textPanel.show("=== HINT ===\n\n" + hint);

// After
textPanel.show("=== HINT ===\n\n" + hintSystem.getHint());
```

---

## ACCUSE (`showAccusation`)

**Problem:** Locked state showed a generic "not enough evidence" message with no numbers. Unlocked state showed suspects with no evidence context.

**Fix (locked state):** Now shows current evidence counts and thresholds.

```
Not enough evidence yet.

James: X/6 (need 3)  |  Daniel: X/4 (need 2)

Keep investigating — examine objects, collect tapes, interview suspects.
```

**Fix (unlocked state):** Header now includes evidence summary before the suspect list.

```
=== WHO KILLED HAROLD VANCE? ===

Evidence against James: X/6  |  Evidence against Daniel: X/4

Select your accusation carefully.
```

---

## SUSPECTS (`showSuspectList`)

**Problem:** Buttons showed only the suspect's name — no indication of cooperation or prior contact.

**Fix:** Each button label now includes the suspect's cooperation percentage. Button width increased from 200 to 260 to accommodate the longer label.

```
James Vance  [45%]
Margaret Vance  [70%]
...
```

> Note: No per-suspect interview count exists in `GameState` (only a global `interviewCount`). Cooperation % alone was used as the fallback per the plan.

---

## NOTEBOOK (`showNotebook`)

**Problem:** All sections concatenated at equal priority — hard to scan, evidence gap buried below contradictions.

**Fix:** Sections reordered by relevance. Most actionable info first, meta/narrative info last.

New order:
1. **Status** — Awareness, evidence count, tape count on one compact line
2. **Active Investigation** — James X/6, Daniel X/4, accusation-ready status
3. **Suspect Cooperation** — All 5 suspects with %
4. **Physical Contradictions** — (conditional)
5. **Narrator Contradictions** — (conditional)
6. **Entity Anomalies** — (conditional)
7. **Narrator Inconsistencies** — Moved to bottom (meta/low priority)
8. **Achievements**

Removed trailing `\n\n` before achievements.

---

## INVENTORY (`showInventory` + `EvidenceSystem.getInventoryText`)

**Problem:** Tapes showed `[NEW - PLAY]` tag which was unclear. PLAY buttons were visually disconnected from the tape entries. Button width was too narrow for longer tape titles.

**Fix (`EvidenceSystem.java`):** Changed tag from `[NEW - PLAY]` to `[PLAY AVAILABLE]` for unwatched, unlocked, undamaged tapes.

**Fix (`GameScreen.java`):** PLAY button width increased from 200 to 220.
