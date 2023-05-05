import java.lang.String;
import java.lang.Exception;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;

import java.util.ArrayList;

public class Protocol extends Exception {
    
    private Client client;
    
    public void Protocol() {
        
    }
    
    public void setClientRef(Client client) {
        this.client = client;
    }
    
    public void write(Client client, String str) /*throws Exception*/ {
        client.write(str);
    }

  public void getResponse(String raw) throws Exception {

    String response = "";
    String[] command = raw.split(" ", 2); // Begin to split raw message to fetch the command (first non-spaced-word)

    // Check for IRCv3 string presence
    if (command[0].startsWith("@")) {
        command = raw.split(" ", 3);
        if (command[1].startsWith(":")) {
            String origUser = (command[1].split(":"))[1];
            command = raw.split(" ", 4);
            
            if (command[2].equals("PRIVMSG")) {
                String destUser = (command[3].split(":", 2))[0];
                if (destUser.startsWith("5PC")) {
                    response = ":" + destUser + " NOTICE " + origUser + " :kikoo";
                    write(client, response);
                    write(client, response);
                    write(client, response);
                    write(client, response);
                    write(client, response);
                    write(client, response);
                    write(client, response);
                    write(client, response);
                    write(client, response);
                    write(client, response);
                }
            }
        }

    }
    else {
        if (command[0].equals("PING")) {
            response = "PONG " + command[1];
            write(client, response);
        }
    }


  }
}
