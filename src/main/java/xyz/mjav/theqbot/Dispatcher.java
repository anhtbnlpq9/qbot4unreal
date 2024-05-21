package xyz.mjav.theqbot;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xyz.mjav.theqbot.exceptions.ChannelNotFoundException;
import xyz.mjav.theqbot.exceptions.DataBaseExecException;
import xyz.mjav.theqbot.exceptions.ItemErrorException;
import xyz.mjav.theqbot.exceptions.ItemExistsException;
import xyz.mjav.theqbot.exceptions.ItemNotFoundException;
import xyz.mjav.theqbot.exceptions.MaxLimitReachedException;

/**
 * Class to dispatch actions to nodes and db when actions are done by a command
 */
public class Dispatcher {

    private static Logger log = LogManager.getLogger("common-log");

    private Database database;
    private Config   config;
    private Protocol protocol;

    public Dispatcher(Config config, Database database, Protocol protocol) {
        this.config = config;
        this.database = database;
        this.protocol = protocol;
    }

    public class Check<T> {

        public Check() {

        }

        private Boolean checkDataConsistency(Set<T> s1, Set<T> s2) {
            Set<T> t1 = new TreeSet<>(s1);
            Set<T> t2 = new TreeSet<>(s2);

            if (t1.equals(t2) == true) return true;
            return false;
        }

    }

    public void setChanFlags(Channel chanNode, Integer chanNewFlagsInt) throws Exception {
        try {
            database.setChanFlags(chanNode, chanNewFlagsInt);
            chanNode.setcServeFlags(chanNewFlagsInt);
        }
        catch (Exception e) {
            throw e;
        }
    }

    public void setUserFlags(Nick userNode, Integer userNewFlags) throws Exception {
        try {
            database.setUserFlags(userNode.getAccount(), userNewFlags);
            userNode.getAccount().setFlags(userNewFlags);
        }
        catch (Exception e) {
            throw e;
        }
    }

    public void setChanlev(Channel chanNode, UserAccount userAccount, Integer userNewChanlevInt) throws Exception {
        try {
            database.setUserChanlev(userAccount, chanNode, userNewChanlevInt);
            userAccount.setChanlev(chanNode, userNewChanlevInt);

            chanNode.setChanlev(database.getChanChanlev(chanNode));
        }
        catch (Exception e) {
            throw e;
        }
    }

    public void dropChan(Channel chanNode, Nick userNode) throws Exception {

        Set<UserAccount> usersWithChanlev = new HashSet<>();

        try {
            for (String username: chanNode.getChanlev().keySet() ) { usersWithChanlev.add(UserAccount.getUserByNameCi(username)); }

            for (UserAccount username: usersWithChanlev) {
                username.clearUserChanlev(chanNode);
            }

            chanNode.clearChanChanlev(userNode);
            chanNode.setRegistered(false);

            protocol.delRegChan(chanNode);

            database.clearChanChanlev(chanNode.getName());
            database.delRegChan(chanNode);

        }
        catch (Exception e) {
            throw e;
        }

    }

    public void addUserCertFp(UserAccount userAccount, String certfp) throws MaxLimitReachedException, ItemErrorException, DataBaseExecException, ItemExistsException {

        Set<String> userCertFPsLive = userAccount.getCertFP();
        Set<String> userCertFPsDb;

        /* Check if user already has registered the certfp => do nothing */
        if (userCertFPsLive.contains(certfp) == true) return;

        /* Check if user has not hit his certfp list limit */
        if (userAccount.getCertFP().size() >= config.getCServeAccountMaxCertFP()) throw new MaxLimitReachedException("Dispatcher::addUserCertFp: Max CertFP limit reached");

        try { database.addCertfp(userAccount, certfp); }
        catch (ItemErrorException e) {
            log.error(String.format("Dispatcher::addUserCertFp: certfp string is empty but it should not"));
            throw e;
        }
        catch (ItemExistsException e) {
            log.error(String.format("Dispatcher::addUserCertFp: something went wrong because certfp already exists in database but not in memory user certfp list"),e);
            throw e;
        }
        catch (DataBaseExecException e) {
            log.error(String.format("Dispatcher::addUserCertFp: couldn't not complete adding certfp into database"));
            throw e;
        }

        userAccount.addCertFP(certfp);


        try { userCertFPsDb = database.getCertFPs(userAccount); }
        catch (ItemNotFoundException e) {
            throw e;
        }

        Check<String> discrepCheck = new Check<>();
        if (discrepCheck.checkDataConsistency(userCertFPsLive, userCertFPsDb) == false) {
            log.error(String.format("Dispatcher::addUserCertFp: discrepancy between database and memory for user %s", userAccount));
            discrepCheck = null;
            throw new ItemErrorException("discrepancy between database and memory");
        }
        discrepCheck = null;

    }

