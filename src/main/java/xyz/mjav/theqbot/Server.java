package xyz.mjav.theqbot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import xyz.mjav.theqbot.exceptions.ItemExistsException;
import xyz.mjav.theqbot.exceptions.ItemNotFoundException;
import xyz.mjav.theqbot.exceptions.ServerNotFoundException;

/**
 * ServerNode class
 *
 * @author me
 */
public class Server {


    private static Set<Server> serverList = new HashSet<>();

    private static Map<String, Server>   serverListBySid  = new HashMap<>();
    private static Map<String, Server>  serverListByName  = new HashMap<>();

    private static Server peerServer;

    private String name; /* This server name */
    private String sid;  /* This server id (SID) */
    private String description; /* This server description */
    private String certfp; /* Server certificate fingerprint */
    private String country; /* Server country */

    private Timestamp ts; /* This server joining timestamp */

    private Integer distance; /* This server distance in hops from here */

    private Boolean hasPeerResponded; /* is set true once the peer server has responded to our SERVER */
    private Boolean isThePeer; /* Is the server the peer from here */
    private Boolean hasEOS; /* Has the server reached EOS */

    private Set<Nick>   localUsers; /* Set of the server local users */
    private Set<Server> childNodes; /* Set of the servers connected to this one */

    private Server parentNode; /* Server that has introduced this server, from our point of view */

    /**
     * Constructor called tp declare new servers on the network usually following SINFO or SERVER
     * @param serverName
     * @param serverDistance
     * @param serverId
     * @param serverDescription
     */
    public Server(String serverName, Integer serverDistance, String serverId, String serverDescription) {
        this.name        = serverName;
        this.distance    = serverDistance;
        this.sid         = serverId;
        this.description = serverDescription;
        this.isThePeer   = false;
    }

    /**
     * Constructor called to declare the peer (1st remote server added)
     * @param serverId network server id
     */
    public Server(String serverId) {
        this.sid       = serverId;
        this.isThePeer = false;
    }

    /**
     * Adds an network user to the memory nick list
     * @param u user node
     * @throws ItemExistsException when the nick to add already exists in the list
     */
    public static void addServer(Server s) throws ItemExistsException {

        if(serverList.contains(s) == true)  throw new ItemExistsException("The nick is already registered on the network.");

        serverList.add(s);
        serverListByName.put(s.name.toLowerCase(), s);
        serverListBySid.put(s.sid.toUpperCase(), s);
    }

    /**
     * Removes an network user to the memory nick list
     * @param u user node
     * @throws ItemNotFoundException when the nick to remove is not in the list
     */
    public static void removeServer(Server s) throws ItemNotFoundException {

        if(serverList.contains(s) == false) throw new ItemNotFoundException("The nick is not registered on the network.");

        serverList.remove(s);
        serverListByName.remove(s.name.toLowerCase());
        serverListBySid.remove(s.sid.toUpperCase());
    }

    /**
     * Returns a user node looked up by its nick (case insensitive)
     * @param nick user nick
     * @return user node
     * @throws ItemNotFoundException when the user does not exist
     */
    public static Server getServerByNameCi(String s) throws ItemNotFoundException {
        if (serverListByName.containsKey(s.toLowerCase()) == false) throw new ItemNotFoundException();
        return serverListByName.get(s.toLowerCase());
    }

    /**
     * Returns a user node looked up by its UID
     * @param uid user UID
     * @return user node
     * @throws ItemNotFoundException when the user does not exist
     */
    public static Server getServerBySid(String s) throws ItemNotFoundException {
        if (serverListBySid.containsKey(s.toUpperCase()) == false) throw new ItemNotFoundException();
        return serverListBySid.get(s.toUpperCase());
    }

    /**
     * Returns the network users list
     * @return a copy of network users list Set
     */
    public static Set<Server> getServerList() {
        return new HashSet<Server>(serverList);
    }

