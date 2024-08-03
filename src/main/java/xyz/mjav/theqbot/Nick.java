package xyz.mjav.theqbot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xyz.mjav.theqbot.exceptions.ChannelNotFoundException;
import xyz.mjav.theqbot.exceptions.ItemExistsException;
import xyz.mjav.theqbot.exceptions.ItemNotFoundException;
import xyz.mjav.theqbot.exceptions.NickNotFoundException;
import xyz.mjav.theqbot.exceptions.UserNoAuthException;

/**
 * UserNode class to store the connected users.
 * @author me
 */
public class Nick {

    /* Static fields */
    private static Logger log = LogManager.getLogger("common-log");

    private static Set<Nick> userList          = new HashSet<>();

    private static Map<String, Nick>   userListByNick  = new HashMap<>();
    private static Map<String, Nick>    userListByUid  = new HashMap<>();

    private static Map<String, String> nickToUid = new HashMap<>();

    /* Instance fields */
    private String nick;
    private String ident;
    private String host;
    private String realHost;
    private String cloakedHost;
    private String realName;
    private String uid;
    private String certFp;
    private String operLogin;
    private String operClass;
    private String country;

    private byte[] ipAddress;

    private Boolean isUsingSaslAuth;
    private Boolean isAuthed;
    private Boolean isNickRegistered;
    private Boolean isConnPlainText;
    private Boolean isAccountPending;

    private Server userServer;

    private UserAccount userAccount = null;

    private Long userTS;
    private Long authTS;

    /** HashMap contains: channel -> user mode inside channel */
    //private Map<Channel, String> userChanList;

    private Set<String> securityGroups;
    private Map<Channel, Set<String>> chanList;

    private Map<String, String> saslAuthParams;
    private Map<String, String> userModes;

    private Map<Timestamp, String> nickHistory;
    //private List<String>
    //private Set<Channel> chanList;

    private UUID authSessUUID;


    /**
     * Builder class to build an UserNode object
     */
    public static class Builder {
        private String nick                          = "";
        private String ident                         = "";
        private String host                          = "";
        private String realHost                      = "";
        private String cloakedHost                   = "";
        private String realName                      = "";
        private String uid                           = "";
        private String certFp                        = "";
        private String operLogin                     = "";
        private String operClass                     = "no oper class";
        private String country                       = "";

        private Boolean isUsingSaslAuth              = false;
        private Boolean isAuthed                     = false;
        private Boolean isNickRegistered             = false;
        private Boolean isConnPlainText              = false;
        private Boolean isAccountPending             = false;

        private byte[] ipAddress                     = base64ToByteArray("AAAAAA=="); /* Base64 0.0.0.0 -- % echo "00000000" | xxd -r -p | base64 */

        private Server userServer;

        private Long userTS                          = 0L;

        private Set<String> securityGroups           = new HashSet<>();

        private Map<String, String> saslAuthParams   = new HashMap<>();
        private Map<String, String> modes            = new HashMap<>();

        private Map<Channel, Set<String>> chanList   = new HashMap<>();

        private Map<Timestamp, String> nickHistory   = new LinkedHashMap<>();


        /**
         * Builder nickname
         * @param val nickname
         * @return object
         */
        public Builder nick(String val) {
            nick = val;
            return this;
        }

        /**
         * Builder ident
         * @param val ident
         * @return object
         */
        public Builder ident(String val) {
            ident = val;
            return this;
        }

        /**
         * Builder hostname
         * @param val hostname
         * @return object
         */
        public Builder host(String val) {
            host = val;
            return this;
        }

        /**
         * Builder real host
         * @param val real host
         * @return object
         */
        public Builder realHost(String val) {
            realHost = val;
            return this;
        }

        /**
         * Builder cloaked host
         * @param val cloaked host
         * @return object
         */
        public Builder cloakedHost(String val) {
            cloakedHost = val;
            return this;
        }

        /**
         * Builder real name (gecos)
         * @param val user real name
         * @return object
         */
        public Builder realName(String val) {
            realName = val;
            return this;
        }

        /**
         * Builder user id
         * @param val user id
         * @return object
         */
        public Builder uid(String val) {
            uid = val;
            return this;
        }

