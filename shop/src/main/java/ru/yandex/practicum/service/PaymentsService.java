package ru.yandex.practicum.service;

import lombok.extern.slf4j.Slf4j;
import main.codegen.ru.yandex.practicum.api.DefaultApi;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@Slf4j
public class PaymentsService extends DefaultApi {
    public Mono<BigDecimal> getBalance() {
        log.info("Start getBalance");
        return super.apiBalanceGet()
                .log()
                .onErrorReturn(BigDecimal.ZERO);
    }

    public Mono<Boolean> createPayment(BigDecimal amount) {
        log.info("Start createPayment: amount={}", amount);
        return super.apiBalancePost(amount)
                .log()
                .onErrorReturn(false);
    }
}
