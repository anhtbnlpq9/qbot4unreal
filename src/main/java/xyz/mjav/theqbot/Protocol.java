package xyz.mjav.theqbot;

import java.util.Map;

import xyz.mjav.theqbot.exceptions.ItemNotFoundException;
import xyz.mjav.theqbot.exceptions.ParseException;

public interface Protocol {

    /**
     * Returns the list of registered channels as a HM
     * @return list of registered channels
     */
    //Map<String, Channel> getRegChanList();

    /**
     * Returns the channel node corresponding to the name
     * @param channelName channel name
     * @return channel node
     */
    Channel getChannelNodeByNameCi(String channelName) throws ItemNotFoundException;

    void setClientRef(Client client);

    void setCService(CService cservice);

    void setOService(OService oservice);

    /**
     * Send a privmsg to an user
     * @param client client
     * @param from originator usernode
     * @param to target usernode
     * @param msg message string
     */
    void sendPrivMsg(Nick from, Nick to, String msg);

    /**
     * Sends a notice from an user to another user
     * @param client client
     * @param from originator usernode
     * @param to target usernode
     * @param msg message string
     */
    void sendNotice(Nick from, Nick to, String msg);

    void sendInvite(Nick to, Channel chanNode);

    /**
     * Makes the bot join a channel
     * @param client client
     * @param who originator usernode
     * @param chan target channel
     */
    void chanJoin(Nick who, Channel chan);

    /**
     * Make the bot leaves the channel
     * @param client client
     * @param who usernode originator
     * @param chan channelnode
     */
    void chanPart(Nick who, Channel chanUserPart);

    /**
     * Make the bot leaves the channel (with message)
     * @param client client
     * @param who usernode originator
     * @param chan channelnode
     */
    void chanPart(Nick who, Channel chanUserPart, String s);

    /**
     * Kicks an user from a channel with the reason
     * @param client client
     * @param who originator
     * @param chan channelnode
     * @param target usernode
     * @param reason reason
     */
    void chanKick(Nick who, Channel chan, Nick target, String reason);

    /**
     * Generic method to set a mode
     * @param client
     * @param who
     * @param target
     * @param modes
     * @param parameters
     * @throws Exception
     */
    void setMode(Object fromWho, Channel chan, String modes, String modesParams);

    /**
     * Sets a mode from an user to an user
     * @param client client
     * @param fromWho usernode
     * @param toTarget usernode
     * @param modes modes
     * @param parameters parameters
     * @throws Exception
     */
    void setSvsMode(Nick fromWho, Nick toTarget, String modes, String parameters) throws Exception;

    /**
     * Sets a mode from a server to an user
     * @param client client
     * @param fromWho servernode
     * @param toTarget usernode
     * @param modes modes
     * @param parameters parameters
     * @throws Exception
     */
    void setSvsMode(Server fromWho, Nick toTarget, String modes, String parameters) throws Exception;

    /**
     * @param client client
     * @param fromWho servernode
     * @param toTarget channelnode
     * @param modes modes
     * @param parameters parameters
     * @throws Exception
     */
    void setMode(Channel toTarget, String modes, String parameters) throws Exception;

    void setTopic(Nick from, Channel to, String topic);

    void setMlock(Server fromWho, Channel toTarget, String modes);

    void sendQuit(Nick from, String msg);

    void sendUid(Nick from);

    void chgHostVhost(Nick toTarget, String vhost);

    void chgHost(Nick toTarget, String vhost);

    Boolean hasFeature(String feature);

    //Map<String, Channel> getChanList();

    String getPeerId();

    void sendSvsLogin(Nick user);

    void sendSvsLogin(Nick user, UserAccount account);

    void sendRaw(Nick fromNick, String str);

    void getResponse(String raw) throws Exception;
    //Supplier<String> getResponse(String raw) throws Exception;

    void addRegChan(Channel c);

    void delRegChan(Channel c);

    void sendServerIdent();

    void sendSetHost(Nick user, String vhost);

    void sendWallops(Nick from, String msg);

    void sendWallops(Server from, String msg);

    Map<String, Map<String, String>> parseChanModes(String str) throws ParseException;

    void sendMlock(Server from, Channel channel, String mlock);

    String getUserChanJoinMode();

    boolean isBursting();

    String userModeToTxt(String s);
    String chanModeToTxt(String s);

    String getChanMode(String m);

    String getUserMode(String m);
}
