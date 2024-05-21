package xyz.mjav.theqbot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteException;

import xyz.mjav.theqbot.exceptions.ChannelNotFoundException;
import xyz.mjav.theqbot.exceptions.DataBaseExecException;
import xyz.mjav.theqbot.exceptions.ItemErrorException;
import xyz.mjav.theqbot.exceptions.ItemExistsException;
import xyz.mjav.theqbot.exceptions.ItemNotFoundException;

/**
 * Singleton class to manage a Sqlite database.
 */
public class SqliteDb implements Database {

    private static Logger log = LogManager.getLogger("common-log");

    private static SqliteDb instance = null;

    private Connection connection;

    private Config config;

    private Protocol protocol;

    /**
     * Class constructor (private for singleton)
     * @param config
     */
    private SqliteDb(Config config){
        this.config = config;

        try { connection = DriverManager.getConnection("jdbc:sqlite:" + config.getDbSqlitePath()); }
        catch ( Exception e ) {
            log.fatal(String.format("SqliteDb/constructor: could not open the database"), e);
            System.exit(0);
        }
    }

    /**
     * Static method to create the singleton
     */
    public static synchronized SqliteDb getInstance(Config config) {
        if (instance == null) {
            instance = new SqliteDb(config);
            return instance;
        }
        else return instance;
    }

