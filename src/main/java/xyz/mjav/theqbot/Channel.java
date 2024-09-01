package xyz.mjav.theqbot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import xyz.mjav.theqbot.exceptions.ChannelNotFoundException;
import xyz.mjav.theqbot.exceptions.ItemExistsException;
import xyz.mjav.theqbot.exceptions.ItemNotFoundException;

/**
 * Channel class
 * Class to store network channels.
 * @author me
 */

public class Channel extends Account implements Comparable<Channel> {

    /*
     * Static fields
     */

    /** List of network channels */
    private static Set<Channel> channelList = new HashSet<>();

    /** List of network channels (by name) */
    private static Map<String, Channel> channelListByName = new HashMap<>();

    /** List of registered channels */
    private static Set<Channel> channelRegList = new HashSet<>();

    /** List of registered channels (by name) */
    private static Map<String, Channel> channelRegListByName = new HashMap<>();

    /** List of registered channels (by id) */
    private static Map<Integer, Channel> channelRegListById = new HashMap<>();


    /*
     * Instance fields
     */

    /* ** Network related ** */

    /** Channel user count */
    private Integer userCount;

    /** Channel current topic */
    private Topic topic;

    /** Channel locked modes (mode -> parameter) */
    private String mLockPolicy;

    /** Channel current modes, maps mode -> parameter */
    private Map<String, String> channelModes;

    /** User present on the channel and their modes */
    private Map<Nick, Set<String>> chanUserList;

    /** Channel banned users list */
    private Set<Bei> banList;

    /** Channel excepted users list */
    private Set<Bei> exceptList;

    /** Channel invited users (invex) list */
    private Set<Bei> inviteList;

    /** Channel last activity time */
    protected Timestamp lastActivity;



    /* ** CService related ** */

    /** Chanserv channel locked modes (mode -> parameter) */
    private String cServeMLockModes;

    /** Chanserv stored topic */
    private Topic cServeSavedTopic;

    /** Chanserv channel registration status */
    private Boolean isCServeRegistered;

    /** Chanserv chanlev. Maps username -> chanlev value */
    private Map<String, Integer> chanlev; // TODO: change username to account id or UserAccount

    /** Chanserv channel flags (CHANFLAGS) */
    private Integer cServeFlags;

    /** Chanserv autolimit value */
    private Integer cServeAutoLimit;

    /** Chanserv bantime value */
    private Integer cServeBanTime;

    /** Chanserv channel id */
    private Integer cServeId;

    /** Chanserv welcome message */
    private String cServeWelcomeMsg;

    /** Chanserv persistent ban list */
    private BeiList cServeBanList;

    /** Chanserv persistent except list */
    private BeiList cServeExceptList;

    /** Chanserv persistent invite list */
    private BeiList cServeInviteList;

    /**
     * Adds an channel to the memory channel list
     * @param c channel
     * @throws ItemExistsException when the channel is already in the list
     */
    public static void addChannel(Channel c) throws ItemExistsException {

        if(channelList.contains(c) == true)  throw new ItemExistsException("The channel is already registered on the network.");

        channelList.add(c);
        channelListByName.put(c.name.toLowerCase(), c);
    }

    /**
     * Removes an channel from the memory channel list
     * @param c channel
     * @throws ItemNotFoundException when the channel to remove is not in the list
     */
    public static void removeChannel(Channel c) throws ItemNotFoundException {

        if(channelList.contains(c) == false) throw new ItemNotFoundException("The channel is not registered on the network.");

        channelList.remove(c);
        channelListByName.remove(c.name.toLowerCase());
    }

    /**
     * Adds an registered channel to the memory channel list
     * @param c channel
     * @throws ItemExistsException when the channel is already in the list
     */
    public static void addRegChannel(Channel c) throws ItemExistsException {

        if(channelRegList.contains(c) == true)  throw new ItemExistsException("The channel is already registered.");

        channelRegList.add(c);
        channelRegListByName.put(c.name.toLowerCase(), c);
        channelRegListById.put(c.cServeId, c);
    }

    /**
     * Removes an registered channel from the memory channel list
     * @param c channel
     * @throws ItemNotFoundException when the channel to remove is not in the list
     */
    public static void removeRegChannel(Channel c) throws ItemNotFoundException {

        if(channelRegList.contains(c) == false) throw new ItemNotFoundException("The channel is not registered.");

        channelRegList.remove(c);
        channelRegListByName.remove(c.name);
        channelRegListById.remove(c.cServeId);
    }

