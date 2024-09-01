package xyz.mjav.theqbot;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import xyz.mjav.theqbot.exceptions.InvalidFormatException;
import xyz.mjav.theqbot.exceptions.ItemExistsException;
import xyz.mjav.theqbot.exceptions.ItemNotFoundException;
import xyz.mjav.theqbot.exceptions.NickNotFoundException;
import xyz.mjav.theqbot.exceptions.ParseException;

import static java.util.Map.entry;

import java.io.FileNotFoundException;

public class UnrealIRCd extends Exception implements Protocol {

    private static Logger log = LogManager.getLogger("common-log");

    private static UnrealIRCd instance;

    private static final Map<String, String> USER_MODES = Map.ofEntries(
        entry("mode_a",            "a"),
        entry("mode_b",            "b"),
        entry("mode_c",            "c"),
        entry("chandeaf",          "d"),
        entry("mode_e",            "e"),
        entry("mode_f",            "f"),
        entry("mode_g",            "g"),
        entry("mode_h",            "h"),
        entry("invisible",         "i"),
        entry("mode_j",            "j"),
        entry("mode_k",            "k"),
        entry("mode_l",            "l"),
        entry("mode_m",            "m"),
        entry("mode_n",            "n"),
        entry("oper",              "o"),
        entry("hidechans",         "p"),
        entry("unkickable",        "q"),
        entry("registered",        "r"),
        entry("snotices",          "s"),
        entry("vhost",             "t"),
        entry("mode_u",            "u"),
        entry("mode_v",            "v"),
        entry("wallops",           "w"),
        entry("cloackedhost",      "x"),
        entry("mode_y",            "y"),
        entry("tlsconn",           "z"),
        entry("mode_A",            "A"),
        entry("bot",               "B"),
        entry("mode_C",            "C"),
        entry("privdeaf",          "D"),
        entry("mode_E",            "E"),
        entry("mode_F",            "F"),
        entry("filterbadwords",    "G"),
        entry("hideoper",          "H"),
        entry("hideoperidle",      "I"),
        entry("mode_J",            "J"),
        entry("mode_K",            "K"),
        entry("mode_L",            "L"),
        entry("mode_M",            "M"),
        entry("mode_N",            "N"),
        entry("mode_O",            "O"),
        entry("mode_P",            "P"),
        entry("mode_Q",            "Q"),
        entry("regdeaf",           "R"),
        entry("service",           "S"),
        entry("noctcp",            "T"),
        entry("mode_U",            "U"),
        entry("mode_V",            "V"),
        entry("spywhois",          "W"),
        entry("mode_X",            "X"),
        entry("mode_Y",            "Y"),
        entry("tlsdeaf",           "Z")
    );

    private static final Map<String, String> CHANNEL_MODES = Map.ofEntries(
        entry("admin",              "a"),
        entry("banned",             "b"),
        entry("blockcolor",         "c"),
        entry("hasdelayed",         "d"),
        entry("except",             "e"),
        entry("floodwparam",        "f"),
        entry("mode_g",             "g"),
        entry("halfop",             "h"),
        entry("invite",             "i"),
        entry("mode_j",             "j"),
        entry("key",                "k"),
        entry("limit",              "l"),
        entry("moderated",          "m"),
        entry("noextmsg",           "n"),
        entry("op",                 "o"),
        entry("private",            "p"),
        entry("owner",              "q"),
        entry("registered",         "r"),
        entry("secret",             "s"),
        entry("topiconlyops",       "t"),
        entry("mode_u",             "u"),
        entry("voice",              "v"),
        entry("mode_w",             "w"),
        entry("mode_x",             "x"),
        entry("mode_y",             "y"),
        entry("tlsjoin",            "z"),
        entry("mode_A",             "A"),
        entry("mode_B",             "B"),
        entry("noctcp",             "C"),
        entry("delayed",            "D"),
        entry("mode_E",             "E"),
        entry("floodwprofile",      "F"),
        entry("filterbadwords",     "G"),
        entry("history",            "H"),
        entry("invex",              "I"),
        entry("mode_J",             "J"),
        entry("noknock",            "K"),
        entry("link",               "L"),
        entry("modregvoice",        "M"),
        entry("nonickchange",       "N"),
        entry("opersjoin",          "O"),
        entry("permanent",          "P"),
        entry("nokick",             "Q"),
        entry("regjoin",            "R"),
        entry("stripcolor",         "S"),
        entry("nonotice",           "T"),
        entry("mode_U",             "U"),
        entry("noinvite",           "V"),
        entry("mode_W",             "W"),
        entry("mode_X",             "X"),
        entry("mode_Y",             "Y"),
        entry("hasonlytlsusers",    "Z")
    );

    private static final List<String> protoFeatures = List.of(
        "NICKv2",
        "VHP",
        "UMODE2",
        "NICKIP",
        "SJOIN",
        "SJOIN2",
        "SJ3",
        "NOQUIT",
        "TKLEXT",
        "MLOCK",
        "SID",
        "MTAGS"
    );

    /** Maps user mode letter => letter:long-name */
    private static final Map<String, String> userModeToTxt;

    /** Maps channel mode letter => letter:long-name */
    private static final Map<String, String> chanModeToTxt;

    static {
        /* Create reverse maps of modes */
        userModeToTxt = new HashMap<>();
        chanModeToTxt = new HashMap<>();
        for(Map.Entry<String, String> set:  USER_MODES.entrySet()) userModeToTxt.put(set.getValue(), set.getValue() + ":" + set.getKey());
        for(Map.Entry<String, String> set: CHANNEL_MODES.entrySet()) chanModeToTxt.put(set.getValue(), set.getValue() + ":" + set.getKey());
    }

    private Client client;

    private Config config;

    private CService cservice;

    private OService oservice;

    private Database database;

    private Boolean isNetBursting = true;

    private Map<String, String> protocolProps = new HashMap<>();

    private Map<String, Boolean> featureList = new HashMap<>();

    private String myPeerServerId;
    private String userChanJoinMode;
    private String networkChanModesGroup1 = "";
    private String networkChanModesGroup2 = "";
    private String networkChanModesGroup3 = "";
    private String networkChanModesGroup4 = "";
    private String networkChanUserModes   = "";
    //private String networkUserModes       = "";

    private Dispatcher dispatcher = new Dispatcher(config, database, this);

    public static synchronized UnrealIRCd getInstance(Config config, Database database, Client client) {
        if (instance.equals(null) == true) instance = new UnrealIRCd(config, database, client);
        return instance;
    }

    /**
     * Class constructor
     * Creates a protocol and populates the registered users list given by database
     * @param config object to static configuration
     * @param database object to static database
     */
    public UnrealIRCd(Config config, Database database, Client client) {
        this.config = config;
        this.database = database;
        this.client = client;
        database.setProtocol(this);

        UserAccount.setUserList(database.getRegUsers());

        Channel.setRegChanList(database.getRegChans());

        this.sendServerIdent();
    }

    /**
     * Returns the channel node corresponding to the name
     * @param channelName channel name
     * @return channel node
     */
    @Override
    public Channel getChannelNodeByNameCi(String channelName) throws ItemNotFoundException {

        Channel returnval = null;

        String name;

        for (Channel chan: Channel.getChanList()) {
            name = chan.getName();
            if (channelName.toLowerCase().equals(name.toLowerCase())) returnval = chan;
        }

        if (returnval == null) throw new ItemNotFoundException("Channel does not exist");
        else return returnval;

    }

    @Override
    public void setClientRef(Client client) {
        this.client = client;
    }

    @Override
    public void setCService(CService cservice) {
        this.cservice = cservice;
    }

    @Override
    public void setOService(OService oservice) {
        this.oservice = oservice;
    }

    private void write(Client client, String str) /*throws Exception*/ {
        String[] strList = str.split("\n");
        for (String strToSend: strList) client.write(strToSend);
    }

    /**
     * Sends a notice from an user to another user
     * @param client client
     * @param from originator usernode
     * @param to target usernode
     * @param msg message string
     */
    @Override
    public void sendNotice(Nick from, Nick to, String msg) /*throws Exception*/ {
        String str;
        str = String.format(":%s NOTICE %s :%s", from.getUid(), to.getUid(), msg);
        client.write(str);
    }

    @Override
    public void sendWallops(Nick from, String msg) /*throws Exception*/ {
        String str;
        str = String.format(":%s WALLOPS :%s", from.getUid(), msg);
        client.write(str);
    }

    @Override public void sendMlock(Server from, Channel channel, String mlock) {
        String str;
        str = String.format(":%s MLOCK 0 %s %s", from.getSid() , channel.getName(), mlock);
        client.write(str);
    }

    @Override
    public void sendWallops(Server from, String msg) /*throws Exception*/ {
        String str;
        str = String.format(":%s WALLOPS :%s", from.getSid(), msg);
        client.write(str);
    }

    /**
     * Sends a privmsg from an user to another user
     * @param client client
     * @param from originator usernode
     * @param to target usernode
     * @param msg message string
     */
    @Override
    public void sendPrivMsg(Nick from, Nick to, String msg) /*throws Exception*/ {
        String str;
        str = String.format(":%s PRIVMSG %s :%s", from.getUid(), to.getUid(), msg);
        client.write(str);
    }

    @Override
    public void sendInvite(Nick to, Channel chanNode) /*throws Exception*/ {
        String str;
        str = String.format(":%s INVITE %s %s", config.getServerId() + config.getCServeUniq(), to.getUid(), chanNode.getName());
        client.write(str);
    }

