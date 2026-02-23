package com.dsa.game.state;

public enum EntityAnomaly {
    BREATHING_WALL("Breathing Wall", "The cellar wall seems to pulse slightly, as if something behind it is breathing."),
    THOMAS_REFERENCE("Thomas Reference", "A name -- 'Thomas' -- scratched into the wood. No one in the household is named Thomas."),
    NARRATOR_I_SLIP("Narrator 'I' Slip", "The narrator said 'I remember' before correcting himself. Who is he remembering as?"),
    SCRATCHED_INITIALS("Scratched Initials", "Initials 'A.H.' are scratched into the bedpost. Arthur Hollis -- the investigator who came before you."),
    PHOTO_UNKNOWN_MAN("Unknown Man in Photo", "A photograph shows Harold with an unidentified man. The face has been scratched out."),
    COLD_SPOT_CELLAR("Cold Spot", "An inexplicable cold spot near the cellar wall. Your breath mists even when the rest of the room is warm."),
    CONSTRUCTION_RECORD("Construction Record", "Records show the cellar wall was added in 1957 -- decades after the house was built. The invoice is marked 'URGENT - seal immediately.' A margin note in Harold's handwriting reads: 'He left me no choice.' The same year Thomas Ashford disappeared.");

    private final String displayName;
    private final String description;

    EntityAnomaly(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
