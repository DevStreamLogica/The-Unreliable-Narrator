package com.dsa.game.state;

public enum Evidence {
    LETTER_OPENER("Letter Opener", "A silver letter opener found on Harold's desk. Could be the murder weapon."),
    TORN_LETTER("Torn Letter", "A partially destroyed letter in the fireplace. Mentions 'the will' and 'betrayal'."),
    FINANCIAL_RECORDS("Financial Records", "Hidden in a desk drawer. Shows large transfers to an unknown account."),
    MUDDY_BOOTS("Muddy Boots", "Boots with fresh mud, found near the kitchen. Someone went outside recently."),
    SLEEPING_POWDER("Sleeping Powder", "A vial of sleeping powder hidden in the flour tin."),
    BLOODSTAINED_CUFF("Bloodstained Cuff", "A shirt cuff with blood spots, found under a loose floorboard."),
    WILL_COPY("Will Copy", "A copy of Harold's will. James inherits everything, Margaret gets nothing."),
    BLACKMAIL_NOTE("Blackmail Note", "A note demanding silence. 'I know what you did. Pay or I talk.'"),
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
