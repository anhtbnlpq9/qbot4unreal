package xyz.mjav.theqbot;

import java.util.Map;
import java.util.StringJoiner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xyz.mjav.theqbot.exceptions.InvalidFormatException;

import static java.util.Map.entry;

import java.util.Base64;
import java.util.HashMap;

public class Extban implements Bei {

    /*
     * Format: [~time:N:][~group2:][~group3:][~group4:]
     *
     * Stacking rules:
     * o 1
     * o 1+2
     * o 1+3
     * o 1+4
     * o 1+2+3
     * o 2
     * o 2+3
     * o 3
     * o 4
     *
     * Group 1: time limit / The following ban type can be used in front of any (ext)ban:
     *  ~t / ~time           :: b e I :: Timed bans are automatically unset by the server after the specified number of minutes. (~time:3:*!*@hostname)
     *
     *  Group 2: actions / These bantypes specify which actions are affected by a ban:
     *  ~q / ~quiet          :: b e I :: People matching these bans can join but are unable to speak, unless they have +v or higher. (~quiet:nick*!*@*)
     *  ~n / ~nickchange     :: b e I :: People matching these bans cannot change nicks, unless they have +v or higher. (~nickchange:nick*!*@*)
     *  ~j / ~join           :: b e I :: Users matching this may not join the channel. However, if they are already in the channel then they may still speak, change nicks, etc. (~join:*!*@*.aol.com)
     *  ~m / ~msgbypass      ::   e   :: Bypass message restrictions. This extended ban is only available as a ban exception (+e) and not as a ban (+b). The syntax is: +e/ ~msgbypass:type:mask.
     *                                   Valid types are: 'external' (bypass +n), 'censor' (bypass +G), 'moderated' (bypass +m/+M), 'color' (bypass +S/+c), and 'notice' (bypass +T).
     *                                   (~msgbypass:moderated:*!*@192.168.1.1 ~msgbypass:external:*!*@192.168.1.1 ~msgbypass:color:~account:ColorBot)
     *  ~f / ~forward        ::   e   :: If a user matches the ban or other limits (eg +l/+k/etc) then they will be forwarded to the specified channel. (~forward:#badisp:*!*@*.isp.xx)
     *  ~F / ~flood          ::   e   :: Bypass mode +f/+F flood protection. This extended ban is only available as +e and not as +b. Syntax: +e ~flood:types:mask. Valid flood types are: c, j, k, m, n, t, r, and * for all.
     *                                   For the meaning of the letters, see channel mode +f. (~flood:*:*!*@192.168.*, ~flood:*:~account:TestUser, ~flood:m:*!*@192.168.*)
     *
     *  Group 3: selectors  / These bantypes introduce new criteria which can be used:
     *  ~a / ~account        :: b e I :: If a user is logged in to services with this account name, then this ban will match. (~account:Name)
     *  ~c / ~channel        :: b e I :: If the user is in this channel then they are unable to join.
     *                                   A prefix can also be specified (+/%/@/&/~) which means that it will only match if the user
     *                                   has that rights or higher on the specified channel. (~channel:#lamers ~channel:@#trusted)
     *  ~A / ~asn            :: b e I :: Acts on IP AS number (~asn:12345)
     *  ~C / ~country        :: b e I :: The GEOIP module tries to map IP addresses of users to a country code, like NL and US.
     *                                   You can ban or exempt a user based on the two letter country code this way. (~country:NL)
     *  ~O / ~operclass      :: b e I :: If the user is an IRCOp and is logged in with an oper block with a matching oper::operclass name then this will match.
     *                                   This way you can create channels which only specific type(s) of opers may join. Set +i and use +I. (~operclass:*admin*)
     *  ~r / ~realname       :: b e I :: If the realname of a user matches this then they are unable to join. (~realname:*Stupid_bot_script*) NOTE: an underscore ('_') matches both a space (' ') and an underscore ('_').
     *  ~G / ~security-group :: b e I :: If the security group of a user matches this then they are unable to join. (~security-group:unknown-users)
     *  ~S / ~certfp         :: b e I :: When a user is using SSL/TLS with a client certificate then you can match the certificate fingerprint. Good for ban and invite exceptions. (~certfp:0011223344556677..)
     *
     *  Group 4: special / These bantypes are special and don't fit anywhere else:
     *  ~T / ~text           ::       :: Channel-specific text filtering. Supports two actions: 'censor' and 'block'. (~text:censor:*badword* and ~text:block:*something*)
     *  ~p / ~partmsg        ::       :: Hide part/quit messages on matching users. (~partmsg:*!*@*.blah.com)
     */


