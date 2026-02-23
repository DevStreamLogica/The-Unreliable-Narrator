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
