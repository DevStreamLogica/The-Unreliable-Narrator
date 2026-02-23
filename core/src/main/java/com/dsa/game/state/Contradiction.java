package com.dsa.game.state;

public enum Contradiction {
    WEAPON("The letter opener doesn't match the wound pattern -- it was planted. The fireplace poker shows blood traces matching Harold's skull fracture. James used the poker, then staged the letter opener to mislead investigators."),
    BODY_POSITION("Harold was moved after death. The blood pooling pattern shows he died in the study, but the body was found in the cellar. It took two people to move him -- James and Daniel working together between 11:30 PM and 2:00 AM."),

    NARRATOR_WEAPON("The narrator claimed the letter opener was the murder weapon, but forensic evidence contradicts this."),
    NARRATOR_BOOTS("The narrator said no one left the house that night, but muddy boots prove otherwise."),
    NARRATOR_LETTER("The narrator called the torn letter 'unimportant,' but it reveals the motive for murder."),
    NARRATOR_TIME("The narrator claimed Harold died after midnight, but Charles saw James heading to the study at 10:45 PM -- and the body was moved before Marcus left at 11. The murder happened between 10:45 and 11:00 PM, not after midnight.");

    private final String description;

    Contradiction(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    public boolean isNarratorContradiction() {
        return this == NARRATOR_WEAPON || this == NARRATOR_BOOTS
            || this == NARRATOR_LETTER || this == NARRATOR_TIME;
    }
}
