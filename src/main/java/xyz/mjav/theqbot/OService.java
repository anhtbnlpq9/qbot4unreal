package xyz.mjav.theqbot;

import java.time.Instant;
import java.util.Map;

import java.util.TreeMap;


public class OService extends Service {

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

        this.myUserNode = new Nick.Builder()
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

        protocol.sendUid(myUserNode);

        protocol.setOService(this);

    }

    public void run() {

    }


    public void handleMessage(Nick fromNickRaw, String str) throws Exception {
        Nick fromNick = fromNickRaw;
        String commandName;
        String[] strSplit;

        try {
            strSplit = str.split(" ");
            commandName = strSplit[0].toUpperCase();
        }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        switch (commandName) {
            default:                  sendReply(fromNick, Messages.strErrCommandUnknown); break;
        }

    }
}