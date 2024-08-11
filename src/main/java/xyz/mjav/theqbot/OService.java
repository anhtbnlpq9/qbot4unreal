package xyz.mjav.theqbot;

import static java.util.Map.entry;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import java.util.TreeMap;


public class OService extends Service {


    private static final Map<String, String> CMD_ALWAYS = Map.ofEntries(

    );

    private static final Map<String, String> CMD_NOAUTH = Map.ofEntries(

    );

    private static final Map<String, String> CMD_AUTHED = Map.ofEntries(

    );

    private static final Map<String, String> CMD_STAFF = Map.ofEntries(

    );

    private static final Map<String, String> CMD_OPER = Map.ofEntries(

    );

    private static final Map<String, String> CMD_ADMIN = Map.ofEntries(

    );

    private static final Map<String, String> CMD_DEVGOD = Map.ofEntries(

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
     * Class constructor
     * @param protocol reference to the protocol
     * @param database reference to the database
     */
    public OService(Protocol protocol, Database database) {
        this.protocol = protocol;
        this.database = database;
    }

    public void runOService(Config config, Protocol protocol) {

        Long unixTime;

        String myUniq;

        Map<String, String> myModes = new TreeMap<>();

        this.dispatcher = new Dispatcher(config, database, protocol);
        this.config = config;
        myUniq = config.getServerId() + config.getOServeUniq();
        chanJoinModes = config.getCserveChanDefaultModes();
        unixTime = Instant.now().getEpochSecond();

        /*
         * Converts the modes string to a Map
         */
        for (Character c: config.getOServeModes().toCharArray()) {
            if (c.equals('+') == true) continue;
            myModes.put(c.toString(), "");
        }

        this.myUserNode =  new Nick.Builder()
                            .uid(myUniq)
                            .nick(config.getOServeNick())
                            .ident(config.getOServeIdent())
                            .host(config.getOServeHost())
                            .realHost(config.getOServeHost())
                            .realName(config.getOServeRealName())
                            .modes(myModes)
                            .server(Server.getServerBySid(config.getServerId()))
                            .userTS(unixTime)
                            .ip("fwAAAQ==") /* IP address = 127.0.0.1 */
                            .build();


        this.myServerNode = Server.getServerBySid(config.getServerId());
        this.myServerNode.addLocalUser(this.myUserNode);

        protocol.sendUid(myUserNode);

        protocol.setOService(this);

    }

    public void run() {

    }


    public void handleMessage(IrcMessage ircMsg) throws Exception {

        CSCommand osCommand = CSCommand.create(ircMsg);

        switch (osCommand.getCommandName()) {

            case "NETINFO":           oServeNetinfo(osCommand); break;
            case "VERSION":           oServeVersion(osCommand); break;

            default:                  sendReply(osCommand.getFromNick(), Messages.strErrCommandUnknown); break;
        }

    }


    private void oServeHelp(CSCommand csCommand) {
        /* HELP <command> */

        /** Command name UPPERCASE */
        String helpCommandName;

        try { helpCommandName = csCommand.getArgs().get(0).toUpperCase(); }
        catch (IndexOutOfBoundsException e) { /*getShowcommands(csCommand, "showcommandsshort");*/ return; }

        /* Commands does not exist */
        if (CMD_LIST.containsKey(helpCommandName) == false) {
            sendReply(csCommand.getFromNick(), String.format(Messages.strErrNoAccess));
            return;
        }

       // sendHelp(csCommand.getFromNick(), helpCommandName);

    }

    private void oServeVersion(CSCommand csCommand) {
        sendReply(csCommand.getFromNick(), Const.QBOT_VERSION_STRING);
    }

    private void oServeNetinfo(CSCommand csCommand) {
        sendReply(csCommand.getFromNick(), Const.QBOT_VERSION_STRING);
    }
}