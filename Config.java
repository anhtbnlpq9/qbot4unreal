
import java.io.* ;
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
    
    public String linkPeer;
    public String linkHost;
    public Integer linkPort;
    public String linkPassword;    

    public String cserviceNick;
    public String cserviceUniq;
    public String cserviceIdent;
    public String cserviceHost;
    public String cserviceReal;


    public Config(String configFile) {
        
        this.inputStream = inputStream;

        try {
            inputStream = new FileInputStream(new File(configFile));
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }

        Yaml yaml = new Yaml();

        Map<String, Object> data = yaml.load(inputStream);

        //System.out.println(data);
        //System.out.println(data.get("me"));
        
        
        LinkedHashMap confme = (LinkedHashMap) data.get("me");
        
        serverName              = (String) confme.get("name");
        serverId                = (String) confme.get("sid");
        serverDescription       = (String) confme.get("description");
        serverProtocolVersion   = (String) confme.get("protocolversion");
        serverVersionFlags      = (String) confme.get("versionflags");
        serverFullVersionText   = (String) confme.get("fullversiontext");

        LinkedHashMap conflink = (LinkedHashMap) data.get("link");
        linkPeer = (String) conflink.get("peer");
        linkHost = (String) conflink.get("host");
        linkPort = (Integer) conflink.get("port");
        linkPassword = (String) conflink.get("password");

        LinkedHashMap confcservice = (LinkedHashMap) data.get("cservice");        
        cserviceNick    = (String) conflink.get("nick");
        cserviceUniq    = (String) conflink.get("uniq");
        cserviceIdent   = (String) conflink.get("ident");
        cserviceHost    = (String) conflink.get("host");
        cserviceReal    = (String) conflink.get("realname");


        System.out.println("* Config:\n"
        + "  --> Me name             = " + serverName  + "\n"
        + "  --> Me SID              = " + serverId + "\n"
        + "  --> Me Description      = " + serverDescription + "\n"
        + "  --> Me protocolversion  = " + serverProtocolVersion  + "\n"
        + "  --> Me versionflags     = " + serverVersionFlags + "\n"
        + "  --> Me fullversiontext  = " + serverFullVersionText + "\n"
        + "  --> Link peer name      = " + linkPeer + "\n"
        + "  --> Link Peer host      = " + linkHost + "\n");
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
    
}