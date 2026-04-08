# DSA 2D — Sound Design Reference

Each entry lists the sound needed, its filename, duration, and a detailed AI generation prompt.

---

## TITLE SCREEN / MAIN MENU

### Button Hover
**File:** `ui_btn_hover.wav`
**Duration:** 80–100ms
> A short, soft UI tone that plays when the mouse moves over a menu button.

**Prompt:** A very brief, soft UI hover sound lasting around 80–100 milliseconds. A gentle, muted high-frequency tick or soft chime — like a muffled piano key or a lightly brushed glass rim. Slightly warm, not harsh or digital. Should feel like a whisper of acknowledgment. No reverb tail. Tone sits around 800–1200 Hz. Quiet, polished, tasteful — the kind of sound you barely notice but miss when it's gone.

---

### New Game Button Click
**File:** `ui_btn_new_game.wav`
**Duration:** ~150ms
> Confirms starting a new game. Should feel intentional and weighted.

**Prompt:** A satisfying, deliberate UI click sound lasting around 150 milliseconds. A clean, medium-weight mechanical thud with a very brief high-frequency tick layered on top — like pressing a firm keyboard key on a high-quality typewriter. Followed by a faint, warm resonance tail that fades in under 300 ms. Not too sharp, not too soft. Should feel like a commitment — heavier than a hover sound, lighter than a dramatic sting. Slightly warm and analog in character, as if the UI belongs to an older, tactile world.

---

### Load Game Button Click
**File:** `ui_btn_load_game.wav`
**Duration:** ~120ms
> Opens the load screen. Similar weight to New Game but slightly more neutral.

**Prompt:** A clean, medium-weight button click sound, around 120 milliseconds. Similar to a firm mouse click layered with a brief soft resonance. Slightly dryer than the New Game click — less sense of beginning, more sense of retrieval. A subtle metallic undertone, like the latch of a drawer being pulled. Short decay, minimal reverb. Sits comfortably in the mid-frequency range around 600–900 Hz.

---

### Settings Button Click
**File:** `ui_btn_settings.wav`
**Duration:** 80–100ms
> Opens the settings panel. Should feel lightweight and administrative.

**Prompt:** A light, clean UI click around 80–100 milliseconds. A soft tap with a brief high-frequency shimmer — like tapping a glass surface with a fingertip. Neutral in tone, not dramatic. Slight digital crispness, short decay, no reverb tail. Feels quick and functional.

---

### Quit Button Click
**File:** `ui_btn_quit.wav`
**Duration:** ~150ms
> Exits the game. Should feel slightly final without being melodramatic.

**Prompt:** A brief, clean UI click with a slightly descending pitch — around 150 milliseconds total. Starts with a firm tap sound then drops in pitch slightly, like a soft downward note on a xylophone. Not dramatic or sad, just a small sense of closure. Minimal reverb, dry and direct. Should feel deliberate and resolved.

---

### Settings Panel Open
**File:** `ui_panel_open.wav`
**Duration:** ~200ms
> The settings panel slides or fades into view.

**Prompt:** A soft mechanical slide sound lasting around 200 milliseconds. Like a smooth drawer being opened on a wooden desk — a brief whoosh of air followed by a faint wooden settle at the end. Subtle low-mid frequency presence. Not a digital swipe — should feel physical and grounded. Slight room ambience on the tail end.

---

### Settings Panel Close
**File:** `ui_panel_close.wav`
**Duration:** ~180ms
> The settings panel dismisses.

**Prompt:** The reverse of the panel open sound — a brief soft whoosh with a gentle wooden settle, but in reverse order. Around 180 milliseconds. Feels like something being tucked away neatly. Slightly softer than the open sound — less energy, more finality.

---

### Text Speed Changed
**File:** `ui_toggle_click.wav`
**Duration:** 60–80ms
> Player selects a new text speed setting.

**Prompt:** A tiny, crisp toggle click around 60–80 milliseconds. Like a small physical switch being flipped — a clean, bright tick with no reverb and very fast decay. High frequency, precise, almost mechanical. The kind of sound a light switch or small dial makes when snapped into a new position.

---

### Load Slot Selected
**File:** `ui_slot_select.wav`
**Duration:** ~100ms
> Player clicks on a save slot in the load menu.

**Prompt:** A clean, soft selection click around 100 milliseconds. A slightly hollow tap, like pressing a key on an old typewriter with moderate force. Warm mid-range, minimal brightness. Brief resonance tail, fades in under 200 ms. Feels like selecting something from a list — neutral, efficient, slightly tactile.

---

## INTRO SCREEN

### Intro Screen Loads — Atmosphere
**File:** `amb_intro_screen.wav`
**Duration:** 4–6 seconds (fades into looping hum)
> The intro screen appears. Sets the tone for the entire game.

**Prompt:** A slow, atmospheric swell lasting 4–6 seconds that fades in from silence. Deep, low strings humming in a minor key beneath a layer of distant, muffled wind. Very faint, almost subliminal low-frequency rumble — like the manor itself breathing. A subtle high-pitched tone sits underneath everything, barely audible, creating unease. No percussion. No melody. Just texture and tension. Should feel like stepping into a space where something is wrong but you can't name it yet. Ends in a soft, unresolved ambient hum that loops naturally.

---

### Narrator Opens Gate — Gate Creak
**File:** `sfx_gate_open.wav`
**Duration:** 2–3 seconds
> The detective/narrator arrives and opens the iron manor gate.

**Prompt:** A slow, heavy iron gate creak lasting 2–3 seconds. Starts with a dry metal groan as the latch disengages — a low, grinding scrape of aged iron on iron. As the gate swings open, the creak rises slightly in pitch and gains a resonant, hollow metallic quality. Ends with a deep clang and a brief chain rattle as the gate settles. The whole sound has a damp, slightly reverberant quality — as if recorded outdoors on a cold evening. No music. No processing. Raw and physical.

---

### Detective Walks Up Gravel Path — Footsteps
**File:** `sfx_footsteps_gravel.wav`
**Duration:** ~5 seconds (6–8 steps)
> The narrator/detective walks up the manor's gravel path toward the front door.

**Prompt:** A sequence of slow, deliberate footsteps on loose gravel, 6–8 steps total. Each step has a satisfying crunch — dry, compact gravel underfoot, with slight variation in the crunch texture between steps so they don't sound looped. Pace is measured and cautious — not rushed. Slight ambient outdoor space around the sound — a faint breeze in the background. The footsteps have a slight echo off a stone surface nearby. Recording perspective feels close, as if from the walker's own ears. Ends after the last step with a brief silence before the door.

---

### Front Door Opens
**File:** `sfx_door_front_open.wav`
**Duration:** 2–4 seconds
> The detective enters the manor through the heavy front door.

**Prompt:** A slow, heavy wooden door opening, lasting 2–4 seconds. Starts with a deep, bass-heavy creak as the door's weight shifts — the kind of creak only a solid, antique door makes. The wood groans and strains as it swings inward. A faint rush of displaced air. At the end, the door settles with a deep, resonant thud against the wall or door stop. Slight reverb from the interior of the manor seeping through. Rich, physical, old. The sound should feel like entering another world.

---

### Grandfather Clock Heard Inside Manor
**File:** `sfx_clock_distant.wav`
**Duration:** Looping (2-beat cycle)
> Upon entering, the tick of the manor's grandfather clock can be heard in the distance.

**Prompt:** A distant, slow, measured grandfather clock ticking — two alternating tones (tick-tock) at roughly 60 BPM. Each tick is a deep, resonant wooden thunk with a slight metallic overtone — the sound of a large, quality pendulum clock. The sound is muffled by distance, as if coming from another room. Slight reverb from a large, high-ceilinged hallway. The ticking is steady and calm but has a weight to it — each beat feels significant. Should loop seamlessly at a 2-beat cycle.

---

### Tape Recorder Crackle Detected
**File:** `sfx_tape_crackle_intro.wav`
**Duration:** 1–2 seconds
> A tape recorder is briefly heard — faint static and crackle indicating its presence.

**Prompt:** A brief burst of analog tape recorder noise lasting 1–2 seconds. Starts with a soft mechanical click of a play button being pressed, then a 1-second rush of magnetic tape hiss — warm, analog static with slight wow and flutter artifacts. Beneath the hiss, the faintest ghost of a human voice — too quiet and distorted to make out any words. Then it cuts out abruptly with a mechanical stop click. The sound feels old, worn, and slightly wrong — like a recording that has been played too many times.

---

### "Enter the Manor" Button Click
**File:** `ui_btn_enter_manor.wav`
**Duration:** ~200ms
> Player chooses to proceed into the manor.

**Prompt:** A weighty, deliberate UI click with a slight dramatic undertone — around 200 milliseconds. A firm, deep mechanical press followed by a very brief, low resonant hum — like a heavy door latch clicking into place. Not a light button tap. This choice matters. The sound should feel slightly final, slightly ominous. A soft bass presence beneath the click. Minimal reverb, short decay.

---

### "Ignore and Move On" Button Click
**File:** `ui_btn_ignore.wav`
**Duration:** ~100ms
> Player dismisses something. A more neutral, dismissive click.

**Prompt:** A clean, neutral UI click around 100 milliseconds. A dry, crisp tap with no resonance or emotional weight. Slightly higher pitched than the Enter the Manor click. Feels like brushing something aside. Fast decay, no reverb. Flat and functional.

---

## DINNER CUTSCENE SCREEN

### Cutscene Loads — Dinner Ambience
**File:** `amb_dinner_cutscene.wav`
**Duration:** Looping (30+ second loop)
> Background atmosphere for the dinner table cutscene — a formal Victorian dinner in progress.

