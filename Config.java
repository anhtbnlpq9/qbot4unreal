
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.yaml.snakeyaml.Yaml;

public class Config {

    public InputStream inputStream = null; 

    public String serverName;
    public String serverId;
    public String serverDescription;
    public String serverProtocolVersion;
    public String serverVersionFlags;
    public String serverFullVersionText;

    public String[] adminInformation;
    
    public String  linkPeer;
    public String  linkHost;
    public Integer linkPort;
    public String  linkPassword;    

    public String cserviceNick;
    public String cserviceUniq;
    public String cserviceIdent;
    public String cserviceHost;
    public String cserviceReal;
    public String cserviceModes;
    public String cserviceStaticChan;

   /**
    * Constructor for the class
    * ...
    * 
    * @param configFile Configuration file name
    */
    public Config(String configFile) {

        //this.inputStream = inputStream;

        try {
            inputStream = new FileInputStream(new File(configFile));
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        LinkedHashMap confme       = (LinkedHashMap) data.get("me");
        LinkedHashMap conflink     = (LinkedHashMap) data.get("link");
        LinkedHashMap confcservice = (LinkedHashMap) data.get("cservice");   
        
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
        
        System.out.println("* Config:\n"
                        + "  +- Me name             = " + serverName  + "\n"
                        + "  |---  sid              = " + serverId + "\n"
                        + "  |---  description      = " + serverDescription + "\n"
                        + "  |---  protocolversion  = " + serverProtocolVersion  + "\n"
                        + "  |---  versionflags     = " + serverVersionFlags + "\n"
                        + "  |---  fullversiontext  = " + serverFullVersionText + "\n"
                        + "  +- Link peer name      = " + linkPeer + "\n"
                        + "  `--- peer host         = " + linkHost + "\n");
                        
        //UserNode cservice = new UserNode(cserviceNick, cserviceUniq, );
    }

    public String getServerName() {
        return this.serverName;
    }

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

    public String getCServeUniq() {
        return this.cserviceUniq;
        //return this.serverId+this.cserviceUniq;
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

}