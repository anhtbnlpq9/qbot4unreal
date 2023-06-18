
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.yaml.snakeyaml.Yaml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration class
 * Loads and parses the configuration file
 * @author me
 */
public class Config {

    private static Logger log = LogManager.getLogger("common-log");

    private InputStream inputStream = null; 

    /* Server parameters */
    private String serverName            = "";
    private String serverId              = "";
    private String serverDescription     = "";
    private String serverProtocolVersion = "6000";
    private String serverVersionFlags    = "";
    private String serverFullVersionText = "qbot4u";
    private String serverVersionString   = "qbot4u - The Q Bot for UnrealIRCd.";

    /* Admin parameters */
    private ArrayList<String> adminInformation = new ArrayList<String>();
    
    /* Link parameters */
    private String  linkPeer     = "";
    private String  linkHost     = "";
    private Integer linkPort     = 0;
    private String  linkPassword = "";    

    /* Chanservice parameters */
    private String    cserviceBotNick                = "";
    private String    cserviceBotUid                 = "";
    private String    cserviceBotIdent               = "";
    private String    cserviceBotHost                = "";
    private String    cserviceBotReal                = "";
    private String    cserviceBotModes               = "";
    private String    cserviceAccountHostPrefix      = "configureme";
    private String    cserviceAccountHostSuffix      = "please";
    private String    cserviceChanDefaultModes       = "";
    private Integer   cserviceAccountMaxCertFP       = 10;
    private Integer   cserviceAccountMinPassLength   = 10;
    private Integer   cserviceAccountMaxPassLength   = 20;
    private Integer   cserviceAccountWrongCredWait   = 3;
    private Integer   cserviceAccountMaxChannels     = 50;
    private Integer   cserviceAccountMaxAuthHistory  = 10;
    private Integer   cserviceChanAutoLimitFreq      = 30;
    private Integer   cserviceChanMaxChanlevs        = 50;


    /* Network parameters */
    private String networkName  = "";

    /* Features parameters */
    private Boolean featureSasl      = false;
    private Boolean featureSvslogin  = false;
    private Boolean featureChgHost   = false;
    HashMap<String, Boolean> featuresList = new HashMap<String, Boolean>();

    /* Logging parameters */
    private Boolean logDebugIn  = false;
    private Boolean logDebugOut = false;

    /* Database parameters */
    private String databasePath = "";

    /* SSL parameters */
    private String sslTruststorePath      = "";
    private String sslTruststorePassword  = "";
    private String sslKeystorePath        = "";
    private String sslKeystorePassword    = "";

