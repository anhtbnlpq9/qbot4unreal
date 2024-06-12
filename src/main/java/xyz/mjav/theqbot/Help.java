package xyz.mjav.theqbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

import java.io.FileNotFoundException;

/**
* Help class
* @author me
*/
public class Help {

    /* Converts commands to a similar command (e.g DEOP will point to OP) */
    // TODO: the approach is a bit dirty, commands should not be in the code like this
    private static final Map<String, String> CMD_ALWAYS = Map.ofEntries(
        entry("HELP", "HElP"),
        entry("VERSION", "VERSION")
    );

    private static final Map<String, String> CMD_NOAUTH = Map.ofEntries(
        entry("AUTH", "AUTH"),
        entry("HELLO", "HELLO")
    );

    private static final Map<String, String> CMD_AUTHED = Map.ofEntries(
        entry("ADDUSER", "ADDUSER"),
        entry("ADMIN", "ADMIN"),
        entry("DEADMIN", "ADMIN"),
        entry("AUTHHISTORY", "AUTHHISTORY"),
        entry("AUTOLIMIT", "AUTOLIMIT"),
        entry("BANCLEAR", "BEICLEAR"),
        entry("BANDEL", "BEIDEL"),
        entry("BANLIST", "BEILIST"),
        entry("BANTIMER", "BANTIMER"),
        entry("CERTFP", "CERTFP"),
        entry("CERTFPADD", "CERTFPADD"),
        entry("CERTFPDEL", "CERTFPADD"),
        entry("CHANFLAGS", "CHANFLAGS"),
        entry("CHANINFO", "CHANINFO"),
        entry("CHANLEV", "CHANLEV"),
        entry("CHANLEVHISTORY", "CHANLEVHISTORY"),
        entry("CHANMODE", "CHANMODE"),
        entry("CHANOPHISTORY", "CHANOPHISTORY"),
        entry("CHANSTAT", "CHANSTAT"),
        entry("CLEARCHAN", "CLEARCHAN"),
        entry("CLEARTOPIC", "CLEARTOPIC"),
        entry("DEADMINALL", "DEOPALL"),
        entry("DEAUTH", "DEAUTH"),
        entry("DEAUTHALL", "DEAUTH"),
        entry("DEHALFOPALL", "DEOPALL"),
        entry("DEOPALL", "DEOPALL"),
        entry("DEOWNERALL", "DEOPALL"),
        entry("DEVOICEALL", "DEOPALL"),
        entry("DROPCHAN", "PERMBEI"),
        entry("DROPUSER", "PERMBEI"),
        entry("EMAIL", "PERMBEI"),
        entry("EXCEPTCLEAR", "BEICLEAR"),
        entry("EXCEPTDEL", "BEIDEL"),
        entry("EXCEPTLIST", "BEILIST"),
        entry("HALFOP", "HALFOP"),
        entry("DEHALFOP", "HALFOP"),
        entry("GIVEOWNER", "GIVEOWNER"),
        entry("INVITE", "INVITE"),
        entry("INVITECLEAR", "BEICLEAR"),
        entry("INVITEDEL", "BEIDEL"),
        entry("INVITELIST", "BEILIST"),
        entry("NEWPASS", ""),
        entry("OP", "OP"),
        entry("DEOP", "OP"),
        entry("OWNER/DEOWNER", "PERMBEI"),
        entry("PERMBAN", "PERMBEI"),
        entry("PERMEXCEPT", "PERMBEI"),
        entry("PERMINVITE", "PERMBEI"),
        entry("RECOVER", "RECOVER"),
        entry("REMOVEUSER", "REMOVEUSER"),
        entry("REQUESTBOT", "REQUESTBOT"),
        entry("REQUESTOWNER", "REQUESTOWNER"),
        entry("REQUESTPASSWORD", "REQUESTPASSWORD"),
        entry("RESET", "RESET"),
        entry("SETTOPIC", "SETTOPIC"),
        entry("SHOWCOMMANDS", "SHOWCOMMANDS"),
        entry("TEMPBAN", "TEMPBEI"),
        entry("TEMPEXCEPT", "TEMPBEI"),
        entry("TEMPINVITE", "TEMPBEI"),
        entry("UNBANALL", "UNBANALL"),
        entry("UNBANMASK", "UNBANMASK"),
        entry("UNBANME", "UNBANME"),
        entry("USERFLAGS", "USERFLAGS"),
        entry("VOICE", "VOICE"),
        entry("DEVOICE", "VOICE"),
        entry("WELCOME", "WELCOME"),
        entry("WHOAMI", "WHOAMI"),
        entry("WHOIS", "WHOIS")
    );

