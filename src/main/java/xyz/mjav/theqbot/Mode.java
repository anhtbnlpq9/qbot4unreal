package xyz.mjav.theqbot;

import static java.util.Map.entry;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xyz.mjav.theqbot.exceptions.ItemNotFoundException;
import xyz.mjav.theqbot.exceptions.NickNotFoundException;

/**
 * Class Mode
 * Store an IRC mode
 *
 * Store for MLOCK:
 * +abcdef-ghijkl param1 param2 param3 param4
 *
 */
public class Mode {

    private static Logger log = LogManager.getLogger("common-log");

    private static Map<String, String> protocolProps;

    public static final long MODE_a      = 0x0000000000000001L;
    public static final long MODE_b      = 0x0000000000000002L;
    public static final long MODE_c      = 0x0000000000000004L;
    public static final long MODE_d      = 0x0000000000000008L;
    public static final long MODE_e      = 0x0000000000000010L;
    public static final long MODE_f      = 0x0000000000000020L;
    public static final long MODE_g      = 0x0000000000000040L;
    public static final long MODE_h      = 0x0000000000000080L;
    public static final long MODE_i      = 0x0000000000000100L;
    public static final long MODE_j      = 0x0000000000000200L;
    public static final long MODE_k      = 0x0000000000000400L;
    public static final long MODE_l      = 0x0000000000000800L;
    public static final long MODE_m      = 0x0000000000001000L;
    public static final long MODE_n      = 0x0000000000002000L;
    public static final long MODE_o      = 0x0000000000004000L;
    public static final long MODE_p      = 0x0000000000008000L;
    public static final long MODE_q      = 0x0000000000010000L;
    public static final long MODE_r      = 0x0000000000020000L;
    public static final long MODE_s      = 0x0000000000040000L;
    public static final long MODE_t      = 0x0000000000080000L;
    public static final long MODE_u      = 0x0000000000100000L;
    public static final long MODE_v      = 0x0000000000200000L;
    public static final long MODE_w      = 0x0000000000400000L;
    public static final long MODE_x      = 0x0000000000800000L;
    public static final long MODE_y      = 0x0000000001000000L;
    public static final long MODE_z      = 0x0000000002000000L;
    public static final long MODE_A      = 0x0000000004000000L;
    public static final long MODE_B      = 0x0000000008000000L;
    public static final long MODE_C      = 0x0000000010000000L;
    public static final long MODE_D      = 0x0000000020000000L;
    public static final long MODE_E      = 0x0000000040000000L;
    public static final long MODE_F      = 0x0000000080000000L;
    public static final long MODE_G      = 0x0000000100000000L;
    public static final long MODE_H      = 0x0000000200000000L;
    public static final long MODE_I      = 0x0000000400000000L;
    public static final long MODE_J      = 0x0000000800000000L;
    public static final long MODE_K      = 0x0000001000000000L;
    public static final long MODE_L      = 0x0000002000000000L;
    public static final long MODE_M      = 0x0000004000000000L;
    public static final long MODE_N      = 0x0000008000000000L;
    public static final long MODE_O      = 0x0000010000000000L;
    public static final long MODE_P      = 0x0000020000000000L;
    public static final long MODE_Q      = 0x0000040000000000L;
    public static final long MODE_R      = 0x0000080000000000L;
    public static final long MODE_S      = 0x0000100000000000L;
    public static final long MODE_T      = 0x0000200000000000L;
    public static final long MODE_U      = 0x0000400000000000L;
    public static final long MODE_V      = 0x0000800000000000L;
    public static final long MODE_W      = 0x0001000000000000L;
    public static final long MODE_X      = 0x0002000000000000L;
    public static final long MODE_Y      = 0x0004000000000000L;
    public static final long MODE_Z      = 0x0008000000000000L;

    //public static final long MODE_       = 0x0010000000000000L;
    //public static final long MODE_       = 0x0020000000000000L;
    //public static final long MODE_       = 0x0040000000000000L;
    //public static final long MODE_       = 0x0080000000000000L;

    //public static final long MODE_       = 0x0100000000000000L;
    //public static final long MODE_       = 0x0200000000000000L;
    //public static final long MODE_       = 0x0400000000000000L;
    //public static final long MODE_       = 0x0800000000000000L;

    //public static final long MODE_       = 0x1000000000000000L;
    //public static final long MODE_       = 0x2000000000000000L;
    //public static final long MODE_       = 0x4000000000000000L;
    //public static final long MODE_       = 0x8000000000000000L;

