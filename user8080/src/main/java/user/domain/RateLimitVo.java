package user.domain;

import lombok.Data;

/**
 * @author: cpw
 *操作令牌桶的vo
 **/
@Data
public class RateLimitVo {

    /** 放入1个令牌到令牌桶的间隔时间 */
    private Double interval;

    /** 令牌桶最大容量 */
    private Integer maxPermits;

    /** 令牌桶初始化容量 */
    private Integer initialPermits;

}