package io.github.rohergun.budgetmanager.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CacheHealthIndicator implements HealthIndicator {

    private final CacheManager cacheManager;

    public CacheHealthIndicator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Health health() {
        boolean cacheExists = cacheManager.getCache("monthlySummary") != null;
        if (cacheExists) {
            return Health.up()
                    .withDetail("cache", "monthlySummary")
                    .withDetail("provider", "Caffeine")
                    .build();
        }
        return Health.down()
                .withDetail("reason", "monthlySummary cache not found")
                .build();
    }
}
