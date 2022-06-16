package com.luohao.reggie.config;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 设置redis的序列化方式
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    @Bean
    public RedisTemplate<Object,Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<Object,Object> redisTemplate=new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());        //重新设置key的序列化方式，默认为jdk序列化
        redisTemplate.setValueSerializer(new StringRedisSerializer());      //重新设置value的序列化方式，默认为jdk序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());    //重新设置Hash类型key的序列化方式，默认为jdk序列化
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());  //重新设置Hash类型的value的序列化方式，默认为jdk序列化
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}