    /*
     * ~t:<time>:<usermask or selector>  => 2 parameters / always first
     *
     * ~q:<usermask or selector>         => 1 parameters
     * ~n:<usermask or selector>         => 1 parameters
     * ~j:<usermask or selector>         => 1 parameters
     * ~m:<type>:<usermask or selector>  => 2 parameters
     * ~f:<chan>:<usermask or selector>  => 2 parameters
     * ~F:<type>:<usermask or selector>  => 2 parameters
     *
     * ~a:<name>        => 1 parameters
     * ~c:<chan>        => 1 parameters
     * ~C:<country>     => 1 parameters
     * ~O:<class>       => 1 parameters
     * ~r:<realname>    => 1 parameters
     * ~G:<group>       => 1 parameters
     * ~S:<cert>        => 1 parameters
     *
     * ~T:<type>:<message>        => 2 parameters
     * ~p:<usermask or selector>  => 1 parameters
     *
     */

    protected static Logger log = LogManager.getLogger("common-log");

    /** Map of correspondance extban-long-name => extban-short-name */
    private static final Map<String, String> extBans = Map.ofEntries(
        /* group 1 */
        entry("time",           "t"),

        /* group 2 */
        entry("quiet",          "q"),
        entry("nickchange",     "n"),
        entry("join",           "j"),
        entry("msgbypass",      "m"),
        entry("forward",        "f"),
        entry("flood",          "F"),

        /* group 3 */
        entry("account",        "a"),
        entry("channel",        "c"),
        entry("asn",            "A"),
        entry("country",        "C"),
        entry("operclass",      "O"),
        entry("realname",       "r"),
        entry("security-group", "G"),
        entry("certfp",         "S"),

        /* group 4 */
        entry("text",           "T"),
        entry("partmsg",        "p")
    );

    /** Map of correspondance extban-short-name => extban-long-name */
    private static final Map<String, String> extBansReverse;
    static {
        extBansReverse = new HashMap<>();
        for(Map.Entry<String, String> set: extBans.entrySet()) extBansReverse.put(set.getValue(), set.getKey());
    }

    private static Map<String, Extban> extBanList = new HashMap<>();
    private static Map<String, Extban> extbans64  = new HashMap<>();


    /*
     * +b ~time:1:n!i@h => time=1 + action=default + selector=usermask
     * +b ~time:1:~join:n!i@h => time=1 + action=prevent-join + selector=usermask
     * +b ~time:1:~account:plop => time=1 + action=default + selector=[time=0]
     * +b ~time:1:~join:~account:plop => time=1 + action=prevent-join + selector=usermask
     */

    /** Stores the extban raw value, should contain the long name */
    private final String extBanRaw;

    /** Time limit (group 1) */
    private final int timeLimit;

    /** Action (group 2) */
    private final String actionName;
    private final String actionValue;

    /** Selector (group 3/4) - can be an Usermask or another extban */
    private final String selectorName;
    private final String selectorParam;
    private final Object selectorValue;

    /** Extban signature (to check if object already existing for reuse) */
    private final String b64signature;

    public static Extban getExtBan(String s) throws Exception {
        if (extBanList.containsKey(s.toLowerCase()) == true) return extBanList.get(s.toLowerCase());
        throw new Exception();

    }

    private static boolean exists(String s) {
        if (extbans64.containsKey(s) == false) return false;
        return true;
    }

