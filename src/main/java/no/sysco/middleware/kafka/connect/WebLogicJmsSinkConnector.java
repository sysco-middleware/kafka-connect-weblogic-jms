package no.sysco.middleware.kafka.connect;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WebLogic JMS Sink Connector implementation.
 */
public class WebLogicJmsSinkConnector extends SinkConnector implements WebLogicJmsConnector {
    private final ConfigDef configDef = WebLogicJmsConfig.config;

    private Map<String, String> configProps;

    public String version() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> props) {
        configProps = props;
    }

    public Class<? extends Task> taskClass() {
        return WebLogicJmsSinkTask.class;
    }

    public List<Map<String, String>> taskConfigs(int maxTasks) {
        final Map<String, String> taskConfigs = new HashMap<>();
        taskConfigs.putAll(configProps);

        final ArrayList<Map<String, String>> configs = new ArrayList<>();
        configs.add(taskConfigs);
        return configs;
    }

    public void stop() {
    }

    public ConfigDef config() {
        return configDef;
    }
}
