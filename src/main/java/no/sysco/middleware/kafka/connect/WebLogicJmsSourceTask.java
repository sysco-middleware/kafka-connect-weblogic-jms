package no.sysco.middleware.kafka.connect;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * WebLogic JMS Source Task implementation.
 */
public class WebLogicJmsSourceTask extends SourceTask implements WebLogicJmsTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogicJmsSourceTask.class);

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
            throw new RuntimeException(e);
        }
        kafkaTopic = props.get(KAFKA_TOPIC_PREFIX_CONFIG) + props.get(WEBLOGIC_JMS_DESTINATION_CONFIG);

        LOGGER.info("Starting Kafka Connector - WebLogic JMS Source");
    }

    public List<SourceRecord> poll() throws InterruptedException {
        try {
            final Map<String, Object> sourcePartition = Collections.singletonMap("destination", destinationName);
            final Map<String, Object> sourceOffset = Collections.emptyMap();

            final Message message = messageConsumer.receive();
            final ArrayList<String> propertyNames = Collections.list(message.getPropertyNames());
            final SchemaBuilder propertiesSchemaBuilder = SchemaBuilder.struct();
            propertyNames.forEach(propertyName -> propertiesSchemaBuilder.field(propertyName, Schema.OPTIONAL_STRING_SCHEMA));
            final Schema propertiesSchema = propertiesSchemaBuilder.build();
            final Struct propertiesStruct = new Struct(propertiesSchema);
            propertyNames.forEach(propertyName -> {
                try {
                    final String stringProperty = message.getStringProperty(propertyName);
                    propertiesStruct.put(propertyName, stringProperty);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            });

            final SchemaBuilder headersBuilder = SchemaBuilder.struct();
            headersBuilder.field("JMSCorrelationID", Schema.OPTIONAL_STRING_SCHEMA);
            headersBuilder.field("JMSReplyTo", Schema.OPTIONAL_STRING_SCHEMA);
            headersBuilder.field("JMSType", Schema.OPTIONAL_STRING_SCHEMA);

            final Schema headersSchema = headersBuilder.build();
            final Struct headersStruct = new Struct(headersSchema);
            headersStruct.put("JMSCorrelationID", message.getJMSCorrelationID());
            headersStruct.put("JMSReplyTo", message.getJMSReplyTo());
            headersStruct.put("JMSType", message.getJMSType());

            final SchemaBuilder builder =
                SchemaBuilder.struct()
                    .name("jms")
                    .field("headers", headersSchema)
                    .field("properties", propertiesSchema);

            final Schema keySchema = builder.build();
            final Struct key = new Struct(keySchema);
            key.put("headers", headersStruct);
            key.put("properties", propertiesStruct);

            final TextMessage textMessage = (TextMessage) message;
            final String text = textMessage.getText();
            final SourceRecord sourceRecord =
                new SourceRecord(
                    sourcePartition,
                    sourceOffset,
                    kafkaTopic,
                    keySchema,
                    key,
                    Schema.STRING_SCHEMA,
                    text);

            LOGGER.info("Producing message: key={} value={}", key.toString(), context);

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
