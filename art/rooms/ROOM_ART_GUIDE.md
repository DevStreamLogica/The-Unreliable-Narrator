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
- **Chalk outline** on the floor where Harold's body was — this is the crime scene
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
- Forward: Servants' Quarters (arrow, top center)
- Door: Study (door hotspot, center of screen)

---

## 4. The Kitchen — `kitchen.png`

**Perspective:** Standing in the kitchen, looking across the room.

**Examinable objects (2):**
- **Storage cellar door** — a narrow door in the floor or wall, center of the image. This is both an examinable object (tape hidden on a shelf inside it) and a navigation point (leads down to Cellar)
- **Flour tin** — on a shelf, left side of the room. Large metal tin among other containers/spices. (Sleeping powder hidden inside)

**Other visual elements:**
- Large industrial kitchen — copper pots hanging from overhead racks, counters, a stove
- Shelves with spices, jars, ingredients
- Feel: functional, not cozy. Cold tile or stone floor.

**Navigation exits:**
- Back: Entrance (arrow, bottom center)
- Left: Servants' Quarters (arrow, left edge)
- Down: Cellar (stairs hotspot, center of screen)

---

## 5. Guest Rooms — `guest_rooms.png`

**Perspective:** Standing in the upstairs hallway, looking at two bedroom doors.

**Examinable objects (4):**
- **Margaret's room/doorway** — left side. Through the ajar door you can see: a tidy room, a half-packed suitcase on the bed, a nightstand with a tape recorder. A dresser visible with a folded letter/note on it.
- **James's room/doorway** — right side. Through the ajar door: messy, unmade bed, overflowing ashtray, half-finished whisky glass. Wardrobe door hanging open with a coat on it (bloodstained cuff).
- **Letter on dresser** — inside Margaret's room, on the dresser surface
- **James's coat** — inside James's room, hanging on the wardrobe door

**Other visual elements:**
- The hallway between the two rooms — carpet runner, maybe wall sconces
- The contrast between Margaret's neatness and James's chaos tells a story
- Feel: upstairs, quieter, more intimate

**Navigation exits:**
- Down: Entrance (stairs hotspot, center of screen)

---

## 6. Groundskeeper's Shed — `groundskeeper_shed.png`

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
- Back: Entrance (arrow, bottom center)

---

## 7. Servants' Quarters — `servants_quarters.png`

**Perspective:** Standing in the staff living area.

**Examinable objects (2):**
- **Narrow staircase** — left side, going up/back. Scratches visible on the banister. (Something heavy was dragged along it)
- **Loose floorboard** — center-right, near the wall. Slightly raised, gap visible. (A bundled bloody shirt hidden underneath)

**Other visual elements:**
- Simple, clean room — modest compared to the rest of the manor
- A simple bed, a small table
- Plain walls, basic furniture
- Feel: humble, orderly. The staff keeps things neat.

**Navigation exits:**
- Back: Parlor (arrow, bottom center)
- Right: Kitchen (arrow, right edge)

---

## 8. The Cellar — `cellar.png`

**Perspective:** Standing at the bottom of the stairs, looking into a dark underground space.

**Examinable objects (2):**
- **Wine racks** — left side, floor to ceiling. Dusty bottles, but one section is recently disturbed. (Tape hidden behind bottles, taped to the wall)
- **Flour sacks** — center-right, stacked against the wall. Drag marks in the dust on the floor around them. (This is where the body was moved to)

**Other visual elements:**
- Dark, damp underground room — stone walls, low ceiling, very dim lighting
- Barrels in the background
- The stairs going up (back to Kitchen)
- Feel: the darkest, most oppressive room. Cold, damp air. This is where the body was hidden.

**Navigation exits:**
- Up: Kitchen (stairs hotspot, center of screen)
