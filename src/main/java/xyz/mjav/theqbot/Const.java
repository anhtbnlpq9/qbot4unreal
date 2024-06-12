package xyz.mjav.theqbot;

import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

public abstract class Const {

    private static Config config = Config.getConfig();

    /* Generic constants */
    public static final String QBOT_VERSION_NUMBER = "1.1";
    public static final String QBOT_VERSION_STRING = String.format("TheQBot4Unreal-%s", QBOT_VERSION_NUMBER);

    public static final String UNREAL_PROTOCOL_VERSION = "6100";
    public static final String UNREAL_VERSION_FLAGS    = "";
    public static final String UNREAL_VERSION_FULLTEXT = "qbot4unreal";

    /* Password pattern to use */
    public static final String   PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{" + config.getCServiceAccountMinPassLength() + "," + config.getCServiceAccountMaxPassLength() + "}$";

    /* List of AUTH/DEAUTH types */
    public static final int      AUTH_TYPE_PLAIN                = 0x0001; /* login/pass AUTH-command auth */
    public static final int      AUTH_TYPE_REAUTH_PLAIN         = 0x0002; /* re-authentication of previously AUTH-command authed user */
    public static final int      AUTH_TYPE_CERTFP               = 0x0003; /* certfp auth through AUTH command */
    public static final int      AUTH_TYPE_SASL_PLAIN           = 0x0004; /* plain login/pass sasl auth */
    public static final int      AUTH_TYPE_SASL_EXT             = 0x0005; /* certfp sasl auth */
    public static final int      AUTH_TYPE_SASL_ECDSA_NIST256P  = 0x0006; /* not implemented */
    public static final int      AUTH_TYPE_SASL_SCRAM_SHA256    = 0x0007; /* not implemented */
    public static final int      AUTH_TYPE_REAUTH_SASL          = 0x0008; /* re-authentication of previously sasl-authed user */
    /* Reserved values
    public static final int      AUTH_TYPE_xxx                  = 0x0009;
        ...
    public static final int      AUTH_TYPE_xxx                  = 0x001f;
    */

    public static final int      DEAUTH_TYPE_QUIT        = 0x0020;
    public static final int      DEAUTH_TYPE_SQUIT       = 0x0021;
    public static final int      DEAUTH_TYPE_KILL        = 0x0022;
    public static final int      DEAUTH_TYPE_GLINE       = 0x0023;
    public static final int      DEAUTH_TYPE_MANUAL      = 0x0024;
    public static final int      DEAUTH_TYPE_EXPIRE      = 0x0025;
    public static final int      DEAUTH_TYPE_DROP        = 0x0026;
    public static final int      DEAUTH_TYPE_SUSPEND     = 0x0027;
    /* Reserved values
    public static final int      DEAUTH_TYPE_xxx         = 0x0028;
        ...
    public static final int      DEAUTH_TYPE_xxx         = 0x003f;
    */

    private static final Map<String, Integer> constStrToInt = Map.ofEntries(
        entry("Plain",              AUTH_TYPE_PLAIN),
        entry("Reauth (Plain)",     AUTH_TYPE_REAUTH_PLAIN),
        entry("Reauth (SASL)",      AUTH_TYPE_REAUTH_SASL),
        entry("CertFP",             AUTH_TYPE_CERTFP),
        entry("SASL-PLAIN",         AUTH_TYPE_SASL_PLAIN),
        entry("SASL-EXTERNAL",      AUTH_TYPE_SASL_EXT),
        entry("Quit",               DEAUTH_TYPE_QUIT),
        entry("Squit",              DEAUTH_TYPE_SQUIT),
        entry("Kill",               DEAUTH_TYPE_KILL),
        entry("Gline",              DEAUTH_TYPE_GLINE),
        entry("Logout",             DEAUTH_TYPE_MANUAL),
        entry("Expire",             DEAUTH_TYPE_EXPIRE),
        entry("Account dropped",    DEAUTH_TYPE_DROP),
        entry("Account suspended",  DEAUTH_TYPE_SUSPEND)
    );

