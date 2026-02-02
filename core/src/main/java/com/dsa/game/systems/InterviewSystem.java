package com.dsa.game.systems;

import com.dsa.game.data.SuspectDialogue;
import com.dsa.game.state.*;

import java.util.List;

public class InterviewSystem {

    private final GameState state;

    public InterviewSystem(GameState state) {
        this.state = state;
    }

    /** Start an interview. Returns the greeting text. */
    public String startInterview(Suspect suspect) {
        state.setCurrentInterviewSuspect(suspect);
        return SuspectDialogue.getGreeting(suspect);
    }

    /** End the current interview. */
    public void endInterview() {
        state.setCurrentInterviewSuspect(null);
    }

    /** Get available topics for current suspect. */
    public List<String> getAvailableTopics() {
        Suspect s = state.getCurrentInterviewSuspect();
        if (s == null) return java.util.Collections.emptyList();
        return SuspectDialogue.getTopics(s);
    }

    /** Ask a topic. Returns response text. Degrades cooperation. */
    public String askTopic(String topic) {
        Suspect s = state.getCurrentInterviewSuspect();
        if (s == null) return "You're not interviewing anyone.";

        state.markTopicAsked(s, topic);

        // Degrade cooperation slightly with repeated questioning
        state.adjustCooperation(s, -3);

        // 4-tier cooperation response system
        int coop = state.getCooperation(s);
        if (coop < 10) {
            return s.getDisplayName() + " refuses to even look at you. \"Get out. Now. Before I call the authorities myself.\"";
        }
        if (coop < 25) {
            return s.getDisplayName() + " snaps: \"I have nothing more to say to you!\" They turn away sharply.";
        }
        if (coop < 40) {
            return s.getDisplayName() + " answers reluctantly, clearly agitated. " + SuspectDialogue.getResponse(s, topic);
        }
        if (coop < 60) {
            return s.getDisplayName() + " hesitates before answering. " + SuspectDialogue.getResponse(s, topic);
        }

        return SuspectDialogue.getResponse(s, topic);
    }

    /** Returns true if the topic is dangerous (cellar-related, Victor-related). */
    public boolean isDangerousTopic(Suspect suspect, String topic) {
        String lower = topic.toLowerCase();
        return lower.contains("cellar") || lower.contains("victor") || lower.contains("tape_7")
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
                return "You point out that the letter opener doesn't match the wound. James goes pale. \"I... I don't know what you're talking about. The letter opener was always on the desk.\"";
            case DANIEL:
                return "Daniel's jaw clenches when you mention the weapon discrepancy. \"Maybe the doctors got it wrong. I wouldn't know about such things.\" But he knows something.";
            default:
                return s.getDisplayName() + " considers the contradiction carefully but offers no useful insight.";
        }
    }

    /** Present body position contradiction during interview. */
    public String presentBodyContradiction() {
        Suspect s = state.getCurrentInterviewSuspect();
        if (s == null) return "You're not interviewing anyone.";

        state.discoverContradiction(Contradiction.BODY_POSITION);

        switch (s) {
            case JAMES:
                return "\"The body was moved?\" James whispers. He stares at his hands. \"I didn't... we didn't mean...\" He catches himself. \"I don't know anything about that.\"";
            case MARGARET:
                return "Margaret nods slowly. \"I heard dragging sounds that night. From the cellar direction. Two people, at least. One was James. I'm almost certain now.\"";
            case DANIEL:
                return "Daniel's face goes blank. \"Bodies don't move themselves. If someone moved Harold, you should ask who had access to the house. And a reason.\" He's deflecting.";
            case ELEANOR:
                return "Eleanor gasps. \"Moved? The body was moved? That explains the scratches on the servants' staircase! I thought furniture was being rearranged, but at two in the morning...\"";
            default:
                return s.getDisplayName() + " seems disturbed by this information but adds nothing useful.";
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
                    return "You lay out the evidence against James. His composure cracks completely. " +
                        "\"You don't understand the pressure I was under! Father was going to cut me off -- " +
                        "everything I'd built, gone!\" He slams his fist on the table. \"We moved him, " +
                        "alright? Daniel and I moved the body to the cellar. We panicked. But I didn't -- " +
                        "I didn't mean for it to go that far.\"\n\n" +
                        "[CONTRADICTION DISCOVERED: Body Position -- The body was moved after death.]";
                }
                return "You confront James with everything you know. He stares at you with hollow eyes. " +
                    "\"You think you know what happened? You don't know the half of it. This house... " +
                    "this family... we're all guilty of something.\" He refuses to say more.";

            case DANIEL:
                // May auto-discover weapon contradiction
                if (!state.hasContradiction(Contradiction.WEAPON) && state.hasEvidence(Evidence.LETTER_OPENER)) {
                    state.discoverContradiction(Contradiction.WEAPON);
                    return "You press Daniel with the evidence. The groundskeeper's calm facade shatters. " +
                        "\"The letter opener? That's not even the real weapon! James grabbed it from the " +
                        "study, but Harold was already... Look, I was just supposed to clean up. James " +
                        "said it would be clean. It wasn't.\"\n\n" +
                        "[CONTRADICTION DISCOVERED: Weapon -- The letter opener doesn't match the wound.]";
                }
                return "You confront Daniel. He turns away, jaw tight. \"I'm just the groundskeeper. " +
                    "I keep the grounds. That's all I do.\" But his hands are shaking. \"You're " +
                    "making a mistake pushing this. Some things are better left buried. Like what's " +
                    "in that cellar.\"";

            case MARGARET:
                return "You present your case to Margaret. Her eyes fill with tears, but her voice " +
                    "stays steady. \"I knew. Of course I knew something was wrong. Harold changed his " +
                    "will three times in the last year. James was desperate, Daniel was always lurking " +
                    "around. But what could I do? Who would believe the wife who gets nothing?\" She " +
                    "pauses. \"I wrote that letter -- the torn one. I was trying to warn Harold. I was " +
                    "too late.\"";

            case ELEANOR:
                return "You lay out what you've found to Eleanor. The housekeeper sits heavily in her " +
                    "chair. \"I've served this family for thirty years. I saw James sneaking around the " +
                    "night it happened. I heard the cellar door. I found the sleeping powder missing from " +
                    "the kitchen.\" She wrings her hands. \"I should have said something sooner. I was " +
                    "afraid they'd come after me next.\"";

            case REGINALD:
                return "You confront Reginald with your evidence. The butler's professional mask slips " +
                    "for just a moment. \"Sir, I pride myself on discretion. But you've found enough that " +
                    "silence helps no one.\" He straightens his jacket. \"I saw Daniel entering through " +
                    "the study window at half past midnight. James was waiting inside. I heard raised " +
                    "voices, then silence. I should have intervened. I did not. That is my shame.\"";

            default:
                return suspect.getDisplayName() + " has nothing more to say.";
        }
    }

    public boolean isInterviewActive() {
        return state.getCurrentInterviewSuspect() != null;
    }

    public Suspect getCurrentSuspect() {
        return state.getCurrentInterviewSuspect();
    }
}
