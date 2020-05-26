package user.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import user.domain.RateLimitVo;
import user.enums.RateLimitMethod;
import user.enums.RateLimitResult;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @author: cpw
 **/
@Service
@Slf4j
public class RateLimitClient {

    private static final String RATE_LIMIT_PREFIX = "rateLimter:";

    @Autowired
    StringRedisTemplate redisTemplate;

    @Resource
    @Qualifier("rateLimitLua")
    RedisScript<Long> rateLimitScript;

    public RateLimitResult init(String key, RateLimitVo rateLimitInfo){
        return exec(key, RateLimitMethod.init,
                rateLimitInfo.getInitialPermits(),
                rateLimitInfo.getMaxPermits(),
                rateLimitInfo.getInterval(),
                key);
    }

    public RateLimitResult modify(String key, RateLimitVo rateLimitInfo){
        return exec(key, RateLimitMethod.modify, key,
                rateLimitInfo.getMaxPermits(),
                rateLimitInfo.getInterval());
    }

    public RateLimitResult delete(String key){
        return exec(key, RateLimitMethod.delete);
    }

    public RateLimitResult acquire(String key){
        return acquire(key, 1);
    }

    public RateLimitResult acquire(String key, Integer permits){
        return exec(key, RateLimitMethod.acquire, permits);
    }

    /**
     * 执行redis的具体方法，限制method,保证没有其他的东西进来
     * @param key
     * @param method
     * @param params
     * @return
     */
    private RateLimitResult exec(String key, RateLimitMethod method, Object... params){
        try {
            Long timestamp = getRedisTimestamp();
            String[] allParams = new String[params.length + 2];
            allParams[0] = method.name();
            allParams[1] = timestamp.toString();
            for(int index = 0;index < params.length; index++){
                allParams[2 + index] = params[index].toString();
            }
            Long result = redisTemplate.execute(rateLimitScript,
                    Collections.singletonList(getKey(key)),
                    allParams);
            return RateLimitResult.getResult(result);
        } catch (Exception e){
            log.error("execute redis script fail, key:{}, method:{}",
                    key, method.name(), e);
            return RateLimitResult.ERROR;
        }
    }

    private Long getRedisTimestamp(){
        Long currMillSecond = redisTemplate.execute(
                (RedisCallback<Long>) redisConnection -> redisConnection.time()
        );
        return currMillSecond;
    }
    private String getKey(String key){
        return RATE_LIMIT_PREFIX + key;
    }
}