**Prompt:** A subtle, looping dinner party ambience for a formal Victorian setting. Very quiet background — no distinct voices, just the warm murmur of polite conversation in a medium-large dining room. The occasional soft clink of cutlery on fine china. A glass being set down. Distant, muted. The room has a warm, wood-paneled acoustic quality — sound is absorbed and intimate, not echoey. A fire in a nearby fireplace adds a faint, low crackle to the background. Loop length should be at least 30 seconds with seamless loop points. Calm but with a subtle underlying tension — everyone is on their best behavior.

---

### Character Portrait Transitions In
**File:** `ui_portrait_in.wav`
**Duration:** 150–200ms
> A character's portrait appears during the cutscene.

**Prompt:** A brief, soft transition sound around 150–200 milliseconds. A gentle, slightly photographic whoosh — like a page being turned very slowly, or a card being placed on a table. Slight paper or linen texture to the sound. Very quiet high-frequency shimmer at the tail. Understated. Should feel like someone stepping into the frame of a portrait.

---

### Character Portrait Transitions Out
**File:** `ui_portrait_out.wav`
**Duration:** ~120ms
> A character's portrait fades or slides away.

**Prompt:** The reverse of the portrait-in sound — a soft, quiet whoosh retreating away, around 120 milliseconds. Like a page being turned backward or a card being withdrawn from view. Slightly softer than the in-transition. Brief high-frequency shimmer at the start, fades to nothing.

---

### Text Advances to New Page
**File:** `ui_text_advance.wav`
**Duration:** ~150ms
> Player clicks through to the next block of dialogue or narration.

**Prompt:** A soft, subtle page-turn sound lasting around 150 milliseconds. The whisper of a single page of heavy, quality paper being turned — slightly crisp at the start of the turn, then a quiet settle. Not a sharp flip — deliberate and unhurried. Sits comfortably in the high-mid frequency range. Very quiet, almost subliminal. Should feel like turning the page of a novel.

---

### "Begin Investigation" Button Click
**File:** `ui_btn_begin_investigation.wav`
**Duration:** ~500ms (click + swell)
> The investigation officially begins.

**Prompt:** A deliberate, slightly ceremonious UI click lasting around 200–250 milliseconds. A firm, deep mechanical press with a brief rising resonance tail — as if something has been set in motion. The initial click is clean and authoritative, followed immediately by a subtle low swell lasting about 300 ms — barely musical, almost like a distant bell's fundamental tone decaying. Should feel like the moment a case file is opened. Purposeful. Important. Slightly ominous.

---

## ROOM TRANSITIONS

### Door Opens — Room Exit
**File:** `sfx_door_interior_open.wav`
**Duration:** 1.5–2 seconds
> Player moves from one room to another. Door opens sound.

**Prompt:** A medium-weight interior wooden door opening, lasting 1.5–2 seconds. A brief resistance as the handle is turned — a small mechanical latch click — followed by the door swinging open with a low, wooden creak. Not as heavy as the front door, but solid and old. Slight air displacement. Interior room acoustics — the sound has a slight reverb from a carpeted hallway. Ends with the door settling against a doorstop or hinge stop. Feels functional but slightly worn.

---

### Door Closes — Room Entry
**File:** `sfx_door_interior_close.wav`
**Duration:** 1–1.5 seconds
> Player enters a new room; door closes behind them.

**Prompt:** A medium-weight interior door closing from inside a room, lasting 1–1.5 seconds. A swing with minimal creak, then a firm, satisfying latch click as it closes fully. Slight bass thud as the door meets the frame. Room acoustics change subtly — the new room's acoustic character bleeds in at the very end. Old wood, solid construction. Not a slam — deliberate and quiet.

---

### Screen Fade Out — Transition Begins
**File:** `sfx_transition_out.wav`
**Duration:** ~300ms
> The screen begins fading to black between rooms.

**Prompt:** A very subtle, brief audio fade-down lasting about half a second. A soft, low exhale of air or a barely-perceptible distant bass tone descending — like a room holding its breath. Under 300 milliseconds. Should be nearly subliminal.

---

### Screen Fade In — Transition Completes
**File:** `sfx_transition_in.wav`
**Duration:** ~500ms
> The new room fades in from black.

**Prompt:** A very subtle audio bloom lasting about 500 milliseconds. A soft, slightly ethereal swell of room tone — the new room's acoustics opening up, like stepping outside from a closet into a large hallway. Barely perceptible. A breath of new space. Not musical, just spatial. Should make the listener feel like a new environment has arrived around them.

---

### Locked Door Attempt
**File:** `sfx_door_locked.wav`
**Duration:** ~500ms
> Player tries to enter a room that is locked.

**Prompt:** A brief, definitive rejection sound lasting about 500 milliseconds. A firm door handle rattle — metallic, slightly hollow — followed immediately by the clunk of a locked latch refusing to yield. A second, shorter rattle as the handle is released. The whole sound has a slightly echoey, corridor acoustic. Feels like a dead end. No musical tone. Purely physical and final.

---

### Sealed Door Attempt — Margaret's Room
**File:** `sfx_door_sealed_supernatural.wav`
**Duration:** 1.5–2 seconds
> Player attempts to enter Margaret's sealed room. Supernatural seal.

**Prompt:** A strange, unsettling rejection lasting about 1.5–2 seconds. Begins with a normal door handle rattle — but as the hand pushes against the door, there is an unnatural resistance — not the solid thud of a lock, but a soft, yielding pressure, as if the door is pushing back. Beneath this, a faint, resonant hum begins — low frequency, slightly dissonant, almost vocal in quality. A cold, airy tone sweeps briefly. The handle is released and the hum fades quickly. Should feel like the door is alive. Unsettling rather than frightening.

---

## ROOM AMBIENCES (Looping)

### Entrance Hall — Ambient Loop
**File:** `amb_room_entrance.wav`
**Duration:** Looping (60–90 second loop)
> The grand manor entrance. First room. Sets the entire tone.

**Prompt:** A looping ambient atmosphere for a large Victorian entrance hall, 60–90 seconds minimum before loop point. The foundation is a faint, very low frequency room tone — the bass resonance of a large, high-ceilinged space. Layered above: the slow, distant tick-tock of the grandfather clock two rooms away — deep, resonant, slightly muffled. Occasional barely-perceptible creak of old floorboards settling. A faint breath of air — the manor's natural draft through old corridors. Extremely subtle, almost subliminal high-pitched tone — the sound of silence that isn't quite silent. No music. No obvious sound events. Just the feeling of an old, grand, slightly wrong space.

---

### Study — Ambient Loop
**File:** `amb_room_study.wav`
**Duration:** Looping (60–90 second loop)
> The victim's private study. Fireplace. Bookshelves. Papers.

**Prompt:** A looping ambient atmosphere for a book-lined Victorian study, 60–90 seconds before loop point. Centered around a low, steady fireplace — the gentle, irregular crackle and pop of a real wood fire. The fire is medium-sized, well-established — not roaring, not dying. Warm and low. Layered on top: the very occasional creak of a large wooden bookshelf or floorboard. The distant tick of the entrance hall clock, much quieter than in the hall. A faint paper rustle, as if a loose document is being moved by a draft. The room feels enclosed and warm but with an undercurrent of unease — someone worked here, and now they're dead.

---

### Parlor — Ambient Loop
**File:** `amb_room_parlor.wav`
**Duration:** Looping (60–90 second loop)
> The formal sitting room. Grandfather clock. Victorian elegance.

**Prompt:** A looping ambient atmosphere for a Victorian parlor, 60–90 seconds before loop point. Dominated by the grandfather clock — now much closer and more present. Its tick-tock is clear, measured, deep. Resonant wooden body. Each tick has a slight metallic overtone from the pendulum. Underneath: a very faint creak of ornate wooden furniture settling. A barely-audible draft from tall windows. The clock is the heartbeat of this room. The overall mood is formal, slightly cold, and timelessly oppressive. No fire. No warmth. Just time, ticking.

---

### Kitchen — Ambient Loop
**File:** `amb_room_kitchen.wav`
**Duration:** Looping (60–90 second loop)
> The manor kitchen. Evidence of daily life. Staff and secrets.

**Prompt:** A looping ambient atmosphere for a large Victorian kitchen, 60–90 seconds before loop point. A faint background hum — either a distant water pipe or a very old appliance maintaining temperature. Occasional creak of a well-used wooden floor. Very faint drip of a tap that's not fully closed — irregular, not rhythmic. A distant ventilation draft from a stone chimney flue. The room has a slightly damp, cool stone acoustic quality. It should feel like a room where people used to be busy, now empty. Slightly melancholy.

---

### Guest Rooms Hallway — Ambient Loop
**File:** `amb_room_guest_hallway.wav`
**Duration:** Looping (60–90 second loop)
> The upper-floor hallway outside the guest rooms.

**Prompt:** A looping ambient atmosphere for an upper-floor Victorian hallway, 60–90 seconds before loop point. The most prominent quality is space and slight echo — footsteps from elsewhere in the building travel as faint, distant creaks. A long, narrow space with a slightly hollow acoustic. Very subtle draft — wind finding its way through old window seals. An almost imperceptible settling of the house structure. Slightly more isolated and lonelier than the ground floor rooms. The clock from downstairs is barely audible — one floor removed.

---

### James's Room — Ambient Loop
**File:** `amb_room_james.wav`
**Duration:** Looping (60–90 second loop)
> The son's bedroom. Personal and unsettled.

