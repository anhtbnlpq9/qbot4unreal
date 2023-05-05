import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.lang.String;
import java.lang.Thread;
import java.lang.Exception;
import java.math.BigInteger;

import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.security.cert.Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import javax.security.cert.X509Certificate;

public class Client implements Runnable {
    
    public Config config;
    
    public Socket clientSocket;
    private BufferedReader in;
    public BufferedWriter out;
    private boolean clientReady = false;
 

    public Client(Config config) {
        this.config = config;
        System.setProperty("javax.net.ssl.trustStore", "/home/thib/.keystore");
        System.setProperty("javax.net.ssl.trustStorePassword","123456");

        System.setProperty("javax.net.ssl.keyStore", "/home/thib/.keystore");
        System.setProperty("javax.net.ssl.keyStorePassword","123456");
        
    }

    public Client getClientRef() {
        return this;
    }

    public void run() {
        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            clientSocket = ssf.createSocket(config.getLinkPeerHost(), config.getLinkPeerPort());
            
            SSLSession session = ((SSLSocket) clientSocket).getSession();
            Certificate[] cchain = session.getPeerCertificates();

            System.out.println("* Connected");

            Protocol protocol = new Protocol(config);
            protocol.setClientRef(this);

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          
            System.out.println("* Client is ready");
            this.clientReady = true;

            String str;
            while ((str = in.readLine()) != null) {
                //System.out.println("<<< " + str);
                protocol.getResponse(str);
            }

        }
        catch (Exception e) { e.printStackTrace(); }
    }
    public void write(String str) {
		try {
            //System.out.println(">>> " + str);
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
}
