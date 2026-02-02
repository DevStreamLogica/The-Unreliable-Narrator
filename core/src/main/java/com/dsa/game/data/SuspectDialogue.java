package com.dsa.game.data;

import com.dsa.game.state.Evidence;
import com.dsa.game.state.Suspect;

import java.util.*;

public class SuspectDialogue {

    // Greetings per suspect
    private static final Map<Suspect, String> GREETINGS = new EnumMap<>(Suspect.class);

    // Topics available per suspect
    private static final Map<Suspect, List<String>> TOPICS = new EnumMap<>(Suspect.class);

    // Topic responses: key = "SUSPECT_topic"
    private static final Map<String, String> RESPONSES = new HashMap<>();

    // Evidence reactions: key = "SUSPECT_EVIDENCE"
    private static final Map<String, String> EVIDENCE_REACTIONS = new HashMap<>();

    static {
        initGreetings();
        initTopics();
        initResponses();
        initEvidenceReactions();
    }

    private static void initGreetings() {
        GREETINGS.put(Suspect.JAMES,
            "James Vance sits in an armchair, a glass of whisky in hand. He doesn't look up. \"What do you want now, detective? Haven't you poked around enough?\"");
        GREETINGS.put(Suspect.MARGARET,
            "Margaret stands by the window, arms crossed. She looks like she hasn't slept. \"Detective. I... I want to help. I do. Ask me what you need.\"");
        GREETINGS.put(Suspect.DANIEL,
            "Daniel the groundskeeper leans against his workbench, dirt under his fingernails. \"I already told the police everything I know. But go ahead.\"");
        GREETINGS.put(Suspect.ELEANOR,
            "Eleanor the housekeeper is polishing silver, her movements mechanical. \"Detective. I've served this family for thirty years. I'll tell you what I can.\"");
        GREETINGS.put(Suspect.REGINALD,
            "Reginald the butler stands ramrod straight, hands clasped behind his back. \"How may I be of assistance, detective?\"");
    }

    private static void initTopics() {
        TOPICS.put(Suspect.JAMES, Arrays.asList(
            "last_night", "father", "will", "daniel", "whereabouts"
        ));
        TOPICS.put(Suspect.MARGARET, Arrays.asList(
            "last_night", "brother", "father", "inheritance", "sounds"
        ));
        TOPICS.put(Suspect.DANIEL, Arrays.asList(
            "last_night", "harold", "james", "logbook", "shed"
        ));
        TOPICS.put(Suspect.ELEANOR, Arrays.asList(
            "last_night", "family", "kitchen", "sleeping_powder", "arguments"
        ));
        TOPICS.put(Suspect.REGINALD, Arrays.asList(
            "last_night", "household", "visitors", "cellar", "routine"
        ));
    }

