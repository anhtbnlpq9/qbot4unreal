
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.time.Instant;


public class Protocol extends Exception {
    
    private Client client;
    private Config config;
    private ServerNode server;
    private CService cservice;
    private SqliteDb sqliteDb;
    
    private Map<String, ServerNode> serverList = new HashMap<String, ServerNode>();
    private Map<String, UserNode> userList = new HashMap<String, UserNode>();
    private Map<String, ChannelNode> channelList = new HashMap<String, ChannelNode>();
    private Map<String, String> userNickSidLookup = new HashMap<String, String>(); // Lookup map for Nick -> Sid

    private Map<String, String> protocolProps = new HashMap<String, String>();
    
    String myPeerServerId;
    long unixTime;
    String foundNickLookUpCi;

    public Protocol() {
    }  
    public Protocol(Config config, SqliteDb sqliteDb) {
        this.config = config;
        this.sqliteDb = sqliteDb;
    }

    public void addNickLookupTable(String nick, String sid) {
        userNickSidLookup.put(nick, sid);
    }
    public void delNickLookupTable(String nick) {
        userNickSidLookup.remove(nick);
    }
    public String getNickLookupTable(String nick) {
        return userNickSidLookup.get(nick);
    }
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
        int chanUserCount=0;

        if (channelList.containsKey(chan)) {
            chanUserCount = channelList.get(chan).getChanUserCount();
        }
        else {
            unixTime = Instant.now().getEpochSecond();
            ChannelNode newChannel = new ChannelNode(chan, unixTime);
            channelList.put(chan, newChannel);
        }

        userList.get(who).addUserToChan(chan, channelList.get(chan), "");
        try {
            channelList.get(chan).setChanChanlev(sqliteDb.getChanChanlev(chan));
        }
        catch (Exception e) { e.printStackTrace(); return; }

