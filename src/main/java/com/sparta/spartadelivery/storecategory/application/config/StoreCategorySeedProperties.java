package com.sparta.spartadelivery.storecategory.application.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed.store-categories")
public record StoreCategorySeedProperties(
        boolean enabled,
        List<String> values
) {

    public StoreCategorySeedProperties {
        values = values == null ? List.of() : List.copyOf(values);
    }
}
