package com.houjiang.payment.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.channel.RouteListRequest;
import com.houjiang.payment.dto.channel.RouteSaveRequest;
import com.houjiang.payment.entity.PayChannelRouteEntity;

/**
 * 渠道路由规则服务
 * @author Amos
 */
public interface ChannelRouteService {

    /**
     * @param req 请求
     * @param operatorId operator主键
     * @return 处理结果
     */
    Long save(RouteSaveRequest req, String operatorId);

    /**
     * @param id 主键
     * @param status 状态
     * @param operatorId operator主键
     * @return 处理结果
     */
    boolean changeStatus(Long id, Integer status, String operatorId);

    /**
     * @param id 主键
     * @return 处理结果
     */
    boolean delete(Long id);

    /**
     * @param req 请求
     * @return 处理结果
     */
    IPage<PayChannelRouteEntity> page(RouteListRequest req);

    /**
     * @param id 主键
     * @return 处理结果
     */
    PayChannelRouteEntity detail(Long id);
}