**Prompt:** A looping ambient atmosphere for a young man's Victorian bedroom, 60–90 seconds before loop point. Similar quiet to the hallway but more enclosed. A slight stuffiness to the acoustic — the room doesn't breathe well. Very faint creak of a wooden bed frame as if someone shifted in it — but no one is there. Occasional tick from a small mantle clock, slower and higher-pitched than the grandfather clock downstairs. A barely-audible draft from a window that doesn't seal properly. The room has an unsettled quality — not sinister, but anxious.

---

### Margaret's Room — Ambient Loop
**File:** `amb_room_margaret.wav`
**Duration:** Looping (60–90 second loop)
> The daughter's bedroom. Delicate. Sealed. She was planning to flee.

**Prompt:** A looping ambient atmosphere for a young woman's Victorian bedroom, 60–90 seconds before loop point. The most distinctive element: a very faint, distant music box melody — barely audible, a single delicate theme playing at the very edge of hearing. It is unclear if it is real or imagined. Underneath: soft, feminine room acoustics — more carpeted and draped than other rooms, absorbing sound warmly. A very slight draft — but colder than it should be. The room is still but not peaceful. The music box melody should be in a minor key, not a recognizable tune — just 6–8 notes cycling slowly.

---

### Groundskeeper's Shed — Ambient Loop
**File:** `amb_room_shed.wav`
**Duration:** Looping (60–90 second loop)
> Outdoor tool storage. Earthy. Industrial. Connected to secrets.

**Prompt:** A looping ambient atmosphere for a stone or wooden outbuilding shed, 60–90 seconds before loop point. Distinctly different from the interior manor rooms — more raw and unfinished. Wind is more present here, audible around gaps in the structure. Occasional creak of a tin or wooden roof panel in the breeze. Metal tools hang — a very faint rattle when wind moves through. A distant bird call, very occasional, from outside. The room feels honest and unguarded — a working space.

---

### Servants' Quarters — Ambient Loop
**File:** `amb_room_servants.wav`
**Duration:** Looping (60–90 second loop)
> Small, sparse bedroom for household staff. Humble. Isolated.

**Prompt:** A looping ambient atmosphere for a small, sparse servants' room, 60–90 seconds before loop point. Acoustically flat and small — not much space for sound to travel. A faint creak from the building structure. Distant sounds from elsewhere in the manor, further away than any other room. Very quiet — the kind of quiet that speaks to isolation and low status. A barely-audible hum from a pipe running through the wall. The room has a slightly oppressive, forgotten quality.

---

### Cellar — Ambient Loop
**File:** `amb_room_cellar.wav`
**Duration:** Looping (90–120 second loop)
> Underground storage. Damp. Ominous. Contains the most disturbing evidence.

**Prompt:** A looping ambient atmosphere for an underground stone cellar, 90–120 seconds before loop point. Deep, resonant underground acoustic — sounds have a slight distant echo. The dominant texture is slow, irregular water drips from the ceiling — not rapid, just occasional, with the echo of each drop fading slowly. A low, subsonic rumble — felt more than heard — the weight of the building above. Faint, damp air movement — the slow circulation of cold underground air. Very occasionally, the faint creak of a wine rack or wooden shelf above. The temperature of the sound is cold. This is a place of secrets.

---

## HOTSPOT / EXAMINATION SYSTEM

### Hotspot Hover Highlight
**File:** `ui_hotspot_hover.wav`
**Duration:** 80–100ms
> Player hovers over an examinable object.

**Prompt:** A very brief, gentle highlight tone around 80–100 milliseconds. A soft, high-frequency shimmer — like a lightly bowed crystal glass, but much quieter and shorter. Barely musical — more of a tonal click. Sits around 1500–2000 Hz. Slight warmth, not harsh. Should feel like the object has quietly acknowledged being noticed. Fast decay, minimal resonance.

---

### Examination Hold Timer — Pulse
**File:** `sfx_examine_pulse.wav`
**Duration:** Looping (plays while hold timer fills, up to 2–3 seconds)
> Player holds on a hotspot to examine it. A subtle pulse plays while holding.

**Prompt:** A low, slow pulsing tone that plays while the hold timer fills — lasting up to 2–3 seconds total. A soft, rhythmic pulse — like a very quiet heartbeat or a slow sonar ping — repeating at about 1.5–2 beats per second. Each pulse is a brief, muted tone around 400–600 Hz with a quick decay. The pulses should feel investigative and focused — the sound of careful attention. Slightly tense but not alarming. Very low volume. Should loop or repeat seamlessly until examination completes.

---

### Examination Complete — Success Chime
**File:** `sfx_examine_success.wav`
**Duration:** ~400ms + ~300ms tail
> Player successfully examines an object.

**Prompt:** A clean, satisfying success chime lasting about 400 milliseconds. Two or three ascending notes — a small, bright arpeggio — played on something resembling a soft bell or marimba. The notes are clear, close together in pitch, and resolve upward. A brief, warm resonance tail lasting about 300 ms. Not triumphant — this is routine discovery. Feels like a small confirmation. The tone should suggest "found something" without being dramatic.

---

## STUDY EXAMINATIONS

### Desk Examined — Paper Rustle / Drawer Slide
**File:** `sfx_desk_examine.wav`
**Duration:** ~1 second

**Prompt:** A layered sound lasting about 1 second. Begins with the soft rustle of paper — multiple loose documents shifting against each other, crisp and dry. Layered: a brief, smooth wooden drawer being slid open partway — the sound of wood on wood, slightly resistant from age. Ends with papers settling. Quiet and deliberate — the careful examination of a desk that belongs to someone recently deceased.

---

### Letter Opener Collected
**File:** `sfx_pickup_letter_opener.wav`
**Duration:** ~300ms

**Prompt:** A brief metallic object being lifted from a wooden surface, lasting about 300 milliseconds. A very faint scrape against the desk surface as it's grasped. Then the object is lifted — a brief, clean metallic ring. Slight high-frequency shimmer. Silver or steel quality. Followed immediately by the soft evidence-pickup chime — a small, ascending two-note tone.

---

### Hidden Compartment Revealed
**File:** `sfx_compartment_reveal.wav`
**Duration:** ~2 seconds

**Prompt:** A sequence of sounds lasting about 2 seconds. A very quiet exploratory tap — fingertips feeling the underside of a desk panel. Then a soft click — a hidden latch disengaging, mechanical and precise but aged. Then the slow slide of a hidden drawer — wood on wood, slightly stiff from years of disuse. Ends with a brief settling sound as the compartment opens fully. Quiet and precise — the sound of something that was meant to stay hidden being found.

---

### Financial Records Collected
**File:** `sfx_pickup_documents.wav`
**Duration:** ~800ms

**Prompt:** The sound of multiple pages of paper being carefully gathered and stacked, lasting about 800 milliseconds. Several sheets of crisp, old paper shuffled together — each sheet has a slightly brittle quality. A brief tap of the stack against the desk to align the pages. Followed by the evidence-pickup chime. Careful and methodical.

---

### Construction Invoice — ANOMALY STING
**File:** `sfx_anomaly_construction_invoice.wav`
**Duration:** 2–3 seconds

**Prompt:** A dissonant, unsettling sting lasting 2–3 seconds. Begins with a sharp, piercing high-frequency tone — like a glass resonance going slightly wrong — then immediately descends into a low, rumbling dissonance. Two or three notes clash against each other — minor second intervals — creating a sense of wrongness. A brief, cold reverb tail. Should feel like a stomach drop. Resolves into silence rather than a clean note.

---

### Bookshelves Examined — Book Slide
**File:** `sfx_bookshelf_examine.wav`
**Duration:** ~1.5 seconds

**Prompt:** The sound of a leather-bound book being slowly slid from a tightly packed shelf, lasting about 1.5 seconds. The initial resistance of the book against its neighbors — a faint creak of leather and pressed paper. Then the book slides free — smooth, deliberate — with a soft papery friction sound. Ends with the book being held — a brief settling of the weight. Old books, quality leather bindings, wooden shelves.

---

### Tape Recorder Discovered in Bookshelves
**File:** `sfx_tape_discover_study_shelf.wav`
**Duration:** ~1.5 seconds

**Prompt:** A sequence lasting about 1.5 seconds. A book slides out, then — beneath the sound of the book being placed aside — a hollow, mechanical click of plastic against wood. A brief moment of recognition. Then a distinct discovery sting: a short, mysterious two-note tone — descending rather than ascending — slightly eerie, slightly intriguing. The tape recorder has a slightly cold, mechanical quality to its discovery sound.

---

### Photo of Unknown Man — ANOMALY
**File:** `sfx_anomaly_unknown_photo.wav`
**Duration:** ~2 seconds

**Prompt:** A subtle, mysterious anomaly sting lasting about 2 seconds. A soft, slightly off-key resonance begins — like a string instrument being bowed with slight bow pressure, producing a wavering, uncertain tone. Beneath it, a quiet, low rumble. The sound is not dramatic — it's confused and questioning. Something doesn't fit. A slightly warped, photographic quality. Fades to a quiet, unresolved tone.

---

### Window Examined — Glass Sound / Latch
**File:** `sfx_window_examine.wav`
**Duration:** ~1.5 seconds

**Prompt:** Two distinct sounds in sequence, about 1.5 seconds total. First: a soft tap against glass — a knuckle or fingertip on an old, thick windowpane. The glass resonates briefly with a clear, high-frequency ring that decays quickly. Second: the window latch mechanism — a small, slightly stiff metal latch being tested — a brief rattle and click. A faint draft of cold air at the very end.

---

### Boot Prints Discovered on Windowsill
**File:** `sfx_discover_bootprints.wav`
**Duration:** ~1 second

