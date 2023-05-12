import java.sql.*;
//import org.sqlite.JDBC;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;

public class SqliteDb {
    Connection connection;
    Long unixTime;

    public SqliteDb() {      
        try {
           Class.forName("org.sqlite.JDBC");
           connection = DriverManager.getConnection("jdbc:sqlite:db/qbot.sqlite3");
        } catch ( Exception e ) {
           e.printStackTrace();
           System.exit(0);
        }
        //System.out.println("* Opened database successfully");

    }

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
        catch (Exception e) { e.printStackTrace(); }
        
        return regChannels;
    }
    public void addRegChan(String channel, String owner) throws Exception {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        try { 
            statement = connection.createStatement();
            sql = "SELECT name FROM channels WHERE name='" + channel + "'";
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

    public void delRegChan(String channel) throws Exception {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        try { 
            statement = connection.createStatement();
            sql = "SELECT name FROM channels WHERE name='" + channel + "';";
            System.out.println(sql);
            resultSet = statement.executeQuery(sql);
        }
        catch (Exception e) { e.printStackTrace(); }

        if (resultSet.next() == false) {
            throw new Exception("Cannot drop the new channel '" + channel + "' because it is not registered.");
        }
        statement.close();

        try {
            statement = connection.createStatement();
            sql = "DELETE FROM channels WHERE name='" + channel + "';";
            statement.executeUpdate(sql);
        }
        catch (Exception e) { 
            e.printStackTrace(); 
            throw new Exception("Error while dropping the channel."); 
        }
        statement.close();
    }

    public void addUser(String username, String email, String passwordHash, String salt) throws Exception {
        Statement statement = null;
        String sql = null;
        ResultSet resultSet = null;

        try { 
            statement = connection.createStatement();
            sql = "SELECT name FROM users WHERE name='" + username + "'";
            resultSet = statement.executeQuery(sql);

            
        }
        catch (Exception e) { e.printStackTrace(); }

        if (resultSet.next() == true) { throw new Exception("Cannot register the new user '" + username + "' in the database because it already exists."); }
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
            
            sql = "SELECT uid, name, password, salt FROM users WHERE name='" + username + "'";
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
            throw new Exception("Cannot fetch the new user '" + username + "' in the database because it does not exist.");
        }

        user.put("name",     name);
        user.put("password", password);
        user.put("salt",     salt);
        user.put("id",       userId.toString());

        return user;
    }

    public Map<String, String> getUserChanlev(String username) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;
        Map<String, String> userChanlev = new HashMap<String, String>();
        String chanlev;
        String channel;
        Integer userId           = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT uid FROM users WHERE name='" + username + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.userId = " + userId + ";";
            resultSet = statement.executeQuery(sql);
            while(resultSet.next()) {
                channel = resultSet.getString("name");
                chanlev = resultSet.getString("chanlev");
                userChanlev.put(channel, chanlev);
            }
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Could not get user " + username + " chanlev."); }
        statement.close();
        return userChanlev;
    }
    public void setUserChanlev(String username, String channel, String chanlev) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;

        Integer userId           = 0;
        Integer channelId        = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT uid FROM users WHERE name='" + username + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            sql = "SELECT cid FROM channels WHERE name='" + channel + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            channelId = resultSet.getInt("cid");

            sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.userId = " + userId + ";";
            resultSet = statement.executeQuery(sql);


            if(resultSet.next() == false) {
                System.out.println("BAD user chanlev does not exist => creating it");

                if (chanlev.isEmpty() == false) {
                    sql = "INSERT INTO chanlev (channelId, userId, chanlev) VALUES ('" + channelId + "', '" + userId + "', '" + chanlev + "');";
                    //System.out.println(sql);
                    statement.executeUpdate(sql);
                }
                else {
                    System.out.println("BAG user chanlev does not exist => doing nothing");
                    return;
                }

            }
            else {
                
                if (chanlev.isEmpty() == false) {
                    System.out.println("BAE user chanlev exists => updating it");
                    sql = "UPDATE chanlev SET channelId='" + channelId + "', userId='" + userId +"', chanlev='" + chanlev +"';";
                    //System.out.println(sql);
                    statement.executeUpdate(sql);
                }
                else {
                    System.out.println("BAF user chanlev exists => deleting it");
                    sql = "DELETE FROM chanlev WHERE channelId='" + channelId + "', userId='" + userId +"';";
                    //System.out.println(sql);
                    statement.executeUpdate(sql);
                }
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Could not set user " + username + " chanlev."); }

    }
    public void unSetUserChanlev(String username, String channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;

        Integer userId           = 0;
        Integer channelId        = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT uid FROM users WHERE name='" + username + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            userId = resultSet.getInt("uid");

            sql = "SELECT cid FROM channels WHERE name='" + channel + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            channelId = resultSet.getInt("cid");

            sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.userId = " + userId + ";";
            resultSet = statement.executeQuery(sql);


            if(resultSet.next() == false) {
                System.out.println("BAH user chanlev does not exist => doing nothing");
                return;
            }
            else {
                System.out.println("BAI user chanlev exists => deleting it");
                sql = "DELETE FROM chanlev WHERE channelId='" + channelId + "' AND userId='" + userId +"';";
                //System.out.println(sql);
                statement.executeUpdate(sql);
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Could not unset user " + username + " chanlev."); }

    }
    public void unSetUserChanlev(String channel) throws Exception {
        Statement statement      = null;
        String sql               = null;
        ResultSet resultSet      = null;

        Integer userId           = 0;
        Integer channelId        = 0;

        try { 
            statement = connection.createStatement();
            
            sql = "SELECT cid FROM channels WHERE name='" + channel + "'";
            resultSet = statement.executeQuery(sql);
            resultSet.next();
            channelId = resultSet.getInt("cid");

            sql = "SELECT name, chanlev FROM channels LEFT JOIN chanlev ON (chanlev.channelId = channels.cid) WHERE chanlev.channelId = " + channelId + ";";
            resultSet = statement.executeQuery(sql);


            if(resultSet.next() == false) {
                System.out.println("BAH user chanlev does not exist => doing nothing");
                return;
            }
            else {
                System.out.println("BAI user chanlev exists => deleting it");
                sql = "DELETE FROM chanlev WHERE channelId='" + channelId + "';";
                //System.out.println(sql);
                statement.executeUpdate(sql);
            }
            statement.close();
        }
        catch (Exception e) { e.printStackTrace(); throw new Exception("Could not unset " + channel + " chanlev."); }

    }

}
