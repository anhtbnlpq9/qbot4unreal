import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;

/**
 * UserNode class to store the connected users.
 * @author me
 */
public class UserNode {
    
    private String userNick       = "";
    private String userOldNick    = "";
    private String userIdent      = "";
    private String userHost       = "";
    private String userRealHost   = "";
    private String userRealName   = "";
    private String userUniq       = "";
    private String userModes      = "";
    private String userCertFP     = "";
    private String userAccountP   = "";
    private String userAccountId  = "";

    private ServerNode userServer;
    private UserAccount userAccount;

    private long userTS;

    private Map<String, ChannelNode> userChanList   = new HashMap<String, ChannelNode>();
    private Map<String, String>      userChanModes  = new HashMap<String, String>();
    private Map<String, String>      userChanlev    = new HashMap<String, String>();

    private Boolean userAuthed         = false;
    private Boolean userNickRegistered = false;

    /**
     * Class constructor
     */
    public UserNode() {
        
    }
    /**
     * Class constructor
     * @param userUniq User SID
     */
    public UserNode(String userUniq) {
        this.userUniq = userUniq;
    } 
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
     * Sets the user nickname
     * @param nick User nick
     */
    public void setUserNick(String nick) {
        this.userOldNick = this.userNick;
        this.userNick = nick;
    }
    /**
     * Sets the user account number
     * @param accountId User account number
     */
    public void setUserAccountId(String accountId) {
        this.userAccountId = accountId;
    }
    /**
     * Sets the user ident
     * @param ident User ident
     */
    public void setUserIdent(String ident) {
        this.userIdent = ident;
    }
    /**
     * Sets the user (v)host
     * @param host User (v)host
     */
    public void setUserHost(String host) {
        this.userHost = host;
    }
    /**
     * Sets the user real host
     * @param rhost User real host
     */
    public void setUserRealHost(String rhost) {
        this.userRealHost = rhost;
    }
    /**
     * Sets the user real name (gecos)
     * @param realName User gecos
     */
    public void setUserRealName(String realName) {
        this.userRealName = realName;
    }
    /**
     * Sets the user SID
     * @param uniq User SID
     */
    public void setUserUniq(String uniq) {
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
    public void setUserServer(ServerNode server) {
        this.userServer = server;
    }
    /**
     * Sets the user certificate fingerprint
     * @param certfp User certfp
     */
    public void setUserCertFP(String certfp) {
        this.userCertFP = certfp;
    }
    /**
     * Sets the user account name
     * @param account User account name
     */
    public void setUserAccount(String account) {
        this.userAccountP = account;
    }
    /**
     * Adds the user to the channel
     * @param channel Channel name
     * @param chanObj Channel object
     * @param mode User mode on the channel
     */
    public void addUserToChan(String channel, ChannelNode chanObj, String mode) /*throws Exception*/ {
        //if (this.userChanList.contains(channel)) {
        //    throw new Exception("Cannot add the user inside a channel they already are in"); 
        //}
        //else {
        //    this.userChanList.add(channel);
        //}
        userChanList.put(channel, chanObj);
        userChanModes.put(channel, mode);
    }
    /**
     * Removes the user from the channel
     * @param channel Channel name
     */
    public void delUserFromChan(String channel) /*throws Exception*/ {
        //if (this.userChanList.contains(channel)) {
        //    throw new Exception("Cannot add the user inside a channel they already are in"); 
        //}
        //else {
        //    this.userChanList.add(channel);
        //}
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

        //this.userChanModes.forEach( (key, value) -> { System.out.println("AAB userChanModes map = " + key + " -> " + value); });
        
        this.userChanModes.replace(chan, this.userChanModes.get(chan).replaceAll("[" + modes + "]", ""));

        //this.userChanModes.forEach( (key, value) -> { System.out.println("AAC userChanModes map = " + key + " -> " + value); });
    }
    /**
     * Sets the user auth status
     * @param state User auth status
     */
    public void setUserAuthed(Boolean state) {
        this.userAuthed = state;
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
    public void setUserTS(Integer userTS) {
        this.userTS = userTS;
    }
    /**
     * Sets the user chanlev on the channels they are known of
     * @param userChanlev User chanlevs
     */
    public void setUserChanlev(Map<String, String> userChanlev) {
        this.userChanlev = userChanlev;
    }
    /**
     * Sets the user's chanlev for the channel
     * @param channel Channel name
     * @param chanlev User chanlev
     */
    public void setUserChanlev(String channel, String chanlev) {
        if (chanlev.isEmpty() == false) {
            if (this.userChanlev.containsKey(channel) == true) { 
                this.userChanlev.replace(channel, chanlev);
            }
            else {
                this.userChanlev.put(channel, chanlev);
            }
        }
        
    }
    /**
     * Parses a chanlev change and applies it to an input chanlev
     * @param chanlevUser User current chanlev
     * @param chanlevNew Chanlev change
     * @return User new chanlev
     */
    public static String parseChanlev(String chanlevUser, String chanlevNew) {
        
       /*
        * CHANLEV flags:
        * +a = auto (op/voice, op has priority on voice)
        * +b = ban
        * +d = will be automatically deopped / incompatible with +op / setting +d will unset +op
        * +j = auto-invite when authing
        * +k = known-user (can use INVITE command)
        * +m = master (can (un)set all the flags except n and m)
        * +n = owner (can (un)set all the flags)
        * +o = can use OP command / incompatible with +d / setting +o will unset +d
        * +p = protect (Q will revoice/reop the user) / incompatible with +dq / setting +p will unset +dq
        * +q = will be automatically devoiced / incompatible with +vp / setting +q will unset +vp
        * +t = can use SETTOPIC command
        * +v = can use VOICE command / incompativle with +q / setting +v will unser +q
        * +w = disable auto welcome notice on join
        * 
        * incompatibilites:
        * +d with +o +p
        * +q with +v +p
        * +o with +d
        * +v with +q
        * +p with +d +q
        * 
        */


        Map<String, String> chanlevTemp = new HashMap<String, String>();
        chanlevTemp.put("+", "");
        chanlevTemp.put("-", "");
        chanlevTemp.put("result", "");
        chanlevTemp.put("user", "");
 
        chanlevTemp.put("user", chanlevUser);
         

        boolean plusMode = false;
        for(int i=0; i < chanlevNew.length(); i++) {
            if (chanlevNew.charAt(i) == '+') {
                plusMode = true;
            }
            else if (chanlevNew.charAt(i) == '-') {
                plusMode = false;
            }
            else {
                if (plusMode == true) {
                chanlevTemp.replace("+", chanlevTemp.get("+") + String.valueOf(chanlevNew.charAt(i)));
                }
                else {
                    chanlevTemp.replace("-", chanlevTemp.get("-") + String.valueOf(chanlevNew.charAt(i)));
                }
            }
        }
        /*
        * +d => -o
        * +q => -v
        * +od => +d
        * +vq => +q
        * +abdjkmnopqtvw => +abdjkmnpqtw
        */

        String chanlevTempP = chanlevTemp.get("+");
        String chanlevTempM = chanlevTemp.get("-");
        String chanlevTempR = "";

        // applying priority in +flags (+abdjkmnopqtvw => +abdjkmnpqtw)
        if (chanlevTempP.contains("d")) {
            chanlevTempP.replaceAll("o","");
        }
        if (chanlevTempP.contains("q")) {
            chanlevTempP.replaceAll("v","");
        }

        // Append user chanlev to +flags to temp chanlev
        chanlevTemp.replace("result", sortString(removeDuplicate(chanlevTemp.get("user") + chanlevTempP)));
        // remove the -flags from temp chanlev
        for (int i=0; i < chanlevTempM.length(); i++) {
            chanlevTemp.replace("result", chanlevTemp.get("result").replaceAll(String.valueOf(chanlevTempM.charAt(i)), ""));
        }

        // analyse incompatibilities
        chanlevTempR = chanlevTemp.get("result"); // store result temporarily here to avoid loops in incompatibilities analysis
        if (chanlevTempR.contains("d")) {
            chanlevTemp.replace("result", chanlevTemp.get("result").replaceAll("o", ""));
        }
        if (chanlevTempR.contains("q")) {
            chanlevTemp.replace("result", chanlevTemp.get("result").replaceAll("v", ""));
        }

        //System.out.println("BBS old chanlev=" + chanlevUser);
        //System.out.println("BBT new chanlev=" + chanlevTemp.get("result"));

        return chanlevTemp.get("result");

     }
    /**
     * Removes the user's chanlev from the channel
     * @param channel Channel name
     */
    public void unSetUserChanlev(String channel) {
        this.userChanlev.remove(channel);
    }
    /**
     * Fetches the user's chanlev for all the channels they are known of
     * @return User chanlevs
     */
    public Map<String, String> getUserChanlev() {
        return this.userChanlev;
    }
    /**
     * Fetches the user chanlev of the channel
     * @param channel Channel name
     * @return User channel chanlev
     */
    public String getUserChanlev(String channel) {
        return this.userChanlev.get(channel);
    }
    /**
     * Fetches the user nickname
     * @return User nickname
     */
    public String getUserNick() {
        return this.userNick;
    }
    /**
     * Fetches the user account number
     * @return User account number
     */
    public String getUserAccountId() {
        return this.userAccountId;
    }
    /**
     * Fetches the user previous nickname
     * @return Previous user nickname
     */
    public String getUserOldNick() {
        return this.userOldNick;
    }
    /**
     * Fetches the user ident
     * @return User ident
     */
    public String getUserIdent() {
        return this.userIdent;
    }
    /**
     * Fetches the user (v)host
     * @return User (v)host
     */
    public String getUserHost() {
        return this.userHost;
    }
    /**
     * Fetches the user real host
     * @return User realhost
     */
    public String getUserRealHost() {
        return this.userRealHost;
    }
    /**
     * Fetches the user real name (gecos)
     * @return User gecos
     */
    public String getUserRealName() {
        return this.userRealName;
    }
    /**
     * Fetches the user SID
     * @return User SID
     */
    public String getUserUniq() {
        return this.userUniq;
    }
    /**
     * Fetches the user usermodes
     * @return User usermodes
     */
    public String getUserModes() {
        return this.userModes;
    }
    /**
     * Fetches the user server SID
     * @return User server SID
     */
    public ServerNode getUserServer() {
        return this.userServer;
    }
    /**
     * Fetches the user certificate fingerprint
     * @return User certfp
     */
    public String getUserCertFP() {
        return this.userCertFP;
    }
    /**
     * Fetches the account name the user is authed as
     * @return User account name
     */
    public String getUserAccount() {
        return this.userAccountP;
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
    public Boolean getUserAuthed() {
        return this.userAuthed;
    }
    /**
     * Fetches whether the user's nick is registered
     * @return User nick registration status
     */
    public Boolean getUserNickRegistered() {
        return this.userNickRegistered;
    }
    /**
     * Fetches the user timestamp
     * @return User timestamp
     */
    public long getUserTS() {
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
}