    /**
     * Returns the list of registered chans as an ArrayList<String>
     * @return registered chan list
     */
    @Override
    public Set<Channel> getRegChans(){
        String sql = null;
        ResultSet resultSet = null;

        Integer chanId;

        Timestamp regTS;

        Set<Channel> regChannels = new HashSet<>();

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT * FROM channels;";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {

                Topic topic;
                if (resultSet.getString("topic") == null) topic = new Topic("", "", new Timestamp());
                else topic = new Topic(resultSet.getString("topic"), "", new Timestamp());

                regTS     = new Timestamp(resultSet.getLong("regTS"));
                chanId    = resultSet.getInt("cid");

                Channel newChan = new Channel.Builder()
                    .name(resultSet.getString("name"))
                    .cServeId(chanId)
                    .registrationTS(regTS)
                    .flags(resultSet.getInt("chanflags"))
                    .cServeWelcomeMsg(resultSet.getString("welcome"))
                    .cServeRegisteredTopic(topic)
                    .cServeBanTime(resultSet.getInt("bantime"))
                    .cServeAutoLimit(resultSet.getInt("autolimit"))
                    .cServeMLock(resultSet.getString("mlock"))
                    .registered()
                    .build();

                newChan.setSuspendLastTS(getLastSuspendTS(Const.ENTITY_CHANNEL, chanId));
                newChan.setSuspendCount(getSuspendCount(Const.ENTITY_CHANNEL, chanId));
                newChan.setSuspendMessage(getSuspendMessage(Const.ENTITY_CHANNEL, chanId));

                newChan.setCServeBanList(getChanBei(Const.CHANBEI_BANS, newChan));
                newChan.setCServeExceptList(getChanBei(Const.CHANBEI_EXCEPTS, newChan));
                newChan.setCServeInviteList(getChanBei(Const.CHANBEI_INVITES, newChan));

                regChannels.add(newChan);
            }
        }
        catch (Exception e) { e.printStackTrace(); System.exit(0); }

        return regChannels;
    }

    //@Override
    private void getRegChans(Channel channel){
        String sql = null;
        ResultSet resultSet = null;

        Integer chanId;

        Timestamp regTS;

        Set<Channel> regChannels = new HashSet<>();

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT * FROM channels;";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {

                Topic topic;
                if (resultSet.getString("topic") == null) topic = new Topic("", "", new Timestamp());
                else topic = new Topic(resultSet.getString("topic"), "", new Timestamp());

                regTS     = new Timestamp(resultSet.getLong("regTS"));
                chanId    = resultSet.getInt("cid");

                Channel newChan = new Channel.Builder()
                    .name(resultSet.getString("name"))
                    .cServeId(chanId)
                    .registrationTS(regTS)
                    .flags(resultSet.getInt("chanflags"))
                    .cServeWelcomeMsg(resultSet.getString("welcome"))
                    .cServeRegisteredTopic(topic)
                    .cServeBanTime(resultSet.getInt("bantime"))
                    .cServeAutoLimit(resultSet.getInt("autolimit"))
                    .cServeMLock(resultSet.getString("mlock"))
                    .registered()
                    .build();

                newChan.setSuspendLastTS(getLastSuspendTS(Const.ENTITY_CHANNEL, chanId));
                newChan.setSuspendCount(getSuspendCount(Const.ENTITY_CHANNEL, chanId));
                newChan.setSuspendMessage(getSuspendMessage(Const.ENTITY_CHANNEL, chanId));

                newChan.setCServeBanList(getChanBei(Const.CHANBEI_BANS, newChan));
                newChan.setCServeExceptList(getChanBei(Const.CHANBEI_EXCEPTS, newChan));
                newChan.setCServeInviteList(getChanBei(Const.CHANBEI_INVITES, newChan));

                regChannels.add(newChan);
            }
        }
        catch (Exception e) { e.printStackTrace(); System.exit(0); }

        //return regChannels;
    }

    /**
     * Returns the list of user account names as an ArrayList<String>
     * @return list of user account names
     */
    @Override
    public Set<UserAccount> getRegUsers() {
        String sql = null;
        ResultSet resultSet = null;

        String name;
        String email;

        Integer uid;
        Integer userFlags;

        Timestamp userTs;

        Set<UserAccount> regUsers = new HashSet<>();

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT * FROM users;";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                userTs    = new Timestamp(resultSet.getLong("regTS"));
                name      = resultSet.getString("name");
                email     = resultSet.getString("email");
                uid       = resultSet.getInt("uid");
                userFlags = resultSet.getInt("userFlags");

                UserAccount newUser = new UserAccount.Builder()
                    .id(uid)
                    .name(name)
                    .flags(userFlags)
                    .email(email)
                    .registrationTS(userTs)
                    .build();

                newUser.setCertFP(getCertFPs(newUser));
                newUser.setChanlev(getUserChanlev(newUser));
                newUser.setSuspendLastTS(getLastSuspendTS(Const.ENTITY_USERACCOUNT, uid));
                newUser.setSuspendCount(getSuspendCount(Const.ENTITY_USERACCOUNT, uid));
                newUser.setSuspendMessage(getSuspendMessage(Const.ENTITY_USERACCOUNT, uid));

                regUsers.add(newUser);
            }

        }
        catch (SQLException e) { log.error(String.format("SqliteDb::getRegUsers: could not get the users list from the database"), e); }

        try (Statement statement = connection.createStatement()) {
        }
        catch (SQLException e) { log.error(String.format("SqliteDb::getRegUsers: could not get the suspend time from the database"), e); }

        return regUsers;
    }

    /**
     * Add a channel into the databse
     * @param channel channel name
     * @throws Exception
     */
    @Override
    public int addRegChan(Channel channel) throws ItemExistsException, Exception {
        String sql = null;
        ResultSet resultSet = null;

        int chanId = 0;

        Long unixTime;

        unixTime = Instant.now().getEpochSecond();

        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT name FROM channels WHERE name LIKE '%s' OR lcname LIKE '%s'", channel.getName(), channel.getName().toLowerCase());
            resultSet = statement.executeQuery(sql);

            if (resultSet.next() == true) throw new ItemExistsException("Cannot register the new channel '" + channel .getName()+ "' in the database because it already exists.");

        }
        catch (ItemExistsException e) { throw e; }
        catch (Exception e) { e.printStackTrace(); }

        try (Statement statement = connection.createStatement()) {
            sql = String.format("INSERT INTO channels (name, lcname, regTS, welcome, topic, bantime, autolimit, mlock) VALUES ('%s', '%s', '%s', '', '', '', '', '')", channel.getName(), channel.getName().toLowerCase(), unixTime.toString());
            statement.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseExecException("Error while registering the channel.");
        }

        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT cid FROM channels WHERE name LIKE '%s' OR lcname LIKE '%s'", channel.getName(), channel.getName().toLowerCase());
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) chanId = resultSet.getInt("cid");
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new DataBaseExecException("Error while registering the channel.");
        }

        return chanId;

    }

    /**
     * Removes a channel from the database (chanlev + channel)
     * @param channel channel name
     * @throws Exception
     */
    @Override
    public void delRegChan(Channel channel) throws ItemNotFoundException, Exception {
        String sql = null;
        ResultSet resultSet = null;

        /* Check if the channel exists if the database, if not throws an error */
        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT name FROM channels WHERE cid = '%s'", channel.getcServeId());
            resultSet = statement.executeQuery(sql);

            if (resultSet.next() == false) throw new ItemNotFoundException("Error dropping the channel '" + channel + "': it is not in the database.");
        }
        catch (ItemNotFoundException e) { throw e; }
        catch (Exception e) { e.printStackTrace(); }



        /* Channel exists in the db => we can delete the chanlev first */
        try (Statement statement = connection.createStatement()) {
            sql = String.format("DELETE FROM chanlev WHERE channelId = '%s';", channel.getcServeId());
            statement.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseExecException("Unhandled error while deleting the channel chanlev.");
        }

        /* Channel exists in the db => we can delete the channel */
        try (Statement statement = connection.createStatement()) {
            sql = String.format("DELETE FROM channels WHERE cid = '%s';", channel.getcServeId());
            statement.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseExecException("Unhandled error while deleting the channel.");
        }

    }

    /**
     * Add an user into the database
     * @param username user name
     * @param email user email
     * @param passwordHash hashed password (base64)
     * @param salt user salt (base64)
     * @throws Exception
     */
    @Override
    public void addUser(String username, String email, String passwordHash, String salt, Timestamp regTS, Integer userflags) throws ItemExistsException, Exception {
        String sql = null;
        ResultSet resultSet = null;

        /* Check if the user exists, if yes we throw an error */
        try (Statement statement = connection.createStatement()) {
            sql = "SELECT name FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);

            if (resultSet.next() == true) throw new ItemExistsException("Error registering the user '" + username + "': it already exists.");
        }
        catch (ItemExistsException e) { throw e; }
        catch (SQLException e) {
            log.error(String.format("SQL issue while registering the user %s", username), e);
            throw new DataBaseExecException();
        }

        try (Statement statement = connection.createStatement()) {
            sql = "INSERT INTO users (name, email, password, salt, regTS, userflags) VALUES ('" + username + "', '" + email + "', '" + passwordHash + "', '" + salt + "', '" + regTS + "', '" + userflags + "');";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { e.printStackTrace(); throw new DataBaseExecException(String.format("Error registering the user %s", username));}
    }

    @Override
    public void updateUserPassword(UserAccount user, String passwordHash, String salt) throws Exception {
        String sql = null;
        ResultSet resultSet = null;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT name FROM users WHERE lower(name)='" + user.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
        }
        catch (Exception e) { e.printStackTrace(); throw new ItemNotFoundException(String.format("User %s does not exist in the database", user.getName())); }

        try (Statement statement = connection.createStatement()) {
            sql = String.format("UPDATE users SET password='%s', salt='%s' WHERE lower(name)='%s';", passwordHash, salt, user.getName().toLowerCase());
            statement.executeUpdate(sql);

        }
        catch (Exception e) { e.printStackTrace(); throw new DataBaseExecException(String.format("Cannot update user %s password", user.getName())); }
    }

    /**
     * Returns an user as a Map<String, String> of field:value
     * @param useraccount user name
     * @return Map of the user as field:value
     * @throws Exception
     */
    @Override
    public Map<String, String> getUserInfoByNameCi(UserAccount useraccount) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        Map<String, String> user = new HashMap<String, String>();
        String name              = "";
        String password          = "";
        String salt              = "";
        Integer userId           = 0;
        String certfp            = "";

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT uid, name, password, salt FROM users WHERE lower(name)='" + useraccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();

            name = resultSet.getString("name");
            password = resultSet.getString("password");
            salt = resultSet.getString("salt");
            userId = resultSet.getInt("uid");
        }
        catch (Exception e) { e.printStackTrace(); throw new ItemNotFoundException("Error: cannot fetch the user '" + useraccount.getName() + "' in the database."); }

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT certfp FROM certfp WHERE userid='" + userId + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();

            certfp = resultSet.getString("certfp");
        }
        catch (Exception e) { e.printStackTrace(); throw new ItemNotFoundException("Error: cannot fetch the user '" + useraccount.getName() + "' in the database."); }

        if (name.isEmpty() == true) {
            throw new ItemNotFoundException("Error: cannot fetch the user '" + useraccount.getName() + "' in the database.");
        }

        user.put("name",     name);
        user.put("password", password);
        user.put("salt",     salt);
        user.put("id",       userId.toString());
        user.put("certfp",   certfp);

        return user;
    }

    /**
     * Returns an user chanlev as a Map<String, Integer> as chan:chanlev
     * @param useraccount
     * @return Map of chan:chanlev
     * @throws Exception
     */
    @Override
    public Map<String, Integer> getUserChanlev(UserAccount useraccount) throws ItemNotFoundException {
        String sql               = null;
        ResultSet resultSet      = null;
        Map<String, Integer> userChanlev = new HashMap<String, Integer>();
        Integer userId           = 0;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT uid FROM users WHERE lower(name)='" + useraccount.getName().toLowerCase() + "';";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.userId = " + userId + ";";
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                userChanlev.put(resultSet.getString("name"), resultSet.getInt("chanlev"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new ItemNotFoundException("Could not get user " + useraccount.getName() + " chanlev.");
        }
        return userChanlev;
    }

    /**
     * Returns the user chanlev for a specific channel
     * @param username user name
     * @param channel channel name
     * @return chanlev
     * @throws Exception
     */
    @Override
    public Integer getUserChanlev(UserAccount userAccount, Channel channel) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;

        Integer userChanlev      = 0;
        Integer userId           = 0;
        Integer chanId           = 0;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT uid FROM users WHERE lower(name)='" + userAccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            sql = "SELECT cid FROM channels WHERE lower(name)='" + channel.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            chanId = resultSet.getInt("cid");

            sql = "SELECT chanlev FROM chanlev WHERE userId = '" + userId + "' AND channelId = '" + chanId + "';";
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                userChanlev = resultSet.getInt("chanlev");
            }
        }
        catch (Exception e) {
            throw new ItemNotFoundException("Could not get user " + userAccount.getName() + " chanlev.");
        }
        return userChanlev;
    }

    /**
     * Returns the chanlev for a specific channel as a Map<String, Integer) mapping user:chanlev
     * @param channel channel name
     * @return user:chanlev map
     * @throws Exception
     */
    @Override
    public Map<String, Integer> getChanChanlev(Channel channel) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;

        Map<String, Integer> chanChanlev = new HashMap<String, Integer>();

        Integer chanlev;
        Integer chanId           = 0;

        String username;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT cid FROM channels WHERE lower(name)='" + channel.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            chanId = resultSet.getInt("cid");

            sql = "SELECT name, chanlev FROM users LEFT JOIN chanlev ON (chanlev.userId = users.uid) WHERE chanlev.channelId = " + chanId + ";";
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                username = resultSet.getString("name");
                chanlev = resultSet.getInt("chanlev");
                chanChanlev.put(username, chanlev);
            }
        }
        catch (Exception e) {
            log.error(String.format("SqliteDb::getChanChanlev: could not get channel %s chanlev", channel.getName()), e);
            throw new ItemNotFoundException("Error: could not get channel " + channel.getName() + " chanlev.");
        }

        return chanChanlev;
    }

    /**
     * Sets the chanlev for an user on a channel
     * @param username user name
     * @param channel channel name
     * @param chanlev chanlev
     * @throws Exception
     */
    @Override
    public void setUserChanlev(UserAccount userAccount, Channel channel, Integer chanlev) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;

        Integer userId           = 0;
        Integer channelId        = 0;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT uid FROM users WHERE lower(name)='" + userAccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            sql = "SELECT cid FROM channels WHERE lower(name)='" + channel.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            channelId = resultSet.getInt("cid");

            sql = "SELECT chanlev FROM chanlev WHERE channelId='"+ channelId +"' AND userId = " + userId + ";";
            resultSet = statement.executeQuery(sql);

            if(resultSet.next() == false) {
                if (chanlev != 0) {
                    sql = "INSERT INTO chanlev (channelId, userId, chanlev) VALUES ('" + channelId + "', '" + userId + "', '" + chanlev + "');";
                    statement.executeUpdate(sql);
                }
                else {
                    return;
                }
            }
            else {

                if (chanlev != 0) {
                    sql = "UPDATE chanlev SET chanlev='" + chanlev + "' WHERE channelId='" + channelId + "' AND userId='" + userId +"';";
                    statement.executeUpdate(sql);
                }
                else {
                    sql = "DELETE FROM chanlev WHERE channelId='" + channelId + "' AND userId='" + userId +"';";
                    statement.executeUpdate(sql);
                }
            }
        }
        catch (Exception e) { e.printStackTrace(); throw new DataBaseExecException("Error: could not set user " + userAccount.getName() + " chanlev."); }
    }

    /**
     * Deletes the chanlev of an user on a channel
     * @param username user name
     * @param channel channel
     * @throws Exception
     */
    @Override
    public void clearUserChanlev(String username, String channel) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;

        Integer userId           = 0;
        Integer channelId        = 0;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT uid FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            sql = "SELECT cid FROM channels WHERE lower(name)='" + channel.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            channelId = resultSet.getInt("cid");

            sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.userId = " + userId + ";";
            resultSet = statement.executeQuery(sql);


            if(resultSet.next() == false) {
                return;
            }
            else {
                sql = "DELETE FROM chanlev WHERE channelId='" + channelId + "' AND userId='" + userId +"';";
                statement.executeUpdate(sql);
            }
        }
        catch (Exception e) { e.printStackTrace(); throw new DataBaseExecException("Could not unset user " + username + " chanlev."); }

    }

    @Override
    public void clearUserChanlev(String username) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;

        Integer userId           = 0;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT uid FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            resultSet = statement.executeQuery(sql);

            sql = "DELETE FROM chanlev WHERE userId='" + userId + "';";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { e.printStackTrace(); throw new ItemNotFoundException("Could not unset user " + username + " chanlev."); }

    }

    /**
     * Clear the channel chanlev
     * @param channel channel name
     * @throws Exception
     */
    @Override
    public void clearChanChanlev(String channel) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;

        Integer channelId        = 0;

        try (Statement statement = connection.createStatement()) {
            channelId = protocol.getChannelNodeByNameCi(channel).getcServeId();

            sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.channelId = " + channelId + ";";
            resultSet = statement.executeQuery(sql);


            if(resultSet.next() == false) {
                return;
            }
            else {
                sql = "DELETE FROM chanlev WHERE channelId='" + channelId + "';";
                statement.executeUpdate(sql);
            }
        }
        catch (Exception e) { e.printStackTrace(); throw new DataBaseExecException("Could not unset " + channel + " chanlev."); }
    }

    /**
     * Returns the user flags
     * @param username user name
     * @return user flags
     * @throws Exception
     */
    @Override
    public Integer getUserFlags(UserAccount useraccount) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        Integer userFlags        = 0;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT userflags FROM users WHERE lower(name)='" + useraccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userFlags = resultSet.getInt("userflags");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ItemNotFoundException("Could not get user " + useraccount.getName() + " userflags.");
        }
        return userFlags;
    }

    /**
     * Returns the user id
     * @param username user name
     * @return user flags
     * @throws Exception
     */
    @Override
    public Integer getUserId(UserAccount useraccount) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        Integer uid               = 0;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT uid FROM users WHERE lower(name)='" + useraccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            uid = resultSet.getInt("uid");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ItemNotFoundException("Could not get user " + useraccount.getName() + " id.");
        }
        return uid;
    }

    /**
     * Returns the user flags
     * @param username user name
     * @return user flags
     * @throws Exception
     */
    @Override
    public Integer getChanFlags(Channel channel) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        Integer chanFlags        = 0;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT chanflags FROM channels WHERE lower(name)='" + channel.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            chanFlags = resultSet.getInt("chanflags");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseExecException("Could not get user " + channel.getName() + " flags.");
        }
        return chanFlags;
    }

    /**
     * Sets the userflags for an user
     * @param username user name
     * @param channel channel name
     * @param chanlev chanlev
     * @throws Exception
     */
    @Override
    public void setUserFlags(UserAccount userAccount, Integer userflags) throws Exception {
        String sql               = null;

        try (Statement statement = connection.createStatement()) {
            sql = "UPDATE users SET userflags='" + userflags + "' WHERE name='" + userAccount.getName() +"';";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { e.printStackTrace(); throw new DataBaseExecException("Error: could not set user " + userAccount.getName() + " flags."); }
    }

    /**
     * Returns the user email
     * @param username user name
     * @return user email
     * @throws Exception
     */
    @Override
    public String getUserEmail(UserAccount useraccount) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        String userEmail         = "";

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT email FROM users WHERE lower(name)='" + useraccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userEmail = resultSet.getString("userflags");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseExecException("Could not get user " + useraccount.getName() + " userflags.");
        }

        return userEmail;
    }

    /**
     * Returns the account registration TS
     * @param username user name
     * @return user account reg TS
     * @throws Exception
     */
    @Override
    public Long getUserRegTS(UserAccount useraccount) throws Exception {
        String sql               = null;
        String username          = useraccount.getName();
        ResultSet resultSet      = null;
        Long regTS               = 0L;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT regTS FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            regTS = resultSet.getLong("regTS");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseExecException("Could not get user " + username + " userflags.");
        }

        return regTS;
    }

    @Override
    public Long getUserLastAuthTS(UserAccount useraccount) throws Exception {
        String sql               = null;
        Integer userid           = useraccount.getId();
        ResultSet resultSet      = null;
        Long authTS              = 0L;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT authTS FROM authhistory WHERE userId='" + userid + "' AND authType IS NOT '" + Const.AUTH_TYPE_REAUTH_PLAIN + "' ORDER by authTS DESC LIMIT 0,1;";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            authTS = resultSet.getLong("authTS");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseExecException("Could not get user " + useraccount.getName() + " last auth ts.");
        }

        return authTS;
    }

    /**
     * Returns the user certfp
     * @param username
     * @return user certfp
     * @throws Exception
     */
    @Override
    public String getUserCertFP(UserAccount useraccount) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        String userCertFP         = "";

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT certfp FROM certfp WHERE userid='" + useraccount.getId() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userCertFP = resultSet.getString("certfp");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseExecException("Could not get user " + useraccount.getName() + " certfp.");
        }

        return userCertFP;
    }

    /**
     * Returns the channel AutoLimit value
     * @param channel channel node
     * @return autolimit value
     * @throws Exception
     */
    @Override
    public Integer getChanAutoLimit(Channel channel) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        Integer autoLimit        = 10;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT autolimit FROM channels WHERE lower(name)='" + channel.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            autoLimit = resultSet.getInt("autolimit");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new DataBaseExecException("Could not get channel " + channel.getName() + " autolimit.");
        }
        return autoLimit;
    }

    /**
     * Sets the channel AutoLimit value
     * @param channel channel node
     * @param autolimit autolimit value
     * @throws Exception
     */
    @Override
    public void setChanAutoLimit(Channel channel, Integer autolimit) throws Exception {
        String sql               = null;

        try (Statement statement = connection.createStatement()) {
            sql = "UPDATE channels SET autolimit='" + String.valueOf(autolimit) + "' WHERE lower(name)='" + channel.getName().toLowerCase() +"';";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { e.printStackTrace(); throw new DataBaseExecException("Error: could not set channel " + channel.getName() + " autolimit."); }
    }

    /**
     * Returns the user account id
     * @param username username
     * @return user id
     * @throws Exception
     */
    @Override
    public Integer getAccountId(String username) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        Integer userId           = 0;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT uid FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ItemNotFoundException("Could not get user " + username + " id.");
        }
        return userId;
    }

    /**
     * Returns the user account id
     * @param username username
     * @return user id
     * @throws Exception
     */
    @Override
    public Integer getChanId(Channel chan) throws Exception {

        String sql               = null;
        ResultSet resultSet      = null;
        Integer userId           = 0;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT cid FROM channels WHERE name='" + chan.getName() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("cid");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ItemNotFoundException("Could not get chan " + chan.getName() + " id.");
        }

        return userId;
    }

    @Override
    public Set<String> getCertFPs(UserAccount userAccount) throws ItemNotFoundException {
        String sql               = null;
        ResultSet resultSet      = null;
        Set<String> userCertFPList = new HashSet<>();

        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT certfp FROM certfp WHERE userid = '%s'", userAccount.getId());
            resultSet = statement.executeQuery(sql);
            while(resultSet.next() == true) {
                userCertFPList.add(resultSet.getString("certfp"));
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ItemNotFoundException("Could not get user " + userAccount.getName() + " certfp.");
        }
        return userCertFPList;
    }

    private UserAccount lookUpCertFP(String certfp) throws DataBaseExecException, ItemNotFoundException {
        String sql = "";
        Integer userid = null;
        UserAccount userAccount = null;
        ResultSet resultSet      = null;

        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT userid, certfp FROM certfp WHERE certfp = '%s'", certfp);
            resultSet = statement.executeQuery(sql);

            if (resultSet.next() == true) {
                userid = Integer.valueOf(resultSet.getString("userid"));
            }
            else throw new ItemNotFoundException("Protocol::lookUpCertFP: no user matching this certfp");

        }
        catch (SQLException e) {
            log.error(String.format("Protocol::lookUpCertFP: error returned from SQL operation: %s", e.getMessage()), e);
            throw new DataBaseExecException();
        }

        userAccount = UserAccount.getUser(userid);
        return userAccount;
    }

    private void existsCertFP(String certfp) throws DataBaseExecException, ItemExistsException {
        String sql = "";
        ResultSet resultSet      = null;

        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT certfp FROM certfp WHERE certfp = '%s'", certfp);
            resultSet = statement.executeQuery(sql);

            if (resultSet.next() == true) {
                throw new ItemExistsException("Protocol::existsCertFP: that certfp exists");
            }
        }
        catch (SQLException e) {
            log.error(String.format("Protocol::existsCertFP: error returned from SQL operation: %s", e.getMessage()), e);
            throw new DataBaseExecException();
        }

    }

    @Override
    public void addCertfp(UserAccount userAccount, String certfp) throws ItemErrorException, DataBaseExecException, ItemExistsException {
        String sql               = null;

        if (certfp.isEmpty() == true) throw new ItemErrorException("Protocol::addCertfp: provided CERTFP string is empty, thus refusing to add it to the database.");

        try { existsCertFP(certfp); }
        catch (ItemExistsException e) {
            log.error(String.format("Protocol::addCertfp: provided CERTFP string already exists in database."));
            throw e;
        }
        catch (DataBaseExecException e) {
            log.error(String.format("Protocol::addCertfp: could not check certfp existence in database."));
            throw e;
        }

        try (Statement statement = connection.createStatement()) {
            sql = String.format("INSERT INTO certfp (userid, certfp) VALUES ('%s', '%s');", userAccount.getId(), certfp);
            statement.executeUpdate(sql);
        }
        catch (SQLException e) {
            log.error(String.format("Protocol::addCertfp: error returned from SQL operation: %s", e.getMessage()), e);
            throw new DataBaseExecException();
        }
        catch (Exception e) {
            log.error(String.format("Protocol::addCertfp: error returned from SQL operation: %s", e.getMessage()), e);
            throw new DataBaseExecException();
        }
    }

    @Override
    public void removeCertfp(UserAccount userAccount, String certfp) throws ItemErrorException, DataBaseExecException {
        String sql               = null;

        if (certfp.isEmpty() == true) throw new ItemErrorException("Protocol::removeCertfp: provided CERTFP string is empty, thus refusing to remove it from the database.");

        try (Statement statement = connection.createStatement()) {
            sql = String.format("DELETE FROM certfp WHERE userid='%s' AND certfp='%s'", userAccount.getId(), certfp);
            statement.executeUpdate(sql);
        }
        catch (SQLException e) {
            log.error(String.format("Protocol::removeCertfp: error returned from SQL operation: %s", e.getMessage()), e);
            throw new DataBaseExecException();
        }
        catch (Exception e) {
            log.error(String.format("Protocol::removeCertfp: error returned from SQL operation: %s", e.getMessage()), e);
            throw new DataBaseExecException();
        }
    }

    @Override
    public String getWelcomeMsg(Channel channel) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        String welcomeMsg = "";

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT welcome FROM channels WHERE name='" + channel.getName() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            welcomeMsg = resultSet.getString("welcome");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return welcomeMsg;
    }

    @Override
    public String getTopic(Channel channel) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        String topic;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT topic FROM channels WHERE name='" + channel.getName() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            topic = resultSet.getString("topic");
        }
        catch (Exception e) {
            e.printStackTrace();
            topic = "";
        }
        return topic;
    }

    @Override
    public void addUserAuth(Nick userNode, Integer authType) throws ItemExistsException, Exception {
        String sql = null;

        /* Check if we are re-authing the user or not, in case of reauth there is no need to add the token in the db again */
        if (authType.equals(Const.AUTH_TYPE_REAUTH_PLAIN) == false) {
            try (Statement statement = connection.createStatement()) {
                //sql = "SELECT id FROM logins WHERE userSid='" + userNode.getUid() + "';";
                //resultSet = statement.executeQuery(sql);
                sql = String.format("DELETE FROM logins WHERE userSid='%s';", userNode.getUid());
                statement.executeUpdate(sql);

                //if (resultSet.next() == true) {
                //    log.warn("SqliteDb/addUserAuth: cannot reauth '" + userNode.getAccount().getId() + "' with '" + userNode.getUid() + "'.");
                //    throw new ItemExistsException("SqliteDb/addUserAuth: cannot reauth '" + userNode.getAccount().getId() + "' with '" + userNode.getUid() + "'.");
                //}
            }
            //catch (ItemExistsException e) { throw e; }
            catch (Exception e) { e.printStackTrace(); }

            try (Statement statement = connection.createStatement()) {
                sql = "INSERT INTO logins (userId, userSid, userTS) VALUES ('" + userNode.getAccount().getId() + "', '" + userNode.getUid() + "', '" + userNode.getUserTS().toString() + "');";
                statement.executeUpdate(sql);
            }
            catch (Exception e) {
                e.printStackTrace();
                log.error("SqliteDb/addUserAuth: cannot map login token '" + userNode.getUid() + "' -> '" + userNode.getAccount().getId() + "'.");
                throw new DataBaseExecException("SqliteDb/addUserAuth: cannot map login token '" + userNode.getUid() + "' -> '" + userNode.getAccount().getId() + "'.");
            }
        }

        try (Statement statement = connection.createStatement()) {
            sql = "INSERT INTO authhistory (userId, maskFrom, authType, authTS, sessionUuid) VALUES ('" + userNode.getAccount().getId() + "', '" + userNode.getIdent() + "@" + userNode.getRealHost() + "', '" + Integer.valueOf(authType) + "', '" + userNode.getAuthTS() + "', '" + userNode.getAuthUuid() + "');";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { e.printStackTrace(); throw new ItemExistsException("Error: could not add auth for user " + userNode.getAccount() + "."); }
    }

    @Override
    public void delUserAuth(Object usernode) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        String userSid           = "";

        if (usernode instanceof Nick) userSid = ((Nick) usernode).getUid();
        else if (usernode instanceof String) userSid = (String) usernode;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT userSid FROM logins WHERE userSid='" + userSid + "'";
            resultSet = statement.executeQuery(sql);

            if(resultSet.next() == true) {
                sql = "DELETE FROM logins WHERE userSid='" + userSid + "';";
                statement.executeUpdate(sql);
            }

        }
        catch (Exception e) { e.printStackTrace(); throw new ItemNotFoundException("Could not unauth " + userSid); }
    }

    @Override
    public void delUserAuth(Nick userNode, Integer deAuthType, String quitMsg) throws Exception {
        String sql = null;

        Long unixTime;

        this.delUserAuth(userNode);

        unixTime = Instant.now().getEpochSecond();

        try (Statement statement = connection.createStatement()) {
            sql = "UPDATE authhistory SET deAuthTS='" + unixTime + "', deAuthType='" + deAuthType + "', deAuthReason='" + quitMsg + "', sessionUuid='' WHERE sessionUuid='" + userNode.getAuthUuid().toString() + "'";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { e.printStackTrace(); throw new ItemNotFoundException("Error: could not close auth session for user " + userNode.getAccount() + "."); }
    }

    @Override
    public void delUserAccount(UserAccount userAccount) throws Exception {
        String sql               = null;
        try (Statement statement = connection.createStatement()) {
            sql = "DELETE FROM users WHERE lower(name)='" + userAccount.getName().toLowerCase() + "';";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { e.printStackTrace(); throw new ItemNotFoundException("Error: could not delete account for user " + userAccount.getName() + "."); }
    }

    @Override
    public void updateUserAuth(Nick userNode) throws ItemNotFoundException, Exception {
        String sql = null;

        try (Statement statement = connection.createStatement()) {
            sql = "UPDATE logins SET userTS='" + userNode.getUserTS().toString() + "' WHERE userSid='" + userNode.getUid() + "';";
            statement.executeUpdate(sql);

            sql = "UPDATE authhistory SET maskFrom='" + userNode.getIdent() + "' WHERE sessionUuid='" + userNode.getAuthUuid() + "';";
            statement.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ItemNotFoundException("Error: cannot update login token/history for user '" + userNode.getUid());
        }

    }

    /**
     * Returns the list of login tokens for the user account
     * @param userAccountName user account name
     * @return list of tokens as HM(Sid : TS)
     */
    @Override
    public Map<String, Integer> getUserLoginTokens(Integer userAccountId) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        Map<String, Integer> tokenList = new HashMap<String, Integer>();

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT userSid, userTS FROM logins WHERE lower(userId)='" + userAccountId + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            tokenList.put(resultSet.getString("userSid"), resultSet.getInt("userTS"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return tokenList;
    }

    /**
     * Returns the user account associated to a login token
     * @param user user node
     * @return user account
     * @throws Exception
     */
    @Override
    public UserAccount getUserLoginToken(Nick user) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        UserAccount account = null;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT userId FROM logins WHERE userSid='" + user.getUid() + "' AND userTS='" + user.getUserTS()  + "';";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            account = UserAccount.getUser(resultSet.getInt("userId"));
        }
        catch (Exception e) {
           // e.printStackTrace();
        }
        return account;
    }

    /**
     * Returns the list of user UID in the token list
     * @return HS of the UIDs
     * @throws Exception
     */
    @Override
    public Set<String> getUserLoginToken() {
        String sql                   = null;
        ResultSet resultSet          = null;
        Set<String> uidTokenList = new HashSet<>();

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT userSid FROM logins;";
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                uidTokenList.add(resultSet.getString("userSid"));
            }
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
        return uidTokenList;
    }

    /**
     * Sets the reference of the protocol object
     * @param protocol protocol object
     */
    @Override
    public void setProtocol(UnrealIRCd protocol) {
        this.protocol = protocol;
    }

    /**
     * Sets the chan flags inside the database
     * @param chan channel node
     * @param flags flags
     * @throws Exception
     */
    @Override
    public void setChanFlags(Channel chan, Integer flags) throws Exception {
        String sql               = null;

        try (Statement statement = connection.createStatement()) {
            sql = "UPDATE channels SET chanflags='" + flags + "' WHERE name='" + chan.getName() +"';";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { e.printStackTrace(); throw new ItemNotFoundException("Error: could not set chan " + chan.getName() + " flags."); }
    }

    @Override
    public void setWelcomeMsg(Channel chan, String msg) throws Exception {
        String sql               = null;

        try (Statement statement = connection.createStatement()) {
            sql = "UPDATE channels SET welcome='" + msg + "' WHERE name='" + chan.getName() +"';";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { e.printStackTrace(); throw new ItemNotFoundException("Error: could not set chan " + chan.getName() + " welcome message."); }
    }

    @Override
    public void setTopic(Channel chan, String msg) throws Exception {
        String sql               = null;

        try (Statement statement = connection.createStatement()) {
            sql = "UPDATE channels SET topic='" + msg + "' WHERE name='" + chan.getName() +"';";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { e.printStackTrace(); throw new DataBaseExecException("Error: could not set chan " + chan.getName() + " topic message."); }
    }

    /**
     * Cleans the "expired" tokens in the database, ie it deletes the stored user UID that does not match
     * user UIDs on the network, meaning that the user has disconnected and will not be able to recover his auth.
     * This is to prevent the table to grow indefinitely.
     * Do not run this method too often because sometimes users are temporarily "disconnected" (ie during splits).
     */
    @Override
    public void cleanInvalidLoginTokens() {
        Set<String> userUidTokens = null;
        Set<String> userUidNetwork = new HashSet<>();

        /* Getting the list of user UIDs in the DB */
        //try { userUidTokens = this.getUserLoginToken(); }
        //catch (Exception e) { log.error(String.format("SqliteDb::cleanInvalidLoginTokens: could not fetch the login tokens. "), e);}
        userUidTokens = this.getUserLoginToken();

        /* Getting the list of user UIDs on the network */
        Nick.getUsersList().forEach((user) -> {
            userUidNetwork.add(user.getUid());
        });

        /* Parsing the DB token list and removing the tokens corresponding of users present on the network  */
        /* We can remove the ones corresponding to nobody on the network */
        for(String userUid : userUidTokens) {
            if (userUidNetwork.contains(userUid) == false) {
                log.info("DB cleanup: deleting expired user UID " + userUid);
                try { this.delUserAuth(userUid); }
                catch (Exception e) { log.error(String.format("SqliteDb/cleanInvalidLoginTokens: could not remove expired login token for user UID %s: ", userUid), e); }
            }
        }
    }

    @Override
    public void openUserAuthSessionHistory(Nick user, Integer authType) throws Exception {
        String sql               = null;

        try (Statement statement = connection.createStatement()) {
            sql = "INSERT INTO authhistory (userId, maskFrom, authType, authTS, sessionUuid) VALUES ('" + user.getAccount().getId() + "', '" + user.getIdent() + "@" + user.getRealHost() + "', '" + Integer.valueOf(authType) + "', '" + user.getAuthTS() + "', '" + user.getAuthUuid() + "');";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { log.error(String.format("SqliteDb/openUserAuthSessionHistory: could not open auth session for account %s: ", user.getAccount().getName()), e); throw new ItemExistsException("Error: could not add auth for user " + user.getAccount().getName() + "."); }
    }

    @Override
    public void closeUserAuthSessionHistory(Nick user, Integer authType, Integer deAuthType, String quitMsg) throws Exception {
        String sql               = null;

        Long unixTime;

        unixTime = Instant.now().getEpochSecond();

        try (Statement statement = connection.createStatement()) {
            sql = "UPDATE authhistory SET deAuthTS='" + unixTime + "', deAuthType='" + deAuthType + "', deAuthReason='" + quitMsg + "', sessionUuid='' WHERE sessionUuid='" + user.getAuthUuid().toString() + "'";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { log.error(String.format("SqliteDb/closeUserAuthSessionHistory: could not close auth session for account %s: ", user.getAccount().getName()), e); throw new ItemNotFoundException("Error: could not close auth session for user " + user.getAccount().getName() + "."); }
    }

    @Override
    public void addSuspendHistory(Object node, Timestamp unixTime, UserAccount by, String reason) throws Exception {
        String sql               = null;
        Integer nodeType;
        //Long unixTime;

        //unixTime = Instant.now().getEpochSecond();

        if (node instanceof UserAccount) {
            UserAccount theNode = (UserAccount) node;
            nodeType = Const.ENTITY_USERACCOUNT;
            sql = String.format("INSERT INTO suspendhistory (itemId, itemType, suspendTS, reason, suspendById, suspendByName) VALUES ('%s', '%s', '%s', '%s', '%s', '%s');",
                                 theNode.getId(), nodeType, unixTime, reason, by.getId(), by.getName());
        }
        else if (node instanceof Channel) {
            Channel theNode = (Channel) node;
            nodeType = Const.ENTITY_CHANNEL;
            sql = String.format("INSERT INTO suspendhistory (itemId, itemType, suspendTS, reason, suspendById, suspendByName) VALUES ('%s', '%s', '%s', '%s', '%s', '%s');",
                                 theNode.getcServeId(), nodeType, unixTime, reason, by.getId(), by.getName());
        }
        else {
            log.error("SqliteDb/addSuspendHistory: unknown node type: " + node.getClass());
            throw new DataBaseExecException("Suspend add history: unknown entity");
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
        catch (Exception e) { log.error(String.format("SqliteDb/addSuspendHistory: could not add suspend history: "), e); throw new ItemNotFoundException("Error: could not add suspend history line for that entity."); }
    }

    @Override
    public void addUnSuspendHistory(Object node, UserAccount by) throws Exception {
        String sql = null;
        Integer nodeType;
        Long unixTime;

        unixTime = Instant.now().getEpochSecond();

        if (node instanceof UserAccount) {
            UserAccount theNode = (UserAccount) node;
            nodeType = Const.ENTITY_USERACCOUNT;
            sql = String.format("UPDATE suspendhistory SET unsuspendTS='%s', unsuspendById='%s', unsuspendByName='%s' WHERE itemId='%s' AND itemType='%s' AND unsuspendTS IS NULL;",
                                unixTime, by.getId(), by.getName(), theNode.getId(), nodeType);
        }
        else if (node instanceof Channel) {
            Channel theNode = (Channel) node;
            nodeType = Const.ENTITY_CHANNEL;
            sql = String.format("UPDATE suspendhistory SET unsuspendTS='%s', unsuspendById='%s', unsuspendByName='%s' WHERE itemId='%s' AND itemType='%s' AND unsuspendTS IS NULL;",
                                unixTime, by.getId(), by.getName(), theNode.getcServeId(), nodeType);
        }
        else {
            log.error("SqliteDb/addUnSuspendHistory: unknown node type: " + node.getClass());
            throw new ItemNotFoundException("UnSuspend add history: unknown entity");
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
        catch (Exception e) { log.error(String.format("SqliteDb/addUnSuspendHistory: could not update suspend history: "), e); throw new Exception("Error: could not update suspend history line for that entity."); }
    }

    @Override
    public List<Map<String, Object>> getAuthHistory(UserAccount userAccount) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;
        List<Map<String, Object>> authHist = new ArrayList<>();
        Map<String, Object> authLine;

        try (Statement statement = connection.createStatement()) {
            sql = "SELECT * FROM authhistory WHERE userId='" + userAccount.getId() + "' ORDER BY authTS DESC LIMIT 0,10;";
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                authLine = new HashMap<>();
                authLine.put("maskFrom", resultSet.getString("maskFrom"));
                authLine.put("authType", resultSet.getInt("authType"));
                authLine.put("authTS", resultSet.getLong("authTS"));
                authLine.put("deAuthType", resultSet.getInt("deAuthType"));
                authLine.put("deAuthTS", resultSet.getLong("deAuthTS"));
                authLine.put("deAuthReason", resultSet.getString("deAuthReason"));
                authHist.add(authLine);
            }
        }
        catch (Exception e) {
            log.error(String.format("SqliteDb/getAuthHistory: could not fetch auth history for account %s: ", userAccount.getName()), e);
            throw new ItemNotFoundException("Could not get auth history for account " + userAccount.getEmail() + ".");
        }
        return authHist;
    }

    private Timestamp getLastSuspendTS(Integer itemType, Integer itemId) {
        String sql               = null;
        ResultSet resultSet      = null;

        Timestamp ts = new Timestamp(0L);

        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT suspendTS FROM suspendhistory WHERE itemId='%s' AND itemType='%s' ORDER BY suspendTS DESC LIMIT 0,1;", itemId, itemType);
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            ts = new Timestamp(resultSet.getLong("suspendTS"));
        }
        catch (SQLException e) {
        }

        return ts;

    }

    private Integer getSuspendCount(Integer itemType, Integer itemId) {
        String sql               = null;
        ResultSet resultSet      = null;

        Integer count = 0;

        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT COUNT() as count FROM suspendhistory WHERE itemId='%s' AND itemType='%s';", itemId, itemType);
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            count = resultSet.getInt("count");
        }
        catch (SQLException e) {
        }

        return count;

    }

    private String getSuspendMessage(Integer itemType, Integer itemId) {
        String sql               = null;
        ResultSet resultSet      = null;

        String reason = "";

        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT reason FROM suspendhistory WHERE itemId='%s' AND itemType='%s' ORDER BY suspendTS DESC LIMIT 0,1;", itemId, itemType);
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            reason = resultSet.getString("reason");
        }
        catch (SQLException e) {
        }

        if (reason == null) reason = "";
        return reason;

    }

    public Map<String, Object> getSuspendHistory(Integer type, Integer id) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;

        Map<String, Object> history = new HashMap<>();

        Integer itemId;

        String idSql;

        List<Timestamp> suspendTS           = new ArrayList<>();
        List<Timestamp> unsuspendTS         = new ArrayList<>();
        List<Integer> suspendById       = new ArrayList<>();
        List<Integer> unsuspendById     = new ArrayList<>();
        List<String> suspendByName       = new ArrayList<>();
        List<String> unsuspendByName     = new ArrayList<>();
        List<String> suspendReason          = new ArrayList<>();
        List<Channel> suspendChan           = new ArrayList<>();
        List<UserAccount> suspendUser       = new ArrayList<>();

        history.put("suspendTS", suspendTS);
        history.put("unsuspendTS", unsuspendTS);
        history.put("suspendById", suspendById);
        history.put("unsuspendById", unsuspendById);
        history.put("suspendByName", suspendByName);
        history.put("unsuspendByName", unsuspendByName);
        history.put("suspendReason", suspendReason);

        switch(type) {
            case Const.ENTITY_CHANNEL:       history.put("itemId", suspendChan); break;
            case Const.ENTITY_USERACCOUNT:   history.put("itemId", suspendUser);  break;
            default: throw new Exception("Unknown or incompatible entity type");
        }

        try (Statement statement = connection.createStatement()) {
            if (id.equals(-1) == true) idSql = "%s";
            else idSql = id.toString();

            sql = String.format("SELECT itemId, suspendTS, suspendById, unsuspendTS, unsuspendById, suspendByName, unsuspendByName, reason FROM suspendhistory WHERE itemType='%s' AND itemId='%s';", type, idSql);
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {

                itemId = resultSet.getInt("itemId");

                switch(type) {
                    case Const.ENTITY_CHANNEL:      suspendChan.add(Channel.getRegChanById(id)); break;
                    case Const.ENTITY_USERACCOUNT:  suspendUser.add(UserAccount.getUser(itemId)); break;
                    default: break;
                }

                suspendTS.add(new Timestamp(resultSet.getLong("suspendTS")));
                unsuspendTS.add(new Timestamp(resultSet.getLong("unsuspendTS")));

                suspendById.add(resultSet.getInt("suspendById"));
                suspendByName.add(resultSet.getString("suspendByName"));

                unsuspendById.add(resultSet.getInt("unsuspendById"));
                unsuspendByName.add(resultSet.getString("unsuspendByName"));

                suspendReason.add(resultSet.getString("reason"));
            }
        }
        catch (SQLException e) {
            log.error("Could not retrieve suspend history", e);
            throw e;
        }

        return history;
    }

    @Override public void setChanMlock(Channel c, String mLock) throws ItemExistsException, Exception {
        String sql               = null;
        try (Statement statement = connection.createStatement()) {
            sql = String.format("UPDATE channels SET mlock='%s' WHERE cid='%s';", mLock, c.getcServeId());
            statement.executeUpdate(sql);
        }
        catch (SQLException e) { log.error(String.format("SqliteDb/setChanMlock: error", e)); throw new Exception(); }
    }

    @Override public String getChanMlock(Channel c) throws ItemExistsException, Exception {
        String sql               = null;
        String mlock = "";
        ResultSet resultSet;
        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT mlock FROM channels WHERE cid='%s';", c.getcServeId());
            resultSet = statement.executeQuery(sql);
            while(resultSet.next() == true) {
                mlock = resultSet.getString("mlock");
            }
        }
        catch (SQLException e) { }

        return mlock;
    }


    @Override public void addChanBei(int type, Channel c, Bei m, UserAccount u, Timestamp fromTS, Timestamp toTS, String r) throws ItemExistsException, Exception {
        String sql               = null;
        ResultSet resultSet;


        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT mask FROM chanBeiList WHERE channelId='%s' AND type='%s' AND mask='%s';", c.getcServeId(), String.valueOf(type), m.getString());
            resultSet = statement.executeQuery(sql);
            if(resultSet.next() == true) throw new ItemExistsException();
        }
        catch (SQLException e) { log.error(String.format("SqliteDb/addChanBei: could not precheck mask %s to channel %s list %s: %s", m, c, type, e)); throw new Exception("Error during check"); }

        try (Statement statement = connection.createStatement()) {
            sql = String.format("INSERT INTO chanBeiList (channelId, type, mask, userId, reason, toTS, fromTS) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s');",
            c.getcServeId(), String.valueOf(type), m.getString(), u.getId(), r, toTS.toString(), fromTS.toString());
            statement.executeUpdate(sql);
        }
        catch (SQLException e) { log.error(String.format("SqliteDb/addChanBei: could not add mask %s to channel %s list %s: %s", m, c, type, e)); throw new Exception("Error during insertion"); }
    }

    @Override public Map<Bei, Map<String, Object>> getChanBei(int type, Channel channel) throws Exception {
        String sql               = null;
        ResultSet resultSet      = null;

        Bei usermask;
        int userId;
        String reason;
        Timestamp toTS;
        Timestamp fromTS;
        Set<Bei> beis = new LinkedHashSet<>();

        Map<String, Object> uMaskProperties;
        Map<Bei, Map<String, Object>> userMasks = new LinkedHashMap<>();

        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT mask, reason, userId, fromTS, toTS FROM chanBeiList WHERE channelId='%s' AND type='%s' ORDER BY fromTS ASC;", channel.getcServeId(), String.valueOf(type));
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                uMaskProperties = new HashMap<>();
                usermask = Bei.create(resultSet.getString("mask"));
                beis.add(usermask);

                reason = resultSet.getString("reason");

                toTS = new Timestamp(resultSet.getLong("toTS"));

                fromTS = new Timestamp(resultSet.getLong("fromTS"));

                userId = resultSet.getInt("userId");

                uMaskProperties.put("author", userId);
                uMaskProperties.put("fromTS", fromTS);
                uMaskProperties.put("toTS", toTS);
                uMaskProperties.put("reason", reason);
                userMasks.put(usermask, uMaskProperties);

            }
        }
        catch (SQLException e) { log.error(String.format("SqliteDb/addChanBei: could not get masks for channel %s list %s: %s", channel, type, e)); throw new Exception("Error during fetch"); }

        return userMasks;
    }

    @Override public void removeChanBei(int type, Channel channel, Bei mask) throws Exception {
        String sql               = null;

        try (Statement statement = connection.createStatement()) {
            sql = String.format("DELETE FROM chanBeiList WHERE channelId='%s' AND type='%s' AND mask='%s';", channel.getcServeId(), String.valueOf(type), mask.getString());
            statement.executeUpdate(sql);
        }
        catch (SQLException e) { log.error(String.format("SqliteDb/removeChanBei: could not remove mask %s from channel %s list %s: %s", mask, channel, type, e)); throw new Exception("Error during deletion"); }
    }

    public void removeChanBei(int type, Channel channel) throws Exception {
        String sql               = null;

        try (Statement statement = connection.createStatement()) {
            sql = String.format("DELETE FROM chanBeiList WHERE channelId='%s' AND type='%s';", channel.getcServeId(), String.valueOf(type));
            statement.executeUpdate(sql);
        }
        catch (SQLException e) { log.error(String.format("SqliteDb/removeChanBei: could not remove masks from channel %s list %s: %s", channel, type, e)); throw new Exception("Error during deletion"); }
    }

    @Override public void renameChannel(Channel oldChan, Channel newChan) throws ChannelNotFoundException {
        String sql = null;

        try (Statement statement = connection.createStatement()) {
            sql = String.format("UPDATE channels SET name='%s' WHERE cid='%s'", newChan.getName(), oldChan.getcServeId());
            statement.executeUpdate(sql);
        }
        catch (SQLException e) {
            log.error(String.format("SqliteDb::renameChannel: error while changing channel name from %s to %s", oldChan, newChan), e);
            throw new ChannelNotFoundException(String.format("SqliteDb::renameChannel: error while changing channel name from %s to %s", oldChan, newChan));
        }

    }

    @Override public Set<NickAlias> getNickAliases(UserAccount account) {
        String sql;
        NickAlias nickAlias;
        Set<NickAlias> nickAliases = new HashSet<>();
        ResultSet resultSet;

        try (Statement statement = connection.createStatement()) {
            sql = String.format("SELECT * FROM userAliases WHERE uid = '%s';", account.getId());
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                nickAlias = new NickAlias.Builder().alias(resultSet.getString("nick")).created(new Timestamp(resultSet.getInt("addedTS"))).lastUsed(new Timestamp(resultSet.getInt("lastUsedTS"))).build();
                nickAliases.add(nickAlias);
                nickAlias = null;
            }
        }
        catch (SQLException e) {
            log.error(String.format("SqliteDb::getNickAliases: error while getting nick aliases list for user account %s", account), e);
            throw new ItemNotFoundException();
        }
        return nickAliases;
    }

    @Override public void addNickAlias(NickAlias nickAlias) throws ItemExistsException {
        String sql;

        try (Statement statement = connection.createStatement()) {
            sql = String.format("INSERT INTO userAliases (uid, nick, addedTS, lastUsedTS) VALUES ('%s', '%s', '%s', '%s');", nickAlias.getUserAccount().getId(), nickAlias.getAlias(), nickAlias.getCreatedTS(), nickAlias.getLastUsedTS());
            statement.executeUpdate(sql);
        }
        catch (SQLiteException e) {
            log.error(String.format("SqliteDb::addNickAlias: error while adding nick aliase '%s' for user account '%s' -- %s", nickAlias, nickAlias.getUserAccount(), e.getResultCode().code));
            throw new ItemExistsException();
        }
        catch (SQLException e) {
            log.error(String.format("SqliteDb::addNickAlias: --- error while adding nick aliase '%s' for user account '%s' -- %s", nickAlias, nickAlias.getUserAccount(), e.getErrorCode()));
            throw new ItemExistsException();
        }
    }


    @Override public void removeNickAlias(NickAlias nickAlias) {
        String sql;

        try (Statement statement = connection.createStatement()) {
            sql = String.format("DELETE FROM userAliases WHERE nick = '%s';", nickAlias.getAlias());
            statement.executeUpdate(sql);
        }
        catch (SQLException e) {
            log.error(String.format("SqliteDb::removeNickAlias: error while removing nick aliase '%s' -- %s", nickAlias, e.getErrorCode()), e);
            return;
        }
    }

    @Override public void updateNickAlias(NickAlias nickAlias) throws ItemExistsException {
        String sql;



        try (Statement statement = connection.createStatement()) {
            sql = String.format("UPDATE userAliases SET lastUsedTS='%s' WHERE nick='%s';", nickAlias.getLastUsedTS(), nickAlias.getAlias());
            statement.executeUpdate(sql);
        }
        catch (SQLiteException e) {
            log.error(String.format("SqliteDb::updateNickAlias: error while adding nick aliase '%s' for user account '%s' -- %s", nickAlias, nickAlias.getUserAccount(), e.getResultCode().code));
            throw new ItemExistsException();
        }
        catch (SQLException e) {
            log.error(String.format("SqliteDb::updateNickAlias: --- error while adding nick aliase '%s' for user account '%s' -- %s", nickAlias, nickAlias.getUserAccount(), e.getErrorCode()));
            throw new ItemExistsException();
        }
    }

}
