package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.swing.text.html.Option;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class GenericTest {
    @Test
    @DisplayName("Mono.zip test")
    public void monoZipTest() {
        Mono.zip(
                Mono.just("one").map(Optional::of),
                Mono.just(1).map(Optional::of),
                (a, b) -> new AlphaNumberic(a.get(), b.get())
        ).as(StepVerifier::create)
                .expectNext(new AlphaNumberic("one", 1))
                .verifyComplete();
    }

    @Test
    @DisplayName("Mono.zip(null,2) test")
    public void monoZipTest01() {
        var monoString = Mono.just("").map(Optional::of);
        var monoInteger = Mono.just(2).map(Optional::of);

        Mono.zip(monoString,monoInteger, (a, b) -> new AlphaNumberic(a.orElse(""), b.get()))
                .as(StepVerifier::create)
                .expectNext(new AlphaNumberic("", 2))
                .verifyComplete();
    }

    @Test
    void reactiveTest() {
        Optional.ofNullable(null);
    }
}

record AlphaNumberic(String alpha, Integer numeric) {}