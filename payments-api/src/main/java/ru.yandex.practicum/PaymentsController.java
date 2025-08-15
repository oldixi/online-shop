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
        if (balance == null) setRandomBalance();
        return ResponseEntity.ok(balance);
    }

    @Override
    public ResponseEntity<Boolean> apiBalancePost(BigDecimal amount) {
        if (balance == null) setRandomBalance();
        if (balance.subtract(amount).signum() != -1) balance = balance.subtract(amount);
        return ResponseEntity.ok(balance.subtract(amount).signum() != -1);
    }

    private void setRandomBalance() {
        balance = BigDecimal.valueOf(Math.random() * 1000);
    }
}
