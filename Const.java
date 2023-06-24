import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

public abstract class Const {

    public static final Integer      AUTH_TYPE_PLAIN                = 0x0001;
    public static final Integer      AUTH_TYPE_REAUTH               = 0x0002;
    public static final Integer      AUTH_TYPE_CERTFP               = 0x0003;
    public static final Integer      AUTH_TYPE_SASL_PLAIN           = 0x0004;
    public static final Integer      AUTH_TYPE_SASL_EXT             = 0x0005;
    public static final Integer      AUTH_TYPE_SASL_ECDSA_NIST256P  = 0x0006; // not implemented
    public static final Integer      AUTH_TYPE_SASL_SCRAM_SHA256    = 0x0007; // not implemented
    /* Reserved values
    public static final Integer      AUTH_TYPE_xxx    = 0x0008;
    ...
    public static final Integer      AUTH_TYPE_xxx    = 0x000f; // 0x001f ?
    */

    public static final Integer      DEAUTH_TYPE_QUIT        = 0x0020;
    public static final Integer      DEAUTH_TYPE_SQUIT       = 0x0021;
    public static final Integer      DEAUTH_TYPE_KILL        = 0x0022;
    public static final Integer      DEAUTH_TYPE_GLINE       = 0x0023;
    public static final Integer      DEAUTH_TYPE_MANUAL      = 0x0024;
    public static final Integer      DEAUTH_TYPE_EXPIRE      = 0x0025;
    public static final Integer      DEAUTH_TYPE_DROP        = 0x0026;
    public static final Integer      DEAUTH_TYPE_SUSPEND     = 0x0027;
    /* Reserved values
    public static final Integer      DEAUTH_TYPE_xxx    = 0x0028;
    ...
    public static final Integer      DEAUTH_TYPE_xxx    = 0x002f; // 0x003f ?
    */

    private static final Map<String, Integer> constStrToInt = Map.ofEntries(
        entry("Plain",              AUTH_TYPE_PLAIN),
        entry("Reauth",             AUTH_TYPE_REAUTH),
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

    private static final Map<Integer, String> constIntToStr = dictionaryRev(constStrToInt);



    public static final Integer      ENTITY_CHANNEL           = 0x0001;
    public static final Integer      ENTITY_USERACCOUNT       = 0x0002;




    public static final Integer      ENTITY_NICK              = 0x0003;
    public static final Integer      ENTITY_SERVER            = 0x0004;


    public static String getAuthTypeString(Integer type) {
        if (constIntToStr.containsKey(type)) return constIntToStr.get(type);
        else { return "Other/Unknown";}
    }

    private static Map<Integer, String> dictionaryRev(Map<String, Integer> dictionary) {
        Map<Integer, String> dictionaryRev = new HashMap<>();
        dictionary.forEach( (string, integer) -> {
            dictionaryRev.put(integer, string);
        });
        return dictionaryRev;
    }

}
