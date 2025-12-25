package org.meldtech.platform.shared.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    @Primary
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper objectMapper
    ) {
        Jackson2JsonRedisSerializer<Object> valueSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        RedisSerializationContext<String, Object> context =
                RedisSerializationContext
                        .<String, Object>newSerializationContext(new StringRedisSerializer())
                        .value(valueSerializer)
                        .hashKey(new StringRedisSerializer())
                        .hashValue(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    public static  <T> T convert(ObjectMapper objectMapper, Object data, Class<T> clazz) {
        try {
            if(data instanceof String d) return objectMapper.readValue(d, clazz);
            return clazz.cast(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            System.err.println("Error converting data to " + clazz.getSimpleName() + " : "+e.getMessage());
            return null;
        }
    }

    public static  <T> List<T> convert(ObjectMapper objectMapper, String data, TypeReference<List<T>> type) {
        try {
            return objectMapper.readValue(data, type);
        } catch (Exception e) {
            System.err.println("Error converting data to " + type.getType() + " : "+e.getMessage());
            return null;
        }
    }

}

