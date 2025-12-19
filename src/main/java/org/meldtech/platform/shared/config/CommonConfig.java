package org.meldtech.platform.shared.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.context.request.RequestScope;

/**
 * Shared configuration toggles and common beans can be declared here as the system evolves.
 */
@Configuration
@EnableConfigurationProperties({ RateLimitProperties.class  })
public class CommonConfig {

    // Ensure ISO-8601 for Java time types in JSON (no numeric timestamps)
    @Bean
    Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // Expose a TransactionalOperator when a ReactiveTransactionManager is available (R2DBC)
    @Bean
    @ConditionalOnBean(ReactiveTransactionManager.class)
    TransactionalOperator transactionalOperator(ReactiveTransactionManager txManager) {
        return TransactionalOperator.create(txManager);
    }

    /**
     * Register request scope for WebFlux environment
     */
    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("request", new RequestScope());
        return configurer;
    }
}
