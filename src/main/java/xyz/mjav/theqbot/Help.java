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

    /* Converts commands to a similar command (e.g DEOP will point to OP) + gives the file name (.txt) */
    protected static final Map<String, String> CSERVICE_CMD_LIST = Map.ofEntries(
        entry("ADDCHAN",                "ADDCHAN"),
        entry("ADDUSER",                "ADDUSER"),
        entry("ADMIN",                  "ADMIN"),
        entry("AUTH",                   "AUTH"),
        entry("AUTHHISTORY",            "AUTHHISTORY"),
        entry("AUTOLIMIT",              "AUTOLIMIT"),
        entry("BANCLEAR",               "BEICLEAR"),
        entry("BANDEL",                 "BEIDEL"),
        entry("BANLIST",                "BEILIST"),
        entry("BANTIMER",               "BANTIMER"),
        entry("CERTFP",                 "CERTFP"),
        entry("CERTFPADD",              "CERTFPADD"),
        entry("CERTFPDEL",              "CERTFPADD"),
        entry("CHANFLAGS",              "CHANFLAGS"),
        entry("CHANINFO",               "CHANINFO"),
        entry("CHANLEV",                "CHANLEV"),
        entry("CHANLEVHISTORY",         "CHANLEVHISTORY"),
        entry("CHANMODE",               "CHANMODE"),
        entry("CHANOPHISTORY",          "CHANOPHISTORY"),
        entry("CHANSTAT",               "CHANSTAT"),
        entry("CLEARCHAN",              "CLEARCHAN"),
        entry("CLEARTOPIC",             "CLEARTOPIC"),
        entry("CRASH",                  "CRASH"),
        entry("DEADMIN",                "ADMIN"),
        entry("DEADMINALL",             "DEOPALL"),
        entry("DEAUTH",                 "DEAUTH"),
        entry("DEAUTHALL",              "DEAUTH"),
        entry("DEHALFOP",               "HALFOP"),
        entry("DEHALFOPALL",            "DEOPALL"),
        entry("DEOP",                   "OP"),
        entry("DEOPALL",                "DEOPALL"),
        entry("DEOWNERALL",             "DEOPALL"),
        entry("DEVOICE",                "VOICE"),
        entry("DEVOICEALL",             "DEOPALL"),
        entry("DIE",                    "DIE"),
        entry("DROPCHAN",               "PERMBEI"),
        entry("DROPUSER",               "PERMBEI"),
        entry("EMAIL",                  "PERMBEI"),
        entry("EXCEPTCLEAR",            "BEICLEAR"),
        entry("EXCEPTDEL",              "BEIDEL"),
        entry("EXCEPTLIST",             "BEILIST"),
        entry("GIVEOWNER",              "GIVEOWNER"),
        entry("HALFOP",                 "HALFOP"),
        entry("HELLO",                  "HELLO"),
        entry("HELP",                   "HELP"),
        entry("INVITE",                 "INVITE"),
        entry("INVITECLEAR",            "BEICLEAR"),
        entry("INVITEDEL",              "BEIDEL"),
        entry("INVITELIST",             "BEILIST"),
        entry("LISTGHOSTNICKS",         "LISTGHOSTNICKS"),
        entry("NETCHANLIST",            "NETLIST"),
        entry("NETSERVERLIST",          "NETLIST"),
        entry("NETUSERLIST",            "NETLIST"),
        entry("NEWPASS",                ""),
        entry("NICKHISTORY",            "NICKHISTORY"),
        entry("NICKINFO",               "NICKINFO"),
        entry("OBJCOUNTER",             "OBJCOUNTER"),
        entry("OP",                     "OP"),
        entry("ORPHCHANLIST",           "ORPHCHANLIST"),
        entry("OWNER/DEOWNER",          "PERMBEI"),
        entry("PERMBAN",                "PERMBEI"),
        entry("PERMEXCEPT",             "PERMBEI"),
        entry("PERMINVITE",             "PERMBEI"),
        entry("RAW",                    "RAW"),
        entry("RECOVER",                "RECOVER"),
        entry("REGCHANLIST",            "REGLIST"),
        entry("REGUSERLIST",            "REGLIST"),
        entry("REJOIN",                 "REJOIN"),
        entry("REJOINSYNC",             "REJOINSYNC"),
        entry("REMOVEUSER",             "REMOVEUSER"),
        entry("RENCHAN",                "RENCHAN"),
        entry("REQUESTBOT",             "REQUESTBOT"),
        entry("REQUESTOWNER",           "REQUESTOWNER"),
        entry("REQUESTPASSWORD",        "REQUESTPASSWORD"),
        entry("RESET",                  "RESET"),
        entry("SERVERINFO",             "SERVERINFO"),
        entry("SETTOPIC",               "SETTOPIC"),
        entry("SETUSERPASSWORD",        "SETUSERPASSWORD"),
        entry("SHOWCOMMANDS",           "SHOWCOMMANDS"),
        entry("SLEEP",                  "SLEEP"),
        entry("SUSPENDCHAN",            "SUSPENDCHAN"),
        entry("SUSPENDUSER",            "SUSPENDUSER"),
        entry("TEMPBAN",                "TEMPBEI"),
        entry("TEMPEXCEPT",             "TEMPBEI"),
        entry("TEMPINVITE",             "TEMPBEI"),
        entry("TESTEXTBAN",             "TESTEXTBAN"),
        entry("TESTMASK",               "TESTMASK"),
        entry("TESTWILDCARD",           "TESTWILDCARD"),
        entry("TESTWILDCARD2",          "TESTWILDCARD"),
        entry("UNBANALL",               "UNBANALL"),
        entry("UNBANMASK",              "UNBANMASK"),
        entry("UNBANME",                "UNBANME"),
        entry("UNSUSPENDCHAN",          "SUSPENDCHAN"),
        entry("UNSUSPENDUSER",          "SUSPENDUSER"),
        entry("USERFLAGS",              "USERFLAGS"),
        entry("VERSION",                "VERSION"),
        entry("VOICE",                  "VOICE"),
        entry("WELCOME",                "WELCOME"),
        entry("WHOAMI",                 "WHOAMI"),
        entry("WHOIS",                  "WHOIS")
    );

    /**
     * Returns help object
     * @param type Help type (levels, commands, ...)
     * @param command Command name
     * @return help object
     */
    public static Help getHelp(String command, String section, String type) {

        Cache cache = null;
        Help help = null;

        String commandFile;

        switch (type) {
            case "command_single":
            {
                if (CSERVICE_CMD_LIST.containsKey(command) == false) help = new Help();
                else {
                    commandFile = "CService/" + CSERVICE_CMD_LIST.get(command);

                    try { cache = Cache.create(commandFile, "helpfile"); }
                    catch (FileNotFoundException e) { return new Help(); }

                    help = new Help(cache, type);
                } break;
            }

            case "command_list":
            {
                commandFile = "CService/" + command;

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
