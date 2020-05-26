package user.contorller;

import user.annotation.RateLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {



    @GetMapping("/m1/{id}")
    @RateLimiter(key = "m1")
    public void m1(@PathVariable Long id) {

        System.err.println("m1被调用");
    }



    @GetMapping("/m2/{id}")
    @RateLimiter(key = "m2")
    public void m2(@PathVariable Long id) {

        System.err.println("m2被调用");
    }

}