**Prompt:** A quiet, investigative discovery sound lasting about 1 second. A soft, careful brushing sound — the detective's finger tracing the outline of a dried mud impression. Then a very brief, low-key discovery tone — just one note, a slightly hollow bell tone. Subtle. The kind of discovery that changes everything but is found quietly.

---

### Fireplace Examined — Fire Up Close
**File:** `sfx_fireplace_examine.wav`
**Duration:** 2–3 seconds

**Prompt:** A brief, focused foreground fire sound lasting 2–3 seconds — closer and more detailed than the ambient fireplace loop. The crack and pop of burning wood is more present. A deep, warm low-frequency rumble from the flue. The brief rush of hot air. One distinct, sharp wood-pop. Then the sound recedes slightly back to ambient level. Should feel like leaning close to the fire.

---

### "THOMAS WAS RIGHT" Scratched into Fireplace — ANOMALY
**File:** `sfx_anomaly_thomas_scratches.wav`
**Duration:** 3–4 seconds

**Prompt:** A deeply unsettling anomaly sting lasting 3–4 seconds. Begins with the sound of fingers tracing over a rough surface — a dry, scraping texture as the scratches are felt before seen. Then, as the message registers: a harsh, dissonant tone — like metal being dragged slowly across stone — begins low and rises in pitch, creating a rising dread. Layered beneath: a barely-audible whispered voice, completely unintelligible — just the texture of a voice. The scraping tone resolves into a cold, resonant silence with a slight reverb tail. Should feel like the walls have spoken.

---

### Fireplace Poker Examined
**File:** `sfx_poker_examine.wav`
**Duration:** ~1 second

**Prompt:** The sound of a metal fireplace poker being carefully lifted from its stand, lasting about 1 second. A soft metallic ring as it's lifted — iron or steel, with a slight hollow resonance. Brief clank against the stand as it's removed. The metal has weight. Cold metal sound — utilitarian and slightly ominous.

---

### Blood on Poker — Horror Sting
**File:** `sfx_horror_blood_poker.wav`
**Duration:** 1.5–2 seconds

**Prompt:** A sudden, sharp horror sting lasting 1.5–2 seconds. A brief, jarring dissonant chord — strings or synth — hitting hard and immediately. Not a long swell — an immediate impact. The chord contains at least two notes in strong dissonance — a tritone or minor ninth. Brief reverb tail. Then sudden silence. The silence after is as important as the sound itself — visceral and irreversible.

---

### Poker Collected as Evidence
**File:** `sfx_pickup_poker.wav`
**Duration:** ~500ms

**Prompt:** The careful placement of the poker — the soft clink of metal being set down precisely, deliberately. Then, the evidence-collection tone — a slightly darker version of the standard success chime. Two notes, ascending but in a minor key — acknowledging the find without celebrating it. Something important has been found, but it's terrible.

---

### Ashes Examined
**File:** `sfx_ashes_examine.wav`
**Duration:** ~800ms

**Prompt:** The soft, whispering sound of ash being gently disturbed — a dry, ultra-fine powder shifting. Almost like sand but lighter, with less weight and more air. Lasting about 800 milliseconds. A slightly powdery, soft acoustic — very little resonance, very dry. A brief billow of displaced ash at the end — a tiny, airy whoosh.

---

### Torn Letter Fragments Found in Ashes
**File:** `sfx_discover_torn_letter.wav`
**Duration:** ~1.5 seconds

**Prompt:** A sequence lasting 1.5 seconds. The ash sift sound, then beneath the powder — the brittle sound of partial paper — charred at the edges, fragile. A quiet, careful crinkle as the fragments are picked up — the sound of paper that has been burned but not destroyed. A delicate, careful sound. Followed by the discovery tone — a single, slightly uncertain note. Something important survived.

---

## PARLOR EXAMINATIONS

### Grandfather Clock Examined — Up Close
**File:** `sfx_clock_close.wav`
**Duration:** 3–4 seconds

**Prompt:** An immersive close-up clock sound lasting 3–4 seconds. The tick-tock is now immediate and present — a large, mechanical pendulum swing. Each tick is a rich, complex sound: the click of the escapement, the resonant thud of the pendulum at its apex, the tock as it returns. The wooden case amplifies the sound — a warm bass resonance. Faint gear movements audible beneath the main tick. Should feel like pressing your ear against the case. Authoritative, precise, ancient.

---

### Grandfather Clock Case Opened
**File:** `sfx_clock_case_open.wav`
**Duration:** ~2 seconds

**Prompt:** Two sounds in sequence, about 2 seconds total. First: the small brass latch of the clock case being turned — a precise, slightly stiff click. Then: the door of the clock case swinging open — a smooth, wood-on-hinge sound with slight air displacement. The interior of the case amplifies the clock mechanism slightly. At the end, the escapement clicking is now very audible. Old, precise, mechanical.

---

### Tape Recorder in Grandfather Clock
**File:** `sfx_tape_discover_parlor_clock.wav`
**Duration:** ~2 seconds

**Prompt:** A sequence lasting 2 seconds. The clock case opens, then — a hollow plastic knock as something shifts inside the case. A brief moment of confused silence against the clock's ticking. Then the discovery sting: a slightly eerie, two-note descending tone — heard against the clock's ticking, it feels more significant and more wrong.

---

### Briefcase Examined — Leather Creak / Opening
**File:** `sfx_briefcase_open.wav`
**Duration:** ~1.5 seconds

**Prompt:** A two-part sound lasting about 1.5 seconds. First: the creak of aged leather — the briefcase being lifted or handled, the leather protesting softly. Then: the brass latches clicking open — two sharp, clean metallic snaps in quick succession. The briefcase lid swings open — slight leather and metal sound as the hinges move. Old, quality, executive briefcase.

---

### Will Copy Discovered
**File:** `sfx_discover_will.wav`
**Duration:** ~1.5 seconds

**Prompt:** A sequence lasting about 1.5 seconds. The sound of a formal document being carefully unfolded — heavy, legal-quality paper. Each fold unfolds with a deliberate, stiff crinkle. Followed by the discovery tone — a single, clear, slightly resonant bell tone. One note. Important and official.

---

### Parlor Fireplace — Cold, No Fire
**File:** `sfx_fireplace_cold.wav`
**Duration:** ~1.5 seconds

**Prompt:** A brief, atmospheric examination sound lasting about 1.5 seconds. No fire — just the sound of cold air moving through a stone chimney. A faint, hollow draft — slightly eerie — the sound of a fireplace that is drawing air but generating no warmth. A faint resonance in the stone flue above. Should feel noticeably colder and emptier than the study's fireplace.

---

### Half-Burned Papers — Parlor Fireplace
**File:** `sfx_discover_burned_papers.wav`
**Duration:** ~1.5 seconds

**Prompt:** The sound of charred, fragile paper being carefully handled in a cold fireplace grate. Brittle, dry crinkle — more destroyed than the study fragments. Some pieces crumble slightly at the touch — a faint powdering sound. The grate produces a faint metallic sound as the detective leans in. Cold, still, slightly smoky. Evidence deliberately destroyed.

---

## KITCHEN EXAMINATIONS

### Storage Cellar Door Examined
**File:** `sfx_cellar_door_examine.wav`
**Duration:** ~2 seconds

**Prompt:** A sequence lasting 2 seconds. The iron ring-handle being grasped — cold metal, slightly rough. The door is pulled — heavy and reluctant — a deep, grinding creak as it lifts. A rush of cold, damp air from below — the faint acoustic of underground space breathing upward. The door settles, revealing the cellar entrance. Functional but ominous.

---

### Flour Tin Examined — Lid Opens
**File:** `sfx_flour_tin_open.wav`
**Duration:** ~800ms

**Prompt:** A brief, domestic kitchen sound lasting about 800 milliseconds. The flour tin is metal — round, with a fitted lid. The lid is grasped and twisted slightly before lifting with a soft metallic seal breaking. A brief rush of fine white powder — the soft, whispering bloom of flour disturbed. A faint, hollow resonance from inside the tin.

---

### Hidden Correspondence in Flour Tin
**File:** `sfx_discover_flour_tin_letter.wav`
**Duration:** ~1.5 seconds

**Prompt:** The flour bloom sound, then — beneath the powder — the sound of folded paper being found in an unexpected place. The paper is slightly damp from the enclosed tin environment. A quiet, surprised handling sound — the paper being lifted out carefully. The discovery tone: a brief, two-note ascending chime with a slightly hollow, tin-resonant quality. Finding a letter in a flour tin is an intimate, personal revelation.

---

### Kitchen Floor Tape — Special Discovery
**File:** `sfx_tape_discover_kitchen.wav`
**Duration:** 3–4 seconds

**Prompt:** A sequence lasting 3–4 seconds. The detective's footsteps stop. Then — heard from a lower angle, as if crouching — the sound of a tape recorder on a cold kitchen floor. A plastic, mechanical object on stone or tile. The detective picks it up — a careful lift. Then — immediately as it's picked up — the atmosphere subtly shifts: the kitchen ambience seems to hold its breath. A very quiet, barely-perceptible low tone beneath everything — almost subliminal — as if the kitchen itself remembers.

---

## JAMES'S ROOM EXAMINATIONS

### Wardrobe Opened — First Time
**File:** `sfx_wardrobe_open.wav`
**Duration:** 2–3 seconds

**Prompt:** A dramatic wardrobe opening lasting 2–3 seconds. Heavy wooden doors — possibly double doors — pulled open with some resistance. The hinges are large and slightly stiff — a deep, resonant creak building as the doors swing outward. Inside: the rustle of hanging clothes — fabric shifting against each other, wooden hangers sliding along a rail. A brief, cool air release from inside the enclosed space. Old cedar or mahogany. Domestic but slightly imposing.

