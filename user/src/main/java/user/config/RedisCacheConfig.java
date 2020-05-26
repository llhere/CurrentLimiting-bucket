package user.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import user.domain.RateLimitVo;
import user.util.RateLimitClient;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Autowired
    private RateLimitClient rateLimitClient;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {


        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Springboot Redis cacheManager 加载完成");
        }

        return RedisCacheManager
                .builder( RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(redisCacheConfiguration).build();
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式）
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        template.setValueSerializer(serializer);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        LOGGER.info("Springboot RedisTemplate 加载完成");

        //初始化redis时初始化令牌桶配置信息
        initBucketConfig(template);

        return template;
    }

    @Bean("rateLimitLua")
    public DefaultRedisScript<Long> getRateLimitScript() {
        DefaultRedisScript<Long> rateLimitLua = new DefaultRedisScript<>();
        rateLimitLua.setLocation(new ClassPathResource("rateLimter.lua"));
        rateLimitLua.setResultType(Long.class);
        LOGGER.info("初始化lua脚本完成");
        return rateLimitLua;
    }



    /**
     * @description 初始化令牌桶信息（模拟） 
     * @Param []
     * @return void
     * @author chenpengwei
     * @date 2020/5/26 下午 8:26
     */ 
    private void initBucketConfig(RedisTemplate template) {

        //服务1
        RateLimitVo vo1 = new RateLimitVo();
        vo1.setInitialPermits(10);
        vo1.setMaxPermits(10);
        vo1.setInterval(1000.0);
        rateLimitClient.init("111222333", vo1);


        //服务2
        RateLimitVo vo2 = new RateLimitVo();
        vo2.setInitialPermits(20);
        vo2.setMaxPermits(20);
        vo2.setInterval(500.0);
        rateLimitClient.init("222333444", vo2);
    }
}