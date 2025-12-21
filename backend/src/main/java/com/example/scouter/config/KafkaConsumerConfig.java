package com.example.scouter.config;

import com.example.scouter.domain.model.PredictionResponse;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.lang.NonNull; // 追加

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumerの設定クラス。
 * PythonからのJSONメッセージを、JavaのPredictionResponse型に正しくデシリアライズする設定を行う。
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    @NonNull // 戻り値がNullでないことを明示
    public ConsumerFactory<String, PredictionResponse> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // JsonDeserializerの構築
        JsonDeserializer<PredictionResponse> jsonDeserializer = new JsonDeserializer<>(PredictionResponse.class);
        
        // Python連携のための重要な設定
        jsonDeserializer.setUseTypeHeaders(false); 
        jsonDeserializer.addTrustedPackages("*"); 

        return new DefaultKafkaConsumerFactory<>(
                props, 
                new StringDeserializer(), 
                jsonDeserializer
        );
    }

    @Bean
    @NonNull
    public ConcurrentKafkaListenerContainerFactory<String, PredictionResponse> kafkaListenerContainerFactory(
            @NonNull ConsumerFactory<String, PredictionResponse> consumerFactory) { // 引数で注入する
        
        ConcurrentKafkaListenerContainerFactory<String, PredictionResponse> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}