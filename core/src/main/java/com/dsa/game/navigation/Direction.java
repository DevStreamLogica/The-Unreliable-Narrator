package com.dsa.game.navigation;

public enum Direction {
    NORTH("Forward", "Go forward"),
    SOUTH("Back", "Go back"),
    EAST("Right", "Turn right"),
    WEST("Left", "Turn left"),
    UP("Upstairs", "Go upstairs"),
    DOWN("Downstairs", "Go downstairs"),
    ENTER("Enter", "Enter");

    private String displayName;
    private String actionText;

    Direction(String displayName, String actionText) {
        this.displayName = displayName;
        this.actionText = actionText;
    }

    public String getDisplayName() { return displayName; }
    public String getActionText() { return actionText; }
}
