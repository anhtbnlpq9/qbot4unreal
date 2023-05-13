/**
 * UserNode class
 *
 * Class to store network users:
 * - nick
 * - old nick (to handle nick changes)
 * - user/ident
 * - host, real host
 * - realname
 * - uniq id
 * - modes
 * - channels membership
 * - server
 * - certfp
 * - account
 * - auth status
 * - register status (for nick ownerships)
 * - timestamp
 *
 * @author me
 */ 
 
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;

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

    public UserNode() {
        
    }
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

    public void setUserNick(String nick) {
        this.userOldNick = this.userNick;
        this.userNick = nick;
    }
    public void setUserAccountId(String accountId) {
        this.userAccountId = accountId;
    }
    public void setUserIdent(String ident) {
        this.userIdent = ident;
    }
    public void setUserHost(String host) {
        this.userHost = host;
    }
    public void setUserRealHost(String rhost) {
        this.userRealHost = rhost;
    }
    public void setUserRealName(String realName) {
        this.userRealName = realName;
    }
    public void setUserUniq(String uniq) {
        this.userUniq = uniq;
    }
    public void setUserModes(String modes) {
        this.userModes = modes;
    }
    public void setUserServer(ServerNode server) {
        this.userServer = server;
    }
    public void setUserCertFP(String certfp) {
        this.userCertFP = certfp;
    }
    public void setUserAccount(String account) {
        this.userAccount = account;
    }
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
    public String getUserChanMode(String chan) {
        return this.userChanModes.get(chan);
    }
    public void addUserChanMode(String chan, String modes) {
        this.userChanModes.replace(chan, sortString(removeDuplicate(this.userChanModes.get(chan) + modes)));
    }
    public void delUserChanMode(String chan, String modes) {

        //this.userChanModes.forEach( (key, value) -> { System.out.println("AAB userChanModes map = " + key + " -> " + value); });
        
        this.userChanModes.replace(chan, this.userChanModes.get(chan).replaceAll("[" + modes + "]", ""));

        //this.userChanModes.forEach( (key, value) -> { System.out.println("AAC userChanModes map = " + key + " -> " + value); });
    }
    public void setUserAuthed(Boolean state) {
        this.userAuthed = state;
    }
    public void setUserNickRegistered(Boolean state) {
        this.userNickRegistered = state;
    }
    public void setUserTS(Integer userTS) {
        this.userTS = userTS;
    }
    public void setUserChanlev(Map<String, String> userChanlev) {
        this.userChanlev = userChanlev;
    }
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
    public void unSetUserChanlev(String channel) {
        this.userChanlev.remove(channel);
    }
    public Map<String, String> getUserChanlev() {
        return this.userChanlev;
    }
    public String getUserChanlev(String channel) {
        return this.userChanlev.get(channel);
    }
    public String getUserNick() {
        return this.userNick;
    }
    public String getUserAccountId() {
        return this.userAccountId;
    }
    public String setUserAccountId() {
        return this.userAccountId;
    }
    public String getUserOldNick() {
        return this.userOldNick;
    }
    public String getUserIdent() {
        return this.userIdent;
    }
    public String getUserHost() {
        return this.userHost;
    }
    public String getUserRealHost() {
        return this.userRealHost;
    }
    public String getUserRealName() {
        return this.userRealName;
    }
    public String getUserUniq() {
        return this.userUniq;
    }
    public String getUserModes() {
        return this.userModes;
    }
    public ServerNode getUserServer() {
        return this.userServer;
    }
    public String getUserCertFP() {
        return this.userCertFP;
    }
    public String getUserAccount() {
        return this.userAccount;
    }
    public Map<String, ChannelNode> getUserChanList() {
        return this.userChanList;
    }
    public Map<String, String> getUserChanModes() {
        return this.userChanModes;
    }
    public Boolean getUserAuthed() {
        return this.userAuthed;
    }
    public Boolean getUserNickRegistered() {
        return this.userNickRegistered;
    }
    public long getUserTS() {
        return this.userTS;
    }
    public Boolean isOper() {
        if (this.userModes.matches("(.*)o(.*)") == true) return true;
        else return false;
    }
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
    private static String sortString(String str) {
        char charArray[] = str.toCharArray();
        Arrays.sort(charArray);
        return new String(charArray);
    }
}
