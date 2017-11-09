package no.sysco.middleware.kafka.connect;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import java.util.Collection;
import java.util.Map;

/**
 * WebLogic JMS Sink Task implementation.
 */
public class WebLogicJmsSinkTask extends SinkTask implements WebLogicJmsTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogicJmsSourceTask.class);

    private WebLogicJms.WebLogicJmsSession webLogicJmsSession;
    private MessageProducer messageProducer;

    public String version() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> props) {
        try {
            webLogicJmsSession = buildSession(props);
            messageProducer = webLogicJmsSession.createProducer();

            LOGGER.info("Starting Kafka Connector - WebLogic JMS Sink");
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(Collection<SinkRecord> records) {
        records.stream()
            .map(this::getTextMessage)
            .forEach(this::processTextMessage);
    }

    private void processTextMessage(TextMessage textMessage) {
        try {
            messageProducer.send(textMessage);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private TextMessage getTextMessage(SinkRecord sinkRecord) {
        try {
            LOGGER.info("Processing Record: key={} value={}", sinkRecord.key(), sinkRecord.value());
            final String payload = sinkRecord.value().toString();
            final TextMessage textMessage = webLogicJmsSession.session().createTextMessage(payload);
            textMessage.setStringProperty("KafkaTopic", sinkRecord.topic());
            textMessage.setIntProperty("KafkaPartition", sinkRecord.kafkaPartition());
            textMessage.setLongProperty("KafkaOffset", sinkRecord.kafkaOffset());
            Object key = sinkRecord.key();
            try {
                Schema keySchema = sinkRecord.keySchema();
                Struct keyStruct = (Struct) key;
                //Struct jmsStruct = keyStruct.getStruct("jms");
                Struct jmsProperties = keyStruct.getStruct("properties");
                keySchema
                    .field("properties")
                    .schema()
                    .fields()
                    .forEach(field -> {
                        String value = jmsProperties.getString(field.name());
                        try {
                            textMessage.setStringProperty(field.name(), value);
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                    });
                Struct jmsHeaders = keyStruct.getStruct("headers");
                textMessage.setJMSCorrelationID(jmsHeaders.getString("JMSCorrelationID"));
                final String jmsReplyTo = jmsHeaders.getString("JMSReplyTo");
                if (jmsReplyTo != null)
                    textMessage.setJMSReplyTo(webLogicJmsSession.session().createQueue(jmsReplyTo));
                textMessage.setJMSCorrelationID(jmsHeaders.getString("JMSCorrelationID"));
                textMessage.setJMSType(jmsHeaders.getString("JMSType"));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return textMessage;
        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        webLogicJmsSession.closeSession();
    }
}