    public void removeUserCertFp(UserAccount userAccount, String certfp) throws ItemErrorException, DataBaseExecException, ItemNotFoundException {

        Set<String> userCertFPsLive = userAccount.getCertFP();
        Set<String> userCertFPsDb;

        try { database.removeCertfp(userAccount, certfp); }
        catch (ItemErrorException e) {
            throw e;
        }
        catch (DataBaseExecException e) {
            throw e;
        }

        userAccount.removeCertFP(certfp);

        try { userCertFPsDb = database.getCertFPs(userAccount); }
        catch (ItemNotFoundException e) {
            throw e;
        }

        Check<String> discrepCheck = new Check<>();
        if (discrepCheck.checkDataConsistency(userCertFPsLive, userCertFPsDb) == false) {
            log.error(String.format("Dispatcher::removeUserCertFp: discrepancy between database and memory for user %s", userAccount));
            discrepCheck = null;
            throw new ItemErrorException("discrepancy between database and memory");
        }
        discrepCheck = null;

    }

    public void addUserToChan(Channel channel, Nick user) throws Exception {

        try {
            channel.addUser(user);
            user.addChan(channel, channel.getUserModes(user));
        }
        catch (ItemExistsException e) {
            log.error(String.format("Dispatcher::addUserToChan: error adding user %s to channel %s userlist, user already in channel user list", user.getNick(), channel.getName()), e);
            throw e;
        }
    }

    public void removeUserFromChan(Channel channel, Nick user) throws Exception {

        try {
            channel.delUser(user);
            user.delChan(channel);
        }
        catch (ItemNotFoundException e) {
            log.error(String.format("Dispatcher::addUserToChan: error removing user %s from channel %s userlist, user not in channel user list", user.getNick(), channel.getName()), e);
            throw e;
        }

    }

    public void addUserChanMode(Channel chan, Nick user, String mode, String modeParam) throws ItemNotFoundException {
        try {  chan.addUserMode(user, mode, modeParam); }
        catch (ItemNotFoundException e) {
            log.error(String.format("Dispatcher::addUserChanMode: Cannot manipulate mode %s because nick %s is not on the chan %s", mode, user, chan), e);
            throw e;
        }
    }

    public void delUserChanMode(Channel chan, Nick user, String mode) throws ItemNotFoundException {
        try {  chan.delUserMode(user, mode); }
        catch (ItemNotFoundException e) {
            log.error(String.format("Dispatcher::delUserChanMode: Cannot manipulate mode %s because nick %s is not on the chan %s", mode, user, chan), e);
            throw e;
        }
    }

    public void deleteUserAccount(UserAccount u) throws Exception {

        try {
            database.delUserAccount(u);
            log.debug(String.format("Dispatcher::deleteUserAccount: %s has been deleted from the database.", u.getName()));
        }
        catch (Exception e) {
            log.error(String.format("Dispatcher::deleteUserAccount: could not delete %s from the database.", u.getName()));
            throw new Exception();
        }

        UserAccount.removeUser(u);
    }

    public void createUserAccount(Map<String, String> accountParams) throws Exception {
        String accountName    = accountParams.get("accountName");
        String email          = accountParams.get("email");
        String pwHash         = accountParams.get("pwHash");
        String pwSalt         = accountParams.get("pwSalt");

        Integer userId = 0;

        Timestamp timestamp = new Timestamp(Instant.now().getEpochSecond());

        Integer defUserFlags = Flags.getDefaultUserFlags();

        try {
            database.addUser(accountName, email, pwHash, pwSalt, timestamp, defUserFlags);
        }
        catch (Exception e) {
            throw e;
        }

        UserAccount newUserAccount = new UserAccount.Builder()
            .name(accountName)
            .flags(defUserFlags)
            .email(email)
            .registrationTS(timestamp)
            .build();

        userId = database.getUserId(newUserAccount);
        newUserAccount.setId(userId);

    }

