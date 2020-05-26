package user.annotation;

import java.lang.annotation.*;

/**
* @author chenpengwei
* @version 1.0
* @date 2018/10/27 1:25
* @className RateLimiter
* @desc 限流注解
*/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
    * 限流key
    * @return
    */
    String key() default "rate:limiter";
}