        /**
         * Builder certfp
         * @param val certfp
         * @return object
         */
        public Builder certFp(String val) {
            certFp = val;
            return this;
        }

        /**
         * Builder server
         * @param val server
         * @return object
         */
        public Builder server(Server val) {
            userServer = val;
            return this;
        }

        /**
         * Builder timestamp
         * @param val timestamp
         * @return object
         */
        public Builder userTS(Long val) {
            userTS = val;
            return this;
        }

        /**
         * Builder operLogin
         * @param val oper login
         * @return object
         */
        public Builder operLogin(String val) {
            operLogin = val;
            return this;
        }

        /**
         * Builder operClass
         * @param val oper class
         * @return object
         */
        public Builder operClass(String val) {
            operClass = val;
            return this;
        }

        /**
         * Builder country
         * @param val country
         * @return object
         */
        public Builder country(String val) {
            country = val;
            return this;
        }

        /**
         * Builder securityGroup
         * @param val security group
         * @return object
         */
        public Builder securityGroups(Set<String> val) {
            securityGroups = val;
            return this;
        }

        /**
         * Builder ip address (as string)
         * @param val ip address as a string
         * @return object
         */
        public Builder ip(String val) {
            ipAddress = base64ToByteArray(val);
            return this;
        }

        /**
         * Builder ip address (as bytes)
         * @param val ip address as a bytes array
         * @return object
         */
        public Builder ip(byte[] val) {
            ipAddress = val;
            return this;
        }

        /**
         * Builder user modes
         * @param val map of the user modes
         * @return object
         */
        public Builder modes(Map<String, String> val) {
            modes = val;
            return this;
        }

        /**
         * Builds the usernode
         * @return usernode
         */
        public Nick build(){
            return new Nick(this);
        }
    }



    /**
     * Adds an network user to the memory nick list
     * @param u user node
     * @throws ItemExistsException when the nick to add already exists in the list
     */
    public static void addUser(Nick u) throws ItemExistsException {

        if(userList.contains(u) == true)  throw new ItemExistsException("The nick is already registered on the network.");

        userList.add(u);
        userListByNick.put(u.nick.toLowerCase(), u);
        userListByUid.put(u.uid.toUpperCase(), u);
        nickToUid.put(u.nick.toLowerCase(), u.uid.toUpperCase());
        u.addNickHistory(u.nick);
    }

    /**
     * Removes an network user to the memory nick list
     * @param u user node
     * @throws ItemNotFoundException when the nick to remove is not in the list
     */
    public static void removeUser(Nick u) throws ItemNotFoundException {

        if(userList.contains(u) == false) throw new ItemNotFoundException("The nick is not registered on the network.");

        userList.remove(u);
        userListByNick.remove(u.nick.toLowerCase());
        userListByUid.remove(u.uid.toUpperCase());
        nickToUid.remove(u.nick.toLowerCase());
    }

    /**
     * Returns a user node looked up by its nick (case insensitive)
     * @param nick user nick
     * @return user node
     * @throws ItemNotFoundException when the user does not exist
     */
    public static Nick getUserByNickCi(String nick) throws NickNotFoundException { // TODO: descope to private and redirect to getNick()

        if (userListByNick.containsKey(nick.toLowerCase()) == false) throw new NickNotFoundException();
        return userListByNick.get(nick.toLowerCase());
    }

    /**
     * Returns a user node looked up by its UID
     * @param uid user UID
     * @return user node
     * @throws ItemNotFoundException when the user does not exist
     */
    public static Nick getUserByUid(String uid) { // TODO: descope to private and redirect to getNick()
        //if (userListByUid.containsKey(uid.toUpperCase()) == false) throw new ItemNotFoundException();
        return userListByUid.get(uid.toUpperCase());
    }

    /**
     * Returns the network users list
     * @return a copy of network users list Set
     */
    public static Set<Nick> getUsersList() {
        return new HashSet<Nick>(userList);
    }

