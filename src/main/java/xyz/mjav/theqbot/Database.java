package xyz.mjav.theqbot;

import java.util.List;
import java.util.Map;
import java.util.Set;

import xyz.mjav.theqbot.exceptions.ChannelNotFoundException;
import xyz.mjav.theqbot.exceptions.DataBaseExecException;
import xyz.mjav.theqbot.exceptions.ItemErrorException;
import xyz.mjav.theqbot.exceptions.ItemExistsException;
import xyz.mjav.theqbot.exceptions.ItemNotFoundException;

public interface Database {

    /**
     * Returns the list of registered chans as an ArrayList<String>
     * @return registered chan list
     */
    Set<Channel> getRegChans();

    /**
     * Returns the list of user account names as an ArrayList<String>
     * @return list of user account names
     */
    Set<UserAccount> getRegUsers();

    /**
     * Add a channel into the databse
     * @param channel channel name
     * @throws Exception
     */
    int addRegChan(Channel channel) throws Exception;

    /**
     * Removes a channel from the database (chanlev + channel)
     * @param channel channel name
     * @throws Exception
     */
    void delRegChan(Channel channel) throws Exception;

    /**
     * Add an user into the database
     * @param username user name
     * @param email user email
     * @param passwordHash hashed password (base64)
     * @param salt user salt (base64)
     * @throws Exception
     */
    void addUser(String username, String email, String passwordHash, String salt, Timestamp regTS, Integer userflags) throws Exception;

    void updateUserPassword(UserAccount user, String passwordHash, String salt) throws Exception;

    /**
     * Returns an user as a Map<String, String> of field:value
     * @param useraccount user name
     * @return Map of the user as field:value
     * @throws Exception
     */
    Map<String, String> getUserInfoByNameCi(UserAccount useraccount) throws Exception;

    /**
     * Returns an user chanlev as a Map<String, Integer> as chan:chanlev
     * @param useraccount
     * @return Map of chan:chanlev
     * @throws Exception
     */
    Map<String, Integer> getUserChanlev(UserAccount useraccount) throws Exception;

    /**
     * Returns the user chanlev for a specific channel
     * @param username user name
     * @param channel channel name
     * @return chanlev
     * @throws Exception
     */
    Integer getUserChanlev(UserAccount userAccount, Channel channel) throws Exception;

    /**
     * Returns the chanlev for a specific channel as a Map<String, Integer) mapping user:chanlev
     * @param channel channel name
     * @return user:chanlev map
     * @throws Exception
     */
    Map<String, Integer> getChanChanlev(Channel channel) throws Exception;

    /**
     * Sets the chanlev for an user on a channel
     * @param username user name
     * @param channel channel name
     * @param chanlev chanlev
     * @throws Exception
     */
    void setUserChanlev(UserAccount userAccount, Channel channel, Integer chanlev) throws Exception;

    /**
     * Deletes the chanlev of an user on a channel
     * @param username user name
     * @param channel channel
     * @throws Exception
     */
    void clearUserChanlev(String username, String channel) throws Exception;

    void clearUserChanlev(String username) throws Exception;

    /**
     * Clear the channel chanlev
     * @param channel channel name
     * @throws Exception
     */
    void clearChanChanlev(String channel) throws Exception;

    /**
     * Returns the user flags
     * @param username user name
     * @return user flags
     * @throws Exception
     */
    Integer getUserFlags(UserAccount useraccount) throws Exception;

    /**
     * Returns the user flags
     * @param username user name
     * @return user flags
     * @throws Exception
     */
    Integer getChanFlags(Channel channel) throws Exception;

    /**
     * Sets the userflags for an user
     * @param username user name
     * @param channel channel name
     * @param chanlev chanlev
     * @throws Exception
     */
    void setUserFlags(UserAccount userAccount, Integer userflags) throws Exception;

    /**
     * Returns the user email
     * @param username user name
     * @return user email
     * @throws Exception
     */
    String getUserEmail(UserAccount useraccount) throws Exception;

    /**
     * Returns the account registration TS
     * @param username user name
     * @return user account reg TS
     * @throws Exception
     */
    Long getUserRegTS(UserAccount useraccount) throws Exception;

    Long getUserLastAuthTS(UserAccount useraccount) throws Exception;

    /**
     * Returns the user certfp
     * @param username
     * @return user certfp
     * @throws Exception
     */
    String getUserCertFP(UserAccount useraccount) throws Exception;

