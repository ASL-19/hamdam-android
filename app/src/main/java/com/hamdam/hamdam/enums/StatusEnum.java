package com.hamdam.hamdam.enums;

import java.util.EnumSet;


/**
 * StatusEnum contains the multiple-choice values that users can select in their
 * daily status questionnaires. The broad question categories (Body, Mental etc)
 * are subdivided into types of status (Pain, sleep, energy, etc), and the user
 * responses (Lots of sleep, little sleep, headache, good mood etc) belong to these
 * subtypes.
 */
public final class StatusEnum {

    // Some status values support multiple responses, while some are
    // mutually exclusive. Potential for future categories to be added that are also multiple choice.
    private static final EnumSet<StatusType> MULTI_ANSWER = EnumSet.of(StatusType.PAIN, StatusType.MOOD);

    public static boolean isMultiAnswer(StatusType type) {
        return MULTI_ANSWER.contains(type);
    }

    public enum StatusMain {

        ACTIVITY,
        BODY,
        MENTAL

    }

    public enum Options {
        ONE(0),
        TWO(1),
        THREE(2),
        FOUR(3);

        private final int value;

        Options(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    // While enums do have an ordinal property, changing their order would change the ordinal value,
    // so values are explicitly defined to limit potential for bugs.
    public final class Topics {
        public static final int SEX = 0;
        public static final int EXERCISE = 1;
        public static final int SLEEP = 2;
        public static final int MOOD = 3;
        public static final int PAIN = 4;
        public static final int FLUIDS = 5;
        public static final int BLEEDING = 6;
    }

    /*
     * @param   statusMain  StatusMain supercategory
     * @param   tag         fixed ordinal value
     */
    public enum StatusType {

        // Activity subtypes
        SEX(StatusMain.ACTIVITY, Topics.SEX),
        EXERCISE(StatusMain.ACTIVITY, Topics.EXERCISE),

        // Mental status subtypes
        MOOD(StatusMain.MENTAL, Topics.MOOD),

        // Body status subtypes
        SLEEP(StatusMain.BODY, Topics.SLEEP),
        PAIN(StatusMain.BODY, Topics.PAIN),
        FLUIDS(StatusMain.BODY, Topics.FLUIDS),
        BLEEDING(StatusMain.BODY, Topics.BLEEDING);

        private final StatusMain statusMain;
        private final int tag;

        StatusType(StatusMain statusMain, int tag) {
            this.statusMain = statusMain;
            this.tag = tag;
        }

        public StatusMain getStatusMain() {
            return this.statusMain;
        }

        public int getTag() {
            return tag;
        }

        public static StatusType getByTag(int tag) {
            for (StatusType s : StatusType.values()) {
                if (s.getTag() == tag) {
                    return s;
                }
            }
            return null;
        }

    }

    /*
     * @param   statusType  Type supercategory
     * @param   ordinal     enum representing an artificial ordinal for values.
     *                      Values belonging to same StatusType category must have unique ordinals.
     */
    public enum StatusValue {

        // Exercise value
        YES_EXERCISE(StatusType.EXERCISE, Options.ONE),

        // Sex value
        PROTECTED_SEX(StatusType.SEX, Options.ONE),
        UNPROTECTED_SEX(StatusType.SEX, Options.TWO),

        // Pain values
        HEADACHE(StatusType.PAIN, Options.ONE),
        CRAMPS(StatusType.PAIN, Options.TWO),
        BACKACHE(StatusType.PAIN, Options.THREE),
        BREASTS(StatusType.PAIN, Options.FOUR), // Tender breasts

        // Sleep values
        NO_SLEEP(StatusType.SLEEP, Options.ONE), // 0-3
        LITTLE_SLEEP(StatusType.SLEEP, Options.TWO), // 3-6
        AVERAGE_SLEEP(StatusType.SLEEP, Options.THREE), // 6-9
        LOTS_SLEEP(StatusType.SLEEP, Options.FOUR), // 9+

        // Fluids values
        STICKY(StatusType.FLUIDS, Options.ONE),
        CREAMY(StatusType.FLUIDS, Options.TWO),
        EGGWHITE(StatusType.FLUIDS, Options.THREE),
        ABNORMAL(StatusType.FLUIDS, Options.FOUR),

        // Mood values
        HAPPY(StatusType.MOOD, Options.ONE),
        NEUTRAL(StatusType.MOOD, Options.TWO),
        SAD(StatusType.MOOD, Options.THREE),
        PMS(StatusType.MOOD, Options.FOUR),

        SPOTTING(StatusType.BLEEDING, Options.ONE),
        LIGHT(StatusType.BLEEDING, Options.TWO),
        MEDIUM(StatusType.BLEEDING, Options.THREE),
        HEAVY(StatusType.BLEEDING, Options.FOUR);

        private final StatusType statusType;
        private final Options ordinal;

        StatusValue(StatusType statusType, Options ordinal) {
            this.statusType = statusType;
            this.ordinal = ordinal;
        }

        public StatusType getStatusType() {
            return this.statusType;
        }

        public Options getOrdinal() {
            return this.ordinal;
        }

        public static StatusValue getByOrdinal(StatusType t, Options o) {
            for (StatusValue v : StatusValue.values()) {
                if (v.getOrdinal() == o && v.getStatusType() == t) {
                    return v;
                }
            }
            return null;
        }
    }
}
