
public class Qbot {

    private static final String CONFIG_FILE = "./conf/config.yml";

    public static void main(String[] args) {
        System.out.println("+----------------------------------------------------------------+\n"
+ "|                                                                |\n"
+ "|                      _..---...,\"\"-._     ,/}/)                 |\n"
+ "|                   .''        ,      ``..'(/-<                  |\n"
+ "|                  /   _      {      )         \\                 |\n"
+ "|                 ;   _ `.     `.   <         a(                 |\n"
+ "|               ,'   ( \\  )      `.  \\ __.._ .: y                |\n"
+ "|              (  <\\_-) )'-.____...\\  `._   //-'                 |\n"
+ "|               `. `-' /-._)))      `-._)))                      |\n"
+ "|                 `...'                                          |\n"
+ "|               _       _   ___                     _            |\n"
+ "|           ___| |_ ___| |_| | |_ _ ___ ___ ___ ___| |           |\n"
+ "|          | . | . | . |  _|_  | | |   |  _| -_| .'| |           |\n"
+ "|          |_  |___|___|_|   |_|___|_|_|_| |___|__,|_|           |\n"
+ "|            |_|                                                 |\n"
+ "|                                                                |\n"
+ "| ___        __    __         _                        __  __    |\n"
+ "|  | |_  _  /  \\  |__) _ |_  (_ _  _  /  \\ _  _ _ _ |||__)/   _| |\n"
+ "|  | | )(-  \\_\\/  |__)(_)|_  | (_)|   \\__/| )| (-(_|||| \\ \\__(_| |\n"
+ "|                                                                |\n"
+ "+----------------------------------------------------------------+\n"
                );

        System.out.println("* Loading conf");
        Config config = new Config(CONFIG_FILE);

        System.out.println("* Opening database");
        SqliteDb sqliteDb = new SqliteDb(config);
        sqliteDb.getRegChan();

        System.out.println("* Starting client socket");
        Client tlsClient = new Client(config, sqliteDb);
        Thread thread = new Thread(tlsClient);
        thread.start();

        tlsClient.setThread(thread);

        while (tlsClient.getReady() == false) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) { e.printStackTrace(); }
        }

        System.out.println("* Sending server ident");
        tlsClient.sendIdent();

        //try { Thread.sleep(5000); } catch (Exception e) { e.printStackTrace(); }
        System.out.println("* Starting CService");
        tlsClient.launchCService(); 
    }    
}
