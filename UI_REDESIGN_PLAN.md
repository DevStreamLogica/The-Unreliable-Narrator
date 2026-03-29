# UI Redesign Plan
_Created: 2026-03-27_

---

## Overview
Major UI/UX design changes across GameScreen, TextPanel, ActionBar, and TitleScreen.
Items are listed in the order they were requested. Implementation order is noted separately.

---

## Items

### 1. Character Dialogue Box — Paginated, 30% Height, Speaker Header

**Status:** Done

**Files:** `TextPanel.java`, `GameScreen.java`

**Problem:** Interview/character dialogue is displayed as one long block in the full-size text panel. Hard to read, no speaker attribution.

**Solution:**
- Add a **dialogue mode** to `TextPanel`:
  - Height = `SCREEN_HEIGHT * 0.30f` = 216px, positioned at bottom of screen
  - **Header bar** (~30px) at top with a lighter background (`0.12f, 0.12f, 0.17f` vs body's `0.05f, 0.05f, 0.08f`), showing the current speaker's name
  - **Paragraph pagination** — text split by `\n\n`; shows one paragraph at a time
  - **"Next ▶"** button (action `"next_page"`) to advance; replaced by action buttons on the last page
- New method: `textPanel.showDialogue(String speakerName, String text, List<TextButton> afterButtons)`
- New internal fields: `String[] dialoguePages`, `int currentPage`, `String speakerName`, `boolean dialogueMode`, `Texture headerTexture`
- New method: `textPanel.nextPage()` — advances `currentPage`, re-runs typewriter on next segment

**GameScreen changes:**
- `startInterview()` → `textPanel.showDialogue(suspect.getDisplayName(), sb.toString(), buttons)`
- `handleAskTopic()` → `textPanel.showDialogue(suspect.getDisplayName(), sb.toString(), buttons)`
- `handleConfrontation()` → same
- `handlePanelAction()` → add case for `"next_page"` that calls `textPanel.nextPage()`
- Regular `textPanel.show(...)` (examination, tape, menus) keeps full-size mode unchanged

---

### 2. Remove the Tape Solution Mechanic

**Status:** Done

**Files:** `GameScreen.java`, `GameState.java`, `ExaminationSystem.java`, `EvidenceSystem.java`

**Problem:** The tape repair mechanic (repair kit in Margaret's room, damaged tape gates) adds friction that isn't needed.

**Solution — remove all of the following:**
- `GameScreen.handleExamine()`: Remove the `if (result.grantsRepairSolution())` block (~lines 657–661)
- `GameScreen.handleMargaretDrawer()` case `"kit"`: Remove `addRepairSolution()` call; simplify or remove the kit pickup entirely
- `GameScreen.playTape()`: Remove the 4 gate checks for `isDamagedTape()` / `getRepairSolutionsRemaining()` (~lines 1363–1411); damaged tapes now play freely
- `GameScreen.isDamagedTape()`: Delete the method
- `GameScreen.showInventory()`: Remove `blockedByDamage` check (~lines 738–743)
- `GameState.java`: Remove `repairSolutionsRemaining`, `hasTapeRepairKit`, `tapeRepaired` fields and all their getters/setters
- `EvidenceSystem.getInventoryText()`: Remove any mention of repair solutions
- `ExaminationSystem.java`: Remove `grantsRepairSolution()` from ExamResult (or entire ExamResult field if unused after)

---

### 3. Upper-Right Pop-Up Notifications (5 seconds)

**Status:** Done

**Files:** `GameScreen.java`

**Problem:** Evidence/tape discoveries are appended to the examination text panel, making them easy to miss and cluttering the main dialogue.

**Solution:**
- Add notification queue fields to `GameScreen`:
  ```java
  private final java.util.Queue<String> notifQueue = new java.util.LinkedList<>();
  private String currentNotif = null;
  private float notifTimer = 0f;
  private static final float NOTIF_DURATION = 5f;
  ```
- Add helper: `private void showNotification(String msg)` — pushes to queue
- In `render()`: update timer; pop next when expired; draw in upper-right:
  - Dark semi-transparent background (`0.05f, 0.12f, 0.08f, 0.90f`)
  - Gold border
  - White-cream text
  - Position: `x = SCREEN_WIDTH - notifW - 16`, `y = SCREEN_HEIGHT - 60`
- In `handleExamine()`:
  - Replace `display.append("\n\n[EVIDENCE FOUND: ...]")` → `showNotification("Evidence: " + evidence.getDisplayName())`
  - Replace `display.append("\n\n[TAPE FOUND: ...]")` → `showNotification("Tape found: " + tape.getTitle())`

---

### 4. Speaker Name in Character Text Box Header

**Status:** Done (part of Item 1)

Covered entirely by the dialogue mode header bar described in Item 1. No separate implementation needed.

---

### 5. Inventory Button — Bottom Left

**Status:** Done

**File:** `ActionBar.java`

**Problem:** The INVENTORY button is centered at the bottom, which is less ergonomic.

**Solution:**
- Line 40 in `ActionBar.java`:
  - Change: `clusterX = (DSAGame.SCREEN_WIDTH - clusterWidth) / 2;`
  - To: `clusterX = BUTTON_MARGIN;`
- The background bar drawn at `clusterX - 12` follows automatically.

---

### 6. Remove Back Button Visual

**Status:** Done

**File:** `GameScreen.java`

**Problem:** The floating back button (back.png overlay that fades in when mouse is near floor) is visual noise.

**Solution — remove all of the following:**
- Rendering block at ~lines 2273–2287 (`// Draw back button if this room has a back hotspot`)
- `backButtonTexture` field + load in `generateUITextures()`
- `backButtonAlpha` field and all update logic in `render()` (~lines 2202–2206)
- `mouseGameY` field and its assignment in `mouseMoved` (line 329)
- Constants `FLOOR_Y_THRESHOLD` and `BACK_FADE_SPEED`

**Note:** The `ARROW_BACK` hotspot stays — clicking the floor area still navigates back. Only the visual button is removed.

---

### 7. Hotspot Hover — Cursor Change & Tooltip Fix

**Status:** Done

**File:** `GameScreen.java`

**Problem:** (a) Hovering over an interactive hotspot gives no cursor feedback. (b) `currentTooltip` is cleared every frame but never populated from hotspot labels — tooltips are invisible.

**Solution — rewrite the hotspot hover block in `mouseMoved()`:**
```java
boolean overHotspot = false;
for (Hotspot hotspot : roomManager.getCurrentRoom().getHotspots()) {
    hotspot.checkHover(gameX, gameY);
    if (hotspot.isHovered()) {
        currentTooltip = hotspot.getTooltip();
        overHotspot = true;
        if (hotspot.getType() == Hotspot.HotspotType.EXAMINE) {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
        } else {
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        }
    }
}
if (!overHotspot) {
    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
}
```

---

### 8. Main Menu Button Hover Highlight

**Status:** Done

**File:** `TitleScreen.java`

**Problem:** When using the image background (`usingImageBackground = true`), hovering over menu buttons gives no visual feedback.

**Solution:**
- Add field: `private String hoveredAction = null;`
- Add field: `private Texture highlightTexture;` — 1x1 white pixel, created in constructor
- In `mouseMoved()`, when `usingImageBackground`:
  ```java
  hoveredAction = null;
  for (ClickRegion region : clickRegions) {
      if (region.contains(gameX, gameY)) {
          hoveredAction = region.action;
          break;
      }
  }
  ```
- In `render()`, draw highlight over hovered region after background:
  ```java
  if (usingImageBackground && hoveredAction != null) {
      for (ClickRegion region : clickRegions) {
          if (region.action.equals(hoveredAction)) {
              batch.setColor(0.9f, 0.9f, 0.7f, 0.18f);
              batch.draw(highlightTexture, region.x, region.y, region.width, region.height);
              batch.setColor(Color.WHITE);
          }
      }
  }
  ```

---

### 9. Back Button — Room Restriction (Deferred)

**Status:** Deferred (to do later)

**Scope:** Parlor, Kitchen, Study, James's Room, Margaret's Room — only these rooms would show the back navigation. All other rooms lose back navigation.

---

## Implementation Order

| Priority | Item | Est. Effort | File(s) |
|----------|------|-------------|---------|
| 1 | #5 Inventory bottom-left | Trivial | ActionBar.java |
| 2 | #6 Remove back button visual | Small | GameScreen.java |
| 3 | #7 Hotspot hover + tooltip fix | Small | GameScreen.java |
| 4 | #8 Main menu hover highlight | Small | TitleScreen.java |
| 5 | #3 Notification system | Medium | GameScreen.java |
| 6 | #2 Remove tape solution | Medium | GameScreen.java, GameState.java, ExaminationSystem.java, EvidenceSystem.java |
| 7 | #1 + #4 Dialogue mode TextPanel | Large | TextPanel.java, GameScreen.java |

---

## Updates Log

| Date | Item | Status | Notes |
|------|------|--------|-------|
| 2026-03-27 | All | Planned | Initial plan written |
| 2026-03-27 | #5 Inventory bottom-left | **Done** | `ActionBar.java` — changed `clusterX` to `BUTTON_MARGIN` |
| 2026-03-27 | #6 Remove back button | **Done** | `GameScreen.java` — removed rendering, field, fade logic, constants |
| 2026-03-27 | #7 Hotspot hover + tooltip fix | **Done** | `GameScreen.java` — cursor changes to Hand on EXAMINE hotspots; tooltip now populated |
| 2026-03-27 | #8 Main menu hover highlight | **Done** | `TitleScreen.java` — semi-transparent highlight drawn over hovered click region |
| 2026-03-27 | #3 Notification system | **Done** | `GameScreen.java` — queued upper-right pop-ups, 5-second duration, evidence/tape finds trigger them |
| 2026-03-27 | #2 Remove tape solution | **Done** | Removed from `GameScreen`, `GameState`, `SaveLoadSystem`, `EvidenceSystem`, `ExamResult`, `ExaminationSystem` |
| 2026-03-27 | #1 + #4 Dialogue mode TextPanel | **Done** | `TextPanel.java` rewritten with dialogue mode: 30% height, speaker header, paragraph pagination, Next button |

