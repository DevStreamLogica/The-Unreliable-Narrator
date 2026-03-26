# The Unreliable Narrator — Research Document

## Overview

**Game:** The Unreliable Narrator
**Setting:** Vance Manor, November 1987
**Genre:** 2D point-and-click murder mystery with horror elements
**Platform:** LibGDX (Java)

The player investigates the murder of Harold Vance, guided by a mysterious mental presence — a voice that fills the mind rather than coming from any physical direction. The narrator is helpful but unreliable: as the player uncovers more truth, the narrator begins distorting evidence and gaslighting the investigation.

---

## The Murder — What Actually Happened

Harold Vance discovered that his son James had been embezzling £50,000 from the family company, aided by the groundskeeper Daniel Hobbs. The night of November 15th, 1987, Harold told James during an argument (around 10:00 PM) that he was disinheriting him — changing the will the next morning at 9:00 AM, leaving everything to Margaret.

James could not let the will be signed. Between 10:45 PM and 11:00 PM, he returned to the study and killed Harold with the fireplace poker. He then staged the scene — placing the letter opener as a false murder weapon — and planted a blackmail note in Margaret's room to implicate her. At approximately 2:00 AM, James and Daniel together dragged the body down to the cellar.

The murder was motivated by inheritance and covered up through James and Daniel's conspiracy.

---

## Characters

### Harold Vance (Victim)
- Patriarch of Vance Manor, wealthy industrialist
- Discovered James's embezzlement; planned to disinherit him
- Had a dark secret: in 1957, he sealed his business partner Thomas Ashford inside a walled-off cellar chamber after a "disagreement"
- His margin note on the construction invoice reads: *"He left me no choice. Ashford threatened to expose everything. This is the only way."*

### James Vance (Killer)
- Harold's son; stood to inherit everything under the old will
- Had embezzled £50,000 from the company over 2 years, with Daniel's help
- Motive: prevent disinheritance before the will was signed the next morning
- Actions: killed Harold with the poker (10:45–11:00 PM), staged the scene, planted blackmail note in Margaret's room, moved body to cellar with Daniel at 2:00 AM

**Key evidence against James:** Financial Records, Will Copy, Fireplace Poker, Bloodstained Cuff, Blackmail Note (he planted it), Tape: Argument, Tape: James Interview

**Tells:**
- Claims he was asleep in his room — contradicted by Charles seeing him head toward the study at 10:45 PM
- Reacts to fireplace poker by saying "I cleaned that" before catching himself
- Bloodstained cuff in the cellar hidden behind flour sacks
- Coat in his room has a bloodstained right cuff

### Daniel Hobbs (Accomplice)
- Groundskeeper; 15 years at Vance Manor
- Had been helping James hide embezzled transactions; deposited £20,000 in 2 years on a groundskeeper's salary
- Claims he was in his shed all night — contradicted by muddy boots with cellar dust
- Helped James move the body to the cellar at 2:00 AM
- Torn a page from his logbook for the night of November 15th

**Key evidence against Daniel:** Groundskeeper Log (torn page), Muddy Boots, Tape: Daniel Interview, Tape: Margaret Interview (she heard two people)

**Physical limitation:** 63 years old, arthritis in both hands. Could not have acted alone — needs James's involvement for the accusation to hold.

### Margaret Vance (Witness/Suspect Red Herring)
- Harold's daughter; received nothing in the original will
- Runs a successful textile import company she built herself
- Heard: the 10 PM argument, midnight footsteps (two people, one sounding like James), 2 AM dragging sounds toward the cellar
- Was too frightened to investigate; locked her door
- Left a personal tape recording for the detective (Arthur Hollis) because the police didn't believe her
- The blackmail note in her room was planted by James

**Her defense:** She wasn't in the will at all. She had a train ticket bought 3 days prior. The "wine-stained shoes" are wine, not blood.

### Marcus Blackwood (Suspect Red Herring)
- Harold's business rival; £2M patent lawsuit pending
- At the manor that night for a settlement dinner; Harold refused every offer
- Left at 11:00 PM; confirmed by hotel check-in at 11:47 PM and Charles seeing his car leave
- Overheard the Harold-James argument; saw James storm past the parlor
- His logic: Harold's death makes the lawsuit *worse* for him, not better

