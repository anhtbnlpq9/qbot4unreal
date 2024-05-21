package xyz.mjav.theqbot;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import xyz.mjav.theqbot.exceptions.NickNotFoundException;
import xyz.mjav.theqbot.exceptions.ServerNotFoundException;

public class IrcMessage {

    /*
     * IRC Message format
     *
     * @ircv3Tag :<FROM> COMMAND [TARGET] [ARGUMENTS]... :[LAST ARGUMENT WITH POTENTIAL SPACES]
     */

    public static final IrcMessage create(String str) {

        String fromTxt = "";
        //String targetTxt;
        String commandStartTxt = "";
        String ircv3Txt = "";

        StringJoiner commandJoiner;

        Object from;
        //Object target;

        List<String> argv = new ArrayList<>();

        String[] strSplit;
        String[] strSplitArgs;
        //String[] strSplitArgsLast;

        strSplit = str.split(" ");

        /* Gets start of the string (ircv3 tag, from, command) */
        int i1 = 0;
        for (String s: strSplit) {

            /* Detect IRCv3 tag */
            if (s.startsWith("@") == true) ircv3Txt = s;

            /* Detect source */
            else if (s.startsWith(":") == true) fromTxt = s.replaceFirst("^:", "");

            /* Detect command */
            else { commandStartTxt = s; i1++; break; }
            i1++;
        }

        /* Reconstitute the arguments */
        commandJoiner = new StringJoiner(" ");
        while(i1 < strSplit.length) {
            commandJoiner.add(strSplit[i1]);
            i1++;
        }

        strSplitArgs = commandJoiner.toString().split(" :", 2);

        for (String s: strSplitArgs[0].split(" ")) {
            if (s.isEmpty() == false) {
                if (i1 <= 2) s = s.replaceFirst("^:", "");
                argv.add(s);
            }
        }

        try { argv.add(strSplitArgs[1]); }
        catch (IndexOutOfBoundsException e) { }


        /* Check is from is a server or a nick */
        try { from = Server.getServer(fromTxt); }
        catch (ServerNotFoundException e) {
            try { from = Nick.getNick(fromTxt); }
            catch (NickNotFoundException f) {
                from = null;
            }
        }

        if (from == null) {
            from = Server.getPeer();
        }

        IrcMessage ircMsg = new IrcMessage.Builder().from(from).ircv3Tag(ircv3Txt).command(commandStartTxt).argv(argv).build();
        return ircMsg;
    }

    private Timestamp timestamp;
    private Ircv3Tag ircv3Tag;
    private String fromType;
    private Server fromServer;
    private Nick fromNick;
    private String command;
    private String targetType;
    private Nick targetNick;
    private Channel targetChan;
    private List<String> argv;
    private String qMsgId;

    private IrcMessage(Builder b) {
        this.timestamp      = b.timestamp;
        this.ircv3Tag       = b.ircv3Tag;
        this.fromType       = b.fromType;
        this.fromServer     = b.fromServer;
        this.fromNick       = b.fromNick;
        this.command        = b.command;
        this.targetType     = b.targetType;
        this.targetNick     = b.targetNick;
        this.targetChan     = b.targetChan;
        this.argv           = b.argv;

        RandomString rds = new RandomString(32);
        this.qMsgId = rds.nextString();
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public Ircv3Tag getIrcv3Tag() {
        return this.ircv3Tag;
    }

    public String getFromType() {
        return this.fromType;
    }

    public Nick getFromNick() {
        return this.fromNick;
    }

    public Server getFromServer() {
        return this.fromServer;
    }

    public Object getFrom() {
        Object from = null;
        if (fromNick != null) from = fromNick;
        else if (fromServer != null) from = fromServer;
        return from;
    }

    public String getTargetType() {
        return this.targetType;
    }

    public Nick getTargetNick() {
        return this.targetNick;
    }

    public Channel gettargetChan() {
        return this.targetChan;
    }

    public String getCommand() {
        return this.command.toUpperCase();
    }

    public List<String> getArgv() {
        return new ArrayList<String>(this.argv);
    }

    public String getQMsgId() {
        return this.qMsgId;
    }

    public String toString() {
        return this.qMsgId;
    }

    private static class Builder {

        private Timestamp timestamp  = new Timestamp();

        private Ircv3Tag ircv3Tag;

        private String fromType = "";
        private Server fromServer;
        private Nick fromNick;

        private String command;

        private String targetType;
        private Nick targetNick;
        private Channel targetChan;

        private List<String> argv = new ArrayList<>();

        private Builder ircv3Tag(String val) {
            this.ircv3Tag = new Ircv3Tag(val);
            return this;
        }

        /*private Builder ircv3Tag(Ircv3Tag val) {
            this.ircv3Tag = val;
            return this;
        }*/

        private Builder from(Object val) {
            if (val instanceof Server) { this.fromServer = (Server) val; this.fromType = "server"; }
            else if (val instanceof Nick) { this.fromNick = (Nick) val; this.fromType = "nick"; }

            return this;
        }

        private Builder command(String val) {
            this.command = val;
            return this;
        }

        /*private Builder targetType(String val) {
            this.targetType = val;
            return this;
        }*/

        /*private Builder targetNick(Nick val) {
            this.targetNick = val;
            return this;
        }*/

        private Builder argv(List<String> val) {
            this.argv = val;
            return this;
        }

        private IrcMessage build() {
            return new IrcMessage(this);
        }

    }

}