    public static final Map<String, Long> modeStrToLongTable = Map.ofEntries(
        entry("a",        MODE_a),
        entry("b",        MODE_b),
        entry("c",        MODE_c),
        entry("d",        MODE_d),
        entry("e",        MODE_e),
        entry("f",        MODE_f),
        entry("g",        MODE_g),
        entry("h",        MODE_h),
        entry("i",        MODE_i),
        entry("j",        MODE_j),
        entry("k",        MODE_k),
        entry("l",        MODE_l),
        entry("m",        MODE_m),
        entry("n",        MODE_n),
        entry("o",        MODE_o),
        entry("p",        MODE_p),
        entry("q",        MODE_q),
        entry("r",        MODE_r),
        entry("s",        MODE_s),
        entry("t",        MODE_t),
        entry("u",        MODE_u),
        entry("v",        MODE_v),
        entry("w",        MODE_w),
        entry("x",        MODE_x),
        entry("y",        MODE_y),
        entry("z",        MODE_z),
        entry("A",        MODE_A),
        entry("B",        MODE_B),
        entry("C",        MODE_C),
        entry("D",        MODE_D),
        entry("E",        MODE_E),
        entry("F",        MODE_F),
        entry("G",        MODE_G),
        entry("H",        MODE_H),
        entry("I",        MODE_I),
        entry("J",        MODE_J),
        entry("K",        MODE_K),
        entry("L",        MODE_L),
        entry("M",        MODE_M),
        entry("N",        MODE_N),
        entry("O",        MODE_O),
        entry("P",        MODE_P),
        entry("Q",        MODE_Q),
        entry("R",        MODE_R),
        entry("S",        MODE_S),
        entry("T",        MODE_T),
        entry("U",        MODE_U),
        entry("V",        MODE_V),
        entry("W",        MODE_W),
        entry("X",        MODE_X),
        entry("Y",        MODE_Y),
        entry("Z",        MODE_Z)
    );

    public static final Map<Long, String> modeLongToStrTable = Map.ofEntries(
        entry(MODE_a,     "a"),
        entry(MODE_b,     "b"),
        entry(MODE_c,     "c"),
        entry(MODE_d,     "d"),
        entry(MODE_e,     "e"),
        entry(MODE_f,     "f"),
        entry(MODE_g,     "g"),
        entry(MODE_h,     "h"),
        entry(MODE_i,     "i"),
        entry(MODE_j,     "j"),
        entry(MODE_k,     "k"),
        entry(MODE_l,     "l"),
        entry(MODE_m,     "m"),
        entry(MODE_n,     "n"),
        entry(MODE_o,     "o"),
        entry(MODE_p,     "p"),
        entry(MODE_q,     "q"),
        entry(MODE_r,     "r"),
        entry(MODE_s,     "s"),
        entry(MODE_t,     "t"),
        entry(MODE_u,     "u"),
        entry(MODE_v,     "v"),
        entry(MODE_w,     "w"),
        entry(MODE_x,     "x"),
        entry(MODE_y,     "y"),
        entry(MODE_z,     "z"),
        entry(MODE_A,     "A"),
        entry(MODE_B,     "B"),
        entry(MODE_C,     "C"),
        entry(MODE_D,     "D"),
        entry(MODE_E,     "E"),
        entry(MODE_F,     "F"),
        entry(MODE_G,     "G"),
        entry(MODE_H,     "H"),
        entry(MODE_I,     "I"),
        entry(MODE_J,     "J"),
        entry(MODE_K,     "K"),
        entry(MODE_L,     "L"),
        entry(MODE_M,     "M"),
        entry(MODE_N,     "N"),
        entry(MODE_O,     "O"),
        entry(MODE_P,     "P"),
        entry(MODE_Q,     "Q"),
        entry(MODE_R,     "R"),
        entry(MODE_S,     "S"),
        entry(MODE_T,     "T"),
        entry(MODE_U,     "U"),
        entry(MODE_V,     "V"),
        entry(MODE_W,     "W"),
        entry(MODE_X,     "X"),
        entry(MODE_Y,     "Y"),
        entry(MODE_Z,     "Z")
    );

    private Character mode;

    private String parameter;





    public static long modeStrToLong(String s) {
        return modeStrToLong(s);
    }

    public static String modeLongToStr(long l) {
        return modeLongToStr(l);
    }


