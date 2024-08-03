package xyz.mjav.theqbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xyz.mjav.theqbot.exceptions.NickNotFoundException;

/**
 * <pre>
 * A CService command containing the following properties:
 * - (Nick) nick from
 * - (boolean) is the nick from authed
 * - (Nick) nick to
 * - (UserAccount) if the nick from is authed, its associated user account
 * - (String) the command name (such as CHANLEV)
 * - (List) the command arguments (such as [#channel nick +fla-gs] for a command CHANLEV #channel nick +fla-gs)
 * - (IrcMessage) the associated IRC message
 * - (int) the associated result (once the command is completed)
 * </pre>
 */
public final class CSCommand {

    private static Logger log = LogManager.getLogger("common-log");
    //private static Logger jsonLog = LogManager.getLogger("common-json-log");

    private final String commandName;
    //private final String direction; // Like inbound or outbound
    private final List<String> args;
    private final Nick fromNick;
    private final Nick toNick;
    private final boolean isNickAuthed;
    private final UserAccount fromNickAccount;
    private final IrcMessage ircMsg;
    private       int result = 0xff;

    public static CSCommand create(IrcMessage ircMsg) {
        Nick fromNick = ircMsg.getFromNick();
        Nick toNick   = ircMsg.getTargetNick();
        List<String> cmdArgs;
        List<String> cmdArgs2;
        String str = ircMsg.getArgv().get(1);
        String commandName;

        cmdArgs = Arrays.asList(str.split(" "));
        cmdArgs.set(0, cmdArgs.get(0).toUpperCase());
        commandName = cmdArgs.get(0);

        cmdArgs2 = cmdArgs.subList(1, cmdArgs.size());

        return new CSCommand(ircMsg, fromNick, toNick, commandName, cmdArgs2);
    }

    private CSCommand(IrcMessage ircMsg, Nick fNick, Nick tNick, String command, List<String> args) {
        this.fromNick = fNick;
        this.commandName = command;
        this.args = args;
        this.ircMsg = ircMsg;
        this.toNick = tNick;

        if (fNick.isAuthed() == true) {
            this.isNickAuthed = true;
            this.fromNickAccount = fNick.getAccount();
        }
        else {
            this.isNickAuthed = false;
            this.fromNickAccount = null;
        }

        this.log(); // FIXME: to remove once CService class put the error code in the command
    }

    public String getCommandName() {
        return this.commandName;
    }

    public List<String> getArgs() {
        return new ArrayList<>(this.args);
    }

    public Nick getFromNick() {
        return this.fromNick;
    }

    public Nick getToNick() throws NickNotFoundException {
        if (this.toNick == null) throw new NickNotFoundException();
        return this.toNick;
    }

    public boolean isFromNickAuthed() {
        return this.isNickAuthed;
    }

    public UserAccount getFromNickAccount() {
        return this.fromNickAccount;
    }

    public String toString() {
        return this.ircMsg.toString();
    }

    public void setResult(int i) {
        this.result = i;
    }

    public void log() {

        String accountName;
        List<String> args = new ArrayList<>(this.args);

        /* Need to remove potential sensitive data from AUTH command (such as passwords, emails) */
        switch (this.commandName.toUpperCase()) {
            case "AUTH": /* AUTH [args: username password] */
                try { args.set(1, "<password masked>"); }
                catch (IndexOutOfBoundsException e) { }
                break;
            case "HELLO": /* HELLO [args password email] */
                try { args.set(1, "<password masked>"); args.set(0, "<email masked>"); }
                catch (IndexOutOfBoundsException e) { }
                break;
        }

        String resultString = Const.getCmdErrNoString(this.result);


        log.debug(String.format("CSCommand::CSCommand: parsed CService command:"));
        log.debug(String.format("CSCommand::CSCommand:   |    from: %s", this.fromNick.getNick()));
        log.debug(String.format("CSCommand::CSCommand:   |      to: %s", this.toNick.getNick()));
        log.debug(String.format("CSCommand::CSCommand:   | command: %s", this.commandName));
        log.debug(String.format("CSCommand::CSCommand:   |    args: %s", String.valueOf(args)));
        log.debug(String.format("CSCommand::CSCommand:   |  authed: %s", this.isNickAuthed));
        log.debug(String.format("CSCommand::CSCommand:   | account: %s", this.fromNickAccount));
        log.debug(String.format("CSCommand::CSCommand:   |  result: %s", resultString));
        log.debug(String.format("CSCommand::CSCommand:   |  qmsgid: %s", this.ircMsg.getQMsgId()));

        try { accountName = this.fromNickAccount.getName(); }
        catch (NullPointerException e) { accountName = ""; }

        Map<String, String> logMap = new TreeMap<>();
        logMap.put("from", this.fromNick.getNick());
        logMap.put("to", this.toNick.getNick());
        logMap.put("command", this.commandName);
        logMap.put("args", String.valueOf(this.args));
        logMap.put("authed", String.valueOf(this.isNickAuthed));
        logMap.put("account", accountName);
        logMap.put("result", resultString);

        ESLog esLog = new ESLog.Builder()
            .type("qcommand")
            .logLevel("DEBUG")
            .qmsgid(this.ircMsg.getQMsgId())
            .logMap(logMap)
            .build();

        CompletableFuture.runAsync(() -> {
                try { ESClient esClient = ESClient.getInstance();
                    esClient.index(esLog.toString()); }
                catch (Exception e) { log.error("UnrealIRCd::getResponse: cannot send handlePrivmsg() to CompletableFuture.", e); return; }
        });



    }
}