---

### Bloodstain on Coat Cuff — Horror Sting
**File:** `sfx_horror_blood_coat.wav`
**Duration:** ~1.5 seconds

**Prompt:** A sharp, immediate horror sting lasting 1.5 seconds. Similar to the poker blood discovery but slightly more personal — this is clothing, this is a person. A brief, dissonant string chord hits immediately — a jarring impact. Then a low, sustained dissonant hum — the kind of tone that sits in the stomach. A cold high-frequency shimmer. Brief reverb tail. Then silence. Visceral and accusatory.

---

## MARGARET'S ROOM EXAMINATIONS

### Bedside Lamp Lifted
**File:** `sfx_lamp_lift.wav`
**Duration:** ~800ms

**Prompt:** A domestic object being carefully lifted, lasting about 800 milliseconds. The soft scrape of a ceramic or metal lamp base against a wooden bedside table. A slight rattle of the lamp shade. The object is lifted slowly — careful, deliberate. A small, quiet sound of finding — a folded paper edge scraping against the table surface as it's revealed.

---

### Blackmail Note Discovered
**File:** `sfx_discover_blackmail_note.wav`
**Duration:** ~1.5 seconds

**Prompt:** The lamp lift sound, then — a folded piece of paper being picked up. Ordinary stationery, folded hastily. The crinkle is softer, more personal. As it's unfolded: one deliberate fold opening, then another. The discovery tone is lower and darker than the standard chime — a single descending note, almost like a quiet question. Someone was being threatened. Should feel invasive and uncomfortable.

---

### Margaret's Dresser Drawers Opened (x2)
**File:** `sfx_dresser_drawer.wav`
**Duration:** ~600ms each

**Prompt:** A wooden dresser drawer being pulled open — smooth but slightly resistant from a full drawer. A quiet slide of wood on wood. The first drawer: contents shift slightly — fabric, small personal items. Light, intimate, slightly voyeuristic — these are someone's private belongings. Two variations should be made for each drawer.

---

### Tape Recorder in Margaret's Dresser
**File:** `sfx_tape_discover_margaret.wav`
**Duration:** ~1.5 seconds

**Prompt:** The drawer slides open, then — beneath clothing — the sound of a tape recorder being found. A heavier, more mechanical sound than expected in this intimate space. As it's lifted from the drawer, it comes free with a slight tug. The discovery sting carries a more melancholy quality — a two-note tone in a minor key, gentle but sad. This is her tape. It feels like a secret kept rather than a secret hidden.

---

### Half-Packed Suitcase Observed
**File:** `sfx_suitcase_observe.wav`
**Duration:** 2–3 seconds

**Prompt:** A quiet, atmospheric sound lasting 2–3 seconds. The sound of a suitcase being looked at rather than touched — just the ambient detail of a partially packed bag. The rustle of clothes inside — a very soft, involuntary shift. A quiet, sad ambience beneath: a single held tone, slightly wavering — almost like a distant voice. The sound of something unfinished. Someone was leaving. They didn't make it.

---

## GROUNDSKEEPER'S SHED EXAMINATIONS

### Logbook Examined — Pages
**File:** `sfx_logbook_examine.wav`
**Duration:** ~1.5 seconds

**Prompt:** A well-used hardcover logbook being opened and leafed through, lasting about 1.5 seconds. The cover opens with a stiff creak — cloth or leather binding. Pages turn one at a time — heavier paper than standard, slightly waxy from practical use. Each page turn is deliberate. A workmanlike, honest sound. Not decorative stationery — a functional record.

---

### Torn-Out Page Discovered
**File:** `sfx_discover_torn_page.wav`
**Duration:** ~2 seconds

**Prompt:** A sequence lasting about 2 seconds. Leafing through the logbook, then — the torn edge being found: a finger running along a ragged paper edge — a dry, slightly scratchy texture. The discovery tone: a flat, low two-note sequence — more of a statement than a chime. No uplift. This was deliberate destruction. The sound should feel like an absence.

---

### Muddy Boots Found
**File:** `sfx_discover_muddy_boots.wav`
**Duration:** 1.5–2 seconds

**Prompt:** A sequence lasting 1.5–2 seconds. A paint can sliding across a wooden shelf — a low, hollow metallic thunk and scrape. Then: the reveal of the boots — the slight brushing of leather against the shelf. As they're examined: the subtle crunch and flake of dried mud on the boot surface — a dry, earthy sound. Physical and grounded.

---

### Muddy Boots Analyzed — Cellar Mud
**File:** `sfx_boots_analysis.wav`
**Duration:** ~1 second

**Prompt:** A careful, detailed examination sound lasting about 1 second. A fingernail or tool scraping a small amount of dried mud from the boot — a dry, precise scraping sound. The discovery tone: a single, clear bell tone that descends slightly — confirming a connection. This is analysis, not discovery. Quiet, precise, significant.

---

## SERVANTS' QUARTERS EXAMINATIONS

### Bedpost Carved Scratches — "A.H." Initials — ANOMALY
**File:** `sfx_anomaly_ah_initials.wav`
**Duration:** 3–4 seconds

**Prompt:** A deeply unsettling anomaly sequence lasting 3–4 seconds. The sound of fingertips running over carved wood — feeling the indentations before seeing them. A dry, woody texture. Then: recognition. A rising, eerie tone — not quite musical — like a sine wave slowly pushed off key. A cold, hollow resonance building beneath it. The "A.H." discovery sting should feel ancient and impossible — someone was here before. Before you. A haunting, sustained dissonance that fades slowly.

---

### Nightstand Drawer Examined
**File:** `sfx_nightstand_drawer.wav`
**Duration:** ~600ms

**Prompt:** A small, simple wooden drawer being pulled open, lasting about 600 milliseconds. Light, unadorned — just functional wood on wood. The drawer sticks slightly at first, then releases. Inside: minimal contents — a quiet shift of simple objects. Humble. No ornament. The drawer of a person who owned very little.

---

### Notebook Discovered
**File:** `sfx_discover_notebook.wav`
**Duration:** ~1.5 seconds

**Prompt:** A sequence lasting about 1.5 seconds. The drawer sound, then — a soft-covered notebook being lifted out. The covers are flexible — a quiet bend of the spine as it's picked up. It opens naturally to a well-used page — the pages are soft from handling. The discovery tone is quiet and slightly ominous — a two-note minor descent. Someone wrote their thoughts here. Kept it hidden.

---

### Notebook Final Entry — Ominous Line
**File:** `sfx_notebook_final_entry.wav`
**Duration:** ~2 seconds

**Prompt:** A brief, chilling sound lasting about 2 seconds — designed to accompany an ominous final entry. A single, low string tone — bowed slowly, slightly pressured — creating a wavering, uncertain pitch. A cold reverb tail extends the tone beyond its natural decay. A barely-audible second tone a semitone away creates a dissonance that throbs slowly. The last words of someone who knew something terrible.

---

## CELLAR EXAMINATIONS

### Glass Vial of Sleeping Powder Found
**File:** `sfx_discover_vial.wav`
**Duration:** ~1.5 seconds

**Prompt:** A sequence lasting about 1.5 seconds. A glass object being carefully lifted from a wooden shelf — the soft, precise clink of glass against wood. The vial is small and clear — the glass has a clean, slightly sharp ring when touched. As it's examined: a faint swirl of powder inside — a whisper of fine substance moving in a sealed container. The discovery tone: a high, clear, single glass-tone note — bright and slightly cold. Beautiful and deadly.

---

### Cold Spot on Wall — ANOMALY
**File:** `sfx_anomaly_cold_spot.wav`
**Duration:** 3–4 seconds

**Prompt:** A deeply atmospheric anomaly lasting 3–4 seconds. As the detective approaches the wall: the ambient cellar sound subtly changes — the drips slow, the background hum drops in pitch. Then — a breath. A human breath, but slightly wrong — slightly too cold, slightly too slow. As the cold spot is touched: a low, resonant tone begins — like the stone itself vibrating at a frequency below normal. A faint, ghostly high-frequency shimmer above it. A barely-perceptible voice texture beneath everything — not words, just the suggestion of a presence. Should feel like reaching through a membrane into somewhere else.

---

### Drag Marks Discovered
**File:** `sfx_discover_drag_marks.wav`
**Duration:** ~1.5 seconds

**Prompt:** A sequence lasting about 1.5 seconds. The sound of the detective crouching — a brief cloth and footstep sound. Then: a slow, investigative sweep of a hand or light across the floor. The discovery tone: a single, deep, resonant bell tone that decays slowly. Heavy. Irrefutable. Something heavy was moved here.

---

### Bloodstained Shirt Found
**File:** `sfx_horror_blood_shirt.wav`
**Duration:** 2–3 seconds

**Prompt:** A major horror sting lasting 2–3 seconds. The most impactful blood discovery in the game. Begins with the sound of flour sacks being moved — heavy cloth shifting. Then — fabric. Then: the sting. A harsh, immediate dissonant chord — full and jarring. Multiple notes, strongly dissonant, with a slight orchestral swell. The chord is held for about 1 second then releases into a low, resonant bass tone that fades over the next second. This is the culmination of physical evidence. Terrible and irrefutable.

---

### Wine Bottles Moved
**File:** `sfx_wine_bottles_move.wav`
**Duration:** ~2 seconds

