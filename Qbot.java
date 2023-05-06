
public class Qbot {

    public static void main(String[] args) {
        System.out.println(   "            _..---...,\"\"-._     ,/}/)      \n"
                            + "         .''        ,      ``..'(/-<       \n"
                            + "        /   _      {      )         \\      \n"
                            + "       ;   _ `.     `.   <         a(      \n"
                            + "     ,'   ( \\  )      `.  \\ __.._ .: y     \n"
                            + "    (  <\\_-) )'-.____...\\  `._   //-'      \n"
                            + "     `. `-' /-._)))      `-._)))           \n"
                            + "       `...'                               \n"
                            + "     _       _   ___                     _\n"
                            + " ___| |_ ___| |_| | |_ _ ___ ___ ___ ___| |\n"
                            + "| . | . | . |  _|_  | | |   |  _| -_| .'| |\n"
                            + "|_  |___|___|_|   |_|___|_|_|_| |___|__,|_|\n"
                            + "  |_|                                      \n"
                            + "                  ~~~                      \n"
                );

        System.out.println("* Loading conf");
        Config config = new Config("./config.yml");

        System.out.println("* Starting client socket");
        Client tlsClient = new Client(config);
        Thread thread = new Thread(tlsClient);
        thread.start();

        while (tlsClient.getReady() == false) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) { e.printStackTrace(); }
        }

        
        tlsClient.sendIdent();
        System.out.println("* Sent server ident");
        //try { Thread.sleep(5000); } catch (Exception e) { e.printStackTrace(); }
        tlsClient.launchCService(); 
        System.out.println("* Started CService");
    }    
}
