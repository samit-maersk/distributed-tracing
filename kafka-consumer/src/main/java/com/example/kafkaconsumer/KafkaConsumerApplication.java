package com.example.kafkaconsumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class KafkaConsumerApplication {

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(KafkaConsumerApplication.class, args);
	}
}

@Component
@Slf4j
class KafkaConsumer {

	@KafkaListener(topics = "test")
	public void processMessage(@Payload(required = false) String message/*, @Header(KafkaHeaders.RECEIVED_KEY) String key*/) {
		//log.info("message consumed from kafka , message : {}, header : {} ", content, key);
		log.info("message consumed from kafka , message : {}", message);
	}

	@Bean
	RouterFunction route() {
		return RouterFunctions
				.route()
				.GET("/ping", request -> ServerResponse.ok().bodyValue("pong"))
				.after((request, response) -> {
					log.info("{} {} {}",request.method(), request.path(), response.statusCode());
					return response;
				})
				.build();
	}
}
