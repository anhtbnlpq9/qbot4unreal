
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.HashSet;
import java.time.Instant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Protocol extends Exception {

    private static Logger log = LogManager.getLogger("common-log");
    
    private Client      client;

    private Config      config;

    private ServerNode  server;

    private CService    cservice;

    private SqliteDb    sqliteDb;

    private Boolean     networkInsideNetBurst = true;
    
    private HashMap<String, ServerNode>      serverList          = new HashMap<String, ServerNode>();

    private HashMap<String, UserNode>        userList            = new HashMap<String, UserNode>();

    private HashMap<String, UserAccount>     userAccounts        = new HashMap<String, UserAccount>();

    private HashMap<String, ChannelNode>     regChannels         = new HashMap<String, ChannelNode>();
    private HashMap<String, ChannelNode>     channelList         = new HashMap<String, ChannelNode>();

    private HashMap<String, String>          userNickSidLookup   = new HashMap<String, String>(); // Lookup map for Nick -> Sid ; TODO : to transform to <String, UserNode>
    private HashMap<String, String>          protocolProps       = new HashMap<String, String>();

    private HashMap<String, Boolean>         featureList         = new HashMap<>();
    

    private String myPeerServerId;
    private String foundNickLookUpCi;

    private Long   unixTime;


    /**
     * Class constructor
     * Creates a protocol and populates the registered users list given by sqliteDB
     * @param config object to static configuration
     * @param sqliteDb object to static database
     */
    public Protocol(Config config, SqliteDb sqliteDb) {
        this.config = config;
        this.sqliteDb = sqliteDb;
        sqliteDb.setProtocol(this);

        sqliteDb.getRegUsers().forEach( (username, userHM) -> {
            this.userAccounts.put(
                username, 
                new UserAccount(
                    sqliteDb, 
                    (String) userHM.get("name"), 
                    (Integer) userHM.get("uid"), 
                    (Integer) userHM.get("userFlags"), 
                    (String) userHM.get("email"), 
                    (HashSet<String>) userHM.get("certfp"), 
                    (Long) userHM.get("regTS"))
                );
            this.userAccounts.get(username).setConfigRef(config);
        });

        sqliteDb.getRegChans().forEach( (chanName, chanHM) -> {
            
            this.regChannels.put(chanName, 
                                 new ChannelNode(sqliteDb, 
                                 (String) chanHM.get("name"), 
                                 (Long) chanHM.get("regTS"), 
                                 (Integer) chanHM.get("chanflags"), 
                                 (Integer) chanHM.get("channelId"), 
                                 (String) chanHM.get("welcome"),
                                 (String) chanHM.get("topic"),
                                 (Integer) chanHM.get("bantime"),
                                 (Integer) chanHM.get("autolimit")
                                   )
            );

        });

        //this.channelList = (HashMap<String, ChannelNode>) this.regChannels.clone();

        // Copy the hashmap, else regChannels will also be modified
        this.channelList = new HashMap<String, ChannelNode>(this.regChannels);

        //this.regChannels.forEach( (regChannelName, regChannelNode) -> {
        //    channelList.put(regChannelName, regChannelNode);
        //});
    }

    /**
     * Return the list of registered users as a HM
     * @return HM of registered users
     */
    public HashMap<String, UserAccount> getRegUserList() {
        return userAccounts;
    }

    /**
     * Returns an user account object given its account name
     * @param userAccountName user account name
     * @return user account object
     */
    public UserAccount getRegUserAccount(String userAccountName) throws Exception {
        if (userAccounts.containsKey(userAccountName) == true) return userAccounts.get(userAccountName);
        else throw new Exception("User account " + userAccountName + " not found.");
    }

    /**
     * Returns the list of registered channels as a HM
     * @return list of registered channels
     */
    public HashMap<String, ChannelNode> getRegChanList() {
        return regChannels;
    }

    /**
     * Adds a map nick -> user SID
     * @param nick user nick
     * @param sid user SID
     */
    public void addNickLookupTable(String nick, String sid) {
        userNickSidLookup.put(nick, sid);
    }

    /**
     * Returns the UserNode behind a SID
     * @param userSid user SID
     * @return UserNode
     */
    public UserNode getUserNodeBySid(String userSid) {
        return userList.get(userSid);
    }

    /**
     * Returns a UserNode behind the nick (case insensitive)
     * @param userNick user nick
     * @return UserNode
     */
    public UserNode getUserNodeByNick(String userNick) {
        if (userNickSidLookup.containsKey(userNick)) {
            return userList.get(getNickLookupTableCi(userNick));
        }
        else return null;
    }

    /**
     * Returns an user account based on their username
     * @param username user name
     * @return user account
     */
    public UserAccount getUserAccount(String username) {
        var wrapper = new Object(){ UserAccount foundUserAccount = null; };
        this.userAccounts.forEach( (theusername, useraccount) -> {
            if (username.toLowerCase().equals(theusername.toLowerCase())) { wrapper.foundUserAccount = useraccount;}
        });
        return wrapper.foundUserAccount;
    }

    /**
     * Returns an user account based on their user id
     * @param userId user id
     * @return user account
     */
    public UserAccount getUserAccount(Integer userId) {
        var wrapper = new Object(){ UserAccount userAccountFound = null; };
        this.getRegUserList().forEach( (userName, userAccount) -> {
            if (userAccount.getId() == userId) { 
                wrapper.userAccountFound = userAccount;
            }
        });
        return wrapper.userAccountFound;
    }

    /**
     * Returns the channel node corresponding to the name
     * @param channelName channel name
     * @return channel node
     */
    public ChannelNode getChannelNodeByName(String channelName) {
        return channelList.get(channelName);
    }

    /**
     * Removes a map nick -> sid
     * @param nick nick
     */
    public void delNickLookupTable(String nick) {
        userNickSidLookup.remove(nick);
    }

    /**
     * Renicks a SID when their nick changes
     * @param sid user SID
     * @param oldNick previous nick
     * @param newNick new nick
     */
    public void renameNickLookupTable(String sid, String oldNick, String newNick) {
        addNickLookupTable(sid, newNick);
        delNickLookupTable(oldNick);
    }

    /**
     * Returns an user SID given his nickname (cs)
     * @param nick
     * @return
     */
    public String getNickLookupTable(String nick) {
        return userNickSidLookup.get(nick);
    }

    /**
     * Returns an user SID given his nickname (ci)
     * @param nick user nick
     * @return user SID
     */
    public String getNickLookupTableCi(String nick) {
        foundNickLookUpCi = "";
        userNickSidLookup.forEach( (userNick, userSid) -> {
            if (userNick.toLowerCase().equals(nick.toLowerCase())) { 
                foundNickLookUpCi = userSid; 
            }
        });
        return foundNickLookUpCi;
    }

    public void setClientRef(Client client) {
        this.client = client;
    }

    public void setCService(CService cservice) {
        this.cservice = cservice;
    }

    public void write(Client client, String str) /*throws Exception*/ {
        client.write(str);
    }

    /**
     * Send a privmsg to an user
     * @param client client
     * @param from originator usernode
     * @param to target usernode
     * @param msg message string
     */
    public void sendPrivmsg(Client client, UserNode from, UserNode to, String msg) /*throws Exception*/ {
        String str = ":" + from.getUid() + " PRIVMSG " + to.getUid() + " :" + msg;
        client.write(str);
    }
    
    /**
     * Sends a notice from an user to another user
     * @param client client
     * @param from originator usernode
     * @param to target usernode
     * @param msg message string
     */
    public void sendNotice(Client client, UserNode from, UserNode to, String msg) /*throws Exception*/ {
        String str = ":" + from.getUid() + " NOTICE " + to.getUid() + " :" + msg;
        client.write(str);
    }

    public void sendInvite(Client client, UserNode to, ChannelNode chanNode) /*throws Exception*/ {
        String str = ":" + config.getServerId() + config.getCServeUniq() + " INVITE " + to.getUid() + " " + chanNode.getName();
        client.write(str);
    }

    /**
     * Makes the bot join a channel
     * @param client client
     * @param who originator usernode
     * @param chan target channel
     */
    public void chanJoin(Client client, UserNode who, ChannelNode chan) /*throws Exception*/ {
        String str;

        str = ":" + who.getUid() + " JOIN " + chan.getName();

        ChannelNode newChannel = null;

        if (channelList.containsKey(chan.getName())) {
            log.info(String.format("Protocol/chanJoin: user %s joined existing channel %s", who.getNick(), chan.getName()));
        }
        else {
            unixTime = Instant.now().getEpochSecond();
            newChannel = new ChannelNode(chan.getName(), unixTime);
            channelList.put(newChannel.getName(), newChannel);

            if (this.getRegChanList().containsKey(newChannel.getName()) == true) {
                try {
                    newChannel.setAutoLimit(sqliteDb.getChanAutoLimit(newChannel));
                    newChannel.setFlags(sqliteDb.getChanFlags(newChannel));
                    newChannel.setId(sqliteDb.getChanId(newChannel));
                    newChannel.setChanlev(sqliteDb.getChanChanlev(newChannel));
                    newChannel.setRegistered(true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            log.info(String.format("Protocol/chanJoin: user %s joined new channel %s (creating it)", who.getNick(), chan.getName()));
        }

        if (newChannel != null) chan = newChannel;

        try {
            chan.setChanlev(sqliteDb.getChanChanlev(chan));
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(String.format("Protocol/chanJoin: error setting chanlev for channel %s", chan.getName()));
        }

        try {
            who.addToChan(chan, "");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        log.info(String.format("Protocol/chanJoin: Updating channel %s usercount to %s", chan.getName(), chan.getUserCount()));
        client.write(str);

        /* Set topic when joining the chan (if cflag SAVETOPIC) */
        if (Flags.isChanTopicSave(chan.getFlags()) == true) {
            String savedTopic = "";

            try {
                savedTopic = sqliteDb.getTopic(chan);
            }
            catch (Exception e) { return; }
 
            str = ":" + who.getUid() + " TOPIC " + chan.getName() + " :" + savedTopic;
            client.write(str);
        }
    }

    /**
     * Make the bot leaves the channel
     * @param client client
     * @param who usernode originator
     * @param chan channelnode
     */
    public void chanPart(Client client, UserNode who, ChannelNode chanUserPart) /*throws Exception*/ {
        String str = ":" + who.getUid() + " PART " + chanUserPart.getName();

        try {
            who.removeFromChan(chanUserPart);
            log.info(String.format("Protocol/chanPart: user %s left chan %s", who.getNick(), chanUserPart.getName()));
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(String.format("Protocol/chanPart: cannot remove the user %s from chan %s because it is not inside it", who.getNick(), chanUserPart.getName()));
        }

        Integer chanUserCount = chanUserPart.getUserCount();

        if (chanUserCount.equals(0) == true && chanUserPart.getModes().containsKey("P") == false) {
            channelList.remove( chanUserPart.getName() );
            log.info(String.format("Protocol/chanPart: deleting channel %s because it is empty and it is not persistent", chanUserPart.getName()));
            chanUserPart = null;
        }

        client.write(str);
    }

    /**
     * Kicks an user from a channel with the reason
     * @param client client
     * @param who originator
     * @param chan channelnode
     * @param target usernode
     * @param reason reason
     */
    public void chanKick(Client client, UserNode who, ChannelNode chan, UserNode target, String reason) /*throws Exception*/ {
        String str = ":" + who.getUid() + " KICK " + chan.getName() + " " + target.getNick() + " :" + reason;
        
        try {
            who.removeFromChan(chan);
            log.info(String.format("Protocol/chanKick: user %s kicked from chan %s", who.getNick(), chan.getName()));
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(String.format("Protocol/chanKick: cannot remove the user %s from chan %s because it is not inside it", who.getNick(), chan.getName()));
        }

        Integer chanUserCount = chan.getUserCount();

        if (chanUserCount.equals(0) == true && chan.getModes().containsKey("P") == false) {
            channelList.remove( chan.getName() );
            log.info(String.format("Protocol/chanKick: deleting channel %s because it is empty and it is not persistent", chan.getName()));
        }

        client.write(str);
    }

    /**
     * Generic method to set a mode
     * @param client
     * @param who
     * @param target
     * @param modes
     * @param parameters
     * @throws Exception
     */
    public void setMode(Client client, String who, String target, String modes, String parameters) throws Exception { // XXX: to reimplement
        String networkChanUserModes          = protocolProps.get("PREFIX").replaceAll("[^A-Za-z0-9]", "");
        /*
         * CHANMODES=beI,fkL,lFH,cdimnprstzCDGKMNOPQRSTVZ
         *            |   |    |           `----------------------- group1: no parameter
         *            |   |     `---------------------------------- group2: parameter for set, no parameter for unset
         *            |    `--------------------------------------- group3: parameter for set, parameter for unset
         *             `------------------------------------------- group4: (list) parameter for set, parameter for unset
         */
    
        String networkChanModesGroup1        = ((protocolProps.get("CHANMODES")).split(",", 4))[0]; // (list) parameter for set, parameter for unset
        String networkChanModesGroup2        = ((protocolProps.get("CHANMODES")).split(",", 4))[1]; // parameter for set, parameter for unset
        String networkChanModesGroup3        = ((protocolProps.get("CHANMODES")).split(",", 4))[2]; // parameter for set, no parameter for unset
        String networkChanModesGroup4        = ((protocolProps.get("CHANMODES")).split(",", 4))[3]; // no parameter

        String str;
        if (who.isEmpty() == true) str = ":" + config.getServerId() + " MODE " + target + " " + modes + " " + parameters;
        else str = ":" + who + " MODE " + target + " " + modes + " " + parameters;

        
        userList.forEach( (userSid, user) -> { userNickSidLookup.put(user.getNick(), userSid); });

        if (modes.replaceFirst("[^A-za-z0-9]", "").matches("[" + networkChanUserModes + "]")) {
            if(modes.startsWith("+")) {
                userList.get(userNickSidLookup.get(parameters)).addUserModeChan(this.getChannelNodeByName(target), modes.replaceFirst("[^A-za-z0-9]", ""));
            }
            else if(modes.startsWith("-")) {
                userList.get(userNickSidLookup.get(parameters)).removeUserModeChan(this.getChannelNodeByName(target), modes.replaceFirst("[^A-za-z0-9]", ""));
            }
            else { throw new Exception("Set(+)/Unset(-) mode must be defined."); }

        }
        else if (modes.replaceFirst("[^A-za-z0-9]", "").matches("[" + networkChanModesGroup4 + "]")) {
            if(modes.startsWith("+")) {
                channelList.get(target).setMode(modes.replaceFirst("[^A-za-z0-9]", ""), "");
            }
            else if(modes.startsWith("-")) {
                channelList.get(target).delMode(modes.replaceFirst("[^A-za-z0-9]", ""), "");
            }
            else { throw new Exception("Set(+)/Unset(-) mode must be defined."); }            
        }

        else if (modes.replaceFirst("[^A-za-z0-9]", "").matches("[" + networkChanModesGroup3 + "]")) {
            if(modes.startsWith("+")) {
                channelList.get(target).setMode(modes.replaceFirst("[^A-za-z0-9]", ""), parameters);
            }
            else if(modes.startsWith("-")) {
                channelList.get(target).delMode(modes.replaceFirst("[^A-za-z0-9]", ""), "");
            }
            else { throw new Exception("Set(+)/Unset(-) mode must be defined."); }            
        }

        else if (modes.replaceFirst("[^A-za-z0-9]", "").matches("[" + networkChanModesGroup2 + "]")) {
            if(modes.startsWith("+")) {
                channelList.get(target).setMode(modes.replaceFirst("[^A-za-z0-9]", ""), parameters);
            }
            else if(modes.startsWith("-")) {
                channelList.get(target).delMode(modes.replaceFirst("[^A-za-z0-9]", ""), parameters);
            }
            else { throw new Exception("Set(+)/Unset(-) mode must be defined."); }            
        }
        else if (modes.replaceFirst("[^A-za-z0-9]", "").matches("[" + networkChanModesGroup1 + "]")) {
            if(modes.startsWith("+")) {
                switch(modes.charAt(1)) {
                    case 'b':
                    channelList.get(target).addBanList(parameters);
                    break;

                    case 'e':
                    channelList.get(target).addExceptList(parameters);
                    break;

                    case 'I':
                    channelList.get(target).addInviteList(parameters);
                    break;

                    default: throw new Exception("Unknown list.");
                }
            }
            else if(modes.startsWith("-")) {
                switch(modes.charAt(1)) {
                    case 'b':
                    channelList.get(target).delBanList(parameters);
                    break;

                    case 'e':
                    channelList.get(target).delExceptList(parameters);
                    break;

                    case 'I':
                    channelList.get(target).delInviteList(parameters);
                    break;

                    default: throw new Exception("Unknown list.");
                }
            }
            else { throw new Exception("Set(+)/Unset(-) mode must be defined."); }            
        }
        client.write(str);
    }

    /**
     * Sets a mode from an user to an user
     * @param client client
     * @param fromWho usernode
     * @param toTarget usernode
     * @param modes modes
     * @param parameters parameters
     * @throws Exception
     */
    public void setMode(Client client, UserNode fromWho, UserNode toTarget, String modes, String parameters) throws Exception { // FIXME: will not work because needs SVSMODE
        String who = fromWho.getUid();
        String target = toTarget.getNick();
        setMode(client, who, target, modes, parameters);
    }

    /**
     * Sets a mode from a server to an user
     * @param client client
     * @param fromWho servernode
     * @param toTarget usernode
     * @param modes modes
     * @param parameters parameters
     * @throws Exception
     */
    public void setMode(Client client, ServerNode fromWho, UserNode toTarget, String modes, String parameters) throws Exception { // FIXME: will not work because needs SVSMODE
        String who = fromWho.getServerId();
        String target = toTarget.getNick();
        setMode(client, who, target, modes, parameters);
    }

    /**
     * Sets a mode from an user to a channel
     * @param client client
     * @param fromWho usernode
     * @param toTarget channelnide
     * @param modes modes
     * @param parameters parameters
     * @throws Exception
     */
    public void setMode(Client client, UserNode fromWho, ChannelNode toTarget, String modes, String parameters) throws Exception {
        String who = fromWho.getUid();
        String target = toTarget.getName();
        setMode(client, who, target, modes, parameters);
    }

    public void setMlock(Client client, ServerNode fromWho, ChannelNode toTarget, String modes) {
        /* :5PB MLOCK 1681424518 #chan PCfHntT */
        String str;
        Long unixTime;
        unixTime = Instant.now().getEpochSecond();

        str = String.format(":%s MLOCK %s %s %s", fromWho.getServerId(), unixTime, toTarget.getName(), modes);
        client.write(str);
    }

    /**
     * @param client client
     * @param fromWho servernode
     * @param toTarget channelnode
     * @param modes modes
     * @param parameters parameters
     * @throws Exception
     */
    public void setMode(Client client, ServerNode fromWho, ChannelNode toTarget, String modes, String parameters) throws Exception {
        String who = fromWho.getServerId();
        String target = toTarget.getName();
        setMode(client, who, target, modes, parameters);
    }

    /**
     * @param client client
     * @param fromWho servernode
     * @param toTarget channelnode
     * @param modes modes
     * @param parameters parameters
     * @throws Exception
     */
    public void setMode(Client client, ChannelNode toTarget, String modes, String parameters) throws Exception {
        String who = config.getServerId();
        String target = toTarget.getName();
        setMode(client, who, target, modes, parameters);
    }

    public void setTopic(Client client, UserNode from, ChannelNode to, String topic) /*throws Exception*/ {
        String str = ":" + from.getUid() + " TOPIC " + to.getName() + " :" + topic;
        client.write(str);
    }

    public void chgHostVhost(Client client, UserNode toTarget, String vhost) {
        String who = config.getServerId();
        String vhostComplete = config.getCServeHostPrefix() + vhost + config.getCServeHostSuffix();
        String str;

        if (toTarget.getHost().equals(vhostComplete)) return;
        
        str= ":" + who + " CHGHOST " + toTarget.getUid() + " " + vhostComplete;
        client.write(str);
    }

    public void chgHost(Client client, UserNode toTarget, String vhost) {
        String who = config.getServerId();
        String vhostComplete = vhost;
        String str;

        if (toTarget.getHost().equals(vhostComplete)) return;
        
        str = ":" + who + " CHGHOST " + toTarget.getUid() + " " + vhostComplete;
        client.write(str);
    }

    public Boolean getFeature(String feature) {
        Boolean featureValue = false;
        if (featureList.containsKey(feature)) { featureValue = featureList.get(feature); }
        return featureValue;
    }

    public Map<String, ServerNode> getServerList() {
        return this.serverList;
    }

    public Map<String, UserNode> getUserList() {
        return this.userList;
    }

    public Map<String, ChannelNode> getChanList() {
        return this.channelList;
    }

    public String getPeerId() {
        return this.myPeerServerId;
    }

    private void sendSaslResult(UserNode user, Boolean success) {
        String str;
        
        if (success == true) str = ":" + config.getServerId() + config.getCServeUniq() + " SASL " + user.getSaslAuthParam("authServer") + " " + user.getUid() + " D S";
        else                 str = ":" + config.getServerId() + config.getCServeUniq() + " SASL " + user.getSaslAuthParam("authServer") + " " + user.getUid() + " D F";
        client.write(str);
    }

    private void sendSaslQuery(UserNode user) {
        String str;

        str = ":" + config.getServerId() + config.getCServeUniq() + " SASL " + user.getSaslAuthParam("authServer") + " " + user.getUid() + " C +";
        client.write(str);
    }

    private void sendSvsLogin(UserNode user, UserAccount account, Boolean auth) {
        String str;
        String toServerSid = "";

        if (user.getSaslAuthParam("authServer") != null) {
            toServerSid = user.getSaslAuthParam("authServer");
        }
        else {
            toServerSid = user.getServer().getServerId();
        }

        // :5PB SVSLOGIN ocelot. 5P0QVW5M3 AnhTay
        if (auth == true) str = ":" + config.getServerId() + " SVSLOGIN " + toServerSid + " " + user.getUid() + " " + account.getName();
        else str = ":" + config.getServerId() + " SVSLOGIN " + toServerSid + " " + user.getUid() + " 0";
        client.write(str);
    }

    public void sendSvsLogin(UserNode user) {
        sendSvsLogin(user, null, false);
    }

    public void sendSvsLogin(UserNode user, UserAccount account) {
        sendSvsLogin(user, account, true);
    }

    /**
     * ...
     * @param str Modes string, e.g "+abc-de+g... param1 param2 param3 param4 param5..."
     * @param chanNode Channel Node (to use only with MODE command, use null for SJOIN)
     * @return ...
     */
    public HashMap<String, HashMap<String, String>> parseChanModes(String str) {

        /*
         * CHANMODES=beI,fkL,lFH,cdimnprstzCDGKMNOPQRSTVZ
         *           --- --- --- ------------------------
         *            |   |    |           `----------------------- group4: no parameter
         *            |   |     `---------------------------------- group3: parameter for set, no parameter for unset
         *            |    `--------------------------------------- group2: parameter for set, parameter for unset
         *             `------------------------------------------- group1: (list) parameter for set, parameter for unset
         */

        HashMap<String, String>     chanModes = new HashMap<>();
        HashMap<String, String> chanUserModes = new HashMap<>();
        HashMap<String, String>     chanLists = new HashMap<>();

        HashMap<String, HashMap<String, String>> result = new HashMap<>();
        result.put("chanModes",     chanModes);
        result.put("chanLists",     chanLists);
        result.put("chanUserModes", chanUserModes);

        String networkChanModesGroup1        = ((protocolProps.get("CHANMODES")).split(",", 4))[0]; // (lists) parameter for set, parameter for unset
        String networkChanModesGroup2        = ((protocolProps.get("CHANMODES")).split(",", 4))[1]; // parameter for set, parameter for unset
        String networkChanModesGroup3        = ((protocolProps.get("CHANMODES")).split(",", 4))[2]; // parameter for set, no parameter for unset
        String networkChanModesGroup4        = ((protocolProps.get("CHANMODES")).split(",", 4))[3]; // no parameter
        String   networkChanUserModes        =   protocolProps.get("PREFIX").replaceAll("[^A-Za-z0-9]", ""); // Channel modes for users


        /*
         *       Modes = strSplit[0]
         *  Parameters = strSplit[1+]
         */
        String[] strSplit = str.split(" ");

        String modes = strSplit[0];
        String curMode;
        String strUserModes = "";
        String strLists = "";

        char modeAction = '+';

        Integer modeIndex  = 0;
        Integer paramIndex = 1;

        UserNode userNode;

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

                if (curMode.matches("[" + networkChanModesGroup1 + "]")) {
                    strLists = "";
                    strLists = String.join(" ", chanLists.get(modeAction + curMode), strSplit[paramIndex]);
                    chanLists.replace(modeAction + curMode, strLists);
                    paramIndex++;
                }

                else if (curMode.matches("[" + networkChanModesGroup2 + "]")) {
                    chanModes.put(modeAction + curMode, strSplit[paramIndex]);
                    paramIndex++;
                }

                else if (curMode.matches("[" + networkChanModesGroup3 + "]")) {
                    if (modeAction == '+') {
                        chanModes.put(modeAction + curMode, strSplit[paramIndex]);
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
                    userNode = this.getUserNodeByNick(strSplit[paramIndex]);

                    strUserModes = String.join(" ", chanUserModes.get(modeAction + curMode), userNode.getNick());
                    chanUserModes.replace(modeAction + curMode, strUserModes);
                    paramIndex++;
                }
            }

            modeIndex++;
        }

        return result;
    }


    public void getResponse(String raw) throws Exception {
        String response = "";
        String fromEnt;
        String v3Tag = "";

        String[] command;
        
        command = raw.split(" ", 3); // Begin to split raw message to fetch the command (part0 part1 part2part3part4...)

        // Check for IRCv3 string presence, if yes we cut if off to part1 part2 part3part4...
        // @blaablaa ...
        if (command[0].startsWith("@")) {
            v3Tag = command[0];
            command = (command[1] + " " + command[2]).split(" ", 3); // This cuts the IRCv3 prelude
        }
        if (command[1].equals("PRIVMSG")) {
            // :ABC PRIVMSG  DEF :MESSAGE
            // | 0| |    1| |      2     |   
            String fromEntity = (command[0].split(":"))[1];
            UserNode fromUser = userList.get(fromEntity);

            command = (command[2]).split(" ", 2);
            String toEntity   =  command[0];

            String message    =  command[1];

            /* In here we forward to Chanservice the PRIVMSG if sent to them */
            if (toEntity.equals(config.getServerId() + config.getCServeUniq())) {
                // Stripping raw to keep only the message
                message = (message.split(":",2))[1];
                cservice.handleMessage(fromUser, message);
            }
        }
        else if (command[1].equals("SID")) {
            // SID is used by the peer to introduce the other servers
            // :peer SID name hop sid :description
            //<<< :5P0 SID sandcat. 2 5PX :Mjav Network IRC server

            fromEnt = (command[0].split(":"))[1];

            ServerNode fromEntNode = serverList.get(fromEnt);
            
            command = (command[2]).split(" ", 4);
            String name = command[0];
            Integer hop = Integer.valueOf(command[1]);
            String sid = command[2];
            String desc = (command[3].split(":"))[1];
            server = new ServerNode(name, hop, sid, desc);
            server.setIntroducedBy(fromEntNode);
            serverList.put(sid, server);
        }
        else if (command[1].equals("EOS")) {
            //<<< :5PX EOS

            fromEnt = (command[0].split(":"))[1];

            ServerNode server = serverList.get(fromEnt);
            server.setEOS(true);

            String str = ":" + config.getServerId() + " EOS";
            client.write(str);
            serverList.get(config.getServerId()).setEOS(true);

            /* If our peer sends the EOS (so last to send EOS) */
            if(server.getServerPeer() == true) {

                /* Not in netburst anymore */
                this.networkInsideNetBurst = false;

                // Identify what are the available channel modes
                if (protocolProps.containsKey("PREFIX")) {
                    String chanPrefix = protocolProps.get("PREFIX"); /* PREFIX = (modelist)prefixlist, e.g PREFIX=(qaohv)~&@%+ */
                    String chanModes = "";
                    Pattern pattern = Pattern.compile("\\((.*?)\\)");
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
            }
        }
        else if (command[0].equals("NETINFO")) {
            //:ABC NETINFO 13        1683227483  6000                   SHA256:06aa55fd33c824d6132b0aebc1da0cd0e253473f68391a5ace8cf0bd 0 0 0 :Mjav
            //     NETINFO maxglobal currenttime protocolversion        cloakhash                                                       0 0 0 :networkname
            //     NETINFO xx        unixTime    config.protocolversion *                                                               0 0 0 :config.netName

            String[] netinfoParam = raw.split(" ", 20);

            unixTime = Instant.now().getEpochSecond();

            String str = ":" + config.getServerId() + " NETINFO " + netinfoParam[1] + " " + unixTime + " " + config.getSrvProtocolVersion() + " * 0 0 0 :" + config.getNetworkName();
            client.write(str);


            /* Sending that we can handle SASL (in enabled in the config) */
            if (config.getFeature("sasl") == true) {
                str = ":" + config.getServerId() + " MD client " + config.getServerName() + " saslmechlist :EXTERNAL,PLAIN";
                client.write(str);
            }

        }
        else if (command[1].equals("MD")) {
            //:ABC MD client lynx.      saslmechlist :EXTERNAL,PLAIN
            //:ABC MD client <nick|uid> <varname>    <value>
            String   mdString = command[2];
            String[] mdParams;

            mdParams = mdString.split(" ", 10);


            
            switch(mdParams[2]) {
                case "certfp": //:SID MD client <UID> certfp :<certfp string>
                    String target = mdParams[1];
                    UserNode userNode;
                    if (target.length() == 9) { 
                        userNode = getUserNodeBySid(target);
                        userNode.setCertFP(mdParams[3].replaceFirst(":", ""));
                    } 
                    break;
            }
        }
        else if (command[1].equals("SINFO")) {
            //<<< :5PX SINFO 1683275149 6000 diopqrstwxzBDGHIRSTWZ beI,fkL,lFH,cdimnprstzCDGKMNOPQRSTVZ * :UnrealIRCd-6.1.0

            //fromEnt = (command[0].split(":"))[1];
            
            //command = (command[2]).split(" ", 4);
            //server = serverList.get(fromEnt);
            //System.out.println("@@@ update server info for " + fromEnt + " " + server.getServerName());
            
            //server.setTS(command[0]);
        }
        else if (command[0].equals("PROTOCTL")) {
            //<<< PROTOCTL NOQUIT NICKv2 SJOIN SJOIN2 UMODE2 VL SJ3 TKLEXT TKLEXT2 NICKIP ESVID NEXTBANS SJSBY MTAGS
            //<<< PROTOCTL CHANMODES=beI,fkL,lFH,cdimnprstzCDGKMNOPQRSTVZ USERMODES=diopqrstwxzBDGHIRSTWZ BOOTED=1683274785 PREFIX=(ov)@+ SID=5P0 MLOCK TS=1683309454 EXTSWHOIS
            //<<< PROTOCTL NICKCHARS= CHANNELCHARS=utf8

            String[] prop = raw.split(" ", 20);
            int propsCount = raw.split(" ", 20).length;
            
            String propertyName;
            String propertyValue;
            
            for (int i=1; i < propsCount; i++) {
                if (prop[i].contains("=")) {
                    propertyName = (prop[i].split("=", 2))[0];
                    propertyValue = (prop[i].split("=", 2))[1];
                }
                else {
                    propertyName = prop[i];
                    propertyValue = "";
                }
                protocolProps.put( propertyName, propertyValue );
                if (prop[i].startsWith("SID")) {
                    myPeerServerId = (prop[i].split("="))[1];
                    server = new ServerNode((prop[i].split("="))[1]);
                    server.setPeer(true);
                    server.setIntroducedBy(server);
                    serverList.put((prop[i].split("="))[1], server);
                }

            }
            serverList.get(config.getServerId()).setServerPeerResponded(true);
        }
        else if (command[0].equals("SERVER")) {
            //<<< SERVER ocelot. 1 :U6000-Fhn6OoEmM-5P0 Mjav Network IRC server
            String[] string = raw.split(" ", 4);
            ServerNode server = serverList.get(myPeerServerId);
            server.setServerName(string[1]);
            server.setServerDistance(Integer.valueOf(string[2]));
            server.setServerDescription((string[3].split(":"))[1]);
            
            serverList.get(config.getServerId()).setServerPeerResponded(true);
        }
        else if (command[0].equals("SQUIT")) { // XXX: to be (largely) improved
            //<<< SQUIT ocelot. :squit message
            String serverName = command[1];
            var wrapper = new Object(){ ServerNode sQuittedServer; };
            ServerNode sQuittedServer;
            serverList.forEach( (sid, servernode) -> {
                if (servernode.getServerName().equals(serverName)) { wrapper.sQuittedServer = servernode; }
            });
            sQuittedServer = wrapper.sQuittedServer;

            ArrayList<ServerNode> affectedServers = new ArrayList<>();
            ArrayList<UserNode>   affectedUsers = new ArrayList<>();

            // SQUITted server is first affected
            affectedServers.add(sQuittedServer);

            // Then we need to find all the servers introduced by the SQUITted server
            serverList.forEach( (sid, servernode) -> {
                if (servernode.getIntroducedBy().equals(sQuittedServer)) {
                    affectedServers.add(servernode);
                }
            });

            // List usernodes on those servers
            userList.forEach( (uniq, usernode) -> {
                if(affectedServers.contains(usernode.getServer())) {
                    affectedUsers.add(usernode);
                }
            });

            // Delete the usernodes
            for(UserNode user : affectedUsers) {
                // Deauth user if needed
                if (user.isAuthed() == true) {
                    user.getAccount().delUserAuth(user);
                    user.setUserAuthed(false);
                }
                userList.remove(user.getUid());
            }

            // Delete the servers
            for(ServerNode servernode : affectedServers) {
                serverList.remove(servernode.getServerName());
            }
        }
        else if (command[1].equals("UID")) {
            // :AAAAA UID nickname hopcount timestamp username hostname uid servicestamp usermodes virtualhost cloakedhost ip :gecos

            fromEnt = (command[0].split(":"))[1];
            command = command[2].split(" ", 12);

            UserNode user;

            if (userList.containsKey(command[5]) == false) {
                user = new UserNode( command[0],                 // nick
                                            command[3],                   // ident
                                            command[8],                   // vhost
                                            command[4],                   // realhost
                                            (command[11].split(":"))[1],  // gecos
                                            command[5],                   // unique id
                                            Integer.parseInt(command[2]), // TS
                                            command[7]                    // modes
                                        );

                userList.put(command[5], user);
                userNickSidLookup.put(command[0], command[5]);
                user.setServer(serverList.get(fromEnt));
                user.setCloakedHost(command[9]);                    // cloaked host
                user.setIpAddress(command[10]);                            // IP address
            }

            else {
                user = userList.get(command[5]);

                user.setNick(command[0]);                       // nick
                user.setIdent(command[3]);                      // ident
                user.setHost(command[8]);                       // vhost
                user.setRealHost(command[4]);                   // realhost
                user.setRealName((command[11].split(":"))[1]);  // gecos
                user.setUserTS(Long.parseLong(command[2]));         // TS
                user.setUserModes(command[7]);                      // modes
                user.setCloakedHost(command[9]);                    // cloaked host
                user.setIpAddress(command[10]);                     // IP address

                /* Section to update auth token in the db if the user was authed using SASL, because in this case their TS and ident was unknown */
                /* Also a good place to set the vhost */
                //@s2s-md/creationtime=1685464827 :5PK UID plop 0 1685464824 plop desktop-lpvlp15 5PKE08M08 0 +iwx cloak/-F9228E5A cloak/-F9228E5A wKgKGA== :...
                if (user.getAuthBySasl() == true) {
                    sqliteDb.updateUserAuth(user);
                }

            }

            if (this.networkInsideNetBurst == true) {
                /* Trying to authenticate the user if it was already authed (netjoin), only during sync */
                UserAccount accountToReauth = null;
                try {
                    accountToReauth = sqliteDb.getUserLoginToken(user);
                }
                catch (Exception e) {
        
                }

                if (accountToReauth != null) {
                    user.setUserAuthed(true);
                    user.setAccount(accountToReauth);

                    if (config.getFeature("svslogin") == true) {
                        this.sendSvsLogin(user, user.getAccount());
                    }
            
                    if (Flags.isUserAutoVhost(user.getAccount().getFlags()) == true && config.getFeature("chghost") == true) {
                        this.chgHostVhost(client, user, user.getAccount().getName());
                    }

                    sqliteDb.addUserAuth(user, Const.AUTH_TYPE_REAUTH);
                }
            }
        }
        else if (command[1].equals("SASL") && config.getFeature("sasl") == true) {

            fromEnt = command[0].replaceFirst(":", "");
            command = command[2].split(" ", 12);



            /*
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
            * 1.  user_sid -> auth_sid H <realhost> <ip>
            *
            * 2a. user_sid -> auth_sid S PLAIN
            * 2b. user_sid -> auth_sid S EXTERNAL <certfp>
            *
            * 3.  auth_uid -> user_uid C +
            *
            * 4a. user_sid -> auth_uid C <base64(login\0login\0pass)>
            * 4b. user_sid -> auth_uid C +
            *
            * 5a. auth_uid -> user_uid D S  (success)
            * 5b. auth_uid -> user_sid D F  (failure)
            *
            */

            /*
             *    Received " H " from user's server
             * command[0] = SASL server
             * command[1] = User SID
             * command[2] = H
             * command[3] = Hostname
             * command[4] = IP
             * command[5] = P if plaintext connection, else empty
             */

            /*
             *    Received " S " from user's server
             * command[0] = SASL server
             * command[1] = User SID
             * command[2] = S
             * command[3] = Client SASL type (PLAIN for user/pass, EXTERNAL for authentication through ircd)
             * command[4] = CertFP if EXTERNAL, empty if PLAIN
             */


            /*
             *    Received " C " from user's server
             * command[0] = SASL server
             * command[1] = User SID
             * command[2] = C
             * command[3] = Base64 hash of <Login\0Login\0Pass>
             */

            /*
             *   Auth failure => Sent " D " from authenticator user SID to user's server
             * command[0] = user's server SID
             * command[1] = User SID
             * command[2] = D
             * command[3] = F
             */

            /*
             *    Auth success => Sent " SVSLOGIN " from authenticator server SID to user's server
             */

            /*
             *    Auth success => Sent " D " from authenticator SID to user's server
             * command[0] = user's server SID
             * command[1] = User SID
             * command[2] = D
             * command[3] = S
             */

            /*
             *    Auth success => Sent " CHGHOST " from authenticator server SID to user's server
             * command[0] = User SID
             * command[1] = vhost
             */
            UserNode user;
            UserAccount userAccountToAuth;

            byte[] decodedAuthString;
            String authString;
            Integer authType;

            switch(command[2]) {
                case "H": // first SASL handshake => create the user
                    /*
                    *    Received " H " from user's server
                    * command[0] = SASL server
                    * command[1] = User SID
                    * command[2] = H
                    * command[3] = Hostname
                    * command[4] = IP
                    * command[5] = P if plaintext connection, else empty
                    */
                    user = new UserNode(command[1], command[3]);
                    user.setServer(serverList.get(fromEnt));
                    userList.put(command[1], user);
                    break;

                case "S":
                   /*
                    *    Received " S " from user's server
                    * command[0] = SASL server
                    * command[1] = User SID
                    * command[2] = S
                    * command[3] = Client SASL type (PLAIN for user/pass, EXTERNAL for authentication through ircd)
                    * command[4] = CertFP if EXTERNAL, empty if PLAIN
                    */

                    user = this.getUserNodeBySid(command[1]);
                    user.setSaslAuthParam("authType", command[3]);
                    if (command[3].equals("EXTERNAL")) { 
                        try {
                            user.setSaslAuthParam("authExt", command[4]);
                            user.setCertFP(command[4]);
                        }
                        catch (Exception e) {
                            log.warn("* User is trying SASL EXTERNAL but does not provide certfp");
                            e.printStackTrace();
                        }
                    }
                    user.setSaslAuthParam("authServer", command[1]);

                    /*
                    * Send " C + " from authenticator UID (e.g. Q) to user's server
                    * >>> :5PBAAAAAF SASL ocelot. 5P0QVW5M3 C +
                    */

                    this.sendSaslQuery(user);

                    break;

                case "C":
                    /*
                    *    Received " C " from user's server
                    * command[0] = SASL server
                    * command[1] = User SID
                    * command[2] = C
                    * command[3] = Base64 hash of <Login\0Login\0Pass>
                    */

                    user = this.getUserNodeBySid(command[1]);

                    if (user.getSaslAuthParam("authType").equals("EXTERNAL") == true && command[3].equals("+") == true) { // SASL EXTERNAL auth
                        authType = Const.AUTH_TYPE_SASL_EXT;

                        String authCertFp = user.getSaslAuthParam("authExt");

                        /* SASL EXTERNAL => no username provided => need to find in all the accounts to which belongs the certfp */
                        var wrapper = new Object() { UserAccount userAccountToAuth; };
                        this.userAccounts.forEach( (username, useraccount) -> {

                            if (useraccount.getCertFP().contains(authCertFp)) {
                                wrapper.userAccountToAuth = useraccount;
                            }
                            else { wrapper.userAccountToAuth = null; }

                        });

                        userAccountToAuth = wrapper.userAccountToAuth;

                        if (userAccountToAuth != null) { // Certfp found => Auth successful

                            userAccountToAuth.authUserToAccount( user,  authCertFp,  authType);

                            this.sendSaslResult(user, true);


                            if (config.getFeature("svslogin") == true) {
                                this.sendSvsLogin(user, userAccountToAuth);
                            }

                            if (Flags.isUserAutoVhost(user.getAccount().getFlags()) == true && config.getFeature("chghost") == true) {
                                this.chgHostVhost(client, user, user.getAccount().getName());
                            }

                            userAccountToAuth.getChanlev().forEach( (channel, chanlev) -> { 
                                if (Flags.isChanLAutoInvite(chanlev) == true) {
                                    this.sendInvite(client, user, this.getChannelNodeByName(channel));
                                }

                            });

                            return;
                        }
                        else { // Certfp not found => Auth fail
                            this.sendSaslResult(user, false);
                            return;
                        }
                    }

                    else { // SASL PLAIN auth
                        Base64.Decoder dec = Base64.getDecoder();
                        decodedAuthString = dec.decode(command[3]);
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
                            e.printStackTrace(); 
                            this.sendSaslResult(user, false);
                            return;

                        }

                        userAccountToAuth = this.getUserAccount(login);

                        if (userAccountToAuth == null) { /* Auth failed (unknown user account) */
                            Thread.sleep(config.getCServeAccountWrongCredWait() *1000);
                            this.sendSaslResult(user, false);
                            return;
                        }
                        
                        userAccountToAuth.authUserToAccount(user, password, authType);

                        if (user.isAuthed() == false) { /* Auth failed */
                            //Thread.sleep(config.getCServeAccountWrongCredWait() *1000);
                            this.sendSaslResult(user, false);
                            return;
                        }

                        /* Auth success */

                        this.sendSaslResult(user, true);
                        if (config.getFeature("svslogin") == true) {
                            this.sendSvsLogin(user, userAccountToAuth);
                        }
                        if (Flags.isUserAutoVhost(user.getAccount().getFlags()) == true && config.getFeature("chghost") == true) {
                            this.chgHostVhost(client, user, user.getAccount().getName());
                        }

                        userAccountToAuth.getChanlev().forEach( (channel, chanlev) -> { 
                            if (Flags.isChanLAutoInvite(chanlev) == true) {
                                this.sendInvite(client, user, this.getChannelNodeByName(channel));
                            }

                        });
                        

                    }


                    break;



            }


        }
        else if (command[1].equals("SJOIN")) {
            // :5P0 SJOIN 1680362593 #mjav         +fnrtCPST [5j#R1,7m#M1,3n#N1,5t#b1]:6         :5PKEPJH3U @5PXDR1D20 @5P0FWO841 
            // :5P0 SJOIN 1681424518 #Civilization +fnrtCHPS [30j#R10,40m#M10,10n#N15]:15 50:15m :@5PX8ZA302 @5PBAAAAAI &test!*@* "test!*@* 'test!*@*
            // :5PX SJOIN 1679224907 #test                                                       :5PX8ZA302
            // :5PX SJOIN 1683480448 #newChan                                                    :@5PX8ZA302

            /*
             * SJOIN prefixes
             *   + -> +v
             *   % -> +h
             *   @ -> +o
             *   ~ -> +a
             *   * -> +q
             *   & -> +b
             *   " -> +e
             *   ' -> +I
             */

            fromEnt = (command[0].split(":"))[1];

            String[] sjoinParam      = command[2].split(" ", 64);
            int      sjoinParamCount = command[2].split(" ", 64).length;

            String sJoinChan = (command[2].split(":", 2) [0]).split(" ", 3) [1];
            String sJoinModes = "";
            String sJoinList  = "";
            String chanListItem = "";

            ChannelNode chan;
            UserNode user = null;

            // command[2]               = 1678637814 #thibland +fmnrstzCPST [5j#R1,7m#M1,3n#N1,5t#b1]:6 :@5P0PWVF03
            // command[2].split(, 3)    = 1678637814/#thibland/+fmnrstzCPST [5j#R1,7m#M1,3n#N1,5t#b1]:6 :@5P0PWVF03
            // command[2].split(, 3)[2] = +fmnrstzCPST [5j#R1,7m#M1,3n#N1,5t#b1]:6 :@5P0PWVF03
            // command[2].split(, 3)[2].split() = +fmnrstzCPST/[5j#R1,7m#M1,3n#N1,5t#b1]:6/:@5P0PWVF03
            String[] sJoinParameters = command[2].split(" ", 3)[2].split(" ");

            Long channelTS = Long.parseLong(sjoinParam[0]);

            Boolean modeSection = true;
            for (String param: sJoinParameters) {
                if (param.startsWith(":") == true) modeSection = false;

                if (modeSection == true) sJoinModes = String.join(" ", sJoinModes, param);
                else sJoinList = String.join(" ", sJoinList, param.replaceFirst(":", ""));
            }

            log.debug(String.format("Protocol/SJOIN: SJOIN (raw) for %s received modes: %s", sJoinChan, sJoinModes));
            log.debug(String.format("Protocol/SJOIN: SJOIN (raw) for %s received list: %s", sJoinChan, sJoinList));

            HashMap<String, HashMap<String, String>>   modChanModesAll  = this.parseChanModes(sJoinModes);

            HashMap<String, String>      modChanModes   =  modChanModesAll.get("chanModes");

            if (this.channelList.containsKey(sJoinChan) == false) { /* channel does not exist => creating it */
                chan = new ChannelNode( sJoinChan, channelTS );

                channelList.put(sJoinChan, chan);
                channelList.get(sJoinChan).setChanlev(sqliteDb.getChanChanlev(chan));

                log.info(String.format("Protocol/SJOIN: creating channel %s (TS %s)", chan.getName(), chan.getChanTS()));
            }
            else { /* channel already exists because either it was registered or it is just a regular join after EOS */
                chan = this.getChannelNodeByName(sJoinChan);
            }

            /* 
             * Sets the chan modes
             */ 

            modChanModes.forEach( (mode, parameter) -> {
                log.debug(String.format("Protocol/SJOIN: Channel %s: (parsed) change mode: %s %s", chan.getName(), mode, parameter));
                if (mode.startsWith("+")) chan.addMode(mode, parameter);
                else chan.delMode(mode, parameter);
            });

            /* Parsing the SJOIN list */
            for ( String listItem : sJoinList.split(" ") ) {

                chanListItem = ""; 

                if (listItem.startsWith("&") == false && listItem.startsWith("\"") == false && listItem.startsWith("'") == false && listItem.isEmpty() == false) {
                    user = this.getUserNodeBySid(listItem.replaceAll("^[^A-Za-z0-9]{1}", ""));
                    log.info(String.format("Protocol/SJOIN: Channel %s: user %s (%s) joined channel", chan.getName(), user.getNick(), user.getUid()));
                }
                else { 
                    chanListItem =  listItem.replaceAll("^.", ""); 
                    log.debug(String.format("Protocol/SJOIN: Channel %s: list %s", chan.getName(), listItem));
                }


                
                if (listItem.startsWith("+")) { // +v
                    user.addToChan(chan, "v");
                    log.debug(String.format("Protocol/SJOIN: Channel %s: (parsed) join+set usermode: +v %s (%s)", chan.getName(), user.getNick(), user.getUid()));
                }

                else if (listItem.startsWith("%")) { // +h
                    user.addToChan(chan, "h");
                    log.debug(String.format("Protocol/SJOIN: Channel %s: (parsed) join+set usermode: +h %s (%s)", chan.getName(), user.getNick(), user.getUid()));
                }

                else if (listItem.startsWith("@")) { // +o
                    user.addToChan(chan, "o");
                    log.debug(String.format("Protocol/SJOIN: Channel %s: (parsed) join+set usermode: +o %s (%s)", chan.getName(), user.getNick(), user.getUid()));
                }

                else if (listItem.startsWith("~")) { // +h
                    user.addToChan(chan, "a");
                    log.debug(String.format("Protocol/SJOIN: Channel %s: (parsed) join+set usermode: +a %s (%s)", chan.getName(), user.getNick(), user.getUid()));
                }

                else if (listItem.startsWith("*")) { // +q
                    user.addToChan(chan, "q");
                    log.debug(String.format("Protocol/SJOIN: Channel %s: (parsed) join+set usermode: +q %s (%s)", chan.getName(), user.getNick(), user.getUid()));
                }

                else if (listItem.startsWith("&")) { // +b
                    chan.addBanList(chanListItem);
                    log.debug(String.format("Protocol/SJOIN: Channel %s: (parsed) set list: +b %s", chan.getName(), chanListItem));

                }

                else if (listItem.startsWith("\"")) { // +e
                    chan.addExceptList(chanListItem);
                    log.debug(String.format("Protocol/SJOIN: Channel %s: (parsed) set list: +e %s", chan.getName(), chanListItem));

                }

                else if (listItem.startsWith("'")) { // +I
                    chan.addInviteList(chanListItem);
                    log.debug(String.format("Protocol/SJOIN: Channel %s: (parsed) set list: +I %s", chan.getName(), chanListItem));

                }

                else { // no mode
                    if (user != null) {
                        user.addToChan(chan, "");
                        log.debug(String.format("Protocol/SJOIN: Channel %s: (parsed) join+no usermode: %s (%s) - usercount = %s", chan.getName(), user.getNick(), user.getUid(), chan.getUserCount()));
                    }
                }


                Boolean cServeReady = false;
                try {
                    cServeReady = cservice.isReady();
                }
                catch (Exception e) {
                    log.debug("Protocol/SJOIN: user joined the chan but CService not ready yet");
                }

                if (cServeReady == true && user != null && chan.isRegistered() == true) {
                    log.info(String.format("Protocol/SJOIN: CService ready, sending the join to CService"));
                    cservice.handleJoin(user, chan);
                }

            }
            log.info(String.format("Protocol/SJOIN: setting channel %s (usercount %s)", chan.getName(), chan.getUserCount()));

        }
        else if (command[1].equals("MODE")) {
            // :5PX     MODE  #newChan      +ntCT         1683480448
            // :AAAAAAA MODE  #Civilization +o AnhTay

            String[] modeList      = command[2].split(" ", 2);

            String channelName = modeList[0];
            String modeChange  = modeList[1];
            
            ChannelNode chan = channelList.get(channelName);

            log.debug(String.format("Protocol/MODE: Channel %s: (raw) received mode change: %s", chan.getName(), modeChange));

            HashMap<String, HashMap<String, String>>   modChanModesAll  = this.parseChanModes(modeChange);

            HashMap<String, String>      modChanModes   =  modChanModesAll.get("chanModes");
            HashMap<String, String>      modChanLists   =  modChanModesAll.get("chanLists");
            HashMap<String, String>  modChanUserModes   =  modChanModesAll.get("chanUserModes");

            /* Sets the chan user modes */
            var wrapperUMode = new Object() { String[] nicks; UserNode userNode; };
            modChanUserModes.forEach( (mode, nicks) -> {
                wrapperUMode.nicks = nicks.split(" ");
                for (String nick: wrapperUMode.nicks) {
                    if (nick.isEmpty() == false) {
                        wrapperUMode.userNode = this.getUserNodeByNick(nick);
                        log.debug(String.format("Protocol/MODE: Channel %s: (parsed) change usermode: %s %s", chan.getName(), mode, nick));
                        try {
                            if (mode.startsWith("+")) wrapperUMode.userNode.addUserModeChan(chan, String.valueOf(mode.charAt(1)));
                            else wrapperUMode.userNode.removeUserModeChan(chan, String.valueOf(mode.charAt(1)));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            /* Sets the chan modes */
            modChanModes.forEach( (mode, parameter) -> {
                log.debug(String.format("Protocol/MODE: Channel %s: (parsed) change mode: %s %s", chan.getName(), mode, parameter));
                if (mode.startsWith("+")) chan.addMode(mode, parameter);
                else chan.delMode(mode, parameter);
            });

            /* Sets the chan lists */
            var wrapperCList = new Object() { String[] parameters; };
            modChanLists.forEach( (list, parameters) -> {
                wrapperCList.parameters = parameters.split(" ");
                for (String parameter: wrapperCList.parameters) {
                    if (parameter.isEmpty() == false) {
                        log.debug(String.format("Protocol/MODE: Channel %s: (parsed) change list: %s %s", chan.getName(), list, parameter));
                        if (list.equals("+b")) chan.addBanList(parameter); 
                        else if (list.equals("-b")) chan.delBanList(parameter); 
                        else if (list.equals("+e")) chan.addExceptList(parameter); 
                        else if (list.equals("-e")) chan.delExceptList(parameter); 
                        else if (list.equals("+I")) chan.addInviteList(parameter); 
                        else if (list.equals("-I")) chan.delInviteList(parameter);
                    }
                }
            });

            log.debug("Protocol/MODE: Sending to CService");
            cservice.handleChanMode(chan, modChanModesAll);

        }
        else if (command[1].equals("PART")) {
            // :XXXXXXXXX PART #1 :message

            fromEnt = (command[0].split(":"))[1];
            UserNode fromUser = userList.get(fromEnt);

            String fromChannel = (command[2].split(" "))[0];

            ChannelNode chanUserPart = channelList.get(fromChannel);

            Integer chanUserCount = 0;
            
            try {
            fromUser.removeFromChan( chanUserPart );
            log.info(String.format("Protocol/chanPart: user %s left chan %s", fromUser.getNick(), chanUserPart.getName()));
            }
            catch (Exception e) {
                e.printStackTrace();
                log.error(String.format("Protocol/chanPart: cannot remove the user %s from chan %s because it is not inside it", fromUser.getNick(), chanUserPart.getName()));
            }
            chanUserCount = chanUserPart.getUserCount();
            
            if (chanUserCount.equals(0) == true && ! chanUserPart.getModes().containsKey("P") ) {
                log.info(String.format("Protocol/PART: deleting channel %s because it is empty and it is not persistent", chanUserPart.getName()));
                channelList.remove( chanUserPart.getName() );
                chanUserPart = null;
            }
            else {
                log.info(String.format("Protocol/chanPart: setting channel %s usercount to %s", chanUserPart.getName(), chanUserPart.getUserCount()));
            }
        }
        else if (command[1].equals("KICK")) {
            // :XXXXXXXXX KICK CHAN SID :message

            fromEnt = (command[0].split(":"))[1];
            UserNode kickedUSer = userList.get((command[2].split(" "))[1]);

            String kickChannelName = (command[2].split(" "))[0];

            ChannelNode chanUserPart = channelList.get(kickChannelName);

            Integer chanUserCount = 0;
            
            kickedUSer.removeFromChan( this.getChannelNodeByName(kickChannelName) );
            chanUserCount = chanUserPart.getUserCount();
            
            if (chanUserCount.equals(0) == true && ! chanUserPart.getModes().containsKey("P") ) {
                channelList.remove( chanUserPart.getName() );
                chanUserPart = null;
            }
        }
        else if (command[1].equals("QUIT")) {
            // :XXXXXXXXX QUIT :message
            fromEnt = (command[0].split(":"))[1];
            
            UserNode userToRemove = userList.get(fromEnt);
            HashMap<ChannelNode, String> curUserChanList = new HashMap<>(userToRemove.getChanList());

            curUserChanList.forEach( (chan, mode) -> {
                try {
                    userToRemove.removeFromChan(chan);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });

            if (userToRemove.isAuthed() == true) sqliteDb.delUserAuth(userToRemove, Const.DEAUTH_TYPE_QUIT, command[2].toString().replaceFirst(":", ""));
            userToRemove.setAccount(null);

            userNickSidLookup.remove(userToRemove.getNick());
            //userToRemove = null;
            userList.remove(fromEnt);
        }
        else if (command[1].equals("KILL")) {
            // :AAAAAAA KILL AAAAAAA :message
            fromEnt = (command[0].split(":"))[1];
            UserNode killedUser = this.getUserNodeBySid(command[2].split(" ")[0]);

            HashMap<ChannelNode, String> curUserChanList = new HashMap<>(killedUser.getChanList());

            curUserChanList.forEach( (chan, mode) -> {
                try {
                    killedUser.removeFromChan(chan);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });

            if (killedUser.isAuthed() == true) sqliteDb.delUserAuth(killedUser, Const.DEAUTH_TYPE_QUIT, command[2].toString().replaceFirst(":", ""));
            killedUser.setAccount(null);

            userNickSidLookup.remove(killedUser.getNick());
            //userToRemove = null;
            userList.remove(killedUser.getUid());
        }
        else if (command[1].equals("NICK")) {
            // :XXXXXXXXX NICK ...

            fromEnt = (command[0].split(":"))[1];
            
            userNickSidLookup.remove(userList.get(fromEnt).getNick());
            userNickSidLookup.put((command[2].split(" "))[0], fromEnt);

            userList.get(fromEnt).setNick( (command[2].split(" "))[0] );


        }
        else if (command[0].equals("TOPIC")) { /* only at syncing */
            //TOPIC #chan w!h@h ts :topic


            String[] topicRawStr = raw.split(" ", 5);
            ChannelNode chanNode;
            
            /* Normally TOPIC is always sent after SJOIN, so the channel should be created. */
            chanNode = getChannelNodeByName(topicRawStr[1]);

            chanNode.setTopic(String.valueOf(topicRawStr[4]).replaceFirst(":", ""));
            chanNode.setTopicTS(Long.valueOf(topicRawStr[3]));
            chanNode.setTopicBy(topicRawStr[2]);
        }
        else if (command[1].equals("TOPIC")) { /* only after syncing */
            //:ABC TOPIC #chan w!h@h ts :topic

            String[] topicRawStr = command[2].split(" ", 4);
            ChannelNode chanNode;
            
            chanNode = getChannelNodeByName(topicRawStr[0]);

            chanNode.setTopic(String.valueOf(topicRawStr[3]).replaceFirst(":", ""));
            chanNode.setTopicTS(Long.valueOf(topicRawStr[2]));
            chanNode.setTopicBy(topicRawStr[1]);

            try {
                cservice.handleTopic(chanNode);
            }
            catch (Exception e) { /* CServe not yet connected */ }
        }
        else {
            if (command[0].equals("PING")) {
                response = "PONG " + command[1];
                write(client, response);
            }
        }
    }
}