    /**
     * Sets the network user list
     * @param l user list Set
     */
    public static void setUsersList(Set<Nick> l) {
        userList = new HashSet<Nick>(l);

        /* Then populates the other lists */
        userList.forEach(
            (u) -> {
                userListByUid.put(u.uid.toUpperCase(), u);
                userListByNick.put(u.nick.toLowerCase(), u);
                nickToUid.put(u.nick.toLowerCase(), u.uid.toUpperCase());
            }
        );
    }

    /**
     * Returns a Nick object based on a string (that can be a nickname or a UID)
     * @param s input nickname or UID
     * @return nick object
     * @throws NickNotFoundException when there is no match
     */
    public static Nick getNick(String s) throws NickNotFoundException {

        Nick nick;

        /* Check if the string is a UID */
        if (userListByUid.containsKey(s.toUpperCase()) == true) nick = userListByUid.get(s.toUpperCase());

        /* Else check if the string is a nick */
        else if (userListByNick.containsKey(s.toLowerCase()) == true) nick = userListByNick.get(s.toLowerCase());

        /* Else the string is neither a UID or a nick => entity is not on the network */
        else throw new NickNotFoundException(String.format("Entity %s has not been found on the network", s));

        return nick;

    }

    /**
     * Constructor that creates the usernode object from the builder
     * @param builder builder
     */
    private Nick(Builder builder) {

        if (builder.nick.isEmpty() == true) {
            RandomString randomNick = new RandomString(20);
            builder.nick = "temp#" + randomNick.nextString();
        }

        this.nick                  = builder.nick;
        this.ident                 = builder.ident;
        this.host                  = builder.host;
        this.realHost              = builder.realHost;
        this.cloakedHost           = builder.cloakedHost;
        this.realName              = builder.realName;
        this.uid                   = builder.uid;
        this.certFp                = builder.certFp;
        this.userServer            = builder.userServer;
        this.userTS                = builder.userTS;
        this.ipAddress             = builder.ipAddress;
        this.userModes             = builder.modes;
        this.isUsingSaslAuth       = builder.isUsingSaslAuth;
        this.isAuthed              = builder.isAuthed;
        this.isNickRegistered      = builder.isNickRegistered;
        this.isConnPlainText       = builder.isConnPlainText;
        this.saslAuthParams        = builder.saslAuthParams;
        this.chanList              = builder.chanList;
        this.nickHistory           = builder.nickHistory;
        this.operLogin             = builder.operLogin;
        this.operClass             = builder.operClass;
        this.country               = builder.country;
        this.securityGroups        = builder.securityGroups;
        this.isAccountPending      = builder.isAccountPending;

        try { addUser(this); }
        catch (ItemExistsException e) {
            log.error(String.format("UserNode::UserNode: cannot add the nick %s (UID = %s) because it already exist in the list.", this.nick, this.uid), e);
        }

    }

    /**
     * Adds a mode to the existing user modes
     * @param mode mode name
     * @param param mode parameter
     */
    public void addMode(String mode, String param) {
        this.userModes.put(mode, param);
    }

    /**
     * Replaces or add (if necessary) a mode to the user modes
     * @param mode mode name
     * @param param mode parameter
     */
    public void setMode(String mode, String param) {
        if (this.userModes.containsKey(mode)) { this.userModes.replace(mode, param); }
        else { this.userModes.put(mode, param); }
    }

    /**
     * Removes a mode from the user modes
     * @param mode mode name
     */
    public void delMode(String mode) {
        this.userModes.remove(mode);
    }