    /**
     * Makes the bot join a channel
     * @param client client
     * @param who originator usernode
     * @param chan target channel
     */
    @Override
    public void chanJoin(Nick who, Channel chan) /*throws Exception*/ {
        String str;

        /* If the channel already exists on the network, do nothing */
        if (Channel.isChan(chan.getName()) == true)  log.info(String.format("UnrealIRCd::chanJoin: user %s joined existing channel %s", who.getNick(), chan.getName()));
        else {
            log.info(String.format("UnrealIRCd::chanJoin: user %s joined new channel %s (creating it)", who.getNick(), chan.getName()));
            try { Channel.addChannel(chan); }
            catch (ItemExistsException e) {
                log.error(String.format("UnrealIRCd::chanJoin: error while adding channel %s to channel list: channel exists", chan));
            }
        }

        try { chan.setChanlev(database.getChanChanlev(chan)); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::chanJoin: error setting chanlev for channel %s", chan.getName()), e); }

        try { dispatcher.addUserToChan(chan, who);  }
        catch (Exception e) { log.error(String.format("UnrealIRCd::chanJoin: could not add user %s to chan %s", who.getNick(), chan.getName()), e); }

        log.info(String.format("UnrealIRCd::chanJoin: Updating channel %s usercount to %s", chan.getName(), chan.getUserCount()));

        str = String.format(":%s JOIN %s", who.getUid(), chan.getName());

        client.write(str);
    }

    /**
     * Make the bot leaves the channel (without message)
     * @param client client
     * @param who usernode originator
     * @param chan channelnode
     */
    @Override
    public void chanPart(Nick who, Channel chanUserPart) {
        chanPart(who, chanUserPart, "");
    }

    /**
     * Make the bot leaves the channel (with message)
     * @param client client
     * @param who usernode originator
     * @param chan channelnode
     */
    @Override
    public void chanPart(Nick who, Channel chanUserPart, String s) {
        String reason = "PART";
        String str;

        if (s.isEmpty() == true) str = String.format(":%s PART %s", who.getUid(), chanUserPart.getName());
        else str = String.format(":%s PART %s :%s", who.getUid(), chanUserPart.getName(), s);

        handleLeavingChan(chanUserPart, who, reason);

        client.write(str);
    }

    private void handleLeavingChan(Channel chan, Nick user, String leavingSource) {
        Integer chanUserCount;
        try {
            dispatcher.removeUserFromChan(chan, user);
            log.debug(String.format("UnrealIRCd::handleLeavingChan: user %s left chan %s following action %s", user.getNick(), chan.getName(), leavingSource));
        }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleLeavingChan: cannot remove the user %s from chan %s (%s) because it is not inside it", user.getNick(), chan.getName(), leavingSource), e); }

        chanUserCount = chan.getUserCount();

        /* If the channel is empty and not persistent, delete it */
        if (chanUserCount.equals(0) == true && chan.getModes().containsKey("P") == false) {
            //channelList.remove( chan.getName() );
            Channel.removeChannel(chan);
            log.info(String.format("UnrealIRCd::handleLeavingChan: deleting channel %s because it is empty and it is not persistent", chan.getName()));
            chan = null;
        }
    }

    /**
     * Kicks an user from a channel with the reason
     * @param client client
     * @param who originator
     * @param chan channelnode
     * @param target usernode
     * @param reason reason
     */
    @Override
    public void chanKick(Nick who, Channel chan, Nick target, String reason) /*throws Exception*/ {

        String str;
        str = String.format(":%s KICK %s %s :%s", who.getUid(), chan.getName(), target.getNick(), reason);

        handleLeavingChan(chan, target, "KICK");

        client.write(str);
    }

    /**
     * Generic method to set modes from a source to a channel
     *
     * @param source source entity (Nick or Server)
     * @param chan target Channel
     * @param modes modes list (+- format)
     * @param parameters modes parameters
     *
     */
    @Override
    public void setMode(Object source, Channel chan, String modes, String modesParams) {

        Map<String, Map<String, String>>   modChanModesAll;

        try { modChanModesAll = parseChanModes(modes + " " + modesParams); }
        catch (ParseException e) { log.error(String.format("UnrealIRCd::handleSjoin: error while parsing modes: %s", modes + " " + modesParams)); return; }

        Map<String, String>      modChanModes   =  modChanModesAll.get("chanModes");
        Map<String, String>      modChanLists   =  modChanModesAll.get("chanLists");
        Map<String, String>  modChanUserModes   =  modChanModesAll.get("chanUserModes");

        /* Sets the chan user modes */
        var wrapperUMode = new Object() { String[] nicks; Nick userNode; };
        modChanUserModes.forEach( (mode, nicks) -> {
            wrapperUMode.nicks = nicks.split(" ");
            for (String nick: wrapperUMode.nicks) {
                if (nick.isEmpty() == false) {
                    try { wrapperUMode.userNode = Nick.getUserByNickCi(nick); }
                    catch (NickNotFoundException e) { log.warn(String.format("UnrealIRCd::setMode: it seems that there was a nick that does not exist on the network included in the chanmode"), e); continue; }
                    log.debug(String.format("UnrealIRCd::MODE: Channel %s: (parsed) change usermode: %s %s", chan.getName(), mode, nick));
                    try {
                        if (mode.startsWith("+")) dispatcher.addUserChanMode(chan, wrapperUMode.userNode, String.valueOf(mode.charAt(1)), "");
                        else dispatcher.delUserChanMode(chan, wrapperUMode.userNode, String.valueOf(mode.charAt(1)));
                    }
                    catch (Exception e) { log.error(String.format("UnrealIRCd::setMode: error setting mode for channel %s", chan.getName()), e); }
                }
            }
        });

        /* Sets the chan modes */
        modChanModes.forEach( (mode, parameter) -> {
            log.debug(String.format("UnrealIRCd::MODE: Channel %s: (parsed) change mode: %s %s", chan.getName(), mode, parameter));
            if (mode.startsWith("+") == true) chan.addMode(String.valueOf(mode.charAt(1)), parameter);
            else chan.delMode(String.valueOf(mode.charAt(1)), parameter);
        });

        /* Sets the chan lists */
        var wrapperCList = new Object() { String[] parameters; Bei mask; };
        modChanLists.forEach( (list, parameters) -> {
            wrapperCList.parameters = parameters.split(" ");
            for (String parameter: wrapperCList.parameters) {
                if (parameter.isEmpty() == false) {
                    log.debug(String.format("UnrealIRCd::MODE: Channel %s: (parsed) change list: %s %s", chan.getName(), list, parameter));
                    try { wrapperCList.mask = Bei.create(parameter); }
                    catch (InvalidFormatException e) { log.error(String.format("UnrealIRCd::setMode: beI %s has invalid format", parameter), e); return; }
                    if (list.equals("+b")) chan.addBanList(wrapperCList.mask); // TODO: abstract chanmode
                    else if (list.equals("-b")) chan.delBanList(wrapperCList.mask); // TODO: abstract chanmode
                    else if (list.equals("+e")) chan.addExceptList(wrapperCList.mask); // TODO: abstract chanmode
                    else if (list.equals("-e")) chan.delExceptList(wrapperCList.mask); // TODO: abstract chanmode
                    else if (list.equals("+I")) chan.addInviteList(wrapperCList.mask); // TODO: abstract chanmode
                    else if (list.equals("-I")) chan.delInviteList(wrapperCList.mask); // TODO: abstract chanmode
                }
            }
        });

        String str;

        String actorString = config.getServerId();

        if (source instanceof Nick) { actorString = ((Nick)source).getUid(); }
        else if (source instanceof Server) { actorString = ((Server)source).getSid(); }

        str = String.format(":%s MODE %s %s %s",  actorString, chan.getName(), modes, modesParams);

        client.write(str);
    }

    /**
     * Sets modes from the default server (me) to a channel
     *
     * @param toTarget channel
     * @param modes modes
     * @param parameters parameters
     *
     */
    @Override
    public void setMode(Channel toTarget, String modes, String parameters) {
        setMode(Server.getServerBySid(config.getServerId()), toTarget, modes, parameters);

    }

    /**
     * Sets a user mode from an user to an user
     * @param client client
     * @param fromWho usernode
     * @param toTarget usernode
     * @param modes modes
     * @param parameters parameters
     * @throws Exception
     */
    @Override
    public void setSvsMode(Nick fromWho, Nick toTarget, String modes, String parameters) throws Exception {

    }

    /**
     * Sets a user mode from a server to an user
     * @param client client
     * @param fromWho servernode
     * @param toTarget usernode
     * @param modes modes
     * @param parameters parameters
     * @throws Exception
     */
    @Override
    public void setSvsMode(Server fromWho, Nick toTarget, String modes, String parameters) throws Exception {

    }

    @Override
    public void setTopic(Nick from, Channel to, String topic) /*throws Exception*/ {
        String str;
        str = String.format(":%s TOPIC %s :%s", from.getUid(), to.getName(), topic);
        client.write(str);
    }

    @Override
    public void setMlock(Server fromWho, Channel toTarget, String modes) {
        /* :5PB MLOCK 1681424518 #chan PCfHntT */
        String str;
        Long unixTime;
        unixTime = Instant.now().getEpochSecond();

        str = String.format(":%s MLOCK %s %s %s", fromWho.getSid(), unixTime, toTarget.getName(), modes);
        client.write(str);
    }

    @Override
    public void sendQuit(Nick from, String msg) {
        String str;
        str = String.format(":%s QUIT :%s", from.getUid(), msg);

        try { Nick.removeUser(from); }
        catch (ItemNotFoundException e) { log.error(String.format("UnrealIRCd::sendQuit: Could not remove the user from the nick %s (%s) list because it is not present.", from.getNick(), from.getUid()), e); }
        client.write(str);
    }

    @Override
    public void sendUid(Nick from) {
        String str;
        /* UID nickname hopcount timestamp username hostname uid servicestamp usermodes virtualhost cloakedhost ip :gecos */
        str = String.format(":%s UID %s 1 %s %s %s %s * %s %s * %s :%s",
                config.getServerId(), from.getNick(), from.getUserTS(), from.getIdent(), from.getRealHost(), from.getUid(), from.getModesAsString(), from.getHost(), from.getIpAddressAsBase64(), from.getRealName()
        );

        client.write(str);
    }

    @Override
    public void chgHostVhost(Nick toTarget, String vhost) {
        String who = config.getServerId();
        String vhostComplete = config.getCServeHostPrefix() + vhost + config.getCServeHostSuffix();
        String str;

        if (toTarget.getHost().equals(vhostComplete)) return;

        str= ":" + who + " CHGHOST " + toTarget.getUid() + " " + vhostComplete;
        client.write(str);
    }

    @Override
    public void chgHost(Nick toTarget, String vhost) {
        String who = config.getServerId();
        String vhostComplete = vhost;
        String str;

        if (toTarget.getHost().equals(vhostComplete)) return;

        str = String.format(":%s CHGHOST %s %s", who, toTarget.getUid(), vhostComplete);
        client.write(str);
    }

    @Override
    public Boolean hasFeature(String feature) {
        Boolean featureValue = false;
        if (featureList.containsKey(feature)) { featureValue = featureList.get(feature); }
        return featureValue;
    }

    @Override
    public String getPeerId() {
        return this.myPeerServerId;
    }

    private void sendSaslResult(Nick user, Boolean isSuccessful) {
        String str;
        String fromUid = config.getServerId() + config.getCServeUniq();
        String userSaslAuthParam = user.getSaslAuthParam("authServer");

        str = String.format(":%s SASL %s %s D %s", fromUid, userSaslAuthParam, user.getUid(), isSuccessful == true ? "S" : "F");
        client.write(str);
    }

    private void sendSaslQuery(Nick user) {
        String str;
        String fromUid = config.getServerId() + config.getCServeUniq();
        String userSaslAuthParam = user.getSaslAuthParam("authServer");

        str = String.format(":%s SASL %s %s C +", fromUid, userSaslAuthParam, user.getUid());
        client.write(str);
    }

    private void sendSvsLogin(Nick user, UserAccount account, Boolean isAuth) {

        /*
         * :SID SVSLOGIN destServerSID UID :account name
         */

        String str;
        String toServerSid = "";
        String accountNameToAuth = "0";

        if (user.getSaslAuthParam("authServer") != null) {
            toServerSid = user.getSaslAuthParam("authServer");
        }
        else {
            toServerSid = user.getServer().getSid();
        }

        /* :5PB SVSLOGIN ocelot. 5P0QVW5M3 AnhTay */
        if (isAuth == true) accountNameToAuth = account.getName();

        str = String.format(":%s SVSLOGIN %s %s %s", config.getServerId(), toServerSid, user.getUid(), accountNameToAuth);
        client.write(str);
    }

    @Override
    public void sendSetHost(Nick user, String vhost) {
        String str;

        str = String.format(":%s SETHOST %s", user.getUid(), vhost);
        client.write(str);
    }

    @Override
    public void sendSvsLogin(Nick user) {
        sendSvsLogin(user, null, false);
    }

    @Override
    public void sendSvsLogin(Nick user, UserAccount account) {
        sendSvsLogin(user, account, true);
    }


    /**
     * Parses a channel mode list and group them into 3 Maps (chanModes, chanLists, chanUserModes).
     * chanModes contains a Map of mode->parameter
     * chanLists contains a Map of mode->list
     * chanUserModes contains a Map of mode->nick
     * @param str Modes string, e.g "+abc-de+g... param1 param2 param3 param4 param5..."
     * @return Map of 3 keys (chanModes, chanLists, chanUserModes) with a Map in each
     * @throws ParseException
     */
    public Map<String, Map<String, String>> parseChanModes(String str) throws ParseException {

        /*
         * CHANMODES=beI,fkL,lFH,cdimnprstzCDGKMNOPQRSTVZ
         *           --- --- --- ------------------------
         *            |   |    |           `- group4: no parameter
         *            |   |     `------------ group3: parameter for set, no parameter for unset
         *            |    `----------------- group2: parameter for set, parameter for unset
         *             `--------------------- group1: (list) parameter for set, parameter for unset
         */

        Map<String, String>     chanModes = new HashMap<>();
        Map<String, String> chanUserModes = new HashMap<>();
        Map<String, String>     chanLists = new HashMap<>();

        Map<String, Map<String, String>> result = new HashMap<>();
        result.put("chanModes",     chanModes);
        result.put("chanLists",     chanLists);
        result.put("chanUserModes", chanUserModes);

        //String networkChanModesGroup1        = ((protocolProps.get("CHANMODES")).split(",", 4))[0];
        //String networkChanModesGroup2        = ((protocolProps.get("CHANMODES")).split(",", 4))[1];
        //String networkChanModesGroup3        = ((protocolProps.get("CHANMODES")).split(",", 4))[2];
        //String networkChanModesGroup4        = ((protocolProps.get("CHANMODES")).split(",", 4))[3];
        //String   networkChanUserModes        =   protocolProps.get("PREFIX").replaceAll("[^A-Za-z0-9]", ""); // Channel modes for users


        /*
         *       Modes = strSplit[0]
         *  Parameters = strSplit[1+]
         */
        String[] strSplit = str.split(" ");

        String modes        = strSplit[0];
        String curMode;
        String strUserModes = "";
        String strLists     = "";

        char modeAction = '+';

        Integer modeIndex  = 0;
        Integer paramIndex = 1;

        Nick userNode;

        for(int i=0; i < networkChanUserModes.length(); i++) {
            chanUserModes.put("+" + networkChanUserModes.charAt(i), "");
            chanUserModes.put("-" + networkChanUserModes.charAt(i), "");
        }

        for(int i=0; i < networkChanModesGroup1.length(); i++) {
            chanLists.put("+" + networkChanModesGroup1.charAt(i), "");
            chanLists.put("-" + networkChanModesGroup1.charAt(i), "");
        }

        while(modeIndex < modes.length()) {
            curMode = String.valueOf(modes.charAt(modeIndex));

            switch(curMode) {
                case "+": modeAction = '+'; break;
                case "-": modeAction = '-'; break;
            }

            if (curMode.matches("[A-Za-z]") == true) {

                if (curMode.matches("[" + networkChanModesGroup1 + "]")) { /* Chan lists */
                    strLists = "";
                    try { strLists = String.join(" ", chanLists.get(modeAction + curMode), strSplit[paramIndex]); }
                    catch (IndexOutOfBoundsException e) { throw new ParseException(); }
                    chanLists.replace(modeAction + curMode, strLists);
                    paramIndex++;
                }

                else if (curMode.matches("[" + networkChanModesGroup2 + "]")) {
                    try { chanModes.put(modeAction + curMode, strSplit[paramIndex]); }
                    catch (IndexOutOfBoundsException e) { throw new ParseException(); }
                    paramIndex++;
                }

                else if (curMode.matches("[" + networkChanModesGroup3 + "]")) {
                    if (modeAction == '+') {
                        try { chanModes.put(modeAction + curMode, strSplit[paramIndex]); }
                        catch (IndexOutOfBoundsException e) { throw new ParseException(); }
                        paramIndex++;
                    }
                    else {
                        chanModes.put(modeAction + curMode, "");
                    }
                }

                else if (curMode.matches("[" + networkChanModesGroup4 + "]")) {
                    chanModes.put(modeAction + curMode, "");
                }

                else if (curMode.matches("[" + networkChanUserModes + "]")) {

                    strUserModes = "";
                    try { userNode = Nick.getUserByNickCi(strSplit[paramIndex]); }
                    catch (NickNotFoundException e) {
                        log.error(String.format("UnrealIRCd::parseChanModes: error that should not happen, nick included in mode change is not on the network"), e);
                        throw new ItemNotFoundException(String.format("UnrealIRCd::parseChanModes: error that should not happen, nick included in mode change is not on the network"));
                    }

                    strUserModes = String.join(" ", chanUserModes.get(modeAction + curMode), userNode.getNick());
                    chanUserModes.replace(modeAction + curMode, strUserModes);
                    paramIndex++;
                }
            }

            modeIndex++;
        }

        return result;
    }


    public Map<String, String> parseUserMode(String str) {

        /*
         * USERMODES=diopqrstwxzBDGHIRSTWZ
         */


        String[] strSplit = str.split(" ");

        String modes        = strSplit[0];
        String curMode;

        char modeAction = '+';

        Integer modeIndex  = 0;

        Map<String, String>     userModes = new HashMap<>();

        for (int i=0; i < protocolProps.get("USERMODES").length(); i++) {

        }

        while (modeIndex < modes.length()) {
            curMode = String.valueOf(modes.charAt(modeIndex));

            switch(curMode) {
                case "+": modeAction = '+'; break;
                case "-": modeAction = '-'; break;
            }

            if (curMode.matches("[A-Za-z]") == true) {
                if (curMode.matches("[" + protocolProps.get("USERMODES") + "]")) { userModes.put(modeAction + curMode, ""); }
            }

            modeIndex++;

        }

        return userModes;

    }

    public void addRegChan(Channel c) {
        try { Channel.addRegChannel(c); }
        catch (ItemExistsException e) { log.error(String.format("Error adding channel %s to registered channel list: channel already exists", c)); }
    }

    public void delRegChan(Channel c) {
        Channel.removeRegChannel(c);
    }

    public void sendRaw(Nick fromNick, String str) {

        log.warn(String.format("User %s (with account %s) used RAW command and sent: %s", fromNick.getNick(), fromNick.getAccount().getName(), str));

        client.write(str);
    }

    public void sendNumeric(Nick fromNick, Integer numeric, String str) {

        log.warn(String.format("User %s (with account %s) used RAW command and sent: %s", fromNick.getNick(), fromNick.getAccount().getName(), str));

        client.write(str);
    }

    private void handlePrivmsg(IrcMessage ircMsg) throws Exception {
        log.debug("UnrealIRCd::handlePrivmsg: received a PRIVMSG");
        /* :UID PRIVMSG UID :message */
        String to;

        Nick toUser;

        try {
            //to = rawSplit[2];
            to = ircMsg.getArgv().get(0);
            if (to.contains("@") == true) to = to.split("@")[0];
        }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handlePrivmsg: could not extract 'to' field in raw %s", ircMsg), e); return; }

        /* If the PRIVMSG targets a channel, then we can ignore it */
        if (to.startsWith(Const.USER_ACCOUNT_PREFIX) == true) { log.debug("UnrealIRCd::handlePrivmsg: PRIVMSG targeted a channel => stopping treatment"); return; }

        //try { message = ircMsg.getArgv().get(1); }
        //catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handlePrivmsg: could not extract 'message' field in raw %s", ircMsg), e); return; }

        /* UnrealIRCd uses SID with 3 symbols, UID with 9 symbols */
        //try { fromUser = Nick.getNick(from); }
        //catch (NickNotFoundException e) { log.error(String.format("UnrealIRCd::handlePrivmsg: FROM user %s is not in the userlist", from)); return; }

        /* should match AAABBBBBB, user, user@server */
        try { toUser = Nick.getNick(to); }
        catch (NickNotFoundException e) { log.error(String.format("UnrealIRCd::handlePrivmsg: TO user %s is not in the userlist", to)); return; }

        if (toUser == cservice.getMyUserNode()) {

            /* For the message :foobar PRIVMSG Q :CHANLEV #foo #bar +av */
            /* Sends to CService <from user> as Nick and <message> as String "CHANLEV #foo #bar +av" */

            log.debug(String.format("UnrealIRCd::handlePrivmsg: forwarding to CServe"));

            try { cservice.handleMessage(ircMsg); }
            catch (Exception e) { throw e; } /* Exception is only for CRASH command */
        }

        else if (toUser == oservice.getMyUserNode()) {

            /* For the message :foobar PRIVMSG Q :CHANLEV #foo #bar +av */
            /* Sends to CService <from user> as Nick and <message> as String "CHANLEV #foo #bar +av" */

            log.debug(String.format("UnrealIRCd::handlePrivmsg: forwarding to OperServe"));

            try { oservice.handleMessage(ircMsg); }
            catch (Exception e) { throw e; } /* Exception is only for CRASH command */
        }
    }

    private void handleVersion(IrcMessage ircMsg) {

        log.debug("UnrealIRCd::handleVersion: received a VERSION");

        /*
         * -> <UID> VERSION <server>
         * <- :<server> 351 <dest nick> <version> <server> :<comment>
         * <- :<server> NOTICE <dest nick> :some notices if needed
         * <- :<server> 005 <dest nick> VARIABLE1=value VARIABLE2=value ... :are supported by this server
         * <- :<server> 005 <dest nick> VARIABLExx=value VARIABLEyy=value ... :are supported by this server
         */

        Map<String, String> featuresList = new LinkedHashMap<>();

        Set<String> protoFeatToAdd = Set.of(
            "USERMODES", "CHANMODES", "PREFIX", "BIGLINES", "CHANNELCHARS", "ESVID", "EXTSWHOIS", "MLOCK", "MTAGS", "NEXTBANS", "NICKCHARS", "NICKIP",
            "NICKv2", "NOQUIT", "SJ3", "SJOIN", "SJOIN2", "SJSBY", "TKLEXT", "TKLEXT2", "UMODE2", "VL"
        );

        Nick fromNick;

        String version = Const.QBOT_VERSION_STRING;
        String serverName;
        String systemString = "*";

        Server server;

        StringJoiner featureString = new StringJoiner(" ");

        featuresList.put("NETWORK",                  config.getNetworkName());

        for (String p: protoFeatToAdd) if (protocolProps.containsKey(p)) featuresList.put(p, protocolProps.get(p));

        featuresList.put("Q_SASL",                   String.valueOf(config.hasFeature("sasl")));
        featuresList.put("Q_SVSLOGIN",               String.valueOf(config.hasFeature("svslogin")));
        featuresList.put("Q_CHGHOST",                String.valueOf(config.hasFeature("chghost")));
        featuresList.put("Q_USERMAXCHANS",           String.valueOf(config.getCServeAccountMaxChannels()));
        featuresList.put("Q_USERMAXCERTFP",          String.valueOf(config.getCServeAccountMaxCertFP()));
        featuresList.put("Q_USERMINMAXPASS",         String.format("%s,%s", config.getCServiceAccountMinPassLength(), config.getCServiceAccountMaxPassLength()));
        featuresList.put("Q_CHANBANTIME",            String.valueOf(config.getCserveChanBanTime()));
        featuresList.put("Q_CHANAUTOLIMIT",          String.valueOf(config.getCserveChanAutoLimit()));
        featuresList.put("Q_CHANAUTOLIMITFREQ",      String.valueOf(config.getCServeAutoLimitFreq()));
        featuresList.put("Q_CHANMAXCHANLEVS",        String.valueOf(config.getCServeChanMaxChanlevs()));
        featuresList.put("Q_CHANDEFAULTMODES",       config.getCserveChanDefaultModes());
        featuresList.put("Q_USERACCRANDOM",          String.valueOf(config.hasFeature("randomAccountName")));
        featuresList.put("Q_USERTEMPPASS",           String.valueOf(config.hasFeature("tempAccountPassword")));
        featuresList.put("Q_LOGSTODEBUG",            String.valueOf(config.hasDbgLogging()));
        featuresList.put("Q_LOGSTODB",               String.valueOf(config.getLoggingElasticEnabled()));

        try { serverName = ircMsg.getArgv().get(0); }
        catch (ArrayIndexOutOfBoundsException e) { return; }

        try { server = Server.getServer(serverName); }
        catch (ServerNotFoundException e) {
            return;
        }

        List<String> strResponse = new ArrayList<>();

        try {
            fromNick = ircMsg.getFromNick();
            if (fromNick.isOper() == true) systemString = String.format("[%s %s %s]", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        }
        catch (Exception e) { throw new RuntimeException("Entity probably not a Nick"); }

        strResponse.add(String.format(":%s 351 %s %s %s :%s", server.getName(), ircMsg.getFrom(), version, server.getName(), systemString));

        try {
            fromNick = ircMsg.getFromNick();
            if (fromNick.isOper() == true) strResponse.add(String.format(":%s NOTICE %s :Java: %s %s", server.getName(), ircMsg.getFrom(), System.getProperty("java.runtime.name"), System.getProperty("java.version")));
        }
        catch (Exception e) { throw new RuntimeException("Entity probably not a Nick"); }

        int featureInt = 0;
        for (Map.Entry<String, String> e: featuresList.entrySet()) {

                if (e.getValue().isEmpty() == false) featureString.add(String.format("%s=%s", e.getKey(), e.getValue()));
                else featureString.add(String.format("%s", e.getKey()));

                featureInt++;

                /* Check if we have enough arguments before finishing a batch */
                if (featureInt >= Const.QBOT_VERSION_RESPONSE_MAX_VARIABLES) {
                    strResponse.add(String.format(":%s 005 %s %s :are supported by this server", server.getName(), ircMsg.getFrom(), featureString));
                    featureString = new StringJoiner(" ");
                    featureInt = 0;
                }
        }

        /* Add the rest when the number of arguments if less than the limit*/
        if (featureInt < Const.QBOT_VERSION_RESPONSE_MAX_VARIABLES) strResponse.add(String.format(":%s 005 %s %s :are supported by this server", server.getName(), ircMsg.getFrom(), featureString));

        /*
         * strResponse.add(String.format(":%s NOTICE %s :%s", serverName, ircMsg.getFrom(), "Test version"));
         */

        for (String s: strResponse) write(client, s);
    }

    private void handleStats(IrcMessage ircMsg) {

        log.debug("UnrealIRCd::handleStats: received a STATS");

        // TODO: to finish implement

        /*
         * -> <UID> STATS [ <query> [ <server> ] ]
         * No/unknown query:
         * <- :<server> 210 <dest nick> :<text> (unrealircd) or :<server> NOTICE <nick> :<text> because 210 is not standard
         * <- :<server> 219 <dest nick> * :End of /STATS report
         *
         * Query:
         * <- :<server> xxx <dest nick> :<text>
         * <- :<server> 219 <dest nick> <arg> :End of /STATS report
         */

        /*
         * RFC 2812
         *
         * Except for the ones below, the list of valid queries is
         * implementation dependent.  The standard queries below SHOULD be
         * supported by the server:
         *
         *          l - returns a list of the server's connections, showing how
         *              long each connection has been established and the
         *              traffic over that connection in Kbytes and messages for
         *              each direction;
         *          m - returns the usage count for each of commands supported
         *              by the server; commands for which the usage count is
         *              zero MAY be omitted;
         *          o - returns a list of configured privileged users,
         *              operators;
         *          u - returns a string showing how long the server has been
         *              up.
         *
         * It is also RECOMMENDED that client and server access configuration be
         * published this way.
         *
         * Numeric Replies:
         *
         *         ERR_NOSUCHSERVER  (402)   <server> :<reason>
         *         RPL_STATSLINKINFO (211)   <linkname> <sendq> <sent_msgs> <sent_bytes> <recvd_msgs> <rcvd_bytes> <time_open>
         *         RPL_STATSUPTIME   (242)   :Server Up <days> days <hours>:<minutes>:<seconds>
         *         RPL_STATSCOMMANDS (212)   <command> <count> [<byte_count> <remote_count>]
         *         RPL_STATSOLINE    (243)   O <hostmask> * <nick> [:<info>]
         *         RPL_ENDOFSTATS    (219)   <query> :<info>
         *                           (481)   :server 481 nick :Permission Denied: Insufficient privileges
         */



        Server myServer;
        String serverName;
        String query;

        myServer = Server.getServerBySid(config.getServerId());
        serverName = myServer.getSid();

        String permDenied;
        Nick nickFrom = (Nick) ircMsg.getFrom();

        if (nickFrom.isOper() == false) {
            permDenied = String.format(":%s 481 %s :Permission Denied: Insufficient privileges", serverName, ircMsg.getFrom());
            write(client, permDenied);
            return;
        }

        int uptimeDay  = 0;
        int uptimeHour = 0;
        int uptimeMin  = 0;
        int uptimeSec  = 0;

        long now    = Timestamp.value().getValue();
        long uptime = now - Qbot.BOOT_TIME;

        try { query = ircMsg.getArgv().get(0); }
        catch (IndexOutOfBoundsException e) { return ; }



        List<String> strResponse = new ArrayList<>();

        switch (query) {

            case "u", "uptime": {
                uptimeDay = (int) TimeUnit.SECONDS.toDays(uptime);
                uptimeHour = (int) TimeUnit.SECONDS.toHours(uptime) - (uptimeDay *24);
                uptimeMin = (int) (TimeUnit.SECONDS.toMinutes(uptime) - (TimeUnit.SECONDS.toHours(uptime)* 60));
                uptimeSec = (int) (TimeUnit.SECONDS.toSeconds(uptime) - (TimeUnit.SECONDS.toMinutes(uptime) *60));

                strResponse.add(String.format(":%s 242 %s :Server Up %s days, %s:%s:%s", serverName, ircMsg.getFrom(), uptimeDay, uptimeHour, uptimeMin, uptimeSec));
                strResponse.add(String.format(":%s 250 %s :Highest connection count: %s", serverName, ircMsg.getFrom(), myServer.getLocalUsers().size()));
                break;
            }

            case "l", "links":
            case "m", "commands":
            case "o", "operators": {
                strResponse.add(String.format(":%s NOTICE %s :%s", serverName, ircMsg.getFrom(), "Currently not implemented."));
                break;
            }

            default: {
                strResponse.add(String.format(":%s NOTICE %s :%s", serverName, ircMsg.getFrom(), "l (links)      - Current connections information."));
                strResponse.add(String.format(":%s NOTICE %s :%s", serverName, ircMsg.getFrom(), "m (commands)   - Message usage information."));
                strResponse.add(String.format(":%s NOTICE %s :%s", serverName, ircMsg.getFrom(), "o (operators)  - Operator information."));
                strResponse.add(String.format(":%s NOTICE %s :%s", serverName, ircMsg.getFrom(), "u (uptime)     - Current uptime & highest connection count."));
            }
        }


        strResponse.add(String.format(":%s 219 %s %s :End of /STATS report", serverName, ircMsg.getFrom(), query));

        for (String s: strResponse) write(client, s);
    }

    private void handleSid(String raw) {
        /*
         * SID is used to introduce the other servers
         * :sid1 SID name hop sid2 :description
         */

        log.debug("UnrealIRCd::handleSid: received a SID");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 6);

        String fromSid;
        String name;
        String newSid;
        String description;

        Integer hopCount;

        Server fromServer;
        Server newServer;

        try { fromSid = rawSplit[0].replaceAll("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSid: could not extract 'from' field in raw %s", raw), e); return; }

        try { name = rawSplit[2]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSid: could not extract 'name' field in raw %s", raw), e); return; }

        try { hopCount = Integer.valueOf(rawSplit[3]); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSid: could not extract 'hopCount' field in raw %s", raw), e); return; }

        try { newSid = rawSplit[4]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSid: could not extract 'newSid' field in raw %s", raw), e); return; }

        try { description = rawSplit[5]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSid: could not extract 'description' field in raw %s", raw), e); return; }

        fromServer = Server.getServerBySid(fromSid);

        newServer = new Server.Builder(name, newSid)
            .distance(hopCount)
            .description(description)
            .parentNode(fromServer)
            .build();

        fromServer.addChildren(newServer);

        try { Server.addServer(newServer); }
        catch (ItemExistsException e) {
            log.error(String.format("UnrealIRCd::handleSid: cannot register server %s (%s) because it has been already registered.", newServer.getName(), newServer.getSid()), e);
        }

        log.debug(String.format("UnrealIRCd::handleSid: registered server %s (%s) with parent %s (%s)", name, newSid, fromServer.getName(), fromSid));
    }

    private void handlePing(String raw) {
        /*
         * PING :message
         */

        log.trace("UnrealIRCd::handlePing: PING?");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 2);

        String pingMsg;
        String strResponse;

        try { pingMsg = rawSplit[1].replaceAll("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSid: could not extract 'pingMsg' field in raw %s", raw), e); return; }

        strResponse = String.format("PONG :%s", pingMsg);
        write(client, strResponse);

        log.trace("UnrealIRCd::handlePing: PONG!");

    }

    private void handleMd(String raw) {
        /*
         * raw = :from MD client nick_or_uid_or_sid variable value
         * ex:   :AAA MD client lynx. saslmechlist :EXTERNAL,PLAIN
         */

        /* Country: <<< :5P0 MD client 5PX5B1Y02 geoip :cc=FR|cd=France */

        log.debug("UnrealIRCd::handleMd: received a MD message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 6);

        String from;
        String mdSubject;
        String mdVar;
        String mdVal;

        Nick mdSubjectUser = null;

        //ServerNode fromServer;
        Server mdSubjectServer = null;

        try { from = rawSplit[0].replaceAll("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleMd: could not extract 'from' field in raw %s", raw), e); return; }

        try { mdSubject = rawSplit[3]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleMd: could not extract 'md subject' field in raw %s", raw), e); return; }

        try { mdVar = rawSplit[4].replaceAll("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleMd: could not extract 'md variable' field in raw %s", raw), e); return; }

        try { mdVal = rawSplit[5].replaceAll("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { mdVal = ""; }

        //try { fromServer = serverList.get(from); }
        //catch (Exception e) { log.error(String.format("UnrealIRCd::handleMd: could not get from Server %s as a server node from its SID. raw: %s", from , raw), e); return; }

        if (mdSubject.length() == 3) {
            /* MD subject is a server */
            try { mdSubjectServer = Server.getServerBySid(mdSubject); }
            catch (Exception e) { log.error(String.format("UnrealIRCd::handleMd: could not get MD subject %s as a server node from its SID. raw: %s", mdSubject , raw), e); return; }
        }
        else if (mdSubject.length() == 9) {
            /* MD subject is a user */
            try { mdSubjectUser = Nick.getUserByUid(mdSubject); }
            catch (Exception e) { log.error(String.format("UnrealIRCd::handleMd: could not get MD subject %s as an user node from its UID. raw: %s", mdSubject , raw), e); return; }
        }
        else {
            /* MD subject is of an unknown type */
            log.error(String.format("UnrealIRCd::handleMd: MD subject %s is unknown type. raw: %s", mdSubject , raw)); return;
        }

        switch(mdVar) {
            case "certfp":
                if (mdSubjectUser != null) {
                    mdSubjectUser.setCertFP(mdVal);
                    log.info(String.format("UnrealIRCd::handleMd: Assigning CERTFP value %s to user %s", mdVal , mdSubjectUser.getNick())); return;
                }
                else if (mdSubjectServer != null) {
                    mdSubjectServer.setCertFP(mdVal);
                    log.info(String.format("UnrealIRCd::handleMd: Assigning CERTFP value %s to server %s", mdVal , mdSubjectServer.getName())); return;
                }
            break;

            case "operlogin":
                if (mdSubjectUser != null) {
                    mdSubjectUser.setOperLogin(mdVal);
                    log.info(String.format("UnrealIRCd::handleMd: Assigning OPERLOGIN value `%s` to user %s", mdVal , mdSubjectUser.getNick())); return;
                }

            break;

            case "operclass":
                if (mdSubjectUser != null) {
                    mdSubjectUser.setOperClass(mdVal);
                    log.info(String.format("UnrealIRCd::handleMd: Assigning OPERCLASS value `%s` to user %s", mdVal , mdSubjectUser.getNick())); return;
                }

            break;

            case "geoip": /* mdVal = cc=FR|cd=France */
                if (mdSubjectUser != null) {
                    mdSubjectUser.setCountry(mdVal);
                    log.info(String.format("UnrealIRCd::handleMd: Assigning COUNTRY value `%s` to user %s", mdVal , mdSubjectUser.getNick())); return;
                }
                else if (mdSubjectServer != null) {
                    mdSubjectServer.setCountry(mdVal);
                    log.info(String.format("UnrealIRCd::handleMd: Assigning COUNTRY value `%s` to server %s", mdVal , mdSubjectServer.getName())); return;
                }

            break;

            default:
                log.debug(String.format("UnrealIRCd::handleMd: Ignoring unhandled MD property for %s sent by %s: %s -> %s", mdSubject, from, mdVar, mdVal));
            break;
        }

    }

    private void handleNetInfo(String raw) {
        /*
         * :sid NETINFO maxGlobal currentTime protocolVersion cloakHash 0 0 0 :networkName
         */

        log.debug("UnrealIRCd::handleNetInfo: received a NETINFO message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 10);

        String maxGlobal;
        String strResponse;

        Long unixTime = Instant.now().getEpochSecond();;

        try { maxGlobal = rawSplit[1]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleNetInfo: could not extract 'maxGlobal' field in raw %s", raw), e); return; }

        strResponse = String.format(":%s NETINFO %s %s %s * 0 0 0 :%s", config.getServerId(), maxGlobal, unixTime, Const.UNREAL_PROTOCOL_VERSION, config.getNetworkName());

        write(client, strResponse);

        log.info(String.format("UnrealIRCd::handleNetInfo: sent NETINFO"));

        /* Sending that we can handle SASL (in enabled in the config) */
        if (config.hasFeature("sasl") == true) {
            strResponse = String.format(":%s MD client %s saslmechlist :EXTERNAL,PLAIN", config.getServerId(), config.getServerName());
            write(client, strResponse);
            log.info(String.format("UnrealIRCd::handleNetInfo: sent MD to advertise SASL support"));
        }
    }

    private void handleNick(String raw) {
        /*
         * :aaaaaaaaa NICK newnick
         */

        log.debug("UnrealIRCd::handleNick: received a NICK message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 4);

        String from;
        String newNick;

        Nick fromUser;

        try { from = rawSplit[0].replaceAll("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleNick: could not extract 'from' field in raw %s", raw), e); return; }

        try { newNick = rawSplit[2]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleNick: could not extract 'to' field in raw %s", raw), e); return; }

        /* UnrealIRCd uses UID with 9 symbols */
        if (from.length() == 9) {
            try { fromUser = Nick.getUserByUid(from); }
            catch (ItemNotFoundException e) { log.error(String.format("UnrealIRCd::handleNick: FROM user %s is not in the userlist", from)); return; }
        }
        else { log.error(String.format("UnrealIRCd::handleNick: FROM entity (%s) is not an user %s", from, raw)); return; }

        fromUser.changeNick(newNick);

        log.debug(String.format("UnrealIRCd::handleNick: user %s changed nick %s -> %s", fromUser.getUid(), fromUser.getLastNick(), fromUser.getNick()));

    }

    private void handleTopic(String raw, Boolean bursting) {

        String channelName;
        String topicWho;
        String topicTxt;

        Timestamp topicWhen = new Timestamp();

        Channel channel;
        Topic topic;

        if (bursting == true) {
            /* TOPIC #chan w!h@h ts :topic */

            log.debug("UnrealIRCd::handleTopicBursting: received a TOPIC message (bursting)");
            if (raw.isEmpty() == true) return;

            String[] rawSplit = raw.split(" ", 5);

            try { channelName = rawSplit[1]; }
            catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleTopicBursting: could not extract 'channel' field in raw %s", raw), e); return; }

            try { topicWho = rawSplit[2]; }
            catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleTopicBursting: could not extract 'topicWho' field in raw %s", raw), e); return; }

            try { topicWhen.setValue(Long.valueOf(rawSplit[3])); }
            catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleTopicBursting: could not extract 'topicWhen' field in raw %s", raw), e); return; }

            try { topicTxt = rawSplit[4].replaceAll("^[:]", ""); }
            catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleTopicBursting: could not extract 'topicTxt' field in raw %s", raw), e); return; }

            /* Normally TOPIC is always sent after SJOIN, so the channel should be created. */
            try { channel = getChannelNodeByNameCi(channelName); }
            catch (ItemNotFoundException e) { log.error(String.format("UnrealIRCd::handleTopicBursting: could set channel node for raw %s", raw), e); return;  }

            topic = new Topic(topicTxt, topicWho, topicWhen);
            channel.setTopic(topic);

            log.debug(String.format("UnrealIRCd::handleTopicBursting: updated topic for %s", channelName));
        }

        else if (bursting == false) {

            /* :sid TOPIC #chan w!h@h ts :topic */

            log.debug("UnrealIRCd::handleTopicNotBursting: received a TOPIC message (not bursting)");
            if (raw.isEmpty() == true) return;

            String[] rawSplit = raw.split(" ", 6);

            try { channelName = rawSplit[2]; }
            catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleTopicNotBursting: could not extract 'channel' field in raw %s", raw), e); return; }

            try { topicWho = rawSplit[3]; }
            catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleTopicNotBursting: could not extract 'topicWho' field in raw %s", raw), e); return; }

            try { topicWhen.setValue(Long.valueOf(rawSplit[4])); }
            catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleTopicNotBursting: could not extract 'topicWhen' field in raw %s", raw), e); return; }

            try { topicTxt = rawSplit[5].replaceAll("^[:]", ""); }
            catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleTopicNotBursting: could not extract 'topicTxt' field in raw %s", raw), e); return; }

            /* Normally TOPIC is always sent after SJOIN, so the channel should be created. */
            try { channel = getChannelNodeByNameCi(channelName); }
            catch (ItemNotFoundException e) { log.error(String.format("UnrealIRCd::handleTopicNotBursting: could set channel node for raw %s", raw), e); return;  }


            topic = new Topic(topicTxt, topicWho, topicWhen);
            channel.setTopic(topic);

            try { cservice.handleTopic(channel); }
            catch (Exception e) { /* CServe not yet connected */ return; }

            log.debug(String.format("UnrealIRCd::handleTopicNotBursting: updated topic for %s", channelName));
        }

    }

    private void handleUid(String raw) {
        // :sid UID nickname hopcount timestamp username hostname uid servicestamp usermodes virtualhost cloakedhost ip :gecos

        log.debug("UnrealIRCd::handleUid: received a UID message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 14);

        UserAccount accountToReauth = null;

        Nick user;

        Server userServer;

        String fromServerSid;
        String ident;
        String nick;
        String vhost;
        String realHost;
        String gecos;
        String uid;
        String modes;
        String cloakedHost;
        String ipAddress;

        Map<String, String> modesMap;
        Map<String, String> modesMapTrimmed = new TreeMap<>();

        Long ts;

        try { fromServerSid = rawSplit[0].replaceAll("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'fromServerSid' field in raw %s", raw), e); return; }

        try { nick = rawSplit[2]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'nick' field in raw %s", raw), e); return; }

        try { ts = Long.valueOf(rawSplit[4]); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'ts' field in raw %s", raw), e); return; }

        try { ident = rawSplit[5]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'ident' field in raw %s", raw), e); return; }

        try { realHost = rawSplit[6]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'realHost' field in raw %s", raw), e); return; }

        try { uid = rawSplit[7]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'uid' field in raw %s", raw), e); return; }

        try { modes = rawSplit[9]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'modes' field in raw %s", raw), e); return; }

        try { vhost = rawSplit[10]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'vhost' field in raw %s", raw), e); return; }

        try { cloakedHost = rawSplit[11]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'cloakedHost' field in raw %s", raw), e); return; }

        try { ipAddress = rawSplit[12]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'ipAddress' field in raw %s", raw), e); return; }

        try { gecos = rawSplit[13].replaceAll("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'gecos' field in raw %s", raw), e); return; }

        try { userServer = Server.getServerBySid(fromServerSid); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'userServer' field in raw %s", raw), e); return; }

        modesMap = parseUserMode(modes);

        /*
         * Reformats the modes list to remove prepend + char
         */
        modesMap.forEach( (mode, parameter) -> {
            if (mode.startsWith("+")) modesMapTrimmed.put(String.valueOf(mode.charAt(1)), parameter);
        });


        /* If user not found => creating it */
        if (Nick.getUserByUid(uid) == null) {

            user = new Nick.Builder()
                    .uid(uid)
                    .nick(nick)
                    .ident(ident)
                    .host(vhost)
                    .realHost(realHost)
                    .realName(gecos)
                    .userTS(ts)
                    .server(userServer)
                    .cloakedHost(cloakedHost)
                    .modes(modesMapTrimmed)
                    .build();

            user.setIpAddress(ipAddress);

            log.info(String.format("UnrealIRCd::handleUid: user id %s does not exist in the user list => creating it", user));
        }

        /* If user already exists (likely because of SASL auth), updating its details */
        else {
            user = Nick.getUserByUid(uid);

            user.changeNick(nick);
            user.setIdent(ident);
            user.setHost(vhost);
            user.setRealHost(realHost);
            user.setRealName(gecos);
            user.setUserTS(ts);
            user.setUserModes(modesMapTrimmed);
            user.setCloakedHost(cloakedHost);
            user.setIpAddress(ipAddress);
            user.setServer(userServer);

            log.info(String.format("UnrealIRCd::handleUid: user id %s (%s) already exist in the user list (SASL auth) => updating its details", user.getUid(), user));

            /* Section to update auth token in the db if the user was authed using SASL, because in this case their TS and ident was unknown */
            /* Also a good place to set the vhost */
            //@s2s-md/creationtime=1685464827 :5PK UID plop 0 1685464824 plop desktop-lpvlp15 5PKE08M08 0 +iwx cloak/-F9228E5A cloak/-F9228E5A wKgKGA== :...

            if (user.isAuthBySasl() == true) {
                try { database.updateUserAuth(user); }
                catch (ItemNotFoundException e) { log.info(String.format("UnrealIRCd::handleUid: user does not have a login token raw %s", raw), e); return; }
                catch (Exception e) { log.error(String.format("UnrealIRCd::handleUid: error updating auth for raw %s", raw), e); return; }
            }
        }

        userServer.addLocalUser(user);

        if (this.isNetBursting == true) {
            /* Trying to reauthenticate the user if it was already authed (netjoin), only during sync */
            try {
                log.info(String.format("UnrealIRCd::handleUid: trying to reauthenticate user %s (uid %s - ts %s)", user, user.getUid(), user.getUserTS()));
                accountToReauth = database.getUserLoginToken(user);
            }
            catch (Exception e) { /* If we are thrown an exception here, it is most likely because the user does not have an auth token anymore, in this case we do nothing */
                log.debug(String.format("UnrealIRCd::handleUid: no authentication token for user %s", user, accountToReauth));
            }

            if (accountToReauth != null) {

                log.info(String.format("UnrealIRCd::handleUid: found authentication token for user %s, reauthenticating to account %s", user, accountToReauth));

                user.setAuthed(true);
                user.setAccount(accountToReauth);

                if (config.hasFeature("svslogin") == true) { this.sendSvsLogin(user, user.getAccount()); }

                if (Flags.isUserAutoVhost(user.getAccount().getFlags()) == true && config.hasFeature("chghost") == true) { this.chgHostVhost(user, user.getAccount().getName()); }

                try { database.addUserAuth(user, Const.AUTH_TYPE_REAUTH_PLAIN); }
                catch (Exception e) { log.error(String.format("UnrealIRCd::handleUid: error add auth for raw %s", raw), e); return; }
            }
        }
    }

    private synchronized void handleSasl(String raw) {
        /*
         * SASL auth
         * =========
         *
         * <<< :ocelot. SASL lynx. 5P0QVW5M3 H 2401:d800:7e60:5bb:21e:10ff:fe1f:0 2401:d800:7e60:5bb:21e:10ff:fe1f:0
         * <<< :ocelot. SASL lynx. 5P0QVW5M3 S PLAIN
         * >>> :5PBAAAAAF SASL ocelot. 5P0QVW5M3 C +
         * <<< :ocelot. SASL lynx. 5P0QVW5M3 C <base64("Login" + "Login" + "Pass")>
         * Fail:
         * >>> :5PBAAAAAF SASL ocelot. 5P0IFAZM2 D F
         * Success:
         * >>> :5PB SVSLOGIN ocelot. 5P0QVW5M3 AnhTay
         * >>> :5PBAAAAAF SASL ocelot. 5P0QVW5M3 D S
         * <<< @s2s-md/geoip=cc=VN|cd=Vietnam;s2s-md/tls_cipher=TLSv1.3-TLS_CHACHA20_POLY1305_SHA256;s2s-md/creationtime=1683972418 :5P0 UID AnhTay_ 0 1683972414 ~anh 2401:d800:7e60:5bb:21e:10ff:fe1f:0 5P0QVW5M3 AnhTay +iwxz D0230F10:F347CFC4:51CFDDA3:IP D0230F10:F347CFC4:51CFDDA3:IP JAHYAH5gBbsCHhD//h8AAA== :Toi La Anh
         * >>> :5PB CHGHOST 5P0QVW5M3 user/AnhTay
         *
         * 1.  user_sname -> auth_sname H <realhost> <ip> <P if plaintext connection>
         *
         * 2a. user_sname -> auth_sname S PLAIN
         * 2b. user_sname -> auth_sname S EXTERNAL <certfp>
         *
         * 3.  auth_uid -> user_uid C +
         *
         * 4a. user_sname -> auth_uid C <base64(login\0login\0pass)>
         * 4b. user_sname -> auth_uid C +
         *
         * 5a. auth_uid -> user_uid D S  (success)
         * 5b. auth_uid -> user_sid D F  (failure)
         */

        log.debug("UnrealIRCd::handleSasl: received a SASL message");
        if (raw.isEmpty() == true) return;

        if (config.hasFeature("sasl") == false) { log.debug("UnrealIRCd::handleSasl: not treating SASL message because the feature is disabled in the configuration file."); return; }

        String[] rawSplit = raw.split(" ");

        String userUid              = "";
        String userHost             = "";
        String saslSequence         = "";
        String saslAuthType         = "";
        String saslAuthChallenge    = "";
        String saslAuthExtChallenge = "";
        String authString;

        Boolean isAuthOnPlainConnection = true;

        //ServerNode fromServer;
        //ServerNode userServer;

        Nick user;

        UserAccount userAccountToAuth;

        byte[] decodedAuthString;

        Integer authType;

        /* Fields present in every SASL messages at the same position */

        /*
         * Hopefully we don't need these informations
         *
         * try { fromServerName = rawSplit[0].replaceAll("^[:]", ""); }
         * catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSasl: could not extract 'fromServerName' field in raw %s", raw), e); return;
         * try { fromServer = serverListByName.get(fromServerSid); }
         * catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUid: could not extract 'userServer' field in raw %s", raw), e); return;
         * try { userServerName = rawSplit[2]; }
         * catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSasl: could not extract 'fromServerName' field in raw %s", raw), e); return; }
         */

        try { userUid = rawSplit[3]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSasl: could not extract 'userUid' field in raw %s", raw), e); return; }

        try { saslSequence = rawSplit[4]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSasl: could not extract 'saslSequence' field in raw %s", raw), e); return; }


        /* Fields depending on the SASL sequence */
        switch (saslSequence) {
            case "H":
                try { userHost = rawSplit[5]; }
                catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSasl: could not extract 'userHost' field in raw %s", raw), e); return; }

                //try { userIp = rawSplit[6]; }
                //catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSasl: could not extract 'userIp' field in raw %s", raw), e); return; }

                try { if (rawSplit[7].equals("P")) isAuthOnPlainConnection = true; }
                catch (IndexOutOfBoundsException e) { isAuthOnPlainConnection = false; }

                user = new Nick.Builder().uid(userUid).realHost(userHost).build();
                user.setConnPlainText(isAuthOnPlainConnection);
                user.setAuthBySasl();

                log.debug(String.format("UnrealIRCd::handleSasl: SASL (1) user id %s has connected and try to auth by SASL", user.getUid()));
            break;

            case "S":
                try { saslAuthType = rawSplit[5]; }
                catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSasl: could not extract 'saslAuthType' field in raw %s", raw), e); return; }

                if (saslAuthType.equals("EXTERNAL")) {
                    try { saslAuthExtChallenge = rawSplit[6]; }
                    catch (IndexOutOfBoundsException e) { saslAuthExtChallenge = ""; }
                }

                user = Nick.getUserByUid(userUid);
                user.setSaslAuthParam("authType", saslAuthType);
                if (saslAuthType.equals("EXTERNAL")) {
                    try {
                        user.setSaslAuthParam("authExt", saslAuthExtChallenge);
                        user.setCertFP(saslAuthExtChallenge);
                    }
                    catch (Exception e) {
                        log.warn("User is trying SASL EXTERNAL but does not provide certfp", e);
                        user.setSaslAuthParam("authExt", "");
                    }
                }
                user.setSaslAuthParam("authServer", userUid);

                this.sendSaslQuery(user);

                log.debug(String.format("UnrealIRCd::handleSasl: SASL (2) user id %s wants to auth via %s", user.getUid(), saslAuthType));

            break;

            case "C":
                try { saslAuthChallenge = rawSplit[5]; }
                catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSasl: could not extract 'saslAuthChallenge' field in raw %s", raw), e); return; }

                user = Nick.getUserByUid(userUid);

                if (user.isConnPlainText() == true && config.hasFeature("denyauthplainconn") == true) {
                    /* User is logging from plain text connection */
                    log.info("User auths over a plain text connection but my configuration says we can't do that => failing the auth.");
                    this.sendNotice(cservice.getMyUserNode(), user, "You are not allowed to authenticate over a plain text connection.");
                    this.sendSaslResult(user, false);
                    return;
                }

                if (user.getSaslAuthParam("authType").equals("EXTERNAL") == true && saslAuthChallenge.equals("+") == true) { // SASL EXTERNAL auth
                    authType = Const.AUTH_TYPE_SASL_EXT;
                    log.debug("UnrealIRCd::handleSasl: SASL (3) performing SASL auth type EXTERNAL");

                    String authCertFp = user.getSaslAuthParam("authExt");

                    if (authCertFp.isEmpty() == true) {
                        /* No certfp provided => auth failure */
                        log.info("User did not provide certfp => failing the auth.");
                        this.sendSaslResult(user, false);
                        return;
                    }

                    /* SASL EXTERNAL => no username provided => need to find in all the accounts to which belongs the certfp */
                    log.debug("UnrealIRCd::handleSasl: performing user certfp lookup for EXTERNAL");
                    var wrapper = new Object() { UserAccount userAccountToAuth = null; };

                    UserAccount.getUserList().forEach( (useraccount) -> {
                        /* Checks against the issues certfp and also check if the account is not suspended */
                        if (useraccount.getCertFP().contains(authCertFp) == true && Flags.isUserSuspended(useraccount.getFlags()) == false) {
                            wrapper.userAccountToAuth = useraccount;
                            log.debug("UnrealIRCd::handleSasl: found a match");
                        }
                    });

                    userAccountToAuth = wrapper.userAccountToAuth;

                    if (userAccountToAuth != null) { // Certfp found => Auth successful

                        try { userAccountToAuth.authUserToAccount( user,  authCertFp,  authType); }
                        catch (Exception e) { this.sendSaslResult(user, false); return; }

                        this.sendSaslResult(user, true);

                        if (config.hasFeature("svslogin") == true) { this.sendSvsLogin(user, userAccountToAuth); }

                        if (Flags.isUserAutoVhost(user.getAccount().getFlags()) == true && config.hasFeature("chghost") == true) { this.chgHostVhost(user, user.getAccount().getName()); }

                        userAccountToAuth.getChanlev().forEach( (channel, chanlev) -> {
                            if (Flags.isChanLAutoInvite(chanlev) == true) { this.sendInvite(user, this.getChannelNodeByNameCi(channel)); }
                        });

                        log.debug("UnrealIRCd::handleSasl: auth successful");

                        return;
                    }
                    else { // Certfp not found => Auth fail
                        this.sendSaslResult(user, false);
                        log.debug("UnrealIRCd::handleSasl: auth failed");
                        return;
                    }
                }

                else if (user.getSaslAuthParam("authType").equals("PLAIN") == true) { // SASL PLAIN auth
                    Base64.Decoder dec = Base64.getDecoder();
                    decodedAuthString = dec.decode(saslAuthChallenge);
                    authString = new String(decodedAuthString);

                    String[] authStringItems = authString.split("\0", 3);
                    String login = "";
                    String password = "";
                    authType = Const.AUTH_TYPE_SASL_PLAIN;

                    try {
                        /* Separate items from login string */
                        if (authStringItems[0].equals(authStringItems[1])) login = authStringItems[0];
                        password = authStringItems[2];
                    }
                    catch (Exception e) {
                        log.error(String.format("UnrealIRCd::getResponse: unknown error with SASL plain login"), e);
                        this.sendSaslResult(user, false);
                        return;
                    }

                    try { userAccountToAuth = UserAccount.getUserByNameCi(login); }
                    catch (ItemNotFoundException e) { /* Account does not exist => fail */
                        this.sendSaslResult(user, false);
                        log.debug(String.format("UnrealIRCd::handleSasl: Authentication for user id %s for account %s ended with error (user account not found)", user.getUid(), login));
                        return;
                    }

                    log.debug(String.format("UnrealIRCd::handleSasl: SASL (3) performing PLAIN authentication for user id %s for account %s", user.getUid(), userAccountToAuth));

                    try { userAccountToAuth.authUserToAccount(user, password, authType); }
                    catch (Exception e) { this.sendSaslResult(user, false); return; }

                    if (user.isAuthed() == false) { /* Auth failed */
                        this.sendSaslResult(user, false);
                        return;
                    }

                    /* Auth success */
                    this.sendSaslResult(user, true);
                    if (config.hasFeature("svslogin") == true) { this.sendSvsLogin(user, userAccountToAuth); }

                    if (Flags.isUserAutoVhost(user.getAccount().getFlags()) == true && config.hasFeature("chghost") == true) { this.chgHostVhost(user, user.getAccount().getName()); }

                    userAccountToAuth.getChanlev().forEach( (channel, chanlev) -> {
                        if (Flags.isChanLAutoInvite(chanlev) == true) { this.sendInvite(user, this.getChannelNodeByNameCi(channel)); }
                    });
                }
                else {
                    log.info(String.format("UnrealIRCd::handleSasl: SASL (3) user is trying to auth with unsupported SASL machanism (%s)", user.getSaslAuthParam("authType")));
                    this.sendSaslResult(user, false);
                    return;
                }

            break;
        }
    }

    private void handleSjoin(String raw) {
        // XXX: to rewrite

        // :5P0 SJOIN 1680362593 #mjav         +fnrtCPST [5j#R1,7m#M1,3n#N1,5t#b1]:6         :5PKEPJH3U @5PXDR1D20 @5P0FWO841
        // :5P0 SJOIN 1681424518 #Civilization +fnrtCHPS [30j#R10,40m#M10,10n#N15]:15 50:15m :@5PX8ZA302 @5PBAAAAAI &test!*@* "test!*@* 'test!*@*
        // :5PX SJOIN 1679224907 #test                                                       :5PX8ZA302
        // :5PX SJOIN 1683480448 #newChan                                                    :@5PX8ZA302

        /*
         * SJOIN prefixes (unrealircd)
         *   + -> +v
         *   % -> +h
         *   @ -> +o
         *   ~ -> +a
         *   * -> +q
         *   & -> +b
         *   " -> +e
         *   ' -> +I
         */

        log.debug("UnrealIRCd::handleSJoin: received a SJOIN message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit         = raw.split(" ");
        String[] rawSplitModes    = raw.split(" ", 5);
        String[] command          = raw.split(" ", 3);
        String[] sjoinParam       = command[2].split(" ", 64);
        String[] sJoinParameters  = command[2].split(" ", 3)[2].split(" ");

        //String fromServerSid;
        String sJoinChan     = (command[2].split(":", 2) [0]).split(" ", 3) [1];
        String sJoinModes    = "";
        String sJoinList     = "";
        String chanListItem  = "";
        String joinMode      = "";

        Channel chan;
        Nick user = null;

        Long sJoinTS;
        Timestamp sJoinTSTS;

        Map<String, Map<String, String>>   modChanModesAll;

        Map<String, String>      modChanModes;

        Bei mask;

        sJoinTS = Long.parseLong(sjoinParam[0]);


        try { sJoinChan = rawSplit[3]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSJoin: could not extract 'sJoinChan' field in raw %s", raw), e); return; }

        try { sJoinTS = Long.parseLong(rawSplit[2]); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSJoin: could not extract 'sJoinTS' field in raw %s", raw), e); return; }

        try { if (rawSplit[4].startsWith("+") == true) { sJoinModes = rawSplitModes[4]; } }
        catch (IndexOutOfBoundsException e) { }

        try { sJoinChan = rawSplit[3]; }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleSJoin: could not extract 'sJoinChan' field in raw %s", raw), e); return; }

        /*
         * command[2]                       = 1678637814 #thibland +fmnrstzCPST [5j#R1,7m#M1,3n#N1,5t#b1]:6 :@5P0PWVF03
         * command[2].split(, 3)            = 1678637814/#thibland/+fmnrstzCPST [5j#R1,7m#M1,3n#N1,5t#b1]:6 :@5P0PWVF03
         * command[2].split(, 3)[2]         = +fmnrstzCPST [5j#R1,7m#M1,3n#N1,5t#b1]:6 :@5P0PWVF03
         * command[2].split(, 3)[2].split() = +fmnrstzCPST/[5j#R1,7m#M1,3n#N1,5t#b1]:6/:@5P0PWVF03
         */

        try { modChanModesAll  = this.parseChanModes(sJoinModes); }
        catch (ParseException e) { log.error(String.format("UnrealIRCd::handleSjoin: error while parsing modes: %s", sJoinModes)); return; }

        modChanModes   =  modChanModesAll.get("chanModes");

        sJoinTSTS = new Timestamp(sJoinTS);

        Boolean isModeSection = true;
        for (String param: sJoinParameters) {
            if (param.startsWith(":") == true) isModeSection = false;

            if (isModeSection == true) sJoinModes = String.join(" ", sJoinModes, param);
            else sJoinList = String.join(" ", sJoinList, param.replaceAll("^[:]", ""));
        }

        log.debug(String.format("UnrealIRCd::SJOIN: SJOIN (raw) for %s received modes: %s", sJoinChan, sJoinModes));
        log.debug(String.format("UnrealIRCd::SJOIN: SJOIN (raw) for %s received list: %s", sJoinChan, sJoinList));

        /* If user joins a registered channel, just add it to the live channels */
        if (Channel.isRegChan(sJoinChan) == true && Channel.isChan(sJoinChan) == false) {
            log.info(String.format("UnrealIRCd::handleSjoin: detected SJOIN into a registered channel => copying to live channels list", sJoinChan));
            try { Channel.addChannel(Channel.getRegChanByNameCi(sJoinChan)); }
            catch (ItemExistsException e) { log.error(String.format("UnrealIRCd::handleSjoin: cannot add registered channel object %s to live channel list because it already exists.", sJoinChan)); }
        }

        /* channel does not exist on the network and is not registered => creating it */
        if (Channel.isChan(sJoinChan) == false) {
            chan = new Channel.Builder()
                              .name(sJoinChan)
                              .registrationTS(sJoinTSTS)
                              .build();

            log.info(String.format("UnrealIRCd::SJOIN: creating channel %s (TS %s)", chan.getName(), chan.getRegistrationTS()));
            try {
                Channel.addChannel(chan);
                chan.setChanlev(database.getChanChanlev(chan));
            }
            catch (Exception e) { log.error(String.format("UnrealIRCd::handleSJoin: Aborting. Could not get chanlev for %s", chan.getName()), e); return; }
        }
        else { /* channel already exists because either it was CServe registered or it is just a regular join after EOS */
            chan = this.getChannelNodeByNameCi(sJoinChan);
            log.debug(String.format("UnrealIRCd::SJOIN: NOT creating channel %s (TS %s) because it already exists", chan.getName(), chan.getRegistrationTS()));
        }

        /*
         * Sets the chan modes
         */
        modChanModes.forEach( (mode, parameter) -> {
            log.debug(String.format("UnrealIRCd::SJOIN: Channel %s: (parsed) set mode: %s %s", chan.getName(), mode, parameter));
            if (mode.startsWith("+")) chan.addMode(String.valueOf(mode.charAt(1)), parameter);
            else chan.delMode(String.valueOf(mode.charAt(1)), parameter);
        });

        /* Parsing the SJOIN list */
        for ( String listItem : sJoinList.split(" ") ) {

            chanListItem = "";

            if (listItem.isEmpty() == true) {
                /* There is nothing to treat => go to next iteration */
                continue;
            }

            if (listItem.startsWith("&") == true || listItem.startsWith("\"") == true || listItem.startsWith("'") == true) {
                /* Special cases: we have a channel list */

                chanListItem =  listItem.replaceAll("^.", "");
                try { mask = Bei.create(chanListItem); }
                catch (InvalidFormatException e) { log.error(String.format("UnrealIRCd::setMode: beI %s has invalid format", chanListItem), e); return; }
                log.debug(String.format("UnrealIRCd::SJOIN: Channel %s: list %s", chan.getName(), listItem));

                if (listItem.startsWith("&")) { /* +b */
                    chan.addBanList(mask);
                    log.debug(String.format("UnrealIRCd::SJOIN: Channel %s: (parsed) set list: +b %s", chan.getName(), chanListItem));  // TODO: abstract chanmode
                }

                else if (listItem.startsWith("\"")) { /* +e */
                    chan.addExceptList(mask);
                    log.debug(String.format("UnrealIRCd::SJOIN: Channel %s: (parsed) set list: +e %s", chan.getName(), chanListItem));  // TODO: abstract chanmode
                }

                else if (listItem.startsWith("'")) { /* +I */
                    chan.addInviteList(mask);
                    log.debug(String.format("UnrealIRCd::SJOIN: Channel %s: (parsed) set list: +I %s", chan.getName(), chanListItem));  // TODO: abstract chanmode
                }

                /* No need to go further because list does not contains user modes */
                continue;
            }


            /* At this point we have only items with modes */
            /* 1/ get the modes associated with UID (if in the format of @123456789, +123456789, ~%123456789, *@+123456789 ...) */
            String[] listItemSplitted = new String[0];
            try { listItemSplitted = listItem.split("[A-Za-z0-9]", 0); }
            catch (Exception e) { log.error(String.format("UnrealIRCd::getResponse: error splitting SJOIN list items %s", listItem), e); }

            user = Nick.getUserByUid(listItem.replaceAll("^[^A-Za-z0-9]*", ""));
            log.info(String.format("UnrealIRCd::SJOIN: Channel %s: user %s (%s) joined channel", chan.getName(), user.getNick(), user.getUid()));

            for (String mode: listItemSplitted) {

                for (int i=0; i < mode.length(); i++) {

                    if (String.valueOf(mode.charAt(i)).startsWith("+"))      joinMode = "v"; /* +v */
                    else if (String.valueOf(mode.charAt(i)).startsWith("%")) joinMode = "h"; /* +h */
                    else if (String.valueOf(mode.charAt(i)).startsWith("@")) joinMode = "o"; /* +o */
                    else if (String.valueOf(mode.charAt(i)).startsWith("~")) joinMode = "a"; /* +a */
                    else if (String.valueOf(mode.charAt(i)).startsWith("*")) joinMode = "q"; /* +q */

                    try {
                        if (user.isUserOnChan(chan) == false) {
                            dispatcher.addUserToChan(chan, user);
                            dispatcher.addUserChanMode(chan, user, joinMode, "");
                        }
                        else { dispatcher.addUserChanMode(chan, user, joinMode, ""); }
                        log.debug(String.format("UnrealIRCd::SJOIN: Channel %s: (parsed) join+set usermode: +%s %s (%s)", chan.getName(), joinMode, user.getNick(), user.getUid()));
                    }
                    catch (Exception e) { log.error(String.format("UnrealIRCd::handleSJoin: Aborting. Could not add user %s to channel %s", user.getNick(), chan.getName()), e); return; }
                }
            }


            /* User has no modes */
            if (user != null && listItem.matches("^[A-Za-z0-9]*")) {
                try { dispatcher.addUserToChan(chan, user); }
                catch (Exception e) { log.error(String.format("UnrealIRCd::handleSJoin: Aborting. Could not add user %s to channel %s", user.getNick(), chan.getName()), e); return; }
                log.debug(String.format("UnrealIRCd::SJOIN: Channel %s: (parsed) join no usermode: %s (%s) - usercount = %s", chan.getName(), user.getNick(), user.getUid(), chan.getUserCount()));
            }

            Boolean isCServeReady = false;
            try { isCServeReady = cservice.isServiceReady(); }
            catch (Exception e) { log.debug("UnrealIRCd::SJOIN: user joined the chan but CService not ready yet"); }

            if (isCServeReady == true && user != null && chan.isRegistered() == true) {
                log.info(String.format("UnrealIRCd::SJOIN: user %s has joined registered channel %s, forwarding to CService", user.getNick(), chan.getName()));
                cservice.handleJoin(user, chan);
            }
        }
        log.info(String.format("UnrealIRCd::SJOIN: setting channel %s (usercount %s)", chan.getName(), chan.getUserCount()));

    }

    private void handleSinfo(String raw) {
        //<<< :5PX SINFO 1683275149 6000 diopqrstwxzBDGHIRSTWZ beI,fkL,lFH,cdimnprstzCDGKMNOPQRSTVZ * :UnrealIRCd-6.1.0

        log.debug("UnrealIRCd::handleSinfo: received a SINFO message");
        if (raw.isEmpty() == true) return;

        /* Some implementation example... */
        /*

        fromEnt = (command[0].split(":"))[1];

        command = (command[2]).split(" ", 4);
        server = serverList.get(fromEnt);

        server.setTS(command[0]);

        */
    }

    private void handleProtoctl(String raw) {
        /*
         * <<< PROTOCTL NOQUIT NICKv2 SJOIN SJOIN2 UMODE2 VL SJ3 TKLEXT TKLEXT2 NICKIP ESVID NEXTBANS SJSBY MTAGS
         * <<< PROTOCTL CHANMODES=beI,fkL,lFH,cdimnprstzCDGKMNOPQRSTVZ USERMODES=diopqrstwxzBDGHIRSTWZ BOOTED=1683274785 PREFIX=(ov)@+ SID=5P0 MLOCK TS=1683309454 EXTSWHOIS
         * <<< PROTOCTL NICKCHARS= CHANNELCHARS=utf8
         */

        log.debug("UnrealIRCd::handleProtoctl: received a PROTOCTL message");
        if (raw.isEmpty() == true) return;

        String[] properties = raw.split(" ");

        String propertyName;
        String propertyValue;
        String peerSid = "";

        Server server;

        for (String property: properties) {
            if (property.equals("PROTOCTL") == true) { continue; }

            if (property.contains("=") == true) {
                propertyName = property.split("=")[0];
                try { propertyValue = property.split("=")[1]; }
                catch (IndexOutOfBoundsException e) { propertyValue = ""; }
            }
            else {
                propertyName = property;
                propertyValue = "";
            }

            try { protocolProps.put(propertyName, propertyValue); }
            catch (Exception e) { log.warn(String.format("UnrealIRCd::handleProtoctl: duplicate property %s detected from server. Not adding it.", propertyName), e); }


            /* Property specific operations */
            if (propertyName.equals("SID") == true) {
                try { peerSid = protocolProps.get("SID"); }
                catch (Exception e) { log.error(String.format("UnrealIRCd::handleProtoctl: error while getting PEER information. This should never happen."), e); return; }

                server = new Server.Builder(peerSid)
                    .isThePeer(true)
                    .hasPeerResponded(true)
                    .name("<temporary undefined>") /* Protocol is received by the identifying peer, contains their SID but not the server name
                                                    * => putting a temporary name that will be updated at the next SID message
                                                    */
                    .build();

                server.setParent(server);

                try { Server.addServer(server); }

                catch (Exception e) { log.error(String.format("UnrealIRCd::handleProtoctl: error adding PEER to server list. This should never happen."), e); return; }

                this.myPeerServerId = peerSid;

            }

            else if (propertyName.equals("BOOTED") == true) { // XXX: to fix
                //Timestamp sTS = new Timestamp(Long.valueOf(protocolProps.get("BOOTED")));
                //Server myPeer = Server.getServerBySid(myPeerServerId);
                //myPeer.setTS(sTS);
            }

            else if (propertyName.equals("CHANMODES") == true) {
                networkChanModesGroup1        = ((protocolProps.get("CHANMODES")).split(",", 4))[0];
                networkChanModesGroup2        = ((protocolProps.get("CHANMODES")).split(",", 4))[1];
                networkChanModesGroup3        = ((protocolProps.get("CHANMODES")).split(",", 4))[2];
                networkChanModesGroup4        = ((protocolProps.get("CHANMODES")).split(",", 4))[3];
            }

            else if (propertyName.equals("USRMODES") == true) {
                //networkUserModes              =   protocolProps.get("CHANMODES");
            }

            else if (propertyName.equals("PREFIX") == true) {
                networkChanUserModes          =   protocolProps.get("PREFIX").replaceAll("[^A-Za-z0-9]", ""); // Channel modes for users
            }

        }
        log.debug("UnrealIRCd::handleProtoctl: finished treating PROTOCTL message");
    }


    private void handleServer(String raw) {
        //<<< SERVER ocelot. 1 :U6000-Fhn6OoEmM-5P0 Mjav Network IRC server

        log.debug("UnrealIRCd::handleServer: received a SERVER message");
        if (raw.isEmpty() == true) return;


        String[] rawSplit = raw.split(" ", 4);

        Server server;

        try { server = Server.getServerBySid(myPeerServerId); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleServer: error getting PEER server name. This should never happen."), e); return; }

        try { server.setName(rawSplit[1]); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleServer: error getting PEER server name. This should never happen."), e); return; }

        try { server.setDistance(Integer.valueOf(rawSplit[2])); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleServer: error getting PEER server hop. This should never happen."), e); return; }

        try { server.setDescription(rawSplit[3].replaceAll("^[:]", "")); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleServer: error getting PEER server description. This should never happen."), e); return; }

        server.setPeerResponded(true);

        Server.getServerBySid(config.getServerId()).setPeerResponded(true);
        log.info(String.format("UnrealIRCd::handleServer: New SERVER registered: %s (%s)", server.getName(), server.getSid()));
        log.debug("UnrealIRCd::handleServer: finished treating SERVER message");
    }

    private void handleSquit(String raw) {
        //<<< SQUIT ocelot. :squit message

        log.debug("UnrealIRCd::handleSquit: received a SQUIT message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ");

        String serverName;

        Server sQuittedServer;

        Set<Server>    affectedServers = new HashSet<>();
        Set<Nick>    affectedUsers = new HashSet<>();

        try { serverName = rawSplit[1]; }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleSquit: error parsing server name in raw %s", raw), e); return; }

        sQuittedServer = Server.getServerByNameCi(serverName);

        log.info(String.format("UnrealIRCd::handleSquit: Received SQUIT for server %s (%s)", sQuittedServer.getName(), sQuittedServer.getSid()));

        // SQUITted server is first affected
        affectedServers.add(sQuittedServer);

        // Then we need to find all the servers introduced by the SQUITted server
        traverseTree(affectedServers, sQuittedServer);
        for(Server affectedNode: affectedServers) affectedUsers.addAll(affectedNode.getLocalUsers());

        log.debug(String.format("UnrealIRCd::handleSquit: Impacted server = %s // Impacted users = %s", affectedServers.toString(), affectedUsers.toString()));


        // List usernodes on those servers

        Nick.getUsersList().forEach( (user) -> {
            if(affectedServers.contains(user.getServer())) { affectedUsers.add(user); }
        });

        // Delete the usernodes
        for(Nick user : affectedUsers) {
            // The user leaves the channels he is on
            Map<Channel, Set<String>> userChanList = new HashMap<>(user.getChanList());
            userChanList.forEach( (chan, mode) -> {
                handleLeavingChan(chan, user, "SQUIT");
                log.debug(String.format("UnrealIRCd::handleSquit: Removing user %s from channel %s", user.getNick(), chan.getName()));
            });

            // Deauth user if needed
            if (user.isAuthed() == true) {
                try { user.getAccount().delUserAuth(user);}
                catch (Exception e) { log.error(String.format("UnrealIRCd::handleSquit: error could not deauth user %s", user.getUid()), e); }
                user.setAuthed(false);
                log.debug(String.format("UnrealIRCd::handleSquit: Deauthing user %s", user.getNick()));
            }
            try { Nick.removeUser(user); }
            catch (ItemNotFoundException e) { log.error(String.format("UnrealIRCd::sendQuit: Could not remove the user from the nick %s (%s) list because it is not present.", user.getNick(), user.getUid()), e); }
            log.debug(String.format("UnrealIRCd::handleSquit: Deleting user %s", user.getNick()));
        }

        // Delete the servers
        for(Server servernode : affectedServers) {

            Server.removeServer(servernode);
            log.debug(String.format("UnrealIRCd::handleSquit: Deleting server %s", servernode.getName()));
        }

        log.debug(String.format("UnrealIRCd::handleSquit: Finished treating SQUIT"));
    }

    private void handleMode(String raw) {
        /*
        :5PX     MODE  #newChan      +ntCT         1683480448
        :AAAAAAA MODE  #Civilization +o AnhTay
        */

        log.debug("UnrealIRCd::handleMode: received a MODE message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 4);

        String channelName;
        String modeChange;

        Map<String, Map<String, String>>   modChanModesAll;

        Map<String, String>  modChanModes;
        Map<String, String>  modChanLists;
        Map<String, String>  modChanUserModes;

        try { channelName = rawSplit[2]; }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleMode: error could not parse channel name in raw %s", raw), e); return; }

        try { modeChange      = rawSplit[3]; }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleMode: error could not parse modes in raw %s", raw), e); return; }

        //Channel chan = channelList.get(channelName);
        Channel chan = Channel.getChanByNameCi(channelName);

        log.debug(String.format("UnrealIRCd::handleMode: Channel %s: (raw) received mode change: %s", chan.getName(), modeChange));

        try { modChanModesAll  = this.parseChanModes(modeChange); }
        catch (ParseException e) { log.error(String.format("UnrealIRCd::handleMode: error while parsing modes: %s", modeChange)); return; }

        modChanModes     =  modChanModesAll.get("chanModes");
        modChanLists     =  modChanModesAll.get("chanLists");
        modChanUserModes =  modChanModesAll.get("chanUserModes");

        /* Sets the chan user modes */
        var wrapperUMode = new Object() { String[] nicks; Nick userNode; };

        modChanUserModes.forEach( (mode, nicks) -> {
            wrapperUMode.nicks = nicks.split(" ");
            for (String nick: wrapperUMode.nicks) {
                if (nick.isEmpty() == false) {
                    try { wrapperUMode.userNode = Nick.getUserByNickCi(nick); }
                    catch (NickNotFoundException e) { log.warn(String.format("UnrealIRCd::handleMode: it seems that there was a nick that does not exist on the network included in the chanmode"), e); continue; }
                    log.debug(String.format("UnrealIRCd::handleMode: Channel %s: (parsed) change usermode: %s %s", chan.getName(), mode, nick));
                    try {
                        if (mode.startsWith("+")) dispatcher.addUserChanMode(chan, wrapperUMode.userNode, String.valueOf(mode.charAt(1)), "");
                        else dispatcher.delUserChanMode(chan, wrapperUMode.userNode, String.valueOf(mode.charAt(1)));
                    }
                    catch (Exception e) { log.error(String.format("UnrealIRCd::handleMode: error setting channel %s modes", chan.getName()), e); }
                }
            }
        });

        /* Sets the chan modes */
        modChanModes.forEach( (mode, parameter) -> {
            log.debug(String.format("UnrealIRCd::handleMode: Channel %s: (parsed) change mode: %s %s", chan.getName(), mode, parameter));
            if (mode.startsWith("+")) chan.addMode(String.valueOf(mode.charAt(1)), parameter);
            else chan.delMode(String.valueOf(mode.charAt(1)), parameter);
        });

        /* Sets the chan lists */
        var wrapperCList = new Object() { String[] parameters; Bei mask; };
        modChanLists.forEach( (list, parameters) -> {
            wrapperCList.parameters = parameters.split(" ");
            for (String parameter: wrapperCList.parameters) {
                if (parameter.isEmpty() == false) {
                    log.debug(String.format("UnrealIRCd::handleMode: Channel %s: (parsed) change list: %s %s", chan.getName(), list, parameter));
                    try { wrapperCList.mask = Bei.create(parameter); }
                    catch (InvalidFormatException e) { log.error(String.format("UnrealIRCd::setMode: beI %s has invalid format", parameter), e); return; }
                    if (list.equals("+b")) chan.addBanList(wrapperCList.mask);
                    else if (list.equals("-b")) chan.delBanList(wrapperCList.mask);
                    else if (list.equals("+e")) chan.addExceptList(wrapperCList.mask);
                    else if (list.equals("-e")) chan.delExceptList(wrapperCList.mask);
                    else if (list.equals("+I")) chan.addInviteList(wrapperCList.mask);
                    else if (list.equals("-I")) chan.delInviteList(wrapperCList.mask);
                }
            }
        });

        log.debug("UnrealIRCd::handleMode: Forwarding to CService");
        if (cservice != null) cservice.handleChanMode(chan, modChanModesAll);

    }

    private void handlePart(String raw) {
        // :XXXXXXXXX PART #chan :message

        log.debug("UnrealIRCd::handlePart: received a PART message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 4);

        String userUid;
        String channelName;

        Integer chanUserCount = 0;

        Nick fromUser;

        Channel chanUserPart;

        try { userUid = rawSplit[0].replaceAll("^[:]", ""); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::chanPart: could not parse user UID in raw %s", raw), e); return; }

        try { channelName = rawSplit[2]; }
        catch (Exception e) { log.error(String.format("UnrealIRCd::chanPart: could not parse channel name in raw %s", raw), e); return; }

        try { fromUser = Nick.getUserByUid(userUid); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::chanPart: could not fetch user node in userList (uid %s)",  userUid), e); return; }

        try { chanUserPart = Channel.getChanByNameCi(channelName); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::chanPart: could not fetch channel node in channelList (chan %s)",  channelName), e); return; }

        try {
          dispatcher.removeUserFromChan(chanUserPart, fromUser);
          log.info(String.format("UnrealIRCd::chanPart: user %s left chan %s", fromUser.getNick(), chanUserPart.getName()));
        }
        catch (Exception e) { log.error(String.format("UnrealIRCd::chanPart: cannot remove the user %s from chan %s because it is not inside it", fromUser.getNick(), chanUserPart.getName()), e); }

        chanUserCount = chanUserPart.getUserCount();

        /* If the channel is empty, delete it */
        if (chanUserCount.equals(0) == true && ! chanUserPart.getModes().containsKey("P") ) {
            log.info(String.format("UnrealIRCd::PART: deleting channel %s because it is empty and it is not persistent", chanUserPart.getName()));
            Channel.removeChannel(chanUserPart);
            chanUserPart = null;
        }
        else { log.info(String.format("UnrealIRCd::chanPart: setting channel %s usercount to %s", chanUserPart.getName(), chanUserPart.getUserCount())); }
    }


    private void handleKick(String raw) {
        // :XXXXXXXXX KICK CHAN UID :message

        log.debug("UnrealIRCd::handleKick: received a KICK message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 5);

        String kickedUserUid;
        String kickChannelName;

        Channel chanUserPart;
        Nick kickedUser;

        Integer chanUserCount = 0;

        //try { ... = rawSplit[0].replaceAll("^[:]", ""); }
        //catch (Exception e) { log.error(String.format("UnrealIRCd::chanPart: could not parse user UID in raw %s", raw), e); return; }

        try { kickChannelName = rawSplit[2]; }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleKick: could not parse channel name in raw %s", raw), e); return; }

        try { kickedUserUid = rawSplit[3]; }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleKick: could not parse user UID in raw %s", raw), e); return; }

        try { kickedUser = Nick.getUserByUid(kickedUserUid); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleKick: could not fetch user id %s users list", kickedUserUid), e); return; }

        try { chanUserPart = getChannelNodeByNameCi(kickChannelName); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleKick: could not fetch user id %s users list", kickedUserUid), e); return; }

        try {
            dispatcher.removeUserFromChan(chanUserPart, kickedUser);
            chanUserCount = chanUserPart.getUserCount();
        }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleKick: could not remove user id %s from chan %s because it was not inside it", kickedUserUid, kickChannelName), e); return; }

        /* If the channel is empty, delete it */
        if (chanUserCount.equals(0) == true && chanUserPart.getModes().containsKey("P") == false ) {
            Channel.removeChannel(chanUserPart);
            chanUserPart = null;
        }
    }


    private void handleQuit(String raw) {
        // :XXXXXXXXX QUIT :message

        log.debug("UnrealIRCd::handleQuit: received a QUIT message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 3);

        String userUid;
        String quitMsg = "";

        Nick userToRemove;
        Server userServer;

        Map<Channel, Set<String>> curUserChanList;

        try { userUid = rawSplit[0].replaceAll("^[:]", ""); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleQuit: could not parse user UID in raw %s", raw), e); return; }

        try { quitMsg = rawSplit[2].replaceAll("^[:]", ""); }
        catch (Exception e) { log.info(String.format("UnrealIRCd::handleQuit: could not parse quit message in raw %s", raw)); }

        try { userToRemove = Nick.getUserByUid(userUid); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleQuit: could not fetch user id %s from users list", raw), e); return; }

        curUserChanList = new HashMap<>(userToRemove.getChanList());
        userServer = userToRemove.getServer();

        curUserChanList.forEach( (chan, mode) -> { handleLeavingChan(chan, userToRemove, "QUIT"); });

        if (userToRemove.isAuthed() == true) {
            try { database.delUserAuth(userToRemove, Const.DEAUTH_TYPE_QUIT, quitMsg); }
            catch (Exception e) {  }
            userToRemove.unsetAccount();
        }

        userServer.removeLocalUser(userToRemove); // XXX: to improve?
        try { Nick.removeUser(userToRemove); }
        catch (ItemNotFoundException e) { log.error(String.format("UnrealIRCd::sendQuit: Could not remove the user from the nick %s (%s) list because it is not present.", userToRemove.getNick(), userToRemove.getUid()), e); }
    }

    private void handleKill(String raw) {
        // :AAAAAAA KILL AAAAAAA :message

        log.debug("UnrealIRCd::handleKill: received a KILL message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 4);

        //String killerUid;
        String killedUid;
        String killMsg = "";

        Nick killedUser;

        Server userServer;

        Map<Channel, Set<String>> curUserChanList;

        //try { killerUid = rawSplit[0].replaceAll("^[:]", ""); }
        //catch (Exception e) { log.error(String.format("UnrealIRCd::handleKill: could not parse user UID in raw %s", raw), e); return; }

        try { killedUid = rawSplit[2]; }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleKill: could not parse user UID in raw %s", raw), e); return; }

        try { killMsg = rawSplit[2].replaceAll("^[:]", ""); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleKill: could not parse quit message in raw %s", raw)); }

        try { killedUser = Nick.getUserByUid(killedUid); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleKill: could not fetch user id %s from users list", raw), e); return; }

        userServer = killedUser.getServer();
        curUserChanList = new HashMap<>(killedUser.getChanList());

        curUserChanList.forEach( (chan, mode) -> { handleLeavingChan(chan, killedUser, "KILL"); });

        if (killedUser.isAuthed() == true) {
            try { database.delUserAuth(killedUser, Const.DEAUTH_TYPE_QUIT, killMsg); }
            catch (Exception e) { log.error(String.format("UnrealIRCd::handleKill: could not deauth user id %s", killedUser), e);  }
        }
        killedUser.unsetAccount();

        userServer.removeLocalUser(killedUser); // XXX: to improve?

        try { Nick.removeUser(killedUser); }
        catch (ItemNotFoundException e) { log.error(String.format("UnrealIRCd::sendQuit: Could not remove the user from the nick %s (%s) list because it is not present.", killedUser.getNick(), killedUser.getUid()), e); }



        if (killedUser.getUid().equals(config.getServerId() + config.getCServeUniq())) {
            log.info(String.format("UnrealIRCd::handleKill: CService has been killed! Relaunching it."));
            try { Thread.sleep(1000); }
            catch (Exception e) { log.fatal(String.format("UnrealIRCd::handleKill: Cound not relaunch CService after kill.")); return; }
            client.launchCService();
        }
    }

    private void handleEos(String raw) {
        //<<< :5PX EOS

        log.debug("UnrealIRCd::handleEos: received an EOS message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ");

        String serverSid;
        String userChanJoinMode = "";

        Server server;

        try { serverSid = rawSplit[0].replaceAll("^[:]", ""); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleEos: could not parse server SID in raw %s", raw), e); return; }


        try { server = Server.getServerBySid(serverSid); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleEos: could not get server %s from server list", serverSid), e); return; }

        /* If our peer sends the EOS (so last to send EOS) */
        if(server.isPeer() == true) {

            /* Not in netburst anymore */
            this.isNetBursting = false;

            // Identify what are the available channel modes
            if (protocolProps.containsKey("PREFIX")) {
                String chanPrefix  = protocolProps.get("PREFIX"); /* PREFIX = (modelist)prefixlist, e.g PREFIX=(qaohv)~&@%+ */
                String chanModes   = "";
                Pattern pattern = Pattern.compile("\\((.*?)\\)"); /* Matches `(modelist)' */
                Matcher matcher = pattern.matcher(chanPrefix);

                if (matcher.find() == true) {
                    chanModes = matcher.group(1);

                    if (chanModes.contains("q") == true) featureList.put("chanOwner", true);
                    else featureList.put("chanOwner", false);

                    if (chanModes.contains("a") == true) featureList.put("chanAdmin", true);
                    else featureList.put("chanAdmin", false);

                    if (chanModes.contains("o") == true) featureList.put("chanOp", true);
                    else featureList.put("chanOp", false);

                    if (chanModes.contains("h") == true) featureList.put("chanHalfop", true);
                    else featureList.put("chanHalfop", false);

                    if (chanModes.contains("v") == true) featureList.put("chanVoice", true);
                    else featureList.put("chanVoice", false);
                }
            }

            if      (this.hasFeature("chanOwner")   == true) userChanJoinMode = "q";
            else if (this.hasFeature("chanAdmin")   == true) userChanJoinMode = "a";
            else if (this.hasFeature("chanOp")      == true) userChanJoinMode = "o";
            else if (this.hasFeature("chanHalfop")  == true) userChanJoinMode = "h";
            else if (this.hasFeature("chanVoice")   == true) userChanJoinMode = "v";
            this.userChanJoinMode = userChanJoinMode;

        }

        if(server.isPeer() == true) { /* Only send EOS when our peer sends EOS */
            String str;
            str = String.format(":%s EOS", config.getServerId());
            client.write(str);

            /*
             * Sets our EOS here as true, only when it is our peer sending it
             */
            Server.getServerBySid(config.getServerId()).setEOS(true);
        }

        server.setEOS(true);

    }

    private void handleMotd(String raw) {

        /* :5TL10VQ01 MOTD cheetah. */

        String[] rawSplit = raw.split(" ");

        String nickUid;
        String str;

        Server myServer;

        Nick nick;

        List<String> list = new ArrayList<>();

        Cache cache;

        try { nickUid = rawSplit[0].replaceFirst("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleMotd: could not extract nick UID in raw %s", raw), e); return; }

        try { nick = Nick.getUserByUid(nickUid); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleMotd: could not find nick UID %s in nick list", nickUid), e); return; }

        myServer = Server.getServerBySid(config.getServerId());


        try { cache = Cache.create("motd", "motdfile"); }
        catch (FileNotFoundException e) {
            str = String.format(":%s 422 %s :MOTD File is missing", config.getServerId(), nick.getNick());
            client.write(str);
            return;
         }

        str = String.format(":%s 375 %s :- %s Message of the Day -", config.getServerId(), nick.getNick(), myServer.getName());
        list.add(str);

        for (String l: cache.getData()) list.add(String.format(":%s 372 %s :- %s", config.getServerId(), nick.getNick(), l));

        str = String.format(":%s 376 %s :End of /MOTD command.", config.getServerId(), nick.getNick());
        list.add(str);

        for (String s: list) client.write(s);

    }


    private void handleRules(String raw) {

        /* :5TL10VQ01 RULES cheetah. */

        String[] rawSplit = raw.split(" ");

        String nickUid;
        String str;

        Server myServer;

        Nick nick;

        List<String> list = new ArrayList<>();

        Cache cache;

        try { nickUid = rawSplit[0].replaceFirst("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleRules: could not extract nick UID in raw %s", raw), e); return; }

        try { nick = Nick.getUserByUid(nickUid); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleRules: could not find nick UID %s in nick list", nickUid), e); return; }

        myServer = Server.getServerBySid(config.getServerId());


        try { cache = Cache.create("rules", "rulesfile"); }
        catch (FileNotFoundException e) {
            str = String.format(":%s 434 %s :RULES File is missing", config.getServerId(), nick.getNick());
            client.write(str);
            return;
         }

        str = String.format(":%s 308 %s :- %s Server Rules -", config.getServerId(), nick.getNick(), myServer.getName());
        list.add(str);

        for (String l: cache.getData()) list.add(String.format(":%s 232 %s :- %s", config.getServerId(), nick.getNick(), l));

        str = String.format(":%s 309 %s :End of /RULES command.", config.getServerId(), nick.getNick());
        list.add(str);

        for (String s: list) client.write(s);

    }

    private void handleAdmin(String raw) {

        /* :5TL10VQ01 MOTD cheetah. */

        String[] rawSplit = raw.split(" ");
        String[] adminLines;

        int numeric = 257;

        String nickUid;
        String str;

        Server myServer;

        Nick nick;

        List<String> list = new ArrayList<>();

        try { nickUid = rawSplit[0].replaceFirst("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleAdmin: could not extract nick UID in raw %s", raw), e); return; }

        try { nick = Nick.getUserByUid(nickUid); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleAdmin: could not find nick UID %s in nick list", nickUid), e); return; }

        myServer = Server.getServerBySid(config.getServerId());



        if (config.getAdminInfo().isEmpty() == true) {
            str = String.format(":%s 423 %s :No administrative info available", config.getServerId(), nick.getNick());
            client.write(str);
            return;
        }

        str = String.format(":%s 256 %s :Administrative info about %s", config.getServerId(), nick.getNick(), myServer.getName());
        list.add(str);

        adminLines = config.getAdminInfo().split("\n");
        for (String l: adminLines) {
            list.add(String.format(":%s %s %s :%s", config.getServerId(), numeric, nick.getNick(), l));
            if (numeric < 259) numeric++;
        }

        for (String s: list) client.write(s);

    }


    /**
     * Blackhole method for ignored commands received from the network. Silently discards the command.
     */
    private void handleNothing(IrcMessage ircMsg) {
        log.trace(String.format("UnrealIRCd::handleNothing: ignoring received command: %s", ircMsg.getCommand()));
        return;
    }

    /**
     * Returns a 421 numeric message to the user
     * @param raw input raw message
     */
    private void handleUnknownCommand(String raw) {
        log.debug(String.format("UnrealIRCd::handleUnknownCommand: ignoring received unknown command"));

        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 4);

        String from;
        String s;

        try { from = rawSplit[0].replaceAll("^[:]", ""); }
        catch (IndexOutOfBoundsException e) { log.error(String.format("UnrealIRCd::handleUnknownCommand: could not extract 'from' field in raw %s", raw), e); return; }

        /* UnrealIRCd uses UID with 9 symbols */
        if (from.length() == 9) {

        }
        else { log.error(String.format("UnrealIRCd::handleUnknownCommand: FROM entity (%s) is not an user %s", from, raw)); return; }

        s = String.format(":%s 421 %s :Command is not implemented", config.getServerId(), from); client.write(s);

    }

    private void handleSetHost(String raw) {
        /* :AAAAAAA SETHOST :newHostName */

        log.debug("UnrealIRCd::handleSetHost: received a SETHOST message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 4);

        String uid;
        String newHostName = "";

        Nick user;

        try { uid = rawSplit[0].replaceAll("^[:]", ""); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleSetHost: could not parse user UID in raw %s", raw), e); return; }

        try { newHostName = rawSplit[2].replaceAll("^:", ""); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleSetHost: could not parse user new hostname in raw %s", raw), e); return; }


        try { user = Nick.getUserByUid(uid); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleSetHost: could not fetch user id %s from users list", raw), e); return; }

        user.setHost(newHostName);

    }

    private void handleUMode2(String raw) {
        /* :nick UMODE2 modes */

        log.debug("UnrealIRCd::handleUMode2: received a UMODE2 message");
        if (raw.isEmpty() == true) return;

        String[] rawSplit = raw.split(" ", 4);

        String uid;
        String modeChange = "";

        Nick user;

        Map<String, String>  modUserModes;

        try { uid = rawSplit[0].replaceAll("^[:]", ""); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleSetHost: could not parse user UID in raw %s", raw), e); return; }

        try { modeChange = rawSplit[2]; }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleSetHost: could not parse modes in raw %s", raw), e); return; }


        try { user = Nick.getUserByNickCi(uid); }
        catch (Exception e) { log.error(String.format("UnrealIRCd::handleSetHost: could not fetch user id %s from users list", raw), e); return; }

        modUserModes = parseUserMode(modeChange);

        modUserModes.forEach( (mode, parameter) -> {
            log.debug(String.format("UnrealIRCd::handleUMode2: User %s: (parsed) change mode: %s", user.getNick(), mode));
            if (mode.startsWith("+")) user.addMode(String.valueOf(mode.charAt(1)), parameter);
            else user.delMode(String.valueOf(mode.charAt(1)), parameter);
        });



    }

    @Override public void getResponse(String raw) throws Exception {
        String ircv3TagTxt = "";
        String rawStripped = raw;

        String[] command;
        String[] rawStrippedT;

        IrcMessage ircMsg = IrcMessage.create(raw);
        log.trace(String.format("UnrealIRCd::getResponse: Received IRC message: %s", raw));
        log.trace(String.format("UnrealIRCd::getResponse:   |  Q msg id: %s", ircMsg.getQMsgId()));
        log.trace(String.format("UnrealIRCd::getResponse:   | timestamp: %s", ircMsg.getTimestamp()));
        log.trace(String.format("UnrealIRCd::getResponse:   | ircv3 tag: %s", ircMsg.getIrcv3Tag()));
        log.trace(String.format("UnrealIRCd::getResponse:   |      from: %s %s", ircMsg.getFromType(), ircMsg.getFrom()));
        log.trace(String.format("UnrealIRCd::getResponse:   |        to: %s %s", ircMsg.getTargetType(), ircMsg.getTarget()));
        log.trace(String.format("UnrealIRCd::getResponse:   |   command: %s", ircMsg.getCommand()));
        log.trace(String.format("UnrealIRCd::getResponse:   | arguments: %s", ircMsg.getArgv()));

        String resultString = "";

        Ircv3Tag ircv3tags = ircMsg.getIrcv3Tag();

        Map<String, String> logMap = new TreeMap<>();
        logMap.put("from",    String.format("%s %s", ircMsg.getFromType(), ircMsg.getFrom()));
        logMap.put("command", ircMsg.getCommand());
        logMap.put("args",    String.format("%s", ircMsg.getArgv()));
        logMap.put("ircv3tag", new ObjectMapper().writeValueAsString(ircv3tags.getTags()));
        logMap.put("result",  resultString);

        ESLog esLog = new ESLog.Builder()
            .type("protocol")
            .logLevel("DEBUG")
            .qmsgid(ircMsg.getQMsgId())
            .clientid(client.getClientId())
            .logMap(logMap)
            .build();

        if (ircMsg.getCommand().equals("PING") == false) {
            CompletableFuture.runAsync(() -> {
                try {
                    ESClient esClient = ESClient.getInstance();
                    esClient.index(esLog.toString());
                }
                catch (Exception e) { log.error("UnrealIRCd::getResponse: cannot send handlePrivmsg() to CompletableFuture.", e); return; }
            });
        }



        command = raw.split(" ", 3); // Begin to split raw message to fetch the command (part0 part1 part2part3part4...)

        // Check for IRCv3 string presence, if yes we cut if off to part1 part2 part3part4...
        // @blaablaa ...
        if (command[0].startsWith("@")) {
            command = (command[1] + " " + command[2]).split(" ", 3); // This cuts the IRCv3 prelude

            rawStrippedT = raw.split(" ", 2);
            rawStripped  = rawStrippedT[1];
            ircv3TagTxt  = rawStrippedT[0];
        }

        //log.trace(String.format("UnrealIRCd::getResponse: Received message to handle: %s %s", command[0], command[1]));

        if (ircv3TagTxt.isEmpty() == false) {
            //log.debug(String.format("UnrealIRCd::getResponse: Reveived an IRCv3 message:"));

            //Ircv3Tag v3Tag;

            try {
                //v3Tag = new Ircv3Tag(ircv3TagTxt);

                /* Implementation examples */
                //v3Tag.getTags().forEach( (prop, val) -> { log.debug(String.format("  %s -> %s", prop, val));  });

                /* Implementation examples */
                /*
                 * v3Tag.getTagsStd()               .forEach( (prop, val) -> { log.trace(String.format("UnrealIRCd::getResponse: IRCv3 Standard tags: %s -> %s",                  prop, val));  });
                 * v3Tag.getTagsVendorSpec()        .forEach( (prop, val) -> { log.trace(String.format("UnrealIRCd::getResponse: IRCv3 VendorSpecific tags: %s -> %s",            prop, val)); });
                 * v3Tag.getTagsClientOnly()        .forEach( (prop, val) -> { log.trace(String.format("UnrealIRCd::getResponse: IRCv3 ClientOnly tags: %s -> %s",                prop, val)); });
                 * v3Tag.getTagsVendSpecClientOnly().forEach( (prop, val) -> { log.trace(String.format("UnrealIRCd::getResponse: IRCv3 ClientOnly+VendorSpecific tags: %s -> %s", prop, val)); });
                 */
            }
            catch (Exception e) { log.error(String.format("UnrealIRCd::getResponse: error while parsing IRCv3 tag: ", ircv3TagTxt)); }
        }

        /* Raws like "@ircv3tag :entity COMMAND arguments", @ircv3tag is always stripped */
        //var w = new Object(){ String rawStripped; };
        //w.rawStripped = rawStripped;

        //System.out.println(String.format(">>> command[1] = '%s' // from + ircmsg.getCommand() = '%s + %s'", command[1], ircMsg.getFrom(), ircMsg.getCommand()));
        switch(command[1].toUpperCase()) {
            case "PRIVMSG":
                CompletableFuture.runAsync(() -> {
                    try { handlePrivmsg(ircMsg); }
                    catch (Exception e) { log.error("UnrealIRCd::getResponse: cannot send handlePrivmsg() to CompletableFuture.", e); return; }
                });
                return;

            case "MD":             handleMd(rawStripped);      return;
            case "UID":            handleUid(rawStripped);     return;
            case "SASL":           handleSasl(rawStripped);    return;
            case "SJOIN":          handleSjoin(rawStripped);   return;
            case "MODE":           handleMode(rawStripped);    return;
            case "PART":           handlePart(rawStripped);    return;
            case "KICK":           handleKick(rawStripped);    return;
            case "QUIT":           handleQuit(rawStripped);    return;
            case "KILL":           handleKill(rawStripped);    return;
            case "MOTD":           handleMotd(rawStripped);    return;
            case "NICK":           handleNick(rawStripped);    return;
            case "RULES":          handleRules(rawStripped);   return;
            case "SID":            handleSid(rawStripped);     return;
            case "EOS":            handleEos(rawStripped);     return;
            case "SETHOST":        handleSetHost(rawStripped); return;
            case "UMODE2":         handleUMode2(rawStripped);  return;
            case "ADMIN":          handleAdmin(rawStripped);   return;
            case "TOPIC":          handleTopic(rawStripped, false); return;
            case "VERSION":        handleVersion(ircMsg);      return;

            /* List of unhandled commands */
            case "SINFO":          handleSinfo(rawStripped);          return; /* Not implemented */
            case "MODULE":         handleNothing(ircMsg);             return;
            case "STATS":          handleStats(ircMsg);               return;
            case "TKL":            handleNothing(ircMsg);             return; // TODO: to implement
            case "TIME":           handleUnknownCommand(rawStripped); return; // TODO: to implement
            case "CREDITS":        handleUnknownCommand(rawStripped); return; // TODO: to implement
            case "LICENSE":        handleUnknownCommand(rawStripped); return; // TODO: to implement

            /* No need to implement */
            case "SAJOIN":         handleNothing(ircMsg); return;
            case "SAPART":         handleNothing(ircMsg); return;
            case "SLOG":           handleNothing(ircMsg); return;
            case "SMOD":           handleNothing(ircMsg); return;
            case "AWAY":           handleNothing(ircMsg); return;
            case "NOTICE":         handleNothing(ircMsg); return;
            case "LUSERS":         handleNothing(ircMsg); return;
        }

        /* Raws like "COMMAND arguments" */
        switch(command[0]) {
            case "NETINFO":        handleNetInfo(rawStripped); return;
            case "PROTOCTL":       handleProtoctl(rawStripped); return;
            case "SERVER":         handleServer(rawStripped); return;
            case "SQUIT":          handleSquit(rawStripped); return;
            case "TOPIC":          handleTopic(rawStripped, true); return;
            case "PING":           handlePing(rawStripped); return;
        }

        log.warn(String.format("UnrealIRCd::getResponse: Received unhandled raw: %s", raw));
    }

    private void traverseTree(Set<Server> nodes, Server node) {
        for (Server child: node.getChildren()) {
            nodes.add(child);
            traverseTree(nodes, child);
        }
    }

    public void sendServerIdent() {
        String s;
        StringJoiner protoFeaturesString = new StringJoiner(" ");

        log.info("Sending server ident");

        for (String ss: UnrealIRCd.protoFeatures) protoFeaturesString.add(ss);

        /* PROTOCTL format: PROTOCTL EAUTH=my.server.name[,protocolversion[,versionflags,fullversiontext]] */

        s = String.format(":%s PASS :%s", config.getServerId(), config.getLinkPassword()); client.write(s);
        s = String.format(":%s PROTOCTL %s", config.getServerId(), protoFeaturesString.toString()); client.write(s);
        s = String.format(":%s PROTOCTL EAUTH=%s,%s,%s,%s", config.getServerId(), config.getServerName(), Const.UNREAL_PROTOCOL_VERSION, Const.UNREAL_VERSION_FLAGS, Const.UNREAL_VERSION_FULLTEXT); client.write(s);
        s = String.format(":%s PROTOCTL SID=%s", config.getServerId(), config.getServerId()); client.write(s);
        s = String.format(":%s SERVER %s 1 :%s", config.getServerId(), config.getServerName(), config.getServerDescription()); client.write(s);

        Server server = new Server.Builder(config.getServerName(), config.getServerId())
            .description(config.getServerDescription())
            .distance(0)
            .build();

        server.setEOS(false);
        server.setPeerResponded(false);
        server.setParent(server);

        try { Server.addServer(server); }
        catch (ItemExistsException e) { log.error(String.format("UnrealIRCd::sendServerIdent: could not add server %s (%s) to the server list because it has already been registered.", server.getName(), server.getSid()), e); }
    }

    public String getUserChanJoinMode() {
        return this.userChanJoinMode;
    }

    public boolean isBursting() {
        return this.isNetBursting;
    }

    @Override public String getChanMode(String m) {
        return CHANNEL_MODES.get(m);
    }

    @Override public String getUserMode(String m) {
        return USER_MODES.get(m);
    }

    @Override public String userModeToTxt(String s) {
        if (userModeToTxt.containsKey(s) == true) return userModeToTxt.get(s);
        else return "unknown";
    }

    @Override public String chanModeToTxt(String s) {
        if (chanModeToTxt.containsKey(s) == true) return chanModeToTxt.get(s);
        else return "unknown";
    }

}