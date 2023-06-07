
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.yaml.snakeyaml.Yaml;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration class
 * Loads and parses the configuration file
 * @author me
 */
public class Config {

    private static Logger log = LogManager.getLogger("common-log");
    private static final Level INFO2 = Level.forName("INFO2", 050);

    private InputStream inputStream = null; 

    /* Server parameters */
    private String serverName;
    private String serverId;
    private String serverDescription;
    private String serverProtocolVersion;
    private String serverVersionFlags;
    private String serverFullVersionText;
    private String serverVersionString;

    /* Admin parameters */
    private ArrayList<String> adminInformation = new ArrayList<String>();
    
    /* Link parameters */
    private String  linkPeer;
    private String  linkHost;
    private Integer linkPort;
    private String  linkPassword;    

    /* Chanservice parameters */
    private String    cserviceNick;
    private String    cserviceUniq;
    private String    cserviceIdent;
    private String    cserviceHost;
    private String    cserviceReal;
    private String    cserviceModes;
    private String    cserviceAccountHostPrefix;
    private String    cserviceAccountHostSuffix;
    private Integer   cserviceAccountMaxCertFP;
    private Integer   cserviceAccountMinPassLength;
    private Integer   cserviceAccountMaxPassLength;
    private Integer   cserviceAccountWrongCredWait;
    private Integer   cserviceAccountMaxChannels;
    private Integer   cserviceAccountMaxAuthHistory;
    private Integer   cserviceChanAutoLimitFreq;
    private Integer   cserviceChanMaxChanlevs;


    /* Network parameters */
    private String networkName;

    /* Features parameters */
    private Boolean featureSasl;
    private Boolean featureSvslogin;
    private Boolean featureChgHost;
    HashMap<String, Boolean> featuresList = new HashMap<String, Boolean>();

    /* Logging parameters */
    private Boolean logDebugIn;
    private Boolean logDebugOut;

    /* Database parameters */
    private String databasePath;

    /* SSL parameters */
    private String sslTruststorePath;
    private String sslTruststorePassword;
    private String sslKeystorePath;
    private String sslKeystorePassword;

   /**
    * Constructor for the class
    * @param configFile Configuration file name
    */
    public Config(String configFile) {
        try {
            inputStream = new FileInputStream(new File(configFile));
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }

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
        
        HashMap<String, Object> confcservice   = (HashMap<String, Object>) data.get("cservice");
        cserviceNick                           = (String) confcservice.get("nick");
        cserviceUniq                           = (String) confcservice.get("uniq");
        cserviceIdent                          = (String) confcservice.get("ident");
        cserviceHost                           = (String) confcservice.get("host");
        cserviceReal                           = (String) confcservice.get("realname");
        cserviceModes                          = (String) confcservice.get("modes");
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
        log.info(" -> My name         = " + serverName);
        log.info(" -> My SID          = " + serverId);
        log.info(" -> My description  = " + serverDescription);

        log.info("Peer ");
        //log.info(" -> Peer name      = " + linkPeer);
        log.info(" -> Peer hostname  = " + linkHost);
        log.info(" -> Peer port      = " + linkPort);

        log.info("Database ");
        log.info(" -> Path = " + databasePath);

        log.info("Logging ");
        log.info(" -> LogLevel  = ");
        log.info(" -> Traffic debug input  = " + logDebugIn);
        log.info(" -> Traffic debug output = " + logDebugOut);



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
        return this.cserviceUniq;
    }

    /**
     * Returns the CService nick
     * @return nickname
     */
    public String getCServeNick() {
        return this.cserviceNick;
    }

    /**
     * Returns the CService bot ident
     * @return ident
     */
    public String getCServeIdent() {
        return this.cserviceIdent;
    }

    /**
     * Returns the CService bot host
     * @return host
     */
    public String getCServeHost() {
        return this.cserviceHost;
    }

    /**
     * Returns the CService bot realnam
     * @return gecos
     */
    public String getCServeRealName() {
        return this.cserviceReal;
    }

    /**
     * Returns the modes that the CService bot will have
     * @return modes for cserve bot
     */
    public String getCServeModes() {
        return this.cserviceModes;
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


}