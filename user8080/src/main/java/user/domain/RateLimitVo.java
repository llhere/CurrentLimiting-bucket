package user.domain;

import lombok.Data;

/**
 * @author: cpw
 *操作令牌桶的vo
 **/
@Data
public class RateLimitVo {

    /** 每秒放入令牌数量 */
    private Integer oneSecondNum;

    /** 令牌桶最大容量 */
    private Integer maxPermits;

    /** 令牌桶初始化容量 */
    private Integer initialPermits;

}