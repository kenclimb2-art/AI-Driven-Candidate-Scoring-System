package com.example.scouter.config;

import com.example.scouter.domain.model.KafkaScoreRequest;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producerの設定クラス。
 * PythonのAIエンジンへスコア要求メッセージを送信するためのKafkaTemplateを定義する。
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, KafkaScoreRequest> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Serializerクラスの設定は、DefaultKafkaProducerFactoryのコンストラクタで直接インスタンスを渡すため、propsから削除します。
        // configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); 
        // configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // JsonSerializerの追加設定も直接インスタンスに適用します。
        // configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false); 
        
        // Producer FactoryにKey/Value Serializerを直接渡すことで、propsとの競合を回避します。
        return new DefaultKafkaProducerFactory<>(
            configProps, // configPropsにはBOOTSTRAP_SERVERSのみを残す
            new StringSerializer(), 
            new JsonSerializer<>()
        );
    }

    @Bean
    public KafkaTemplate<String, KafkaScoreRequest> kafkaTemplate() {
        // Springコンテナに KafkaTemplate のBeanを登録する
        return new KafkaTemplate<>(producerFactory());
    }
}