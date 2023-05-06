
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.time.Instant;


public class CService {
    
    Map<String, ServerNode> serverList;
    Map<String, UserNode> userList;
    
    String myUniq;
    
    Client client;
    Protocol protocol;
    
    Boolean cServiceReady = false;

    long unixTime;
    
    public CService() {
        
    }
    public CService(Client client) {
        this.client = client;
    }        

    public CService(Client client, Protocol protocol) {
        this.client = client;
        this.protocol = protocol;
    }

    public CService(Client client, Protocol protocol, Map<String, UserNode> userList) {
        this.client = client;
        this.protocol = protocol;
        this.userList = userList;
    }         
 
    public void runCService(Config config, Protocol protocol, Map<String, UserNode> userList, Map<String, ServerNode> serverList) {
        this.userList = userList;
        this.serverList = serverList;
        unixTime = Instant.now().getEpochSecond();
        client.write(":" + config.getServerId() + " " + "UID " + config.getCServeNick() + " 1 " + unixTime + " " + config.getCServeIdent() + " " + config.getCServeHost() + " " + config.getServerId() + config.getCServeUniq() + " * " + config.getCServeModes() + " * * * :" + config.getCServeRealName());
        // UID nickname hopcount timestamp username hostname uid servicestamp usermodes virtualhost cloakedhost ip :gecos
        UserNode user = new UserNode(config.getCServeNick(), 
                                     config.getCServeIdent(), 
                                     config.getCServeHost(),
                                     config.getCServeHost(),
                                     config.getCServeRealName(),
                                     config.getServerId()+config.getCServeUniq(),
                                     unixTime,
                                     config.getCServeModes());
        user.setUserServer(serverList.get(config.getServerId()));
        userList.put(config.getCServeUniq(), user);
        myUniq = config.getServerId()+config.getCServeUniq();
        
        unixTime = Instant.now().getEpochSecond();
        //client.write(":" + config.getServerId() + " " + "SJOIN " + unixTime + " " + config.getCServeStaticChan() + " + :" + config.getServerId() + config.getCServeUniq());


        unixTime = Instant.now().getEpochSecond();
        //client.write(":" + config.getServerId() + " MODE " + config.getCServeStaticChan() + " +o " + config.getCServeNick());
        ////this.write("MODE " + config.getCServeStaticChan() + " +o " + config.getCServeNick());
        
        protocol.chanJoin(client, myUniq, config.getCServeStaticChan());
        protocol.setMode(client, config.getCServeStaticChan(), "+o Q");
        
        
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
        if (str.equalsIgnoreCase("help")) {
                message2 = "The following commands are available to you.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "For more information on a specific command, type HELP <command>:";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "ADDUSER Adds one or more users to a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "AUTHHISTORY View auth history for an account.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "AUTOLIMIT Shows or changes the autolimit threshold on a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "BANCLEAR Removes all bans from a channel including persistent bans.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "BANDEL Removes a single ban from a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "BANLIST Displays all persistent bans on a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "BANTIMER Shows or changes the time after which bans are removed.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "CHANFLAGS Shows or changes the flags on a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "CHANLEV Shows or modifies user access on a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "CHANMODE Shows which modes are forced or denied on a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "CHANOPHISTORY Displays a list of who has been opped on a channel recently with account names.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "CHANSTAT Displays channel activity statistics.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "CLEARCHAN Removes all modes from a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "CLEARTOPIC Clears the topic on a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "DEOPALL Deops all users on channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "DEVOICEALL Devoices all users on a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "EMAIL Change your email address.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "GIVEOWNER Gives total control over a channel to another user.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "HELP Displays help on a specific command.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "INVITE Invites you to a channel or channels.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "NEWPASS Change your password.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "OP Ops you or other users on channel(s).";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "PERMBAN Permanently bans a hostmask on a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "RECOVER Recovers a channel (same as deopall, unbanall, clearchan).";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "REMOVEUSER Removes one or more users from a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "REQUESTOWNER Requests ownership of a channel on which there are no owners.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "SETTOPIC Changes the topic on a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "SHOWCOMMANDS Lists available commands.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "TEMPBAN Bans a hostmask on a channel for a specified time period.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "UNBANALL Removes all bans from a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "UNBANMASK Removes bans matching a particular mask from a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "UNBANME Removes any bans affecting you from a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "USERFLAGS Shows or changes user flags.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "USERS Displays a list of users on the channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "VERSION Show Version.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "VOICE Voices you or other users on channel(s).";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "WELCOME Shows or changes the welcome message on a channel.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "WHOAMI Displays information about you.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2; 
                client.write(response);
                message2 =  "WHOIS Displays information about a user.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
                message2 =  "End of list.";
                response = ":" + myUniq + " NOTICE " + fromNick + " " + message2;
                client.write(response);
            }
            
            else if (str.equalsIgnoreCase("userlist")) {
                protocol.sendNotice(client, myUniq, fromNick, "List of users:");
                
                /*for (Map.Entry<A, B> e : myMap.entrySet()) {
                    A key    = e.getKey();
                    B value  = e.getValue();
                }*/
                int counter=0;
                for (Map.Entry<String, UserNode> user : userList.entrySet()) {
                    protocol.sendNotice(client, myUniq, fromNick, " * " + user.getValue().getUserUniq() + " " + user.getValue().getUserNick() + "!" + user.getValue().getUserIdent() + "@" + user.getValue().getUserHost() + " (" + user.getValue().getUserRealHost() + ") " + user.getValue().getUserModes());
                    counter++;
                }
                protocol.sendNotice(client, myUniq, fromNick, "There are " + counter + " users on the network.");
                protocol.sendNotice(client, myUniq, fromNick, "End of list.");
            }

            else if (str.equalsIgnoreCase("serverlist")) {
                protocol.sendNotice(client, myUniq, fromNick, "List of servers:");
                
                /*for (Map.Entry<A, B> e : myMap.entrySet()) {
                    A key    = e.getKey();
                    B value  = e.getValue();
                }*/
                
                int counter=0;
                for (Map.Entry<String, ServerNode> server : serverList.entrySet()) {
                    String serverPeerStatus = "";
                    if (server.getValue().getServerPeer()==true) { serverPeerStatus = "@";  }
                    String serverEOS = "no";
                    if (server.getValue().getServerEOS()==true) { serverEOS = "yes";  }
                    protocol.sendNotice(client, myUniq, fromNick, " * " + serverPeerStatus + server.getValue().getServerId() + " " + server.getValue().getServerName() + " EOS:" + serverEOS);
                    counter++;
                }
                protocol.sendNotice(client, myUniq, fromNick, "There are " + counter + " servers on the network.");
                protocol.sendNotice(client, myUniq, fromNick, "End of list.");
            }
            
            else if (str.toUpperCase().startsWith("IRCWHOIS ")) {
                String nick = (str.split(" ", 2))[1];
                int foundNick=0;
                for (Map.Entry<String, UserNode> user : userList.entrySet()) {
                    if ((user.getValue().getUserNick()).toUpperCase().equals(nick.toUpperCase())) {
                        foundNick=1;
                        protocol.sendNotice(client, myUniq, fromNick, nick + " (" + user.getValue().getUserUniq() + ") is " + user.getValue().getUserIdent() + "@" + user.getValue().getUserHost() + " * " + user.getValue().getUserRealName());
                        protocol.sendNotice(client, myUniq, fromNick, nick + " is connecting from @" + user.getValue().getUserRealHost());
                        protocol.sendNotice(client, myUniq, fromNick, nick + " is using modes " + user.getValue().getUserModes());
                        protocol.sendNotice(client, myUniq, fromNick, nick + " is using server " + (user.getValue().getUserServer()).getServerName() + " (" + (user.getValue().getUserServer()).getServerId() + ")");
                        //protocol.sendNotice(client, myUniq, fromNick, nick + " has TS " + (String)user.getValue().getUserTS());
                    }
                }
                if (foundNick == 0) {
                    protocol.sendNotice(client, myUniq, fromNick, "No such nick.");
                }
                else {
                    protocol.sendNotice(client, myUniq, fromNick, "End of WHOIS.");
                }
            }


/* [17:16:56] AnhTay is anh@user/AnhTay * Tôi Là Anh
[17:16:56] AnhTay is using modes +ioptwxzHTW bcdfjkoqsBCOS
[17:16:56] AnhTay is connecting from anh@2001:bc8:3057:f02:4e1c:3752:6901:591c 2001:bc8:3057:f02:4e1c:3752:6901:591c
[17:16:56] AnhTay on #qbot @#Civilization #mjav #Opers/services #Opers 
[17:16:56] AnhTay using sandcat. Mjav Network IRC server
[17:16:56] AnhTay is an IRC Operator (anhtay) [netadmin]
[17:16:56] AnhTay is using a Secure Connection [TLSv1.3-TLS_CHACHA20_POLY1305_SHA256]
[17:16:56] AnhTay is in security-groups: known-users,registered-users,trustedPeople,users-anhtay,tls-and-known-users,tls-users
[17:16:56] AnhTay is using an IP with a reputation score of 10000
[17:16:56] AnhTay FR set by is connecting from France
[17:16:56] AnhTay has client certificate fingerprint 2ba3a87c6d4cd870f40ebc207b26a060b044f16395883169fe161a7c4c9a9b13
[17:16:56] AnhTay is logged in as AnhTay
[17:16:56] AnhTay has been idle 10secs, signed on Fri May 05 08:26:48 2023 */




            else { // Unknown command
                message = "Unknown command \"" + str + "\". Type SHOWCOMMANDS for a list of available commands.";
                protocol.sendNotice(client, myUniq, fromNick, message);
            }
        //return "";
    }

}