import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.time.Instant;
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

    
    private String nick          = "";
    private String previousNick  = "";
    private String ident         = "";
    private String host          = "";
    private String realHost      = "";
    private String cloakedHost   = "";
    private String realName      = "";
    private String uid           = "";
    private String modes         = "";
    private String certFp        = "";

    private byte[] ipAddress;

    private Boolean usingSaslAuth       = false;
    private Boolean userAuthed          = false;
    private Boolean userNickRegistered  = false;
    private Boolean connPlainText       = false;

    private HashMap<String, String> saslAuthParams = new HashMap<>();

    private ServerNode userServer;

    private UserAccount userAccount;

    private Long userTS;
    private Long authTS;

    /**
     * HashMap contains:
     *  - channel where the is inside
     *  - mode the user has inside the channel
     */
    private HashMap<ChannelNode, String>     userChanList  = new HashMap<>();


    private UUID authSessUUID;


    public UserNode(String uid) {
        this.uid = uid;
    }

    /**
     * Constructor used during SASL handshake
     * @param uid
     * @param hostname
     */
    public UserNode(String uid, String hostname) {
        this.uid = uid;
        this.realHost = hostname;
        this.usingSaslAuth = true;
        this.userTS = 0L;
    }

    /**
     * Sets the user nickname
     * @param nick User nick
     */
    public void setNick(String nick) {
        this.previousNick = this.nick;
        this.nick = nick;
    }

    /**
     * Sets the user ident
     * @param ident User ident
     */
    public void setIdent(String ident) {
        this.ident = ident;
    }

    /**
     * Sets the user (v)host
     * @param host User (v)host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Sets the user real host
     * @param rhost User real host
     */
    public void setRealHost(String rhost) {
        this.realHost = rhost;
    }

    /**
     * Sets the user real name (gecos)
     * @param realName User gecos
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * Sets the user SID
     * @param uniq User SID
     */
    public void setUid(String uniq) {
        this.uid = uniq;
    }

    /**
     * Sets the user usermodes
     * @param modes User usermodes
     */
    public void setModes(String modes) {
        this.modes = modes;
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
        this.certFp = certfp.toLowerCase(); // putting lowercased certfp to node
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
                catch (Exception e) { log.error(String.format("UserNode/setAccount: Could not auth nick %s to account %s:", this.getNick(), this.userAccount.getName()), e); }
            }
            else {
                try { this.userAccount.delUserAuth(this); }
                catch (Exception e) { log.error(String.format("UserNode/setAccount: Could not deauth nick %s from account %s:", this.getNick(), this.userAccount.getName()), e); }   
                this.userAccount = null;
            }
        }
    }

    public void setConnPlainText(Boolean plain) {
        this.connPlainText = plain;
    }

    public Boolean getConnPlainText() {
        return this.connPlainText;
    }

    /**
     * Adds the user to the channel
     * @param channel Channel name
     * @param chanObj Channel object
     * @param mode User mode on the channel
     */
    public void addToChan(ChannelNode chanObj, String mode) throws Exception {
        if (userChanList.containsKey(chanObj) == true) {
            log.error("UserNode/addUserToChan: user " + this.getNick() + " is already in the chan");
            throw new Exception("UserNode/addUserToChan: user " + this.getNick() + " is already in the chan");
        }

        chanObj.addUser(this);
        userChanList.put(chanObj, mode);
    }

    public Boolean isUserOnChan(ChannelNode chan) {
        if (this.userChanList.containsKey(chan)) {
            return true;
        }
        else return false;
    }

    public void removeFromChan(ChannelNode chanObj) throws Exception {
        if (userChanList.containsKey(chanObj) == false) {
            log.error("UserNode/removeUserFromChan: user " + this.getNick() + " is not in the chan");
            throw new Exception("UserNode/removeUserFromChan: user " + this.getNick() + " is not in the chan");
        }

        chanObj.removeUser(this);
        userChanList.remove(chanObj);
    }

    public void addUserModeChan(ChannelNode chanObj, String mode) throws Exception {
        if (userChanList.containsKey(chanObj) == false) {
            log.error("UserNode/addUserModeChan: user " + this.getNick() + " is not in the chan");
            throw new Exception("UserNode/addUserModeChan: user " + this.getNick() + " is not in the chan");
        }

        String curUserChanMode = "";
        String newUserChanMode = "";

        curUserChanMode = userChanList.get(chanObj);
        newUserChanMode = curUserChanMode + mode;
        newUserChanMode = removeDuplicate(newUserChanMode);

        userChanList.replace(chanObj, newUserChanMode);
    }

    public void removeUserModeChan(ChannelNode chanObj, String mode) throws Exception {
        if (userChanList.containsKey(chanObj) == false) {
            log.error("UserNode/addUserModeChan: user is not in the chan");
            throw new Exception("UserNode/addUserModeChan: user is not in the chan");
        }

        String curUserChanMode = "";
        String newUserChanMode = "";
        
        curUserChanMode = userChanList.get(chanObj);
        newUserChanMode = curUserChanMode.replaceAll("[" + mode + "]", "");

        userChanList.replace(chanObj, newUserChanMode);
    }

    /**
     * Fetches the user modes on the channel
     * @param chan Channel name
     * @return User channel modes
     */
    public String getChanList(ChannelNode chan) {
        return this.userChanList.get(chan);
    }

    /**
     * Fetches the list of channels the user is in
     * @return List of the user channels
     */
    public HashMap<ChannelNode, String> getChanList() {
        return this.userChanList;
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
        return this.nick;
    }

    /**
     * Fetches the user previous nickname
     * @return Previous user nickname
     */
    public String getOldNick() {
        return this.previousNick;
    }

    /**
     * Fetches the user ident
     * @return User ident
     */
    public String getIdent() {
        return this.ident;
    }

    /**
     * Fetches the user (v)host
     * @return User (v)host
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Fetches the user real host
     * @return User realhost
     */
    public String getRealHost() {
        return this.realHost;
    }

    /**
     * Fetches the user real name (gecos)
     * @return User gecos
     */
    public String getRealName() {
        return this.realName;
    }

    /**
     * Fetches the user SID
     * @return User SID
     */
    public String getUid() {
        return this.uid;
    }

    /**
     * Fetches the user usermodes
     * @return User usermodes
     */
    public String getModes() {
        return this.modes;
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
        return this.certFp;
    }

    /**
     * Fetches the account name the user is authed as
     * @return User account name
     */
    public UserAccount getAccount() {
        return this.userAccount;
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
        if (this.modes.matches("(.*)o(.*)") == true) return true;
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
        catch (NullPointerException e) { log.error(String.format("UserNode/getSaslAuthParam: error fetching the SASL auth parameters") , e); return null; }
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

    public byte[] getIpAddressByte() {
        return this.ipAddress;
    }

    public String getIpAddressAsString() {
        String ip = "";
        switch (this.ipAddress.length) {
            case 4:
                ip = String.format("%d.%d.%d.%d", ipAddress[0] & 0xff, ipAddress[1] & 0xff, ipAddress[2] & 0xff, ipAddress[3] & 0xff); /*  &0xff converts to unsigned int */
                return ip;

            case 16:
                ip = String.format("%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x",
                     ipAddress[0], ipAddress[1], ipAddress[2], ipAddress[3], ipAddress[4], ipAddress[5], ipAddress[6], ipAddress[7], 
                     ipAddress[8], ipAddress[9], ipAddress[10], ipAddress[11], ipAddress[12], ipAddress[13], ipAddress[14], ipAddress[15]);
                return ip;
            
            default: return ip;
        }

    }

    public String getIpAddressBase64() {
        String ipEncoded = "";
        Base64.Encoder enc = Base64.getEncoder();
        try {
            ipEncoded = enc.encodeToString(this.ipAddress);
        }
        catch (Exception e) { log.error("UserNode/getIpAddressBase64: Could not encode client IP to base64: " + this.getNick() + ".", e); }

        return ipEncoded;
    }


    public void setIpAddress(String base64Ip) {
        Base64.Decoder dec = Base64.getDecoder();
        try {
            this.ipAddress = dec.decode(base64Ip);
        }
        catch (Exception e) { log.error("UserNode/setIpAddress: Could not set the IP of client: " + this.getNick() + ": ", e); }
    }

    public void setIpAddress(byte[] ip) {
        this.ipAddress = ip;
    }

    public String getMask(String type) {
        switch (type) {
            case "nick!ident@realhost (ip)": return String.format("%s!%s@%s (%s)", nick, ident, host, "");
            case "nick!ident@realhost": return String.format("%s!%s@%s", nick, ident, host);
            case "ident@realhost (ip)": return String.format("%s@%s (%s)", ident, host, "");
            case "ident@realhost": return String.format("%s@%s", ident, host);
            default: return "0!0@0";
        }

    }

    /**
     * Returns the user mask as NICK!IDENT@REALHOST (IP)
     * @return NICK!IDENT@REALHOST (IP)
     */    
    public String getMask1() {
        return String.format("%s!%s@%s (%s)", nick, ident, host, "");
    }

    /**
     * Returns the user mask as IDENT@REALHOST (IP)
     * @return IDENT@REALHOST (IP)
     */
    public String getMask2() {
        return String.format("%s@%s (%s)", ident, host, "");
    }

    /**
     * Returns the user mask as NICK!IDENT@REALHOST
     * @return NICK!IDENT@REALHOST
     */    
    public String getMask3() {
        return String.format("%s!%s@%s", nick, ident, host);
    }

    /**
     * Returns the user mask as IDENT@REALHOST
     * @return IDENT@REALHOST
     */
    public String getMask4() {
        return String.format("%s@%s", ident, host);
    }

}
