import java.lang.String;
import java.lang.Exception;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Protocol extends Exception {
    
    private Client client;
    private Config config;
    private ServerNode server;

    //private static ArrayList<UserNode> userList = new ArrayList<UserNode>();
    //private static ArrayList<ServerNode> serverList = new ArrayList<ServerNode>();
    //private static ArrayList<ChanNode> chanList = new ArrayList<ChanNode>();
    
    private Map<String, ServerNode> serverList = new HashMap<String, ServerNode>();
    private Map<String, UserNode> userList = new HashMap<String, UserNode>();
    
    private ArrayList<String> protocolProps = new ArrayList<String>();
    
    String myPeerServerId;
    
    public Protocol() {
        
    }
    public Protocol(Config config) {
        this.config = config;
    }

    public void setClientRef(Client client) {
        this.client = client;
    }
    
    public void write(Client client, String str) /*throws Exception*/ {
        client.write(str);
    }

  public void getResponse(String raw) throws Exception {

    String response = "";
    String[] command;
    
    command = raw.split(" ", 3); // Begin to split raw message to fetch the command (part0 part1 part2part3part4...)

    // Check for IRCv3 string presence, if yes we cut if off to part1 part2 part3part4...
    // @blaablaa ...
    if (command[0].startsWith("@")) {
        command = (command[1] + " " + command[2]).split(" ", 3); // This cuts the IRCv3 prelude
    }

    //System.out.println("=== command[0]=" + command[0] + " ; command[1]=" + command[1]);
    
    String fromEnt;
    String toEnt;
    
    // :ABC PRIVMSG  DEF :MESSAGE
    // | 0| |    1| |      2     |   
    if (command[1].equals("PRIVMSG")) {
        String fromEntity = (command[0].split(":"))[1];

        command = (command[2]).split(" ", 2);
        String toEntity   =  command[0];
        String message    =  command[1];
        
        //System.out.println("from=" + fromEntity + " to=" + toEntity + " message="+message + "---" + config.getServerId() + config.getCServUniq());

        String message2;

        // Test for output performance
        if (toEntity.equals(config.getServerId() + config.getCServUniq()) && message.equals(":help")) {
            message2 = "The following commands are available to you.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "For more information on a specific command, type HELP <command>:";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "ADDUSER Adds one or more users to a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "AUTHHISTORY View auth history for an account.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "AUTOLIMIT Shows or changes the autolimit threshold on a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "BANCLEAR Removes all bans from a channel including persistent bans.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "BANDEL Removes a single ban from a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "BANLIST Displays all persistent bans on a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "BANTIMER Shows or changes the time after which bans are removed.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "CHANFLAGS Shows or changes the flags on a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "CHANLEV Shows or modifies user access on a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "CHANMODE Shows which modes are forced or denied on a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "CHANOPHISTORY Displays a list of who has been opped on a channel recently with account names.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "CHANSTAT Displays channel activity statistics.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "CLEARCHAN Removes all modes from a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "CLEARTOPIC Clears the topic on a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "DEOPALL Deops all users on channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "DEVOICEALL Devoices all users on a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2; write(client, response);
            message2 =  "EMAIL Change your email address.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "GIVEOWNER Gives total control over a channel to another user.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "HELP Displays help on a specific command.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "INVITE Invites you to a channel or channels.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "NEWPASS Change your password.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "OP Ops you or other users on channel(s).";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "PERMBAN Permanently bans a hostmask on a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "RECOVER Recovers a channel (same as deopall, unbanall, clearchan).";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "REMOVEUSER Removes one or more users from a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "REQUESTOWNER Requests ownership of a channel on which there are no owners.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "SETTOPIC Changes the topic on a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "SHOWCOMMANDS Lists available commands.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "TEMPBAN Bans a hostmask on a channel for a specified time period.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "UNBANALL Removes all bans from a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "UNBANMASK Removes bans matching a particular mask from a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "UNBANME Removes any bans affecting you from a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "USERFLAGS Shows or changes user flags.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "USERS Displays a list of users on the channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "VERSION Show Version.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "VOICE Voices you or other users on channel(s).";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "WELCOME Shows or changes the welcome message on a channel.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "WHOAMI Displays information about you.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "WHOIS Displays information about a user.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
            message2 =  "End of list.";
            response = ":" + toEntity + " NOTICE " + fromEntity + " " + message2;write(client, response);
        }
    }

    // SID is used by the peer to introduce the other servers
    // :peer SID name hop sid :description
    else if (command[1].equals("SID")) {
        //<<< :5P0 SID sandcat. 2 5PX :Mjav Network IRC server

        fromEnt = (command[0].split(":"))[1];
        
        command = (command[2]).split(" ", 4);
        String name = command[0];
        String hop = command[1];
        String sid = command[2];
        String desc = (command[3].split(":"))[1];
        server = new ServerNode(name, hop, sid, desc);
        serverList.put(sid, server);
        
        System.out.println("@@@ " + fromEnt + " introduced new server " + name + " / " + hop + " / " + sid + " / " + desc);
    }
    
    else if (command[1].equals("EOS")) {
        //<<< :5PX EOS

        fromEnt = (command[0].split(":"))[1];

        ServerNode server = serverList.get(fromEnt);
        System.out.println("@@@ " + fromEnt + " " + server.getServerName() + " reached EOS ");
        server.setEOS(true);
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
        
        for (int i=1; i < propsCount; i++) {
            //System.out.println("PPP " + prop[i]);
            protocolProps.add(prop[i]);
            if (prop[i].startsWith("SID")) {
                myPeerServerId = (prop[i].split("="))[1];
                server = new ServerNode((prop[i].split("="))[1]);
                serverList.put((prop[i].split("="))[1], server);
                System.out.println("@@@ " + (prop[i].split("="))[1] + " introduced itself");
            }
        }
    }

    else if (command[0].equals("SERVER")) {
        //<<< SERVER ocelot. 1 :U6000-Fhn6OoEmM-5P0 Mjav Network IRC server
        String[] string = raw.split(" ", 4);
        ServerNode server = serverList.get(myPeerServerId);
        server.setServerName(string[1]);
        server.setServerDistance(string[2]);
        server.setServerDescription((string[3].split(":"))[1]);
    }

    else if (command[1].equals("UID")) {
        // :XXX UID nickname hopcount timestamp username hostname uid servicestamp usermodes virtualhost cloakedhost ip :gecos

        fromEnt = (command[0].split(":"))[1];
        
        command = command[2].split(" ", 12);

        UserNode user = new UserNode( command[0],    // nick
                                    command[3],      // ident
                                    command[8],      // vhost
                                    command[4],      // realhost
                                    (command[11].split(":"))[1],   // gecos
                                    command[5],      // unique id
                                    Integer.parseInt(command[2]),   // TS
                                    command[7]    // modes
                                 );

        userList.put(command[5], user);
        System.out.println("UUU new user " + command[0] + " " + command[5] + " " + command[8] + " " + command[4] + " " + command[7]);

    }
    else {
        if (command[0].equals("PING")) {
            response = "PONG " + command[1];
            write(client, response);
        }
    }
  }
}
