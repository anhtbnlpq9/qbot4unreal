
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.yaml.snakeyaml.Yaml;

public class Config {

    public InputStream inputStream = null; 

    /* Server parameters */
    private String serverName;
    private String serverId;
    private String serverDescription;
    private String serverProtocolVersion;
    private String serverVersionFlags;
    private String serverFullVersionText;

    /* Admin parameters */
    private ArrayList<String> adminInformation = new ArrayList<String>();
    
    /* Link parameters */
    private String  linkPeer;
    private String  linkHost;
    private Integer linkPort;
    private String  linkPassword;    

    /* Chanservice parameters */
    private String cserviceNick;
    private String cserviceUniq;
    private String cserviceIdent;
    private String cserviceHost;
    private String cserviceReal;
    private String cserviceModes;
    private String cserviceStaticChan;

    /* Network parameters */
    private String networkName;

    /* Features parameters */
    private Boolean featureSasl;
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
        Map<String, Object> data = yaml.load(inputStream);
        
        
        adminInformation              = (ArrayList<String>) data.get("admin");
        
        LinkedHashMap confme          = (LinkedHashMap) data.get("me");
        serverName                    = (String) confme.get("name");
        serverId                      = (String) confme.get("sid");
        serverDescription             = (String) confme.get("description");
        serverProtocolVersion         = (String) confme.get("protocolversion");
        serverVersionFlags            = (String) confme.get("versionflags");
        serverFullVersionText         = (String) confme.get("fullversiontext");

        LinkedHashMap conflink        = (LinkedHashMap) data.get("link");
        linkPeer                      = (String)  conflink.get("peer");
        linkHost                      = (String)  conflink.get("host");
        linkPassword                  = (String)  conflink.get("password");
        linkPort                      = (Integer) conflink.get("port"); 
        
        LinkedHashMap confcservice    = (LinkedHashMap) data.get("cservice");
        cserviceNick                  = (String) confcservice.get("nick");
        cserviceUniq                  = (String) confcservice.get("uniq");
        cserviceIdent                 = (String) confcservice.get("ident");
        cserviceHost                  = (String) confcservice.get("host");
        cserviceReal                  = (String) confcservice.get("realname");
        cserviceModes                 = (String) confcservice.get("modes");
        cserviceStaticChan            = (String) confcservice.get("staticchan");

        LinkedHashMap confNetwork     = (LinkedHashMap) data.get("network");
        networkName                   = (String) confNetwork.get("name");

        LinkedHashMap confLogging     = (LinkedHashMap) data.get("logging");
        logDebugIn                    = (Boolean) confLogging.get("debugIn");
        logDebugOut                   = (Boolean) confLogging.get("debugOut");

        LinkedHashMap confFeatures    = (LinkedHashMap) data.get("features");
        featureSasl                   = (Boolean) confFeatures.get("sasl");

        if (featureSasl == true) featuresList.put("sasl", true);
        else featuresList.put("sasl", false);

        LinkedHashMap confDatabase    = (LinkedHashMap) data.get("database");
        databasePath                  = (String) confDatabase.get("path");

        LinkedHashMap confSsl         = (LinkedHashMap) data.get("ssl");

        LinkedHashMap sslKeystore     = (LinkedHashMap) confSsl.get("keystore");
        sslKeystorePath               = (String) sslKeystore.get("path");
        sslKeystorePassword           = (String) sslKeystore.get("password");

        LinkedHashMap sslTuststore    = (LinkedHashMap) confSsl.get("truststore");
        sslTruststorePath             = (String) sslTuststore.get("path");
        sslTruststorePassword         = (String) sslTuststore.get("password");


        System.out.println("* Config:\n"
                        + "  +- Me name             = " + serverName  + "\n"
                        + "  |--- sid               = " + serverId + "\n"
                        + "  |--- description       = " + serverDescription + "\n"
                        + "  +- Link peer name      = " + linkPeer + "\n"
                        + "  |--- peer host         = " + linkHost + "\n"
                        + "  |--- peer port         = " + linkPort + "\n"
                        + "  +- Database path       = " + databasePath + "\n"                       
                        + "  +- Logging             = " + linkPeer + "\n"
                        + "  |--- debug in          = " + logDebugIn + "\n"
                        + "  `--- debug out         = " + logDebugOut + "\n");
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
    public String getServerDescription() {
        return this.serverDescription;
    }
    public String getLinkPeerName() {
        return this.linkPeer;
    }
    public String getLinkPeerHost() {
        return this.linkHost;
    }
    public Integer getLinkPeerPort() {
        return this.linkPort;
    }
    public String getLinkPassword() {
        return this.linkPassword;
    }
    public String getEAUTH() {
        return this.serverName + "," + this.serverProtocolVersion + "," + this.serverVersionFlags + "," + this.serverFullVersionText;
    }
    public String getSrvProtocolVersion() {
        return this.serverProtocolVersion;
    }
    public String getSrvVersionFlags() {
        return this.serverVersionFlags;
    }
    public String getSrvFullVersionText() {
        return this.serverFullVersionText;
    }
    public String getCServeUniq() {
        return this.cserviceUniq;
    }
    public String getCServeNick() {
        return this.cserviceNick;
    }
    public String getCServeIdent() {
        return this.cserviceIdent;
    }
    public String getCServeHost() {
        return this.cserviceHost;
    }
    public String getCServeRealName() {
        return this.cserviceReal;
    }
    public String getCServeModes() {
        return this.cserviceModes;
    }
    public String getCServeStaticChan() {
        return this.cserviceStaticChan;
    }
    public ArrayList<String> getAdminInfo() {
        return this.adminInformation;
    }
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
    public Boolean getFeature(String feature) {
        switch (feature) {
            case "sasl":
                return this.featuresList.get(feature);
    
            default:
                return false;
        }
    }  
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

}