package com.dsa.game.data;

import com.dsa.game.state.Evidence;
import com.dsa.game.state.Suspect;

import java.util.*;

public class SuspectDialogue {

    private static final Map<Suspect, String> GREETINGS = new EnumMap<>(Suspect.class);

    private static final Map<Suspect, List<String>> TOPICS = new EnumMap<>(Suspect.class);

    private static final Map<String, String> RESPONSES = new HashMap<>();

    private static final Map<String, String> EVIDENCE_REACTIONS = new HashMap<>();

    private static final Map<String, String> FALSE_RESPONSES = new HashMap<>();

    private static final Map<String, String> MARGARET_DEFLECTIONS = new HashMap<>();

    private static final Map<String, String> ACCUSATION_DEFENSES = new HashMap<>();

    static {
        initGreetings();
        initTopics();
        initResponses();
        initEvidenceReactions();
        initFalseResponses();
        initMargaretDeflections();
        initAccusationDefenses();
    }

    private static void initGreetings() {
        GREETINGS.put(Suspect.JAMES,
            "James Vance sat in an armchair, a glass of whisky in hand. He looked up as you approached. \"Detective. I suppose you have questions.\" He gestured to the chair across from him. \"I'll help if I can. Father and I had our differences, but I want to know what happened as much as anyone.\"");
        GREETINGS.put(Suspect.MARGARET,
            "Margaret stood by the window, arms crossed. She looked like she hadn't slept. \"Detective. I... I want to help. I do. Ask me what you need.\"");
        GREETINGS.put(Suspect.DANIEL,
            "Daniel the groundskeeper leaned against his workbench, dirt under his fingernails. \"I already told the police everything I know. But go ahead.\"");
        GREETINGS.put(Suspect.MARCUS,
            "Marcus Blackwood sat stiffly in a guest chair, his expensive suit at odds with the manor's faded elegance. He looked up with guarded eyes. \"Detective. I assume you've heard about the lawsuit. Yes, Harold and I had... differences. But I'm a businessman, not a killer.\"");
        GREETINGS.put(Suspect.CHARLES,
            "Charles Webb straightened his tie nervously as you approached. Harold's assistant from the company -- young, around twenty-eight -- looked genuinely grief-stricken. \"Detective, please -- I want to help. Mr. Vance trusted me with everything. I owe it to him to see this through.\"");
    }

    private static void initTopics() {
        TOPICS.put(Suspect.JAMES, Arrays.asList(
            "last_night", "father", "will", "daniel", "whereabouts"
        ));
        TOPICS.put(Suspect.MARGARET, Arrays.asList(
            "last_night", "brother", "father", "inheritance", "insurance", "company", "sounds"
        ));
        TOPICS.put(Suspect.DANIEL, Arrays.asList(
            "last_night", "harold", "james", "logbook", "shed"
        ));
        TOPICS.put(Suspect.MARCUS, Arrays.asList(
            "lawsuit", "settlement", "that_night", "harold", "alibi"
        ));
        TOPICS.put(Suspect.CHARLES, Arrays.asList(
            "harold", "will_changes", "james", "that_night", "company"
        ));
    }