    private static final Map<String, String> CMD_STAFF = Map.ofEntries(
        entry("NETCHANLIST", "NETLIST"),
        entry("NETSERVERLIST", "NETLIST"),
        entry("NETUSERLIST", "NETLIST"),
        entry("ORPHCHANLIST", "ORPHCHANLIST"),
        entry("REGCHANLIST", "REGLIST"),
        entry("REGUSERLIST", "REGLIST")
    );

    private static final Map<String, String> CMD_OPER = Map.ofEntries(
        entry("ADDCHAN", "ADDCHAN"),
        entry("NICKHISTORY", "NICKHISTORY"),
        entry("NICKINFO", "NICKINFO"),
        entry("SERVERINFO", "SERVERINFO"),
        entry("SUSPENDCHAN", "SUSPENDCHAN"),
        entry("UNSUSPENDCHAN", "SUSPENDCHAN"),
        entry("SUSPENDUSER", "SUSPENDUSER"),
        entry("UNSUSPENDUSER", "SUSPENDUSER")
    );

    private static final Map<String, String> CMD_ADMIN = Map.ofEntries(
        entry("REJOIN", "REJOIN"),
        entry("RENCHAN", "RENCHAN"),
        entry("SETUSERPASSWORD", "SETUSERPASSWORD")
    );

    private static final Map<String, String> CMD_DEVGOD = Map.ofEntries(
        entry("DIE", "DIE"),
        entry("LISTGHOSTNICKS", "LISTGHOSTNICKS"),
        entry("RAW", "RAW"),
        entry("REJOINSYNC", "REJOINSYNC"),
        entry("CRASH", "CRASH"),
        entry("OBJCOUNTER", "OBJCOUNTER"),
        entry("SLEEP", "SLEEP"),
        entry("TESTEXTBAN", "TESTEXTBAN"),
        entry("TESTMASK", "TESTMASK"),
        entry("TESTWILDCARD", "TESTWILDCARD"),
        entry("TESTWILDCARD2", "TESTWILDCARD")
    );

    /** List of the commands */
    private static final Map<String, String> CMD_LIST;
    static {
        CMD_LIST = new HashMap<>();
        CMD_LIST.putAll(CMD_ALWAYS);
        CMD_LIST.putAll(CMD_NOAUTH);
        CMD_LIST.putAll(CMD_AUTHED);
        CMD_LIST.putAll(CMD_STAFF);
        CMD_LIST.putAll(CMD_OPER);
        CMD_LIST.putAll(CMD_ADMIN);
        CMD_LIST.putAll(CMD_DEVGOD);
    };

    /**
     * Returns help object
     * @param type Help type (levels, commands, ...)
     * @param command Command name
     * @return help object
     */
    public static Help getHelp(String command, String type) {

        Cache cache = null;
        Help help = null;

        String commandFile;

        switch (type) {
            case "command_single":
            {
                if (CMD_LIST.containsKey(command) == false) help = new Help();
                else {
                    commandFile = CMD_LIST.get(command);

                    try { cache = Cache.create(commandFile, "helpfile"); }
                    catch (FileNotFoundException e) { return new Help(); }

                    help = new Help(cache, type);
                } break;
            }

            case "command_list":
            {
                commandFile = command;

                try { cache = Cache.create(commandFile, "helpfile"); }
                catch (FileNotFoundException e) { return new Help(); }

                help = new Help(cache, type);
            } break;

            default: help = new Help();
        }
        return help;
    }



    /* Nonstatic fields */

    /** Metadata */
    private Map<String, String> metadata;

    /** Data */
    private List<String> data;

    /** Type */
    private String type;

    /**
     * Contructor for help from cached data
     * @param cache cache object
     * @param type help type
     */
    private Help(Cache cache, String type) {
        this.data = cache.getData();
        this.metadata = cache.getMetadata();
        this.type = type;
    }

    /**
     * Constructor for a stub empty help
     */
    private Help() {
        this.data = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.type = "stub";
    }


    /**
     * Returns the data of the help
     * @return help data
     */
    public List<String> getData() {
        return this.data;
    }

    /**
     * Returns the metadata of the help
     * @return help metadata
     */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    public String getType() {
        return this.type;
    }

}
