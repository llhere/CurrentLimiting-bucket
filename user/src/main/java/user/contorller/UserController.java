package user.contorller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import user.util.RateLimitClient;

import java.util.Map;

@RestController
public class UserController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    private RateLimitClient rateLimitClient;

    @GetMapping("/m1/{id}")
    public String m1(@PathVariable Long id) {
        //渠道111、机构222、服务333  key=111222333 ，100，100，100
        //渠道222、机构333、服务444  key=111222333， 200，200，200

        //调用的服务
        String clientName = (1 == id) ? "111222333" : "222333444";

        //服务A配置  在这里只是为了打印控制台令牌桶的数量，可能打印的结果和实际不匹配，因为在此行代码和获取令牌桶操作中会有其他线程进来
        Map bucketConfigMap = redisTemplate.opsForHash().entries("rateLimter:" + clientName);

        //令牌桶日志信息
        String msg = "当前程序：" + bucketConfigMap.get("app")
                + ",消费后令牌:" + bucketConfigMap.get("stored_permits")
                + ",最多令牌：" + bucketConfigMap.get("max_permits")
                + ",每秒放入令牌桶数量：" + bucketConfigMap.get("oneSecondNum");

        //获取令牌 执行结果为false则没有获取到令牌
        if (!rateLimitClient.execute(clientName)){
            System.err.println("请求失败," + msg );
            return "null";
        }

        System.out.println("请求成功," + msg);

        return "m1被调用完成";
    }

}