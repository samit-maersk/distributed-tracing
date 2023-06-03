package com.example.kafkaconsumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Hooks;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class KafkaConsumerApplication {

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(KafkaConsumerApplication.class, args);
	}
}

@Component
@Slf4j
@RequiredArgsConstructor
class KafkaConsumer {
	private final AuditLogRepository auditLogRepository;
	@KafkaListener(topics = "test")
	public void processMessage(@Payload(required = false) String message/*, @Header(KafkaHeaders.RECEIVED_KEY) String key*/) {
		//log.info("message consumed from kafka , message : {}, header : {} ", content, key);
		log.info("message consumed from kafka , message : {}", message);
		auditLogRepository
				.save(new AuditLog(0, "UNKNOWN", message, LocalDateTime.now()))
				.doOnSuccess(s -> log.info("message saved to db"))
				.doFinally(s -> log.error("message saved to db"))
				.subscribe();
	}

	@Bean
	RouterFunction route() {
		return RouterFunctions
				.route()
				.GET("/auditlog", request -> ServerResponse.ok().body(auditLogRepository.findAll(), AuditLog.class))
				.after((request, response) -> {
					log.info("{} {} {}",request.method(), request.path(), response.statusCode());
					return response;
				})
				.build();
	}
}

@Table("auditlog")
record AuditLog(@Id int id, String username, String message, LocalDateTime date){}
@Repository
interface AuditLogRepository extends R2dbcRepository<AuditLog, Integer>{}

@Configuration
class Configurations {
	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress ;
	@Value("${spring.kafka.consumer.group-id}")
	private String groupId;
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

		//The following code enable observation in the consumer listener
		factory.getContainerProperties().setObservationEnabled(true);
		factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
		return factory;
	}
}
