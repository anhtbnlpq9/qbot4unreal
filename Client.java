import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.lang.String;
import java.lang.Thread;
import java.lang.Exception;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;

import java.net.*;
import javax.net.*;
import java.io.*;
import javax.net.ssl.*;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.Certificate;
import javax.security.cert.X509Certificate;
import java.math.BigInteger;



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

            /*System.out.println("The Certificates used by peer");
            for (int i = 0; i < cchain.length; i++) {
                System.out.println((cchain[i]).getEncoded());
            }
            System.out.println("Peer host is " + session.getPeerHost());
            System.out.println("Cipher is " + session.getCipherSuite());
            System.out.println("Protocol is " + session.getProtocol());
            System.out.println("ID is " + new BigInteger(session.getId()));
            System.out.println("Session created in " + session.getCreationTime());
            System.out.println("Session accessed in "
                    + session.getLastAccessedTime());*/

            System.out.println("* Connected");

            Protocol protocol = new Protocol();
            protocol.setClientRef(this);

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          
            System.out.println("* Client is ready");
            this.clientReady = true;


            String str;
            while ((str = in.readLine()) != null) {
                System.out.println("<<< " + str);
                protocol.getResponse(str);
            }

            /*boolean isChatOver = false;
            while(!isChatOver) {
                if(in.ready()) {
                    Thread.sleep(5000);
                }
                String nextChat = in.readLine();
                System.out.println("server says : " + nextChat);
                if("bye".equalsIgnoreCase(nextChat.trim())) {
                    System.out.println("**************************************Closing Session.*********************************************");
                    isChatOver = true;
                }
            }*/

        }
        catch (Exception e) { e.printStackTrace(); }
    }
    public void write(String str) {
		try {
            System.out.println(">>> " + str);
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
