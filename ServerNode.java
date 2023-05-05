

public class ServerNode {
    
    /*
     * Server:
     * - name
     * - id
     * - description
     * - timestamp
     * - distance
     * - is peer
     * - is EOS
     */

    public String serverName;
    public String serverId;
    public String serverDescription;
    public String serverTS;
    public String serverDistance;
    
    public Boolean serverIsPeer = false;
    public Boolean serverIsEOS  = false;

    public ServerNode() {
        
    }

    public ServerNode(String serverName, String serverDistance, String serverId, String serverDescription) {
        this.serverName = serverName;
        this.serverDistance = serverDistance;
        this.serverId = serverId;
        this.serverDescription = serverDescription;
        this.serverIsPeer = false;
    } 

    public ServerNode(String serverId) {
        this.serverId = serverId;
        this.serverIsPeer = false;
    } 

    public void setEOS(Boolean eos) {
        this.serverIsEOS = eos;
    }

    public void setServerName(String name) {
        this.serverName = name;
    } 

    public void setServerDescription(String desc) {
        this.serverDescription = desc;
    }  

    public void setServerDistance(String dist) {
        this.serverDistance = dist;
    }  

    public String getServerName() {
        return this.serverName;
    }

    public Boolean getServerEOS() {
        return this.serverIsEOS;
    }
}