    /**
     * Returns the channel by its name (case insensitive)
     * @param name channel name
     * @return channel
     * @throws ChannelNotFoundException when the user account is not found
     */
    public static Channel getChanByNameCi(String name) throws ChannelNotFoundException {
        if (channelListByName.containsKey(name.toLowerCase()) == false) throw new ChannelNotFoundException();
        return channelListByName.get(name.toLowerCase());
    }

    /**
     * Returns a Set of channel list
     * @return a copy of channel list Set
     */
    public static Set<Channel> getChanList() {
        return new HashSet<Channel>(channelList);
    }

    /**
     * Returns a Set of registered channel list
     * @return a copy of registered channel list Set
     */
    public static Set<Channel> getRegChanList() {
        return new HashSet<Channel>(channelRegList);
    }

    /**
     * Returns a registered channel by its registration id
     * @param i channel id
     * @return channel
     * @throws ItemNotFoundException when the channel is not found
     */
    public static Channel getRegChanById(Integer i) throws ItemNotFoundException {
        if (channelRegListById.containsKey(i) == false) throw new ItemNotFoundException();
        return channelRegListById.get(i);
    }

    /**
     * Returns the channel by its registration name (case insensitive)
     * @param name registered channel name
     * @return channel
     * @throws ItemNotFoundException when the channel is not found
     */
    public static Channel getRegChanByNameCi(String name) throws ItemNotFoundException {
        if (channelRegListByName.containsKey(name.toLowerCase()) == false) throw new ItemNotFoundException();
        return channelRegListByName.get(name.toLowerCase());
    }

    /**
     * Sets the registered channel list
     * @param l registered channel list Set
     */
    public static void setRegChanList(Set<Channel> l) {
        channelRegList = new HashSet<Channel>(l);

        /* Then populates the other lists */
        channelRegList.forEach(
            (u) -> {
                channelRegListByName.put(u.name.toLowerCase(), u);
                channelRegListById.put(u.cServeId, u);
            }
        );
    }

    public static boolean isChan(String s) {
        try { getChanByNameCi(s); }
        catch (ItemNotFoundException e) { return false; }

        return true;
    }

    public static boolean isRegChan(String s) {
        try { getRegChanByNameCi(s); }
        catch (ItemNotFoundException e) { return false; }

        return true;
    }

    /**
     * Builder class
     */
    public static class Builder extends Account.Builder {

        private Integer userCount                  = 0;
        private Integer flags                      = 0;
        private Integer cServeAutoLimit            = 10;
        private Integer cServeBanTime              = 0;
        private Integer cServeId                   = 0;

        private String cServeWelcomeMsg            = "";

        private Topic topic                        = new Topic();
        private Topic cServeSavedTopic             = new Topic();

        private Boolean isCServeRegistered         = false;

        private Map<String, Integer> chanlev       = new HashMap<>();

        private String mLockModes       = "";
        private String cServeMLockModes = "";
        private Map<String, String> channelModes     = new TreeMap<>();

        private Map<Nick, Set<String>> chanUserList = new HashMap<>();

        private Set<Bei> banList    = new LinkedHashSet<>();
        private Set<Bei> exceptList = new LinkedHashSet<>();
        private Set<Bei> inviteList = new LinkedHashSet<>();

        private BeiList cServeBanList    = new BeiList();
        private BeiList cServeExceptList = new BeiList();
        private BeiList cServeInviteList = new BeiList();

        private Timestamp lastActivity  = new Timestamp(0L);

        public Builder userCount(Integer val) {
            this.userCount = val;
            return this;
        }

        public Builder flags(Integer val) {
            this.flags = val;
            return this;
        }

        public Builder cServeAutoLimit(Integer val) {
            this.cServeAutoLimit = val;
            return this;
        }

        public Builder cServeId(Integer val) {
            this.cServeId = val;
            return this;
        }

        public Builder name(String val) {
            super.name(val);
            return this;
        }

        public Builder cServeRegisteredTopic(Topic val) {
            this.cServeSavedTopic = val;
            return this;
        }

        public Builder cServeWelcomeMsg(String val) {
            this.cServeWelcomeMsg = val;
            return this;
        }

        public Builder cServeBanTime(Integer val) {
            this.cServeBanTime = val;
            return this;
        }

        public Builder registered() {
            this.isCServeRegistered = true;
            return this;
        }

