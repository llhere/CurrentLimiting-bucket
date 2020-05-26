package user.domain;

import lombok.Data;

/**
 * @author: cpw
 **/
@Data
public class RateLimitVo {

    private String url;

    private boolean isLimit;

    private Double interval;

    private Integer maxPermits;

    private Integer initialPermits;

}