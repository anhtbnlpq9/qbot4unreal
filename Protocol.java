
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
    
    private Map<String, ServerNode> serverList = new HashMap<String, ServerNode>();
    private Map<String, UserNode> userList = new HashMap<String, UserNode>();
    private Map<String, ChannelNode> channelList = new HashMap<String, ChannelNode>();

    private Map<String, String> protocolProps = new HashMap<String, String>();
    
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
    public Map<String, ChannelNode> getChanList() {
        return this.channelList;
    }
    public String getPeerId() {
        return this.myPeerServerId;
    }
    public void getResponse(String raw) throws Exception {

        String response = "";
        String[] command;
        String fromEnt;
        String toEnt;
        
        command = raw.split(" ", 3); // Begin to split raw message to fetch the command (part0 part1 part2part3part4...)

        // Check for IRCv3 string presence, if yes we cut if off to part1 part2 part3part4...
        // @blaablaa ...
        if (command[0].startsWith("@")) {
            command = (command[1] + " " + command[2]).split(" ", 3); // This cuts the IRCv3 prelude
        } 
        if (command[1].equals("PRIVMSG")) {
            // :ABC PRIVMSG  DEF :MESSAGE
            // | 0| |    1| |      2     |   
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
        else if (command[1].equals("SID")) {
            // SID is used by the peer to introduce the other servers
            // :peer SID name hop sid :description
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
            
            String propertyName;
            String propertyValue;
            
            for (int i=1; i < propsCount; i++) {
                //System.out.println("PPP " + prop[i]);
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
        else if (command[1].equals("SJOIN")) {
            // :5P0 SJOIN 1680362593 #mjav         +fnrtCPST [5j#R1,7m#M1,3n#N1,5t#b1]:6         :5PKEPJH3U @5PXDR1D20 @5P0FWO841 
            //      SJOIN 1681424518 #Civilization +fnrtCHPS [30j#R10,40m#M10,10n#N15]:15 50:15m :@5PX8ZA302 @5PBAAAAAI &test!*@* "test!*@* 'test!*@*
            System.out.println("DDD SJOIN " + command[2]);
            fromEnt = (command[0].split(":"))[1];

            String[] sjoinParam      = command[2].split(" ", 64);
            int      sjoinParamCount = command[2].split(" ", 64).length;

            int indexFirstUser=0;
            int indexMode = 0;
            String userId;
            String userMode;
            long channelTS = Integer.parseInt(sjoinParam[0]);
            
            String networkChanModes              = protocolProps.get("CHANMODES");
            String networkChanmodesWithOutParams = ((protocolProps.get("CHANMODES")).split(",", 4))[3];
            String networkChanmodesWithParams    = ((protocolProps.get("CHANMODES")).split(",", 4))[0] + ((protocolProps.get("CHANMODES")).split(",", 4))[1] + ((protocolProps.get("CHANMODES")).split(",", 4))[2];
            
            String channelName = sjoinParam[1];

            char[] chanMode;                 // Contains the modes of the channel (without the params)
            String chanModeRaw;              // Contains the modes of the channel (without the params)

            char[] chanModeWithParams;       // Contains the list of channel modes that allows params (arrayed)
            String chanModeRawWithParams;    // Contains the list of channel modes that allows params

            char[] chanModeWithOutParams;    // Contains the list of channel modes that don't allow params (arrayed)
            String chanModeRawWithOutParams; // Contains the list of channel modes that don't allow params

            Map<String, String> chanModeList = new HashMap<String, String>();
            
            String chanUserModes = "\\+%@~\\*";

            chanModeRaw              = (sjoinParam[2].split("\\+", 2))[1];                                     // Contains all the channel ['m','o','d','e','s'] 
            chanModeRawWithParams    = chanModeRaw.replaceAll("["+ networkChanmodesWithOutParams + "]", "");   // Contains only channel ['m','o','d','e','s'] used with parameter
            chanModeRawWithOutParams = chanModeRaw.replaceAll("["+ networkChanmodesWithParams + "]", "");      // Contains only channel ['m','o','d','e','s'] used withOut parameter

            chanMode                 = chanModeRaw.toCharArray();                                              // Contains all the channel modes 
            chanModeWithParams       = chanModeRawWithParams.toCharArray();                                    // Contains only channel modes used with parameter
            chanModeWithOutParams    = chanModeRawWithOutParams.toCharArray();                                 // Contains only channel ['m','o','d','e','s'] used withOut parameter

            ArrayList<String> chanBanList = new ArrayList<String>();
            ArrayList<String> chanExceptList = new ArrayList<String>();
            ArrayList<String> chanInviteList = new ArrayList<String>();
            
            Map<String, String> chanUserMode = new HashMap<String, String>();

            // Now we need to determine index of modes and index of users in the string
            for (int i=3; i < sjoinParamCount; i++) {
                
                // detect first user
                if (sjoinParam[i].startsWith(":")) { indexFirstUser=i; }
                
                if (indexFirstUser == 0) { // first user not detected => we are still in the mode section

                    // Populate first the hashmap with modes with parameters
                    chanModeList.put( String.valueOf(chanModeWithParams[indexMode]) , sjoinParam[i]);
                    indexMode++;

                }
                else {  // user section detected

                    if (sjoinParam[i].equals("")) {
                        // Do nothing because this is not an user
                        break;
                    }

                    // Handle the ban/except/invite lists
                    if ( sjoinParam[i].replaceFirst(":", "").startsWith("&") || sjoinParam[i].replaceFirst(":", "").startsWith("\"") || sjoinParam[i].replaceFirst(":", "").startsWith("'") ) {

                        //for( int pos=0; pos < sjoinParam[i].replaceFirst(":", "").replaceAll("[A-Za-z0-9]","").length() ; pos++ ) {
                            //System.out.println("before chan=" + channelName + " mode=" + sjoinParam[i].replaceFirst(":", "").replaceAll("[A-Za-z0-9]","").substring(pos) + " user=" + sjoinParam[i].replaceFirst(":", "").replaceAll("[^A-Za-z0-9]",""));
                            
                            if ( sjoinParam[i].replaceFirst(":", "").startsWith("&") ) { // bans
                                chanBanList.add(sjoinParam[i].replaceFirst(":", "").substring(1));
                            }
                            else if ( sjoinParam[i].replaceFirst(":", "").startsWith("\"") )  { // excepts
                                chanExceptList.add(sjoinParam[i].replaceFirst(":", "").substring(1));
                            }
                            else if ( sjoinParam[i].replaceFirst(":", "").startsWith("'") ) { // invites    
                                chanInviteList.add(sjoinParam[i].replaceFirst(":", "").substring(1));                           
                            }

                            else {
                                
                            }
                            
                        //}
                    }

                    // Handle the user modes for 1 user
                    else {
                        chanUserMode.put( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") , "");
                        //System.out.println("initial put for " + channelName + " (" + sjoinParam[i].replaceAll("[^A-Za-z0-9]","") + ") -> null");
                        for( int pos=0; pos < sjoinParam[i].replaceFirst(":", "").replaceAll("[A-Za-z0-9]","").length() ; pos++ ) {
                            //System.out.println("before chan=" + channelName + " mode=" + sjoinParam[i].replaceFirst(":", "").replaceAll("[A-Za-z0-9]","").substring(pos) + " user=" + sjoinParam[i].replaceFirst(":", "").replaceAll("[^A-Za-z0-9]",""));
                            
                            if ( sjoinParam[i].replaceFirst(":", "").replaceAll("[A-Za-z0-9]","").substring(pos).startsWith("+") ) {
                                chanUserMode.replace( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") , chanUserMode.get( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") ) + "v");
                            }
                            else if ( sjoinParam[i].replaceFirst(":", "").replaceAll("[A-Za-z0-9]","").substring(pos).startsWith("%") )  {
                                chanUserMode.replace( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") , chanUserMode.get( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") ) + "h");
                            }
                            else if ( sjoinParam[i].replaceFirst(":", "").replaceAll("[A-Za-z0-9]","").substring(pos).startsWith("@") ) {
                                chanUserMode.replace( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") , chanUserMode.get( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") ) + "o");                           
                            }
                            else if ( sjoinParam[i].replaceFirst(":", "").replaceAll("[A-Za-z0-9]","").substring(pos).startsWith("~") ) {
                                chanUserMode.replace( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") , chanUserMode.get( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") ) + "a");
                            }
                            else if ( sjoinParam[i].replaceFirst(":", "").replaceAll("[A-Za-z0-9]","").substring(pos).startsWith("*") ) {
                                chanUserMode.replace( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") , chanUserMode.get( sjoinParam[i].replaceAll("[^A-Za-z0-9]","") ) + "q");
                            }
                            else {
                                
                            }
                            
                        }
                    }
                    //System.out.println("outside swiitch");

                    /*
                    if ( sjoinParam[i].startsWith(":")  ) {
                        
                        if (sjoinParam[i].split(":").length == 2) { // channel has at least 01 user

                            //System.out.println("xxx User1 for chan " + channelName + " = " + sjoinParam[i]);
                        }
                        else { // channel has no user
                            System.out.println("xxx No user for chan " + channelName );
                        }
                    }
                    else { // for the 2nd, 3rd... users
                        //for( String str :
                        if (sjoinParam[i].equals("")) {
                            // Do nothing because this is not an user
                        }
                        else {
                            //System.out.println("xxx User3 for chan " + channelName + " = " + sjoinParam[i]);
                            //userList.get(sjoinParam[i]).addChan(channelName)
                        }
                    }*/
                }
            }

            // Finish populate the hashmap with remaining modes
            for(char m : chanModeWithOutParams) {
                chanModeList.put( String.valueOf(m) , "");
            }

            ChannelNode chan = new ChannelNode( channelName, channelTS, chanModeList, chanBanList, chanExceptList, chanInviteList );
            
            channelList.put(channelName, chan);
            //System.out.println("chanUserModeSize " + channelName + " = " + user + " -> " + modes);
            chanUserMode.forEach( (user, modes) -> {
                //System.out.println("chanUserMode " + channelName + " = " + user + " -> " + modes);
                if (! user.equals("")) { userList.get(user).addUserToChan(channelName, channelList.get(channelName), modes); }
            });

            /*for(String str: chanBanList) {
                System.out.println("banList item " + channelName + " = " + str);
                chan.addBanList(str);
                
            }

            for(String str: chanExceptList) {
                chan.addExceptList(str);
                //System.out.println("exemptList item " + channelName + " = " + str);
            }

            for(String str: chanInviteList) {
                chan.addInviteList(str);
                //System.out.println("inviteList item " + channelName + " = " + str);
            }*/

            //System.out.println("CCC new chan " + channelName + " " + channelTS + " " + chanModeList);
        }

        else {
            if (command[0].equals("PING")) {
                response = "PONG " + command[1];
                write(client, response);
            }
        }
    }
}
