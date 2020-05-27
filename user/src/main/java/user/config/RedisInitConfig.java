package user.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import user.domain.RateLimitVo;
import user.util.RateLimitClient;

import java.util.Map;

@Configuration
@EnableCaching
public class RedisInitConfig {

    //执行lua脚本的bean，在Redis初始化时注入
    @Autowired
    private RateLimitClient rateLimitClient;

    //操作redis的工具类
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * @description 初始化redis和公司令牌桶配置信息
     * @Param [factory]
     * @return org.springframework.data.redis.core.RedisTemplate<java.lang.String,java.lang.Object>
     * @author chenpengwei
     * @date 2020/5/27 9:55 上午
     */
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
        System.out.println("Springboot RedisTemplate 加载完成");

        //初始化redis时初始化令牌桶配置信息
        initBucketConfig(template);

        return template;
    }


    /**
     * @description 初始化lua脚本
     * @Param []
     * @return org.springframework.data.redis.core.script.DefaultRedisScript<java.lang.Long>
     * @author chenpengwei
     * @date 2020/5/27 9:55 上午
     */
    @Bean("rateLimitLua")
    public DefaultRedisScript<Long> getRateLimitScript() {

        DefaultRedisScript<Long> rateLimitLua = new DefaultRedisScript<>();
        rateLimitLua.setLocation(new ClassPathResource("rateLimter.lua"));
        rateLimitLua.setResultType(Long.class);
        System.out.println("初始化lua脚本完成");

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
        Map bucketConfig111222333 = redisTemplate.opsForHash().entries("rateLimter:111222333");

        //111222333服务信息不存在
        if (0 == bucketConfig111222333.size()) {
            //初始化服务1的令牌桶
            RateLimitVo vo1 = new RateLimitVo();
            vo1.setInitialPermits(100);  //初始化令牌数
            vo1.setMaxPermits(100);      //最大令牌数
            vo1.setOneSecondNum(100);    //每秒放入令牌100个
            rateLimitClient.init("111222333", vo1);
        }


        //获取111222333服务信息，若不存在redis则初始化令牌桶，存在则不添加
        Map bucketConfig222333444 = redisTemplate.opsForHash().entries("rateLimter:222333444");

        //222333444服务信息不存在
        if (0 == bucketConfig222333444.size()) {
            //初始化服务2的令牌桶
            RateLimitVo vo2 = new RateLimitVo();
            vo2.setInitialPermits(200);
            vo2.setMaxPermits(200);
            vo2.setOneSecondNum(200);   //每秒放入令牌100个
            rateLimitClient.init("222333444", vo2);
        }
    }

}