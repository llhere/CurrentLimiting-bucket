package user.contorller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import user.annotation.RateLimiter;
import user.domain.RateLimitVo;
import user.util.RateLimitClient;

@RestController
public class UserController {

    @Autowired
    RedisTemplate redisTemplate;



    @Autowired
    private RateLimitClient rateLimitClient;

    public void UserController(){
        RateLimitVo vo = new RateLimitVo();
        vo.setInitialPermits(500);
        vo.setMaxPermits(500);
        vo.setInterval(2.0);
        rateLimitClient.init("test", vo);
    }



    @GetMapping("/m1/{id}")
    @RateLimiter(key = "m1")
    public String m1(@PathVariable Long id) {
        //渠道100、机构100、服务a  key=100100a ，100，10，100
        //渠道101、机构101、服务a  key =101101a，10，10，100
        System.out.println("aaaaaaa");
//        string  ABC =geteRatelimite(渠道、机构、服务);
//        if(ABC){
//            http://www.baiduw.com
//        }else{
//            http://erroe.html
//        }


        //服务A配置
        String m1Config = (String) redisTemplate.opsForValue().get("a1");


        return "m1被调用";
    }



    @GetMapping("/m2/{id}")
    @RateLimiter(key = "m2")
    public String m2(@PathVariable Long id) {

        return "m2被调用";
    }


    public void init(){

    }
}