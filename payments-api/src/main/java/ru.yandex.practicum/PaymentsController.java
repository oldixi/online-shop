package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import main.codegen.ru.yandex.practicum.api.DefaultApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@Slf4j
public class PaymentsController implements DefaultApi {
    private BigDecimal balance;

    @Override
    public ResponseEntity<BigDecimal> apiBalanceGet() {
        log.info("Start apiBalanceGet");
        if (balance == null) setRandomBalance();
        log.info("Processing apiBalanceGet: balance={}", balance);
        return ResponseEntity.ok(balance);
    }

    @Override
    public ResponseEntity<Boolean> apiBalancePost(BigDecimal amount) {
        log.info("Start apiBalancePost: amount={}", amount);
        boolean isPaymentPassed = true;
        if (balance == null) setRandomBalance();
        if (balance.subtract(amount).signum() == -1) isPaymentPassed = false;
        log.info("Processing apiBalancePost: balance={}", balance);
        return ResponseEntity.ok(isPaymentPassed);
    }

    private void setRandomBalance() {
        balance = BigDecimal.valueOf(Math.random() * 1000);
    }
}
