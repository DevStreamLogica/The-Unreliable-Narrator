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

            case "ashes":
                if (count == 1) return new ExamResult(
                    "Sifting through the ashes carefully, you find fragments of a burned letter. The pieces are scattered but might be reconstructed...",
                    ExamResult.MiniGameType.TORN_LETTER_RECONSTRUCTION);
                return new ExamResult("The fireplace ashes. You've found everything hidden here.");

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
                    "An ornate grandfather clock. The pendulum swings steadily. Opening the clock case to examine the mechanism, you discover a tape recorder hidden behind the pendulum assembly.",
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
                return new ExamResult("The narrow door leading down to the cellar storage.");

            case "flour_tin":
                if (count == 1) return new ExamResult(
                    "A battered flour tin on the kitchen shelf. Lifting the lid: beneath a false bottom, wrapped in cloth, a bundle of old correspondence. Letters between Harold and an unknown party. Nothing conclusive, but someone hid them deliberately.");
                return new ExamResult("The flour tin. You already searched it.");

            case "kitchen_floor":
                if (count == 1) return new ExamResult(
                    "A tape recorder, sitting in the middle of the kitchen floor. No attempt to hide it. The reel is hand-labeled in careful block letters -- someone wanted this found, or wanted it close. Margaret spent more time in this kitchen than anywhere else in the manor.\n\nThe narrator's voice comes through tightly: \"I remember her in here. Late. After the others had gone to bed. She was listening to something -- replaying it. Over and over.\"",
                    Tape.TAPE_MARGARET_INTERVIEW);
                return new ExamResult("The kitchen floor where the tape recorder was. You've already taken it.");

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
            case "lamp":
                if (count == 1) return new ExamResult(
                    "A bedside lamp on the nightstand. Lifting the base to examine it, a folded note falls out -- tucked underneath. Opening it: 'I know what you did. Pay what you owe, or I tell everything. You know where to leave the money.' No signature. The handwriting is rough, uneducated -- not Margaret's elegant script. This note wasn't written BY her. Someone hid it here to frame her.",
                    Evidence.BLACKMAIL_NOTE);
                return new ExamResult("The lamp. The blackmail note was hidden underneath it.");

            case "tape_recorder":
                if (count == 1) return new ExamResult(
                    "A tape recorder on the nightstand with a handwritten label: 'For the detective.' The ribbon inside has been deliberately cut -- someone found this before you did and didn't want it heard.",
                    Tape.TAPE_MARGARET_ACCOUNT);
                return new ExamResult("The tape recorder. You've already collected it.");


            case "dresser":
                if (count == 1) return new ExamResult("Margaret's dresser against the wall. A half-packed suitcase sits on top -- clothes folded neatly, but only halfway full. She was planning to leave.");
                return new ExamResult("Margaret's dresser. The half-packed suitcase, undisturbed.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineShed(String object, int count) {
        switch (object) {
            case "logbook":
                if (count == 1) return new ExamResult(
                    "Daniel's logbook records daily activities. Each day is meticulously noted -- except the night of the murder. That page has been torn out. A tape recorder is wedged between the pages. The casing is cracked and the ribbon has been pulled loose -- someone tried to destroy this recording. It will need splicing before it can play.",
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

            case "drawer":
                if (count == 1) return new ExamResult(
                    "A small wooden nightstand between the beds. The drawer contains a worn notebook -- pages of observations in cramped handwriting. The last entry reads: 'The narrator knows I'm here. I don't have much time.'");
                return new ExamResult("The nightstand. The notebook with its ominous final entry.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineCellar(String object, int count) {
        switch (object) {
            case "cellar_shelf":
                if (count == 1) return new ExamResult(
                    "A row of old shelves along the cellar wall. Dusty jars, rusted tins. Behind a cluster of canisters, a small glass vial -- the label reads 'Chloral Hydrate'. A powerful sleeping powder.",
                    Evidence.SLEEPING_POWDER);
                if (count == 2) {
                    state.discoverAnomaly(EntityAnomaly.COLD_SPOT_CELLAR);
                    return new ExamResult("You run your hand along the cellar wall near the shelf. The stone is ice-cold here -- far colder than the rest of the cellar. Your breath mists. The cold seems to be coming from deeper in the wall.\n\n[ANOMALY DISCOVERED: Cold Spot]");
                }
                return new ExamResult("The cellar shelf. You've found everything it had to offer.");

            case "flour_sacks":
                if (count == 1) return new ExamResult(
                    "Flour sacks stacked against the wall. The floor around them has been recently disturbed -- drag marks in the dust. Reaching behind the sacks, your hand closes on bundled fabric. A shirt, hastily hidden. The right cuff is stained dark with blood. Why hide it instead of burning it? The cellar was dark, fast to reach from the study, and easily locked from the inside. Whoever hid this was thinking quickly.",
                    Evidence.BLOODSTAINED_CUFF);
                return new ExamResult("The flour sacks. You've searched behind them thoroughly.");

            case "wine_rack":
                if (count == 1) return new ExamResult(
                    "Wine racks from floor to ceiling. Most bottles are dusty, but one section has been recently disturbed. Behind the bottles, taped to the wall: another tape recorder.\n\n" +
                    "The casing is cracked, the tape ribbon inside tangled and fragile. " +
                    "Someone -- or something -- tried to destroy this recording. " +
                    "Somehow, it still plays.",
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
