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
 
public class ChannelNode {
    
    public String channelName;
    public String channelTopic;
    public Boolean channelRegistered;
    public UserNode channelOwner;
    public String chanlev;
    public Integer chanUserCount=0;

    public long channelTS;
    
    public Map<String, UserNode> chanUserList = new HashMap<String, UserNode>();
    //public Map<String, ChannelMode> channelModes = new HashMap<String, ChannelMode>();
    public Map<String, String> channelModes = new HashMap<String, String>(); // Map mode -> parameter
    
    public ArrayList<String> banList = new ArrayList<String>();
    public ArrayList<String> exceptList = new ArrayList<String>();
    public ArrayList<String> inviteList = new ArrayList<String>();
    
    public ChannelNode() {
        
    }
    public ChannelNode(String channelName, long channelTS) {
        this.channelName = channelName;
        this.channelTS = channelTS;
    }

    public ChannelNode(String channelName, long channelTS, Map<String, String> channelModes) {
        this.channelName = channelName;
        this.channelTS = channelTS;
        this.channelModes = channelModes;
    }

    public ChannelNode( String channelName, 
                        long channelTS,
                        Map<String, String> channelModes,
                        ArrayList<String> banList,
                        ArrayList<String> exceptList,
                        ArrayList<String> inviteList)
    {

        this.channelName = channelName;
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
        this.channelModes.replace(mode, param);
    }

    public void delMode(String mode) {
        this.channelModes.remove(mode);
    }

    public void delMode(String mode, String param) {
        this.channelModes.remove(mode);
    }

    public void setTopic(String topic) {
        this.channelTopic = topic;
    }
    public void setRegistered(Boolean registered) {
        this.channelRegistered = registered;
    }
    public void setOwner(UserNode user) {
        this.channelOwner = user;
    }
    public void setChanlev(String chanlev) {
        this.chanlev = chanlev;
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

    public String getTopic() {
        return this.channelTopic;
    }
    public Boolean getRegistered() {
        return this.channelRegistered;
    }
    public UserNode getOwner() {
        return this.channelOwner;
    }
    public String getChanlev() {
        return this.chanlev;
    }
    public String getChanName() {
        return this.channelName;
    }
    public Long getChanTS() {
        return this.channelTS;
    }

    public Integer getChanUserCount() {
        return this.chanUserCount;
    }
    public void setChanUserCount(int count) {
        this.chanUserCount = count;
    }
}