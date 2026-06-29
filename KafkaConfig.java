package com.ticketbooking.saga;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic reservationReservedTopic() {
        return TopicBuilder.name(SagaTopics.RESERVATION_RESERVED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCapturedTopic() {
        return TopicBuilder.name(SagaTopics.PAYMENT_CAPTURED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(SagaTopics.PAYMENT_FAILED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic ticketGenerationFailedTopic() {
        return TopicBuilder.name(SagaTopics.TICKET_GENERATION_FAILED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic seatReleaseRequestedTopic() {
        return TopicBuilder.name(SagaTopics.SEAT_RELEASE_REQUESTED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentReversalRequestedTopic() {
        return TopicBuilder.name(SagaTopics.PAYMENT_REVERSAL_REQUESTED)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
