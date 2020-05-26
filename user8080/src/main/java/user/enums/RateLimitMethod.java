package user.enums;

/**
 * @author: cpw
 *
 * 限流的具体方法
 */
public enum RateLimitMethod {

    //初始化限流器
    init,

    //修改限流器配置（主要针对限流器的桶大小和放令牌间隔，即1/QPS）
    modify,

    //删除限流器配置
    delete,

    //尝试获取制定数目的令牌
    acquire;
}