**Prompt:** The careful sound of glass wine bottles being moved from a rack, lasting about 2 seconds. Each bottle moved produces a clean, musical glass clink — not sharp or breaking, but the pleasant ring of fine glassware touching. Two or three bottles moved — brief, careful movements. A slight wooden creak of the wine rack adjusting. Almost pleasant — which contrasts with what is about to be discovered.

---

### Tape Recorder Behind Wine Rack — Damaged
**File:** `sfx_tape_discover_cellar.wav`
**Duration:** 2–3 seconds

**Prompt:** A distinctive, damaged discovery sound lasting 2–3 seconds. The tape recorder is revealed — its plastic casing cracks as it's picked up — a sharp, brittle snap. When briefly activated: a mechanical grinding sound from the tape mechanism — warped reels, damaged heads — followed by a burst of heavily distorted tape hiss, more corrupted and strange than any previous tape sound. The discovery sting: a descending, damaged tone — like a clean note played through a broken speaker — warbling and unstable.

---

### Breathing Wall — ANOMALY
**File:** `sfx_anomaly_breathing_wall.wav`
**Duration:** 4–5 seconds + 3–4 second reverb tail

**Prompt:** The most disturbing sound in the game, lasting 4–5 seconds. Begin with silence — an unusual silence, more complete than the cellar normally offers. Then: a single, slow pulse — felt as much as heard — a low, bass frequency event. Like a heartbeat, but impossibly slow and impossibly large. A long inhale sound — the movement of air at large scale, not human-breath-sized — as if the stone wall is expanding. Then a long, slow exhale — air being released from within solid stone. The heartbeat pulse repeats: one more beat. A faint, resonant tone deep within the stone — harmonics of something organic in something inorganic. Should inspire pure, cold dread — not a jump scare, but the deep wrongness of the impossible being real. Reverb tail of 3–4 seconds, fading into silence.

---

## MINIGAMES

### Torn Letter — Piece Picked Up
**File:** `sfx_minigame_letter_pickup.wav`
**Duration:** ~300ms

**Prompt:** A single piece of torn, aged paper being carefully picked up — about 300 milliseconds. A soft, crisp crinkle as fingertips make contact. The paper is slightly brittle — not fresh. Lifted cleanly. A quiet, papery sound with no musical tone. Simple and functional.

---

### Torn Letter — Piece Dragged
**File:** `sfx_minigame_letter_drag.wav`
**Duration:** Continuous (plays while dragging)

**Prompt:** A continuous sound for the duration of dragging — paper sliding across a smooth wooden surface. A soft, dry friction sound — barely audible but present. Very quiet. Should feel like careful, focused work.

---

### Torn Letter — Piece Snaps to Position
**File:** `sfx_minigame_letter_snap.wav`
**Duration:** ~200ms

**Prompt:** A brief, satisfying snap-and-settle sound around 200 milliseconds. A soft impact sound as the piece lands in place — like a puzzle piece snapping home — followed immediately by a tiny, clean chime: a single high note. The snap is tactile and precise. The chime confirms correctness. Quick, clear, rewarding.

---

### Torn Letter — All Pieces Placed
**File:** `sfx_minigame_letter_complete.wav`
**Duration:** ~2 seconds

**Prompt:** A completion fanfare lasting about 2 seconds. Multiple pieces clicking into place rapidly in quick succession — a cascade of soft snaps — then, as the last piece lands: a bright, multi-note chime sequence. Four or five ascending notes, resolving cleanly upward. A brief warm reverb tail. The sound of a lock opening — a sequence completing. Information assembled.

---

### Catcher — Word Spawns
**File:** `sfx_catcher_spawn.wav`
**Duration:** 100–150ms

**Prompt:** A brief appearance sound around 100–150 milliseconds. A soft, slightly digital pop — like a bubble breaking on a water surface, but higher in pitch and slightly mechanical. The sound should feel like a word materializing — sudden but not alarming. Clean and quick.

---

### Catcher — Correct Catch
**File:** `sfx_catcher_catch.wav`
**Duration:** ~200ms

**Prompt:** A satisfying catch sound around 200 milliseconds. A clean, bright single note — a brief ding — combined with a very soft whoosh of the catcher line making contact. Slightly musical and positive. Quick decay. Should feel rewarding without being overdone.

---

### Catcher — Missed Word
**File:** `sfx_catcher_miss.wav`
**Duration:** ~200ms

**Prompt:** A brief, disappointed tone around 200 milliseconds. A soft descending two-note figure — slightly flat and muffled. Like a gentle failure sound — not harsh, just acknowledging a miss. Slightly dull and slightly deflating.

---

### Catcher — Wrong Word Caught
**File:** `sfx_catcher_wrong.wav`
**Duration:** ~300ms

**Prompt:** A clear error sound around 300 milliseconds. A short, buzzing dissonant tone — slightly harsh, slightly electronic. A brief low-pitched buzz followed by a descending pitch drop. Distinct from the miss sound — more active and more wrong. Should communicate a mistake was made.

---

### Catcher — Life Lost
**File:** `sfx_catcher_life_lost.wav`
**Duration:** ~600ms

**Prompt:** A more significant failure sound lasting about 600 milliseconds. A descending chromatic figure — three or four notes falling in pitch — played on something slightly mournful, like a muted horn or low strings. A brief, slightly dramatic fall. Not game-over level — just a setback. A sense of something slipping away.

---

### Catcher — Wave Complete
**File:** `sfx_catcher_wave_complete.wav`
**Duration:** ~1 second

**Prompt:** A brief, uplifting wave-completion sting lasting about 1 second. A two or three note ascending figure — clean, bright, slightly triumphant. Like a small fanfare. Short but satisfying. Signals progress and transitions to the next phase.

---

### Catcher — Key Evidence Word Appears
**File:** `sfx_catcher_key_spawn.wav`
**Duration:** ~300ms

**Prompt:** A more distinctive spawn sound lasting about 300 milliseconds. Different from the regular word spawn — slightly longer, slightly more musical. A bright, slightly resonant chime with a subtle shimmer — the word announces itself as important. Should sound special without being overwhelming.

---

### Catcher — Key Evidence Word Caught
**File:** `sfx_catcher_key_catch.wav`
**Duration:** ~1.5 seconds

**Prompt:** A significant catch sting lasting about 1.5 seconds. Begins with the standard catch sound, then immediately escalates — a brief, rising musical figure resolves into a warm, resonant chord. Four notes, ascending and warm. A satisfied reverb tail. This catch matters more than the others.

---

### Catcher — Game Won
**File:** `sfx_catcher_win.wav`
**Duration:** ~2.5 seconds

**Prompt:** A victory fanfare lasting about 2.5 seconds. A bright, ascending sequence of five or six notes resolving on a held major chord. Slightly orchestral — strings or brass flavor. Warm and triumphant but not overwhelming. Triumphant but thoughtful — this is a mystery game, not an arcade game.

---

### Catcher — Game Lost
**File:** `sfx_catcher_lose.wav`
**Duration:** ~2 seconds

**Prompt:** A failure theme lasting about 2 seconds. A descending four-note sequence — slow, mournful, in a minor key. Slightly orchestral. The notes fall and resolve on a low, unresolved chord — no clean ending. A brief, fading resonance. The investigation suffers.

---

### Maze — Player Footsteps
**File:** `sfx_maze_footsteps.wav`
**Duration:** Looping (1 step cycle ~650ms)

**Prompt:** A looping footstep sound for movement within the maze — footsteps on a surface that sounds slightly unreal. Each step has a slightly hollow, resonant quality — slightly too echoey for the visual environment. This is a metaphorical space — the footsteps should feel real but slightly wrong. About 1.5 steps per second at normal walking pace. Slight variation in timbre to avoid exact looping.

---

### Maze — Correct Path Chosen
**File:** `sfx_maze_correct.wav`
**Duration:** ~300ms

**Prompt:** A brief positive tone around 300 milliseconds. A soft, warm two-note ascending figure — gentle and affirming. Like a quiet "yes." Should feel like confirmation of truth — a small but meaningful acknowledgment.

---

### Maze — Wrong Path / Dead End
**File:** `sfx_maze_wrong.wav`
**Duration:** ~300ms

**Prompt:** A brief, soft rejection sound around 300 milliseconds. A low, slightly hollow single note that descends slightly. A gentle but clear "no." A brief wall impact texture — a soft bump against an invisible barrier. Not harsh — just clear. False statement, wrong direction.

---

### Maze — Monster Appears
**File:** `sfx_maze_monster_appear.wav`
**Duration:** ~2 seconds

**Prompt:** A sudden, threatening creature-arrival sound lasting about 2 seconds. A sharp, low growl or distorted breath — inhuman but not cartoonish. A rising tension swell beneath it — low strings or a dissonant synth pad building quickly. A brief, scattered percussion element — like something large moving. Communicates immediate, physical danger. Not a jump scare — a warning.

---

### Maze — Monster Catches Player
**File:** `sfx_maze_caught.wav`
**Duration:** ~1.5 seconds

**Prompt:** An immediate, jarring impact — like a collision. A sharp, dissonant hit followed by a rising, distorted wail that quickly cuts to silence. The silence should be sudden and total — a reset moment. The impact should feel physical and surprising even when expected. Unsettling, visceral, final for this attempt.

---

### Maze — Won
**File:** `sfx_maze_win.wav`
**Duration:** ~3 seconds

**Prompt:** A significant victory theme lasting about 3 seconds. More orchestral and substantial than the catcher win — this maze contains Margaret's truth. The theme should have a slightly bittersweet quality — major key but with a complexity beneath it. An ascending string figure resolving into a warm held chord. The resolution should feel earned and emotional — not just a game win, but a narrative breakthrough.

---

## TAPE SYSTEM

