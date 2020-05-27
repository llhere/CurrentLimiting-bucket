package user.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import user.config.RedisInitConfig;
import user.domain.RateLimitVo;
import user.enums.RateLimitMethod;
import user.enums.RateLimitResult;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @author: cpw
 * 操作令牌桶的实现类
 **/
@Service
@Slf4j
public class RateLimitClient {

    //日志信息
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisInitConfig.class);

    //令牌桶服务名前缀
    private static final String RATE_LIMIT_PREFIX = "rateLimter:";

    //操作redis的工具类
    @Autowired
    StringRedisTemplate redisTemplate;

    //执行lua脚本的bean，在Redis初始化时注入
    @Resource
    @Qualifier("rateLimitLua")
    RedisScript<Long> rateLimitScript;


    /**
     * @description 初始化令牌桶
     * @Param [key 初始化令牌桶服务的名称, rateLimitInfo 令牌桶vo]
     * @return user.enums.RateLimitResult
     * @author chenpengwei
     * @date 2020/5/27 9:44 上午
     */
    public RateLimitResult init(String key, RateLimitVo rateLimitInfo){
        return exec(key, RateLimitMethod.init,
                rateLimitInfo.getInitialPermits(),
                rateLimitInfo.getMaxPermits(),
                rateLimitInfo.getInterval(),
                key);
    }


    /**
     * @description 修改令牌桶的配置信息
     * @Param [key  需要修改令牌桶配置信息服务的名称, rateLimitInfo 令牌桶vo]
     * @return user.enums.RateLimitResult
     * @author chenpengwei
     * @date 2020/5/27 9:45 上午
     */
    public RateLimitResult modify(String key, RateLimitVo rateLimitInfo){
        return exec(key, RateLimitMethod.modify, key,
                rateLimitInfo.getMaxPermits(),
                rateLimitInfo.getInterval());
    }


    /**
     * @description 删除令牌桶的配置信息
     * @Param [key 需要删除服务的名称]
     * @return user.enums.RateLimitResult
     * @author chenpengwei
     * @date 2020/5/27 9:45 上午
     */
    public RateLimitResult delete(String key){
        return exec(key, RateLimitMethod.delete);
    }


    /**
     * @description 每次请求获得的令牌，默认1
     * @Param [key 需要获得令牌的服务名称]
     * @return user.enums.RateLimitResult
     * @author chenpengwei
     * @date 2020/5/27 9:46 上午
     */
    public RateLimitResult acquire(String key){
        return acquire(key, 1);
    }

    public RateLimitResult acquire(String key, Integer permits){
        return exec(key, RateLimitMethod.acquire, permits);
    }

    /**
     * 执行redis的具体方法，限制method,保证没有其他东西进来
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
            log.error("执行脚本失败, key:{}, method:{}",
                    key, method.name(), e);
            return RateLimitResult.ERROR;
        }
    }


    /**
     * @description 获取令牌桶的计时时间
     * @Param []
     * @return java.lang.Long
     * @author chenpengwei
     * @date 2020/5/27 9:50 上午
     */
    private Long getRedisTimestamp(){
        Long currMillSecond = redisTemplate.execute(
                (RedisCallback<Long>) redisConnection -> redisConnection.time()
        );
        return currMillSecond;
    }


    /**
     * @description 获取令牌桶的服务名
     * @Param [key 令牌桶的服务名称]
     * @return java.lang.String 完整的令牌桶服务名
     * @author chenpengwei
     * @date 2020/5/27 9:51 上午
     */
    private String getKey(String key){
        return RATE_LIMIT_PREFIX + key;
    }


    /**
     * @description 执行获取令牌操作
     * @Param [clientName 服务名称]
     * @return boolean 获取成功返回true，失败返回false
     * @author chenpengwei
     * @date 2020/5/26 下午 8:12
     */
    public boolean execute(String clientName) {

        //执行获取令牌
        RateLimitResult result = acquire(clientName);

        //定义处理日志
        String msg = "服务：" + clientName + "，";

        //返回类型 默认false
        boolean isGet = false;

        //获取结果失败打印失败信息并反回false
        if (result == RateLimitResult.ACQUIRE_FAIL) {
            msg += "获取令牌失败";
        }else if (result == RateLimitResult.NO_LIMIT){
            msg += "未配置令牌桶信息";
        }else if (result == RateLimitResult.MODIFY_ERROR){
            msg += "修改令牌桶配置错误";
        }else if (result == RateLimitResult.UNSUPPORT_METHOD){
            msg += "不支持的操作";
        }else if (result == RateLimitResult.ERROR){
            msg += "获取令牌意外出错";
        }else if (result == RateLimitResult.SUCCESS){
            msg += "获取令牌成功";
            isGet = true;
        }

        //打印日志
//        LOGGER.info(msg);

        return isGet;
    }
}