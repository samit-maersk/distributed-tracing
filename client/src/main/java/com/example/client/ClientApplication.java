package com.example.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
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
				.path("/employee", builder -> builder
						.GET("", request -> ServerResponse.ok().body(personClient.allPerson(), Person.class))
						.GET("/{id}", request -> {
							var id = Integer.parseInt(request.pathVariable("id"));
							return ServerResponse.ok().body(personClient.personById(id), Person.class);
						})
				)
				.after((request, response) -> {
					log.info("{} {} {}",request.method(), request.path(), response.statusCode());
					request.headers().asHttpHeaders().forEach((k, v) -> log.info("{}: {}", k, v));
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
@RequiredArgsConstructor
class Configurations {
	private final WebClient.Builder builder;
	@Bean
	PersonClient personClient(@Value("${spring.application.api.host}") String baseUrl) {
		builder.defaultHeader("Accept", "application/json");
		var webClientAdapter = WebClientAdapter.forClient(builder.baseUrl(baseUrl).build());
		var httpServiceProxyFactory = HttpServiceProxyFactory.builder(webClientAdapter).build();
		return httpServiceProxyFactory.createClient(PersonClient.class);
	}
}

@Configuration
@EnableWebFlux
class CorsGlobalConfiguration implements WebFluxConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry corsRegistry) {
		corsRegistry.addMapping("/**")
				.allowedOrigins("*")
				.allowedMethods("*")
				.maxAge(3600);
	}
}