    /**
     * Sets the network user list
     * @param l user list Set
     */
    public static void setServerList(Set<Server> l) {
        serverList = new HashSet<Server>(l);

        /* Then populates the other lists */
        serverList.forEach(
            (s) -> {
                serverListBySid.put(s.sid.toUpperCase(), s);
                serverListByName.put(s.name.toLowerCase(), s);
            }
        );
    }

    /**
     * Returns a Server object based on a string (that can be a server name or a SID)
     * @param s input server name or SID
     * @return server object
     * @throws ServerNotFoundException when there is no match
     */
    public static Server getServer(String s) throws ServerNotFoundException {

        Server server;

        /* Check if the string is a UID */
        if (serverListBySid.containsKey(s.toUpperCase()) == true) server = serverListBySid.get(s);

        /* Else check if the string is a nick */
        else if (serverListByName.containsKey(s.toLowerCase()) == true) server = serverListByName.get(s);

        /* Else the string is neither a UID or a nick => entity is not on the network */
        else throw new ServerNotFoundException(String.format("Entity %s has not been found on the network", s));

        return server;

    }

    public static class Builder {

        private String name;
        private String sid;
        private String description = "";
        private String certfp = "";
        private String country = "";

        private Timestamp ts = new Timestamp();

        private Integer distance;

        private Boolean hasPeerResponded    = false;
        private Boolean isThePeer           = false;
        private Boolean hasEOS              = false;

        private Set<Nick>   localUsers = new HashSet<>();
        private Set<Server> childNodes = new HashSet<>();

        private Server parentNode = null;

        public Builder(String name, String sid) {
            this.name   = name;
            this.sid    = sid;
        }

        public Builder(String sid) {
            this.sid    = sid;
        }

        public Builder name(String val) {
            this.name = val;
            return this;
        }

        public Builder sid(String val) {
            this.sid = val;
            return this;
        }

        public Builder description(String val) {
            this.description = val;
            return this;
        }

        public Builder certfp(String val) {
            this.certfp = val;
            return this;
        }

        public Builder ts(Timestamp val) {
            this.ts = val;
            return this;
        }

        public Builder distance(Integer val) {
            this.distance = val;
            return this;
        }

        public Builder hasPeerResponded(Boolean val) {
            this.hasPeerResponded = val;
            return this;
        }

        public Builder isThePeer(Boolean val) {
            this.isThePeer = val;
            return this;
        }

        public Builder hasEOS(Boolean val) {
            this.hasEOS = val;
            return this;
        }

        public Builder localUsers(Set<Nick> val) {
            this.localUsers = val;
            return this;
        }

        public Builder childNodes(Set<Server> val) {
            this.childNodes = val;
            return this;
        }

        public Builder parentNode(Server val) {
            this.parentNode = val;
            return this;
        }

        public Builder country(String val) {
            this.country = val;
            return this;
        }

        public Server build() {
            return new Server(this);
        }

    }

    private Server(Builder builder) {
        this.name               = builder.name;
        this.sid                = builder.sid;
        this.description        = builder.description;
        this.certfp             = builder.certfp;
        this.ts                 = builder.ts;
        this.distance           = builder.distance;
        this.hasPeerResponded   = builder.hasPeerResponded;
        this.isThePeer          = builder.isThePeer;
        this.hasEOS             = builder.hasEOS;
        this.localUsers         = builder.localUsers;
        this.childNodes         = builder.childNodes;
        this.parentNode         = builder.parentNode;
        this.country            = builder.country;

        if (this.isThePeer == true) Server.peerServer = this;
    }

    /**
     * Sets if the server has done syncing
     * @param hasEos true or false
     */
    public void setEOS(Boolean hasEos) {
        this.hasEOS = hasEos;
    }

    /**
     * Sets if the server is the peer connected to CService
     * @param isPeer true or false
     */
    public void setPeer(Boolean isPeer) {
        this.isThePeer = isPeer;
    }

