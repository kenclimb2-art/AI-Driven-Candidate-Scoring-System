package com.example.scouter.config;

import com.example.scouter.domain.model.PredictionResponse; // ★追加
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
    
    // ConsumerFactoryを定義し、JsonDeserializerをカスタマイズする
    // ★修正箇所: 型パラメータを <String, PredictionResponse> に変更
    @Bean
    public ConsumerFactory<String, PredictionResponse> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // --- JsonDeserializerのカスタマイズ ---
        
        // 1. TypeReferenceは不要となり、直接クラスを指定する
        // TypeReference<List<PredictionData>> typeRef = new TypeReference<List<PredictionData>>() {}; // 削除
        
        // 2. Classを引数に取るコンストラクタで JsonDeserializer を初期化
        // ★修正箇所: クラスを PredictionResponse.class に変更
        JsonDeserializer<PredictionResponse> jsonDeserializer = new JsonDeserializer<>(PredictionResponse.class);
        
        // 3. Pythonは型情報ヘッダーを付けないので、チェックを無効化
        jsonDeserializer.setUseTypeHeaders(false); 
        jsonDeserializer.addTrustedPackages("*"); 
        
        // ★修正箇所: 型パラメータを PredictionResponse に変更
        return new DefaultKafkaConsumerFactory<>(
                props, 
                new StringDeserializer(), 
                jsonDeserializer
        );
    }
    
    // KafkaListenerが動作するために必要なContainerFactoryを定義する
    // ★修正箇所: 型パラメータを PredictionResponse に変更
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PredictionResponse> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PredictionResponse> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}