    private static void initResponses() {
        RESPONSES.put("JAMES_last_night", "James swirled his drink. \"Dinner was at seven. That dreadful business with Blackwood and Father.\" He sighed. \"After dinner I went to the drawing room for a bit, then up to my room around nine. Read a book. Had a drink. Went to sleep.\" He didn't meet your eyes.");
        RESPONSES.put("JAMES_father", "\"My father was... a difficult man. Demanding. But he was my father.\" James's jaw tightened. \"We had our disagreements. Every family does.\"");
        RESPONSES.put("JAMES_will", "James went very still. \"What about the will? Father's affairs are private. I don't see how that's relevant to your investigation.\"");
        RESPONSES.put("JAMES_daniel", "\"Daniel? He's the groundskeeper. Keeps to himself. I barely know the man.\" James took a long drink. His hand trembled slightly.");
        RESPONSES.put("JAMES_whereabouts", "\"I told you. After dinner I was in the drawing room with Margaret for a bit. Then I went to my room around nine and stayed there all night.\" He'd rehearsed this.");

        RESPONSES.put("MARGARET_last_night", "Margaret's voice was quiet. \"Dinner was at seven -- that awkward affair with Mr. Blackwood. Afterwards, James and I sat in the drawing room for a bit. I went up to bed around nine.\" She paused. \"I... I heard something later. Footsteps in the hallway. But I thought it was just Father.\"");
        RESPONSES.put("MARGARET_brother", "She hesitated. \"James and I aren't close. Not anymore.\" Her voice dropped. \"I heard him that night. Footsteps in the hallway around midnight, whispering with someone -- probably Daniel. Then dragging sounds at two in the morning.\" Her eyes filled with tears. \"The next morning he looked... haunted. Like he hadn't slept. I knew something terrible had happened.\"\n\nShe steadied herself. \"The police aren't taking it seriously. That's why I contacted a private investigator -- a man named Hollis. Arthur Hollis. He's supposed to be thorough.\"");
        RESPONSES.put("MARGARET_father", "\"Father could be cruel. He and James fought constantly about money.\" She paused. \"I heard them arguing around ten that night. Shouting from the study. It went quiet after maybe fifteen minutes.\" She wiped her eyes. \"He didn't deserve... no one deserves that.\"");
        RESPONSES.put("MARGARET_inheritance", "\"I didn't know about the will changes until Charles told me the day after Father died.\" She looked away. \"Charles said I was inheriting everything now. I thought it was strange.\" She was quiet for a moment. \"The will was supposed to be signed that morning. Father never made it to the solicitor.\" Her voice broke. \"Someone made sure he wouldn't.\"");
        RESPONSES.put("MARGARET_insurance", "\"An insurance policy? On Father?\" Margaret looked bewildered. \"I never took out any policy. That's absurd. Someone is trying to make me seem guilty.\"");
        RESPONSES.put("MARGARET_company", "Margaret's expression softened slightly. \"I run a textile import company. Started it eight years ago with nothing but a loan and determination.\" There was quiet pride in her voice. \"Father never approved -- said I was wasting my time. But it's successful now. Profitable. I built it myself.\" She met your eyes. \"I don't need his money or his approval. I never did.\"");
        RESPONSES.put("MARGARET_sounds", "\"Footsteps around midnight, in the hallway. And later, maybe two in the morning, I heard something heavy being moved. Dragging sounds from downstairs, toward the cellar.\" She shuddered. \"I was too frightened to look. I stayed in my room, terrified.\"");

        RESPONSES.put("DANIEL_last_night", "Daniel shrugged. \"Dinner was served at seven. I was in my shed most of the evening, doing repairs.\" He paused. \"Went to bed around eleven. Nothing unusual.\" His eyes darted to the logbook on his desk.");
        RESPONSES.put("DANIEL_harold", "\"Mr. Vance was fair enough. Paid on time. Kept to himself mostly.\" Daniel picked at the dirt under his nails. \"Can't say I knew him well.\"");
        RESPONSES.put("DANIEL_james", "Daniel's expression hardened. \"James Vance is none of my business. We don't exactly move in the same circles, detective.\" The denial was too forceful.");
        RESPONSES.put("DANIEL_logbook", "\"My logbook? It's just daily records. What I planted, what I trimmed.\" He moved between you and the desk. \"Nothing interesting in there.\"");
        RESPONSES.put("DANIEL_shed", "\"This is where I work. Tools, supplies. Sometimes I sleep here when it's late.\" He glanced at the shelf. \"Nothing out of the ordinary.\"");

        RESPONSES.put("MARCUS_lawsuit", "Marcus's jaw tightened. \"The patent infringement case. Two million pounds at stake. Harold was determined to crush my company.\" He straightened his cuffs. \"But corporate warfare is fought in courtrooms, detective. Not with violence.\"");
        RESPONSES.put("MARCUS_settlement", "\"The 'settlement dinner' was Harold's idea of a power play. We had dinner at seven, then moved to the parlor for drinks and negotiations.\" Marcus's eyes darkened. \"He refused every offer. Around half past eight, he excused himself to his study and left me sitting there alone. I waited for two hours hoping he'd return with a reasonable counter-offer. He never did.\"");
        RESPONSES.put("MARCUS_that_night", "\"I was in the parlor from about half past eight until eleven, drinking Harold's whisky and stewing.\" He paused. \"Around ten, I heard shouting from the study. Harold and his son, going at it again. Loud enough that I could hear it through the walls. Then it went quiet. I finally gave up and left at eleven. Charles's window was lit as I left -- he must have seen me drive away.\"");
        RESPONSES.put("MARCUS_harold", "\"Harold Vance was a ruthless man who built his fortune on stolen ideas.\" Marcus leaned forward. \"But I didn't kill him. I wanted to defeat him in court -- publicly, humiliatingly. His death robbed me of that victory.\"");
        RESPONSES.put("MARCUS_alibi", "\"I was in the parlor alone from half past eight until eleven -- over two hours. Then I left, and Charles Webb saw me drive away -- his window was lit when I left.\" Marcus folded his arms. \"I was back at my hotel in town by midnight. The front desk logged me in. Check the records.\"");

        RESPONSES.put("CHARLES_harold", "Charles's eyes welled up. \"Mr. Vance was... demanding, yes. But he trusted me. Five years I worked for him, learned everything about the business.\" He wiped his eyes. \"He didn't deserve this.\"");
        RESPONSES.put("CHARLES_will_changes", "\"I helped prepare the documents. The new will would have left James with nothing -- everything to Margaret.\" Charles lowered his voice. \"James didn't know yet. Harold wanted to announce it formally.\"");
        RESPONSES.put("CHARLES_james", "\"James was always... volatile. Especially about money.\" Charles hesitated. \"Mr. Vance discovered something recently -- he wouldn't tell me what, but he was furious with James. That's when he decided to change the will.\"");
        RESPONSES.put("CHARLES_that_night", "\"Dinner was at seven with the family and Mr. Blackwood. Afterwards I was in the study until nine, helping Mr. Vance with paperwork before he moved to negotiate with Blackwood.\" He paused. \"I retired to my room at nine. But I couldn't sleep -- around quarter to eleven, I saw James heading toward the study from my window. He looked... determined. Then at eleven, I heard Mr. Blackwood's car leaving. After that, everything went quiet.\"");
        RESPONSES.put("CHARLES_company", "\"The lawsuit with Blackwood Industries was consuming Mr. Vance. Two million pounds at stake.\" Charles shook his head. \"And something else was troubling him -- he kept reviewing the company books late at night. He seemed... devastated about something.\" He paused. \"When he told me he was changing the will, he also mentioned restructuring the company. Consolidating. He said my position was being eliminated.\" He looked at his hands. \"I was going to lose my job. But I still can't believe he's gone.\"");
    }

