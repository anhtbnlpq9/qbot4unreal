import java.util.HashSet;

import Exceptions.MaxLimitReachedException;

/**
 * Class to dispatch actions to nodes and db when actions are done by a command
 */
public class Dispatcher {

    private SqliteDb sqliteDb;
    private Config   config;
    private Protocol protocol;
    
    public Dispatcher(Config config, SqliteDb sqliteDb, Protocol protocol) {
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
            throw e;
        }
    }

    public void setUserFlags(UserNode userNode, Integer userNewFlags) throws Exception {
        try {
            sqliteDb.setUserFlags(userNode.getAccount(), userNewFlags);
            userNode.getAccount().setFlags(userNewFlags);
        }
        catch (Exception e) {
            throw e;
        }
    }

    public void setChanlev(ChannelNode chanNode, UserAccount userAccount, Integer userNewChanlevInt) throws Exception {
        try {
            sqliteDb.setUserChanlev(userAccount, chanNode, userNewChanlevInt);
            userAccount.setChanlev(chanNode, userNewChanlevInt);

            chanNode.setChanlev(sqliteDb.getChanChanlev(chanNode));
        }
        catch (Exception e) {
            throw e;
        }
    }

    public void dropChan(ChannelNode chanNode, UserNode userNode) throws Exception {

        HashSet<UserAccount> usersWithChanlev = new HashSet<>();

        try {
            for (String username: chanNode.getChanlev().keySet() ) {
                usersWithChanlev.add(protocol.getRegUserAccount(username));
            }

            for (UserAccount username: usersWithChanlev) {
                username.clearUserChanlev(chanNode);
            }
            
            chanNode.clearChanChanlev(userNode);
            sqliteDb.clearChanChanlev(chanNode.getName());
            sqliteDb.delRegChan(chanNode.getName());
        }
        catch (Exception e) {
            throw e;
        }

    }

    public void addUserCertFp(UserAccount userAccount, String certfp) throws Exception, MaxLimitReachedException {

        try {
            sqliteDb.addCertfp(userAccount, certfp);
            userAccount.addCertFP(certfp);
        }
        catch (MaxLimitReachedException e) {
            throw e;
        }
        catch (Exception e) {
            throw e;
        }

    }

    public void removeUserCertFp(UserAccount userAccount, String certfp) throws Exception {

        HashSet<String> userAccountCertfp;

        try {
            sqliteDb.removeCertfp(userAccount, certfp);
            userAccountCertfp = sqliteDb.getCertfp(userAccount);
            userAccount.setCertFP(userAccountCertfp);
        }
        catch (Exception e) {
            throw e;
        }
        
    }



}
