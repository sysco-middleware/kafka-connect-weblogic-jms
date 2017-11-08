package no.sysco.middleware.kafka.connect;

import weblogic.jms.client.JMSConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Utility interface to create a JMS for a JMS Task.
 */
public interface WebLogicJmsTask extends WebLogicJmsConnector {

    String WEBLOGIC_JNDI_INITIAL_CONTEXT_FACTORY = "weblogic.jndi.WLInitialContextFactory";

    /**
     * Build a new JMS Session.
     *
     * @param config Configuration properties.
     * @return a WebLogic JMS Session
     */
    default WebLogicJms.WebLogicJmsSession buildSession(Map<String, String> config) {
        final String jmsServer = config.get(WEBLOGIC_JMS_SERVER_CONFIG);
        final String jmsModule = config.get(WEBLOGIC_JMS_MODULE_CONFIG);
        final String jmsDestination = config.get(WEBLOGIC_JMS_DESTINATION_CONFIG);
        final String jmsDestinationType = config.get(WEBLOGIC_JMS_DESTINATION_TYPE_CONFIG);
        final String acknowledgeMode = config.get(WEBLOGIC_JMS_ACKNOWLEDGE_MODE_CONFIG);

        final WebLogicJms.Destination.Type destinationType = WebLogicJms.Destination.Type.valueOf(jmsDestinationType);

        final String destinationName = String.format("%s/%s!%s", jmsServer, jmsModule, jmsDestination);

        final WebLogicJms.Destination destination = getDestination(destinationType, destinationName);

        final String weblogicJmsConnectionFactory = config.get(WEBLOGIC_JMS_CONNECTION_FACTORY_CONFIG);
        final String weblogicT3Url = config.get(WEBLOGIC_T3_URL_DESTINATION_CONFIG);
        final String weblogicUsername = config.get(WEBLOGIC_USERNAME_CONFIG);
        final String weblogicPassword = config.get(WEBLOGIC_PASSWORD_CONFIG);

        final Hashtable<String, String> properties = new Hashtable<>();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, WEBLOGIC_JNDI_INITIAL_CONTEXT_FACTORY);
        properties.put(Context.PROVIDER_URL, weblogicT3Url);
        properties.put(Context.SECURITY_PRINCIPAL, weblogicUsername);
        properties.put(Context.SECURITY_CREDENTIALS, weblogicPassword);

        final ConnectionFactory connectionFactory =
            getConnectionFactory(weblogicJmsConnectionFactory, properties);

        final WebLogicJms.Credentials credentials =
            new WebLogicJms.Credentials(weblogicUsername, weblogicPassword);
        final WebLogicJms.WebLogicJmsSession.AcknowledgmentMode acknowledgmentMode =
            WebLogicJms.WebLogicJmsSession.AcknowledgmentMode.valueOf(acknowledgeMode);

        try {
            return WebLogicJms.openSession(connectionFactory, credentials, destination, acknowledgmentMode);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    default ConnectionFactory getConnectionFactory(String weblogicJmsConnectionFactory,
                                                   Hashtable<String, String> properties) {
        ConnectionFactory connectionFactory = null;
        try {
            final InitialContext ctx = new InitialContext(properties);
            connectionFactory = (JMSConnectionFactory) ctx.lookup(weblogicJmsConnectionFactory);
        } catch (NamingException ne) {
            ne.printStackTrace(System.err);
        }
        return connectionFactory;
    }

    default WebLogicJms.Destination getDestination(WebLogicJms.Destination.Type destinationType,
                                                   String destinationName) {
        WebLogicJms.Destination destination = null;
        switch (destinationType) {
            case QUEUE:
                destination = new WebLogicJms.Queue(destinationName);
                break;
            case TOPIC:
                destination = new WebLogicJms.Topic(destinationName);
                break;
        }
        return destination;
    }
}
