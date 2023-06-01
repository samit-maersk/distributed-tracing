package com.example.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		Hooks.enableAutomaticContextPropagation();
		SpringApplication.run(ClientApplication.class, args);
	}
}


@Component
@Slf4j
class Client {

	@Bean
	RouterFunction route(PersonClient personClient) {
		return RouterFunctions
				.route()
				.GET("/all", request -> ServerResponse.ok().body(personClient.allPerson(), Person.class))
				.GET("/{id}", request -> {
					var id = Integer.parseInt(request.pathVariable("id"));
					return ServerResponse.ok().body(personClient.personById(id), Person.class);
				})
				.after((request, response) -> {
					log.info("{} {}",request.path(), response.statusCode());
					return response;
				})
				.build();
	}

}

record Person(int id, String name, String email, Address address, String phone, String website, Company company, Employment employment) {}
record Address(String street, String suite, String city, String zipcode, Geo geo) {}
record Company(String name, String catchPhrase, String bs) {}
record Geo(String lat, String lng) {}
record Employment(int id, String designation, int salary) {}

@HttpExchange(url = "/person")
interface PersonClient {
	@GetExchange
	Flux<Person> allPerson();
	@GetExchange(url = "/{id}")
	Mono<Person> personById(@PathVariable("id") int id);
}

@Component
class Configurations {

	@Bean
	PersonClient personClient(WebClient.Builder builder, @Value("${spring.application.tracing-demo.host}") String baseUrl) {
		builder.defaultHeader("Accept", "application/json");
		var webClientAdapter = WebClientAdapter.forClient(builder.baseUrl(baseUrl).build());
		var httpServiceProxyFactory = HttpServiceProxyFactory.builder(webClientAdapter).build();
		return httpServiceProxyFactory.createClient(PersonClient.class);
	}
}