    public static Server getPeer() {
        return Server.peerServer;
    }

    /**
     * Sets the server name (as appear in /MAP)
     * @param name server name
     */
    public void setName(String name) {
        serverListByName.remove(this.name.toLowerCase());
        this.name = name;
        serverListByName.put(name.toLowerCase(), this);
    }

    /**
     * Sets the server description (as seen in /LINKS)
     * @param desc
     */
    public void setDescription(String desc) {
        this.description = desc;
    }

    /**
     * Sets the server hop count to CService
     * @param dist distance in hops
     */
    public void setDistance(Integer dist) {
        this.distance = dist;
    }

    /**
     * Sets if the server peer has responded (1st message sent fron the peer)
     * @param hasPeerResponded true or false
     */
    public void setPeerResponded(Boolean hasPeerResponded) {
        this.hasPeerResponded = hasPeerResponded;
    }

    /**
     * Returns the server name (as listed in /map)
     * @return server name
     */
    public String getName() {
        return this.name;
    }

    public Nick getLocalUser(Nick node) {
        if (this.localUsers.contains(node) == true) return node;
        else return null;
    }

    public Set<Nick> getLocalUsers() {
        return this.localUsers;
    }

    public void addLocalUser(Nick node) {
        this.localUsers.add(node);
    }

    public void removeLocalUser(Nick node) {
        this.localUsers.remove(node);
    }

    /**
     * Returns the server network SID
     * @return server SID
     */
    public String getSid() {
        return this.sid;
    }

    public void setParent(Server server) {
        this.parentNode = server;
    }

    public Server getParent() {
        return this.parentNode;
    }

    public void addChildren(Server server) {
        this.childNodes.add(server);
    }

    public void delChildren(Server server) {
        this.childNodes.remove(server);
    }

    public Set<Server> getChildren() {
        return this.childNodes;
    }

    /**
     * Returns if the server is the one directly connected to CServive (peer)
     * @return true or false
     */
    public Boolean isPeer() {
        return this.isThePeer;
    }

    /**
     * Returns if the server has done syncing to the network
     * @return true or false
     */
    public Boolean hasEOS() {
        return this.hasEOS;
    }
    /**
     * Gets if the peer server has responded (sent 1st message)
     * @return true or false
     */
    public Boolean hasPeerResponded() {
        return this.hasPeerResponded;
    }

    /**
     * Returns the server description as listed in /LINKS
     * @return server description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the server timestamp
     * @return server timestamp
     */
    public Timestamp getTS() {
        return ts;
    }

    public void setTS(Timestamp ts) {
        this.ts = ts;
    }

    /**
     * Returns the hop count from the server to CService
     * @return hop count from the server to CService
     */
    public Integer getDistance() {
        return distance;
    }

    public void setCertFP(String certfp) {
        this.certfp = certfp;
    }

    public String getCertFP() {
        return this.certfp;
    }

    /**
     * Returns the country
     * @return country
     */
    public String getCountry() {
        return this.country;
    }

    /**
     * Returns the country
     * @return country
     */
    public String getCountry(String s) {
        /* cc=FR|cd=France */

        String nameraw = "";
        String coderaw = "";

        String name = "";
        String code = "";

        String[] split;

        split = this.country.split("\\|");

        try { coderaw = split[0]; }
        catch (IndexOutOfBoundsException e) { coderaw = "cc=00"; }

        try { nameraw = split[1]; }
        catch (IndexOutOfBoundsException e) { nameraw = "cd=CountryParseError"; }

        split = coderaw.split("=");
        try { code = split[1]; }
        catch (IndexOutOfBoundsException e) { code = "00"; }

        split = nameraw.split("=");
        try { name = split[1]; }
        catch (IndexOutOfBoundsException e) { name = "CountryParseError"; }

        switch (s) {
            case "name": return name;
            case "code": return code;
            default: return code;
        }

    }

    public void setCountry(String s) {
        this.country = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}