### Charles Webb (Key Witness)
- Harold's personal assistant; 5 years' service; 28 years old
- Was preparing will documents the night of the murder for signing the next morning
- Saw James heading toward the study at 10:45 PM from his window
- Heard Marcus's car leave at 11:00 PM
- Worked until 1:00 AM, then slept; didn't hear the 2 AM dragging sounds
- Found the body in the morning, then went to get Daniel (notably: Daniel was the one he sought first)
- Was also going to lose his job (company restructuring) — but this makes him helpful to the investigation, not a suspect

### Thomas Ashford (The Entity)
- Harold's former business partner, sealed behind a cellar wall in 1957 after threatening to expose something
- Has been imprisoned for 30 years — what he has become is no longer fully human
- The Breathing Wall, Cold Spot, and other cellar anomalies are caused by his presence
- He manipulated Arthur Hollis (the detective before the player) into breaking the wall open by sounding desperate and human
- Arthur's screams are on Tape 8 (The Opening); Arthur was never seen again
- The initials "A.H." scratched into the servants' quarters bedpost suggest Arthur Hollis made it that far before being taken

---

## The Narrator

The narrator is a disembodied voice that fills the player's mind — not from any direction, but from everywhere at once, "like a memory that isn't yours." The narrator presents itself as a helpful guide who has been "waiting for someone who would listen."

**The narrator's true nature is ambiguous and disturbing:**
- In the NARRATOR_I_SLIP anomaly, the narrator says "I remember when the wall was —" before catching himself, then claiming "I'm merely a voice... Of course I don't. I have no personal memory of this place."
- This slip strongly implies the narrator is Thomas Ashford — or at minimum, is connected to what's sealed in the wall.
- The narrator *deliberately lies* in the I-Slip anomaly text by calling itself "a voice on a tape" — this is the narrator's cover story, not the truth.

**Distortion behavior:**
- At awareness ≥ 40: 20% chance of mild distortions appended to text
- At awareness ≥ 60: 30% chance of severe distortions
- Mild distortions include false claims about the murder weapon (letter opener), boots, the torn letter, and the time of death
- Severe distortions gaslight the player: "You already solved this — Margaret did it," "The tapes are lying to you," "James loved his father"
- Distortions that contradict evidence the player holds auto-discover Narrator Contradictions

**Narrator Mood (based on awareness):**
- 0–19: HOPEFUL — clear, detective-like commentary
- 20–39: CONFUSED — foggy, uncertain prefixes
- 40–59: ANXIOUS — trembling, dark commentary
- 60–80: FRANTIC — panicked, unhinged commentary

**Channeling (Interviews):**
- The narrator "channels" suspect memories during interviews — voices their past statements
- The narrator shows "bleed-through" moments (20% chance) where his own identity bleeds into the channeled voice
- At FRANTIC mood, the narrator's voice fractures and loses control

---

## Evidence

| Evidence | Location | Description |
|---|---|---|
| Letter Opener | Study: desk (1st exam) | Staged murder weapon — doesn't match wound pattern |
| Fireplace Poker | Study: poker (2nd exam) | Real murder weapon — blood traces matching Harold's skull fracture |
| Torn Letter | Study: ashes (1st exam, minigame) | Burned letter; someone warned Harold about the will before it was too late |
| Financial Records | Study: drawers (1st exam) | Hidden compartment; shows £50k in unexplained transfers |
| Will Copy | Parlor: briefcase (1st exam) | James inherits everything; Margaret gets nothing — the will Harold was about to change |
| Muddy Boots | Shed: shelf (2nd exam) | Daniel's boots; fresh cellar dust proves he lied about staying in the shed |
| Sleeping Powder | Cellar: cellar_shelf (1st exam) | Chloral hydrate; used to drug Harold's tea to make him vulnerable |
| Bloodstained Cuff | Cellar: flour_sacks (1st exam) | James's shirt hidden in the cellar; blood on the right cuff |
| Blackmail Note | Margaret's Room: letter (1st exam) | Planted by James; crude handwriting is deliberate disguise |
| Groundskeeper Log | Shed: logbook (1st exam) | November 15th entry torn out |

**James evidence count: 7** (Financial Records, Will Copy, Torn Letter, Fireplace Poker, Blackmail Note, Tape: Argument, Tape: James Interview)
**Daniel evidence count: 4** (Groundskeeper Log, Muddy Boots, Tape: Daniel Interview, Tape: Margaret Interview)
**Accusation requires:** 3 pieces of James evidence + 2 pieces of Daniel evidence

---

## Tapes

