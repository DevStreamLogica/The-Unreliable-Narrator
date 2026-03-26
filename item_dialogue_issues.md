# Item / Dialogue Issues Log

Last updated: 2026-03-18

---

## CONFIRMED BUGS (all fixed — see FIXED section below)

### 1. flour_sacks (Cellar, count 1) — Fireplace explanation contradicts established evidence
**File:** `ExaminationSystem.java` — `examineCellar()`, `flour_sacks` count 1
**Problem:** Text says "The study fireplace was nearly dead by midnight -- James must have panicked."
But the study fireplace examination (count 1) explicitly says "the ashes look fresh. Something was burned here recently -- not just firewood." James DID burn something in the fireplace that night. The two statements directly contradict each other.
**Fix needed:** Change the shirt-hiding explanation to a reason that doesn't rely on the fireplace being dead.

---

### 2. NarratorSystem — NARRATOR_WEAPON contradiction check uses wrong evidence
**File:** `NarratorSystem.java` — `checkDistortionContradictions()`, line 170
**Problem:** When the narrator distorts "letter opener was clearly the murder weapon," the auto-discovery check looks for `Evidence.LETTER_OPENER`. Finding the letter opener does NOT contradict this claim — it confirms it. The contradicting evidence should be `Evidence.FIREPLACE_POKER` (the actual murder weapon).
**Current:** `hasContradictingEvidence = state.hasEvidence(Evidence.LETTER_OPENER);`
**Fix needed:** Change to `Evidence.FIREPLACE_POKER`

---

### 3. NarratorSystem — NARRATOR_TIME contradiction check uses unrelated evidence
**File:** `NarratorSystem.java` — `checkDistortionContradictions()`, line 179
**Problem:** When the narrator distorts "Harold died after midnight," the auto-discovery check looks for `Evidence.SLEEPING_POWDER`. Sleeping powder has nothing to do with establishing time of death. The actual contradiction is established by witness testimony (Charles saw James at 10:45 PM, Marcus heard the argument and left at 11 PM).
**Current:** `hasContradictingEvidence = state.hasEvidence(Evidence.SLEEPING_POWDER);`
**Fix needed:** Determine correct evidence or tape anchor for time of death and update.

---

### 4. Study ashes (count 2) — "Arthur's tools" before Arthur is introduced
**File:** `ExaminationSystem.java` — `examineStudy()`, `ashes` count 2
**Problem:** Text says "Arthur's tools, hidden in the one place no one would search twice." Arthur Hollis has not been introduced at this point if the player examines study ashes before reaching the servants' quarters (where A.H. initials and Arthur's name first appear). The name "Arthur" has no context for the player yet.
**Fix needed:** Remove "Arthur's" — replace with "Someone's tools" or "Investigation tools, hidden in the one place no one would search twice."

---

### 5. Kitchen flour_tin (count 1) — Same premature Arthur attribution
**File:** `ExaminationSystem.java` — `examineKitchen()`, `flour_tin` count 1
**Problem:** Text says "Arthur hid his tools throughout the manor." Same issue as #4 — Arthur Hollis hasn't been introduced yet if the player reaches the kitchen before the servants' quarters. Also, "throughout the manor" is a conclusion the detective couldn't draw from finding one tin.
**Fix needed:** Replace with something like "Someone hid investigation tools throughout the manor." or just remove the attribution entirely.

---

### 6. EvidenceSystem — FIREPLACE_POKER missing from James evidence count
**File:** `EvidenceSystem.java` — `getJamesEvidenceCount()`, lines 25–34
**Problem:** The actual murder weapon (`Evidence.FIREPLACE_POKER`) is not counted in James's evidence total. The 6 counted items are all motive/opportunity (financial records, will, torn letter, blackmail note, two tapes). A player can accuse James without ever finding the murder weapon.
**Fix needed:** Add `if (state.hasEvidence(Evidence.FIREPLACE_POKER)) count++;` to `getJamesEvidenceCount()` and update the "/6" display to "/7".

---

### 7. EvidenceSystem — Wrong tapes flagged as damaged in inventory
**File:** `EvidenceSystem.java` — `getInventoryText()`, line 70
**Current code:** `boolean damaged = (t == Tape.TAPE_DANIEL_INTERVIEW || t == Tape.TAPE_MARGARET_ACCOUNT);`
**Problem:** The wrong tapes are marked damaged.
- `TAPE_DANIEL_INTERVIEW` — examination text says "A tape recorder is wedged between the pages." No mention of damage.
- `TAPE_MARGARET_ACCOUNT` — examination text says "Margaret clearly prepared these recordings carefully." Also not damaged. The repair kit found alongside it is for other tapes.
- `TAPE_ARTHUR_DEATH` — examination text explicitly says "The casing is cracked, the tape ribbon inside snapped and tangled. You'll need tape splicing equipment to repair it" — but this tape is NOT in the damaged list.
**Fix needed:** Change to `boolean damaged = (t == Tape.TAPE_ARTHUR_DEATH);`

