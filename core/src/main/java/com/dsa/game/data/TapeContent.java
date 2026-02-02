package com.dsa.game.data;

import com.dsa.game.state.Tape;

import java.util.EnumMap;
import java.util.Map;

public class TapeContent {

    private static final Map<Tape, String> TRANSCRIPTS = new EnumMap<>(Tape.class);

    static {
        TRANSCRIPTS.put(Tape.TAPE_ARGUMENT,
            "=== TAPE: Harold & James Argument ===\n\n" +
            "HAROLD: \"I've seen the accounts, James. Don't insult me by denying it.\"\n\n" +
            "JAMES: \"Father, I can explain--\"\n\n" +
            "HAROLD: \"Explain? Explain how fifty thousand pounds vanished into an " +
            "account I've never heard of? I'm changing the will, James. Tomorrow.\"\n\n" +
            "JAMES: \"You can't do that. You wouldn't.\"\n\n" +
            "HAROLD: \"Watch me. And don't think your friend Daniel can help you " +
            "this time. I know about your arrangement.\"\n\n" +
            "[Sound of a door slamming]");

        TRANSCRIPTS.put(Tape.TAPE_PHONE_CALL,
            "=== TAPE: Late Night Phone Call ===\n\n" +
            "UNKNOWN VOICE: \"It has to be tonight. He's changing the will in the morning.\"\n\n" +
            "SECOND VOICE: \"What about the detective? The housekeeper said--\"\n\n" +
            "UNKNOWN: \"The detective won't find anything. We make it look natural. " +
            "Heart attack. He's old enough that no one will question it.\"\n\n" +
            "SECOND: \"And the groundskeeper?\"\n\n" +
            "UNKNOWN: \"Daniel knows to keep quiet. He has as much to lose as we do.\"\n\n" +
            "[Click - call ends]");

        TRANSCRIPTS.put(Tape.TAPE_MARGARET_CONFESSION,
            "=== TAPE: Margaret's Confession ===\n\n" +
            "MARGARET: [Crying] \"I knew something was wrong. James came to my " +
            "room that night, asked me to say I was with him all evening. " +
            "I didn't understand why until morning.\"\n\n" +
            "MARGARET: \"He's my brother. What was I supposed to do? But I saw " +
            "the mud on his shoes, and Daniel was there too, in the hallway, " +
            "at two in the morning. They were both... I can't.\"\n\n" +
            "[Recording cuts off]");

        TRANSCRIPTS.put(Tape.TAPE_DANIEL_MEETING,
            "=== TAPE: Daniel's Secret Meeting ===\n\n" +
            "DANIEL: \"The old man found out about the money. James is panicking.\"\n\n" +
            "UNKNOWN: \"And you? If he talks to the police--\"\n\n" +
            "DANIEL: \"I've been moving the money for James for two years. " +
            "If Harold changes that will, James gets nothing, and I lose my " +
            "cut. We're in this together.\"\n\n" +
            "UNKNOWN: \"What are you going to do?\"\n\n" +
            "DANIEL: \"Whatever James tells me to. Same as always.\"\n\n" +
            "[Footsteps retreating]");

        TRANSCRIPTS.put(Tape.TAPE_WILL_READING,
            "=== TAPE: Will Reading Preview ===\n\n" +
            "SOLICITOR: \"Mr. Vance, I've drafted the changes as you requested. " +
            "James receives nothing. The entire estate goes to Margaret, with " +
            "a trust for the staff.\"\n\n" +
            "HAROLD: \"Good. And the codicil about the groundskeeper?\"\n\n" +
            "SOLICITOR: \"Daniel is to be dismissed immediately, yes. Mr. Vance, " +
            "are you certain? Disinheriting your son--\"\n\n" +
            "HAROLD: \"He's a thief and a liar. The will is to be signed tomorrow " +
            "at nine. Don't be late.\"\n\n" +
            "[Recording ends]");

        TRANSCRIPTS.put(Tape.TAPE_KITCHEN_WHISPERS,
            "=== TAPE: Kitchen Whispers ===\n\n" +
            "ELEANOR: \"I found the powder in the flour tin. Someone's been " +
            "putting sleeping powder in Harold's evening tea.\"\n\n" +
            "REGINALD: \"Keep your voice down. Who else knows?\"\n\n" +
            "ELEANOR: \"No one. But it's been going on for weeks. Small doses. " +
            "Whoever killed him wanted him weak and drowsy first.\"\n\n" +
            "REGINALD: \"Don't tell the detective. If the family finds out " +
            "we knew...\"\n\n" +
            "ELEANOR: \"Reginald, a man is dead.\"\n\n" +
            "[Silence]");

        TRANSCRIPTS.put(Tape.TAPE_CELLAR_NOISES,
            "=== TAPE: Cellar Recording ===\n\n" +
            "[Sound of something being dragged]\n\n" +
            "DANIEL: \"Help me with this. We need to move it before morning.\"\n\n" +
            "JAMES: \"This wasn't the plan. You said it would look natural!\"\n\n" +
            "DANIEL: \"Plans change. Now grab the other end and stop whining.\"\n\n" +
            "JAMES: \"There's blood on my sleeve. God, there's blood--\"\n\n" +
            "DANIEL: \"Burn the shirt. Use the fireplace in the study. " +
            "I'll clean up down here.\"\n\n" +
            "[Scraping sounds continue]");
    }

    public static String getTranscript(Tape tape) {
        return TRANSCRIPTS.getOrDefault(tape, "No transcript available.");
    }
}