    /**
     * Removes a mode from the user modes
     * @param mode mode name
     * @param param mode parameter
     */
    public void delMode(String mode, String param) {
        delMode(mode);
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
    public void setUserModes(Map<String, String> modes) {
        this.userModes = modes;
    }

    /**
     * Sets the user server
     * @param server User server
     */
    public void setServer(Server server) {
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
        this.userAccount = account;
        try { this.userAccount.addUserAuth(this); }
        catch (ItemExistsException e) { log.error(String.format("UserNode/setAccount: Could not auth nick %s to account %s:", this.getNick(), account), e); }
    }

    /**
     * Unsets the user account from the user node
     */
    public void unsetAccount() {
        if (this.userAccount == null) {
            log.debug(String.format("UserNode/setAccount: Nick %s is not authed, not deauthenticating it", this.getNick()));
            return;
        }
        try { this.userAccount.delUserAuth(this); }
        catch (Exception e) { log.error(String.format("UserNode/setAccount: Could not deauth nick %s:", this.getNick()), e); }
        this.userAccount = null;
    }

    /**
     * Sets whether the user connection is unencrypted or not
     * @param isPlain encrypted (true) or not (false)
     */
    public void setConnPlainText(Boolean isPlain) {
        this.isConnPlainText = isPlain;
    }

    /**
     * Sets that the user has authenticated through SASL
     */
    public void setAuthBySasl() {
        this.isUsingSaslAuth = true;
    }

    /**
     * Fetches the encryption status of the user connection
     * @return true (encrypted) or false (not)
     */
    public Boolean isConnPlainText() {
        return this.isConnPlainText;
    }

    /**
     * Adds the user to the channel
     * @param chan channel
     * @param modes Map of modes
     */
    public void addChan(Channel chan, Set<String> modes) throws ItemExistsException {
        if (chanList.containsKey(chan) == true) {
            log.error(String.format("Nick::addChan: user %s is already in the chan %s", this.getNick(), chan));
            throw new ItemExistsException(String.format("Nick::addChan: user %s is already in the chan %s", this.getNick(), chan));
        }

        chanList.put(chan, modes);
    }

    /**
     * Removes the user from the channel
     * @param chan channel
     */
    public void delChan(Channel chan) throws ItemNotFoundException {
        if (chanList.containsKey(chan) == false) {
            log.error(String.format("Nick::delChan: user %s is not in the chan %s", this.getNick(), chan));
            throw new ItemNotFoundException(String.format("Nick::delChan: user %s is not in the chan %s", this.getNick(), chan));
        }

        chanList.remove(chan);
    }

    /**
     * Returns true if the user is on the given channel
     * @param chan Channel
     * @return true if the user is on the channel, false otherwise
     */
    public Boolean isUserOnChan(Channel chan) {
        if (this.chanList.containsKey(chan)) { return true; }
        else return false;
    }

    /**
     * Fetches the user modes on the channel
     * @param chan Channel name
     * @return User channel modes
     */
    public Set<String> getModesChan(Channel chan) throws ChannelNotFoundException {
        if (this.chanList.get(chan) == null) throw new ChannelNotFoundException();
        return this.chanList.get(chan);
    }

    /**
     * Fetches the list of channels the user is in
     * @return List of the user channels
     */
    public Map<Channel, Set<String>> getChanList() {
        return this.chanList;
    }

    /**
     * Sets the user auth status
     * @param state User auth status
     */
    public void setAuthed(Boolean state) {
        this.isAuthed = state;

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
    public void setNickRegistered(Boolean state) {
        this.isNickRegistered = state;
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
    public Map<Timestamp, String> getNickHistory() {
        return this.nickHistory;
    }

    public String getLastNick() {
        String lastNick = "";
        for(String s: this.nickHistory.values()) lastNick = s;
        return lastNick;
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
     * @return map uf user modes
     */
    public Map<String, String> getUserModes() {
        return this.userModes;
    }

    /**
     * Fetches the user server SID
     * @return User server SID
     */
    public Server getServer() {
        return this.userServer;
    }

    public String getModesAsString() {

        var wrapper = new Object() {
            String bufferMode = "";
            String bufferParam = "";
        };

        wrapper.bufferMode  = "";
        wrapper.bufferParam = "";
        this.getUserModes().forEach( (mode,param ) -> {
            wrapper.bufferMode   = String.join("", wrapper.bufferMode, mode);
            if (param.isEmpty() == false) wrapper.bufferParam  = String.join(" ", wrapper.bufferParam, param);
        });

        return String.format("%s %s", wrapper.bufferMode, wrapper.bufferParam);

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
    public UserAccount getAccount() throws UserNoAuthException {
        if (this.userAccount == null) throw new UserNoAuthException();
        else return this.userAccount;
    }

    /**
     * Fetches whether the user is authed
     * @return User auth status
     */
    public Boolean isAuthed() {
        return this.isAuthed;
    }

    /**
     * Fetches whether the user's nick is registered
     * @return User nick registration status
     */
    public Boolean isNickRegistered() {
        return this.isNickRegistered;
    }

    /**
     * Fetches the user timestamp
     * @return User timestamp
     */
    public Long getUserTS() {
        return this.userTS;
    }

    /**
     * Returns the user session UUID
     * @return session UUID
     */
    public UUID getAuthUuid() {
        return this.authSessUUID;
    }

    /**
     * Returns the user authentication timestamp
     * @return timestamp
     */
    public Long getAuthTS() {
        return this.authTS;
    }

    /**
     * Sets the SASL auth parameter -> value for the user
     * @param param Parameter name
     * @param value Parameter value
     */
    public void setSaslAuthParam(String param, String value) {
        this.saslAuthParams.put(param, value);
    }

    /**
     * Gets the SASL user auth parameter
     * @param param Parameter name
     * @return Parameter value
     */
    public String getSaslAuthParam(String param) {
        try { return this.saslAuthParams.get(param); }
        catch (NullPointerException e) { log.error(String.format("UserNode/getSaslAuthParam: error fetching the SASL auth parameters") , e); return null; }
    }

    /**
     * Returns if the user is authenticated using SASL
     * @return true if the user is authenticated using SASL, false otherwise
     */
    public Boolean isAuthBySasl() {
        return this.isUsingSaslAuth;
    }

    /**
     * Sets the user cloaked hostname
     * @param clkdHost Cloaked hostname
     */
    public void setCloakedHost(String clkdHost) {
        this.cloakedHost = clkdHost;
    }

    /**
     * Gets the user cloaked host
     * @return User cloaked hostname
     */
    public String getCloakedHost() {
        return this.cloakedHost;
    }

    /**
     * Gets the user IP address as bytes
     * @return user IP address
     */
    public byte[] getIpAddressAsByte() {
        return this.ipAddress;
    }

    /**
     * Returns the user IP address as a human readeable string
     * - a.b.c.d for an IPv4
     * - 1:2:3:4:5:6:7:8 for an IPv6 (trailing 0's not stripped)
     * @return user IP address as a string
     */
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

    /**
     * Returns the user IP address as a base 64 string
     * @return user IP address as base 64
     */
    public String getIpAddressAsBase64() {
        String ipEncoded = "";
        Base64.Encoder enc = Base64.getEncoder();
        try { ipEncoded = enc.encodeToString(this.ipAddress); }
        catch (Exception e) { log.error("UserNode/getIpAddressBase64: Could not encode client IP to base64: " + this.getNick() + ".", e); }

        return ipEncoded;
    }

    /**
     * Converts a base64 value to a bytes array
     * @param base64val base 64 value
     * @return bytes array
     */
    private static byte[] base64ToByteArray(String base64val) {
        byte[] byteArr;
        Base64.Decoder dec = Base64.getDecoder();
        try { byteArr = dec.decode(base64val); }
        catch (Exception e) { byteArr = null; }
        return byteArr;
    }

    /**
     * Sets the user IP address
     * @param base64Ip IP address (base64)
     */
    public void setIpAddress(String base64Ip) {
        try { this.ipAddress = base64ToByteArray(base64Ip); }
        catch (Exception e) { log.error("UserNode/setIpAddress: Could not set the IP of client: " + this.getNick() + ": ", e); }
    }

    /**
     * Sets the user IP address
     * @param ip IP address (bytes array)
     */
    public void setIpAddress(byte[] ip) {
        this.ipAddress = ip;
    }

    /**
     * Gets the user mask
     * @param type Mask type
     * @return User mask
     */
    public String getMask(String type) {
        switch (type) {
            case "nick!ident@realhost+ip":        return String.format("%s!%s@%s (%s)", nick, ident, host, getIpAddressAsString());
            case "nick!ident@realhost":           return String.format("%s!%s@%s", nick, ident, host);
            case "ident@realhost+ip":             return String.format("%s@%s (%s)", ident, host, getIpAddressAsString());
            case "ident@realhost":                return String.format("%s@%s", ident, host);

            default:                              return "undefined*mask*type";
        }
    }

    @Override public String toString() {
        return this.nick;
    }

    /**
     * Fetches whether the user is an oper
     * @return User oper status
     */
    public Boolean isOper() {

        if (this.userModes.get("o") == null) return false;
        else return true;

    }

    /**
     * Sets the oper login
     * @param s oper login
     */
    public void setOperLogin(String s) {
        this.operLogin = s;
    }

    /**
     * Sets the oper class
     * @param s oper class
     */
    public void setOperClass(String s) {
        this.operClass = s;
    }

    /**
     * Sets the country
     * @param s country
     */
    public void setCountry(String s) {
        this.country = s;
    }

    /**
     * Sets the security group
     * @param s security group
     */
    public void setSecurityGroups(Set<String> s) {
        this.securityGroups = s;
    }

    /**
     * Returns the oper login
     * @return oper login
     */
    public String getOperLogin() {
        return this.operLogin;
    }

    /**
     * Returns the oper class
     * @return oper class
     */
    public String getOperClass() {
        return this.operClass;
    }

    /**
     * Returns the country
     * @return country
     */
    public String getCountry() {
        return this.country;
    }

    /**
     * Returns the country
     * @return country
     */
    public String getCountry(String s) {
        /* cc=FR|cd=France */

        String nameraw = "";
        String coderaw = "";

        String name = "";
        String code = "";

        String[] split;

        split = this.country.split("\\|");

        try { coderaw = split[0]; }
        catch (IndexOutOfBoundsException e) { coderaw = "cc=00"; }

        try { nameraw = split[1]; }
        catch (IndexOutOfBoundsException e) { nameraw = "cd=CountryParseError"; }

        split = coderaw.split("=");
        try { code = split[1]; }
        catch (IndexOutOfBoundsException e) { code = "00"; }

        split = nameraw.split("=");
        try { name = split[1]; }
        catch (IndexOutOfBoundsException e) { name = "CountryParseError"; }

        switch (s) {
            case "name": return name;
            case "code": return code;
            default: return code;
        }

    }

    /**
     * Returns the security group
     * @return security group
     */
    public Set<String> getSecurityGroups() {
        return this.securityGroups;
    }

    /**
     * Changes a user nickname in the memory
     * @param newNick new nickname
     */
    public void changeNick(String newNick) {
        userListByNick.remove(this.nick.toLowerCase());
        nickToUid.remove(this.nick.toLowerCase());

        this.nick = newNick;

        userListByNick.put(newNick.toLowerCase(), this);
        nickToUid.put(newNick.toLowerCase(), this.uid);

        addNickHistory(this.nick);

    }

    /**
     * Adds a nickname to the Nick nicknames history. If the history reaches the limit,
     * the oldest nickname will be popped out.
     * @param s nickname to add
     */
    public void addNickHistory(String s) {

        int limit = 10;

        Timestamp ts = new Timestamp();

        LinkedHashMap<Timestamp, String>  history = new LinkedHashMap<>(this.nickHistory) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Timestamp, String> eldest) {
                return size() > limit;
            }
        };

        history.put(ts, s);

        this.nickHistory = history;

    }

    public Boolean isAccountPending() {
        return this.isAccountPending;
    }

    public void setAccountPending(Boolean b) {
        this.isAccountPending = b;
    }

    public UserMask getUserMask() {
        return UserMask.create(String.format("%s!%s%s", this.nick, this.ident, this.host));
    }

    public Set<UserMask> getAllUserMasks() {
        Set<UserMask> userMasks = new HashSet<>();

        if (this.host.isEmpty() == false) userMasks.add(UserMask.create(String.format("%s!%s@%s", this.nick, this.ident, this.host)));
        if (this.realHost.isEmpty() == false) userMasks.add(UserMask.create(String.format("%s!%s@%s", this.nick, this.ident, this.realHost)));
        if (this.cloakedHost.isEmpty() == false) userMasks.add(UserMask.create(String.format("%s!%s@%s", this.nick, this.ident, this.cloakedHost)));
        if (this.getIpAddressAsString().isEmpty() == false) userMasks.add(UserMask.create(String.format("%s!%s@%s", this.nick, this.ident, this.getIpAddressAsString())));

        return userMasks;
    }
}
