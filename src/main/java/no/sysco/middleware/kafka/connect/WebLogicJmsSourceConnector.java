package no.sysco.middleware.kafka.connect;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class WebLogicJmsSourceConnector extends SourceConnector {
    private final ConfigDef configDef =
        WebLogicJmsConfig.config
            .define(
                WebLogicJmsConnector.WEBLOGIC_JMS_SELECTOR_CONFIG,
                ConfigDef.Type.STRING,
                WebLogicJmsConnector.WEBLOGIC_JMS_SELECTOR_DEFAULT,
                ConfigDef.Importance.LOW,
                WebLogicJmsConnector.WEBLOGIC_JMS_SELECTOR_DOC)
            .define(
                WebLogicJmsConnector.KAFKA_TOPIC_PREFIX_CONFIG,
                ConfigDef.Type.STRING,
                WebLogicJmsConnector.KAFKA_TOPIC_PREFIX_DEFAULT,
                ConfigDef.Importance.MEDIUM,
                WebLogicJmsConnector.KAFKA_TOPIC_PREFIX_DOC);

    private Map<String, String> configProps;

    public String version() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> props) {
        configProps = props;
    }

    public Class<? extends Task> taskClass() {
        return WebLogicJmsSourceTask.class;
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
