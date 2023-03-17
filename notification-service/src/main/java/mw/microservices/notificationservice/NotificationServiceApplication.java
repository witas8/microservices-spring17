package mw.microservices.notificationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
@Slf4j
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    //the same topic name as defined in producer service (order service)
    @KafkaListener(topics = "notificationTopic")
    public void handleNotification(OrderPlacedEvent orderPlacedEvent) {
        // here implement an action of sending out an email with a notification
        log.info("Received Notification for Order - {}", orderPlacedEvent.getOrderNumber());
    }
}