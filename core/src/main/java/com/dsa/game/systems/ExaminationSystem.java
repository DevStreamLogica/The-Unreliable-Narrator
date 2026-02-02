package com.dsa.game.systems;

import com.dsa.game.navigation.Room;
import com.dsa.game.state.*;

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
                return new ExamResult("The same papers as before. The solicitor's letter stands out.");

            case "bookshelves":
                if (count == 1) return new ExamResult(
                    "Floor-to-ceiling bookshelves. Running your hand behind the books on the third shelf, your fingers find a small tape recorder wedged behind a volume of Shakespeare.",
                    Tape.TAPE_WILL_READING);
                return new ExamResult("The bookshelves. Leather-bound volumes in neat rows. You already found the tape.");

            case "window":
                if (count == 1) return new ExamResult("Large bay windows overlooking the grounds. The latch is unlocked. Fresh scuff marks on the windowsill -- someone climbed through here recently.");
                return new ExamResult("The window. The unlocked latch and scuff marks remain.");

            case "fireplace":
                if (count == 1) return new ExamResult("A large stone fireplace. The fire has burned down to embers, but the ashes look fresh. Something was burned here recently -- not just firewood.");
                return new ExamResult("The fireplace. The unusual ashes still sit in the grate.");

            case "ashes":
                if (count == 1) return new ExamResult(
                    "Sifting through the ashes carefully, you find fragments of a letter. Pieced together, you can read: '...cannot allow this betrayal... the will must... James has...' The rest is destroyed.",
                    Evidence.TORN_LETTER);
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
                    Tape.TAPE_PHONE_CALL);
                return new ExamResult("The grandfather clock ticks on. You already found the tape inside.");

            case "briefcase":
                if (count == 1) return new ExamResult(
                    "A leather briefcase by one of the chairs. Inside: legal documents, a copy of Harold's current will. James inherits everything. Margaret receives nothing.",
                    Evidence.WILL_COPY);
                return new ExamResult("The briefcase. You already have the copy of the will.");

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
                    "The narrow door to the cellar storage. Behind it, hidden on a shelf just inside the doorway, you find another tape recorder.",
                    Tape.TAPE_KITCHEN_WHISPERS);
                return new ExamResult("The cellar door. You already found the tape here.");

            case "flour_tin":
                if (count == 1) return new ExamResult(
                    "A large flour tin on the shelf. Reaching inside past the flour, your hand closes around a small glass vial. The label reads 'Chloral Hydrate' -- a powerful sleeping powder.",
                    Evidence.SLEEPING_POWDER);
                return new ExamResult("The flour tin. You already found the sleeping powder hidden inside.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineGuestRooms(String object, int count) {
        switch (object) {
            case "margarets_room":
                if (count == 1) return new ExamResult(
                    "Margaret's room is tidy but tense. A suitcase sits half-packed on the bed. On the nightstand, a tape recorder with a handwritten label: 'For the detective.'",
                    Tape.TAPE_MARGARET_CONFESSION);
                return new ExamResult("Margaret's room. The half-packed suitcase remains on the bed.");

            case "james_room":
                if (count == 1) return new ExamResult("James's room is messier. The bed is unmade, an ashtray overflows with cigarette stubs. A glass of whisky, half-finished, sits on the nightstand. The wardrobe door hangs open.");
                return new ExamResult("James's room. Messy and lived-in. The stale smell of cigarettes.");

            case "letter":
                if (count == 1) return new ExamResult(
                    "On Margaret's dresser, a folded note. Opening it: 'I know what you did. Pay what you owe, or I tell everything. You know where to leave the money.' No signature.",
                    Evidence.BLACKMAIL_NOTE);
                return new ExamResult("The dresser where you found the blackmail note.");

            case "coat":
                if (count == 1) return new ExamResult(
                    "James's coat hangs on the wardrobe door. Checking the pockets, you find nothing. But the right sleeve -- the cuff is stained dark. Blood.",
                    Evidence.BLOODSTAINED_CUFF);
                return new ExamResult("James's coat. You already noticed the bloodstained cuff.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineShed(String object, int count) {
        switch (object) {
            case "logbook":
                if (count == 1) return new ExamResult(
                    "Daniel's logbook records daily activities. Each day is meticulously noted -- except the night of the murder. That page has been torn out. A tape recorder is wedged between the pages.",
                    Evidence.GROUNDSKEEPER_LOG, Tape.TAPE_DANIEL_MEETING);
                return new ExamResult("The logbook. The torn-out page is still missing.");

            case "shelf":
                if (count == 1) return new ExamResult("Gardening supplies, tools, a tin of nails. Behind a can of paint thinner, a pair of boots caked in fresh mud. They're much too large to be Daniel's.");
                if (count == 2) return new ExamResult(
                    "Looking more carefully at the muddy boots... The size, the style -- these are expensive boots. Not a groundskeeper's. Someone from the main house wore these outside recently.",
                    Evidence.MUDDY_BOOTS);
                return new ExamResult("The shelf. You already found the muddy boots.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineServants(String object, int count) {
        switch (object) {
            case "staircase":
                if (count == 1) return new ExamResult("A narrow staircase leading to the back of the house. The steps creak. Fresh scratches on the banister, as if something heavy was dragged along it.");
                return new ExamResult("The narrow staircase with its scratched banister.");

            case "floorboard":
                if (count == 1) return new ExamResult(
                    "A loose floorboard near the wall. Prying it up, you find a bundled shirt with blood on the right cuff. It matches the stain on James's coat.",
                    Evidence.BLOODSTAINED_CUFF);
                return new ExamResult("The space under the floorboard. You already found the hidden shirt.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }

    private ExamResult examineCellar(String object, int count) {
        switch (object) {
            case "flour_tin":
                if (count == 1) return new ExamResult("Flour sacks stacked against the wall. Nothing hidden in them, but the floor around them has been recently disturbed -- drag marks in the dust.");
                return new ExamResult("The flour sacks and the disturbed floor around them.");

            case "wine_rack":
                if (count == 1) return new ExamResult(
                    "Wine racks from floor to ceiling. Most bottles are dusty, but one section has been recently disturbed. Behind the bottles, taped to the wall: another tape recorder.",
                    Tape.TAPE_CELLAR_NOISES);
                return new ExamResult("The wine rack. You already found the tape hidden behind the bottles.");

            default:
                return new ExamResult("Nothing noteworthy here.");
        }
    }
}
