//package org.yaml.snakeyaml;
import java.net.* ;
import java.io.* ;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.time.Instant;
//import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;

//import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
//import org.yaml.snakeyaml.constructor.Constructor;

public class Qbot {

    public static void main(String[] args) {
        
        
        System.out.println("* Loading config");
        //InputStream inputStream = null;
        Config config = new Config("./config.yml");
        
        /*Yaml yaml = new Yaml();

        Map<String, Object> data = yaml.load(inputStream);

        System.out.println(data);
        System.out.println(data.get("me"));
        LinkedHashMap me = (LinkedHashMap) data.get("me");
        System.out.println(me.get("name"));*/
        
       

	    //Properties properties = System.getProperties();
	    //properties.forEach((k, v) -> System.out.println(k + ":" + v)); // Java 8






        System.out.println("* Start client socket");
        Client tlsClient = new Client(config);
        Thread thread = new Thread(tlsClient);
        thread.start();

        while (tlsClient.getReady() == false) {
            try {
                //System.out.println("* Not ready");
                Thread.sleep(1000);
            }
            catch (Exception e) { e.printStackTrace(); }
        }
        long unixTime;
        System.out.println("* Sending initial");
        tlsClient.write(":" + config.getServerId() + " " + "PASS" + " :" + config.getLinkPassword());
        tlsClient.write(":" + config.getServerId() + " " + "PROTOCTL NICKv2 VHP UMODE2 NICKIP SJOIN SJOIN2 SJ3 NOQUIT TKLEXT MLOCK SID MTAGS");
        tlsClient.write(":" + config.getServerId() + " " + "PROTOCTL EAUTH=" + config.getEAUTH());
        tlsClient.write(":" + config.getServerId() + " " + "PROTOCTL SID=" + config.getServerId());
        tlsClient.write(":" + config.getServerId() + " " + "SERVER" + " " + config.getServerName() + " 1 :" + config.getServerDescription());

        tlsClient.write(":" + config.getServerId() + " " + "EOS");

        try { Thread.sleep(5000); } catch (Exception e) { e.printStackTrace(); }

        unixTime = Instant.now().getEpochSecond();
        tlsClient.write(":" + config.getServerId() + " " + "UID Q 1 " + Instant.now().getEpochSecond() + " theqbot CServe " + config.getServerId() + "AAAAAA * +BqioS * * * :The Q Bot");
        

        unixTime = Instant.now().getEpochSecond();
        tlsClient.write(":" + config.getServerId() + " " + "SJOIN " + Instant.now().getEpochSecond() + " #mjav + :@" + config.getServerId() + "AAAAAA");

//try { Thread.sleep(1000); } catch (Exception e) { e.printStackTrace(); }
        unixTime = Instant.now().getEpochSecond();
        tlsClient.write("MODE #mjav +o Q");


 
    }    
}
