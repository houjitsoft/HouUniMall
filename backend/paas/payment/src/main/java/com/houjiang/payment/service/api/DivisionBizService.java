package com.houjiang.payment.service.api;

import com.houjiang.payment.entity.PayDivisionReceiverEntity;

import java.util.List;
import java.util.Map;

/**
 * 自研分账业务服务（W4 P0）。
 *
 * 与历史 DivisionService（通企付适配器）物理隔离，新接口走 /api/payment/division/biz/**。
 *
 * @author Amos
 */
public interface DivisionBizService {

    /** 1. 接收方绑定（新增分账接收方配置） */
    /**
     * @param req 请求
     * @param operatorId operator主键
     * @return 处理结果
     */
    PayDivisionReceiverEntity receiverBind(Map<String, Object> req, String operatorId);

    /** 2. 接收方列表（按商户号查接收方） */
    /**
     * @param req 请求
     * @return 处理结果
     */
    List<PayDivisionReceiverEntity> receiverList(Map<String, Object> req);

    /** 3. 接收方解绑（停用接收方） */
    /**
     * @param req 请求
     * @param operatorId operator主键
     * @return 处理结果
     */
    Boolean receiverUnbind(Map<String, Object> req, String operatorId);

    /** 4. 规则引擎计算（按商户预配置规则计算分账明细，骨架仅做万分比拆分） */
    /**
     * @param req 请求
     * @return 处理结果
     */
    List<Map<String, Object>> ruleEngineCalc(Map<String, Object> req);
}