    private static final Map<Integer, String> constIntToStr = buildRevDict(constStrToInt);

    /* List of entities */
    public static final int      ENTITY_CHANNEL           = 0x0001;
    public static final int      ENTITY_USERACCOUNT       = 0x0002;
    public static final int      ENTITY_NICK              = 0x0003;
    public static final int      ENTITY_SERVER            = 0x0004;

    /* List of chan lists */
    public static final int      CHANBEI_BANS             = 0x0001;
    public static final int      CHANBEI_EXCEPTS          = 0x0002;
    public static final int      CHANBEI_INVITES          = 0x0003;


    public static String getAuthTypeString(Integer type) {
        if (constIntToStr.containsKey(type) == true) return constIntToStr.get(type);
        else { return "Other/Unknown";}
    }



    /* List of commands error codes */
    public static final int      CMD_ERRNO_SUCCESS                     = 0x0000; /* command successful */
    public static final int      CMD_ERRNO_FAILED                      = 0x0001; /* generically failed */
    public static final int      CMD_ERRNO_UNKNOWNERR                  = 0x0002; /* unknown failure */
    public static final int      CMD_ERRNO_DENIED                      = 0x0003; /* generically denied */
    public static final int      CMD_ERRNO_NOTFOUND                    = 0x0004; /* command not found */
    public static final int      CMD_ERRNO_SYNTAX                      = 0x0005; /* syntax error */
    public static final int      CMD_ERRNO_a                           = 0x0020; /*  */
    public static final int      CMD_ERRNO_b                           = 0x0021; /*  */
    public static final int      CMD_ERRNO_c                           = 0x0022; /*  */
    public static final int      CMD_ERRNO_d                           = 0x0023; /*  */
    public static final int      CMD_ERRNO_e                           = 0x0024; /*  */

    private static final Map<String, Integer> cmdErrNoStrToInt = Map.ofEntries(
        entry("Success",               CMD_ERRNO_SUCCESS),
        entry("ErrFailed",             CMD_ERRNO_FAILED),
        entry("ErrUnknown",            CMD_ERRNO_UNKNOWNERR),
        entry("ErrDenied",             CMD_ERRNO_DENIED),
        entry("ErrUnknownCommand",     CMD_ERRNO_NOTFOUND),
        entry("ErrSyntax",             CMD_ERRNO_SYNTAX)
    );

    private static final Map<Integer, String> cmdErrNoIntToStr = buildRevDict(cmdErrNoStrToInt);

    public static String getCmdErrNoString(Integer errno) {
        if (cmdErrNoIntToStr.containsKey(errno) == true) return cmdErrNoIntToStr.get(errno);
        else { return "Other/Unknown"; }
    }



    /**
     * Builds a reverse dictionnary of the given Map
     * @param dictionary a Map of String -> Integer
     * @return a Map of Integer -> String
     */
    private static Map<Integer, String> buildRevDict(Map<String, Integer> dictionary) {
        Map<Integer, String> dictionaryRev = new HashMap<>();
        dictionary.forEach( (string, integer) -> {
            dictionaryRev.put(integer, string);
        });
        return dictionaryRev;
    }


    /* List of flags types */
    public static final int     FLAGTYPE_USERFLAG       = 0x0001;
    public static final int     FLAGTYPE_CHANFLAG       = 0x0002;
    public static final int     FLAGTYPE_CHANLEV        = 0x0003;


    /* Column widths */
    public static final int COLUMN_ID_WIDTH           =   4;
    public static final int COLUMN_DATE_WIDTH         =  25;
    public static final int COLUMN_NICK_WIDTH         =  15;
    public static final int COLUMN_CHAN_WIDTH         =  30;
    public static final int COLUMN_MASK_WIDTH         =  40;
    public static final int COLUMN_MESSAGE_WIDTH      =  60;



    /* User account prefix character */
    public static final String USER_ACCOUNT_PREFIX      = "#";
}
