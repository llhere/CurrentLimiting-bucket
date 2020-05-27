package user.enums;

/**
 * @author: cpw
 * 执行获取令牌后返回的状态值
 **/
public enum RateLimitResult {

    //操作成功
    SUCCESS(1L),
    //未配置
    NO_LIMIT(0L),
    //获取令牌失败
    ACQUIRE_FAIL(-1L),
    //修改错误
    MODIFY_ERROR(-2L),
    //不支持的操作
    UNSUPPORT_METHOD(-500L),
    //执行中意外错误
    ERROR(-505L);

    private Long code;


    //构造函数
    RateLimitResult(Long code){
        this.code = code;
    }

    /**
     * @description 获取执行后的返回码
     * @Param [code]
     * @return user.enums.RateLimitResult
     * @author chenpengwei
     * @date 2020/5/27 9:39 上午
     */
    public static RateLimitResult getResult(Long code){
        for(RateLimitResult enums: RateLimitResult.values()){
            if(enums.code.equals(code)){
                return enums;
            }
        }
        throw new IllegalArgumentException("未知的限制返回码:" + code);
    }
}