### Tape Collected
**File:** `sfx_tape_collect.wav`
**Duration:** ~800ms

**Prompt:** A two-part sound lasting about 800 milliseconds. The physical pickup — a slight plastic-and-metal sound as the recorder is grasped and secured. Then the collection chime: a distinctive, slightly eerie two-note figure — descending slightly. Not the standard success chime — tape discoveries have their own identity. Should feel like securing something important and slightly dangerous. Each tape collected feels like finding a voice.

---

### Tape Locked — Cannot Play
**File:** `sfx_tape_locked.wav`
**Duration:** ~500ms

**Prompt:** A brief, locked-out rejection sound lasting about 500 milliseconds. A mechanical click of a play button that doesn't engage — a dull, flat click. Then immediately: a soft, low tone descending — a "not yet" sound. Not harsh or alarming. Just a door that isn't open yet. Slightly hollow.

---

### Tape Unlocked
**File:** `sfx_tape_unlock.wav`
**Duration:** ~800ms

**Prompt:** A brief, meaningful unlock sound lasting about 800 milliseconds. A slight mechanical release — as if a constraint has been removed. Then: a single, clear ascending note — bright and slightly chime-like. A warm resonance tail. The unlock should feel like a new voice being permitted to speak.

---

### Tape Play Starts
**File:** `sfx_tape_play_start.wav`
**Duration:** ~1.5 seconds

**Prompt:** A precise, mechanical tape-play initiation lasting about 1.5 seconds. The firm press of a large, physical play button — a satisfying, weighted click. Then: the tape mechanism engages — the sound of magnetic tape beginning to move across the playback head. A brief, initial burst of tape leader hiss. The reels begin spinning — a quiet mechanical whirr. Then: the recording begins. The entire sequence has the satisfying weight of analog technology.

---

### Tape Playing — Background Ambience Loop
**File:** `amb_tape_playing.wav`
**Duration:** Looping (plays for duration of tape)

**Prompt:** A subtle, looping background texture for the duration of tape playback. The faint mechanical presence of a running tape recorder: a very quiet reel-spin hum, intermittent slight wow-and-flutter in the tape movement, and a constant, low-level magnetic tape hiss beneath everything — warm, analog, slightly imperfect. Should sit far beneath the foreground audio — barely perceptible — but its absence would be noticed.

---

### Tape Ends
**File:** `sfx_tape_end.wav`
**Duration:** ~1.5 seconds

**Prompt:** A mechanical tape-end sequence lasting about 1.5 seconds. The recording finishes — a final moment of tape hiss. Then: the tape runs out — a faint leader-tape sound. The mechanism detects the end and auto-stops: a soft, firm mechanical click as the play button releases. The reels slow and stop — the whirr fades. Silence. The silence after a tape ends should feel significant.

---

### Climax Tape — Narrator Distortion
**File:** `sfx_tape_distortion_climax.wav`
**Duration:** 10–15 seconds

**Prompt:** A progressive distortion sequence that builds over 10–15 seconds. The tape begins normally. Then: subtle artifacts begin — very occasional pitch wobble, a brief dropout of audio, a slight phase shift. These become more frequent. Beneath the recording: a low, resonant hum begins — not part of the recording. The tape hiss becomes more active. A brief, backwards-sounding audio artifact. The narrator's voice on the tape warbles and stretches on certain words. Toward the end: a jarring, loud distortion burst — as if the playback mechanism is fighting the content. The sound of a recording that doesn't want to be heard.

---

## INTERVIEW SYSTEM

### Interview Started
**File:** `sfx_interview_start.wav`
**Duration:** ~1 second

**Prompt:** A brief scene-transition sound lasting about 1 second. A soft, slightly formal tone — like a meeting beginning. A quiet, neutral ascending two-note figure. Not threatening, not warm — professional distance. The sound of a conversation that is structured and purposeful.

---

### Suspect Hesitates
**File:** `sfx_interview_hesitate.wav`
**Duration:** ~500ms

**Prompt:** A very brief, tension-building sound lasting about 500 milliseconds. A barely-perceptible low string tension note — almost subliminal. Just enough to register that something is happening beneath the surface. The sound of someone choosing their words. A quiet, slightly held note that doesn't resolve.

---

### Suspect Reluctant
**File:** `sfx_interview_reluctant.wav`
**Duration:** ~600ms

**Prompt:** A short, slightly confrontational sound lasting about 600 milliseconds. A brief, low, defensive tone — like a door being partially closed. A slight dissonance building and resolving incompletely. Not aggressive — resistant. The sound of someone not wanting to say something.

---

### Evidence Shown to Suspect
**File:** `sfx_interview_evidence_show.wav`
**Duration:** ~800ms

**Prompt:** A formal presentation sound lasting about 800 milliseconds. The sound of an object being placed on a surface — deliberate and measured. A brief, clean impact. Then: a short, slightly tense two-note figure — the sound of something being made visible that was meant to stay hidden.

---

### Suspect Reacts — Surprise
**File:** `sfx_interview_react_surprise.wav`
**Duration:** ~400ms

**Prompt:** A brief sharp intake of breath followed by a quick, ascending tone — 400 ms. Should feel like an involuntary, physical reaction to unexpected information.

---

### Suspect Reacts — Fear
**File:** `sfx_interview_react_fear.wav`
**Duration:** ~500ms

**Prompt:** A low, descending tone with a slight tremor — wavering, soft — 500 ms. Should feel like composure faltering very slightly. Understated but unmistakable.

---

### Suspect Reacts — Anger
**File:** `sfx_interview_react_anger.wav`
**Duration:** ~400ms

**Prompt:** A percussive, slightly dissonant short chord — firm and impactful — 400 ms. Should feel like someone slamming a wall up. Brief and sharp.

---

### Suspect Reacts — Denial
**File:** `sfx_interview_react_denial.wav`
**Duration:** ~300ms

**Prompt:** A flat, dismissive two-note descending figure — closed and final — 300 ms. Should feel like a door being shut. Controlled, cold, unconvincing.

---

### Contradiction Discovered
**File:** `sfx_contradiction_found.wav`
**Duration:** ~2 seconds

**Prompt:** A sharp, dramatic revelation sting lasting about 2 seconds. Begins with a single, high, sharp note — like a struck bell — immediate and clear. Then: a rapid, descending musical figure — almost like unraveling — resolving into a brief, dissonant held chord. The chord decays with a slight reverb tail. Should feel like a thread being pulled and a larger structure shifting. Clear, precise, significant.

---

### Interview Ended
**File:** `sfx_interview_end.wav`
**Duration:** ~500ms

**Prompt:** A brief, neutral closing sound lasting about 500 milliseconds. A soft, descending two-note figure — lower than the opening sound. A slight ambience return to the investigation space. Neither success nor failure — just conclusion. The sound of a conversation being filed away.

---

## INVESTIGATION STATUS / NOTEBOOK

### Evidence Collected
**File:** `sfx_evidence_collect.wav`
**Duration:** ~500ms

**Prompt:** A clean, satisfying evidence-collection sound lasting about 500 milliseconds. A brief, physical handling sound — the object being secured — followed by a two-note ascending chime. The chime is warmer and slightly lower than a standard UI success sound. This is meaningful, not routine. Clean, confident, resolving.

---

### Anomaly — Narrator "I" Slip
**File:** `sfx_anomaly_narrator_slip.wav`
**Duration:** ~1 second

**Prompt:** A digital glitch sound lasting about 1 second. A brief audio corruption artifact — the sound of a recording skipping, warping, or stuttering. A single word replayed rapidly then cut. A slight pitch shift up or down. Then silence. The sound of a carefully maintained façade developing a crack. Quick, precise, slightly alarming. Should feel like a broadcast signal losing sync for a fraction of a second.

---

### Awareness Meter Increases
**File:** `sfx_awareness_increase.wav`
**Duration:** ~1 second

**Prompt:** A very subtle, barely-perceptible ambient shift lasting about 1 second. A very quiet, slightly higher-than-normal background hum — as if the room's frequency has shifted upward by a small amount. Not a sound effect — more of a texture change. Should be nearly subliminal but cumulative. The player should only notice it after several increases.

---

### Awareness Warning Threshold
**File:** `sfx_awareness_warning.wav`
**Duration:** ~2 seconds

**Prompt:** A more pronounced, threatening tone lasting about 2 seconds. A low, resonant alarm tone — not a standard alarm, but a deep, harmonic warning. A frequency that sits in the chest. A single sustained note in a slightly dissonant pitch relationship to the ambient room tone — creating interference. A brief, urgent pulse beneath it. Should feel like the room itself has noticed you. Not a jump scare — a slow, gathering dread.

---

### Narrator Distortion
**File:** `sfx_narrator_distortion.wav`
**Duration:** 2–3 seconds

**Prompt:** A progressive distortion event lasting 2–3 seconds. Begins with normal audio, then — a low frequency interference begins building. The signal degrades: pitch shifts slightly, a flanging or phasing effect begins, a rising harmonic dissonance. At the peak: a jarring, full distortion burst — loud, complex, briefly overwhelming. Then it settles back — but not quite to normal. Something has changed. The narrator's voice has shown its seams.

---

## UI INTERACTIONS

### Accusation Button Click
**File:** `ui_btn_accuse.wav`
**Duration:** ~1 second (300ms click + 700ms sting)

**Prompt:** A deliberate, weighty click lasting about 300 milliseconds followed by a brief dramatic sting of about 700 ms. The click itself is deep and firm — a heavy mechanical press. The sting: a brief, low, serious tone — a single bass note with a slight orchestral swell beneath it, immediately cut off after 700 ms. Should feel like the moment a verdict is about to be delivered. Irreversible. Significant. Slightly frightening.

