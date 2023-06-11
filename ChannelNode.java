/**
 * ChannelNode class
 *
 * Class to store network channels.
 *
 * @author me
 */ 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
 
public class ChannelNode {
    
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
    private String mLocked             = "";

    private UUID confirmCode = null;


    private Long topicTS;
    private Long channelTS; /* If channel is registered, channel TS = registration TS */

    private Boolean channelRegistered;

    private UserNode channelOwner;

    private Map<String, Integer> chanChanlev; // Map username -> chanlev

    
    /* Contains the UserNodes inside the chan */
    private HashMap<String, UserNode> chanUserList = new HashMap<String, UserNode>();
   
    private Map<String, String> channelModes = new HashMap<String, String>(); // Map mode -> parameter
    
    private ArrayList<String> banList = new ArrayList<String>();
    private ArrayList<String> exceptList = new ArrayList<String>();
    private ArrayList<String> inviteList = new ArrayList<String>();
    
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
        catch (Exception e) {
            e.printStackTrace();
        }
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
                        Map<String, String> channelModes,
                        ArrayList<String> banList,
                        ArrayList<String> exceptList,
                        ArrayList<String> inviteList)
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

    public void setChanlev(Map<String, Integer> chanChanlev) {
        this.chanChanlev = chanChanlev;
    }

    public void setChanlev(UserNode user, Integer chanlev) {
        if (chanlev != 0) {
            if (this.chanChanlev.containsKey(user.getAccount().getName()) == true) {
                this.chanChanlev.replace(user.getAccount().getName(), chanlev);
            }
            else {
                this.chanChanlev.put(user.getAccount().getName(), chanlev);
            }
        }
        else {
            if (this.chanChanlev.containsKey(user.getAccount().getName()) == true) {
                this.chanChanlev.remove(user.getAccount().getName());
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

    public void setBanList(ArrayList<String> banList) {
        this.banList = banList;
    }

    public void setExceptList(ArrayList<String> exceptList) {
        this.exceptList = exceptList;
    }

    public void setInviteList(ArrayList<String> inviteList) {
        this.inviteList = inviteList;
    }

    public ArrayList<String> getBanList() {
        return this.banList;
    }

    public ArrayList<String> getExceptList() {
        return this.exceptList;
    }

    public ArrayList<String> getInviteList() {
        return this.inviteList;
    }

    public Map<String, String> getModes() {
        return this.channelModes;
    }

    public String getMode(String mode) {
        return this.channelModes.get(mode);
    }

    public void setModes(Map<String, String> channelModes) {
        this.channelModes = channelModes;
    }

    public String getTopic() {
        //if (this.topic == null) { return ""; }
        return this.topic;
    }

    public Boolean isRegistered() {
        return this.channelRegistered;
    }

    public UserNode getOwner() {
        return this.channelOwner;
    }

    public Map<String, Integer> getChanlev() {
        return this.chanChanlev;
    }

    public Map<String, Integer> getChanlevWoutPersonalFlags() {
        HashMap<String, Integer> chanlevMap = new HashMap<>();

        chanChanlev.forEach( (user, chanlev) -> {
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

    public HashMap<String, UserNode> getUsers() {
        return this.chanUserList;
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