
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
    
    Map<String, ServerNode> serverList;

    /**
     * {@link String} userSid -> {@link UserNode} user
     */
    Map<String, UserNode> userList;
    Map<String, ChannelNode> channelList;
    //Map<String, String> userChanlev;
    
    String myUniq;
    
    Client client;
    Protocol protocol;
    SqliteDb sqliteDb;
    Config config;
    
    Boolean cServiceReady = false;

    String bufferMode = "";
    String bufferParam = "";
    String userChanlevFilter = "";
    String fromNick;
    String userAccount= "";
    String channel = "";

    final String CHANLEV_FLAGS = "abdjkmnopqtvw";
    final String CHANLEV_SYMBS = "+-";

    final Integer CHANLEV_FOUNDER_DEFAULT = (Flags.getChanLFlagInt("a") | Flags.getChanLFlagInt("n") | Flags.getChanLFlagInt("o")); // +ano

    long unixTime;
    
    public CService() {
        
    }
    /**
     * @param client
     */
    public CService(Client client) {
        this.client = client;
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
    /**
     * @param client
     * @param protocol
     * @param userList
     */
    public CService(Client client, Protocol protocol, Map<String, UserNode> userList) {
        this.client = client;
        this.protocol = protocol;
        this.userList = userList;
    }         
 
    public void runCService(Config config, Protocol protocol, Map<String, UserNode> userList, Map<String, ServerNode> serverList, Map<String, ChannelNode> channelList) {
        this.userList = userList;
        this.serverList = serverList;
        this.channelList = channelList;
        this.config = config;

        myUniq = config.getServerId()+config.getCServeUniq();
        
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

        user.setUserServer(serverList.get(config.getServerId()));
        userList.put(myUniq, user);
        protocol.addNickLookupTable(config.getCServeNick(), myUniq);


        unixTime = Instant.now().getEpochSecond();

        for (String regChannel: sqliteDb.getRegChan()) {
            protocol.chanJoin(client, myUniq, regChannel);
            try { protocol.setMode(client, regChannel, "+o", config.getCServeNick()); }
            catch (Exception e) { e.printStackTrace(); }
        }  
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
    public void handleMessage(String fromNickRaw, String str) {
        fromNick = fromNickRaw;
        

        if (str.toUpperCase().startsWith("HELP ALL")) {                    Help.getHelp("commands", "ALL").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP AUTH")) {              Help.getHelp("commands", "AUTH").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP AUTHHISTORY")) {       Help.getHelp("commands", "AUTHHISTORY").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP CHANFLAGS")) {         Help.getHelp("commands", "CHANFLAGS").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP CHANLEV")) {           Help.getHelp("commands", "CHANLEV").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP DROP")) {              Help.getHelp("commands", "DROP").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP HELLO")) {             Help.getHelp("commands", "HELLO").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP LOGOUT")) {            Help.getHelp("commands", "LOGOUT").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP REQUESTBOT")) {        Help.getHelp("commands", "HELLO").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP SHOWCOMMANDS")) {      Help.getHelp("commands", "SHOWCOMMANDS").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP VERSION")) {           Help.getHelp("commands", "VERSION").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("HELP WHOIS")) {             Help.getHelp("commands", "WHOIS").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        else if (str.toUpperCase().startsWith("SHOWCOMMANDS")) {
            if (userList.get(fromNick).getUserAuthed() == false) {  Help.getHelp("levels", "0-UNAUTHED_USER").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
            else if (userList.get(fromNick).getUserAuthed() == true) { Help.getHelp("levels", "10-AUTHED_USER").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
            else if (userList.get(fromNick).getUserModes().matches("(.*)o(.*)") == true) {  Help.getHelp("levels", "20-OPER").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }

            if (userList.get(fromNick).getUserModes().matches("(.*)o(.*)") == true) {  Help.getHelp("levels", "20-OPER").forEach( (line) -> { protocol.sendNotice(client, myUniq, fromNick, line);} ); }
        }

        else if (str.equalsIgnoreCase("USERLIST")) {

            if (userList.get(fromNick).getUserModes().matches("(.*)o(.*)") == false) {
                protocol.sendNotice(client, myUniq, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            protocol.sendNotice(client, myUniq, fromNick, "List of users:");
            
            /*for (Map.Entry<A, B> e : myMap.entrySet()) {
                A key    = e.getKey();
                B value  = e.getValue();
            }*/

            for (Map.Entry<String, UserNode> user : userList.entrySet()) {
                protocol.sendNotice(client, myUniq, fromNick, " * " + user.getValue().getUserUniq() + " " + user.getValue().getUserNick() + "!" + user.getValue().getUserIdent() + "@" + user.getValue().getUserHost() + " [" + user.getValue().getUserRealHost() + "] " + user.getValue().getUserModes() + " * " + user.getValue().getUserRealName());
            }
            protocol.sendNotice(client, myUniq, fromNick, "There are " + userList.size() + " users on the network.");
            protocol.sendNotice(client, myUniq, fromNick, "End of list.");
        }
        else if (str.equalsIgnoreCase("SERVERLIST")) {
            if (userList.get(fromNick).getUserModes().matches("(.*)o(.*)") == false) {
                protocol.sendNotice(client, myUniq, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            protocol.sendNotice(client, myUniq, fromNick, "List of servers:");
            
            /*for (Map.Entry<A, B> e : myMap.entrySet()) {
                A key    = e.getKey();
                B value  = e.getValue();
            }*/

            for (Map.Entry<String, ServerNode> server : serverList.entrySet()) {
                String serverPeerStatus = "";
                if (server.getValue().getServerPeer()==true) { serverPeerStatus = "@";  }
                String serverEOS = "no";
                if (server.getValue().getServerEOS()==true) { serverEOS = "yes";  }
                protocol.sendNotice(client, myUniq, fromNick, " * " + serverPeerStatus + server.getValue().getServerName() + " (" + server.getValue().getServerId() + ") /  EOS:" + serverEOS);
            }
            protocol.sendNotice(client, myUniq, fromNick, "There are " + serverList.size() + " servers on the network.");
            protocol.sendNotice(client, myUniq, fromNick, "End of list.");
        }
        else if (str.toUpperCase().startsWith("CHANLIST")) {

            if (userList.get(fromNick).getUserModes().matches("(.*)o(.*)") == false) {
                protocol.sendNotice(client, myUniq, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }
            protocol.sendNotice(client, myUniq, fromNick, "List of channels:");
            
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

                    protocol.sendNotice(client, myUniq, fromNick, " + " + chan + " (users: " + node.getChanUserCount() + ")");
                    protocol.sendNotice(client, myUniq, fromNick, " |- modes: +" + bufferMode + " " + bufferParam );
                    protocol.sendNotice(client, myUniq, fromNick, " |- created: " + chanTSdate );
                    protocol.sendNotice(client, myUniq, fromNick, " |- ban list: " + node.getBanList().toString() );
                    protocol.sendNotice(client, myUniq, fromNick, " |- except list: " + node.getExceptList().toString() );
                    protocol.sendNotice(client, myUniq, fromNick, " `- invite list: " + node.getInviteList().toString() );
                }
            });
            protocol.sendNotice(client, myUniq, fromNick, "There are " + channelList.size() + " channels on the network.");
            protocol.sendNotice(client, myUniq, fromNick, "End of list.");
        } 
        else if (str.toUpperCase().startsWith("WHOIS ")) {
            String nick = (str.split(" ", 2))[1];

            if (nick.startsWith("#")) { // lookup user in database
                protocol.sendNotice(client, myUniq, fromNick, "User lookup");
            }
            else {
                int foundNick=0;
                for (Map.Entry<String, UserNode> user : userList.entrySet()) {
                    if ((user.getValue().getUserNick()).toLowerCase().equals(nick.toLowerCase())) {
                        foundNick=1;
                        
                        Date date = new Date((user.getValue().getUserTS())*1000L);
                        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String userTSdate = jdf.format(date);

                        protocol.sendNotice(client, myUniq, fromNick, " + " + user.getValue().getUserNick() + " (" + user.getValue().getUserUniq() + ") is " + user.getValue().getUserIdent() + "@" + user.getValue().getUserHost() + " * " + user.getValue().getUserRealName());

                        if (userList.get(fromNick).isOper() == true || user.getValue().getUserNick().equals(userList.get(fromNick).getUserNick()) ) {
                            protocol.sendNotice(client, myUniq, fromNick, "|- is connecting from " + user.getValue().getUserRealHost());
                            protocol.sendNotice(client, myUniq, fromNick, "|- is using modes " + user.getValue().getUserModes());
                            protocol.sendNotice(client, myUniq, fromNick, "|- is using server " + (user.getValue().getUserServer()).getServerName() + " (" + (user.getValue().getUserServer()).getServerId() + ")");
                            protocol.sendNotice(client, myUniq, fromNick, "|- signed on " + userTSdate );
                        }

                        if (user.getValue().getUserAuthed() == true) {
                            protocol.sendNotice(client, myUniq, fromNick, "|- is authed as " + user.getValue().getUserAccount());
                        }

                        //bufferMode = "";
                        if (userList.get(fromNick).isOper() == true || user.getValue().getUserNick().equals(userList.get(fromNick).getUserNick()) ) {
                            protocol.sendNotice(client, myUniq, fromNick, "|- on channels: ");
                            user.getValue().getUserChanModes().forEach( (key, value) -> {
                                bufferMode = "";
                                if (value.isEmpty() == false) { bufferMode = "(+" + value + ")"; }
                                protocol.sendNotice(client, myUniq, fromNick, "| |- " + key + " " + bufferMode);
                            });
                        }

                        if (user.getValue().getUserAuthed() == true && ( user.getValue().getUserNick().equals(userList.get(fromNick).getUserNick()) ) ) {
                            protocol.sendNotice(client, myUniq, fromNick, "|- chanlev: ");

                            user.getValue().getUserChanlev().forEach( (key, value) -> {
                                bufferMode = "";
                                if (Flags.flagsIntToChars("chanlev", value).isEmpty() == false) { bufferMode = "+" + Flags.flagsIntToChars("chanlev", value); }
                                protocol.sendNotice(client, myUniq, fromNick, "| |- " + key + ": " + bufferMode);
                            });
                        }
                    }
                }
                if (foundNick == 0) {
                    protocol.sendNotice(client, myUniq, fromNick, "No such nick.");
                }
                else {
                    protocol.sendNotice(client, myUniq, fromNick, "End of IRCWHOIS.");
                }
            }
        }
        else if (str.toUpperCase().matches("HELLO[ ]{0,1}.*")) { // HELLO <password> <email>
            String password;
            String email;
            String accountName;
            
            String[] command = str.split(" ",4);
            if (userList.get(fromNick).getUserAuthed() == true) { 
                protocol.sendNotice(client, myUniq, fromNick, "You are already authed."); 
                return;                 
            }

            try { email = command[2]; }
            catch (ArrayIndexOutOfBoundsException e) { protocol.sendNotice(client, myUniq, fromNick, "Invalid command. Command is HELLO <password> <email>."); return; }
            try { password = command[1]; }
            catch (ArrayIndexOutOfBoundsException e) { protocol.sendNotice(client, myUniq, fromNick, "Invalid command. Command is HELLO <password> <email>."); return; }

            if (email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+")==false) {
                protocol.sendNotice(client, myUniq, fromNick, "HELLO: Invalid email address.");
                return;
            }
            if (password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,64}$")==false) {
                protocol.sendNotice(client, myUniq, fromNick, "HELLO: Password must contain at least 8 (at most 64) characters with at least one of the following types: lowercase, uppercase, number, symbol.");
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
                sqliteDb.addUser(userList.get(fromNick).getUserNick(), email, pwHash, pwSalt); 
            }
            catch (Exception e) { 
                protocol.sendNotice(client, myUniq, fromNick, "An account with that name already exists."); 
                return;
            }

            protocol.sendNotice(client, myUniq, fromNick, "Your account has been created with username \"" + userList.get(fromNick).getUserNick() + "\". You can now auth using AUTH " + userList.get(fromNick).getUserNick() + " <password>");

        }
        else if (str.toUpperCase().startsWith("AUTH ")) { // AUTH <username> <password>
            String password;
            String username;

            Map<String, Integer> userChanlev;
            
            String[] command = str.split(" ",4);
            if (userList.get(fromNick).getUserAuthed() == true) { 
                protocol.sendNotice(client, myUniq, fromNick, "You are already authed."); 
                return;                 
            }
            try { password = command[2]; }
            catch (ArrayIndexOutOfBoundsException e) { 
                protocol.sendNotice(client, myUniq, fromNick, "Invalid command. Command is AUTH <username> <password>."); 
                return; 
            }

            try { username = command[1]; }
            catch (ArrayIndexOutOfBoundsException e) { 
                protocol.sendNotice(client, myUniq, fromNick, "Invalid command. Command is AUTH <username> <password>."); 
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
                protocol.sendNotice(client, myUniq, fromNick, "User account not found or incorrect password."); 
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
                userList.get(fromNick).setUserAccount(username);
                
                userList.get(fromNick).setUserAuthed(true);
                userList.get(fromNick).setUserAccountId(userToAuth.get("userId"));
                
                userList.get(fromNick).setUserChanlev(userChanlev);
                protocol.sendNotice(client, myUniq, fromNick, "Auth successful."); 

                // Now we apply the modes of the user's chanlev as it was joining the channels
                userList.get(fromNick).getUserChanList().forEach( (chanName, chanObj) -> {
                    this.handleJoin(userList.get(fromNick), chanObj);
                });

            }
            else { protocol.sendNotice(client, myUniq, fromNick, "User account not found or incorrect password."); }
        }
        else if (str.toUpperCase().startsWith("LOGOUT")) { // LOGOUT
            if (userList.get(fromNick).getUserAuthed() == false) { 
                protocol.sendNotice(client, myUniq, fromNick, "You are not authed."); 
            }
            else {
                userList.get(fromNick).setUserAccount("");
                userList.get(fromNick).setUserAuthed(false);
                protocol.sendNotice(client, myUniq, fromNick, "Logout successful.");
            }         
        }
        else if (str.toUpperCase().startsWith("VERSION")) {
            protocol.sendNotice(client, myUniq, fromNick, "qbot4u - The Q Bot for UnrealIRCd."); 
        }
        else if (str.toUpperCase().startsWith("REQUESTBOT ")) { // REQUESTBOT #channel
            String channel = (str.split(" ", 2))[1];

            if (userList.get(fromNick).getUserAuthed() == false) {
                protocol.sendNotice(client, myUniq, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            String ownerAccount = userList.get(fromNick).getUserAccount();

            // First check that the user is on the channel and opped
            if (userList.get(fromNick).getUserChanMode(channel).matches("(.*)o(.*)")) {
                try {
                    sqliteDb.addRegChan(channel, ownerAccount);
                    //userList.get(fromNick).setUserChanlev(channel, "+amno");
                    //sqliteDb.setUserChanlev(userList.get(fromNick).getUserAccount(), channel, userList.get(fromNick).getUserChanlev(channel));

                    sqliteDb.setUserChanlev(ownerAccount, channel, CHANLEV_FOUNDER_DEFAULT);
                    userList.forEach( (user, usernode) -> {
                        if (usernode.getUserAccount().equals(ownerAccount)) {
                            usernode.setUserChanlev(channel, CHANLEV_FOUNDER_DEFAULT);
                        }
                    } );
                    // updating channel chanlev as well
                    Map<String, Integer> chanNewChanlev = sqliteDb.getChanChanlev(channel);
                    channelList.get(channel).setChanChanlev(chanNewChanlev);
                    
                    protocol.chanJoin(client, myUniq, channel);
                    protocol.setMode(client, channel, "+ro", config.getCServeNick());
                    protocol.sendNotice(client, myUniq, fromNick, "Channel successfully registered."); 
                }
                catch (Exception e) { 
                    protocol.sendNotice(client, myUniq, fromNick, "Error while registering the channel."); 
                    e.printStackTrace();
                    return;
                }
            }
            else {
                protocol.sendNotice(client, myUniq, fromNick, "You must be present on the channel and be opped."); 
            }
            
        }
        else if (str.toUpperCase().startsWith("DROP ")) { // DROP #channel
            String channel = (str.split(" ", 2))[1];

            if (userList.get(fromNick).getUserAuthed() == false) {
                protocol.sendNotice(client, myUniq, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            // First check that the user is a the channel's owner (chanlev +n)
            try {
                if (   Flags.hasChanLOwnerPriv(userList.get(fromNick).getUserChanlev(channel)) == true ) {

                    userList.get(fromNick).unSetUserChanlev(channel);
                    sqliteDb.clearChanChanlev(channel);
                    sqliteDb.delRegChan(channel);
                    protocol.setMode(client, channel, "-r", "");
                    protocol.chanPart(client, myUniq, channel);
                    protocol.sendNotice(client, myUniq, fromNick, "Channel successfully dropped."); 
                }
                else {
                    protocol.sendNotice(client, myUniq, fromNick, "You must have the flag +n in the channel's chanlev to be able to drop it."); 
                }
            }
            catch (Exception e) { 
                protocol.sendNotice(client, myUniq, fromNick, "You must have the flag +n in the channel's chanlev to be able to drop it."); 
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
            userAccount = "";
            userChanlevFilter = "";

            if (userList.get(fromNick).getUserAuthed() == false) {
                protocol.sendNotice(client, myUniq, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            try {  channel = command[1]; }
            catch (ArrayIndexOutOfBoundsException e) { 
                protocol.sendNotice(client, myUniq, fromNick, "Invalid command. CHANLEV <channel> [<user> [<change>]]."); 
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
                userAccount = userNick.replaceFirst("#", "").toLowerCase();
            }
            else if (userNick.isEmpty() == true) { // no nick/account provided => only display chanlev
            }
            else { // indirect access to account => need to lookup account name
                try {
                    if (userList.get(protocol.getNickLookupTableCi(userNick)).getUserAccount().isEmpty() == false)  {
                        userAccount = userList.get(protocol.getNickLookupTableCi(userNick)).getUserAccount();
                        //System.out.println("BBX 1=" + userNick + " 2=" + userAccount);
                    }
                    else {
                        protocol.sendNotice(client, myUniq, fromNick, "That nickname is not authed.");
                        return; 
                    }
                }
                catch (NullPointerException e) { 
                    e.printStackTrace();
                    protocol.sendNotice(client, myUniq, fromNick, "No such nick.");
                    return;
                }
            }

            if ( userAccount.isEmpty() == false) {
                userChanlevFilter = userAccount;
            }

            if (chanlevMod.isEmpty() == true) { // no chanlev => list
                //System.out.println("BCB no chanlev => list userChanlevFilter=" +userChanlevFilter);

                try {
                    if (  Flags.hasChanLSignificant(userList.get(fromNick).getUserChanlev(channel))  ) {
                        protocol.sendNotice(client, myUniq, fromNick, "Displaying CHANLEV for channel " + channel + ":"); 
                        channelList.get(channel).getChanlev().forEach( (user, chanlev) -> {
                            //System.out.println("BCD userChanlevFilter=" +userChanlevFilter+ " user=" + user + " chanlev=" + chanlev);
                            if ( (userChanlevFilter.isEmpty() == false && user.toLowerCase().equals(userChanlevFilter.toLowerCase())) || userChanlevFilter.isEmpty() == true) {
                                protocol.sendNotice(client, myUniq, fromNick, user + "     +" + Flags.flagsIntToChars("chanlev", chanlev)); 
                            }
                        });
                        protocol.sendNotice(client, myUniq, fromNick, "End of list."); 
                    }
                    else {
                        protocol.sendNotice(client, myUniq, fromNick, "You do not have sufficient access on " + channel + " to use chanlev."); 
                        return;
                    }
                }
                catch (Exception e) { 
                    e.printStackTrace();
                    protocol.sendNotice(client, myUniq, fromNick, "You do not have sufficient access on " + channel + " to use chanlev."); 
                    return;
                }


            }
            else { 
                if (    chanlevMod.matches("^(?=.*["+ CHANLEV_FLAGS +"])(?=.*["+ CHANLEV_SYMBS +"]).+$")    ) {
                    if (   (  Flags.hasChanLMasterPriv(userList.get(fromNick).getUserChanlev(channel)) && ( Flags.containsChanLMasterConFlags(chanlevModInt.get("+")) || Flags.containsChanLMasterConFlags(chanlevModInt.get("-")) )) ||
                    (  Flags.hasChanLOwnerPriv(userList.get(fromNick).getUserChanlev(channel)) && ( Flags.containsChanLOwnerConFlags(chanlevModInt.get("+")) || Flags.containsChanLOwnerConFlags(chanlevModInt.get("-")) ))  ) {

                        // user wants to modify chanlev by account name directly
                        // in this case, we need to update the db + check if the account is online and update that nick chanlev
                        try {
                            // getting account chanlev from db to apply it to every usernode logged with account
                            //XXX to change next 3 lines

                            //System.out.println("BEA ");
                            Integer userCurChanlev = sqliteDb.getUserChanlev(userAccount, channel);
                            Integer userNewChanlev = Flags.applyFlagsFromStr("chanlev", userCurChanlev, chanlevModStr);
                            //String userNewChanlev = UserNode.parseChanlev(userCurChanlev, chanlevMod);
                            

                            //protocol.sendNotice(client, myUniq, fromNick, "BCA current chanlev " + channel + " -> " + Flags.flagsIntToChars("chanlev", userCurChanlev) + " :: mod=" + chanlevMod + " :: result=" + Flags.flagsIntToChars("chanlev", userNewChanlev));
                            //System.out.println("BCA current chanlev " + channel + " -> " + userCurChanlev + " :: mod=" + chanlevMod + " :: result=" + userNewChanlev);

                            sqliteDb.setUserChanlev(userAccount, channel, userNewChanlev);

                            channelList.get(channel).setChanChanlev(sqliteDb.getChanChanlev(channel));

                            userList.forEach( (user, usernode) -> {
                                //System.out.println("BCG user=" + user + " account="+ usernode.getUserAccount() + " userAccount=" + userAccount);
                                if (usernode.getUserAccount().equals(userAccount)) {
                                    //System.out.println("BCH user=" + user + " : " + channel + " -> " + userNewChanlev);
                                    //usernode.setUserChanlev(channel, userNewChanlev);
                                    try {
                                        // apply chanlev to every usernode logged as the user
                                        usernode.setUserChanlev(sqliteDb.getUserChanlev(userAccount));

                                        // Now we apply the modes of the user's chanlev as it was joining the channels
                                        this.handleJoin(usernode, channelList.get(channel));
                                    }
                                    catch (Exception e) { 
                                        e.printStackTrace();
                                        protocol.sendNotice(client, myUniq, fromNick, "Error setting chanlev."); 
                                        return; 
                                    }
                                }
                            } );

                            protocol.sendNotice(client, myUniq, fromNick, "Chanlev set. Chanlev for user account " + userAccount + " is now +" + Flags.flagsIntToChars("chanlev", userNewChanlev) + ".");
                        }
                        catch (Exception e) {
                            e.printStackTrace(); 
                            protocol.sendNotice(client, myUniq, fromNick, "Error setting chanlev."); 
                            return; 
                        }

                        if (channelList.get(channel).getChanlev() == null || channelList.get(channel).getChanlev().isEmpty() == true) {
                            try {
                                userList.get(fromNick).unSetUserChanlev(channel);
                                sqliteDb.clearChanChanlev(channel);
                                sqliteDb.delRegChan(channel);
                                protocol.setMode(client, channel, "-r", "");
                                protocol.chanPart(client, myUniq, channel);
                                protocol.sendNotice(client, myUniq, fromNick, "Channel has been dropped because its chanlev was left empty."); 
                            }
                            catch (Exception e) { return; }
                        }
                    }
                    else {
                        protocol.sendNotice(client, myUniq, fromNick, "You do not have sufficient rights on " + channel + " to set chanlev with those flags.");
                        return;
                    }
                }
                else {
                    protocol.sendNotice(client, myUniq, fromNick, "Invalid chanlev flags. Valid flags are: <+|->" + CHANLEV_FLAGS);
                } 
            }

            
        }
        else { // Unknown command
            protocol.sendNotice(client, myUniq, fromNick, "Unknown command \"" + str + "\". Type SHOWCOMMANDS for a list of available commands.");
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
            if (user.getUserChanlev().containsKey(channel.getChanName())) {
                if (  Flags.isChanLBanned( user.getUserChanlev(channel.getChanName()) ) == true ) {
                    //System.out.println("BBC chanlev ban");
                    try {
                        protocol.setMode(client, myUniq, channel.getChanName(), "+b", "*!*" + user.getUserIdent() + "@" + user.getUserHost());
                        protocol.chanKick(client, myUniq, channel.getChanName(), user.getUserNick(), "You are BANNED from this channel.");
                    }
                    catch (Exception e) { e.printStackTrace(); }
                }
                else if (   Flags.isChanLAuto( user.getUserChanlev(channel.getChanName())  ) ) {
                    if (  Flags.isChanLOp( user.getUserChanlev(channel.getChanName()) ) ) {
                        //System.out.println("BBD chanlev op");
                        try {
                            protocol.setMode(client, myUniq, channel.getChanName(), "+o", user.getUserNick());
                        }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                    else if (  Flags.isChanLVoice( user.getUserChanlev(channel.getChanName()))  ) {
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