   /**
    * Constructor for the class
    * @param configFile Configuration file name
    */
    public Config(String configFile) {
        try {
            inputStream = new FileInputStream(new File(configFile));
        }
        catch (FileNotFoundException e) { log.error(String.format("Config/constructor: configuration file \"%s\" not found: ", configFile), e); }

        Yaml yaml = new Yaml();
        HashMap<String, Object> data  = yaml.load(inputStream);
        
        adminInformation               = (ArrayList<String>) data.get("admin");
        
        HashMap<String, Object> confme   = (HashMap<String, Object>) data.get("me");
        serverName                       = (String) confme.get("name");
        serverId                         = (String) confme.get("sid");
        serverDescription                = (String) confme.get("description");
        serverProtocolVersion            = (String) confme.get("protocolversion");
        serverVersionFlags               = (String) confme.get("versionflags");
        serverFullVersionText            = (String) confme.get("fullversiontext");
        serverVersionString              = (String) confme.get("versionstring");

        HashMap<String, Object> conflink    = (HashMap<String, Object>) data.get("link");
        linkPeer                            = (String)  conflink.get("peer");
        linkHost                            = (String)  conflink.get("host");
        linkPassword                        = (String)  conflink.get("password");
        linkPort                            = (Integer) conflink.get("port"); 
        
        HashMap<String, Object> confcservice    = (HashMap<String, Object>) data.get("cservice");
        HashMap<String, Object> confcserviceBot = (HashMap<String, Object>) confcservice.get("bot");
        cserviceBotNick                           = (String) confcserviceBot.get("nick");
        cserviceBotUid                            = (String) confcserviceBot.get("uid");
        cserviceBotIdent                          = (String) confcserviceBot.get("ident");
        cserviceBotHost                           = (String) confcserviceBot.get("host");
        cserviceBotReal                           = (String) confcserviceBot.get("realname");
        cserviceBotModes                          = (String) confcserviceBot.get("modes");
        HashMap<String, Object> cserviceaccount   = (HashMap<String, Object>) confcservice.get("accountsettings");
        cserviceAccountHostPrefix                 = (String)   cserviceaccount.get("authvhostprefix");
        cserviceAccountHostSuffix                 = (String)   cserviceaccount.get("authvhostsuffix");
        cserviceAccountMaxCertFP                  = (Integer)  cserviceaccount.get("maxcertfp");
        cserviceAccountMinPassLength              = (Integer)  cserviceaccount.get("minpasslen");
        cserviceAccountMaxPassLength              = (Integer)  cserviceaccount.get("maxpasslen");
        cserviceAccountMaxChannels                = (Integer)  cserviceaccount.get("maxchannels");
        cserviceAccountWrongCredWait              = (Integer)  cserviceaccount.get("wrongcredwait");
        HashMap<String, Object> cservicechan   = (HashMap<String, Object>) confcservice.get("chansettings");
        cserviceChanAutoLimitFreq              = (Integer) cservicechan.get("autolimitfreq");
        cserviceChanMaxChanlevs                = (Integer) cservicechan.get("maxchanlevs");
        cserviceChanDefaultModes               = (String)  cservicechan.get("defaultmodes");

        HashMap<String, Object> confNetwork  = (HashMap<String, Object>) data.get("network");
        networkName                          = (String) confNetwork.get("name");

        HashMap<String, Object> confLogging  = (HashMap<String, Object>) data.get("logging");
        logDebugIn                           = (Boolean) confLogging.get("debugIn");
        logDebugOut                          = (Boolean) confLogging.get("debugOut");

        HashMap<String, Object> confFeatures  = (HashMap<String, Object>) data.get("features");
        featureSasl                           = (Boolean) confFeatures.get("sasl");
        featureSvslogin                       = (Boolean) confFeatures.get("svslogin");
        featureChgHost                        = (Boolean) confFeatures.get("chghost");

        featuresList.put("sasl",        featureSasl);
        featuresList.put("svslogin",    featureSvslogin);
        featuresList.put("chghost",     featureChgHost);


        HashMap<String, Object> confDatabase    = (HashMap<String, Object>) data.get("database");
        databasePath                            = (String) confDatabase.get("path");

        HashMap<String, Object> confSsl     = (HashMap<String, Object>) data.get("ssl");

        HashMap<String, Object> sslKeystore     = (HashMap<String, Object>) confSsl.get("keystore");
        sslKeystorePath                         = (String) sslKeystore.get("path");
        sslKeystorePassword                     = (String) sslKeystore.get("password");

        HashMap<String, Object> sslTuststore    = (HashMap<String, Object>) confSsl.get("truststore");
        sslTruststorePath                       = (String) sslTuststore.get("path");
        sslTruststorePassword                   = (String) sslTuststore.get("password");

        log.info("Configuration loaded:");
        log.info("Me ");
        log.info(" -> My name             = " + serverName);
        log.info(" -> My SID              = " + serverId);
        log.info(" -> My description      = " + serverDescription);
        log.info(" -> My protocolVersion  = " + serverProtocolVersion);
        log.info(" -> My versionFlags     = " + serverVersionFlags);
        log.info(" -> My fullVersionText  = " + serverFullVersionText);
        log.info(" -> My versionString    = " + serverVersionString);

        log.info("Peer ");
        log.info(" -> Peer hostname  = " + linkHost);
        log.info(" -> Peer port      = " + linkPort);

        log.info("Database ");
        log.info(" -> Path = " + databasePath);

        log.info("Logging ");
        log.info(" -> LogLevel  = ");
        log.info(" -> Traffic debug input  = " + logDebugIn);
        log.info(" -> Traffic debug output = " + logDebugOut);

        log.info(String.format("CService configured as as %s!%s@%s / %s (%s) , modes %s", cserviceBotNick, cserviceBotIdent, cserviceBotHost, cserviceBotUid, cserviceBotReal, cserviceBotModes));



    }
    

