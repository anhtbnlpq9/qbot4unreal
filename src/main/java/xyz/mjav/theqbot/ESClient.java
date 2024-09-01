package xyz.mjav.theqbot;

import java.io.Reader;
import java.io.StringReader;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;


/**
 * Singleton class to manage a Sqlite database.
 */
public class ESClient implements Runnable {

    private static Logger log = LogManager.getLogger("common-log");

    private static ESClient instance = null;

    private static Config config = null;

    private String serverUrl;
    private String apiKey;
    private String index;

    private boolean esEnabled      = false;
    private boolean connectionReady = false;

    private RestClient restClient;
    private ElasticsearchTransport transport;
    private ElasticsearchAsyncClient esClient;

    private ESClient() {
        // URL and API key
        serverUrl = config.getLoggingElasticUri();
        apiKey    = config.getLoggingElasticApiKey();
        index     = config.getLoggingElasticIndex();
        esEnabled = config.getLoggingElasticEnabled();
    }

    /**
     * Static method to create the singleton
     */
    public static synchronized ESClient getInstance(Config config) {

        ESClient.config = config;

        if (instance == null) {
            instance = new ESClient();
            return instance;
        }
        else return instance;
    }

    public static synchronized ESClient getInstance() throws InstantiationException {
        if (instance == null) {
            throw new InstantiationException("Class is not ready");
        }
        else return instance;
    }

    @Override public void run() {

        if (this.esEnabled == false) { this.connectionReady = true; return; }

        log.info(String.format("ESClient::run: Starting Elasticsearch client"));

        // Create the low-level client
        restClient = RestClient
            .builder(HttpHost.create(serverUrl))
            .setDefaultHeaders(new Header[]{
                new BasicHeader("Authorization", "ApiKey " + apiKey)
            })
            .build();

        // Create the transport with a Jackson mapper
        transport = new RestClientTransport(
            restClient, new JacksonJsonpMapper());

        // And create the API client
        esClient = new ElasticsearchAsyncClient(transport);

        this.connectionReady = true;

    }

    public boolean isReady() {
        return this.connectionReady;
    }

    public void index(String s) {

        if (this.esEnabled == false) return;

        Reader json = new StringReader(s);

        try {
            esClient.index(i -> i.index(index).withJson(json)).whenComplete((response, exception) -> {
                if (exception != null) {
                    log.error("ESClient::index: Failed to index", exception);
                }
            });

        }
        catch (Exception e) {
            log.error(String.format("ESClient::index: could not index the string", e));
            return;
        }

    }

    public boolean isEsEnabled() {
        return this.esEnabled;
    }


}
