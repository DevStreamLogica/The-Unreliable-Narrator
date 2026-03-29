package com.dsa.game.systems;

import com.dsa.game.state.Evidence;
import com.dsa.game.state.Tape;

public class ExamResult {

    public enum MiniGameType {
        NONE,
        TORN_LETTER_RECONSTRUCTION
    }

    private final String text;
    private final Evidence evidence;  // nullable
    private final Tape tape;          // nullable
    private final MiniGameType miniGame;

    public ExamResult(String text) {
        this(text, null, null, MiniGameType.NONE);
    }

    public ExamResult(String text, Evidence evidence) {
        this(text, evidence, null, MiniGameType.NONE);
    }

    public ExamResult(String text, Tape tape) {
        this(text, null, tape, MiniGameType.NONE);
    }

    public ExamResult(String text, Evidence evidence, Tape tape) {
        this(text, evidence, tape, MiniGameType.NONE);
    }

    public ExamResult(String text, MiniGameType miniGame) {
        this(text, null, null, miniGame);
    }

    public ExamResult(String text, Evidence evidence, MiniGameType miniGame) {
        this(text, evidence, null, miniGame);
    }

    public ExamResult(String text, Evidence evidence, Tape tape, MiniGameType miniGame) {
        this.text = text;
        this.evidence = evidence;
        this.tape = tape;
        this.miniGame = miniGame;
    }

    public String getText() { return text; }
    public Evidence getEvidence() { return evidence; }
    public Tape getTape() { return tape; }
    public MiniGameType getMiniGame() { return miniGame; }
    public boolean hasEvidence() { return evidence != null; }
    public boolean hasTape() { return tape != null; }
    public boolean hasMiniGame() { return miniGame != MiniGameType.NONE; }
}
