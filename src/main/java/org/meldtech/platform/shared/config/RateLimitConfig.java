package org.meldtech.platform.shared.config;


import org.meldtech.platform.shared.web.NoopRateLimiter;
import org.meldtech.platform.shared.web.RateLimiter;
import org.meldtech.platform.shared.web.RedisLeakyBucketRateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
public class RateLimitConfig {

    @Bean
    @ConditionalOnBean(ReactiveRedisConnectionFactory.class)
    ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveStringRedisTemplate(factory);
    }

    @Bean
    @ConditionalOnBean({ReactiveStringRedisTemplate.class, RateLimitProperties.class})
    @ConditionalOnProperty(prefix = "app.ratelimit", name = "enabled", havingValue = "true")
    RateLimiter redisRateLimiter(ReactiveStringRedisTemplate redis, RateLimitProperties props) {
        return new RedisLeakyBucketRateLimiter(redis, props.getKeyPrefix(), props.getCapacity(), props.getLeakPerSecond());
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    RateLimiter noopRateLimiter() {
        return new NoopRateLimiter();
    }
}