    public void renameChannel(Channel curChan, Channel newChan) throws ChannelNotFoundException { // TODO: unfinished

        int cFlags = Integer.valueOf(curChan.getcServeFlags());
        int sCount = Integer.valueOf(curChan.getSuspendCount());
        int bTime  = Integer.valueOf(curChan.getcServeBanTime());
        int aLimit = Integer.valueOf(curChan.getcServeAutoLimit());

        Timestamp rTS = new Timestamp(curChan.getRegistrationTS());
        Timestamp sTS = new Timestamp(curChan.getSuspendLastTS());

        String sMessage = new String(curChan.getSuspendMessage());
        String wMessage = new String(curChan.getcServeWelcomeMsg());

        Topic rTopic = new Topic(curChan.getCServeRegisteredTopic());

        Map<String, Integer> chanlev = curChan.getChanlev();

        newChan.setRegistered(true);
        curChan.setRegistered(false);

        newChan.setcServeFlags(cFlags);
        newChan.setSuspendCount(sCount);
        newChan.setcServeBanTime(bTime);
        newChan.setcServeAutoLimit(aLimit);
        newChan.setRegistrationTS(rTS);
        newChan.setSuspendLastTS(sTS);
        newChan.setSuspendMessage(sMessage);
        newChan.setcServeWelcomeMsg(wMessage);
        newChan.setcServeRegisteredTopic(rTopic);
        newChan.setChanlev(chanlev);

        curChan.setcServeFlags(0);
        curChan.setSuspendCount(0);
        curChan.setcServeBanTime(0);
        curChan.setcServeAutoLimit(0);
        curChan.setRegistrationTS(null);
        curChan.setSuspendLastTS(null);
        curChan.setSuspendMessage("");
        curChan.setcServeWelcomeMsg("");
        curChan.setcServeRegisteredTopic(null);
        curChan.setChanlev(new HashMap<>());

        var w = new Object(){ UserAccount u; };
        chanlev.forEach(
            (accountName, chanlevVal) -> {
                w.u = UserAccount.getUserByNameCi(accountName);
                w.u.getChanlev().remove(curChan.getName());
                w.u.getChanlev().put(newChan.getName(), chanlevVal);
            }
        );

        try { database.renameChannel(curChan, newChan); }
        catch (ChannelNotFoundException e) {
            log.error("Dispatcher::renameChannel: failure renaming %s to %s", curChan, newChan);
            throw new ChannelNotFoundException();
        }


    }


    public void addChanBei(int type, Channel channel, Bei mask, UserAccount u, String r, long d) {

        Timestamp fromTS  = new Timestamp();
        Timestamp toTS = new Timestamp(0L);

        if(d > 0L) toTS = new Timestamp(fromTS.toLong() + d);

        switch(type) {
            case      Const.CHANBEI_BANS:  channel.addCServeBanList   (mask, u, r, fromTS, toTS); break;
            case   Const.CHANBEI_EXCEPTS:  channel.addCServeExceptList(mask, u, r, fromTS, toTS); break;
            case   Const.CHANBEI_INVITES:  channel.addCServeInviteList(mask, u, r, fromTS, toTS); break;
        }

        try { database.addChanBei(type, channel, mask, u, fromTS, toTS, r); }
        catch(Exception e) { log.error(String.format("Dispatcher::addDbChanBei: error adding BEI mask to database (type %s, channel %s, mask %s)", type, channel, mask)); }

    }

    public void removeDbChanBei(int type, Channel channel, Bei mask) {
        switch(type) {
            case      Const.CHANBEI_BANS:  channel.removeCServeBanList   (mask); break;
            case   Const.CHANBEI_EXCEPTS:  channel.removeCServeExceptList(mask); break;
            case   Const.CHANBEI_INVITES:  channel.removeCServeInviteList(mask); break;
        }

        try { database.removeChanBei(type, channel, mask); }
        catch(Exception e) { log.error(String.format("Dispatcher::addDbChanBei: error deleting BEI mask to database (type %s, channel %s, mask %s)", type, channel, mask)); }

    }


    public void setChanMlock(Channel channel, String s) {

        try { database.setChanMlock(channel, s); }
        catch (Exception e) { log.error(String.format("Dispatcher::setChanMlock: error setting mlock %s for %s in database", s, channel), e); }

        channel.setcServeMLockModes(s);

    }

    public void clearChanMlock(Channel channel) {

        try { database.setChanMlock(channel, ""); }
        catch (Exception e) { log.error(String.format("Dispatcher::clearChanMlock: error clearing mlock for %s in database", channel), e); }

        channel.setcServeMLockModes("");
    }

    public void addNickAlias(NickAlias n) {
        n.getUserAccount().addNickAlias(n);

        try { database.addNickAlias(n); }
        catch (ItemExistsException e) { log.error(String.format("Dispatcher::addNickAlias: failed to add alias '%s' for account '%s'", n, n.getUserAccount()), e); }
    }

    public void removeNickAlias(NickAlias n) {
        n.getUserAccount().removeNickAlias(n);

        database.removeNickAlias(n);
        //catch (ItemExistsException e) { log.error(String.format("Dispatcher::addNickAlias: failed to add alias '%s' for account '%s'", n, n.getUserAccount()), e); }
    }

}
