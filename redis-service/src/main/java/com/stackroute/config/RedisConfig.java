package com.stackroute.config;

import com.stackroute.domain.DataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;


@Configuration
@ComponentScan("com.stackroute")
public class RedisConfig {


    // Jedis Connection Factory Configuration
    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName("redis");
        factory.setPort(6379);
        factory.setUsePool(true);
        return factory;
    }


    // Jedis Client we are defining a Connection Factory
    @Bean
    @Autowired
    RedisTemplate<String, DataModel> redisTemplate() {
        final RedisTemplate<String, DataModel> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setValueSerializer(new GenericToStringSerializer<>(DataModel.class));
        return template;
    }


}
