package no.sysco.middleware.kafka.connect;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * WebLogic JMS Source Task implementation.
 */
public class WebLogicJmsSourceTask extends SourceTask implements WebLogicJmsTask {
    private String kafkaTopic;

    private WebLogicJms.WebLogicJmsSession webLogicJmsSession;
    private MessageConsumer messageConsumer;
    private String destinationName;

    public String version() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    public void start(Map<String, String> props) {
        webLogicJmsSession = buildSession(props);
        try {
            destinationName = props.get(WEBLOGIC_JMS_DESTINATION_CONFIG);

            final String weblogicJmsSelector = props.get(WEBLOGIC_JMS_SELECTOR_CONFIG);
            messageConsumer = webLogicJmsSession.createConsumer(weblogicJmsSelector);
        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException(e); //TODO fix
        }
        kafkaTopic = props.getOrDefault(KAFKA_TOPIC_CONFIG, props.get(WEBLOGIC_JMS_DESTINATION_CONFIG));
    }

    public List<SourceRecord> poll() throws InterruptedException {
        try {
            final Message message = messageConsumer.receive();
            final TextMessage textMessage = (TextMessage) message;
            final String text = textMessage.getText();

            final Map<String, Object> sourcePartition = Collections.singletonMap("destination", destinationName);
            final Map<String, Object> sourceOffset = Collections.emptyMap();
            final SourceRecord sourceRecord =
                new SourceRecord(sourcePartition, sourceOffset, kafkaTopic, Schema.STRING_SCHEMA, text);

            return Collections.singletonList(sourceRecord);
        } catch (JMSException e) {
            // Underlying stream was killed, probably as a result of calling stop. Allow to return
            // null, and driving thread will handle any shutdown if necessary.
        }
        return null;
    }

    public void stop() {
        webLogicJmsSession.closeSession();
    }

}