    private static String getSignature(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes());
    }

    public static Extban create(String s) throws InvalidFormatException {

        // [~time:N:][~group2:][~group3:][~group4:]

        Extban extban;

        String[] extBanSplit;

        String extBanStr         = "";
        String curExtBanName     = "";
        String curExtBanParam    = "";
        String curExtBanArgument = "";
        String b64signature      = "";

        StringJoiner reconstructed;

        int groupPass = 1;

        /* Time limit (group 1) */
        int timeLimit = 0;

        /** Action (group 2) */
        String actionName = "";
        String actionParam = "";

        /** Selector (group 3/4) - can be an Usermask or another extban */
        String selectorName = "";
        String selectorParam = "";
        Object selectorValue = "";


        extBanStr = s;

        passes: while(groupPass <= 4) {

            /* Contains the current extban being analyzed */
            curExtBanName     = "";
            curExtBanParam    = "";
            curExtBanArgument = "";

            extBanSplit = extBanStr.split(":");

            /* extban string does not begin with ~ => not an extban */
            //if (extBanStr.startsWith("~") == false) { System.out.println("Invalid format0.0"); throw new InvalidFormatException(); }

            try { curExtBanName = extBanSplit[0].replaceFirst("~", ""); }
            catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }


            if (extBans.containsKey(curExtBanName) == true) { }
            else if (extBansReverse.containsKey(curExtBanName) == true) { curExtBanName = extBansReverse.get(curExtBanName); }
            else { throw new InvalidFormatException(); }


            /* Pass 1: catch all the group 1 (time limited ban) */
            if (groupPass == 1) {
                switch(curExtBanName) {
                    /* time = 2 arguments */
                    case "time": {
                        extBanSplit = extBanStr.split(":", 3);

                        try {
                            curExtBanParam = extBanSplit[1];
                            timeLimit = Integer.valueOf(curExtBanParam);
                        }
                        catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }
                        catch (NumberFormatException e) { throw new InvalidFormatException(); }

                        try { curExtBanArgument = extBanSplit[2]; }
                        catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }

                        break;
                    }

                    default: {
                        try { curExtBanArgument = extBanStr; }
                        catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }
                        groupPass = 2; break;
                    }
                }

                /* if the argument begins with ~, the selector is an extban => need another pass */
                if (curExtBanArgument.startsWith("~") == true) groupPass = 2;
                /* else it should be an usermask */
                else {
                    groupPass = 99;

                    if (UserMask.isValidStrict(curExtBanArgument) == true) {
                        UserMask usermask;
                        usermask = UserMask.create(curExtBanArgument);
                        selectorValue = usermask;
                    }
                    else { selectorValue = curExtBanArgument; }
                }
            }

            /* Pass 2: catch the actions (group 2) */
            else if (groupPass == 2) {

                switch(curExtBanName) {
                    /* 1 parameter */
                    case "quiet":
                    case "nickchange":
                    case "join": {
                        /* Resplit to match the correct number of arguments */
                        extBanSplit = extBanStr.split(":", 2);

                        try { curExtBanArgument = extBanSplit[1]; }
                        catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }

                        break;
                    }

                    /* 2 parameters */
                    case "msgbypass":
                    case "forward":
                    case "flood": {
                        /* Resplit to match the correct number of arguments */
                        extBanSplit = extBanStr.split(":", 3);

                        try { curExtBanParam = extBanSplit[1]; }
                        catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }

                        try { curExtBanArgument = extBanSplit[2]; }
                        catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }

                        break;
                    }

                    default: groupPass = 3; continue passes;
                }

                actionName = curExtBanName;
                actionParam = curExtBanParam;

                /* if the argument begins with ~, the selector is an extban => need another pass */
                if (curExtBanArgument.startsWith("~") == true) groupPass = 3;
                /* else it should be an usermask */
                else {
                    groupPass = 99;

                    if (UserMask.isValidStrict(curExtBanArgument) == true) {
                        UserMask usermask;
                        usermask = UserMask.create(curExtBanArgument);
                        selectorValue = usermask;
                    }
                    else { selectorValue = curExtBanArgument; }

                }

            }

            /* Pass 3: catch the selectors (group 3) */
            else if (groupPass == 3) {

                switch(curExtBanName) {
                    /* 1 parameter */
                    case "account":
                    case "channel":
                    case "country":
                    case "operclass":
                    case "realname":
                    case "security-group":
                    case "asn":
                    case "certfp": {
                        /* Resplit to match the correct number of arguments */
                        extBanSplit = extBanStr.split(":", 2);

                        try { curExtBanArgument = extBanSplit[1].replaceFirst("~", "?"); }
                        catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }

                        break;
                    }

                    default: groupPass = 4; continue passes;
                }

                selectorName = curExtBanName;
                selectorValue = curExtBanArgument;

                /* if the argument begins with ~, the selector is an extban => need another pass */
                if (curExtBanArgument.startsWith("~") == true) groupPass = 4;
                /* else it should be an usermask */
                else {
                    groupPass = 99;

                    if (UserMask.isValidStrict(curExtBanArgument) == true) {
                        UserMask usermask;
                        usermask = UserMask.create(curExtBanArgument);
                        selectorValue = usermask;
                    }
                    else { selectorValue = curExtBanArgument; }

                }

            }

            /* Pass 4: catch the group 4 */
            else if (groupPass == 4) {

                switch(curExtBanName) {
                    /* 1 parameter */
                    case "partmsg": {
                        /* Resplit to match the correct number of arguments */
                        extBanSplit = extBanStr.split(":", 2);

                        try { curExtBanArgument = extBanSplit[1]; }
                        catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }

                        break;
                    }

                    /* 2 parameters */
                    case "text": {
                        /* Resplit to match the correct number of arguments */
                        extBanSplit = extBanStr.split(":", 3);

                        try { curExtBanParam = extBanSplit[1]; }
                        catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }

                        try { curExtBanArgument = extBanSplit[2]; }
                        catch (IndexOutOfBoundsException e) { throw new InvalidFormatException(); }



                        break;
                    }

                    default: { throw new InvalidFormatException(); }

                }

                selectorName = curExtBanName;
                selectorParam = curExtBanParam;

                if (UserMask.isValidStrict(curExtBanArgument) == true) {
                    UserMask usermask;
                    usermask = UserMask.create(curExtBanArgument);
                    selectorValue = usermask;
                }
                else { selectorValue = curExtBanArgument; }
                groupPass = 99;
            }
            extBanStr = curExtBanArgument;
        }

        /* Reconstruction */
        reconstructed = new StringJoiner(":");
        if (timeLimit > 0) reconstructed.add("~time:" + timeLimit);
        if (actionName.isEmpty() == false) reconstructed.add(String.format("~%s", actionName.toLowerCase()));
        if (actionParam.isEmpty() == false) reconstructed.add(String.format("%s", actionParam.toLowerCase()));
        if (selectorName.isEmpty() == false) reconstructed.add(String.format("~%s", selectorName.toLowerCase()));
        if (selectorParam.isEmpty() == false) reconstructed.add(String.format("%s", selectorParam.toLowerCase()));
        if (String.valueOf(selectorValue).isEmpty() == false) reconstructed.add(String.format("%s", String.valueOf(selectorValue).toLowerCase()));


        //log.debug(String.format("Extban::create: extban %s :: TL='%s' AN='%s' AP='%s' SN='%s' SP='%s' SV='%s' :: orig %s", reconstructed.toString(), timeLimit, actionName, actionParam, selectorName, selectorParam, selectorValue, s));

        b64signature = getSignature(reconstructed.toString());

        if (exists(b64signature) == true) {
            extban = extbans64.get(b64signature);
            //log.debug(String.format("Extban::create: found extban signature %s for %s", b64signature, reconstructed.toString()));
        }
        else {
            extban = new Extban(timeLimit, actionName, actionParam, selectorName, selectorParam, selectorValue, reconstructed.toString());
            //log.debug(String.format("Extban::create: new extban signature %s for %s", b64signature, reconstructed.toString()));
        }

        return extban;
    }

    public static int getExtbanCounter() {
        return extBans.size();
    }

    private Extban(int tL, String aN, String aV, String sN, String sP, Object sV, String orig) {
        this.extBanRaw     = orig;

        this.timeLimit     = tL;
        this.actionName    = aN;
        this.actionValue   = aV;

        this.selectorName  = sN;
        this.selectorParam = sP;
        this.selectorValue = sV;

        this.b64signature  = getSignature(orig);

        extbans64.put(b64signature, this);
        extBanList.put(orig, this);

    }

    public boolean matches(Nick nick) {

        String matcher;

        Wildcard wildcard = Wildcard.create(String.valueOf(this.selectorValue));

        //log.debug(String.format("Extban::matches: extban %s :: TL='%s' AN='%s' AP='%s' SN='%s' SP='%s' SV='%s'", this.toString(), timeLimit, actionName, actionValue, selectorName, selectorParam, selectorValue));

        switch (this.selectorName) {
            case "account":
                try { matcher = nick.getAccount().getName(); }
                catch (Exception e) { return false; }

                if (wildcard.matches(matcher) == true) return true;
                else return false;

            case "channel":
                try { matcher = nick.getAccount().getName(); }
                catch (Exception e) { return false; }

                for (Channel c: nick.getChanList().keySet()) if (wildcard.matches(c.getName()) == true) return true;
                return false;

            case "country":
                try { matcher = nick.getCountry("code").toLowerCase(); }
                catch (Exception e) { return false; }

                if (wildcard.matches(matcher) == true) return true;
                else return false;

            case "operclass":
                try { matcher = nick.getOperClass(); }
                catch (Exception e) { return false; }

                if (wildcard.matches(matcher) == true) return true;
                else return false;

            case "realname":
                try { matcher = nick.getRealName(); }
                catch (Exception e) { return false; }

                if (wildcard.matches(matcher) == true) return true;
                else return false;

            case "security-group": return false; /* TODO: to implement */
            case "certfp":
                try { matcher = nick.getCertFP(); }
                catch (Exception e) { return false; }

                if (wildcard.matches(matcher) == true) return true;
                else return false;


            case "":
                for (UserMask um: nick.getAllUserMasks()) {
                    if (wildcard.matches(um.getString()) == true) return true;
                    else return false;
                }

        }

        return false;
    }

    public String getRaw() {
        return this.extBanRaw;
    }

    public int getTimeLimit() {
        return this.timeLimit;
    }

    public String getActionName() {
        return this.actionName;
    }

    public String getActionValue() {
        return this.actionValue;
    }

    public String getSelectorName() {
        return this.selectorName;
    }

    public String getSelectorParam() {
        return this.selectorParam;
    }

    public String getSelectorValue() {
        return String.valueOf(this.selectorValue);
    }

    @Override public String getString() {
        return this.extBanRaw;
    }

    @Override public String toString() {
        return this.extBanRaw;
    }


}