    public Boolean checkConfig() {

        Integer configErrors = 0;

        /* Server parameters */
        if (this.serverName == null) {
            log.fatal("Configuration: me::name is not defined!");
            configErrors++;
        }
        if (this.serverId.isEmpty() == true) {
            log.fatal("Configuration: me::sid is not defined!");
            configErrors++;
        }
        if (this.serverDescription.isEmpty() == true) {
            log.fatal("Configuration: me::description is not defined!");
            configErrors++;
        }

        /* Link parameters */
        if (this.linkPeer.isEmpty() == true) {
            log.fatal("Configuration: link::peer is not defined!");
            configErrors++;
        }
        if (this.linkHost.isEmpty() == true) {
            log.fatal("Configuration: link::host is not defined!");
            configErrors++;
        }
        if (this.linkPort.equals(0) == true) {
            log.fatal("Configuration: link::port is not defined!");
            configErrors++;
        }
        if (this.linkPassword.isEmpty() == true) {
            log.warn("Configuration: link::password is not defined. Unless you are using certificate authentication (in whoch case you should put * as password), the link may not work.");
            configErrors++;
        }

        /* Chanservice parameters */
        if (this.cserviceBotNick == null) {
            log.fatal("Configuration: cservice::nick is not defined!");
            configErrors++;
        }
        if (this.cserviceBotUid.isEmpty() == true) {
            log.fatal("Configuration: cservice::uid is not defined!");
            configErrors++;
        }
        if (this.cserviceBotReal.isEmpty() == true) {
            log.fatal("Configuration: cservice::realname is not defined!");
            configErrors++;
        }
        if (this.cserviceBotIdent.isEmpty() == true) {
            log.fatal("Configuration: cservice::ident is not defined!");
            configErrors++;
        }
        if (this.cserviceBotHost.isEmpty() == true) {
            log.fatal("Configuration: cservice::host is not defined!");
            configErrors++;
        }
        if (cserviceBotModes.isEmpty() == true) {
            log.fatal("Configuration: cservice::modes is not defined!");
            configErrors++;
        }

        /* Network parameters */
        if (networkName.isEmpty() == true) {
            log.fatal("Configuration: network::name is not defined!");
            configErrors++;
        }

        /* Database parameters */
        if (databasePath.isEmpty() == true) {
            log.fatal("Configuration: database::path is not defined!");
            configErrors++;
        }

        /* SSL parameters */
        if (sslTruststorePath.isEmpty() == true) {
            log.fatal("Configuration:ssl::truststore::path is not defined!");
            configErrors++;
        }
        if (sslTruststorePassword.isEmpty() == true) {
            log.fatal("Configuration: ssl::truststore::password is not defined!");
            configErrors++;
        }
        if (sslKeystorePath.isEmpty() == true) {
            log.fatal("Configuration: ssl::keystore::path is not defined!");
            configErrors++;
        }
        if (sslKeystorePassword.isEmpty() == true) {
            log.fatal("Configuration: ssl::keystore::password is not defined!");
            configErrors++;
        }

        if (configErrors > 0) return false;
        else return true;
    }

    /**
     * Returns the server name
     * 
     * @return this.serverName Server name
     */
    public String getServerName() {
        return this.serverName;
    }

    /**
     * Returns the server SID
     * 
     * @return this.serverId server SID
     */
    public String getServerId() {
        return this.serverId;
    }

    /**
     * Returns the server description
     * @return description
     */
    public String getServerDescription() {
        return this.serverDescription;
    }

    /**
     * Returns the remote server name
     * @return remote server name
     */
    public String getLinkPeerName() {
        return this.linkPeer;
    }

    /**
     * Returns the link remote host
     * @return remote host
     */
    public String getLinkPeerHost() {
        return this.linkHost;
    }

    /**
     * Returns the link remote port
     * @return remote port
     */
    public Integer getLinkPeerPort() {
        return this.linkPort;
    }

    /**
     * Returns the link password sent to the peer
     * @return link password
     */
    public String getLinkPassword() {
        return this.linkPassword;
    }
    
    /**
     * Returns the EAUTH string
     * @return EAUTH string
     */
    public String getEAUTH() {
        return this.serverName + "," + this.serverProtocolVersion + "," + this.serverVersionFlags + "," + this.serverFullVersionText;
    }

    /**
     * Returns the server protocol version
     * @return protocol version
     */
    public String getSrvProtocolVersion() {
        return this.serverProtocolVersion;
    }