---

### 8. GameScreen — Correct accusation never sets ACCUSATION_CORRECT ending
**File:** `GameScreen.java` — `handleAccusation()`, correct path (~line 1366)
**Problem:** When the player correctly accuses James & Daniel, `setGameWon(true)` is called but `setChosenEnding(Ending.ACCUSATION_CORRECT)` is never called. The ending state stays `NONE`. This breaks the CYCLE_BREAKER achievement (which requires `ending != NONE`) for players who solve via accusation.
**Fix needed:** Add `gameState.setChosenEnding(GameState.Ending.ACCUSATION_CORRECT);` after `setGameWon(true)`.

---

### 9. GameScreen — Wrong accusation never sets ACCUSATION_WRONG ending
**File:** `GameScreen.java` — `handleAccusation()`, wrong accusation path (~line 1432)
**Problem:** Wrong accusations increment the counter and add awareness but never call `setChosenEnding(Ending.ACCUSATION_WRONG)`. The enum value is dead code.
**Fix needed:** Add `gameState.setChosenEnding(GameState.Ending.ACCUSATION_WRONG);` in the wrong accusation handler.

---

### 10. GameScreen — Leave manor never sets ending state
**File:** `GameScreen.java` — `handleLeaveManor()` (~line 1508–1546)
**Problem:** Leaving the manor calls `setGameOver(true)` but never sets a `chosenEnding`. Ending stays `NONE`, which again blocks CYCLE_BREAKER for anomaly-focused players who leave without accusing.
**Fix needed:** Assign an appropriate ending (e.g. `ESCAPE_MANOR`) when the player leaves through the manor exit without choosing a moral ending path.

---

### 11. SuspectDialogue — James's two false alibi responses contradict each other
**File:** `SuspectDialogue.java` — `FALSE_RESPONSES`, keys `JAMES_last_night` and `JAMES_whereabouts`
**Problem:** Both responses can appear in the same playthrough (whenever coop < 25):
- `last_night`: *"I was reading in my room. I fell asleep around nine... Margaret can confirm -- she brought me tea."* (alone in room)
- `whereabouts`: *"I was with Margaret all evening. We were in the parlor playing cards until midnight."* (with Margaret all evening)
A player who asks both topics will see James give two mutually exclusive locations for himself. Depending on intent, this could be design (catching him in contradictions) or a real continuity problem.
**Fix needed:** Align the two false alibis so they tell the same false story, OR intentionally make the contradiction detectable as a gameplay mechanic.

---

### 12. InterviewSystem — BODY_POSITION contradiction arbitrarily disables Margaret deflections
**File:** `InterviewSystem.java` — cooperation routing, deflection condition
**Problem:** Deflections are blocked with `!state.hasContradiction(Contradiction.BODY_POSITION)`. Once the player discovers the body was moved, ALL suspects stop deflecting to Margaret entirely. The BODY_POSITION contradiction has nothing to do with suspects blaming Margaret — this gate silently disables an entire dialogue mechanic at a point mid-game when the player is getting closer to the truth.
**Fix needed:** Remove the `BODY_POSITION` gate from the deflection condition, or replace it with a more relevant check.

---

### 13. HintSystem — Third-tier accusation hint fully spoils the solution
**File:** `HintSystem.java` — `getAccusationHint()`, tier 2 (third request)
**Problem:** The hint explicitly states: "Click ACCUSE and select 'James & Daniel (together)'. They conspired to kill Harold before he could change the will." This gives the player the exact answer — suspect names and motive — with no deduction required.
**Fix needed:** Replace with directional guidance that doesn't name the killers, e.g. "You need evidence of who committed the murder AND who helped cover it up. Check the cellar and the groundskeeper's shed."

---

### 15. Evidence.java — BLACKMAIL_NOTE description names James before player could know
**File:** `Evidence.java` — `BLACKMAIL_NOTE` description
**Current:** `"...James planted this to frame Margaret and make her look suspicious. It was never a real blackmail note."`
**Problem:** The inventory description directly names James as the person who planted the note. The examination text only says "someone" planted it. A player can collect the note early and immediately read their inventory, seeing James's name before gathering enough evidence to deduce he's the killer. Also introduces "mimicking Daniel's style" — the examination text describes "rough, uneducated handwriting" but never links it to Daniel's handwriting style.
**Fix needed:** Replace "James planted this" with "Someone planted this" and remove the Daniel handwriting attribution.

---

