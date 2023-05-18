import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.Exception;
import java.lang.String;
import java.lang.Thread;
import java.net.Socket;
import java.util.Map;
import javax.net.ssl.SSLSocketFactory;

public class Client implements Runnable {
    
    public Config config;
    
    public Socket clientSocket;
    private BufferedReader in;
    public BufferedWriter out;
    private boolean clientReady = false;
    private Protocol protocol;
    private SqliteDb sqliteDb;
    CService cservice;
    
    Thread thread;

    long unixTime;

    public Client(Config config, SqliteDb sqliteDb) {
        this.config   = config;
        this.sqliteDb = sqliteDb;
        
        System.setProperty("javax.net.ssl.trustStore",          config.getTrustStorePath());
        System.setProperty("javax.net.ssl.trustStorePassword",  config.getTrustStorePassword());
        System.setProperty("javax.net.ssl.keyStore",            config.getKeyStorePath());
        System.setProperty("javax.net.ssl.keyStorePassword",    config.getKeyStorePassword());
    }

    public Client getClientRef() {
        return this;
    }

    public void run() {
        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            clientSocket = ssf.createSocket(config.getLinkPeerHost(), config.getLinkPeerPort());
            
            //SSLSession session = ((SSLSocket) clientSocket).getSession();
            //Certificate[] cchain = session.getPeerCertificates();

            System.out.println("* Connected");

            protocol = new Protocol(config, sqliteDb);
            protocol.setClientRef(this);

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          
            System.out.println("* Client is ready");
            this.clientReady = true;

            String str;
            while ((str = in.readLine()) != null) {
                if (config.getLogging("debugIn") == true) { System.out.println("<<< " + str); }
                protocol.getResponse(str);
            }
            throw new Exception("Connection has been closed.");
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void launchCService() {
        
        Map<String, ServerNode>   serverList = protocol.getServerList();

        cservice = new CService(this, protocol, sqliteDb);

        while (serverList.get(config.getServerId()).getServerPeerResponded() != true) {
            System.out.println("* Waiting for peer to register");
            try {
                Thread.sleep(2000);
            }
            catch (Exception e) { e.printStackTrace(); }
        }

        while ((serverList.get(protocol.getPeerId())).getServerEOS() != true) {
            System.out.println("* Waiting for the final EOS");
            try {
                Thread.sleep(2000);
            }
            catch (Exception e) { e.printStackTrace(); }
        }

        System.out.println("* Peer has registered and we have EOS");

        System.out.println("* CService is going online");
        cservice.runCService(config, protocol);

    }
    
    public void sendIdent() {
        Map<String, ServerNode> serverList = protocol.getServerList();

        this.write(":" + config.getServerId() + " " + "PASS" + " :" + config.getLinkPassword());
        this.write(":" + config.getServerId() + " " + "PROTOCTL NICKv2 VHP UMODE2 NICKIP SJOIN SJOIN2 SJ3 NOQUIT TKLEXT MLOCK SID MTAGS");
        // PROTOCTL EAUTH=my.server.name[,protocolversion[,versionflags,fullversiontext]]
        this.write(":" + config.getServerId() + " " + "PROTOCTL EAUTH=" + config.getServerName() + "," + config.getSrvProtocolVersion() + "," + config.getSrvVersionFlags() + "," + config.getSrvFullVersionText());
        //this.write(":" + config.getServerId() + " " + "PROTOCTL EAUTH=" + config.getEAUTH());
        this.write(":" + config.getServerId() + " " + "PROTOCTL SID=" + config.getServerId());
        this.write(":" + config.getServerId() + " " + "SERVER" + " " + config.getServerName() + " 1 :" + config.getServerDescription());

        ServerNode server = new ServerNode(config.getServerName(), "0", config.getServerId(), config.getServerDescription());
        server.setEOS(false);
        server.setServerPeerResponded(false);
        serverList.put(config.getServerId(), server);
    }

    public void write(String str) {
		try {
            if (config.getLogging("debugOut") == true) { System.out.println(">>> " + str); }

            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			out.write(str + "\n");
			out.flush();
		}
        catch (IOException e) { e.printStackTrace(); }
    }
    
    public Socket getSocket() {
        return this.clientSocket;
    }
    
    public boolean getReady() {
        return this.clientReady;
    }
    
    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