    /**
     * Returns the server "version flags"
     * @return versionflags
     */
    public String getSrvVersionFlags() {
        return this.serverVersionFlags;
    }

    /**
     * Returns the server "full version text"
     * @return fullversiontext
     */
    public String getSrvFullVersionText() {
        return this.serverFullVersionText;
    }

    /**
     * Returns the CService bot SID
     * @return
     */
    public String getCServeUniq() {
        return this.cserviceBotUid;
    }

    /**
     * Returns the CService nick
     * @return nickname
     */
    public String getCServeNick() {
        return this.cserviceBotNick;
    }

    /**
     * Returns the CService bot ident
     * @return ident
     */
    public String getCServeIdent() {
        return this.cserviceBotIdent;
    }

    /**
     * Returns the CService bot host
     * @return host
     */
    public String getCServeHost() {
        return this.cserviceBotHost;
    }

    /**
     * Returns the CService bot realnam
     * @return gecos
     */
    public String getCServeRealName() {
        return this.cserviceBotReal;
    }

    /**
     * Returns the modes that the CService bot will have
     * @return modes for cserve bot
     */
    public String getCServeModes() {
        return this.cserviceBotModes;
    }

    /**
     * Returns the administrative information
     * @return administrative information
     */
    public ArrayList<String> getAdminInfo() {
        return this.adminInformation;
    }

    /**
     * Returns the network name
     * @return network name
     */
    public String getNetworkName() {
        return this.networkName;
    }

    /**
     * Fetches the configured database path
     * @return Path of the database
     */
    public String getDatabasePath() {
        return this.databasePath;
    }   

    /**
     * Returns the features of services
     * @param feature
     * @return feature enable/disable
     */
    public Boolean getFeature(String feature) {
        if (this.featuresList.containsKey(feature) == true) {
            return this.featuresList.get(feature);
        }
        else {
            return false;
        }
    }

    /**
     * Returns logging parameters
     * @param source steam direction
     * @return logging status
     */
    public Boolean getLogging(String source) {
        switch (source) {
            case "debugIn":
                return this.logDebugIn;

            case "debugOut":
                return this.logDebugOut;

            default:
                return false;
        }
    }

    /**
     * Returns the key store path
     * @return path
     */
    public String getKeyStorePath() {
        return sslKeystorePath;
    }

    /**
     * Returns the key store password
     * @return password
     */
    public String getKeyStorePassword() {
        return sslKeystorePassword;
    }

    /**
     * Returns the trust store path
     * @return path
     */
    public String getTrustStorePath() {
        return sslTruststorePath;
    }

    /**
     * Returns the trust store password
     * @return password
     */
    public String getTrustStorePassword() {
        return sslTruststorePassword;
    }

    /**
     * Returns the configures vhost prefix for user authentication
     * @return host prefix
     */
    public String getCServeHostPrefix() {
        return this.cserviceAccountHostPrefix;
    }

    /**
     * Returns the configures vhost suffix for user authentication
     * @return host suffix
     */
    public String getCServeHostSuffix() {
        return this.cserviceAccountHostSuffix;
    }

    /**
     * Returns the frequency in seconds of the channel autolimit auto settings
     * @return frequency in seconds
     */
    public Integer getCServeAutoLimitFreq(){
        return this.cserviceChanAutoLimitFreq;
    }

    public Integer getCServeAccountMaxCertFP() {
        return cserviceAccountMaxCertFP;
    }

    public Integer getCServeAccountMaxChannels() {
        return cserviceAccountMaxChannels;
    }

    public Integer getCServeAccountMaxAuthhistory() {
        return cserviceAccountMaxAuthHistory;
    }

    public Integer getCServeChanMaxChanlevs() {
        return cserviceChanMaxChanlevs;
    }

    public Integer getCServeAccountWrongCredWait() {
        return cserviceAccountWrongCredWait;
    }

    public Integer getCServiceAccountMinPassLength() {
        return this.cserviceAccountMinPassLength;
    }

    public Integer getCServiceAccountMaxPassLength() {
        return this.cserviceAccountMaxPassLength;
    }

    public String getCServeVersionString() {
        return this.serverVersionString;
    }

    public String getCserveChanDefaultModes() {
        return this.cserviceChanDefaultModes;
    }


}