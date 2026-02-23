package com.dsa.game.systems;

import com.dsa.game.data.NarratorText;
import com.dsa.game.data.SuspectDialogue;
import com.dsa.game.state.*;

import java.util.List;
import java.util.Random;

public class InterviewSystem {

    private final GameState state;
    private final Random random = new Random();

    public InterviewSystem(GameState state) {
        this.state = state;
    }

    public String startInterview(Suspect suspect) {
        state.setCurrentInterviewSuspect(suspect);
        return SuspectDialogue.getGreeting(suspect);
    }

    public void endInterview() {
        state.setCurrentInterviewSuspect(null);
    }

    public List<String> getAvailableTopics() {
        Suspect s = state.getCurrentInterviewSuspect();
        if (s == null) return java.util.Collections.emptyList();
        return SuspectDialogue.getTopics(s);
    }

    public String askTopic(String topic) {
        Suspect s = state.getCurrentInterviewSuspect();
        if (s == null) return "You're not interviewing anyone.";

        state.markTopicAsked(s, topic);

        state.adjustCooperation(s, -3);

        int coop = state.getCooperation(s);
        if (coop < 10) {
            // At very low cooperation, 33% chance of a parting lie
            if (SuspectDialogue.hasFalseResponse(s, topic) && random.nextInt(3) == 0) {
                String lieKey = s.name() + "_" + topic + "_lie";
                state.addReceivedLie(lieKey);
                return "[" + s.getDisplayName() + " turned back with a cold smile.]\n\n" +
                    SuspectDialogue.getFalseResponse(s, topic);
            }
            return NarratorText.getChannelingMemoryFade();
        }
        if (coop < 25) {
            // At low cooperation, deliver lies instead of refusals
            if (SuspectDialogue.hasFalseResponse(s, topic)) {
                String lieKey = s.name() + "_" + topic + "_lie";
                state.addReceivedLie(lieKey);
                return SuspectDialogue.getFalseResponse(s, topic);
            }
            return NarratorText.getChannelingMemoryFragment();
        }

        // Margaret deflection (Feature 3): at coop 25-59, 40% chance for non-Margaret suspects
        if (coop < 60 && s != Suspect.MARGARET
                && SuspectDialogue.hasMargaretDeflection(s)
                && !state.hasContradiction(Contradiction.BODY_POSITION)
                && random.nextInt(100) < 40) {
            return SuspectDialogue.getMargaretDeflection(s);
        }

        if (coop < 40) {
            return s.getDisplayName() + " answered reluctantly, clearly agitated. " + SuspectDialogue.getResponse(s, topic);
        }
        if (coop < 60) {
            return s.getDisplayName() + " hesitated before answering. " + SuspectDialogue.getResponse(s, topic);
        }

        return SuspectDialogue.getResponse(s, topic);
    }

    /** Returns true if the topic is dangerous (cellar-related, Arthur-related). */
    public boolean isDangerousTopic(Suspect suspect, String topic) {
        String lower = topic.toLowerCase();
        return lower.contains("cellar") || lower.contains("arthur")
            || lower.contains("entity") || lower.contains("wall");
    }

    /** Returns the awareness cost for asking a topic. 5 for dangerous, 1 otherwise. */
    public int getTopicAwarenessCost(Suspect suspect, String topic) {
        return isDangerousTopic(suspect, topic) ? 5 : 1;
    }

    /** Show evidence to current suspect. Returns reaction. Costs +2 awareness. */
    public String showEvidence(Evidence evidence) {
        Suspect s = state.getCurrentInterviewSuspect();
        if (s == null) return "You're not interviewing anyone.";

        // Showing evidence affects cooperation
        if (SuspectDialogue.hasEvidenceReaction(s, evidence)) {
            // Incriminating evidence lowers cooperation
            state.adjustCooperation(s, -5);
        }

        return SuspectDialogue.getEvidenceReaction(s, evidence);
    }

    /** Present weapon contradiction during interview. */
    public String presentWeaponContradiction() {
        Suspect s = state.getCurrentInterviewSuspect();
        if (s == null) return "You're not interviewing anyone.";

        if (!state.hasEvidence(Evidence.LETTER_OPENER)) {
            return "You don't have evidence to support this contradiction.";
        }

        state.discoverContradiction(Contradiction.WEAPON);

        switch (s) {
            case JAMES:
                return "You pointed out that the letter opener didn't match the wound. James went pale. \"I... I don't know what you're talking about. The letter opener was always on the desk.\"";
            case DANIEL:
                return "Daniel's jaw clenched when you mentioned the weapon discrepancy. \"Maybe the doctors got it wrong. I wouldn't know about such things.\" But he knew something.";
            default:
                return s.getDisplayName() + " considered the contradiction carefully but offered no useful insight.";
        }
    }

