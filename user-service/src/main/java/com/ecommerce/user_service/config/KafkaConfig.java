package com.ecommerce.user_service.config;

import com.ecommerce.user_service.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean public NewTopic userRegistered()      { return TopicBuilder.name(KafkaTopics.USER_REGISTERED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic userUpdated()         { return TopicBuilder.name(KafkaTopics.USER_UPDATED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic userDeleted()         { return TopicBuilder.name(KafkaTopics.USER_DELETED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic userStatusChanged()   { return TopicBuilder.name(KafkaTopics.USER_STATUS_CHANGED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic userPasswordChanged() { return TopicBuilder.name(KafkaTopics.USER_PASSWORD_CHANGED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic userLoginSuccess()    { return TopicBuilder.name(KafkaTopics.USER_LOGIN_SUCCESS).partitions(3).replicas(1).build(); }
    @Bean public NewTopic userLoginFailed()     { return TopicBuilder.name(KafkaTopics.USER_LOGIN_FAILED).partitions(3).replicas(1).build(); }
    @Bean public NewTopic userRoleAssigned()    { return TopicBuilder.name(KafkaTopics.USER_ROLE_ASSIGNED).partitions(3).replicas(1).build(); }
}