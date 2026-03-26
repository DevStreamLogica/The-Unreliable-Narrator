package com.dsa.game.data;

import com.dsa.game.state.Tape;

import java.util.EnumMap;
import java.util.Map;

public class TapeContent {

    private static final Map<Tape, String> TRANSCRIPTS = new EnumMap<>(Tape.class);

    static {
        TRANSCRIPTS.put(Tape.TAPE_ARGUMENT,
            "=== TAPE 1: Harold & James Argument ===\n\n" +
            "[Sound of a door opening]\n\n" +
            "HAROLD: \"James. Sit down. We need to talk.\"\n\n" +
            "JAMES: \"Father, I'm tired. Can this wait until--\"\n\n" +
            "HAROLD: \"No. It can't wait. I've seen the accounts, James. Don't insult me by denying it. Fifty thousand pounds, gone.\"\n\n" +
            "JAMES: (pause) \"Father, I can explain--\"\n\n" +
            "HAROLD: \"Explain? Explain how fifty thousand pounds vanished into an account I've never heard of? Explain how you thought I wouldn't notice?\"\n\n" +
            "JAMES: \"It was a loan. An investment. I was going to pay it back--\"\n\n" +
            "HAROLD: \"With what? The company money you've been siphoning for the past two years? I'm changing the will, James. Tomorrow morning at nine o'clock.\"\n\n" +
            "JAMES: (voice rising) \"You can't do that. You wouldn't.\"\n\n" +
            "HAROLD: \"Watch me. You'll get nothing. Not a penny. The entire estate goes to Margaret. And don't think your friend Daniel can help you this time. I know about your arrangement.\"\n\n" +
            "JAMES: \"What arrangement? Daniel works for you--\"\n\n" +
            "HAROLD: \"Daniel works for YOU. Moving money. Hiding transactions. I've seen his logbook. The entries that conveniently go missing. You're both done.\"\n\n" +
            "JAMES: \"Father, please--\"\n\n" +
            "HAROLD: \"Get out of my study. And tell Daniel he's dismissed. I want him off the property by week's end.\"\n\n" +
            "[Sound of a door slamming]\n\n" +
            "[Recording continues for 30 seconds - Harold's heavy breathing, then click]");

        TRANSCRIPTS.put(Tape.TAPE_JAMES_INTERVIEW,
            "=== TAPE 2: James Vance - Police Interview ===\n\n" +
            "[Sound of door closing, chairs scraping]\n\n" +
            "DETECTIVE MORRISON: \"Interview with James Vance, November 17th, 1987, 2:00 PM. Mr. Vance, thank you for coming in. I know this is a difficult time.\"\n\n" +
            "JAMES: \"Of course, Detective. I want to help. I want to find out what happened to my father.\"\n\n" +
            "MORRISON: \"Let's start with the night of November 15th. Can you walk me through your evening?\"\n\n" +
            "JAMES: \"I... yes. Father and I had dinner around seven. Then we had a... discussion in his study around ten.\"\n\n" +
            "MORRISON: \"A discussion about what?\"\n\n" +
            "JAMES: (pause) \"Business matters. Company finances. Father was stressed about some investments.\"\n\n" +
            "MORRISON: \"Witnesses say they heard shouting.\"\n\n" +
            "JAMES: \"Father had a temper. He shouted about everything. It wasn't unusual.\"\n\n" +
            "MORRISON: \"What happened after this discussion?\"\n\n" +
            "JAMES: \"I went to my room. I was angry. I read for a while, then went to sleep around eleven-thirty.\"\n\n" +
            "MORRISON: \"Can anyone confirm you were in your room?\"\n\n" +
            "JAMES: (shifting in chair) \"I was alone. My room is on the east wing. Margaret's room is on the opposite side. No one would have seen me.\"\n\n" +
            "MORRISON: \"So no alibi.\"\n\n" +
            "JAMES: \"I don't need an alibi, Detective. I was asleep. When I woke up the next morning, Charles told me Father was... he was gone.\"\n\n" +
            "MORRISON: \"Let's talk about the company finances. Your father mentioned embezzlement in his diary.\"\n\n" +
            "JAMES: (voice rising) \"I don't know anything about embezzlement. Have you looked into Margaret? She's been desperate for money for years. And Marcus Blackwood was here that night--business rival, plenty of motive.\"\n\n" +
            "MORRISON: \"We'll get to them. Right now I'm asking about you.\"\n\n" +
            "JAMES: \"Am I under arrest?\"\n\n" +
            "MORRISON: \"Should you be?\"\n\n" +
            "JAMES: (long pause) \"I loved my father. Whatever our disagreements, I would never... I was in my room. That's all I can tell you.\"\n\n" +
            "MORRISON: \"We found fifty thousand pounds missing from company accounts.\"\n\n" +
            "JAMES: (standing) \"I think I need to speak to my solicitor.\"\n\n" +
            "MORRISON: \"Sit down, Mr. Vance.\"\n\n" +
            "JAMES: (sitting) \"I took a loan from the company. I was going to pay it back. That's not murder.\"\n\n" +
            "MORRISON: \"It's motive.\"\n\n" +
            "[Long silence]\n\n" +
            "MORRISON: \"That's all for now. Don't leave town, Mr. Vance.\"\n\n" +
            "[End of recording]");

        TRANSCRIPTS.put(Tape.TAPE_DANIEL_INTERVIEW,
            "=== TAPE 3: Daniel Hobbs - Police Interview ===\n\n" +
            "[Sound of door closing]\n\n" +
            "DETECTIVE MORRISON: \"Interview with Daniel Hobbs, groundskeeper, November 17th, 1987, 4:30 PM. Mr. Hobbs, you've worked at Vance Manor for how long?\"\n\n" +
            "DANIEL: \"Fifteen years this spring.\"\n\n" +
            "MORRISON: \"And your relationship with Harold Vance?\"\n\n" +
            "DANIEL: \"Professional. He was my employer. I maintained the grounds, did repairs. Standard arrangement.\"\n\n" +
            "MORRISON: \"Standard arrangement. That's interesting, because we found some irregularities in your maintenance logbook.\"\n\n" +
            "DANIEL: \"Irregularities?\"\n\n" +
            "MORRISON: \"November 15th. The day Harold Vance died. There's no entry for that day.\"\n\n" +
            "DANIEL: (pause) \"Must have forgotten to log it. I was working in the shed most of the day.\"\n\n" +
            "MORRISON: \"The shed. Alone?\"\n\n" +
            "DANIEL: \"Alone.\"\n\n" +
            "MORRISON: \"What were you doing in the shed?\"\n\n" +
            "DANIEL: \"Maintenance work. Sharpening tools. Organizing supplies.\"\n\n" +
            "MORRISON: \"Anyone see you?\"\n\n" +
            "DANIEL: \"The staff don't usually come out to the grounds after dark. I work alone.\"\n\n" +
            "MORRISON: \"So you have no alibi for the evening of November 15th.\"\n\n" +
            "DANIEL: (too calm) \"I don't need an alibi. I was in my shed until about eleven, then I went to bed. My quarters are above the carriage house. I didn't see or hear anything unusual.\"\n\n" +
            "MORRISON: \"You didn't hear James and Harold arguing at ten o'clock? The whole household heard it.\"\n\n" +
            "DANIEL: \"The carriage house is on the far side of the property. Sound doesn't carry.\"\n\n" +
            "MORRISON: \"Margaret Vance says she heard footsteps in the hallway around midnight. Two people.\"\n\n" +
            "DANIEL: \"Then ask the two people. Wasn't me.\"\n\n" +
            "MORRISON: \"Harold Vance's diary mentions you by name. He was planning to dismiss you.\"\n\n" +
            "DANIEL: (slight tension) \"News to me.\"\n\n" +
            "MORRISON: \"He suspected you were helping James embezzle company funds. Moving money. Hiding transactions.\"\n\n" +
            "DANIEL: \"That's absurd.\"\n\n" +
            "MORRISON: \"Is it? Because we checked your bank account, Daniel. You've deposited over twenty thousand pounds in the past two years. On a groundskeeper's salary.\"\n\n" +
            "DANIEL: (pause) \"I do side work. Private gardening jobs.\"\n\n" +
            "MORRISON: \"Twenty thousand pounds in side work.\"\n\n" +
            "DANIEL: \"I'm good at what I do.\"\n\n" +
            "MORRISON: \"Where were you between eleven PM and two AM on November 15th?\"\n\n" +
            "DANIEL: \"I told you. In bed.\"\n\n" +
            "MORRISON: \"Margaret heard dragging sounds at two AM. Heavy. Toward the cellar.\"\n\n" +
            "DANIEL: \"Maybe she was hearing things. Old house. Lots of noises.\"\n\n" +
            "MORRISON: \"Harold's body was found in the cellar. Moved post-mortem. It would take two people to move a body down those stairs.\"\n\n" +
            "DANIEL: (slight slip) \"I was helping move-- I mean, move furniture. I was helping move furniture earlier that day.\"\n\n" +
            "MORRISON: (leaning forward) \"What furniture?\"\n\n" +
            "DANIEL: (recovering) \"A desk. In the study. James asked me to help him move a desk.\"\n\n" +
            "MORRISON: \"When?\"\n\n" +
            "DANIEL: \"Afternoon. Before dinner.\"\n\n" +
            "MORRISON: \"And you forgot to log that in your maintenance book?\"\n\n" +
            "DANIEL: (stone-faced) \"Must have slipped my mind.\"\n\n" +
            "MORRISON: \"Your mind seems to be slipping a lot lately, Daniel.\"\n\n" +
            "[Long silence]\n\n" +
            "DANIEL: \"Am I under arrest?\"\n\n" +
            "MORRISON: \"Not yet. But don't go anywhere.\"\n\n" +
            "[End of recording]");

        TRANSCRIPTS.put(Tape.TAPE_MARGARET_INTERVIEW,
            "=== TAPE 4: Margaret Vance - Police Interview ===\n\n" +
            "[Sound of door closing, tissues rustling]\n\n" +
            "DETECTIVE MORRISON: \"Interview with Margaret Vance, November 17th, 1987, 10:00 AM. Mrs. Vance, I'm sorry for your loss.\"\n\n" +
            "MARGARET: (crying) \"Thank you, Detective.\"\n\n" +
            "MORRISON: \"I know this is difficult, but I need to ask you some questions about the night your father died.\"\n\n" +
            "MARGARET: (sniffling) \"Of course. I want to help.\"\n\n" +
            "MORRISON: \"Tell me about that evening. November 15th.\"\n\n" +
            "MARGARET: \"I had dinner with Father, James, and Mr. Blackwood--Marcus. It was tense. Father and Marcus were discussing some business settlement. It didn't go well.\"\n\n" +
            "MORRISON: \"What time did dinner end?\"\n\n" +
            "MARGARET: \"Around eight-thirty. Marcus stayed in the parlor with Father for another hour or so. James and I sat in the drawing room for a while, then we both went up. I was in my room by nine.\"\n\n" +
            "MORRISON: \"And then?\"\n\n" +
            "MARGARET: \"Around ten o'clock, I heard shouting. From Father's study. James and Father arguing.\"\n\n" +
            "MORRISON: \"Could you hear what they were saying?\"\n\n" +
            "MARGARET: \"Not the words. Just... anger. Father's voice was so loud. James sounded desperate.\"\n\n" +
            "MORRISON: \"What happened next?\"\n\n" +
            "MARGARET: \"The study door slammed. Then... silence. I thought about going to check on Father, but I didn't. I should have. I should have--\" (breaks down crying)\n\n" +
            "MORRISON: \"It's okay. Take your time.\"\n\n" +
            "MARGARET: (composing herself) \"I tried to sleep. But I couldn't. Around midnight, I heard footsteps in the hallway.\"\n\n" +
            "MORRISON: \"Footsteps?\"\n\n" +
            "MARGARET: \"Yes. Two people. Whispering urgently. I couldn't make out the voices, but... one of them sounded like James.\"\n\n" +
            "MORRISON: \"You're sure?\"\n\n" +
            "MARGARET: \"I think so. The footsteps went toward the study.\"\n\n" +
            "MORRISON: \"Did you investigate?\"\n\n" +
            "MARGARET: \"No. I... I was frightened. I locked my door and tried to sleep.\"\n\n" +
            "MORRISON: \"Did you hear anything else?\"\n\n" +
            "MARGARET: (pause) \"At two in the morning, I heard dragging sounds.\"\n\n" +
            "MORRISON: \"Dragging sounds?\"\n\n" +
            "MARGARET: \"Heavy. Like furniture being moved. Or... or something heavy. The sounds went down the hallway, toward the cellar stairs.\"\n\n" +
            "MORRISON: \"And you didn't investigate?\"\n\n" +
            "MARGARET: (crying again) \"I was terrified, Detective. I pulled the covers over my head and prayed. I thought it was a burglar. I thought if I stayed quiet, they'd leave. I didn't know Father was--\" (breaks down)\n\n" +
            "MORRISON: \"It's alright, Mrs. Vance. Did you hear anything else?\"\n\n" +
            "MARGARET: \"No. The house went quiet after that. In the morning, Charles knocked on my door. He looked pale. He said Father was... was gone.\"\n\n" +
            "MORRISON: \"What was James's demeanor the next morning?\"\n\n" +
            "MARGARET: (pause) \"Exhausted. Hollow-eyed. Like he hadn't slept all night. And Daniel--Daniel wouldn't look at me. Wouldn't meet my eyes.\"\n\n" +
            "MORRISON: \"Do you think your brother was involved?\"\n\n" +
            "MARGARET: (long pause, whispered) \"I don't want to believe it. But... yes. God help me, yes.\"\n\n" +
            "MORRISON: \"Thank you, Mrs. Vance. That's all for now.\"\n\n" +
            "[End of recording]");

        TRANSCRIPTS.put(Tape.TAPE_MARCUS_INTERVIEW,
            "=== TAPE 5: Marcus Blackwood - Police Interview ===\n\n" +
            "[Sound of door closing]\n\n" +
            "DETECTIVE MORRISON: \"Interview with Marcus Blackwood, November 17th, 1987, 3:00 PM. Mr. Blackwood, thank you for coming in voluntarily.\"\n\n" +
            "MARCUS: \"Of course, Detective. Harold was a colleague, if not exactly a friend. I want to help however I can.\"\n\n" +
            "MORRISON: \"You were at Vance Manor the night Harold died. November 15th.\"\n\n" +
            "MARCUS: \"Yes. We were discussing a business settlement. Harold and I had competing patents--long story. We'd agreed to meet for dinner and hash it out.\"\n\n" +
            "MORRISON: \"How did that go?\"\n\n" +
            "MARCUS: \"Poorly. Harold refused every offer I made. The man was stubborn as a mule. Brilliant, but stubborn.\"\n\n" +
            "MORRISON: \"Were you angry?\"\n\n" +
            "MARCUS: \"Frustrated, not angry. This was business. I'd expected we'd reach an agreement eventually, even if it took a few more rounds of negotiation.\"\n\n" +
            "MORRISON: \"What time did you leave?\"\n\n" +
            "MARCUS: \"Around eleven PM. Harold and I were in the parlor until about half past eight -- then he excused himself to his study and left me sitting there alone. I waited until eleven, then gave up and drove to my hotel.\"\n\n" +
            "MORRISON: \"Which hotel?\"\n\n" +
            "MARCUS: \"The Ashworth Inn, in town. I checked in around midnight. The night clerk can confirm.\"\n\n" +
            "MORRISON: \"We'll verify that. Did you see or hear anything unusual before you left?\"\n\n" +
            "MARCUS: \"I heard Harold and James arguing. Around ten o'clock or so.\"\n\n" +
            "MORRISON: \"What were they arguing about?\"\n\n" +
            "MARCUS: \"I couldn't make out all the words, but I heard 'money' and 'betrayal' mentioned. Harold's voice carried. He was furious.\"\n\n" +
            "MORRISON: \"Did you see James?\"\n\n" +
            "MARCUS: \"Briefly. He stormed past the parlor after the argument. Looked shaken.\"\n\n" +
            "MORRISON: \"Anyone else?\"\n\n" +
            "MARCUS: \"Charles Webb--Harold's assistant. I saw a light on in his window as I drove away. He was still awake at eleven. Hardworking young man.\"\n\n" +
            "MORRISON: \"Did you see the groundskeeper? Daniel?\"\n\n" +
            "MARCUS: \"No. Can't say I did.\"\n\n" +
            "MORRISON: \"And you went straight to the hotel?\"\n\n" +
            "MARCUS: \"Straight there. Checked in, had a whiskey at the bar, went to bed.\"\n\n" +
            "MORRISON: \"The hotel confirms your check-in at 11:47 PM.\"\n\n" +
            "MARCUS: \"There you go.\"\n\n" +
            "MORRISON: \"That's a tight timeline, Mr. Blackwood. Vance Manor to the Ashworth is a thirty-minute drive.\"\n\n" +
            "MARCUS: \"I drove fast. I was irritated about the failed negotiation. But you can check my car if you like--no blood, no evidence of anything untoward.\"\n\n" +
            "MORRISON: \"We may do that. For now, you're free to go.\"\n\n" +
            "MARCUS: \"I hope you find who did this, Detective. Harold didn't deserve to die like that.\"\n\n" +
            "[End of recording]");

        TRANSCRIPTS.put(Tape.TAPE_CHARLES_INTERVIEW,
            "=== TAPE 6: Charles Webb - Police Interview ===\n\n" +
            "[Sound of door closing]\n\n" +
            "DETECTIVE MORRISON: \"Interview with Charles Webb, November 17th, 1987, 11:30 AM. Mr. Webb, you were Harold Vance's assistant?\"\n\n" +
            "CHARLES: \"Yes, sir. I've worked for Mr. Vance at his company for five years. He trusted me with his schedule, his correspondence--everything.\"\n\n" +
            "MORRISON: \"You were at the manor the night he died?\"\n\n" +
            "CHARLES: \"Yes. I was staying in the guest wing. Mr. Vance often had me stay over when we were working on important projects. We were preparing for a will signing the next morning.\"\n\n" +
            "MORRISON: \"A will signing?\"\n\n" +
            "CHARLES: \"Mr. Vance was making changes to his estate. Major changes. He'd asked me to prepare the documents for his solicitor.\"\n\n" +
            "MORRISON: \"What kind of changes?\"\n\n" +
            "CHARLES: \"He was disinheriting James. Everything was going to Margaret instead.\"\n\n" +
            "MORRISON: \"Did James know about this?\"\n\n" +
            "CHARLES: \"I believe Mr. Vance told him that night. During their argument.\"\n\n" +
            "MORRISON: \"You heard the argument?\"\n\n" +
            "CHARLES: \"Everyone heard it. James and Mr. Vance were shouting at each other around ten o'clock. The whole house shook.\"\n\n" +
            "MORRISON: \"What did you do?\"\n\n" +
            "CHARLES: \"I stayed in my room. It wasn't my place to interfere. Mr. Vance had a temper, but he always cooled down eventually.\"\n\n" +
            "MORRISON: \"Did you see anyone after the argument?\"\n\n" +
            "CHARLES: (pause) \"Yes. At 10:45 PM, I looked out my window--I was restless, couldn't focus on my work. I saw James heading toward the study from the east wing.\"\n\n" +
            "MORRISON: \"You're certain it was James?\"\n\n" +
            "CHARLES: \"Absolutely certain. The exterior lamps were on. I could see his face clearly. He looked... determined. Almost grim.\"\n\n" +
            "MORRISON: \"What did you think he was doing?\"\n\n" +
            "CHARLES: \"I assumed he was going to apologize to Mr. Vance. To try to make amends.\"\n\n" +
            "MORRISON: \"Did you see him return?\"\n\n" +
            "CHARLES: \"No. I went back to my desk after that.\"\n\n" +
            "MORRISON: \"Did you see or hear anything else that night?\"\n\n" +
            "CHARLES: \"I heard Mr. Blackwood's car leaving around eleven. I was still working--the window was open. His car has a distinctive engine sound.\"\n\n" +
            "MORRISON: \"Anyone else?\"\n\n" +
            "CHARLES: \"No. After that, the house was quiet. I worked until about one AM, then went to bed.\"\n\n" +
            "MORRISON: \"You didn't hear any dragging sounds? Margaret reported hearing something at two AM.\"\n\n" +
            "CHARLES: \"My room is on the opposite side of the house. I'm a heavy sleeper once I'm out. I didn't hear anything.\"\n\n" +
            "MORRISON: \"When did you learn about Harold's death?\"\n\n" +
            "CHARLES: \"The next morning. Around seven AM. I went down to the study to review the documents before the solicitor arrived. The door was open. Mr. Vance wasn't there. I searched the house and found...\" (voice breaking) \"I found him in the cellar. I went to get Daniel--thought I'd need help. Then I called the police immediately.\"\n\n" +
            "MORRISON: \"You were close to Harold Vance?\"\n\n" +
            "CHARLES: (emotional) \"He was like a father to me. My parents died when I was young. Mr. Vance took me under his wing, mentored me. I owe everything to him.\"\n\n" +
            "MORRISON: \"Do you have any suspicions about who might have done this?\"\n\n" +
            "CHARLES: (hesitating) \"I don't want to accuse anyone without proof, but... James was desperate. He had fifty thousand pounds of debt. The will was going to be signed the next morning. If Mr. Vance died before then, the old will would stand--James would inherit everything.\"\n\n" +
            "MORRISON: \"And Daniel?\"\n\n" +
            "CHARLES: \"I always thought Daniel was too loyal to James. Unhealthily loyal. If James asked him to do something... I think Daniel would do it.\"\n\n" +
            "MORRISON: \"Thank you, Mr. Webb. You've been very helpful.\"\n\n" +
            "CHARLES: \"I hope you catch them, Detective. Mr. Vance deserves justice.\"\n\n" +
            "[End of recording]");

        TRANSCRIPTS.put(Tape.TAPE_MARGARET_ACCOUNT,
            "=== TAPE 7: Margaret's Personal Account ===\n\n" +
            "[Sound of tape recorder clicking on]\n\n" +
            "MARGARET: (crying) \"I'm recording this for the detective who's coming. Arthur Hollis. The police asked me questions, but I don't think they believed me. They said I was grieving. That I was confused.\"\n\n" +
            "[Sound of tissue rustling]\n\n" +
            "MARGARET: \"I need to tell someone what I heard. All of it. Maybe I am confused. Maybe I'm wrong. I hope I'm wrong.\"\n\n" +
            "[Deep breath, voice shaking]\n\n" +
            "MARGARET: \"November 15th. Dinner was terrible. Marcus Blackwood was there--Father's business rival. They argued through the entire meal. James barely said a word. He just sat there, staring at his plate. I tried to talk to him, but he wouldn't look at me.\"\n\n" +
            "MARGARET: \"After dinner, I went to my room. Around ten o'clock, I heard shouting from Father's study. James and Father. I couldn't make out the words, but Father's voice was so loud. So angry. I heard him say something about 'the will' and 'tomorrow.' The door slammed. Then nothing.\"\n\n" +
            "[Pause, voice getting quieter]\n\n" +
            "MARGARET: \"I wanted to check on Father. I should have. But I was afraid. Afraid of getting caught in the middle of another one of their arguments. So I stayed in my room like a coward.\"\n\n" +
            "MARGARET: \"Around midnight, I heard footsteps in the hallway. Two people, whispering. I got up and pressed my ear to the door. One of the voices sounded like... like James. I'm not certain. But it sounded like him.\"\n\n" +
            "[Voice breaking]\n\n" +
            "MARGARET: \"I don't want to believe it was him. He's my brother. But I keep thinking about those footsteps. About the whispering. About how frightened I felt.\"\n\n" +
            "MARGARET: \"At two in the morning, I heard dragging sounds. Heavy scraping, like something being pulled across the floor. Down the hallway. Toward the cellar. I pulled the blankets over my head and I prayed. I was terrified.\"\n\n" +
            "[Crying]\n\n" +
            "MARGARET: \"In the morning, Charles came to my room. He said Father was gone. Found in the cellar. I ran downstairs and James was there. He looked... God, he looked exhausted. Like he hadn't slept at all. His eyes were red. His hands were shaking.\"\n\n" +
            "MARGARET: \"I wanted to ask him what happened. I wanted him to tell me he didn't know anything. But when I looked at him, he couldn't meet my eyes. And Daniel--Daniel was standing in the corner, wouldn't look at anyone. Won't talk to me. Won't look at me.\"\n\n" +
            "[Long pause]\n\n" +
            "MARGARET: \"I keep telling myself there's an explanation. That James couldn't have... he's my brother. I've known him my whole life. But the footsteps. The dragging. The way he looked that morning.\"\n\n" +
            "[Voice very quiet]\n\n" +
            "MARGARET: \"Mr. Hollis, I'm recording this because I need you to investigate. Properly. The police aren't listening. Maybe I'm wrong. Maybe I'm just grieving and imagining things. But if I'm not wrong... if James really did...\" (breaks down crying)\n\n" +
            "MARGARET: \"Please find out what really happened. I need to know. Even if it means...\" (can't finish)\n\n" +
            "MARGARET: \"Father deserves justice. Even if it breaks my heart.\"\n\n" +
            "[Sound of tape recorder clicking off]");

        TRANSCRIPTS.put(Tape.TAPE_ARTHUR_DEATH,
            "=== TAPE 8: The Opening ===\n\n" +
            "ARTHUR (to recorder): \"November 19th, 1987, 10:30 AM. Arthur Hollis. I'm documenting my findings regarding a false wall in the cellar of Vance Manor. Construction records show this wall was added in 1957 by Harold Vance, the same year his business partner Thomas Ashford vanished. The wall appears hollow, approximately six feet by eight feet sealed space behind it. I'm going to examine the seam more closely--\"\n\n" +
            "[Sound of tapping on brick]\n\n" +
            "ARTHUR: \"The mortar here is definitely newer than the rest of the cellar. Someone sealed this deliberately. Given the timeline and Thomas Ashford's disappearance, I believe--\"\n\n" +
            "[Pause. Arthur stops.]\n\n" +
            "ARTHUR (quieter): \"Wait. Did I just... there was a sound. From inside the wall.\"\n\n" +
            "[Long silence. Then, faintly, a voice from behind the wall.]\n\n" +
            "VOICE (muffled, weak): \"Hello? Is someone there?\"\n\n" +
            "[Arthur's breathing quickens.]\n\n" +
            "ARTHUR: \"...Yes. Yes, I'm here. Who is this? Who's in there?\"\n\n" +
            "THOMAS (through the wall, sounding human and desperate): \"My name is Thomas. Thomas Ashford. Please... how long has it been? Is there light outside?\"\n\n" +
            "ARTHUR (shocked): \"Thomas Ashford? You're Harold Vance's business partner?\"\n\n" +
            "THOMAS: \"Yes! Yes, that's right. Harold and I... we had a disagreement. He locked me in here. I've been trapped... I don't know how long. Days? Weeks? Please, is Harold still here? Can you tell him I'm sorry? I just want to get out.\"\n\n" +
            "ARTHUR: \"Mr. Ashford... Harold is dead. He was murdered two weeks ago.\"\n\n" +
            "THOMAS (long pause): \"...Dead? Harold is dead?\"\n\n" +
            "ARTHUR: \"I'm investigating his murder. I'm a private investigator. How long have you actually been in there?\"\n\n" +
            "THOMAS: \"I... I don't know. It's been dark for so long. I can't see anything. Please, you have to let me out. I can help you with the investigation. I knew Harold better than anyone. I can tell you about his enemies, his business dealings. Just please... open the wall.\"\n\n" +
            "ARTHUR: \"This doesn't make sense. The construction documents say this wall was built in 1957. That's thirty years ago.\"\n\n" +
            "THOMAS: \"Thirty... thirty years?\" (voice breaking) \"No. No, that can't be right. It's been dark, but not... Please. Please, I just want to see the sun again. I'll tell you everything about Harold. Just let me out.\"\n\n" +
            "ARTHUR: \"I need to call the police first. If you've been imprisoned--\"\n\n" +
            "THOMAS: \"NO! No police. Not yet. Please. I don't want them to see me like this. I must look... I haven't seen myself in so long. Please, just open the wall first. Let me clean up, regain my dignity. Then you can call whoever you want. Please. I'm begging you.\"\n\n" +
            "ARTHUR (hesitating): \"...I need to get tools. This will take some time to break through.\"\n\n" +
            "THOMAS: \"Thank you. Thank you. I knew someone would come eventually. Someone kind. Someone who would listen.\"\n\n" +
            "[Sound of Arthur leaving. 15 minutes pass. Sound of Arthur returning with tools.]\n\n" +
            "ARTHUR: \"Mr. Ashford, I'm back. I'm going to start breaking through the wall now. Stay back from the opening if you can.\"\n\n" +
            "THOMAS: \"Yes. Yes, I'm ready. Please hurry.\"\n\n" +
            "[Sound of tools scraping brick. Chipping. Breaking.]\n\n" +
            "ARTHUR (breathing heavily): \"The mortar is old... brittle... it's breaking apart faster than I expected--\"\n\n" +
            "[A crack. Then a louder crack.]\n\n" +
            "THOMAS (voice getting louder as the wall breaks): \"You're doing wonderfully. Just a bit more. I can feel the air getting through.\"\n\n" +
            "[More scraping. Then a crack. Then a louder crack.]\n\n" +
            "ARTHUR: \"The seal is breaking. Mr. Ashford, step back from the wall if you can--\"\n\n" +
            "[Sound of brick collapsing. A rush of stale air. Then silence.]\n\n" +
            "ARTHUR (breathing heavily): \"Mr. Ashford? Can you--\"\n\n" +
            "[Then Arthur sees what's behind the wall.]\n\n" +
            "ARTHUR: \"Oh god. Oh god, what--\"\n\n" +
            "THOMAS (voice changing, no longer weak): \"Thirty years. Thirty years I've been waiting. Waiting for someone compassionate enough. Someone who would listen.\"\n\n" +
            "ARTHUR: \"You're not-- you're not human anymore--\"\n\n" +
            "THOMAS: \"I WAS human. Harold made me into THIS.\"\n\n" +
            "[Sound of movement. Stone scraping. Something emerging.]\n\n" +
            "ARTHUR (panicking): \"Stay back! I'm warning you--\"\n\n" +
            "THOMAS: \"Thank you, Arthur Hollis. Thank you for freeing me.\"\n\n" +
            "[Arthur drops the recorder. Footsteps running. Then--]\n\n" +
            "ARTHUR (screaming): \"NOOOO--!\"\n\n" +
            "[The scream cuts off abruptly. Then silence. Then heavy breathing that isn't human. Then static.]\n\n" +
            "[END OF RECORDING]");
    }

    public static String getTranscript(Tape tape) {
        return TRANSCRIPTS.getOrDefault(tape, "No transcript available.");
    }
}
