package xyz.mjav.theqbot;

import static java.util.Map.entry;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import xyz.mjav.theqbot.exceptions.AccountNotFoundException;
import xyz.mjav.theqbot.exceptions.ChanlevChanNoRightException;
import xyz.mjav.theqbot.exceptions.ChanlevModIsZeroException;
import xyz.mjav.theqbot.exceptions.ChannelNotFoundException;
import xyz.mjav.theqbot.exceptions.DataBaseExecException;
import xyz.mjav.theqbot.exceptions.InvalidFormatException;
import xyz.mjav.theqbot.exceptions.ItemErrorException;
import xyz.mjav.theqbot.exceptions.ItemExistsException;
import xyz.mjav.theqbot.exceptions.ItemNotFoundException;
import xyz.mjav.theqbot.exceptions.ItemSuspendedException;
import xyz.mjav.theqbot.exceptions.MaxLimitReachedException;
import xyz.mjav.theqbot.exceptions.NickNotFoundException;
import xyz.mjav.theqbot.exceptions.ParseException;
import xyz.mjav.theqbot.exceptions.UserAuthCredException;
import xyz.mjav.theqbot.exceptions.UserAuthException;
import xyz.mjav.theqbot.exceptions.UserNoAuthException;

public class CService extends Service {

    private interface Whois {
        /**
         * Displays the whois of an user
         * @param whoisUserAccount user account
         */
        void displayW(UserAccount whoisUserAccount, Nick whoisNick);
    }

    private interface ChanlevList {
        void displayCL(Nick fromNick, Channel chanNode, UserAccount userAccount);
    }

    private interface CheckCertFp {
        boolean checkCertFp(String certFp);
    }

    //private interface SuspendHistory {
    //    void displaySH(Nick fromNick, Channel chanNode, UserAccount userAccount);
    //}

    private interface ChanInfo {
        void displayChanInfo(Channel channel);
    }

    private static CService instance;

    private static final Map<String, String> CMD_ALWAYS = Map.ofEntries(
        entry("HELP",                   "HELP.txt"),
        entry("VERSION",                "VERSION.txt")
    );

    private static final Map<String, String> CMD_NOAUTH = Map.ofEntries(
        entry("AUTH",                   "AUTH.txt"),
        entry("HELLO",                  "HELLO.txt")
    );

    private static final Map<String, String> CMD_AUTHED = Map.ofEntries(
        entry("ADDUSER",                "ADDUSER.txt"),
        entry("ADMIN",                  "ADMIN.txt"),
        entry("DEADMIN",                "ADMIN.txt"),
        entry("AUTHHISTORY",            "AUTHHISTORY.txt"),
        entry("AUTOLIMIT",              "AUTOLIMIT.txt"),
        entry("BANCLEAR",               "BEICLEAR.txt"),
        entry("BANDEL",                 "BEIDEL.txt"),
        entry("BANLIST",                "BEILIST.txt"),
        entry("BANTIMER",               "BANTIMER.txt"),
        entry("CERTFP",                 "CERTFP.txt"),
        entry("CERTFPADD",              "CERTFPADD.txt"),
        entry("CERTFPDEL",              "CERTFPADD.txt"),
        entry("CHANFLAGS",              "CHANFLAGS.txt"),
        entry("CHANINFO",               "CHANINFO.txt"),
        entry("CHANLEV",                "CHANLEV.txt"),
        entry("CHANLEVHISTORY",         "CHANLEVHISTORY.txt"),
        entry("CHANMODE",               "CHANMODE.txt"),
        entry("CHANOPHISTORY",          "CHANOPHISTORY.txt"),
        entry("CHANSTAT",               "CHANSTAT.txt"),
        entry("CLEARCHAN",              "CLEARCHAN.txt"),
        entry("CLEARTOPIC",             "CLEARTOPIC.txt"),
        entry("DEADMINALL",             "DEOPALL.txt"),
        entry("DEAUTH",                 "DEAUTH.txt"),
        entry("DEAUTHALL",              "DEAUTH.txt"),
        entry("DEHALFOPALL",            "DEOPALL.txt"),
        entry("DEOPALL",                "DEOPALL.txt"),
        entry("DEOWNERALL",             "DEOPALL.txt"),
        entry("DEVOICEALL",             "DEOPALL.txt"),
        entry("DROPCHAN",               "PERMBEI.txt"),
        entry("DROPUSER",               "PERMBEI.txt"),
        entry("EMAIL",                  "PERMBEI.txt"),
        entry("EXCEPTCLEAR",            "BEICLEAR.txt"),
        entry("EXCEPTDEL",              "BEIDEL.txt"),
        entry("EXCEPTLIST",             "BEILIST.txt"),
        entry("HALFOP",                 "HALFOP.txt"),
        entry("DEHALFOP",               "HALFOP.txt"),
        entry("GIVEOWNER",              "GIVEOWNER.txt"),
        entry("INVITE",                 "INVITE.txt"),
        entry("INVITECLEAR",            "BEICLEAR.txt"),
        entry("INVITEDEL",              "BEIDEL.txt"),
        entry("INVITELIST",             "BEILIST.txt"),
        entry("NEWPASS",                ""),
        entry("OP",                     "OP.txt"),
        entry("DEOP",                   "OP.txt"),
        entry("OWNER/DEOWNER",          "PERMBEI.txt"),
        entry("PERMBAN",                "PERMBEI.txt"),
        entry("PERMEXCEPT",             "PERMBEI.txt"),
        entry("PERMINVITE",             "PERMBEI.txt"),
        entry("RECOVER",                "RECOVER.txt"),
        entry("REMOVEUSER",             "REMOVEUSER.txt"),
        entry("REQUESTBOT",             "REQUESTBOT.txt"),
        entry("REQUESTOWNER",           "REQUESTOWNER.txt"),
        entry("REQUESTPASSWORD",        "REQUESTPASSWORD.txt"),
        entry("RESET",                  "RESET.txt"),
        entry("SETTOPIC",               "SETTOPIC.txt"),
        entry("SHOWCOMMANDS",           "SHOWCOMMANDS.txt"),
        entry("TEMPBAN",                "TEMPBEI.txt"),
        entry("TEMPEXCEPT",             "TEMPBEI.txt"),
        entry("TEMPINVITE",             "TEMPBEI.txt"),
        entry("UNBANALL",               "UNBANALL.txt"),
        entry("UNBANMASK",              "UNBANMASK.txt"),
        entry("UNBANME",                "UNBANME.txt"),
        entry("USERFLAGS",              "USERFLAGS.txt"),
        entry("VOICE",                  "VOICE.txt"),
        entry("DEVOICE",                "VOICE.txt"),
        entry("WELCOME",                "WELCOME.txt"),
        entry("WHOAMI",                 "WHOAMI.txt"),
        entry("WHOIS",                  "WHOIS.txt")
    );

    private static final Map<String, String> CMD_STAFF = Map.ofEntries(
        entry("NETCHANLIST",            "NETLIST.txt"),
        entry("NETSERVERLIST",          "NETLIST.txt"),
        entry("NETUSERLIST",            "NETLIST.txt"),
        entry("ORPHCHANLIST",           "ORPHCHANLIST.txt"),
        entry("REGCHANLIST",            "REGLIST.txt"),
        entry("REGUSERLIST",            "REGLIST.txt")
    );

    private static final Map<String, String> CMD_OPER = Map.ofEntries(
        entry("ADDCHAN",                "ADDCHAN.txt"),
        entry("NICKHISTORY",            "NICKHISTORY.txt"),
        entry("NICKINFO",               "NICKINFO.txt"),
        entry("SERVERINFO",             "SERVERINFO.txt"),
        entry("SUSPENDCHAN",            "SUSPENDCHAN.txt"),
        entry("UNSUSPENDCHAN",          "SUSPENDCHAN.txt"),
        entry("SUSPENDUSER",            "SUSPENDUSER.txt"),
        entry("UNSUSPENDUSER",          "SUSPENDUSER.txt")
    );

    private static final Map<String, String> CMD_ADMIN = Map.ofEntries(
        entry("REJOIN",                 "REJOIN.txt"),
        entry("RENCHAN",                "RENCHAN.txt"),
        entry("SETUSERPASSWORD",        "SETUSERPASSWORD.txt")
    );

    private static final Map<String, String> CMD_DEVGOD = Map.ofEntries(
        entry("DIE",                    "DIE.txt"),
        entry("LISTGHOSTNICKS",         "LISTGHOSTNICKS.txt"),
        entry("RAW",                    "RAW.txt"),
        entry("REJOINSYNC",             "REJOINSYNC.txt"),
        entry("CRASH",                  "CRASH.txt"),
        entry("OBJCOUNTER",             "OBJCOUNTER.txt"),
        entry("SLEEP",                  "SLEEP.txt"),
        entry("TESTEXTBAN",             "TESTEXTBAN.txt"),
        entry("TESTMASK",               "TESTMASK.txt"),
        entry("TESTWILDCARD",           "TESTWILDCARD.txt"),
        entry("TESTWILDCARD2",          "TESTWILDCARD.txt")
    );

    /** List of the commands */
    /*private static final Map<String, String> CMD_LIST;
    static {
        CMD_LIST = new HashMap<>();
        CMD_LIST.putAll(CMD_ALWAYS);
        CMD_LIST.putAll(CMD_NOAUTH);
        CMD_LIST.putAll(CMD_AUTHED);
        CMD_LIST.putAll(CMD_STAFF);
        CMD_LIST.putAll(CMD_OPER);
        CMD_LIST.putAll(CMD_ADMIN);
        CMD_LIST.putAll(CMD_DEVGOD);
    };*/

    /**
     * Class constructor
     * @param protocol reference to the protocol
     * @param database reference to the database
     */
    public CService(Protocol protocol, Database database) {
        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.protocol = protocol;
        this.database = database;
        instance = this;
    }

    public static CService getInstance() {
        return CService.instance;
    }

    public void runCService(Config config, Protocol protocol) {

        Long unixTime;

        String myUniq;

        Map<String, String> myModes = new TreeMap<>();

        this.dispatcher = new Dispatcher(config, database, protocol);
        this.config = config;

        myUniq = config.getServerId() + config.getCServeUniq();
        userChanJoinMode = protocol.getUserChanJoinMode();
        chanJoinModes = config.getCserveChanDefaultModes() + userChanJoinMode;
        unixTime = Instant.now().getEpochSecond();

        /* Converts the modes string to a Map */
        for (Character c: config.getCServeModes().toCharArray()) {
            if (c.equals('+') == true) continue;
            myModes.put(c.toString(), "");
        }

        /*
         * For IPv4 127.0.0.1: echo "0x7f000001" | xxd -r | base64 => fwAAAQ==
         * For IPv6 ::1      : echo "0x00000000000000000000000000000001" | xxd -r | base64 => AAAAAAAAAAAAAAAAAAAAAQ==
         */
        this.myUserNode = new Nick.Builder()
                            .uid(myUniq)
                            .nick(config.getCServeNick())
                            .ident(config.getCServeIdent())
                            .host(config.getCServeHost())
                            .realHost("localhost")
                            .realName(config.getCServeRealName())
                            .modes(myModes)
                            .server(Server.getServerBySid(config.getServerId()))
                            .userTS(unixTime)
                            .ip("AAAAAAAAAAAAAAAAAAAAAQ==")
                            .build();

        this.myServerNode = Server.getServerBySid(config.getServerId());

        this.myServerNode.addLocalUser(this.myUserNode);

        protocol.sendUid(myUserNode);
        protocol.sendSetHost(myUserNode, myUserNode.getHost()); // XXX: maybe a bug there. Vhost is sent through UID but not read by the ircd

        Set<Channel> regChannels = Channel.getRegChanList();
        regChannels.forEach( (regChannelNode) -> {

            /* Making the bot join the registered (and +j) channels */
            if (Flags.isChanJoined(regChannelNode.getcServeFlags()) == true) {
                protocol.chanJoin(myUserNode, regChannelNode);
                performJoinActions(myUserNode, regChannelNode);
            }
        });

        isServiceReady = true;
        this.protocol = protocol;
        protocol.setCService(this);

        /* Starting thread for channel auto limit */
        ChanAutoLimitTask chanAutoLimit = ChanAutoLimitTask.create();
        Thread chanAutoLimitThread = new Thread(chanAutoLimit);
        chanAutoLimitThread.start();
    }

    private void performJoinActions(Nick fromNick, Channel channel) {

        /* 1. join the chan */
        //protocol.chanJoin(fromNick, channel); /* No need because it is managed in the calling method */

        /* 2. set bot modes: Setting the channel modes and bot mode inside the channel */
        //try { protocol.setMode( channel, "+r" + chanJoinModes, fromNick.getNick()); }
        //catch (Exception e) { log.error(String.format("Cannot set mode on %s: %s", channel.getName(), "+r" + chanJoinModes + " " + fromNick.getNick()), e); }
        try { protocol.setMode( channel, String.format("+%s%s", protocol.getChanMode("registered"), chanJoinModes), fromNick.getNick()); }
        catch (Exception e) { log.error(String.format("Cannot set mode on %s: %s", channel.getName(), String.format("+%s%s %s", protocol.getChanMode("registered"), chanJoinModes, fromNick.getNick())), e); }

        /* 3a. apply channel properties: modes */
        String lockedModes = "";
        String lockedParams = "";

        try { lockedModes = channel.getcServeMLockModes().split(" ", 2)[0]; }
        catch (IndexOutOfBoundsException e) { return; }

        try { lockedParams = channel.getcServeMLockModes().split(" ", 2)[1]; }
        catch (IndexOutOfBoundsException e) { }

        if (lockedModes.isEmpty() == false) protocol.setMode(myUserNode, channel, lockedModes, lockedParams);
        protocol.sendMlock(myServerNode, channel, lockedModes.replaceAll("\\+", "").replaceAll("\\-", ""));

        /* 3b. apply channel properties: topic: Set topic when joining the chan (if cflag SAVETOPIC) */
        if (channel.getCServeRegisteredTopic().getText().isEmpty() == false && Flags.isChanTopicSave(channel.getcServeFlags()) == true) {
            String savedTopic = channel.getCServeRegisteredTopic().getText();
            protocol.setTopic(fromNick, channel, savedTopic);
        }

        /* 3c. apply channel properties: BEI lists */ // FIXME: expired list items still will be applied
        channel.getcServeBanList().forEach(    (mask, map) -> { protocol.setMode(fromNick, channel, String.format("+%s", protocol.getChanMode("banned")), mask.getString()); });
        channel.getcServeExceptList().forEach( (mask, map) -> { protocol.setMode(fromNick, channel, String.format("+%s", protocol.getChanMode("except")), mask.getString()); });
        channel.getcServeInviteList().forEach( (mask, map) -> { protocol.setMode(fromNick, channel, String.format("+%s", protocol.getChanMode("invex")), mask.getString()); });

        /* 4. apply channel properties: kick banned users */
        if (Flags.isChanEnforce(channel.getcServeFlags()) == true) {

            Set<Nick> channelUsers = new HashSet<>(channel.getUsers().keySet());

            channelUsers.forEach( (usernode) -> {
                for (Bei bannedUm: channel.getBanList()) {
                    if (bannedUm.matches(usernode) == true) {
                        if (usernode == fromNick) continue;
                        protocol.chanKick(fromNick, channel, usernode, Messages.strAutoBanReason);
                        break;
                    }
                }

                for (Bei bannedUm: channel.getcServeBanList().keySet()) {
                    if (bannedUm.matches(usernode) == true) {
                        if (usernode == fromNick) continue;
                        protocol.setMode(fromNick, channel, String.format("+%s", protocol.getChanMode("banned")), bannedUm.getString());
                        protocol.chanKick(fromNick, channel, usernode, Messages.strAutoBanReason);
                        break;
                    }
                }
            });
        }


        /* 5. apply channel properties: chanlev: Look into every user account belonging to the channel chanlev and applying rights to authed logins of accounts */
        channel.getChanlev().forEach( (username, chanlev) -> {

            UserAccount useraccount;

            try { useraccount = UserAccount.getUserByNameCi(username); }
            catch (Exception e) { log.error(String.format("Inconsistency in channel %s chanlev 0x%08h for account %s", channel.getName(), chanlev, username), e); return; }

            useraccount.getUserLogins().forEach( (usernode) -> { if (usernode.getChanList().containsKey(channel)) this.handleJoin(usernode, channel); });
        });

    }

    public void handleMessage(IrcMessage ircMsg) throws Exception {

        CSCommand csCommand = CSCommand.create(ircMsg);

        switch (csCommand.getCommandName()) {
            case "ADDCHAN":           cServeRequestBot(csCommand, true); break;
            case "ADDUSER":           cServeAddUserChan(csCommand); break;
            case "ADMIN":             cServeGiveChanMode(csCommand, "admin"); break;
            case "AUTH":              cServeAuth(csCommand); break;
            case "AUTHHISTORY":       cServeAuthHistory(csCommand); break;
            case "AUTOLIMIT":         cServeAutoLimit(csCommand); break;

            case "BANCLEAR":          cServeBeiClear(csCommand, Const.CHANBEI_BANS); break;
            case "BANDEL":            cServeBeiDel(csCommand, Const.CHANBEI_BANS); break;
            case "BANLIST":           cServeBeiList(csCommand, Const.CHANBEI_BANS); break;

            case "CERTFP":            cServeCertfp(csCommand); break;
            case "CHANFLAGS":         cServeChanFlags(csCommand); break;
            case "CHANINFO":          cServeChanInfo(csCommand); break;
            case "CHANLEV":           cServeChanlev(csCommand); break;
            case "CHANMODE":          cServeChanModeLockSet(csCommand); break;
            case "CLEARTOPIC":        cServeClearTopic(csCommand); break;
            case "CRASH":             cServeCrash(csCommand); break;

            case "DEADMIN":           cServeGiveChanMode(csCommand, "deadmin"); break;
            case "DEADMINALL":        cServeDeModeAll(csCommand, "deadmin"); break;
            case "DEAUTH":            cServeDeAuth(csCommand); break;
            case "DEAUTHALL":         cServeDeAuthAll(csCommand); break;
            case "DEHALFOP":          cServeGiveChanMode(csCommand, "dehalfop"); break;
            case "DEHALFOPALL":       cServeDeModeAll(csCommand, "dehalfop"); break;
            case "DEOP":              cServeGiveChanMode(csCommand, "deop"); break;
            case "DEOPALL":           cServeDeModeAll(csCommand, "deop"); break;
            case "DEOWNER":           cServeGiveChanMode(csCommand, "deowner"); break;
            case "DEOWNERALL":        cServeDeModeAll(csCommand, "deowner"); break;
            case "DEVOICE":           cServeGiveChanMode(csCommand, "devoice"); break;
            case "DEVOICEALL":        cServeDeModeAll(csCommand, "devoice"); break;
            case "DIE":               cServeDie(csCommand); break;
            case "DROPCHAN":          cServeDropChan(csCommand); break;
            case "DROPUSER":          cServeDropUser(csCommand); break;

            case "EXCEPTCLEAR":       cServeBeiClear(csCommand, Const.CHANBEI_EXCEPTS); break;
            case "EXCEPTDEL":         cServeBeiDel(csCommand, Const.CHANBEI_EXCEPTS); break;
            case "EXCEPTLIST":        cServeBeiList(csCommand, Const.CHANBEI_EXCEPTS); break;

            case "HALFOP":            cServeGiveChanMode(csCommand, "halfop"); break;
            case "HELLO":             cServeHello(csCommand); break;
            case "HELP":              cServeHelp(csCommand); break;

            case "INVITECLEAR":       cServeBeiClear(csCommand, Const.CHANBEI_INVITES); break;
            case "INVITEDEL":         cServeBeiDel(csCommand, Const.CHANBEI_INVITES); break;
            case "INVITELIST":        cServeBeiList(csCommand, Const.CHANBEI_INVITES); break;

            case "LIVECHANLIST":      cServeLiveChanlist(csCommand); break;
            case "LIVESERVERLIST":    cServeLiveServerlist(csCommand); break;
            case "LIVEUSERLIST":      cServeLiveUserlist(csCommand); break;
            case "NEWPASS":           cServeNewPass(csCommand); break;
            case "NICKALIAS":         cServeNickAlias(csCommand); break;
            case "NICKHISTORY":       cServeNickHistory(csCommand); break;
            case "NICKINFO":          cServeNickInfo(csCommand); break;

            case "OP":                cServeGiveChanMode(csCommand, "op"); break;
            case "OWNER":             cServeGiveChanMode(csCommand, "owner"); break;

            case "PERMBAN":           cServePermBei(csCommand, Const.CHANBEI_BANS); break;
            case "PERMEXCEPT":        cServePermBei(csCommand, Const.CHANBEI_EXCEPTS); break;
            case "PERMINVITE":        cServePermBei(csCommand, Const.CHANBEI_INVITES); break;

            case "RAW":               cServeRawCmd(csCommand); break;
            case "REJOIN":            cServeRejoin(csCommand); break;
            case "RENCHAN":           cServeRenchan(csCommand); break;
            case "REGUSERLIST":       cServeRegUserlist(csCommand); break;
            case "REGCHANLIST":       cServeRegChanlist(csCommand); break;
            case "REQUESTBOT":        cServeRequestBot(csCommand, false); break;

            case "SERVERINFO":        cServeServerInfo(csCommand); break;
            case "SETTOPIC":          cServeSetTopic(csCommand); break;
            case "SETUSERPASSWORD":   cServeSetUserPass(csCommand); break;
            case "SHOWCOMMANDS":      cServeShowcommands(csCommand); break;
            case "SUSPENDCHAN":       cServeSuspendChan(csCommand); break;
            case "SUSPENDHISTORY":    cServeSuspendHistory(csCommand); break;
            case "SUSPENDUSER":       cServeSuspendUser(csCommand); break;

            case "TEMPBAN":           cServeTempBei(csCommand, Const.CHANBEI_BANS); break;
            case "TEMPEXCEPT":        cServeTempBei(csCommand, Const.CHANBEI_EXCEPTS); break;
            case "TEMPINVITE":        cServeTempBei(csCommand, Const.CHANBEI_INVITES); break;

            case "USERFLAGS":         cServeUserflags(csCommand); break;
            case "UNSUSPENDCHAN":     cServeUnSuspendChan(csCommand); break;
            case "UNSUSPENDUSER":     cServeUnSuspendUser(csCommand); break;

            case "VERSION":           cServeVersion(csCommand); break;
            case "VOICE":             cServeGiveChanMode(csCommand, "voice"); break;

            case "WELCOME":           cServeWelcome(csCommand); break;
            case "WHOAMI":            cServeWhois(csCommand, true); break; /* Pretends to be doing "WHOIS <itself>" */
            case "WHOIS":             cServeWhois(csCommand, false); break;

            /* Commands to implement */
            case "CONFIG":            sendReply(csCommand.getFromNick(), "Placeholder."); break;
            case "SETUSERFLAGS":      sendReply(csCommand.getFromNick(), "Placeholder."); break;
            case "STATUS":            sendReply(csCommand.getFromNick(), "Placeholder."); break;


            /* Debug commands */
            case "FLUSHCACHECLASS":   cServeFlushCacheClass(csCommand); break;
            case "REJOINSYNC":        cServePerformJoin(csCommand); break;
            case "SLEEP":             cServeSleep(csCommand); break;
            case "TESTMASK":          cServeTestMask(csCommand); break;
            case "TESTEXTBAN":        cServeTestExtban(csCommand); break;
            case "TESTWILDCARD":      cServeTestWildcard(csCommand); break;
            case "TESTWILDCARD2":     cServeTestWildcard2(csCommand); break;

            default:                  sendReply(csCommand.getFromNick(), Messages.strErrCommandUnknown); break;
        }

    }

