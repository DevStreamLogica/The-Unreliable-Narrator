package com.dsa.game.data;

/**
 * Static data class containing pre-written narrator text for 4 moods.
 * Mood shifts based on awareness level to create escalating tension.
 */
public class NarratorText {

    public enum Mood { HOPEFUL, CONFUSED, ANXIOUS, FRANTIC }

    // --- Warnings (4 per mood) ---

    private static final String[][] WARNINGS = {
        // HOPEFUL (awareness 0-19)
        {
            "A floorboard creaks somewhere behind you. Probably nothing.",
            "You notice a shadow shift in the corner of your eye. Just the light changing.",
            "The house settles with a quiet groan. Old houses do that.",
            "A faint draft brushes past. Someone left a window open, perhaps."
        },
        // CONFUSED (awareness 20-39)
        {
            "Wait -- did you already examine that? Your notes seem... incomplete.",
            "The hallway looks different than you remember. Have the paintings moved?",
            "You could swear this door was open a moment ago.",
            "Your thoughts feel sluggish. When did you last sleep?"
        },
        // ANXIOUS (awareness 40-59)
        {
            "Whispers stop the moment you enter the room. They know you're listening.",
            "Someone is watching from the upstairs window. You can feel it.",
            "The air feels thick and wrong. Something is very off in this house.",
            "Your hands are shaking. The evidence is pointing somewhere terrible."
        },
        // FRANTIC (awareness 60+)
        {
            "GET OUT. You need to get out NOW. But you can't, can you? Not until you know.",
            "Doors are locking behind you. Footsteps in the walls. They're closing in.",
            "Your vision swims. The house is breathing. THE HOUSE IS BREATHING.",
            "Time is running out. You can hear them gathering. Whispering your name."
        }
    };

    // --- Environmental cues (4 per mood) ---

    private static final String[][] ENVIRONMENTAL_CUES = {
        // HOPEFUL
        {
            "Dust motes drift lazily in a beam of pale sunlight.",
            "The grandfather clock ticks steadily. Reassuring, almost.",
            "A bird calls outside the window. The world beyond the manor continues.",
            "The fire crackles warmly. This would be a pleasant house, under other circumstances."
        },
        // CONFUSED
        {
            "The shadows seem longer than they should be at this hour.",
            "A door somewhere in the house opens and closes by itself.",
            "The clock seems to be running backwards. No -- you're imagining things.",
            "The smell of old paper and something metallic hangs in the air."
        },
        // ANXIOUS
        {
            "The lights flicker. When they steady, every shadow seems deeper.",
            "A cold spot passes through the room. Your breath mists briefly.",
            "The portrait eyes follow you. Every single one.",
            "Something scratches inside the walls. Rats, you tell yourself. Just rats."
        },
        // FRANTIC
        {
            "The lights die. For three terrible seconds, you are in absolute darkness.",
            "Blood-red light seeps under the cellar door. It pulses like a heartbeat.",
            "The walls are wet. The walls shouldn't be wet.",
            "Every mirror you pass shows someone standing behind you. But when you turn -- nothing."
        }
    };

    // --- Commentary prefixes (4 per mood) ---

    private static final String[][] COMMENTARY_PREFIXES = {
        // HOPEFUL
        {
            "You note with interest: ",
            "A promising lead -- ",
            "Your detective's instinct says: ",
            "Carefully, you observe: "
        },
        // CONFUSED
        {
            "Through the fog of exhaustion, you notice: ",
            "Something doesn't add up -- ",
            "You squint at the details: ",
            "Wait... is this right? "
        },
        // ANXIOUS
        {
            "With trembling hands, you uncover: ",
            "God help you -- ",
            "You almost wish you hadn't found this: ",
            "The truth is getting darker: "
        },
        // FRANTIC
        {
            "NO NO NO -- ",
            "It all makes horrible sense now: ",
            "You can barely read through the panic: ",
            "With the last of your composure: "
        }
    };

    // --- Atmospheric events (for awareness >= 40) ---

    private static final String[] ATMOSPHERIC_EVENTS = {
        "A door slams shut somewhere deep in the house. No one is there when you check.",
        "The temperature drops sharply. Your breath fogs. Then, just as suddenly, the warmth returns.",
        "You hear a child's laughter from an empty room. The Vances have no children.",
        "Every candle in the room gutters simultaneously, as if something large just passed through.",
        "A wet footprint appears on the floor ahead of you. Then another. Leading toward the cellar.",
        "The grandfather clock strikes thirteen. You count twice to be sure."
    };

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
}