---

### Wrong Accusation — Rejection
**File:** `sfx_accusation_wrong.wav`
**Duration:** ~1.5 seconds

**Prompt:** A clear rejection tone lasting about 1.5 seconds. A firm, dissonant buzz — not electronic, slightly orchestral. Two notes in strong dissonance, medium volume, then descending and fading. The sound of being wrong when it matters. Not cruel — just final and incorrect. A slight hollow echo at the end.

---

### Accusation Blocked — Missing Evidence
**File:** `sfx_accusation_blocked.wav`
**Duration:** ~600ms

**Prompt:** A brief, locked-out sound lasting about 600 milliseconds. A soft, low impact — like pressing on something that won't yield — followed by a descending, muffled tone. Not as harsh as the wrong accusation — this is a "not yet" rather than a "wrong." Gently firm.

---

### Pause Menu Opens
**File:** `ui_pause_open.wav`
**Duration:** ~300ms

**Prompt:** A soft, clear pause-state sound lasting about 300 milliseconds. A brief, slightly warm tone — a single note with a gentle decay. The ambient room sounds seem to hold — the pause should feel like time has been suspended. Calm and neutral — a moment outside the game.

---

### Pause Menu Closes — Resume
**File:** `ui_pause_close.wav`
**Duration:** ~300ms

**Prompt:** The reverse of the pause sound — a brief, slightly brighter tone. Room ambience returns. A sense of resumption. Time begins again.

---

### Save Successful
**File:** `ui_save_success.wav`
**Duration:** ~500ms

**Prompt:** A clean, brief success chime lasting about 500 milliseconds. Two notes — ascending — with a slight warm reverb. Reliable and satisfying. The sound of a record being kept. Understated but confirmatory.

---

### Load Successful
**File:** `ui_load_success.wav`
**Duration:** ~700ms

**Prompt:** A slightly longer success tone lasting about 700 milliseconds. Two or three ascending notes — slightly more elaborate than the save chime — reflecting the retrieval of a full game state. Warm and slightly reassuring — returning to a known place.

---

## CASE PROGRESSION & CLIMAX

### Cellar Unlocked
**File:** `sfx_cellar_unlock.wav`
**Duration:** ~1.5 seconds

**Prompt:** A meaningful unlock sound lasting about 1.5 seconds. A heavy iron bolt drawing back — a deep, grinding metal sound — followed by a door being unsealed. A brief, resonant boom as the lock releases. Then: a brief, significant musical tone — a short ascending figure resolving on a held chord. Not triumphant — this is the cellar. The player should feel they've earned access to something they might have preferred to remain locked.

---

### Margaret's Room Unsealed
**File:** `sfx_margarets_room_unseal.wav`
**Duration:** ~3 seconds

**Prompt:** A supernatural unsealing lasting about 3 seconds. The cold, buzzing tone that was present when approaching the room dissolves — a harmonic resolution, dissonance resolving to consonance. A brief, warm sound as the door becomes normal — a slight sigh, as if the room itself has exhaled. Then a quiet, slightly sad tone — a two-note descending figure. The room is open now, but what happened here cannot be undone.

---

### James Outburst
**File:** `sfx_james_outburst.wav`
**Duration:** ~800ms

**Prompt:** A brief, emotional reaction sound lasting about 800 milliseconds. A sharp percussion impact — the sound of someone standing abruptly or striking a surface — a quick, startling thud. Then a brief, tense held tone — the room reacting to raised emotion. The atmosphere tightens. Not a musical sting — a physical reaction sound with tension ambience beneath it.

---

### Doors Closing — Household Closes Ranks
**File:** `sfx_door_close_ominous.wav`
**Duration:** ~1 second

**Prompt:** A distant door closing sound — heard from another room — with a slightly ominous quality. The click of a lock engaging. Then a brief, quiet tone — a single descending note — confirming that access has been lost. Each door closing should feel like a wall going up.

---

## ENDINGS

### Correct Accusation — Case Closed
**File:** `mus_case_closed.wav`
**Duration:** ~4 seconds

**Prompt:** A case-closed fanfare lasting about 4 seconds. Begins with a moment of silence — the weight of a correct answer. Then: a full, satisfying resolution theme — orchestral, major key, four or five notes ascending into a warm, held chord. A slight trumpet or brass quality — official and confirmed. Then the chord blooms and sustains. Not euphoric — the truth is complicated — but resolved and complete.

---

### Wrong Accusation — Ending
**File:** `sfx_accusation_wrong_ending.wav`
**Duration:** ~2 seconds

**Prompt:** A failure sound lasting about 2 seconds. A brief, full dissonant chord — firm and irrefutable. A descending figure afterward — the case unraveling. A slight, mournful quality — someone innocent has been accused, or the guilty party has been missed. Not as harsh as a game-over — but final and costly.

---

### Awareness Maximum — Game Over
**File:** `mus_game_over.wav`
**Duration:** ~3 seconds

**Prompt:** A game-over theme lasting about 3 seconds. A slow, descending sequence in a minor key — five or six notes falling from high to low, resolving on a held low chord. Slightly orchestral — strings or piano. Defeated and final. A slight dissonance in the final chord — the truth remains unknown. A reverb tail that fades into the darkness.

---

### Seal the Wall Ending
**File:** `sfx_ending_seal_wall.wav`
**Duration:** 5–6 seconds

**Prompt:** A supernatural sealing sequence lasting 5–6 seconds. A low, resonant hum begins — building in intensity. A sense of stone and earth responding to intention. A crystalline, high-frequency shimmer builds above the low hum. Then — with a decisive, physical impact — the seal is made: a deep, resonant boom, followed by the harmonic of stone resonating. The breathing stops. The wall goes silent. A final, sustained tone resolves. Whatever was behind the wall has been acknowledged and contained. Slightly mournful.

---

### Destroy Tapes Ending
**File:** `sfx_ending_destroy_tapes.wav`
**Duration:** 4–5 seconds

**Prompt:** A tape-destruction sequence lasting 4–5 seconds. Magnetic tape being pulled from a cassette — the thin, ribbon-like sound of tape unspooling. A brief, bright burn sound — the hiss and crackle of magnetic tape catching fire. Beneath the fire: a faint ghost of voices — barely perceptible — as the recordings are destroyed. Then silence. The fire fades. Nothing remains. The sound of truth being unmade.

---

### Escape Manor — Escape Theme
**File:** `mus_escape_theme.wav`
**Duration:** Looping (plays until escape ends)

**Prompt:** An urgent, rhythmic escape theme beginning immediately. A driving, medium-tempo percussion element — not a standard drum kit, but a propulsive, physical rhythm — footsteps and heartbeats blended. Beneath it: a building string tension line, ascending with urgency. The theme should feel like controlled panic — purposeful movement, high stakes. No melodic resolution — the theme continues and intensifies until the escape succeeds or fails.

---

### Escape — Tape Pickup
**File:** `sfx_escape_tape_pickup.wav`
**Duration:** ~400ms

**Prompt:** A faster, more urgent version of the tape pickup sound — same basic character (plastic, mechanical, slight chime) but compressed into 400 milliseconds. Quick grab, brief confirmation tone. No time to linger.

---

### Escape — Exit Door Reached
**File:** `sfx_escape_exit_door.wav`
**Duration:** ~3 seconds

**Prompt:** The heavy front door opening — but this time followed immediately by a rush of outdoor air — cold, moving, free. The sound of outside. A brief moment of open space acoustic — enormous compared to the manor's enclosed rooms. A single, held ascending note — freedom. Then the escape theme fades as the sequence resolves.

---

### Escape — Success
**File:** `mus_escape_success.wav`
**Duration:** ~3 seconds

**Prompt:** A release-of-tension success theme lasting about 3 seconds. The escape theme's urgency suddenly drops away — a moment of silence, then a warm, resolving chord. A brief, exhaled breath sound. The resolution is major key and warm but tinged with relief rather than triumph — escaping is surviving, not winning. A sustained, fading chord.

---

### Escape — Failed
**File:** `mus_escape_fail.wav`
**Duration:** ~2.5 seconds

**Prompt:** The escape theme cuts abruptly — a jarring, immediate stop. Then: a brief, sharp impact sound — caught. A dissonant, distorted sting. Then the game-over theme takes over — the slow descending figure, the minor key resolution. The manor has won.

---

### Leave Manor Ending — Peaceful Departure
**File:** `sfx_ending_leave_manor.wav`
**Duration:** ~3 seconds

**Prompt:** A gentle, understated departure sequence lasting about 3 seconds. Footsteps on the gravel path — moving away. The gate opens — the same gate creak — but more resolving this time. The outdoor ambience of the grounds fills the space — wind, distant nature, open air. A simple, quiet two-note descending figure — leave-taking. The manor's ambient hum fades as distance grows. Melancholy, unresolved, but no longer threatening.

---

### End Credits Music
**File:** `mus_end_credits.wav`
**Duration:** 2–3 minutes

**Prompt:** A full end-credits piece, 2–3 minutes, designed to loop or complete within the credits sequence. In a minor key with occasional major resolution — reflecting the ambiguous, bittersweet nature of the investigation. A piano-led theme with subtle strings. The melody should be simple and memorable — four to six notes — recurring in different arrangements. The opening bars should feel like reflection and closure. A central section builds slightly in complexity, acknowledging what was experienced. The final section returns to the simple piano theme, quieter, ending on an unresolved or softly resolved chord — the truth has been found, but the story doesn't entirely close. Acoustic, intimate, slightly melancholy.

---
