package com.example.kafkaproducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Hooks;

import java.util.Map;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerApplication {
	private final KafkaTemplate<String, String> kafkaTemplate;

	@Value("${spring.kafka.topic}")
	public String topicName;

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(KafkaProducerApplication.class, args);
	}

	@Bean
	public RouterFunction route() {
		return RouterFunctions
				.route()
				.POST("/produce/auditlog", request -> {
					return request.bodyToMono(String.class)
							.doOnNext(message -> kafkaTemplate.send(topicName, message))
							.then(ServerResponse.ok().build())
							.doOnSuccess(s -> log.info("message sent to kafka"))
							.doOnError(e -> log.error("error while sending message to kafka", e));
				})
				.after((request, response) -> {
					log.info("{} {} {}",request.method(), request.path(), response.statusCode());
					request.headers().asHttpHeaders().forEach((k, v) -> log.info("{}: {}", k, v));
					return response;
				})
				.build();
	}

}

@Configuration
class Configurations {
	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

	@Bean
	public NewTopic createTopic(@Value("${spring.kafka.topic}") String topicName) {
		return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		Map<String, Object> configProps = Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress,
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
		);

		KafkaTemplate<String, String> stringStringKafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configProps));
		stringStringKafkaTemplate.setObservationEnabled(true);
		return stringStringKafkaTemplate;
	}
}
