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
import org.springframework.lang.NonNull; // 追加

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
    @NonNull
    public ProducerFactory<String, KafkaScoreRequest> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // JsonSerializerのインスタンスを作成し、詳細設定を行う
        JsonSerializer<KafkaScoreRequest> jsonSerializer = new JsonSerializer<>();
        
        // Python側でデシリアライズしやすくするため、Java固有の型情報ヘッダーを付与しない設定
        // これにより、純粋なJSONとして送信される
        jsonSerializer.setAddTypeInfo(false);

        return new DefaultKafkaProducerFactory<>(
            configProps,
            new StringSerializer(), 
            jsonSerializer
        );
    }

    @Bean
    @NonNull
    public KafkaTemplate<String, KafkaScoreRequest> kafkaTemplate(
            @NonNull ProducerFactory<String, KafkaScoreRequest> producerFactory) { // 引数で注入
        
        return new KafkaTemplate<>(producerFactory);
    }
}