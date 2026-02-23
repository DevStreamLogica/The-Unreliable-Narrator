package com.dsa.game.state;

public enum Evidence {
    LETTER_OPENER("Letter Opener", "A silver letter opener found on Harold's desk. Blood on the handle, but forensic analysis shows it doesn't match the wound pattern. Someone wanted you to think this was the murder weapon."),
    FIREPLACE_POKER("Fireplace Poker", "A heavy iron poker from the study fireplace. Blood traces on the weighted end match Harold's skull fracture. This is the real murder weapon -- hidden in plain sight."),
    TORN_LETTER("Torn Letter", "A partially destroyed letter in the fireplace. Mentions 'the will' and 'betrayal'."),
    FINANCIAL_RECORDS("Financial Records", "Hidden in a desk drawer. Shows large transfers to an unknown account."),
    MUDDY_BOOTS("Muddy Boots", "Daniel's work boots, caked with dirt and cellar dust. Fresh mud tracked through the manor the night of the murder -- the same night Daniel claims he stayed in his shed all night."),
    SLEEPING_POWDER("Sleeping Powder", "A vial of sleeping powder discovered in the kitchen. Someone was drugging Harold's evening tea."),
    BLOODSTAINED_CUFF("Bloodstained Cuff", "A shirt with a bloodstained cuff, hidden behind flour sacks in the cellar."),
    WILL_COPY("Will Copy", "A copy of Harold's existing, signed will. James inherits everything, Margaret gets nothing -- the very will Harold was about to change."),
    BLACKMAIL_NOTE("Blackmail Note", "A blackmail note with rough handwriting, planted in Margaret's room. The handwriting is deliberately crude, mimicking Daniel's style. James planted this to frame Margaret and make her look suspicious. It was never a real blackmail note."),
    GROUNDSKEEPER_LOG("Groundskeeper Log", "Daniel's logbook. An entry is missing for the night of the murder.");

    private final String displayName;
    private final String description;

    Evidence(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