    private static void initEvidenceReactions() {
        EVIDENCE_REACTIONS.put("JAMES_LETTER_OPENER", "James barely glanced at it. \"That's Father's letter opener. It's always on his desk. So what?\"");
        EVIDENCE_REACTIONS.put("JAMES_FIREPLACE_POKER", "James stared at the poker, his face draining of color. \"Where did you... I cleaned that. I cleaned it thoroughly.\" He realized what he'd just admitted. \"I mean -- that's just a fireplace poker. It's been there for years.\"");
        EVIDENCE_REACTIONS.put("JAMES_FINANCIAL_RECORDS", "James's face drained of color. \"Where did you get those? Those are private! Father's finances are none of your concern!\" His composure cracked.");
        EVIDENCE_REACTIONS.put("JAMES_TORN_LETTER", "\"A burned letter? Could be anything. Father burned correspondence all the time.\" But his voice wavered.");
        EVIDENCE_REACTIONS.put("JAMES_WILL_COPY", "James slammed his glass down. \"So you know about the will. Fine. Yes, I inherit. That doesn't mean I killed him! I loved my father!\"");
        EVIDENCE_REACTIONS.put("JAMES_BLOODSTAINED_CUFF", "James stared at the cuff. A long silence. \"I... I cut myself. Shaving. That's all it is.\" His hand was shaking.");
        EVIDENCE_REACTIONS.put("JAMES_MUDDY_BOOTS", "\"Those are Daniel's boots, aren't they? I never went outside that night. Ask Daniel why his boots have fresh mud and cellar dust on them.\" He deflected suspicion.");
        EVIDENCE_REACTIONS.put("JAMES_BLACKMAIL_NOTE", "James looked at the note and forced a surprised expression. \"A blackmail note? In Margaret's room?\" He shook his head. \"I knew she was desperate for money. This proves she had a reason to want Father dead.\" He was overacting -- this was his plant all along.");
        EVIDENCE_REACTIONS.put("JAMES_SLEEPING_POWDER", "James went pale. \"That's not mine. I've never seen that before.\" His denial was too quick. \"Maybe Daniel put it there. He had access to the kitchen.\" He was deflecting -- the powder was his insurance to make Father sluggish and easier to overpower.");
        EVIDENCE_REACTIONS.put("JAMES_GROUNDSKEEPER_LOG", "James stared at the logbook, then looked away. \"Daniel keeps meticulous records. Clearly there's a gap on the night in question.\" His voice was careful, too careful. \"Old houses have a way of making pages... disappear.\"");

        EVIDENCE_REACTIONS.put("MARGARET_TORN_LETTER", "Margaret read the fragments carefully. \"'The will must...' Father was changing the will. James would have lost everything.\" She looked up. \"You understand what that means?\"");
        EVIDENCE_REACTIONS.put("MARGARET_WILL_COPY", "\"I knew Father favored James. Seeing it in writing still hurts.\" She straightened. \"But this gives James a motive, not me. He was about to lose it all.\"");
        EVIDENCE_REACTIONS.put("MARGARET_BLACKMAIL_NOTE", "Margaret stared at the note, bewildered. \"This was in my room? I've never seen this before.\" She studied it carefully. \"This isn't my handwriting. Someone put this there to make it look like I was being blackmailed. James came to my room that night... did he plant this?\"");
        EVIDENCE_REACTIONS.put("MARGARET_FIREPLACE_POKER", "Margaret recoiled from the poker, pressing back in her chair. \"There's blood on it.\" Her voice dropped to a whisper. \"From the study fireplace. James was in the study that night.\" She covered her mouth. \"That's what he used, isn't it?\"");
        EVIDENCE_REACTIONS.put("MARGARET_FINANCIAL_RECORDS", "Margaret studied the records, her expression hardening. \"Fifty thousand pounds. Gone.\" She set them down carefully. \"This is what Father found out. This is why he was so furious that night. James has been stealing from him for years.\" Her voice was flat. \"And Father was about to cut him off entirely.\"");

        EVIDENCE_REACTIONS.put("DANIEL_FIREPLACE_POKER", "Daniel glanced at the poker and shrugged. \"Fireplace poker? Yeah, I've seen it. Part of the study furniture. Why?\" He was too casual. \"If there's blood on it, ask James. He was the one in the study that night.\"");
        EVIDENCE_REACTIONS.put("DANIEL_GROUNDSKEEPER_LOG", "Daniel's eyes widened. \"You went through my logbook? That's private!\" He reached for it. \"The missing page -- I spilled coffee on it. That's all.\"");
        EVIDENCE_REACTIONS.put("DANIEL_MUDDY_BOOTS", "\"Boots? Those are just... I have lots of boots. Groundskeeper. Mud happens.\" He was sweating now. \"Those could be anyone's.\"");
        EVIDENCE_REACTIONS.put("DANIEL_FINANCIAL_RECORDS", "Daniel went very quiet. \"I don't know anything about finances. I'm just the groundskeeper.\" But his eyes said otherwise.");
        EVIDENCE_REACTIONS.put("DANIEL_BLACKMAIL_NOTE", "Daniel squinted at the note. \"That's supposed to be my handwriting? I don't write like that.\" He scoffed. \"Someone tried to make it look like I wrote this. Probably James, trying to frame Margaret. Classic misdirection.\"");
        EVIDENCE_REACTIONS.put("DANIEL_BLOODSTAINED_CUFF", "Daniel's jaw tightened. He looked at the shirt for a long moment without speaking. \"Where did you find that?\" His voice was low, controlled. \"Someone hid it well.\" He wouldn't meet your eyes. \"I don't know anything about that shirt.\"");
        EVIDENCE_REACTIONS.put("DANIEL_SLEEPING_POWDER", "Daniel glanced at the vial and said nothing for a moment. \"Chloral hydrate.\" He said the word carefully, like he already knew what it was. \"You'd use that to make sure someone couldn't fight back.\" He shrugged. \"I've seen it in the garden supply shed. For pests.\" The lie was smooth, but his hands had stopped moving.");
        EVIDENCE_REACTIONS.put("DANIEL_TORN_LETTER", "Daniel looked at the fragments of the letter, then looked away quickly. \"Found that in the ashes, did you?\" He crossed his arms. \"People burn letters all the time. Private correspondence. Means nothing.\" But something in his expression said he recognized it.");

        EVIDENCE_REACTIONS.put("MARCUS_FINANCIAL_RECORDS", "Marcus glanced at the records and scoffed. \"Embezzlement? From the Vance accounts?\" He looked genuinely surprised. \"I didn't know about this. But it explains why James was so desperate that night.\"");
        EVIDENCE_REACTIONS.put("MARCUS_WILL_COPY", "\"So James was being disinherited.\" Marcus read carefully. \"That explains the shouting I heard. A man about to lose everything... that's a dangerous man.\"");
        EVIDENCE_REACTIONS.put("MARCUS_BLACKMAIL_NOTE", "Marcus examined the note with interest. \"Someone was being blackmailed. 'Pay or I talk.'\" He shook his head. \"This wasn't my doing. I fight in courtrooms, not with threats.\"");

        EVIDENCE_REACTIONS.put("CHARLES_FIREPLACE_POKER", "Charles examined the poker carefully. \"Blood on the weighted end? This matches the wound pattern from the autopsy report.\" He looked grave. \"The letter opener was a plant. This is the real murder weapon.\"");
        EVIDENCE_REACTIONS.put("CHARLES_FINANCIAL_RECORDS", "Charles examined the records carefully. \"Fifty thousand pounds... siphoned off over two years.\" His eyes widened. \"This is what Mr. Vance discovered. This is why he was so angry with James. James was stealing from his own father.\"");
        EVIDENCE_REACTIONS.put("CHARLES_WILL_COPY", "\"I prepared that document myself.\" Charles touched the pages carefully. \"Mr. Vance was so angry when he made those changes. He said James had betrayed him for the last time.\"");
        EVIDENCE_REACTIONS.put("CHARLES_TORN_LETTER", "Charles studied the fragments. \"This was in the study fireplace? Someone tried to destroy evidence.\" He looked troubled. \"Only James and Daniel had access to the study that night.\"");
        EVIDENCE_REACTIONS.put("CHARLES_SLEEPING_POWDER", "Charles stared at the vial. \"Chloral hydrate? In the kitchen?\" He thought hard. \"That explains why Mr. Vance complained of drowsiness the past week. Someone was drugging his evening tea to make him vulnerable. I'd wager James was the one administering it.\"");
        EVIDENCE_REACTIONS.put("CHARLES_BLOODSTAINED_CUFF", "Charles went pale. \"Blood? On a shirt hidden in the cellar?\" He sat down heavily. \"The body was moved there. Someone dragged Mr. Vance down those stairs.\" His voice cracked. \"I was in my room all night, door shut, head buried in the new will documents until past one. After that I was out cold. If something happened down in the cellar...\" He stared at his hands. \"God. I was right above them and I didn't hear a thing.\"");
    }

