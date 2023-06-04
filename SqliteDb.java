import java.sql.*;
import java.util.ArrayList;
//import org.sqlite.JDBC;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.time.Instant;

public class SqliteDb {
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
           Class.forName("org.sqlite.JDBC");
           connection = DriverManager.getConnection("jdbc:sqlite:" + config.getDatabasePath());
        } catch ( Exception e ) {
           e.printStackTrace();
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

        //HashMap<String,UserAccount> regUsers = new HashMap<String,UserAccount>();
        ArrayList<String> certfpList = new ArrayList<>();
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
     * @param owner owner user id //TODO to be deleted because ownership can be handled by chanlev
     * @throws Exception
     */
    public void addRegChan(ChannelNode channel, UserAccount owner) throws Exception {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        try { 
            statement = connection.createStatement();
            sql = "SELECT name FROM channels WHERE lower(name)='" + channel.getChanName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            
        }
        catch (Exception e) { e.printStackTrace(); }

        if (resultSet.next() == true) {
            throw new Exception("Cannot register the new channel '" + channel .getChanName()+ "' in the database because it already exists.");
        }
        statement.close();

        try {

            Integer userAccountId = 0;
            statement = connection.createStatement();
            sql = "SELECT uid FROM users WHERE name='" + owner.getUserAccountId() + "';";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                userAccountId = resultSet.getInt("uid");
            }
            unixTime = Instant.now().getEpochSecond();
            statement.close();

            statement = connection.createStatement();
            sql = "INSERT INTO channels (name, owner, regTS) VALUES ('" + channel.getChanName() + "', '" + userAccountId.toString() + "', '" + unixTime.toString() + "');";
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
            //System.out.println("drop sql="+sql);
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
            
            sql = "SELECT uid, name, password, salt, certfp FROM users WHERE lower(name)='" + useraccount.getUserAccountName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            name = resultSet.getString("name");
            password = resultSet.getString("password");
            salt = resultSet.getString("salt");
            userId = resultSet.getInt("uid");
            certfp = resultSet.getString("certfp");
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: cannot fetch the user '" + useraccount.getUserAccountName() + "' in the database."); }
        statement.close();

        if (name.isEmpty() == true) {
            throw new Exception("Error: cannot fetch the user '" + useraccount.getUserAccountName() + "' in the database.");
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
            
            sql = "SELECT uid FROM users WHERE lower(name)='" + useraccount.getUserAccountName().toLowerCase() + "';";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.userId = " + userId + ";";
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                //System.out.println("BFP chan=" + resultSet.getString("name") + " -> chanlev=" + resultSet.getInt("chanlev"));
                userChanlev.put(resultSet.getString("name"), resultSet.getInt("chanlev"));
            }
        }
        catch (Exception e) { 
            e.printStackTrace();
            throw new Exception("Could not get user " + useraccount.getUserAccountName() + " chanlev.");  /* XXX: Normally we should not throw an exception but return an empty CL if it does not exist */
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
            
            sql = "SELECT uid FROM users WHERE lower(name)='" + userAccount.getUserAccountName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");
            //System.out.println("BBB db userId=" + userId);

            sql = "SELECT cid FROM channels WHERE lower(name)='" + channel.getChanName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            chanId = resultSet.getInt("cid");
            //System.out.println("BBC db chanId=" + chanId);

            sql = "SELECT chanlev FROM chanlev WHERE userId = '" + userId + "' AND channelId = '" + chanId + "';";
            //System.out.println("BBD db chanlev=" + sql);
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                userChanlev = resultSet.getInt("chanlev");
            }
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + userAccount.getUserAccountName() + " chanlev."); /* XXX: Normally we should not throw an exception but return an empty CL if it does not exist */
        } 
        statement.close();
        //System.out.println("BBE db chanlev=" + userChanlev);
        return userChanlev;
    }

    /**
     * Returns the chanlev for a specific channel as a Map<String, Integer) mapping user:chanlev
     * @param channel channel name
     * @return user:chanlev map
     * @throws Exception
     */
    public Map<String, Integer> getChanChanlev(ChannelNode channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Map<String, Integer> chanChanlev = new HashMap<String, Integer>();
        Integer chanlev;
        String username;
        Integer chanId           = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT cid FROM channels WHERE lower(name)='" + channel.getChanName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            chanId = resultSet.getInt("cid");

            sql = "SELECT name, chanlev FROM users LEFT JOIN chanlev ON (chanlev.userId = users.uid) WHERE chanlev.channelId = " + chanId + ";";
            //System.out.println("BCD sql=" + sql);
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                username = resultSet.getString("name");
                chanlev = resultSet.getInt("chanlev");
                chanChanlev.put(username, chanlev);
            }
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not get channel " + channel.getChanName() + " chanlev."); }
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
            
            sql = "SELECT uid FROM users WHERE lower(name)='" + userAccount.getUserAccountName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            sql = "SELECT cid FROM channels WHERE lower(name)='" + channel.getChanName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            channelId = resultSet.getInt("cid");

            sql = "SELECT chanlev FROM chanlev WHERE channelId='"+ channelId +"' AND userId = " + userId + ";";
            resultSet = statement.executeQuery(sql);

            if(resultSet.next() == false) {
                if (chanlev != 0) {
                    //System.out.println("BAD user chanlev does not exist => creating it");
                    sql = "INSERT INTO chanlev (channelId, userId, chanlev) VALUES ('" + channelId + "', '" + userId + "', '" + chanlev + "');";
                    statement.executeUpdate(sql);
                }
                else {
                    //System.out.println("BAG user chanlev does not exist => doing nothing");
                    return;
                }
            }
            else {
                
                if (chanlev != 0) {
                    //System.out.println("BAE user chanlev exists => updating it");
                    sql = "UPDATE chanlev SET chanlev='" + chanlev + "' WHERE channelId='" + channelId + "' AND userId='" + userId +"';";
                    statement.executeUpdate(sql);
                }
                else {
                    //System.out.println("BAF user chanlev exists => deleting it");
                    sql = "DELETE FROM chanlev WHERE channelId='" + channelId + "' AND userId='" + userId +"';";
                    statement.executeUpdate(sql);
                }
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set user " + userAccount.getUserAccountName() + " chanlev."); }
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
                //System.out.println("BAH user chanlev does not exist => doing nothing");
                return;
            }
            else {
                //System.out.println("BAI user chanlev exists => deleting it");
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
                //System.out.println("BAH user chanlev does not exist => doing nothing");
                return;
            }
            else {
                //System.out.println("BAI user chanlev exists => deleting it");
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
    public Integer getUserFlags(String username) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Integer userFlags        = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT userflags FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userFlags = resultSet.getInt("userflags");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + username + " userflags.");
        } 
        statement.close();
        return userFlags;
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
            
            sql = "UPDATE users SET userflags='" + userflags + "' WHERE name='" + userAccount.getUserAccountName() +"';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set user " + userAccount.getUserAccountName() + " flags."); }
    }

    /**
     * Returns the user email
     * @param username user name
     * @return user email
     * @throws Exception
     */
    public String getUserEmail(String username) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        String userEmail         = "";

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT email FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userEmail = resultSet.getString("userflags");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + username + " userflags.");
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
    public Long getUserRegTS(String username) throws Exception {
        Statement statement      = null;
        String sql               = null;
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

    /**
     * Returns the user certfp
     * @param username
     * @return user certfp
     * @throws Exception
     */
    public String getUserCertFP(String username) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        String userCertFP         = "";

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT certfp FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userCertFP = resultSet.getString("userflags");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + username + " userflags.");
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
            
            sql = "SELECT autolimit FROM channels WHERE lower(name)='" + channel.getChanName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            autoLimit = resultSet.getInt("autolimit");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get channel " + channel.getChanName() + " autolimit.");
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
            
            sql = "UPDATE channels SET autolimit='" + String.valueOf(autolimit) + "' WHERE lower(name)='" + channel.getChanName().toLowerCase() +"';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set channel " + channel.getChanName() + " autolimit."); }
    }

    /**
     * Returns the user account id
     * @param username username
     * @return user id
     * @throws Exception
     */
    public Integer getId(String username) throws Exception {
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
            throw new Exception("Could not get user " + username + " userflags.");
        } 
        statement.close();
        return userId;
    }

    public HashSet<String> getCertfp(UserAccount userAccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        String certfp = "";
        try { 
            statement = connection.createStatement();
            
            sql = "SELECT certfp FROM users WHERE lower(name)='" + userAccount.getUserAccountName().toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            certfp = resultSet.getString("certfp");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + userAccount.getUserAccountName() + " certfp.");
        } 
        statement.close();
        return stringToHS(certfp);
    }

    public void addCertfp(UserAccount userAccount, String certfp) throws Exception {
        Statement statement      = null;
        String sql               = null;
        String certfpForDb = "";

        HashSet<String> userCertfp = this.getCertfp(userAccount);

        if (userCertfp.size() > config.getCServeAccountMaxCertFP()) { throw new Exception("(EX) Client reached max certfp"); }

        try {
            userCertfp.add(certfp);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("* Could not readd certfp to user " + userAccount.getUserAccountName() + " because already in the list");
        }

        certfpForDb = hashSetToString(userCertfp);

        try { 
            statement = connection.createStatement();

            sql = "UPDATE users SET certfp='" + certfpForDb + "' WHERE lower(name)='" + userAccount.getUserAccountName().toLowerCase() + "'";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + userAccount.getUserAccountName() + " certfp.");
        } 
        statement.close();
        //return stringToArrayList(certfp);
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
            System.out.println("* Could not remove certfp to user " + userAccount.getUserAccountName() + " because not in the list");
        }

        certfpForDb = hashSetToString(userCertfp);

        try { 
            statement = connection.createStatement();

            sql = "UPDATE users SET certfp='" + certfpForDb + "' WHERE lower(name)='" + userAccount.getUserAccountName().toLowerCase() + "'";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Could not get user " + userAccount.getUserAccountName() + " certfp.");
        } 
        statement.close();
        //return stringToArrayList(certfp);
    }

    public String getWelcomeMsg(ChannelNode channelNode) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        String welcomeMsg = "";

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT welcome FROM channels WHERE name='" + channelNode.getChanName() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            welcomeMsg = resultSet.getString("welcome");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            //throw new Exception("Could not get user " + channelNode.getChanName() + " welcome.");
            //welcomeMsg = "";
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
            
            sql = "SELECT topic FROM channels WHERE name='" + channelNode.getChanName() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            topic = resultSet.getString("topic");
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            //throw new Exception("Could not get user " + channelNode.getChanName() + " welcome.");
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
                sql = "SELECT id FROM logins WHERE userSid='" + userNode.getUserUniq() + "';";
                resultSet = statement.executeQuery(sql);
                
            }
            catch (Exception e) { e.printStackTrace(); }

            if (resultSet.next() == true) {
                System.out.println("Error: cannot reauth '" + userNode.getUserAccount().getUserAccountId() + "' with '" + userNode.getUserUniq() + "'.");
                throw new Exception("Error: cannot reauth '" + userNode.getUserAccount().getUserAccountId() + "' with '" + userNode.getUserUniq() + "'.");
            }
            statement.close();

            try {
                statement = connection.createStatement();
                sql = "INSERT INTO logins (userId, userSid, userTS) VALUES ('" + userNode.getUserAccount().getUserAccountId() + "', '" + userNode.getUserUniq() + "', '" + userNode.getUserTS().toString() + "');";
                statement.executeUpdate(sql);
                statement.close();
            }
            catch (Exception e) { 
                e.printStackTrace(); 
                System.out.println("Error: cannot map login token '" + userNode.getUserUniq() + "' -> '" + userNode.getUserAccount().getUserAccountId() + "'.");
                throw new Exception("Error: cannot map login token '" + userNode.getUserUniq() + "' -> '" + userNode.getUserAccount().getUserAccountId() + "'."); 
            }
        }

        try { 
            statement = connection.createStatement();
            
            sql = "INSERT INTO authhistory (userId, maskFrom, authType, authTS, sessionUuid) VALUES ('" + userNode.getUserAccount().getUserAccountId() + "', '" + userNode.getUserIdent() + "@" + userNode.getUserRealHost() + "', '" + Integer.valueOf(authType) + "', '" + userNode.getUserAuthTS() + "', '" + userNode.getUserAuth() + "');";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not add auth for user " + userNode.getUserAccount() + "."); }
    }

    public void delUserAuth(String userSid) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT userSid FROM logins WHERE userSid='" + userSid + "'";
            resultSet = statement.executeQuery(sql);

            if(resultSet.next() == true) {
                //System.out.println("BAI user chanlev exists => deleting it");
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

        this.delUserAuth(userNode.getUserUniq());

        unixTime = Instant.now().getEpochSecond();

        try { 
            statement = connection.createStatement();
            
            sql = "UPDATE authhistory SET deAuthTS='" + unixTime + "', deAuthType='" + deAuthType + "', deAuthReason='" + quitMsg + "', sessionUuid='' WHERE sessionUuid='" + userNode.getUserAuth().toString() + "'";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not close auth session for user " + userNode.getUserAccount() + "."); }
    }

    public void delUserAccount(UserAccount userAccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        try { 
            statement = connection.createStatement();
            
            sql = "DELETE FROM users WHERE lower(name)='" + userAccount.getUserAccountName().toLowerCase() + "';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not delete account for user " + userAccount.getUserAccountName() + "."); }
    }

    public void updateUserAuth(UserNode userNode) throws Exception {
        Statement statement = null;
        String sql = null;

        try {
            statement = connection.createStatement();
            sql = "UPDATE logins SET userTS='" + userNode.getUserTS().toString() + "' WHERE userSid='" + userNode.getUserUniq() + "';";
            statement.executeUpdate(sql);
            statement.close();

            statement = connection.createStatement();
            
            sql = "UPDATE authhistory SET maskFrom='" + userNode.getUserIdent() + "' WHERE sessionUuid='" + userNode.getUserAuth() + "';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Error: cannot update login token/history for user '" + userNode.getUserUniq()); 
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
            
            sql = "SELECT userId FROM logins WHERE userSid='" + user.getUserUniq() + "' AND userTS='" + user.getUserTS()  + "';";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            //System.out.println("BFG userId=" + resultSet.getInt("userId"));
            account = protocol.getUserAccount(resultSet.getInt("userId"));
        }
        catch (Exception e) { 
            e.printStackTrace(); 
        } 
        statement.close();
        //System.out.println("BFG accountFound=" + account.getUserAccountName());
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
            
            sql = "UPDATE channels SET chanflags='" + flags + "' WHERE name='" + chan.getChanName() +"';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set chan " + chan.getChanName() + " flags."); }
    }

    public void setWelcomeMsg(ChannelNode chan, String msg) throws Exception {
        Statement statement      = null;
        String sql               = null;

        try { 
            statement = connection.createStatement();

            sql = "UPDATE channels SET welcome='" + msg + "' WHERE name='" + chan.getChanName() +"';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set chan " + chan.getChanName() + " welcome message."); }
    }

    public void setTopic(ChannelNode chan, String msg) throws Exception {
        Statement statement      = null;
        String sql               = null;

        try { 
            statement = connection.createStatement();

            sql = "UPDATE channels SET topic='" + msg + "' WHERE name='" + chan.getChanName() +"';";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set chan " + chan.getChanName() + " topic message."); }
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
        catch (Exception e) { e.printStackTrace();}
        
        /* Getting the list of user UIDs on the network */
        protocol.getUserList().forEach((userUid, userNode) -> {
            userUidNetwork.add(userUid);
        });

        /* Parsing the DB token list and removing the tokens corresponding of users present on the network  */
        /* We can remove the ones corresponding to nobody on the network */
        for(String userUid : userUidTokens) {
            if (userUidNetwork.contains(userUid) == false) {
                System.out.println("* DB cleanup: deleting expired user UID " + userUid);
                try { this.delUserAuth(userUid); }
                catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    public void openUserAuthSessionHistory(UserNode user, Integer authType) throws Exception {
        Statement statement      = null;
        String sql               = null;

        try { 
            statement = connection.createStatement();
            
            sql = "INSERT INTO authhistory (userId, maskFrom, authType, authTS, sessionUuid) VALUES ('" + user.getUserAccount().getUserAccountId() + "', '" + user.getUserIdent() + "@" + user.getUserRealHost() + "', '" + Integer.valueOf(authType) + "', '" + user.getUserAuthTS() + "', '" + user.getUserAuth() + "');";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not add auth for user " + user.getUserAccount() + "."); }
    }

    public void closeUserAuthSessionHistory(UserNode user, Integer authType, Integer deAuthType, String quitMsg) throws Exception {
        Statement statement      = null;
        String sql               = null;

        unixTime = Instant.now().getEpochSecond();

        try { 
            statement = connection.createStatement();
            
            sql = "UPDATE authhistory SET deAuthTS='" + unixTime + "', deAuthType='" + deAuthType + "', deAuthReason='" + quitMsg + "', sessionUuid='' WHERE sessionUuid='" + user.getUserAuth().toString() + "'";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not close auth session for user " + user.getUserAccount() + "."); }
    }

    public ArrayList<HashMap<String, Object>> getAuthHistory(UserAccount userAccount) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        ArrayList<HashMap<String, Object>> authHist = new ArrayList<>();
        HashMap<String, Object> authLine;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT * FROM authhistory WHERE userId='" + userAccount.getUserAccountId() + "' ORDER BY authTS DESC LIMIT 0,10;";
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
            e.printStackTrace(); 
            throw new Exception("Could not get auth history for account " + userAccount.getUserAccountEmail() + ".");
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
