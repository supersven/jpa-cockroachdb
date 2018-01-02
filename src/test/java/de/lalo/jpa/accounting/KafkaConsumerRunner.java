package de.lalo.jpa.accounting;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author llorenzen
 * @since 31.12.17
 */
public class KafkaConsumerRunner implements Runnable {
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final KafkaConsumer consumer;

    public KafkaConsumerRunner(KafkaConsumer consumer) {
        this.consumer = consumer;
    }

    public void run() {
        try {
            consumer.subscribe(Arrays.asList("transactionChanges", "transactionFailures"));
            while (!closed.get()) {
                ConsumerRecords<String, String> records = consumer.poll(10000);
                // Handle new records
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(record);
                }
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (!closed.get()) throw e;
        } finally {
            consumer.close();
        }
    }

    // Shutdown hook which can be called from a separate thread
    public void shutdown() {
        closed.set(true);
        consumer.wakeup();
    }
}