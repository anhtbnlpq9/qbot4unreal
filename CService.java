
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.Instant;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class CService {
    
    String       myUniq;
    UserNode     myUserNode;
    
    Client       client;
    Protocol     protocol;
    SqliteDb     sqliteDb;
    Config       config;
    
    Boolean      cServiceReady = false;

    String       bufferMode = "";
    String       bufferParam = "";
    String       userChanlevFilter = "";
    UserNode     fromNick;
    UserAccount  userAccount;
    String       channel = "";
    ChannelNode  chanNode;

    static String chanJoinModes = "";

    final String CHANLEV_FLAGS = "abdjkmnopqtvw";
    final String CHANLEV_SYMBS = "+-";

    final Integer CHANLEV_FOUNDER_DEFAULT = Flags.getChanLFlagOwnerDefault();

    long unixTime;

    interface Whois {
        /**
         * Displays the whois of an user
         * @param whoisUserAccount user account
         */
        void displayW(UserAccount whoisUserAccount);
    }

    /**
     * @param client
     * @param protocol
     * @param sqliteDb
     */
    public CService(Client client, Protocol protocol, SqliteDb sqliteDb) {
        this.client = client;
        this.protocol = protocol;
        this.sqliteDb = sqliteDb;
    }
 
    public void runCService(Config config, Protocol protocol) {

        this.config = config;
        this.myUniq = config.getServerId()+config.getCServeUniq();

        unixTime = Instant.now().getEpochSecond();
        client.write(":" + config.getServerId() + " " + "UID " + config.getCServeNick() + " 1 " + unixTime + " " + config.getCServeIdent() + " " + config.getCServeHost() + " " + config.getServerId() + config.getCServeUniq() + " * " + config.getCServeModes() + " * * * :" + config.getCServeRealName());
        // UID nickname hopcount timestamp username hostname uid servicestamp usermodes virtualhost cloakedhost ip :gecos
        UserNode user = new UserNode(config.getCServeNick(), 
                                     config.getCServeIdent(), 
                                     config.getCServeHost(),
                                     config.getCServeHost(),
                                     config.getCServeRealName(),
                                     myUniq,
                                     unixTime,
                                     config.getCServeModes());

        this.myUserNode = user;

        user.setUserServer(protocol.getServerList().get(config.getServerId()));
        protocol.getUserList().put(myUniq, user);
        protocol.addNickLookupTable(config.getCServeNick(), myUniq);

        unixTime = Instant.now().getEpochSecond();

        String chanJoinModes = "";
        if (protocol.getFeature("chanOwner") == true) chanJoinModes += "q";
        else if (protocol.getFeature("chanAdmin") == true) chanJoinModes += "a";
        else if (protocol.getFeature("chanOp") == true) chanJoinModes += "o";
        else if (protocol.getFeature("chanHalfop") == true) chanJoinModes += "h";
        else if (protocol.getFeature("chanVoice") == true) chanJoinModes += "v";
        CService.chanJoinModes = chanJoinModes;

 
        var wrapper = new Object(){ String chanJoinModes; };
        wrapper.chanJoinModes = chanJoinModes;

        HashMap<String, ChannelNode> regChannels = protocol.getRegChanList();
        regChannels.forEach( (regChannelName, regChannelNode) -> {
            /* Making the bot join the registered channels */
            protocol.chanJoin(client, myUserNode, regChannelNode);
            try { protocol.setMode(client, regChannelNode, "+r" + wrapper.chanJoinModes, myUserNode.getUserNick()); }
            catch (Exception e) { e.printStackTrace(); }

            /* Look into every user account belonging to the channel chanlev and applying rights to authed logins of accounts */
            regChannelNode.getChanlev().forEach( (username, chanlev) -> {

                UserAccount useraccount;
                try { useraccount = protocol.getRegUserAccount(username); }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                useraccount.getUserLogins().forEach( (usernode) -> {
                    if (usernode.getUserChanList().containsKey(regChannelNode.getChanName())) {
                        this.handleJoin(usernode, regChannelNode);
                    }
                });
            });
        });

        cServiceReady = true;
        this.protocol = protocol;
        protocol.setCService(this);

        /* Starting thread for channel auto limit */
        ChanAutoLimit chanAutoLimit = new ChanAutoLimit(this, config);
        Thread chanAutoLimitThread = new Thread(chanAutoLimit);
        chanAutoLimitThread.start();

    }
    public void setClient(Client client) {
        this.client = client;
    }
    public Boolean isReady() {
        return this.cServiceReady;
    }

    public static String getChanJoinModes() {
        return chanJoinModes;
    }

    public void handleMessage(UserNode fromNickRaw, String str) {
        fromNick = fromNickRaw;

        if (str.toUpperCase().startsWith("HELP")) { 
            String[] helpCommandNameSplit = str.toUpperCase().split(" ", 3);
            String helpCommandName;

            if(helpCommandNameSplit[0].equals("HELP") == true) {
                try { helpCommandName = helpCommandNameSplit[1]; }
                catch (ArrayIndexOutOfBoundsException e) {  cServeShowcommands(fromNick); return; }
                cServeHelp(fromNick, helpCommandName);
            }
        }
        else if (str.toUpperCase().startsWith("SHOWCOMMANDS")) {
            cServeShowcommands(fromNick);
        }
        else if (str.equalsIgnoreCase("USERLIST")) {

            if (fromNick.getUserModes().matches("(.*)o(.*)") == false) {
                protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            protocol.sendNotice(client, myUserNode, fromNick, "List of users:");
            
            /*for (Map.Entry<A, B> e : myMap.entrySet()) {
                A key    = e.getKey();
                B value  = e.getValue();
            }*/

            for (Map.Entry<String, UserNode> user : protocol.getUserList().entrySet()) {
                protocol.sendNotice(client, myUserNode, fromNick, " * " + user.getValue().getUserUniq() + " " + user.getValue().getUserNick() + "!" + user.getValue().getUserIdent() + "@" + user.getValue().getUserHost() + " [" + user.getValue().getUserRealHost() + "] " + user.getValue().getUserModes() + " * " + user.getValue().getUserRealName());
            }
            protocol.sendNotice(client, myUserNode, fromNick, "There are " + protocol.getUserList().size() + " users on the network.");
            protocol.sendNotice(client, myUserNode, fromNick, "End of list.");
        }
        else if (str.equalsIgnoreCase("SERVERLIST")) {
            if (fromNick.getUserModes().matches("(.*)o(.*)") == false) {
                protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            protocol.sendNotice(client, myUserNode, fromNick, "List of servers:");
            
            /*for (Map.Entry<A, B> e : myMap.entrySet()) {
                A key    = e.getKey();
                B value  = e.getValue();
            }*/

            for (Map.Entry<String, ServerNode> server : protocol.getServerList().entrySet()) {
                String serverPeerStatus = "";
                if (server.getValue().getServerPeer()==true) { serverPeerStatus = "@";  }
                else { serverPeerStatus = "*"; }
                String serverEOS = "no";
                if (server.getValue().getServerEOS()==true) { serverEOS = "yes";  }
                String introducedBy = "(none)";
                if (server.getValue().getIntroducedBy() != null) { introducedBy = server.getValue().getIntroducedBy().getServerName(); }
                protocol.sendNotice(client, myUserNode, fromNick, serverPeerStatus + " " + server.getValue().getServerName() + " (" + server.getValue().getServerId() + ") /  EOS:" + serverEOS + " / introduced by: " + introducedBy);
            }
            protocol.sendNotice(client, myUserNode, fromNick, "There are " + protocol.getServerList().size() + " servers on the network.");
            protocol.sendNotice(client, myUserNode, fromNick, "End of list.");
        }
        else if (str.toUpperCase().startsWith("CHANLIST")) {

            if (fromNick.getUserModes().matches("(.*)o(.*)") == false) {
                protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }
            protocol.sendNotice(client, myUserNode, fromNick, "List of channels:");
            
            /*for (Map.Entry<A, B> e : myMap.entrySet()) {
                A key    = e.getKey();
                B value  = e.getValue();
            }*/
            String filterInput, filter;
            if ((str.split(" ", 2)).length > 1) {
                filterInput = ((str.split(" ", 2))[1]).replaceAll("[^A-Za-z0-9]", ""); 
            }
            else filterInput = "";
            filter = ".*" + filterInput + ".*"; 
            
            protocol.getChanList().forEach( (chan, node) -> {

                if (chan.matches("(?i)" + filter)) {
                    Date date = new Date((node.getChanTS())*1000L);
                    SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                    jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String chanTSdate = jdf.format(date);
                    bufferMode = "";
                    bufferParam = "";
                    node.getModes().forEach( (mode,param ) -> {
                        bufferMode = bufferMode + mode;
                        bufferParam = bufferParam + " " + param;
                    });

                    protocol.sendNotice(client, myUserNode, fromNick, " + " + chan + " (users: " + node.getChanUserCount() + ")");
                    protocol.sendNotice(client, myUserNode, fromNick, " |- modes: +" + bufferMode + " " + bufferParam );
                    protocol.sendNotice(client, myUserNode, fromNick, " |- created: " + chanTSdate );
                    protocol.sendNotice(client, myUserNode, fromNick, " |- ban list: " + node.getBanList().toString() );
                    protocol.sendNotice(client, myUserNode, fromNick, " |- except list: " + node.getExceptList().toString() );
                    protocol.sendNotice(client, myUserNode, fromNick, " `- invite list: " + node.getInviteList().toString() );
                }
            });
            protocol.sendNotice(client, myUserNode, fromNick, "There are " + protocol.getChanList().size() + " channels on the network.");
            protocol.sendNotice(client, myUserNode, fromNick, "End of list.");
        } 
        else if (str.toUpperCase().startsWith("WHOAMI")) {
            cServeWhois(fromNickRaw, fromNickRaw.getUserNick(), str);
        }
        else if (str.toUpperCase().startsWith("WHOIS ")) {
            String nick = (str.split(" ", 2))[1];
            cServeWhois(fromNickRaw, nick, str);
        }
        else if (str.toUpperCase().startsWith("WHOIS2 ")) {
            String nick = (str.split(" ", 2))[1];
            int foundNick=0;
            for (Map.Entry<String, UserNode> user : protocol.getUserList().entrySet()) {
                if ((user.getValue().getUserNick()).toLowerCase().equals(nick.toLowerCase())) {
                    foundNick=1;
                    
                    Date date = new Date((user.getValue().getUserTS())*1000L);
                    SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                    jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String userTSdate = jdf.format(date);

                    protocol.sendNotice(client, myUserNode, fromNick, " + " + user.getValue().getUserNick() + " (" + user.getValue().getUserUniq() + ") is " + user.getValue().getUserIdent() + "@" + user.getValue().getUserHost() + " * " + user.getValue().getUserRealName());

                    if (fromNick.isOper() == true || user.getValue().getUserNick().equals(fromNick.getUserNick()) ) {
                        protocol.sendNotice(client, myUserNode, fromNick, "|- is connecting from " + user.getValue().getUserRealHost());
                        protocol.sendNotice(client, myUserNode, fromNick, "|- is using modes " + user.getValue().getUserModes());
                        protocol.sendNotice(client, myUserNode, fromNick, "|- is using server " + (user.getValue().getUserServer()).getServerName() + " (" + (user.getValue().getUserServer()).getServerId() + ")");
                        protocol.sendNotice(client, myUserNode, fromNick, "|- signed on " + userTSdate );
                    }

                    if (user.getValue().getUserAuthed() == true) {
                        protocol.sendNotice(client, myUserNode, fromNick, "|- is authed as " + user.getValue().getUserAccount().getUserAccountName());
                    }

                    if (fromNick.isOper() == true || user.getValue().getUserNick().equals(fromNick.getUserNick()) ) {
                        protocol.sendNotice(client, myUserNode, fromNick, "|- on channels: ");
                        user.getValue().getUserChanModes().forEach( (key, value) -> {
                            bufferMode = "";

                            if (value.isEmpty() == false) { bufferMode = "(+" + value + ")"; }

                            protocol.sendNotice(client, myUserNode, fromNick, "| |- " + key + " " + bufferMode);

                        });
                    }

                    if (user.getValue().getUserAuthed() == true && ( user.getValue().getUserNick().equals(fromNick.getUserNick()) ) ) {
                        protocol.sendNotice(client, myUserNode, fromNick, "|- chanlev: ");

                        user.getValue().getUserAccount().getUserChanlev().forEach( (key, value) -> {
                            bufferMode = "";

                            if (Flags.flagsIntToChars("chanlev", value).isEmpty() == false) { bufferMode = "+" + Flags.flagsIntToChars("chanlev", value); }

                            protocol.sendNotice(client, myUserNode, fromNick, "| |- " + key + ": " + bufferMode);
                        });
                    }
                    protocol.sendNotice(client, myUserNode, fromNick, "End of List.");
                }
            }
            if (foundNick == 0) {
                protocol.sendNotice(client, myUserNode, fromNick, "No such nick.");
            }
        }
        else if (str.toUpperCase().matches("HELLO[ ]{0,1}.*")) { // HELLO <password> <email>
            String password;
            String email;
            
            String[] command = str.split(" ",4);
            if (fromNick.getUserAuthed() == true) { 
                protocol.sendNotice(client, myUserNode, fromNick, "You are already authed."); 
                return;                 
            }

            try { email = command[2]; }
            catch (ArrayIndexOutOfBoundsException e) { protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. Command is HELLO <password> <email>."); return; }
            try { password = command[1]; }
            catch (ArrayIndexOutOfBoundsException e) { protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. Command is HELLO <password> <email>."); return; }

            if (email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+")==false) {
                protocol.sendNotice(client, myUserNode, fromNick, "HELLO: Invalid email address.");
                return;
            }
            if (password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,64}$")==false) {
                protocol.sendNotice(client, myUserNode, fromNick, "HELLO: Password must contain at least 8 (at most 64) characters with at least one of the following types: lowercase, uppercase, number, symbol.");
                return;
            }
            String pwHash = null;
            String pwSalt = null;
            try { 
                SecureRandom random = new SecureRandom();
                byte[] salt = new byte[16];
                random.nextBytes(salt);
                KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
                SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] hash = f.generateSecret(spec).getEncoded();
                Base64.Encoder enc = Base64.getEncoder();

                pwHash = enc.encodeToString(hash);
                pwSalt = enc.encodeToString(salt);
            }
            catch (Exception e) { e.printStackTrace();}

            try {
                sqliteDb.addUser(fromNick.getUserNick(), email, pwHash, pwSalt, Instant.now().getEpochSecond(), Flags.getDefaultUserFlags()); 
                UserAccount newUserAccount = new UserAccount(sqliteDb, fromNick.getUserNick(), Flags.getDefaultUserFlags(), email, Instant.now().getEpochSecond());
                protocol.getRegUserList().put(fromNick.getUserNick(), newUserAccount);
            }
            catch (Exception e) { 
                protocol.sendNotice(client, myUserNode, fromNick, "An account with that name already exists."); 
                return;
            }
            protocol.sendNotice(client, myUserNode, fromNick, "Your account has been created with username \"" + fromNick.getUserNick() + "\" but you are not authed. You can now auth using AUTH " + fromNick.getUserNick() + " <password>");

        }
        else if (str.toUpperCase().startsWith("AUTH ")) { // AUTH <username> <password>
            String password;
            String username;

            HashMap<String, Integer> userChanlev;
            
            String[] command = str.split(" ",4);
            if (fromNick.getUserAuthed() == true) { 
                protocol.sendNotice(client, myUserNode, fromNick, "You are already authed."); 
                return;                 
            }
            try { password = command[2]; }
            catch (ArrayIndexOutOfBoundsException e) { 
                protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. Command is AUTH <username> <password>."); 
                return; 
            }

            try { username = command[1]; }
            catch (ArrayIndexOutOfBoundsException e) { 
                protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. Command is AUTH <username> <password>."); 
                return; 
            }

            String pwHash = null;

            Map<String, String> userToAuth;
            username = username.toLowerCase();
            try {
                userToAuth = sqliteDb.getUser(username);
                userChanlev = sqliteDb.getUserChanlev(username);
            }
            catch (Exception e) { 
                protocol.sendNotice(client, myUserNode, fromNick, "User account not found or incorrect password."); 
                return;
            }

            try { 
                Base64.Decoder dec = Base64.getDecoder();
                KeySpec spec = new PBEKeySpec(password.toCharArray(), dec.decode(userToAuth.get("salt")), 65536, 128);
                SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                byte[] hash = f.generateSecret(spec).getEncoded();
                Base64.Encoder enc = Base64.getEncoder();

                pwHash = enc.encodeToString(hash);
            }
            catch (Exception e) { e.printStackTrace(); }

            /* Delay auth to slow down brute force attack */
            try { Thread.sleep(3000); }
            catch (Exception e) { e.printStackTrace(); }

            if (userToAuth.get("password").equals(pwHash)) {

                UserAccount userAccount;
                try { userAccount = protocol.getRegUserAccount(userToAuth.get("name")); }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                if (Flags.isUserSuspended(userAccount.getUserAccountFlags()) == true) {
                    protocol.sendNotice(client, myUserNode, fromNick, "The user account " + userAccount.getUserAccountName() + " is suspended. Please contact a staff member for more information.");
                    return;
                }
                
                fromNick.setUserAuthed(true);
                fromNick.setUserAccount(userAccount);

                try { sqliteDb.addUserAuth(fromNick, Const.AUTH_TYPE_PLAIN);}
                catch (Exception e) {
                    e.printStackTrace();
                    protocol.sendNotice(client, myUserNode, fromNick, "Error finalizing the auth.");
                }
                
                fromNick.getUserAccount().setUserChanlev(userChanlev);

                if (Flags.isUserAutoVhost(fromNick.getUserAccount().getUserAccountFlags()) == true) {
                    protocol.chgHost(client, fromNick, fromNick.getUserAccount().getUserAccountName());
                }

                protocol.sendNotice(client, myUserNode, fromNick, "Auth successful."); 

                // Now we apply the modes of the user's chanlev as it was joining the channels
                // But no welcome message
                fromNick.getUserChanList().forEach( (chanName, chanObj) -> {
                    this.handleJoin(fromNick, chanObj, false);
                });

            }
            else { protocol.sendNotice(client, myUserNode, fromNick, "User account not found or incorrect password."); }
        }
        else if (str.toUpperCase().startsWith("LOGOUT")) { // LOGOUT
            if (fromNick.getUserAuthed() == false) {
                protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }
            else {
                try { sqliteDb.delUserAuth(fromNick, Const.DEAUTH_TYPE_MANUAL, ""); }
                catch (Exception e) {
                    e.printStackTrace();
                    protocol.sendNotice(client, myUserNode, fromNick, "Error finalizing unauth."); 
                }
                fromNick.setUserAccount(null);
                fromNick.setUserAuthed(false);
                protocol.sendNotice(client, myUserNode, fromNick, "Logout successful.");
            }         
        }
        else if (str.toUpperCase().startsWith("VERSION")) {
            protocol.sendNotice(client, myUserNode, fromNick, "qbot4u - The Q Bot for UnrealIRCd."); 
        }
        else if (str.toUpperCase().startsWith("REQUESTBOT ")) { // REQUESTBOT #channel
            String channel = (str.split(" ", 2))[1];
            ChannelNode chanNode = protocol.getChannelNodeByName(channel);

            if (fromNick.getUserAuthed() == false) {
                protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            UserAccount ownerAccount = fromNick.getUserAccount();

            // First check that the user is on the channel and opped
            if (fromNick.getUserChanMode(channel).matches("(.*)o(.*)")) {
                try {
                    sqliteDb.addRegChan(chanNode, ownerAccount);
                    
                    sqliteDb.setChanFlags(chanNode, Flags.getDefaultChanFlags());


                    sqliteDb.setUserChanlev(ownerAccount, chanNode, CHANLEV_FOUNDER_DEFAULT);

                    ownerAccount.setUserChanlev(chanNode, CHANLEV_FOUNDER_DEFAULT);

                    // updating channel chanlev as well
                    Map<String, Integer> chanNewChanlev = sqliteDb.getChanChanlev(chanNode);
                    chanNode.setChanChanlev(chanNewChanlev);
                    chanNode.setChanFlags(Flags.getDefaultChanFlags());
                    
                    protocol.chanJoin(client, myUserNode, chanNode);
                    protocol.setMode(client, chanNode, "+r" + chanJoinModes, myUserNode.getUserNick());
                    protocol.sendNotice(client, myUserNode, fromNick, "Channel successfully registered."); 
                }
                catch (Exception e) { 
                    protocol.sendNotice(client, myUserNode, fromNick, "Error while registering the channel."); 
                    e.printStackTrace();
                    return;
                }
            }
            else {
                protocol.sendNotice(client, myUserNode, fromNick, "You must be present on the channel and be opped."); 
            }
            
        }
        else if (str.toUpperCase().startsWith("DROP ")) { /* DROP #channel */
            String channel = (str.split(" ", 2))[1];
            ChannelNode chanNode = protocol.getChannelNodeByName(channel);

            if (fromNick.getUserAuthed() == false) {
                protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            // First check that the user is a the channel's owner (chanlev +n)
            try {
                if ( Flags.hasChanLOwnerPriv(fromNick.getUserAccount().getUserChanlev(chanNode)) == true ) {

                    fromNick.getUserAccount().clearUserChanlev(chanNode);
                    chanNode.clearChanChanlev(fromNick);
                    sqliteDb.clearChanChanlev(channel);

                    sqliteDb.delRegChan(channel);

                    protocol.setMode(client, chanNode, "-r", "");
                    protocol.chanPart(client, myUserNode, chanNode);
                    protocol.sendNotice(client, myUserNode, fromNick, "Channel successfully dropped."); 
                }
                else {
                    protocol.sendNotice(client, myUserNode, fromNick, "You must have the flag +n in the channel's chanlev to be able to drop it."); 
                }
            }
            catch (Exception e) { 
                protocol.sendNotice(client, myUserNode, fromNick, "You must have the flag +n in the channel's chanlev to be able to drop it."); 
                e.printStackTrace();
                return;
            }
        }
        else if (str.toUpperCase().startsWith("CHANLEV ")) { /* CHANLEV <channel> [user [change]] */
            cServeChanlev(fromNickRaw, str);
        }
        else if (str.toUpperCase().startsWith("USERFLAGS")) { /* USERFLAGS [flags] */
            cServeUserflags(fromNickRaw, str);
        }
        else if (str.toUpperCase().startsWith("CHANFLAGS")) { /* CHANFLAGS [flags] */
            cServeChanflags(fromNickRaw, str);
        }
        else if (str.toUpperCase().startsWith("AUTOLIMIT")) { /* CHANFLAGS [flags] */
            cServeAutoLimit(fromNickRaw, str);
        }
        else if (str.toUpperCase().startsWith("AUTHHISTORY")) { /* AUTHHISTORY */
            cServeAuthHistory(fromNickRaw, str);
        }
        else if (str.toUpperCase().startsWith("WELCOME")) { /* WELCOME <chan> [msg] */
            cServeWelcome(fromNickRaw, str);
        }
        else if (str.toUpperCase().startsWith("SETTOPIC")) { /* SETTOPIC <chan> [topic] */
            cServeSetTopic(fromNickRaw, str);
        }
        else if (str.toUpperCase().startsWith("CLEARTOPIC")) { /* CLEARTOPIC <chan> */
            cServeSetTopic(fromNickRaw, str);
        }
        else if (str.toUpperCase().startsWith("REJOIN")) { /* REJOIN <chan> */
            cServeRejoin(fromNickRaw, str);
        }


        else { // Unknown command
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command \"" + str.split(" ",2)[0] + "\". Type SHOWCOMMANDS for a list of available commands.");
        }
    }

    /**
     * Triggers the activities to perform when an user joins a channel
     * @param user user node joining channel
     * @param channel channel node joined
     */
    public void handleJoin(UserNode user, ChannelNode channel, Boolean dispWelcome) {
        //System.out.println("BBA chanjoin");
        // check if user is authed
        if (user.getUserAuthed() == true) {
            if (user.getUserAccount().getUserChanlev().containsKey(channel.getChanName())) {
                if (  Flags.isChanLBanned( user.getUserAccount().getUserChanlev(channel)) == true ) {
                    //System.out.println("BBC chanlev ban");
                    try {
                        protocol.setMode(client, myUniq, channel.getChanName(), "+b", "*!*" + user.getUserIdent() + "@" + user.getUserHost());
                        protocol.chanKick(client, myUserNode, channel, user, "You are BANNED from this channel.");
                    }
                    catch (Exception e) { e.printStackTrace(); }
                }

                else if (   Flags.isChanLAuto( user.getUserAccount().getUserChanlev(channel))  ) { /* Sets the auto channel modes */

                    if (  Flags.isChanLOwner( user.getUserAccount().getUserChanlev(channel)) && protocol.getFeature("chanOwner") == true) {
                        try {
                            protocol.setMode(client, myUniq, channel.getChanName(), "+q", user.getUserNick());
                        }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                    else if (  Flags.isChanLMaster( user.getUserAccount().getUserChanlev(channel)) && protocol.getFeature("chanAdmin") == true) {
                        try {
                            protocol.setMode(client, myUniq, channel.getChanName(), "+a", user.getUserNick());
                        }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                    else if (  Flags.isChanLOp( user.getUserAccount().getUserChanlev(channel)) && protocol.getFeature("chanOp") == true) {
                        //System.out.println("BBD chanlev op");
                        try {
                            protocol.setMode(client, myUniq, channel.getChanName(), "+o", user.getUserNick());
                        }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                    else if (  Flags.isChanLHalfOp( user.getUserAccount().getUserChanlev(channel)) && protocol.getFeature("chanHalfop") == true) {
                        try {
                            protocol.setMode(client, myUniq, channel.getChanName(), "+h", user.getUserNick());
                        }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                    else if (  Flags.isChanLVoice( user.getUserAccount().getUserChanlev(channel)) && protocol.getFeature("chanVoice") == true ) {
                        //System.out.println("BBE chanlev voice");
                        try {
                            protocol.setMode(client, myUniq, channel.getChanName(), "+v", user.getUserNick());
                        }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }
        }

        if (Flags.isChanWelcome(channel.getChanFlags()) == true && dispWelcome == true) {
            if (user.getUserAuthed() == false || ( user.getUserAuthed() == true && Flags.isUserWelcome(user.getUserAccount().getUserAccountFlags()) == false && Flags.isChanLHideWelcome(user.getUserAccount().getUserChanlev(channel)) == false) ) {
                String welcomeMsg = "";
                try { welcomeMsg = sqliteDb.getWelcomeMsg(channel); }
                catch (Exception e) { }
                if (welcomeMsg == null) { welcomeMsg = ""; }

                if (welcomeMsg.isEmpty() == false) {
                    protocol.sendNotice(client, myUserNode, fromNick, welcomeMsg);
                }
            }
        }
    }


    public void handleJoin(UserNode user, ChannelNode channel) {
        handleJoin(user, channel, true); 
    }

    public void handleTopic(ChannelNode chanNode) {
        String savedTopic = "";
        try {
            savedTopic = sqliteDb.getTopic(chanNode);
        }

        catch (Exception e) { }

        if (Flags.isChanForceTopic(chanNode.getChanFlags()) == true) protocol.setTopic(client, myUserNode, chanNode, savedTopic);
    }

    /**
     * 
     * @param fromNick requester user node
     * @param nick requested nick or account
     * @param str command string
     */
    public void cServeWhois(UserNode fromNick, String nick, String str) {
        Whois whois = (whoisUserAccount) -> {

            String spaceFill = " ";

            var wrapper = new Object(){ String buffer = ""; String buffer2 = ""; };
            whoisUserAccount.getUserLogins().forEach( (userNode) -> {
                wrapper.buffer += userNode.getUserNick() + " ";
            });
            if (wrapper.buffer.isEmpty() == true) { wrapper.buffer = "(none)"; }

            if ( Flags.hasUserOperPriv(whoisUserAccount.getUserAccountFlags()) == true) {
                protocol.sendNotice(client, myUserNode, fromNick, config.getNetworkName() + " Staff     : IRC Operator");
            }

            else if ( Flags.hasUserStaffPriv(whoisUserAccount.getUserAccountFlags()) == true) {
                protocol.sendNotice(client, myUserNode, fromNick, config.getNetworkName() + " Staff     : Staff Member");
            }

            if ( (Flags.hasUserStaffPriv(fromNick.getUserAccount().getUserAccountFlags()) == true) || (fromNick.getUserAccount() == whoisUserAccount) ) {
                protocol.sendNotice(client, myUserNode, fromNick, "User ID        : " + whoisUserAccount.getUserAccountId());

                if (whoisUserAccount.getUserAccountFlags() != 0) { wrapper.buffer2 = "+" + Flags.flagsIntToChars("userflags", whoisUserAccount.getUserAccountFlags()); }
                else wrapper.buffer2 = "(none)";

                protocol.sendNotice(client, myUserNode, fromNick, "User flags     : " + wrapper.buffer2);
            }
            protocol.sendNotice(client, myUserNode, fromNick, "Account users  : " + wrapper.buffer);

            if ( (Flags.hasUserStaffPriv(fromNick.getUserAccount().getUserAccountFlags()) == true) || (fromNick.getUserAccount() == whoisUserAccount) ) {
                SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                jdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date date = new Date((whoisUserAccount.getRegTS())*1000L);
                String accountCreationTS = jdf.format(date);
                protocol.sendNotice(client, myUserNode, fromNick, "User created   : " + accountCreationTS);
                //protocol.sendNotice(client, myUserNode, fromNick, "Last auth      : ");
                protocol.sendNotice(client, myUserNode, fromNick, "Email address  : " + whoisUserAccount.getUserAccountEmail() );
                //protocol.sendNotice(client, myUserNode, fromNick, "Email last set : ");
                //protocol.sendNotice(client, myUserNode, fromNick, "Pass last set  : ");
                protocol.sendNotice(client, myUserNode, fromNick, "Known on the following channels:");
                protocol.sendNotice(client, myUserNode, fromNick, "Channel                        Flags:");

                whoisUserAccount.getUserChanlev().forEach( (chan, chanlev) -> {
                    protocol.sendNotice(client, myUserNode, fromNick, " " + chan + spaceFill.repeat(30-chan.length()) +"+" + Flags.flagsIntToChars("chanlev", chanlev));
                } );

            }
        };

        if (fromNick.getUserAuthed() == false) {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

        if (nick.startsWith("#")) { // lookup user in database

            if (protocol.getUserAccount(nick.replaceFirst("#","")) != null) {
                UserAccount userAccount = protocol.getUserAccount(nick.replaceFirst("#",""));

                protocol.sendNotice(client, myUserNode, fromNick, "-Information for account " + userAccount.getUserAccountName() + ":");
                whois.displayW(userAccount);
                protocol.sendNotice(client, myUserNode, fromNick, "End of list.");
            }
            else { protocol.sendNotice(client, myUserNode, fromNick, "Can't find user " +  nick + "."); }

        }
        else {
            int foundNick=0;
            for (Map.Entry<String, UserNode> user : protocol.getUserList().entrySet()) {
                if ((user.getValue().getUserNick()).toLowerCase().equals(nick.toLowerCase())) {
                    foundNick=1;
                    UserNode foundUser = user.getValue();

                    if (foundUser.getUserAuthed() == true) {

                        protocol.sendNotice(client, myUserNode, fromNick, "-Information for user " + foundUser.getUserNick() + " (using account " + foundUser.getUserAccount().getUserAccountName() + "):");
                        whois.displayW(foundUser.getUserAccount());
                        protocol.sendNotice(client, myUserNode, fromNick, "End of list.");
                    }
                    else { protocol.sendNotice(client, myUserNode, fromNick, "The user " + nick + " is not authed."); }
                }
            }
            if (foundNick == 0) { protocol.sendNotice(client, myUserNode, fromNick, "Can't find user " + nick + ".");  }
        }
    }

    /**
     * Handles the setting of chanlev
     * @param fromNick requester user node
     * @param str command string
     */
    public void cServeChanlev(UserNode fromNick, String str) {
        String[] command = str.split(" ",5);
        String userNick = "";
        String chanlevMod = "";
        HashMap<String, String> chanlevModStr = new HashMap<String, String>(); 
        HashMap<String, Integer> chanlevModInt = new HashMap<String, Integer>(); 
        String userAccountStr = "";
        userChanlevFilter = "";
        String spaceFill = " ";

        if (fromNick.getUserAuthed() == false) {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

        try { channel = command[1]; }
        catch (ArrayIndexOutOfBoundsException e) { 
            protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. CHANLEV <channel> [<user> [<change>]]."); 
            return; 
        }
        try {
            chanNode = protocol.getChannelNodeByName(channel);
        }
        catch (Exception e) {
            protocol.sendNotice(client, myUserNode, fromNick, "This channel does not exist on the network and is not registered."); 
            return;
        }
        
        try {  userNick = command[2]; }
        catch (ArrayIndexOutOfBoundsException e) {  }

        try {  chanlevMod =  command[3]; }
        catch (ArrayIndexOutOfBoundsException e) {  }
        chanlevModStr = Flags.parseFlags(chanlevMod);
        chanlevModInt.put("+", Flags.flagsCharsToInt("chanlev", chanlevModStr.get("+")));
        chanlevModInt.put("-", Flags.flagsCharsToInt("chanlev", chanlevModStr.get("-")));



        if (userNick.startsWith("#")) { // direct access to account
            userAccountStr = userNick.replaceFirst("#", "").toLowerCase();
            try { userAccount = protocol.getRegUserAccount(userAccountStr); }
            catch (Exception e) {
                e.printStackTrace();
                protocol.sendNotice(client, myUserNode, fromNick, "No such user account.");
                return;
            }
        }
        else if (userNick.isEmpty() == true) { // no nick/account provided => only display chanlev
        }
        else { // indirect access to account => need to lookup account name
            try {
                if (protocol.getUserNodeByNick(userNick).getUserAuthed() == true)  {
                    userAccount = protocol.getUserNodeByNick(userNick).getUserAccount();
                    //System.out.println("BBX 1=" + userNick + " 2=" + userAccount);
                }
                else {
                    protocol.sendNotice(client, myUserNode, fromNick, "That nickname is not authed.");
                    return; 
                }
            }
            catch (NullPointerException e) { 
                e.printStackTrace();
                protocol.sendNotice(client, myUserNode, fromNick, "No such nick.");
                return;
            }
        }

        if ( userAccountStr.isEmpty() == false) {
            userChanlevFilter = userAccountStr;
        }

        if (chanlevMod.isEmpty() == true) { // no chanlev => list
            //System.out.println("BCB no chanlev => list userChanlevFilter=" +userChanlevFilter);

            try {
                if (  Flags.hasChanLSignificant(fromNick.getUserAccount().getUserChanlev(chanNode)) == true || Flags.hasUserStaffPriv(fromNick.getUserAccount().getUserAccountFlags()) == true  ) {
                    protocol.sendNotice(client, myUserNode, fromNick, "Displaying CHANLEV for channel " + chanNode.getChanName() + ":"); 
                    protocol.sendNotice(client, myUserNode, fromNick, "Account             Chanlev");
                    chanNode.getChanlev().forEach( (user, chanlev) -> {
                        //System.out.println("BCD userChanlevFilter=" +userChanlevFilter+ " user=" + user + " chanlev=" + chanlev);
                        if ( (userChanlevFilter.isEmpty() == false && user.toLowerCase().equals(userChanlevFilter.toLowerCase())) || userChanlevFilter.isEmpty() == true) {
                            protocol.sendNotice(client, myUserNode, fromNick, " " + user + spaceFill.repeat(19-user.length()) + "+" + Flags.flagsIntToChars("chanlev", chanlev)); 
                        }
                    });
                    protocol.sendNotice(client, myUserNode, fromNick, "End of list."); 
                }
                else {
                    protocol.sendNotice(client, myUserNode, fromNick, "You do not have sufficient access on " + channel + " to use chanlev."); 
                    return;
                }
            }
            catch (Exception e) { 
                e.printStackTrace();
                protocol.sendNotice(client, myUserNode, fromNick, "You do not have sufficient access on " + channel + " to use chanlev."); 
                return;
            }
        }
        else { 
            if (    chanlevMod.matches("^(?=.*["+ CHANLEV_FLAGS +"])(?=.*["+ CHANLEV_SYMBS +"]).+$")    ) {
                chanlevModInt.put("combined", chanlevModInt.get("+") | chanlevModInt.get("-"));

                if (   (  (Flags.hasChanLMasterPriv(fromNick.getUserAccount().getUserChanlev(chanNode)) == true) && ( Flags.containsChanLMasterConFlags(chanlevModInt.get("combined"))  ) == true) ||
                (  (Flags.hasChanLOwnerPriv(fromNick.getUserAccount().getUserChanlev(chanNode)) == true) && ( Flags.containsChanLOwnerConFlags(chanlevModInt.get("combined"))  ) == true)  || Flags.hasUserOperPriv(fromNick.getUserAccount().getUserAccountFlags()) == true ) {

                    // user wants to modify chanlev by account name directly
                    // in this case, we need to update the db + check if the account is online and update that nick chanlev
                    try {

                        Integer userCurChanlev = sqliteDb.getUserChanlev(userAccount, chanNode);
                        Integer userNewChanlev = Flags.applyFlagsFromStr("chanlev", userCurChanlev, chanlevModStr);
                        
                        //protocol.sendNotice(client, myUserNode, fromNick, "BCA current chanlev " + channel + " -> " + Flags.flagsIntToChars("chanlev", userCurChanlev) + " :: mod=" + chanlevMod + " :: result=" + Flags.flagsIntToChars("chanlev", userNewChanlev));
                        //System.out.println("BCA current chanlev " + channel + " -> " + userCurChanlev + " :: mod=" + chanlevMod + " :: result=" + userNewChanlev);

                        sqliteDb.setUserChanlev(userAccount, chanNode, userNewChanlev);
                        userAccount.setUserChanlev(chanNode, userNewChanlev);

                        chanNode.setChanChanlev(sqliteDb.getChanChanlev(chanNode));

                        protocol.sendNotice(client, myUserNode, fromNick, "Done.");
                        protocol.sendNotice(client, myUserNode, fromNick, "Chanlev set. Chanlev for user account " + userAccount.getUserAccountName() + " is now +" + Flags.flagsIntToChars("chanlev", userNewChanlev) + ".");

                        userAccount.getUserLogins().forEach( (usernode) -> {
                            if (usernode.getUserChanList().containsKey(chanNode.getChanName())) {
                                this.handleJoin(usernode, chanNode);
                            }
                        });
                    }
                    catch (Exception e) {
                        e.printStackTrace(); 
                        protocol.sendNotice(client, myUserNode, fromNick, "Error setting chanlev."); 
                        return; 
                    }

                    if (chanNode.getChanlev() == null || chanNode.getChanlev().isEmpty() == true) {
                        try {
                            fromNick.getUserAccount().clearUserChanlev(chanNode);
                            sqliteDb.clearChanChanlev(channel);
                            sqliteDb.delRegChan(channel);
                            protocol.setMode(client, chanNode, "-r", "");
                            protocol.chanPart(client, myUserNode, chanNode);
                            protocol.sendNotice(client, myUserNode, fromNick, "Channel has been dropped because its chanlev was left empty."); 
                        }
                        catch (Exception e) { return; }
                    }
                }
                else {
                    protocol.sendNotice(client, myUserNode, fromNick, "You do not have sufficient rights on " + channel + " to set chanlev with those flags.");
                    return;
                }
            }
            else {
                protocol.sendNotice(client, myUserNode, fromNick, "Invalid chanlev flags. Valid flags are: <+|->" + CHANLEV_FLAGS);
            } 
        }


    }

    /**
     * Handles the setting of userflags
     * @param fromNick requester user node
     * @param str command string
     */
    public void cServeUserflags(UserNode fromNick, String str) {
        String[] command = str.split(" ",5);
        String flagsModRaw = "";

        HashMap<String, String> flagsModStr = new HashMap<String, String>(); 
        HashMap<String, Integer> flagsModInt = new HashMap<String, Integer>(); 

        if (fromNick.getUserAuthed() == false) {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

        try { flagsModRaw = command[1]; }
        catch (ArrayIndexOutOfBoundsException e) { 
            if (fromNick.getUserAccount().getUserAccountFlags() == 0) { 
                protocol.sendNotice(client, myUserNode, fromNick, "User flags for " + fromNick.getUserAccount().getUserAccountName() + ": (none)"); 
            }
            else protocol.sendNotice(client, myUserNode, fromNick, "User flags for " + fromNick.getUserAccount().getUserAccountName() + ": +" + Flags.flagsIntToChars("userflags", fromNick.getUserAccount().getUserAccountFlags()));
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

        /* Stripping only admin control flags if the user has admin privileges */
        if (Flags.hasUserAdminPriv(fromNick.getUserAccount().getUserAccountFlags()) == true) {
            flagsModInt.replace("+", Flags.stripUserAdminConFlags(flagsModInt.get("+")));
            flagsModInt.replace("-", Flags.stripUserAdminConFlags(flagsModInt.get("-")));
            flagsModInt.replace("combined", (flagsModInt.get("+") | flagsModInt.get("-")));

        }

        /* Stripping only admin control flags if the user has oper privileges */
        else if (Flags.hasUserOperPriv(fromNick.getUserAccount().getUserAccountFlags()) == true) {
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
            protocol.sendNotice(client, myUserNode, fromNick, "You may have specified an invalid flags combination. Consult HELP USERFLAGS for valid flags."); 
            return; 
        }

        try {
            Integer userCurFlags = fromNick.getUserAccount().getUserAccountFlags();
            Integer userNewFlags = Flags.applyFlagsFromInt("userflags", userCurFlags, flagsModInt);

            sqliteDb.setUserFlags(fromNick.getUserAccount(), userNewFlags);
            fromNick.getUserAccount().setUserAccountFlags(userNewFlags);

            String userNewFlagsStr = "";
            if (userNewFlags != 0) { userNewFlagsStr = "+" + Flags.flagsIntToChars("userflags", userNewFlags); }
            else { userNewFlagsStr = "(none)"; }

            protocol.sendNotice(client, myUserNode, fromNick, "Done.");
            protocol.sendNotice(client, myUserNode, fromNick, "User flags for " + fromNick.getUserAccount().getUserAccountName() + " : " + userNewFlagsStr + ".");
        }
        catch (Exception e) {
            e.printStackTrace(); 
            protocol.sendNotice(client, myUserNode, fromNick, "Error setting userflags."); 
            return; 
        }
    }

    /**
     * Handles the setting of chanflags
     * @param fromNick requester user node
     * @param str command string
     */
    public void cServeChanflags(UserNode fromNick, String str) {
        String[] command = str.split(" ",5);
        String chanFlagsModRaw = "";
        Integer chanNewFlagsInt = 0;
        Integer chanCurFlagsInt = 0;

        HashMap<String, String> chanFlagsModSepStr;
        HashMap<String, Integer> chanFlagsModSepInt = new HashMap<>();

        if (fromNick.getUserAuthed() == false) {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

        try { channel = command[1]; }
        catch (ArrayIndexOutOfBoundsException e) { 
            protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. CHANFLAGS <channel> [flags]."); 
            return; 
        }

        try { chanNode = protocol.getChannelNodeByName(channel); }
        catch (Exception e) {
            protocol.sendNotice(client, myUserNode, fromNick, "Can't find this channel."); 
            return;
        }

        try {  chanFlagsModRaw =  command[2]; }
        catch (ArrayIndexOutOfBoundsException e) {
            Integer applicableChFlagsInt = 0;
            String applicableChFlagsStr = "";

            if ( Flags.hasUserStaffPriv(fromNick.getUserAccount().getUserAccountFlags()) == true ) {
                applicableChFlagsInt = chanNode.getChanFlags();
            }
            else { applicableChFlagsInt = Flags.stripChanNonPublicFlags(chanNode.getChanFlags()); } 

            if (applicableChFlagsInt > 0) {
                applicableChFlagsStr = "+" + Flags.flagsIntToChars("chanflags", applicableChFlagsInt);
            }
            else { applicableChFlagsStr = "(none)"; }

            protocol.sendNotice(client, myUserNode, fromNick, "Channel flags for " + chanNode.getChanName() + ": " + applicableChFlagsStr); 
            return;
        }

        chanFlagsModSepStr = Flags.parseFlags(chanFlagsModRaw);
        chanFlagsModSepInt.put("+", Flags.flagsCharsToInt("chanflags", chanFlagsModSepStr.get("+")));
        chanFlagsModSepInt.put("-", Flags.flagsCharsToInt("chanflags", chanFlagsModSepStr.get("-")));
        chanFlagsModSepInt.put("combined", 0);

        /* Stripping the unknown and readonly flags */
        chanFlagsModSepInt.replace("+", Flags.stripUnknownChanFlags(chanFlagsModSepInt.get("+")));
        chanFlagsModSepInt.replace("-", Flags.stripUnknownChanFlags(chanFlagsModSepInt.get("-")));

        /* Keeping admin editable flags if the user is admin */
        if (Flags.hasUserAdminPriv(fromNick.getUserAccount().getUserAccountFlags()) == true) {
            chanFlagsModSepInt.replace("+", Flags.keepChanAdminConFlags(chanFlagsModSepInt.get("+")));
            chanFlagsModSepInt.replace("-", Flags.keepChanAdminConFlags(chanFlagsModSepInt.get("-")));
        }
        /* Keeping oper editable flags if the user is oper */
        else if (Flags.hasUserOperPriv(fromNick.getUserAccount().getUserAccountFlags()) == true) {
            chanFlagsModSepInt.replace("+", Flags.keepChanOperConFlags(chanFlagsModSepInt.get("+")));
            chanFlagsModSepInt.replace("-", Flags.keepChanOperConFlags(chanFlagsModSepInt.get("-")));
        }

        /* Keeping chanowner editable flags if the user is owner of the than */
        else if (Flags.hasChanLOwnerPriv(fromNick.getUserAccount().getUserChanlev().get(chanNode.getChanName())) == true) {
            chanFlagsModSepInt.replace("+", Flags.keepChanOwnerConFlags(chanFlagsModSepInt.get("+")));
            chanFlagsModSepInt.replace("-", Flags.keepChanOwnerConFlags(chanFlagsModSepInt.get("-")));
        }
        /* Keeping chanmaster editable flags if the user is master of the than */
        else if (Flags.hasChanLMasterPriv(fromNick.getUserAccount().getUserChanlev().get(chanNode.getChanName())) == true) {
            chanFlagsModSepInt.replace("+", Flags.keepChanMasterConFlags(chanFlagsModSepInt.get("+")));
            chanFlagsModSepInt.replace("-", Flags.keepChanMasterConFlags(chanFlagsModSepInt.get("-")));
        }
        /* User has no rights on the chan */
        else {
            protocol.sendNotice(client, myUserNode, fromNick, "You do not have sufficient access on " + chanNode.getChanName() + " to use chanflags."); 
            return;
        }

        chanFlagsModSepInt.replace("combined", chanFlagsModSepInt.get("+") | chanFlagsModSepInt.get("-"));

        if (chanFlagsModSepInt.get("combined") == 0) {
            protocol.sendNotice(client, myUserNode, fromNick, "Nothing changed. Your requested flag combination change was either the same as the existing flags, impossible, or you don't have enough access."); 
            return; 
        }

        try {

            chanCurFlagsInt = chanNode.getChanFlags();
            chanNewFlagsInt = Flags.applyFlagsFromInt("chanflags", chanCurFlagsInt, chanFlagsModSepInt);

            sqliteDb.setChanFlags(chanNode, chanNewFlagsInt);
            chanNode.setChanFlags(chanNewFlagsInt);

            String chanNewFlagsStr = "";
            if (chanNewFlagsInt > 0) { chanNewFlagsStr = "+" + Flags.flagsIntToChars("chanflags", chanNode.getChanFlags()); }
            else { chanNewFlagsStr = "(none)"; }

            protocol.sendNotice(client, myUserNode, fromNick, "Done.");
            protocol.sendNotice(client, myUserNode, fromNick, " - New chan flags for " + chanNode.getChanName() + " : " + chanNewFlagsStr + ".");
        }

        catch (Exception e) {
            e.printStackTrace(); 
            protocol.sendNotice(client, myUserNode, fromNick, "Error setting chanflags."); 
            return; 
        }

    }

    public void cServeAuthHistory(UserNode fromNick, String str) {
        String[] command = str.split(" ",5);
        String target = "";
        ArrayList<HashMap<String, Object>> authHistList; 
        UserAccount userAccount;
        String authType;
        String deAuthType;
        SimpleDateFormat jdf = new SimpleDateFormat("dd/MM/yy HH:mm z");
        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strFiller = " ";


        if (fromNick.getUserAuthed() == false) {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

        
        try {
            target = command[1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            target = "";
        }

        if (target.isEmpty() == false) {
            if ( Flags.hasUserStaffPriv(fromNick.getUserAccount().getUserAccountFlags()) == true ) {
                /* STAFF PARAMETER */
                /* target begins with # => lookup account */
                /* target does not => lookup nick then account */
                if (target.startsWith("#") == true) {
                    userAccount = protocol.getUserAccount(target.replaceFirst("#", ""));
                    if (userAccount == null) {
                        protocol.sendNotice(client, myUserNode, fromNick, "This account does not exist."); 
                        return;
                    }
                }
                else {
                    try { userAccount = protocol.getUserNodeByNick(target).getUserAccount(); }
                    catch (NullPointerException e) { protocol.sendNotice(client, myUserNode, fromNick, "This nick does not exist."); return; }
                }

                try { authHistList = sqliteDb.getAuthHistory(userAccount); }
                catch (Exception e) { authHistList = new ArrayList<>(); }



            }
            else {
                protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }
        }
        else {
            userAccount = fromNick.getUserAccount();
            try { authHistList = sqliteDb.getAuthHistory(userAccount); }
            catch (Exception e) { authHistList = new ArrayList<>(); }
        }
        protocol.sendNotice(client, myUserNode, fromNick, "#:  User:                                             Authed:                         Disconnected:       Reason:");
        int i=1;
        for(HashMap<String, Object> authLine : authHistList) {
            Date dateAuthTS = new Date( (Long) authLine.get("authTS")*1000L);
            Date dateDeAuthTS = new Date( (Long) authLine.get("deAuthTS")*1000L);
            authType = Const.getAuthTypeString((Integer) authLine.get("authType"));
            Object deAuthResult;
            if ((Long) authLine.get("deAuthTS") == 0L) { deAuthResult = "(never)"; }
            else deAuthResult = jdf.format(dateDeAuthTS);

            String quitResult = (authLine.get("deAuthReason")) == null ? "(none)" : (String)authLine.get("deAuthReason");
           //deAuthType = Const.getDeAuthTypeString(authLine.get("authType"));

            protocol.sendNotice(client, myUserNode, fromNick, 
              "#" + String.valueOf(i) + strFiller.repeat(3 - String.valueOf(i).length()) 
              + authLine.get("maskFrom") + strFiller.repeat(50 - String.valueOf(authLine.get("maskFrom")).length())
              + jdf.format(dateAuthTS) + " (" + authType + ")" + strFiller.repeat(42 - String.valueOf(dateAuthTS).length() - String.valueOf(" (" + authType + ")").length())
              + String.valueOf(deAuthResult) + strFiller.repeat(20 - (String.valueOf(deAuthResult).length())) + quitResult); 
            i++;
        }
        protocol.sendNotice(client, myUserNode, fromNick, "End of list."); 
        


    }

    public void cServeDrop() {

    }

    public void cServeRequestbot() {

    }

    public void cServeHello() {

    }

    public void cServeAuth() {

    }

    public void cServeChanlist() {

    }

    public void cServeUserlist () {

    }

    public void cServeServerlist() {

    }

    public void cServeLogout() {

    }

    public void cServeRejoin(UserNode userNode, String str) {
        String[] command = str.split(" ",5);
        if (fromNick.getUserAuthed() == false) {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

        if (Flags.hasUserOperPriv(userNode.getUserAccount().getUserAccountFlags()) == true) {

            try { channel = command[1]; }
            catch (ArrayIndexOutOfBoundsException e) { 
                protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. CHANFLAGS <channel> [flags]."); 
                return; 
            }
    
            try { chanNode = protocol.getChannelNodeByName(channel); }
            catch (Exception e) {
                protocol.sendNotice(client, myUserNode, fromNick, "Can't find this channel."); 
                return;
            }
            protocol.chanPart(client, myUserNode, chanNode);
            protocol.chanJoin(client, myUserNode, chanNode);
            try {
                protocol.setMode(client, chanNode, "+r" + chanJoinModes, myUserNode.getUserNick());
            }
            catch (Exception e) { e.printStackTrace(); System.out.println("* Could not set mode for "+ chanNode.getChanName() + " after REJOIN command"); return; }
            protocol.sendNotice(client, myUserNode, fromNick, "Done."); 
        }
        else {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

    }

    public void cServeWelcome(UserNode fromNick, String str) {
        String[] command = str.split(" ",3);
        String newWelcomeMsg;

        if (fromNick.getUserAuthed() == false) {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

        try { channel = command[1]; }
        catch (ArrayIndexOutOfBoundsException e) { 
            protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. WELCOME <channel> [message]."); 
            return; 
        }

        try { chanNode = protocol.getChannelNodeByName(channel); }
        catch (Exception e) {
            protocol.sendNotice(client, myUserNode, fromNick, "Can't find this channel."); 
            return;
        }

        try { newWelcomeMsg = command[2]; }
        catch (ArrayIndexOutOfBoundsException e) { 
            String curWelcomeMsg = "";
            try {
                curWelcomeMsg = sqliteDb.getWelcomeMsg(chanNode);
                if (curWelcomeMsg.isEmpty() == true) { curWelcomeMsg = "(none)"; }
            }
            catch (Exception f) { }
            if (Flags.hasChanLSignificant(fromNick.getUserAccount().getUserChanlev(chanNode)) == true || Flags.hasUserOperPriv(fromNick.getUserAccount().getUserAccountFlags()) == true) {
                protocol.sendNotice(client, myUserNode, fromNick, "Welcome message for " + chanNode.getChanName() + ": " + curWelcomeMsg); 
            }
            else protocol.sendNotice(client, myUserNode, fromNick, "You do not have sufficient access on " + chanNode.getChanName() + " to use welcome."); 
            return; 
        }

        if (Flags.hasChanLMasterPriv(fromNick.getUserAccount().getUserChanlev(chanNode)) == true || Flags.hasUserOperPriv(fromNick.getUserAccount().getUserAccountFlags()) == true ) {

            try {
                sqliteDb.setWelcomeMsg(chanNode, newWelcomeMsg);
                protocol.sendNotice(client, myUserNode, fromNick, "Done."); 
                protocol.sendNotice(client, myUserNode, fromNick, "Welcome message for " + chanNode.getChanName() + ": " + newWelcomeMsg); 
            }
            catch (Exception e) {
                protocol.sendNotice(client, myUserNode, fromNick, "Error setting welcome for " + chanNode.getChanName() + "."); 
                return;
            }
        }
        else {
            protocol.sendNotice(client, myUserNode, fromNick, "You do not have sufficient access on " + chanNode.getChanName() + " to use welcome."); 
        }
    }

    public void cServeSetTopic(UserNode fromNick, String str) {
        String[] command = str.split(" ",3);
        String newTopic;

        if (fromNick.getUserAuthed() == false) {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

        try { channel = command[1]; }
        catch (ArrayIndexOutOfBoundsException e) { 
            protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. SETTOPIC <channel> [topic]."); 
            return; 
        }

        try { chanNode = protocol.getChannelNodeByName(channel); }
        catch (Exception e) {
            protocol.sendNotice(client, myUserNode, fromNick, "Can't find this channel."); 
            return;
        }


        try { newTopic = command[2]; }
        catch (ArrayIndexOutOfBoundsException e) { 
                newTopic = chanNode.getTopic();
        }

        if (Flags.hasChanLMasterPriv(fromNick.getUserAccount().getUserChanlev(chanNode)) == true || Flags.hasUserOperPriv(fromNick.getUserAccount().getUserAccountFlags()) == true || Flags.isChanLTopic(fromNick.getUserAccount().getUserChanlev(chanNode)) == true ) {

            try {
                if (newTopic == null) { chanNode.getTopic();}
                sqliteDb.setTopic(chanNode, newTopic);
                if (newTopic.equals(chanNode.getTopic()) == false)  protocol.setTopic(client, myUserNode, chanNode, newTopic);
                protocol.sendNotice(client, myUserNode, fromNick, "Done."); 
            }
            catch (Exception e) {
                protocol.sendNotice(client, myUserNode, fromNick, "Error setting topic for " + chanNode.getChanName() + "."); 
                return;
            }

        }
        else {
            protocol.sendNotice(client, myUserNode, fromNick, "You do not have sufficient access on " + chanNode.getChanName() + " to use topic."); 
        }
    }

    public void cServeClearTopic(UserNode fromNick, String str) {
        String[] command = str.split(" ",3);
        String newTopic = "";

        if (fromNick.getUserAuthed() == false) {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

        try { channel = command[1]; }
        catch (ArrayIndexOutOfBoundsException e) { 
            protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. SETTOPIC <channel> [topic]."); 
            return; 
        }

        try { chanNode = protocol.getChannelNodeByName(channel); }
        catch (Exception e) {
            protocol.sendNotice(client, myUserNode, fromNick, "Can't find this channel."); 
            return;
        }

        if (Flags.hasChanLMasterPriv(fromNick.getUserAccount().getUserChanlev(chanNode)) == true || Flags.hasUserOperPriv(fromNick.getUserAccount().getUserAccountFlags()) == true || Flags.isChanLTopic(fromNick.getUserAccount().getUserChanlev(chanNode)) == true ) {
            try {
                sqliteDb.setTopic(chanNode, newTopic);
                if (newTopic.equals(chanNode.getTopic()) == false)  protocol.setTopic(client, myUserNode, chanNode, newTopic);
                protocol.sendNotice(client, myUserNode, fromNick, "Done."); 
            }
            catch (Exception e) {
                protocol.sendNotice(client, myUserNode, fromNick, "Error setting topic for " + chanNode.getChanName() + "."); 
                return;
            }
        }
        else {
            protocol.sendNotice(client, myUserNode, fromNick, "You do not have sufficient access on " + chanNode.getChanName() + " to use topic."); 
        }
    }

    /**
     * Handles the help
     * @param fromNick requester user node
     * @param commandName command string
     */
    public void cServeHelp(UserNode fromNick, String commandName) {
        Help.getHelp("commands", commandName).forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} );
    }


    public void cServeShowcommands(UserNode fromNick) {
        /*
         * List of contexts
         * ================
         * - 000 = unauth user
         * - 050 = authed user
         * - 100 = staff member
         * - 150 = oper member
         * - 200 = admin member
         * - 900 = developper member
         * - 950 = debug member
         * 
         */
        Help.getHelp("levels", "COMMANDS_LIST").forEach( (line) -> { 
            String context = "";
            String content = "";
            try {
                context = line.split("!", 2)[0];
                content = line.split("!", 2)[1];
            }
            catch (Exception e) {
                content = line;
            }


            switch (context) {
                case "000":
                    if (fromNick.getUserAuthed() == false) {
                        protocol.sendNotice(client, myUserNode, fromNick, content);
                    }
                    break;

                case "050":
                    if (fromNick.getUserAuthed() == true) {
                        protocol.sendNotice(client, myUserNode, fromNick, content);
                    }
                    break;

                case "100":
                    if (fromNick.getUserAuthed() == true && Flags.hasUserStaffPriv(fromNick.getUserAccount().getUserAccountFlags())) {
                        protocol.sendNotice(client, myUserNode, fromNick, content);
                    }
                    break;


                case "150":
                    if (fromNick.getUserAuthed() == true && Flags.hasUserOperPriv(fromNick.getUserAccount().getUserAccountFlags())) {
                        protocol.sendNotice(client, myUserNode, fromNick, content);
                    }
                    break;

                case "200":
                    if (fromNick.getUserAuthed() == true && Flags.hasUserAdminPriv(fromNick.getUserAccount().getUserAccountFlags())) {
                        protocol.sendNotice(client, myUserNode, fromNick, content);
                    }
                    break;

                case "900":

                    break;

                case "950":

                    break;

                default:
                    protocol.sendNotice(client, myUserNode, fromNick, content);
                    break;
                
            }

        } );
    }

    public void cServeVersion() {

    }

    /**
     * Sets the channel limit based on the channel autolimit feature
     */
    public void cServeSetAutolimit() {

        //ChannelNode chanNode;
        protocol.getRegChanList().forEach( (chanName, chanNode) -> {

            Integer curChanUserCount = chanNode.getChanUserCount();
            Integer curChanModeLimit;
            Integer chanAutoLimit = chanNode.getChanAutoLimit();
            Integer newLimit = (chanAutoLimit + curChanUserCount);

            try {
                curChanModeLimit = Integer.valueOf(chanNode.getMode("l"));
            }
            catch (Exception e) {
                curChanModeLimit = 0;
            }

            if ((Flags.isChanAutolimit(chanNode.getChanFlags()) == true) && newLimit != curChanModeLimit) {
                try {
                    protocol.setMode(client, myUserNode, chanNode, "+l", String.valueOf(newLimit));
                    System.out.println("* Autolimit: set limit of " + chanName + " to " + String.valueOf(newLimit));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * Handles the setting of channel autolimit
     * @param fromNick requester user node
     * @param str command string
     */
    public void cServeAutoLimit(UserNode fromNick, String str) {
        String[] command = str.split(" ",5);
        Integer chanAutoLimitInt = 0;

        if (fromNick.getUserAuthed() == false) {
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
            return;
        }

        try { channel = command[1]; }
        catch (ArrayIndexOutOfBoundsException e) { 
            protocol.sendNotice(client, myUserNode, fromNick, "Invalid command. AUTOLIMIT <channel> [limit]]."); 
            return; 
        }

        try { chanNode = protocol.getChannelNodeByName(channel); }
        catch (Exception e) {
            protocol.sendNotice(client, myUserNode, fromNick, "Channel " + channel + " is unknown or suspended."); 
            return;
        }

        try {  chanAutoLimitInt =  Integer.valueOf(command[2]); }
        catch (ArrayIndexOutOfBoundsException e) {
            Integer chanCurAutoLimit = 0;

            if ( Flags.hasUserStaffPriv(fromNick.getUserAccount().getUserAccountFlags()) == true || Flags.hasChanLOpPriv(fromNick.getUserAccount().getUserChanlev(chanNode)) == true ) {
                chanCurAutoLimit = chanNode.getChanAutoLimit();
                protocol.sendNotice(client, myUserNode, fromNick, "Current autolimit setting on " + chanNode.getChanName() + ": " + chanCurAutoLimit); 
            }
            else { protocol.sendNotice(client, myUserNode, fromNick, "You do not have sufficient access on " + chanNode.getChanName() + " to use autolimit."); } 
            return;

        }
        if (Flags.hasUserOperPriv(fromNick.getUserAccount().getUserAccountFlags()) == true || Flags.hasChanLMasterPriv(fromNick.getUserAccount().getUserChanlev(chanNode)) == true) {

            try {
                sqliteDb.setChanAutoLimit(chanNode, chanAutoLimitInt);
                chanNode.setAutoLimit(chanAutoLimitInt);

                protocol.sendNotice(client, myUserNode, fromNick, "Done.");
                protocol.sendNotice(client, myUserNode, fromNick, " - Autolimit for " + chanNode.getChanName() + " : " + chanAutoLimitInt + ".");

            }
            catch (Exception e) {
                e.printStackTrace(); 
                protocol.sendNotice(client, myUserNode, fromNick, "Error setting autolimit."); 
                return; 
            }

        }
        /* User has no rights on the chan */
        else {
            protocol.sendNotice(client, myUserNode, fromNick, "You do not have sufficient access on " + chanNode.getChanName() + " to use autolimit.");
            return;
        }
    }
}