    /**
     * Returns the channel AutoLimit value
     * @param channel channel node
     * @return autolimit value
     * @throws Exception
     */
    Integer getChanAutoLimit(Channel channel) throws Exception;

    /**
     * Sets the channel AutoLimit value
     * @param channel channel node
     * @param autolimit autolimit value
     * @throws Exception
     */
    void setChanAutoLimit(Channel channel, Integer autolimit) throws Exception;

    /**
     * Returns the user account id
     * @param username username
     * @return user id
     * @throws Exception
     */
    Integer getAccountId(String username) throws Exception;

    /**
     * Returns the user account id
     * @param username username
     * @return user id
     * @throws Exception
     */
    Integer getChanId(Channel chan) throws Exception;

    Set<String> getCertFPs(UserAccount userAccount) throws ItemNotFoundException;

    void addCertfp(UserAccount userAccount, String certfp) throws ItemErrorException, DataBaseExecException, ItemExistsException;

    void removeCertfp(UserAccount userAccount, String certfp) throws ItemErrorException, DataBaseExecException;

    String getWelcomeMsg(Channel channelNode) throws Exception;

    String getTopic(Channel channelNode) throws Exception;

    void addUserAuth(Nick userNode, Integer authType) throws Exception;

    void delUserAuth(Object usernode) throws Exception;

    void delUserAuth(Nick userNode, Integer deAuthType, String quitMsg) throws Exception;

    void delUserAccount(UserAccount userAccount) throws Exception;

    void updateUserAuth(Nick userNode) throws ItemNotFoundException, Exception;

    /**
     * Returns the list of login tokens for the user account
     * @param userAccountName user account name
     * @return list of tokens as HM(Sid : TS)
     */
    Map<String, Integer> getUserLoginTokens(Integer userAccountId) throws Exception;

    /**
     * Returns the user account associated to a login token
     * @param user user node
     * @return user account
     * @throws Exception
     */
    UserAccount getUserLoginToken(Nick user) throws Exception;

    /**
     * Returns the list of user UID in the token list
     * @return HS of the UIDs
     * @throws Exception
     */
    Set<String> getUserLoginToken() throws Exception;

    /**
     * Sets the reference of the protocol object
     * @param protocol protocol object
     */
    void setProtocol(UnrealIRCd protocol);

    /**
     * Sets the chan flags inside the database
     * @param chan channel node
     * @param flags flags
     * @throws Exception
     */
    void setChanFlags(Channel chan, Integer flags) throws Exception;

    void setWelcomeMsg(Channel chan, String msg) throws Exception;

    void setTopic(Channel chan, String msg) throws Exception;

    /**
     * Cleans the "expired" tokens in the database, ie it deletes the stored user UID that does not match
     * user UIDs on the network, meaning that the user has disconnected and will not be able to recover his auth.
     * This is to prevent the table to grow indefinitely.
     * Do not run this method too often because sometimes users are temporarily "disconnected" (ie during splits).
     */
    void cleanInvalidLoginTokens();

    void openUserAuthSessionHistory(Nick user, Integer authType) throws Exception;

    void closeUserAuthSessionHistory(Nick user, Integer authType, Integer deAuthType, String quitMsg)
            throws Exception;

    void addSuspendHistory(Object node, Timestamp unixTime, UserAccount by, String reason) throws Exception;

    void addUnSuspendHistory(Object node, UserAccount by) throws Exception;

    List<Map<String, Object>> getAuthHistory(UserAccount userAccount) throws Exception;

    Integer getUserId(UserAccount useraccount) throws Exception;

    Map<String, Object> getSuspendHistory(Integer type, Integer id) throws Exception;

    void renameChannel(Channel oldChan, Channel newChan) throws ChannelNotFoundException;

    void addChanBei(int type, Channel c, Bei m, UserAccount u, Timestamp from, Timestamp to, String r) throws ItemExistsException, Exception;
    Map<Bei, Map<String, Object>> getChanBei(int type, Channel channel) throws Exception;
    void removeChanBei(int type, Channel channel, Bei mask) throws Exception;
    void setChanMlock(Channel c, String mLock) throws ItemExistsException, Exception;
    String getChanMlock(Channel c) throws ItemExistsException, Exception;

    Set<NickAlias> getNickAliases(UserAccount account);
    void addNickAlias(NickAlias nickAlias) throws ItemExistsException;
    void removeNickAlias(NickAlias nickAlias);
    void updateNickAlias(NickAlias nickAlias) throws ItemExistsException;

}