    private static void initResponses() {
        // JAMES
        RESPONSES.put("JAMES_last_night", "James swirls his drink. \"I was in my room all evening after dinner. Read a book. Had a drink. Went to sleep. Didn't hear anything unusual.\" He doesn't meet your eyes.");
        RESPONSES.put("JAMES_father", "\"My father was... a difficult man. Demanding. But he was my father.\" James's jaw tightens. \"We had our disagreements. Every family does.\"");
        RESPONSES.put("JAMES_will", "James goes very still. \"What about the will? Father's affairs are private. I don't see how that's relevant to your investigation.\"");
        RESPONSES.put("JAMES_daniel", "\"Daniel? He's the groundskeeper. Keeps to himself. I barely know the man.\" James takes a long drink. His hand trembles slightly.");
        RESPONSES.put("JAMES_whereabouts", "\"I told you. My room. All night. Ask Margaret -- she saw me go upstairs after dinner.\" He's rehearsed this.");

        // MARGARET
        RESPONSES.put("MARGARET_last_night", "Margaret's voice is quiet. \"I went to bed early. Around ten. I... I heard something, later. Footsteps in the hallway. But I thought it was just Father.\"");
        RESPONSES.put("MARGARET_brother", "She hesitates. \"James and I aren't close. Not anymore. He came to my room that night, actually. Late. Asked me to... confirm he'd been in his room all evening. I didn't understand why.\"");
        RESPONSES.put("MARGARET_father", "\"Father could be cruel. He and James fought constantly about money. But he didn't deserve...\" She wipes her eyes. \"No one deserves that.\"");
        RESPONSES.put("MARGARET_inheritance", "\"I know what people think. That I had a reason because Father cut me out of the will. But I didn't even know about the changes until after.\" She looks away.");
        RESPONSES.put("MARGARET_sounds", "\"The footsteps. And later, maybe two in the morning, I heard something heavy being moved. Dragging sounds. From downstairs. I was too frightened to look.\"");

        // DANIEL
        RESPONSES.put("DANIEL_last_night", "Daniel shrugs. \"Was in my shed. Went to bed early. Groundskeepers start before dawn.\" His eyes dart to the logbook on his desk.");
        RESPONSES.put("DANIEL_harold", "\"Mr. Vance was fair enough. Paid on time. Kept to himself mostly.\" Daniel picks at the dirt under his nails. \"Can't say I knew him well.\"");
        RESPONSES.put("DANIEL_james", "Daniel's expression hardens. \"James Vance is none of my business. We don't exactly move in the same circles, detective.\" The denial is too forceful.");
        RESPONSES.put("DANIEL_logbook", "\"My logbook? It's just daily records. What I planted, what I trimmed.\" He moves between you and the desk. \"Nothing interesting in there.\"");
        RESPONSES.put("DANIEL_shed", "\"This is where I work. Tools, supplies. Sometimes I sleep here when it's late.\" He glances at the shelf. \"Nothing out of the ordinary.\"");

        // ELEANOR
        RESPONSES.put("ELEANOR_last_night", "\"I prepared dinner as usual. Served at seven. Cleared up by nine. I retired to my quarters after that.\" She pauses. \"I did hear the master's study door close around midnight. Unusual.\"");
        RESPONSES.put("ELEANOR_family", "\"The Vances are... complicated. Harold was strict but fair. James is resentful. Margaret is trapped. And they all keep secrets.\" She lowers her voice. \"Terrible secrets.\"");
        RESPONSES.put("ELEANOR_kitchen", "\"My kitchen is my domain. Everything has its place.\" She hesitates. \"Though I've noticed things being moved lately. The flour tin especially. Someone's been in my kitchen at night.\"");
        RESPONSES.put("ELEANOR_sleeping_powder", "Eleanor pales. \"Sleeping powder? I... I may have found something in the flour tin. A vial. I didn't know what to do. If the family found out I was snooping...\"");
        RESPONSES.put("ELEANOR_arguments", "\"Mr. Harold and James had a terrible row the night before. About money. I could hear them from the kitchen. James said something like 'You can't do this to me.' Harold said he already had.\"");

        // REGINALD
        RESPONSES.put("REGINALD_last_night", "\"I locked up at ten as always. Checked all doors and windows. Everything was secure.\" Reginald pauses precisely. \"I did not hear anything during the night. My quarters are at the far end of the house.\"");
        RESPONSES.put("REGINALD_household", "\"I have served the Vance family for fifteen years. The household runs on routine and discretion.\" His expression is carefully neutral. \"Recent events have been... disruptive.\"");
        RESPONSES.put("REGINALD_visitors", "\"No unexpected visitors that evening. Only the family, the staff, and Daniel, who came up from the shed around seven for dinner, as is his custom on Tuesdays.\"");
        RESPONSES.put("REGINALD_cellar", "\"The cellar is used for wine storage and dry goods. I'm the only one with a key, though...\" He frowns. \"I noticed the lock had been tampered with recently. Scratches around the keyhole.\"");
        RESPONSES.put("REGINALD_routine", "\"Mr. Harold's routine never varied. Tea at six, dinner at seven, study at eight, bed by ten. The night he died, Eleanor said he never came to the study for his tea. That was the first break in routine in fifteen years.\"");
    }

