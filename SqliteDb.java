import java.sql.*;
//import org.sqlite.JDBC;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;

public class SqliteDb {
    Connection connection;
    Long unixTime;
    Config config;

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
    public ArrayList<String> getRegChan(){
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        ArrayList<String> regChannels = new ArrayList<String>();

        try { 
            statement = connection.createStatement();
            sql = "SELECT name FROM channels;";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String  name = resultSet.getString("name");
                regChannels.add(name);
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); System.exit(0); }
        
        return regChannels;
    }

    /**
     * Returns an user account object
     * @param username user name
     * @return user account object
     */
    public UserAccount getUserAccount(String username) {
        Integer userAccountId = 0;

        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        try { 
            statement = connection.createStatement();
            sql = "SELECT uid FROM users WHERE lower(name) = " + username.toLowerCase() + ";";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                userAccountId = resultSet.getInt("uid");
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); System.exit(0); }
        
        UserAccount userAccount = new UserAccount(this, username, userAccountId);



        return userAccount;

    }

    /**
     * Returns the list of user account names as an ArrayList<String>
     * @return list of user account names
     */
    public HashMap<String,UserAccount> getRegUsers() {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        HashMap<String,UserAccount> regUsers = new HashMap<String,UserAccount>();

        try { 
            statement = connection.createStatement();
            sql = "SELECT name, uid, userFlags, email, certfp FROM users;";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                regUsers.put(resultSet.getString("name"), 
                             new UserAccount(this, resultSet.getString("name"), resultSet.getInt("uid"), resultSet.getInt("userFlags"), resultSet.getString("email"), resultSet.getString("certfp")));
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); System.exit(0); }
        
        return regUsers;
    }

    /**
     * Add a channel into the databse
     * @param channel channel name
     * @param owner owner user id //XXX to be deleted because ownership can be handled by chanlev
     * @throws Exception
     */
    public void addRegChan(String channel, String owner) throws Exception {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        try { 
            statement = connection.createStatement();
            sql = "SELECT name FROM channels WHERE lower(name)='" + channel.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            
        }
        catch (Exception e) { e.printStackTrace(); }

        if (resultSet.next() == true) {
            throw new Exception("Cannot register the new channel '" + channel + "' in the database because it already exists.");
        }
        statement.close();

        try {

            Integer userAccountId = 0;
            statement = connection.createStatement();
            sql = "SELECT uid FROM users WHERE name='" + owner + "';";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                userAccountId = resultSet.getInt("uid");
            }
            unixTime = Instant.now().getEpochSecond();
            statement.close();

            statement = connection.createStatement();
            sql = "INSERT INTO channels (name, owner, regTS) VALUES ('" + channel + "', '" + userAccountId.toString() + "', '" + unixTime.toString() + "');";
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
            //System.out.println(sql);
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
    public void addUser(String username, String email, String passwordHash, String salt) throws Exception {
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
            
            sql = "INSERT INTO users (name, email, password, salt) VALUES ('" + username + "', '" + email + "', '" + passwordHash + "', '" + salt + "');";
            //System.out.println(sql);
            statement.executeUpdate(sql);
            statement.close();
            
        }
        catch (Exception e) { e.printStackTrace(); }
    }
    
    /**
     * Returns an user as a Map<String, String> of field:value
     * @param username user name
     * @return Map of the user as field:value
     * @throws Exception
     */
    public Map<String, String> getUser(String username) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Map<String, String> user = new HashMap<String, String>();
        String name              = "";
        String password          = "";
        String salt              = "";
        Integer userId           = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT uid, name, password, salt FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            name = resultSet.getString("name");
            password = resultSet.getString("password");
            salt = resultSet.getString("salt");
            userId = resultSet.getInt("uid");
        }
        catch (Exception e) { e.printStackTrace(); }
        statement.close();

        if (name.isEmpty() == true) {
            throw new Exception("Error: cannot fetch the user '" + username + "' in the database.");
        }

        user.put("name",     name);
        user.put("password", password);
        user.put("salt",     salt);
        user.put("id",       userId.toString());

        return user;
    }
    public Map<String, Integer> getUserChanlev(String username) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Map<String, Integer> userChanlev = new HashMap<String, Integer>();
        Integer chanlev;
        String channel;
        Integer userId           = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT uid FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.userId = " + userId + ";";
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                channel = resultSet.getString("name");
                chanlev = resultSet.getInt("chanlev");
                userChanlev.put(channel, chanlev);
            }
        }
        catch (Exception e) { 
            e.printStackTrace();
            throw new Exception("Could not get user " + username + " chanlev.");  /* XXX: Normally we should not throw an exception but return an empty CL if it does not exist */
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
    public Integer getUserChanlev(String username, String channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Integer userChanlev      = 0;
        Integer userId           = 0;
        Integer chanId           = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT uid FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");
            //System.out.println("BBB db userId=" + userId);

            sql = "SELECT cid FROM channels WHERE lower(name)='" + channel.toLowerCase() + "'";
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
            throw new Exception("Could not get user " + username + " chanlev."); /* XXX: Normally we should not throw an exception but return an empty CL if it does not exist */
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
    public Map<String, Integer> getChanChanlev(String channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Map<String, Integer> chanChanlev = new HashMap<String, Integer>();
        Integer chanlev;
        String username;
        Integer chanId           = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT cid FROM channels WHERE lower(name)='" + channel.toLowerCase() + "'";
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
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not get channel " + channel + " chanlev."); }
        statement.close();
        return chanChanlev;
    }
    // XXX to delete
    public void setUserChanlev(String username, String channel, String chanlev) throws Exception {
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

            //sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.userId = " + userId + ";";
            sql = "SELECT chanlev FROM chanlev WHERE channelId='"+ channelId +"' AND userId = " + userId + ";";
            resultSet = statement.executeQuery(sql);

            if(resultSet.next() == false) {
                if (chanlev.isEmpty() == false) {
                    //System.out.println("BAD user chanlev does not exist => creating it");
                    sql = "INSERT INTO chanlev (channelId, userId, chanlev) VALUES ('" + channelId + "', '" + userId + "', '" + chanlev + "');";
                    //System.out.println(sql);
                    statement.executeUpdate(sql);
                }
                else {
                    //System.out.println("BAG user chanlev does not exist => doing nothing");
                    return;
                }
            }
            else {
                
                if (chanlev.isEmpty() == false) {
                    //System.out.println("BAE user chanlev exists => updating it");
                    sql = "UPDATE chanlev SET chanlev='" + chanlev + "' WHERE channelId='" + channelId + "' AND userId='" + userId +"';";
                    //System.out.println(sql);
                    statement.executeUpdate(sql);
                }
                else {
                    //System.out.println("BAF user chanlev exists => deleting it");
                    sql = "DELETE FROM chanlev WHERE channelId='" + channelId + "' AND userId='" + userId +"';";
                    //System.out.println(sql);
                    statement.executeUpdate(sql);
                }
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Error: could not set user " + username + " chanlev."); }
    }

    public void setUserChanlev(String username, String channel, Integer chanlev) throws Exception {
    /**
     * Deletes the chanlev of an user on a channel
     * @param username user name
     * @param channel channel
     * @throws Exception
     */
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
                if (chanlev != 0) {
                    //System.out.println("BAD user chanlev does not exist => creating it");
                    sql = "INSERT INTO chanlev (channelId, userId, chanlev) VALUES ('" + channelId + "', '" + userId + "', '" + chanlev + "');";
                    //System.out.println(sql);
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
                    //System.out.println(sql);
                    statement.executeUpdate(sql);
                }
                else {
                    //System.out.println("BAF user chanlev exists => deleting it");
                    sql = "DELETE FROM chanlev WHERE channelId='" + channelId + "' AND userId='" + userId +"';";
                    //System.out.println(sql);
                    statement.executeUpdate(sql);
                }
            }
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
                //System.out.println(sql);
                statement.executeUpdate(sql);
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Could not unset " + channel + " chanlev."); }
    }

    }
    public void unSetUserChanlev(String channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;

        Integer channelId        = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT certfp FROM users WHERE lower(name)='" + username.toLowerCase() + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            channelId = resultSet.getInt("cid");

    /**
     * Add an authentication token in the db between a (SID, TS) and a user account
     * @param userNick user nickname
     * @param userTS user timestamp
     */
    public void addUserAuth(Integer userId, String userSid, Long userTS) throws Exception {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        try { 
            statement = connection.createStatement();
            sql = "SELECT id FROM logins WHERE userSid='" + userSid + "';";
            resultSet = statement.executeQuery(sql);
            
        }
        catch (Exception e) { e.printStackTrace(); }

        if (resultSet.next() == true) {
            throw new Exception("Error: cannot reauth '" + userId + "' with '" + userSid + "'.");
        }
        statement.close();

        try {
            statement = connection.createStatement();
            sql = "INSERT INTO logins (userId, userSid, userTS) VALUES ('" + userId + "', '" + userSid + "', '" + userTS.toString() + "');";
            statement.executeUpdate(sql);
            statement.close();
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Error: cannot map login token '" + userSid + "' -> '" + userId + "'."); 
        }
    }

    /**
     * Deletes an authentication token in the db between a (SID, TS) and a user account
     * @param username
     * @param userSid
     * @param userTS
     */
    public void delUserAuth(String userSid) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT userSid FROM logins WHERE userSid='" + userSid + "'";
            resultSet = statement.executeQuery(sql);

            if(resultSet.next() == false) {
                //System.out.println("BAI user chanlev exists => deleting it");
                sql = "DELETE FROM logins WHERE userSid='" + userSid + "';";
                //System.out.println(sql);
                statement.executeUpdate(sql);
            }

            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Could not unauth " + userSid + " chanlev."); }

    }

    public HashMap<String, Integer> getUserLoginTokens(String userAccountName) {
        return null;
    }
}
