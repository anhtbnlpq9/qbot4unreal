package xyz.mjav.theqbot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ESLog {

    /**
     *
     * {
     *   "@timestamp": "2024-04-30T14:41:40.452Z",
     *   "ecs.version": "1.2.0",
     *   "log.level": "DEBUG",
     *   "message": {
     *       "account": "AnhTay",
     *       "args": "[#mjav]",
     *       "authed": "true",
     *       "command": "OP",
     *       "from": "AnhTayLocal",
     *       "qmsgid": "5ZxrTovt95JgWEv8lAFwlQm8K8kuauKX"
     *   },
     *   "process.thread.name": "ForkJoinPool.commonPool-worker-6",
     *   "log.logger": "common-json-log"
     * }
     *
     */

    private ObjectMapper mapper;
    private ObjectNode node;
    private String stamp;

    public static class Builder {

        private String logLevel = "INFO";
        private String type = "unknown";
        private String qmsgid = "";
        private String clientid = "";
        private String direction = "in";
        private Map<String, String> logMap = new TreeMap<>();


        public Builder logLevel(String v) {
            this.logLevel = v;
            return this;
        }

        public Builder type(String v) {
            this.type = v;
            return this;
        }

        public Builder logMap(Map<String, String> v) {
            this.logMap = new TreeMap<>(v);
            return this;
        }

        public Builder qmsgid(String v) {
            this.qmsgid = v;
            return this;
        }

        public Builder clientid(String v) {
            this.clientid = v;
            return this;
        }

        public Builder direction(String v) {
            this.direction = v;
            return this;
        }

        public ESLog build() {
            return new ESLog(this);
        }
    }

    private ESLog(Builder b) {

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        stamp = sdf.format(date);

        mapper = new ObjectMapper();
        node = mapper.createObjectNode();

        ObjectNode message = mapper.valueToTree(b.logMap);

        node.put("@timestamp", stamp);
        node.put("ecs.version", "1.2.0");
        node.put("log.level", b.logLevel);
        node.put("type", b.type);
        node.put("qmsgid", b.qmsgid);
        node.put("clientid", b.clientid);
        node.put("direction", b.direction);

        node.set("message", message);

    }

    public String toString() {
        try { return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node); }
        catch (Exception e) { return "{}"; }
    }

}
