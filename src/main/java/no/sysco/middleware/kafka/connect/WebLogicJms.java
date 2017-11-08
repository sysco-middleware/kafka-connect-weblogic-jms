package no.sysco.middleware.kafka.connect;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.util.Optional;

/**
 * Utility class to manage JMS Sessions.
 */
final class WebLogicJms {

    /**
     * Open and start a session.
     *
     * @param connectionFactory JMS Connection Factory.
     * @param credentials WebLogic Credentials.
     * @param destination JMS Destination, Queue or Topic.
     * @param acknowledgmentMode JMS Acknowledgment Mode.
     * @return a JMS Session
     * @throws JMSException if an error with the JMS Connection occur.
     */
    static WebLogicJmsSession openSession(ConnectionFactory connectionFactory,
                                          Credentials credentials,
                                          Destination destination,
                                          WebLogicJmsSession.AcknowledgmentMode acknowledgmentMode)
        throws JMSException {
        final Connection connection = connectionFactory.createConnection(credentials.username, credentials.password);
        connection.start();
        final Session session = connection.createSession(false, acknowledgmentMode.ack);
        final javax.jms.Destination jmsDestination = destination.build(session);
        return new WebLogicJmsSession(connection, session, jmsDestination);
    }

    /**
     * JMS Destination
     */
    abstract static class Destination {
        final String name;
        final Type type;

        Destination(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        abstract javax.jms.Destination build(Session session) throws JMSException;

        enum Type {
            QUEUE, TOPIC
        }
    }

    /**
     * JMS Credentials.
     */
    static class Credentials {
        private final String username;
        private final String password;

        Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    /**
     * JMS Queue destination.
     */
    static class Queue extends Destination {

        Queue(String name) {
            super(name, Type.QUEUE);
        }

        @Override
        javax.jms.Destination build(Session session) throws JMSException {
            return session.createQueue(name);
        }
    }

    /**
     * JMS Topic destination.
     */
    static class Topic extends Destination {

        Topic(String name) {
            super(name, Type.TOPIC);
        }

        @Override
        javax.jms.Destination build(Session session) throws JMSException {
            return session.createTopic(name);
        }
    }

    /**
     * JMS Session.
     */
    static class WebLogicJmsSession {
        final javax.jms.Destination destination;
        private final Connection connection;
        private final Session session;

        /**
         * Instantiate a JMS Session Manager.
         *
         * @param connection JMS Connection.
         * @param session JMS Session.
         * @param destination JMS Destination.
         */
        WebLogicJmsSession(Connection connection, Session session, javax.jms.Destination destination) {
            this.connection = connection;
            this.session = session;
            this.destination = destination;
        }

        void closeSession() {
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }

        MessageProducer createProducer() throws JMSException {
            return session.createProducer(destination);
        }

        MessageConsumer createConsumer(String selector) throws JMSException {
            if (Optional.ofNullable(selector).isPresent()) {
                return session.createConsumer(destination, selector);
            } else {
                return session.createConsumer(destination);
            }
        }

        Session session() {
            return session;
        }

        /**
         * Valid JMS Acknowledgment Mode.
         */
        enum AcknowledgmentMode {
            AUTO_ACKNOWLEDGE(1),
            CLIENT_ACKNOWLEDGE(2),
            DUPS_OK_ACKNOWLEDGE(3),
            SESSION_TRANSACTED(0);

            private final int ack;

            AcknowledgmentMode(int ack) {
                this.ack = ack;
            }
        }
    }
}
