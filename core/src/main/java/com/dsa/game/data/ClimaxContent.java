package com.dsa.game.data;

public class ClimaxContent {

    public static final int TAPE_8_AWARENESS_COST = 5;
    public static final int STANDARD_TAPE_AWARENESS_COST = 4;

    public static final String TAPE_8_CLIMAX = getClimaxText(0);

    public static String getClimaxText(int anomalyCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== THE WALL OPENS ===\n\n");

        if (anomalyCount >= 5) {
            sb.append("You've felt it building -- the breathing wall, the scratched initials, the cold spots. " +
                "Every anomaly pointed here. And now, as the tape ends, the truth rips open.\n\n");
        } else if (anomalyCount >= 3) {
            sb.append("The strange things you've noticed -- the impossible cold, the scratched names -- " +
                "they weren't random. They were warnings. And now the warning is over.\n\n");
        } else if (anomalyCount >= 1) {
            sb.append("You sensed something was wrong in this house. Something beyond murder. " +
                "Now you're about to find out what.\n\n");
        }

        sb.append("As the tape ends, you hear it -- a grinding sound from deep within the manor. " +
            "Stone against stone. The recording has triggered something.\n\n" +
            "The narrator's voice trembles: \"You've found it. The wall in the cellar. " +
            "The thing behind it. I remember now... I found it too.\"\n\n" +
            "The house shudders. Dust falls from the ceiling.\n\n" +
            "\"Harold Vance sealed his own business partner behind that wall in 1957. " +
            "Ashford had threatened to expose him -- to ruin everything Harold had built. " +
            "So Harold buried him alive and built a fortune on the silence.\"\n\n" +
            "A cold wind sweeps through the room, though every window is shut.\n\n" +
            "\"Thirty years behind a wall. What do you think that does to a man? " +
            "What comes out is not what went in. The Entity doesn't sleep. It waits. " +
            "It listens. And when someone gets too close to the truth...\"\n\n" +
            "The lights flicker violently.\n\n" +
            "\"Harold sealed a man behind a wall. His own son sealed Harold in a cellar. " +
            "The Vances don't solve their problems -- they bury them. And what's buried " +
            "always finds a way back.\"\n\n" +
            "\"Solve the murder. Find the killers. But do it quietly, detective. " +
            "Because if the Entity fully wakes... none of us leave Vance Manor.\"\n\n" +
            "[The recording dissolves into static. The grinding stops. " +
            "Somewhere in the cellar, a wall stands slightly ajar.]");

        return sb.toString();
    }

    public static String getTape8Climax(int anomalyCount) {
        return getClimaxText(anomalyCount);
    }

    public static final String TAPE_8_CELLAR_PREFIX =
        "[NOTE: This recording was found hidden behind the wine rack in the cellar. " +
        "It appears to have been left by a hidden recorder placed to capture what happened " +
        "that night.]\n\n";

    public static final String ENDING_SEAL_WALL =
        "=== SEAL THE WALL ===\n\n" +
        "You descend into the cellar one final time. The wall stands ajar, pulsing with " +
        "that terrible warmth. Behind it, darkness deeper than darkness -- and something " +
        "that watches.\n\n" +
        "You brought the mortar. The bricks. Everything you need.\n\n" +
        "Brick by brick, you seal the wall shut. The Entity screams -- not with sound, " +
        "but with a pressure in your skull that makes your vision blur and your nose bleed. " +
        "It doesn't want to be forgotten. It feeds on being known.\n\n" +
        "But you deny it. Each brick is a silence. Each layer of mortar is a forgotten truth.\n\n" +
        "When you're done, the cellar is just a cellar again. Cold, damp, ordinary.\n\n" +
        "The household watches you leave from the windows. They understand what you've done. " +
        "Harold's murder will go officially unsolved. James and Daniel will live with their " +
        "guilt, but they'll live. And the Entity will sleep -- for now.\n\n" +
        "You chose to be the guardian of a terrible secret.";

    public static final String ENDING_ESCAPE =
        "=== ESCAPE THE MANOR ===\n\n" +
        "You run. Through the parlor, past the entrance, out the front door into the night air.\n\n" +
        "Behind you, every light in Vance Manor goes out simultaneously. The building seems to " +
        "exhale, like a predator losing its prey.\n\n" +
        "You don't stop until you reach the road. Your car is where you left it. Your hands " +
        "shake so badly it takes three tries to start the engine.\n\n" +
        "In the rearview mirror, Vance Manor shrinks into the darkness. A light flickers in " +
        "the cellar window -- then goes out.\n\n" +
        "You survived. Harold's killers go free. The Entity remains. And somewhere in your " +
        "notes, enough fragments of truth remain that someday, someone else will come asking " +
        "questions.\n\n" +
        "You escaped with your life. You'll never escape the knowledge of what's behind that wall.";

    public static final String ENDING_DESTROY_TAPES =
        "=== DESTROY THE TAPES ===\n\n" +
        "One by one, you feed the recordings into the study fireplace. The tape melts " +
        "and curls, releasing a chemical smell and thin wisps of black smoke.\n\n" +
        "With each tape destroyed, the house grows quieter. The floorboards stop creaking. " +
        "The shadows recede. The Entity's connection to the world weakens -- its story, " +
        "its memory, its proof of existence, all burning.\n\n" +
        "The last tape -- the cellar recording -- fights you. The recorder falls from your " +
        "hands twice. The fire dims. But you persist, and the flames finally consume it.\n\n" +
        "A sound like a long sigh passes through the manor. Every door opens at once. " +
        "Every window unlatches. The house releases its grip.\n\n" +
        "Without the tapes, there is no evidence of the Entity. Without evidence, there is " +
        "no investigation. Without investigation, there is no awareness. The cycle is broken.\n\n" +
        "Harold's murder might still be solved through conventional means. But the deeper " +
        "horror of Vance Manor dies with these tapes. You made sure of that.";
}