### 16. Evidence.java — SLEEPING_POWDER description invents unestablished delivery method
**File:** `Evidence.java` — `SLEEPING_POWDER` description
**Current:** `"A vial of sleeping powder discovered in the kitchen. Someone was drugging Harold's evening tea."`
**Problem:** "Evening tea" is a specific delivery method not established by any examination text, tape, or suspect dialogue. The vial is found in the kitchen with no context about how it was administered. The tea delivery is an assumption the inventory description presents as fact.
**Fix needed:** Change to something like `"A vial of sleeping powder discovered in the kitchen. Someone was planning to drug Harold."` (removing the tea specificity).

---

### 17. CYCLE_BREAKER achievement — hidden ending requirement missing from description
**File:** `AchievementSystem.java` — `checkOnEnding()`
**Problem:** Achievement description says "Discover all 7 anomalies" but the unlock code also requires `ending != GameState.Ending.NONE` — meaning the player must both discover all anomalies AND choose an ending. The ending requirement is invisible to the player from the description alone.
**Fix needed:** Update achievement description to mention that an ending must also be chosen, e.g. "Discover all 7 anomalies and reach an ending."

---

## DESIGN NOTES (not bugs, but worth awareness)

### Daniel naming inconsistency
Win text (`GameScreen.java` line 1370): "James Vance and **Daniel the groundskeeper** are arrested" but `TapeContent.java` headers and Tape 3 title use "**Daniel Hobbs**." Suspect enum display name is "Daniel the Groundskeeper." Minor inconsistency between formal (Hobbs) and informal (the groundskeeper) usage across different contexts.

### Marcus vs Margaret — when Harold left the parlor
TAPE_MARCUS_INTERVIEW: "Harold and I were in the parlor until about half past eight -- then he excused himself to his study." TAPE_MARGARET_INTERVIEW: "Marcus stayed in the parlor with Father for another hour or so." One says Harold left at 8:30, the other implies they stayed together until ~9:30. This discrepancy doesn't affect the murder timeline but is a minor witness inconsistency. Could be intentional (unreliable witnesses) or an unintended continuity error.

### Margaret's shifting account — what she heard during the argument
TAPE_MARGARET_INTERVIEW (police): "Not the words. Just... anger." TAPE_MARGARET_ACCOUNT (private tape for detective): "I heard him say something about 'the will' and 'tomorrow.'" This is intentional design — she was guarded with police but forthcoming on the private tape left "For the detective." Noted here for awareness.

### Study room description — "still has warm ashes"
`RoomDescriptions.java` — study first visit says "A fireplace on the north wall still has warm ashes." Physically implausible if the investigation takes place 8+ hours after the murder. Low priority but worth aligning with examination text which focuses on freshness/evidence of burning rather than warmth.

### Dramatic prefixes + recap texts
FRANTIC mood prefixes ("NO NO NO -- ", "It all makes horrible sense now: ") can clash with count 2+ recap texts like "The desk. You've already found the letter opener." This is a structural side effect of randomly applying mood prefixes to all examination text including mundane recaps. Not a logic error — just occasionally awkward.

---

## FIXED (resolved)

- James's "bad shoulder" in `accuse_james` — removed unestablished physical disability — **FIXED 2026-03-18**
- NARRATOR_TIME "Marcus heard nothing unusual" — changed to "heard the argument, then silence" — **FIXED 2026-03-18**
- **#1** flour_sacks cellar count 1 — removed "fireplace nearly dead by midnight" contradiction with study fireplace fresh ashes — **FIXED 2026-03-18**
- **#2** NarratorSystem NARRATOR_WEAPON check — `LETTER_OPENER` → `FIREPLACE_POKER` (poker contradicts the claim, opener confirms it) — **FIXED 2026-03-18**
- **#3** NarratorSystem NARRATOR_TIME check — `SLEEPING_POWDER` → `TAPE_CHARLES_INTERVIEW` (Charles placed James at study at 10:45 PM) — **FIXED 2026-03-18**
- **#4** study ashes count 2 — "Arthur's tools" → "Investigation tools" (Arthur not yet introduced) — **FIXED 2026-03-18**
- **#5** kitchen flour_tin count 1 — "Arthur hid his tools" → "Someone hid investigation tools" — **FIXED 2026-03-18**
- **#6** EvidenceSystem getJamesEvidenceCount — added FIREPLACE_POKER; HintSystem updated /6 → /7 everywhere — **FIXED 2026-03-18**
- **#7** EvidenceSystem damaged tape flags — `TAPE_DANIEL_INTERVIEW || TAPE_MARGARET_ACCOUNT` → `TAPE_ARTHUR_DEATH` (only tape actually described as damaged) — **FIXED 2026-03-18**
- **#8** GameScreen correct accusation — added `setChosenEnding(ACCUSATION_CORRECT)` and `checkOnEnding()` call — **FIXED 2026-03-18**
- **#9** GameScreen wrong accusation — added `setChosenEnding(ACCUSATION_WRONG)` — **FIXED 2026-03-18**
- **#10** GameScreen handleLeaveManor — added `setChosenEnding(ESCAPE_MANOR)` and `checkOnEnding()` call — **FIXED 2026-03-18**
- **#11** SuspectDialogue JAMES_whereabouts false response — aligned with JAMES_last_night false response (room by 9, alone) — **FIXED 2026-03-18**
- **#12** InterviewSystem deflection — removed unrelated `BODY_POSITION` gate from Margaret deflection condition — **FIXED 2026-03-18**
- **#13** HintSystem accusation hint tier 2 — removed killer names/motive; replaced with directional guidance — **FIXED 2026-03-18**
- **#15** Evidence.java BLACKMAIL_NOTE — "James planted this" → "Someone planted this"; removed Daniel handwriting attribution — **FIXED 2026-03-18**
- **#16** Evidence.java SLEEPING_POWDER — removed unestablished "evening tea" delivery method — **FIXED 2026-03-18**
- **#17** Achievement.java CYCLE_BREAKER — description already correct ("choose an ending") — **NO CHANGE NEEDED**

