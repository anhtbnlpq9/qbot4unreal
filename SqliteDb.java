import java.sql.*;
import java.util.ArrayList;
//import org.sqlite.JDBC;
import java.util.HashMap;
import java.util.HashSet;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Exceptions.*;

public class SqliteDb {

    private static Logger log = LogManager.getLogger("common-log");

    private Connection connection;

    private Long unixTime;

    private Config config;

    private Protocol protocol;

    /**
     * Class constructor
     * @param config
     */
    public SqliteDb(Config config) {   
        this.config = config;

        try {
           //Class.forName("org.sqlite.JDBC");
           connection = DriverManager.getConnection("jdbc:sqlite:" + config.getDatabasePath());
        } catch ( Exception e ) {
            log.fatal(String.format("SqliteDb/constructor: could not open the database: "), e);
            System.exit(0);
        }
    }

    /**
     * Returns the list of registered chans as an ArrayList<String>
     * @return registered chan list
     */
    public HashMap<String, HashMap<String, Object>> getRegChans(){
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        HashMap<String, HashMap<String, Object>> regChannels = new HashMap<String, HashMap<String, Object>>();

        try { 
            statement = connection.createStatement();
            sql = "SELECT * FROM channels;";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                HashMap<String, Object> channelProperties = new HashMap<>();
                channelProperties.put("name",         resultSet.getString("name") );
                channelProperties.put("channelId",    resultSet.getInt("cid") );
                channelProperties.put("regTS",        resultSet.getLong("regTS") );
                channelProperties.put("chanflags",    resultSet.getInt("chanflags") );
                channelProperties.put("welcome",      resultSet.getString("welcome") );
                channelProperties.put("topic",        resultSet.getString("topic") );
                channelProperties.put("bantime",      resultSet.getInt("bantime") );
                channelProperties.put("autolimit",    resultSet.getInt("autolimit") );
                regChannels.put(resultSet.getString("name"), channelProperties);
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); System.exit(0); }
        