        public Builder topic(Topic val) {
            this.topic = val;
            return this;
        }

        public Builder registrationTS(Timestamp val) {
            super.registrationTS(val);
            return this;
        }

        public Builder chanlev(Map<String, Integer> val) {
            chanlev = val;
            return this;
        }

        public Builder mLockModes(String val) {
            mLockModes = val;
            return this;
        }

        public Builder chanUserList(Map<Nick, Set<String>> val) {
            chanUserList = val;
            return this;
        }

        public Builder cServeBanList(BeiList s) {
            cServeBanList = s;
            return this;
        }

        public Builder cServeExceptList(BeiList s) {
            cServeExceptList = s;
            return this;
        }

        public Builder cServeInviteList(BeiList s) {
            cServeInviteList = s;
            return this;
        }

        public Builder cServeMLock(String val) {
            cServeMLockModes = val;
            return this;
        }

        public Channel build() {
            return new Channel(this);
        }
    }

    /**
     * Channel constructor
     * @param builder
     */
    private Channel(Builder builder) {
        super(builder);
        this.userCount              = builder.userCount;
        this.cServeFlags            = builder.flags;
        this.cServeAutoLimit        = builder.cServeAutoLimit;
        this.cServeBanTime          = builder.cServeBanTime;
        this.cServeId               = builder.cServeId;
        this.topic                  = builder.topic;
        this.cServeSavedTopic       = builder.cServeSavedTopic;
        this.cServeWelcomeMsg       = builder.cServeWelcomeMsg;
        this.isCServeRegistered     = builder.isCServeRegistered;
        this.chanlev                = builder.chanlev;
        this.mLockPolicy            = builder.mLockModes;
        this.cServeMLockModes       = builder.cServeMLockModes;
        this.chanUserList           = builder.chanUserList;
        this.channelModes           = builder.channelModes;
        this.banList                = builder.banList;
        this.exceptList             = builder.exceptList;
        this.inviteList             = builder.inviteList;
        this.cServeBanList          = builder.cServeBanList;
        this.cServeExceptList       = builder.cServeExceptList;
        this.cServeInviteList       = builder.cServeInviteList;
        this.lastActivity           = builder.lastActivity;
    }

    public void addMode(String mode, String param) {
        this.channelModes.put(mode, param);
    }

