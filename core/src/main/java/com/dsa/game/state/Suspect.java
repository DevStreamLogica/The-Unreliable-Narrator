package com.dsa.game.state;

public enum Suspect {
    JAMES("James Vance", 52, 70),
    MARGARET("Margaret Vance", 48, 60),
    DANIEL("Daniel the Groundskeeper", 63, 50),
    MARCUS("Marcus Blackwood", 55, 55),
    CHARLES("Charles Webb", 28, 70);

    private final String displayName;
    private final int age;
    private final int startingCooperation;

    Suspect(String displayName, int age, int startingCooperation) {
        this.displayName = displayName;
        this.age = age;
        this.startingCooperation = startingCooperation;
    }

    public String getDisplayName() { return displayName; }
    public int getAge() { return age; }
    public int getStartingCooperation() { return startingCooperation; }
}