        channelList.get(chan).setChanUserCount(chanUserCount+1);
        client.write(str);
    }
    public void chanPart(Client client, String who, String chan) /*throws Exception*/ {
        String str = ":" + who + " PART " + chan;

        ChannelNode chanUserPart = channelList.get(chan);
        userList.get(who).delUserFromChan(chan);

        int chanUserCount = chanUserPart.getChanUserCount();

        if (chanUserCount == 1 && ! chanUserPart.getModes().containsKey("P") ) {
            chanUserPart = null;
            channelList.remove( chan );
        }
        else {
            chanUserPart.setChanUserCount(chanUserCount - 1);
        }

        client.write(str);
    }
    public void chanKick(Client client, String who, String chan, String target, String reason) /*throws Exception*/ {
        String str = ":" + who + " KICK " + chan + " " + target + " :" + reason;
        
        ChannelNode chanUserPart = channelList.get(chan);
        userList.get(who).delUserFromChan(chan);

        int chanUserCount = chanUserPart.getChanUserCount();

        
        if (chanUserCount == 1 && chanUserPart.getModes().containsKey("P") == false ) {
            chanUserPart = null;
            channelList.remove( chan );
        }
        else {
            chanUserPart.setChanUserCount(chanUserCount - 1);
        }
        client.write(str);
    }
    public void setMode(Client client, String who, String target, String modes, String parameters) throws Exception {
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

        String str = ":" + who + " MODE " + target + " " + modes + " " + parameters;

        
        userList.forEach( (userSid, user) -> { userNickSidLookup.put(user.getUserNick(), userSid); });

        if (modes.replaceFirst("[^A-za-z0-9]", "").matches("[" + networkChanUserModes + "]")) {
            if(modes.startsWith("+")) {
                userList.get(userNickSidLookup.get(parameters)).addUserChanMode(target, modes.replaceFirst("[^A-za-z0-9]", ""));
            }
            else if(modes.startsWith("-")) {
                userList.get(userNickSidLookup.get(parameters)).delUserChanMode(target, modes.replaceFirst("[^A-za-z0-9]", ""));
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
    public void setMode(Client client, String target, String modes, String parameters) throws Exception {
        setMode(client, config.getServerId(), target, modes, parameters);
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
        
        Integer chanUserCount;
        
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

            String str = ":" + config.getServerId() + " EOS";
            client.write(str);
            serverList.get(config.getServerId()).setEOS(true);
        }
        else if (command[0].equals("NETINFO")) {
            //:ABC NETINFO 13        1683227483  6000                   SHA256:06aa55fd33c824d6132b0aebc1da0cd0e253473f68391a5ace8cf0bd 0 0 0 :Mjav
            //     NETINFO maxglobal currenttime protocolversion        cloakhash                                                       0 0 0 :networkname
            //     NETINFO xx        unixTime    config.protocolversion *                                                               0 0 0 :config.netName

            String[] netinfoParam = raw.split(" ", 20);
            //command = (command[1]).split(" ", 4);

            unixTime = Instant.now().getEpochSecond();

            String str = ":" + config.getServerId() + " NETINFO " + netinfoParam[1] + " " + unixTime + " " + config.getSrvProtocolVersion() + " * 0 0 0 :" + config.getNetworkName();
            client.write(str);

            //:ABC MD client lynx.      saslmechlist :EXTERNAL,PLAIN
            //:ABC MD client <nick|uid> <varname>    <value>
            if (config.getFeature("sasl") == true) {
                str = ":" + config.getServerId() + " MD client " + config.getServerName() + " saslmechlist :EXTERNAL,PLAIN";
                client.write(str);
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
            // :XXXX UID nickname hopcount timestamp username hostname uid servicestamp usermodes virtualhost cloakedhost ip :gecos

            fromEnt = (command[0].split(":"))[1];
            
            command = command[2].split(" ", 12);
            
            
        else if (command[1].equals("SASL")) {
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
            */

            fromEnt = command[0].replaceFirst(":","");
            command = command[2].split(" ", 12);

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
             *    Sent " C " from authenticator SID (e.g. Q) to user's server
             * command[0] = user's server SID
             * command[1] = User SID
             * command[2] = C
             * command[3] = +
             */

            /*
             *    Received " C " from user's server
             * command[0] = SASL server
             * command[1] = User SID
             * command[2] = C
             * command[3] = Base64 hash of <LoginLoginPass>
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
             * command[0] = user's server SID
             * command[1] = User SID
             * command[2] = User login
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
            userNickSidLookup.put(command[0], command[5]);








        }
        else if (command[1].equals("SJOIN")) {
            // :5P0 SJOIN 1680362593 #mjav         +fnrtCPST [5j#R1,7m#M1,3n#N1,5t#b1]:6         :5PKEPJH3U @5PXDR1D20 @5P0FWO841 
            // :5P0 SJOIN 1681424518 #Civilization +fnrtCHPS [30j#R10,40m#M10,10n#N15]:15 50:15m :@5PX8ZA302 @5PBAAAAAI &test!*@* "test!*@* 'test!*@*
            // :5PX SJOIN 1679224907 #test                                                       :5PX8ZA302
            // :5PX SJOIN 1683480448 #newChan                                                    :@5PX8ZA302

            //System.out.println("DDD SJOIN " + command[2]);

            fromEnt = (command[0].split(":"))[1];

            String[] sjoinParam      = command[2].split(" ", 64);
            int      sjoinParamCount = command[2].split(" ", 64).length;

            int indexFirstUser=0;
            int indexMode = 0;

            long channelTS = Integer.parseInt(sjoinParam[0]);

            String networkChanmodesWithOutParams = ((protocolProps.get("CHANMODES")).split(",", 4))[3];
            String networkChanmodesWithParams    = ((protocolProps.get("CHANMODES")).split(",", 4))[0] + ((protocolProps.get("CHANMODES")).split(",", 4))[1] + ((protocolProps.get("CHANMODES")).split(",", 4))[2];
            
            String channelName = sjoinParam[1];

            String chanModeRaw = "";              // Contains the modes of the channel (without the params)

            char[] chanModeWithParams;            // Contains the list of channel modes that allows params (arrayed)
            String chanModeRawWithParams = "";    // Contains the list of channel modes that allows params

            char[] chanModeWithOutParams;         // Contains the list of channel modes that don't allow params (arrayed)
            String chanModeRawWithOutParams = ""; // Contains the list of channel modes that don't allow params

            if (sjoinParam[2].startsWith("+")) { // Case when SJOIN contains modes (syncing from network)
                chanModeRaw              = (sjoinParam[2].split("\\+", 2))[1];                                     // Contains all the channel modes
            }

            chanModeRawWithParams    = chanModeRaw.replaceAll("["+ networkChanmodesWithOutParams + "]", "");   // Contains only channel modes used with parameter
            chanModeRawWithOutParams = chanModeRaw.replaceAll("["+ networkChanmodesWithParams + "]", "");      // Contains only channel modes used withOut parameter

            //chanMode                 = chanModeRaw.toCharArray();                                              // Contains all the channel ['m','o','d','e','s']
            chanModeWithParams       = chanModeRawWithParams.toCharArray();                                    // Contains only channel ['m','o','d','e','s'] used with parameter
            chanModeWithOutParams    = chanModeRawWithOutParams.toCharArray();                                 // Contains only channel ['m','o','d','e','s'] used withOut parameter


            ArrayList<String> chanBanList = new ArrayList<String>();
            ArrayList<String> chanExceptList = new ArrayList<String>();
            ArrayList<String> chanInviteList = new ArrayList<String>();
            
            Map<String, String> chanUserMode = new HashMap<String, String>();
            Map<String, String> chanModeList = new HashMap<String, String>();

            // Now we need to determine index of modes and index of users in the string
            for (int i=1; i < sjoinParamCount; i++) {
                
                // detect first user
                if (sjoinParam[i].startsWith(":")) { indexFirstUser=i; }
                
                if (indexFirstUser == 0) { // first user not detected => we are still before the users list
                    if (sjoinParam[2].startsWith("+") && i >= 3) {
                        // Populate the hashmap with modes with parameters
                        chanModeList.put( String.valueOf(chanModeWithParams[indexMode]) , sjoinParam[i]);
                        indexMode++;
                    }
                }
                else {  // user section detected

                    if (sjoinParam[i].equals("")) {
                        // Do nothing because this is not an user
                        break;
                    }
                    else {

                        // Handle the ban/except/invite lists
                        if ( sjoinParam[i].replaceFirst(":", "").startsWith("&") || sjoinParam[i].replaceFirst(":", "").startsWith("\"") || sjoinParam[i].replaceFirst(":", "").startsWith("'") ) {

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
                                else { // when the user joins without any mode => send to CServe
                                    
                                }
                            }
                        }
                    }
                }
            }
            if (sjoinParam[2].startsWith("+")) { // Case when SJOIN contains modes (syncing from network)     
                // Populate the hashmap with remaining modes
                for(char m : chanModeWithOutParams) {
                    chanModeList.put( String.valueOf(m) , "");
                }
            }
        
            //chanUserMode.forEach( (user, mode) -> { System.out.println("BDA chan=" + channelName + " user=" + user + " -> mode=" + mode);  } );

            chanUserMode.remove("");

            //chanUserCount = chanUserMode.size();

            //chanUserMode.forEach( (key, value) -> { System.out.println("BBN chanUserMode chan=" + channelName + " " + userList.get(key).getUserNick() + " -> " + value); });

            if (chanUserMode.size() == 0) { chanUserCount = 0; }
            else { chanUserCount = chanUserMode.size(); }

            //System.out.println("BBO chanUserCount chan="+ channelName + " count=" + chanUserCount);

            if ( ! channelList.containsKey(channelName) ) {
            
                ChannelNode chan = new ChannelNode( channelName, channelTS, chanModeList, chanBanList, chanExceptList, chanInviteList );

                channelList.put(channelName, chan);
                channelList.get(channelName).setChanUserCount(chanUserCount);
                channelList.get(channelName).setChanChanlev(sqliteDb.getChanChanlev(channelName));
                //System.out.println("BBP chanUserCount newchan="+ channelName + " count=" + chanUserCount);
            }
            else {
                if(channelList.get(channelName).getChanUserCount() > 0) { 
                    //System.out.println("BBZ here");
                    channelList.get(channelName).setChanUserCount(channelList.get(channelName).getChanUserCount()+1); 
                }
                else { channelList.get(channelName).setChanUserCount(chanUserCount); }
            }

            chanUserMode.forEach( (user, modes) -> {
                //System.out.println("chanUserMode " + channelName + " = " + user + " -> " + modes);
                if (! user.equals("")) {
                    userList.get(user).addUserToChan(channelName, channelList.get(channelName), modes);
                }
                if (modes.isEmpty()) {
                    try {
                        cservice.handleJoin(userList.get(user), channelList.get(channelName));
                    }
                    catch (Exception e) { /* System.out.println("* Ignoring CServe JOIN command as CServe not connected yet."); */ }
                }
            });
            
        }
        else if (command[1].equals("MODE")) {
            // :5PX     MODE  #newChan      +ntCT         1683480448
            // :XXXXXXX MODE  #Civilization +o AnhTay

            String[] modeList      = command[2].split(" ", 128);

            String channelName = modeList[0];
            
            ChannelNode chan = channelList.get(channelName);

            String networkChanUserModes          = protocolProps.get("PREFIX").replaceAll("[^A-Za-z0-9]", "");

            /*
             * CHANMODES=beI,fkL,lFH,cdimnprstzCDGKMNOPQRSTVZ
             *            |   |    |           `----------------------- group4: no parameter
             *            |   |     `---------------------------------- group3: parameter for set, no parameter for unset
             *            |    `--------------------------------------- group2: parameter for set, parameter for unset
             *             `------------------------------------------- group1: (list) parameter for set, parameter for unset
             */
        
            String networkChanModesGroup1        = ((protocolProps.get("CHANMODES")).split(",", 4))[0]; // (list) parameter for set, parameter for unset
            String networkChanModesGroup2        = ((protocolProps.get("CHANMODES")).split(",", 4))[1]; // parameter for set, parameter for unset
            String networkChanModesGroup3        = ((protocolProps.get("CHANMODES")).split(",", 4))[2]; // parameter for set, no parameter for unset
            String networkChanModesGroup4        = ((protocolProps.get("CHANMODES")).split(",", 4))[3]; // no parameter

            

            String chanModeRaw;              // Contains the modes of the channel (without the params)
            chanModeRaw              = modeList[1];                                     // Contains all the channel modes




            int indexMode;
            int indexParam;
            boolean plusMode = false;
            indexParam = 2; // indexParam begins at '2' because 1st param == modeList[2]

            Map<String, String> userNickSidLookup = new HashMap<String, String>(); // Lookup map for Nick -> Sid
            userList.forEach( (userSid, user) -> { userNickSidLookup.put(user.getUserNick(), userSid); });
            
            for (indexMode = 0; indexMode < chanModeRaw.length() ; indexMode++) {
                //System.out.println("MMM " + chanModeRaw.charAt(indexMode) );
                if (chanModeRaw.charAt(indexMode) == '+') {
                    plusMode = true;
                }
                else if (chanModeRaw.charAt(indexMode) == '-') {
                    plusMode = false;
                }
                else {

                    if (  String.valueOf(chanModeRaw.charAt(indexMode)).matches("["+ networkChanModesGroup1 + "]") == true) { // matches all the modes with params
                        
                        switch ( chanModeRaw.charAt(indexMode) ) {  // matches lists (b/e/I)
                            case 'b':
                                if (plusMode == true) {
                                    chan.addBanList( modeList[indexParam] ); 
                                    //System.out.println("MMO channel " + channelName + " mode +" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                                }
                                else { 
                                    chan.delBanList( modeList[indexParam] ); 
                                    //System.out.println("MMP channel " + channelName + " mode -" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                                }
                                break;

                            case 'e':
                                if (plusMode == true) { 
                                    chan.addExceptList( modeList[indexParam] ); 
                                    //System.out.println("MMQ channel " + channelName + " mode +" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                                }
                                else { 
                                    chan.delInviteList( modeList[indexParam] );
                                    //System.out.println("MMR channel " + channelName + " mode -" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                                 }
                                break;

                            case 'I':
                                if (plusMode == true) { 
                                    chan.addInviteList( modeList[indexParam] );
                                    //System.out.println("MMS channel " + channelName + " mode +" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                                 }
                                else { 
                                    chan.delInviteList( modeList[indexParam] );
                                    //System.out.println("MMT channel " + channelName + " mode -" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                                 }
                                break;
                            
                            default:
                                 throw new Exception("Should not happen!!");
                        }
                        indexParam++;
                    }
                        
                    else if (String.valueOf(chanModeRaw.charAt(indexMode)).matches("["+ networkChanModesGroup2 + "]") == true) { // matches modes (f/k/L) -> need parameter for both set and unset
                        
                        if (plusMode == true) {
                            chan.addMode(String.valueOf(chanModeRaw.charAt(indexMode)), modeList[indexParam]);
                            //System.out.println("MMA channel " + channelName + " mode +" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                         }
                        else { 
                            chan.delMode(String.valueOf(chanModeRaw.charAt(indexMode))); 
                            //System.out.println("MMB channel " + channelName + " mode -" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                        }
                        indexParam++;
                        
                    }

                    else if (String.valueOf(chanModeRaw.charAt(indexMode)).matches("["+ networkChanModesGroup3 + "]") == true) { // matches modes (l/F/H) -> need parameter ONLY for set
                        
                        if (plusMode == true) { 
                            chan.addMode(String.valueOf(chanModeRaw.charAt(indexMode)), modeList[indexParam]);
                            //System.out.println("MMC channel " + channelName + " mode +" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                            indexParam++;
                        }
                        else { 
                            chan.delMode(String.valueOf(chanModeRaw.charAt(indexMode))); 
                            //System.out.println("MMD channel " + channelName + " mode -" + String.valueOf(chanModeRaw.charAt(indexMode))); 
                        }
                        
                        
                    }

                    else if (String.valueOf(chanModeRaw.charAt(indexMode)).matches("["+ networkChanModesGroup4 + "]") == true) { // matches modes with no params (C/T/n/t/s/...)
                        
                        if (plusMode == true) { 
                            chan.addMode(String.valueOf(chanModeRaw.charAt(indexMode)), "");
                            //System.out.println("MME channel " + channelName + " mode +" + String.valueOf(chanModeRaw.charAt(indexMode)) ); 
                         }
                        else { 
                            chan.delMode(String.valueOf(chanModeRaw.charAt(indexMode)));
                            //System.out.println("MMF channel " + channelName + " mode -" + String.valueOf(chanModeRaw.charAt(indexMode)) ); 
                         }
                        
                    }


                    else if (String.valueOf(chanModeRaw.charAt(indexMode)).matches("["+ networkChanUserModes + "]") == true) { // matches user modes (q/a/o/h/v)
                        // It is necessary to lookup the user nick because mode is applied on a nick and not a SID

                        if (plusMode == true) {
                            userList.get(userNickSidLookup.get(modeList[indexParam])).addUserChanMode(channelName, String.valueOf(chanModeRaw.charAt(indexMode))); 
                            //System.out.println("MMG channel " + channelName + " user mode +" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                        }
                        else {
                            userList.get(userNickSidLookup.get(modeList[indexParam])).delUserChanMode(channelName, String.valueOf(chanModeRaw.charAt(indexMode))); 
                            //System.out.println("MMH channel " + channelName + " user mode -" + String.valueOf(chanModeRaw.charAt(indexMode)) + " " + modeList[indexParam]); 
                            if (userList.get(userNickSidLookup.get(modeList[indexParam])).getUserNick().equals(config.getCServeNick()) && chanModeRaw.charAt(indexMode) == 'o') {
                                this.setMode(client, channelName, "+o", config.getCServeNick());
                            }
                        }
                        indexParam++;
                        
                    }
                    else {
                        throw new Exception("Mode not defined in PROTOCTL.");
                    }


                }
                
                
            }


        }
        else if (command[1].equals("PART")) {
            // :XXXXXXXXX PART #1 :message

            //System.out.println("DDD PART " + command[2]);

            fromEnt = (command[0].split(":"))[1];

            ChannelNode chanUserPart = channelList.get((command[2].split(" "))[0]);
            
            userList.get(fromEnt).delUserFromChan( (command[2].split(" "))[0] );
            chanUserCount = chanUserPart.getChanUserCount();
            //System.out.println("DDE chan=" + chanUserPart.getChanName() + " mode=" + chanUserPart.getModes().containsKey("P"));
            
            if (chanUserCount == 1 && ! chanUserPart.getModes().containsKey("P") ) {
                chanUserPart = null;
                channelList.remove( (command[2].split(" "))[0] );
            }
            else {
                chanUserPart.setChanUserCount(chanUserCount - 1);
            }
        }
        else if (command[1].equals("KICK")) {
            // :XXXXXXXXX KICK #1 SID :message

            //System.out.println("DDD KICK " + command[2]);

            fromEnt = (command[0].split(":"))[1];

            ChannelNode chanUserPart = channelList.get((command[2].split(" "))[0]);
            
            userList.get((command[2].split(" "))[1]).delUserFromChan( (command[2].split(" "))[0] );
            chanUserCount = chanUserPart.getChanUserCount();
            //System.out.println("DDE chan=" + chanUserPart.getChanName() + " mode=" + chanUserPart.getModes().containsKey("P"));
            
            if (chanUserCount == 1 && ! chanUserPart.getModes().containsKey("P") ) {
                chanUserPart = null;
                channelList.remove( (command[2].split(" "))[0] );
            }
            else {
                chanUserPart.setChanUserCount(chanUserCount - 1);
            }
        }
        else if (command[1].equals("QUIT")) {
            // :XXXXXXXXX QUIT :message

            //System.out.println("DDD QUIT " + command[2]);
            
            fromEnt = (command[0].split(":"))[1];
            
            UserNode userToRemove = userList.get(fromEnt);
            userNickSidLookup.remove(userToRemove.getUserNick());
            userToRemove = null;
            userList.remove(fromEnt);
        }
        else if (command[1].equals("NICK")) {
            // :XXXXXXXXX NICK ...

            //System.out.println("DDD NICK " + command[2]);

            fromEnt = (command[0].split(":"))[1];
            
            //String oldNick = userList.get(fromEnt).getUserNick();
            //userList.get(fromEnt).setOldNick(oldNick);
            userNickSidLookup.remove(userList.get(fromEnt).getUserNick());
            userNickSidLookup.put((command[2].split(" "))[0], fromEnt);

            userList.get(fromEnt).setUserNick( (command[2].split(" "))[0] );


        }

        else {
            if (command[0].equals("PING")) {
                response = "PONG " + command[1];
                write(client, response);
            }
        }
    }
}