    /**
     * Adds a mode to the user on the channel
     * @param user user
     * @param mode mode
     * @param param mode parameter
     * @throws ItemNotFoundException if the user is not on the channel
     */
    public void addUserMode(Nick user, String mode, String param) throws ItemNotFoundException {

        Set<String> userModes;

        try { userModes = this.getUserModes(user); }
        catch (ItemNotFoundException e) { throw e; }

        userModes.add(mode);
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

    /**
     * Removes a mode to the user on the channel
     * @param user user
     * @param mode mode
     * @throws ItemNotFoundException if the user is not on the channel
     */
    public void delUserMode(Nick user, String mode) throws ItemNotFoundException {

        Set<String> userModes;

        try { userModes = this.getUserModes(user); }
        catch (ItemNotFoundException e) { throw e; }

        userModes.remove(mode);
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public void setRegistered(Boolean isRegistered) {
        this.isCServeRegistered = isRegistered;
    }

    public void setChanlev(Map<String, Integer> chanChanlev) {
        this.chanlev = chanChanlev;
    }

    public void setChanlev(Nick user, Integer chanlev) {
        if (chanlev != 0) {
            if (this.chanlev.containsKey(user.getAccount().getName()) == true) { this.chanlev.replace(user.getAccount().getName(), chanlev); }
            else { this.chanlev.put(user.getAccount().getName(), chanlev); }
        }
        else {
            if (this.chanlev.containsKey(user.getAccount().getName()) == true) { this.chanlev.remove(user.getAccount().getName()); }
        }
    }

    public void clearChanChanlev(Nick user) {
        setChanlev(user, 0);
    }

    public void addBanList(Bei str) {
        this.banList.add(str);
    }

    public void addExceptList(Bei str) {
        this.exceptList.add(str);
    }

    public void addInviteList(Bei str) {
        this.inviteList.add(str);
    }

    public void delBanList(Bei str) {

        this.banList.remove(str);
    }

    public void delExceptList(Bei str) {
        this.exceptList.remove(str);
    }

    public void delInviteList(Bei str) {
        this.inviteList.remove(str);
    }

    public void setBanList(Set<Bei> banList) {
        this.banList = banList;
    }

    public void setExceptList(Set<Bei> exceptList) {
        this.exceptList = exceptList;
    }

    public void setInviteList(Set<Bei> inviteList) {
        this.inviteList = inviteList;
    }

    public Set<Bei> getBanList() {
        return this.banList;
    }

    public Set<Bei> getExceptList() {
        return this.exceptList;
    }

    public Set<Bei> getInviteList() {
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

    public Topic getTopic() {
        return this.topic;
    }

    public Boolean isRegistered() {
        return this.isCServeRegistered;
    }

    public Map<String, Integer> getChanlev() {
        return this.chanlev;
    }

    public Map<String, Integer> getChanlevWoutPersonalFlags() {
        Map<String, Integer> chanlevMap = new HashMap<>();

        chanlev.forEach( (user, chanlev) -> {
            if (Flags.stripChanlevPersonalFlags(chanlev) != 0) {
                chanlevMap.put(user, chanlev);
            }
        } );

        return chanlevMap;
    }

    public Integer getcServeId() {
        return this.cServeId;
    }

    public void setcServeId(Integer id) {
        this.cServeId = id;
    }

    public Integer getUserCount() {
        return this.userCount;
    }

    public void setUserCount(Integer count) {
        this.userCount = count;
    }

    public void setcServeFlags(Integer chanFlags) {
        this.cServeFlags = chanFlags;
    }

    public Integer getcServeFlags() {
        return this.cServeFlags;
    }

    public Map<Nick, Set<String>> getUsers() {
        return this.chanUserList;
    }

    /**
     * Adds the Nick to the channel
     * @param user
     * @throws ItemExistsException
     */
    public void addUser(Nick user) throws ItemExistsException {

        if (this.chanUserList.containsKey(user) == true) throw new ItemExistsException("User already is inside channel list");
        this.chanUserList.put(user, new TreeSet<>());

        this.userCount++;
    }

    /**
     * Removes the Nick from the channel
     * @param user
     */
    public void delUser(Nick user) {
        this.chanUserList.remove(user);

        this.userCount--;
    }

    public Nick getUser(Nick user) {
        if (this.chanUserList.containsKey(user)) return user;
        else return null;
    }

    public Nick getUser(String nick) {
        var wrapper = new Object() { Nick userNode = null; };
        this.chanUserList.forEach( (user, mode) -> {
            if (user.getNick().equals(nick) == true) { wrapper.userNode = user; }
        });

        return wrapper.userNode;
    }

    public Integer getcServeAutoLimit() {
        return this.cServeAutoLimit;
    }

    /**
     * Returns a Map of the modes of the user on the channel. If the user has no mode, the returned Map is empty.
     * @param user user
     * @return Map of the modes
     * @throws ItemNotFoundException if the user is not on the channel
     */
    public Set<String> getUserModes(Nick user) throws ItemNotFoundException {
        if(this.chanUserList.containsKey(user) == false) throw new ItemNotFoundException(String.format("Nick %s not on the channel %s", user, this));
        return this.chanUserList.get(user);
    }

    public void setcServeAutoLimit(Integer autolimit) {
        this.cServeAutoLimit = autolimit;
    }

    public void setcServeWelcomeMsg(String string) {
        this.cServeWelcomeMsg = string;
    }

    public void setcServeRegisteredTopic(Topic string) {
        this.cServeSavedTopic = string;
    }

    public String getcServeWelcomeMsg() {
        return this.cServeWelcomeMsg;
    }

    /**
     * Sets the Chanserv ban time
     * @param time ban time
     */
    public void setcServeBanTime(Integer time) {
        this.cServeBanTime = time;
    }

    /**
     * Gets the Chanserv channel ban time
     * @return ban time
     */
    public Integer getcServeBanTime() {
        return this.cServeBanTime;
    }

    /**
     * Gets the channel registered topic
     * @return
     */
    public Topic getCServeRegisteredTopic() {
        return this.cServeSavedTopic;
    }

    /**
     * Sets the channel MLOCK map
     * @param mLockMap MLOCK map
     */
    public void setMLockModes(String s) {
        this.mLockPolicy = s;
    }

    /**
     * Sets the chanserv channel MLOCK map
     * @param s MLOCK map
     */
    public void setcServeMLockModes(String s) {
        this.cServeMLockModes = s;
    }

    /**
     *
     * @param mode
     * @param param
     */
    public void addMLock(String mode, String param) {

    }

    /**
     *
     * @param mode
     */
    public void delMLock(String mode) {

    }

    /**
     *
     * @param mode
     * @param param
     */
    public void addcServeMLock(String mode, String param) {

    }

    /**
     *
     * @param mode
     */
    public void delcServeMLock(String mode) {

    }

    /**
     * Returns the current MLOCK modes of the channel
     * @return current channel MLOCK
     */
    public String getMLockModes() {
        return this.mLockPolicy;
    }

    /*
    public String getMLockModes(String mode) {
        String modeParam;
        modeParam = this.mLockModes.get(mode);
        if (modeParam == null) return "";
        return modeParam;
    }*/

    /**
     * Returns the ChanServ MLOCK modes of the channel
     * @return chanserv channel MLOCK
     */
    public String getcServeMLockModes() {
        return this.cServeMLockModes;
    }

    /*
    public String getcServeMLockModes(String mode) {
        String modeParam;
        modeParam = this.cServeMLockModes.get(mode);
        if (modeParam == null) return "";
        return modeParam;
    }*/

    public BeiList getcServeBanList() {
        return this.cServeBanList;
    }

    public BeiList getcServeExceptList() {
        return this.cServeExceptList;
    }

    public BeiList getcServeInviteList() {
        return this.cServeInviteList;
    }

    public BeiProperty getcServeBanList(Bei m) {
        return this.cServeBanList.get(m);
    }

    public BeiProperty getcServeExceptList(Bei m) {
        return this.cServeExceptList.get(m);
    }

    public BeiProperty getcServeInviteList(Bei m) {
        return this.cServeInviteList.get(m);
    }

    public synchronized void addCServeBanList(Bei m, UserAccount u, String r, Timestamp fromTS, Timestamp toTS) {
        Map<String, Object> uMaskProperties = new HashMap<>();
        BeiProperty beiProp = new BeiProperty.Builder().author(u.getId()).fromTS(fromTS).toTS(toTS).reason(r).build();

        uMaskProperties.put("author", u.getId());
        uMaskProperties.put("fromTS", fromTS);
        uMaskProperties.put("toTS", toTS);
        uMaskProperties.put("reason", r);
        //this.cServeBanList.put(m, uMaskProperties);
        this.cServeBanList.put(m, beiProp);

    }

    public synchronized void addCServeExceptList(Bei m, UserAccount u, String r, Timestamp fromTS, Timestamp toTS) {
        Map<String, Object> uMaskProperties = new HashMap<>();
        BeiProperty beiProp = new BeiProperty.Builder().author(u.getId()).fromTS(fromTS).toTS(toTS).reason(r).build();

        uMaskProperties.put("author", u.getId());
        uMaskProperties.put("fromTS", fromTS);
        uMaskProperties.put("toTS", toTS);
        uMaskProperties.put("reason", r);
        //this.cServeExceptList.put(m, uMaskProperties);
        this.cServeBanList.put(m, beiProp);
    }

    public synchronized void addCServeInviteList(Bei m, UserAccount u, String r, Timestamp fromTS, Timestamp toTS) {
        Map<String, Object> uMaskProperties = new HashMap<>();
        BeiProperty beiProp = new BeiProperty.Builder().author(u.getId()).fromTS(fromTS).toTS(toTS).reason(r).build();

        uMaskProperties.put("author", u.getId());
        uMaskProperties.put("fromTS", fromTS);
        uMaskProperties.put("toTS", toTS);
        uMaskProperties.put("reason", r);
        //this.cServeInviteList.put(m, uMaskProperties);
        this.cServeBanList.put(m, beiProp);
    }

    public void removeCServeBanList(Bei m) {
        this.cServeBanList.remove(m);
    }

    public void removeCServeExceptList(Bei m) {
        this.cServeExceptList.remove(m);
    }

    public void removeCServeInviteList(Bei m) {
        this.cServeInviteList.remove(m);
    }

    public void setCServeBanList(BeiList list) {
        this.cServeBanList = list;
    }

    public void setCServeExceptList(BeiList list) {
        this.cServeExceptList = list;
    }

    public void setCServeInviteList(BeiList list) {
        this.cServeInviteList = list;
    }

    public void setLastActivity(Timestamp ts) {
        this.lastActivity = ts;
    }

    public Timestamp getLastActivity() {
        return this.lastActivity;
    }

    @Override public int compareTo(Channel c) {
        return this.name.compareTo(c.getName());
    }


}
