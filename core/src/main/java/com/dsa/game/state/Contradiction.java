package com.dsa.game.state;

public enum Contradiction {
    WEAPON("The letter opener doesn't match the wound pattern. The real weapon is still missing."),
    BODY_POSITION("Harold was moved after death. The blood pooling doesn't match where the body was found.");

    private final String description;

    Contradiction(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
