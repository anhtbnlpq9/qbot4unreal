
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

    private String serverName;
    private String serverId;
    private String serverDescription;
    private String serverProtocolVersion;
    private String serverVersionFlags;
    private String serverFullVersionText;

    private ArrayList<String> adminInformation = new ArrayList<String>();
    
    private String  linkPeer;
    private String  linkHost;
    private Integer linkPort;
    private String  linkPassword;    

    private String cserviceNick;
    private String cserviceUniq;
    private String cserviceIdent;
    private String cserviceHost;
    private String cserviceReal;
    private String cserviceModes;
    private String cserviceStaticChan;

    private String networkName;

    private Boolean featureSasl;

    private Boolean logDebugIn;
    private Boolean logDebugOut;

    HashMap<String, Boolean> featuresList = new HashMap<String, Boolean>();

   /**
    * Constructor for the class
    * ...
    * 
    * @param configFile Configuration file name
    */
    public Config(String configFile) {
        try {
            inputStream = new FileInputStream(new File(configFile));
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        LinkedHashMap confme       = (LinkedHashMap) data.get("me");
        LinkedHashMap conflink     = (LinkedHashMap) data.get("link");
        LinkedHashMap confcservice = (LinkedHashMap) data.get("cservice");
        LinkedHashMap confNetwork  = (LinkedHashMap) data.get("network");
        LinkedHashMap confLogging  = (LinkedHashMap) data.get("logging");
        LinkedHashMap confFeatures = (LinkedHashMap) data.get("features");

        adminInformation           = (ArrayList<String>) data.get("admin");
        
        serverName              = (String) confme.get("name");
        serverId                = (String) confme.get("sid");
        serverDescription       = (String) confme.get("description");
        serverProtocolVersion   = (String) confme.get("protocolversion");
        serverVersionFlags      = (String) confme.get("versionflags");
        serverFullVersionText   = (String) confme.get("fullversiontext");

        linkPeer     = (String)  conflink.get("peer");
        linkHost     = (String)  conflink.get("host");
        linkPassword = (String)  conflink.get("password");
        linkPort     = (Integer) conflink.get("port"); 
        
        cserviceNick         = (String) confcservice.get("nick");
        cserviceUniq         = (String) confcservice.get("uniq");
        cserviceIdent        = (String) confcservice.get("ident");
        cserviceHost         = (String) confcservice.get("host");
        cserviceReal         = (String) confcservice.get("realname");
        cserviceModes        = (String) confcservice.get("modes");
        cserviceStaticChan   = (String) confcservice.get("staticchan");

        networkName          = (String) confNetwork.get("name");

        logDebugIn           = (Boolean) confLogging.get("debugIn");
        logDebugOut          = (Boolean) confLogging.get("debugOut");

        featureSasl          = (Boolean) confFeatures.get("sasl");



        if (featureSasl == true) featuresList.put("sasl", true);
        else featuresList.put("sasl", false);

        
        System.out.println("* Config:\n"
                        + "  +- Me name             = " + serverName  + "\n"
                        + "  |--- sid               = " + serverId + "\n"
                        + "  |--- description       = " + serverDescription + "\n"
                        + "  +- Link peer name      = " + linkPeer + "\n"
                        + "  |--- peer host         = " + linkHost + "\n"
                        + "  |--- peer port         = " + linkPort + "\n"
                        + "  +- Logging             = " + linkPeer + "\n"
                        + "  |--- debug in          = " + logDebugIn + "\n"
                        + "  `--- debug out         = " + logDebugOut + "\n");
        
        //System.out.println("conf=" + data.toString());
        //System.out.println("admin=" + adminInformation);

                        
        //UserNode cservice = new UserNode(cserviceNick, cserviceUniq, );
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

}