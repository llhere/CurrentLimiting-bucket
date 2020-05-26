package user.contorller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import user.enums.RateLimitResult;
import user.util.RateLimitClient;

import java.util.Map;

@RestController
public class UserController {

    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    private RateLimitClient rateLimitClient;

    @GetMapping("/m1/{id}")
    public String m1(@PathVariable Long id) {
        //渠道100、机构100、服务a  key=100100a ，100，10，100
        //渠道101、机构101、服务a  key =101101a，10，10，100
//        string  ABC =geteRatelimite(渠道、机构、服务);
//        if(ABC){
//            http://www.baiduw.com
//        }else{
//            http://erroe.html
//        }

        //调用的服务
        String a = (1 == id) ? "111222333" : "222333444";


        //服务A配置
        Map bucketConfigMap = redisTemplate.opsForHash().entries("rateLimter:" + a);

        //配置不为空
        if (bucketConfigMap.size() > 0) {

            //令牌桶日志信息
            String msg = "当前程序：" + bucketConfigMap.get("app") + ",消费后令牌:" + bucketConfigMap.get("stored_permits") + ",最多令牌：" + bucketConfigMap.get("max_permits") + ",放入一个令牌时间间隔：" + bucketConfigMap.get("interval");

            //调用脚本并执行 执行结果为false则没有获取到令牌
            if (!execute(a, msg)){
                return "null";
            }
        }

        return "m1被调用完成";
    }


    /**
     * @description 是否可以获取令牌
     * @Param [a 服务名称, msg 令牌剩余状态]
     * @return boolean
     * @author chenpengwei
     * @date 2020/5/26 下午 8:12
     */
    private boolean execute(String a, String msg) {

        //执行获取令牌
        RateLimitResult result = rateLimitClient.acquire(a);

        //获取结果
        if (result == RateLimitResult.ACQUIRE_FAIL) {
            System.err.println("请求[失败]," + msg );
            return false;
        }else if (result == RateLimitResult.SUCCESS){
            System.err.println("请求成功," + msg);
            return true;
        }

        return false;
    }
}