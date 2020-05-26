package user.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import user.domain.RateLimitVo;
import user.util.RateLimitClient;

import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheConfig.class);

    //令牌桶操作类
    @Autowired
    private RateLimitClient rateLimitClient;

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


        //获取111222333服务信息，若不存在redis则初始化令牌桶，存在则不添加
        Map bucketConfig111222333 = template.opsForHash().entries("rateLimter:111222333");

        //111222333服务信息不存在
        if (0 == bucketConfig111222333.size()) {
            //初始化服务1的令牌桶
            RateLimitVo vo1 = new RateLimitVo();
            vo1.setInitialPermits(10);
            vo1.setMaxPermits(10);
            vo1.setInterval(1000.0);
            rateLimitClient.init("111222333", vo1);
        }


        //获取111222333服务信息，若不存在redis则初始化令牌桶，存在则不添加
        Map bucketConfig222333444 = template.opsForHash().entries("rateLimter:222333444");

        //222333444服务信息不存在
        if (0 == bucketConfig222333444.size()) {
            //初始化服务2的令牌桶
            RateLimitVo vo2 = new RateLimitVo();
            vo2.setInitialPermits(20);
            vo2.setMaxPermits(20);
            vo2.setInterval(500.0);
            rateLimitClient.init("222333444", vo2);
        }

    }
}