    public static String modeMapToString2(Map<String, Map<String, String>> modeMap, boolean mLockFormat) {

        Map<String, String>     chanModes = new TreeMap<>(modeMap.get("chanModes"));
        //Map<String, String> chanUserModes = modeMap.get("chanUserModes");
        //Map<String, String>     chanLists = modeMap.get("chanLists");

        String strPlus = "";
        String strMinus = "";
        StringBuilder strModesPlus = new StringBuilder();
        StringBuilder strModesMinus = new StringBuilder();

        StringBuilder strParamsPlus = new StringBuilder();
        StringBuilder strParamsMinus = new StringBuilder();

        for(String mode: chanModes.keySet()) {

            //if (mode.startsWith("+")) {
            //    strModesPlus.append(mode.replaceFirst("[+|-]", ""));
            //}

            switch(mode.charAt(0)) {
                case '+':
                strModesPlus.append(mode.replaceFirst("[+|-]", ""));
                if (chanModes.get(mode).isEmpty() == false) strParamsPlus.append(" " + chanModes.get(mode));
                break;

                case '-':
                if (chanModes.containsKey(mode.replaceFirst("[+|-]", "+")) == true) continue;
                strModesMinus.append(mode.replaceFirst("[+|-]", ""));
                if (chanModes.get(mode).isEmpty() == false) strParamsMinus.append(" " + chanModes.get(mode));
                break;

                default:
                strModesPlus.append(mode.replaceFirst("[+|-]", ""));
                if (chanModes.get(mode).isEmpty() == false) strParamsPlus.append(" " + chanModes.get(mode));
                break;
            }
        }

        if (strModesPlus.isEmpty() == false) strPlus = "+" + strModesPlus;
        if (strModesMinus.isEmpty() == false) strMinus = "-" + strModesMinus;

        if (mLockFormat == true) {
            //if (strModesPlus.isEmpty() == false) strPlus = "+" + strModesPlus;
            //if (strModesMinus.isEmpty() == false) strMinus = "-" + strModesMinus;

            return String.format("%s%s", strModesPlus, strModesMinus);

        }

        return String.format("%s%s%s%s", strPlus, strMinus, strParamsPlus, strParamsMinus);

    }


