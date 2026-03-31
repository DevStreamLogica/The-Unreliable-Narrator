package com.dsa.game.data;

import com.dsa.game.state.Contradiction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NarratorText {

    public enum Mood { HOPEFUL, CONFUSED, ANXIOUS, FRANTIC }

    private static final String[][] WARNINGS = {
        {
            "A floorboard creaks somewhere behind you. Probably nothing.",
            "You notice a shadow shift in the corner of your eye. Just the light changing.",
            "The house settles with a quiet groan. Old houses do that.",
            "A faint draft brushes past. Someone left a window open, perhaps."
        },
        {
            "Wait -- did you already examine that? Your memory feels slippery here.",
            "The hallway looks different than you remember. Have the paintings moved?",
            "You could swear this door was open a moment ago.",
            "Your thoughts feel sluggish. When did you last sleep?"
        },
        {
            "Whispers stop the moment you enter the room. They know you're listening.",
            "Someone is watching from the upstairs window. You can feel it.",
            "The air feels thick and wrong. Something is very off in this house.",
            "Your hands are shaking. The evidence is pointing somewhere terrible."
        },
        {
            "GET OUT. You need to get out NOW. But you can't, can you? Not until you know.",
            "Doors are locking behind you. Footsteps in the walls. They're closing in.",
            "Your vision swims. The house is breathing. THE HOUSE IS BREATHING.",
            "Time is running out. You can hear them gathering. Whispering your name."
        }
    };

    private static final String[][] ENVIRONMENTAL_CUES = {
        {
            "Dust motes drift lazily in a beam of pale sunlight.",
            "The grandfather clock ticks steadily in the hall.",
            "A bird calls outside the window. The world beyond the manor continues.",
            "The fire crackles in the grate. The manor is quiet."
        },
        {
            "The shadows seem longer than they should be at this hour.",
            "The clock seems to be running backwards. No -- you're imagining things.",
            "The smell of old paper and something metallic hangs in the air.",
            "This would be a pleasant house, under other circumstances."
        },
        {
            "The lights flicker. When they steady, every shadow seems deeper.",
            "A cold spot passes through the room. Your breath mists briefly.",
            "The portrait eyes follow you. Every single one.",
            "Something scratches inside the walls. Rats, you tell yourself. Just rats."
        },
        {
            "The lights die. For three terrible seconds, you are in absolute darkness.",
            "Blood-red light seeps under the cellar door. It pulses like a heartbeat.",
            "The walls are wet. The walls shouldn't be wet.",
            "Every mirror you pass shows someone standing behind you. But when you turn -- nothing."
        }
    };

    private static final String[][] COMMENTARY_PREFIXES = {
        {
            "You note with interest: ",
            "A promising lead -- ",
            "Something pulls your attention: ",
            "Carefully, you observe: "
        },
        {
            "Through the fog of exhaustion, you notice: ",
            "Something doesn't add up -- ",
            "You squint at the details: ",
            "Wait... is this right? "
        },
        {
            "With trembling hands, you uncover: ",
            "God help you -- ",
            "You almost wish you hadn't found this: ",
            "The truth is getting darker: "
        },
        {
            "NO NO NO -- ",
            "It all makes horrible sense now: ",
            "You can barely read through the panic: ",
            "With the last of your composure: "
        }
    };

    private static final String[] ATMOSPHERIC_EVENTS = {
        "A door slams shut somewhere deep in the house. No one is there when you check.",
        "The temperature drops sharply. Your breath fogs. Then, just as suddenly, the warmth returns.",
        "You hear a child's laughter from somewhere deeper in the manor. There is no one here.",
        "Every candle in the room gutters simultaneously, as if something large just passed through.",
        "A wet footprint appears on the floor ahead of you. Then another. Leading toward the cellar.",
        "The grandfather clock strikes thirteen. You count twice to be sure."
    };

    private static final String[] MILD_DISTORTIONS = {
        "The letter opener was clearly the murder weapon -- it was right there on the desk, covered in blood.",
        "No one left the house that night. Every door was locked, every window latched. The boots are irrelevant.",
        "That torn letter in the ashes? Just old correspondence. Nothing to do with the will.",
        "Harold died after midnight, closer to dawn. The household was asleep for hours before anyone noticed.",
        // Atmospheric red herrings: no evidence directly contradicts these, so they have
        // no entry in DISTORTION_CONTRADICTIONS. They misdirect without triggering discovery.
        "Margaret was seen near the study that night. Several witnesses confirm it.",
        "The cellar has always been sealed. There's nothing down there but wine and dust."
    };

    private static final String[] SEVERE_DISTORTIONS = {
        "You already solved this, didn't you? Margaret did it. The evidence is overwhelming. Why are you still looking?",
        "The tapes are lying to you. Harold was paranoid -- everyone knew it. His recordings prove nothing.",
        "James loved his father. LOVED him. A son doesn't kill his father over money. That's not how families work.",
        "You're seeing patterns that aren't there. Scratches on walls? Cold spots? You need sleep, detective.",
        "Everyone here is trying to HELP you. Stop treating them like suspects. They're grieving."
    };

    private static final Map<String, Contradiction> DISTORTION_CONTRADICTIONS;
    static {
        Map<String, Contradiction> m = new HashMap<>();
        m.put("letter opener was clearly the murder weapon", Contradiction.NARRATOR_WEAPON);
        m.put("No one left the house that night", Contradiction.NARRATOR_BOOTS);
        m.put("torn letter in the ashes? Just old correspondence", Contradiction.NARRATOR_LETTER);
        m.put("Harold died after midnight", Contradiction.NARRATOR_TIME);
        DISTORTION_CONTRADICTIONS = Collections.unmodifiableMap(m);
    }

    // --- Channeling: Narrator channels suspect memories ---

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
        "[The presence fractures. Another voice breaks through.]"
    };

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

    private static final String[] CHANNELING_BLEED_THROUGH = {
        "[The narrator's own voice breaks through: \"How do I know what they said? How can I possibly know this?\"]",
        "[The narrator surfaces: \"This feels familiar. Like I've heard these words before. But that's impossible.\"]",
        "[The narrator's voice returns briefly: \"Why does it feel like I'm remembering this instead of hearing it?\"]",
        "[The narrator slips through: \"For a moment I could see the room. Smell the whisky. How?\"]"
    };

    public static final String CHANNELING_MEMORY_FADE =
        "[The presence recedes.]\n\n" +
        "\"I'm losing them. The words won't hold. I can't reach any further.\"";

    public static final String CHANNELING_MEMORY_FRAGMENT =
        "[The voice wavers, breaking apart.]\n\n" +
        "\"The memory is fragmenting. I can barely hold onto what they said...\"\n\n" +
        "[The connection shatters.]";

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

    // --- Hold resistance: narrator pushes back when player lingers on hidden items ---

    private static final String[][] HOLD_RESISTANCE = {
        // HOPEFUL
        {
            "\"I wouldn't bother with that. The answers are elsewhere.\"",
            "\"There's nothing useful there -- trust me.\"",
            "\"Perhaps try a different direction.\""
        },
        // CONFUSED
        {
            "\"I... don't think you need to look there.\"",
            "\"Why are you stopping? There's nothing there.\"",
            "\"That spot feels wrong. Please, just move on.\""
        },
        // ANXIOUS
        {
            "\"Don't. Please don't look there.\"",
            "\"There is NOTHING there. Why won't you listen?\"",
            "\"Move away. Right now. Please.\""
        },
        // FRANTIC
        {
            "\"STOP LOOKING THERE.\"",
            "\"YOU DON'T NEED TO SEE THAT. LEAVE IT ALONE.\"",
            "\"I'M WARNING YOU. THERE IS NOTHING THERE.\""
        }
    };

    public static String getRandomHoldResistance(Mood mood) {
        String[] lines = HOLD_RESISTANCE[mood.ordinal()];
        return lines[(int)(Math.random() * lines.length)];
    }

    public static String getChannelingFirstIntro(Mood mood) { return CHANNELING_FIRST_INTRO[mood.ordinal()]; }
    public static String getChannelingReturnIntro(Mood mood) { return CHANNELING_RETURN_INTRO[mood.ordinal()]; }
    public static String[] getChannelingBleedThrough() { return CHANNELING_BLEED_THROUGH; }
    public static String getChannelingMemoryFade() { return CHANNELING_MEMORY_FADE; }
    public static String getChannelingMemoryFragment() { return CHANNELING_MEMORY_FRAGMENT; }
    public static String getChannelingEnd(Mood mood) { return CHANNELING_END[mood.ordinal()]; }

    public static Mood getMoodForAwareness(int awareness) {
        if (awareness < 20) return Mood.HOPEFUL;
        if (awareness < 40) return Mood.CONFUSED;
        if (awareness < 60) return Mood.ANXIOUS;
        return Mood.FRANTIC;
    }

    public static String[] getWarnings(Mood mood) { return WARNINGS[mood.ordinal()]; }
    public static String[] getEnvironmentalCues(Mood mood) { return ENVIRONMENTAL_CUES[mood.ordinal()]; }
    public static String[] getCommentaryPrefixes(Mood mood) { return COMMENTARY_PREFIXES[mood.ordinal()]; }
    public static String[] getAtmosphericEvents() { return ATMOSPHERIC_EVENTS; }
    public static String[] getMildDistortions() { return MILD_DISTORTIONS; }
    public static String[] getSevereDistortions() { return SEVERE_DISTORTIONS; }
    public static Map<String, Contradiction> getDistortionContradictions() { return DISTORTION_CONTRADICTIONS; }
}
