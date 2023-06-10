import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * UserNode class to store the connected users.
 * @author me
 */
public class UserNode {

    private static Logger log = LogManager.getLogger("common-log");

    
    private String userNick       = "";
    private String userOldNick    = "";
    private String userIdent      = "";
    private String userHost       = "";
    private String userRealHost   = "";
    private String cloakedHost    = "";
    private String userRealName   = "";
    private String userUniq       = "";
    private String userModes      = "";
    private String userCertFP     = "";

    private byte[] ipAddress;

    private Boolean usingSaslAuth       = false;
    private Boolean userAuthed          = false;
    private Boolean userNickRegistered  = false;

    private HashMap<String, String> saslAuthParams = new HashMap<>();

    private ServerNode userServer;

    private UserAccount userAccount;

    private Long userTS;
    private Long authTS;

    private Map<String, ChannelNode>  userChanList   = new HashMap<String, ChannelNode>();
    private Map<String, String>       userChanModes  = new HashMap<String, String>();

    private UUID authSessUUID;


    /**
     * User object contains information for connected users
     * @param userNick User nickname
     * @param userIdent User ident
     * @param userHost User hostname/vhost
     * @param userRealHost User real host
     * @param userRealName User real name (gecos)
     * @param userUniq User SID
     * @param userTS User timestamp
     * @param userModes User modes
     */
    public UserNode(String userNick,     String userIdent,    String userHost,
                    String userRealHost, String userRealName, String userUniq,
                    long userTS,         String userModes) {
                        
        this.userNick = userNick;
        this.userIdent = userIdent;
        this.userRealHost = userRealHost;
        if (userHost == null) {
            this.userHost = userRealHost;
        }
        else {
            this.userHost = userHost;
        }
        this.userRealName = userRealName;
        this.userUniq = userUniq;
        this.userTS = userTS;
        this.userModes = userModes;
        
    } 

    /**
     * Constructor used during SASL handshake
     * @param uid
     * @param hostname
     */
    public UserNode(String uid, String hostname) {
        this.userUniq = uid;
        this.userRealHost = hostname;
        this.usingSaslAuth = true;
        this.userTS = 0L;
    }

    /**
     * Sets the user nickname
     * @param nick User nick
     */
    public void setNick(String nick) {
        this.userOldNick = this.userNick;
        this.userNick = nick;
    }

    /**
     * Sets the user ident
     * @param ident User ident
     */
    public void setIdent(String ident) {
        this.userIdent = ident;
    }

    /**
     * Sets the user (v)host
     * @param host User (v)host
     */
    public void setHost(String host) {
        this.userHost = host;
    }

    /**
     * Sets the user real host
     * @param rhost User real host
     */
    public void setRealHost(String rhost) {
        this.userRealHost = rhost;
    }

    /**
     * Sets the user real name (gecos)
     * @param realName User gecos
     */
    public void setRealName(String realName) {
        this.userRealName = realName;
    }

    /**
     * Sets the user SID
     * @param uniq User SID
     */
    public void setUid(String uniq) {
        this.userUniq = uniq;
    }

    /**
     * Sets the user usermodes
     * @param modes User usermodes
     */
    public void setUserModes(String modes) {
        this.userModes = modes;
    }

    /**
     * Sets the user server
     * @param server User server
     */
    public void setServer(ServerNode server) {
        this.userServer = server;
    }

    /**
     * Sets the user certificate fingerprint
     * @param certfp User certfp
     */
    public void setCertFP(String certfp) {
        this.userCertFP = certfp.toLowerCase(); // putting lowercased certfp to node
    }

    /**
     * Sets the user account for the user node
     * @param account user account object
     */
    public void setAccount(UserAccount account) {
        if (this.userAuthed == true) {
            if (account != null) {
                this.userAccount = account;
                try { this.userAccount.addUserAuth(this); }
                catch (Exception e) { e.printStackTrace(); log.error("(EE) Could not auth user."); }
            }
            else {
                try { this.userAccount.delUserAuth(this); }
                catch (Exception e) { e.printStackTrace(); log.error("(EE) Could not de-auth user."); }   
                this.userAccount = null;
            }
        }
    }

    /**
     * Adds the user to the channel
     * @param channel Channel name
     * @param chanObj Channel object
     * @param mode User mode on the channel
     */
    public void addUserToChan(String channel, ChannelNode chanObj, String mode) /*throws Exception*/ {
        userChanList.put(channel, chanObj);
        userChanModes.put(channel, mode);
    }

    /**
     * Removes the user from the channel
     * @param channel Channel name
     */
    public void delUserFromChan(String channel) /*throws Exception*/ {
        userChanList.remove(channel);
        userChanModes.remove(channel);
    }

    /**
     * Fetches the user modes on the channel
     * @param chan Channel name
     * @return User channel modes
     */
    public String getUserChanMode(String chan) {
        return this.userChanModes.get(chan);
    }

    /**
     * Adds the user channel mode to the channel
     * @param chan Channel name
     * @param modes User channel modes
     */
    public void addUserChanMode(String chan, String modes) {
        this.userChanModes.replace(chan, sortString(removeDuplicate(this.userChanModes.get(chan) + modes)));
    }