    /**
     * Parses a channel mode list and group them into 3 Maps (chanModes, chanLists, chanUserModes).
     * chanModes contains a Map of mode->parameter
     * chanLists contains a Map of mode->list
     * chanUserModes contains a Map of mode->nick
     * @param str Modes string, e.g "+abc-de+g... param1 param2 param3 param4 param5..."
     * @return Map of 3 keys (chanModes, chanLists, chanUserModes) with a Map in each
     */
    private Map<String, Map<String, String>> parseChanModes(String str) {

        /*
         * CHANMODES=beI,fkL,lFH,cdimnprstzCDGKMNOPQRSTVZ
         *           --- --- --- ------------------------
         *            |   |    |           `- group4: no parameter
         *            |   |     `------------ group3: parameter for set, no parameter for unset
         *            |    `----------------- group2: parameter for set, parameter for unset
         *             `--------------------- group1: (list) parameter for set, parameter for unset
         */

        Map<String, String>     chanModes = new HashMap<>();
        Map<String, String> chanUserModes = new HashMap<>();
        Map<String, String>     chanLists = new HashMap<>();

        Map<String, Map<String, String>> result = new HashMap<>();
        result.put("chanModes",     chanModes);
        result.put("chanLists",     chanLists);
        result.put("chanUserModes", chanUserModes);

        String networkChanModesGroup1        = ((protocolProps.get("CHANMODES")).split(",", 4))[0];
        String networkChanModesGroup2        = ((protocolProps.get("CHANMODES")).split(",", 4))[1];
        String networkChanModesGroup3        = ((protocolProps.get("CHANMODES")).split(",", 4))[2];
        String networkChanModesGroup4        = ((protocolProps.get("CHANMODES")).split(",", 4))[3];
        String   networkChanUserModes        =   protocolProps.get("PREFIX").replaceAll("[^A-Za-z0-9]", ""); // Channel modes for users


        /*
         *       Modes = strSplit[0]
         *  Parameters = strSplit[1+]
         */
        String[] strSplit = str.split(" ");

        String modes        = strSplit[0];
        String curMode;
        String strUserModes = "";
        String strLists     = "";

        char modeAction = '+';

        Integer modeIndex  = 0;
        Integer paramIndex = 1;

        Nick userNode;

        for(int i=0; i < networkChanUserModes.length(); i++) {
            chanUserModes.put("+" + networkChanUserModes.charAt(i), "");
            chanUserModes.put("-" + networkChanUserModes.charAt(i), "");
        }

        for(int i=0; i < networkChanModesGroup1.length(); i++) {
            chanLists.put("+" + networkChanModesGroup1.charAt(i), "");
            chanLists.put("-" + networkChanModesGroup1.charAt(i), "");
        }

        while(modeIndex < modes.length()) {
            curMode = String.valueOf(modes.charAt(modeIndex));

            switch(curMode) {
                case "+": modeAction = '+'; break;
                case "-": modeAction = '-'; break;
            }

            if (curMode.matches("[A-Za-z]") == true) {

                if (curMode.matches("[" + networkChanModesGroup1 + "]")) { /* Chan lists */
                    strLists = "";
                    strLists = String.join(" ", chanLists.get(modeAction + curMode), strSplit[paramIndex]);
                    chanLists.replace(modeAction + curMode, strLists);
                    paramIndex++;
                }

                else if (curMode.matches("[" + networkChanModesGroup2 + "]")) {
                    chanModes.put(modeAction + curMode, strSplit[paramIndex]);
                    paramIndex++;
                }

                else if (curMode.matches("[" + networkChanModesGroup3 + "]")) {
                    if (modeAction == '+') {
                        chanModes.put(modeAction + curMode, strSplit[paramIndex]);
                        paramIndex++;
                    }
                    else {
                        chanModes.put(modeAction + curMode, "");
                    }
                }

                else if (curMode.matches("[" + networkChanModesGroup4 + "]")) {
                    chanModes.put(modeAction + curMode, "");
                }

                else if (curMode.matches("[" + networkChanUserModes + "]")) {

                    strUserModes = "";
                    try { userNode = Nick.getUserByNickCi(strSplit[paramIndex]); }
                    catch (NickNotFoundException e) {
                        log.error(String.format("Protocol/parseChanModes: error that should not happen, nick included in mode change is not on the network"), e);
                        throw new ItemNotFoundException(String.format("Protocol/parseChanModes: error that should not happen, nick included in mode change is not on the network"));
                    }

                    strUserModes = String.join(" ", chanUserModes.get(modeAction + curMode), userNode.getNick());
                    chanUserModes.replace(modeAction + curMode, strUserModes);
                    paramIndex++;
                }
            }

            modeIndex++;
        }

        return result;
    }




    public Map<String, String> parseUserMode(String str) {

        /*
         * USERMODES=diopqrstwxzBDGHIRSTWZ
         */


        String[] strSplit = str.split(" ");

        String modes        = strSplit[0];
        String curMode;
        //String strUserModes = "";
        //String strLists     = "";

        char modeAction = '+';

        Integer modeIndex  = 0;
        //Integer paramIndex = 1;

        //UserNode userNode;

        Map<String, String>     userModes = new HashMap<>();

        for (int i=0; i < protocolProps.get("USERMODES").length(); i++) {

        }

        while (modeIndex < modes.length()) {
            curMode = String.valueOf(modes.charAt(modeIndex));

            switch(curMode) {
                case "+": modeAction = '+'; break;
                case "-": modeAction = '-'; break;
            }

            if (curMode.matches("[A-Za-z]") == true) {

                if (curMode.matches("[" + protocolProps.get("USERMODES") + "]")) {
                    userModes.put(modeAction + curMode, "");
                }
            }
            modeIndex++;
        }
        return userModes;
    }


    public Mode(Character m) {
        this.mode = m;
        this.parameter = "";
    }

    public Mode(Character m, String p) {
        this.mode = m;
        this.parameter = p;
    }

    public Character getMode() {
        return mode;
    }

    public String getParameter() {
        return parameter;
    }

    public Map<Character, String> get() {
        Map<Character, String> mode = new HashMap<>();
        mode.put(this.mode, this.parameter);
        return mode;
    }

    public static String modeMapToString(Map<String, String> modeMap) {

        StringBuilder strModes = new StringBuilder();

        for(String mode: modeMap.keySet()) strModes.append(mode);
        for(String param: modeMap.values()) if (param.isEmpty() == false) strModes.append(" " + param);

        return strModes.toString();
    }




}
