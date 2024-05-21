package xyz.mjav.theqbot;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xyz.mjav.theqbot.exceptions.NickNotFoundException;
import xyz.mjav.theqbot.exceptions.UserNoAuthException;

/**
 * Service class
 * Contains the generic stuff to handle a service (Q, O, ...)
 */
public abstract class Service {

    protected static Logger log = LogManager.getLogger("common-log");
    protected static Logger jsonLog = LogManager.getLogger("common-json-log");

    protected static SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    protected Service instance;

    protected Nick myUserNode;

    protected Server myServerNode;

    protected Protocol protocol;

    protected Database database;

    protected Config config;

    protected Dispatcher dispatcher;

    protected String chanJoinModes    = "";
    protected String userChanJoinMode = "";

    protected Boolean isServiceReady = false;

    //protected String chanJoinModes    = "";
    //protected String userChanJoinMode = "";

    public Service() {
        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    protected Boolean isServiceReady() {
        return this.isServiceReady;
    }

    /**
     * Sends a reply to the user. Reply can be either NOTICE or PRIVMSG depending on user account preference.
     * @param to
     * @param s
     */
    protected void sendReply(Nick to, String s) {

        Integer userFlags = 0;
        UserAccount useraccount = null;

        try { useraccount = to.getAccount(); }
        catch (UserNoAuthException e) {  }

        if (useraccount != null) userFlags = useraccount.getFlags();

        if (Flags.isUserPrivMsg(userFlags) == true) protocol.sendPrivMsg(myUserNode, to, s);
        else protocol.sendNotice(myUserNode, to, s);

    }

    protected void sendReply(CSCommand csCommand, String s) {
        sendReply(csCommand.getFromNick(), s);
    }

    protected Boolean isPassComplex(String pass) {
        if (pass.matches(Const.PASSWORD_PATTERN) == false) return false;
        return true;
    }

    public void handleChanMode(Channel chan, Map<String, Map<String, String>> modChanModesAll) {

        Map<String, String> chanUserModes = modChanModesAll.get("chanUserModes");

        String[] userChanModeNickList;
        for (Map.Entry<String, String> entry: chanUserModes.entrySet()) {
            String mode = entry.getKey();
            String nickList = entry.getValue();

            Nick userNode = null;
            UserAccount userAccount = null;

            userChanModeNickList = nickList.split(" ");

            for (String nick: userChanModeNickList) {

                try { userNode = Nick.getUserByNickCi(nick); }
                catch(NickNotFoundException e) { continue; }

                if (userNode == null) continue; /* Special case because of the String.join, string might begin with a space, so we ignore it and skip to the next turn */

                if (userNode.getNick().equals(myUserNode.getNick()) && mode.equals("-" + userChanJoinMode)) { /* Protects the bot from being demoted on the channel */

                    log.warn(String.format("Bot critical mode has changed on channel %s", chan));
                    try {
                        protocol.setMode( chan, "+" + userChanJoinMode, myUserNode.getNick());
                    }
                    catch (Exception e) {
                        log.error(String.format("Could not set mode on channel %s", chan));
                    }
                }
                else if (userNode.isAuthed() == true) { /* Look at mode change for regular authed users */

                    try { userAccount = userNode.getAccount(); }
                    catch (Exception e) { log.error(String.format("CService/handleMode: could not set user account of authed nick %s", userNode.getNick()), e); return; }

                    /* Check punishment flags */
                    /* +q/+a/+h are never punished (to implement for +h in the future) */
                    if (Flags.isChanLDenyOp(userAccount.getChanlev(chan)) == true && mode.equals("+o") == true) {
                        try {
                            protocol.setMode(myUserNode, chan, "-o", userNode.getNick());
                            //return; /* Return, else protect might be run */
                        }
                        catch (Exception e) { log.error(String.format("Could not set mode on channel %s", chan)); }
                    }
                    else if (Flags.isChanLDenyVoice(userAccount.getChanlev(chan)) == true && mode.equals("+v") == true) {
                        try {
                            protocol.setMode(myUserNode, chan, "-v", userNode.getNick());
                            //return; /* Return, else protect might be run */
                        }
                        catch (Exception e) { log.error(String.format("Could not set mode on channel %s", chan)); }
                    }

                    /* Check protect flags */
                    else if (Flags.isChanLProtect(userAccount.getChanlev(chan)) == true && Flags.isChanLOwner(userAccount.getChanlev(chan)) && mode.equals("-q") == true) {
                        try { protocol.setMode(myUserNode, chan, "+q", userNode.getNick()); }
                        catch (Exception e) { log.error(String.format("Could not set mode on channel %s", chan)); }
                    }
                    else if (Flags.isChanLProtect(userAccount.getChanlev(chan)) == true && Flags.isChanLMaster(userAccount.getChanlev(chan)) && mode.equals("-a") == true) {
                        try { protocol.setMode(myUserNode, chan, "+a", userNode.getNick()); }
                        catch (Exception e) { log.error(String.format("Could not set mode on channel %s", chan)); }
                    }
                    else if (Flags.isChanLProtect(userAccount.getChanlev(chan)) == true && Flags.isChanLOp(userAccount.getChanlev(chan)) && mode.equals("-o") == true) {
                        try { protocol.setMode(myUserNode, chan, "+o", userNode.getNick()); }
                        catch (Exception e) { log.error(String.format("Could not set mode on channel %s", chan)); }
                    }
                    else if (Flags.isChanLProtect(userAccount.getChanlev(chan)) == true && Flags.isChanLHalfOp(userAccount.getChanlev(chan)) && mode.equals("-h") == true) {
                        try { protocol.setMode(myUserNode, chan, "+h", userNode.getNick()); }
                        catch (Exception e) { log.error(String.format("Could not set mode on channel %s", chan)); }
                    }
                    else if (Flags.isChanLProtect(userAccount.getChanlev(chan)) == true && Flags.isChanLVoice(userAccount.getChanlev(chan)) && mode.equals("-v") == true) {
                        try { protocol.setMode(myUserNode, chan, "+v", userNode.getNick()); }
                        catch (Exception e) { log.error(String.format("Could not set mode on channel %s", chan)); }
                    }
                }
            }
        }
    }

    public Nick getMyUserNode() {
        return this.myUserNode;
    }
    //protected String collectionToString() {

        //return "";
    //}

}