    /** Present body position contradiction during interview. */
    public String presentBodyContradiction() {
        Suspect s = state.getCurrentInterviewSuspect();
        if (s == null) return "You're not interviewing anyone.";

        state.discoverContradiction(Contradiction.BODY_POSITION);

        switch (s) {
            case JAMES:
                return "\"The body was moved?\" James whispered. He stared at his hands. \"I didn't... we didn't mean...\" He caught himself. \"I don't know anything about that.\"";
            case MARGARET:
                return "Margaret nodded slowly. \"I heard dragging sounds that night. From the cellar direction. Two people, at least. One was James. I'm almost certain now.\"";
            case DANIEL:
                return "Daniel's face went blank. \"Bodies don't move themselves. If someone moved Harold, you should ask who had access to the house. And a reason.\" He was deflecting.";
            case MARCUS:
                return "Marcus looked genuinely shocked. \"The body was moved? After death?\" He frowned. \"I left at eleven. Whatever happened after that... I wasn't here. But it sounds like more than one person was involved.\"";
            case CHARLES:
                return "Charles went pale. \"Moved? But that means...\" He swallowed hard. \"When I found the body that morning, something felt wrong. The position, the staging. It was too neat. Too deliberate. Like someone had arranged it.\"";
            default:
                return s.getDisplayName() + " seemed disturbed by this information but added nothing useful.";
        }
    }

    /** Present a confrontation to the current suspect. Costs -15 cooperation. May auto-discover contradictions. */
    public String presentConfrontation(Suspect suspect) {
        // Heavy cooperation hit
        state.adjustCooperation(suspect, -15);

        switch (suspect) {
            case JAMES:
                // May auto-discover body position contradiction
                if (!state.hasContradiction(Contradiction.BODY_POSITION)) {
                    state.discoverContradiction(Contradiction.BODY_POSITION);
                    return "You laid out the evidence against James. His composure cracked completely. " +
                        "\"You don't understand the pressure I was under! Father was going to cut me off -- " +
                        "everything I'd built, gone!\" He slammed his fist on the table. \"We moved him, " +
                        "alright? Daniel and I moved the body to the cellar. We panicked. But I didn't -- " +
                        "I didn't mean for it to go that far.\"\n\n" +
                        "[CONTRADICTION DISCOVERED: Body Position -- The body was moved after death.]";
                }
                return "You confronted James with everything you knew. He stared at you with hollow eyes. " +
                    "\"You think you know what happened? You don't know the half of it. This house... " +
                    "this family... we're all guilty of something.\" He refused to say more.";

            case DANIEL:
                // May auto-discover weapon contradiction
                if (!state.hasContradiction(Contradiction.WEAPON) && state.hasEvidence(Evidence.LETTER_OPENER)) {
                    state.discoverContradiction(Contradiction.WEAPON);
                    return "You pressed Daniel with the evidence. The groundskeeper's calm facade shattered. " +
                        "\"The letter opener? That's not even the real weapon! James grabbed it from the " +
                        "study, but Harold was already... Look, I was just supposed to clean up. James " +
                        "said it would be clean. It wasn't.\"\n\n" +
                        "[CONTRADICTION DISCOVERED: Weapon -- The letter opener doesn't match the wound.]";
                }
                return "You confronted Daniel. He turned away, jaw tight. \"I'm just the groundskeeper. " +
                    "I keep the grounds. That's all I do.\" But his hands were shaking. \"You're " +
                    "making a mistake pushing this. Some things are better left buried. Like what's " +
                    "in that cellar.\"";

            case MARGARET:
                return "You presented your case to Margaret. Her eyes filled with tears, but her voice " +
                    "stayed steady. \"I knew. Of course I knew something was wrong. Harold changed his " +
                    "will three times in the last year. James was desperate, Daniel was always lurking " +
                    "around. But what could I do? Who would believe the wife who gets nothing?\" She " +
                    "paused. \"I wrote that letter -- the torn one. I was trying to warn Harold. I was " +
                    "too late.\"";

            case MARCUS:
                return "You confronted Marcus with your evidence. The businessman's composure cracked. " +
                    "\"Fine. You want to know what I saw?\" He took a breath. \"Before I left, I saw " +
                    "James heading toward the study. He looked... determined. Desperate, even. And " +
                    "Daniel was watching from the shadows near the kitchen. They exchanged a look -- " +
                    "like they had an understanding.\" He shrugged. \"I thought it was family business.\"";

            case CHARLES:
                return "You laid out your evidence to Charles. The assistant's grief turned to something " +
                    "harder. \"I knew something was wrong. Mr. Vance was reviewing the company books late " +
                    "at night, furious about something. I should have asked. I should have done more.\" He looked at you " +
                    "directly. \"I saw James go to the study at quarter to eleven. And looking back now... " +
                    "Daniel was too loyal to James. Unhealthily loyal. If James asked him to do something... " +
                    "I think Daniel would have done it without question.\"";

            default:
                return suspect.getDisplayName() + " had nothing more to say.";
        }
    }

    public boolean isInterviewActive() {
        return state.getCurrentInterviewSuspect() != null;
    }

    public Suspect getCurrentSuspect() {
        return state.getCurrentInterviewSuspect();
    }
}
