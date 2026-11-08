package com.houjiang.payment.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.channel.ChannelLogListRequest;
import com.houjiang.payment.dto.channel.ChannelResultReport;
import com.houjiang.payment.dto.channel.RouteSelectRequest;
import com.houjiang.payment.dto.channel.RouteSelectResponse;
import com.houjiang.payment.entity.PayChannelLogEntity;

/**
 * 渠道路由核心引擎服务
 * select：风控检查通过后，按路由规则 + 权重/优先级 选出目标渠道
 * report：渠道调用完成后回写成功/失败，更新健康度与熔断状态
 * @author Amos
 */
public interface ChannelRouteEngineService {

    /** 选择渠道 */
    /**
     * @param req 请求
     * @return 处理结果
     */
    RouteSelectResponse select(RouteSelectRequest req);

    /**
     * 退款渠道路由选择（W5+ P2-1）
     * <p>
     * 与 {@link #select} 的差异：
     * <ul>
     *   <li>强制以 bizType=2（退款）匹配规则</li>
     *   <li>必须传 originChannelCode；选中渠道必须等于原渠道（原路退回硬约束）</li>
     *   <li>原渠道在路由 candidates 中但被熔断/超限：返回 errorMsg=MANUAL_REFUND_REQUIRED 提示走人工冲正</li>
     *   <li>原渠道不在路由 candidates 中：返回 errorMsg 提示路由配置缺失</li>
     * </ul>
     */
    RouteSelectResponse selectRefund(RouteSelectRequest req);

    /** 上报调用结果 */
    /**
     * @param report 报表
     * @return 处理结果
     */
    boolean report(ChannelResultReport report);

    /** 调用日志分页 */
    /**
     * @param req 请求
     * @return 处理结果
     */
    IPage<PayChannelLogEntity> logPage(ChannelLogListRequest req);

    /** 日志详情 */
    /**
     * @param logNo 日志编号
     * @return 处理结果
     */
    PayChannelLogEntity logDetail(String logNo);
}