    /**
     * Removes the user channel mode to the channel
     * @param chan Channel name
     * @param modes User channel modes
     */
    public void delUserChanMode(String chan, String modes) {
        this.userChanModes.replace(chan, this.userChanModes.get(chan).replaceAll("[" + modes + "]", ""));
    }

    /**
     * Sets the user auth status
     * @param state User auth status
     */
    public void setUserAuthed(Boolean state) {
        this.userAuthed = state;

        if (state == true) {
            UUID uuid = UUID.randomUUID();

            this.authTS = Instant.now().getEpochSecond();
            this.authSessUUID = uuid;
        }
        else {
            this.authTS = null;
            this.authSessUUID = null;
        }

    }

    /**
     * Sets the registration status of the user's nickname
     * @param state User nickname registration status
     */
    public void setUserNickRegistered(Boolean state) {
        this.userNickRegistered = state;
    }

    /**
     * Sets the user timestamp
     * @param userTS User timestamp
     */
    public void setUserTS(Long userTS) {
        this.userTS = userTS;
    }

    /**
     * Fetches the user nickname
     * @return User nickname
     */
    public String getNick() {
        return this.userNick;
    }

    /**
     * Fetches the user previous nickname
     * @return Previous user nickname
     */
    public String getOldNick() {
        return this.userOldNick;
    }

    /**
     * Fetches the user ident
     * @return User ident
     */
    public String getIdent() {
        return this.userIdent;
    }

    /**
     * Fetches the user (v)host
     * @return User (v)host
     */
    public String getHost() {
        return this.userHost;
    }

    /**
     * Fetches the user real host
     * @return User realhost
     */
    public String getRealHost() {
        return this.userRealHost;
    }

    /**
     * Fetches the user real name (gecos)
     * @return User gecos
     */
    public String getRealName() {
        return this.userRealName;
    }

    /**
     * Fetches the user SID
     * @return User SID
     */
    public String getUid() {
        return this.userUniq;
    }

    /**
     * Fetches the user usermodes
     * @return User usermodes
     */
    public String getModes() {
        return this.userModes;
    }

    /**
     * Fetches the user server SID
     * @return User server SID
     */
    public ServerNode getServer() {
        return this.userServer;
    }

    /**
     * Fetches the user certificate fingerprint
     * @return User certfp
     */
    public String getCertFP() {
        return this.userCertFP;
    }

    /**
     * Fetches the account name the user is authed as
     * @return User account name
     */
    public UserAccount getAccount() {
        return this.userAccount;
    }

    /**
     * Fetches the list of channels the user is in
     * @return List of the user channels
     */
    public Map<String, ChannelNode> getUserChanList() {
        return this.userChanList;
    }

    /**
     * Fetches the list of the user's modes on the channels they are in
     * @return Modes of user in their channels
     */
    public Map<String, String> getUserChanModes() {
        return this.userChanModes;
    }

    /**
     * Fetches whether the user is authed
     * @return User auth status
     */
    public Boolean isAuthed() {
        return this.userAuthed;
    }

    /**
     * Fetches whether the user's nick is registered
     * @return User nick registration status
     */
    public Boolean isNickRegistered() {
        return this.userNickRegistered;
    }

    /**
     * Fetches the user timestamp
     * @return User timestamp
     */
    public Long getUserTS() {
        return this.userTS;
    }

    /**
     * Fetches whether the user is an oper
     * @return User oper status
     */
    public Boolean isOper() {
        if (this.userModes.matches("(.*)o(.*)") == true) return true;
        else return false;
    }

    /**
     * Removes duplicate chars in a string
     * @param s Input string
     * @return String with unique chars
     */
    private static String removeDuplicate(String s) {

        char[] chars = s.toCharArray();
        Set<Character> charSet = new LinkedHashSet<Character>();
        for (char c : chars) {
            charSet.add(c);
        }

        StringBuilder sb = new StringBuilder();
        for (Character character : charSet) {
            sb.append(character);
        }
        return sb.toString();

    }

    /**
     * Sort a string alphabetically
     * @param str Input string
     * @return Sorted string
     */
    private static String sortString(String str) {
        char charArray[] = str.toCharArray();
        Arrays.sort(charArray);
        return new String(charArray);
    }
    
    public UUID getAuthUuid() {
        return this.authSessUUID;
    }

    public Long getAuthTS() {
        return this.authTS;
    }

    public void setSaslAuthParam(String param, String value) {
        this.saslAuthParams.put(param, value);
    }

    public String getSaslAuthParam(String param) {
        try {
            return this.saslAuthParams.get(param);
        }
        catch (NullPointerException e) { e.printStackTrace(); return null; }
    }

    public Boolean getAuthBySasl() {
        return this.usingSaslAuth;
    }

    public void setCloakedHost(String clkdHost) {
        this.cloakedHost = clkdHost;
    }

    public String getCloakedHost() {
        return this.cloakedHost;
    }

    public byte[] getIpAddress() {
        return this.ipAddress;
    }


    public void setIpAddress(String base64Ip) {
        Base64.Decoder dec = Base64.getDecoder();
        try {
            this.ipAddress = dec.decode(base64Ip);
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("(EE) Could not set the IP of client: " + this.getNick());
        }
    }

    public void setIpAddress(byte[] ip) {
        this.ipAddress = ip;
    }

}
