package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@SpringBootApplication
@Slf4j
public class ApiApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(ApiApplication.class, args);
    }


    @Bean
    public RouterFunction routerFunction(UserServices userServices) {
        return RouterFunctions
                .route()
                .GET("/person", request -> ServerResponse.ok().body(userServices.allUsers(), User.class))
                .GET("/person/{id}", request -> {
                    var id = Integer.parseInt(request.pathVariable("id"));
                    return ServerResponse.ok().body(userServices.userById(id), User.class);
                })
                .after((request, response) -> {
                    log.info("{} {} {}", request.method(), request.path(), response.statusCode());
                    request.headers().asHttpHeaders().forEach((k, v) -> log.info("{}: {}", k, v));
                    return response;
                })
                .build();
    }
}

record User(int id, String name, String email, Address address, String phone, String website, Company company,
            @JsonProperty("employment") EmploymentDetails employmentDetails) {
}

record Address(String street, String suite, String city, String zipcode, Geo geo) {
}

record Company(String name, String catchPhrase, String bs) {
}

record Geo(String lat, String lng) {
}

@Builder
record EmploymentDetails(@Id int id, String designation, int salary) { }

@Repository
interface EmploymentDetailsRepository extends R2dbcRepository<EmploymentDetails, Integer> {
}

@Slf4j
@Service
@RequiredArgsConstructor
class UserServices {
    private final UserClient userClient;
    private final ProduceClient produceClient;
    private final EmploymentDetailsRepository employmentDetailsRepository;

    public Flux<User> allUsers() {
        var allUser = userClient.allUsers()
                .map(Optional::ofNullable)
                .defaultIfEmpty(Optional.empty())
                .onErrorReturn(Optional.empty())
                .doOnError(e -> log.error("Error while fetching all users {}", e));

        var allUserEmpDetails = employmentDetailsRepository.findAll()
                .map(Optional::ofNullable)
                .defaultIfEmpty(Optional.empty())
                .onErrorReturn(Optional.empty())
                .doOnError(e -> log.error("Error while fetching all empDetails for users {}", e));

        return allUser.zipWith(allUserEmpDetails, (t1, t2) -> {
            var u = t1.get();
            var e = t2.get();
            return new User(u.id(), u.name(), u.email(), u.address(), u.phone(), u.website(), u.company(), e);
        })
                // The trace chain broken from here //TODO find why?
                .doFinally(signal -> produceClient.auditLog(String.format("allUsers()::%s",signal)).subscribe())
                .onErrorResume(e -> Mono.error(new UserNotFoundException()));
    }

    public Mono<User> userById(int id) {
        var user = userClient.userById(id)
                .map(Optional::ofNullable)
                .defaultIfEmpty(Optional.empty());

        var emp = employmentDetailsRepository
                .findById(id)
                .map(Optional::ofNullable)
                .defaultIfEmpty(Optional.of(EmploymentDetails.builder().build()))
                .onErrorReturn(Optional.of(EmploymentDetails.builder().build()));

        return user.zipWhen(user1 -> emp, (t1,t2) -> {
                            var u = t1.get();
                            var e = t2.get();
                            return new User(u.id(), u.name(), u.email(), u.address(), u.phone(), u.website(), u.company(), e);
                        })
                .doFinally(signal -> produceClient.auditLog(String.format("userById(%s)::%s",id,signal)).subscribe())
                .onErrorResume(e -> Mono.error(new UserNotFoundException()));
    }
}

@HttpExchange(url = "/users")
interface UserClient {
    @GetExchange
    Flux<User> allUsers();

    @GetExchange(url = "/{id}")
    Mono<User> userById(@PathVariable("id") int id);
}

interface ProduceClient {
    @PostExchange("/produce/auditlog")
    Mono<Void> auditLog(@RequestBody String message);
}

@Configuration
@Slf4j
@RequiredArgsConstructor
class Configurations {
    private final WebClient.Builder builder;

    @Bean
    UserClient userClient(@Value("${spring.application.jsonplaceHolder.host}") String baseUrl) {
        builder.defaultHeader("Accept", "application/json");
        var webClientAdapter = WebClientAdapter.forClient(builder.baseUrl(baseUrl).build());
        var httpServiceProxyFactory = HttpServiceProxyFactory.builder(webClientAdapter).build();
        return httpServiceProxyFactory.createClient(UserClient.class);
    }

    @Bean
    ProduceClient produceClient(@Value("${spring.application.kafka-producer.auditlog.host}") String baseUrl) {
        builder.baseUrl(baseUrl);
        builder.filter((request, next) -> {
            log.info("Request: {} {} Invoked", request.method(), request.url());
            request.headers().forEach((k, v) -> log.info("{}: {}", k, v));
            return next.exchange(request);
        });
        var webClientAdapter = WebClientAdapter.forClient(builder.build());
        var httpServiceProxyFactory = HttpServiceProxyFactory.builder(webClientAdapter).build();
        return httpServiceProxyFactory.createClient(ProduceClient.class);
    }

}

@ResponseStatus(code = NOT_FOUND, reason = "User not found")
class UserNotFoundException extends RuntimeException {
}