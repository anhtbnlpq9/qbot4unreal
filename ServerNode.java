/**
 * ServerNode class
 *
 * Class to store network servers:
 * - name
 * - id
 * - description
 * - timestamp
 * - distance
 * - is peer
 * - is EOS
 *
 * @author me
 */ 

 
public class ServerNode {

    private String serverName;
    private String serverId;
    private String serverDescription;
    private String serverTS;
    private String serverDistance;
    private ServerNode introducedBy;
    
    // Only used for me (detect that server peer has responded => it exists in serverList)
    private Boolean serverPeerResponded = null;
    
    private Boolean serverIsPeer = false;
    private Boolean serverIsEOS  = false;

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

    public void setPeer(Boolean peer) {
        this.serverIsPeer = peer;
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

    public void setServerPeerResponded(Boolean serverPeerResponded) {
        this.serverPeerResponded = serverPeerResponded;
    }
    
    public String getServerName() {
        return this.serverName;
    }

    public String getServerId() {
        return this.serverId;
    }

    public Boolean getServerPeer() {
        return this.serverIsPeer;
    }

    public Boolean getServerEOS() {
        return this.serverIsEOS;
    }
    public Boolean getServerPeerResponded() {
        return this.serverPeerResponded;
    }

    public ServerNode getIntroducedBy() {
        return this.introducedBy;
    }

    public void setIntroducedBy(ServerNode introducer) {
        this.introducedBy = introducer;
    }


}