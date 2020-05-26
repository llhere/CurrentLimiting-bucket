package user.contorller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import user.annotation.RateLimiter;
import user.domain.RateLimitVo;
import user.util.RateLimitClient;

@RestController
public class UserController {



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

        System.out.println("aaaaaaa");
        return "m1被调用";
    }



    @GetMapping("/m2/{id}")
    @RateLimiter(key = "m2")
    public String m2(@PathVariable Long id) {

        return "m2被调用";
    }

}