    /**
     * Triggers the activities to perform when an user joins a channel
     * @param user user node joining channel
     * @param channel channel node joined
     */
    public void handleJoin(Nick user, Channel channel, Boolean hasDispWelcome) {
        if (Flags.isChanJoined(channel.getcServeFlags()) == false) {
            /* Channel does not have +j flag (could be suspended or something), in this case there is nothing to do */
            return;
        }

        String autoBanReason  = Messages.strAutoBanReason;
        String joinMode       = "";

        Integer userChanlev = 0;

        /* Checks if user is authed */
        if (user.isAuthed() == true) {
            if (user.getAccount().getChanlev().containsKey(channel.getName())) {

                userChanlev = user.getAccount().getChanlev(channel);

                if (  Flags.isChanLBanned( userChanlev) == true ) {
                    protocol.setMode( myUserNode, channel, String.format("+%s", protocol.getChanMode("banned")), String.format("~account:%s", user.getAccount().getName()));
                    protocol.chanKick(myUserNode, channel, user, autoBanReason);
                }

                else if (Flags.isChanLAuto(userChanlev)) { /* Sets the auto channel modes */

                    if (Flags.isChanLOwner(userChanlev)       && protocol.hasFeature("chanOwner")  == true) joinMode = String.format("+%s", protocol.getChanMode("owner"));
                    else if (Flags.isChanLMaster(userChanlev) && protocol.hasFeature("chanAdmin")  == true) joinMode = String.format("+%s", protocol.getChanMode("admin"));
                    else if (Flags.isChanLOp(userChanlev)     && protocol.hasFeature("chanOp")     == true) joinMode = String.format("+%s", protocol.getChanMode("op"));
                    else if (Flags.isChanLHalfOp(userChanlev) && protocol.hasFeature("chanHalfop") == true) joinMode = String.format("+%s", protocol.getChanMode("hafop"));
                    else if (Flags.isChanLVoice(userChanlev)  && protocol.hasFeature("chanVoice")  == true) joinMode = String.format("+%s", protocol.getChanMode("voice"));

                    try { protocol.setMode( myUserNode, channel, joinMode, user.getNick()); }
                    catch (Exception e) { log.error(String.format("CService/handleJoin: error while %s nick %s on %s: ", joinMode, user.getNick(), channel.getName()), e); }
                }
            }
        }

        if (Flags.isChanEnforce(channel.getcServeFlags()) == true) {
            for (Bei bannedUm: channel.getBanList()) {
                if (bannedUm.matches(user) == true) {
                    if (user == myUserNode) continue;
                    protocol.setMode(myUserNode, channel, String.format("+%s", protocol.getChanMode("banned")), bannedUm.getString());
                    protocol.chanKick(myUserNode, channel, user, Messages.strAutoBanReason);
                    break;
                }
            }

            for (Bei bannedUm: channel.getcServeBanList().keySet()) {
                if (bannedUm.matches(user) == true) {
                    if (user == myUserNode) continue;
                    protocol.setMode(myUserNode, channel, String.format("+%s", protocol.getChanMode("banned")), bannedUm.getString());
                    protocol.chanKick(myUserNode, channel, user, Messages.strAutoBanReason);
                    break;
                }
            }
        }

        if (Flags.isChanWelcome(channel.getcServeFlags()) == true && hasDispWelcome == true) {
            if (user.isAuthed() == false || ( user.isAuthed() == true && Flags.isUserWelcome(user.getAccount().getFlags()) == false && Flags.isChanLHideWelcome(user.getAccount().getChanlev(channel)) == false) ) {
                String welcomeMsg = "";
                try { welcomeMsg = database.getWelcomeMsg(channel); }
                catch (Exception e) { log.error(String.format("CService/handleJoin: error fetching welcome message for %s: ", channel), e); }

                if (welcomeMsg == null) welcomeMsg = "";
                if (welcomeMsg.isEmpty() == false) sendReply(user, welcomeMsg);
            }
        }
    }

    public void handleJoin(Nick user, Channel channel) {
        handleJoin(user, channel, true);
    }

    public void handleTopic(Channel chanNode) {
        //String savedTopic = "";

        if (Flags.isChanJoined(chanNode.getcServeFlags()) == false) {
            /* Channel does not have +j flag (could be suspended or something) => do nothing */
            return;
        }

        /*
        try {
            savedTopic = database.getTopic(chanNode);
        }

        catch (Exception e) { log.error(String.format("CService/handleTopic: error while fetching topic for %s: ", chanNode.getName()), e); }

        if (Flags.isChanForceTopic(chanNode.getFlags()) == true) protocol.setTopic(myUserNode, chanNode, savedTopic);
        */
    }

    /**
     *
     * @param fromNick requester user node
     * @param nick requested nick or account
     * @param str command string
     */
    private void cServeWhois(CSCommand csCommand, boolean self) {

        /* Syntax: WHOIS <nick|#username> */

        String nick;

        Nick user;
        Nick fromNick = csCommand.getFromNick();

        UserAccount userAccount;

        Whois whois = (whoisUserAccount, whoisNick) -> {

            String accountCreationTS;
            String accountLastAuthTS;
            String accountLastSuspendTS = "never";
            String strSuspended = Messages.strNo;

            Date dateRegTS;
            Date dateAuthTS;
            Date dateSuspendTS;

            Map<String, Integer> chanlevSorted;

            Set<String> strReply = new LinkedHashSet<>();

            var wrapper = new Object(){
                String loginList       = "";
                String flagsShortList  = "";
                String flagsLongList   = "";
            };

            /* Fetches the user status (staff, oper) */
            if ( Flags.hasUserOperPriv(whoisUserAccount.getFlags()) == true)        strReply.add(String.format(Messages.strWhoisContentUserLevel, config.getNetworkName(), Messages.strWhoisContentUserLevIrcop));
            else if ( Flags.hasUserStaffPriv(whoisUserAccount.getFlags()) == true)  strReply.add(String.format(Messages.strWhoisContentUserLevel, config.getNetworkName(), Messages.strWhoisContentUserLevStaff));

            /* Registration date */
            dateRegTS = new Date((whoisUserAccount.getRegistrationTS().getValue())*1000L);
            accountCreationTS = jdf.format(dateRegTS);

            strReply.add(String.format(Messages.strWhoisContentUserCreated, accountCreationTS));

            /* Last auth date */
            dateAuthTS = new Date((whoisUserAccount.getAuthLastTS().getValue()*1000L));
            accountLastAuthTS = jdf.format(dateAuthTS);
            strReply.add(String.format(Messages.strWhoisContentUserLastAuth, accountLastAuthTS));


            /* User logins */
            /* Fetch the list of users logged with the account */
            whoisUserAccount.getUserLogins().forEach( (userNode) -> { wrapper.loginList += userNode.getNick() + " "; });

            /* If there are no logins, we display something like "(none)" */
            if (wrapper.loginList.isEmpty() == true) wrapper.loginList = Messages.strMsgNone;

            strReply.add(String.format(Messages.strWhoisContentUserLinkedNicks, wrapper.loginList));



            /* Information only accessible to Staff or more */
            if ( (Flags.hasUserStaffPriv(fromNick.getAccount().getFlags()) == true) ) {

                /* User ID */
                strReply.add(String.format(Messages.strWhoisContentUserId, whoisUserAccount.getId())); /* "User ID" */

                /* User suspensions */
                if (whoisUserAccount.getSuspendLastTS().getValue() > 0L) {
                    dateSuspendTS        = new Date(whoisUserAccount.getSuspendLastTS().getValue()*1000L);
                    accountLastSuspendTS = jdf.format(dateSuspendTS);
                }

                if (Flags.isUserSuspended(whoisUserAccount.getFlags()) == true) strSuspended = Messages.strYes;

                strReply.add(String.format(Messages.strWhoisContentUserSuspended, strSuspended, accountLastSuspendTS, whoisUserAccount.getSuspendCount(), whoisUserAccount.getSuspendMessage()));

            }

            /* Information only accessible to Staff or more, and user itself */
            if ( (Flags.hasUserStaffPriv(fromNick.getAccount().getFlags()) == true) || (fromNick.getAccount() == whoisUserAccount) ) {

                /* User flags */
                if (whoisUserAccount.getFlags() != 0) {
                    wrapper.flagsShortList = "+" + Flags.flagsIntToChars("userflags", whoisUserAccount.getFlags());
                    wrapper.flagsLongList =        Flags.flagsIntToString("userflags", whoisUserAccount.getFlags());
                }
                else { wrapper.flagsShortList = Messages.strMsgNone; wrapper.flagsLongList = Messages.strMsgNone; }

                strReply.add(String.format(Messages.strWhoisContentUserFlags, wrapper.flagsShortList, wrapper.flagsLongList));

                /* SASL auth type */
                if (whoisNick != null) {
                    String saslAuthed   = Messages.strNo;
                    String saslAuthType = "";
                    if (whoisNick.isAuthBySasl() == true) {
                        saslAuthed = Messages.strYes;
                        saslAuthType = String.format("(%s)", saslAuthType = whoisNick.getSaslAuthParam("authType"));
                    }
                    strReply.add(String.format(Messages.strWhoisContentUserAuthSasl, saslAuthed, saslAuthType));
                }

                /* Email address */
                strReply.add(String.format(Messages.strWhoisContentUserEmail, whoisUserAccount.getEmail()));

                /* Email last set date */
                strReply.add(String.format(Messages.strWhoisContentUserEmailLast, ""));

                /* Password last set date */
                strReply.add(String.format(Messages.strWhoisContentUserPassLast, ""));

                /* Aliases list */
                if (whoisUserAccount.getNickAlias().isEmpty() == false) {
                    strReply.add(String.format(Messages.strWhoisContentUserNickAliases, whoisUserAccount.getNickAlias().size()));
                    whoisUserAccount.getNickAlias().forEach( (nickL, alias) -> strReply.add(String.format(Messages.strWhoisContentSublistItem, nickL)) );
                }

                /* Certfp list */
                if (whoisUserAccount.getCertFP().isEmpty() == false) {
                    strReply.add(String.format(Messages.strWhoisContentUserCertFpTitle, whoisUserAccount.getCertFP().size()));
                    var wrapperCertfp = new Object(){ Integer lineCounter=1;};
                    whoisUserAccount.getCertFP().forEach( (certfp) -> {
                        if (certfp.isEmpty() == false) {
                            strReply.add(String.format(Messages.strWhoisContentSublistItem, certfp));
                            wrapperCertfp.lineCounter++;
                        }
                    } );
                }

                /* Chanlev list */
                strReply.add(Messages.strWhoisContentUserChanlevTitle);
                strReply.add(String.format(Messages.strWhoisContentUserChanlevHeadFormat, Messages.strWhoisContentUserChanlevCol1, Messages.strWhoisContentUserChanlevCol2));

                chanlevSorted = new TreeMap<>(whoisUserAccount.getChanlev());
                var wrapperCL = new Object() { Integer chanlev; };
                chanlevSorted.forEach( (chan, chanlev) -> {
                    wrapperCL.chanlev = chanlev;
                    if (wrapperCL.chanlev != 0) {
                        strReply.add(String.format(Messages.strWhoisContentUserChanlevRowFormat, chan, Flags.flagsIntToChars("chanlev", wrapperCL.chanlev) + " " + "[" + Flags.flagsIntToString("chanlev", wrapperCL.chanlev) + "]"));
                    }
                } );

            }

            for (String strLine: strReply) {
                sendReply(fromNick, strLine);
            }
        };

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        if (self == false) {
            try { nick = csCommand.getArgs().get(0); }
            catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }
        }
        else {
            nick = csCommand.getFromNick().getNick();
        }

