package com.houjiang.payment.dto.channel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 路由选择结果
 * @author Amos
 */
@Data
public class RouteSelectResponse {

    /** 是否匹配到可用渠道 */
    private boolean matched;

    /** 日志编号 */
    private String logNo;

    /** 命中的路由规则编号 */
    private String routeNo;

    /** 选中的渠道编码 */
    private String channelCode;

    /** 渠道名称（便于前端展示） */
    private String channelName;

    /** 服务商 */
    private String provider;

    /** 选中策略：PRIORITY/WEIGHT/RANDOM/FALLBACK */
    private String selectStrategy;

    /** 候选渠道列表 */
    private List<String> candidates = new ArrayList<>();

    /** 未匹配时的错误信息 */
    private String errorMsg;

    public static RouteSelectResponse fail(String logNo, String msg) {
        RouteSelectResponse r = new RouteSelectResponse();
        r.setMatched(false);
        r.setLogNo(logNo);
        r.setErrorMsg(msg);
        return r;
    }
}