    private static void initFalseResponses() {
        FALSE_RESPONSES.put("JAMES_last_night", "James sighed dramatically. \"I told you, I was reading in my room. I fell asleep around nine and didn't wake until morning. Margaret can confirm -- she brought me tea.\" This contradicted Margaret's account.");
        FALSE_RESPONSES.put("JAMES_father", "\"Father and I were closer than ever. He was planning to increase my inheritance, actually. We had dinner together that very evening and he told me how proud he was.\" His eyes were too steady.");
        FALSE_RESPONSES.put("JAMES_will", "\"The will? It's straightforward -- Father left everything divided equally between Margaret and me. Standard family arrangement.\" This didn't match the copy you found.");
        FALSE_RESPONSES.put("JAMES_daniel", "\"Daniel? I barely know the man. He tends the gardens. We've spoken perhaps twice in as many years.\" The groundskeeper's log told a different story.");
        FALSE_RESPONSES.put("JAMES_whereabouts", "\"I already told you -- I was in my room all evening. Read a book, had a drink, went to sleep around nine. I never left.\" He'd told this story before.");

        FALSE_RESPONSES.put("MARGARET_last_night", "\"I was in my room writing letters all evening. I didn't hear anything at all. It was a perfectly quiet night.\" But she'd mentioned footsteps earlier.");
        FALSE_RESPONSES.put("MARGARET_brother", "\"James and I are very close. He's protective of me. He would never do anything to hurt the family.\" Her tone suggested rehearsal.");
        FALSE_RESPONSES.put("MARGARET_father", "\"Father was generous and kind. He treated us all equally. I can't imagine anyone in the family wanting to harm him.\" The will said otherwise.");
        FALSE_RESPONSES.put("MARGARET_inheritance", "\"I don't know anything about the inheritance details. Father handled all financial matters privately.\" But she'd reacted to the will copy when you showed it.");
        FALSE_RESPONSES.put("MARGARET_sounds", "\"Sounds? No, nothing. These old houses are quiet as the grave at night.\" She'd changed her story completely.");

        FALSE_RESPONSES.put("DANIEL_last_night", "\"Was at the pub in the village until closing. Got back well after midnight. Bartender can vouch for me.\" The pub was ten miles away and his truck had been parked at the shed all night.");
        FALSE_RESPONSES.put("DANIEL_harold", "\"Mr. Vance was practically a stranger to me. I just tend the grounds. Couldn't pick Harold out of a lineup, honestly.\" He'd worked there fifteen years.");
        FALSE_RESPONSES.put("DANIEL_james", "\"James Vance? I think he lives in London. Barely comes around. Don't think he was even here this week.\" James had been there all week.");
        FALSE_RESPONSES.put("DANIEL_logbook", "\"What logbook? I don't keep records. I just do the work.\" You could see the logbook on his desk.");
        FALSE_RESPONSES.put("DANIEL_shed", "\"I just store tools here. Nothing else. Don't even come here most days.\" The shed had a cot with recently slept-in blankets.");

        FALSE_RESPONSES.put("MARCUS_lawsuit", "\"The lawsuit? A minor disagreement between companies. Harold and I were practically friends.\" His earlier hostility contradicted this entirely.");
        FALSE_RESPONSES.put("MARCUS_settlement", "\"The dinner went splendidly. We were close to reaching an agreement.\" He'd claimed it was a disaster earlier.");
        FALSE_RESPONSES.put("MARCUS_that_night", "\"I left around nine, right after dinner. The evening was perfectly pleasant.\" He'd said eleven o'clock before, and mentioned shouting.");
        FALSE_RESPONSES.put("MARCUS_harold", "\"Harold was a fair businessman. I had nothing but respect for him.\" His contempt for Harold had been obvious earlier.");
        FALSE_RESPONSES.put("MARCUS_alibi", "\"I drove straight to the airport after dinner. Caught a late flight home.\" He'd claimed he went to a hotel earlier.");

        FALSE_RESPONSES.put("CHARLES_harold", "\"Mr. Vance was... difficult. Cruel, even. He made my life miserable.\" This contradicted his earlier grief and loyalty.");
        FALSE_RESPONSES.put("CHARLES_will_changes", "\"I don't know anything about the will. Mr. Vance kept those matters private.\" He'd said he helped prepare the documents.");
        FALSE_RESPONSES.put("CHARLES_james", "\"James? I barely knew him. He never came to the office.\" He'd mentioned observing James's suspicious behavior before.");
        FALSE_RESPONSES.put("CHARLES_that_night", "\"I wasn't at the manor that night. I was at my flat in London.\" But he'd described being in the study until nine.");
        FALSE_RESPONSES.put("CHARLES_company", "\"The company was doing wonderfully. No problems at all.\" The lawsuit and Harold's agitation told a different story.");
    }

