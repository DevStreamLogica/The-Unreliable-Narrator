package com.dsa.game.systems;

import com.dsa.game.state.Evidence;
import com.dsa.game.state.Tape;

public class ExamResult {

    private final String text;
    private final Evidence evidence;  // nullable
    private final Tape tape;          // nullable

    public ExamResult(String text) {
        this(text, null, null);
    }

    public ExamResult(String text, Evidence evidence) {
        this(text, evidence, null);
    }

    public ExamResult(String text, Tape tape) {
        this(text, null, tape);
    }

    public ExamResult(String text, Evidence evidence, Tape tape) {
        this.text = text;
        this.evidence = evidence;
        this.tape = tape;
    }

    public String getText() { return text; }
    public Evidence getEvidence() { return evidence; }
    public Tape getTape() { return tape; }
    public boolean hasEvidence() { return evidence != null; }
    public boolean hasTape() { return tape != null; }
}
