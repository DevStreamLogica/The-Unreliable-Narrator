package com.dsa.game.systems;

import com.dsa.game.navigation.Room;
import com.dsa.game.state.*;
import com.dsa.game.state.EntityAnomaly;

public class ExaminationSystem {

    private final GameState state;

    public ExaminationSystem(GameState state) {
        this.state = state;
    }

    public ExamResult examine(Room.RoomID room, String object) {
        state.incrementExamCount(room, object);
        int count = state.getExamCount(room, object);

        switch (room) {
            case STUDY: return examineStudy(object, count);
            case PARLOR: return examineParlor(object, count);
            case KITCHEN: return examineKitchen(object, count);
            case GUEST_ROOMS: return examineGuestRooms(object, count);
            case JAMES_ROOM: return examineJamesRoom(object, count);
            case MARGARET_ROOM: return examineMargaretRoom(object, count);
            case GROUNDSKEEPER_SHED: return examineShed(object, count);
            case SERVANTS_QUARTERS: return examineServants(object, count);
            case CELLAR: return examineCellar(object, count);
            default: return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineStudy(String object, int count) {
        switch (object) {
            case "desk":
                if (count == 1) return new ExamResult(
                    "Harold's mahogany desk. Papers are scattered across it -- financial documents, correspondence. A silver letter opener catches your eye, positioned oddly far from the papers.",
                    Evidence.LETTER_OPENER);
                return new ExamResult("The desk. You've already found the letter opener. The papers seem mostly routine correspondence.");

            case "drawers":
                if (count == 1) return new ExamResult(
                    "You pull open the desk drawers. Most contain ordinary supplies, but a hidden compartment in the bottom drawer holds a leather folder with financial records showing large, unexplained transfers.",
                    Evidence.FINANCIAL_RECORDS);
                return new ExamResult("The drawers. You've already found the hidden financial records.");

            case "papers":
                if (count == 1) return new ExamResult("Correspondence, bills, invitations. Nothing immediately suspicious, but one letter from a solicitor mentions 'urgent changes to the estate documents.'");
                if (count == 2) {
                    state.discoverAnomaly(EntityAnomaly.CONSTRUCTION_RECORD);
                    return new ExamResult("Shuffling through the papers again, you find an old construction invoice from 1957. A contractor was paid to 'seal the lower cellar extension -- URGENT.' The house was built in 1890. Why add a wall sixty years later?\n\nA margin note in Harold's handwriting reads: 'He left me no choice. Ashford threatened to expose everything. This is the only way.'\n\n1957 -- the same year Harold's business partner Thomas Ashford vanished without a trace.\n\n[ANOMALY DISCOVERED: Construction Record]");
                }
                return new ExamResult("The same papers as before. The solicitor's letter and the 1957 construction invoice stand out.");

            case "bookshelves":
                if (count == 1) return new ExamResult(
                    "Floor-to-ceiling bookshelves. Running your hand behind the books on the third shelf, your fingers find a small tape recorder wedged behind a volume of Shakespeare.",
                    Tape.TAPE_JAMES_INTERVIEW);
                if (count == 2) {
                    state.discoverAnomaly(EntityAnomaly.PHOTO_UNKNOWN_MAN);
                    return new ExamResult("Looking more carefully, you notice a photograph tucked behind the books. Harold stands with an unidentified man in front of the cellar door. The man's face has been violently scratched out of the photograph.\n\n[ANOMALY DISCOVERED: Unknown Man in Photo]");
                }
                return new ExamResult("The bookshelves. Leather-bound volumes in neat rows. You already found the tape and the photograph.");

            case "window":
                if (count == 1) return new ExamResult("Large bay windows overlooking the grounds. The latch is unlocked -- unusual for a winter night. Fresh scuff marks on the windowsill, both inside and out. Someone climbed through here recently.");
                if (count == 2) return new ExamResult("The scuff marks are fresh -- boot prints in the frost on the outer sill. Whoever came through here was careful, moving at night when the household was asleep. They didn't use the front door. They couldn't have. Someone with access to the grounds.");
                return new ExamResult("The window. The unlocked latch and frost-covered boot prints on the sill remain -- someone came in from the grounds that night.");

            case "fireplace":
                if (count == 1) return new ExamResult("A large stone fireplace. The fire has burned down to embers, but the ashes look fresh. Something was burned here recently -- not just firewood.");
                if (count == 2) {
                    state.discoverAnomaly(EntityAnomaly.THOMAS_REFERENCE);
                    return new ExamResult("Examining the fireplace stones more carefully, you notice scratches on the inner wall. Letters, carved with something sharp: 'THOMAS WAS RIGHT.' No one in the household is named Thomas.\n\n[ANOMALY DISCOVERED: Thomas Reference]");
                }
                return new ExamResult("The fireplace. The unusual ashes and the scratched name 'THOMAS' remain.");

            case "poker":
                if (count == 1) return new ExamResult("An iron poker rests beside the fireplace. Standard fireplace equipment. Wait -- there's a faint reddish-brown stain near the weighted end that doesn't match soot or rust.");
                if (count == 2) return new ExamResult(
                    "Looking more closely at the poker, you notice the handle has been recently wiped down -- aggressively. But the weighted end still shows traces of blood caught in the ornate metalwork. This is the murder weapon.",
                    Evidence.FIREPLACE_POKER);
                return new ExamResult("The fireplace poker. The real murder weapon.");

            case "ashes":
                if (count == 1) return new ExamResult(
                    "Sifting through the ashes carefully, you find fragments of a burned letter. The pieces are scattered but might be reconstructed...",
                    ExamResult.MiniGameType.TORN_LETTER_RECONSTRUCTION);
                return new ExamResult("You've already sifted through the ashes and found the torn letter fragments.");

            case "under_desk":
                if (count == 1) return new ExamResult(
                    "Crouching under the desk, you find a small tape recorder taped to the underside. Someone was recording conversations in this room.",
                    Tape.TAPE_ARGUMENT);
                return new ExamResult("Nothing else under the desk.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineParlor(String object, int count) {
        switch (object) {
            case "grandfather_clock":
                if (count == 1) return new ExamResult(
                    "An ornate grandfather clock. The pendulum swings steadily. Opening the clock case to adjust the time, you discover a tape recorder hidden behind the mechanism.",
                    Tape.TAPE_CHARLES_INTERVIEW);
                return new ExamResult("The grandfather clock ticks on. You already found the tape inside.");

            case "briefcase":
                if (count == 1) return new ExamResult(
                    "A leather briefcase by one of the chairs. Inside: legal documents, a copy of Harold's current will. James inherits everything. Margaret receives nothing. Tucked beneath the documents, you find a tape recorder.",
                    Evidence.WILL_COPY, Tape.TAPE_MARCUS_INTERVIEW);
                return new ExamResult("The briefcase. You already have the will copy and found the tape.");

            case "fireplace":
                if (count == 1) return new ExamResult("The parlor fireplace. Cold now. A few pieces of half-burned paper, but too damaged to read. The mantle holds family photographs -- Harold, James, and Margaret, looking strained.");
                return new ExamResult("The cold fireplace and strained family photos.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineKitchen(String object, int count) {
        switch (object) {
            case "storage_cellar":
                if (count == 1) return new ExamResult(
                    "The narrow door to the cellar storage. Beside it, a shelf holds tea supplies. Reaching behind a canister, you find a small glass vial. The label reads 'Chloral Hydrate' -- a powerful sleeping powder. Behind another canister, a tape recorder sits wedged between jars.",
                    Evidence.SLEEPING_POWDER, Tape.TAPE_MARGARET_INTERVIEW);
                if (count == 2) {
                    state.discoverAnomaly(EntityAnomaly.COLD_SPOT_CELLAR);
                    return new ExamResult("You examine the cellar door again. The metal handle is ice-cold to the touch, far colder than it should be. A distinct chill emanates from below -- from the cellar itself. Your breath mists briefly.\n\n[ANOMALY DISCOVERED: Cold Spot]");
                }
                return new ExamResult("The cellar door. The handle remains unnaturally cold. You already found the sleeping powder on the nearby shelf.");

            case "flour_tin":
                if (count == 1) return new ExamResult(
                    "A large flour tin on the shelf. You check inside but find only flour. It's been used recently -- fingerprints in the dust.");
                return new ExamResult("The flour tin. Just flour inside.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineGuestRooms(String object, int count) {
        return new ExamResult("Nothing noteworthy here.");
    }

    private ExamResult examineJamesRoom(String object, int count) {
        switch (object) {
            case "coat":
                if (count == 1) return new ExamResult("James's coat hangs on the wardrobe door. Checking the pockets, you find nothing. But the right sleeve -- the cuff is stained dark. Blood. James was wearing this the night of the murder. Where is the shirt that went with it?");
                return new ExamResult("James's coat. The bloodstained cuff is damning, but a coat alone isn't enough evidence. Where did he hide the shirt?");

            case "wardrobe":
                if (count == 1) return new ExamResult("The wardrobe door hangs open. Inside, clothes are hastily thrown about. Nothing suspicious except the coat hanging on the door.");
                return new ExamResult("The wardrobe. Messy, just like the rest of James's room.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineMargaretRoom(String object, int count) {
        switch (object) {
            case "letter":
                if (count == 1) return new ExamResult(
                    "On Margaret's dresser, a folded note. Opening it: 'I know what you did. Pay what you owe, or I tell everything. You know where to leave the money.' No signature. The handwriting is rough, uneducated -- not Margaret's elegant script. This note wasn't written TO her. Someone planted it here.",
                    Evidence.BLACKMAIL_NOTE);
                return new ExamResult("The dresser where you found the blackmail note.");

            case "dresser":
                if (count == 1) {
                    state.setHasTapeRepairKit(true);
                    return new ExamResult(
                        "Margaret's dresser. On the nightstand beside it, a tape recorder with a handwritten label: 'For the detective.'\n\n" +
                        "Beside the recorder, a small leather case holds tape splicing tools -- scissors, adhesive strips, " +
                        "a manual splicer. Margaret clearly prepared these recordings carefully.\n\n" +
                        "[Tape repair kit acquired. You can now repair damaged tapes.]",
                        Tape.TAPE_MARGARET_ACCOUNT);
                }
                if (count == 2) return new ExamResult("Looking more carefully at the dresser area, you notice a pair of shoes near the door with dark reddish-brown stains. A half-empty bottle of port wine sits on top. The stains on the shoes... could they be wine spills? Or something worse?");
                return new ExamResult("Margaret's dresser. The half-packed suitcase, the stained shoes, and the wine bottle paint an ambiguous picture.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineShed(String object, int count) {
        switch (object) {
            case "logbook":
                if (count == 1) return new ExamResult(
                    "Daniel's logbook records daily activities. Each day is meticulously noted -- except the night of the murder. That page has been torn out. A tape recorder is wedged between the pages.",
                    Evidence.GROUNDSKEEPER_LOG, Tape.TAPE_DANIEL_INTERVIEW);
                return new ExamResult("The logbook. The torn-out page is still missing.");

            case "shelf":
                if (count == 1) return new ExamResult("Gardening supplies, tools, a tin of nails. Behind a can of paint thinner, a pair of worn work boots caked in fresh mud. Daniel's boots. He claims he stayed in his shed all night, but these boots tell a different story.");
                if (count == 2) return new ExamResult(
                    "Looking more carefully at the muddy boots... Fresh cellar dust mixed with garden soil. The same mud that would be on the grounds near the study window. Daniel must have gone outside that night to enter through the window -- proof he's lying about staying in his shed.",
                    Evidence.MUDDY_BOOTS);
                return new ExamResult("The shelf. You already found the muddy boots.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineServants(String object, int count) {
        switch (object) {
            case "bedpost":
                if (count == 1) return new ExamResult("A simple wooden bedpost, worn smooth by years of use. Deep scratches mar the wood near the base -- someone gripped this hard, or something scraped against it.");
                if (count == 2) {
                    state.discoverAnomaly(EntityAnomaly.SCRATCHED_INITIALS);
                    return new ExamResult("Looking more closely at the scratches on the bedpost, you realize they're not random. Near the base, deliberate letters are carved into the wood: 'A.H.' Arthur Hollis -- the investigator who came before you. He was never seen again.\n\n[ANOMALY DISCOVERED: Scratched Initials]");
                }
                return new ExamResult("The worn bedpost. The 'A.H.' initials carved into the wood.");

            case "floorboard":
                if (count == 1) return new ExamResult("A loose floorboard near the wall. Prying it up, you find nothing but dust and old nails. But the hiding spot has been used before -- scratch marks show something was stored here recently and removed.");
                return new ExamResult("The empty space under the floorboard. Whatever was hidden here is gone now.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineCellar(String object, int count) {
        switch (object) {
            case "flour_sacks":
                if (count == 1) return new ExamResult(
                    "Flour sacks stacked against the wall. The floor around them has been recently disturbed -- drag marks in the dust. Reaching behind the sacks, your hand closes on bundled fabric. A shirt, hastily hidden. The right cuff is stained dark with blood. Why hide it instead of burning it? The study fireplace was nearly dead by midnight -- James must have panicked.",
                    Evidence.BLOODSTAINED_CUFF);
                return new ExamResult("The flour sacks. You already found the bloodstained shirt hidden behind them.");

            case "wine_rack":
                if (count == 1) return new ExamResult(
                    "Wine racks from floor to ceiling. Most bottles are dusty, but one section has been recently disturbed. Behind the bottles, taped to the wall: another tape recorder.\n\n" +
                    "But this one is damaged. The casing is cracked, the tape ribbon inside snapped and tangled. " +
                    "Someone -- or something -- tried to destroy this recording. " +
                    "You'll need tape splicing equipment to repair it before you can listen.",
                    Tape.TAPE_ARTHUR_DEATH);
                if (count == 2) {
                    state.discoverAnomaly(EntityAnomaly.BREATHING_WALL);
                    return new ExamResult("Moving more bottles aside, you press your hand against the cellar wall behind the rack. It's warm. Not warm like stone in summer -- warm like flesh. And beneath your palm, you feel a slow, rhythmic pulse. The wall is breathing.\n\n[ANOMALY DISCOVERED: Breathing Wall]");
                }
                return new ExamResult("The wine rack. The wall behind it still pulses with that unnatural warmth.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }
}