    private static void initMargaretDeflections() {
        MARGARET_DEFLECTIONS.put("JAMES_deflect", "James lowered his voice. \"Have you looked into Margaret's finances? She's been desperate for money. Ask her about the insurance policy she took out on Father last month.\"");
        MARGARET_DEFLECTIONS.put("DANIEL_deflect", "Daniel leaned in. \"I shouldn't say this, but... I saw Margaret coming out of the study at eleven that night. She was carrying something. Looked like she'd been crying -- or maybe it was guilt.\"");
        MARGARET_DEFLECTIONS.put("MARCUS_deflect", "Marcus glanced around before speaking. \"I observed Miss Vance that evening. She seemed agitated. Pacing in the hallway near the study around ten o'clock. Ask her what she was doing there.\"");
        MARGARET_DEFLECTIONS.put("CHARLES_deflect", "Charles hesitated before speaking. \"I shouldn't say this, but... Margaret asked Mr. Vance about the will that afternoon. She was furious when she learned she was getting everything. Said she didn't want it -- that it would make her a target.\"");
    }

    private static void initAccusationDefenses() {
        ACCUSATION_DEFENSES.put("accuse_james", "James rose from his chair, trembling with fury. \"You accuse ME? Without proof that I acted alone?\" He slammed his fist on the mantel. \"I couldn't have moved the body down those cellar stairs by myself -- not without help. And the window? Someone had to hold it open from outside. You're looking for a conspiracy and you've only found half of it.\"\n\nIn his outburst, James had inadvertently confirmed the body was moved and that someone entered through the window.");
        ACCUSATION_DEFENSES.put("accuse_margaret", "Margaret stared at you with hollow eyes. \"You think I killed my own father? For what? I'm not even IN the will.\" She reached into her bag and produced a train ticket. \"I was planning to leave. I bought this ticket three days ago. Why would I kill him if I was already running away?\" She held up the wine-stained shoes from her room. \"And these? This is wine, detective. Not blood. Taste it if you don't believe me.\"\n\nHer defense was compelling. The evidence against her was entirely circumstantial.");
        ACCUSATION_DEFENSES.put("accuse_daniel", "Daniel's weathered face hardened. \"Me? Acting alone?\" He shook his head slowly. \"Detective, I'm sixty-three years old with arthritis in both knees. You think I overpowered Harold Vance, dragged his body to the cellar, AND staged a break-in? By myself?\"\n\nHe held up his gnarled hands. \"Look at these hands. They can barely grip a trowel some mornings. I couldn't have done this alone, and you know it. Someone else was there that night. Someone stronger. Someone with more to gain.\"\n\nDaniel's physical limitations were obvious. He couldn't have acted alone.");
        ACCUSATION_DEFENSES.put("accuse_marcus", "Marcus Blackwood laughed coldly. \"The rival CEO? How predictable.\" He stood, adjusting his cuffs with deliberate calm.\n\n\"I had motive, yes -- the lawsuit would have bankrupted me. But I was GONE by eleven o'clock. Charles Webb saw me drive away -- his window was lit. Check the hotel records -- I was back in my room by midnight.\"\n\nHe leaned forward. \"Why would I kill Harold before the lawsuit concluded? A dead man can't drop charges. His estate will pursue the case with even more vigor now. His death HURTS my position, detective.\"\n\nHis logic was sound. Killing Harold made his legal situation worse, not better.");
        ACCUSATION_DEFENSES.put("accuse_charles", "Charles Webb's face crumpled with hurt. \"Me? After everything I told you?\"\n\nHe took a shaky breath. \"I was going to LOSE my job because of the will changes. Mr. Vance was consolidating the company -- my position was being eliminated. Why would I kill the man who trusted me with his secrets?\"\n\nHe pulled out his appointment book. \"I was in my room from nine o'clock onward, working on documents. I found the body WITH Daniel the next morning.\"\n\nCharles had helped you build the case against James. Accusing him made no sense.");
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
            case "insurance": return "Insurance";
            case "sounds": return "Strange Sounds";
            case "harold": return "Harold";
            case "james": return "James";
            case "logbook": return "Logbook";
            case "shed": return "The Shed";
            case "lawsuit": return "The Lawsuit";
            case "settlement": return "Settlement Dinner";
            case "that_night": return "That Night";
            case "alibi": return "Your Alibi";
            case "will_changes": return "Will Changes";
            case "company": return "The Company";
            default: return topic;
        }
    }

    public static String getResponse(Suspect suspect, String topic) {
        String key = suspect.name() + "_" + topic;
        return RESPONSES.getOrDefault(key, "They don't seem to want to talk about that.");
    }

    public static String getEvidenceReaction(Suspect suspect, Evidence evidence) {
        String key = suspect.name() + "_" + evidence.name();
        return EVIDENCE_REACTIONS.getOrDefault(key, suspect.getDisplayName() + " examined the " + evidence.getDisplayName() + " but showed no particular reaction.");
    }

    public static boolean hasEvidenceReaction(Suspect suspect, Evidence evidence) {
        return EVIDENCE_REACTIONS.containsKey(suspect.name() + "_" + evidence.name());
    }

    public static String getFalseResponse(Suspect suspect, String topic) {
        String key = suspect.name() + "_" + topic;
        return FALSE_RESPONSES.get(key);
    }

    public static boolean hasFalseResponse(Suspect suspect, String topic) {
        return FALSE_RESPONSES.containsKey(suspect.name() + "_" + topic);
    }

    public static String getMargaretDeflection(Suspect suspect) {
        return MARGARET_DEFLECTIONS.get(suspect.name() + "_deflect");
    }

    public static boolean hasMargaretDeflection(Suspect suspect) {
        return MARGARET_DEFLECTIONS.containsKey(suspect.name() + "_deflect");
    }

    public static String getAccusationDefense(String accuseAction) {
        return ACCUSATION_DEFENSES.get(accuseAction);
    }

    public static boolean hasAccusationDefense(String accuseAction) {
        return ACCUSATION_DEFENSES.containsKey(accuseAction);
    }
}
