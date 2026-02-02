package com.dsa.game.state;

public enum Suspect {
    JAMES("James Vance", 70),
    MARGARET("Margaret Vance", 60),
    DANIEL("Daniel the Groundskeeper", 50),
    ELEANOR("Eleanor the Housekeeper", 80),
    REGINALD("Reginald the Butler", 75);

    private final String displayName;
    private final int startingCooperation;

    Suspect(String displayName, int startingCooperation) {
        this.displayName = displayName;
        this.startingCooperation = startingCooperation;
    }

    public String getDisplayName() { return displayName; }
    public int getStartingCooperation() { return startingCooperation; }
}
