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
    private CService cservice;

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
    
    public void setCService(CService cservice) {
        this.cservice = cservice;
    }
    
    public void write(Client client, String str) /*throws Exception*/ {
        client.write(str);
    }

    public void sendPrivmsg(Client client, String from, String to, String msg) /*throws Exception*/ {
        String str = ":" + from + " PRIVMSG " + to + " :" + msg;
        client.write(str);
    }
    public void sendNotice(Client client, String from, String to, String msg) /*throws Exception*/ {
        String str = ":" + from + " NOTICE " + to + " :" + msg;
        client.write(str);
    }
    public void chanJoin(Client client, String who, String chan) /*throws Exception*/ {
        String str = ":" + who + " JOIN " + chan;
        client.write(str);
    }
    public void chanPart(Client client, String who, String chan) /*throws Exception*/ {
        String str = ":" + who + " PART " + chan;
        client.write(str);
    }
    public void setMode(Client client, String who, String target, String parameters) /*throws Exception*/ {
        String str = ":" + who + " MODE " + target + " " + parameters;
        client.write(str);
    }
    public void setMode(Client client, String target, String parameters) /*throws Exception*/ {
        String who = config.getServerId();
        String str = ":" + who + " MODE " + target + " " + parameters;
        client.write(str);
    }

    public Map<String, ServerNode> getServerList() {
        return this.serverList;
    }

    public Map<String, UserNode> getUserList() {
        return this.userList;
    }

    public String getPeerId() {
        return this.myPeerServerId;
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

        // Test for output performance
        if (toEntity.equals(config.getServerId() + config.getCServeUniq())) {
            // Stripping message
            message = (message.split(":",2))[1];
            cservice.handleMessage(fromEntity, message);
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
        
        //System.out.println("@@@ " + fromEnt + " introduced new server " + name + " / " + hop + " / " + sid + " / " + desc);
    }
    
    else if (command[1].equals("EOS")) {
        //<<< :5PX EOS

        fromEnt = (command[0].split(":"))[1];

        ServerNode server = serverList.get(fromEnt);
        //System.out.println("@@@ " + fromEnt + " " + server.getServerName() + " reached EOS ");
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
                server.setPeer(true);
                serverList.put((prop[i].split("="))[1], server);
                //System.out.println("@@@ " + (prop[i].split("="))[1] + " introduced itself");
            }
        }
        serverList.get(config.getServerId()).setServerPeerResponded(true);
    }

    else if (command[0].equals("SERVER")) {
        //<<< SERVER ocelot. 1 :U6000-Fhn6OoEmM-5P0 Mjav Network IRC server
        String[] string = raw.split(" ", 4);
        ServerNode server = serverList.get(myPeerServerId);
        server.setServerName(string[1]);
        server.setServerDistance(string[2]);
        server.setServerDescription((string[3].split(":"))[1]);
        
        serverList.get(config.getServerId()).setServerPeerResponded(true);
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
        user.setUserServer(serverList.get(fromEnt));
        userList.put(command[5], user);
        //System.out.println("UUU new user " + command[0] + " " + command[5] + " " + command[8] + " " + command[4] + " " + command[7]);

    }
    else {
        if (command[0].equals("PING")) {
            response = "PONG " + command[1];
            write(client, response);
        }
    }
  }
}
