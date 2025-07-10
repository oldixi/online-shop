package ru.yandex.practicum.logger;

import lombok.Data;
import org.slf4j.event.Level;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "blog.http.logging")
public class RequestLoggerProperties {
    private Level level = Level.INFO;
    private boolean enabled = true;
}
