
/**
 * Class to dispatch actions to nodes and db when actions are done by a command
 */
public class Dispatcher {

    private Client   client;
    private SqliteDb sqliteDb;
    private Config   config;
    private Protocol protocol;
    
    public Dispatcher(Client client, Config config, SqliteDb sqliteDb, Protocol protocol) {
        this.client = client;
        this.config = config;
        this.sqliteDb = sqliteDb;
        this.protocol = protocol;
    }

    public void setChanFlags(ChannelNode chanNode, Integer chanNewFlagsInt) throws Exception {
        try {
            sqliteDb.setChanFlags(chanNode, chanNewFlagsInt);
            chanNode.setFlags(chanNewFlagsInt);
        }
        catch (Exception e) {
            throw new Exception();
        }
    }

    public void setUserFlags(UserNode userNode, Integer userNewFlags) throws Exception {
        try {
            sqliteDb.setUserFlags(userNode.getAccount(), userNewFlags);
            userNode.getAccount().setFlags(userNewFlags);
        }
        catch (Exception e) {
            throw new Exception();
        }
    }

    public void setChanlev(ChannelNode chanNode, UserAccount userAccount, Integer userNewChanlevInt) throws Exception {
        try {
            sqliteDb.setUserChanlev(userAccount, chanNode, userNewChanlevInt);
            userAccount.setChanlev(chanNode, userNewChanlevInt);

            chanNode.setChanlev(sqliteDb.getChanChanlev(chanNode));
        }
        catch (Exception e) {
            throw new Exception();
        }
    }

    public void dropChan(ChannelNode chanNode, UserNode userNode) throws Exception {

        try {
            userNode.getAccount().clearUserChanlev(chanNode);
            sqliteDb.clearChanChanlev(chanNode.getName());
            sqliteDb.delRegChan(chanNode.getName());            
        }
        catch (Exception e) {
            throw new Exception();
        }

    }



}
