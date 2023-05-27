import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;

public abstract class Const {

    public static final Integer      AUTH_TYPE_PLAIN         = 0x0001;
    public static final Integer      AUTH_TYPE_REAUTH        = 0x0002;
    public static final Integer      AUTH_TYPE_CERTFP        = 0x0003;
    public static final Integer      AUTH_TYPE_SASL_PLAIN    = 0x0004;
    public static final Integer      AUTH_TYPE_SASL_EXT      = 0x0005;

    public static final Integer      DEAUTH_TYPE_QUIT        = 0x0006;
    public static final Integer      DEAUTH_TYPE_SQUIT       = 0x0007;
    public static final Integer      DEAUTH_TYPE_KILL        = 0x0008;
    public static final Integer      DEAUTH_TYPE_GLINE       = 0x0009;
    public static final Integer      DEAUTH_TYPE_MANUAL      = 0x000a;
    public static final Integer      DEAUTH_TYPE_EXPIRE      = 0x000b;


    private static final Map<String, Integer> constStrToInt = Map.ofEntries(
        entry("Plain",   AUTH_TYPE_PLAIN),
        entry("Reauth",   AUTH_TYPE_REAUTH),
        entry("CertFP",   AUTH_TYPE_CERTFP),
        entry("SASL-PLAIN",   AUTH_TYPE_SASL_PLAIN),
        entry("SASL-EXTERNAL",   AUTH_TYPE_SASL_EXT),
        entry("Quit",   DEAUTH_TYPE_QUIT),
        entry("Squit",   DEAUTH_TYPE_SQUIT),
        entry("Kill",   DEAUTH_TYPE_KILL),
        entry("Gline",   DEAUTH_TYPE_GLINE),
        entry("Logout",   DEAUTH_TYPE_MANUAL),
        entry("Expire",   DEAUTH_TYPE_EXPIRE)
    );

    private static final Map<Integer, String> constIntToStr = dictionaryRev(constStrToInt);

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