---

## FALSE POSITIVES (reviewed, not real issues)

- Study desk awards LETTER_OPENER — intentional planted red herring, NARRATOR_WEAPON contradiction is built around this
- Thomas Ashford / "THOMAS WAS RIGHT" anomalies — intentional horror subplot (EntityAnomaly)
- Cold cellar door vs warm pulsing wall — two separate supernatural anomalies in different locations
- Muddy boots "entered through window" deduction — intentional detective inference text
- "One place no one would search twice" phrase — refers to why that location was chosen, not uniqueness
- Margaret train ticket — half-packed suitcase in room establishes she was planning to leave
- Narrator identifies Harold's name, will contents, Daniel's alibi claim — detective second-person narration, not narrator first-person voice
- "Warm like flesh / the wall is breathing" — sensory second-person description, not narrator omniscience
- James coat count 1 "was wearing this the night of the murder" — reasonable detective deduction
- All MILD_DISTORTIONS text — well-written, intentional misdirection
- All SEVERE_DISTORTIONS text — appropriate panic/manipulation for high awareness
- Distortion firing on "Nothing noteworthy here" — creates atmosphere, narrator interjects regardless
- Severe distortions lacking Contradiction enum mapping — intentional, they are psychological not evidence-based
- TAPE_MARGARET_INTERVIEW counted as Daniel evidence — Margaret's tape describes midnight footsteps + 2 AM dragging by two people, directly implicating Daniel
- BLOODSTAINED_CUFF not in Daniel count — it's James's shirt, not Daniel's; correctly excluded
- James evidence /6 count matches EvidenceSystem exactly — verified correct
- All 7 EntityAnomaly descriptions match what examination text reveals
- All Tape titles and hidden locations match examination text
- TAPE_ARTHUR_DEATH titled "The Opening" — intentional cryptic title (opening the sealed wall)
- TAPE_MARGARET_INTERVIEW found in kitchen behind canisters — odd but consistent game mechanic of scattered recordings
- All ClimaxContent moral ending text — all false positives; sealing/escape/destroy endings are internally consistent; poetic parallels are intentional
- Cellar/Margaret's room narrator transition messages — intentional unreliable narrator misdirection, not factual errors
- Charles Webb age=28 (in greeting text) vs cooperation=70 — those are two separate fields, not a bug
- James and Charles both starting at coop 70 — intentional: James feigns cooperation, Charles genuinely helps
- All cooperation routing thresholds — intentional design choices, not factual errors
- All GameScreen win/lose/moral-choice UI text — factually accurate, correct killers/weapon/motive throughout
- Awareness warning messages — escalate correctly, no false information
- All room descriptions (escalation variants) — design notes only, no factual contradictions
- Shed logbook "now closed" awareness variant — describes post-examination state, not a contradiction
- GUARDIAN/ARSONIST/SURVIVOR achievements — all reachable via handleMoralEnding(), functional
- COMPLETIONIST/PERFECT_INVESTIGATION hidden requirement (only unlocks on correct win) — intentional design
- James starting coop 70 / Charles starting coop 28 — design preference, not a factual error
- Margaret "probably Daniel" whisper identification — plausible given household familiarity
- Margaret awake at 2 AM — waking at night is normal, not a continuity error
- All tape "issues" (James lying, Daniel's slip, Marcus's timeline, Margaret's sounds) — intentional suspect behaviour by design
- All low-coop false responses contradicting the truth — that is literally their purpose
- Evidence reactions revealing guilt (poker, bloodstained cuff, cleaned handle) — intentional incriminating reactions
- James identifying muddy boots as Daniel's — he is correct, they are Daniel's boots found in Daniel's shed
- Charles immediately suspecting James from sleeping powder — in-character for loyal secretary, not a factual error
- Thomas Ashford's impossible survival — intentional supernatural horror element