        return regChannels;
    }

    /**
     * Returns the list of user account names as an ArrayList<String>
     * @return list of user account names
     */
    public HashMap<String, HashMap<String, Object>> getRegUsers() {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        HashMap<String, HashMap<String, Object>> regUsers = new HashMap<String, HashMap<String, Object>>();

        try { 
            statement = connection.createStatement();
            sql = "SELECT * FROM users;";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                HashMap<String, Object> accountProperties = new HashMap<String, Object>();
                accountProperties.put("name",      resultSet.getString("name"));
                accountProperties.put("uid",       resultSet.getInt("uid"));
                accountProperties.put("userFlags", resultSet.getInt("userFlags"));
                accountProperties.put("email",     resultSet.getString("email"));
                accountProperties.put("certfp",    this.stringToHS(resultSet.getString("certfp")));
                accountProperties.put("name",      resultSet.getString("name"));
                accountProperties.put("regTS",      resultSet.getLong("regTS"));
                regUsers.put(resultSet.getString("name"), accountProperties);
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); System.exit(0); }
        
        return regUsers;
    }

    /**
     * Add a channel into the databse
     * @param channel channel name
     * @throws Exception
     */
    public void addRegChan(ChannelNode channel) throws Exception {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        try { 
            statement = connection.createStatement();
            sql = "SELECT name FROM channels WHERE lower(name)='" + channel.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            
        }
        catch (Exception e) { e.printStackTrace(); }

        if (resultSet.next() == true) {
            throw new Exception("Cannot register the new channel '" + channel .getName()+ "' in the database because it already exists.");
        }
        statement.close();

        try {
            statement = connection.createStatement();
            sql = "INSERT INTO channels (name, regTS) VALUES ('" + channel.getName() + "', '" + unixTime.toString() + "');";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Error while registering the channel."); 
        }
    }

    /**
     * Removes a channel from the database (chanlev + channel)
     * @param channel channel name
     * @throws Exception
     */
    public void delRegChan(String channel) throws Exception {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        /* Check if the channel exists if the database, if not throws an error */
        try { 
            statement = connection.createStatement();
            sql = "SELECT name FROM channels WHERE lower(name)='" + channel.toLowerCase() + "';";
            resultSet = statement.executeQuery(sql);
        }
        catch (Exception e) { e.printStackTrace(); }

        if (resultSet.next() == false) {
            throw new Exception("Error dropping the channel '" + channel + "': it is not registered.");
        }
        statement.close();

        /* Channel exists in the db => we can delete the chanlev first */
        try {
            statement = connection.createStatement();
            sql = "DELETE FROM chanlev WHERE channelId IN (SELECT channelId FROM chanlev INNER JOIN channels ON chanlev.channelId=channels.cid WHERE lower(channels.name)='" + channel.toLowerCase() + "');";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Unhandled error while deleting the channel chanlev."); 
        }
        statement.close();

        /* Channel exists in the db => we can delete the channel */
        try {
            statement = connection.createStatement();
            sql = "DELETE FROM channels WHERE lower(name)='" + channel .toLowerCase()+ "';";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Unhandled error while deleting the channel."); 
        }
        statement.close();
    }
    
    /**
     * Add an user into the database
     * @param username user name
     * @param email user email
     * @param passwordHash hashed password (base64)
     * @param salt user salt (base64)
     * @throws Exception
     */
    public void addUser(String username, String email, String passwordHash, String salt, Long regTS, Integer userflags) throws Exception {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        /* Check if the user exists, if yes we throw an error */
        try { 
            statement = connection.createStatement();
            sql = "SELECT name FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
        }
        catch (Exception e) { e.printStackTrace(); }

        if (resultSet.next() == true) { throw new Exception("Error registering the user '" + username + "': it already exists."); }
        statement.close();

        try { 
            statement = connection.createStatement();
            
            sql = "INSERT INTO users (name, email, password, salt, regTS, userflags) VALUES ('" + username + "', '" + email + "', '" + passwordHash + "', '" + salt + "', '" + regTS + "', '" + userflags + "');";
            statement.executeUpdate(sql);
            statement.close();
            
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    
    /**
     * Returns an user as a Map<String, String> of field:value
     * @param useraccount user name
     * @return Map of the user as field:value
     * @throws Exception
     */
    public HashMap<String, String> getUser(UserAccount useraccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        HashMap<String, String> user = new HashMap<String, String>();
        String name              = "";
        String password          = "";
        String salt              = "";
        Integer userId           = 0;
        String certfp            = "";

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT uid, name, password, salt, certfp FROM users WHERE lower(name)='" + useraccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            name = resultSet.getString("name");
            password = resultSet.getString("password");
            salt = resultSet.getString("salt");
            userId = resultSet.getInt("uid");
            certfp = resultSet.getString("certfp");
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: cannot fetch the user '" + useraccount.getName() + "' in the database."); }
        statement.close();

        if (name.isEmpty() == true) {
            throw new Exception("Error: cannot fetch the user '" + useraccount.getName() + "' in the database.");
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
    public HashMap<String, Integer> getUserChanlev(UserAccount useraccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        HashMap<String, Integer> userChanlev = new HashMap<String, Integer>();
        Integer userId           = 0;

        try { 
            statement = connection.createStatement();
            
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
        catch (Exception e) { 
            e.printStackTrace();
            throw new Exception("Could not get user " + useraccount.getName() + " chanlev.");  /* XXX: Normally we should not throw an exception but return an empty CL if it does not exist */
        }
        statement.close();
        return userChanlev;
    }

    /**
     * Returns the user chanlev for a specific channel
     * @param username user name
     * @param channel channel name
     * @return chanlev
     * @throws Exception
     */
    public Integer getUserChanlev(UserAccount userAccount, ChannelNode channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Integer userChanlev      = 0;
        Integer userId           = 0;
        Integer chanId           = 0;

        try { 
            statement = connection.createStatement();
            
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
            e.printStackTrace(); 
            throw new Exception("Could not get user " + userAccount.getName() + " chanlev."); /* XXX: Normally we should not throw an exception but return an empty CL if it does not exist */
        } 
        statement.close();
        return userChanlev;
    }

    /**
     * Returns the chanlev for a specific channel as a Map<String, Integer) mapping user:chanlev
     * @param channel channel name
     * @return user:chanlev map
     * @throws Exception
     */
    public HashMap<String, Integer> getChanChanlev(ChannelNode channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        HashMap<String, Integer> chanChanlev = new HashMap<String, Integer>();
        Integer chanlev;
        String username;
        Integer chanId           = 0;

        try { 
            statement = connection.createStatement();
            
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
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not get channel " + channel.getName() + " chanlev."); }
        statement.close();
        return chanChanlev;
    }

    /**
     * Sets the chanlev for an user on a channel
     * @param username user name
     * @param channel channel name
     * @param chanlev chanlev
     * @throws Exception
     */
    public void setUserChanlev(UserAccount userAccount, ChannelNode channel, Integer chanlev) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;

        Integer userId           = 0;
        Integer channelId        = 0;

        try { 
            statement = connection.createStatement();
            
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
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set user " + userAccount.getName() + " chanlev."); }
    }

    /**
     * Deletes the chanlev of an user on a channel
     * @param username user name
     * @param channel channel
     * @throws Exception
     */
    public void clearUserChanlev(String username, String channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;

        Integer userId           = 0;
        Integer channelId        = 0;

        try { 
            statement = connection.createStatement();
            
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
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Could not unset user " + username + " chanlev."); }

    }

    public void clearUserChanlev(String username) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;

        Integer userId           = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT uid FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            resultSet = statement.executeQuery(sql);

            sql = "DELETE FROM chanlev WHERE userId='" + userId + "';";
            statement.executeUpdate(sql);

            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Could not unset user " + username + " chanlev."); }

    }

    /**
     * Clear the channel chanlev
     * @param channel channel name
     * @throws Exception
     */
    public void clearChanChanlev(String channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;

        Integer channelId        = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT cid FROM channels WHERE lower(name)='" + channel.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            channelId = resultSet.getInt("cid");

            sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.channelId = " + channelId + ";";
            resultSet = statement.executeQuery(sql);


            if(resultSet.next() == false) {
                return;
            }
            else {
                sql = "DELETE FROM chanlev WHERE channelId='" + channelId + "';";
                statement.executeUpdate(sql);
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Could not unset " + channel + " chanlev."); }
    }

    /**
     * Returns the user flags
     * @param username user name
     * @return user flags
     * @throws Exception
     */
    public Integer getUserFlags(UserAccount useraccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Integer userFlags        = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT userflags FROM users WHERE lower(name)='" + useraccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userFlags = resultSet.getInt("userflags");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + useraccount.getName() + " userflags.");
        } 
        statement.close();
        return userFlags;
    }

    /**
     * Returns the user flags
     * @param username user name
     * @return user flags
     * @throws Exception
     */
    public Integer getChanFlags(ChannelNode channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Integer chanFlags        = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT chanflags FROM channels WHERE lower(name)='" + channel.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            chanFlags = resultSet.getInt("chanflags");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + channel.getName() + " flags.");
        } 
        statement.close();
        return chanFlags;
    }

    /**
     * Sets the userflags for an user
     * @param username user name
     * @param channel channel name
     * @param chanlev chanlev
     * @throws Exception
     */
    public void setUserFlags(UserAccount userAccount, Integer userflags) throws Exception {
        Statement statement      = null;
        String sql               = null;

        try { 
            statement = connection.createStatement();
            
            sql = "UPDATE users SET userflags='" + userflags + "' WHERE name='" + userAccount.getName() +"';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set user " + userAccount.getName() + " flags."); }
    }

    /**
     * Returns the user email
     * @param username user name
     * @return user email
     * @throws Exception
     */
    public String getUserEmail(UserAccount useraccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        String userEmail         = "";

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT email FROM users WHERE lower(name)='" + useraccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userEmail = resultSet.getString("userflags");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + useraccount.getName() + " userflags.");
        } 
        statement.close();
        return userEmail;
    }

    /**
     * Returns the account registration TS
     * @param username user name
     * @return user account reg TS
     * @throws Exception
     */
    public Long getUserRegTS(UserAccount useraccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        String username          = useraccount.getName();
        ResultSet resultSet      = null;
        Long regTS               = 0L;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT regTS FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            regTS = resultSet.getLong("regTS");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + username + " userflags.");
        } 
        statement.close();
        return regTS;
    }

    public Long getUserLastAuthTS(UserAccount useraccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        Integer userid           = useraccount.getId();
        ResultSet resultSet      = null;
        Long authTS              = 0L;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT authTS FROM authhistory WHERE userId='" + userid + "' AND authType IS NOT '" + Const.AUTH_TYPE_REAUTH + "' ORDER by authTS DESC LIMIT 0,1;";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            authTS = resultSet.getLong("authTS");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + useraccount.getName() + " last auth ts.");
        } 
        statement.close();
        return authTS;
    }

    /**
     * Returns the user certfp
     * @param username
     * @return user certfp
     * @throws Exception
     */
    public String getUserCertFP(UserAccount useraccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        String userCertFP         = "";

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT certfp FROM users WHERE lower(name)='" + useraccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userCertFP = resultSet.getString("userflags");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + useraccount.getName() + " userflags.");
        } 
        statement.close();
        return userCertFP;
    }

    /**
     * Returns the channel AutoLimit value
     * @param channel channel node
     * @return autolimit value
     * @throws Exception
     */
    public Integer getChanAutoLimit(ChannelNode channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Integer autoLimit        = 10;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT autolimit FROM channels WHERE lower(name)='" + channel.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            autoLimit = resultSet.getInt("autolimit");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get channel " + channel.getName() + " autolimit.");
        }
        statement.close();
        return autoLimit;
    }

    /**
     * Sets the channel AutoLimit value
     * @param channel channel node
     * @param autolimit autolimit value
     * @throws Exception
     */
    public void setChanAutoLimit(ChannelNode channel, Integer autolimit) throws Exception {
        Statement statement      = null;
        String sql               = null;

        try { 
            statement = connection.createStatement();
            
            sql = "UPDATE channels SET autolimit='" + String.valueOf(autolimit) + "' WHERE lower(name)='" + channel.getName().toLowerCase() +"';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set channel " + channel.getName() + " autolimit."); }
    }

    /**
     * Returns the user account id
     * @param username username
     * @return user id
     * @throws Exception
     */
    public Integer getAccountId(String username) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Integer userId           = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT uid FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + username + " id.");
        } 
        statement.close();
        return userId;
    }

    /**
     * Returns the user account id
     * @param username username
     * @return user id
     * @throws Exception
     */
    public Integer getChanId(ChannelNode chan) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Integer userId           = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT cid FROM channels WHERE name='" + chan.getName() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("cid");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get chan " + chan.getName() + " id.");
        } 
        statement.close();
        return userId;
    }


    public HashSet<String> getCertfp(UserAccount userAccount) throws Exception, MaxLimitReachedException {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        String certfp = "";
        try { 
            statement = connection.createStatement();
            
            sql = "SELECT certfp FROM users WHERE lower(name)='" + userAccount.getName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            certfp = resultSet.getString("certfp");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + userAccount.getName() + " certfp.");
        } 
        statement.close();
        return stringToHS(certfp);
    }

    public void addCertfp(UserAccount userAccount, String certfp) throws Exception {
        Statement statement      = null;
        String sql               = null;
        String certfpForDb = "";

        HashSet<String> userCertfp = this.getCertfp(userAccount);

        if (userCertfp.size() > config.getCServeAccountMaxCertFP()) { throw new MaxLimitReachedException("Client reached max certfp"); }

        try {
            userCertfp.add(certfp);
        }
        catch (Exception e) {
            e.printStackTrace();
            log.warn("SqliteDb/addCertfp: Could not readd certfp to user " + userAccount.getName() + " because already in the list");
        }

        certfpForDb = hashSetToString(userCertfp);

        try { 
            statement = connection.createStatement();

            sql = "UPDATE users SET certfp='" + certfpForDb + "' WHERE lower(name)='" + userAccount.getName().toLowerCase() + "'";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + userAccount.getName() + " certfp.");
        } 
        statement.close();
    }

    public void removeCertfp(UserAccount userAccount, String certfp) throws Exception {
        Statement statement      = null;
        String sql               = null;
        String certfpForDb = "";

        HashSet<String> userCertfp = this.getCertfp(userAccount);

        try {
            userCertfp.remove(certfp);
        }
        catch (Exception e) {
            e.printStackTrace();
            log.warn("SqliteDb/removeCertfp: Could not remove certfp to user " + userAccount.getName() + " because not in the list");
        }

        certfpForDb = hashSetToString(userCertfp);

        try { 
            statement = connection.createStatement();

            sql = "UPDATE users SET certfp='" + certfpForDb + "' WHERE lower(name)='" + userAccount.getName().toLowerCase() + "'";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + userAccount.getName() + " certfp.");
        } 
        statement.close();
    }

    public String getWelcomeMsg(ChannelNode channelNode) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        String welcomeMsg = "";

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT welcome FROM channels WHERE name='" + channelNode.getName() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            welcomeMsg = resultSet.getString("welcome");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
        } 
        statement.close();
        return welcomeMsg;
    }

    public String getTopic(ChannelNode channelNode) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        String topic;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT topic FROM channels WHERE name='" + channelNode.getName() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            topic = resultSet.getString("topic");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            topic = "";
        } 
        statement.close();
        return topic;
    }

    public void addUserAuth(UserNode userNode, Integer authType) throws Exception {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        /* Check if we are re-authing the user or not, in case of reauth there is no need to add the token in the db again */
        if (authType.equals(Const.AUTH_TYPE_REAUTH) == false) {
            try { 
                statement = connection.createStatement();
                sql = "SELECT id FROM logins WHERE userSid='" + userNode.getUid() + "';";
                resultSet = statement.executeQuery(sql);
                
            }
            catch (Exception e) { e.printStackTrace(); }

            if (resultSet.next() == true) {
                log.warn("SqliteDb/addUserAuth: cannot reauth '" + userNode.getAccount().getId() + "' with '" + userNode.getUid() + "'.");
                throw new Exception("SqliteDb/addUserAuth: cannot reauth '" + userNode.getAccount().getId() + "' with '" + userNode.getUid() + "'.");
            }
            statement.close();

            try {
                statement = connection.createStatement();
                sql = "INSERT INTO logins (userId, userSid, userTS) VALUES ('" + userNode.getAccount().getId() + "', '" + userNode.getUid() + "', '" + userNode.getUserTS().toString() + "');";
                statement.executeUpdate(sql);
                statement.close();
            }
            catch (Exception e) { 
                e.printStackTrace(); 
                log.error("SqliteDb/addUserAuth: cannot map login token '" + userNode.getUid() + "' -> '" + userNode.getAccount().getId() + "'.");
                throw new Exception("SqliteDb/addUserAuth: cannot map login token '" + userNode.getUid() + "' -> '" + userNode.getAccount().getId() + "'."); 
            }
        }

        try { 
            statement = connection.createStatement();
            
            sql = "INSERT INTO authhistory (userId, maskFrom, authType, authTS, sessionUuid) VALUES ('" + userNode.getAccount().getId() + "', '" + userNode.getIdent() + "@" + userNode.getRealHost() + "', '" + Integer.valueOf(authType) + "', '" + userNode.getAuthTS() + "', '" + userNode.getAuthUuid() + "');";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not add auth for user " + userNode.getAccount() + "."); }
    }

    public void delUserAuth(Object usernode) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        String userSid           = "";

        if (usernode instanceof UserNode) userSid = ((UserNode) usernode).getUid();
        else if (usernode instanceof String) userSid = (String) usernode;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT userSid FROM logins WHERE userSid='" + userSid + "'";
            resultSet = statement.executeQuery(sql);

            if(resultSet.next() == true) {
                sql = "DELETE FROM logins WHERE userSid='" + userSid + "';";
                statement.executeUpdate(sql);
            }

            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Could not unauth " + userSid + " chanlev."); }
    }

    public void delUserAuth(UserNode userNode, Integer deAuthType, String quitMsg) throws Exception {
        Statement statement      = null;
        String sql               = null;

        this.delUserAuth(userNode);

        unixTime = Instant.now().getEpochSecond();

        try { 
            statement = connection.createStatement();
            
            sql = "UPDATE authhistory SET deAuthTS='" + unixTime + "', deAuthType='" + deAuthType + "', deAuthReason='" + quitMsg + "', sessionUuid='' WHERE sessionUuid='" + userNode.getAuthUuid().toString() + "'";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not close auth session for user " + userNode.getAccount() + "."); }
    }

    public void delUserAccount(UserAccount userAccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        try { 
            statement = connection.createStatement();
            
            sql = "DELETE FROM users WHERE lower(name)='" + userAccount.getName().toLowerCase() + "';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not delete account for user " + userAccount.getName() + "."); }
    }

    public void updateUserAuth(UserNode userNode) throws Exception {
        Statement statement = null;
        String sql = null;

        try {
            statement = connection.createStatement();
            sql = "UPDATE logins SET userTS='" + userNode.getUserTS().toString() + "' WHERE userSid='" + userNode.getUid() + "';";
            statement.executeUpdate(sql);
            statement.close();

            statement = connection.createStatement();
            
            sql = "UPDATE authhistory SET maskFrom='" + userNode.getIdent() + "' WHERE sessionUuid='" + userNode.getAuthUuid() + "';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Error: cannot update login token/history for user '" + userNode.getUid()); 
        }

    }

    /**
     * Returns the list of login tokens for the user account
     * @param userAccountName user account name
     * @return list of tokens as HM(Sid : TS)
     */
    public HashMap<String, Integer> getUserLoginTokens(Integer userAccountId) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        HashMap<String, Integer> tokenList = new HashMap<String, Integer>();

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT userSid, userTS FROM logins WHERE lower(userId)='" + userAccountId + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            tokenList.put(resultSet.getString("userSid"), resultSet.getInt("userTS"));
        }
        catch (Exception e) { 
            e.printStackTrace(); 
        } 
        statement.close();

        return tokenList;
    }

    /**
     * Returns the user account associated to a login token
     * @param user user node
     * @return user account
     * @throws Exception
     */
    public UserAccount getUserLoginToken(UserNode user) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        UserAccount account = null;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT userId FROM logins WHERE userSid='" + user.getUid() + "' AND userTS='" + user.getUserTS()  + "';";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            account = protocol.getUserAccount(resultSet.getInt("userId"));
        }
        catch (Exception e) { 
            e.printStackTrace(); 
        } 
        statement.close();
        return account;
    }

    /**
     * Returns the list of user UID in the token list
     * @return HS of the UIDs
     * @throws Exception
     */
    public HashSet<String> getUserLoginToken() throws Exception {
        Statement statement          = null;
        String sql                   = null;
        ResultSet resultSet          = null;
        HashSet<String> uidTokenList = new HashSet<>();

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT userSid FROM logins;";
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                uidTokenList.add(resultSet.getString("userSid"));
            }
        }
        catch (Exception e) { 
            e.printStackTrace(); 
        } 
        statement.close();
        return uidTokenList;
    }

    /**
     * Sets the reference of the protocol object
     * @param protocol protocol object
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Sets the chan flags inside the database
     * @param chan channel node
     * @param flags flags
     * @throws Exception
     */
    public void setChanFlags(ChannelNode chan, Integer flags) throws Exception {
        Statement statement      = null;
        String sql               = null;

        try { 
            statement = connection.createStatement();
            
            sql = "UPDATE channels SET chanflags='" + flags + "' WHERE name='" + chan.getName() +"';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set chan " + chan.getName() + " flags."); }
    }

    public void setWelcomeMsg(ChannelNode chan, String msg) throws Exception {
        Statement statement      = null;
        String sql               = null;

        try { 
            statement = connection.createStatement();

            sql = "UPDATE channels SET welcome='" + msg + "' WHERE name='" + chan.getName() +"';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set chan " + chan.getName() + " welcome message."); }
    }

    public void setTopic(ChannelNode chan, String msg) throws Exception {
        Statement statement      = null;
        String sql               = null;

        try { 
            statement = connection.createStatement();

            sql = "UPDATE channels SET topic='" + msg + "' WHERE name='" + chan.getName() +"';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set chan " + chan.getName() + " topic message."); }
    }

    /**
     * Cleans the "expired" tokens in the database, ie it deletes the stored user UID that does not match
     * user UIDs on the network, meaning that the user has disconnected and will not be able to recover his auth.
     * This is to prevent the table to grow indefinitely.
     * Do not run this method too often because sometimes users are temporarily "disconnected" (ie during splits).
     */
    public void cleanInvalidLoginTokens() {
        HashSet<String> userUidTokens = null;
        HashSet<String> userUidNetwork = new HashSet<>();

        /* Getting the list of user UIDs in the DB */
        try {
            userUidTokens = this.getUserLoginToken();
        }
        catch (Exception e) { log.error(String.format("SqliteDb/cleanInvalidLoginTokens: could not fetch the login tokens: "), e);}
        
        /* Getting the list of user UIDs on the network */
        protocol.getUserList().forEach((userUid, userNode) -> {
            userUidNetwork.add(userUid);
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

    public void openUserAuthSessionHistory(UserNode user, Integer authType) throws Exception {
        Statement statement      = null;
        String sql               = null;

        try { 
            statement = connection.createStatement();
            
            sql = "INSERT INTO authhistory (userId, maskFrom, authType, authTS, sessionUuid) VALUES ('" + user.getAccount().getId() + "', '" + user.getIdent() + "@" + user.getRealHost() + "', '" + Integer.valueOf(authType) + "', '" + user.getAuthTS() + "', '" + user.getAuthUuid() + "');";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { log.error(String.format("SqliteDb/openUserAuthSessionHistory: could not open auth session for account %s: ", user.getAccount().getName()), e); throw new Exception("Error: could not add auth for user " + user.getAccount().getName() + "."); }
    }

    public void closeUserAuthSessionHistory(UserNode user, Integer authType, Integer deAuthType, String quitMsg) throws Exception {
        Statement statement      = null;
        String sql               = null;

        unixTime = Instant.now().getEpochSecond();

        try { 
            statement = connection.createStatement();
            
            sql = "UPDATE authhistory SET deAuthTS='" + unixTime + "', deAuthType='" + deAuthType + "', deAuthReason='" + quitMsg + "', sessionUuid='' WHERE sessionUuid='" + user.getAuthUuid().toString() + "'";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { log.error(String.format("SqliteDb/closeUserAuthSessionHistory: could not close auth session for account %s: ", user.getAccount().getName()), e); throw new Exception("Error: could not close auth session for user " + user.getAccount().getName() + "."); }
    }

    public void addSuspendHistory(Object node, String reason) throws Exception {
        Statement statement      = null;
        String sql               = null;
        Integer nodeType;
        Long unixTime;

        unixTime = Instant.now().getEpochSecond();

        if (node instanceof UserAccount) {
            UserAccount theNode = (UserAccount) node;
            nodeType = Const.ENTITY_USERACCOUNT;
            sql = "INSERT INTO suspendhistory (itemId, itemType, suspendTS, reason) VALUES ('" + theNode.getId() + "', '" + nodeType + "', '" + unixTime + "', '" + reason + "');";
        }
        else if (node instanceof ChannelNode) {
            ChannelNode theNode = (ChannelNode) node;
            nodeType = Const.ENTITY_CHANNEL;
            sql = "INSERT INTO suspendhistory (itemId, itemType, suspendTS, reason) VALUES ('" + theNode.getId() + "', '" + nodeType + "', '" + unixTime + "', '" + reason + "');";
        }
        else {
            log.error("SqliteDb/addSuspendHistory: unknown node type: " + node.getClass());
            throw new Exception("Suspend add history: unknown entity");
        }

        try { 
            statement = connection.createStatement();
            
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { log.error(String.format("SqliteDb/addSuspendHistory: could not add suspend history: "), e); throw new Exception("Error: could not add suspend history line for that entity."); }
    }

    public void addUnSuspendHistory(Object node) throws Exception {
        Statement statement      = null;
        String sql               = null;

        Integer nodeType;

        Long unixTime;

        unixTime = Instant.now().getEpochSecond();

        if (node instanceof UserAccount) {
            UserAccount theNode = (UserAccount) node;
            nodeType = Const.ENTITY_USERACCOUNT;
            sql = "UPDATE suspendhistory SET unsuspendTS='" + unixTime + "' WHERE itemId='"+ theNode.getId() +"' AND itemType='" + nodeType + "' AND unsuspendTS IS NULL;";
        }
        else if (node instanceof ChannelNode) {
            ChannelNode theNode = (ChannelNode) node;
            nodeType = Const.ENTITY_CHANNEL;
            sql = "UPDATE suspendhistory SET unsuspendTS='" + unixTime + "' WHERE itemId='"+ theNode.getId() +"' AND itemType='" + nodeType + "' AND unsuspendTS IS NULL;";
        }
        else {
            log.error("SqliteDb/addUnSuspendHistory: unknown node type: " + node.getClass());
            throw new Exception("UnSuspend add history: unknown entity");
        }

        try { 
            statement = connection.createStatement();
            
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { log.error(String.format("SqliteDb/addUnSuspendHistory: could not update suspend history: "), e); throw new Exception("Error: could not update suspend history line for that entity."); }
    }


    public ArrayList<HashMap<String, Object>> getAuthHistory(UserAccount userAccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        ArrayList<HashMap<String, Object>> authHist = new ArrayList<>();
        HashMap<String, Object> authLine;

        try { 
            statement = connection.createStatement();
            
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
            throw new Exception("Could not get auth history for account " + userAccount.getEmail() + ".");
        }
        statement.close();
        return authHist;
    }

    private HashSet<String> stringToHS(String list) {
        HashSet<String> outputHS = new HashSet<>();
        if (list == null || list.isEmpty() == true) { list = ""; }
        String[] listItems = list.split(",", 0);

        for(String item : listItems) {
            outputHS.add(item);
        }
        return outputHS;
    }

    private String hashSetToString(HashSet<String> inputHS) {
        return String.join(",", inputHS);
    }

}
