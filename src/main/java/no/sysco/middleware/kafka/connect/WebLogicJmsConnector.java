package no.sysco.middleware.kafka.connect;

/**
 * Utility interface to manage JMS Connector properties and documentation.
 */
public interface WebLogicJmsConnector {
    String CONNECT_WEBLOGIC_JMS_PREFIX = "weblogic.jms.";
    String WEBLOGIC_T3_URL_DESTINATION_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "url";
    String WEBLOGIC_T3_URL_DESTINATION_DOC = "Provides WebLogic T3 url";
    String WEBLOGIC_USERNAME_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "username";
    String WEBLOGIC_USERNAME_DOC = "Provides weblogic username";
    String WEBLOGIC_PASSWORD_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "password";
    String WEBLOGIC_PASSWORD_DOC = "Provides weblogic password";
    String WEBLOGIC_JMS_CONNECTION_FACTORY_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "connection.factory";
    String WEBLOGIC_JMS_CONNECTION_FACTORY_DOC = "Provides the JMS Connection Factory name";
    String WEBLOGIC_JMS_DESTINATION_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "destination";
    String WEBLOGIC_JMS_DESTINATION_DOC = "Provides the JMS Queue name";
    String WEBLOGIC_JMS_DESTINATION_TYPE_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "destination.type";
    String WEBLOGIC_JMS_DESTINATION_TYPE_DOC = "Provides the JMS Topic name";
    String WEBLOGIC_JMS_DESTINATION_TYPE_DEFAULT = WebLogicJms.Destination.Type.QUEUE.name();
    String WEBLOGIC_JMS_SERVER_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "server";
    String WEBLOGIC_JMS_SERVER_DOC = "Provides the JMS Server name";
    String WEBLOGIC_JMS_MODULE_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "module";
    String WEBLOGIC_JMS_MODULE_DOC = "Provides the JMS Module name";
    String WEBLOGIC_JMS_ACKNOWLEDGE_MODE_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "acknowledge.mode";
    String WEBLOGIC_JMS_ACKNOWLEDGE_MODE_DOC = "Provides the JMS Acknowledge Mode";
    String WEBLOGIC_JMS_ACKNOWLEDGE_MODE_DEFAULT = WebLogicJms.WebLogicJmsSession.AcknowledgeMode.AUTO_ACKNOWLEDGE.name();
    String WEBLOGIC_JMS_SELECTOR_CONFIG = CONNECT_WEBLOGIC_JMS_PREFIX + "selector";
    String WEBLOGIC_JMS_SELECTOR_DOC = "Provides the JMS Selector filter";
    String WEBLOGIC_JMS_SELECTOR_DEFAULT = null;
    String KAFKA_TOPIC_PREFIX_CONFIG = "topic.prefix";
    String KAFKA_TOPIC_PREFIX_DOC = "Provides the prefix for Kafka Topic name";
    String KAFKA_TOPIC_PREFIX_DEFAULT = "";
}
