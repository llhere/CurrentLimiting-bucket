package user.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import user.annotation.RateLimiter;
import user.domain.RateLimitVo;
import user.enums.RateLimitResult;
import user.util.RateLimitClient;

import javax.annotation.PostConstruct;
import java.util.Map;

@Aspect
@Component
public class RateLimterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimterHandler.class);

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private RateLimitClient rateLimitClient;

    private DefaultRedisScript<Long> getRedisScript;

    @PostConstruct
    public void init() {
        getRedisScript = new DefaultRedisScript<>();
        getRedisScript.setResultType(Long.class);
        getRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("rateLimter.lua")));
        LOGGER.info("RateLimterHandler[分布式限流处理器]脚本加载完成");
    }

    @Pointcut("@annotation(user.annotation.RateLimiter)")
    public void rateLimiter() {}

    //这里我们定义了一个切点，表示只要注解了 @RateLimiter 的方法，均可以触发限流操作。
    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, RateLimiter rateLimiter) throws Throwable {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("RateLimterHandler[分布式限流处理器]开始执行限流操作");
        }

        Signature signature = proceedingJoinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("the Annotation @RateLimter must used on method!");
        }



        /**
         * 获取注解参数
         */
        // 限流模块key
        String limitKey = rateLimiter.key();
        //令牌数（桶大小）,最多大小,间隔（每隔间隔数放一个令牌）

        //根据模块判断令牌桶是否存在，存在则获取配置，不存在初始化配置
        boolean exist = redisTemplate.opsForHash().hasKey("hash", "rateLimter:" + limitKey);
        Map map = redisTemplate.opsForHash().entries("rateLimter:" + limitKey);
        //令牌桶
        RateLimitVo vo = new RateLimitVo();
        //不存在则初始化
        if (!exist){
            vo.setInitialPermits(50);
            vo.setMaxPermits(100);
            vo.setInterval(1000.0);
            rateLimitClient.init(limitKey, vo);
        }else {
            //获取redis的初始化配置
//            Map map = redisTemplate.opsForHash().entries("rateLimter:" + limitKey);
//            Integer initialPermits = (Integer) map.get("stored_permits");
//            Integer maxPermits = (Integer) map.get("max_permits");
//            Double interval = (Double) map.get("interval");
//            vo.setInitialPermits(initialPermits);
//            vo.setMaxPermits(maxPermits);
//            vo.setInterval(interval);
        }

        String msg = "当前总令牌:" + vo.getInitialPermits() + ",最多令牌：" + vo.getMaxPermits() + ",放入一个令牌时间间隔：" + vo.getInterval();

        //调用脚本并执行
        RateLimitResult result = rateLimitClient.acquire(limitKey);
        if (result == RateLimitResult.ERROR) {
            LOGGER.info("请求[失败]," + msg );
            return null;
        }else if (result == RateLimitResult.SUCCESS){
            LOGGER.info("请求成功" + msg);
        }


        return proceedingJoinPoint.proceed();
    }

}