    private static void initEvidenceReactions() {
        // JAMES reactions
        EVIDENCE_REACTIONS.put("JAMES_LETTER_OPENER", "James barely glances at it. \"That's Father's letter opener. It's always on his desk. So what?\"");
        EVIDENCE_REACTIONS.put("JAMES_FINANCIAL_RECORDS", "James's face drains of color. \"Where did you get those? Those are private! Father's finances are none of your concern!\" His composure cracks.");
        EVIDENCE_REACTIONS.put("JAMES_TORN_LETTER", "\"A burned letter? Could be anything. Father burned correspondence all the time.\" But his voice wavers.");
        EVIDENCE_REACTIONS.put("JAMES_WILL_COPY", "James slams his glass down. \"So you know about the will. Fine. Yes, I inherit. That doesn't mean I killed him! I loved my father!\"");
        EVIDENCE_REACTIONS.put("JAMES_BLOODSTAINED_CUFF", "James stares at the cuff. A long silence. \"I... I cut myself. Shaving. That's all it is.\" His hand is shaking.");
        EVIDENCE_REACTIONS.put("JAMES_MUDDY_BOOTS", "\"Those aren't mine. I didn't go outside that night. Someone must have borrowed my boots.\" He realizes what he's admitted.");
        EVIDENCE_REACTIONS.put("JAMES_BLACKMAIL_NOTE", "\"I've never seen that before. Someone is trying to frame me.\" But he can't stop staring at it.");

        // MARGARET reactions
        EVIDENCE_REACTIONS.put("MARGARET_TORN_LETTER", "Margaret reads the fragments carefully. \"'The will must...' Father was changing the will. James would have lost everything.\" She looks up. \"You understand what that means?\"");
        EVIDENCE_REACTIONS.put("MARGARET_WILL_COPY", "\"I knew Father favored James. Seeing it in writing still hurts.\" She straightens. \"But this gives James a motive, not me. He was about to lose it all.\"");
        EVIDENCE_REACTIONS.put("MARGARET_BLACKMAIL_NOTE", "Margaret reads it and goes white. \"This was in my room? Someone... someone put this there. To make it look like I...\" She's trembling.");

        // DANIEL reactions
        EVIDENCE_REACTIONS.put("DANIEL_GROUNDSKEEPER_LOG", "Daniel's eyes widen. \"You went through my logbook? That's private!\" He reaches for it. \"The missing page -- I spilled coffee on it. That's all.\"");
        EVIDENCE_REACTIONS.put("DANIEL_MUDDY_BOOTS", "\"Boots? Those are just... I have lots of boots. Groundskeeper. Mud happens.\" He's sweating now. \"Those could be anyone's.\"");
        EVIDENCE_REACTIONS.put("DANIEL_FINANCIAL_RECORDS", "Daniel goes very quiet. \"I don't know anything about finances. I'm just the groundskeeper.\" But his eyes say otherwise.");

        // ELEANOR reactions
        EVIDENCE_REACTIONS.put("ELEANOR_SLEEPING_POWDER", "Eleanor sits down heavily. \"So you found it too. I should have told someone. But I was afraid -- afraid of what it meant, and who put it there.\"");
        EVIDENCE_REACTIONS.put("ELEANOR_TORN_LETTER", "\"Burned in the study fireplace? After Harold's death?\" Eleanor thinks. \"Only James and Daniel were in the study that night. I'm certain of it.\"");

        // REGINALD reactions
        EVIDENCE_REACTIONS.put("REGINALD_SLEEPING_POWDER", "Reginald's composure finally cracks. \"Sleeping powder in the kitchen? In Eleanor's flour tin? Good lord. Someone was drugging the master's food.\"");
        EVIDENCE_REACTIONS.put("REGINALD_GROUNDSKEEPER_LOG", "\"The groundskeeper's logbook with a missing page? On the night of the murder?\" Reginald shakes his head. \"That man has always been too close to James for comfort.\"");
    }

    public static String getGreeting(Suspect suspect) {
        return GREETINGS.getOrDefault(suspect, "They regard you silently.");
    }

    public static List<String> getTopics(Suspect suspect) {
        return TOPICS.getOrDefault(suspect, Collections.emptyList());
    }

    public static String getTopicDisplayName(String topic) {
        switch (topic) {
            case "last_night": return "Last Night";
            case "father": return "Your Father";
            case "will": return "The Will";
            case "daniel": return "Daniel";
            case "whereabouts": return "Whereabouts";
            case "brother": return "Your Brother";
            case "inheritance": return "Inheritance";
            case "sounds": return "Strange Sounds";
            case "harold": return "Harold";
            case "james": return "James";
            case "logbook": return "Logbook";
            case "shed": return "The Shed";
            case "family": return "The Family";
            case "kitchen": return "The Kitchen";
            case "sleeping_powder": return "Sleeping Powder";
            case "arguments": return "Arguments";
            case "household": return "Household";
            case "visitors": return "Visitors";
            case "cellar": return "The Cellar";
            case "routine": return "Routine";
            default: return topic;
        }
    }

    public static String getResponse(Suspect suspect, String topic) {
        String key = suspect.name() + "_" + topic;
        return RESPONSES.getOrDefault(key, "They don't seem to want to talk about that.");
    }

    public static String getEvidenceReaction(Suspect suspect, Evidence evidence) {
        String key = suspect.name() + "_" + evidence.name();
        return EVIDENCE_REACTIONS.getOrDefault(key, suspect.getDisplayName() + " examines the " + evidence.getDisplayName() + " but shows no particular reaction.");
    }

    public static boolean hasEvidenceReaction(Suspect suspect, Evidence evidence) {
        return EVIDENCE_REACTIONS.containsKey(suspect.name() + "_" + evidence.name());
    }
}
