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

**Examinable objects (8) — position matters:**
- **Harold's mahogany desk** — large, center-bottom of the image. Papers scattered across it. A silver letter opener glinting on the surface. Desk has drawers visible on the front/side.
- **Papers** on the desk surface (separate clickable area from the desk itself)
- **Under the desk** — the area beneath, shadowed (a tape is hidden here)
- **Bookshelves** — floor-to-ceiling, on the right side of the room. Leather-bound volumes in rows.
- **Large bay windows** — center-back wall, overlooking the grounds. Latch visible. Scuff marks on the sill.
- **Stone fireplace** — left side of the room, large. Embers still glowing, ashes visible in the grate (torn letter fragments hidden here)
- **Ashes** — in the fireplace grate (separate clickable area from the fireplace)
- A **doorway** visible somewhere (leads to Parlor)
- Overall feel: a rich man's study, now disturbed and ominous

**Navigation exits:**
- Back: Entrance (arrow, bottom center)
- Forward: Parlor (door hotspot, center of screen)

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
