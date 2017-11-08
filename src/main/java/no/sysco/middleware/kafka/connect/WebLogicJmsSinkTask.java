package no.sysco.middleware.kafka.connect;

import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import java.util.Collection;
import java.util.Map;

/**
 * WebLogic JMS Sink Task implementation.
 */
public class WebLogicJmsSinkTask extends SinkTask implements WebLogicJmsTask {

    private WebLogicJms.WebLogicJmsSession webLogicJmsSession;
    private MessageProducer messageProducer;

    public String version() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> props) {
        try {
            webLogicJmsSession = buildSession(props);
            messageProducer = webLogicJmsSession.createProducer();
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
            final String payload = sinkRecord.value().toString();
            final TextMessage textMessage = webLogicJmsSession.session().createTextMessage(payload);
            textMessage.setStringProperty("kafka-topic", sinkRecord.topic());
            textMessage.setIntProperty("kafka-partition", sinkRecord.kafkaPartition());
            textMessage.setLongProperty("kafka-offset", sinkRecord.kafkaOffset());
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