        /* Looks up user in database */
        if (nick.startsWith(Const.USER_ACCOUNT_PREFIX) == true) {

            try { userAccount = UserAccount.getUserByNameCi(nick.replaceFirst(Const.USER_ACCOUNT_PREFIX,"")); }
            catch (ItemNotFoundException e) { sendReply(fromNick, Messages.strErrUserNotFound); return; }

            sendReply(fromNick, String.format(Messages.strWhoisHeaderAccount, userAccount.getName()));
            whois.displayW(userAccount, null);
            sendReply(fromNick, Messages.strEndOfList);
        }
        else {
            try { user = Nick.getUserByNickCi(nick); }
            catch (NickNotFoundException e) { sendReply(fromNick, Messages.strErrNickNotFound); return; }

            try { userAccount = user.getAccount(); }
            catch (UserNoAuthException e) { sendReply(fromNick, Messages.strErrNickNotAuthed); return; }

            sendReply(fromNick, String.format(Messages.strWhoisHeaderNick, user.getNick(), userAccount.getName()));
            whois.displayW(userAccount, user);
            sendReply(fromNick, Messages.strEndOfList);

        }
    }

    private void cServeTestMask(CSCommand csCommand) {

        /* TESTMASK <usermask> */

        String mask;

        UserMask usermask;
        Nick fromNick = csCommand.getFromNick();

        try { mask = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { usermask = UserMask.create(mask); }
        catch (Exception e) { sendReply(fromNick, String.format("error: mask %s not valid %s", mask, e.toString())); return; }

        sendReply(fromNick, String.format("mask: mask %s = %s", mask, usermask.getFullMask()));

    }

    private void cServeFlushCacheClass(CSCommand csCommand) {

        /* FLUSHCACHECLASS */
        Nick fromNick = csCommand.getFromNick();

        if (csCommand.isFromNickAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        if (Flags.hasUserDevGodPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, String.format(Messages.strErrCommandUnknown)); return; }

        Cache.flush();

        sendReply(fromNick, Messages.strSuccess);

    }

    private void cServeNickInfo(CSCommand csCommand) {

        /* Syntax: NICKINFO <nick> */

        String userTSdate;

        StringJoiner allUserMasks = new StringJoiner(" :: ");

        Date date;

        Set<String> response = new LinkedHashSet<>();

        Nick user;
        Nick fromNick = csCommand.getFromNick();

        String secureConn = Messages.strNo;

        var w = new Object() {
            String bufferMode = "";
            String bModeLongTmp = "";
            String bufferParam = "";
            StringBuilder mode  = new StringBuilder();
            StringBuilder param = new StringBuilder();
            Set<String> bufferModeList;
            StringJoiner bufferModeLong = new StringJoiner(" ");
        };

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { user = Nick.getUserByNickCi(csCommand.getArgs().get(0)); }
        catch (NickNotFoundException e) {
            sendReply(fromNick, Messages.strErrNickNotFound);
            return;
        }

        date  = new Date((user.getUserTS())*1000L);
        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        userTSdate = jdf.format(date);

        response.add(String.format(Messages.strNickInfoContentNickSummary, user.getNick()));
        response.add(String.format(Messages.strNickInfoContentUid, user.getUid()));
        response.add(String.format(Messages.strNickInfoContentIdent, user.getIdent()));
        response.add(String.format(Messages.strNickInfoContentRealname, user.getRealName()));
        response.add(String.format(Messages.strNickInfoContentHostname, user.getHost()));


        w.bufferMode  = "";
        user.getUserModes().forEach( (mode,param ) -> {
            w.bufferMode   = String.join("", w.bufferMode, mode);
            if (param.isEmpty() == false) w.bufferParam  = String.join(" ", w.bufferParam, param);

            w.bModeLongTmp = protocol.userModeToTxt(mode);
            if (param.isEmpty() == false) w.bModeLongTmp  = String.join("=", w.bModeLongTmp, param);

            w.bufferModeLong.add(w.bModeLongTmp);


        });

        for (UserMask u: user.getAllUserMasks()) allUserMasks.add(u.getString());

        /* Realhost + IP address */
        response.add(String.format(Messages.strNickInfoContentConnectFrom, user.getRealHost(), user.getIpAddressAsString()));

        /* Cloaked host */
        if (user.getCloakedHost().isEmpty() == false) response.add(String.format(Messages.strNickInfoContentCloakedHost, user.getCloakedHost()));

        /* All user masks */
        response.add(String.format(Messages.strNickInfoContentUserMasks, allUserMasks));

        /* User Modes */
        response.add(String.format(Messages.strNickInfoContentUserModes, w.bufferMode));
        response.add(String.format(Messages.strNickInfoContentUserModesLong, w.bufferModeLong));

        /* Server */
        response.add(String.format(Messages.strNickInfoContentServerName, user.getServer(), user.getServer().getSid()));

        /* Secure connection */
        if (user.isConnPlainText() == true) secureConn = Messages.strYes;
        response.add(String.format(Messages.strNickInfoContentSecureConnection, secureConn));

        /* Timestamp */
        response.add(String.format(Messages.strNickInfoContentSignOnTS, userTSdate));

        /* User account */
        if (user.isAuthed() == true) response.add(String.format(Messages.strNickInfoContentAuthAccount, user.getAccount()));

        /* Oper login/class if the user is oper */
        if (user != null && user.isOper() == true) response.add(String.format(Messages.strNickInfoContentUserOperLogin, user.getOperLogin(), user.getOperClass()));

        /* CertFP */
        if (user != null && user.getCertFP().isEmpty() == false) response.add(String.format(Messages.strNickInfoContentUserCertFP, user.getCertFP()));

        /* Country */
        if (user != null && user.getCountry().isEmpty() == false) response.add(String.format(Messages.strNickInfoContentUserCountry, user.getCountry("name"), user.getCountry("code")));

        /* Channel list */
        response.add(Messages.strNickInfoContentChanListTitle);
        user.getChanList().forEach( (chan, mode) -> {
            w.mode = new StringBuilder();
            w.param = new StringBuilder();

            w.bufferModeList = chan.getUserModes(user);
            w.bufferModeList.forEach( (userchanmode) -> {
                if (userchanmode.isEmpty() == false) w.mode.append(userchanmode);
            });

            response.add(String.format(Messages.strNickInfoContentChanListLine, chan, w.mode, w.param));

        });


        response.add(Messages.strEndOfList);

        for(String line: response) sendReply(fromNick, line);

    }

    private void cServeServerInfo(CSCommand csCommand) {

        /* Syntax: SERVERINFO <server> */

        String serverName;
        String serverTSdate;

        Date date;

        Set<String> response = new LinkedHashSet<>();

        Server server;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }


        try { serverName = csCommand.getArgs().get(0); }
        catch (ItemNotFoundException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { server = Server.getServerByNameCi(serverName); }
        catch (ItemNotFoundException e) { sendReply(fromNick, Messages.strErrServerNotFound); return; }

        date  = new Date((server.getTS().getValue())*1000L);
        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        serverTSdate = jdf.format(date);

        response.add(String.format(Messages.strServerInfoHeader, server.getName()));
        response.add(String.format(Messages.strServerInfoContentSid, server.getSid()));
        response.add(String.format(Messages.strServerInfoContentName, server.getName()));
        response.add(String.format(Messages.strServerInfoContentDescription, server.getDescription()));
        response.add(String.format(Messages.strServerInfoContentCertfp, server.getCertFP()));
        response.add(String.format(Messages.strServerInfoContentTimestamp, serverTSdate));
        response.add(String.format(Messages.strServerInfoContentUserCount, server.getLocalUsers().size()));
        response.add(String.format(Messages.strServerInfoContentParent, server.getParent().getName(), server.getParent().getSid()));
        response.add(String.format(Messages.strServerInfoContentChild, server.getChildren().size()));

        for (Server s: server.getChildren()) response.add(String.format(Messages.strServerInfoContentSublistItem, s.getSid(), s.getName()));

        response.add(Messages.strEndOfList);

        for(String line: response) sendReply(fromNick, line);

    }

    /**
     * Handles the setting of chanlev
     * @param fromNick requester user node
     * @param str command string
     */
    private void cServeChanlev(CSCommand csCommand) {

        /* Syntax: CHANLEV <#channel> [<nick|#username> [<flags>]] */

        UserAccount userAccountTarget;

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        String userAccountStr            = "";
        String userNick                  = "";
        String chanlevModRaw             = "";
        String channel                   = "";

        /* Lambda function to display chanlev */
        ChanlevList displayCL = (fromN, channode, useraccount) -> {

            var wrapper = new Object() { Integer chanlev; };

            /* User has significant chanlev on chan or user has staff userflag */
            if (  Flags.hasChanLSignificant(fromN.getAccount().getChanlev(channode)) == true || Flags.hasUserStaffPriv(fromN.getAccount().getFlags()) == true  ) {

                sendReply(fromN, String.format(Messages.strChanlevListHeadFormat, Messages.strChanlevListCol1, Messages.strChanlevListCol2));

                channode.getChanlev().forEach( (user, chanlev) -> {

                    /*
                     * We should display:
                     *  o all flags if requester has Staff priv
                     *  o public + punishment flags if requester has chan master priv
                     *  o public + personal when requester's line comes
                     */

                    wrapper.chanlev = chanlev;

                    /* Stripping personal flags if the line is not about the requester account and has not staff privilege */
                    if ( fromN.getAccount().getName().equals(user) == false && Flags.hasUserStaffPriv(fromN.getAccount().getFlags()) == false) {
                        wrapper.chanlev = Flags.stripChanlevPersonalFlags(wrapper.chanlev);
                    }

                    /* Stripping punishment flags if the requester has not chan master privilege */
                    if ( Flags.hasChanLMasterPriv(fromN.getAccount().getChanlev(channode)) == false && Flags.hasUserStaffPriv(fromN.getAccount().getFlags()) == false) {
                        wrapper.chanlev = Flags.stripChanlevPunishFlags(wrapper.chanlev);
                    }


                    if ( wrapper.chanlev != 0 && ((useraccount != null && user.equals(useraccount.getName())) || useraccount == null)) {
                        sendReply(fromN, String.format(Messages.strChanlevListRowFormat, user, Flags.flagsIntToChars("chanlev", wrapper.chanlev) + " " + "[" + Flags.flagsIntToString("chanlev", wrapper.chanlev) + "]"));
                    }
                });

                sendReply(fromN, Messages.strEndOfList);
            }

            /* No access to chanlev */
            else { sendReply(fromN, String.format(Messages.strErrNoAccess)); return; }

        };

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); csCommand.setResult(Const.CMD_ERRNO_FAILED); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); csCommand.setResult(Const.CMD_ERRNO_SYNTAX); return; }

        try {
            //channel = protocol.getChannelNodeByNameCi(channel).getName();
            //chanNode = protocol.getChannelNodeByNameCi(channel);
            chanNode = Channel.getRegChanByNameCi(channel);
            //if (Channel.isChan(channel) == false) { throw new Exception(); }
        }
        catch (ItemNotFoundException e) { sendReply(fromNick, String.format(Messages.strErrChanSusOrNotFound, channel)); csCommand.setResult(Const.CMD_ERRNO_DENIED); return; }
        catch (Exception e) { sendReply(fromNick, String.format(Messages.strErrChanSusOrNotFound, channel)); csCommand.setResult(Const.CMD_ERRNO_DENIED); return; }

        if (chanNode.isRegistered() == false) { sendReply(fromNick, Messages.strErrChanNonReg); csCommand.setResult(Const.CMD_ERRNO_DENIED); return; }

        try {
            userNick = csCommand.getArgs().get(1);

            if (userNick.startsWith(Const.USER_ACCOUNT_PREFIX)) { // direct access to account
                userAccountStr = userNick.replaceFirst(Const.USER_ACCOUNT_PREFIX, "").toLowerCase();
                userAccountTarget = UserAccount.getUserByNameCi(userAccountStr);
            }
            else { /* indirect access to account => need to lookup account name */
                Nick usernode;

                try { usernode = Nick.getUserByNickCi(userNick); }
                catch (ItemNotFoundException e) { sendReply(fromNick, Messages.strErrNickNotFound); csCommand.setResult(Const.CMD_ERRNO_DENIED); return; }

                if (usernode.isAuthed() == true) userAccountTarget = usernode.getAccount();
                else { sendReply(fromNick, Messages.strErrNickNotAuthed); csCommand.setResult(Const.CMD_ERRNO_DENIED); return; }
            }

        }
        catch (IndexOutOfBoundsException e) {
            /* Display current chanlev */
            displayCL.displayCL(fromNick, chanNode, null);
            return;
        }
        catch (Exception f) {
            f.printStackTrace();
            if (userNick.startsWith(Const.USER_ACCOUNT_PREFIX)) sendReply(fromNick, Messages.strErrUserNonReg);
            else {
                Nick nick;
                try { nick = Nick.getUserByNickCi(userNick); }
                catch (NickNotFoundException e) { sendReply(fromNick, Messages.strErrNickNotFound); return; }

                if (nick.isAuthed() == false) sendReply(fromNick, Messages.strErrNickNotAuthed);
            }
            return;
        }

        try {  chanlevModRaw =  csCommand.getArgs().get(2); }
        catch (IndexOutOfBoundsException e) { displayCL.displayCL(fromNick, chanNode, userAccountTarget); return; }

        setChanLev(fromNick, userAccountTarget, chanNode, chanlevModRaw);
        sendReply(fromNick, Messages.strSuccess);
    }

    /**
     * Sets the chanlev of the named user to the named chan
     * @param fromNick source nick
     * @param targetAccount target account
     * @param channel target channel
     * @param flags flags to apply
     */
    private void setChanLev(Nick fromNick, UserAccount targetAccount, Channel channel, String flags) {

        Channel chanNode = channel;

        UserAccount userAccountRequester;
        UserAccount userAccountTarget = targetAccount;

        String chanlevModRaw = flags;

        Map<String, Integer> modResult;

        int userCurChanlevInt  = 0;
        int userNewChanlevInt  = 0;

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        userAccountRequester = fromNick.getAccount();

        if (Flags.isChanSuspended(chanNode.getcServeFlags()) == true) { sendReply(fromNick, Messages.strErrChanSuspended); return; }

        try { modResult = parseChanlevModRequest(chanNode, userAccountRequester, userAccountTarget, chanlevModRaw); }
        catch (ChanlevChanNoRightException e) { sendReply(fromNick, Messages.strChanlevErrNoMod); return; }
        catch (ChanlevModIsZeroException e)   { sendReply(fromNick, Messages.strChanlevErrNoMod);  return; }

        try {

            var wrapper = new Object() { Integer chanlev;};

            userCurChanlevInt = database.getUserChanlev(userAccountTarget, chanNode);
            userNewChanlevInt = Flags.applyFlagsFromInt("chanlev", userCurChanlevInt, modResult);

            dispatcher.setChanlev(chanNode, userAccountTarget, userNewChanlevInt);

            wrapper.chanlev = userNewChanlevInt;
            /* Stripping personal flags if the line is not the requester account and has not staff privilege */
            if ( fromNick.getAccount() != userAccountTarget && Flags.hasUserStaffPriv(fromNick.getAccount().getFlags()) == false) { wrapper.chanlev = Flags.stripChanlevPersonalFlags(wrapper.chanlev); }

            /* Stripping punishment flags if the requester has not chan master privilege */
            if ( Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(chanNode)) == false && Flags.hasUserStaffPriv(fromNick.getAccount().getFlags()) == false) { wrapper.chanlev = Flags.stripChanlevPunishFlags(wrapper.chanlev); }

            sendReply(fromNick, String.format(Messages.strChanlevSuccessSummary, userAccountTarget.getName(), chanNode.getName(), Flags.flagsIntToChars("chanlev", wrapper.chanlev)));

            userAccountTarget.getUserLogins().forEach( (usernode) -> {
                if (usernode.getChanList().containsKey(chanNode)) this.handleJoin(usernode, chanNode);
            });
        }
        catch (Exception e) {
            log.error(String.format("CService/cServeChanlev: error whith chanlev %s for account/chan %s / %s: ", chanlevModRaw, userAccountTarget.getName(), chanNode.getName()), e);
            sendReply(fromNick, Messages.strChanlevErrUnknown);
            return;
        }

        if (chanNode.getChanlevWoutPersonalFlags() == null || chanNode.getChanlevWoutPersonalFlags().isEmpty() == true) {
            try {
                dispatcher.dropChan(chanNode, fromNick);
                protocol.setMode( chanNode, "-r", "");
                protocol.chanPart(myUserNode, chanNode);
                sendReply(fromNick, Messages.strChanlevDropChanLEmpty);
            }
            catch (Exception e) { log.error(String.format("CService/cServeChanlev: error dropping channel %s after chanlev left empty: ", chanNode.getName()), e); }
        }
    }

    private void cServeAddUserChan(CSCommand csCommand) {

        /* Syntax ADDUSER <#channel> <flags> <user1 user2 ... userN> */

        List<String> nickList;

        String flags = "";
        String channelName;
        String accountName;

        Channel channel;

        UserAccount userAccountTarget;

        Nick userNode;
        Nick fromNick;

        fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) {
            sendReply(fromNick, Messages.strErrCommandUnknown);
            return;
        }

        try { channelName = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) {
            sendReply(fromNick, Messages.strErrCommandSyntax);
            return;
        }

        try { flags = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) {
            sendReply(fromNick, Messages.strErrCommandSyntax);
            return;
        }

        try {
            channelName = protocol.getChannelNodeByNameCi(channelName).getName();
            channel = protocol.getChannelNodeByNameCi(channelName);
            if (Channel.getRegChanByNameCi(channelName) == null) { throw new Exception(); }
        }
        catch (Exception e) {
            sendReply(fromNick, Messages.strErrChanSusOrNotFound);
            return;
        }

        try {
            nickList = csCommand.getArgs().subList(2, csCommand.getArgs().size());
        }
        catch (Exception e) {
            /* User did not provide a nick list => applying on the user */
            nickList = new ArrayList<>();
            nickList.add(csCommand.getFromNick().getNick());
        }

        for (String nick: nickList) {

            if (nick.startsWith(Const.USER_ACCOUNT_PREFIX) == true) {
                accountName = nick.replaceFirst(Const.USER_ACCOUNT_PREFIX, "");
                try {
                    log.debug(String.format("CService::cServeAddUserChan: Looking up user account %s", accountName));
                    userAccountTarget = UserAccount.getUserByNameCi(accountName);
                }
                catch (ItemNotFoundException e) { log.debug(String.format("CService::cServeAddUserChan: Account not found, skipping")); continue; }
            }

            else {
                try {
                    log.debug(String.format("CService::cServeAddUserChan: Looking up nick %s", nick));
                    userNode = Nick.getUserByNickCi(nick);
                }
                catch (NickNotFoundException e) { log.debug(String.format("CService::cServeAddUserChan: Nick %s not found, skipping", nick)); continue; }

                try {
                    log.debug(String.format("CService::cServeAddUserChan: Looking up user account for nick %s", nick));
                    userAccountTarget = userNode.getAccount();
                }
                catch (ItemNotFoundException e) { log.debug(String.format("CService::cServeAddUserChan: Account not found (nick not authed)")); continue; }

                log.debug(String.format("CService::cServeAddUserChan: Account %s for authed nick %s", userAccountTarget, nick));
            }

            log.debug(String.format("CService::cServeAddUserChan: Applying chanlev %s %s %s", channel, flags, userAccountTarget));
            setChanLev(fromNick, userAccountTarget, channel, flags);
        }

        sendReply(fromNick, Messages.strSuccess);
    }

    /**
     * Handles the setting of userflags
     * @param fromNick requester user node
     * @param str command string
     */
    private void cServeUserflags(CSCommand csCommand) {

        /* Syntax: USERFLAGS [<flags>] */

        String flagsModRaw = "";
        String outputFlagsList   = Messages.strMsgNone;
        String outputFlagsString = Messages.strMsgNone;

        Nick fromNick = csCommand.getFromNick();

        Map<String, String> flagsModStr = new HashMap<String, String>();
        Map<String, Integer> flagsModInt = new HashMap<String, Integer>();

        if (fromNick.isAuthed() == false) {
            sendReply(fromNick, Messages.strErrCommandUnknown);
            return;
        }

        try { flagsModRaw = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) {
            if (fromNick.getAccount().getFlags() != 0) {
                outputFlagsList    = Flags.flagsIntToChars("userflags", fromNick.getAccount().getFlags());
                outputFlagsString  = Flags.flagsIntToString("userflags", fromNick.getAccount().getFlags());
            }
            sendReply(fromNick, String.format(Messages.strUserFlagsList, fromNick.getAccount().getName(), outputFlagsList, outputFlagsString));
            return;
        }

        flagsModStr = Flags.parseFlags(flagsModRaw);

        flagsModInt.put("+", Flags.flagsCharsToInt("userflags", flagsModStr.get("+")));
        flagsModInt.put("-", Flags.flagsCharsToInt("userflags", flagsModStr.get("-")));
        flagsModInt.put("combined", (flagsModInt.get("+") | flagsModInt.get("-")));

        /* Stripping of unknown flags */
        flagsModInt.replace("+", Flags.stripUnknownUserFlags(flagsModInt.get("+")));
        flagsModInt.replace("-", Flags.stripUnknownUserFlags(flagsModInt.get("-")));
        flagsModInt.replace("combined", (flagsModInt.get("+") | flagsModInt.get("-")));

        /* Stripping only DevGod control flags if the user has devgod privileges */
        if (Flags.hasUserDevGodPriv(fromNick.getAccount().getFlags()) == true) {
            flagsModInt.replace("+", Flags.stripUserDevGodConFlags(flagsModInt.get("+")));
            flagsModInt.replace("-", Flags.stripUserDevGodConFlags(flagsModInt.get("-")));
            flagsModInt.replace("combined", (flagsModInt.get("+") | flagsModInt.get("-")));

        }

        /* Stripping only admin control flags if the user has admin privileges */
        if (Flags.hasUserAdminPriv(fromNick.getAccount().getFlags()) == true) {
            flagsModInt.replace("+", Flags.stripUserAdminConFlags(flagsModInt.get("+")));
            flagsModInt.replace("-", Flags.stripUserAdminConFlags(flagsModInt.get("-")));
            flagsModInt.replace("combined", (flagsModInt.get("+") | flagsModInt.get("-")));

        }

        /* Stripping only admin control flags if the user has oper privileges */
        else if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == true) {
            flagsModInt.replace("+", Flags.stripUserOperConFlags(flagsModInt.get("+")));
            flagsModInt.replace("-", Flags.stripUserOperConFlags(flagsModInt.get("-")));
            flagsModInt.replace("combined", (flagsModInt.get("+") | flagsModInt.get("-")));
        }

        /* Stripping only admin control flags if the user has no privileges */
        else {
            flagsModInt.replace("+", Flags.stripUserUserConFlags(flagsModInt.get("+")));
            flagsModInt.replace("-", Flags.stripUserUserConFlags(flagsModInt.get("-")));
            flagsModInt.replace("combined", (flagsModInt.get("+") | flagsModInt.get("-")));
        }

        if (flagsModInt.get("combined") == 0) {
            sendReply(fromNick, Messages.strUserFlagsErrNoMode);
            return;
        }

        try {
            Integer userCurFlags = fromNick.getAccount().getFlags();
            Integer userNewFlags = Flags.applyFlagsFromInt("userflags", userCurFlags, flagsModInt);

            dispatcher.setUserFlags(fromNick, userNewFlags);

            String userNewFlagsStr = Messages.strMsgNone;
            String userNewFlagsString = Messages.strMsgNone;
            if (userNewFlags != 0) {
                userNewFlagsStr    = Flags.flagsIntToChars("userflags", userNewFlags);
                userNewFlagsString = Flags.flagsIntToString("userflags", userNewFlags);
            }


            sendReply(fromNick, Messages.strSuccess);
            sendReply(fromNick, String.format(Messages.strUserFlagsList, fromNick.getAccount().getName(), userNewFlagsStr, userNewFlagsString) );
        }
        catch (Exception e) {
            log.error(String.format("CService/cServeUserFlags: error whith userflags %s for chan %s: ", flagsModStr, fromNick.getAccount().getName()), e);
            sendReply(fromNick, Messages.strUserFlagsErrUnknown);
            return;
        }
    }

    /**
     * Handles the setting of chanflags
     * @param fromNick requester user node
     * @param str command string
     */
    private void cServeChanFlags(CSCommand csCommand) {

        /* Syntax: CHANFLAGS <#channel> [<flags>] */

        String chanFlagsModRaw = "";
        String channel         = "";

        Integer chanNewFlagsInt = 0;
        Integer chanCurFlagsInt = 0;

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        Map<String, String> chanFlagsModSepStr;
        Map<String, Integer> chanFlagsModSepInt = new HashMap<>();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { chanNode = protocol.getChannelNodeByNameCi(channel); }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanNonExist); return; }

        if (chanNode.isRegistered() == false) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

        try {  chanFlagsModRaw =  csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) {
            Integer applicableChFlagsInt = 0;
            String applicableChFlagsStr = "";
            String outputFlagsString = Messages.strMsgNone;

            if ( Flags.hasUserStaffPriv(fromNick.getAccount().getFlags()) == true )  applicableChFlagsInt = chanNode.getcServeFlags();
            else {
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == true) applicableChFlagsInt = Flags.stripChanNonPublicFlags(chanNode.getcServeFlags());
                else  sendReply(fromNick, String.format(Messages.strErrNoAccess)); return;
            }

            if (applicableChFlagsInt > 0) {
                applicableChFlagsStr = "+" + Flags.flagsIntToChars("chanflags", applicableChFlagsInt);
                outputFlagsString    = Flags.flagsIntToString("chanflags", applicableChFlagsInt);
            }
            else { applicableChFlagsStr = Messages.strMsgNone; }

            sendReply(fromNick, String.format(Messages.strChanFlagsList, chanNode.getName(), applicableChFlagsStr, outputFlagsString));
            return;
        }

        if (Flags.isChanSuspended(chanNode.getcServeFlags()) == true) { sendReply(fromNick, Messages.strErrChanSuspended); return; }

        chanFlagsModSepStr = Flags.parseFlags(chanFlagsModRaw);
        chanFlagsModSepInt.put("+", Flags.flagsCharsToInt("chanflags", chanFlagsModSepStr.get("+")));
        chanFlagsModSepInt.put("-", Flags.flagsCharsToInt("chanflags", chanFlagsModSepStr.get("-")));
        chanFlagsModSepInt.put("combined", 0);

        /* Stripping the unknown and readonly flags */
        chanFlagsModSepInt.replace("+", Flags.stripUnknownChanFlags(chanFlagsModSepInt.get("+")));
        chanFlagsModSepInt.replace("-", Flags.stripUnknownChanFlags(chanFlagsModSepInt.get("-")));

        /* Keeping admin editable flags if the user is admin */
        if (Flags.hasUserAdminPriv(fromNick.getAccount().getFlags()) == true) {
            chanFlagsModSepInt.replace("+", Flags.keepChanAdminConFlags(chanFlagsModSepInt.get("+")));
            chanFlagsModSepInt.replace("-", Flags.keepChanAdminConFlags(chanFlagsModSepInt.get("-")));
        }
        /* Keeping oper editable flags if the user is oper */
        else if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == true) {
            chanFlagsModSepInt.replace("+", Flags.keepChanOperConFlags(chanFlagsModSepInt.get("+")));
            chanFlagsModSepInt.replace("-", Flags.keepChanOperConFlags(chanFlagsModSepInt.get("-")));
        }

        /* Keeping chanowner editable flags if the user is owner of the than */
        else if (Flags.hasChanLOwnerPriv(fromNick.getAccount().getChanlev().get(chanNode.getName())) == true) {
            chanFlagsModSepInt.replace("+", Flags.keepChanOwnerConFlags(chanFlagsModSepInt.get("+")));
            chanFlagsModSepInt.replace("-", Flags.keepChanOwnerConFlags(chanFlagsModSepInt.get("-")));
        }
        /* Keeping chanmaster editable flags if the user is master of the than */
        else if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev().get(chanNode.getName())) == true) {
            chanFlagsModSepInt.replace("+", Flags.keepChanMasterConFlags(chanFlagsModSepInt.get("+")));
            chanFlagsModSepInt.replace("-", Flags.keepChanMasterConFlags(chanFlagsModSepInt.get("-")));
        }
        /* User has no rights on the chan */
        else {
            sendReply(fromNick, String.format(Messages.strErrNoAccess));
            return;
        }

        chanFlagsModSepInt.replace("combined", chanFlagsModSepInt.get("+") | chanFlagsModSepInt.get("-"));

        if (chanFlagsModSepInt.get("combined") == 0) {
            sendReply(fromNick, Messages.strChanFlagsErrNoMod);
            return;
        }

        try {

            chanCurFlagsInt = chanNode.getcServeFlags();
            chanNewFlagsInt = Flags.applyFlagsFromInt("chanflags", chanCurFlagsInt, chanFlagsModSepInt);

            dispatcher.setChanFlags(chanNode, chanNewFlagsInt);

            String chanNewFlagsStr = "";
            if (chanNewFlagsInt > 0) { chanNewFlagsStr = "+" + Flags.flagsIntToChars("chanflags", chanNode.getcServeFlags()); }
            else { chanNewFlagsStr = Messages.strMsgNone; }

            sendReply(fromNick, Messages.strSuccess);
            sendReply(fromNick, String.format(Messages.strChanFlagsSuccessSumm, chanNode.getName(), chanNewFlagsStr) );
        }

        catch (Exception e) {
            log.error(String.format("CService/cServeChanFlags: error whith chanflags %s for chan %s: ", chanFlagsModRaw, chanNode.getName()), e);
            sendReply(fromNick, Messages.strChanFlagsErrUnknown);
            return;
        }

    }

    private void cServeAuthHistory(CSCommand csCommand) {

        /* Syntax: AUTHHISTORY [<nick|#useraccount>] */

        String target = "";
        List<Map<String, Object>> authHistList;
        UserAccount userAccount;
        String authType;
        String header;

        Nick fromNick = csCommand.getFromNick();

        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { target = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { target = ""; }

        if (target.isEmpty() == false) {
            if ( Flags.hasUserStaffPriv(fromNick.getAccount().getFlags()) == true ) {
                /* STAFF PARAMETER */
                /* target begins with # => lookup account */
                /* target does not => lookup nick then account */
                if (target.startsWith(Const.USER_ACCOUNT_PREFIX) == true) {
                    try { userAccount = UserAccount.getUserByNameCi(target.replaceFirst(Const.USER_ACCOUNT_PREFIX, "")); }
                    catch (ItemNotFoundException e) { sendReply(fromNick, Messages.strErrUserNonReg); return; }
                }
                else {
                    try { userAccount = Nick.getUserByNickCi(target).getAccount(); }
                    catch (NickNotFoundException e) { sendReply(fromNick, Messages.strErrNickNotFound); return; }
                }

                try { authHistList = database.getAuthHistory(userAccount); }
                catch (Exception e) { authHistList = new ArrayList<>(); }

            }
            else { sendReply(fromNick, Messages.strErrCommandUnknown);  return;
            }
        }
        else {
            userAccount = fromNick.getAccount();
            try { authHistList = database.getAuthHistory(userAccount); }
            catch (Exception e) { authHistList = new ArrayList<>(); }
        }

        header = String.format(Messages.strAuthHisHeadFormat,
            Messages.strAuthHisHeadColID, Messages.strAuthHisHeadColUser, Messages.strAuthHisHeadColDate1, Messages.strAuthHisHeadColDate2, Messages.strSuspendHisHeadColReason);

        sendReply(fromNick, header);
        int lineNumber=1;
        for(Map<String, Object> authLine : authHistList) {
            Date dateAuthTS = new Date( (Long) authLine.get("authTS")*1000L);
            Date dateDeAuthTS = new Date( (Long) authLine.get("deAuthTS")*1000L);
            authType = Const.getAuthTypeString((Integer) authLine.get("authType"));
            Object deAuthResult;
            if ((Long) authLine.get("deAuthTS") == 0L) { deAuthResult = Messages.strMsgNever; }
            else deAuthResult = jdf.format(dateDeAuthTS);

            //String quitResult = (authLine.get("deAuthReason")) == null ? Messages.strMsgNone : (String)authLine.get("deAuthReason");
            String quitResult = (String) deAuthResult;

            sendReply(fromNick, String.format(Messages.strAuthHisHeadFormat, String.valueOf(lineNumber), authLine.get("maskFrom"), jdf.format(dateAuthTS) + " (" + authType + ")", "", quitResult));
            lineNumber++;
        }
        sendReply(fromNick, Messages.strEndOfList);



    }

    private void cServeDropChan(CSCommand csCommand) {
        String channel;
        String confirmCode = "";

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) {
            sendReply(fromNick, Messages.strErrCommandUnknown);
            return;
        }

        try {
            channel  = csCommand.getArgs().get(0);
            chanNode = protocol.getChannelNodeByNameCi(channel);
        }
        catch (IndexOutOfBoundsException e) {
            return;
        }
        catch (Exception f) {
            return;
        }

        if (Channel.isChan(channel) == false) {
            sendReply(fromNick, String.format(Messages.strDropChanErrChanNotReg, channel));
            return;
        }

        if ( Flags.hasChanLOwnerPriv(fromNick.getAccount().getChanlev(chanNode)) == false && Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false ) {
            sendReply(fromNick, Messages.strDropChanErrUserNotOwner);
            return;
        }

        if ( Flags.isChanSuspended(chanNode.getcServeFlags()) == true ) {
            sendReply(fromNick, Messages.strDropChanErrChanSuspended);
            return;
        }

        if (chanNode.getConfirmationCode() == null) { /* This is the first time the user requests the dropping of the channel */

            chanNode.setConfirmationCode(UUID.randomUUID());
            sendReply(fromNick, String.format(Messages.strDropChanConfirmMessage1, chanNode.getName()));
            sendReply(fromNick, String.format(Messages.strDropChanConfirmMessage2, chanNode.getName(), chanNode.getConfirmationCode()));
            return;

        }

        try {
            confirmCode = csCommand.getArgs().get(1);
        }
        catch (Exception e) {
            sendReply(fromNick, String.format(Messages.strDropChanConfirmMessage3, chanNode.getName(), chanNode.getConfirmationCode()));
            return;
        }

        if (confirmCode.equals(chanNode.getConfirmationCode().toString()) == false) {
            chanNode.setConfirmationCode(null);
            sendReply(fromNick, Messages.strDropChanErrWrongConfirm);
            return;
        }

        try {
            dispatcher.dropChan(chanNode, fromNick);

            protocol.setMode( chanNode, "-r", "");
            protocol.chanPart(myUserNode, chanNode, Messages.strChanPartDropManual);

            sendReply(fromNick, Messages.strDropChanSuccess);
            log.info(String.format("CService/cServeCDropChan: channel %s dropped by %s", chanNode.getName(), fromNick.getAccount().getName()));

        }
        catch (Exception e) {
            sendReply(fromNick, Messages.strDropChanErrUnknown);
            log.error(String.format("CService/cServeDropChan: Error while dropping channel %s", chanNode.getName()), e);
            return;
        }
    }

    private void cServeDropUser(CSCommand csCommand) {
        String user;
        String confirmCode = "";

        Set<Nick>  loggedUserNodes  = new HashSet<>();
        Set<Channel>   knownChannels    = new HashSet<>();

        Nick targetUserNode;
        Nick fromNick = csCommand.getFromNick();

        UserAccount targetUserAccount = null;

        if (fromNick.isAuthed() == false) {
            sendReply(fromNick, Messages.strErrCommandUnknown);
            return;
        }

        try {
            user = csCommand.getArgs().get(0);
        }
        catch (IndexOutOfBoundsException e) {
            sendReply(fromNick, Messages.strDropUserErrSyntax);
            return;
        }

        if (user.startsWith(Const.USER_ACCOUNT_PREFIX) == true) {
            try { targetUserAccount = UserAccount.getUserByNameCi(user.replaceFirst(Const.USER_ACCOUNT_PREFIX, "")); }
            catch (Exception e) {
                sendReply(fromNick, Messages.strErrUserNonReg);
                return;
            }
        }
        else {
            try { targetUserNode = Nick.getUserByNickCi(user); }
            catch (NickNotFoundException e) {
                sendReply(fromNick, Messages.strErrNickNotFound);
                return;
            }

            if (targetUserNode.isAuthed() == true) {
                targetUserAccount = targetUserNode.getAccount();
            }
            else {
                sendReply(fromNick, Messages.strErrNickNotAuthed);
                return;
            }
        }

        if (fromNick.getAccount().equals(targetUserAccount) == false && Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) {
            sendReply(fromNick, Messages.strDropUserErrTargetUser);
            return;
        }
        else {
            if ( Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == true && Flags.isUserSuspended(targetUserAccount.getFlags()) == true ) {
                sendReply(fromNick, Messages.strDropUserErrUserSuspended);
                return;
            }
        }

        if (targetUserAccount.getConfirmationCode() == null) { /* This is the first time the user requests the dropping of the channel */

            targetUserAccount.setConfirmationCode(UUID.randomUUID());
            sendReply(fromNick, String.format(Messages.strDropUserConfirmMessage1, targetUserAccount.getName()));
            sendReply(fromNick, String.format(Messages.strDropUserConfirmMessage2, targetUserAccount.getName(), targetUserAccount.getConfirmationCode()));
            return;

        }

        try {
            confirmCode = csCommand.getArgs().get(1);
        }
        catch (Exception e) {
            sendReply(fromNick, String.format(Messages.strDropUserConfirmMessage3, targetUserAccount.getName(), targetUserAccount.getConfirmationCode()));
            return;
        }

        if (confirmCode.equals(targetUserAccount.getConfirmationCode().toString()) == false) {
            targetUserAccount.setConfirmationCode(null);
            sendReply(fromNick, Messages.strDropUserErrWrongConfirm);
            return;
        }

        /* Deauth all the nicks associated */
        targetUserAccount.getUserLogins().forEach( (usernode) -> {
            loggedUserNodes.add(usernode);
        });

        for (Nick loggedUserNode : loggedUserNodes) {
            try {
                this.logoutUser(loggedUserNode, Const.DEAUTH_TYPE_DROP);
                sendReply(loggedUserNode, Messages.strDropUserDeAuth);
            }
            catch (Exception e) {
                log.error("CService/DropAccount: could not deauthenticate nick " + loggedUserNode.getNick() + " from account " + targetUserAccount.getName() + ": ", e);
            }
        }

        try {

            // TODO: move to Dispatcher
            /* Clean the chanlevs */
            targetUserAccount.getChanlev().forEach( (chanName, chanlev) -> {
                knownChannels.add(protocol.getChannelNodeByNameCi(chanName));
            });
            for (Channel channel : knownChannels) {
                database.clearUserChanlev(targetUserAccount.getName(), channel.getName());
                channel.setChanlev(database.getChanChanlev(channel));
            }
            database.clearUserChanlev(targetUserAccount.getName());

            dispatcher.deleteUserAccount(targetUserAccount);
            log.info(String.format("CService/DropAccount: %s has been dropped", targetUserAccount.getName()));

            /* Delete the reference */
            targetUserAccount = null;

            sendReply(fromNick, Messages.strDropUserSuccess);

        }
        catch (Exception e) {
            sendReply(fromNick, Messages.strDropUserErrUnknown);
            log.error(String.format("CService/cServeDropUser: Error dropping user account %s", targetUserAccount.getName()), e);
            return;
        }
    }

    private void cServeRequestBot(CSCommand csCommand, Boolean hasOperMode) { // FIXME: after requesting a bot, chanlev/autolimit seem not properly synchronized somewhere

        int chanId = 0;
        String channel;
        String target;

        Channel chanNode;

        UserAccount targetAccount = null;
        UserAccount ownerAccount;

        Nick user = csCommand.getFromNick();

        if (user.isAuthed() == false) {
            sendReply(user, Messages.strErrCommandUnknown);
            return;
        }

        try {
            channel = csCommand.getArgs().get(0);
            chanNode = protocol.getChannelNodeByNameCi(channel);
        }
        catch (ItemNotFoundException e) {
            sendReply(user, Messages.strErrChanNonExist);
            return;
        }
        catch (Exception e) {
            sendReply(user, Messages.strRequestBotErrUnknown);
            log.error(String.format("CService/cServeRequestBot: error while getting channel name to channel node. String was: %s", csCommand.getArgs()), e);
            return;
        }

        if (hasOperMode == true) {

            try { target = csCommand.getArgs().get(1); }
            catch (Exception e) {
                sendReply(user, Messages.strRequestBotErrCmdIncomplete);
                return;
            }

            try {
                if (target.startsWith(Const.USER_ACCOUNT_PREFIX) == true) { targetAccount = UserAccount.getUserByNameCi(target.replaceFirst("^" + Const.USER_ACCOUNT_PREFIX, "")); }
                else { targetAccount = Nick.getUserByNickCi(target).getAccount();  }
            }
            catch (Exception e) {
                sendReply(user, Messages.strRequestBotErrNickOrAccNotFound);
                return;
            }
            if (targetAccount == null) {
                sendReply(user, Messages.strErrNickNotAuthed);
                return;
            }
        }

        if (hasOperMode == true) ownerAccount = targetAccount;
        else ownerAccount = user.getAccount();

        /* Check the user chanlev in case the limit is reached */
        if (ownerAccount.getChanlev().size() >= config.getCServeAccountMaxChannels() && Flags.hasUserOperPriv(ownerAccount.getFlags()) == false) {
            sendReply(user, Messages.strRequestBotErrChanlevFull);
            return;
        }

        /* First check that the user is on the channel and opped */
        /* TODO: to move to dispatcher */
        if (hasOperMode == true || user.getModesChan(chanNode).contains("o") == true) {
            try {
                chanId = database.addRegChan(chanNode);

                database.setChanFlags(chanNode, Flags.getDefaultChanFlags());


                database.setUserChanlev(ownerAccount, chanNode, Flags.getChanLFlagOwnerDefault());

                ownerAccount.setChanlev(chanNode, Flags.getChanLFlagOwnerDefault());

                /* updating channel chanlev as well */
                Map<String, Integer> chanNewChanlev = database.getChanChanlev(chanNode);
                chanNode.setChanlev(chanNewChanlev);
                chanNode.setcServeFlags(Flags.getDefaultChanFlags());
                chanNode.setRegistered(true);
                chanNode.setcServeId(chanId);

                protocol.addRegChan(chanNode);
                protocol.chanJoin(myUserNode, chanNode);
                performJoinActions(myUserNode, chanNode);
                //protocol.setMode( chanNode, "+r" + chanJoinModes, myUserNode.getNick());

                sendReply(user, Messages.strRequestBotSuccess);
            }
            catch (Exception e) {
                sendReply(user, Messages.strRequestBotErrUnknown);
                log.error(String.format("CService/cServeRequestBot: Error registering channel %s", chanNode.getName()), e);
                return;
            }
        }
        else {
            sendReply(user, Messages.strRequestBotErrChanNotPresentOrOp);
        }
    }

    private void cServeHello(CSCommand csCommand) {
        String password = "";
        String accountName;
        String email;
        Nick userNode = csCommand.getFromNick();

        if (userNode.isAuthed() == true) { sendReply(userNode, Messages.strHelloErrAlreadyAuth); return; }

        if (userNode.isAccountPending() == true) { sendReply(userNode, Messages.strHelloThrottle); return; }

        try {
            email = csCommand.getArgs().get(0);
            if (config.hasFeature("tempAccountPassword") == false) password = csCommand.getArgs().get(1);
        }
        catch (IndexOutOfBoundsException e) { sendReply(userNode, Messages.strErrCommandSyntax); return; }

        if (email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+")==false) {
            sendReply(userNode, Messages.strHelloErrEmailInvalid);
            return;
        }

        if (config.hasFeature("tempAccountPassword") == false) {
            if (isPassComplex(password) == false) {
                sendReply(userNode, String.format(Messages.strHelloErrTooEasy, config.getCServiceAccountMinPassLength(), config.getCServiceAccountMaxPassLength()));
                return;
            }
        }
        else password = (new RandomString(config.getTempAccountPasswordLength())).nextString();


        if (config.hasFeature("randomAccountName") == false) accountName = userNode.getNick();
        else accountName = (new RandomString(config.getRandomAccountNameLength())).nextString().toLowerCase();

        String pwHash = null;
        String pwSalt = null;
        try {

            /* Argon2 salt/password hash generator */
            pwSalt = Argon2Hash.generateSalt();
            Argon2Hash pwGen = new Argon2Hash(pwSalt);

            pwHash = pwGen.generateHash(password);
        }
        catch (Exception e) { log.error(String.format("CService/cServeHello: Error handling password."), e); }

        Map<String, String> newAccoutParams = new HashMap<>();

        newAccoutParams.put("accountName", accountName);
        newAccoutParams.put("email", email);
        newAccoutParams.put("pwHash", pwHash);
        newAccoutParams.put("pwSalt", pwSalt);

        try { dispatcher.createUserAccount(newAccoutParams); }
        catch (Exception e) { sendReply(userNode, Messages.strHelloErrAccountExists); e.printStackTrace(); return; }

        userNode.setAccountPending(true);

        sendReply(userNode, String.format(Messages.strHelloNewAccountCreated, accountName, password));

    }

    private void cServeAuth(CSCommand csCommand) {

        /* Syntax: AUTH <username> [<password>] */

        String   password;
        String   username;
        String   certfp = "";
        Integer  authType = 0;
        UserAccount useraccount;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == true) { sendReply(fromNick, Messages.strAuthErrAlreadyAuth); return; }

        try { username = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { password = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) { password = ""; }

        try { useraccount = UserAccount.getUserByNameCi(username); }
        catch (Exception e) {
            /* Delay auth to slow down brute force attack */
            try { Thread.sleep(config.getCServeAccountWrongCredWait() *1000); }
            catch (Exception f) { /* Nothing to do */ }
            sendReply(fromNick, Messages.strAuthErrAccountCred);
            return;
        }

        if (password.isEmpty() == false) { /* Doing password auth */

            authType = Const.AUTH_TYPE_PLAIN;

            try { useraccount.authUserToAccount(fromNick, password, authType); }
            catch (UserAuthCredException | ItemSuspendedException e) {
                try { Thread.sleep(config.getCServeAccountWrongCredWait() *1000); }
                catch (Exception f) { /* Nothing to do */ }
                sendReply(fromNick, Messages.strAuthErrAccountCred);
                return;
            }
            catch (UserAuthException e) {
                log.error(String.format("Error during auth process for user '%s' with nick '%s'", useraccount, fromNick), e);
                sendReply(fromNick, Messages.strAuthErrUnknown);
                return;
            }

        }

        else { /* Doing certfp auth */
            if (fromNick.getCertFP().isEmpty() == false && fromNick.getCertFP() != null) {
                certfp = fromNick.getCertFP();
                authType = Const.AUTH_TYPE_CERTFP;

                try {
                    useraccount.authUserToAccount(fromNick, certfp, authType);
                }
                catch (UserAuthCredException | ItemSuspendedException e) {
                    sendReply(fromNick, Messages.strAuthErrAccountCred);
                    return;
                }
                catch (UserAuthException e) {
                    log.error(String.format("Error during auth process for user '%s' with nick '%s'", useraccount, fromNick), e);
                    sendReply(fromNick, Messages.strAuthErrUnknown);
                    return;
                }

            }
            else {
                sendReply(fromNick, Messages.strAuthErrCertFpNotAvail);
                return;
            }
        }

        if (config.hasFeature("svslogin") == true) {
            protocol.sendSvsLogin(fromNick, fromNick.getAccount());
        }

        if (Flags.isUserAutoVhost(fromNick.getAccount().getFlags()) == true && config.hasFeature("chghost") == true) {
            protocol.chgHostVhost(fromNick, fromNick.getAccount().getName());
        }

        fromNick.getAccount().getChanlev().forEach( (channel, chanlev) -> {
            if (Flags.isChanLAutoInvite(chanlev) == true && fromNick.getChanList().containsKey(protocol.getChannelNodeByNameCi(channel)) == false) {
                protocol.sendInvite(fromNick, protocol.getChannelNodeByNameCi(channel));
            }
        });

        sendReply(fromNick, Messages.strAuthSuccess);

        /*
         * Now we apply the modes of the user's chanlev as it was joining the channels
         * But no welcome message
         */
        fromNick.getChanList().forEach( (chanObj, mode) -> {
            this.handleJoin(fromNick, chanObj, false);
        });
    }

    private void cServeCertfp(CSCommand csCommand) {

        UserAccount userAccount = null;

        Map<String, Set<String>> certfpList;

        Set<String> certfpToAdd;
        Set<String> certfpToRemove;
        Set<String> certFpAdded   = new TreeSet<>();
        Set<String> certFpRemoved = new TreeSet<>();

        CheckCertFp checkCertFp = (certfp) -> {
            if (certfp.matches(Const.CS_CERTFP_FP_REGEX) == false && certfp.length() <= Const.CS_CERTFP_MAX_SIZE) return false;
            return true;
        };

        if (csCommand.isFromNickAuthed() == false) { sendReply(csCommand.getFromNick(), Messages.strErrCommandUnknown); return; }

        userAccount    = csCommand.getFromNickAccount();
        certfpList     = StringTools.addRemoveString(csCommand.getArgs(), Const.CS_CERTFP_MAX_INPUT);
        certfpToAdd    = certfpList.get("+");
        certfpToRemove = certfpList.get("-");

        /* Display registered certfp */
        if (certfpToAdd.size() + certfpToRemove.size() == 0) {
            sendReply(csCommand.getFromNick(), String.format(Messages.strCertFpTitle, userAccount.getCertFP().size()));
            for(String s: userAccount.getCertFP()) {
                sendReply(csCommand.getFromNick(), String.format(Messages.strWhoisContentUserCertfpRowFormat, s));
            }

            return;
        }

        for (String s: certfpToAdd) {
            if (config.getCServeAccountAllowManualCertFP() == false) {
                break;
            }
            if (checkCertFp.checkCertFp(s) == false) {
                sendReply(csCommand.getFromNick(), Messages.strCertFpErrMalformed);
                return;
            }
            try { dispatcher.addUserCertFp(userAccount, s); }
            catch (MaxLimitReachedException e) {
                sendReply(csCommand.getFromNick(), String.format(Messages.strCertFpErrAddMax, config.getCServeAccountMaxCertFP()));
                return;
            }
            catch (ItemErrorException e) {
                log.error(String.format("CService::cServeCertfp: Could not add certfp %s to user %s", s, userAccount.getName()), e);
                sendReply(csCommand.getFromNick(), Messages.strErrCommandSyntax);
                return;
            }
            catch (DataBaseExecException e) {
                log.error(String.format("CService::cServeCertfp: Could not add certfp %s to user %s", s, userAccount.getName()), e);
                sendReply(csCommand.getFromNick(), Messages.strErrCommandSyntax);
                return;
            }
            catch (ItemExistsException e) {
                log.error(String.format("CService::cServeCertfp: Could not add certfp %s to user %s", s, userAccount.getName()), e);
                sendReply(csCommand.getFromNick(), Messages.strErrCommandSyntax);
                return;
            }

            certFpAdded.add(s);
        }

        if (certfpToAdd.size() > 0 && config.getCServeAccountAllowManualCertFP() == false) {
            sendReply(csCommand.getFromNick(), String.format(Messages.strCertFpDenyManualAdd));
            String autoCertFp = csCommand.getFromNick().getCertFP();

            if (autoCertFp.isEmpty() == true) {
                sendReply(csCommand.getFromNick(), Messages.strCertFpErrNoCertFp);
                return;
            }
            if (checkCertFp.checkCertFp(autoCertFp) == false) {
                sendReply(csCommand.getFromNick(), Messages.strCertFpErrMalformed);
                return;
            }
            try { dispatcher.addUserCertFp(userAccount, autoCertFp); }
            catch (MaxLimitReachedException e) {
                sendReply(csCommand.getFromNick(), String.format(Messages.strCertFpErrAddMax, config.getCServeAccountMaxCertFP()));
                return;
            }
            catch (ItemErrorException e) {
                log.error(String.format("CService::cServeCertfp: Could not add certfp %s to user %s", autoCertFp, userAccount.getName()), e);
                sendReply(csCommand.getFromNick(), Messages.strErrCommandSyntax);
                return;
            }
            catch (DataBaseExecException e) {
                log.error(String.format("CService::cServeCertfp: Could not add certfp %s to user %s", autoCertFp, userAccount.getName()), e);
                sendReply(csCommand.getFromNick(), Messages.strErrCommandSyntax);
                return;
            }
            catch (ItemExistsException e) {
                log.error(String.format("CService::cServeCertfp: Could not add certfp %s to user %s", autoCertFp, userAccount.getName()), e);
                sendReply(csCommand.getFromNick(), Messages.strErrCommandSyntax);
                return;
            }

            certFpAdded.add(autoCertFp);
        }

        for (String s: certfpToRemove) {
            if (checkCertFp.checkCertFp(s) == false) {
                sendReply(csCommand.getFromNick(), Messages.strCertFpErrMalformed);
                return;
            }
            try { dispatcher.removeUserCertFp(userAccount, s); }
            catch (ItemErrorException e) {
                log.error(String.format("CService::cServeCertfp: Could not remove certfp %s from user %s", s, userAccount.getName()), e);
                sendReply(csCommand.getFromNick(), Messages.strErrCommandSyntax);
                return;
            }
            catch (DataBaseExecException e) {
                log.error(String.format("CService::cServeCertfp: Could not remove certfp %s from user %s", s, userAccount.getName()), e);
                sendReply(csCommand.getFromNick(), Messages.strErrCommandSyntax);
                return;
            }
            catch (ItemNotFoundException e) {
                log.error(String.format("CService::cServeCertfp: Could not remove certfp %s from user %s", s, userAccount.getName()), e);
                sendReply(csCommand.getFromNick(), Messages.strErrCommandSyntax);
                return;
            }

            certFpRemoved.add(s);
        }

        if (certFpAdded.size() > 0) sendReply(csCommand.getFromNick(), String.format(Messages.strCertFpSucAdd, Arrays.toString(certFpAdded.toArray())));

        if (certFpRemoved.size() > 0) sendReply(csCommand.getFromNick(), String.format(Messages.strCertFpSucDel, Arrays.toString(certFpRemoved.toArray())));
        sendReply(csCommand.getFromNick(), Messages.strSuccess);

        return;
    }

    private void cServeLiveChanlist(CSCommand csCommand) {

        Set<Channel> chanListSorted;

        Nick userNode = csCommand.getFromNick();
        String inStr = csCommand.getArgs().get(0);

        if (userNode.isOper() == false) {
            sendReply(userNode, Messages.strErrCommandUnknown);
            return;
        }
        sendReply(userNode, "List of channels:");

        /*for (Map.Entry<A, B> e : myMap.entrySet()) {
            A key    = e.getKey();
            B value  = e.getValue();
        }*/
        String filterInput, filter;
        if (inStr.length() > 1) {
            filterInput = inStr.replaceAll("[^A-Za-z0-9]", "");
        }
        else filterInput = "";
        filter = ".*" + filterInput + ".*";

        var wrapperChanList = new Object() {
            String bufferMode = "";
            String bufferParam = "";
        };
        var wrapperUserList = new Object() {
            String userNick = "";
        };

        sendReply(userNode, "There are " + Channel.getChanList().size() + " channels on the network.");

        chanListSorted = new TreeSet<>(Channel.getChanList());
        chanListSorted.forEach( (node) -> {
            String chan = node.getName();

            if (chan.matches("(?i)" + filter)) {
                Long channelTsValue = node.getRegistrationTS().getValue();

                Date date = new Date(channelTsValue*1000L);
                jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String chanTSdate = jdf.format(date);
                wrapperChanList.bufferMode  = "";
                wrapperChanList.bufferParam = "";
                node.getModes().forEach( (mode,param ) -> {
                    wrapperChanList.bufferMode   = String.join("", wrapperChanList.bufferMode, mode);
                    if (param.isEmpty() == false) wrapperChanList.bufferParam  = String.join(" ", wrapperChanList.bufferParam, param);
                });
                wrapperUserList.userNick = "";
                node.getUsers().forEach( (usernode, mode) -> { wrapperUserList.userNick = String.join(" ", wrapperUserList.userNick, usernode.getNick());  } );
                sendReply(userNode, String.format("+ %s [TS %s] [+%s %s]", chan, chanTSdate, wrapperChanList.bufferMode, wrapperChanList.bufferParam));
                sendReply(userNode, "|- users (" + node.getUserCount() + "): " + wrapperUserList.userNick );
                sendReply(userNode, "|- ban list: " + node.getBanList().toString() );
                sendReply(userNode, "|- except list: " + node.getExceptList().toString() );
                sendReply(userNode, "`- invite list: " + node.getInviteList().toString() );
            }
        });
        sendReply(userNode, Messages.strEndOfList);
    }

    private void cServeLiveUserlist (CSCommand csCommand) {

        Nick userNode = csCommand.getFromNick();

        if (userNode.isOper() == false) { sendReply(userNode, Messages.strErrCommandUnknown); return; }

        sendReply(userNode, "List of users:");

        Nick userGetValue = null;
        for (Nick user : Nick.getUsersList()) {
            userGetValue = user;
            //sendReply(userNode, " * " + user.getValue().getUid() + " " + user.getValue().getNick() + "!" + user.getValue().getIdent() + "@" + user.getValue().getHost() + " [" + user.getValue().getRealHost() + "] +" + user.getValue().getModes() + " * " + user.getValue().getRealName());
            sendReply(userNode, String.format(" * %s %s!%s@%s [%s] +%s * %s",
                                userGetValue.getUid(), userGetValue.getNick(), userGetValue.getIdent(), userGetValue.getHost(), userGetValue.getRealHost(), userGetValue.getModesAsString(), userGetValue.getRealName()));
        }
        sendReply(userNode, "There are " + Nick.getUsersList().size() + " users on the network.");
        sendReply(userNode, Messages.strEndOfList);
    }

    private void cServeLiveServerlist(CSCommand csCommand) {

        String serverPeerStatus = "";
        String serverEOS = "no";
        String parent = Messages.strMsgNone;
        String out = "";
        Nick userNode = csCommand.getFromNick();


        if (userNode.isOper() == false) { sendReply(userNode, Messages.strErrCommandUnknown); return; }

        sendReply(userNode, "List of servers:");

        for (Server server : Server.getServerList()) {

            serverPeerStatus = "";
            serverEOS = "no";
            parent = Messages.strMsgNone;

            if (server.isPeer()==true) { serverPeerStatus = "*";  }
            else { serverPeerStatus = " "; }

            if (server.hasEOS()==true) { serverEOS = "yes";  }

            if (server.getParent() != null) { parent = server.getParent().getName(); }

            out = String.format("%s%s (%s) / EOS: %s / parent: %s (%s)", serverPeerStatus, server.getName(), server.getSid(), serverEOS, parent, server.getParent().getSid());
            sendReply(userNode, out);

        }
        sendReply(userNode, "There are " + Server.getServerList().size() + " servers on the network.");
        sendReply(userNode, Messages.strEndOfList);
    }

    private void cServeDeAuth(CSCommand csCommand) {

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        UserAccount userAccount = fromNick.getAccount();

        try { userAccount.deAuthUserFromAccount(fromNick, Const.DEAUTH_TYPE_MANUAL); }
        catch (Exception e) {
            log.error(String.format("Cannot logout nick %s / account %s", fromNick.getNick(), userAccount.getName()), e);
            sendReply(fromNick, Messages.strDeAuthErrUnknown);
            return;
        }

        if (Flags.isUserAutoVhost(userAccount.getFlags()) == true && config.hasFeature("chghost") == true) { protocol.chgHost(fromNick, fromNick.getCloakedHost()); }

        if (config.hasFeature("svslogin") == true) { protocol.sendSvsLogin(fromNick); }

        sendReply(fromNick, Messages.strSuccess);
    }

    private void cServeDeAuthAll(CSCommand csCommand) {

        String accountNameTxt = "";

        UserAccount userAccount;

        Nick usernode = csCommand.getFromNick();

        if (usernode.isAuthed() == false) { sendReply(usernode, Messages.strErrCommandUnknown); return; }

        try { accountNameTxt = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { accountNameTxt = usernode.getAccount().getName(); }


        if (Flags.hasUserOperPriv(usernode.getAccount().getFlags()) == true) {
            try { userAccount = UserAccount.getUserByNameCi(accountNameTxt); }
            catch (ItemNotFoundException e) { sendReply(usernode, Messages.strErrUserNonReg); return; }
        }

        else userAccount = usernode.getAccount();

        for (Nick authedNick: userAccount.getUserLogins()) {
            try {
                userAccount.deAuthUserFromAccount(authedNick, Const.DEAUTH_TYPE_MANUAL);
                sendReply(authedNick, String.format(Messages.strDeAuthAllNotice, usernode.getNick(), userAccount.getName()));
            }
            catch (Exception e) {
                log.error(String.format("CService/cServeDeAuthAll: Cannot logout nick %s / account %s", authedNick.getNick(), userAccount.getName()), e);
                sendReply(usernode, Messages.strDeAuthErrUnknown);
                return;
            }

            if (Flags.isUserAutoVhost(userAccount.getFlags()) == true && config.hasFeature("chghost") == true) { protocol.chgHost(authedNick, authedNick.getCloakedHost()); }

            if (config.hasFeature("svslogin") == true) { protocol.sendSvsLogin(authedNick); }
        }

        sendReply(usernode, Messages.strSuccess);
    }

    private void logoutUser(Nick usernode, Integer deAuthType) {

        UserAccount userAccount = usernode.getAccount();

        try { userAccount.deAuthUserFromAccount(usernode, deAuthType); }
        catch (Exception e) { log.error("CService/logoutUser: error while logging out nick " + usernode.getNick(), e); return; }

        if (Flags.isUserAutoVhost(userAccount.getFlags()) == true && config.hasFeature("chghost") == true) { protocol.chgHost(usernode, usernode.getCloakedHost()); }

        if (config.hasFeature("svslogin") == true) { protocol.sendSvsLogin(usernode); }
    }

    private void cServeDie(CSCommand csCommand) {

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == true && Flags.hasUserAdminPriv(fromNick.getAccount().getFlags()) == true) {
            sendReply(fromNick, Messages.strMiscTheCakeIsALie);
            protocol.sendWallops(myServerNode, Messages.strMiscTheCakeIsALie);
            log.fatal(String.format("Received DIE command from user %s (account %s), exiting.", fromNick, fromNick.getAccount()));
            System.exit(0);
        }
        else sendReply(fromNick, Messages.strErrCommandUnknown);
    }

    private void cServeCrash(CSCommand csCommand) throws Exception {

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == true && Flags.hasUserAdminPriv(fromNick.getAccount().getFlags()) == true) {
            sendReply(fromNick, "So long, my friend.");
            throw new Exception("Catch me if you can");
        }
        else sendReply(fromNick, Messages.strErrCommandUnknown);
    }

    private void cServeRenchan(CSCommand csCommand) {
        /*
         * RENCHAN #currentName #newName
         */

        String curChannelName = "";
        String newChannelName = "";

        Channel curChannel;
        Channel newChannel;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown);  return; }

        if (Flags.hasUserAdminPriv(fromNick.getAccount().getFlags()) == true) {

            try { curChannelName = csCommand.getArgs().get(0); }
            catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

            try { newChannelName = csCommand.getArgs().get(1); }
            catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

            try { curChannel = protocol.getChannelNodeByNameCi(curChannelName); }
            catch (Exception e) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

            try { newChannel = protocol.getChannelNodeByNameCi(newChannelName); }
            catch (Exception e) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

            /* Channel does not have +j flag (could be suspended or something) */
            if (Flags.isChanJoined(curChannel.getcServeFlags()) == false) { sendReply(fromNick, Messages.strErrChanNotJoined); return; }

            try { dispatcher.renameChannel(curChannel, newChannel); }
            catch (ChannelNotFoundException e) { sendReply(fromNick, Messages.strErrCmdExec); return; }

            try { protocol.setMode( curChannel, "-r", ""); }
            catch (Exception e) { log.error(String.format("Could not unset mode for %s after RENCHAN command", curChannel.getName()), e); return; }


            protocol.chanPart(myUserNode, curChannel, Messages.strChanPartRenchan);
            protocol.chanJoin(myUserNode, newChannel);

            //try { protocol.setMode( newChannel, "+r" + chanJoinModes, myUserNode.getNick()); }
            //catch (Exception e) { log.error(String.format("Could not set mode for %s after RENCHAN command", curChannel.getName()), e); return; }

            /*
            * Now we apply the modes of the channel's chanlev as if everyone were joining the channel
            * We display the welcome message as well
            */
            /*
            newChannel.getUsers().forEach( (nick, modeList) -> {
                this.handleJoin(nick, newChannel, true);
            });
            */

            performJoinActions(myUserNode, newChannel);

            sendReply(fromNick, Messages.strSuccess);
        }

        else { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

    }

    private void cServeRejoin(CSCommand csCommand) {
        String channel = "";

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == true) {

            try { channel = csCommand.getArgs().get(0); }
            catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

            try { chanNode = protocol.getChannelNodeByNameCi(channel); }
            catch (Exception e) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

            /* Channel does not have +j flag (could be suspended or something) */
            if (Flags.isChanJoined(chanNode.getcServeFlags()) == false) { sendReply(fromNick, Messages.strErrChanNotJoined); return; }

            protocol.chanPart(myUserNode, chanNode, Messages.strChanPartRejoin);
            protocol.chanJoin(myUserNode, chanNode);

            performJoinActions(myUserNode, chanNode);

            //try { protocol.setMode( chanNode, "+r" + chanJoinModes, myUserNode.getNick()); }
            //catch (Exception e) { log.error(String.format("Could not set mode for %s after REJOIN command", chanNode.getName()), e); return; }
            sendReply(fromNick, Messages.strSuccess);
        }

        else { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

    }

    private void cServeWelcome(CSCommand csCommand) {

        String newWelcomeMsg;
        String channel = "";

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { chanNode = protocol.getChannelNodeByNameCi(channel); }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

        try { newWelcomeMsg = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) {
            String curWelcomeMsg = "";
            try {
                curWelcomeMsg = database.getWelcomeMsg(chanNode);
                if (curWelcomeMsg.isEmpty() == true) { curWelcomeMsg = Messages.strMsgNone; }
            }
            catch (Exception f) { }
            if (Flags.hasChanLSignificant(fromNick.getAccount().getChanlev(chanNode)) == true || Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == true) {
                sendReply(fromNick, String.format(Messages.strWelcomeDispMess, chanNode.getName(), curWelcomeMsg) );
            }
            else sendReply(fromNick, String.format(Messages.strErrNoAccess));
            return;
        }

        if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(chanNode)) == true || Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == true ) {

            try {
                database.setWelcomeMsg(chanNode, newWelcomeMsg);
                sendReply(fromNick, Messages.strSuccess);
                sendReply(fromNick, String.format(Messages.strWelcomeDispMess, chanNode.getName(), newWelcomeMsg));
            }
            catch (Exception e) { sendReply(fromNick, String.format(Messages.strWelcomeErrUnknown, chanNode.getName())); return; }
        }
        else { sendReply(fromNick,  String.format(Messages.strErrNoAccess)); }
    }

    private void cServeSetTopic(CSCommand csCommand) {

        String newTopic;
        String channel = "";

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        int fromNickChanlev;
        int fromNickUserFlags;

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { chanNode = protocol.getChannelNodeByNameCi(channel); }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

        try { newTopic = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) { newTopic = chanNode.getTopic().getText(); }

        fromNickChanlev = fromNick.getAccount().getChanlev(chanNode);
        fromNickUserFlags = fromNick.getAccount().getFlags();

        if ( (Flags.hasChanLMasterPriv(fromNickChanlev) == true) || (Flags.hasUserOperPriv(fromNickUserFlags) == true) || (Flags.isChanLTopic(fromNickChanlev) == true) ) {

            try {
                if (newTopic == null) { chanNode.getTopic().getText(); }

                database.setTopic(chanNode, newTopic);

                if (newTopic.equals(chanNode.getTopic().getText()) == false) protocol.setTopic(myUserNode, chanNode, newTopic);

                sendReply(fromNick, Messages.strSuccess);
            }
            catch (Exception e) {sendReply(fromNick, String.format(Messages.strSetTopicErrUnknown, chanNode.getName())); return; }

        }
        else { sendReply(fromNick, String.format(Messages.strErrNoAccess)); }
    }

    private void cServeClearTopic(CSCommand csCommand) {

        /* CLEARTOPIC #channel */

        String channel   = "";
        String newTopic  = "";

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { chanNode = protocol.getChannelNodeByNameCi(channel); }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

        if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(chanNode)) == true || Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == true || Flags.isChanLTopic(fromNick.getAccount().getChanlev(chanNode)) == true ) {
            try {
                database.setTopic(chanNode, newTopic);
                if (newTopic.equals(chanNode.getTopic().getText()) == false)  protocol.setTopic(myUserNode, chanNode, newTopic);
                sendReply(fromNick, Messages.strSuccess);
            }
            catch (Exception e) { sendReply(fromNick, String.format(Messages.strClearTopicErrUnknown, chanNode.getName())); return; }
        }
        else { sendReply(fromNick, String.format(Messages.strErrNoAccess)); }
    }

    /**
     * Handles the help
     * @param fromNick requester user node
     * @param commandName command string
     */
    private void sendHelp(Nick fromNick, String commandName) {
        Help help;
        List<String> helpContent;

        String level;
        String commandNameUp;

        commandNameUp = commandName.toUpperCase();
        help = Help.getHelp(commandNameUp, "CService", "command_single");
        level = help.getMetadata().get("level");
        if (level == null || level.isEmpty() == true) level = "NONE_PRIV";

        if (Flags.hasLevelPrivilege(fromNick, level) == false) { helpContent = List.of(Messages.strErrCommandUnknown); }
        else helpContent = help.getData();

        for (String l: helpContent) sendReply(fromNick, l);

    }

    private void cServeHelp(CSCommand csCommand) {
        /* HELP <command> */

        /** Command name to get help on */
        String helpCommandName;

        try {
            /* Command name to UPPERCASE */
            helpCommandName = csCommand.getArgs().get(0).toUpperCase();
        }
        catch (IndexOutOfBoundsException e) {
            /* If no command name is provided, send the short help */
            getShowcommands(csCommand, "showcommandsshort");
            return;
        }

        /* Commands does not exist */
        if (Help.CSERVICE_CMD_LIST.containsKey(helpCommandName) == false) {
            sendReply(csCommand.getFromNick(), String.format(Messages.strErrNoAccess));
            return;
        }

        sendHelp(csCommand.getFromNick(), helpCommandName);

    }

    private void getShowcommands(CSCommand csCommand, String type) {
        /*
         * List of contexts
         * ================
         * - 000 = always displayed
         * - 001 = unauth user
         * - 050 = authed user
         * - 100 = staff member
         * - 150 = oper member
         * - 200 = admin member
         * - 900 = developper member (aka devgod)
         *
         */

        /* TODO: get rid of levels and work with the roles */
        /* Role list
         *
         */

        Help help;
        List<String> helpContent;
        List<String> helpOutput;

        String level;
        String lineLevel;
        String lineString;
        String commandNameUp;

        Nick fromNick = csCommand.getFromNick();

        switch (type) {
            case "showcommandsfull": commandNameUp = "COMMANDS_LIST"; break;
            case "showcommandsshort": commandNameUp = "COMMANDS_LIST_SHORT"; break;
            default: commandNameUp = "COMMANDS_LIST"; break;
        }
        help = Help.getHelp(commandNameUp, "CService", "command_list");
        level = help.getMetadata().get("level");

        /* If no command level, consider the max level */
        if (level == null || level.isEmpty() == true) level = "NONE_PRIV";

        if (Flags.hasLevelPrivilege(fromNick, level) == false) { helpOutput = List.of(Messages.strErrCommandUnknown); }
        else {
            helpContent = help.getData();
            helpOutput = new ArrayList<>();

            for (String l: helpContent) {
                try {
                    lineLevel = l.split("!", 2)[0].replaceAll("\\s","");
                    lineString = l.split("!", 2)[1];
                }
                catch (IndexOutOfBoundsException e) { lineLevel = "NONE_PRIV"; lineString = ""; }

                if (Flags.hasLevelPrivilege(fromNick, lineLevel) == true) { helpOutput.add(lineString); }
            }
        }

        for (String l: helpOutput) sendReply(fromNick, l);
    }

    private void cServeShowcommands(CSCommand csCommand) {
        getShowcommands(csCommand, "showcommandsfull");
    }

    private void cServeVersion(CSCommand csCommand) {
        sendReply(csCommand.getFromNick(), Const.QBOT_VERSION_STRING);
    }

    /**
     * Sets the channel limit based on the channel autolimit feature
     */
    public void setAutolimit() {
        Channel.getRegChanList().forEach( (chanNode) -> {

            Integer curChanUserCount = chanNode.getUserCount();
            Integer curChanModeLimit;
            Integer chanAutoLimit = chanNode.getcServeAutoLimit();
            Integer newLimit = (chanAutoLimit + curChanUserCount);

            String chanName = chanNode.getName();

            try { curChanModeLimit = Integer.valueOf(chanNode.getMode("l")); }
            catch (Exception e) { curChanModeLimit = 0; }

            if (Flags.isChanAutolimit(chanNode.getcServeFlags()) == true && Flags.isChanJoined(chanNode.getcServeFlags()) == true && newLimit != curChanModeLimit) {
                try {
                    protocol.setMode(myUserNode, chanNode, "+l", String.valueOf(newLimit));
                    log.info("Autolimit: setting limit of " + chanName + " to " + String.valueOf(newLimit));
                }
                catch (Exception e) { log.error(String.format("CService/cServeSetAutolimit: cannot set autolimit for channel %s", chanNode.getName()), e); }
            }

        });
    }

    /**
     * Handles the setting of channel autolimit
     * @param fromNick requester user node
     * @param str command string
     */
    private void cServeAutoLimit(CSCommand csCommand) {

        /* Syntax: AUTOLIMIT <#channel> [<limit>] */

        String channel = "";

        Integer chanAutoLimitInt = 0;

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try {
            chanNode = protocol.getChannelNodeByNameCi(channel);
            if (Flags.isChanSuspended(chanNode.getcServeFlags()) == true) { throw new Exception("Channel suspended."); }
        }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanSusOrNotFound); return; }

        try {  chanAutoLimitInt =  Integer.valueOf(csCommand.getArgs().get(1)); }
        catch (IndexOutOfBoundsException e) {
            Integer chanCurAutoLimit = 0;

            if ( Flags.hasUserStaffPriv(fromNick.getAccount().getFlags()) == true || Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == true ) {
                chanCurAutoLimit = chanNode.getcServeAutoLimit();
                sendReply(fromNick, String.format(Messages.strAutoLimitCurConf, chanNode.getName(), chanCurAutoLimit));
            }
            else { sendReply(fromNick, String.format(Messages.strErrNoAccess)); }
            return;

        }
        if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == true || Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(chanNode)) == true) {

            try {
                database.setChanAutoLimit(chanNode, chanAutoLimitInt);
                chanNode.setcServeAutoLimit(chanAutoLimitInt);

                sendReply(fromNick, Messages.strSuccess);
                sendReply(fromNick, String.format(Messages.strAutoLimitStrSuccessSummary, chanNode.getName(), chanAutoLimitInt));

            }
            catch (Exception e) {
                log.error(String.format("CService/cServeAutoLimit: cannot set autolimit for channel %s", chanNode.getName()), e);
                sendReply(fromNick, Messages.strAutoLimitErrUnknown);
                return;
            }

        }

        /* User has no rights on the chan */
        else { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
    }

    /**
     * SUSPENDCHAN command
     * Syntax: SUSPENDCHAN <channel> <reason>
     * @param fromNick
     * @param str
     */
    private void cServeSuspendChan(CSCommand csCommand) {

        String reason;
        String channel = "";

        Channel chan;

        Nick fromNick = csCommand.getFromNick();

        Integer curChanFlags;
        Integer newChanFlags;

        Timestamp suspendTS = new Timestamp();

        /* Preliminary checks */
        if (fromNick.isAuthed() == false || Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { reason = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) {  sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { chan = protocol.getChannelNodeByNameCi(channel); }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

        if (chan.isRegistered() == false) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

        if (Flags.isChanSuspended(chan.getcServeFlags()) == true) { sendReply(fromNick, Messages.strSuspendChanErrSuspended); return; }

        /* The suspend thing */
        curChanFlags = chan.getcServeFlags();
        newChanFlags = curChanFlags;
        newChanFlags = Flags.setChanSuspended(newChanFlags);
        newChanFlags = Flags.clearChanJoined(newChanFlags);

        try { database.setChanFlags(chan, newChanFlags); }
        catch (Exception e) { log.error("CService/cServeSuspendChan: Cannot suspend channel" + chan.getName()); return; }

        chan.setcServeFlags(newChanFlags);
        chan.incSuspendCount();
        chan.setSuspendMessage(reason);

        protocol.chanPart(myUserNode, chan, Messages.strChanPartSuspend);

        try { protocol.setMode( chan, "-r", null); }
        catch (Exception e) { log.error(String.format("CService/cServeSuspendChan: cannot set mode for chan %s", chan.getName()), e); }

        try {
            database.addSuspendHistory(chan, suspendTS, fromNick.getAccount(), reason);
            chan.setSuspendLastTS(suspendTS);
        }
        catch (Exception e) {
            sendReply(fromNick, String.format(Messages.strSuspendChanErrHistory, chan.getName()));
            log.error(String.format(Messages.strSuspendChanErrHistory, chan.getName()), e);
            return;
        }
        sendReply(fromNick, Messages.strSuccess);
    }

    private void cServeUnSuspendChan(CSCommand csCommand) {

        /* Syntax: UNSUSPENDCHAN <#channel> */

        String channel = "";

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        Integer curChanFlags;
        Integer newChanFlags;

        /* Preliminary checks */
        if (fromNick.isAuthed() == false || Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { chanNode = protocol.getChannelNodeByNameCi(channel); }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

        if (chanNode.isRegistered() == false) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

        if (Flags.isChanSuspended(chanNode.getcServeFlags()) == false) { sendReply(fromNick, Messages.strUnSuspendChanErrSuspended); return; }

        /* The unsuspend thing */
        curChanFlags = chanNode.getcServeFlags();
        newChanFlags = curChanFlags;
        newChanFlags = Flags.clearChanSuspended(newChanFlags);
        newChanFlags = Flags.setChanJoined(newChanFlags);

        try { database.setChanFlags(chanNode, newChanFlags); }
        catch (Exception e) { log.error("CService/cServeUnSuspendChan: Cannot unsuspend channel" + chanNode.getName()); return; }

        chanNode.setcServeFlags(newChanFlags);
        protocol.chanJoin(myUserNode, chanNode);

        //try { protocol.setMode( chanNode, "+r" + chanJoinModes, myUserNode.getNick()); }
        //catch (Exception e) { log.error(String.format("CService/cServeUnsuspendChan: cannot set mode for chan %s", chanNode.getName()), e); }

        performJoinActions(myUserNode, chanNode);

        try {  database.addUnSuspendHistory(chanNode, fromNick.getAccount()); }
        catch (Exception e) {
            sendReply(fromNick, String.format(Messages.strUnSuspendChanErrHistory, chanNode.getName()));
            log.error(String.format(Messages.strUnSuspendChanErrHistory, chanNode.getName()), e);
            return;
        }

        sendReply(fromNick, Messages.strSuccess);
    }

    /**
     * SUSPENDUSER command
     * Syntax: SUSPENDUSER <nick|#user> <reason>
     *
     * Suspends an user based on its username or one of his logged nicks. Deauth all logins.
     * @param fromNick
     * @param str
     */
    private void cServeSuspendUser(CSCommand csCommand) {

        String reason;
        String user = "";

        UserAccount userAccount;
        Nick userNode;
        Nick fromNick = csCommand.getFromNick();

        Integer curFlags;
        Integer newFlags;

        Timestamp suspendTS = new Timestamp();

        Set<Nick>    loggedUserNodes  = new HashSet<>();

        /* Preliminary checks */
        if (fromNick.isAuthed() == false || Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) {
            sendReply(fromNick, Messages.strErrCommandUnknown);
            return;
        }

        try { user = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) {
            sendReply(fromNick, Messages.strErrCommandSyntax);
            return;
        }

        try { reason = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) {
            sendReply(fromNick, Messages.strErrCommandSyntax);
            return;
        }

        if (user.startsWith(Const.USER_ACCOUNT_PREFIX) == true) {
            try { userAccount = UserAccount.getUserByNameCi(user.replaceFirst(Const.USER_ACCOUNT_PREFIX, "")); }
            catch (Exception e) {
                sendReply(fromNick, Messages.strErrUserNonReg);
                return;
            }
        }
        else {
            try { userNode = Nick.getUserByNickCi(user); }
            catch (Exception e) {
                sendReply(fromNick, Messages.strErrNickNotFound);
                return;
            }

            try {
                userAccount = userNode.getAccount();
            }
            catch (Exception e) {
                sendReply(fromNick, Messages.strErrNickNotAuthed);
                return;
            }
        }

        if (Flags.isUserSuspended(userAccount.getFlags()) == true) {
            sendReply(fromNick, Messages.strSuspendUserErrSuspended);
            return;
        }


        /* The suspend thing */
        curFlags = userAccount.getFlags();
        newFlags = curFlags;
        newFlags = Flags.setUserSuspended(newFlags);

        try { database.setUserFlags(userAccount, newFlags); }
        catch (Exception e) {
            log.error("CService/cServeSuspendUser: Cannot suspend user" + userAccount.getName(), e);
            return;
        }
        userAccount.setFlags(newFlags);
        userAccount.incSuspendCount();
        userAccount.setSuspendMessage(reason);

        try {
            database.addSuspendHistory(userAccount, suspendTS, fromNick.getAccount(), reason);
            userAccount.setSuspendLastTS(suspendTS);
        }
        catch (Exception e) {
            sendReply(fromNick, String.format(Messages.strSuspendUserErrHistory, userAccount.getName()));
            log.error(String.format(Messages.strSuspendUserErrHistory, userAccount.getName()), e);
            return;
        }

        try {
            /* Deauth all the nicks associated */
            userAccount.getUserLogins().forEach( (usernode) -> {
                loggedUserNodes.add(usernode);
            });
            for (Nick loggedUserNode : loggedUserNodes) {
                try {
                    this.logoutUser(loggedUserNode, Const.DEAUTH_TYPE_SUSPEND);
                    sendReply(loggedUserNode, String.format(Messages.strSuspendUserDeAuth, reason));
                }
                catch (Exception e) {
                    log.error("CService/cServeSuspenduser: could not deauthenticate: " + loggedUserNode.getNick() + " from account " + userAccount.getName(), e);
                }
            }
        }
        catch (Exception e) {
            sendReply(fromNick, Messages.strSuspendUserErrUnknown);
            log.error("CService/cServeSuspendUser: Suspenduser: could not deauth user: " + userAccount.getName() + ".", e);
            return;
        }

        sendReply(fromNick, Messages.strSuccess);
    }

    private void cServeUnSuspendUser(CSCommand csCommand) {

        /* Syntax: UNSUSPENDUSER <#username> */

        String user = "";

        UserAccount userAccount;

        Integer curFlags;
        Integer newFlags;

        Nick fromNick = csCommand.getFromNick();


        /* Preliminary checks */
        if (fromNick.isAuthed() == false || Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { user = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        if (user.startsWith(Const.USER_ACCOUNT_PREFIX) == true) {
            try { userAccount = UserAccount.getUserByNameCi(user.replaceFirst(Const.USER_ACCOUNT_PREFIX, "")); }
            catch (Exception e) { sendReply(fromNick, Messages.strErrUserNonReg); return; }
        }
        else { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        if (Flags.isUserSuspended(userAccount.getFlags()) == false) { sendReply(fromNick, Messages.strUnSuspendUserErrSuspended); return; }

        /* The suspend thing */
        curFlags = userAccount.getFlags();
        newFlags = curFlags;
        newFlags = Flags.clearUserSuspended(newFlags);

        try { database.setUserFlags(userAccount, newFlags); }
        catch (Exception e) {
            log.error("CService/cServeUnSuspendUser: Cannot unsuspend user" + userAccount.getName());
            return;
        }
        userAccount.setFlags(newFlags);

        try { database.addUnSuspendHistory(userAccount, fromNick.getAccount()); }
        catch (Exception e) {
            sendReply(fromNick, String.format(Messages.strUnSuspendUserErrHistory, userAccount.getName()));
            log.error(String.format(Messages.strUnSuspendUserErrHistory, userAccount.getName()), e);
            return;
        }

        sendReply(fromNick, Messages.strSuccess);
    }

    /**
     * SUSPENDHISTORY command
     * Syntax: SUSPENDHISTORY <user|chan> <user|#chan>
     *
     * Lists the suspension history of an user or a channel
     * @param fromNick
     * @param str
     */
    private void cServeSuspendHistory(CSCommand csCommand) {

        /* Syntax: SUSPENDHISTORY <USER|CHAN> <username|#chan> */

        String typeStr;
        String target = "";
        String header;
        String suspendDateTxt;
        String unsuspendDateTxt;

        Integer typeInt;
        Integer historySize = 0;
        Integer entId = -1;

        UserAccount user;
        Channel chan;
        Nick fromNick = csCommand.getFromNick();

        Date suspendDate;
        Date unsuspendDate;

        Map<String, Object> suspendHistory;

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { typeStr = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { target = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        switch(typeStr) {
            case "user":
                typeInt = Const.ENTITY_USERACCOUNT;
                if (target.isEmpty() == false) {
                    try { user = UserAccount.getUserByNameCi(target); entId = user.getId(); }
                    catch (ItemNotFoundException e) { sendReply(fromNick, Messages.strErrUserNotFound); return; }
                }
                break;

            case "chan":
                typeInt = Const.ENTITY_CHANNEL;
                if (target.isEmpty() == false) {
                    try { chan = Channel.getRegChanByNameCi(target); entId = chan.getcServeId(); }
                    catch (ItemNotFoundException e) { sendReply(fromNick, Messages.strErrChanNonReg); return; }
                }
                break;

            default: sendReply(fromNick, Messages.strErrCommandSyntax); return;
        }

        try { suspendHistory = database.getSuspendHistory(typeInt, entId); }
        catch (Exception e) {
            log.error(String.format("CService::cServeSuspendHistory: could not retrieve suspend history using parameter TYPE=%s(%s), ENTID=%s and ENTTARGET=%s", typeStr, typeInt, entId, target), e); return;
        }

        @SuppressWarnings("unchecked") List<Timestamp> suspendTS      = (List<Timestamp>) suspendHistory.get("suspendTS");
        @SuppressWarnings("unchecked") List<Timestamp> unsuspendTS    = (List<Timestamp>) suspendHistory.get("unsuspendTS");
        @SuppressWarnings("unchecked") List<Integer> suspendById      = (List<Integer>) suspendHistory.get("suspendById");
        @SuppressWarnings("unchecked") List<Integer> unsuspendById    = (List<Integer>) suspendHistory.get("unsuspendById");
        @SuppressWarnings("unchecked") List<String> suspendByName     = (List<String>) suspendHistory.get("suspendByName");
        @SuppressWarnings("unchecked") List<String> unsuspendByName   = (List<String>) suspendHistory.get("unsuspendByName");
        @SuppressWarnings("unchecked") List<String> suspendReason     = (List<String>) suspendHistory.get("suspendReason");

        historySize = suspendTS.size();

        header = String.format(Messages.strSuspendHisHeaderFormat,
            Messages.strSuspendHisHeadColID, Messages.strSuspendHisHeadColDate1, Messages.strSuspendHisHeadColDate2, Messages.strSuspendHisHeadColBy1, Messages.strSuspendHisHeadColBy2, Messages.strSuspendHisHeadColReason);

        sendReply(fromNick, header);
        for(int i=0; i<historySize; i++) {

            if (suspendTS.get(i).getValue().equals(0L) == false) {
                suspendDate = new Date(suspendTS.get(i).getValue()*1000L);
                suspendDateTxt = jdf.format(suspendDate);

            }
            else { suspendDateTxt = "No suspend date"; }

            if (unsuspendTS.get(i).getValue().equals(0L) == false) {
                unsuspendDate = new Date(unsuspendTS.get(i).getValue()*1000L);
                unsuspendDateTxt = jdf.format(unsuspendDate);

            }
            else { unsuspendDateTxt = "No unsuspend date"; }

            sendReply(fromNick, String.format(Messages.strSuspendHisHeaderFormat,
                  i, suspendDateTxt, unsuspendDateTxt, suspendByName.get(i) + " (" + suspendById.get(i) + ")", unsuspendByName.get(i) + " (" + unsuspendById.get(i) + ")", suspendReason.get(i)));
        }


    }

    /**
     * MODELOCK command
     * Syntax: MODELOCK <#chan> [modes]
     *
     * Defines modes to lock on the channel
     * @param fromNick
     * @param str Contains the string MODELOCK <#chan> [modes]
     */
    private void cServeChanModeLockSet(CSCommand csCommand) {

        /* Syntax: CHANMODE <#channel> [<modes>] */

        String channel = "";
        String modes   = "";

        Map<String, Map<String, String>> parsedModes;
        Set<String> parsedModesTmp;

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax);  return; }

        try {
            chanNode = protocol.getChannelNodeByNameCi(channel);
            if (Flags.isChanSuspended(chanNode.getcServeFlags()) == true) {
                throw new ItemSuspendedException("Channel suspended");
            }
        }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanSusOrNotFound); return; }

        if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, Messages.strErrNoAccess); return; }

        try { modes = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, String.format(Messages.strMlockModeList, chanNode.getName(), Mode.modeMapToString(chanNode.getModes()), chanNode.getcServeMLockModes())); return; }

        if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, Messages.strErrNoAccess); return; }

        if (modes.equals("none") == true) { cServeChanModeLockClear(csCommand); return; }

        try { parsedModes = protocol.parseChanModes(modes); }
        catch (ParseException e) {  sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        /* Scan for special modes to remove */
        parsedModesTmp = new HashSet<>(parsedModes.get("chanModes").keySet());

        for(String mode: parsedModesTmp) {
            switch(mode) {
                /* Lists: not applicable */
                case "b", "+b", "-b":
                case "e", "+e", "-e":
                case "I", "+I", "-I": parsedModes.get("chanModes").remove(mode); break;

                /* Mode +r reserved to services => nobody allowed to force it */
                case "r", "+r", "-r": parsedModes.get("chanModes").remove(mode); break;

                /* Modes +Zd only settable by server => nobody allowed to force it */
                case "d", "+d", "-d":
                case "Z", "+Z", "-Z": parsedModes.get("chanModes").remove(mode); break;

                /* Modes +OP only settable by IRCop => only IRCop allowed to force it */
                case "O", "+O", "-O":
                case "P", "+P", "-P": if (fromNick.isOper() == false) { parsedModes.get("chanModes").remove(mode); } break;
            }
        }

        String stringizedModes = Mode.modeMapToString2(parsedModes, false);

        String lockedModes;
        String lockedParams = "";

        try { lockedModes = stringizedModes.split(" ", 2)[0]; }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, "error"); return; }

        try { lockedParams = stringizedModes.split(" ", 2)[1]; }
        catch (IndexOutOfBoundsException e) {  }

        dispatcher.setChanMlock(chanNode, stringizedModes);

        try { protocol.setMode(myUserNode, chanNode, lockedModes, lockedParams); }
        catch(IndexOutOfBoundsException e) { }
        protocol.sendMlock(myServerNode, chanNode, Mode.modeMapToString2(parsedModes, true));

        sendReply(fromNick, String.format(Messages.strMlockModeSet, chanNode.getName(),stringizedModes));

    }

    private void cServeChanModeLockClear(CSCommand csCommand) {

        /* Syntax: CHANMODELOCKCLEAR <#channel> */

        String channel = "";

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax);  return; }

        try {
            chanNode = protocol.getChannelNodeByNameCi(channel);
            if (Flags.isChanSuspended(chanNode.getcServeFlags()) == true) {
                throw new ItemSuspendedException("Channel suspended");
            }
        }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanSusOrNotFound); return; }

        if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, Messages.strErrNoAccess); return; }

        dispatcher.clearChanMlock(chanNode);
        protocol.sendMlock(myServerNode, chanNode, "");

        sendReply(fromNick, String.format(Messages.strMlockModeClear, chanNode.getName()));
        sendReply(fromNick, "Done.");

    }

    private void cServeDeModeAll(CSCommand csCommand, String type) {

        String channel = "";
        String applyMode;

        Channel chanNode;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { channel = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax);  return; }

        try {
            chanNode = protocol.getChannelNodeByNameCi(channel);
            if (Flags.isChanSuspended(chanNode.getcServeFlags()) == true) throw new ItemSuspendedException("Channel suspended");
        }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanSusOrNotFound); return; }

        switch (type) {
            case "owner":
                if (protocol.hasFeature("chanOwner") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOwnerPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                applyMode = "+" + protocol.getChanMode("owner");
            break;

            case "admin":
                if (protocol.hasFeature("chanAdmin") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                applyMode = "+" + protocol.getChanMode("admin");
            break;

            case "op":
                if (protocol.hasFeature("chanOp") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                applyMode = "+" + protocol.getChanMode("op");
            break;

            case "halfop":
                if (protocol.hasFeature("chanHalfOp") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                applyMode = "+" + protocol.getChanMode("halfop");
            break;

            case "voice":
                if (protocol.hasFeature("chanVoice") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                applyMode = "+" + protocol.getChanMode("voice");
            break;

            case "deowner":
                if (protocol.hasFeature("chanOwner") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOwnerPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                applyMode = "-" + protocol.getChanMode("owner");
            break;

            case "deadmin":
                if (protocol.hasFeature("chanAdmin") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                applyMode = "-" + protocol.getChanMode("admin");
            break;

            case "deop":
                if (protocol.hasFeature("chanOp") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                applyMode = "-" + protocol.getChanMode("op");
            break;

            case "dehalfop":
                if (protocol.hasFeature("chanHalfOp") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                applyMode = "-" + protocol.getChanMode("halfop");
            break;

            case "devoice":
                if (protocol.hasFeature("chanVoice") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                applyMode = "-" + protocol.getChanMode("voice");
            break;

            default: return;
        }


        chanNode.getUsers().forEach( (nick, m) -> {
            if (nick == myUserNode) return;
            try { protocol.setMode(myUserNode, chanNode, applyMode, nick.getNick()); }
            catch (Exception e) { log.error(String.format("CService::cServeDeModeAll: channel %s: could not '%s' nick %s", chanNode.getName(), applyMode, nick.getNick())); }
        });














        sendReply(fromNick, "Done.");

    }

    private void cServeGiveChanMode(CSCommand csc, String type) {

        //String[] command = str.split(" ", 3); /* index=0 => command name, index=1 => channel name, index=2 => nick list */
        Set<Nick> nickList = new HashSet<>();
        String channel = "";
        String mode = "";
        Nick userNode;
        Channel chanNode;
        Nick fromNick = csc.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown);  return; }

        try { channel = csc.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) {  sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { chanNode = protocol.getChannelNodeByNameCi(channel); }
        catch (ItemNotFoundException e) { sendReply(fromNick, Messages.strErrChanNonReg); return; }

        switch (type) {
            case "owner":
                if (protocol.hasFeature("chanOwner") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOwnerPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                mode = "-" + protocol.getChanMode("owner");
            break;

            case "admin":
                if (protocol.hasFeature("chanAdmin") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                mode = "-" + protocol.getChanMode("admin");
            break;

            case "op":
                if (protocol.hasFeature("chanOp") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                mode = "-" + protocol.getChanMode("op");
            break;

            case "halfop":
                if (protocol.hasFeature("chanHalfOp") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                mode = "-" + protocol.getChanMode("halfop");
            break;

            case "voice":
                if (protocol.hasFeature("chanVoice") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                mode = "-" + protocol.getChanMode("voice");
            break;

            case "deowner":
                if (protocol.hasFeature("chanOwner") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOwnerPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                mode = "-" + protocol.getChanMode("owner");
            break;

            case "deadmin":
                if (protocol.hasFeature("chanAdmin") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                mode = "-" + protocol.getChanMode("admin");
            break;

            case "deop":
                if (protocol.hasFeature("chanOp") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                mode = "-" + protocol.getChanMode("op");
            break;

            case "dehalfop":
                if (protocol.hasFeature("chanHalfOp") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                mode = "-" + protocol.getChanMode("halfop");
            break;

            case "devoice":
                if (protocol.hasFeature("chanVoice") == false) { sendReply(fromNick, Messages.strErrModeNotSupported); return;  }
                if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(chanNode)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }
                mode = "-" + protocol.getChanMode("voice");
            break;

            default:
        }

        if (csc.getArgs().size() <= 1) {
            nickList.add(csc.getFromNick());
        }
        else {
            for (String s: csc.getArgs().subList(1, csc.getArgs().size())) {
                try { userNode = Nick.getUserByNickCi(s); }
                catch (NickNotFoundException e) { continue; }
                nickList.add(userNode);
            }
        }

        for (Nick nick: nickList) {
            /* Check if we are not doing on the bot */
            if (nick == myUserNode) continue;

            protocol.setMode(myUserNode, chanNode, mode, nick.getNick());
        }

        sendReply(fromNick, Messages.strSuccess);

    }

    private void cServeNewPass(CSCommand csCommand) {

        /* Syntax: NEWPASS <password> */

        UserAccount userAccount = null;
        String newPass = "";
        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        userAccount = fromNick.getAccount();

        try { newPass = csCommand.getArgs().get(0); }
        catch (Exception e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        if (isPassComplex(newPass) == false) {
            sendReply(fromNick, String.format(Messages.strHelloErrTooEasy, config.getCServiceAccountMinPassLength(), config.getCServiceAccountMaxPassLength()));
            return;
        }

        String salt = Argon2Hash.generateSalt();
        Argon2Hash pwGen = new Argon2Hash(salt);

        String hashedPass = pwGen.generateHash(newPass);

        try { database.updateUserPassword(userAccount, hashedPass, salt); }
        catch (Exception e) {
            sendReply(fromNick, Messages.strNewPassErrUpdate);
            log.error(String.format("error updating the password in the database for user %s", userAccount.getName()));
            return;
        }
        sendReply(fromNick, Messages.strSuccess);
    }

    private void cServeNickAlias(CSCommand csCommand) {

        /* Syntax: NICKALIAS [ [ +alias | -alias ] | [ #username | nickname ] ] */

        UserAccount userAccount;
        String firstArg;
        String alias;

        Nick fromNick;

        NickAlias nickAlias;

        fromNick = csCommand.getFromNick();

        if (csCommand.isFromNickAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        /* No arguments */
        if (csCommand.getArgs().size() == 0) { userAccount = fromNick.getAccount(); showNickAlias(fromNick, userAccount); return; }

        /* 1st argument does not start with +- => lookup an user account */
        int startIndex = 0;
        firstArg = csCommand.getArgs().get(0);
        if (firstArg.startsWith("+") == false && firstArg.startsWith("-") == false) {

            /* User has no oper privilege => deny access to someone else nick aliases */
            if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

            /* Lookup username */
            try { userAccount = UserAccount.getUserByNameOrNickCi(firstArg); }
            catch (NickNotFoundException e) { sendReply(fromNick, Messages.strErrNickNotFound); return; }
            catch (AccountNotFoundException e) { sendReply(fromNick, Messages.strErrUserNotFound); return; }
            catch (UserNoAuthException e) { sendReply(fromNick, Messages.strErrNickNotAuthed); return; }
            startIndex = 1;
        }
        else userAccount = fromNick.getAccount();

        if (csCommand.getArgs().size() == 1 && firstArg.startsWith("+") == false && firstArg.startsWith("-") == false) { showNickAlias(fromNick, userAccount); return; }

        while (startIndex < csCommand.getArgs().size()) {
            alias = csCommand.getArgs().get(startIndex);

            nickAlias = NickAlias.create(alias.replaceFirst("^[\\+|\\-]", ""), new Timestamp(), new Timestamp(), userAccount);

            switch (alias.charAt(0)) {
                case '+': dispatcher.addNickAlias(nickAlias); sendReply(fromNick, "Plus " + alias.replaceFirst("^\\+", "")); break;
                case '-': dispatcher.removeNickAlias(nickAlias); sendReply(fromNick, "Minus " + alias.replaceFirst("^\\-", "")); break;
                default: sendReply(fromNick, Messages.strErrCommandSyntax); return;
            }

            startIndex++;
        }
        sendReply(fromNick, Messages.strSuccess);
    }

    private void showNickAlias(Nick fromNick, UserAccount account) {
        Set<NickAlias> nickAliases = new TreeSet<>(account.getNickAlias().values());

        sendReply(fromNick, "List of aliases for account " + account.getName());
        for (NickAlias n: nickAliases) sendReply(fromNick, String.format("alias = %s, created = %s, lastused = %s", n.getAlias(), n.getCreatedTS(), n.getLastUsedTS()));

    }

    private void cServeSetUserPass(CSCommand csCommand) {

        /* Syntax: SETUSERPASS <#account> <new password> */

        UserAccount userAccount = null;
        String newPass = "";

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserAdminPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return;  }

        try { userAccount = UserAccount.getUserByNameCi(csCommand.getArgs().get(0).replaceFirst(Const.USER_ACCOUNT_PREFIX, "")); }
        catch (ItemNotFoundException e)          { sendReply(fromNick, Messages.strErrUserNonReg); return; }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { newPass = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        if (isPassComplex(newPass) == false) { sendReply(fromNick, String.format(Messages.strHelloErrTooEasy, config.getCServiceAccountMinPassLength(), config.getCServiceAccountMaxPassLength())); return; }

        String salt = Argon2Hash.generateSalt();
        Argon2Hash pwGen = new Argon2Hash(salt);

        String hashedPass = pwGen.generateHash(newPass);

        try { database.updateUserPassword(userAccount, hashedPass, salt); }
        catch (Exception e) {
            sendReply(fromNick, Messages.strNewPassErrUpdate);
            log.error(String.format("error updating the password in the database for user %s", userAccount.getName()));
            return;
        }

        sendReply(fromNick, String.format(Messages.strNewPassSucOtherUser, userAccount.getName()));
        sendReply(fromNick, Messages.strSuccess);
    }

    private void cServeRegUserlist(CSCommand csCommand) {

        /* REGUSERLIST <pattern> */

        String pattern = "";

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { pattern = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        //final String patternRegex = UserMask.wildcardToRegex(pattern.toLowerCase());
        final Wildcard patternRegex = Wildcard.create(pattern);

        Set<UserAccount> sortedUserList = new TreeSet<>();
        UserAccount.getUserList().forEach((u) -> {
            //if (u.getName().toLowerCase().matches(patternRegex) == true) sortedUserList.add(u);
            //else if (String.valueOf(u.getId()).matches(patternRegex) == true) sortedUserList.add(u);
            if (patternRegex.matches(u.getName().toLowerCase()) == true) sortedUserList.add(u);
            else if (patternRegex.matches(String.valueOf(u.getId())) == true) sortedUserList.add(u);
        });

        sendReply(fromNick, Messages.strUserlistHeader);
        for (UserAccount account: sortedUserList) { sendReply(fromNick, String.format(Messages.strUserlistEntry, account.getId(), account.getName())); }
        sendReply(fromNick, Messages.strEndOfList);
    }

    private void cServeRegChanlist(CSCommand csCommand) {
        String pattern = "";

        Nick fromNick = csCommand.getFromNick();

        /*
         * Information to display on channel quick list:
         * o ban status (channel glined or not)
         * o protection status
         * o
         */
        // Banned, Orphan, Protected, Suspended, -:N/A

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { pattern = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        //final String patternRegex = UserMask.wildcardToRegex(pattern.toLowerCase());
        final Wildcard patternRegex = Wildcard.create(pattern);

        Set<Channel> sortedChanList = new TreeSet<>();
        Channel.getRegChanList().forEach((chan) -> {
            //if (chan.getName().replaceFirst("#", "").toLowerCase().matches(patternRegex) == true) sortedChanList.add(chan);
            //else if (String.valueOf(chan.getcServeId()).matches(patternRegex) == true) sortedChanList.add(chan);
            if (patternRegex.matches(chan.getName().replaceFirst("#", "").toLowerCase()) == true) sortedChanList.add(chan);
            else if (patternRegex.matches(String.valueOf(chan.getcServeId())) == true) sortedChanList.add(chan);
        });

        sendReply(fromNick, Messages.strChanlistHeader);
        sendReply(fromNick, Messages.strChanlistHeader2);
        for (Channel chan: sortedChanList) { sendReply(fromNick, String.format(Messages.strChanlistEntry, "----", chan.getcServeId(), chan.getName())); }
        sendReply(fromNick, Messages.strEndOfList);

    }

    private void cServeNickHistory(CSCommand csCommand) {

        /* NICKHISTORY nick */

        String nick;

        Nick user;
        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserOperPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { nick = csCommand.getArgs().get(0); user = Nick.getUserByNickCi(nick); }
        catch (NickNotFoundException e) { sendReply(fromNick, Messages.strErrNickNotFound); return; }

        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        var wrapper = new Object(){ Date date; String strDate; };

        sendReply(fromNick, String.format(Messages.strNickHistoryHeader, nick));

        user.getNickHistory().forEach(
            (ts, s) -> {
                wrapper.date = new Date(ts.toLong()*1000L);
                wrapper.strDate = jdf.format(wrapper.date);
                sendReply(fromNick, String.format(Messages.strNickHistoryLine, wrapper.strDate, s)); }
        );

        sendReply(fromNick, Messages.strEndOfList);
    }

    private void cServeChanInfo(CSCommand csCommand) {
        /* CHANINFO #channel */

        Nick fromNick = csCommand.getFromNick();

        ChanInfo chaninfo = (Channel channel) -> {

            int counterTotal = 0;

            List<String> strReply = new ArrayList<>();

            Map<Nick, Set<String>>  chanUserList;
            Map<String, String>     chanModes;

            String regId              = Messages.strChanInfoCServeNotRegistered;
            String strChanRegTS       = "";
            String strPassword        = "";
            String strFloodProfile    = "";
            String strFloodParams     = "";
            String strHistoryParams   = "";
            String strLinkChan        = "";
            String strUserLimit       = "";
            String bModeLongTmp       = "";

            StringJoiner bufferModeLong;

            Date dateRegTS;

            if (channel.isRegistered() == true) {
                regId = channel.getcServeId().toString();
                strReply.add(Messages.strChanInfoCServeHeader);
                strReply.add(String.format(Messages.strChanInfoContentRegistered, Messages.strYes));
                strReply.add(String.format(Messages.strChanInfoContentChanId, regId));
            }

            dateRegTS = new Date((channel.getRegistrationTS().getValue())*1000L);
            strChanRegTS = jdf.format(dateRegTS);

            if (channel.isRegistered() == true && (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(channel)) || Flags.hasUserOperPriv(fromNick.getAccount().getFlags())) == true) {
                strReply.add(String.format(Messages.strChanInfoContentChanFlags, Flags.flagsIntToChars("chanflags", channel.getcServeFlags()), Flags.flagsIntToString("chanflags", channel.getcServeFlags())));

                strReply.add(String.format(Messages.strChanInfoContentChanCreated, strChanRegTS));

                if (channel.getcServeWelcomeMsg().isEmpty() == false) strReply.add(String.format(Messages.strChanInfoContentChanWelcome, channel.getcServeWelcomeMsg()));
                if (channel.getCServeRegisteredTopic().getText().isEmpty() == false) strReply.add(String.format(Messages.strChanInfoContentChanTopic, channel.getCServeRegisteredTopic()));
                strReply.add(String.format(Messages.strChanInfoContentChanBanTime, channel.getcServeBanTime()));
                strReply.add(String.format(Messages.strChanInfoContentChanAutoLimit, channel.getcServeAutoLimit()));

                if (channel.getcServeMLockModes().isEmpty() == false) strReply.add(String.format(Messages.strChanInfoContentLockedModes, channel.getcServeMLockModes()));

                /* Display beI present in database */
                if (channel.getcServeBanList().isEmpty() == false) {
                    strReply.add(String.format(Messages.strChanInfoContentChanBanTitle, channel.getcServeBanList().size()));
                    /* Don't display current beI list (can be accessed with BAN/EX/INVLIST) */
                    //for (UserMask line: channel.getcServeBanList().keySet()) strReply.add(String.format(Messages.strChanInfoContentSublistItem, line));
                }

                if (channel.getcServeExceptList().isEmpty() == false) {
                    strReply.add(String.format(Messages.strChanInfoContentChanExcTitle, channel.getcServeExceptList().size()));
                    /* Don't display current beI list (can be accessed with BAN/EX/INVLIST) */
                    //for (UserMask line: channel.getcServeExceptList().keySet()) strReply.add(String.format(Messages.strChanInfoContentSublistItem, line));
                }

                if (channel.getcServeInviteList().isEmpty() == false) {
                    strReply.add(String.format(Messages.strChanInfoContentChanInvTitle, channel.getcServeInviteList().size()));
                    /* Don't display current beI list (can be accessed with BAN/EX/INVLIST) */
                    //for (UserMask line: channel.getcServeInviteList().keySet()) strReply.add(String.format(Messages.strChanInfoContentSublistItem, line));
                }

            }

            /* Network section */
            if (Flags.hasUserStaffPriv(fromNick.getAccount().getFlags()) == true && Channel.getChanList().contains(channel) == true) {
                chanModes = channel.getModes();

                /* Gets parameters for the modes that allow parameters */
                if (chanModes.containsKey("k")) strPassword        = chanModes.get("k");
                if (chanModes.containsKey("F")) strFloodProfile    = chanModes.get("F");
                if (chanModes.containsKey("f")) strFloodParams     = chanModes.get("f");
                if (chanModes.containsKey("H")) strHistoryParams   = chanModes.get("H");
                if (chanModes.containsKey("L")) strLinkChan        = chanModes.get("L");
                if (chanModes.containsKey("l")) strUserLimit       = chanModes.get("l");

                strReply.add(Messages.strChanInfoNetworkHeader);
                strReply.add(String.format(Messages.strChanInfoContentCurTimestamp, strChanRegTS));

                bufferModeLong = new StringJoiner(" ");
                for (String m: chanModes.keySet()) {
                    bModeLongTmp = protocol.chanModeToTxt(m);
                    if (chanModes.get(m).isEmpty() == false) bModeLongTmp  = String.join("=", bModeLongTmp, chanModes.get(m));
                    bufferModeLong.add(bModeLongTmp);
                }

                strReply.add(String.format(Messages.strChanInfoContentCurModes, Mode.modeMapToString(chanModes)));
                strReply.add(String.format(Messages.strChanInfoContentCurModesLong, bufferModeLong));
                if (channel.getTopic().getText().toString().isEmpty() == false) strReply.add(String.format(Messages.strChanInfoContentCurTopic, channel.getTopic().getText()));

                var w = new Object(){ int counterModeq=0; int counterModea=0; int counterModeo=0; int counterModeh=0; int counterModev=0; };
                chanUserList = channel.getUsers();
                chanUserList.forEach(
                    (Nick n, Set<String> modes) -> {
                        for (String m: modes) {
                            switch (m) {
                                case "q": w.counterModeq++; break;
                                case "a": w.counterModea++; break;
                                case "o": w.counterModeo++; break;
                                case "h": w.counterModeh++; break;
                                case "v": w.counterModev++; break;
                                default: break;
                            }
                        }
                    }
                );

                counterTotal = chanUserList.size();

                if (counterTotal > 0) strReply.add(String.format(Messages.strChanInfoContentCounter, counterTotal, w.counterModeq, w.counterModea, w.counterModeo, w.counterModeh, w.counterModev));

                if (strPassword.isEmpty()      == false) strReply.add(String.format(Messages.strChanInfoContentCurKey, strPassword));
                if (strUserLimit.isEmpty()     == false) strReply.add(String.format(Messages.strChanInfoContentCurUserLimit, strUserLimit));
                if (strFloodProfile.isEmpty()  == false) strReply.add(String.format(Messages.strChanInfoContentCurFloodProf, strFloodProfile));
                if (strFloodParams.isEmpty()   == false) strReply.add(String.format(Messages.strChanInfoContentCurFloodParam, strFloodParams));
                if (strHistoryParams.isEmpty() == false) strReply.add(String.format(Messages.strChanInfoContentCurHistoParam, strHistoryParams));
                if (strLinkChan.isEmpty()      == false) strReply.add(String.format(Messages.strChanInfoContentCurChanLink, strLinkChan));

                if (channel.getBanList().isEmpty() == false) {
                    strReply.add(String.format(Messages.strChanInfoContentCurBanTitle, channel.getBanList().size()));
                    for (Bei line: channel.getBanList()) strReply.add(String.format(Messages.strChanInfoContentSublistItem, line));
                }

                if (channel.getExceptList().isEmpty() == false) {
                    strReply.add(String.format(Messages.strChanInfoContentCurExcTitle, channel.getExceptList().size()));
                    for (Bei line: channel.getExceptList()) strReply.add(String.format(Messages.strChanInfoContentSublistItem, line));
                }

                if (channel.getInviteList().isEmpty() == false) {
                    strReply.add(String.format(Messages.strChanInfoContentCurInvTitle, channel.getInviteList().size()));
                    for (Bei line: channel.getInviteList()) strReply.add(String.format(Messages.strChanInfoContentSublistItem, line));
                }

            }

            /* Outputs the channel information to the user */
            for (String strLine: strReply) { sendReply(fromNick, strLine); }

        };

        String chanStr;

        Channel channel;

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { chanStr = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { channel = Channel.getChanByNameCi(chanStr); }
        catch (ItemNotFoundException e) {
            try { channel = Channel.getRegChanByNameCi(chanStr); }
            catch (ItemNotFoundException f) { sendReply(fromNick, Messages.strErrChanNonExist); return; }
        }

        sendReply(fromNick, String.format(Messages.strChanInfoHeader, channel));
        chaninfo.displayChanInfo(channel);
        sendReply(fromNick, Messages.strEndOfList);

    }

    private void addChanBei(Nick fromNick, int type, Channel channel, Bei bei, long duration, String reason) {

        String mode = "";

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown);  return; }

        switch(type) {
            case    Const.CHANBEI_BANS: mode = "+" + protocol.getChanMode("banned"); if (reason.isEmpty() == true) { reason = "Banned."; } break;
            case Const.CHANBEI_EXCEPTS: mode = "+" + protocol.getChanMode("except"); if (reason.isEmpty() == true) { reason = "Excepted."; } break;
            case Const.CHANBEI_INVITES: mode = "+" + protocol.getChanMode("invex");  if (reason.isEmpty() == true) { reason = "Invited."; } break;
        }

        dispatcher.addChanBei(type, channel, bei, fromNick.getAccount(), reason, duration);

        try { protocol.setMode(myUserNode, channel, mode, bei.getString()); }
        catch (Exception e) { sendReply(fromNick, Messages.strErrCmdExec); return; }

        sendReply(fromNick, Messages.strSuccess);
    }

    private void cServePermBei(CSCommand csc, int type) {

        /* Syntax: PERMBAN <#channel> <nick!user@host> <reason> */

        String chanName = "";
        String mask     = "";
        String reason   = "";

        Nick fromNick;

        Channel channel;

        Bei userMask;

        List<String> reasonList = csc.getArgs().subList(3, csc.getArgs().size());

        StringJoiner sjReason = new StringJoiner(" ");

        fromNick = csc.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { chanName = csc.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try {
            channel = protocol.getChannelNodeByNameCi(chanName);
            if (Flags.isChanSuspended(channel.getcServeFlags()) == true) throw new ItemSuspendedException("Channel suspended");
        }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanSusOrNotFound); return; }

        if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(channel)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }

        try { mask = csc.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        for (String s: reasonList) sjReason.add(s);
        if (reasonList.size() != 0) reason = sjReason.toString();

        try { userMask = Bei.create(mask); }
        catch (InvalidFormatException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        addChanBei(fromNick, type, channel, userMask, 0L, reason);

    }

    private void cServeTempBei(CSCommand csc, int type) {

        /* Syntax: TEMPBAN <#channel> <nick!user@host> <duration> <reason> */

        String chanName    = "";
        String mask        = "";
        String reason      = "";
        String durationStr = "";

        List<String> reasonList = csc.getArgs().subList(3, csc.getArgs().size());

        StringJoiner sjReason = new StringJoiner(" ");

        char durationUnit  = 'm';

        long durationVal;

        Channel channel;

        Bei userMask;

        Nick fromNick = csc.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { chanName = csc.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try {
            channel = protocol.getChannelNodeByNameCi(chanName);
            if (Flags.isChanSuspended(channel.getcServeFlags()) == true) throw new ItemSuspendedException("Channel suspended");
        }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanSusOrNotFound); return; }

        if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(channel)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }

        try { mask = csc.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { durationStr = csc.getArgs().get(2); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        /*
         * Nm, Nh, Nd, Nw, NM, NY
         */

        if (Character.isDigit(durationStr.charAt(0)) == false) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }
        for(int i=0; i<durationStr.length(); i++) {
            if (Character.isAlphabetic(durationStr.charAt(i)) == true) {
                durationUnit = durationStr.charAt(i);
                break;
            }
        }
        durationVal = Long.parseLong(durationStr.split(String.valueOf(durationUnit))[0]);
        switch (durationUnit) {
            case 'm': durationVal =        60*durationVal; break;
            case 'h': durationVal =     60*60*durationVal; break;
            case 'd': durationVal =     86400*durationVal; break;
            case 'w': durationVal =   86400*7*durationVal; break;
            case 'M': durationVal =  86400*30*durationVal; break;
            case 'Y': durationVal = 86400*365*durationVal; break;

            default: durationVal = 60*durationVal; break;
        }

        for (String s: reasonList) sjReason.add(s);
        if (reasonList.size() != 0) reason = sjReason.toString();

        try { userMask = Bei.create(mask); }
        catch (InvalidFormatException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        addChanBei(fromNick, type, channel, userMask, durationVal, reason);

    }

    /**
     * Displays the channel b/e/i list
     * @param fromNick source nickname
     * @param type type of the list (ban, except, invite)
     * @param str command string
     */
    private void cServeBeiList(CSCommand csc, int type) {

        /* Syntax: BANLIST <#channel> */

        String chanName = "";
        String mask     = "";
        String reason   = "";
        String line     = "";
        String fromTSstr;
        String toTSstr;

        Date fromTSdate;
        Date toTSdate;

        boolean chanBei = true;

        Timestamp fromTS;
        Timestamp toTS;

        Channel channel;

        UserAccount author;

        List<String> outList = new ArrayList<>();

        Set<Bei> chanBeiList = new LinkedHashSet<>();

        Nick fromNick = csc.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { chanName = csc.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try {
            channel = protocol.getChannelNodeByNameCi(chanName);
            if (Flags.isChanSuspended(channel.getcServeFlags()) == true) throw new ItemSuspendedException("Channel suspended");
        }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanSusOrNotFound); return; }

        if (Flags.hasChanLOpPriv(fromNick.getAccount().getChanlev(channel)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }

        switch(type) {
            case    Const.CHANBEI_BANS:
                line = String.format(Messages.strChBeiListHeadBans, channel.getName());
                chanBeiList.addAll(channel.getcServeBanList().keySet());
                chanBeiList.addAll(channel.getBanList());
            break;
            case Const.CHANBEI_EXCEPTS:
                line = String.format(Messages.strChBeiListHeadExcepts, channel.getName());
                chanBeiList.addAll(channel.getcServeExceptList().keySet());
                chanBeiList.addAll(channel.getExceptList());
            break;
            case Const.CHANBEI_INVITES:
                line = String.format(Messages.strChBeiListHeadInvex, channel.getName());
                chanBeiList.addAll(channel.getcServeInviteList().keySet());
                chanBeiList.addAll(channel.getInviteList());
            break;
        }

        outList.add(line);

        line = String.format(Messages.strChBeiListHeadFormat, Messages.strChBeiListHeadColMask, Messages.strChBeiListHeadColSetBy, Messages.strChBeiListHeadColDate1, Messages.strChBeiListHeadColDate2, Messages.strChBeiListHeadColReason);
        outList.add(line);

        for (Bei um: chanBeiList) {

            reason = null;
            author = null;
            fromTS = null;
            toTS   = null;

            chanBei = true;

            mask = um.getString();

            switch(type) {
                case    Const.CHANBEI_BANS:
                    /* Check if the item is channel or cServe */
                    if (channel.getcServeBanList(um) != null) {
                        chanBei = false;
                        reason  = (String) channel.getcServeBanList(um).getReason();
                        author  = UserAccount.getUser( (Integer) channel.getcServeBanList(um).getAuthor());
                        fromTS  = (Timestamp) channel.getcServeBanList(um).getFromTS();
                        toTS    = (Timestamp) channel.getcServeBanList(um).getToTS();
                    }
                break;

                case Const.CHANBEI_EXCEPTS:
                    /* Check if the item is channel or cServe */
                    if (channel.getcServeExceptList(um) != null) {
                        chanBei = false;
                        reason  = (String) channel.getcServeExceptList(um).getReason();
                        author  = UserAccount.getUser( (Integer) channel.getcServeExceptList(um).getAuthor());
                        fromTS  = (Timestamp) channel.getcServeExceptList(um).getFromTS();
                        toTS    = (Timestamp) channel.getcServeExceptList(um).getToTS();
                    }
                break;

                case Const.CHANBEI_INVITES:
                    /* Check if the item is channel or cServe */
                    if (channel.getcServeInviteList(um) != null) {
                        chanBei = false;
                        reason  = (String) channel.getcServeInviteList(um).getReason();
                        author  = UserAccount.getUser( (Integer) channel.getcServeInviteList(um).getAuthor());
                        fromTS  = (Timestamp) channel.getcServeInviteList(um).getFromTS();
                        toTS    = (Timestamp) channel.getcServeInviteList(um).getToTS();
                    }
                break;
            }

            /* author = null  */
            if (chanBei == true) line = String.format(Messages.strChBeiListHeadFormat, mask, "(n/a)", "(n/a)", "(n/a)", "Channel ban/except/invite");
            else {
                fromTSdate = new Date(fromTS.getValue()*1000L);
                toTSdate   = new Date(toTS.getValue()*1000L);

                fromTSstr = jdf.format(fromTSdate);
                toTSstr   = jdf.format(toTSdate);

                if (fromTS.getValue() == 0L) fromTSstr = "(n/a)";
                if (toTS.getValue() == 0L)   toTSstr = "(permanent)";

                line = String.format(Messages.strChBeiListHeadFormat, mask, author.getName(), fromTSstr, toTSstr, reason);
            }

            outList.add(line);
        }

        for (String s: outList) sendReply(fromNick, s);
        sendReply(fromNick, Messages.strEndOfList);

        return;

    }

    private void cServeBeiDel(CSCommand csc, int type) {

        /* Syntax: BANDEL #channel nick!user@host */

        String chanName = "";
        String mask     = "";
        String mode     = "";

        Channel channel;

        UserMask userMask;

        Nick fromNick = csc.getFromNick();

        if (fromNick.isAuthed() == false) {
            sendReply(fromNick, Messages.strErrCommandUnknown);
            return;
        }

        try { chanName = csc.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try {
            channel = protocol.getChannelNodeByNameCi(chanName);
            if (Flags.isChanSuspended(channel.getcServeFlags()) == true) throw new ItemSuspendedException("Channel suspended");
        }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanSusOrNotFound); return; }

        if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(channel)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }

        try { mask = csc.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        if (UserMask.isValid(mask) == false) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }
        userMask = UserMask.create(mask);

        dispatcher.removeDbChanBei(type, channel, userMask);

        switch(type) {
            case Const.CHANBEI_BANS: mode = "-b"; break;
            case Const.CHANBEI_EXCEPTS: mode = "-e"; break;
            case Const.CHANBEI_INVITES: mode = "-I"; break;
        }

        try { protocol.setMode(myUserNode, channel, mode, userMask.getFullMask()); }
        catch (Exception e) { sendReply(fromNick, Messages.strErrCmdExec); return; }

        sendReply(fromNick, Messages.strSuccess);

    }

    private void cServeBeiClear(CSCommand csc, int type) {

        /* Syntax: BANCLEAR <#channel> */

        String chanName = "";
        String mode     = "";

        Nick fromNick = csc.getFromNick();

        Channel channel;
        Set<Bei> userMaskList;

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { chanName = csc.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try {
            channel = protocol.getChannelNodeByNameCi(chanName);
            if (Flags.isChanSuspended(channel.getcServeFlags()) == true) throw new ItemSuspendedException("Channel suspended");
        }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanSusOrNotFound); return; }

        if (Flags.hasChanLMasterPriv(fromNick.getAccount().getChanlev(channel)) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }

        switch(type) {
            case    Const.CHANBEI_BANS: mode = "-b"; userMaskList = new HashSet<>(channel.getcServeBanList().keySet()); break;
            case Const.CHANBEI_EXCEPTS: mode = "-e"; userMaskList = new HashSet<>(channel.getcServeExceptList().keySet()); break;
            case Const.CHANBEI_INVITES: mode = "-I"; userMaskList = new HashSet<>(channel.getcServeInviteList().keySet()); break;
            default: userMaskList = new HashSet<>();
        }

        for(Bei userMask: userMaskList) {
            dispatcher.removeDbChanBei(type, channel, userMask);
            try { protocol.setMode(myUserNode, channel, mode, userMask.getString()); }
            catch (Exception e) { sendReply(fromNick, Messages.strErrCmdExec); return; }
        }

        sendReply(fromNick, Messages.strSuccess);
    }

    private void cServeRawCmd(CSCommand csc) {

        /* Syntax: RAW <SERVER|CS|RAW> <string> */

        String subCommand = "";
        String rawCommand = "";
        String strToSend  = "";

        Nick fromNick = csc.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserDevGodPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { subCommand = csc.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { rawCommand = StringTools.listToString(csc.getArgs().subList(1, csc.getArgs().size())); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        switch (subCommand.toUpperCase()) {
            case "SERVER": strToSend = String.format(":%s %s", config.getServerId(), rawCommand); break;
            case "CS":     strToSend = String.format(":%s %s", myUserNode.getUid(), rawCommand); break;
            case "RAW":    strToSend = String.format(":%s", rawCommand); break;
            default: sendReply(fromNick, Messages.strErrCommandSyntax); return;
        }

        protocol.sendRaw(fromNick, strToSend);

        sendReply(fromNick, Messages.strSuccess);

    }

    private void cServeSleep(CSCommand csCommand) {

        int duration = 15;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserDevGodPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        sendReply(fromNick, "Sleeping for " + duration + " seconds...");

        try { for (int i=0; i<duration; i++) Thread.sleep(1L *1000); sendReply(fromNick, "1 second"); }
        catch (InterruptedException e) { sendReply(fromNick, "Sleep interrupted: " + e); }

        sendReply(fromNick, Messages.strSuccess);

    }

    private void cServePerformJoin(CSCommand csCommand) {

        String chanName = "";

        Channel channel;

        Nick fromNick = csCommand.getFromNick();

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { chanName = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try {
            channel = protocol.getChannelNodeByNameCi(chanName);
            if (Flags.isChanSuspended(channel.getcServeFlags()) == true) throw new ItemSuspendedException("Channel suspended");
        }
        catch (Exception e) { sendReply(fromNick, Messages.strErrChanSusOrNotFound); return; }

        if (Flags.hasUserDevGodPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, String.format(Messages.strErrNoAccess)); return; }

        performJoinActions(myUserNode, channel);

        sendReply(fromNick, Messages.strSuccess);

    }

    private void cServeTestExtban(CSCommand csCommand) {

        /* TESTEXTBAN <~extban> */

        String extBanString;

        Nick fromNick = csCommand.getFromNick();

        Extban extban;

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserDevGodPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { extBanString = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { extban = Extban.create(extBanString); }
        catch (InvalidFormatException e) { sendReply(fromNick, "extban format error"); return; }

        //for(Extban line: extban.getExtBanList()) {
        //    sendReply(fromNick, String.format("Extban %s, stack %s", extBanString, line.getRaw()) );
        //}

        sendReply(fromNick,
          String.format("Extban %s => time=%s, action=[%s, %s], selector=[%s, %s, %s]", extBanString, extban.getTimeLimit(), extban.getActionName(), extban.getActionValue(), extban.getSelectorName(), extban.getSelectorParam(), extban.getSelectorValue()) );

    }

    private void cServeTestWildcard(CSCommand csCommand) {

        /* TESTWILDCARD <wildcard> <string1 [string2 [string3 ...]]> */

        String[] testSplit;

        String wildcardStr;
        String testStr;

        Nick fromNick = csCommand.getFromNick();

        Wildcard wildcard;

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserDevGodPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { wildcardStr = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { testStr = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        wildcard = Wildcard.create(wildcardStr);

        testSplit = testStr.split(" ");

        for (String s: testSplit) {
            if (wildcard.matches(s) == true) sendReply(fromNick, String.format("Wildcard %s -> %s => match = %s", wildcard.toString(), s, "true"));
            else if (wildcard.matches(s) == false) sendReply(fromNick, String.format("Wildcard %s -> %s => match = %s", wildcard.toString(), s, "false"));
        }
        sendReply(fromNick, Messages.strSuccess );

    }

    private void cServeTestWildcard2(CSCommand csCommand) {

        /* TESTWILDCARD2 <wildcard> <test string> */

        String wildcardStr;
        String testStr;

        Nick fromNick = csCommand.getFromNick();

        Wildcard wildcard;

        if (fromNick.isAuthed() == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }
        if (Flags.hasUserDevGodPriv(fromNick.getAccount().getFlags()) == false) { sendReply(fromNick, Messages.strErrCommandUnknown); return; }

        try { wildcardStr = csCommand.getArgs().get(0); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        try { testStr = csCommand.getArgs().get(1); }
        catch (IndexOutOfBoundsException e) { sendReply(fromNick, Messages.strErrCommandSyntax); return; }

        wildcard = Wildcard.create(wildcardStr);

        if (wildcard.matches(testStr) == true) sendReply(fromNick, String.format("Wildcard %s -> %s => match = %s", wildcard.toString(), testStr, "true"));
        else sendReply(fromNick, String.format("Wildcard %s -> %s => match = %s", wildcard.toString(), testStr, "false"));

        sendReply(fromNick, Messages.strSuccess );

    }

    private Map<String, Integer> parseChanlevModRequest(Channel chan, UserAccount accountRequester, UserAccount accountTarget, String modFlagStr) {

        Map<String, String>   chanlevModSepStr   = null;
        Map<String, Integer>  chanlevModSepInt   = new HashMap<>();

        Map<String, Integer>  modResult          = new HashMap<>();


        /* Transform the flag modification request to the String Map */
        chanlevModSepStr = Flags.parseFlags(modFlagStr);

        /* Transform the String Map to the Integer Map */
        chanlevModSepInt.put("+", Flags.flagsCharsToInt("chanlev", chanlevModSepStr.get("+")));
        chanlevModSepInt.put("-", Flags.flagsCharsToInt("chanlev", chanlevModSepStr.get("-")));

        /* Create extra "combined" key for xxx */
        chanlevModSepInt.put("combined", 0);

        /* Stripping the unknown and readonly flags */
        chanlevModSepInt.replace("+", Flags.stripUnknownChanlevFlags(chanlevModSepInt.get("+")));
        chanlevModSepInt.replace("-", Flags.stripUnknownChanlevFlags(chanlevModSepInt.get("-")));

        /* Copying personal flags to new keys p+ p- (users can set/unset personal flags even if they are not known on the channel) */
        chanlevModSepInt.put("p+", Flags.keepChanlevPersonalConFlags(chanlevModSepInt.get("+")));
        chanlevModSepInt.put("p-", Flags.keepChanlevPersonalConFlags(chanlevModSepInt.get("-")));


        /* Keeping admin editable flags if the target account is admin */
        if (Flags.hasUserAdminPriv(accountRequester.getFlags()) == true) {
            /* Admin can edit everything */
        }
        /* Keeping oper editable flags if the user is oper */
        else if (Flags.hasUserOperPriv(accountRequester.getFlags()) == true) {
            /* Oper can edit everything */
        }

        /* Keeping chanowner editable flags if the user is owner of the chan */
        else if (Flags.hasChanLOwnerPriv(accountRequester.getChanlev(chan)) == true) {
            chanlevModSepInt.replace("+", Flags.keepChanlevOwnerConFlags(chanlevModSepInt.get("+")));
            chanlevModSepInt.replace("-", Flags.keepChanlevOwnerConFlags(chanlevModSepInt.get("-")));
        }
        /* Keeping chanmaster editable flags if the user is master of the chan */
        else if (Flags.hasChanLMasterPriv(accountRequester.getChanlev(chan)) == true) {
            chanlevModSepInt.replace("+", Flags.keepChanlevMasterConFlags(chanlevModSepInt.get("+")));
            chanlevModSepInt.replace("-", Flags.keepChanlevMasterConFlags(chanlevModSepInt.get("-")));
        }

        /* Keeping self editable flags if the user is known on the chan (but can only remove them on themselves) */
        else if (Flags.hasChanLKnown(accountRequester.getChanlev(chan)) == true && accountRequester == accountTarget) {
            chanlevModSepInt.replace("+", 0);
            chanlevModSepInt.replace("-", Flags.keepChanlevSelfConFlags(chanlevModSepInt.get("-")));
        }

        /* User has no rights on the chan */
        else {
            chanlevModSepInt.replace("+", 0);
            chanlevModSepInt.replace("-", 0);
        }


        /* If the user is trying to set personal flags of another user and has not oper privilege => strip it  */
        if ( (accountRequester != accountTarget) &&
             (chanlevModSepInt.get("p+") + chanlevModSepInt.get("p-") != 0) &&
             (Flags.hasUserOperPriv(accountRequester.getFlags()) == false)) {
            chanlevModSepInt.replace("p+", 0);
            chanlevModSepInt.replace("p-", 0);
        }


        /* User has provided no personal flags and has no rights on the chan */
        if (chanlevModSepInt.get("+") + chanlevModSepInt.get("-") + chanlevModSepInt.get("p+") + chanlevModSepInt.get("p-") == 0 ) {
            throw new ChanlevChanNoRightException();
        }


        /*
         * Reaching this point, the user has enough privileges:
         *  - has oper privilege
         *  - has master/owner privilege
         *  - has known privilege
         *  - is setting personal flags on himself
         * Normally the modification request should have been stripped of all the impossible cases
         */


        /* Combining flags */
        chanlevModSepInt.replace("+", chanlevModSepInt.get("+") | chanlevModSepInt.get("p+"));
        chanlevModSepInt.replace("-", chanlevModSepInt.get("-") | chanlevModSepInt.get("p-"));
        chanlevModSepInt.replace("combined", chanlevModSepInt.get("+") | chanlevModSepInt.get("-"));


        if (chanlevModSepInt.get("combined") == 0) {
            throw new ChanlevModIsZeroException();
        }


        modResult.put("+", chanlevModSepInt.get("+"));
        modResult.put("-", chanlevModSepInt.get("-"));

        return modResult;

    }

}
