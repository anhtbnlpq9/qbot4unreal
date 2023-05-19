
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
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

        HashMap<String, ChannelNode> regChannels = protocol.getRegChanList();
        regChannels.forEach( (regChannelName, regChannelNode) -> {
            protocol.chanJoin(client, myUserNode, regChannelNode);
            try { protocol.setMode(client, regChannelNode, "+o", myUserNode.getUserNick()); }
            catch (Exception e) { e.printStackTrace(); }
        });

        cServiceReady = true;
        this.protocol = protocol;
        protocol.setCService(this);
    }
    public void setClient(Client client) {
        this.client = client;
    }
    public Boolean isReady() {
        return this.cServiceReady;
    }
    public void handleMessage(UserNode fromNickRaw, String str) {
        fromNick = fromNickRaw;


        if (str.toUpperCase().startsWith("HELP ALL")) {                    Help.getHelp("commands", "ALL").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP AUTH")) {              Help.getHelp("commands", "AUTH").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP AUTHHISTORY")) {       Help.getHelp("commands", "AUTHHISTORY").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP CHANFLAGS")) {         Help.getHelp("commands", "CHANFLAGS").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP CHANLEV")) {           Help.getHelp("commands", "CHANLEV").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP DROP")) {              Help.getHelp("commands", "DROP").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP HELLO")) {             Help.getHelp("commands", "HELLO").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP LOGOUT")) {            Help.getHelp("commands", "LOGOUT").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP REQUESTBOT")) {        Help.getHelp("commands", "HELLO").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP SHOWCOMMANDS")) {      Help.getHelp("commands", "SHOWCOMMANDS").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP USERFLAGS")) {         Help.getHelp("commands", "USERFLAGS").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP VERSION")) {           Help.getHelp("commands", "VERSION").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP WHOIS")) {             Help.getHelp("commands", "WHOIS").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("SHOWCOMMANDS")) {
            if (fromNick.getUserAuthed() == false) {  Help.getHelp("levels", "0-UNAUTHED_USER").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
            else if (fromNick.getUserAuthed() == true) { Help.getHelp("levels", "10-AUTHED_USER").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
            else if (fromNick.getUserModes().matches("(.*)o(.*)") == true) {  Help.getHelp("levels", "20-OPER").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }

            if (fromNick.getUserModes().matches("(.*)o(.*)") == true) {  Help.getHelp("levels", "20-OPER").forEach( (line) -> { protocol.sendNotice(client, myUserNode, fromNick, line);} ); }
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
                String serverEOS = "no";
                if (server.getValue().getServerEOS()==true) { serverEOS = "yes";  }
                protocol.sendNotice(client, myUserNode, fromNick, " * " + serverPeerStatus + server.getValue().getServerName() + " (" + server.getValue().getServerId() + ") /  EOS:" + serverEOS);
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
        else if (str.toUpperCase().startsWith("WHOIS ")) {
            String nick = (str.split(" ", 2))[1];

            Whois whois = (whoisUserAccount) -> {

                String spaceFill = " ";

                var wrapper = new Object(){ String buffer = ""; };
                whoisUserAccount.getUserLogins().forEach( (userNode) -> {
                    wrapper.buffer += userNode.getUserNick() + " ";
                });
                if (wrapper.buffer.isEmpty() == true) { wrapper.buffer = "(none)"; }

                protocol.sendNotice(client, myUserNode, fromNick, "User ID        : " + whoisUserAccount.getUserAccountId());
                if ( (Flags.hasUserStaffPriv(fromNick.getUserAccount().getUserAccountFlags()) == true) || (fromNick.getUserAccount() == whoisUserAccount) ) {
                  protocol.sendNotice(client, myUserNode, fromNick, "User flags     : " + whoisUserAccount.getUserAccountFlags());
                }
                protocol.sendNotice(client, myUserNode, fromNick, "Account users  : " + wrapper.buffer);
                if ( (Flags.hasUserStaffPriv(fromNick.getUserAccount().getUserAccountFlags()) == true) || (fromNick.getUserAccount() == whoisUserAccount) ) {
                    //protocol.sendNotice(client, myUserNode, fromNick, "User created   : ");
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
                        else { protocol.sendNotice(client, myUserNode, fromNick, "The user " + nick + "is not authed."); }
                    }
                }
                if (foundNick == 0) { protocol.sendNotice(client, myUserNode, fromNick, "Can't find user " + nick + ".");  }
            }
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
                sqliteDb.addUser(fromNick.getUserNick(), email, pwHash, pwSalt); 
            }
            catch (Exception e) { 
                protocol.sendNotice(client, myUserNode, fromNick, "An account with that name already exists."); 
                return;
            }

            protocol.sendNotice(client, myUserNode, fromNick, "Your account has been created with username \"" + fromNick.getUserNick() + "\". You can now auth using AUTH " + fromNick.getUserNick() + " <password>");

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

            if (userToAuth.get("password").equals(pwHash)) {

                UserAccount userAccount = protocol.getRegUserAccount(userToAuth.get("name"));
                
                fromNick.setUserAuthed(true);
                fromNick.setUserAccount(userAccount);

                try { sqliteDb.addUserAuth(userAccount.getUserAccountId(), fromNick.getUserUniq(), fromNick.getUserTS());}
                catch (Exception e) {
                    e.printStackTrace();
                    protocol.sendNotice(client, myUserNode, fromNick, "Error finalizing the auth.");
                }
                
                fromNick.getUserAccount().setUserChanlev(userChanlev);

                if (Flags.isUserNoAutoVhost(fromNick.getUserAccount().getUserAccountFlags()) == false) {
                    protocol.chgHost(client, fromNick, fromNick.getUserAccount().getUserAccountName());
                }

                protocol.sendNotice(client, myUserNode, fromNick, "Auth successful."); 

                // Now we apply the modes of the user's chanlev as it was joining the channels
                fromNick.getUserChanList().forEach( (chanName, chanObj) -> {
                    this.handleJoin(fromNick, chanObj);
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
                try { sqliteDb.delUserAuth(fromNick.getUserUniq()); }
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

                    sqliteDb.setUserChanlev(ownerAccount, chanNode, CHANLEV_FOUNDER_DEFAULT);

                    ownerAccount.setUserChanlev(chanNode, CHANLEV_FOUNDER_DEFAULT);

                    // updating channel chanlev as well
                    Map<String, Integer> chanNewChanlev = sqliteDb.getChanChanlev(chanNode);
                    chanNode.setChanChanlev(chanNewChanlev);
                    
                    protocol.chanJoin(client, myUserNode, chanNode);
                    protocol.setMode(client, chanNode, "+ro", myUserNode.getUserNick());
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
        else if (str.toUpperCase().startsWith("CHANLEV ")) { // CHANLEV <channel> [<user> [<change>]]

            String[] command = str.split(" ",5);
            String userNick = "";
            String chanlevMod = "";
            HashMap<String, String> chanlevModStr = new HashMap<String, String>(); 
            HashMap<String, Integer> chanlevModInt = new HashMap<String, Integer>(); 
            String userAccountStr = "";
            userChanlevFilter = "";

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
                    if (  Flags.hasChanLSignificant(fromNick.getUserAccount().getUserChanlev(chanNode))  ) {
                        protocol.sendNotice(client, myUserNode, fromNick, "Displaying CHANLEV for channel " + chanNode.getChanName() + ":"); 
                        chanNode.getChanlev().forEach( (user, chanlev) -> {
                            //System.out.println("BCD userChanlevFilter=" +userChanlevFilter+ " user=" + user + " chanlev=" + chanlev);
                            if ( (userChanlevFilter.isEmpty() == false && user.toLowerCase().equals(userChanlevFilter.toLowerCase())) || userChanlevFilter.isEmpty() == true) {
                                protocol.sendNotice(client, myUserNode, fromNick, user + "     +" + Flags.flagsIntToChars("chanlev", chanlev)); 
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
                    if (   (  Flags.hasChanLMasterPriv(fromNick.getUserAccount().getUserChanlev(chanNode)) && ( Flags.containsChanLMasterConFlags(chanlevModInt.get("+")) || Flags.containsChanLMasterConFlags(chanlevModInt.get("-")) )) ||
                    (  Flags.hasChanLOwnerPriv(fromNick.getUserAccount().getUserChanlev(chanNode)) && ( Flags.containsChanLOwnerConFlags(chanlevModInt.get("+")) || Flags.containsChanLOwnerConFlags(chanlevModInt.get("-")) ))  ) {

                        // user wants to modify chanlev by account name directly
                        // in this case, we need to update the db + check if the account is online and update that nick chanlev
                        try {
                            // getting account chanlev from db to apply it to every usernode logged with account
                            //XXX to change next 3 lines

                            //System.out.println("BEA ");
                            Integer userCurChanlev = sqliteDb.getUserChanlev(userAccount, chanNode);
                            Integer userNewChanlev = Flags.applyFlagsFromStr("chanlev", userCurChanlev, chanlevModStr);
                            //String userNewChanlev = UserNode.parseChanlev(userCurChanlev, chanlevMod);
                            

                            //protocol.sendNotice(client, myUserNode, fromNick, "BCA current chanlev " + channel + " -> " + Flags.flagsIntToChars("chanlev", userCurChanlev) + " :: mod=" + chanlevMod + " :: result=" + Flags.flagsIntToChars("chanlev", userNewChanlev));
                            //System.out.println("BCA current chanlev " + channel + " -> " + userCurChanlev + " :: mod=" + chanlevMod + " :: result=" + userNewChanlev);

                            sqliteDb.setUserChanlev(userAccount, chanNode, userNewChanlev);
                            userAccount.setUserChanlev(chanNode, userNewChanlev);

                            chanNode.setChanChanlev(sqliteDb.getChanChanlev(chanNode));

                            protocol.sendNotice(client, myUserNode, fromNick, "Done.");
                            protocol.sendNotice(client, myUserNode, fromNick, "Chanlev set. Chanlev for user account " + userAccount + " is now +" + Flags.flagsIntToChars("chanlev", userNewChanlev) + ".");
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
        else if (str.toUpperCase().startsWith("USERFLAGS")) { // USERFLAGS [flags]

            String[] command = str.split(" ",5);
            String userNick = "";
            Integer flagsMod = 0;
            String flagsModRaw = "";

            HashMap<String, String> flagsModStr = new HashMap<String, String>(); 
            HashMap<String, Integer> flagsModInt = new HashMap<String, Integer>(); 

            userChanlevFilter = "";

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

        else { // Unknown command
            protocol.sendNotice(client, myUserNode, fromNick, "Unknown command \"" + str + "\". Type SHOWCOMMANDS for a list of available commands.");
        }
        //return "";
    }
    public void handleJoin(UserNode user, ChannelNode channel) {
        //System.out.println("BBA chanjoin");
        // check if user is authed
        if (user.getUserAuthed() == true) {
            //System.out.println("BBB user authed");
            // check user chanlev
            // +av => +v
            // +ao* => +o
            if (user.getUserAccount().getUserChanlev().containsKey(channel.getChanName())) {
                if (  Flags.isChanLBanned( user.getUserAccount().getUserChanlev(channel)) == true ) {
                    //System.out.println("BBC chanlev ban");
                    try {
                        protocol.setMode(client, myUniq, channel.getChanName(), "+b", "*!*" + user.getUserIdent() + "@" + user.getUserHost());
                        protocol.chanKick(client, myUserNode, channel, user, "You are BANNED from this channel.");
                    }
                    catch (Exception e) { e.printStackTrace(); }
                }
                else if (   Flags.isChanLAuto( user.getUserAccount().getUserChanlev(channel))  ) {
                    if (  Flags.isChanLOp( user.getUserAccount().getUserChanlev(channel)) ) {
                        //System.out.println("BBD chanlev op");
                        try {
                            protocol.setMode(client, myUniq, channel.getChanName(), "+o", user.getUserNick());
                        }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                    else if (  Flags.isChanLVoice( user.getUserAccount().getUserChanlev(channel))  ) {
                        //System.out.println("BBE chanlev voice");
                        try {
                            protocol.setMode(client, myUniq, channel.getChanName(), "+v", user.getUserNick());
                        }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                }
            }
        }
    }

}