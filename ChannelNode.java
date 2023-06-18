/**
 * ChannelNode class
 *
 * Class to store network channels.
 *
 * @author me
 */ 

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
public class ChannelNode {

    private static Logger log = LogManager.getLogger("common-log");
    
    private Integer userCount  = 0;
    private Integer flags   = 0;
    private Integer autoLimit      = 10;
    private Integer banTime        = 0;
    private Integer channelId;
    private Integer id = 0;

    private String name;
    private String topic               = "";
    private String chanRegisteredTopic = "";
    private String chanWelcomeMsg      = "";
    private String topicBy;

    private UUID confirmCode = null;


    private Long topicTS;
    private Long channelTS; /* If channel is registered, channel TS = registration TS */

    private Boolean channelRegistered = false;

    private UserNode channelOwner;

    private HashMap<String, Integer> chanlev  = new HashMap<>(); // Map username -> chanlev

    /* HM maps mode -> parameter */
    private HashMap<String, String> mLockModes = new HashMap<>(); 
    private HashMap<String, String> channelModes = new HashMap<String, String>(); // Map mode -> parameter

    
    /* Contains the UserNodes inside the chan */
    private HashSet<UserNode> chanUserList = new HashSet<>();
   
    
    private HashSet<String> banList = new HashSet<>();
    private HashSet<String> exceptList = new HashSet<>();
    private HashSet<String> inviteList = new HashSet<>();
    
    /**
     * Constructor used when a local user joins an empty channel => creates the new channel
     * @param channelName
     * @param channelTS
     */
    public ChannelNode(String channelName, long channelTS) {
        this.name = channelName;
        this.channelTS = channelTS;
    }

    /**
     * Constructor used to create the registered channels at initiation of the protocol
     * @param sqliteDb
     * @param channelName
     * @param channelTS
     * @param channelFlags
     * @param chanId
     * @param chanWelcomeMsg
     * @param chanRegTopic
     * @param banTime
     * @param autoLimit
     */
    public ChannelNode(SqliteDb sqliteDb, String channelName, Long channelTS, Integer channelFlags, Integer chanId, String chanWelcomeMsg, String chanRegTopic, Integer banTime, Integer autoLimit) {
        this.name = channelName;
        this.channelTS = channelTS;
        this.flags = channelFlags;
        this.channelRegistered = true;
        this.channelId = chanId;
        this.chanWelcomeMsg = chanWelcomeMsg;
        this.chanRegisteredTopic = chanRegTopic;
        this.banTime = banTime;
        this.autoLimit = autoLimit;

        try {
            this.id = sqliteDb.getChanId(this);
        }
        catch (Exception e) { log.error(String.format("ChannelNode/constuctor: Getting channel ID from database: "), e); }
    }

    /**
     * Constructor used at SJOIN when remote user joins empty channels
     * @param channelName
     * @param channelTS
     * @param channelModes
     * @param banList
     * @param exceptList
     * @param inviteList
     */
    public ChannelNode( String channelName, 
                        long channelTS,
                        HashMap<String, String> channelModes,
                        HashSet<String> banList,
                        HashSet<String> exceptList,
                        HashSet<String> inviteList)
    {
        this.name = channelName;
        this.channelTS = channelTS;
        this.channelModes = channelModes;
        this.banList = banList;
        this.exceptList = exceptList;
        this.inviteList = inviteList;
    }

    public void addMode(String mode, String param) {
        this.channelModes.put(mode, param);
    }

    public void setMode(String mode, String param) {
        if (this.channelModes.containsKey(mode)) { this.channelModes.replace(mode, param); }
        else { this.channelModes.put(mode, param); }
    }

    public void delMode(String mode) {
        this.channelModes.remove(mode);
    }

