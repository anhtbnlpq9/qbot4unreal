
import java.util.Map;
import java.util.Date;
import java.util.ArrayList;

import java.time.Instant;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class CService {
    
    Map<String, ServerNode> serverList;
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
        //client.write(":" + config.getServerId() + " " + "SJOIN " + unixTime + " " + config.getCServeStaticChan() + " + :" + config.getServerId() + config.getCServeUniq());


        unixTime = Instant.now().getEpochSecond();
        //client.write(":" + config.getServerId() + " MODE " + config.getCServeStaticChan() + " +o " + config.getCServeNick());
        ////this.write("MODE " + config.getCServeStaticChan() + " +o " + config.getCServeNick());
        
        for (String regChannel: sqliteDb.getRegChan()) {
            protocol.chanJoin(client, myUniq, regChannel);
            try { protocol.setMode(client, regChannel, "+o", config.getCServeNick()); }
            catch (Exception e) { e.printStackTrace(); }
        }

        protocol.chanJoin(client, myUniq, config.getCServeStaticChan());
        try {
            protocol.setMode(client, config.getCServeStaticChan(), "+o", config.getCServeNick());
        }
        catch (Exception e) { e.printStackTrace(); }
        
        
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

    public void handleMessage(String fromNick, String str) {
        String message, message2;
        String response;
        

        if (str.equalsIgnoreCase("HELP")) {
            message2 = "The following commands are available to you.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "For more information on a specific command, type HELP <command>:";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "ADDUSER        Adds one or more users to a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "AUTHHISTORY    View auth history for an account.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "AUTOLIMIT      Shows or changes the autolimit threshold on a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "BANCLEAR       Removes all bans from a channel including persistent bans.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "BANDEL         Removes a single ban from a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "BANLIST        Displays all persistent bans on a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "BANTIMER       Shows or changes the time after which bans are removed.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "CHANFLAGS      Shows or changes the flags on a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "CHANLEV        Shows or modifies user access on a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "CHANMODE       Shows which modes are forced or denied on a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "CHANOPHISTORY  Displays a list of who has been opped on a channel recently with account names.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "CHANSTAT       Displays channel activity statistics.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "CLEARCHAN      Removes all modes from a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "CLEARTOPIC     Clears the topic on a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "DEOPALL        Deops all users on channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "DEVOICEALL     Devoices all users on a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "EMAIL          Change your email address.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "GIVEOWNER      Gives total control over a channel to another user.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "HELP           Displays help on a specific command.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "INVITE         Invites you to a channel or channels.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "NEWPASS        Change your password.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "OP             Ops you or other users on channel(s).";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "PERMBAN        Permanently bans a hostmask on a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "RECOVER        Recovers a channel (same as deopall, unbanall, clearchan).";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "REMOVEUSER     Removes one or more users from a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "REQUESTOWNER   Requests ownership of a channel on which there are no owners.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "SETTOPIC       Changes the topic on a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "SHOWCOMMANDS   Lists available commands.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "TEMPBAN        Bans a hostmask on a channel for a specified time period.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "UNBANALL       Removes all bans from a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "UNBANMASK      Removes bans matching a particular mask from a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "UNBANME        Removes any bans affecting you from a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "USERFLAGS      Shows or changes user flags.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "USERS          Displays a list of users on the channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "VERSION        Show Version.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "VOICE          Voices you or other users on channel(s).";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "WELCOME        Shows or changes the welcome message on a channel.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "WHOAMI         Displays information about you.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
            client.write(response);
            message2 =  "WHOIS          Displays information about a user.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
            message2 =  "End of list.";
            response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
            client.write(response);
        }            
        else if (str.equalsIgnoreCase("USERLIST")) {
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
            
            channelList.forEach( (chan, node) -> {

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
                    if ((user.getValue().getUserNick()).toUpperCase().equals(nick.toUpperCase())) {
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
                                protocol.sendNotice(client, myUniq, fromNick, "| |- " + bufferMode + " " + key);
                            });
                        }

                        
                        if (user.getValue().getUserAuthed() == true && ( user.getValue().getUserNick().equals(userList.get(fromNick).getUserNick()) ) ) {
                            protocol.sendNotice(client, myUniq, fromNick, "|- chanlev: ");

                            user.getValue().getUserChanlev().forEach( (key, value) -> {
                                bufferMode = "";
                                if (value.isEmpty() == false) { bufferMode = "+" + value; }
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
        else if (str.toUpperCase().matches("HELLO[ ]{0,1}.*")) { // REGISTER <password> <email>
            String password;
            String email;
            
            String[] command = str.split(" ",4);

            if (userList.get(fromNick).getUserAuth() == true) { 
                protocol.sendNotice(client, myUniq, fromNick, "You are already authed."); 
                return;                 
            }

            try { email = command[2]; }
            catch (ArrayIndexOutOfBoundsException e) { protocol.sendNotice(client, myUniq, fromNick, "Invalid command. Command is HELLO <password> <email>."); return; }
            try { password = command[1]; }
            catch (ArrayIndexOutOfBoundsException e) { protocol.sendNotice(client, myUniq, fromNick, "Invalid command. Command is HELLO <password> <email>."); return; }

            if (email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+")==false) {
                protocol.sendNotice(client, myUniq, fromNick, "REGISTER: Invalid email address.");
                return;
            }
            if (password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,64}$")==false) {
                protocol.sendNotice(client, myUniq, fromNick, "REGISTER: Password must contain at least 8 (at most 64) characters with at least one of the following types: lowercase, uppercase, number, symbol.");
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

            protocol.sendNotice(client, myUniq, fromNick, "Your account has been created.");

        }
        else if (str.toUpperCase().startsWith("AUTH ")) { // AUTH <username> <password>
            String password;
            String username;
            String userId;
            Map<String, String> userChanlev;
            
            String[] command = str.split(" ",4);
            if (userList.get(fromNick).getUserAuth() == true) { 
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
            }
            else { protocol.sendNotice(client, myUniq, fromNick, "User account not found or incorrect password."); }
        }
        else if (str.toUpperCase().startsWith("LOGOUT")) { // LOGOUT
            if (userList.get(fromNick).getUserAuth() == false) { 
                protocol.sendNotice(client, myUniq, fromNick, "You are not authed."); 
            }
            else {
                userList.get(fromNick).setUserAccount("");
                userList.get(fromNick).setUserAuthed(false);
                protocol.sendNotice(client, myUniq, fromNick, "Logout successful.");
            }         
        }
        else if (str.toUpperCase().startsWith("SHOWCOMMANDS")) {
            protocol.sendNotice(client, myUniq, fromNick, "The following commands are available to you."); 
            protocol.sendNotice(client, myUniq, fromNick, "For more information on a specific command, type HELP <command>:"); 
            if (userList.get(fromNick).getUserAuth() == false) { 
                protocol.sendNotice(client, myUniq, fromNick, "AUTH                 Authenticates you on the bot.");
                //protocol.sendNotice(client, myUniq, fromNick, "CHALLENGE            Returns a challenge for use in challengeauth."); 
                //protocol.sendNotice(client, myUniq, fromNick, "CHALLENGEAUTH        Authenticates you on the bot using challenge response."); 
                protocol.sendNotice(client, myUniq, fromNick, "HELLO                Creates a new user account."); 
                //protocol.sendNotice(client, myUniq, fromNick, "HELP                 Displays help on a specific command."); 
                //protocol.sendNotice(client, myUniq, fromNick, "REQUESTPASSWORD      Requests the current password by email."); 
                //protocol.sendNotice(client, myUniq, fromNick, "RESET                Restores the old details on an account after a change."); 
                protocol.sendNotice(client, myUniq, fromNick, "SHOWCOMMANDS         Lists available commands."); 
                protocol.sendNotice(client, myUniq, fromNick, "VERSION              Show Version.");
            }
            else if (userList.get(fromNick).getUserAuth() == true) { 
                //protocol.sendNotice(client, myUniq, fromNick, "HELP                     Displays help on a specific command."); 
                protocol.sendNotice(client, myUniq, fromNick, "DROP                 Removes the bot to a channel."); 
                protocol.sendNotice(client, myUniq, fromNick, "LOGOUT               Deauthenticates yourself from the bot."); 
                protocol.sendNotice(client, myUniq, fromNick, "REQUESTBOT           Requests the bot to a channel."); 
                protocol.sendNotice(client, myUniq, fromNick, "SHOWCOMMANDS         Lists available commands."); 
                protocol.sendNotice(client, myUniq, fromNick, "VERSION              Show Version."); 
                //protocol.sendNotice(client, myUniq, fromNick, "WHOAMI Displays information about you."); 
                protocol.sendNotice(client, myUniq, fromNick, "WHOIS                Displays information about a user."); 
            }
            else {
                userList.get(fromNick).setUserAccount("");
                userList.get(fromNick).setUserAuthed(false);
                protocol.sendNotice(client, myUniq, fromNick, "Logout successful.");
            }
            protocol.sendNotice(client, myUniq, fromNick, "End of list."); 
        }
        else if (str.toUpperCase().startsWith("VERSION")) {
            protocol.sendNotice(client, myUniq, fromNick, "qbot4u - The Q Bot for UnrealIRCd."); 
        }
        else if (str.toUpperCase().startsWith("REQUESTBOT ")) { // REQUESTBOT #channel
            String channel = (str.split(" ", 2))[1];
            String ownerAccount = userList.get(fromNick).getUserAccount();

            if (userList.get(fromNick).getUserAuthed() == false) {
                protocol.sendNotice(client, myUniq, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            // First check that the user is on the channel and opped
            if (userList.get(fromNick).getUserChanMode(channel).matches("(.*)o(.*)")) {
                try {
                    sqliteDb.addRegChan(channel, ownerAccount);
                    userList.get(fromNick).setUserChanlev(channel, "+amno");
                    sqliteDb.setUserChanlev(userList.get(fromNick).getUserAccount(), channel, userList.get(fromNick).getUserChanlev(channel));
                    
                    protocol.chanJoin(client, myUniq, channel);
                    protocol.setMode(client, channel, "+o", config.getCServeNick());
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
            String ownerAccount = userList.get(fromNick).getUserAccount();

            if (userList.get(fromNick).getUserAuthed() == false) {
                protocol.sendNotice(client, myUniq, fromNick, "Unknown command. Type SHOWCOMMANDS for a list of available commands."); 
                return;
            }

            // First check that the user is a the channel's owner (chanlev +n)
            
            if (userList.get(fromNick).getUserChanlev(channel).matches("(.*)n(.*)")) {
                try {
                    userList.get(fromNick).unSetUserChanlev(channel);
                    sqliteDb.unSetUserChanlev(channel);
                    sqliteDb.delRegChan(channel);
                    protocol.chanPart(client, myUniq, channel);
                    protocol.sendNotice(client, myUniq, fromNick, "Channel successfully dropped."); 
                }
                catch (Exception e) { 
                    protocol.sendNotice(client, myUniq, fromNick, "Error while dropping the channel."); 
                    e.printStackTrace();
                    return;
                }
            }
            else {
                protocol.sendNotice(client, myUniq, fromNick, "You must have the flag +n in the chanlev to drop the channel."); 
            }
            
        }
        else { // Unknown command
            message = "Unknown command \"" + str + "\". Type SHOWCOMMANDS for a list of available commands.";
            protocol.sendNotice(client, myUniq, fromNick, message);
        }
        //return "";
    }

}