All 8 tapes are police interview recordings or personal recordings made on physical tape recorders. Some are damaged and require repair.

### Tape Unlock Chain

Tapes are locked sequentially. Watching one unlocks the next:

| Tape | Title | Location | Status | Unlocked By |
|---|---|---|---|---|
| 1 | Harold & James Argument | Study: under_desk | Always available | — |
| 2 | James Vance - Police Interview | Study: bookshelves | Locked | Watch Tape 4 + learn code ESTATE-42 |
| 3 | Daniel Hobbs - Police Interview | Shed: logbook | DAMAGED (needs repair solution) | Watch Tape 4 |
| 4 | Margaret Vance - Police Interview | Kitchen: under the counter | Available after Tape 1 | Watch Tape 1 |
| 5 | Marcus Blackwood - Police Interview | Parlor: briefcase | Locked | Watch Tape 4 |
| 6 | Charles Webb - Police Interview | Parlor: grandfather_clock | Locked | Watch Tape 5 |
| 7 | Margaret's Personal Account | Margaret's Room: dresser | DAMAGED (needs repair solution) + room locked | Watch Tapes 2 & 3, room unlocks via BREATHING_WALL anomaly |
| 8 | The Opening (Arthur's death) | Cellar: wine_rack | DAMAGED (needs repair kit) + cellar locked | Cellar unlocks at 4 watched tapes + 5 evidence |

### Tape Repair System

- **Repair solutions** (consumable, can be found multiple times): Repair Tape 3 (Daniel) or Tape 7 (Margaret Account)
  - Locations: Kitchen flour tin, Cellar flour sacks (2nd exam), Servants' Quarters drawer
- **Tape repair kit** (non-consumable, required for Tape 8): Found in Margaret's Room dresser (1st exam)
  - Tape 8 requires the kit *and* the cellar to be unlocked

### Tape Summaries

**Tape 1 — Harold & James Argument**
Harold confronts James about £50k embezzlement. Announces he's changing the will the next morning. Tells James he knows about the arrangement with Daniel. Dismisses Daniel. James is desperate and pleading. Recorded from under the desk.

**Tape 2 — James Vance Police Interview (Nov 17, 2:00 PM)**
James claims he went to bed at 11:30 PM and knew nothing. Deflects by suggesting Margaret or Marcus. When confronted about £50k, requests a solicitor. Key admission implied when confronted with evidence. Ends with "I think I need to speak to my solicitor."

**Tape 3 — Daniel Hobbs Police Interview (Nov 17, 4:30 PM)**
*(Damaged — ribbon pulled loose)*
Daniel claims he was in his shed until 11 PM, then bed. Claims no alibi. Key slip: "I was helping move — I mean, move furniture." £20k in unexplained deposits. Margaret heard dragging at 2 AM. Daniel claims he was helping James move "a desk" in the afternoon.

**Tape 4 — Margaret Vance Police Interview (Nov 17, 10:00 AM)**
Margaret describes: tense dinner with Marcus, argument at 10 PM, midnight footsteps (two people whispering, one sounded like James), 2 AM dragging sounds toward the cellar. Notes James looked exhausted and Daniel wouldn't meet her eyes the next morning. Whispered admission: "God help me, yes" when asked if she thinks James was involved.

**Tape 5 — Marcus Blackwood Police Interview (Nov 17, 3:00 PM)**
Marcus describes the failed settlement dinner. Left at 11 PM; hotel confirmed 11:47 PM check-in. Heard the argument. Saw James storm past. Confirmed Charles's window was lit when he left. Logic: Harold's death hurts Marcus's legal position, not helps it.

**Tape 6 — Charles Webb Police Interview (Nov 17, 11:30 AM)**
Charles confirms Harold was disinheriting James. Key witness: saw James heading to the study at 10:45 PM, looking "determined, almost grim." Heard Marcus's car leave at 11 PM. Worked until 1 AM, then slept. Found the body at 7 AM. Notes Charles was also losing his job — but remains loyal and eager to help.

**Tape 7 — Margaret's Personal Account**
*(Damaged — ribbon deliberately cut)*
Margaret recorded this for Arthur Hollis because the police didn't believe her. More detailed and emotional than her police interview. Describes the same events but adds that James "couldn't meet my eyes" the morning after. Ends: "Please find out what really happened. Father deserves justice. Even if it breaks my heart."

**Tape 8 — The Opening (Arthur's Death)**
*(Damaged — requires repair kit)*
Arthur Hollis recording himself investigating the false cellar wall on November 19th, 1987. Discovers the wall was added in 1957. Hears a voice from inside — Thomas Ashford, sounding human and desperate. Thomas begs Arthur to break the wall open, refusing police. Arthur agrees and fetches tools. When the wall breaks: "You're not — you're not human anymore." Thomas's voice changes: "HAROLD MADE ME INTO THIS." Arthur screams. Recording ends with non-human breathing, then static.

---

## Entity Anomalies

Anomalies are supernatural clues pointing toward Thomas Ashford's existence. There are 7 total.

| Anomaly | Location | Discovered By |
|---|---|---|
| Breathing Wall | Cellar: wine_rack (2nd exam) | Move bottles aside, press hand to wall — it pulses like flesh |
| Thomas Reference | Study: fireplace (2nd exam) | Letters scratched on inner wall: "THOMAS WAS RIGHT" |
| Narrator 'I' Slip | Anywhere (20% chance after 3 anomalies found) | Narrator says "I remember when the wall was —" then corrects himself |
| Scratched Initials | Servants' Quarters: bedpost (2nd exam) | "A.H." carved into wood — Arthur Hollis's initials |
| Unknown Man in Photo | Study: bookshelves (2nd exam) | Photo of Harold with a man whose face is scratched out |
| Cold Spot | Cellar: cellar_shelf (2nd exam) | Stone wall ice-cold, breath mists — cold coming from deeper in the wall |
| Construction Record | Study: papers (2nd exam) | 1957 construction invoice marked URGENT; Harold's margin note: "He left me no choice. Ashford threatened to expose everything." |

**Margaret's Room unlock condition:** Discovering the BREATHING_WALL anomaly.
**Cycle Breaker achievement:** Discover all 7 anomalies before an ending.

---

## Contradictions

Contradictions are discovered when the narrator's lies are cross-referenced against evidence.

| Contradiction | Description |
|---|---|
| WEAPON | Letter opener was planted; poker is the real weapon (Harold's skull fracture pattern) |
| BODY_POSITION | Harold died in the study (blood pooling); body was moved to cellar by two people |
| NARRATOR_WEAPON | Narrator claims letter opener was the murder weapon — contradicted by Fireplace Poker evidence |
| NARRATOR_BOOTS | Narrator says no one left the house — contradicted by Muddy Boots evidence |
| NARRATOR_LETTER | Narrator calls the torn letter "unimportant" — contradicted by finding the Torn Letter |
| NARRATOR_TIME | Narrator claims Harold died after midnight — contradicted by Charles's interview (James seen at 10:45 PM, Marcus left at 11 PM with no further shouting) |

Narrator contradictions auto-discover when the narrator's distortion text fires AND the player already holds the contradicting evidence.

---

## Room Layout & Navigation

```
                    [GUEST ROOMS]
                          |
                    UP/DOWN stairs
                          |
[PARLOR] <--WEST-- [ENTRANCE] --EAST--> [STUDY]
                       |NORTH
                    [KITCHEN] --WEST--> [SERVANTS' QUARTERS] --WEST--> [GROUNDSKEEPER'S SHED]
                       |DOWN
                    [CELLAR]

[JAMES' ROOM] and [MARGARET'S ROOM] branch off GUEST ROOMS (east and west doors)
```

### Room Contents

**Entrance Hall** — Hub room. Leads to all main areas.

**The Study** (Crime Scene)
Hotspots: desk, drawers, papers, bookshelves, window, fireplace, poker, ashes, under_desk
Key finds: Letter Opener, Financial Records, Torn Letter (minigame), Tape 1 (under desk), Tape 2 (bookshelves), Fireplace Poker (2nd exam), Construction Record anomaly (papers 2nd), Thomas Reference anomaly (fireplace 2nd), Unknown Man Photo anomaly (bookshelves 2nd)

**The Parlor**
Hotspots: grandfather_clock, briefcase, fireplace
Key finds: Tape 5 (briefcase), Will Copy (briefcase), Tape 6 (grandfather clock)

**The Kitchen**
Hotspots: flour_tin, kitchen_floor (removed after tape collected), storage_cellar (portal)
Key finds: Tape 4 (kitchen_floor — removed after collecting), repair solution (flour_tin)
Texture swaps to "kitchen without.png" after Tape 4 is collected.

**Guest Rooms** — Hallway hub; leads to James's Room and Margaret's Room.

**James's Room**
Hotspots: wardrobe (replaced by coat after opening), coat (appears only after wardrobe opened)
Texture: closed wardrobe by default ("james closed.jpeg"); swaps to open wardrobe after 1st examine
Key finds: James's bloodstained coat sleeve (context clue; the shirt itself is in the cellar)

**Margaret's Room** (locked until BREATHING_WALL anomaly)
Hotspots: letter, dresser
Key finds: Blackmail Note (letter), Tape 7 (dresser, damaged), Tape repair kit (dresser 1st exam), stained shoes clue (dresser 2nd exam)

**Groundskeeper's Shed**
Hotspots: logbook, shelf
Key finds: Tape 3 (logbook, damaged), Groundskeeper Log (logbook), Muddy Boots (shelf 2nd exam)

**Servants' Quarters**
Hotspots: bedpost, drawer
Key finds: Scratched Initials anomaly (bedpost 2nd), repair solution (drawer)

**The Cellar** (locked until 4 watched tapes + 5 evidence)
Hotspots: cellar_shelf, flour_sacks, wine_rack
Key finds: Sleeping Powder (cellar_shelf), Bloodstained Cuff (flour_sacks), Tape 8 (wine_rack, damaged), repair solution (flour_sacks 2nd), Cold Spot anomaly (cellar_shelf 2nd), Breathing Wall anomaly (wine_rack 2nd)

---

## Game Mechanics

### Awareness Meter
- Starts at 0; max is 80
- Increases +1 per examine action, +1 per navigation action
- Watching tapes costs +4 awareness each (Tape 8 costs +5)
- **At 80: Game Over** — the entity has fully noticed the player; sent back to main menu

### Narrator Mood
- Shifts based on awareness (see Narrator section above)
- Affects commentary prefixes, warnings, environmental cues, and interview channeling

### Cooperation (0–100 per suspect)
- Each suspect starts at their configured value
- Adjusts based on interview choices (true answers vs. confrontation)
- Higher cooperation = suspect is more forthcoming

### Tape Damage & Repair
- Three tapes are damaged and cannot be played without repair:
  - **Tape 3 (Daniel):** Casing cracked, ribbon pulled loose — needs a repair solution
  - **Tape 7 (Margaret Account):** Ribbon deliberately cut — needs a repair solution
  - **Tape 8 (Arthur):** Cracked casing, ribbon snapped and tangled — needs the repair kit
- **Repair solutions** (consumable, 1 per use): found in Kitchen flour tin, Cellar flour sacks (2nd), Servants' Quarters drawer
- **Tape repair kit** (non-consumable, acquired once): found in Margaret's Room dresser on 1st examine

### Accusation
To accuse James and Daniel, the player needs:
- 3+ James evidence pieces
- 2+ Daniel evidence pieces
- Accessed via ACCUSE button in the action bar

**Accusation outcomes:**
- Correct (James + Daniel): Win condition
- Wrong accusation: Each suspect gives a defense; wrong accusation count tracked
- Margaret defense: Train ticket, says shoes are wine not blood
- Daniel defense: Physical limitations (63 years old, arthritis) — he couldn't have acted alone
- Marcus defense: Hotel alibi confirmed, killing Harold hurt his lawsuit
- Charles defense: Was going to lose his job, helped build the case against James, found the body

### Minigame — Torn Letter Reconstruction
Triggered by examining the study fireplace ashes (1st exam). Reassembles burned letter fragments. Rewards the Torn Letter evidence.

### Suspect Lies System
Each suspect has a "false response" for each topic — if the narrator distortion system is active and awareness is high enough, the narrator can channel these false responses instead of true ones.

### Save/Load
Full save/load system persists all game state: awareness, evidence, tapes, cooperation, exam counts, anomalies, contradictions, tape repair state, and achievements.

---

## Endings

| Ending | ID | How to Reach |
|---|---|---|
| Correct Accusation | ACCUSATION_CORRECT | Accuse James + Daniel with sufficient evidence |
| Wrong Accusation | ACCUSATION_WRONG | Accuse an incorrect suspect(s) |
| Seal the Wall | SEAL_THE_WALL | Choose to reseal the cellar wall after discovering the entity |
| Escape Manor | ESCAPE_MANOR | Leave the manor without resolving the case |
| Destroy Tapes | DESTROY_TAPES | Destroy the evidence/recordings |
| Awareness Game Over | GAME_OVER_AWARENESS | Reach awareness 80 — returns to main menu |
| Leave Manor | LEAVE_MANOR | Choose to leave |

---

## Achievements

| Achievement | Condition |
|---|---|
| SPEEDRUN | Win in 10 commands or fewer |
| GHOST | Win with awareness below 25 |
| COMPLETIONIST | Win with all evidence and all tapes watched |
| PERFECT_INVESTIGATION | All evidence + all tapes + awareness < 30 |
| GUARDIAN | Choose SEAL_THE_WALL ending |
| ARSONIST | Choose DESTROY_TAPES ending |
| SURVIVOR | Choose ESCAPE_MANOR ending |
| CYCLE_BREAKER | Discover all 7 anomalies before any ending |

---

## Interview System

Each suspect has:
- A **greeting** (initial approach text)
- **Topics** they can be asked about
- **True responses** (normal cooperation)
- **False responses** (may be channeled when narrator is distorting)
- **Evidence reactions** (show evidence during interview for unique responses)
- **Margaret deflections** (each suspect may redirect suspicion toward Margaret)
- **Accusation defense** (used if player wrongly accuses them)

### Topic Lists

- **James:** last_night, father, will, daniel, whereabouts
- **Margaret:** last_night, brother, father, inheritance, insurance, company, sounds
- **Daniel:** last_night, harold, james, logbook, shed
- **Marcus:** lawsuit, settlement, that_night, harold, alibi
- **Charles:** harold, will_changes, james, that_night, company

### Key Evidence Reactions
- **James + Fireplace Poker:** "Where did you... I cleaned that. I cleaned it thoroughly." (inadvertent confession)
- **James + Financial Records:** "Where did you get those? Those are private!" (breaks composure)
- **Daniel + Sleeping Powder:** Says "chloral hydrate" immediately, knows exactly what it's used for (too familiar)
- **Charles + Sleeping Powder:** Deduces Harold was being drugged weekly to make him vulnerable — points to James

---

## Key Timelines

**Night of November 15th, 1987:**
- 7:00 PM — Dinner (Harold, James, Margaret, Marcus)
- 8:30 PM — Dinner ends; Marcus and Harold move to parlor
- ~8:30–9:00 PM — James and Margaret in drawing room; Margaret goes up at 9
- 9:00 PM — Charles finishes study work; retires to guest room
- ~8:30 PM — Marcus alone in parlor (Harold in study)
- 10:00 PM — Harold and James argument in the study (whole household hears it)
- ~10:15 PM — James storms out; study door slams
- 10:45 PM — Charles sees James heading back toward the study (determined, grim)
- 10:45–11:00 PM — **Murder occurs** in the study (fireplace poker)
- 11:00 PM — Marcus hears no more argument; gives up, drives away (Charles sees this)
- ~Midnight — Margaret hears two people whispering in the hallway (James + Daniel)
- 2:00 AM — Margaret hears dragging sounds toward the cellar stairs
- 7:00 AM — Charles goes to study, finds Harold missing; searches manor; finds body in cellar; goes to get Daniel; calls police

**November 17th — Police Interviews:**
- 10:00 AM — Margaret Vance
- 11:30 AM — Charles Webb
- 2:00 PM — James Vance
- 3:00 PM — Marcus Blackwood
- 4:30 PM — Daniel Hobbs

**November 19th:**
- Arthur Hollis (previous detective) investigates the cellar; breaks the wall; is never seen again

---

## The True Narrative (All Layers)

1. **Surface story:** Harold is killed by an unknown assailant. The detective (player) must find who did it.
2. **Investigation layer:** James and Daniel committed the murder together. The evidence trail leads to them.
3. **Narrator layer:** The narrator — supposedly a helpful guide — is actually unreliable, distorting evidence and protecting the killers. The more the player discovers, the more aggressive the distortions become.
4. **Entity layer:** Harold had sealed a man behind the cellar wall 30 years ago. That man (Thomas Ashford) is still there, no longer fully human, and has been manipulating investigators who come too close.
5. **Deepest layer:** The narrator's identity slip ("I remember when the wall was —") suggests the narrator may be Thomas Ashford himself — the entity who has been guiding (and manipulating) the player the entire time, using the investigation as a vehicle to be freed again, as it was freed by Arthur Hollis.

The game's title — *The Unreliable Narrator* — refers both to the literary device and to the literal identity of the voice guiding the player.