    public void delMode(String mode, String param) {
        delMode(mode);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setTopicTS(Long ts) {
        this.topicTS = ts;
    }

    public void setTopicBy(String mask) {
        this.topicBy = mask;
    }
    
    public void setRegistered(Boolean registered) {
        this.channelRegistered = registered;
    }

    public void setOwner(UserNode user) {
        this.channelOwner = user;
    }

    public void setChanlev(HashMap<String, Integer> chanChanlev) {
        this.chanlev = chanChanlev;
    }

    public void setChanlev(UserNode user, Integer chanlev) {
        if (chanlev != 0) {
            if (this.chanlev.containsKey(user.getAccount().getName()) == true) {
                this.chanlev.replace(user.getAccount().getName(), chanlev);
            }
            else {
                this.chanlev.put(user.getAccount().getName(), chanlev);
            }
        }
        else {
            if (this.chanlev.containsKey(user.getAccount().getName()) == true) {
                this.chanlev.remove(user.getAccount().getName());
            }
        }
    }

    public void clearChanChanlev(UserNode user) {
        setChanlev(user, 0);
    }

    public void addBanList(String str) {
        this.banList.add(str);
    }

    public void addExceptList(String str) {
        this.exceptList.add(str);
    }

    public void addInviteList(String str) {
        this.inviteList.add(str);
    }

    public void delBanList(String str) {
        this.banList.remove(str);
    }

    public void delExceptList(String str) {
        this.exceptList.remove(str);
    }

    public void delInviteList(String str) {
        this.inviteList.remove(str);
    }

    public void setBanList(HashSet<String> banList) {
        this.banList = banList;
    }

    public void setExceptList(HashSet<String> exceptList) {
        this.exceptList = exceptList;
    }

    public void setInviteList(HashSet<String> inviteList) {
        this.inviteList = inviteList;
    }

    public HashSet<String> getBanList() {
        return this.banList;
    }

    public HashSet<String> getExceptList() {
        return this.exceptList;
    }

    public HashSet<String> getInviteList() {
        return this.inviteList;
    }

    public HashMap<String, String> getModes() {
        return this.channelModes;
    }

    public String getMode(String mode) {
        return this.channelModes.get(mode);
    }

    public void setModes(HashMap<String, String> channelModes) {
        this.channelModes = channelModes;
    }

    public String getTopic() {
        return this.topic;
    }

    public Boolean isRegistered() {
        return this.channelRegistered;
    }

    public UserNode getOwner() {
        return this.channelOwner;
    }

    public Map<String, Integer> getChanlev() {
        return this.chanlev;
    }

    public Map<String, Integer> getChanlevWoutPersonalFlags() {
        HashMap<String, Integer> chanlevMap = new HashMap<>();

        chanlev.forEach( (user, chanlev) -> {
            if (Flags.stripChanlevPersonalFlags(chanlev) != 0) {
                chanlevMap.put(user, chanlev);
            }
        } );

        return chanlevMap;
    }

    public String getName() {
        return this.name;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getChanTS() {
        return this.channelTS;
    }

    public void setChanTS(Long channelTS) {
        this.channelTS = channelTS;
    }

    public Integer getUserCount() {
        return this.userCount;
    }

    public void setUserCount(Integer count) {
        this.userCount = count;
    }

    public void setFlags(Integer chanFlags) {
        this.flags = chanFlags;
    }

    public Integer getFlags() {
        return this.flags;
    }

    public HashSet<UserNode> getUsers() {
        return this.chanUserList;
    }

    public void addUser(UserNode user) {
        this.chanUserList.add(user);
        this.userCount++;
    }
    public void removeUser(UserNode user) {
        this.chanUserList.remove(user);
        this.userCount--;
    }
    
    public UserNode getUser(UserNode user) {
        if (this.chanUserList.contains(user)) return user;
        else return null;
    }

    public UserNode getUser(String nick) {
        var wrapper = new Object() { UserNode userNode = null; };
        this.chanUserList.forEach( (user) -> {
            if (user.getNick().equals(nick) == true) { wrapper.userNode = user; }
        });

        return wrapper.userNode;
    }



    public Integer getAutoLimit() {
        return this.autoLimit;
    }

    public void setAutoLimit(Integer autolimit) {
        this.autoLimit = autolimit;
    }

    public void setConfirmCode(UUID uuid) {
        this.confirmCode = uuid;
    }

    public UUID getConfirmCode() {
        return this.confirmCode;
    }
}